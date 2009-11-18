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
package org.cipango.littleims.pcscf;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.sip.Address;
import javax.servlet.sip.Proxy;
import javax.servlet.sip.SipFactory;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipURI;
import javax.servlet.sip.TooManyHopsException;
import javax.servlet.sip.URI;

import org.apache.log4j.Logger;
import org.cipango.littleims.pcscf.subscription.debug.DebugIdService;
import org.cipango.littleims.pcscf.subscription.reg.RegEventService;
import org.cipango.littleims.util.AuthorizationHeader;
import org.cipango.littleims.util.Headers;
import org.cipango.littleims.util.URIHelper;

public class PcscfService
{

	private final Logger _log = Logger.getLogger(PcscfServlet.class);
	
	private SipFactory _sipFactory;
	private SipURI _pcscfUri;
	private SipURI _icscfUri;
	private Map<String, String> _registerHeadersToAdd;
	private List<String> _registerHeadersToRemove;
	private Map<String, String> _requestHeadersToAdd;
	private List<String> _requestHeadersToRemove;
	private String _userAgent = "littleIMS :: P-CSCF";
	
	private DebugIdService _debugIdService;
	private RegEventService _regEventService;
	
	public void init()
	{
		if (_debugIdService.getUserAgent() == null)
			_debugIdService.setUserAgent(_userAgent);
		if (_regEventService.getUserAgent() == null)
			_regEventService.setUserAgent(_userAgent);
	}
	
	public void doRegister(SipServletRequest request) throws TooManyHopsException
	{
		request.addAddressHeader(Headers.PATH, _sipFactory.createAddress(_pcscfUri), true);
		processHeaders(request, _registerHeadersToRemove, _registerHeadersToAdd);
		Proxy proxy = request.getProxy();
		proxy.setRecordRoute(true);
		proxy.setSupervised(true);
		if (_icscfUri != null)
			request.pushRoute(_icscfUri);
		proxy.proxyTo(request.getRequestURI());
	}
	
	public void doRegisterResponse(SipServletResponse response) throws ServletException
	{
		if (response.getStatus() < 200 || response.getStatus() >= 300)
			return;
		
		Address contact = response.getAddressHeader(Headers.CONTACT);
		int expires = -1;
		if (contact != null)
			expires = contact.getExpires();
		if (expires == -1)
			expires = response.getExpires();
		
		if (expires != 0)
		{
			URI aor = URIHelper.getCanonicalForm(_sipFactory, response.getTo().getURI());
			String authorization = response.getRequest().getHeader(Headers.AUTHORIZATION);
			AuthorizationHeader ah = authorization == null ? null : new AuthorizationHeader(authorization);
			String  privateUserId = null;
			if (ah != null)
				privateUserId = ah.getUsername();
			else
				privateUserId = URIHelper.extractPrivateIdentity(aor);
			
			_regEventService.subscribe(aor, 
					expires, privateUserId, 
					response.getAddressHeaders(Headers.P_ASSOCIATED_URI),
					response.getRequest().getRemoteAddr());
		}
		
		
		// Handle debug-ID
		Iterator<String> it2 = response.getHeaderNames();
		while (it2.hasNext())
		{
			String name = it2.next();
			
			if (name.equalsIgnoreCase(Headers.P_DEBUG_ID))
				_debugIdService.subscribe(response.getFrom().getURI(), expires);
			
		}
		response.getApplicationSession().invalidate();
	}
	
	
	public void doNonRegisterRequest(SipServletRequest request) throws IOException, ServletException
	{
		if (!request.isInitial())
			return;
				
		
		// Add headers only in originating mode
		if (request.getTo().getURI().equals(request.getRequestURI()))
		{
			Address preferred = request.getAddressHeader(Headers.P_PREFERRED_IDENTITY);
			if (preferred == null)
			{
				preferred = (Address) request.getFrom().clone();
				preferred.removeParameter("tag");
				RegContext context = _regEventService.getRegContext(preferred.getURI());
				if (context != null)
					preferred = context.getDefaultIdentity();
			}
			else
			{
				RegContext context = _regEventService.getRegContext(preferred.getURI());
				if (context != null)
					preferred = context.getAssertedIdentity(preferred);
			}
			processHeaders(request, _requestHeadersToRemove, _requestHeadersToAdd);
			
			request.setAddressHeader(Headers.P_ASSERTED_IDENTITY, preferred);
		}
		Proxy proxy = request.getProxy();
		proxy.setRecordRoute(true);
		proxy.setSupervised(true);
		proxy.proxyTo(request.getRequestURI());
	}
	
