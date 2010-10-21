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
package org.cipango.ims.hss.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
public class RegistrationState
{

	@Embeddable
	public static class Id implements Serializable {
		@Column (name = "private_identity")
		private String _privateId;
		@Column (name = "implicit_registration_set")
		private Long _implicitRegistrationSetId;
		
		public Id()
		{
		}

		public Id(Long implicitRegistrationSetId, String privateId)
		{
			_privateId = privateId;
			_implicitRegistrationSetId = implicitRegistrationSetId;
		}
		public boolean equals(Object o)
		{
			if(o != null && o instanceof Id)
			{
				Id that = (Id)o;
				return _implicitRegistrationSetId.equals(that._implicitRegistrationSetId) &&
				_privateId.equals(that._privateId);
			}
			else
				return false;
		}
		@Override
		public int hashCode()
		{
			return (int) (_privateId.hashCode() * 31 + _implicitRegistrationSetId);
		}
	}


	@EmbeddedId
	private Id _id = new Id();
	
	@ManyToOne
	@JoinColumn (nullable = false, insertable=false, updatable=false)
	private ImplicitRegistrationSet _implicitRegistrationSet;
	
	@Column (name = "private_identity", nullable = false, insertable=false, updatable=false)
	private String _privateIdentity;
	private Short _state;
	
	public RegistrationState()
	{
	}
	
	public RegistrationState(ImplicitRegistrationSet implicitRegistrationSet, String privateIdentity, Short state)
	{
		_implicitRegistrationSet = implicitRegistrationSet;
		_privateIdentity = privateIdentity;
		_state = state;
		_id._implicitRegistrationSetId = implicitRegistrationSet.getId();
		_id._privateId = privateIdentity;
		implicitRegistrationSet.getStates().add(this);
	}
	
	public Id getId()
	{
		return _id;
	}
	public void setId(Id id)
	{
		_id = id;
	}
	public ImplicitRegistrationSet getImplicitRegistrationSet()
	{
		return _implicitRegistrationSet;
	}
	public void setImplicitRegistrationSet(ImplicitRegistrationSet implicitRegistrationSet)
	{
		_implicitRegistrationSet = implicitRegistrationSet;
	}
	public String getPrivateIdentity()
	{
		return _privateIdentity;
	}
	public void setPrivateIdentity(String privateIdentity)
	{
		_privateIdentity = privateIdentity;
	}
	public Short getState()
	{
		return _state;
	}
	public void setState(Short state)
	{
		_state = state;
	}
	
}
