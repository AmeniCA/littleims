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

import java.util.List;

import org.cipango.ims.hss.db.PublicIdentityDao;
import org.cipango.ims.hss.model.PublicIdentity;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

public class PublicIdentityDaoImpl extends AbstractHibernateDao<PublicIdentity> implements PublicIdentityDao
{
	private static final String FIND_LIKE = "SELECT p._identity FROM PublicIdentity AS p WHERE LOWER(p._identity) LIKE :id order by p._identity asc";
	
	private static final String GET_BY_IDENTITY =
		"FROM PublicIdentity WHERE _identity = :key";
	
	public PublicIdentityDaoImpl(SessionFactory sessionFactory)
	{
		super(sessionFactory);
	}

	public List<PublicIdentity> findAll() 
	{
		return all();
	}

	public PublicIdentity findById(String id)
	{
		Query query = currentSession().createQuery(GET_BY_IDENTITY);
		query.setParameter("key", id);
		return (PublicIdentity) query.uniqueResult();
	}

	@Transactional  (readOnly = false, propagation = Propagation.REQUIRES_NEW)
	public void save(PublicIdentity impu) 
	{
		currentSession().saveOrUpdate(impu);
	}
	
	@SuppressWarnings("unchecked")
	public List<String> findLike(String id, int maxResults)
	{
		Query query = query(FIND_LIKE);
		if (maxResults > 0)
			query.setMaxResults(maxResults);
		query.setParameter("id", id.toLowerCase());
		
    	return query.list();
	}
	
}
