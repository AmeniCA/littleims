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
package org.cipango.littleims.scscf.session;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.sip.SipServlet;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;

import org.apache.log4j.Logger;
import org.cipango.diameter.DiameterAnswer;
import org.cipango.diameter.DiameterMessage;
import org.cipango.diameter.app.DiameterListener;
import org.cipango.diameter.ims.IMS;
import org.cipango.littleims.scscf.registrar.Authenticator;
import org.cipango.littleims.util.Headers;
import org.cipango.littleims.util.Methods;
import org.springframework.beans.BeansException;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;


public class SessionServlet extends SipServlet implements DiameterListener
{

	private static final long serialVersionUID = -4447425709778698992L;
	
	public static final String REGISTRAR_SERVLET_NAME = "register";
	
	private SessionManager _sessionManager;
	
	private Authenticator _authenticator;


	private static final Logger __log = Logger.getLogger(SessionServlet.class);

	public void init() throws ServletException
	{
		WebApplicationContext context = WebApplicationContextUtils
		.getWebApplicationContext(getServletContext());

		try 
		{
			_sessionManager = (SessionManager) context.getBean("sessionManager");
			_authenticator = (Authenticator) context.getBean("authenticator");
		} 
		catch (BeansException e) 
		{
			throw new UnavailableException("no session manager " + e);
		}
	}

	protected void doRequest(SipServletRequest request) throws ServletException, IOException
	{
		String method = request.getMethod();
		if (Methods.REGISTER.equals(method))
		{
			getServletContext().getNamedDispatcher(REGISTRAR_SERVLET_NAME).forward(request, null);
			return;
		}
		else if (Methods.SUBSCRIBE.equals(method))
		{
			String event = request.getHeader(Headers.EVENT_HEADER);
			if ("reg".equalsIgnoreCase(event))
			{
				request.getSession().setHandler(REGISTRAR_SERVLET_NAME);
				getServletContext().getNamedDispatcher(REGISTRAR_SERVLET_NAME).forward(request, null);
				return;
			}
		}
		
		if (request.isInitial())
		{
			_sessionManager.doInitialRequest(request);
		}
		else
		{
			_sessionManager.doSubsequentRequest(request);
		}
	}

	protected void doResponse(SipServletResponse response) throws ServletException, IOException
	{
		_sessionManager.doResponse(response);
	}

	public void handle(DiameterMessage message) throws IOException
	{
		if (message.isRequest())
		{
			__log.warn("No handler for diameter request with command " + message.getCommand());
		}
		else
		{
			DiameterAnswer answer = (DiameterAnswer) message;
			int command = message.getCommand();
			if ( command == IMS.MAA)
				_authenticator.handleMaa(answer);
			else if (command == IMS.SAA)
			{
				SipServletRequest request =  
					(SipServletRequest) answer.getRequest().getAttribute(SipServletRequest.class.getName());
				if (Methods.REGISTER.equalsIgnoreCase(request.getMethod()))
					_sessionManager.getRegistrar().handleSaa(answer);
				else
					_sessionManager.handleSaa(answer);
			}
			else
			{
				__log.warn("No handler for diameter answer with command " + command);
			}
		}
	}

		
}
