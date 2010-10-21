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
import org.cipango.diameter.api.DiameterServletAnswer;
import org.cipango.diameter.ims.Cx;
import org.cipango.diameter.ims.Cx.UserAuthorizationType;
import org.cipango.littleims.util.AuthorizationHeader;
import org.cipango.littleims.util.Headers;
import org.cipango.littleims.util.URIHelper;

public class IcscfService
{

	private final Logger _log = Logger.getLogger(IcscfService.class);

	private static final String ORIG_PARAM = "orig";
	private static final String TERM_PARAM = "term";	
	
	private CxManager _cxManager;
	private SipFactory _sipFactory;
	private boolean _terminatingDefault;
	private List<String> _psiSubDomains;
	private String _userAgent = "littleIMS :: I-CSCF";
	
	public void doRegister(SipServletRequest request) throws ServletException, IOException
	{
		// TS 22229 §5.3.
		if(!comesFromTrustedDomain(request))
		{
			sendResponse(request, SipServletResponse.SC_FORBIDDEN);
			return;
		}
		
		URI aor = URIHelper.getCanonicalForm(_sipFactory, request.getTo().getURI());
		String authorization = request.getHeader(Headers.AUTHORIZATION);
		AuthorizationHeader ah = authorization == null ? null : new AuthorizationHeader(authorization);
		String  privateUserId = null;
		if (ah != null)
			privateUserId = ah.getUsername();
		else
			privateUserId = URIHelper.extractPrivateIdentity(aor);
		
		_cxManager.sendUAR(aor.toString(), privateUserId,
				request.getHeader(Headers.P_VISITED_NETWORK_ID), 
				getUserAuthorizationType(request), request);
	}
	
	public void handleUAA(DiameterServletAnswer uaa)
	{
		SipServletRequest request = (SipServletRequest) uaa.getRequest().getAttribute(SipServletRequest.class.getName());
		try
		{
			if (!uaa.getResultCode().isSuccess())
			{
				_log.debug("Diameter UAA answer is not valid: " + uaa.getResultCode() + ". Sending 403 response");
	
				sendResponse(request, SipServletResponse.SC_FORBIDDEN);
				return;
			}
			
			String scscfName = uaa.get(Cx.SERVER_NAME);
			request.setRequestURI(_sipFactory.createURI(scscfName));
			String wilcard = uaa.get(Cx.WILCARDED_IMPU);
			if (wilcard != null)
				request.setHeader(Headers.P_PROFILE_KEY, wilcard);
		
			proxy(request);
		}
		catch (Throwable e) 
		{
			_log.warn("Cannot handle UAA answer, send 480/REGISTER", e);
			sendResponse(request, SipServletResponse.SC_TEMPORARLY_UNAVAILABLE);
		}
		finally
		{
			if (request.getApplicationSession().isValid())
				request.getApplicationSession().invalidate();
		}
	}
	
	public void handleLIA(DiameterServletAnswer lia)
	{
		SipServletRequest request = (SipServletRequest) lia.getRequest().getAttribute(SipServletRequest.class.getName());
		try
		{
			if (isOriginating(request))
			{
				if (!lia.getResultCode().isSuccess())
				{
					_log.debug("Diameter LIA answer from " + request.getFrom().getURI() + " is not valid: " + lia.getResultCode() 
							+ ". Sending 404 response");
					sendResponse(request, SipServletResponse.SC_NOT_FOUND);
					return;
				}
				
				SipURI scscfUri = (SipURI) _sipFactory.createURI(lia.get(Cx.SERVER_NAME));
				scscfUri.setLrParam(true);
				scscfUri.setParameter(ORIG_PARAM, "");
				request.pushRoute(scscfUri);
				
				String wilcard = lia.get(Cx.WILCARDED_IMPU);
				if (wilcard != null)
					request.setHeader(Headers.P_PROFILE_KEY, wilcard);
				
			}
			else
			{
				URI requestUri = request.getRequestURI();
				String scheme = requestUri.getScheme();
				if (lia.getResultCode() == Cx.DIAMETER_ERROR_USER_UNKNOWN)
				{
					if (scheme.equals("tel"))
					{
						// TODO send to bgcf.
						sendResponse(request, SipServletResponse.SC_NOT_FOUND);
					}
					else
					{
						_log.debug("User " + requestUri + " is unknown. Sending '404 Not found' response for "
								+ request.getMethod());
						sendResponse(request, SipServletResponse.SC_NOT_FOUND);
					}
					return;
				}
				else if (lia.getResultCode() == Cx.DIAMETER_ERROR_IDENTITY_NOT_REGISTERED)
				{
					_log.debug("User " + requestUri + " is not registered. Sending '480 Temporarly unvailable' response for " 
							+ request.getMethod());
					sendResponse(request, SipServletResponse.SC_TEMPORARLY_UNAVAILABLE);
					return;
				}
				else if (!lia.getResultCode().isSuccess())
				{
					_log.debug("Diameter LIA answer to " + requestUri + " is not valid: " + lia.getResultCode() 
							+ ". Sending 404 response");
					sendResponse(request, SipServletResponse.SC_NOT_FOUND);
					return;
				}
					
				SipURI scscfUri = (SipURI) _sipFactory.createURI(lia.get(Cx.SERVER_NAME));
				scscfUri.setLrParam(true);
				request.pushRoute(scscfUri);
				String wildcard = lia.get(Cx.WILCARDED_IMPU);
				if (wildcard != null)
					request.setHeader(Headers.P_PROFILE_KEY, wildcard);
				else
				{
					wildcard = lia.get(Cx.WILCARDED_PSI);
					if (wildcard != null)
						request.setHeader(Headers.P_PROFILE_KEY, wildcard);
				}
				
			}

			proxy(request);
		}
		catch (Throwable e) 
		{
			_log.warn("Cannot handle LIA answer, send 480/" + request.getMethod(), e);
			sendResponse(request, SipServletResponse.SC_TEMPORARLY_UNAVAILABLE);
		}
		finally
		{
			if (request.getApplicationSession().isValid())
				request.getApplicationSession().invalidate();
		}
	}
	
	private UserAuthorizationType getUserAuthorizationType(SipServletRequest request) throws ServletParseException
	{
		Address contact = request.getAddressHeader(Headers.CONTACT);

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
			sendResponse(request, SipServletResponse.SC_FORBIDDEN);
			return;
		}
		
		if (isOriginating(request))
		{
			URI pAssertedId = request.getAddressHeader(Headers.P_ASSERTED_IDENTITY).getURI();
			
			request.removeHeader(Headers.P_PROFILE_KEY);
			
			_cxManager.sendLIR(pAssertedId.toString(), true, null, request);
		}
		else
		{
			if (!request.isCommitted())
				request.removeHeader(Headers.P_PROFILE_KEY);
			
			URI requestUri = URIHelper.getCanonicalForm(_sipFactory, request.getRequestURI());
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
	
	public void sendResponse(SipServletRequest request, int statusCode)
	{
		try
		{
			SipServletResponse response = request.createResponse(statusCode);
			String pDebugId = request.getHeader(Headers.P_DEBUG_ID);
			if (pDebugId != null)
				response.setHeader(Headers.P_DEBUG_ID, pDebugId);
			if (_userAgent != null)
				response.setHeader(Headers.SERVER, _userAgent);
			response.send();
		}
		catch (Throwable e)
		{
			_log.warn("Failed to send " + statusCode + "/" + request.getMethod(), e);
		}
	}

}
