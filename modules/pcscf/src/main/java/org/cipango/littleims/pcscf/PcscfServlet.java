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
package org.cipango.littleims.pcscf;

import java.io.IOException;
import java.util.Iterator;

import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.sip.Address;
import javax.servlet.sip.SipServlet;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;

import org.apache.log4j.Logger;
import org.cipango.littleims.pcscf.debug.DebugIdService;
import org.cipango.littleims.util.Headers;
import org.cipango.littleims.util.Methods;
import org.springframework.beans.BeansException;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

public class PcscfServlet extends SipServlet
{
	private PcscfService _pcscfService;
	private DebugIdService _debugIdService;
	private final Logger _log = Logger.getLogger(PcscfServlet.class);
	
	@Override
	public void init() throws ServletException
	{
		WebApplicationContext context = WebApplicationContextUtils
		.getWebApplicationContext(getServletContext());

		try {
			_pcscfService = (PcscfService) context.getBean("pcscfService");
			_debugIdService = (DebugIdService) context.getBean("debugIdService");
		} catch (BeansException e) {
			throw new UnavailableException("no P-CSCF service " + e);
		}
	}

	@Override
	protected void doRequest(SipServletRequest request) throws ServletException, IOException
	{		
		try
		{
			if (request.getMethod().equals(Methods.REGISTER))
				_pcscfService.doRegister(request);
			else
			{	
				_debugIdService.handleDebug(request);
				_pcscfService.doNonRegisterRequest(request);
			}
		}
		catch (Throwable e) 
		{
			_log.warn("Failed to handle request:\n" + request, e);
		}
	}

	@Override
	protected void doResponse(SipServletResponse response) throws ServletException,
			IOException
	{
		try
		{
			if (response.getMethod().equals(Methods.REGISTER))
			{
				Iterator<String> it = response.getHeaderNames();
				while (it.hasNext())
				{
					String name = (String) it.next();
					if (name.equalsIgnoreCase(Headers.P_DEBUG_ID))
					{
						Address contact = response.getAddressHeader(Headers.CONTACT);
						int expires = -1;
						if (contact != null)
							expires = contact.getExpires();
						if (expires == -1)
							expires = response.getExpires();
						_debugIdService.subscribe(response.getFrom().getURI(), expires);
					}
				}
				response.getApplicationSession().invalidate();
			}
			else
				_debugIdService.handleDebug(response);
		}
		catch (Throwable e) 
		{
			_log.warn("Failed to handle response:\n" + response, e);
		}
	}

}
