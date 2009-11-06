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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;

import javax.servlet.sip.Address;
import javax.servlet.sip.ServletParseException;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipSession;
import javax.servlet.sip.URI;

import org.apache.log4j.Logger;
import org.cipango.littleims.scscf.registrar.Registrar;
import org.cipango.littleims.util.Headers;


public class RegEventManager implements RegEventListener
{

	private static final Logger __log = Logger.getLogger(RegEventManager.class);
	public static final String REG_EVENT = "reg";
	

	private Timer _timer = new Timer("RegEventTimer");
	
	private Map<String, List<RegSubscription>> _subscriptions = new HashMap<String, List<RegSubscription>>();
	private Registrar _registrar;

	private int _minExpires;
	private int _maxExpires;


	public void registrationEvent(RegEvent e)
	{
		try
		{
			Iterator<RegInfo> it = e.getRegInfos().iterator();
			while (it.hasNext())
			{
				RegInfo regInfo = it.next();
				String aor = regInfo.getAor();
				synchronized (_subscriptions)
				{
					List<RegSubscription> l = _subscriptions.get(aor);
					if (l != null)
					{
						Iterator<RegSubscription> it2 = l.iterator();
						while (it2.hasNext())
							it2.next().sendNotification(e, "partial");	
					}
					if (e.isTerminated())
						_subscriptions.remove(aor);
				}
			}
		} 
		catch (Throwable e1)
		{
			__log.warn("Got unexpected exception on regEvent manager", e1);
		}
	}
	
	public List<RegSubscription> getSubscriptions(String aor)
	{
		synchronized (_subscriptions)
		{
			return _subscriptions.get(aor);
		}
	}
	
	public void doSubscribe(SipServletRequest subscribe) throws IOException, ServletParseException
	{
		Address subscriber = subscribe.getAddressHeader(Headers.P_ASSERTED_IDENTITY);
		if (subscriber == null)
			subscriber = subscribe.getFrom();
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

		addSubscription(aor.toString(), session, expires, subscriber.getURI());
	}

	private void addSubscription(String aor, SipSession session, int expires, URI subscriberUri)
	{
		RegEvent e = _registrar.getFullRegEvent(aor);
		if (e.isTerminated())
			expires = 0;
		
		RegSubscription subscription = new RegSubscription(session, expires, subscriberUri.toString());
		synchronized (_subscriptions)
		{
			List<RegSubscription> l = _subscriptions.get(aor);
			if (expires != 0)
			{
				if (l == null)
				{
					l = new ArrayList<RegSubscription>();
					_subscriptions.put(aor, l);
				}
				l.add(subscription);
			}
			else if (l != null)
			{
				Iterator<RegSubscription> it = l.iterator();
				while (it.hasNext())
				{
					if (it.next().getSession().equals(session))
						it.remove();
				}
				if (l.isEmpty())
					_subscriptions.remove(aor);
			}
		}

		
		subscription.sendNotification(e, "full");

		ExpiryTask task = subscription.getExpiryTask();

		if (task != null)
			task.cancel();

		if (expires == 0)
			session.getApplicationSession().invalidate();
		else
		{
			task = new ExpiryTask(subscription, this);
			_timer.schedule(task, expires * 1000);
			subscription.setExpiryTask(task);
			session.getApplicationSession().setExpires(expires / 60 + 2);
		}
	}


	protected void removeSubscription(String aor, RegSubscription subscription)
	{
		synchronized (_subscriptions)
		{
			List<RegSubscription> l = _subscriptions.get(aor);
			l.remove(subscription);
			if (l.isEmpty())
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
