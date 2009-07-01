package org.cipango.littleims.pcscf.debug;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.sip.SipServlet;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;

import org.apache.log4j.Logger;

public class DebugIdServlet extends SipServlet
{
	
	private final Logger _log = Logger.getLogger(DebugIdServlet.class);

	@Override
	protected void doNotify(SipServletRequest request) throws ServletException,
			IOException
	{
		request.createResponse(SipServletResponse.SC_OK).send();
		DebugSubscription subscription = 
			(DebugSubscription) request.getApplicationSession().getAttribute(DebugSubscription.class.getName());
		if (subscription == null)
			_log.warn("No subscription session found for\n" + request);
		else
			subscription.handleNotify(request);
		
		// TODO refresh if expired
		
	}

	@Override
	protected void doResponse(SipServletResponse response) throws ServletException,
			IOException
	{
		if (response.getStatus() > SipServletResponse.SC_MULTIPLE_CHOICES)
			_log.warn("Subscription to " + response.getTo().getURI() + " failed: " 
					+ response.getStatus() + " " + response.getReasonPhrase());
		
	}

	
}
