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
package org.cipango.littleims.icscf;

import java.io.IOException;

import javax.servlet.sip.SipServletRequest;

import org.apache.log4j.Logger;
import org.cipango.diameter.AVP;
import org.cipango.diameter.ApplicationId;
import org.cipango.diameter.DiameterFactory;
import org.cipango.diameter.DiameterRequest;
import org.cipango.diameter.base.Base;
import org.cipango.diameter.ims.IMS;

public class CxManager
{
	private static final Logger __log = Logger.getLogger(CxManager.class);
	private DiameterFactory _diameterFactory;
	private String _hssRealm;
	private String _hssHost;
	private String _icscfName;

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

	private DiameterRequest newRequest(int command, String publicUserIdentity, 
			String privateUserId)
	{
		ApplicationId appId = new ApplicationId(ApplicationId.Type.Auth, IMS.CX_APPLICATION_ID, IMS.IMS_VENDOR_ID);
		DiameterRequest request =  _diameterFactory.createRequest(appId, command, _hssRealm, _hssHost);
		if (privateUserId != null)
			request.add(AVP.ofString(Base.USER_NAME, privateUserId));
		request.add(AVP.ofString(IMS.IMS_VENDOR_ID, IMS.PUBLIC_IDENTITY, publicUserIdentity));
		request.add(AVP.ofString(IMS.IMS_VENDOR_ID, IMS.SERVER_NAME, _icscfName));
		return request;
	}
	
	/**
	 * <pre>
	 *	< User-Authorization-Request> ::= < Diameter Header: 300, REQ, PXY, 16777216 >
	 *	 	 < Session-Id >
	 *	 	 { Vendor-Specific-Application-Id }
	 *	 	 { Auth-Session-State }
	 *	 	 { Origin-Host }
	 *	 	 { Origin-Realm }
	 *	 	 [ Destination-Host ]
	 *	 	 { Destination-Realm }
	 *	 	 { User-Name }
	 *	 	*[ Supported-Features ]
	 *	 	 { Public-Identity }
	 *	 	 { Visited-Network-Identifier }
	 *	 	 [ User-Authorization-Type ]
	 *	 	 [ UAR-Flags ]
	 *	 	*[ AVP ]
	 *	 	*[ Proxy-Info ]
	 *	 	*[ Route-Record ]		
	 * </pre>
	 * @throws IOException 
	 */
	public void sendUAR(String publicUserIdentity, 
			String privateUserId, 
			String visitednetworkId, 
			int userAuthorizationType, 
			SipServletRequest request) throws IOException
	{
		DiameterRequest uar = newRequest(IMS.UAR, publicUserIdentity, privateUserId);
		uar.add(AVP.ofInt(IMS.IMS_VENDOR_ID, IMS.USER_AUTHORIZATION_TYPE, userAuthorizationType));
		if (visitednetworkId != null)
			uar.add(AVP.ofBytes(IMS.IMS_VENDOR_ID, IMS.VISITED_NETWORK_IDENTIFIER, visitednetworkId.getBytes()));
		uar.setAttribute(SipServletRequest.class.getName(), request);
		uar.send();
	}
	
	
	/**
	 * <pre>
	 *  <Location-Info-Request> ::=		< Diameter Header: 302, REQ, PXY, 16777216 >
	 *	 	 < Session-Id >
	 *	 	 { Vendor-Specific-Application-Id }
	 *	 	 { Auth-Session-State }
	 *	 	 { Origin-Host }
	 *	 	 { Origin-Realm }
	 *	 	 [ Destination-Host ]
	 *	 	 { Destination-Realm }
	 *	 	 [ Originating-Request ]
	 *	 	*[ Supported-Features ]
	 *	 	 { Public-Identity }
	 *	 	 [ User-Authorization-Type ]
	 *	 	*[ AVP ]
	 *	 	*[ Proxy-Info ]
	 *	 	*[ Route-Record ]		
	 * </pre>
	 * @throws IOException 
	 */
	public void sendLIR(String publicUserIdentity,
			boolean originatingRequest,
			Integer userAuthorizationType,
			SipServletRequest request) throws IOException
	{
		DiameterRequest lir = newRequest(IMS.LIR, publicUserIdentity, null);
		if (originatingRequest)
			lir.add(AVP.ofInt(IMS.IMS_VENDOR_ID, IMS.ORIGININATING_REQUEST, 0));
		if (userAuthorizationType != null)
			lir.add(AVP.ofInt(IMS.IMS_VENDOR_ID, IMS.USER_AUTHORIZATION_TYPE, userAuthorizationType));
		lir.setAttribute(SipServletRequest.class.getName(), request);
		lir.send();
	}
	
	public String getIcscfName()
	{
		return _icscfName;
	}

	public void setIcscfName(String icscfName)
	{
		_icscfName = icscfName;
	}
	
}
