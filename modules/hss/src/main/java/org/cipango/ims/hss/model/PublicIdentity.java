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

/**
 * Public Identities
 */
package org.cipango.ims.hss.model;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.cipango.ims.hss.util.XML.Convertible;
import org.cipango.ims.hss.util.XML.Output;

@Entity
public class PublicIdentity implements Convertible
{
	@Id
	private String _identity;
	
	@OneToMany (mappedBy="_publicIdentity")
	private Set<PublicPrivate> _privateIdentities = new HashSet<PublicPrivate>();

	private boolean _barred;
	
	private String _displayName;
	
	private Short _identityType;
	
	private Short _state;
	
	@ManyToOne
	@JoinColumn (nullable = false, insertable=false, updatable=false)
	private ServiceProfile _serviceProfile;
	
	public String getIdentity()
	{
		return _identity;
	}

	public void setIdentity(String identity)
	{
		_identity = identity;
	}

	public Set<PublicPrivate> getPrivateIdentities()
	{
		return _privateIdentities;
	}

	public boolean isBarred()
	{
		return _barred;
	}

	public void setBarred(boolean barred)
	{
		_barred = barred;
	}

	public String getDisplayName()
	{
		return _displayName;
	}

	public void setDisplayName(String displayName)
	{
		_displayName = displayName;
	}

	public Short getIdentityType()
	{
		return _identityType;
	}

	public void setIdentityType(Short identityType)
	{
		_identityType = identityType;
	}

	public Short getState()
	{
		return _state;
	}

	public void setState(Short state)
	{
		_state = state;
	}

	public void setPrivateIdentities(Set<PublicPrivate> privateIdentities)
	{
		_privateIdentities = privateIdentities;
	}

	public ServiceProfile getServiceProfile()
	{
		return _serviceProfile;
	}

	public void setServiceProfile(ServiceProfile serviceProfile)
	{
		_serviceProfile = serviceProfile;
	}

	public boolean equals(Object o)
	{
		if (!(o instanceof PublicIdentity))
			return false;
		return (_identity.equals(((PublicIdentity) o)._identity));
	}
	
	public int hashCode()
	{
		return _identity.hashCode();
	}
	
	public void print(Output out)
	{
		out.open("PublicIdentity");
		out.add("BarringIndication", _barred);
		out.add("Identity", _identity); // TODO set identity that has matched
		if (_identityType != null || _displayName != null)
		{
			out.open("Extension");
			out.add("IdentityType", _identityType);
			if (_identityType == IdentityType.WILDCARDED_IMPU || _identityType == IdentityType.WILDCARDED_PSI)
				out.add("WildcardedPSI", _identity);
			if (_displayName != null)
			{
				out.open("Extension");
				out.add("DisplayName", _displayName);
				out.close("Extension");
			}
			out.close("Extension");
		}
		out.close("PublicIdentity");
		out.add("InitialFilterCriteria", _serviceProfile.getIfcs());
		
		if (_serviceProfile.hasSharedIfcs())
		{
			out.open("Extension");
			out.add("SharedIFCSetID", _serviceProfile.getSharedIfcs());
			out.close("Extension");
		}
	}
	
	public static class IdentityType
	{
		public static final short PUBLIC_USER_IDENTITY = 0;
		public static final short DISTINCT_PSI = 1;
		public static final short WILDCARDED_PSI = 2;
		public static final short WILDCARDED_IMPU = 3;	
	}
	
	public static class State
	{
		public static final short UNREGISTERED = 0;
		public static final short NOT_REGISTERED = 1;
		public static final short AUTH_PENDING = 2;
		public static final short REGISTERED = 3;	
	}
}
