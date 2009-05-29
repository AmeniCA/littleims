package org.cipango.ims.hss.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
public class SpIfc
{
	@Embeddable
	public static class Id implements Serializable {
		@Column (name = "service_profile")
		private Long _spId;
		@Column (name = "ifc")
		private Integer _ifcId;
		
		public Id()
		{
		}

		public Id(Long spId, Integer ifcId)
		{
			_ifcId = ifcId;
			_spId = spId;
		}
		public boolean equals(Object o)
		{
			if(o != null && o instanceof Id)
			{
				Id that = (Id)o;
				return _ifcId.equals(that._ifcId) && _spId.equals(that._spId);
			}
			else
				return false;
		}
		@Override
		public int hashCode()
		{
			return (int) (_ifcId * 31 + _spId);
		}
	}


	@EmbeddedId
	private Id _id = new Id();
	
	@ManyToOne
	@JoinColumn (nullable = false, insertable=false, updatable=false)
	private InitialFilterCriteria _ifc;
	
	@ManyToOne
	@JoinColumn (nullable = false, insertable=false, updatable=false)
	private ServiceProfile _serviceProfile;
	
	private boolean _shared;

	public SpIfc()
	{	
	}
	
	public SpIfc(InitialFilterCriteria ifc, ServiceProfile serviceProfile, boolean shared)
	{
		_ifc = ifc;
		_serviceProfile = serviceProfile;
		_shared = shared;
		_id = new Id(serviceProfile.getId(), ifc.getId());
		ifc.getServiceProfiles().add(this);
		serviceProfile.getAllIfcs().add(this);
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

	public boolean isShared()
	{
		return _shared;
	}

	public void setShared(boolean shared)
	{
		_shared = shared;
	}

	public Id getId()
	{
		return _id;
	}

	public void setId(Id id)
	{
		_id = id;
	}
}
