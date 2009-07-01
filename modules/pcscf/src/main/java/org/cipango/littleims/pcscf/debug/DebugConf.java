package org.cipango.littleims.pcscf.debug;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.sip.SipServletRequest;

import org.cipango.ims.pcscf.debug.data.SessionDocument.Session;

public class DebugConf
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
		for (Session session : debugConfig.getSessionArray())
		{
			DebugSession debugSession = getSession(session.getId());
			if (debugSession == null)
				_sessions.add(new DebugSession(_aor, session));
			else
				debugSession.update(session);
		}
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
