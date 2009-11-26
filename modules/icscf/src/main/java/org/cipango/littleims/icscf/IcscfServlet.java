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
import javax.servlet.sip.SipServletResponse;

import org.apache.log4j.Logger;
import org.cipango.diameter.DiameterAnswer;
import org.cipango.diameter.DiameterCommand;
import org.cipango.diameter.DiameterMessage;
import org.cipango.diameter.app.DiameterListener;
import org.cipango.diameter.ims.Cx;
import org.cipango.littleims.util.Methods;
import org.springframework.beans.BeansException;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

public class IcscfServlet extends SipServlet implements DiameterListener
{

	private static final Logger __log = Logger.getLogger(IcscfServlet.class);
	private IcscfService _service;
	
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
		try
		{
			if (Methods.REGISTER.equals(request.getMethod()))
			{
				_service.doRegister(request);
			}
			else if (!Methods.CANCEL.equals(request.getMethod()))
			{
				_service.doRequest(request);
			}
		}
		catch (Throwable e) 
		{
			__log.warn("Failed to handle request:\n" + request, e);
			if (!request.isCommitted())
			{
				_service.sendResponse(request, SipServletResponse.SC_SERVICE_UNAVAILABLE);
			}
		}
	}


	public void handle(DiameterMessage message) throws IOException
	{
		try
		{
			if (message.isRequest())
			{
				__log.warn("No handler for diameter request with command " + message.getCommand());
			}
			else
			{
				DiameterAnswer answer = (DiameterAnswer) message;
				DiameterCommand command = message.getCommand();
				if ( command == Cx.UAA)
					_service.handleUAA(answer);
				else if (command == Cx.LIA)
				{
					_service.handleLIA(answer);
				}
				else
				{
					__log.warn("No handler for diameter answer with command " + command);
				}
			}
		}
		catch (Throwable e) 
		{
			__log.warn("Received unexpected exception when handing Diameter " + 
					(message.isRequest() ? "request" : "answer") + ": " + message.getCommand(), e);
		}
	}



}
