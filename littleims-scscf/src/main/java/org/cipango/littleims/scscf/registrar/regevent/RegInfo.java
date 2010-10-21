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
package org.cipango.littleims.scscf.registrar.regevent;

import java.util.ArrayList;
import java.util.List;

import org.cipango.littleims.scscf.registrar.Context.ContactEvent;
import org.cipango.littleims.scscf.registrar.Context.RegState;


public class RegInfo
{

	private String _aor;
	private RegState _aorState;

	private List<ContactInfo> _contacts = new ArrayList<ContactInfo>();
	
	public RegInfo(String aor, RegState state)
	{
		this._aor = aor;
		this._aorState = state;
	}

	public void addContactInfo(String contact, String displayName, RegState state, ContactEvent event, int expires)
	{
		_contacts.add(new ContactInfo(contact, displayName, event, state, expires));
	}

	public String getAor()
	{
		return _aor;
	}

	public RegState getAorState()
	{
		return _aorState;
	}

	public List<ContactInfo> getContacts()
	{
		return _contacts;
	}

	class ContactInfo
	{

		private String _contact;
		private String _displayName;
		private RegState _contactState;
		private ContactEvent _contactEvent;
		private int _expires;

		public ContactInfo(String contact, String displayName, ContactEvent event, RegState state, int expires)
		{
			_contact = contact;
			_contactEvent = event;
			_contactState = state;
			_displayName = displayName;
			_expires = expires;
		}

		public String getContact()
		{
			return _contact;
		}

		public ContactEvent getContactEvent()
		{
			return _contactEvent;
		}

		public RegState getContactState()
		{
			return _contactState;
		}

		public String getDisplayName()
		{
			return _displayName;
		}

		public int getExpires()
		{
			return _expires;
		}
	}


}
