package org.cipango.ims.hss.model;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.cipango.ims.hss.model.ImplicitRegistrationSet.State;
import org.cipango.ims.hss.util.XML;
import org.cipango.ims.hss.util.XML.Output;

@Entity
@DiscriminatorValue("S")
public class PSI extends PublicIdentity
{
	@ManyToOne
	@JoinColumn (nullable = true)
	private Scscf _scscf;
	
	@ManyToOne
	@JoinColumn (nullable = true)
	private ApplicationServer _applicationServer;
	
	private String _privateServiceIdentity;
	
	public String getPrivateServiceIdentity()
	{
		return _privateServiceIdentity;
	}

	public void setPrivateServiceIdentity(String privateServiceIdentity)
	{
		_privateServiceIdentity = privateServiceIdentity;
	}

	public PSI()
	{
		setIdentityType(IdentityType.DISTINCT_PSI);
	}
	
	@Override
	public Scscf getScscf()
	{
		return _scscf;
	}

	@Override
	public void setScscf(Scscf scscf)
	{
		_scscf = scscf;
	}
	
	public boolean isPsiActivation()
	{
		return !isBarred();
	}

	public void setPsiActivation(boolean state)
	{
		setBarred(!state);
	}

	@Override
	public Short getState()
	{
		return _scscf == null ? State.NOT_REGISTERED : State.UNREGISTERED;
	}
	
	@Override
	public void updateState(String privateIdentity, Short state)
	{
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
		out.add("PrivateID", _privateServiceIdentity);
		
		out.add("ServiceProfile", this);
		out.close("IMSSubscription");
		return out.toString();
	}

	@Override
	public void setIdentityType(Short identityType)
	{
		if (IdentityType.DISTINCT_PSI == identityType)
			setWilcard(false);
		else if (IdentityType.WILDCARDED_PSI == identityType)
			setWilcard(true);
		else
			throw new IllegalStateException("Could not identity type: " + IdentityType.toString(identityType)
					 + " to a public user identity");
	}
	

	@Override
	public Short getIdentityType()
	{
		if (getRegex() == null)
			return IdentityType.DISTINCT_PSI;
		else
			return IdentityType.WILDCARDED_PSI;
	}

	public ApplicationServer getApplicationServer()
	{
		return _applicationServer;
	}

	public void setApplicationServer(ApplicationServer applicationServer)
	{
		_applicationServer = applicationServer;
	}

	@Override
	protected String getAliasGroupId()
	{
		return null;
	}

	@Override
	protected void printDebugConfigs(Output out)
	{
		printDebugConfig(out);
	}

	@Override
	protected boolean hasDebugConfig()
	{
		return !getDebugSessions().isEmpty();
	}

	
}
