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
package org.cipango.littleims.scscf.debug;

import java.io.IOException;
import java.io.Serializable;

import javax.servlet.ServletException;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.TimerService;

import org.apache.log4j.Logger;
import org.cipango.littleims.scscf.data.UserProfile;
import org.cipango.littleims.scscf.data.UserProfileCache;
import org.cipango.littleims.scscf.registrar.regevent.RegEventManager;
import org.cipango.littleims.scscf.util.MessageSender;
import org.cipango.littleims.util.Headers;

public class DebugIdService
{
	private static final int DEFAULT_EXPIRES = 43200;
	public static final String DEBUG_EVENT = "debug";
	private final Logger _log = Logger.getLogger(RegEventManager.class);
	
	private int _minExpires;
	private int _maxExpires;
	private UserProfileCache _userProfileCache;
	private TimerService _timerService;
	private MessageSender _messageSender;

	public void doSubscribe(SipServletRequest subscribe) throws ServletException, IOException
	{
		DebugSession session = (DebugSession) subscribe.getSession().getAttribute(DebugSession.class.getName());
		
		// TODO check if user if authorized
		int expires = subscribe.getExpires();
		if (expires == -1)
		{
			// no expires values specified, use default value
			expires = DEFAULT_EXPIRES;
		}
		// check that expires is shorter than minimum value
		if (expires != 0 && _minExpires != -1 && expires < _minExpires)
		{
			_log.info("Debug subscription expiration (" + expires + ") is shorter"
					+ " than minimum value (" + _minExpires + "). Sending 423 response");
			_messageSender.sendResponse(subscribe, SipServletResponse.SC_INTERVAL_TOO_BRIEF,
					Headers.MIN_EXPIRES, String.valueOf(_minExpires));
			return;
		}

		// if expires is longer than maximum value, reduce it
		if (_maxExpires != -1 && expires > _maxExpires)
		{
			_log.info("Debug subscription expiration (" + expires + ") is greater"
					+ " than maximum value (" + _maxExpires
					+ "). Setting expires to max expires");
			expires = _maxExpires;
		}

		SipServletResponse response = subscribe.createResponse(SipServletResponse.SC_OK);
		if (_messageSender.getUserAgent() != null)
			response.setHeader(Headers.SERVER, _messageSender.getUserAgent());
		response.setExpires(expires);
		response.send();
		
		// Wait a little to ensure 200/SUBSCRIBE is received before NOTIFY
		try { Thread.sleep(100); } catch (Exception e) {}
		
		if (session == null)
		{	
			session = new DebugSession(subscribe.getSession(), expires);
			session.setAor(subscribe.getRequestURI().toString());
			_timerService.createTimer(subscribe.getApplicationSession(), 
					expires, false, new ExpirationTask(session, _timerService));
		}
		else
		{
			session.setExpires(expires);
		}
		
		UserProfile profile = _userProfileCache.getProfile(session.getAor(), null);
		if (profile != null)
		{
			profile.addListener(session);
			session.sendNotify(profile.getServiceLevelTraceInfo());
		}
		else
			session.sendNotify(null);
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



	public UserProfileCache getUserProfileCache()
	{
		return _userProfileCache;
	}



	public void setUserProfileCache(UserProfileCache userProfileCache)
	{
		_userProfileCache = userProfileCache;
	}
	public TimerService getTimerService()
	{
		return _timerService;
	}



	public void setTimerService(TimerService timerService)
	{
		_timerService = timerService;
	}

	static class ExpirationTask implements Runnable, Serializable
	{
		private DebugSession _session;
		private TimerService _timerService;
		
		public ExpirationTask(DebugSession session, TimerService timerService)
		{	
			_session = session;
			_timerService = timerService;
		}
		public void run()
		{
			int expires = _session.getExpires();
			if (expires == 0)
				_session.sendNotify(null);
			else
				_timerService.createTimer(_session.getSipSession().getApplicationSession(), 
						expires, false, this);
		}
		
	}

	public MessageSender getMessageSender()
	{
		return _messageSender;
	}



	public void setMessageSender(MessageSender messageSender)
	{
		_messageSender = messageSender;
	}

}
