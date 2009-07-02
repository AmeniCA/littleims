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

import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipSession;
import javax.servlet.sip.TimerService;

import org.apache.log4j.Logger;
import org.cipango.littleims.scscf.registrar.regevent.RegEventManager;
import org.cipango.littleims.util.Headers;
import org.cipango.littleims.util.Methods;

public class DebugSession
{

	private static final String DEBUG_INFO_CONTENT_TYPE = "application/debuginfo+xml";
	private static final String EMPTY_SERVICE_LEVEL_TRACE_INFO = 
		"<debuginfo xmlns=\"urn:ietf:params:xml:ns:debuginfo\" version=\"0\" state=\"full\"/>";
	private static final Logger __log = Logger.getLogger(DebugSession.class);
	
	private int _version;
	private SipSession _session;
	private long _absoluteExpires;
	private TimerService _timerService;
	
	public DebugSession(SipSession session, int expires)
	{
		_session = session;
		setExpires(expires);
		_version = 0;
		session.setAttribute(DebugSession.class.getName(), this);
	}
	
	public void sendNotify(String serviceLevelTraceInfo)
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
			notify.setHeader(Headers.EVENT_HEADER, DebugIdService.DEBUG_EVENT);
			int expires = getExpires();
			if (expires == 0)
				notify.setHeader(Headers.SUBSCRIPTION_STATE,
						"terminated;reason=timeout");
			else
				notify.setHeader(Headers.SUBSCRIPTION_STATE, "active;expires="
						+ expires);
			notify.send();
			if (expires == 0)
				_session.getApplicationSession().invalidate();
		}
		catch (Exception e)
		{
			__log.warn("Failed to send NOTIFY for user " + _session.getRemoteParty().getURI(), e);
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
	
}
