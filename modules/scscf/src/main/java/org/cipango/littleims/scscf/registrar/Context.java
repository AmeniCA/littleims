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
package org.cipango.littleims.scscf.registrar;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.sip.Address;
import javax.servlet.sip.SipURI;

import org.apache.log4j.Logger;
import org.cipango.littleims.scscf.registrar.Registrar.RegTimerTask;
import org.cipango.littleims.scscf.registrar.regevent.RegInfo;
import org.cipango.littleims.scscf.registrar.regevent.RegState;


public class Context
{
	private static final Logger __log = Logger.getLogger(Context.class);
	
	private String _publicUserIdentity;
	private Map<String, Binding> _bindings = new HashMap<String, Binding>();

	private List<String> _associatedURIs;

	private RegState _state;

	
	public Context(String publicUserIdentity)
	{
		_publicUserIdentity = publicUserIdentity;
		_state = RegState.ACTIVE;
	}
	
	public String getPublicIdentity()
	{
		return _publicUserIdentity;
	}

	public List<Address> getContacts()
	{
		List<Address> list = new ArrayList<Address>();
		synchronized (_bindings)
		{
			Iterator<Binding> it = _bindings.values().iterator();
			while (it.hasNext())
			{
				Binding b = (Binding) it.next();
				list.add(b.getContact());
			}
		}
		return list;
	}

	public List<Binding> getBindings()
	{
		List<Binding> list = new ArrayList<Binding>();
		synchronized (_bindings)
		{
			Iterator<Binding> it = _bindings.values().iterator();
			while (it.hasNext())
			{
				list.add(it.next());
			}
		}
		return list;
	}

	public RegState getState()
	{
		return _state;
	}

	public void setRegTimer(String privateUserIdentity, RegTimerTask task)
	{
		synchronized (_bindings)
		{
			Binding binding = (Binding) _bindings.get(privateUserIdentity);
			binding.setRegTimer(task);
		}
	}

	public RegInfo removeAllBindings(ContactEvent event)
	{
		synchronized (_bindings)
		{
			RegInfo regInfo = new RegInfo(_publicUserIdentity.toString(), RegState.TERMINATED);
			Iterator<Binding> it = _bindings.values().iterator();
			while (it.hasNext())
			{
				Binding binding = it.next();
				binding.setEvent(event);
				binding.setState(RegState.TERMINATED);
				regInfo.addContactInfo(
						binding.getContact().getURI().toString(), 
						binding.getContact().getDisplayName(),
						binding.getState(), 
						binding.getEvent());
				if (binding.getRegTimer() != null)
				{
					binding.getRegTimer().cancel();
					binding.setRegTimer(null);
				}
				it.remove();		
			}
			_state = RegState.TERMINATED;
			return regInfo;
		}
	}
	
	public boolean hasPrivateIdentityRegistered(Collection<String> privateIdentities)
	{
		for (String privateIdentity : privateIdentities)
		{
			if (_bindings.containsKey(privateIdentity))
				return true;
		}
		return false;
	}
	
	public RegInfo removeBinding(String privateUserIdentity, ContactEvent event)
	{

		Binding binding = (Binding) _bindings.remove(privateUserIdentity);
		if (binding == null)
		{
			return null;
		}

		binding.setEvent(event);
		binding.setState(RegState.TERMINATED);

		if (_bindings.size() == 0)
		{
			_state = RegState.TERMINATED;
		}

		RegInfo regInfo = new RegInfo(_publicUserIdentity.toString(), _state);
		
		// Use binding.getContact() in case of unregister with wilcard address.
		regInfo.addContactInfo(
				binding.getContact().getURI().toString(), 
				binding.getContact().getDisplayName(),
				binding.getState(), 
				binding.getEvent());

		if (binding.getRegTimer() != null)
		{
			binding.getRegTimer().cancel();
			binding.setRegTimer(null);
		}

		return regInfo;
	}

	public RegInfo getRegInfo()
	{
		RegInfo regInfo = new RegInfo(_publicUserIdentity, _state);
		synchronized (_bindings)
		{
			Iterator<Binding> it = _bindings.values().iterator();
			while (it.hasNext())
			{
				Binding binding = (Binding) it.next();
				regInfo.addContactInfo(
						binding.getContact().getURI().toString(),
						binding.getContact().getDisplayName(),
						binding.getState(), 
						binding.getEvent());
			}
		}
		return regInfo;
	}

	public RegInfo updateBinding(Address contact, String privateUserIdentity, int expires,
			boolean explicit, SipURI path)
	{
		Binding binding = null;
		synchronized (_bindings)
		{

			binding = (Binding) _bindings.get(privateUserIdentity);
			if (binding == null)
			{
				// New Registration
				binding = new Binding(privateUserIdentity, contact, path, expires);
				binding.setState(RegState.ACTIVE);
				_bindings.put(privateUserIdentity, binding);
				if (explicit)
				{
					binding.setEvent(ContactEvent.REGISTERED);
				}
				else
				{
					binding.setEvent(ContactEvent.CREATED);
				}
			}
			else
			{
				if (binding.getRegTimer() != null)
				{
					binding.getRegTimer().cancel();
					binding.setRegTimer(null);
				}
				// Refresh registration
				binding.refresh(contact, path, expires);
				binding.setEvent(ContactEvent.REFRESHED);
			}

		}

		__log.debug("Updated bindings for " + _publicUserIdentity + ": " + _bindings);
		if (binding == null)
		{
			return null;
		}
		else
		{
			RegInfo regInfo = new RegInfo(_publicUserIdentity.toString(), _state);
			regInfo.addContactInfo(
					contact.getURI().toString(), 
					contact.getDisplayName(),
					binding.getState(), 
					binding.getEvent());
			return regInfo;
		}
	}

	public void setAssociatedURIs(List<String> associatedURIs)
	{
		this._associatedURIs = associatedURIs;
	}

	public List<String> getAssociatedURIs()
	{
		return _associatedURIs;
	}

}
