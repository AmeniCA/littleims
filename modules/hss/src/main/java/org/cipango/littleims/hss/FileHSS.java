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
package org.cipango.littleims.hss;

import java.util.Random;

import org.apache.log4j.Logger;
import org.cipango.littleims.cx.AuthenticationVector;
import org.cipango.littleims.cx.Cx;
import org.cipango.littleims.cx.DigestAuthenticate;
import org.cipango.littleims.cx.IdentityType;
import org.cipango.littleims.cx.LIA;
import org.cipango.littleims.cx.MAA;
import org.cipango.littleims.cx.ResultCode;
import org.cipango.littleims.cx.SAA;
import org.cipango.littleims.cx.SipAuthDataItem;
import org.cipango.littleims.cx.UAA;
import org.cipango.littleims.cx.SipAuthDataItem.AuthenticationScheme;
import org.cipango.littleims.cx.data.userprofile.IMSSubscriptionDocument;
import org.cipango.littleims.cx.data.userprofile.TPublicIdentity;
import org.cipango.littleims.cx.data.userprofile.TPublicIdentityExtension;
import org.cipango.littleims.hss.Credentials.Credential;
import org.cipango.littleims.util.AuthorizationHeader;
import org.cipango.littleims.util.Base642;
import org.cipango.littleims.util.Digest;

import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;


public class FileHSS implements Cx
{

	private static final Logger __log = Logger.getLogger(FileHSS.class);
	
	private HssDao _dao;
	private String _realm;
	private Random _random = new Random();
	private String _scscfName;

	public MAA MAR(String publicUserIdentity, AuthorizationHeader authorization)
	{
		// Procedure defined in TS 29228 §6.3.1
			
		String privateUri = authorization == null ? null : authorization.getParameter(Digest.USERNAME_PARAM);
		Credentials credentials = _dao.getCredentials(publicUserIdentity);
		// 1. Check that the Private User Identity and the Public User Identity exist 
		// in the HSS. If not Experimental-Result-Code shall be set to DIAMETER_ERROR_USER_UNKNOWN.
		if (credentials == null)
		{
			__log.info("Could not found user with public identity " + 
					publicUserIdentity);
			return new MAA(ResultCode.DIAMETER_ERROR_USER_UNKNOWN);
		}
		
		// 2.	Check whether the Private and Public User Identities in the request are associated
		// in the HSS. If not Experimental-Result-Code shall be set to DIAMETER_ERROR_IDENTITIES_DONT_MATCH.
		if (privateUri != null && credentials.getCredential(privateUri) == null)
		{
			__log.info("User with public identity " + 
					publicUserIdentity + " has not the private URI " + privateUri);
			return new MAA(ResultCode.DIAMETER_ERROR_USER_UNKNOWN);
		}
		
		// 3.	Check the authentication scheme indicated in the request, and
		//  -	if it is "Unknown", check the authentication scheme stored in HSS. If it is neither 
		//      NASS-Bundled authentication nor SIP Digest authentication, Experimental-Result-Code
		//      shall be set to DIAMETER_ERROR_AUTH_SCHEME_UNSUPPORTED.
		//  -	if not, check that the authentication scheme indicated in the request is supported.
		//      If not Experimental-Result-Code shall be set to DIAMETER_ERROR_AUTH_SCHEME_UNSUPPORTED.
		SipAuthDataItem sipAuthDataItem;
		if (authorization == null)
		{
			Credential credential = credentials.getDefaultCredential();
			String ha1 = Digest.calculateHa1(credential.getPrivateUri(), _realm, credential.getPassword());
			DigestAuthenticate digest = new DigestAuthenticate(_realm, Digest.AUTH_VALUE, ha1, null);
			sipAuthDataItem = new SipAuthDataItem(digest);
		}
		else
		{
			String algorithm = authorization.getParameter(Digest.ALGORITHM_PARAM);
			if (AuthenticationScheme.DIGEST.getAlgorithm().equals(algorithm))
			{
				String ha1 = Digest.calculateHa1(privateUri, _realm, credentials.getPassword(privateUri));
				DigestAuthenticate digest = new DigestAuthenticate(_realm, Digest.AUTH_VALUE, ha1, null);
				sipAuthDataItem = new SipAuthDataItem(digest);
			}
			else if (algorithm == null || AuthenticationScheme.DIGEST_AKA_MD5.getAlgorithm().equals(algorithm))
			{
				String auts = authorization.getParameter(Digest.AUTS);
				if (auts != null) {
					procesResynchronisation(authorization.getParameter(Digest.NONCE_PARAM), 
							Base642.decode(auts).getBytes(), 
							credentials.getCredential(privateUri),
							publicUserIdentity);
							
				}
				AuthenticationVector v = createAuthenticationVector((credentials.getCredential(privateUri)));
				sipAuthDataItem = new SipAuthDataItem(v);
			}
			else
			{
				__log.info("Unsuported authentication algorithm " + algorithm);
				return new MAA(ResultCode.DIAMETER_ERROR_AUTH_SCHEME_NOT_SUPPORTED);
			}
				
		}
		MAA maa = new MAA(ResultCode.DIAMETER_SUCCESS);
		maa.addSipAuthDataItem(sipAuthDataItem);
		maa.setPublicIdentity(publicUserIdentity);
		return maa;
	}
	
