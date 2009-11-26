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
import javax.servlet.sip.URI;

import org.apache.log4j.Logger;
import org.cipango.diameter.AVP;
import org.cipango.diameter.AVPList;
import org.cipango.diameter.DiameterCommand;
import org.cipango.diameter.DiameterFactory;
import org.cipango.diameter.DiameterRequest;
import org.cipango.diameter.base.Base;
import org.cipango.diameter.ims.Cx;
import org.cipango.diameter.ims.Cx.ServerAssignmentType;
import org.cipango.diameter.ims.Cx.UserDataAlreadyAvailable;
import org.cipango.ims.AuthenticationScheme;
import org.cipango.littleims.util.AuthorizationHeader;
import org.cipango.littleims.util.Digest;
import org.cipango.littleims.util.HexString;
import org.cipango.littleims.util.URIHelper;

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

	private DiameterRequest newRequest(DiameterCommand command, String publicUserIdentity, 
			String privateUserId)
	{
		DiameterRequest request =  _diameterFactory.createRequest(Cx.CX_APPLICATION_ID, command, _hssRealm, _hssHost);
		if (privateUserId != null)
			request.add(Base.USER_NAME, privateUserId);
		request.add(Cx.PUBLIC_IDENTITY, publicUserIdentity);
		request.add(Cx.SERVER_NAME, _scscfName);
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
	public void sendMAR(URI publicUserIdentity, AuthorizationHeader authorization, SipServletRequest request) throws IOException
	{
		String privateId;
		if (authorization != null)
			privateId = authorization.getUsername();
		else
			privateId = URIHelper.extractPrivateIdentity(publicUserIdentity);
		DiameterRequest mar = newRequest(Cx.MAR, publicUserIdentity.toString(), privateId);
		mar.getAVPs().add(getSipAuthDataItem(authorization));
		mar.add(Cx.SIP_NUMBER_AUTH_ITEMS, 1);
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
			ServerAssignmentType serverAssignmentType, 
			UserDataAlreadyAvailable userDataAlreadyAvailable,
			SipServletRequest request) throws IOException
	{
		DiameterRequest sar = newRequest(Cx.SAR, publicUserIdentity, privateUserId);

		// FIXME how detect if it is a wilcarded PSI or a wilcarded IMPU ?
		if (wilcardPublicId != null)
			sar.add(Cx.WILCARDED_PSI, wilcardPublicId);
		sar.add(Cx.SERVER_ASSIGNMENT_TYPE, serverAssignmentType);
		sar.add(Cx.USER_DATA_ALREADY_AVAILABLE, userDataAlreadyAvailable);
		if (request != null)
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
	private AVP<AVPList> getSipAuthDataItem(AuthorizationHeader authorizationHeader)
	{
		String scheme = null;
		String algorithm = authorizationHeader == null ? null : authorizationHeader.getAlgorithm();
		if (authorizationHeader == null)
			scheme = AuthenticationScheme.SIP_DIGEST.getName();
		else if (algorithm == null)
			scheme = AuthenticationScheme.DIGEST_AKA_MD5.getName();
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
		
		String auts = authorizationHeader == null ? null : authorizationHeader.getAuts();

		AVPList avpList = new AVPList();
		avpList.add(Cx.SIP_AUTHENTICATION_SCHEME, scheme);
		if (auts != null)
		{
			__log.debug("Detect auts parameter in authorization header, try to resynchronized");
			byte[] rand = getRand(authorizationHeader.getNonce());
			byte[] bAuts = HexString.hexToBuffer(auts);
			byte[] sipAuthorization = Digest.concat(rand, bAuts);
			
			avpList.add(Cx.SIP_AUTHORIZATION, sipAuthorization);
		}
		
		return new AVP<AVPList>(Cx.SIP_AUTH_DATA_ITEM, avpList);
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
