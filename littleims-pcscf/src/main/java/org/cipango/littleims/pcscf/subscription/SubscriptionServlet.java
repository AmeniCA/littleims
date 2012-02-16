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
package org.cipango.littleims.pcscf.subscription;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.sip.SipServlet;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;

import org.cipango.littleims.util.Headers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SubscriptionServlet extends SipServlet
{
	
	private static final Logger _log = LoggerFactory.getLogger(SubscriptionServlet.class);

	@Override
	protected void doNotify(SipServletRequest request) throws ServletException,
			IOException
	{
		
		Subscription subscription = 
			(Subscription) request.getApplicationSession().getAttribute(Subscription.class.getName());
		if (subscription == null)
		{
			_log.warn("No subscription session found for\n" + request);
			request.createResponse(SipServletResponse.SC_CALL_LEG_DONE).send();
		}
		else
		{

			SipServletResponse response = request.createResponse(SipServletResponse.SC_OK);
			String userAgent = subscription.getUserAgent();
			if (userAgent != null)
				response.setHeader(Headers.SERVER, userAgent);
			subscription.handleNotify(request);
			response.send();
			
			String state = request.getHeader(Headers.SUBSCRIPTION_STATE);
			if (state != null && state.startsWith("terminated"))
				subscription.invalidate();
		}
		// TODO refresh if expired
	}

	@Override
	protected void doResponse(SipServletResponse response) throws ServletException,
			IOException
	{
		
		Subscription subscription = 
			(Subscription) response.getApplicationSession().getAttribute(Subscription.class.getName());
		
		if (subscription != null)
		{
			if (response.getStatus() > SipServletResponse.SC_MULTIPLE_CHOICES)
			{
				_log.warn(subscription.getClass().getSimpleName() + " to "
						+ subscription.getAor() + " failed: " 
						+ response.getStatus() + " " + response.getReasonPhrase());
				subscription.invalidate();
				// TODO if it is 481, do a new subscription
			}
			else
				response.getApplicationSession().setExpires(response.getExpires() / 60 + 30);
		}
		else
		{
			_log.warn("No subscription session found for\n" + response);
		}	
	}

	
}
