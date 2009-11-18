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
package org.cipango.littleims.pcscf;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.sip.Address;
import javax.servlet.sip.ServletParseException;

public class RegContext
{
	private List<Address> _associatedUris;
	private List<String> _associatedIps;
	
	public RegContext(Iterator<Address> it, String remoteAddr)
	{
		_associatedUris = new ArrayList<Address>();
		while (it.hasNext())
		{
			_associatedUris.add(it.next());
		}
		_associatedIps = new ArrayList<String>();
		_associatedIps.add(remoteAddr);
	}
	
	public boolean match(Address identity)
	{
		Iterator<Address> it = _associatedUris.iterator();
		while (it.hasNext())
		{
			if (it.next().getURI().equals(identity.getURI()))
				return true;	
		}
		return false;
	}
	
	public Address getAssertedIdentity(Address identity)
	{
		Iterator<Address> it = _associatedUris.iterator();
		while (it.hasNext())
		{
			Address address = it.next();
			if (address.getURI().equals(identity.getURI()))
				return address;	
		}
		return null;
	}
	
	public Address getDefaultIdentity()
	{
		return _associatedUris.get(0);
	}
	
	public void removeIdentity(String identity)
	{
		Iterator<Address> it = _associatedUris.iterator();
		while (it.hasNext())
		{
			Address address = it.next();
			if (address.getURI().equals(identity))
				it.remove();
		}
	}
	
	public void removeIdentity(Address identity)
	{
		_associatedUris.remove(identity);
	}

	public List<Address> getAssociatedUris()
	{
		return _associatedUris;
	}

	public void setAssociatedUris(List<Address> associatedUris)
	{
		_associatedUris = associatedUris;
	}
	
	public void addIdentity(Address identity)
	{
		if (!_associatedUris.contains(identity))
			_associatedUris.add(identity);
		
	}
	
	public void setIdentitie(List<Address> toAdd, List<Address> toRemove, boolean fullState) throws ServletParseException
	{
		Iterator<Address> it = toAdd.iterator();
		List<Address> previous = new ArrayList<Address>(getAssociatedUris());
					
		while (it.hasNext())
		{
			Address identity = it.next();
			addIdentity(identity);

			previous.remove(identity);
		}
		
		if (previous != null && fullState)
			toRemove.addAll(previous);

		Iterator<Address> it2 = toRemove.iterator();
		while (it2.hasNext())
		{
			Address identity = (Address) it2.next();
			removeIdentity(identity);
		}

	}
}
