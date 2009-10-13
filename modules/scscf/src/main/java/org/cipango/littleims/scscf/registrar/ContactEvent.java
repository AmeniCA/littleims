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

public class ContactEvent
{

	public static final ContactEvent REGISTERED = new ContactEvent("registered");
	public static final ContactEvent CREATED = new ContactEvent("created");
	public static final ContactEvent UNREGISTERED = new ContactEvent("unregistered");
	public static final ContactEvent DEACTIVATED = new ContactEvent("deactivated");
	public static final ContactEvent REJECTED = new ContactEvent("rejected");
	public static final ContactEvent REFRESHED = new ContactEvent("refreshed");
	public static final ContactEvent EXPIRED = new ContactEvent("expired");
	public static final ContactEvent SHORTENED = new ContactEvent("shortened");

	private ContactEvent(String value)
	{
		this.value = value;
	}

	public String getValue()
	{
		return value;
	}

	private String value;
}
