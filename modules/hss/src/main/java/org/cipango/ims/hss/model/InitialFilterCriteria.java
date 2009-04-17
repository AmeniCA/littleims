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

@Entity
public class InitialFilterCriteria implements Comparable<InitialFilterCriteria>, Convertible
{
	@Id @GeneratedValue
	private Long _id;
	
	@Column (unique = true)
	private String _name;
	
	private int _priority;
	private Short _profilePartIndicator;
	
	@ManyToOne
	@JoinColumn (nullable = false)
	private ApplicationServer _applicationServer;
	
	private boolean _conditionTypeCnf;
	
	@OneToMany(mappedBy = "_initialFilterCriteria", cascade = { CascadeType.ALL })
	private Set<SPT> _spts = new HashSet<SPT>();
	
	@ManyToMany (mappedBy = "_ifcs")
	private Set<ServiceProfile> _serviceProfiles = new HashSet<ServiceProfile>();
	
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
			applicationServer.getIcfc().add(this);
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
	public Set<SPT> getSpts()
	{
		return _spts;
	}
	public void setSpts(Set<SPT> spts)
	{
		_spts = spts;
	}
	public void addSpt(SPT spt)
	{
		_spts.add(spt);
	}

	public int getPriority()
	{
		return _priority;
	}

	public Long getId()
	{
		return _id;
	}

	public void setId(Long id)
	{
		_id = id;
	}

	public Short getProfilePartIndicator()
	{
		return _profilePartIndicator;
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
			return hashCode() - o.hashCode();
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
	public String getName()
	{
		return _name;
	}
	public void setName(String name)
	{
		_name = name;
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
