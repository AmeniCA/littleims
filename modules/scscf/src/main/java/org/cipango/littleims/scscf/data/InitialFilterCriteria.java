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
package org.cipango.littleims.scscf.data;

import javax.servlet.sip.SipServletRequest;

import org.cipango.littleims.scscf.data.trigger.CriteriaMatch;


public class InitialFilterCriteria
{

	private int _priority;
	private CriteriaMatch _trigger;
	private AS _as;
	
	public enum SessionCase
	{
		ORIGINATING_SESSION(0),
		TERMINATING_REGISTERED(1),
		TERMINATING_UNREGISTERED(2),
		ORIGINATING_UNREGISTERED(3);
		
		private short _value;
		private SessionCase(int value)
		{
			_value = (short) value;
		}
		
		public short getValue()
		{
			return _value;
		}
	}

	public InitialFilterCriteria(int priority, CriteriaMatch trigger, AS as)
	{
		this._priority = priority;
		this._trigger = trigger;
		this._as = as;
	}

	public boolean matches(SipServletRequest request, SessionCase sessionCase)
	{
		return _trigger.matches(request, sessionCase);
	}

	public int getPriority()
	{
		return _priority;
	}

	public AS getAS()
	{
		return _as;
	}
	
	public String getTriggerPoint()
	{
		return _trigger.getExpression();
	}

	public String toString()
	{
		StringBuffer sb = new StringBuffer();
		sb.append('[');
		sb.append(_priority);
		sb.append(',');
		sb.append(_trigger.getExpression());
		sb.append(',');
		sb.append(_as.getURI());
		sb.append(']');
		return sb.toString();
	}


}
