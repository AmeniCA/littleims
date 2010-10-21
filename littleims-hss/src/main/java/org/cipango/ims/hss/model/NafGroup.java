// ========================================================================
// Copyright 2010 NEXCOM Systems
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

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import org.cipango.ims.hss.model.uss.Uss;

@Entity
public class NafGroup
{
	@Id @GeneratedValue
	private Integer _id;
	
	@Column (unique = true)
	private String _name;
	
	@OneToMany (mappedBy = "_nafGroup")
	private Set<Uss> _ussSet = new HashSet<Uss>();

	public Integer getId()
	{
		return _id;
	}

	public void setId(Integer id)
	{
		_id = id;
	}

	public String getName()
	{
		return _name;
	}

	public void setName(String name)
	{
		_name = name;
	}
	
	@Override
	public String toString()
	{
		return _name;
	}

	public Set<Uss> getUssSet()
	{
		return _ussSet;
	}

	public void setUssSet(Set<Uss> ussSet)
	{
		_ussSet = ussSet;
	}
}
