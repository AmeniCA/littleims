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
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.cipango.littleims.cx.data.userprofile.TSePoTri;
import org.cipango.littleims.cx.data.userprofile.TTrigger;

public class TriggerPointCompiler
{

	/**
	 * Compiles a trigger point.
	 * 
	 * @param triggerPoint
	 *            the trigger point to be compiled
	 * @return the matcher for the trigger point
	 */
	public CriteriaMatch compile(TTrigger triggerPoint)
	{

		CriteriaMatch match = null;

		// 29.228: any logical expression can be transformed to forms
		// called Conjunctive Normal Form (CNF) and Disjunctive Normal Form
		// (DNF)
		// Trigger points are either expressed as a CNF or DNF

		if (triggerPoint.getConditionTypeCNF())
		{
			match = generateCNFCriteria(triggerPoint.getSPTArray());
		}
		else
		{
			match = generateDNFCriteria(triggerPoint.getSPTArray());
		}
		return match;
	}

	/**
	 * Group the Service Trigger Points belonging to the same group together.
	 * 
	 * @param the
	 *            service trigger points
	 * @return a collection of list, each list contains the SPT of a group
	 */
	private Collection<List<TSePoTri>> generateGroups(TSePoTri[] triggerPoints)
	{
		Map<Integer, List<TSePoTri>> sptGroups = new HashMap<Integer, List<TSePoTri>>();
		for (int i = 0; i < triggerPoints.length; i++)
		{
			TSePoTri spt = triggerPoints[i];
			// TODO handle multiple groups for a SPT, probably not used
			Integer group = new Integer(spt.getGroupArray(0));
			if (!sptGroups.containsKey(group))
			{
				sptGroups.put(group, new ArrayList<TSePoTri>());
			}
			sptGroups.get(group).add(spt);
		}
		return sptGroups.values();
	}

	/**
	 * Generates a Criteria based upon a CNF. A boolean expression is said to be
	 * in Conjunctive Normal Form if it is expressed as a conjunction of
	 * disjunctions of literals (positive or negative atoms).
	 * 
	 * @param spts
	 *            the Service Trigger Points
	 * @return the compiled criteria matcher
	 */
	private CriteriaMatch generateCNFCriteria(TSePoTri[] spts)
	{
		AndCriteria and = new AndCriteria();

		Collection<List<TSePoTri>> groups = generateGroups(spts);
		Iterator<List<TSePoTri>> it = groups.iterator();
		while (it.hasNext())
		{
			OrCriteria or = new OrCriteria();
			List<TSePoTri> group = it.next();
			Iterator<TSePoTri> it2 = group.iterator();
			while (it2.hasNext())
			{
				TSePoTri spt = it2.next();
				CriteriaMatch c = generateSPTCriteria(spt);
				or.addCriterion(c);
			}
			and.addCriterion(or);
		}
		return and;
	}

	/**
	 * Generates a Criteria based upon a DNF. A boolean expression is said to be
	 * in Disjunctive Normal Form if it is expressed as a disjunction of
	 * conjunctions of literals (positive or negative atoms).
	 * 
	 * @param spts
	 *            the Service Trigger Points
	 * @return the compiled criteria matcher
	 */

	private CriteriaMatch generateDNFCriteria(TSePoTri[] spts)
	{
		OrCriteria or = new OrCriteria();

		Collection<List<TSePoTri>> groups = generateGroups(spts);
		Iterator<List<TSePoTri>> it = groups.iterator();
		while (it.hasNext())
		{
			AndCriteria and = new AndCriteria();
			List<TSePoTri> group = it.next();
			Iterator<TSePoTri> it2 = group.iterator();
			while (it2.hasNext())
			{
				TSePoTri spt = (TSePoTri) it2.next();
				CriteriaMatch c = generateSPTCriteria(spt);
				and.addCriterion(c);
			}
			or.addCriterion(and);
		}
		return or;
	}

	/**
	 * Generates a single SPT matcher
	 * 
	 * @param spt
	 *            the SPT to compile
	 * @return the SPT matcher
	 */
	private CriteriaMatch generateSPTCriteria(TSePoTri spt)
	{
		CriteriaMatch c = null;
		if (spt.getMethod() != null)
		{
			c = new MethodCriteria(spt.getMethod());
		}
		else if (spt.getRequestURI() != null)
		{
			c = new RURICriteria(spt.getRequestURI());
		}
		else if (spt.getSIPHeader() != null)
		{
			c = new HeaderCriteria(spt.getSIPHeader());
		}
		else if (spt.getSessionDescription() != null)
		{
			// TODO add SDP matching
		}
		else
		{
			// JAXB sessionCase is always initialized to 0 if not present in XML
			// ...
			// we can be sure that session case tag is in SPT
			// only if there are no other SPT type
			c = new SessionCaseCriteria(spt.getSessionCase());
		}

		if (spt.getConditionNegated())
		{
			c = new NotCriteria(c);
		}
		return c;
	}
}
