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
	private String _privateUserIdentity;
	private Address _contact;
	private SipURI _path;
	private long _absExpires;
	private ContactEvent _event;
	private RegState _state;

	private TimerTask _regTimer;

	public Binding(String privateUserIdentity, Address contact, SipURI path, int expires)
	{
		_privateUserIdentity = privateUserIdentity;
		_contact = contact;
		_path = path;
		setExpires(expires);
	}

	public void setEvent(ContactEvent event)
	{
		this._event = event;
	}

	public ContactEvent getEvent()
	{
		return _event;
	}

	public void setState(RegState state)
	{
		this._state = state;
	}

	public RegState getState()
	{
		return _state;
	}

	public void refresh(Address contact, SipURI path, int expires)
	{
		_contact = contact;
		_path = path;
		setExpires(expires);
	}
	
	private void setExpires(int expires)
	{
		_absExpires = System.currentTimeMillis() + expires * 1000;
	}
	
	public int getExpires()
	{
		return (int) ((_absExpires - System.currentTimeMillis()) / 1000);
	}

	public String toString()
	{
		StringBuffer sb = new StringBuffer();
		sb.append(_contact.getURI().toString());
		sb.append(" / ");
		sb.append(_privateUserIdentity);
		sb.append(" / ");
		sb.append(getExpires());
		return sb.toString();
	}

	public Address getContact()
	{
		return _contact;
	}

	public SipURI getPath()
	{
		return _path;
	}

	public void setRegTimer(TimerTask task)
	{
		_regTimer = task;
	}

	public TimerTask getRegTimer()
	{
		return _regTimer;
	}

	public String getPrivateUserIdentity()
	{
		return _privateUserIdentity;
	}

}
