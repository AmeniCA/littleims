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

import java.util.List;

import javax.servlet.sip.Address;

public class RegistrationInfo
{

	private List<Address> _contacts;
	private List<Address> _associatedURIs;
	
	public RegistrationInfo()
	{
	}

	public void setContacts(List<Address> contacts)
	{
		_contacts = contacts;
	}

	public List<Address> getContacts()
	{
		return _contacts;
	}

	public void setAssociatedUris(List<Address> associatedURIs)
	{
		_associatedURIs = associatedURIs;
	}

	public List<Address> getAssociatedURIs()
	{
		return _associatedURIs;
	}

}
