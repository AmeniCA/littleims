package org.cipango.littleims.pcscf.debug;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.servlet.sip.SipServletMessage;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.GDuration;
import org.cipango.ims.pcscf.debug.data.ReasonDocument.Reason;
import org.cipango.ims.pcscf.debug.data.SessionDocument.Session;
import org.cipango.ims.pcscf.debug.data.StartTriggerDocument.StartTrigger;
import org.cipango.ims.pcscf.debug.data.StopTriggerDocument.StopTrigger;
import org.cipango.littleims.util.Headers;
import org.cipango.littleims.util.Methods;

public class DebugSession
{
	private final Logger __log = Logger.getLogger(DebugSession.class);
	private String _id;
	private List<Trigger> _startTriggers = new ArrayList<Trigger>();
	private List<Trigger> _stopTriggers = new ArrayList<Trigger>();
	private String _debugId;
	private String _aor;
	
	public DebugSession(String aor, Session session)
	{
		_id = session.getId();
		_aor = aor;
		update(session);
	}
	
	public void update(Session session)
	{
		_startTriggers.clear();
		_stopTriggers.clear();
		
		StartTrigger startTrigger = session.getStartTrigger();
		if (startTrigger.getFrom() != null)
			_startTriggers.add(new FromTrigger(startTrigger.getFrom()));
		if (startTrigger.getTo() != null)
			_startTriggers.add(new ToTrigger(startTrigger.getTo()));
		if (startTrigger.getMethod() != null)
			_startTriggers.add(new MethodTrigger(startTrigger.getMethod()));
		if (startTrigger.getDebugId() != null)
			_startTriggers.add(new DebugIdTrigger(startTrigger.getDebugId()));
		if (startTrigger.getIcsi() != null)
			_startTriggers.add(new IcsiTrigger(startTrigger.getIcsi()));
		if (startTrigger.getIari() != null)
			_startTriggers.add(new IariTrigger(startTrigger.getIari()));
		if (startTrigger.getTime() != null)
			_startTriggers.add(new TimeTrigger(startTrigger.getTime().getTime()));	
		
		StopTrigger stopTrigger = session.getStopTrigger();
		if (stopTrigger.getTime() != null)
			_stopTriggers.add(new TimeTrigger(stopTrigger.getTime().getTime()));
		if (stopTrigger.getTimePeriod() != null)
			_stopTriggers.add(new DurationTrigger(stopTrigger.getTimePeriod()));
		if (stopTrigger.getReason() != null)
			_stopTriggers.add(new ReasonTrigger(stopTrigger.getReason()));
		
		_debugId = session.getControl().getDebugId();
	}
	
	public String getId()
	{
		return _id;
	}
	
	public String getDebugId()
	{
		return _debugId;
	}
	
	public String getStartTriggerAsString()
	{
		if (_startTriggers.isEmpty())
			return "No start conditions";

		StringBuilder sb = new StringBuilder();
		Iterator<Trigger> it = _startTriggers.iterator();
		while (it.hasNext())
		{
			sb.append(it.next());
			if (it.hasNext())
				sb.append(" && ");
		}
		return sb.toString();
	}
	
	public String getStoptTriggerAsString()
	{
		if (_stopTriggers.isEmpty())
			return "No stop conditions";

		StringBuilder sb = new StringBuilder();
		Iterator<Trigger> it = _stopTriggers.iterator();
		while (it.hasNext())
		{
			sb.append(it.next());
			if (it.hasNext())
				sb.append(" && ");
		}
		return sb.toString();
	}
	
	public boolean checkStartLogging(SipServletRequest initial)
	{
		for (Trigger trigger : _startTriggers)
		{
			if (!trigger.match(initial))
				return false;
		}
		__log.debug("Starting P-debug logging for for user: " + _aor + " with debug-ID " 
				+ _debugId + " for start trigger " + getStartTriggerAsString());
		initial.setHeader(Headers.P_DEBUG_ID, _debugId);
		initial.getApplicationSession().setAttribute(DebugSession.class.getName(), this);
		return true;
	}
	
	public void checkStopLogging(SipServletMessage message)
	{
		if (_stopTriggers.isEmpty())
			return;
		for (Trigger trigger : _stopTriggers)
		{
			if (!trigger.match(message))
				return;
		}
		__log.debug("Stop P-debug logging for for user: " + _aor + " with debug-ID " 
				+ _debugId + " for start trigger " + getStartTriggerAsString());
		message.getApplicationSession().removeAttribute(DebugSession.class.getName());
	}
	
