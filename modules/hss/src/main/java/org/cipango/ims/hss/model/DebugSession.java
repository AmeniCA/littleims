package org.cipango.ims.hss.model;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.apache.wicket.util.string.Strings;
import org.cipango.ims.hss.util.XML.Convertible;
import org.cipango.ims.hss.util.XML.Output;

@Entity
public class DebugSession implements Convertible
{
	@Id @GeneratedValue
	private Long _id;
	
	@Temporal (TemporalType.TIMESTAMP)
	private Date _startDate;
	private String _fromTrigger;
	private String _toTrigger;
	private String _method;
	private String _icsi;
	private String _iari;
	
	@Temporal (TemporalType.TIMESTAMP)
	private Date _stopDate;
	private String _duration;
	private Boolean _reason;

	private Boolean _traceDepth;
	private String _debugId;
	
	@ManyToOne
	private PublicIdentity _publicIdentity;

	public Long getId()
	{
		return _id;
	}

	public void setId(Long id)
	{
		_id = id;
	}

	public Date getStartDate()
	{
		return _startDate;
	}

	public void setStartDate(Date startDate)
	{
		_startDate = startDate;
	}

	public Date getStopDate()
	{
		return _stopDate;
	}

	public void setStopDate(Date stopDate)
	{
		_stopDate = stopDate;
	}

	public String getDebugId()
	{
		return _debugId;
	}

	public void setDebugId(String debugId)
	{
		_debugId = debugId;
	}

	public String getFrom()
	{
		return _fromTrigger;
	}

	public void setFrom(String from)
	{
		_fromTrigger = from;
	}

	public String getTo()
	{
		return _toTrigger;
	}

	public void setTo(String to)
	{
		_toTrigger = to;
	}

	public PublicIdentity getPublicIdentity()
	{
		return _publicIdentity;
	}

	public void setPublicIdentity(PublicIdentity publicIdentity)
	{
		_publicIdentity = publicIdentity;
	}
	
	public String getReasonAsString()
	{
		return getReasonAsString(_reason);
	}
	
	public static String getReasonAsString(Boolean reason)
	{
		if (reason == null)
			return null;
		if (reason)
			return "dialog_established";
		else
			return "session_end";
	}
	
	public String getTraceDepthAsString()
	{
		return getTraceDepthAsString(_traceDepth);
	}
	
	public static String getTraceDepthAsString(Boolean traceDepth)
	{
		if (traceDepth == null)
			return null;
		if (traceDepth)
			return "maximum";
		else
			return "minimum";
	}

	public void print(Output out)
	{
		out.open("start-trigger");
		if (!Strings.isEmpty(_fromTrigger))
			out.add("from", _fromTrigger);
		if (!Strings.isEmpty(_toTrigger))
			out.add("to", _toTrigger);
		if (_startDate != null)
			out.add("time", _startDate);
		if (!Strings.isEmpty(_icsi))
			out.add("icsi", _icsi);
		if (!Strings.isEmpty(_iari))
			out.add("iari", _iari);
		if (!Strings.isEmpty(_method))
			out.add("method", _method);
		out.close("start-trigger");
		
		out.open("stop-trigger");
		if (_stopDate != null)
			out.add("time", _stopDate);
		if (!Strings.isEmpty(_duration))
			out.add("time-period", _duration);
		if (_reason != null)
			out.add("reason", getReasonAsString());
		out.close("stop-trigger");
		
		out.open("control");
		if (_traceDepth != null)
			out.add("depth", getTraceDepthAsString());
		out.add("debug-id", _debugId);
		out.close("control");
	}

	public String getMethod()
	{
		return _method;
	}

	public void setMethod(String method)
	{
		_method = method;
	}

	public String getIcsi()
	{
		return _icsi;
	}

	public void setIcsi(String icsi)
	{
		_icsi = icsi;
	}

	public String getIari()
	{
		return _iari;
	}

	public void setIari(String iari)
	{
		_iari = iari;
	}

	public String getDuration()
	{
		return _duration;
	}

	public void setDuration(String duration)
	{
		_duration = duration;
	}

	public Boolean getReason()
	{
		return _reason;
	}

	public void setReason(Boolean reason)
	{
		_reason = reason;
	}

	public Boolean getTraceDepth()
	{
		return _traceDepth;
	}

	public void setTraceDepth(Boolean traceDepth)
	{
		_traceDepth = traceDepth;
	}
	
	
}
