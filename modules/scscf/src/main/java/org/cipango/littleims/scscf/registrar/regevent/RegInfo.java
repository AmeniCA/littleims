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

import org.cipango.littleims.scscf.registrar.ContactEvent;


public class RegInfo
{

	public RegInfo(String aor, RegState state)
	{
		this.aor = aor;
		this.aorState = state;
	}

	public void addContactInfo(String contact, RegState state, ContactEvent event)
	{
		contacts.add(new ContactInfo(contact, event, state));
	}

	public String getAor()
	{
		return aor;
	}

	public RegState getAorState()
	{
		return aorState;
	}

	public List<ContactInfo> getContacts()
	{
		return contacts;
	}

	class ContactInfo
	{

		public ContactInfo(String contact, ContactEvent event, RegState state)
		{
			this.contact = contact;
			contactEvent = event;
			contactState = state;
		}

		public String getContact()
		{
			return contact;
		}

		public ContactEvent getContactEvent()
		{
			return contactEvent;
		}

		public RegState getContactState()
		{
			return contactState;
		}

		private String contact;
		private RegState contactState;
		private ContactEvent contactEvent;
	}

	private String aor;
	private RegState aorState;

	private List<ContactInfo> contacts = new ArrayList<ContactInfo>();

}
