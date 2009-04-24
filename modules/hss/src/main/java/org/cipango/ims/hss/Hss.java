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

import org.apache.log4j.Logger;
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
import org.cipango.ims.hss.db.PublicIdentityDao;
import org.cipango.ims.hss.db.ScscfDao;
import org.cipango.ims.hss.db.SubscriptionDao;
import org.cipango.ims.hss.diameter.DiameterException;
import org.cipango.ims.hss.model.PrivateIdentity;
import org.cipango.ims.hss.model.PublicIdentity;
import org.cipango.ims.hss.model.PublicPrivate;
import org.cipango.ims.hss.model.Scscf;
import org.cipango.ims.hss.model.Subscription;
import org.cipango.ims.hss.model.PublicIdentity.State;
import org.cipango.littleims.cx.ServerAssignmentType;
import org.cipango.littleims.cx.UserAuthorizationType;
import org.cipango.littleims.util.HexString;
import org.springframework.transaction.annotation.Transactional;

public class Hss
{
	public static final String __ISO_8859_1 = "ISO-8859-1";
	private static final Logger __log = Logger.getLogger(Hss.class);
	
	private PrivateIdentityDao _privateIdentityDao;
	private PublicIdentityDao _publicIdentityDao;
	private SubscriptionDao _subscriptionDao;
	private ScscfDao _scscfDao;
	
	public void setPrivateIdentityDao(PrivateIdentityDao dao)
	{
		_privateIdentityDao = dao;
	}
	
	@Transactional
	public void doUar(DiameterRequest uar) throws Exception
	{
		AVPList avps = uar.getAVPs();
		
		String impu = getMandatoryAVP(avps, IMS.IMS_VENDOR_ID, IMS.PUBLIC_IDENTITY).getString();

		PrivateIdentity privateIdentity = 
			_privateIdentityDao.findById(getMandatoryAVP(avps, Base.USER_NAME).getString());
		
		if (privateIdentity == null)
			throw new DiameterException(IMS.IMS_VENDOR_ID, IMS.DIAMETER_ERROR_USER_UNKNOWN);
		
		PublicIdentity publicIdentity = getPublicIdentity(privateIdentity, impu);
		
		boolean emergencyReg = false;
		AVP avp = avps.getAVP(IMS.IMS_VENDOR_ID, IMS.UAR_FLAGS);
		if (avp != null)
			emergencyReg = ((avp.getInt() & 0x01) == 0x01); // FIXME bit 0 is low or hight bit???
	
		if (publicIdentity.isBarred())
		{
			if (!emergencyReg)
			{
				boolean allBarred = true;
				for (PublicPrivate publicPrivate :privateIdentity.getPublicIdentities())
				{
					if (!publicPrivate.getPublicIdentity().isBarred())
					{
						allBarred = false;
						break;
					}
				}
				if (allBarred)
				{
					__log.debug("LIR: publicIdentity " + publicIdentity.getIdentity() 
							+ " is barred, emergency flag is not set and all associated " +
									"public identities are barred, so send DIAMETER_AUTHORIZATION_REJECTED.");
					throw new DiameterException(Base.DIAMETER_AUTHORIZATION_REJECTED);
				}
			}
		}
		
		int userAuthorizationType = avps.getInt(IMS.IMS_VENDOR_ID, IMS.USER_AUTHORIZATION_TYPE);
		if (userAuthorizationType == -1 || userAuthorizationType == UserAuthorizationType.REGISTRATION)
		{
			if (!emergencyReg)
			{
				// TODO check if roaming is allowed
			}
			
		} 
		else if (userAuthorizationType == UserAuthorizationType.REGISTRATION_AND_CAPABILITIES)
		{
			// TODO add support to userAuthorizationType = REGISTRATION_AND_CAPABILITIES
		}
		
		Subscription subscription = privateIdentity.getSubscription();
		DiameterAnswer answer;
		if (publicIdentity.getState() == State.REGISTERED)
		{
			if (userAuthorizationType == -1 || userAuthorizationType == UserAuthorizationType.REGISTRATION)
			{
				answer = uar.createAnswer(IMS.IMS_VENDOR_ID, IMS.DIAMETER_SUBSEQUENT_REGISTRATION);
			}
			else
			{
				// case UserAuthorizationType.DE_REGISTRATION
				answer = uar.createAnswer(Base.DIAMETER_SUCCESS);
			}
			answer.add(AVP.ofString(IMS.IMS_VENDOR_ID, IMS.SERVER_NAME, 
					subscription.getScscf().getUri()));
		}
		else if (publicIdentity.getState() == State.UNREGISTERED)
		{
			if (userAuthorizationType == -1 || userAuthorizationType == UserAuthorizationType.REGISTRATION)
			{
				answer = uar.createAnswer(IMS.IMS_VENDOR_ID, IMS.DIAMETER_SUBSEQUENT_REGISTRATION);
			}
			else
			{
				// case UserAuthorizationType.DE_REGISTRATION
				answer = uar.createAnswer(Base.DIAMETER_SUCCESS);
			}
			answer.add(AVP.ofString(IMS.IMS_VENDOR_ID, IMS.SERVER_NAME, 
					subscription.getScscf().getUri()));
		}
		else
		{
			if (userAuthorizationType == UserAuthorizationType.DE_REGISTRATION)
			{
				throw new DiameterException(IMS.IMS_VENDOR_ID, IMS.DIAMETER_ERROR_IDENTITY_NOT_REGISTERED);
			}
			else
			{
				if (subscription.getScscf() != null)
				{
					answer = uar.createAnswer(IMS.IMS_VENDOR_ID, IMS.DIAMETER_SUBSEQUENT_REGISTRATION);
					answer.add(AVP.ofString(IMS.IMS_VENDOR_ID, IMS.SERVER_NAME, 
							subscription.getScscf().getUri()));
				}
				else
				{
					Scscf scscf = _scscfDao.findAvailableScscf();
					if (scscf == null)
					{
						__log.warn("Coud not found any available SCSCF for public identity: " 
								+ publicIdentity.getIdentity());
						throw new DiameterException(Base.DIAMETER_UNABLE_TO_COMPLY);
					}
					subscription.setScscf(scscf);
					_subscriptionDao.save(subscription);
					answer = uar.createAnswer(IMS.IMS_VENDOR_ID, IMS.DIAMETER_FIRST_REGISTRATION);
					answer.add(AVP.ofString(IMS.IMS_VENDOR_ID, IMS.SERVER_NAME, 
							scscf.getUri()));
				}
			}
		}
		answer.send();
	}
	
	
	
