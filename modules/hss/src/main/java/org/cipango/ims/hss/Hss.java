// ========================================================================
// Copyright 2008-2009 NEXCOM Systems
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

import java.security.MessageDigest;

import org.cipango.diameter.AVP;
import org.cipango.diameter.AVPList;
import org.cipango.diameter.DiameterAnswer;
import org.cipango.diameter.DiameterRequest;
import org.cipango.diameter.base.Base;
import org.cipango.diameter.ims.IMS;
import org.cipango.ims.Cx;
import org.cipango.ims.hss.auth.AuthenticationVector;
import org.cipango.ims.hss.auth.DigestAuthenticationVector;
import org.cipango.ims.hss.db.PrivateIdentityDao;
import org.cipango.ims.hss.db.SubscriptionDao;
import org.cipango.ims.hss.diameter.DiameterException;
import org.cipango.ims.hss.model.PrivateIdentity;
import org.cipango.ims.hss.model.PublicIdentity;
import org.cipango.ims.hss.model.PublicPrivate;
import org.cipango.littleims.util.HexString;
import org.springframework.transaction.annotation.Transactional;

public class Hss
{
	public static final String __ISO_8859_1 = "ISO-8859-1";
	
	private PrivateIdentityDao _privateIdentityDao;
	private SubscriptionDao _subscriptionDao;
	
	public void setPrivateIdentityDao(PrivateIdentityDao dao)
	{
		_privateIdentityDao = dao;
	}
	
	@Transactional
	public void doMar(DiameterRequest mar) throws Exception
	{
		AVPList list = mar.getAVPs();
		
		String impi = list.getString(Base.USER_NAME);
		
		if (impi == null)
			throw new DiameterException(IMS.IMS_VENDOR_ID, Base.DIAMETER_MISSING_AVP); // TODO add failed-avps
		
		String impu = list.getString(IMS.IMS_VENDOR_ID, IMS.PUBLIC_IDENTITY);

		if (impu == null)
			throw new DiameterException(IMS.IMS_VENDOR_ID, Base.DIAMETER_MISSING_AVP); // TODO add failed-avps

		PrivateIdentity privateIdentity = _privateIdentityDao.findById(impi);
		
		if (privateIdentity == null)
			throw new DiameterException(IMS.IMS_VENDOR_ID, IMS.DIAMETER_ERROR_USER_UNKNOWN);
		
		PublicIdentity publicIdentity = null;
		
		for (PublicPrivate id : privateIdentity.getPublicIdentities())
		{
			if (id.getPublicIdentity().getIdentity().equals(impu))
				publicIdentity = id.getPublicIdentity();
		}
		
		if (publicIdentity == null)
			throw new DiameterException(IMS.IMS_VENDOR_ID, IMS.DIAMETER_ERROR_IDENTITIES_DONT_MATCH);
		
		
		AVP avp = list.getAVP(IMS.IMS_VENDOR_ID, IMS.SIP_AUTH_DATA_ITEM);
		AVPList sadi = avp.getGrouped();
		
		String s = sadi.getString(IMS.IMS_VENDOR_ID, IMS.SIP_AUTHENTICATION_SCHEME);
		
		Cx.AuthenticationScheme scheme = Cx.AuthenticationScheme.get(s);
		
		if (scheme == null)
			System.out.println("Unknown scheme " + s); // TODO
		
		switch (scheme.getOrdinal())
		{
		case Cx.AuthenticationScheme.SIP_DIGEST_ORDINAL:
			
			AuthenticationVector[] authVectors = getDigestAuthVectors(1, mar.getDestinationRealm(), privateIdentity);
			
			DiameterAnswer answer = mar.createAnswer(Base.DIAMETER_SUCCESS);
			answer.getAVPs().addString(Base.USER_NAME, impi);
			
			answer.add(AVP.ofAVPs(IMS.IMS_VENDOR_ID, IMS.SIP_AUTH_DATA_ITEM, authVectors[0].asAuthItem()));
			answer.send();
			break;
		default:
			throw new DiameterException(IMS.IMS_VENDOR_ID, IMS.DIAMETER_ERROR_AUTH_SCHEME_NOT_SUPPORTED);
		}
		
		//DiameterAnswer answer = 
	}
	
