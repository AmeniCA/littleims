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
package org.cipango.ims.hss.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;

@Entity
public class ImplicitRegistrationSet
{
	@Id @GeneratedValue
	private Long _id;
	
	@OneToMany (mappedBy="_implicitRegistrationSet")
	private Set<PublicUserIdentity> _publicIdentities = new HashSet<PublicUserIdentity>();
	
	@OneToMany (mappedBy="_implicitRegistrationSet", cascade = { CascadeType.ALL })
	private Set<RegistrationState> _states = new HashSet<RegistrationState>();
	
	public Long getId()
	{
		return _id;
	}

	public void setId(Long id)
	{
		_id = id;
	}

	public Set<PublicUserIdentity> getPublicIdentities()
	{
		return _publicIdentities;
	}

	public void setPublicIdentities(Set<PublicUserIdentity> publicIdentities)
	{
		_publicIdentities = publicIdentities;
	}

	public Set<RegistrationState> getStates()
	{
		return _states;
	}

	public void setStates(Set<RegistrationState> states)
	{
		_states = states;
	}
	
	public Short getState()
	{
		Short state = State.NOT_REGISTERED;
		for (RegistrationState registrationState : _states)
		{
			if (registrationState.getState() > state)
			{
				state = registrationState.getState();
			}
		}
		return state;
	}
	
	public RegistrationState getRegistrationState(String privateIdentity)
	{
		for (RegistrationState registrationState : _states)
		{
			if (registrationState.getPrivateIdentity().equals(privateIdentity))
			{
				return registrationState;
			}
		}
		return new RegistrationState(this, privateIdentity, State.NOT_REGISTERED);
	}
		
	public String getStateAsString()
	{
		return State.toString(getState());
	}
	
	public void deregister()
	{
		getStates().clear();
	}
	
	public void updateState(String privateIdentity, Short state)
	{
		for (RegistrationState registrationState : _states)
		{
			if (registrationState.getPrivateIdentity().equals(privateIdentity))
			{
				registrationState.setState(state);
				return;
			}
		}
		new RegistrationState(this, privateIdentity, state);	
	}
	
	public List<String> getPublicIds()
	{
		List<String> publicIds = new ArrayList<String>();
		for (PublicIdentity publicIdentity : getPublicIdentities())
		{
			publicIds.add(publicIdentity.getIdentity());
		}
		return publicIds;
	}
	
	public static class State
	{
		public static final short NOT_REGISTERED = 0;
		public static final short UNREGISTERED = 1;
		public static final short AUTH_PENDING = 2;
		public static final short REGISTERED = 3;	
		
		public static String toString(Short id)
		{
			if (id == null)
				return "";
			
			switch (id)
			{
			case UNREGISTERED:
				return "UNREGISTERED";
			case NOT_REGISTERED:
				return "NOT_REGISTERED";
			case AUTH_PENDING:
				return "AUTH_PENDING";
			case REGISTERED:
				return "REGISTERED";
			default:
				return "Unknown id " + id;
			}
		}
	}
}
