// ========================================================================
// Copyright 2009 NEXCOM Systems
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
package org.cipango.littleims.pcscf.subscription.reg;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.sip.Address;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipSession;

import org.apache.log4j.Logger;
import org.cipango.ims.pcscf.reg.data.ReginfoDocument;
import org.cipango.ims.pcscf.reg.data.ReginfoDocument.Reginfo;
import org.cipango.ims.pcscf.reg.data.RegistrationDocument.Registration;
import org.cipango.ims.pcscf.reg.data.RegistrationDocument.Registration.State;
import org.cipango.littleims.pcscf.RegContext;
import org.cipango.littleims.pcscf.subscription.Subscription;

public class RegSubscription implements Subscription
{
	
	private static final Logger __log = Logger.getLogger(RegSubscription.class);
	
	private int _version = -1;
	private RegEventService _regService;
	private SipSession _session;
	private String _privateIdentity;
	private RegContext _regContext;
	private String _aor;
	
	public RegSubscription(RegEventService service, SipSession session, RegContext context, String privateIdentity)
	{
		_regService = service;
		_session = session;
		_regContext = context;
		_aor = context.getDefaultIdentity().toString();
		_privateIdentity = privateIdentity;
	}
		
	public void handleNotify(SipServletRequest notify)
	{
		try
		{
			ReginfoDocument doc = ReginfoDocument.Factory.parse(new String(notify.getRawContent()));
			Reginfo reginfo = doc.getReginfo();
			
			int version = reginfo.getVersion().intValue();

			__log.debug("Received NOTIFY No " + version + " for reg event for user " + _aor);
			if (version <= _version)
			{
				__log.warn("Discard notify request to " + notify.getFrom().getURI() + ": invalid version number: " + 
						version + " <= " + _version);
				return;
			}
			if (version > _version + 1 && reginfo.getState() != Reginfo.State.FULL)
			{
				//TODO request new full state.
			}
			_version = version;
			
			List<Address> registered = new ArrayList<Address>();
			List<Address> unregistered = new ArrayList<Address>();
			for (Registration registration : reginfo.getRegistrationArray())
			{
				Address address = _regService.getSipFactory().createAddress(registration.getAor());
				if (registration.getState() == State.ACTIVE)
					registered.add(address);
				else	
					unregistered.add(address);
			}
			
			_regContext.setIdentitie(registered, unregistered, reginfo.getState() == Reginfo.State.FULL);
			_regService.normalize(_regContext, unregistered);
		}
		catch (Exception e)
		{
			__log.warn("Failed to handle NOTIFY\n" + notify, e);
		}
	}
	
	public void invalidate()
	{
		__log.debug("Remove reg subscription for user " +  _aor);
		_session.invalidate();
		_regService.removeSubscription(this);
	}
	
	public SipSession getSession()
	{
		return _session;
	}

	public String getAor()
	{
		return  _aor;
	}

	public int getVersion()
	{
		return _version;
	}

	public String getUserAgent()
	{
		return _regService.getUserAgent();
	}

	public String getPrivateIdentity()
	{
		return _privateIdentity;
	}

}
