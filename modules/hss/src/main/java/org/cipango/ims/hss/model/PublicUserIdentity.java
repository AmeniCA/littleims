package org.cipango.ims.hss.model;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.cipango.ims.hss.util.XML;
import org.cipango.ims.hss.util.XML.Output;

@Entity
@DiscriminatorValue("U")
public class PublicUserIdentity extends PublicIdentity
{
	@ManyToOne
	private ImplicitRegistrationSet _implicitRegistrationSet;
	
	@OneToMany (mappedBy="_publicIdentity", cascade = { CascadeType.REMOVE })
	private Set<PublicPrivate> _privateIdentities = new HashSet<PublicPrivate>();
	
	public PublicUserIdentity()
	{
		setIdentityType(IdentityType.PUBLIC_USER_IDENTITY);
	}
	
	public Set<PublicPrivate> getPrivateIdentities()
	{
		return _privateIdentities;
	}
	
	public SortedSet<String> getPrivateIds()
	{
		TreeSet<String> publicIds = new TreeSet<String>();
		Iterator<PublicPrivate> it = getPrivateIdentities().iterator();
		while (it.hasNext())
			publicIds.add(it.next().getPrivateId());
		return publicIds;
	}
	
	public void setPrivateIdentities(Set<PublicPrivate> privateIdentities)
	{
		_privateIdentities = privateIdentities;
	}
	
	public ImplicitRegistrationSet getImplicitRegistrationSet()
	{
		return _implicitRegistrationSet;
	}

	public void setImplicitRegistrationSet(ImplicitRegistrationSet implicitRegistrationSet)
	{
		if (_implicitRegistrationSet != null)
			_implicitRegistrationSet.getPublicIdentities().remove(this);
		
		_implicitRegistrationSet = implicitRegistrationSet;
		
		if (implicitRegistrationSet != null)
			implicitRegistrationSet.getPublicIdentities().add(this);
	}

	@Override
	public Short getState()
	{
		return getImplicitRegistrationSet().getState();
	}

	@Override
	public void updateState(String privateIdentity, Short state)
	{
		getImplicitRegistrationSet().updateState(privateIdentity, state);
	}
	
	public String getImsSubscriptionAsXml(PrivateIdentity privateIdentity)
	{
		Output out = XML.getDefault().newOutput();
		out.open("IMSSubscription");
		if (privateIdentity == null)
			// If no private identity is registered, use the first private identity.
			out.add("PrivateID", getPrivateIdentities().iterator().next().getPrivateId());
		else
			out.add("PrivateID", privateIdentity.getIdentity());
		out.add("ServiceProfile", _implicitRegistrationSet.getPublicIdentities());
		out.close("IMSSubscription");
		return out.toString();
	}
	
	@Override
	public void setIdentityType(Short identityType)
	{
		if (IdentityType.PUBLIC_USER_IDENTITY == identityType
				|| IdentityType.WILDCARDED_IMPU == identityType)
			super.setIdentityType(identityType);
		else
			throw new IllegalStateException("Could not identity type: " + IdentityType.toString(identityType)
					 + " to a public user identity");
	}

	@Override
	public Scscf getScscf()
	{
		return getPrivateIdentities().iterator().next().getPrivateIdentity().getSubscription().getScscf();
	}

	@Override
	public void setScscf(Scscf scscf)
	{
		getPrivateIdentities().iterator().next().getPrivateIdentity().getSubscription().setScscf(scscf);
	}
}
