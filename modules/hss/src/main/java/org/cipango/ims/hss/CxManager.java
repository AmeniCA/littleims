// ========================================================================
// Copyright 2009 NEXCOM Systems
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

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.cipango.diameter.AVP;
import org.cipango.diameter.ApplicationId;
import org.cipango.diameter.DiameterFactory;
import org.cipango.diameter.DiameterRequest;
import org.cipango.diameter.base.Base;
import org.cipango.diameter.ims.IMS;
import org.cipango.ims.hss.db.PublicIdentityDao;
import org.cipango.ims.hss.model.ApplicationServer;
import org.cipango.ims.hss.model.ImplicitRegistrationSet;
import org.cipango.ims.hss.model.InitialFilterCriteria;
import org.cipango.ims.hss.model.PSI;
import org.cipango.ims.hss.model.PublicIdentity;
import org.cipango.ims.hss.model.PublicUserIdentity;
import org.cipango.ims.hss.model.Scscf;
import org.cipango.ims.hss.model.ServiceProfile;
import org.cipango.ims.hss.model.SpIfc;
import org.cipango.ims.hss.model.ImplicitRegistrationSet.State;

public class CxManager
{
	private static final Logger __log = Logger.getLogger(CxManager.class);
	private DiameterFactory _diameterFactory;
	private String _scscfRealm;
	private Set<String> _publicIdsToUpdate = new HashSet<String>();
	private PublicIdentityDao _publicIdentityDao;
	
	/**
	 * <pre>
	 *   < Push-Profile-Request > ::= < Diameter Header: 305, REQ, PXY, 16777216 >
	 *   < Session-Id >
	 *   { Vendor-Specific-Application-Id }
	 *   { Auth-Session-State }
	 *   { Origin-Host }
	 *   { Origin-Realm }
	 *   { Destination-Host }
	 *   { Destination-Realm }
	 *   { User-Name }
	 *  *[ Supported-Features ]
	 *   [ User-Data ]
	 *   [ Charging-Information ]
	 *   [ SIP-Auth-Data-Item ]
	 *  *[ AVP ]
	 *  *[ Proxy-Info ]
	 *  *[ Route-Record ]
	 * </pre>
	 * @throws IOException 
	 */
	public void sendPpr(PublicIdentity publicIdentity) throws IOException
	{
		Scscf scscf = publicIdentity.getScscf();
		
		if (scscf == null)
		{
			__log.info("No S-CSCF assigned to " + publicIdentity + ", could not send PPR request");
			_publicIdsToUpdate.remove(publicIdentity.getIdentity());
			return;
		}
		
		if (!_publicIdsToUpdate.contains(publicIdentity.getIdentity()))
		{
			__log.info("The public identity " + publicIdentity + " has been already updated.");
			return;
		}
			
		
		DiameterRequest request = newRequest(IMS.PPR, scscf);
		String privateIdentity = getPrivateIdentity(publicIdentity);		
		request.add(AVP.ofString(Base.USER_NAME, privateIdentity));
		String serviceProfile = publicIdentity.getImsSubscriptionAsXml(privateIdentity, null, false);
		request.getAVPs().addString(IMS.IMS_VENDOR_ID, IMS.USER_DATA, serviceProfile);
		request.send();
		
		if (publicIdentity instanceof PublicUserIdentity)
		{
			ImplicitRegistrationSet set = ((PublicUserIdentity) publicIdentity).getImplicitRegistrationSet();
			for (PublicIdentity publicId : set.getPublicIdentities())
				_publicIdsToUpdate.remove(publicId.getIdentity());
		}
		else
			_publicIdsToUpdate.remove(publicIdentity.getIdentity());
	}
	
	private String getPrivateIdentity(PublicIdentity publicIdentity)
	{
		if (publicIdentity instanceof PublicUserIdentity)
		{
			PublicUserIdentity publicUserIdentity = (PublicUserIdentity) publicIdentity;
			String privateIdentity = 
				publicUserIdentity.getImplicitRegistrationSet().getRegisteredPrivateIdentity();
			if (privateIdentity == null)
				privateIdentity = publicUserIdentity.getPrivateIdentities().iterator().next().getIdentity();
			return privateIdentity;
		}
		else
		{
			return ((PSI) publicIdentity).getPrivateServiceIdentity();
		}
	}
	
	private DiameterRequest newRequest(int command, Scscf scscf)
	{
		ApplicationId appId = new ApplicationId(ApplicationId.Type.Auth, IMS.CX_APPLICATION_ID, IMS.IMS_VENDOR_ID);
		DiameterRequest request =  _diameterFactory.createRequest(appId, command, _scscfRealm, scscf.getDiameterHost());
		return request;
	}
	
	public DiameterFactory getDiameterFactory()
	{
		return _diameterFactory;
	}
	public void setDiameterFactory(DiameterFactory diameterFactory)
	{
		_diameterFactory = diameterFactory;
	}
	
	public Set<PublicIdentity> getPublicIdsToUpdate()
	{
		Set<PublicIdentity> set = new HashSet<PublicIdentity>();
		for (String identity : _publicIdsToUpdate)
		{
			PublicIdentity publicIdentity = _publicIdentityDao.findById(identity);
			if (publicIdentity == null)
				__log.warn("Could not found public identity " + publicIdentity);
			else
				set.add(publicIdentity);
		}
		return set;
	}
	
	public int getNbPublicIdsToUpdate()
	{
		return _publicIdsToUpdate.size();
	}
	
	public void identityUpdated(PublicIdentity publicIdentity)
	{		
		if (State.NOT_REGISTERED != publicIdentity.getState() && publicIdentity.getScscf() !=  null)
			_publicIdsToUpdate.add(publicIdentity.getIdentity());
	}
	
	public void profileUpdated(ServiceProfile serviceProfile)
	{
		for (PublicIdentity publicId : serviceProfile.getPublicIdentites())
			identityUpdated(publicId);
	}
	
	public void ifcUpdated(InitialFilterCriteria ifc)
	{
		for (SpIfc spIfc : ifc.getServiceProfiles())
			profileUpdated(spIfc.getServiceProfile());
	}
	
	public void applicationServerUpdated(ApplicationServer as)
	{
		for (InitialFilterCriteria ifc : as.getIfcs())
			ifcUpdated(ifc);
		for (PSI psi : as.getPsis())
			identityUpdated(psi);
	}

	public String getScscfRealm()
	{
		return _scscfRealm;
	}

	public void setScscfRealm(String scscfRealm)
	{
		_scscfRealm = scscfRealm;
	}

	public PublicIdentityDao getPublicIdentityDao()
	{
		return _publicIdentityDao;
	}

	public void setPublicIdentityDao(PublicIdentityDao publicIdentityDao)
	{
		_publicIdentityDao = publicIdentityDao;
	}
	
}