	/**
	 * cf TS 24229 §5.2.6.4.4
	 */
	public void doNonRegisterResponse(SipServletResponse response)
	{
		SipServletRequest request = response.getRequest();
		if (!request.isInitial())
			return;
		
		// Add headers only if response is UE originating 
		if (request.getTo().getURI().equals(request.getRequestURI()))
			return;
		
		response.removeHeader(Headers.P_CALLED_PARTY_ID);
		
		String pCalledParty = request.getHeader(Headers.P_CALLED_PARTY_ID);
		if (pCalledParty != null)
			response.setHeader(Headers.P_ASSERTED_IDENTITY, pCalledParty);
	}
	
	private void processHeaders(SipServletRequest request, List<String> toRemove, Map<String, String> toAdd)
	{
		Iterator<String> it = toRemove.iterator();
		while (it.hasNext())
		{
			request.removeHeader(it.next());
		}
		it = toAdd.keySet().iterator();
		while (it.hasNext())
		{
			String name = (String) it.next();
			request.addHeader(name, toAdd.get(name));
		}
	}
	
	public Map<String, String> getRegisterHeadersToAdd()
	{
		return _registerHeadersToAdd;
	}
	public void setRegisterHeadersToAdd(Map<String, String> registerHeadersToAdd)
	{
		_registerHeadersToAdd = registerHeadersToAdd;
	}


	public SipURI getPcscfUri()
	{
		return _pcscfUri;
	}

	public void setPcscfUri(SipURI pcscfUri)
	{
		_pcscfUri = pcscfUri;
		_pcscfUri.setLrParam(true);
		_log.info("P-CSCF URI: " + _pcscfUri);
	}

	public SipFactory getSipFactory()
	{
		return _sipFactory;
	}

	public void setSipFactory(SipFactory sipFactory)
	{
		_sipFactory = sipFactory;
	}

	public List<String> getRegisterHeadersToRemove()
	{
		return _registerHeadersToRemove;
	}

	public void setRegisterHeadersToRemove(List<String> registerHeadersToRemove)
	{
		_registerHeadersToRemove = registerHeadersToRemove;
	}

	public Map<String, String> getRequestHeadersToAdd()
	{
		return _requestHeadersToAdd;
	}

	public void setRequestHeadersToAdd(Map<String, String> requestHeadersToAdd)
	{
		_requestHeadersToAdd = requestHeadersToAdd;
	}

	public List<String> getRequestHeadersToRemove()
	{
		return _requestHeadersToRemove;
	}

	public void setRequestHeadersToRemove(List<String> requestHeadersToRemove)
	{
		_requestHeadersToRemove = requestHeadersToRemove;
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

	public SipURI getIcscfUri()
	{
		return _icscfUri;
	}

	public void setIcscfUri(SipURI icscfUri)
	{
		_icscfUri = icscfUri;
	}

	public String getUserAgent()
	{
		return _userAgent;
	}

	public void setUserAgent(String userAgent)
	{
		_userAgent = userAgent;
	}

	public DebugIdService getDebugIdService()
	{
		return _debugIdService;
	}

	public void setDebugIdService(DebugIdService debugIdService)
	{
		_debugIdService = debugIdService;
	}

	public RegEventService getRegEventService()
	{
		return _regEventService;
	}

	public void setRegEventService(RegEventService regEventService)
	{
		_regEventService = regEventService;
	}

}