	@Transactional
	public void doMar(DiameterRequest mar) throws Exception
	{
		AVPList avps = mar.getAVPs();
		
		String impi = getMandatoryAVP(avps, Base.USER_NAME).getString();
		
		String impu = getMandatoryAVP(avps, IMS.IMS_VENDOR_ID, IMS.PUBLIC_IDENTITY).getString();

		PrivateIdentity privateIdentity = _privateIdentityDao.findById(impi);
		
		if (privateIdentity == null)
			throw new DiameterException(IMS.IMS_VENDOR_ID, IMS.DIAMETER_ERROR_USER_UNKNOWN);
		
		PublicIdentity publicIdentity = getPublicIdentity(privateIdentity, impu);
		
		
		AVP avp = avps.getAVP(IMS.IMS_VENDOR_ID, IMS.SIP_AUTH_DATA_ITEM);
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
	
	private PublicIdentity getPublicIdentity(PrivateIdentity privateIdentity, String impu) throws DiameterException
	{
		for (PublicPrivate id : privateIdentity.getPublicIdentities())
		{
			if (id.getPublicIdentity().getIdentity().equals(impu))
				return id.getPublicIdentity();
		}
		
		throw new DiameterException(IMS.IMS_VENDOR_ID, IMS.DIAMETER_ERROR_IDENTITIES_DONT_MATCH);
	}
	
	@Transactional
	public void doSar(DiameterRequest sar) throws Exception 
	{
		// See 3GPP TS 29-228 §6.1.2.1
		AVPList avps = sar.getAVPs();
		
		String impi = avps.getString(Base.USER_NAME);
		
		String impu = getMandatoryAVP(avps, IMS.IMS_VENDOR_ID, IMS.PUBLIC_IDENTITY).getString();
		
		int serverAssignmentType = getMandatoryAVP(avps, IMS.IMS_VENDOR_ID, IMS.SERVER_ASSIGNMENT_TYPE).getInt();

		// TODO wilcard
		// TODO use list for public identity
		PublicIdentity publicIdentity = null;
		PrivateIdentity privateIdentity;
		if (impu != null)
		{
			publicIdentity = _publicIdentityDao.findById(impu);
			if (publicIdentity == null)
				throw new DiameterException(IMS.IMS_VENDOR_ID, IMS.DIAMETER_ERROR_USER_UNKNOWN);
		}
		
		if (impi != null)
		{
			privateIdentity = _privateIdentityDao.findById(impi);
			if (privateIdentity == null)
				throw new DiameterException(IMS.IMS_VENDOR_ID, IMS.DIAMETER_ERROR_USER_UNKNOWN);
		}
		else if (publicIdentity != null)
		{
			privateIdentity = publicIdentity.getPrivateIdentities().iterator().next().getPrivateIdentity();
			impi = privateIdentity.getIdentity();
		}
		else
			throw new DiameterException(IMS.IMS_VENDOR_ID, Base.DIAMETER_MISSING_AVP);
		
		if (publicIdentity == null && privateIdentity != null)
		{
			publicIdentity = privateIdentity.getPublicIdentities().iterator().next().getPublicIdentity();
			impu = publicIdentity.getIdentity();
		}
		else
			throw new DiameterException(IMS.IMS_VENDOR_ID, Base.DIAMETER_MISSING_AVP);
		
		if (impu == null 
				&& serverAssignmentType != ServerAssignmentType.TIMEOUT_DEREGISTRATION
				&& serverAssignmentType != ServerAssignmentType.USER_DEREGISTRATION
				&& serverAssignmentType != ServerAssignmentType.DEREGISTRATION_TOO_MUCH_DATA
				&& serverAssignmentType != ServerAssignmentType.TIMEOUT_DEREGISTRATION_STORE_SERVER_NAME
				&& serverAssignmentType != ServerAssignmentType.USER_DEREGISTRATION_STORE_SERVER_NAME 
				&& serverAssignmentType != ServerAssignmentType.ADMINISTRATIVE_DEREGISTRATION)
			throw new DiameterException(IMS.IMS_VENDOR_ID, Base.DIAMETER_MISSING_AVP);
		
		if (impi == null && serverAssignmentType != ServerAssignmentType.UNREGISTERED_USER)
			throw new DiameterException(IMS.IMS_VENDOR_ID, Base.DIAMETER_MISSING_AVP);
		
			
		String serverName = getMandatoryAVP(avps, IMS.IMS_VENDOR_ID, IMS.SERVER_NAME).getString();
						
		int userDataAlreadyAvailable = 
			getMandatoryAVP(avps, IMS.IMS_VENDOR_ID, IMS.USER_DATA_ALREADY_AVAILABLE).getInt();
		
		//TODO if (IdentityType.WILDCARDED_PSI == publicIdentity.getIdentityType()
		//		&& publicIdentity.getPSI Activation State )
		
		Subscription subscription = privateIdentity.getSubscription();
		DiameterAnswer answer = sar.createAnswer(Base.DIAMETER_SUCCESS);
		switch (serverAssignmentType)
		{
		case ServerAssignmentType.REGISTRATION:
		case ServerAssignmentType.RE_REGISTRATION:
			Scscf scscf =  subscription.getScscf();
			if (scscf != null && !serverName.equals(scscf.getUri()))
			{
				AVP avp = AVP.ofString(IMS.IMS_VENDOR_ID, IMS.SERVER_NAME, scscf.getUri());
				throw new DiameterException(IMS.IMS_VENDOR_ID, IMS.DIAMETER_ERROR_IDENTITY_ALREADY_REGISTERED).addAvp(avp);
			}
			publicIdentity.setState(State.REGISTERED);
			answer.getAVPs().addString(Base.USER_NAME, impi);
			String serviceProfile = privateIdentity.toXml();
			answer.getAVPs().addString(IMS.IMS_VENDOR_ID, IMS.USER_DATA, serviceProfile);
			AVPList associatedIds = new AVPList();
			for (String identity : subscription.getPrivateIds())
			{
				if (!impi.equals(identity))
					associatedIds.addString(Base.USER_NAME, identity);
			}
			if (!associatedIds.isEmpty())
				answer.getAVPs().add(AVP.ofAVPs(IMS.IMS_VENDOR_ID, IMS.ASSOCIATED_IDENTITIES, associatedIds));
			break;
			
		case ServerAssignmentType.UNREGISTERED_USER:
			scscf =  subscription.getScscf();
			if (scscf != null && !serverName.equals(scscf.getUri()))
			{
				AVP avp = AVP.ofString(IMS.IMS_VENDOR_ID, IMS.SERVER_NAME, scscf.getUri());
				throw new DiameterException(IMS.IMS_VENDOR_ID, IMS.DIAMETER_ERROR_IDENTITY_ALREADY_REGISTERED).addAvp(avp);
			}
			else
			{
				scscf = _scscfDao.findByUri(serverName);
				if (scscf == null)
					throw new IllegalArgumentException("Could not find S-CSCF with URI: " + serverName);
				subscription.setScscf(scscf);
			}
			
			if (State.NOT_REGISTERED == publicIdentity.getState() || State.REGISTERED == publicIdentity.getState())
				publicIdentity.setState(State.UNREGISTERED);
			
			answer.getAVPs().addString(Base.USER_NAME, privateIdentity.getIdentity());
			
			serviceProfile = privateIdentity.toXml();
			answer.getAVPs().addString(IMS.IMS_VENDOR_ID, IMS.USER_DATA, serviceProfile);
			
			associatedIds = new AVPList();
			for (String identity : subscription.getPrivateIds())
			{
				if (!impi.equals(identity))
					associatedIds.addString(Base.USER_NAME, identity);
			}
			if (!associatedIds.isEmpty())
				answer.getAVPs().add(AVP.ofAVPs(IMS.IMS_VENDOR_ID, IMS.ASSOCIATED_IDENTITIES, associatedIds));

			break;
			
		case ServerAssignmentType.TIMEOUT_DEREGISTRATION:
		case ServerAssignmentType.USER_DEREGISTRATION:
		case ServerAssignmentType.DEREGISTRATION_TOO_MUCH_DATA:
		case ServerAssignmentType.ADMINISTRATIVE_DEREGISTRATION:
			if (State.REGISTERED == publicIdentity.getState())
			{
				if (publicIdentity.getPrivateIdentities().size() == 1)
				{
					publicIdentity.setState(State.NOT_REGISTERED);
					checkClearScscf(subscription);
				}
				// FIXME end
			}
			break;
			
		default:
			throw new IllegalArgumentException("Unsuported ServerAssignmentType: " + serverAssignmentType);
		}
		answer.send();
	}
	
	private void checkClearScscf(Subscription subscription)
	{
		boolean activePublic = false;
		for (PublicIdentity publicIdentity : subscription.getPublicIdentities())
		{
			if (State.NOT_REGISTERED != publicIdentity.getState())
				activePublic = true;
		}
		if (!activePublic)
			subscription.setScscf(null);
	}
	
	private static AVP getMandatoryAVP(AVPList avps, int code) throws DiameterException
	{
		AVP avp = avps.getAVP(code);
		if (avp == null)
			throw new DiameterException(IMS.IMS_VENDOR_ID, Base.DIAMETER_MISSING_AVP); // TODO add failed-avps
		return avp;
	}
	
	private static AVP getMandatoryAVP(AVPList avps, int vendorId, int code) throws DiameterException
	{
		AVP avp = avps.getAVP(vendorId, code);
		if (avp == null)
			throw new DiameterException(IMS.IMS_VENDOR_ID, Base.DIAMETER_MISSING_AVP);
		return avp;
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

	public void setSubscriptionDao(SubscriptionDao subscriptionDao)
	{
		_subscriptionDao = subscriptionDao;
	}

	public void setScscfDao(ScscfDao scscfDao)
	{
		_scscfDao = scscfDao;
	}

	public void setPublicIdentityDao(PublicIdentityDao publicIdentityDao)
	{
		_publicIdentityDao = publicIdentityDao;
	}
}
