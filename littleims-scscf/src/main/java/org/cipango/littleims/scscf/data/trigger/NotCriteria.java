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
package org.cipango.littleims.scscf.data.trigger;

import javax.servlet.sip.SipServletRequest;

import org.cipango.littleims.scscf.data.InitialFilterCriteria.SessionCase;

public class NotCriteria implements CriteriaMatch
{

	public NotCriteria(CriteriaMatch criteria)
	{
		this.criteria = criteria;
	}

	public NotCriteria()
	{
	}

	public void setCriteria(CriteriaMatch criteria)
	{
		this.criteria = criteria;
	}

	public boolean matches(SipServletRequest request, SessionCase sessionCase)
	{
		return !criteria.matches(request, sessionCase);
	}

	public String getExpression()
	{
		return "(NOT " + criteria.getExpression() + ")";
	}

	private CriteriaMatch criteria;
}
