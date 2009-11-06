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
package org.cipango.littleims.scscf.registrar.regevent;

import java.util.Iterator;

import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipSession;

import org.apache.log4j.Logger;
import org.cipango.littleims.scscf.registrar.Context.RegState;
import org.cipango.littleims.scscf.registrar.regevent.RegInfo.ContactInfo;
import org.cipango.littleims.util.Headers;
import org.cipango.littleims.util.Methods;

public class RegSubscription
{
	private static final Logger __log = Logger.getLogger(RegEventManager.class);

	private static final String REG_INFO_CONTENT_TYPE = "application/reginfo+xml";
	
	private SipSession _session;
	private RegEvent _lastEvent;
	private long _absoluteExpires;
	private int _version;
	private ExpiryTask _expiryTask;
	private String _subscriberUri;
	
	public RegSubscription(SipSession session, int expires, String subscriberUri)
	{
		_session = session;
		_absoluteExpires = System.currentTimeMillis() + expires * 1000;
		_version = 0;
		_subscriberUri = subscriberUri;
	}
	
	public SipSession getSession()
	{
		return _session;
	}
	public RegEvent getLastEvent()
	{
		return _lastEvent;
	}
	public int getVersion()
	{
		return _version;
	}
	public void sendNotification(RegEvent e, String state)
	{
		try
		{
			_lastEvent = e;
			SipServletRequest notify = _session.createRequest(Methods.NOTIFY);
			notify.setHeader(Headers.EVENT, RegEventManager.REG_EVENT);
			notify.setHeader(Headers.SUBSCRIPTION_STATE, getSubscriptionState(e.isTerminated()));

			String body = generateRegInfo(e, state, _version++);
			byte[] content = body.getBytes();
			notify.setContent(content, REG_INFO_CONTENT_TYPE);
			notify.send();			
		}
		catch (Exception ex)
		{
			__log.warn("Failed to send NOTIFY", ex);
		}
	}
	
	private String generateRegInfo(RegEvent e, String state, int version)
	{
		StringBuilder sb = new StringBuilder();
		sb.append("<?xml version=\"1.0\"?>\n");
		sb.append("<reginfo xmlns=\"urn:ietf:params:xml:ns:reginfo\" ");
		sb.append("version=\"").append(version).append("\" ");
		sb.append("state=\"").append(state).append("\">\n");
		
		Iterator<RegInfo> it = e.getRegInfos().iterator();
		while (it.hasNext())
		{
			RegInfo regInfo = it.next();
			sb.append("<registration aor=\"").append(regInfo.getAor()).append("\" ");
			sb.append("id=\"").append(regInfo.getAor().hashCode()).append("\" ");
			sb.append("state=\"").append(regInfo.getAorState().getValue()).append("\">\n");

			Iterator<ContactInfo> it2 = regInfo.getContacts().iterator();
			while (it2.hasNext())
			{
				ContactInfo contactInfo = it2.next();
				sb.append("<contact state=\"").append(contactInfo.getContactState().getValue()).append("\" ");
				sb.append("event=\"").append(contactInfo.getContactEvent().getValue()).append("\"");
				if (contactInfo.getContactState() == RegState.ACTIVE)
					sb.append(" expires=\"" + contactInfo.getExpires() + "\"");
				sb.append(">\n");
				sb.append("<uri>").append(contactInfo.getContact()).append("</uri>\n");
				if (contactInfo.getDisplayName() != null)
					sb.append("<display-name>").append(contactInfo.getDisplayName()).append("</display-name>\n");
				sb.append("</contact>\n");
			}
			sb.append("</registration>\n");
		}
		sb.append("</reginfo>\n");
		return sb.toString();
	}
	
	private String getSubscriptionState(boolean terminated)
	{
		if (terminated)
			return "terminated";
		
		int expires = getExpires();
		if (expires <= 0)
			return "terminated;reason=timeout";
		else
			return "active;expires=" + expires;
	}
	
	public int getExpires()
	{
		return (int) (_absoluteExpires - System.currentTimeMillis()) / 1000;
	}

	public ExpiryTask getExpiryTask()
	{
		return _expiryTask;
	}

	public void setExpiryTask(ExpiryTask expiryTask)
	{
		_expiryTask = expiryTask;
	}

	public String getSubscriberUri()
	{
		return _subscriberUri;
	}
	
}
