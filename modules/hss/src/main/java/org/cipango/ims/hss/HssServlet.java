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
import org.cipango.diameter.DiameterMessage;
import org.cipango.diameter.DiameterRequest;
import org.cipango.diameter.app.DiameterListener;
import org.cipango.diameter.base.Base;
import org.cipango.diameter.ims.IMS;
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
	private ClassLoader _loader; //FIXME remove use of class loader: patch due to bug in cipango-diameter
	
	
	public void init()
	{
		ApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(getServletContext());
		_hss = (Hss) context.getBean("hss");
		_loader =  Thread.currentThread().getContextClassLoader();
	}
	
	public void handle(DiameterMessage message) throws IOException
	{
		ClassLoader oldClassLoader = null;
		Thread currentThread = null;
		
		if (_loader != null)
		{
			currentThread = Thread.currentThread();
			oldClassLoader = currentThread.getContextClassLoader();
			currentThread.setContextClassLoader(_loader);
		}

		try
		{
			if (message.isRequest())
				doRequest((DiameterRequest) message);
			else 
				doAnswer((DiameterAnswer) message);
		}
		finally
		{
			if (_loader != null)
			{
				currentThread.setContextClassLoader(oldClassLoader);
			}
		}
		
	}
	
	protected void doRequest(DiameterRequest request) throws IOException
	{
		int command = request.getCommand();
		try
		{
			switch (command) 
			{
			case IMS.LIR:
				_hss.doLir(request);
				break;
			case IMS.MAR:
				_hss.doMar(request);
				break;
			case IMS.SAR:
				_hss.doSar(request);
				break;
			case IMS.UAR:
				_hss.doUar(request);
				break;
			default:
				DiameterAnswer answer = request.createAnswer(Base.DIAMETER_COMMAND_UNSUPPORTED);
				answer.send();
				break;
			}
		}
		catch (DiameterException e)
		{
			if (__log.isDebugEnabled())
				__log.debug("Unable to process request: " + command, e);
			DiameterAnswer answer = request.createAnswer(e.getVendorId(), e.getResultCode());
			if (e.getAvps() != null)
			{
				for (AVP avp : e.getAvps())
					answer.add(avp);
			}
			answer.send();
		}
		catch (Throwable e)
		{
			if (__log.isDebugEnabled())
				__log.debug("Unable to process request: " + command, e);
			DiameterAnswer answer = request.createAnswer(Base.DIAMETER_UNABLE_TO_COMPLY);
			answer.send();
		}
	}
	
	protected void doAnswer(DiameterAnswer answer)
	{
		
	}
}
