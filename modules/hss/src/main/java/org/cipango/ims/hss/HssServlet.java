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

import org.cipango.diameter.DiameterAnswer;
import org.cipango.diameter.DiameterMessage;
import org.cipango.diameter.DiameterRequest;
import org.cipango.diameter.app.DiameterListener;
import org.cipango.diameter.base.Base;
import org.cipango.diameter.ims.IMS;
import org.cipango.ims.hss.diameter.DiameterException;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * Handles for diameter messages.
 */
public class HssServlet extends SipServlet implements DiameterListener
{
	private Hss _hss;
	
	public void init()
	{
		ApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(getServletContext());
		_hss = (Hss) context.getBean("hss");
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
		int command = request.getCommand();
		try
		{
			switch (command) 
			{
			case IMS.MAR:
				_hss.doMar(request);
				break;
			case IMS.SAR:
				_hss.doSar(request);
				break;
			default:
				DiameterAnswer answer = request.createAnswer(Base.DIAMETER_COMMAND_UNSUPPORTED);
				answer.send();
				break;
			}
		}
		catch (DiameterException e)
		{
			DiameterAnswer answer = request.createAnswer(e.getVendorId(), e.getResultCode());
			answer.send();
		}
		catch (Exception e)
		{
			DiameterAnswer answer = request.createAnswer(Base.DIAMETER_UNABLE_TO_COMPLY);
			answer.send();
		}
	}
	
	protected void doAnswer(DiameterAnswer answer)
	{
		
	}
}
