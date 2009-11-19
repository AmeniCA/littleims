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

import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipSession;

import org.apache.log4j.Logger;
import org.cipango.littleims.scscf.data.UserProfile;
import org.cipango.littleims.scscf.data.UserProfileListener;
import org.cipango.littleims.util.Headers;
import org.cipango.littleims.util.Methods;

public class DebugSession implements UserProfileListener
{

	private static final String DEBUG_INFO_CONTENT_TYPE = "application/debuginfo+xml";
	private static final String EMPTY_SERVICE_LEVEL_TRACE_INFO = 
		"<debuginfo xmlns=\"urn:ietf:params:xml:ns:debuginfo\" version=\"0\" state=\"full\"/>";
	private static final Logger __log = Logger.getLogger(DebugSession.class);
	
	private int _version;
	private SipSession _session;
	private long _absoluteExpires;
	private String _aor;
	private String _subscriberUri;
	private UserProfile _userProfile;
	
	public DebugSession(SipSession session, int expires, String subscriberUri)
	{
		_session = session;
		setExpires(expires);
		_version = 0;
		session.setAttribute(DebugSession.class.getName(), this);
		_subscriberUri = subscriberUri;
	}
	
	protected void sendNotify(String serviceLevelTraceInfo)
	{
		try
		{
			SipServletRequest notify = _session.createRequest(Methods.NOTIFY);
			if (serviceLevelTraceInfo == null)
				serviceLevelTraceInfo = EMPTY_SERVICE_LEVEL_TRACE_INFO;
			
			serviceLevelTraceInfo = serviceLevelTraceInfo.replace(
					"version=\"0\"", "version=\"" + (_version++) + "\"");
			notify.setContent(serviceLevelTraceInfo.getBytes(),
					DEBUG_INFO_CONTENT_TYPE);
			notify.setHeader(Headers.EVENT, DebugIdService.DEBUG_EVENT);
			int expires = getExpires();
			if (expires == 0)
				notify.setHeader(Headers.SUBSCRIPTION_STATE,
						"terminated;reason=timeout");
			else
				notify.setHeader(Headers.SUBSCRIPTION_STATE, "active;expires="
						+ expires);
			notify.send();
			if (expires == 0)
			{
				if (_userProfile != null)
					_userProfile.removeListener(this);
				_session.getApplicationSession().invalidate();
			}
			
			__log.debug("Send NOTIFY No " + (_version - 1) + " for resource " 
					+ _aor + " and to " + _subscriberUri + " for debug event");
		}
		catch (Exception e)
		{
			__log.warn("Failed to send NOTIFY for user " + _aor, e);
		}
	}

	public SipSession getSipSession()
	{
		return _session;
	}
	
	public int getExpires()
	{
		int expires = (int) (_absoluteExpires - System.currentTimeMillis()) / 1000;
		if (expires <= 0)
			return 0;
		else
			return expires;
	}
	
	public void setExpires(int expires)
	{
		_absoluteExpires = System.currentTimeMillis() + expires * 1000;
		_session.getApplicationSession().setExpires(expires / 60 + 30);
	}

	public void serviceLevelTraceInfoChanged(String newValue)
	{
		sendNotify(newValue);
	}

	public String getAor()
	{
		return _aor;
	}

	public void setAor(String aor)
	{
		_aor = aor;
	}
	public int getVersion()
	{
		return _version;
	}

	public String getSubscriberUri()
	{
		return _subscriberUri;
	}

	public void userProfileUncached()
	{
		_absoluteExpires = System.currentTimeMillis();
		sendNotify(_userProfile.getServiceLevelTraceInfo());
	}

	public UserProfile getUserProfile()
	{
		return _userProfile;
	}

	public void setUserProfile(UserProfile userProfile)
	{
		_userProfile = userProfile;
	}
	
}
