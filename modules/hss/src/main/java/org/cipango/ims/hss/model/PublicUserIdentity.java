package org.cipango.ims.hss.model;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;

import org.cipango.ims.hss.util.XML;
import org.cipango.ims.hss.util.XML.Output;

@Entity
@DiscriminatorValue("U")
public class PublicUserIdentity extends PublicIdentity
{
	@ManyToOne
	private ImplicitRegistrationSet _implicitRegistrationSet;
	
	@ManyToMany (mappedBy="_publicIdentities")
	private Set<PrivateIdentity> _privateIdentities = new HashSet<PrivateIdentity>();
	
	private Boolean _defaultIdentity;
	
	public PublicUserIdentity()
	{
		setIdentityType(IdentityType.PUBLIC_USER_IDENTITY);
	}
	
	public Set<PrivateIdentity> getPrivateIdentities()
	{
		return _privateIdentities;
	}
	
	public SortedSet<String> getPrivateIds()
	{
		TreeSet<String> publicIds = new TreeSet<String>();
		Iterator<PrivateIdentity> it = getPrivateIdentities().iterator();
		while (it.hasNext())
			publicIds.add(it.next().getIdentity());
		return publicIds;
	}
	
	public void setPrivateIdentities(Set<PrivateIdentity> privateIdentities)
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
	
	@Override
	public String getImsSubscriptionAsXml(String privateIdentity, String realImpu, boolean prettyPrint)
	{
		Output out;
		if (prettyPrint)
			out = XML.getPretty().newOutput();
		else
			out = XML.getDefault().newOutput();
		out.setParameter("realImpu", realImpu);
		out.open("IMSSubscription");
		if (privateIdentity == null)
			// If no private identity is registered, use the first private identity.
			out.add("PrivateID", getPrivateIdentities().iterator().next().getIdentity());
		else
			out.add("PrivateID", privateIdentity);
		out.add("ServiceProfile", _implicitRegistrationSet.getPublicIdentities());
		out.close("IMSSubscription");
		return out.toString();
	}
	
	protected String getAliasGroupId()
	{
		return String.valueOf(getImplicitRegistrationSet().getId()) + "-" + getServiceProfile().getId();
	}
	
	@Override
	public void setIdentityType(Short identityType)
	{
		if (IdentityType.PUBLIC_USER_IDENTITY == identityType)
			setWilcard(false);
		else if (IdentityType.WILDCARDED_IMPU == identityType)
			setWilcard(true);
		else
			throw new IllegalStateException("Could not identity type: " + IdentityType.toString(identityType)
					 + " to a public user identity");
	}
	

	@Override
	public Short getIdentityType()
	{
		if (getRegex() == null)
			return IdentityType.PUBLIC_USER_IDENTITY;
		else
			return IdentityType.WILDCARDED_IMPU;
	}

	@Override
	public Scscf getScscf()
	{
		return getPrivateIdentities().iterator().next().getSubscription().getScscf();
	}

	@Override
	public void setScscf(Scscf scscf)
	{
		getPrivateIdentities().iterator().next().getSubscription().setScscf(scscf);
	}
	
	public Subscription getSubscription()
	{
		Iterator<PrivateIdentity> it = getPrivateIdentities().iterator();
		if (it.hasNext())
			return it.next().getSubscription();
		return null;
	}

	public String toString()
	{
		return "Public user identity: " + getIdentity();
	}

	public boolean isDefaultIdentity()
	{

		return _defaultIdentity != null && _defaultIdentity;
	}

	public void setDefaultIdentity(boolean defaultIdentity)
	{
		_defaultIdentity = defaultIdentity;
	}

}
