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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.sip.SipServletRequest;

import org.cipango.littleims.scscf.data.InitialFilterCriteria.SessionCase;

public class AndCriteria implements CriteriaMatch
{

	public AndCriteria(List<CriteriaMatch> criterias)
	{
		this.criterias = criterias;
	}

	public AndCriteria()
	{
		this.criterias = new ArrayList<CriteriaMatch>();
	}

	public void addCriterion(CriteriaMatch c)
	{
		criterias.add(c);
	}

	public boolean matches(SipServletRequest request, SessionCase sessionCase)
	{
		Iterator<CriteriaMatch> it = criterias.iterator();
		while (it.hasNext())
		{
			CriteriaMatch c = (CriteriaMatch) it.next();
			if (!c.matches(request, sessionCase))
			{
				return false;
			}
		}
		return true;
	}

	public String getExpression()
	{
		StringBuffer buffer = new StringBuffer("(");

		boolean first = true;
		Iterator<CriteriaMatch> iter = criterias.iterator();
		while (iter.hasNext())
		{
			if (!first)
			{
				buffer.append(" AND ");
			}

			CriteriaMatch c = iter.next();
			buffer.append(c.getExpression());
			first = false;
		}

		buffer.append(")");
		return buffer.toString();
	}

	private List<CriteriaMatch> criterias;
}
