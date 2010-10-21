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

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import org.cipango.ims.hss.HssException;
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
	
	@OneToMany (mappedBy = "_serviceProfile", cascade = {CascadeType.ALL})
	private Set<PublicIdentity> _publicIdentites = new HashSet<PublicIdentity>();
	
	@OneToMany (mappedBy = "_serviceProfile")
	@Sort (type = SortType.NATURAL)
	private SortedSet<SpIfc> _allIfcs = new TreeSet<SpIfc>();

	public boolean hasSharedIfcs()
	{
		Iterator<SpIfc> it = getAllIfcs().iterator();
		while (it.hasNext())
		{
			SpIfc spIfc = (SpIfc) it.next();
			if (spIfc.isShared())
				return true;
		}
		return false;
	}

	public Long getId()
	{
		return _id;
	}

	public void setId(Long id)
	{
		_id = id;
	}

	public SortedSet<InitialFilterCriteria> getIfcs(boolean shared)
	{
		SortedSet<InitialFilterCriteria> ifcs = new TreeSet<InitialFilterCriteria>();
		Iterator<SpIfc> it = getAllIfcs().iterator();
		while (it.hasNext())
		{
			SpIfc spIfc = (SpIfc) it.next();
			if (!(shared ^ spIfc.isShared()))
				ifcs.add(spIfc.getIfc());
		}
		return ifcs;
	}

	public void addIfc(InitialFilterCriteria ifc, boolean shared) throws HssException
	{
		if (ifc != null)
		{
			checkPriority(ifc);
			new SpIfc(ifc, this, shared);
		}
	}
	
	public void moveIfc(InitialFilterCriteria ifc, boolean shared) throws HssException
	{
		SpIfc spIfc = getSpIfc(ifc);
		if (spIfc != null)
		{
			spIfc.setShared(shared);
		}
	}
	
	private SpIfc getSpIfc(InitialFilterCriteria ifc)
	{
		if (ifc != null)
		{
			Iterator<SpIfc> it = getAllIfcs().iterator();
			while (it.hasNext())
			{
				SpIfc spIfc = (SpIfc) it.next();
				if (spIfc.getIfc().getId().equals(ifc.getId()))
				{
					return spIfc;
				}
			}
		}
		return null;
	}
	

	private void checkPriority(InitialFilterCriteria ifc) throws HssException
	{
		Iterator<SpIfc> it = getAllIfcs().iterator();
		while (it.hasNext())
		{
			InitialFilterCriteria ifc2 = it.next().getIfc();
			if (ifc2.getPriority() == ifc.getPriority())
			{
				throw new HssException("Could not add the IFC " 
						+ ifc.getName() + " as the IFC " + ifc2.getName() 
						+ " as the same priority (" + ifc.getPriority() + ")");
			}	
		}
	}
	
	public boolean hasUnregisteredServices()
	{
		for (SpIfc spIfc : getAllIfcs())
		{
			InitialFilterCriteria ifc = spIfc.getIfc();
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

	public String getName()
	{
		return _name;
	}

	public void setName(String name)
	{
		_name = name;
	}

	public Set<PublicIdentity> getPublicIdentites()
	{
		return _publicIdentites;
	}

	public void setPublicIdentites(Set<PublicIdentity> publicIdentites)
	{
		_publicIdentites = publicIdentites;
	}

	public SortedSet<SpIfc> getAllIfcs()
	{
		return _allIfcs;
	}

	public void setAllIfcs(SortedSet<SpIfc> allIfcs)
	{
		_allIfcs = allIfcs;
	}

}