	protected void doSar(DiameterRequest sar) throws Exception 
	{
		AVPList avps = sar.getAVPs();
		
		String serverName = avps.getString(IMS.IMS_VENDOR_ID, IMS.SERVER_NAME);
		if (serverName == null)
			throw new DiameterException(IMS.IMS_VENDOR_ID, Base.DIAMETER_MISSING_AVP);
		
		int serverAssignmentType = avps.getInt(IMS.IMS_VENDOR_ID, IMS.SERVER_ASSIGNMENT_TYPE);
		if (serverAssignmentType == -1)
			throw new DiameterException(IMS.IMS_VENDOR_ID, Base.DIAMETER_MISSING_AVP);
		
		int userDataAlreadyAvailable = avps.getInt(IMS.IMS_VENDOR_ID, IMS.USER_DATA_ALREADY_AVAILABLE);
		if (userDataAlreadyAvailable == -1)
			throw new DiameterException(IMS.IMS_VENDOR_ID, Base.DIAMETER_MISSING_AVP);
		
		switch (serverAssignmentType)
		{
		case Cx.ServerAssignmentType.REGISTRATION:
		case Cx.ServerAssignmentType.RE_REGISTRATION:
			
			String user = avps.getString(Base.USER_NAME);
			if (user == null)
				throw new DiameterException(IMS.IMS_VENDOR_ID, Base.DIAMETER_MISSING_AVP);
			
			String puid = avps.getString(IMS.IMS_VENDOR_ID, IMS.PUBLIC_IDENTITY);
			if (puid == null)
				throw new DiameterException(IMS.IMS_VENDOR_ID, Base.DIAMETER_MISSING_AVP);
			
			PrivateIdentity impi = _privateIdentityDao.findById(user);
			if (impi == null)
				throw new DiameterException(IMS.IMS_VENDOR_ID, IMS.DIAMETER_ERROR_USER_UNKNOWN);
			
			/*
			PublicIdentity publicIdentity = null;
			
			for (PublicIdentity id : impi.getPublicIdentities())
			{
				if (id.getIdentity().equals(puid))
					publicIdentity = id;
			}
			
			if (publicIdentity == null)
				throw new DiameterException(IMS.IMS_VENDOR_ID, IMS.DIAMETER_ERROR_IDENTITIES_DONT_MATCH);
			
			*/
			DiameterAnswer answer = sar.createAnswer(Base.DIAMETER_SUCCESS);
			answer.getAVPs().addString(Base.USER_NAME, user);
			
			
		}
		/*
		String pu = avps.getString(IMS.IMS_VENDOR_ID, IMS.PUBLIC_IDENTITY);
		if (pu == null)
			throw new DiameterException(IMS.IMS_VENDOR_ID, Base.DIAMETER_MISSING_AVP);
		
		
		PrivateIdentity impi = _privateIdentityDao.findById(user);
		if (impi == null)
			throw new DiameterException(IMS.IMS_VENDOR_ID, IMS.DIAMETER_ERROR_USER_UNKNOWN);
		
		PublicIdentity publicIdentity = null;
		
		for (PublicIdentity id : impi.getPublicIdentities())
		{
			if (id.getIdentity().equals(pu))
				publicIdentity = id;
		}
		
		if (publicIdentity == null)
			throw new DiameterException(IMS.IMS_VENDOR_ID, IMS.DIAMETER_ERROR_IDENTITIES_DONT_MATCH);
			*/
		

	}
	
	protected AuthenticationVector[] getDigestAuthVectors(int nb, String realm, PrivateIdentity identity)
	{
		DigestAuthenticationVector vector = new DigestAuthenticationVector();
		
		byte[] ha1;
		try
		{
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(identity.getIdentity().getBytes(__ISO_8859_1));
			md.update((byte)':');
			md.update(realm.getBytes(__ISO_8859_1));
			md.update((byte)':');
			md.update(identity.getPassword());
			ha1 = md.digest();
		}
		catch (Exception e)
		{
			return null; // TODO
		}
		String sha1 = HexString.bufferToHex(ha1);
		
		vector.setRealm(realm);
		vector.setHA1(sha1);
		
		return new AuthenticationVector[] {vector};
	}
}
