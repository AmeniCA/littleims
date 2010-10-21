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
package org.cipango.littleims.scscf.session;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SessionMap
{
	private Map<String, Session> _sessions = new HashMap<String, Session>();

	
	public SessionMap()
	{
	}

	public void addSession(String key, Session session)
	{
		synchronized (_sessions)
		{
			_sessions.put(key, session);
		}
	}

	public Session getSession(String key)
	{
		synchronized (_sessions)
		{
			return (Session) _sessions.get(key);
		}
	}

	public void removeSession(String key)
	{
		synchronized (_sessions)
		{
			_sessions.remove(key);
		}
	}
	
	public List<Session> getSessions()
	{
		return new ArrayList<Session>(_sessions.values());
	}
}