	private interface Trigger
	{
		public boolean match(SipServletMessage message);
	}
	
	private class FromTrigger implements Trigger
	{
		private String _from;
		
		public FromTrigger(String from)
		{
			_from = from; 
		}
		
		public boolean match(SipServletMessage message)
		{
			return _from.equals(message.getFrom().getURI().toString());
		}
		
		public String toString()
		{
			return "from = " + _from;
		}
	}
	
	private class ToTrigger implements Trigger
	{
		private String _to;
		
		public ToTrigger(String to)
		{
			_to = to; 
		}
		
		public boolean match(SipServletMessage message)
		{
			return _to.equals(message.getTo().getURI().toString());
		}
		
		public String toString()
		{
			return "to =" + _to;
		}
	}
	
	private class MethodTrigger implements Trigger
	{
		private String _method;
		
		public MethodTrigger(String method)
		{
			_method = method; 
		}
		
		public boolean match(SipServletMessage message)
		{
			return _method.equals(message.getMethod());
		}
		
		public String toString()
		{
			return "method = " + _method;
		}
	}
	
	private class DebugIdTrigger implements Trigger
	{
		private String _debugId;
		
		public DebugIdTrigger(String debugId)
		{
			_debugId = debugId; 
		}
		
		public boolean match(SipServletMessage message)
		{
			return _debugId.equals(message.getHeader(Headers.P_DEBUG_ID));
		}
		
		public String toString()
		{
			return "debugId = " + _debugId;
		}
	}
	
	private class IcsiTrigger implements Trigger
	{
		private String _icsi;
		
		public IcsiTrigger(String icsi)
		{
			_icsi = icsi; 
		}
		
		public boolean match(SipServletMessage message)
		{
			// TODO
			return true;
		}
		
		public String toString()
		{
			return "isci = " + _icsi;
		}
	}
	
	private class IariTrigger implements Trigger
	{
		private String _iari;
		
		public IariTrigger(String iari)
		{
			_iari = iari; 
		}
		
		public boolean match(SipServletMessage message)
		{
			// TODO
			return true;
		}
		
		public String toString()
		{
			return "IARI = " + _iari;
		}
	}
	
	private class TimeTrigger implements Trigger
	{
		private Date _date;
		
		public TimeTrigger(Date date)
		{
			_date = date; 
		}
		
		public boolean match(SipServletMessage message)
		{
			return new Date().after(_date);
		}
		
		public String toString()
		{
			return "time > " + _date;
		}
	}
	
	private class DurationTrigger implements Trigger
	{
		private long _duration;
		
		public DurationTrigger(GDuration d)
		{
			_duration = 
				((((d.getYear() * 365 + d.getDay()) * 24 + d.getHour()) * 60 + d.getMinute()) * 60 + d.getSecond()) * 1000; 
		}
		
		public boolean match(SipServletMessage message)
		{
			return message.getSession().getCreationTime() + _duration > new Date().getTime();
		}
		
		public String toString()
		{
			return "duration < " + _duration;
		}
		
	}
	
	private class ReasonTrigger implements Trigger
	{
		private Reason.Enum _reason;
		
		public ReasonTrigger(Reason.Enum reason)
		{
			_reason = reason;
		}
		
		public boolean match(SipServletMessage message)
		{
			if (Reason.DIALOG_ESTABLISHED.equals(_reason))
			{
				if (Methods.ACK.equals(message.getMethod()))
						return true;
				if (message instanceof SipServletResponse)
				{
					SipServletResponse response = (SipServletResponse) message;
					if (response.getStatus() > SipServletResponse.SC_MULTIPLE_CHOICES
							|| !Methods.INVITE.equals(response.getMethod()))
						return true;
				}
				return false;
			}
			else
			{
				if (message instanceof SipServletResponse)
				{
					SipServletResponse response = (SipServletResponse) message;
					if (response.getStatus() > SipServletResponse.SC_MULTIPLE_CHOICES)
						return true;
					String method = response.getMethod();
					if (Methods.INVITE.equals(method)
							|| Methods.PRACK.equals(method)
							|| Methods.UPDATE.equals(method))
						return false;
					if (Methods.SUBSCRIBE.equals(method)
							&& response.getExpires() != 0)
						return false;
					return true;
					
				}
				return false;
			}
		}
		
		public String toString()
		{
			return "reason = " + _reason;
		}
	}
	
}
