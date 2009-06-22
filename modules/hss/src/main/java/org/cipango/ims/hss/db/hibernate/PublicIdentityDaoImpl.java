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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.cipango.ims.hss.db.PublicIdentityDao;
import org.cipango.ims.hss.model.PublicIdentity;
import org.cipango.littleims.util.RegexUtil;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

public class PublicIdentityDaoImpl extends AbstractHibernateDao<PublicIdentity> implements PublicIdentityDao
{
	private static final String FIND_LIKE = 
		"SELECT p._identity FROM PublicIdentity AS p WHERE LOWER(p._identity) LIKE :id order by p._identity asc";
	
	private static final String GET_BY_IDENTITY =
		"FROM PublicIdentity WHERE _identity = :key";
	
	private static final String FIND_WILCARD = 
		"FROM PublicIdentity AS p WHERE :id like _regex";
	
	private static final String GET_ALL_WILCARDS = 
		"SELECT p._identity FROM PublicIdentity AS p WHERE _regex != null";
			
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

	public PublicIdentity findWilcard2(String id)
	{
		Query query = currentSession().createQuery(FIND_WILCARD);
		query.setParameter("id", id);
		// FIXME case NonUniqueResult ??
		return (PublicIdentity) query.uniqueResult();
	}
	
	@SuppressWarnings("unchecked")
	public PublicIdentity findWilcard(String id)
	{
		Iterator<String> it = currentSession().createQuery(GET_ALL_WILCARDS).list().iterator();
		while (it.hasNext())
		{
			String identity = it.next();
			if (id.matches(RegexUtil.extendedRegexToJavaRegex(identity)))
				return findById(identity);	
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public List<String> findWilcards(String id)
	{
		Iterator<String> it = currentSession().createQuery(GET_ALL_WILCARDS).list().iterator();
		List<String> list = new ArrayList<String>();
		while (it.hasNext())
		{
			String identity = it.next();
			if (id.matches(RegexUtil.extendedRegexToJavaRegex(identity)))
				list.add(identity);	
		}
		return list;
	}

	@SuppressWarnings("unchecked")
	public Iterator<PublicIdentity> iterator(int first, int count, String sort,
			boolean sortAsc, String foreignKeyName, Long foreignKeyId)
	{
		if (foreignKeyId == null || foreignKeyName == null)
			return iterator(first, count, sort, sortAsc);
		
		StringBuilder hql = new StringBuilder();
    	hql.append("FROM ").append(isPsiField(foreignKeyName) ? "PSI" : "PublicIdentity").append(" AS p");
		hql.append(" WHERE p.").append(foreignKeyName).append(".id = :id ");
		if (sort != null && !sort.trim().equals("")) 
			hql.append(" order by ").append(sort).append((sortAsc) ? " asc" : " desc");
		
		Query query = query(hql.toString());
		if (count > 0)
			query.setMaxResults(count);
		query.setParameter("id", foreignKeyId);
		query.setFirstResult(first);
		
    	return query.list().iterator();	
	}
	
	@SuppressWarnings("unchecked")
	public Iterator<PublicIdentity> likeIterator(int first, int count, String sort,
			boolean sortAsc, String likeIdentity)
	{
		if (likeIdentity == null)
			return iterator(first, count, sort, sortAsc);
				
		StringBuilder hql = new StringBuilder();
    	hql.append("FROM PublicIdentity AS p WHERE LOWER(p._identity) LIKE :id");

		Iterator<String> it  = findWilcards(likeIdentity).iterator();
		while (it.hasNext())
    		hql.append(" OR p._identity = \'").append(it.next()).append('\'');
			
		if (sort != null && !sort.trim().equals("")) 
			hql.append(" order by ").append(sort).append((sortAsc) ? " asc" : " desc");
		
		Query query = query(hql.toString());
		if (count > 0)
			query.setMaxResults(count);
		query.setParameter("id", "%" + likeIdentity + "%");
		query.setFirstResult(first);
    	return query.list().iterator();	
	}
	
	public int countLike(String likeIdentity)
	{
		if (likeIdentity == null)
			return count();
		Query query = query("SELECT count(*) FROM PublicIdentity AS p WHERE LOWER(p._identity) LIKE :id");
		query.setParameter("id", "%" + likeIdentity + "%");
		return ((Long) query.uniqueResult()).intValue() + findWilcards(likeIdentity).size();
	}

	private boolean isPsiField(String name)
	{
		return "_scscf".equals(name) || "_applicationServer".equals(name) || "_privateServiceIdentity".equals(name);
	}

	
}
