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
public class SharedIfc
{

	@Embeddable
	public static class Key implements Serializable{
		@Column(name="ID")
		private int _id;

		@Column(name = "SERVICE_PROFILE")
		private Long _serviceProfileId;

		public Key()
		{
		}

		public Key(int sharedIfcId, Long serviceProfileId)
		{
			_id = sharedIfcId;
			_serviceProfileId = serviceProfileId;
		}
		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = 1;
			result = prime * result + _id;
			result = prime * result + (int) (_serviceProfileId ^ (_serviceProfileId >>> 32));
			return result;
		}

		@Override
		public boolean equals(Object obj)
		{
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Key other = (Key) obj;
			if (_id != other._id)
				return false;
			if (_serviceProfileId != other._serviceProfileId)
				return false;
			return true;
		}

	}

	@EmbeddedId
	private Key _key = new Key();
	
	@Column (name = "ID", nullable = false, insertable=false, updatable=false)
	private int _id;

	@ManyToOne
	private InitialFilterCriteria _ifc;
	
	@ManyToOne
	@JoinColumn (name = "SERVICE_PROFILE", nullable = false, insertable=false, updatable=false)
	private ServiceProfile _serviceProfile;

	public SharedIfc()
	{
		
	}
	
	public SharedIfc(int id, ServiceProfile serviceProfile)
	{
		_id = id;
		_serviceProfile = serviceProfile;
		_key._id = _id;
		_key._serviceProfileId = serviceProfile.getId();
		_serviceProfile.getSharedIfcs().add(this);
	}
	
	public InitialFilterCriteria getIfc()
	{
		return _ifc;
	}

	public void setIfc(InitialFilterCriteria ifc)
	{
		_ifc = ifc;
	}
	

	public ServiceProfile getServiceProfile()
	{
		return _serviceProfile;
	}

	public void setServiceProfile(ServiceProfile serviceProfile)
	{
		_serviceProfile = serviceProfile;
	}
	public Key getKey()
	{
		return _key;
	}

	public void setKey(Key key)
	{
		_key = key;
	}

	public int getId()
	{
		return _id;
	}

	public void setId(int id)
	{
		_id = id;
	}

	

	
}
