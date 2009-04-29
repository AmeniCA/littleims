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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;

import org.cipango.ims.hss.model.InitialFilterCriteria.ProfilePartIndicator;
import org.cipango.ims.hss.model.spt.SPT;
import org.cipango.ims.hss.model.spt.SessionCaseSpt;
import org.cipango.ims.hss.model.spt.SessionCaseSpt.SessionCase;
import org.hibernate.annotations.Sort;
import org.hibernate.annotations.SortType;

@Entity
public class ServiceProfile
{
	@Id @GeneratedValue
	private Long _id;
	
	@Column (unique = true)
	private String _name;
	
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

	public Long getId()
	{
		return _id;
	}

	public void setId(Long id)
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
		if (ifc != null)
		{
			_ifcs.add(ifc);
			ifc.getServiceProfiles().add(this);
		}
	}
	
	public boolean hasUnregisteredServices()
	{
		for (InitialFilterCriteria ifc : getIfcs())
		{
			Short profilePartIndicator = ifc.getProfilePartIndicator();
			if (profilePartIndicator == null)
			{
				boolean hasSessionCase = false;
				for (SPT spt : ifc.getSpts())
				{
					if (spt instanceof SessionCaseSpt)
					{
						hasSessionCase = true;
						Short sessionCase = ((SessionCaseSpt) spt).getSessionCase();
						switch (sessionCase)
						{
						case SessionCase.ORIGINATING_UNREGISTERED:
						case SessionCase.TERMINATING_UNREGISTERED:
							// No check of isConditionNegated() as in this case it could match on the other
							// UNREGISTERED session case.
							return true;
						case SessionCase.ORIGINATING_SESSION:
						case SessionCase.TERMINATING_REGISTERED:
							if (spt.isConditionNegated())
								return true;
							break;
						default:
							break;
						}
					}
				}
				if (!hasSessionCase)
					return true;
			} 
			else if (profilePartIndicator == ProfilePartIndicator.UNREGISTERED)
				return true;
		}
		return false;
	}
	
	public void removeIfc(InitialFilterCriteria ifc)
	{
		if (ifc != null)
		{
			_ifcs.remove(ifc);
			ifc.getServiceProfiles().remove(this);
		}
	}

	public String getName()
	{
		return _name;
	}

	public void setName(String name)
	{
		_name = name;
	}

}
