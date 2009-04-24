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
package org.cipango.littleims.icscf;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.sip.Address;
import javax.servlet.sip.Proxy;
import javax.servlet.sip.ServletParseException;
import javax.servlet.sip.SipFactory;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipURI;
import javax.servlet.sip.TooManyHopsException;
import javax.servlet.sip.URI;

import org.apache.log4j.Logger;
import org.cipango.diameter.AVP;
import org.cipango.diameter.DiameterAnswer;
import org.cipango.diameter.ims.IMS;
import org.cipango.littleims.cx.UserAuthorizationType;
import org.cipango.littleims.util.AuthorizationHeader;
import org.cipango.littleims.util.Digest;
import org.cipango.littleims.util.Headers;
import org.cipango.littleims.util.URIHelper;

public class IcscfService
{

	private static final Logger __log = Logger.getLogger(IcscfService.class);

	private static final String ORIG_PARAM = "orig";
	private static final String TERM_PARAM = "term";	
	
	private CxManager _cxManager;
	private SipFactory _sipFactory;
	private boolean _terminatingDefault;
	private List<String> _psiSubDomains;
	
	public void doRegister(SipServletRequest request) throws ServletException, IOException
	{
		// TS 22229 §5.3.
		if(!comesFromTrustedDomain(request))
		{
			request.createResponse(SipServletResponse.SC_FORBIDDEN).send();
			return;
		}
		
		SipURI to = (SipURI) request.getTo().getURI();
		SipURI aor = URIHelper.getCanonicalForm(_sipFactory, to);
		String authorization = request.getHeader(Headers.AUTHORIZATION_HEADER);
		AuthorizationHeader ah = authorization == null ? null : new AuthorizationHeader(authorization);
		String  privateUserId = null;
		if (ah != null)
			privateUserId = ah.getParameter(Digest.USERNAME_PARAM);
		
		_cxManager.sendUAR(aor.toString(), privateUserId,
				request.getHeader(Headers.P_VISITED_NETWORK_ID), 
				getUserAuthorizationType(request), request);
	}
	
	public void handleUAA(DiameterAnswer uaa)
	{
		SipServletRequest request = (SipServletRequest) uaa.getRequest().getAttribute(SipServletRequest.class.getName());
		try
		{
			if ( uaa.getResultCode() >= 3000)
			{
				__log.debug("Diameter UAA answer is not valid: " + uaa.getResultCode() + ". Sending 403 response");
	
				request.createResponse(SipServletResponse.SC_FORBIDDEN).send();
				return;
			}
			
			String scscfName = uaa.getAVP(IMS.IMS_VENDOR_ID, IMS.SERVER_NAME).getString();
			request.setRequestURI(_sipFactory.createURI(scscfName));
			AVP avp = uaa.getAVP(IMS.IMS_VENDOR_ID, IMS.WILCARDED_IMPU);
			if (avp != null)
			{
				request.setHeader(Headers.P_PROFILE_KEY, avp.getString());
			}
			proxy(request);
		}
		catch (Throwable e) 
		{
			__log.warn("Cannot handle UAA answer, send 480/REGISTER", e);
			try { request.createResponse(SipServletResponse.SC_TEMPORARLY_UNAVAILABLE).send(); } catch (Exception _) {}
		}
		finally
		{
			request.getApplicationSession().invalidate();
		}
	}
	
	public void handleLIA(DiameterAnswer lia)
	{
		SipServletRequest request = (SipServletRequest) lia.getRequest().getAttribute(SipServletRequest.class.getName());
		try
		{
			if ( lia.getResultCode() >= 3000)
			{
				__log.debug("Diameter LIA answer is not valid: " + lia.getResultCode() + ". Sending 403 response");
	
				request.createResponse(SipServletResponse.SC_FORBIDDEN).send();
				return;
			}
			
			if (isOriginating(request))
			{
				if (lia.getResultCode() >= 3000)
				{
					__log.debug("Diameter LIA answer is not valid: " + lia.getResultCode() + ". Sending 404 response");	
					request.createResponse(SipServletResponse.SC_NOT_FOUND).send();
					return;
				}
				
				SipURI scscfUri = (SipURI) _sipFactory.createURI(lia.getAVP(IMS.IMS_VENDOR_ID, IMS.SERVER_NAME).getString());
				scscfUri.setLrParam(true);
				scscfUri.setParameter(ORIG_PARAM, "");
				request.pushRoute(scscfUri);
				
				AVP avp = lia.getAVP(IMS.IMS_VENDOR_ID, IMS.WILCARDED_IMPU);
				if (avp != null)
					request.setHeader(Headers.P_PROFILE_KEY, avp.getString());
			}
			else
			{
				URI requestUri = request.getRequestURI();
				String scheme = requestUri.getScheme();
				if (lia.getResultCode() >= 3000)
				{
					if (scheme.equals("tel"))
					{
						// TODO send to bgcf.
						request.createResponse(SipServletResponse.SC_NOT_FOUND).send();
					}
					else
						request.createResponse(SipServletResponse.SC_NOT_FOUND).send();
					return;
				}
				SipURI scscfUri = (SipURI) _sipFactory.createURI(lia.getAVP(IMS.IMS_VENDOR_ID, IMS.SERVER_NAME).getString());
				scscfUri.setLrParam(true);
				request.pushRoute(scscfUri);
				AVP avp = lia.getAVP(IMS.IMS_VENDOR_ID, IMS.WILCARDED_IMPU);
				if (avp != null)
					request.setHeader(Headers.P_PROFILE_KEY, avp.getString());
				else
				{
					avp = lia.getAVP(IMS.IMS_VENDOR_ID, IMS.WILCARDED_PSI);
					if (avp != null)
						request.setHeader(Headers.P_PROFILE_KEY, avp.getString());
				}
				
			}

			proxy(request);
		}
		catch (Throwable e) 
		{
			__log.warn("Cannot handle UAA answer, send 480/REGISTER", e);
			try { request.createResponse(SipServletResponse.SC_TEMPORARLY_UNAVAILABLE).send(); } catch (Exception _) {}
		}
		finally
		{
			request.getApplicationSession().invalidate();
		}
	}
	
