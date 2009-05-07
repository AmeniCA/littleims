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

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;

import org.cipango.ims.hss.model.ImplicitRegistrationSet.State;
import org.cipango.ims.hss.util.XML.Convertible;
import org.cipango.ims.hss.util.XML.Output;
import org.hibernate.annotations.Index;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(
		name = "TYPE",
		discriminatorType = DiscriminatorType.STRING)
public abstract class PublicIdentity implements Convertible, Comparable<PublicIdentity>
{
	@Id @GeneratedValue
	private Long _id;
	
	@Column (unique = true)
	@Index (name = "IDX_IDENTITY")
	private String _identity;

	private boolean _barred;
	
	private String _displayName;
	
	private Short _identityType;
		
	@ManyToOne
	private ServiceProfile _serviceProfile;
		
	public PublicIdentity() 
	{
		
	}
	
	public Long getId()
	{
		return _id;
	}

	public void setId(Long id)
	{
		_id = id;
	}
	
	public String getIdentity()
	{
		return _identity;
	}

	public void setIdentity(String identity)
	{
		_identity = identity;
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
	
	public String getIdentityTypeAsString()
	{
		return IdentityType.toString(_identityType);
	}

	public void setIdentityType(Short identityType)
	{
		_identityType = identityType;
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
	
	public int compareTo(PublicIdentity o)
	{
		return getIdentity().compareTo(o.getIdentity());
	}
	
	public abstract String getImsSubscriptionAsXml(PrivateIdentity privateIdentity);
	
	public abstract Short getState();
	
	public String getStateAsString()
	{
		return State.toString(getState());
	}
	
	public abstract void updateState(String privateIdentity, Short state);
	
	public abstract Scscf getScscf();

	public abstract void setScscf(Scscf scscf);
	
	public static class IdentityType
	{
		public static final short PUBLIC_USER_IDENTITY = 0;
		public static final short DISTINCT_PSI = 1;
		public static final short WILDCARDED_PSI = 2;
		public static final short WILDCARDED_IMPU = 3;	
		
		public static String toString(Short id)
		{
			if (id == null)
				return "";
			
			switch (id)
			{
			case PUBLIC_USER_IDENTITY:
				return "PUBLIC_USER_IDENTITY";
			case DISTINCT_PSI:
				return "DISTINCT_PSI";
			case WILDCARDED_PSI:
				return "WILDCARDED_PSI";
			case WILDCARDED_IMPU:
				return "WILDCARDED_IMPU";
			default:
				return "Unknown id " + id;
			}
		}
	}

}
