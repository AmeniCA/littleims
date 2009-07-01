package org.cipango.littleims.scscf.debug;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;

import org.apache.log4j.Logger;
import org.cipango.littleims.scscf.data.UserProfile;
import org.cipango.littleims.scscf.data.UserProfileCache;
import org.cipango.littleims.scscf.registrar.regevent.RegEventManager;
import org.cipango.littleims.util.Headers;
import org.cipango.littleims.util.Methods;

public class DebugIdService
{
	private static final int DEFAULT_EXPIRES = 43200;
	private static final String DEBUG_INFO_CONTENT_TYPE = "application/debuginfo+xml";
	public static final String DEBUG_EVENT = "debug";
	private final Logger _log = Logger.getLogger(RegEventManager.class);
	
	private int _minExpires;
	private int _maxExpires;
	private UserProfileCache _userProfileCache;

	

	public void doSubscribe(SipServletRequest subscribe) throws ServletException, IOException
	{
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
			SipServletResponse response = 
				subscribe.createResponse(SipServletResponse.SC_INTERVAL_TOO_BRIEF);
			response.setHeader(Headers.MIN_EXPIRES_HEADER, String.valueOf(_minExpires));
			response.send();
			subscribe.getApplicationSession().invalidate();
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
		response.setExpires(expires);
		response.send();
		// Wait a little to ensure 200/SUBSCRIBE is received before NOTIFY
		try { Thread.sleep(100); } catch (Exception e) {}
		
		SipServletRequest notify = response.getSession().createRequest(Methods.NOTIFY);
		notify.setExpires(expires);
		UserProfile profile = _userProfileCache.getProfile(subscribe.getRequestURI().toString(), null);
		notify.setContent(profile.getServiceLevelTraceInfo().getBytes(), DEBUG_INFO_CONTENT_TYPE);
		notify.setHeader(Headers.EVENT_HEADER, DEBUG_EVENT);
		notify.setHeader(Headers.SUBSCRIPTION_STATE, "active;expires=" + expires);
		notify.send();
		
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

}