	private AuthenticationVector createAuthenticationVector(Credential credential)
	{
		try
		{
			byte[] rand = new byte[16];
			_random.nextBytes(rand);
			
			return new AuthenticationVectorImpl(
					credential.getAkaPassword(),
					credential.getNextSqn(),
					rand,
					credential.getOperatorId());
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}
	
	private void procesResynchronisation(String nonce, byte[] auts, Credential credential, String uri) {
		__log.debug("SQN Desynchronization detected on user " + uri);
		byte[] sqn = new byte[6];
		byte[] macs = new byte[8];
		if (auts.length != (sqn.length + macs.length)) {
			throw new IllegalArgumentException("Auths length is invalid");
		}

		try
		{
			byte[] k = credential.getAkaPassword();
			byte[] opC = Milenage.computeOpC(k, credential.getOperatorId());
			byte[] rand = getRand(nonce);
			byte[] ak = Milenage.f5star(credential.getAkaPassword(), rand, opC);
			for (int i = 0; i < auts.length; i++) {
				if (i < sqn.length) {
					sqn[i] =  (byte) (auts[i] ^ ak[i]);
				} else {
					macs[i - sqn.length] = auts[i];
				}
			}
			byte[] computeMacs = Milenage.f1star(k, rand, opC, sqn, AuthenticationVectorImpl.getAmf());
			for (int i = 0; i < macs.length; i++) {
				if (computeMacs[i] != macs[i]) {
					__log.info("MACS verification failed user " + uri);
					return;
				}
			}
			credential.setSqn(sqn);
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	private byte[] getRand(String nonce)
	{
		byte[] bNonce = Base64.decode(nonce);
		byte[] rand = new byte[16];
		for (int i = 0; i < rand.length; i++)
			rand[i] = bNonce[i];
		return rand;
	}

	public SAA sar(String publicUserIdentity, String privateUserId, String wilcardPublicId,
			int serverAssignmentType,
			boolean userDataAlreadyAvailable)
	{
		// Procedure defined in TS 29 228 §6.1.2.1 2
		
		
		IMSSubscriptionDocument profile = _dao.getUserProfile(publicUserIdentity, wilcardPublicId);
		
		// 1. Check that the Public Identity and Private Identity exist in the HSS. If not 
		// Experimental-Result-Code shall be set to DIAMETER_ERROR_USER_UNKNOWN.
		if (profile == null)
		{
			return new SAA(ResultCode.DIAMETER_ERROR_USER_UNKNOWN);
		}
		
		// 2. The HSS may check whether the Private and Public Identities received
		// in the request are associated in the HSS. If not Experimental-Result-Code shall be set 
		// to DIAMETER_ERROR_IDENTITIES_DONT_MATCH.
		if (privateUserId != null && !privateUserId.equals(profile.getIMSSubscription().getPrivateID()))
		{
			return new SAA(ResultCode.DIAMETER_ERROR_IDENTITIES_DONT_MATCH);
		}
		
		// 4.	If the identity in the request is a Public Service Identity, 
		// then check if the PSI Activation State for that identity is active.  If not, then the
		// response shall contain Experimental-Result-Code set to DIAMETER_ERROR_USER_UNKNOWN.
		
		// 5.	Check the Server Assignment Type value received in the request:
		
		SAA saa = new SAA(ResultCode.DIAMETER_SUCCESS);
		saa.setPrivateIdentity(profile.getIMSSubscription().getPrivateID());
		saa.setUserProfile(profile);
		return saa;
	}

	public UAA uar(String publicUserIdentity, String privateUserId, String visitednetworkId,
			UserAuthorizationType type)
	{
		try
		{
			// Procedure defined in TS 29228 §6.1.1

			Credentials credentials = _dao.getCredentials(publicUserIdentity);
			// TODO check  wilcard public identity
			
			// 1. Check that the Private User Identity and the Public User Identity exist 
			// in the HSS. If not Experimental-Result-Code shall be set to DIAMETER_ERROR_USER_UNKNOWN.
			if (credentials == null)
			{
				__log.info("Could not found user with public identity " + 
						publicUserIdentity);
				return new UAA(ResultCode.DIAMETER_ERROR_USER_UNKNOWN);
			}
			
			// 2.	Check whether the Private and Public User Identities in the request are associated
			// in the HSS. If not Experimental-Result-Code shall be set to DIAMETER_ERROR_IDENTITIES_DONT_MATCH.
			if (privateUserId != null && credentials.getCredential(privateUserId) == null)
			{
				__log.info("User with public identity " + 
						publicUserIdentity + " has not the private user identity " + privateUserId);
				return new UAA(ResultCode.DIAMETER_ERROR_USER_UNKNOWN);
			}
			
			// 3.	Check whether the Public User Identity received in the request is barred from the 
			// establishment of multimedia sessions.
			// -	If it is an IMS Emergency Registration (by checking the UAR Flags) or the Public 
			//      User Identity received in the request is not barred, continue to step 4. 
			// -	Otherwise, the HSS shall check whether there are other non-barred Public User 
			//      Identities to be implicitly registered with that one. 
			// -	If so, continue to step 4.
			// -	If not, Result-Code shall be set to DIAMETER_AUTHORIZATION_REJECTED.
			// TODO support IMS Emergency Registration	
			if (_dao.getIdentity(publicUserIdentity, null).getBarringIndication())
				return new UAA(ResultCode.DIAMETER_AUTHORIZATION_REJECTED);
			
			
			// 4.	Check the User-Authorization-Type received in the request:
			// TODO support roaming
			
			// 5.	Check the state of the Public User Identity received in the request:
			// TODO save registration state
			
			UAA uaa = new UAA(ResultCode.DIAMETER_SUCCESS);
			uaa.setScscfName(_scscfName);
			return uaa;
		}
		catch (HssException e)
		{
			__log.warn(e.getMessage());
			return new UAA(e.getResultCode());
		}
	}
	
	public LIA lir(String publicUserIdentity, UserAuthorizationType type, boolean originatingRequest)
	{
		// Procedure defined in TS 29228 §6.1.4
		
		// 1.	Check that the Public Identity is known. If not the Experimental-Result-Code shall 
		// be set to DIAMETER_ERROR_USER_UNKNOWN.
		String wilcardPublicId = null;
		try
		{
			wilcardPublicId = _dao.getWilcardPublicIdentity(publicUserIdentity);
			
			// 2.	Check the type of the Public Identity contained in the request:
			TPublicIdentity identity = _dao.getIdentity(publicUserIdentity, wilcardPublicId);

			TPublicIdentityExtension extension = identity.getExtension();
			if ( extension != null 
					&& (extension.getIdentityType() == IdentityType.DISTINCT_PSI
							|| extension.getIdentityType() == IdentityType.WILDCARDED_IMPU))
			{
				// if the PSI Activation State for that identity is active. If not, then the response 
				// shall contain Experimental-Result-Code set to DIAMETER_ERROR_USER_UNKNOWN.
				if (identity.getBarringIndication())
				{
					__log.debug("PSI " + publicUserIdentity + " found but not active");
					return new LIA(ResultCode.DIAMETER_ERROR_USER_UNKNOWN);
				}
				
				// Check if the name of the AS hosting the Public Service Identity is stored in the HSS
				// and that the request does not contain the Originating-Request AVP. If this is the 
				// case the HSS shall return the AS name and the Result-Code AVP shall be set to 
				// DIAMETER_SUCCESS. Otherwise, continue to step 3.
				if (!originatingRequest)
				{
					// TODO maintains the address information of the AS hosting the PSI for the "PSI user". 
				}
			}
			
			// 2a.	Check if User-Authorization-Type was received in the request, and if the value is 
			// REGISTRATION_AND_CAPABILITIES
			// TODO support REGISTRATION_AND_CAPABILITIES
			
			// 3.	Check the state of the Public Identity received in the request, and where necessary,
			// check if the Public Identity has services related to the unregistered state.
			// State is not saved so could not do this step.
			LIA lia = new LIA(ResultCode.DIAMETER_SUCCESS);
			lia.setScscfName(_scscfName);
			if (extension != null && extension.getIdentityType() == IdentityType.WILDCARDED_PSI)
				lia.setWilcardPSI(wilcardPublicId);
			else
				lia.setWilcardPublicId(wilcardPublicId);
			return lia;
		}
		catch (HssException e)
		{
			__log.warn(e.getMessage());
			return new LIA(e.getResultCode());
		}
	}

	public String getRealm()
	{
		return _realm;
	}

	public void setRealm(String realm)
	{
		_realm = realm;
	}

	public String getScscfName()
	{
		return _scscfName;
	}

	public void setScscfName(String scscfName)
	{
		_scscfName = scscfName;
		__log.info("S-CSCF name: " + _scscfName);
	}

	public void setDao(HssDao dao)
	{
		_dao = dao;
	}

	public HssDao getDao()
	{
		return _dao;
	}

}
