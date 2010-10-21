// ========================================================================
// Copyright 2010 NEXCOM Systems
// ------------------------------------------------------------------------
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at 
// http://www.apache.org/licenses/LICENSE-2.0
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
// ========================================================================
package org.cipango.ims.hss;

import java.util.Iterator;

import org.cipango.diameter.AVPList;
import org.cipango.diameter.api.DiameterServletAnswer;
import org.cipango.diameter.api.DiameterServletRequest;
import org.cipango.diameter.base.Common;
import org.cipango.diameter.ims.Cx;
import org.cipango.diameter.ims.Sh;
import org.cipango.diameter.ims.Zh;
import org.cipango.ims.hss.auth.AuthenticationVector;
import org.cipango.ims.hss.db.PrivateIdentityDao;
import org.cipango.ims.hss.db.PublicIdentityDao;
import org.cipango.ims.hss.diameter.DiameterException;
import org.cipango.ims.hss.model.PrivateIdentity;
import org.cipango.ims.hss.model.PublicIdentity;
import org.cipango.ims.hss.model.PublicUserIdentity;
import org.cipango.ims.hss.util.XML;
import org.springframework.transaction.annotation.Transactional;

public class ZhHandler
{
	private PublicIdentityDao _publicIdentityDao;
	private PrivateIdentityDao _privateIdentityDao;
	private long _keyLifetime = 86400;
	private Hss _hss;

	@Transactional
	public void doMar(DiameterServletRequest mar) throws Exception
	{
		AVPList avps = mar.getAVPs();

		PrivateIdentity privateIdentity;
		String impi = mar.get(Common.USER_NAME);
		String impu = mar.get(Cx.PUBLIC_IDENTITY);
		if (impi == null)
		{
			if (impu == null)
				throw DiameterException.newMissingDiameterAvp(Cx.PUBLIC_IDENTITY);
			PublicIdentity publicIdentity = _publicIdentityDao.findById(impu);
			if (publicIdentity == null || !(publicIdentity instanceof PublicUserIdentity))
				throw new DiameterException(Zh.DIAMETER_ERROR_IDENTITY_UNKNOWN, 
						"Could not found private identity with IMPU: " + impu);
			PublicUserIdentity publicId = (PublicUserIdentity) publicIdentity;
			Iterator<PrivateIdentity> it = publicId.getPrivateIdentities().iterator();
			privateIdentity = it.next();
			impi = privateIdentity.getIdentity();
			if (it.hasNext())
				throw new DiameterException(Sh.DIAMETER_ERROR_OPERATION_NOT_ALLOWED, 
						"the IMPU: " + impu + " is shared with multiple IMPIs");			
		}
		else
		{
			privateIdentity = _privateIdentityDao.findById(impi);
			if (privateIdentity == null)
				throw new DiameterException(Zh.DIAMETER_ERROR_IDENTITY_UNKNOWN, 
						"Could not found private identity with IMPI: " + impi);
			
		}

		
		AVPList sadi =  avps.getValue(Cx.SIP_AUTH_DATA_ITEM);
		
		DiameterServletAnswer answer = mar.createAnswer(Common.DIAMETER_SUCCESS);
		answer.add(Common.USER_NAME, impi);
		if (impu != null)
			answer.add(Cx.PUBLIC_IDENTITY, impu);
		
		if (sadi != null)
		{
			byte[] sipAuthorization = sadi.getValue(Cx.SIP_AUTHORIZATION);
			if (sipAuthorization != null)
				_hss.procesResynchronisation(sipAuthorization, privateIdentity);
		}

		AuthenticationVector[] authVectors = _hss.getAkaAuthVectors(1, privateIdentity);			
		answer.getAVPs().add(authVectors[0].asAuthItem());
		answer.add(Zh.GBA_USER_SEC_SETTINGS, privateIdentity.getGuss(_keyLifetime, XML.getDefault().newOutput()));
		answer.send();
	}

	public PublicIdentityDao getPublicIdentityDao()
	{
		return _publicIdentityDao;
	}

	public void setPublicIdentityDao(PublicIdentityDao publicIdentityDao)
	{
		_publicIdentityDao = publicIdentityDao;
	}

	public PrivateIdentityDao getPrivateIdentityDao()
	{
		return _privateIdentityDao;
	}

	public void setPrivateIdentityDao(PrivateIdentityDao privateIdentityDao)
	{
		_privateIdentityDao = privateIdentityDao;
	}

	public long getKeyLifetime()
	{
		return _keyLifetime;
	}

	public void setKeyLifetime(long keyLifetime)
	{
		_keyLifetime = keyLifetime;
	}

	public Hss getHss()
	{
		return _hss;
	}

	public void setHss(Hss hss)
	{
		_hss = hss;
	}
}
