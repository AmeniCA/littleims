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

import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.sip.SipServlet;
import javax.servlet.sip.SipServletRequest;

import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

public class PcscfServlet extends SipServlet
{
	private PcscfService _pcscfService;
	private final static Logger __log = Logger.getLogger(PcscfServlet.class);
	
	@Override
	public void init() throws ServletException
	{
		WebApplicationContext context = WebApplicationContextUtils
		.getWebApplicationContext(getServletContext());

		try {
			_pcscfService = (PcscfService) context.getBean("pcscfService");
		} catch (BeansException e) {
			throw new UnavailableException("no I-CSCF service " + e);
		}
	}

	@Override
	protected void doRequest(SipServletRequest request) throws ServletException, IOException
	{		
		if (request.getMethod().equals("REGISTER"))
			_pcscfService.doRegister(request);
		else
			_pcscfService.doNonRegisterRequest(request);
		
	}

}
