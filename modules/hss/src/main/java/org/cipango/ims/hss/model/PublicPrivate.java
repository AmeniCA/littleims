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

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.cipango.ims.hss.util.XML.Convertible;
import org.cipango.ims.hss.util.XML.Output;

@Entity
public class PublicPrivate implements Convertible
{
	
	@Embeddable
	public static class Id implements Serializable {
		@Column (name = "private_identity")
		private Long _privateId;
		@Column (name = "public_identity")
		private Long _publicId;
		
		public Id()
		{
		}

		public Id(Long publicId, Long privateId)
		{
			_privateId = privateId;
			_publicId = publicId;
		}
		public boolean equals(Object o)
		{
			if(o != null && o instanceof Id)
			{
				Id that = (Id)o;
				return _publicId.equals(that._publicId) &&
				_privateId.equals(that._privateId);
			}
			else
				return false;
		}
		@Override
		public int hashCode()
		{
			return (int) (_privateId * 31 + _publicId);
		}
	}


	@EmbeddedId
	private Id _id = new Id();

	@ManyToOne (cascade = {CascadeType.PERSIST, CascadeType.MERGE})
	@JoinColumn (nullable = false, insertable=false, updatable=false)
	private PrivateIdentity _privateIdentity;
	
	@ManyToOne (cascade = {CascadeType.PERSIST, CascadeType.MERGE})
	@JoinColumn (nullable = false, insertable=false, updatable=false)
	private PublicIdentity _publicIdentity;
	
	private boolean _authenticationPending;
	
	public PublicPrivate()
	{		
	}
	
	public PublicPrivate(PublicIdentity publicIdentity, PrivateIdentity privateIdentity)
	{
		_privateIdentity = privateIdentity;
		_publicIdentity = publicIdentity;
		_id._privateId = privateIdentity.getId();
		_id._publicId = publicIdentity.getId();
		publicIdentity.getPrivateIdentities().add(this);
		privateIdentity.getPublicIdentities().add(this);
	}
	
	public void refresh()
	{
		if (_id._privateId == null)
			_id._privateId = _privateIdentity.getId();
		if (_id._publicId == null)
			_id._publicId = _publicIdentity.getId();
	}
	
	public Id getId()
	{
		return _id;
	}

	public void setId(Id id)
	{
		_id = id;
	}

	
	public String getPublicId()
	{
		return _publicIdentity.getIdentity();
	}
	
	public String getPrivateId()
	{
		return _privateIdentity.getIdentity();
	}
	
	public PrivateIdentity getPrivateIdentity()
	{
		return _privateIdentity;
	}
	public void setPrivateIdentity(PrivateIdentity privateIdentity)
	{
		_privateIdentity = privateIdentity;
	}
	public PublicIdentity getPublicIdentity()
	{
		return _publicIdentity;
	}
	public void setPublicIdentity(PublicIdentity publicIdentity)
	{
		_publicIdentity = publicIdentity;
	}
	public boolean isAuthenticationPending()
	{
		return _authenticationPending;
	}
	public void setAuthenticationPending(boolean authenticationPending)
	{
		_authenticationPending = authenticationPending;
	}

	public void print(Output out)
	{
		_publicIdentity.print(out);
	}
	
	
}
