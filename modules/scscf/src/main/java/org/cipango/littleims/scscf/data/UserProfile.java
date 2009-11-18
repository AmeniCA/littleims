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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;



public class UserProfile
{

	private String _uri;
	private boolean _barred;
	private ServiceProfile _serviceProfile;
	private String _serviceLevelTraceInfo;
	private List<String> _aliases;
	private String _displayName;
	private List<UserProfileListener> _listeners;
	
	public UserProfile(String uri)
	{
		_uri = uri;
	}

	public boolean isBarred()
	{
		return _barred;
	}

	public void setBarred(boolean b)
	{
		_barred = b;
	}
	
	public void setAliases(List<UserProfile> aliases)
	{
		_aliases = null;
		if (aliases == null)
			return;
		
		Iterator<UserProfile> it = aliases.iterator();
		while (it.hasNext())
		{
			UserProfile profile = (UserProfile) it.next();
			if (profile != this)
			{
				if (_aliases == null)
					_aliases = new ArrayList<String>();
				_aliases.add(profile.getUri());
			}
		}
	}
	
	public List<String> getAliases()
	{
		return _aliases;
	}
	
	public String getTelUriAlias()
	{
		if (_aliases == null)
			return null;
		Iterator<String> it = _aliases.iterator();
		while (it.hasNext())
		{
			String uri = (String) it.next();
			if (uri.startsWith("tel:"))
				return uri;
		}
		return null;
	}

	public void setServiceProfile(ServiceProfile profile)
	{
		_serviceProfile = profile;
	}

	public ServiceProfile getServiceProfile()
	{
		return _serviceProfile;
	}

	public String getUri()
	{
		return _uri;
	}

	public String getServiceLevelTraceInfo()
	{
		return _serviceLevelTraceInfo;
	}

	public void setServiceLevelTraceInfo(String serviceLevelTraceInfo)
	{
		if (_listeners != null && 
				(((serviceLevelTraceInfo != null && !serviceLevelTraceInfo.equals(_serviceLevelTraceInfo)))
				|| (serviceLevelTraceInfo == null &&  _serviceLevelTraceInfo != null)))
		{
			for (UserProfileListener l : _listeners)
				l.serviceLevelTraceInfoChanged(serviceLevelTraceInfo);
		}
		_serviceLevelTraceInfo = serviceLevelTraceInfo;
	}
	
	protected void fireUncacheProfile()
	{
		if (_listeners != null)
		{
			// Create a new list to prevent concurrent ConcurrentModificationException
			for (UserProfileListener l : new ArrayList<UserProfileListener>(_listeners))
				l.userProfileUncached();
		}
	}
	
	public void addListener(UserProfileListener l)
	{
		if (_listeners == null)
			_listeners = new ArrayList<UserProfileListener>();
		_listeners.add(l);
	}
	
	public void removeListener(UserProfileListener l)
	{
		if (_listeners != null)
			_listeners.remove(l);
	}
	
	public List<UserProfileListener> getListeners()
	{
		return _listeners;
	}

	public String getDisplayName()
	{
		return _displayName;
	}

	public void setDisplayName(String displayName)
	{
		_displayName = displayName;
	}
}
