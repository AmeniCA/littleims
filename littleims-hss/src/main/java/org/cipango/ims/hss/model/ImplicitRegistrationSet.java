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
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import org.hibernate.annotations.Sort;
import org.hibernate.annotations.SortType;

@Entity
public class ImplicitRegistrationSet
{
	@Id @GeneratedValue
	private Long _id;
	
	@OneToMany (mappedBy="_implicitRegistrationSet")
	@Sort (type = SortType.COMPARATOR, comparator = IdentityCompator.class)
	private SortedSet<PublicUserIdentity> _publicIdentities = new TreeSet<PublicUserIdentity>();
	
	@OneToMany (mappedBy="_implicitRegistrationSet", cascade = { CascadeType.ALL, CascadeType.REMOVE })
	private Set<RegistrationState> _states = new HashSet<RegistrationState>();
		
	public Long getId()
	{
		return _id;
	}

	public void setId(Long id)
	{
		_id = id;
	}

	public SortedSet<PublicUserIdentity> getPublicIdentities()
	{
		return _publicIdentities;
	}

	public void setPublicIdentities(SortedSet<PublicUserIdentity> publicIdentities)
	{
		_publicIdentities = publicIdentities;
	}

	public Set<RegistrationState> getStates()
	{
		return _states;
	}
	
	public String getRegisteredPrivateIdentity()
	{
		for (RegistrationState state : _states)
		{
			if (state.getState() == State.REGISTERED)
				return state.getPrivateIdentity();
		}
		return null;
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
		Iterator<RegistrationState> it = getStates().iterator();
		while (it.hasNext())
			it.next().setState(State.NOT_REGISTERED);
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
	
	public static class IdentityCompator implements Comparator<PublicUserIdentity>
	{

		public int compare(PublicUserIdentity id1, PublicUserIdentity id2)
		{
			if (id1.isDefaultIdentity() == id2.isDefaultIdentity())
				return id1.getIdentity().compareTo(id2.getIdentity());
			if (id1.isDefaultIdentity())
				return -1;
			return 1;
		}
		
	}
}
