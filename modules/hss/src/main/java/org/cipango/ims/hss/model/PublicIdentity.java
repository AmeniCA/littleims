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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.apache.wicket.util.string.Strings;
import org.cipango.ims.hss.model.ImplicitRegistrationSet.State;
import org.cipango.ims.hss.util.XML.Convertible;
import org.cipango.ims.hss.util.XML.Output;
import org.cipango.littleims.util.RegexUtil;
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
		
	private String _regex;
	
	@OneToMany (mappedBy = "_publicIdentity", cascade = {CascadeType.ALL})
	private Set<DebugSession> _debugSessions = new HashSet<DebugSession>();
		
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
		updateRegex();
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

	public abstract Short getIdentityType();
	
	public String getIdentityTypeAsString()
	{
		return IdentityType.toString(getIdentityType());
	}

	public abstract void setIdentityType(Short identityType);

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
		String realImpu = (String) out.getParameter("realImpu");
		if (realImpu != null && _regex != null 
				&& realImpu.matches(RegexUtil.extendedRegexToJavaRegex(_identity)))
			out.add("Identity", realImpu);
		else
			out.add("Identity", _identity);

		out.open("Extension");
		out.add("IdentityType", getIdentityType());
		if (_regex != null)
			out.add("WildcardedPSI", _identity);
		if (!Strings.isEmpty(_displayName) || !_debugSessions.isEmpty())
		{
			out.open("Extension");
			
			if (!Strings.isEmpty(_displayName))
				out.add("DisplayName", _displayName);
			
			if (!_debugSessions.isEmpty())
			{
				out.open("Extension");
				out.open("ServiceLevelTraceInfo");
				out.openCdata();
				
				out.open(/*"?xml version=\"1.0\"?>\n\t\t\t\t\t\t\t<"
						+ */"debuginfo xmlns=\"urn:ietf:params:xml:ns:debuginfo\" "
						+ "version=\"0\" state=\"full\"");
				//FIXME manage version			
				out.open("debugconfig aor=\"" + (realImpu != null ? realImpu : _identity) + '"');
				Map<String, String> attributes = new HashMap<String, String>();
				for (DebugSession debugSession : _debugSessions)
				{
					attributes.put("id", String.valueOf(debugSession.getId()));
					out.add("session", debugSession, attributes);
				}
				out.close("debugconfig");
				out.close("debuginfo");
				out.closeCdata();
				out.close("ServiceLevelTraceInfo");
				out.close("Extension");
			}
			
			out.close("Extension");
		}
		out.close("Extension");

		out.close("PublicIdentity");
		out.add("InitialFilterCriteria", _serviceProfile.getIfcs(false));
		
		if (_serviceProfile.hasSharedIfcs())
		{
			out.open("Extension");
			Iterator<InitialFilterCriteria> it = _serviceProfile.getIfcs(true).iterator();
			while (it.hasNext())
				out.add("SharedIFCSetID", it.next().getId());
			out.close("Extension");
		}
	}
	
	public int compareTo(PublicIdentity o)
	{
		return getIdentity().compareTo(o.getIdentity());
	}
	
	/**
	 * 
	 * @param privateIdentity
	 * @param realImpu In case of wildcard, the IMPU is not the identity.
	 * @return
	 */
	public abstract String getImsSubscriptionAsXml(String privateIdentity, String realImpu, boolean prettyPrint);
	
	public abstract Short getState();
	
	public String getStateAsString()
	{
		return State.toString(getState());
	}
	
	public abstract void updateState(String privateIdentity, Short state);
	
	public abstract Scscf getScscf();

	public abstract void setScscf(Scscf scscf);
	
	public String getRegex()
	{
		return _regex;
	}

	private void setRegex(String regex)
	{
		_regex = regex;
	}
	
	protected void updateRegex()
	{
		if (_regex != null)
			setRegex(RegexUtil.extendedRegexToSqlRegex(getIdentity()));
	}
	
	protected void setWilcard(boolean isWilcard)
	{
		if (isWilcard)
			setRegex(RegexUtil.extendedRegexToSqlRegex(getIdentity()));
		else
			setRegex(null);
	}
	
	public boolean isWilcard()
	{
		return _regex != null;
	}
	
	public Set<DebugSession> getDebugSessions()
	{
		return _debugSessions;
	}

	public void setDebugSessions(Set<DebugSession> debugSessions)
	{
		_debugSessions = debugSessions;
	}
	
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
