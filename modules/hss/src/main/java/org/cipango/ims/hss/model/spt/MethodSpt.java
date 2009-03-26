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

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import org.cipango.ims.hss.util.XML.Output;

@Entity
@DiscriminatorValue("M")
public class MethodSpt extends SPT
{
	private String _method;

	public String getMethod()
	{
		return _method;
	}

	public void setMethod(String method)
	{
		_method = method;
	}
	
	@Override
	protected void doPrint(Output out)
	{
		out.add("Method", _method);	
	}

}
