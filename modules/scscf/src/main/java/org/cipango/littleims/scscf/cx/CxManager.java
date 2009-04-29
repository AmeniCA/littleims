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
package org.cipango.littleims.scscf.cx;

import java.io.IOException;

import javax.servlet.sip.SipServletRequest;

import org.apache.log4j.Logger;
import org.cipango.diameter.AVP;
import org.cipango.diameter.ApplicationId;
import org.cipango.diameter.DiameterFactory;
import org.cipango.diameter.DiameterRequest;
import org.cipango.diameter.base.Base;
import org.cipango.diameter.ims.IMS;
import org.cipango.ims.Cx.AuthenticationScheme;
import org.cipango.littleims.util.AuthorizationHeader;
import org.cipango.littleims.util.Base642;
import org.cipango.littleims.util.Digest;

import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;

public class CxManager
{
	private static final Logger __log = Logger.getLogger(CxManager.class);
	private DiameterFactory _diameterFactory;
	private String _hssRealm;
	private String _hssHost;
	private String _scscfName;

	public DiameterFactory getDiameterFactory()
	{
		return _diameterFactory;
	}

	public void setDiameterFactory(DiameterFactory diameterFactory)
	{
		_diameterFactory = diameterFactory;
	}

	public String getHssRealm()
	{
		return _hssRealm;
	}

	public void setHssRealm(String hssRealm)
	{
		_hssRealm = hssRealm;
	}

	public String getHssHost()
	{
		return _hssHost;
	}

	public void setHssHost(String hssHost)
	{
		_hssHost = hssHost;
	}

	public String getScscfName()
	{
		return _scscfName;
	}

	public void setScscfName(String scscfName)
	{
		_scscfName = scscfName;
	}

	private DiameterRequest newRequest(int command, String publicUserIdentity, 
			String privateUserId)
	{
		ApplicationId appId = new ApplicationId(ApplicationId.Type.Auth, IMS.CX_APPLICATION_ID, IMS.IMS_VENDOR_ID);
		DiameterRequest request =  _diameterFactory.createRequest(appId, command, _hssRealm, _hssHost);
		if (privateUserId != null)
			request.add(AVP.ofString(Base.USER_NAME, privateUserId));
		request.add(AVP.ofString(IMS.IMS_VENDOR_ID, IMS.PUBLIC_IDENTITY, publicUserIdentity));
		request.add(AVP.ofString(IMS.IMS_VENDOR_ID, IMS.SERVER_NAME, _scscfName));
		return request;
	}
	
	/**
	 * <pre>
	 *	 	< Multimedia-Auth-Request > ::=  < Diameter Header: 303, REQ, PXY, 16777216 >
	 *		< Session-Id >
	 *		{ Vendor-Specific-Application-Id }
	 *		{ Auth-Session-State }
	 *		{ Origin-Host }
	 *		{ Origin-Realm }
	 *		{ Destination-Realm }
	 *		[ Destination-Host ]
	 *		{ User-Name }
	 *	   *[ Supported-Features ]
	 *		{ Public-Identity }
	 *		{ SIP-Auth-Data-Item }
	 *		{ SIP-Number-Auth-Items } 
	 *		{ Server-Name }
	 *	   *[ AVP ]
	 *	   *[ Proxy-Info ]
	 *	   *[ Route-Record ]
	 * </pre>
	 * @throws IOException 
	 */
	public void sendMAR(String publicUserIdentity, AuthorizationHeader authorization, SipServletRequest request) throws IOException
	{
		DiameterRequest mar = newRequest(IMS.MAR, publicUserIdentity,  authorization.getParameter(Digest.USERNAME_PARAM));
		mar.add(getSipAuthDataItem(authorization));
		mar.add(AVP.ofInt(IMS.IMS_VENDOR_ID,IMS.SIP_NUMBER_AUTH_ITEMS, 1));
		mar.setAttribute(SipServletRequest.class.getName(), request);
		mar.send();
	}
	
