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
package org.cipango.littleims.scscf.registrar;

import java.util.TimerTask;

import javax.servlet.sip.Address;
import javax.servlet.sip.SipURI;

import org.cipango.littleims.scscf.registrar.regevent.RegState;


/**
 * Gathers all information needed about a SIP phone where a Public User Identity
 * is registered.
 * 
 */
public class Binding
{
	private String privateUserIdentity;
	private Address contact;
	private SipURI path;
	private int expires;
	private ContactEvent event;
	private RegState state;

	private TimerTask regTimer;

	public Binding(String privateUserIdentity, Address contact, SipURI path, int expires)
	{
		this.privateUserIdentity = privateUserIdentity;
		this.contact = contact;
		this.path = path;
		this.expires = expires;
	}

	public void setEvent(ContactEvent event)
	{
		this.event = event;
	}

	public ContactEvent getEvent()
	{
		return event;
	}

	public void setState(RegState state)
	{
		this.state = state;
	}

	public RegState getState()
	{
		return state;
	}

	public void refresh(Address contact, SipURI path, int expires)
	{
		this.contact = contact;
		this.path = path;
		this.expires = expires;
	}

	public String toString()
	{
		StringBuffer sb = new StringBuffer();
		sb.append(contact.getURI().toString());
		sb.append(" / ");
		sb.append(privateUserIdentity);
		sb.append(" / ");
		sb.append(expires);
		return sb.toString();
	}

	public Address getContact()
	{
		return contact;
	}

	public SipURI getPath()
	{
		return path;
	}

	public void setRegTimer(TimerTask task)
	{
		this.regTimer = task;
	}

	public TimerTask getRegTimer()
	{
		return regTimer;
	}

}