	private int getUserAuthorizationType(SipServletRequest request) throws ServletParseException
	{
		Address contact = request.getAddressHeader(Headers.CONTACT_HEADER);

		int expires = contact.getExpires();
		if (expires == -1)
			expires = request.getExpires();
		
		if (expires == 0)
			return UserAuthorizationType.DE_REGISTRATION;
		else
			return UserAuthorizationType.REGISTRATION;
	}

		
	public void doRequest(SipServletRequest request) throws IOException, ServletException
	{
		
		// TS 24229 §5.3.2.1A
		if(!comesFromTrustedDomain(request))
		{
			request.createResponse(SipServletResponse.SC_FORBIDDEN).send();
			return;
		}
		
		if (isOriginating(request))
		{
			URI pAssertedId = request.getAddressHeader(Headers.P_ASSERTED_IDENTITY_HEADER).getURI();
			
			request.removeHeader(Headers.P_PROFILE_KEY);
			
			_cxManager.sendLIR(pAssertedId.toString(), true, null, request);
		}
		else
		{
			request.removeHeader(Headers.P_PROFILE_KEY);
			
			URI requestUri = request.getRequestURI();
			String scheme = requestUri.getScheme();
			// a pres: or an im: URI, then translate the pres: or im: URI to a public user identity
			// and replace the Request-URI of the incoming request with that public user identity; or
			if ("im".equals(scheme) || "pres".equals(scheme))
			{
				// TODO translate
			}
			else if (requestUri.isSipURI())
			{
				SipURI uri = (SipURI) requestUri;
				// b)	 a SIP-URI that is not a GRUU and with the user part starting with a + and 
				// the "user" SIP URI parameter equals "phone" then replace the Request-URI with a 
				// tel-URI with the user part of the SIP-URI in the telephone-subscriber element in 
				// the tel-URI, and carry forward the tel-URI parameters that may be present in the Request-URI; or
				
				// c)	a SIP URI that is a GRUU, then obtain the public user identity from the Request-URI
				// and use it for location query procedure to the HSS. When forwarding the request, the 
				// I-CSCF shall not modify the Request-URI of the incoming request;
				// TODO
				
				// 3)	check if the domain name of the Request-URI matches with one of the PSI subdomains
				// configured in the I-CSCF
				if (_psiSubDomains.contains(uri.getHost()))
				{
					proxy(request);
					return;
				}
			}
			_cxManager.sendLIR(requestUri.toString(), false, null, request);
		}
	}
	
	private void proxy(SipServletRequest request) throws TooManyHopsException
	{
		Proxy proxy = request.getProxy();
		proxy.setRecordRoute(false);
		proxy.proxyTo(request.getRequestURI());
	}
	
	private boolean comesFromTrustedDomain(SipServletRequest request)
	{
		// 3GPP TS 33.210 
		return true;
	}

	public SipFactory getSipFactory()
	{
		return _sipFactory;
	}


	public void setSipFactory(SipFactory sipFactory)
	{
		_sipFactory = sipFactory;
	}


	public boolean isTerminatingDefault()
	{
		return _terminatingDefault;
	}


	public void setTerminatingDefault(boolean isTerminatingDefault)
	{
		_terminatingDefault = isTerminatingDefault;
	}


	public List<String> getPsiSubDomains()
	{
		return _psiSubDomains;
	}


	public void setPsiSubDomains(List<String> psiSubDomains)
	{
		_psiSubDomains = psiSubDomains;
	}
	
	private boolean isOriginating(SipServletRequest request)
	{
		String orig = request.getParameter(ORIG_PARAM);
		String term = request.getParameter(TERM_PARAM);
		__log.debug("Orig param is: *" + orig + "*. Term param is: *" + term + "*");
		if (isTerminatingDefault()) // default standard mode
			return orig != null;
		else
			return term != null;
	}

	public CxManager getCxManager()
	{
		return _cxManager;
	}

	public void setCxManager(CxManager cxManager)
	{
		_cxManager = cxManager;
	}

}
