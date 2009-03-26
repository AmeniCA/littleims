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
import java.util.SortedSet;
import java.util.TreeSet;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;

import org.hibernate.annotations.Sort;
import org.hibernate.annotations.SortType;

@Entity
public class ServiceProfile
{
	@Id @GeneratedValue
	private long _id;
	
	@ManyToMany
	@JoinTable (
			name = "SP_IFC",
			joinColumns = {@JoinColumn(name = "serviceProfile")},
			inverseJoinColumns = {@JoinColumn(name = "ifc")})
	@Sort (type = SortType.NATURAL)
	private SortedSet<InitialFilterCriteria> _ifcs = new TreeSet<InitialFilterCriteria>();
	
	@OneToMany(mappedBy="_serviceProfile")
	private Set<SharedIfc> _sharedIfcs = new HashSet<SharedIfc>();

	public boolean hasSharedIfcs()
	{
		return _sharedIfcs != null && !_sharedIfcs.isEmpty();
	}
	
	public Set<SharedIfc> getSharedIfcs()
	{
		return _sharedIfcs;
	}

	public void setSharedIfcs(Set<SharedIfc> sharedIfcs)
	{
		_sharedIfcs = sharedIfcs;
	}

	public long getId()
	{
		return _id;
	}

	public void setId(long id)
	{
		_id = id;
	}

	public SortedSet<InitialFilterCriteria> getIfcs()
	{
		return _ifcs;
	}

	public void setIfcs(SortedSet<InitialFilterCriteria> ifcs)
	{
		_ifcs = ifcs;
	}
	
	public void addIfc(InitialFilterCriteria ifc)
	{
		_ifcs.add(ifc);
	}

}
