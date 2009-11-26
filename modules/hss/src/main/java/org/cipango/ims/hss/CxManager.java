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
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.wicket.util.string.Strings;
import org.cipango.diameter.AVP;
import org.cipango.diameter.AVPList;
import org.cipango.diameter.DiameterCommand;
import org.cipango.diameter.DiameterFactory;
import org.cipango.diameter.DiameterRequest;
import org.cipango.diameter.base.Base;
import org.cipango.diameter.ims.Cx;
import org.cipango.diameter.ims.Sh;
import org.cipango.diameter.ims.Cx.ReasonCode;
import org.cipango.ims.hss.db.PublicIdentityDao;
import org.cipango.ims.hss.db.SubscriptionDao;
import org.cipango.ims.hss.model.ApplicationServer;
import org.cipango.ims.hss.model.ImplicitRegistrationSet;
import org.cipango.ims.hss.model.InitialFilterCriteria;
import org.cipango.ims.hss.model.PSI;
import org.cipango.ims.hss.model.PrivateIdentity;
import org.cipango.ims.hss.model.PublicIdentity;
import org.cipango.ims.hss.model.PublicUserIdentity;
import org.cipango.ims.hss.model.Scscf;
import org.cipango.ims.hss.model.ServiceProfile;
import org.cipango.ims.hss.model.SpIfc;
import org.cipango.ims.hss.model.Subscription;
import org.cipango.ims.hss.model.ImplicitRegistrationSet.State;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

public class CxManager
{
	private static final Logger __log = Logger.getLogger(CxManager.class);
	private DiameterFactory _diameterFactory;
	private String _scscfRealm;
	private Set<String> _publicIdsToUpdate = new HashSet<String>();
	private PublicIdentityDao _publicIdentityDao;
	private SubscriptionDao _subscriptionDao;
	
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
			
		
		DiameterRequest request = newRequest(Cx.PPR, scscf);
		String privateIdentity = getPrivateIdentity(publicIdentity);		
		request.add(Base.USER_NAME, privateIdentity);
		String serviceProfile = publicIdentity.getImsSubscriptionAsXml(privateIdentity, null, false);
		request.getAVPs().add(Sh.USER_DATA, serviceProfile.getBytes());
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
	
	/**
	 * 
	 * <pre>
	 *  <Registration-Termination-Request> ::= < Diameter Header: 304, REQ, PXY, 16777216 >
	 *   < Session-Id >
	 *   { Vendor-Specific-Application-Id }
	 *   { Auth-Session-State }
	 *   { Origin-Host }
	 *   { Origin-Realm }
	 *   { Destination-Host }
	 *   { Destination-Realm }
	 *   { User-Name }
	 *   [ Associated-Identities ]
	 *  *[ Supported-Features ]
	 *  *[ Public-Identity ]
	 *   { Deregistration-Reason }
	 *  *[ AVP ]
	 *  *[ Proxy-Info ]
	 *  *[ Route-Record ]
	 * </pre>
	 * @param publicIdentity
	 * @return
	 * @throws IOException 
	 */
	@Transactional (readOnly = false, propagation = Propagation.REQUIRES_NEW)
	public void sendRtr(Collection<PublicIdentity> publicIdentities, ReasonCode reasonCode, String reasonPhrase) throws IOException
	{
		
		Scscf scscf = publicIdentities.iterator().next().getScscf();
		Subscription subscription = null;
		if (scscf == null)
			throw new IOException("No S-CSCF assigned to " + publicIdentities);
		
		DiameterRequest request = newRequest(Cx.RTR, scscf);
		for (PublicIdentity publicIdentity : publicIdentities)
			request.add(Cx.PUBLIC_IDENTITY, publicIdentity.getIdentity());
		
		String privateIdentity = getPrivateIdentity(publicIdentities.iterator().next());		
		request.add(Base.USER_NAME, privateIdentity);
		request.getAVPs().add(getDeregistrationReason(reasonCode, reasonPhrase));
		
		request.send();
		
		Iterator<PublicIdentity> it2 = publicIdentities.iterator();
		while (it2.hasNext())
		{
			PublicIdentity publicIdentity = it2.next();
			if (publicIdentity instanceof PublicUserIdentity)
			{
				PublicUserIdentity publicUserIdentity = (PublicUserIdentity) publicIdentity;
				subscription = publicUserIdentity.getPrivateIdentities().iterator().next().getSubscription();
				publicUserIdentity.getImplicitRegistrationSet().deregister();
			}
		}
		checkClearScscf(subscription);
	}
	
	private AVP<AVPList> getDeregistrationReason(ReasonCode reasonCode, String reasonPhrase)
	{
		AVP<AVPList> avpList = new AVP<AVPList>(Cx.DERISTRATION_REASON, new AVPList());
		avpList.getValue().add(Cx.REASON_CODE, reasonCode);
		if (!Strings.isEmpty(reasonPhrase))
			avpList.getValue().add(Cx.REASON_INFO, reasonPhrase);
		
		return avpList;
	}
	
	@Transactional (readOnly = false, propagation = Propagation.REQUIRES_NEW)
	public void sendRtrPrivate(Collection<PrivateIdentity> privateIdentities, ReasonCode reasonCode, String reasonPhrase) throws IOException
	{
		Iterator<PrivateIdentity> it = privateIdentities.iterator();
		PrivateIdentity privateIdentity = it.next();
		Scscf scscf = privateIdentity.getSubscription().getScscf();
		
		if (scscf == null)
			throw new IOException("No S-CSCF assigned to the subscription " + privateIdentity.getSubscription().getName());
		
		DiameterRequest request = newRequest(Cx.RTR, scscf);
			
		
		request.add(Base.USER_NAME, privateIdentity.getIdentity());
		request.getAVPs().add(getDeregistrationReason(reasonCode, reasonPhrase));
		
		if (it.hasNext())
		{
			AVPList l = new AVPList();
			while (it.hasNext())
				l.add(Base.USER_NAME, it.next().getIdentity());
			request.add(Cx.ASSOCIATED_IDENTITIES, l);
		}
		
		request.send();
		
		for (PrivateIdentity privateId : privateIdentities)
		{
			Iterator<PublicUserIdentity> it2 = privateId.getPublicIdentities().iterator();
			while (it2.hasNext())
				it2.next().updateState(privateId.getIdentity(), State.NOT_REGISTERED);
		}
		checkClearScscf(privateIdentity.getSubscription());
	}
	
	private void checkClearScscf(Subscription subscription)
	{
		boolean activePublic = false;
		for (PublicIdentity publicId : subscription.getPublicIdentities())
		{
			Short state = publicId.getState();
			if (State.NOT_REGISTERED != state)
				activePublic = true;
		}
		if (!activePublic)
			subscription.setScscf(null);
		_subscriptionDao.save(subscription);
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
	
	private DiameterRequest newRequest(DiameterCommand command, Scscf scscf)
	{
		DiameterRequest request =  _diameterFactory.createRequest(Cx.CX_APPLICATION_ID, 
				command, _scscfRealm, scscf.getDiameterHost());
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
	
	public Set<String> getPublicIdsToUpdateAsString()
	{
		return _publicIdsToUpdate;
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

	public SubscriptionDao getSubscriptionDao()
	{
		return _subscriptionDao;
	}

	public void setSubscriptionDao(SubscriptionDao subscriptionDao)
	{
		_subscriptionDao = subscriptionDao;
	}
	
}
