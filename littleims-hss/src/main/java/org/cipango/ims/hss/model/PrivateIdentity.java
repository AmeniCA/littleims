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
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.cipango.ims.hss.model.uss.Uss;
import org.cipango.ims.hss.util.XML;
import org.cipango.ims.hss.util.XML.Output;
import org.hibernate.annotations.Index;
import org.hibernate.annotations.Sort;
import org.hibernate.annotations.SortType;

/**
 * Private Identity
 */
@Entity
public class PrivateIdentity 
{
	public static final byte[] DEFAULT_OPERATOR_ID = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
	
	
	@Id @GeneratedValue
	private Long _id;
	
	@Column (unique = true)
	@Index (name = "IDX_IDENTITY")
	private String _identity;

	private byte[] _password;

	//@Column(nullable=false, length=16)
	private byte[] _operatorId = DEFAULT_OPERATOR_ID;
	
	private byte[] _sqn;
	
	@ManyToOne
	@JoinColumn (nullable = true)
	private Subscription _subscription;
	
	@ManyToMany
	@JoinTable (
			name = "PUBLIC_PRIVATE",
			joinColumns = {@JoinColumn(name = "privateIdentity")},
			inverseJoinColumns = {@JoinColumn(name = "publicIdentity")})
	@Sort (type = SortType.NATURAL)
	private SortedSet<PublicUserIdentity> _publicIdentities = new TreeSet<PublicUserIdentity>();
		
	@OneToMany (mappedBy = "_privateIdentity", cascade = { CascadeType.ALL })
	private Set<Uss> _ussSet = new HashSet<Uss>();
	
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

	public byte[] getPassword()
	{
		return _password;
	}
	
	public byte[] getAkaPassword()
	{
		byte[] k = new byte[16];
		for (int i = 0; i < k.length; i++)
		{
			if (i < _password.length)
				k[i] = _password[i];
			else
				k[i] = 0;
		}
		return k;
	}

	public void setPassword(byte[] password)
	{
		_password = password;
	}
	
	public String getPasswordAsString()
	{
		if (_password == null)
			return null;
		return new String(_password); // TODO encoding
	}

	public void setPasswordAsString(String password)
	{
		if (password == null)
			_password = null;
		else
			_password = password.getBytes();
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

	public Set<PublicUserIdentity> getPublicIdentities()
	{
		return _publicIdentities;
	}
	
	public SortedSet<String> getPublicIds()
	{
		TreeSet<String> publicIds = new TreeSet<String>();
		Iterator<PublicUserIdentity> it = getPublicIdentities().iterator();
		while (it.hasNext())
			publicIds.add(it.next().getIdentity());
		return publicIds;
	}

	public void setPublicIdentities(SortedSet<PublicUserIdentity> publicIdentities)
	{
		_publicIdentities = publicIdentities;
	}
	
	public void addPublicId(PublicUserIdentity publicIdentity)
	{
		getPublicIdentities().add(publicIdentity);
		publicIdentity.getPrivateIdentities().add(this);
	}
	
	public void removePublicId(PublicUserIdentity publicIdentity)
	{
		getPublicIdentities().remove(publicIdentity);
		publicIdentity.getPrivateIdentities().remove(this);
	}
	
	public String getGuss(long keyLifetime, Output output)
	{
		output.open("guss id=\"" + getIdentity() + "\"");
		output.open("bsfInfo");
		output.add("lifeTime", keyLifetime);
		output.close("bsfInfo");
		output.open("ussList");
		Iterator<Uss> it = _ussSet.iterator();
		while (it.hasNext())
		{
			Uss uss = (Uss) it.next();
			output.add("uss", uss, uss.getXmlAttributes());
		}
		
		output.close("ussList");
		output.close("guss");
		
		return output.toString();
	}

	public Set<Uss> getUssSet()
	{
		return _ussSet;
	}

	public void setUssSet(Set<Uss> ussSet)
	{
		_ussSet = ussSet;
	}
	
}
