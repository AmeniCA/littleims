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
package org.cipango.littleims.scscf.registrar.regevent;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Timer;

import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipSession;
import javax.servlet.sip.URI;

import org.apache.log4j.Logger;
import org.cipango.littleims.scscf.registrar.Registrar;
import org.cipango.littleims.scscf.registrar.regevent.RegInfo.ContactInfo;
import org.cipango.littleims.util.Headers;
import org.cipango.littleims.util.Methods;


public class RegEventManager implements Runnable, RegEventListener
{

	private static final Logger __log = Logger.getLogger(RegEventManager.class);
	private static final String ABSOLUTE_EXPIRES = "absoluteExpires";
	private static final String NOTIFY_VERSION = "notifyVersion";
	private static final String REG_INFO_CONTENT_TYPE = "application/reginfo+xml";
	public static final String REG_EVENT = "reg";
	

	private Timer _timer = new Timer("RegEventTimer");
	
	private Map<String, SipSession> _subscriptions = new HashMap<String, SipSession>();
	private LinkedList<RegEvent> _queue = new LinkedList<RegEvent>();
	private Registrar _registrar;

	private int _minExpires;
	private int _maxExpires;

	public void start()
	{
		new Thread(this, "RegEventManager").start();
	}

	public void registrationEvent(RegEvent e)
	{
		synchronized (_queue)
		{
			_queue.addLast(e);
			_queue.notifyAll();
		}
	}
	
	public void doSubscribe(SipServletRequest subscribe) throws IOException
	{
		// TODO check if user if authorized
		int expires = subscribe.getExpires();
		if (expires == -1)
		{
			// no expires values specified, use default value
			expires = Registrar.DEFAULT_EXPIRES;
		}
		// check that expires is shorter than minimum value
		if (expires != 0 && _minExpires != -1 && expires < _minExpires)
		{
			__log.info("Reg event subscription expiration (" + expires + ") is shorter"
					+ " than minimum value (" + _minExpires + "). Sending 423 response");
			SipServletResponse response = 
				subscribe.createResponse(SipServletResponse.SC_INTERVAL_TOO_BRIEF);
			response.setHeader(Headers.MIN_EXPIRES, String.valueOf(_minExpires));
			response.send();
			subscribe.getApplicationSession().invalidate();
			return;
		}

		// if expires is longer than maximum value, reduce it
		if (_maxExpires != -1 && expires > _maxExpires)
		{
			__log.info("Reg event subscription expiration (" + expires + ") is greater"
					+ " than maximum value (" + _maxExpires
					+ "). Setting expires to max expires");
			expires = _maxExpires;
		}

		SipServletResponse response = subscribe.createResponse(SipServletResponse.SC_OK);
		response.setExpires(expires);
		response.send();
		// Wait a little to ensure 200/SUBSCRIBE is received before NOTIFY
		try
		{
			Thread.sleep(100);
		}
		catch (Exception e)
		{
		}

		URI aor = subscribe.getRequestURI();
		SipSession session = subscribe.getSession();

		addSubscription(aor.toString(), session, expires);
	}

	private void addSubscription(String aor, SipSession session, int expires)
	{
		synchronized (_subscriptions)
		{
			if (expires != 0)
			{
				_subscriptions.put(aor, session);
			}
			else
			{
				Object o = _subscriptions.get(aor);
				if (session.equals(o))
				{
					_subscriptions.remove(aor);
				}
			}
		}

		session.setAttribute(ABSOLUTE_EXPIRES,
				new Date(System.currentTimeMillis() + expires * 1000));
		RegInfo regInfo = _registrar.getBindings(aor);
		RegEvent e = new RegEvent();
		e.addRegInfo(regInfo);
		sendNotification(e, session, "full");

		ExpiryTask task = (ExpiryTask) session.getAttribute(ExpiryTask.class.getName());

		if (task != null)
		{
			task.cancel();
		}

		if (expires == 0)
		{
			session.getApplicationSession().invalidate();
		}
		else
		{
			task = new ExpiryTask(session, this);
			_timer.schedule(task, expires * 1000);
			session.setAttribute(ExpiryTask.class.getName(), task);
			session.getApplicationSession().setExpires(expires / 60 + 2);
		}
	}

	public void run()
	{
		while (true)
		{
			try
			{
				RegEvent e;
				synchronized (_queue)
				{
					while (_queue.size() == 0)
					{
						try
						{
							_queue.wait();
						}
						catch (InterruptedException _)
						{
						}
					}
					e = _queue.removeFirst();
				}
	
				Iterator<RegInfo> it = e.getRegInfos().iterator();
				while (it.hasNext())
				{
					RegInfo regInfo = it.next();
					String aor = regInfo.getAor();
					synchronized (_subscriptions)
					{
						if (_subscriptions.containsKey(aor))
						{
							SipSession session = (SipSession) _subscriptions.get(aor);
							sendNotification(e, session, "partial");
						}
					}
				}
			} 
			catch (Throwable e)
			{
				__log.warn("Got unexpected exception on regEvent manager", e);
			}
		}
	}

	protected void sendNotification(RegEvent e, SipSession session, String state)
	{
		try
		{
			session.setAttribute(ExpiryTask.LAST_EVENT, e);
			final SipServletRequest notify = session.createRequest(Methods.NOTIFY);
			notify.setHeader(Headers.EVENT, REG_EVENT);
			notify.setHeader(Headers.SUBSCRIPTION_STATE, getSubscriptionState(session));
			Integer version = (Integer) session.getAttribute(NOTIFY_VERSION);
			if (version == null)
				version = new Integer(0);
			else
				version = new Integer(version.intValue() + 1);
			
			session.setAttribute(NOTIFY_VERSION, version);

			String body = generateRegInfo(e, state, version.intValue());
			byte[] content = body.getBytes();
			notify.setContent(content, REG_INFO_CONTENT_TYPE);
			notify.send();			
		}
		catch (Exception ex)
		{
			__log.warn("Failed to send NOTIFY", ex);
		}
	}

	private String getSubscriptionState(SipSession session)
	{
		Date absExpiry = (Date) session.getAttribute(ABSOLUTE_EXPIRES);
		int expires = (int) (absExpiry.getTime() - System.currentTimeMillis()) / 1000;
		if (expires <= 0)
		{
			return "terminated;reason=timeout";
		}
		else
		{
			return "active;expires=" + expires;
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
			sb.append("id=\"").append("123").append("\" ");
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

	protected void removeSubscription(String aor)
	{
		synchronized (_subscriptions)
		{
			_subscriptions.remove(aor);
		}
	}

	public Registrar getRegistrar()
	{
		return _registrar;
	}

	public void setRegistrar(Registrar registrar)
	{
		_registrar = registrar;
		_registrar.setListener(this);
	}

	public int getMinExpires()
	{
		return _minExpires;
	}

	public void setMinExpires(int minExpires)
	{
		_minExpires = minExpires;
	}

	public int getMaxExpires()
	{
		return _maxExpires;
	}

	public void setMaxExpires(int maxExpires)
	{
		_maxExpires = maxExpires;
	}
}
