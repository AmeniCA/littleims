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
package org.cipango.littleims.scscf.registrar;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.sip.SipServlet;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipSession;
import javax.servlet.sip.URI;

import org.apache.log4j.Logger;
import org.cipango.littleims.scscf.registrar.regevent.RegEventManager;
import org.springframework.beans.BeansException;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;


public class RegistrarServlet extends SipServlet
{

	private static final long serialVersionUID = -8870540545902150339L;

	

	public static final Logger __log = Logger.getLogger(RegistrarServlet.class);

	private RegEventManager _regEventManager;
	private Registrar _registrar;
	private Authenticator _authenticator;
	
	public void init(ServletConfig config) throws ServletException
	{
		super.init(config);

		WebApplicationContext context = WebApplicationContextUtils
		.getWebApplicationContext(getServletContext());

		try {
			_regEventManager = (RegEventManager) context.getBean("regEventManager");
			_registrar = (Registrar) context.getBean("registrar");
			_authenticator = (Authenticator) context.getBean("authenticator");
		} catch (BeansException e) {
			throw new UnavailableException("no I-CSCF service " + e);
		}
		
	}

	/**
	 * Handles REGISTER request
	 */
	public void doRegister(SipServletRequest request) throws ServletException, IOException
	{
		try
		{
			__log.debug("Received REGISTER request: \r\n" + request);
			String privateUserIdentity = _authenticator.authenticate(false, request);	
			if (privateUserIdentity != null)
				_registrar.doRegister(request, privateUserIdentity);
		}
		finally
		{
			if (request.isCommitted())
				request.getSession().invalidate();
		}
	}
	


	public void doSubscribe(SipServletRequest subscribe) throws IOException
	{
		_regEventManager.doSubscribe(subscribe);
	}

	
}
