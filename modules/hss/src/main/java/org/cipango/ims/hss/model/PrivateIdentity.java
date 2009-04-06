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
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.cipango.ims.hss.util.XML;
import org.cipango.ims.hss.util.XML.Output;

/**
 * Private Identity
 */
@Entity
public class PrivateIdentity 
{
	@Id
	private String _identity;

	private byte[] _password;

	//@Column(nullable=false, length=16)
	private byte[] _operatorId;
	
	private byte[] _sqn;
	
	@ManyToOne
	@JoinColumn (nullable = true)
	private Subscription _subscription;
	
	@OneToMany (mappedBy = "_privateIdentity", cascade = { CascadeType.ALL })
	private Set<PublicPrivate> _publicIdentities = new HashSet<PublicPrivate>();
	
	@ManyToMany
	private Set<ServiceProfile> _serviceProfiles;
	
	public Set<ServiceProfile> getServiceProfiles()
	{
		return _serviceProfiles;
	}

	public void setServiceProfiles(Set<ServiceProfile> serviceProfiles)
	{
		_serviceProfiles = serviceProfiles;
	}

	public String getIdentity()
	{
		return _identity;
	}

	public void setIdentity(String identity)
	{
		_identity = identity;
	}

	public byte[] getPassword()
	{
		return _password;
	}

	public void setPassword(byte[] password)
	{
		_password = password;
	}

	public byte[] getOperatorId()
	{
		return _operatorId;
	}

	public void setOperatorId(byte[] operatorId)
	{
		_operatorId = operatorId;
	}

	public byte[] getSqn()
	{
		return _sqn;
	}

	public void setSqn(byte[] sqn)
	{
		_sqn = sqn;
	}

	public Subscription getSubscription()
	{
		return _subscription;
	}

	public void setSubscription(Subscription subscription)
	{
		_subscription = subscription;
		subscription.getPrivateIdentities().add(this);
	}

	public Set<PublicPrivate> getPublicIdentities()
	{
		return _publicIdentities;
	}
	
	public SortedSet<String> getPublicIds()
	{
		TreeSet<String> publicIds = new TreeSet<String>();
		Iterator<PublicPrivate> it = getPublicIdentities().iterator();
		while (it.hasNext())
			publicIds.add(it.next().getPublicId());
		return publicIds;
	}

	public void setPublicIdentities(Set<PublicPrivate> publicIdentities)
	{
		_publicIdentities = publicIdentities;
	}
	
	public PublicPrivate addPublicId(PublicIdentity publicId)
	{
		return new PublicPrivate(publicId, this);
	}
	
	public String toXml()
	{
		Output out = XML.getDefault().newOutput();
		out.open("IMSSubscription");
		out.add("PrivateID", _identity);
		out.add("ServiceProfile", _publicIdentities);
		out.close("IMSSubscription");
		return out.toString();
	}
}
