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

import org.cipango.ims.hss.util.XML.Convertible;
import org.cipango.ims.hss.util.XML.Output;

@Entity
public class ApplicationServer implements Convertible
{
	@Id @GeneratedValue
	private Long _id;
	
	@Column (unique = true)
	private String _name;
	
	@Column (unique = true)
	private String _serverName;
	private Short _defaultHandling;
	private String _serviceInformation;
	private Boolean _includeRegisterRequest;
	private Boolean _includeRegisterResponse;
	
	@OneToMany(mappedBy = "_applicationServer")
	private Set<InitialFilterCriteria> _ifcs = new HashSet<InitialFilterCriteria>();
	
	@OneToMany(mappedBy = "_applicationServer")
	private Set<PSI> _psis = new HashSet<PSI>();
	
	public Set<InitialFilterCriteria> getIfcs()
	{
		return _ifcs;
	}

	public void setIfcs(Set<InitialFilterCriteria> ifcs)
	{
		_ifcs = ifcs;
	}

	public Long getId()
	{
		return _id;
	}

	public void setId(Long id)
	{
		_id = id;
	}

	public String getServerName()
	{
		return _serverName;
	}

	public void setServerName(String serverName)
	{
		_serverName = serverName;
	}

	public Short getDefaultHandling()
	{
		return _defaultHandling;
	}

	public void setDefaultHandling(Short defaultHandling)
	{
		_defaultHandling = defaultHandling;
	}

	public String getServiceInformation()
	{
		return _serviceInformation;
	}

	public void setServiceInformation(String serviceInformation)
	{
		_serviceInformation = serviceInformation;
	}

	public Boolean getIncludeRegisterRequest()
	{
		return _includeRegisterRequest;
	}

	public void setIncludeRegisterRequest(Boolean includeRegisterRequest)
	{
		_includeRegisterRequest = includeRegisterRequest;
	}

	public Boolean getIncludeRegisterResponse()
	{
		return _includeRegisterResponse;
	}

	public void setIncludeRegisterResponse(Boolean includeRegisterResponse)
	{
		_includeRegisterResponse = includeRegisterResponse;
	}	
	
	public String getName()
	{
		return _name;
	}

	public void setName(String name)
	{
		_name = name;
	}
	
	public String toString()
	{
		return _name;
	}
	
	public void print(Output out)
	{
		out.add("ServerName", _serverName);
		out.add("DefaultHandling", _defaultHandling);
		if (_serviceInformation != null && !_serviceInformation.trim().equals(""))
			out.add("ServiceInfo", _serviceInformation);
		if (_includeRegisterRequest || _includeRegisterResponse)
		{
			out.open("Extension");
			if (_includeRegisterRequest)
				out.add("IncludeRegisterRequest", null);
			if (_includeRegisterResponse)
				out.add("IncludeRegisterResponse", null);
			out.close("Extension");
		}
	}
	public Set<PSI> getPsis()
	{
		return _psis;
	}

	public void setPsis(Set<PSI> psis)
	{
		_psis = psis;
	}
	
	public static class DefaultHandling
	{
		public static final short SESSION_CONTINUED = 0;
		public static final short SESSION_TERMINATED = 1;
		
		public static String toString(Short id)
		{
			if (id == null)
				return "";
			
			switch (id)
			{
			case SESSION_CONTINUED:
				return "SESSION_CONTINUED";
			case SESSION_TERMINATED:
				return "SESSION_TERMINATED";
			default:
				return "Unknown id " + id;
			}
		}
	}
}
