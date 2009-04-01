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

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.Iterator;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Projections;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

public abstract class AbstractHibernateDao<T>
{
	private final Class<T> _entityClass;
	private final SessionFactory _sessionFactory;
	
	@SuppressWarnings("unchecked")
	public AbstractHibernateDao(SessionFactory sessionFactory)
	{
		_entityClass = (Class<T>) ((ParameterizedType) getClass()
                .getGenericSuperclass()).getActualTypeArguments()[0];
		_sessionFactory = sessionFactory;
	}
	
	protected Criteria criteria()
	{
		return currentSession().createCriteria(_entityClass);
	}
	
	protected Query query(String hql)
	{
		return currentSession().createQuery(hql);
	}
	
	protected Session currentSession()
	{
		return _sessionFactory.getCurrentSession();
	}
	
	@SuppressWarnings("unchecked")
	protected List<T> all() 
	{
        return criteria().list();
    }

    @SuppressWarnings("unchecked")
	protected T get(Serializable id)
    {
        return (T) currentSession().get(_entityClass, id);
    }

    public Class<T> getEntityClass()
    {
        return _entityClass;
    }
    
    public int count() {
		Criteria criteria = currentSession().createCriteria(_entityClass);
		criteria.setProjection(Projections.rowCount());
		return (Integer) criteria.uniqueResult();
	}
    
    @SuppressWarnings("unchecked")
	public Iterator<T> iterator(int first, int count, String sort, boolean sortAsc) 
    {
    	StringBuilder sql = new StringBuilder();
    	sql.append("FROM ").append(_entityClass.getName());
		if (sort != null && !sort.trim().equals("")) {
			sql.append(" order by ").append(sort).append((sortAsc) ? " asc" : " desc");
		}
		Query query = currentSession().createQuery(sql.toString());
		if (count > 0)
			query.setMaxResults(count);
		query.setFirstResult(first);
    	return query.list().iterator();
	}
    
    @Transactional  (readOnly = false, propagation = Propagation.REQUIRES_NEW)
	public void delete(T o) {
		currentSession().delete(o);
	}

}
