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
package org.cipango.ims.hss.model.spt;

import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.cipango.ims.hss.model.InitialFilterCriteria;
import org.cipango.ims.hss.util.XML;
import org.cipango.ims.hss.util.XML.Output;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(
		name = "SPT_TYPE",
		discriminatorType = DiscriminatorType.STRING)
public abstract class SPT implements XML.Convertible
{
	@Id @GeneratedValue
	private Long _id;
	private boolean _conditionNegated;
	private int _groupId;
	
	@ManyToOne
	@JoinColumn (nullable = false, insertable=false, updatable=false)
	private InitialFilterCriteria _initialFilterCriteria;
	

	public Long getId()
	{
		return _id;
	}

	public InitialFilterCriteria getInitialFilterCriteria()
	{
		return _initialFilterCriteria;
	}

	public void setInitialFilterCriteria(InitialFilterCriteria initialFilterCriteria)
	{
		_initialFilterCriteria = initialFilterCriteria;
	}

	public void setId(Long id)
	{
		_id = id;
	}
	public boolean isConditionNegated()
	{
		return _conditionNegated;
	}
	public void setConditionNegated(boolean conditionNegated)
	{
		_conditionNegated = conditionNegated;
	}

	public int getGroupId()
	{
		return _groupId;
	}

	public void setGroupId(int groupId)
	{
		_groupId = groupId;
	}

	public void print(Output out)
	{
		out.add("group", _groupId);
		out.add("ConditionNegated", _conditionNegated);	
		doPrint(out);
	}

	protected abstract void doPrint(Output out);
}
