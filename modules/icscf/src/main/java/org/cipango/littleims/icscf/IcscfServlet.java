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

import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.sip.SipServlet;
import javax.servlet.sip.SipServletRequest;

import org.apache.log4j.Logger;
import org.cipango.littleims.util.Methods;
import org.springframework.beans.BeansException;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

public class IcscfServlet extends SipServlet
{

	private static final Logger __log = Logger.getLogger(IcscfServlet.class);
	private IcscfService _service;
	public static final String ORIG_PARAM = "orig";
	public static final String TERM_PARAM = "term";	
	
	@Override
	public void init() throws ServletException
	{
		WebApplicationContext context = WebApplicationContextUtils
		.getWebApplicationContext(getServletContext());

		try {
			_service = (IcscfService) context.getBean("icscfService");
		} catch (BeansException e) {
			throw new UnavailableException("no I-CSCF service " + e);
		}
	}

	@Override
	protected void doRequest(SipServletRequest request) throws ServletException, IOException
	{
		if (Methods.REGISTER.equals(request.getMethod()))
		{
			_service.doRegister(request);
		}
		else if (isOriginating(request))
		{
			_service.doOriginatingRequest(request);
		}
		else
		{
			_service.doTerminatingRequest(request);
		}
	}
	
	private boolean isOriginating(SipServletRequest request)
	{
		String orig = request.getParameter(ORIG_PARAM);
		String term = request.getParameter(TERM_PARAM);
		__log.debug("Orig param is: *" + orig + "*. Term param is: *" + term + "*");
		if (_service.isTerminatingDefault()) // default standard mode
			return orig != null;
		else
			return term != null;
	}



}
