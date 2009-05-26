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
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.cipango.ims.hss.model.spt.SPT;
import org.cipango.ims.hss.util.XML.Convertible;
import org.cipango.ims.hss.util.XML.Output;
import org.hibernate.annotations.Sort;
import org.hibernate.annotations.SortType;

@Entity
public class InitialFilterCriteria implements Comparable<InitialFilterCriteria>, Convertible
{
	@Id @GeneratedValue
	private Integer _id;
	
	@Column (unique = true)
	private String _name;
	
	private int _priority;
	private Short _profilePartIndicator;
	
	@ManyToOne
	@JoinColumn (nullable = false)
	private ApplicationServer _applicationServer;
		
	private boolean _conditionTypeCnf;
	
	@OneToMany(mappedBy = "_initialFilterCriteria", cascade = { CascadeType.ALL })
	@Sort (type = SortType.NATURAL)
	private SortedSet<SPT> _spts = new TreeSet<SPT>();
	
	@ManyToMany (mappedBy = "_ifcs")
	private Set<ServiceProfile> _serviceProfiles = new HashSet<ServiceProfile>();
	
	@ManyToMany (mappedBy = "_sharedIfcs")
	private Set<ServiceProfile> _sharedServiceProfiles = new HashSet<ServiceProfile>();
	
	public Set<ServiceProfile> getServiceProfiles()
	{
		return _serviceProfiles;
	}
	public void setServiceProfiles(Set<ServiceProfile> serviceProfiles)
	{
		_serviceProfiles = serviceProfiles;
	}
	public ApplicationServer getApplicationServer()
	{
		return _applicationServer;
	}
	public void setApplicationServer(ApplicationServer applicationServer)
	{
		_applicationServer = applicationServer;
		if (applicationServer != null)
			applicationServer.getIfcs().add(this);
	}
	public String getApplicationServerName()
	{
		if (_applicationServer == null)
			return "";
		else
			return _applicationServer.getName();
	}
	
	public boolean isConditionTypeCnf()
	{
		return _conditionTypeCnf;
	}
	public void setConditionTypeCnf(boolean conditionTypeCnf)
	{
		_conditionTypeCnf = conditionTypeCnf;
	}
	public SortedSet<SPT> getSpts()
	{
		return _spts;
	}
	public void setSpts(SortedSet<SPT> spts)
	{
		_spts = spts;
	}
	public void addSpt(SPT spt)
	{
		if (spt != null)
		{
			_spts.add(spt);
			spt.setInitialFilterCriteria(this);
		}
	}

	public int getPriority()
	{
		return _priority;
	}

	public Integer getId()
	{
		return _id;
	}

	public void setId(Integer id)
	{
		_id = id;
	}

	public Short getProfilePartIndicator()
	{
		return _profilePartIndicator;
	}
	
	public String getProfilePartIndicatorAsString()
	{
		return ProfilePartIndicator.toString(_profilePartIndicator);
	}

	public void setProfilePartIndicator(Short profilePartIndicator)
	{
		_profilePartIndicator = profilePartIndicator;
	}

	public void setPriority(int priority)
	{
		_priority = priority;
	}

	public int compareTo(InitialFilterCriteria o)
	{
		if (getPriority() == o.getPriority())
			return (int) (_id - o.getId());
		return getPriority() - o.getPriority();
	}
	
	public void print(Output out)
	{
		out.add("Priority", _priority);
		out.open("TriggerPoint");
		out.add("ConditionTypeCNF", _conditionTypeCnf);
		out.add("SPT", _spts);
		out.close("TriggerPoint");
		out.add("ApplicationServer", _applicationServer);
		out.add("ProfilePartIndicator", _profilePartIndicator);
	}
	
	public String getExpression()
	{
		if (_spts.isEmpty())
			return "";
		if (_spts.size() == 1)
			return _spts.iterator().next().getExpression();
		Iterator<SPT> it = _spts.iterator();
		Integer groupId = null;
		StringBuilder sb = new StringBuilder();
		while (it.hasNext())
		{
			SPT spt = (SPT) it.next();
			if (groupId == null)
			{
				groupId = spt.getGroupId();
				sb.append('(');
			} else if (groupId != spt.getGroupId())
			{
				groupId = spt.getGroupId();
				sb.append(')');
				sb.append(_conditionTypeCnf ? " && " : " || ");
				sb.append('(');
			}
			else
			{
				sb.append(_conditionTypeCnf ? " || " : " && ");
			}
			sb.append(spt.getExpression());
		}
		sb.append(')');
		return sb.toString();
	}
	
	public String getName()
	{
		return _name;
	}
	public void setName(String name)
	{
		_name = name;
	}
	public Set<ServiceProfile> getSharedServiceProfiles()
	{
		return _sharedServiceProfiles;
	}
	public void setSharedServiceProfiles(Set<ServiceProfile> sharedServiceProfiles)
	{
		_sharedServiceProfiles = sharedServiceProfiles;
	}
	
	public static class ProfilePartIndicator
	{
		public static final short REGISTERED = 0;
		public static final short UNREGISTERED = 1;
		
		public static String toString(Short id)
		{
			if (id == null)
				return "";
			
			switch (id)
			{
			case REGISTERED:
				return "REGISTERED";
			case UNREGISTERED:
				return "UNREGISTERED";
			default:
				return "Unknown id " + id;
			}
		}
	}
}
