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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

/**
 * IMS Subscription
 */
@Entity
public class Subscription 
{
	@Id @GeneratedValue
	private Long _id;
	
	@Column (unique = true)
	private String _name;
	
	@OneToMany(mappedBy = "_subscription")
	private Set<PrivateIdentity> _privateIdentities = new HashSet<PrivateIdentity>();
	
	@ManyToOne
	@JoinColumn (nullable = true)
	private Scscf _scscf;

	public Long getId()
	{
		return _id;
	}

	public void setId(Long id)
	{
		_id = id;
	}
	
	public String getName()
	{
		return _name;
	}

	public void setName(String name)
	{
		_name = name;
	}

	public Set<PrivateIdentity> getPrivateIdentities()
	{
		return _privateIdentities;
	}
	
	public SortedSet<String> getPrivateIds()
	{
		TreeSet<String> ids = new TreeSet<String>();
		Iterator<PrivateIdentity> it = getPrivateIdentities().iterator();
		while (it.hasNext())
			ids.add(it.next().getIdentity());
		return ids;
	}
	
	public SortedSet<PublicUserIdentity> getPublicIdentities()
	{
		TreeSet<PublicUserIdentity> publicIds = new TreeSet<PublicUserIdentity>();
		Iterator<PrivateIdentity> it = getPrivateIdentities().iterator();
		while (it.hasNext()) 
		{
			Iterator<PublicUserIdentity> it2 = it.next().getPublicIdentities().iterator();
			while (it2.hasNext())
				publicIds.add(it2.next());
		}
		return publicIds;
	}
	
	public SortedSet<String> getPublicIds()
	{
		TreeSet<String> publicIds = new TreeSet<String>();
		Iterator<PrivateIdentity> it = getPrivateIdentities().iterator();
		while (it.hasNext()) 
		{
			Iterator<PublicUserIdentity> it2 = it.next().getPublicIdentities().iterator();
			while (it2.hasNext())
				publicIds.add(it2.next().getIdentity());
		}
		return publicIds;
	}

	public void setPrivateIdentities(Set<PrivateIdentity> privateIdentities)
	{
		_privateIdentities = privateIdentities;
	}
	
	public Scscf getScscf()
	{
		return _scscf;
	}

	public void setScscf(Scscf scscf)
	{
		_scscf = scscf;
		if (_scscf != null)
			_scscf.getSubscriptions().remove(this);
		if (scscf != null)
			scscf.getSubscriptions().add(this);
	}
}
