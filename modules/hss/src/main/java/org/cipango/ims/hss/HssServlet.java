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

package org.cipango.ims.hss;

import java.io.IOException;

import javax.servlet.sip.SipServlet;

import org.apache.log4j.Logger;
import org.cipango.diameter.AVP;
import org.cipango.diameter.DiameterAnswer;
import org.cipango.diameter.DiameterCommand;
import org.cipango.diameter.DiameterMessage;
import org.cipango.diameter.DiameterRequest;
import org.cipango.diameter.ResultCode;
import org.cipango.diameter.app.DiameterListener;
import org.cipango.diameter.base.Base;
import org.cipango.diameter.ims.Cx;
import org.cipango.diameter.ims.Zh;
import org.cipango.ims.hss.db.AdminUserDao;
import org.cipango.ims.hss.diameter.DiameterException;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * Handler for diameter messages.
 */
public class HssServlet extends SipServlet implements DiameterListener
{
	private static final Logger __log = Logger.getLogger(HssServlet.class);
	
	private Hss _hss;
	private ZhHandler _zhHandler;

	public void init()
	{
		ApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(getServletContext());
		_hss = (Hss) context.getBean("hss");
		_zhHandler = (ZhHandler) context.getBean("zhHandler");
		
		AdminUserDao dao = (AdminUserDao) context.getBean("adminUserDao");
		dao.insertDefaultUserIfNone();
	}
	
	public void handle(DiameterMessage message) throws IOException
	{
		if (message.isRequest())
			doRequest((DiameterRequest) message);
		else 
			doAnswer((DiameterAnswer) message);
	}
	
	protected void doRequest(DiameterRequest request) throws IOException
	{
		DiameterCommand command = request.getCommand();
		try
		{
			if (command == Cx.LIR)
				_hss.doLir(request);
			else if (command == Cx.MAR)
			{
				if (request.getApplicationId() == Cx.CX_APPLICATION)
					_hss.doMar(request);
				else if (request.getApplicationId() == Zh.ZH_APPLICATION)
					_zhHandler.doMar(request);
				else
					throw new DiameterException(Base.DIAMETER_UNABLE_TO_COMPLY, 
							"Unsupported application ID: " + request.getApplicationId());
			}
			else if (command == Cx.SAR)
				_hss.doSar(request);
			else if (command == Cx.UAR)
				_hss.doUar(request);
			else
			{
				DiameterAnswer answer = request.createAnswer(Base.DIAMETER_COMMAND_UNSUPPORTED);
				answer.send();
			}
		}
		catch (DiameterException e)
		{
			if (__log.isDebugEnabled())
			{
				ResultCode resultCode = e.getResultCode();
				if ((resultCode == Cx.DIAMETER_ERROR_USER_UNKNOWN))
					__log.debug("Unable to process request: " + command + ", Result code: " + e.getResultCode() 
							+ " Reason: " + e.getMessage());
				else
					__log.debug("Unable to process request: " + command + ", Result code: " + e.getResultCode(), e);
			}
			DiameterAnswer answer = request.createAnswer(e.getResultCode());
			if (e.getAvps() != null)
			{
				for (AVP avp : e.getAvps())
					answer.getAVPs().add(avp);
			}
			answer.send();
		}
		catch (Throwable e)
		{
			__log.warn("Unable to process request: " + command, e);
			DiameterAnswer answer = request.createAnswer(Base.DIAMETER_UNABLE_TO_COMPLY);
			answer.send();
		}
	}
	
	protected void doAnswer(DiameterAnswer answer)
	{
		DiameterCommand command = answer.getCommand();
		try
		{
			if (command == Cx.PPA)
				_hss.doPpa(answer);
			else if (command == Cx.RTA)
				_hss.doRta(answer);
			else
				__log.warn("Received unknown answer: " + command);
		}
		catch (Throwable e)
		{
			if (__log.isDebugEnabled())
				__log.debug("Unable to process request: " + command, e);
		}
	}
}
