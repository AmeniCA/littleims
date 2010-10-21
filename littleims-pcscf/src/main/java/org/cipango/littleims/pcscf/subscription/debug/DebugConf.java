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
package org.cipango.littleims.pcscf.subscription.debug;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.sip.SipServletRequest;

import org.cipango.ims.pcscf.debug.data.SessionDocument.Session;

public class DebugConf implements Serializable
{
	private List<DebugSession> _sessions = new ArrayList<DebugSession>();
	private String _aor;
	
	public DebugConf(org.cipango.ims.pcscf.debug.data.DebugconfigDocument.Debugconfig debugConfig)
	{
		_aor = debugConfig.getAor();
		for (Session session : debugConfig.getSessionArray())
		{
			_sessions.add(new DebugSession(_aor, session));
		}
	}
	
	public void updateConfig(org.cipango.ims.pcscf.debug.data.DebugconfigDocument.Debugconfig debugConfig)
	{
		List<DebugSession> toRemove = new ArrayList<DebugSession>(_sessions);
		
		for (Session session : debugConfig.getSessionArray())
		{
			DebugSession debugSession = getSession(session.getId());
			if (debugSession == null)
				_sessions.add(new DebugSession(_aor, session));
			else
			{
				debugSession.update(session);
				toRemove.remove(debugSession);
			}
		}
		
		for (DebugSession session : toRemove)
			_sessions.remove(session);
	}
	
	public List<DebugSession> getSessions()
	{
		return _sessions;
	}
	
	public DebugSession getSession(String id)
	{
		for (DebugSession session : _sessions)
		{
			if (session.getId().equals(id))
				return session;
		}
		return null;
	}
	
	public String getAor()
	{
		return _aor;
	}
	
	public void checkStartLogging(SipServletRequest initial)
	{
		for (DebugSession session : _sessions)
		{
			if (session.checkStartLogging(initial))
				return;
		}
	}
}