	/**
	 * <pre>
	 * <Server-Assignment-Request> ::=	< Diameter Header: 301, REQ, PXY, 16777216 >
	 *		< Session-Id >
	 *		{ Vendor-Specific-Application-Id }
	 *		{ Auth-Session-State }
	 *		{ Origin-Host }
	 *		{ Origin-Realm }
	 *		[ Destination-Host ]
	 *		{ Destination-Realm }
	 *		[ User-Name ]
	 *	   *[ Supported-Features ]
	 *	   *[ Public-Identity ]
	 *		[ Wildcarded-PSI ]
	 *		[ Wildcarded-IMPU ]
	 *		{ Server-Name }
	 *		{ Server-Assignment-Type }
	 *		{ User-Data-Already-Available }
	 *		[ SCSCF-Restoration-Info ]
	 *	   *[ AVP ]
	 *	   *[ Proxy-Info ]
	 *	   *[ Route-Record ]
	 *</pre>
	 */
	public void sendSAR(String publicUserIdentity, 
			String privateUserId, 
			String wilcardPublicId,
			int serverAssignmentType, 
			boolean userDataAlreadyAvailable,
			SipServletRequest request) throws IOException
	{
		DiameterRequest sar = newRequest(IMS.SAR, publicUserIdentity, privateUserId);

		// FIXME how detect if it is a wilcarded PSI or a wilcarded IMPU ?
		if (wilcardPublicId != null)
			sar.add(AVP.ofString(IMS.IMS_VENDOR_ID, IMS.WILCARDED_PSI, wilcardPublicId));
		sar.add(AVP.ofInt(IMS.IMS_VENDOR_ID, IMS.SERVER_ASSIGNMENT_TYPE, serverAssignmentType));
		sar.add(AVP.ofInt(IMS.IMS_VENDOR_ID, IMS.USER_DATA_ALREADY_AVAILABLE, userDataAlreadyAvailable ? 1 : 0));
		
		sar.setAttribute(SipServletRequest.class.getName(), request);
		sar.send();
		if (__log.isDebugEnabled())
			__log.debug("Send SAR request for public identity " + publicUserIdentity 
				+ " and serverAssignmentType " + serverAssignmentType);
	}
	

	/**
	 * <pre>
	 * 	SIP-Auth-Data-Item :: = < AVP Header : 612 10415 >
	 * 		[ SIP-Item-Number ]
	 * 		[ SIP-Authentication-Scheme ]
	 * 		[ SIP-Authenticate ]
	 * 		[ SIP-Authorization ]
	 * 		[ SIP-Authentication-Context ]
	 * 		[ Confidentiality-Key ]
	 * 		[ Integrity-Key ]
	 * 		[ SIP-Digest-Authenticate ]
	 * 	  * [ Line-Identifier ]
	 * 	  * [AVP]
	 * </pre>
	 */
	private AVP getSipAuthDataItem(AuthorizationHeader authorizationHeader)
	{
		String scheme = null;
		String algorithm = authorizationHeader.getParameter(Digest.ALGORITHM_PARAM);
		if (algorithm == null)
			scheme = AuthenticationScheme.SIP_DIGEST.getName();
		else
		{
			AuthenticationScheme authScheme = AuthenticationScheme.getFromAlgorithm(algorithm);
			if (authScheme == null)
			{
				__log.warn("Unknown algorithm " + algorithm);
				scheme = "Unknown";
			}
			else
				scheme = authScheme.getName();
		}
		
		String auts = authorizationHeader.getParameter(Digest.AUTS);
		if (auts != null)
		{
			byte[] rand = getRand(authorizationHeader.getParameter(Digest.NONCE_PARAM));
			byte[] bAuts = Base642.decode(auts).getBytes();
			
			byte[] sipAuthorization = Digest.concat(rand, bAuts);
			
			return AVP.ofAVPs(IMS.IMS_VENDOR_ID, 
					IMS.SIP_AUTH_DATA_ITEM, 
					AVP.ofString(IMS.IMS_VENDOR_ID, IMS.SIP_AUTHENTICATION_SCHEME, scheme),
					AVP.ofBytes(IMS.IMS_VENDOR_ID, IMS.SIP_AUTHORIZATION, sipAuthorization));
		}
		else
			return AVP.ofAVPs(IMS.IMS_VENDOR_ID, 
				IMS.SIP_AUTH_DATA_ITEM, 
				AVP.ofString(IMS.IMS_VENDOR_ID, IMS.SIP_AUTHENTICATION_SCHEME, scheme));
	}
	
	private byte[] getRand(String nonce)
	{
		byte[] bNonce = Base64.decode(nonce);
		byte[] rand = new byte[16];
		for (int i = 0; i < rand.length; i++)
			rand[i] = bNonce[i];
		return rand;
	}
	
}
