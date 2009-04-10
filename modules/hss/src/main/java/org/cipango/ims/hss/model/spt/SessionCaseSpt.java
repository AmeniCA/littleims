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
package org.cipango.ims.hss.model.spt;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import org.cipango.ims.hss.util.XML.Output;

@Entity
@DiscriminatorValue("SC")
public class SessionCaseSpt extends SPT
{
	private Short _sessionCase;
	
	public Short getSessionCase()
	{
		return _sessionCase;
	}

	public void setSessionCase(Short sessionCase)
	{
		_sessionCase = sessionCase;
	}
	
	@Override
	protected void doPrint(Output out)
	{
		out.add("SessionCase", _sessionCase);
	}

	public static class SessionCase
	{
		public static final short ORIGINATING_SESSION = 0;
		public static final short TERMINATING_REGISTERED = 1;
		public static final short TERMINATING_UNREGISTERED = 2;
		public static final short ORIGINATING_UNREGISTERED = 3;
		
		public static String toString(Short id)
		{
			if (id == null)
				return "";
			
			switch (id)
			{
			case ORIGINATING_SESSION:
				return "ORIGINATING_SESSION";
			case TERMINATING_REGISTERED:
				return "TERMINATING_REGISTERED";
			case TERMINATING_UNREGISTERED:
				return "TERMINATING_UNREGISTERED";
			case ORIGINATING_UNREGISTERED:
				return "ORIGINATING_UNREGISTERED";
			default:
				return "Unknown id " + id;
			}
		}
	}
}
