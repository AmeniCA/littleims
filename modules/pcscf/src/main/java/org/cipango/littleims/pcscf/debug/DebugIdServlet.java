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
package org.cipango.littleims.pcscf.debug;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.sip.SipServlet;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;

import org.apache.log4j.Logger;
import org.cipango.littleims.util.Headers;

public class DebugIdServlet extends SipServlet
{
	
	private final Logger _log = Logger.getLogger(DebugIdServlet.class);

	@Override
	protected void doNotify(SipServletRequest request) throws ServletException,
			IOException
	{
		SipServletResponse response = request.createResponse(SipServletResponse.SC_OK);
		
		DebugSubscription subscription = 
			(DebugSubscription) request.getApplicationSession().getAttribute(DebugSubscription.class.getName());
		if (subscription == null)
			_log.warn("No subscription session found for\n" + request);
		else
		{
			String userAgent = subscription.getDebugIdService().getUserAgent();
			if (userAgent != null)
				response.setHeader(Headers.SERVER, userAgent);
			subscription.handleNotify(request);
		}
		response.send();
		// TODO refresh if expired
		
	}

	@Override
	protected void doResponse(SipServletResponse response) throws ServletException,
			IOException
	{
		DebugSubscription subscription = 
			(DebugSubscription) response.getApplicationSession().getAttribute(DebugSubscription.class.getName());
		if (subscription != null)
			subscription.handleSubscribeResponse(response);		
	}

	
}
