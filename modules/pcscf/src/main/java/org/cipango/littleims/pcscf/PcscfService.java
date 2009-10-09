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
import javax.servlet.sip.SipURI;
import javax.servlet.sip.TooManyHopsException;

import org.apache.log4j.Logger;
import org.cipango.littleims.util.Headers;

public class PcscfService
{

	private final Logger _log = Logger.getLogger(PcscfServlet.class);
	
	private SipFactory _sipFactory;
	private SipURI _pcscfUri;
	private Map<String, String> _registerHeadersToAdd;
	private List<String> _registerHeadersToRemove;
	private Map<String, String> _requestHeadersToAdd;
	private List<String> _requestHeadersToRemove;
	
	public void doRegister(SipServletRequest request) throws TooManyHopsException
	{
		request.addAddressHeader(Headers.PATH, _sipFactory.createAddress(_pcscfUri), true);
		processHeaders(request, _registerHeadersToRemove, _registerHeadersToAdd);
		Proxy proxy = request.getProxy();
		proxy.setRecordRoute(true);
		proxy.setSupervised(true);
		proxy.proxyTo(request.getRequestURI());
	}
	
	public void doNonRegisterRequest(SipServletRequest request) throws IOException, ServletException
	{
		if (!request.isInitial())
			return;
				
		// Add headers only in originating mode
		if (request.getTo().getURI().equals(request.getRequestURI()))
		{
			Address asserted = request.getAddressHeader(Headers.P_PREFERRED_IDENTITY);
			if (asserted == null)
			{
				asserted = (Address) request.getFrom().clone();
				asserted.removeParameter("tag");
			}
			processHeaders(request, _requestHeadersToRemove, _requestHeadersToAdd);
			
			request.setAddressHeader(Headers.P_ASSERTED_IDENTITY, asserted);
		}
		Proxy proxy = request.getProxy();
		proxy.setRecordRoute(true);
		proxy.setSupervised(true);
		proxy.proxyTo(request.getRequestURI());
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
}
