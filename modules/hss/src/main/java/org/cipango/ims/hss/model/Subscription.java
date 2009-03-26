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
	private long _id;
	
	@OneToMany(mappedBy = "_subscription")
	private Set<PrivateIdentity> _privateIdentities = new HashSet<PrivateIdentity>();
	
	@ManyToOne
	@JoinColumn (nullable = true)
	private Scscf _scscf;

	public long getId()
	{
		return _id;
	}

	public void setId(long id)
	{
		_id = id;
	}

	public Set<PrivateIdentity> getPrivateIdentities()
	{
		return _privateIdentities;
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
	}
}
