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
import javax.servlet.sip.ServletTimer;
import javax.servlet.sip.SipServlet;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.TimerListener;

import org.cipango.diameter.DiameterCommand;
import org.cipango.diameter.api.DiameterListener;
import org.cipango.diameter.api.DiameterServletAnswer;
import org.cipango.diameter.api.DiameterServletMessage;
import org.cipango.diameter.api.DiameterServletRequest;
import org.cipango.diameter.ims.Cx;
import org.cipango.littleims.scscf.debug.DebugIdService;
import org.cipango.littleims.scscf.registrar.Authenticator;
import org.cipango.littleims.scscf.registrar.Registrar;
import org.cipango.littleims.scscf.registrar.regevent.RegEventManager;
import org.cipango.littleims.util.Headers;
import org.cipango.littleims.util.Methods;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;


public class SessionServlet extends SipServlet implements DiameterListener, TimerListener
{

	private static final long serialVersionUID = -4447425709778698992L;
	
	public static final String REGISTRAR_SERVLET_NAME = "register";
	
	private SessionManager _sessionManager;
	
	private Authenticator _authenticator;
	
	private DebugIdService _debugIdService;
	
	private Registrar _registrar;

	private static final Logger _log = LoggerFactory.getLogger(SessionServlet.class);

	public void init() throws ServletException
	{
		WebApplicationContext context = WebApplicationContextUtils
		.getWebApplicationContext(getServletContext());

		try 
		{
			_sessionManager = (SessionManager) context.getBean("sessionManager");
			_authenticator = (Authenticator) context.getBean("authenticator");
			_debugIdService = (DebugIdService) context.getBean("debugIdService");
			_registrar = (Registrar) context.getBean("registrar");
		} 
		catch (BeansException e) 
		{
			throw new UnavailableException("no session manager " + e);
		}
	}

	protected void doRequest(SipServletRequest request) throws ServletException, IOException
	{
		try
		{
			String method = request.getMethod();
			if (Methods.REGISTER.equals(method))
			{
				getServletContext().getNamedDispatcher(REGISTRAR_SERVLET_NAME).forward(request, null);
				return;
			}
			else if (Methods.SUBSCRIBE.equals(method))
			{
				String event = request.getHeader(Headers.EVENT);
				if (RegEventManager.REG_EVENT.equalsIgnoreCase(event))
				{
					request.getSession().setHandler(REGISTRAR_SERVLET_NAME);
					getServletContext().getNamedDispatcher(REGISTRAR_SERVLET_NAME).forward(request, null);
					return;
				}
				if (DebugIdService.DEBUG_EVENT.equalsIgnoreCase(event))
				{
					_debugIdService.doSubscribe(request);
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
		catch (Throwable e) 
		{
			_log.warn("Failed to handle request:\n" + request, e);
			if (!request.isCommitted() && request.isInitial())
			{
				_sessionManager.getMessageSender().sendResponse(request, SipServletResponse.SC_SERVICE_UNAVAILABLE);
			}
				
		}
	}

	protected void doResponse(SipServletResponse response) throws ServletException, IOException
	{
		try
		{
			_sessionManager.doResponse(response);
		}
		catch (Throwable e) 
		{
			_log.warn("Failed to handle response:\n" + response, e);
		}
	}

	public void handle(DiameterServletMessage message) throws IOException
	{

		DiameterCommand command = message.getCommand();
		try
		{
			if (message.isRequest())
			{
				DiameterServletRequest request = (DiameterServletRequest) message;
				if (command == Cx.PPR)
					_sessionManager.handlePpr(request);
				else if (command == Cx.RTR)
					_registrar.handleRtr(request);
				else
					_log.warn("No handler for diameter request with command " + message.getCommand());
			}
			else
			{
				DiameterServletAnswer answer = (DiameterServletAnswer) message;
				if ( command == Cx.MAA)
					_authenticator.handleMaa(answer);
				else if (command == Cx.SAA)
				{
					SipServletRequest request =  
						(SipServletRequest) answer.getRequest().getAttribute(SipServletRequest.class.getName());
					if (request == null || Methods.REGISTER.equalsIgnoreCase(request.getMethod()))
						_sessionManager.getRegistrar().handleSaa(answer);
					else
						_sessionManager.handleSaa(answer);
				}
				else
				{
					_log.warn("No handler for diameter answer with command " + command);
				}
			}
		}
		catch (Throwable e) 
		{
			_log.warn("Received unexpected exception when handing Diameter " + 
					(message.isRequest() ? "request" : "answer") + ": " + command, e);
		}
	}

	public void timeout(ServletTimer timer)
	{
		try
		{
			((Runnable) timer.getInfo()).run();
		}
		catch (Throwable e) 
		{
			_log.warn("Failed to handle timer " + timer, e);
		}
	}

		
}
