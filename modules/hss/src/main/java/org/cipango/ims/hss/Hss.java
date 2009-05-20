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

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.util.Random;

import org.apache.log4j.Logger;
import org.cipango.diameter.AVP;
import org.cipango.diameter.AVPList;
import org.cipango.diameter.DiameterAnswer;
import org.cipango.diameter.DiameterRequest;
import org.cipango.diameter.base.Base;
import org.cipango.diameter.ims.IMS;
import org.cipango.ims.Cx;
import org.cipango.ims.hss.auth.AkaAuthenticationVector;
import org.cipango.ims.hss.auth.AuthenticationVector;
import org.cipango.ims.hss.auth.DigestAuthenticationVector;
import org.cipango.ims.hss.auth.Milenage;
import org.cipango.ims.hss.db.PrivateIdentityDao;
import org.cipango.ims.hss.db.PublicIdentityDao;
import org.cipango.ims.hss.db.ScscfDao;
import org.cipango.ims.hss.db.SubscriptionDao;
import org.cipango.ims.hss.diameter.DiameterException;
import org.cipango.ims.hss.model.PSI;
import org.cipango.ims.hss.model.PrivateIdentity;
import org.cipango.ims.hss.model.PublicIdentity;
import org.cipango.ims.hss.model.PublicUserIdentity;
import org.cipango.ims.hss.model.RegistrationState;
import org.cipango.ims.hss.model.Scscf;
import org.cipango.ims.hss.model.Subscription;
import org.cipango.ims.hss.model.ImplicitRegistrationSet.State;
import org.cipango.ims.hss.model.PublicIdentity.IdentityType;
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
	private SequenceNumberManager _sequenceNumberManager = new SequenceNumberManager();
	private Random _random = new Random();
	
	public void setPrivateIdentityDao(PrivateIdentityDao dao)
	{
		_privateIdentityDao = dao;
	}
	
	@Transactional
	public void doLir(DiameterRequest lir) throws Exception
	{
		AVPList avps = lir.getAVPs();
		String impu = getMandatoryAVP(avps, IMS.IMS_VENDOR_ID, IMS.PUBLIC_IDENTITY).getString();
		
		PublicIdentity publicIdentity = getPublicIdentity(impu, null);
		
		if (publicIdentity instanceof PSI)
		{
			PSI psi = (PSI) publicIdentity;
			if (!psi.isPsiActivation())
				throw new DiameterException(IMS.IMS_VENDOR_ID, IMS.DIAMETER_ERROR_USER_UNKNOWN,
						"PSI: " + impu + " has PSI Activation State set to inactive");
			
			// support to direct routing to AS
			if (psi.getApplicationServer() != null
					&& avps.getAVP(IMS.IMS_VENDOR_ID, IMS.ORIGININATING_REQUEST) == null)
			{
				DiameterAnswer lia = lir.createAnswer(Base.DIAMETER_SUCCESS);
				lia.add(AVP.ofString(IMS.IMS_VENDOR_ID, IMS.SERVER_NAME, psi.getApplicationServer().getServerName()));
				lia.send();
				return;
			}
			
			int userAuthorizationType = avps.getInt(IMS.IMS_VENDOR_ID, IMS.USER_AUTHORIZATION_TYPE);
			if (userAuthorizationType == UserAuthorizationType.REGISTRATION_AND_CAPABILITIES)
			{
				// TODO support Server-Capabilities 
			}
		}

		Scscf scscf = publicIdentity.getScscf();
		Short state = publicIdentity.getState();
		if (state != State.REGISTERED)
		{
			if (avps.getAVP(IMS.IMS_VENDOR_ID, IMS.ORIGININATING_REQUEST) != null
					|| publicIdentity.getServiceProfile().hasUnregisteredServices())
			{
				if (scscf == null)
				{
					scscf = _scscfDao.findAvailableScscf();
					if (scscf == null)
						throw new DiameterException(Base.DIAMETER_UNABLE_TO_COMPLY, 
								"Coud not found any available SCSCF for public identity: " 
								+ publicIdentity.getIdentity());
					publicIdentity.setScscf(scscf);
				}	
			}
			else
			{
				lir.createAnswer(IMS.IMS_VENDOR_ID, IMS.DIAMETER_ERROR_IDENTITY_NOT_REGISTERED).send();
				return;
			}
		}
		DiameterAnswer lia = lir.createAnswer(Base.DIAMETER_SUCCESS);
		lia.add(AVP.ofString(IMS.IMS_VENDOR_ID, IMS.SERVER_NAME, scscf.getUri()));
		if (publicIdentity.getIdentityType() == IdentityType.WILDCARDED_IMPU)
			lia.add(AVP.ofString(IMS.IMS_VENDOR_ID, IMS.WILCARDED_IMPU, publicIdentity.getIdentity()));
		if (publicIdentity.getIdentityType() == IdentityType.WILDCARDED_PSI)
			lia.add(AVP.ofString(IMS.IMS_VENDOR_ID, IMS.WILCARDED_PSI, publicIdentity.getIdentity()));

		lia.send();
		
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
				for (PublicUserIdentity publicUserIdentity :privateIdentity.getPublicIdentities())
				{
					if (!publicUserIdentity.isBarred())
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
		Short state = publicIdentity.getState();	
		if (State.REGISTERED == state)
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
		else if (State.UNREGISTERED == state)
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
				throw new DiameterException(IMS.IMS_VENDOR_ID, IMS.DIAMETER_ERROR_IDENTITY_NOT_REGISTERED,
						"Public identity " + impu + " is not registered");
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
						throw new DiameterException(Base.DIAMETER_UNABLE_TO_COMPLY, 
								"Coud not found any available SCSCF for public identity: " 
								+ publicIdentity.getIdentity());

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
			throw new DiameterException(IMS.IMS_VENDOR_ID, 
					IMS.DIAMETER_ERROR_AUTH_SCHEME_NOT_SUPPORTED, 
					"Unknown scheme: " + s);

		DiameterAnswer answer = mar.createAnswer(Base.DIAMETER_SUCCESS);
		answer.getAVPs().addString(Base.USER_NAME, impi);
		
		if (publicIdentity.getIdentityType() == IdentityType.WILDCARDED_IMPU)
			answer.add(AVP.ofString(IMS.IMS_VENDOR_ID, IMS.WILCARDED_IMPU, publicIdentity.getIdentity()));
		
		switch (scheme.getOrdinal())
		{
		case Cx.AuthenticationScheme.SIP_DIGEST_ORDINAL:
			AuthenticationVector[] authVectors = getDigestAuthVectors(1, mar.getDestinationRealm(), privateIdentity);
			
			answer.add(AVP.ofAVPs(IMS.IMS_VENDOR_ID, IMS.SIP_AUTH_DATA_ITEM, authVectors[0].asAuthItem()));
			break;
			
		case Cx.AuthenticationScheme.DIGEST_AKA_MD5_ORDINAL:
			AVP sipAuthorization = sadi.getAVP(IMS.IMS_VENDOR_ID, IMS.SIP_AUTHORIZATION);
			if (sipAuthorization != null)
				procesResynchronisation(sipAuthorization.getBytes(), privateIdentity);

			authVectors = getAkaAuthVectors(1, privateIdentity);			
			answer.add(AVP.ofAVPs(IMS.IMS_VENDOR_ID, IMS.SIP_AUTH_DATA_ITEM, authVectors[0].asAuthItem()));
			break;
		default:
			throw new DiameterException(IMS.IMS_VENDOR_ID, IMS.DIAMETER_ERROR_AUTH_SCHEME_NOT_SUPPORTED);
		}
		answer.send();
	}
	
	private PublicIdentity getPublicIdentity(String impu, String wilcardImpu) throws DiameterException
	{
		PublicIdentity publicIdentity = null;
		if (wilcardImpu != null)
		{
			publicIdentity = _publicIdentityDao.findById(wilcardImpu);
			if (publicIdentity == null && __log.isDebugEnabled())
				__log.warn("Could not found public identity with wilcarded IMPU or wilcarded PSI " + wilcardImpu);
		}
		if (publicIdentity == null)
			publicIdentity = _publicIdentityDao.findById(impu);
		if (publicIdentity == null)
			publicIdentity = _publicIdentityDao.findWilcard(impu);
		if (publicIdentity == null)
			throw new DiameterException(IMS.IMS_VENDOR_ID, IMS.DIAMETER_ERROR_USER_UNKNOWN,
					"Could not find public identity with IMPU: " + impu);
		return publicIdentity;
	}
	
	private PublicIdentity getPublicIdentity(PrivateIdentity privateIdentity, String impu) throws DiameterException
	{
		for (PublicUserIdentity id : privateIdentity.getPublicIdentities())
		{
			if (id.getIdentity().equals(impu) || (id.isWilcard() && impu.matches(id.getRegex())))
				return id;
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

		// TODO use list for public identity
		PublicIdentity publicIdentity = null;
		PrivateIdentity privateIdentity;

		if (impu != null)
		{
			String wilcardImpu = avps.getString(IMS.IMS_VENDOR_ID, IMS.WILCARDED_IMPU);
			if (wilcardImpu == null)
				wilcardImpu = avps.getString(IMS.IMS_VENDOR_ID, IMS.WILCARDED_PSI);
			publicIdentity = getPublicIdentity(impu, wilcardImpu);
		}
		
		if (impi != null)
		{
			privateIdentity = _privateIdentityDao.findById(impi);
			if (privateIdentity == null)
				throw new DiameterException(IMS.IMS_VENDOR_ID, IMS.DIAMETER_ERROR_USER_UNKNOWN,
						"Unknown private identity: " + impu);
		}
		else if (publicIdentity != null)
		{
			if (publicIdentity instanceof PublicUserIdentity)
			{
				PublicUserIdentity userId = (PublicUserIdentity) publicIdentity;
				privateIdentity = userId.getPrivateIdentities().iterator().next();
				impi = privateIdentity.getIdentity();
			}
			else
			{
				impi = ((PSI) publicIdentity).getPrivateServiceIdentity();
				privateIdentity = null;
			}
		}
		else
			throw new DiameterException(IMS.IMS_VENDOR_ID, Base.DIAMETER_MISSING_AVP);
		
		if (publicIdentity == null)
		{
			if (privateIdentity != null)
			{
				publicIdentity = privateIdentity.getPublicIdentities().iterator().next();
				impu = publicIdentity.getIdentity();
			}
			else
				throw new DiameterException(IMS.IMS_VENDOR_ID, Base.DIAMETER_MISSING_AVP);
		}
		
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
						
		boolean userDataAlreadyAvailable = 
			getMandatoryAVP(avps, IMS.IMS_VENDOR_ID, IMS.USER_DATA_ALREADY_AVAILABLE).getInt() == 1;
		
		if (publicIdentity instanceof PSI
				&& !((PSI) publicIdentity).isPsiActivation())
			throw new DiameterException(IMS.IMS_VENDOR_ID, IMS.DIAMETER_ERROR_USER_UNKNOWN, 
					"The PSI: " + publicIdentity.getIdentity() + " has PSI activation state set to inactive");
		
		DiameterAnswer answer = sar.createAnswer(Base.DIAMETER_SUCCESS);
		Short state = publicIdentity.getState();
		Scscf scscf = publicIdentity.getScscf();
		switch (serverAssignmentType)
		{
		case ServerAssignmentType.REGISTRATION:
		case ServerAssignmentType.RE_REGISTRATION:
			if (scscf != null && !serverName.equals(scscf.getUri()))
			{
				AVP avp = AVP.ofString(IMS.IMS_VENDOR_ID, IMS.SERVER_NAME, scscf.getUri());
				throw new DiameterException(IMS.IMS_VENDOR_ID, IMS.DIAMETER_ERROR_IDENTITY_ALREADY_REGISTERED).addAvp(avp);
			}

			publicIdentity.updateState(impi, State.REGISTERED);
			answer.getAVPs().addString(Base.USER_NAME, impi);
			if (!userDataAlreadyAvailable)
			{
				String serviceProfile = publicIdentity.getImsSubscriptionAsXml(privateIdentity, impu, false);
				answer.getAVPs().addString(IMS.IMS_VENDOR_ID, IMS.USER_DATA, serviceProfile);
			}
			AVPList associatedIds = getAssociatedIdentities(privateIdentity);
			if (!associatedIds.isEmpty())
				answer.getAVPs().add(AVP.ofAVPs(IMS.IMS_VENDOR_ID, IMS.ASSOCIATED_IDENTITIES, associatedIds));
			break;
			
		case ServerAssignmentType.UNREGISTERED_USER:
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
				publicIdentity.setScscf(scscf);
			}
			
			if (State.NOT_REGISTERED == state || State.REGISTERED == state)
				publicIdentity.updateState(impi, State.UNREGISTERED);
			
			answer.getAVPs().addString(Base.USER_NAME, impi);
			
			if (!userDataAlreadyAvailable)
			{
				String serviceProfile = publicIdentity.getImsSubscriptionAsXml(null, impu, false);
				answer.getAVPs().addString(IMS.IMS_VENDOR_ID, IMS.USER_DATA, serviceProfile);
			}
			
			associatedIds = getAssociatedIdentities(privateIdentity);
			if (!associatedIds.isEmpty())
				answer.getAVPs().add(AVP.ofAVPs(IMS.IMS_VENDOR_ID, IMS.ASSOCIATED_IDENTITIES, associatedIds));

			break;
			
		case ServerAssignmentType.TIMEOUT_DEREGISTRATION:
		case ServerAssignmentType.USER_DEREGISTRATION:
		case ServerAssignmentType.DEREGISTRATION_TOO_MUCH_DATA:
		case ServerAssignmentType.ADMINISTRATIVE_DEREGISTRATION:
			publicIdentity.updateState(impi, State.NOT_REGISTERED);
			if (State.REGISTERED == state)
				checkClearScscf(publicIdentity);
			break;
			
		case ServerAssignmentType.TIMEOUT_DEREGISTRATION_STORE_SERVER_NAME:
		case ServerAssignmentType.USER_DEREGISTRATION_STORE_SERVER_NAME:
			publicIdentity.updateState(impi, State.UNREGISTERED);
			break;
			
		case ServerAssignmentType.NO_ASSIGNEMENT:
			if (scscf == null)
				throw new IllegalStateException("No S-CSCF assigned");
			else if (!serverName.equals(scscf.getUri()))
				throw new IllegalStateException("Requesting S-CSCF: " + serverName 
						+ " is not the same as the assigned S-CSCF: " + scscf.getUri());
				
			if (!userDataAlreadyAvailable)
			{
				String serviceProfile = publicIdentity.getImsSubscriptionAsXml(privateIdentity, impu, false);
				answer.getAVPs().addString(IMS.IMS_VENDOR_ID, IMS.USER_DATA, serviceProfile);
			}
			associatedIds = getAssociatedIdentities(privateIdentity);
			if (!associatedIds.isEmpty())
				answer.getAVPs().add(AVP.ofAVPs(IMS.IMS_VENDOR_ID, IMS.ASSOCIATED_IDENTITIES, associatedIds));
			break;
			
		case ServerAssignmentType.AUTHENTICATION_FAILURE:
		case ServerAssignmentType.AUTHENTICATION_TIMEOUT:
			if (publicIdentity instanceof PublicUserIdentity)
			{
				PublicUserIdentity publicUserIdentity = (PublicUserIdentity) publicIdentity;
				RegistrationState registrationState = publicUserIdentity.getImplicitRegistrationSet().getRegistrationState(impu);
				if (registrationState.getState() == State.AUTH_PENDING)
					registrationState.setState(State.NOT_REGISTERED);
				
				checkClearScscf(publicIdentity);
			}
			break;
		default:
			throw new IllegalArgumentException("Unsuported ServerAssignmentType: " + serverAssignmentType);
		}
		answer.send();
	}
	
	private AVPList getAssociatedIdentities(PrivateIdentity privateIdentity)
	{

		AVPList associatedIds = new AVPList();
		if (privateIdentity == null)
			return associatedIds;
		
		for (String identity : privateIdentity.getSubscription().getPrivateIds())
		{
			if (!privateIdentity.getIdentity().equals(identity))
				associatedIds.addString(Base.USER_NAME, identity);
		}
		return associatedIds;
	}
	
	private void checkClearScscf(PublicIdentity publicIdentity)
	{
		boolean activePublic = false;
		if (publicIdentity instanceof PublicUserIdentity)
		{
			Subscription subscription = 
				((PublicUserIdentity) publicIdentity).getPrivateIdentities().iterator().next().getSubscription();
			for (PublicIdentity publicId : subscription.getPublicIdentities())
			{
				Short state = publicId.getState();
				if (State.NOT_REGISTERED != state)
					activePublic = true;
			}
			if (!activePublic)
				subscription.setScscf(null);
		}
		else
			publicIdentity.setScscf(null);
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
	
	protected AuthenticationVector[] getAkaAuthVectors(int nb, PrivateIdentity identity) throws InvalidKeyException, ArrayIndexOutOfBoundsException, UnsupportedEncodingException
	{
		byte[] sqn = _sequenceNumberManager.getNextSqn(identity.getSqn());
		byte[] rand = new byte[16];
		_random.nextBytes(rand);
		AkaAuthenticationVector vector = new AkaAuthenticationVector(
				identity.getAkaPassword(),
				sqn,
				rand,
				identity.getOperatorId());
		identity.setSqn(sqn);
		
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
	
	private void procesResynchronisation(byte[] sipAuthorization, PrivateIdentity identity) {
		__log.debug("SQN Desynchronization detected on user " + identity.getIdentity());
		byte[] sqn = new byte[6];
		byte[] macs = new byte[8];
		
		byte[] rand = new byte[16];
		byte[] auts = new byte[sqn.length + macs.length];
		for (int i = 0; i < rand.length; i++)
		{
			if (i < rand.length)
				rand[i] = sipAuthorization[i];
			else
				auts[i - rand.length] = sipAuthorization[i];
		}


		try
		{
			byte[] k = identity.getAkaPassword();
			byte[] opC = Milenage.computeOpC(k, identity.getOperatorId());
			byte[] ak = Milenage.f5star(k, rand, opC);
			for (int i = 0; i < auts.length; i++) {
				if (i < sqn.length) {
					sqn[i] =  (byte) (auts[i] ^ ak[i]);
				} else {
					macs[i - sqn.length] = auts[i];
				}
			}
			byte[] computeMacs = Milenage.f1star(k, rand, opC, sqn, AkaAuthenticationVector.getAmf());
			for (int i = 0; i < macs.length; i++) {
				if (computeMacs[i] != macs[i]) {
					__log.info("MACS verification failed user " + identity.getIdentity());
					return;
				}
			}
			identity.setSqn(sqn);
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Based on C.3.1	Profile 1: management of sequence numbers which are partly time-based
	 */
	static class SequenceNumberManager {

		private static final int D = 65536;
		private static final int SEQ_LENGTH = 48;
		private static final int SEQ2_LENGTH = 24;
		private static final int IND_LENGTH = 5;
		private static final int A = (int) Math.pow(2, IND_LENGTH);
		private static final int P = (int) Math.pow(2, SEQ2_LENGTH);

		private long _glcStart = System.currentTimeMillis();


		public byte[] getNextSqn(byte[] sqn) {
			long sqnLong;
			if (sqn == null)
				sqnLong = 0;
			else
				sqnLong = HexString.byteArrayToLong(sqn);
			
			long seq1 = sqnLong >> (SEQ2_LENGTH + IND_LENGTH);
			long seq2 = (sqnLong - (seq1 << (SEQ2_LENGTH + IND_LENGTH))) >> IND_LENGTH;
			long ind = sqnLong - (seq1 << (SEQ2_LENGTH + IND_LENGTH)) - (seq2 << IND_LENGTH);
			long glc = getGlc();
			
			long seq;
			if (seq2 < glc && glc < (seq2 + P - D + 1)) {
				// If SEQ2HE < GLC < SEQ2HE + p – D + 1 then HE sets SEQ= SEQ1HE || GLC
				seq = (seq1 << SEQ2_LENGTH) + glc;
			} else if ((glc <= seq2 && seq2 <= (glc + D - 1))
					|| ((seq2 + P - D + 1) <= glc )) {
				// if GLC <= SEQ2HE <= GLC+D - 1 or SEQ2HE + p – D + 1 <= GLC then HE sets SEQ = SEQHE +1;
				seq = (seq1 << SEQ2_LENGTH) + seq2 + 1;
			} else if ((glc + D + 1) < seq2) {
				// if GLC+D - 1 <  SEQ2HE then HE sets SEQ = (SEQ1HE +1) || GLC.
				seq = ((seq1 + 1) << SEQ2_LENGTH) + glc;
			} else
				seq = (seq1 << SEQ2_LENGTH) + seq2;
			
			ind = (ind + 1)%A;
			return HexString.longToByteArray((seq << IND_LENGTH) + ind, SEQ_LENGTH/8);
		}



		private long getGlc() {
			return ((System.currentTimeMillis() - _glcStart) / 1000)%P;
		}
	}

}
