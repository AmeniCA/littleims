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
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;

@Entity
public class Scscf
{
	@Id @GeneratedValue
	private Long _id;
	
	@Column(unique=true)
	private String _name;
	
	@Column (unique=true)
	private String _uri;
	
	@Column (unique=true)
	private String _diameterHost;
	
	@OneToMany(mappedBy = "_scscf")
	private Set<Subscription> _subscriptions = new HashSet<Subscription>();
	
	@OneToMany(mappedBy = "_scscf")
	private Set<PSI> _psis = new HashSet<PSI>();

	public Set<PSI> getPsis()
	{
		return _psis;
	}

	public void setPsis(Set<PSI> psis)
	{
		_psis = psis;
	}

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

	public String getUri()
	{
		return _uri;
	}

	public void setUri(String uri)
	{
		_uri = uri;
	}
	
	public String toString()
	{
		return _name;
	}

	public Set<Subscription> getSubscriptions()
	{
		return _subscriptions;
	}

	public void setSubscriptions(Set<Subscription> subscriptions)
	{
		_subscriptions = subscriptions;
	}

	public String getDiameterHost()
	{
		return _diameterHost;
	}

	public void setDiameterHost(String diameterHost)
	{
		_diameterHost = diameterHost;
	}
}
