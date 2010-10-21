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

package org.cipango.ims.hss.db.hibernate;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.cipango.ims.hss.db.IfcDao;
import org.cipango.ims.hss.model.InitialFilterCriteria;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

public class IfcDaoImpl extends AbstractHibernateDao<InitialFilterCriteria> implements IfcDao
{
	private static final String GET_BY_NAME =
		"FROM InitialFilterCriteria WHERE _name = :key";
	
	private static final String GET_ALL_SHARED =
		"SELECT ifc FROM InitialFilterCriteria  AS ifc JOIN ifc._serviceProfiles AS spIfc WITH spIfc._shared = true";
	
	private static final String COUNT_BY_AS =
		"SELECT count(*) FROM InitialFilterCriteria AS i WHERE i._applicationServer.id = :as";

	private static final String IFC_SAME_PRIORITY =
		"SELECT ifc FROM InitialFilterCriteria  AS ifc JOIN ifc._serviceProfiles AS spIfc WHERE ifc._priority = :priority AND ifc.id != :ifcId " +
		"AND spIfc._serviceProfile.id IN" +
		"(SELECT i._serviceProfile.id FROM SpIfc AS i WHERE i._ifc.id = :ifcId)";
	
	public IfcDaoImpl(SessionFactory sessionFactory) 
	{
		super(sessionFactory);
	}
	
	@Transactional (readOnly = false, propagation = Propagation.REQUIRES_NEW)
	public void save(InitialFilterCriteria ifc)
	{
		currentSession().saveOrUpdate(ifc);
	}

	public InitialFilterCriteria findById(String id)
	{
		Query query = currentSession().createQuery(GET_BY_NAME);
		query.setParameter("key", id);
		return (InitialFilterCriteria) query.uniqueResult();
	}

	public InitialFilterCriteria findByRealKey(Integer id)
	{
		return get(id);
	}

	@SuppressWarnings("unchecked")
	public List<InitialFilterCriteria> getAllSharedIfcs()
	{
		return currentSession().createQuery(GET_ALL_SHARED).list();
	}

	public int count(Long asId)
	{
		if (asId == null)
			return count();
		Query query = currentSession().createQuery(COUNT_BY_AS);
		query.setParameter("as", asId);
		return ((Long) query.uniqueResult()).intValue();
	}

	@SuppressWarnings("unchecked")
	public Iterator<InitialFilterCriteria> iterator(int first, int count,
			String sort, boolean sortAsc, Long asId)
	{
		if (asId == null)
			return iterator(first, count, sort, sortAsc);
		
		StringBuilder hql = new StringBuilder();
    	hql.append("FROM InitialFilterCriteria AS i");
		hql.append(" WHERE i._applicationServer.id = :asId ");
		if (sort != null && !sort.trim().equals("")) 
			hql.append(" order by ").append(sort).append((sortAsc) ? " asc" : " desc");
		
		Query query = query(hql.toString());
		if (count > 0)
			query.setMaxResults(count);
		query.setParameter("asId", asId);
		query.setFirstResult(first);
		
    	return query.list().iterator();	
	}

	@SuppressWarnings("unchecked")
	public List<InitialFilterCriteria> getIfcsWithSamePriority(InitialFilterCriteria ifc, int priority)
	{
		if (ifc == null)
			return Collections.EMPTY_LIST;
		Query query = query(IFC_SAME_PRIORITY);
		query.setParameter("priority", priority);
		query.setParameter("ifcId", ifc.getId());
		return query.list();
	}

}
