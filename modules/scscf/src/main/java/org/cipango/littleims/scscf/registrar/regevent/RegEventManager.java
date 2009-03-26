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

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Timer;

import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipSession;

import org.apache.log4j.Logger;
import org.cipango.littleims.scscf.registrar.Registrar;


public class RegEventManager implements Runnable, RegEventListener
{

	private Map<String, SipSession> subscriptions = new HashMap<String, SipSession>();
	private LinkedList<RegEvent> queue = new LinkedList<RegEvent>();
	private Registrar _registrar;

	private RegEventGenerator regEventGen;

	private static final Logger log = Logger.getLogger(RegEventManager.class);
	private static final String ABSOLUTE_EXPIRES = "absoluteExpires";
	private static final String NOTIFY_VERSION = "notifyVersion";

	private static Timer timer = new Timer();
	
	public RegEventManager() throws Exception
	{
		regEventGen = new RegEventGenerator();
	}

	public void start()
	{
		new Thread(this).start();
	}

	public void registrationEvent(RegEvent e)
	{
		synchronized (queue)
		{
			queue.addLast(e);
			queue.notifyAll();
		}
	}

	public void addSubscription(String aor, SipSession session, int expires)
	{
		if (expires == -1)
		{
			expires = 3600;
		}

		synchronized (subscriptions)
		{
			if (expires != 0)
			{
				subscriptions.put(aor, session);
			}
			else
			{
				Object o = subscriptions.get(aor);
				if (session.equals(o))
				{
					subscriptions.remove(aor);
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
			timer.schedule(task, expires * 1000);
			session.setAttribute(ExpiryTask.class.getName(), task);
			session.getApplicationSession().setExpires(expires / 60 + 2);
		}
	}

	public void run()
	{
		while (true)
		{
			RegEvent e;
			synchronized (queue)
			{
				while (queue.size() == 0)
				{
					try
					{
						queue.wait();
					}
					catch (InterruptedException _)
					{
					}
				}
				e = queue.removeFirst();
			}

			Iterator<RegInfo> it = e.getRegInfos().iterator();
			while (it.hasNext())
			{
				RegInfo regInfo = it.next();
				String aor = regInfo.getAor();
				synchronized (subscriptions)
				{
					if (subscriptions.containsKey(aor))
					{
						SipSession session = (SipSession) subscriptions.get(aor);
						sendNotification(e, session, "partial");
					}
				}
			}
		}
	}

	protected void sendNotification(RegEvent e, SipSession session, String state)
	{
		try
		{
			session.setAttribute(ExpiryTask.LAST_EVENT, e);
			final SipServletRequest notify = session.createRequest("NOTIFY");
			notify.setHeader("Event", "reg");
			notify.setHeader("Subscription-State", getSubscriptionState(session));
			Integer version = (Integer) session.getAttribute(NOTIFY_VERSION);
			if (version == null)
			{
				version = new Integer(0);
			}
			else
			{
				version = new Integer(version.intValue() + 1);
			}
			session.setAttribute(NOTIFY_VERSION, version);

			String body = regEventGen.generateRegInfo(e, state, version.intValue());
			byte[] content = body.getBytes();
			notify.setContent(content, "application/reginfo+xml");
			notify.send();			
		}
		catch (Exception ex)
		{
			log.warn("Failed to send NOTIFY", ex);
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

	protected void removeSubscription(String aor)
	{
		synchronized (subscriptions)
		{
			subscriptions.remove(aor);
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
}
