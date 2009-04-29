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

import org.cipango.ims.hss.db.ScscfDao;
import org.cipango.ims.hss.model.Scscf;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

public class ScscfDaoImpl extends AbstractHibernateDao<Scscf> implements ScscfDao
{
	private static final String GET_BY_NAME =
		"FROM Scscf WHERE _name = :key";
	private static final String GET_BY_URI =
		"FROM Scscf WHERE _uri = :key";
	private static final String NB_SUBSCRIPTIONS =
		"SELECT count(*) FROM Subscription AS s WHERE s._scscf.id = :scscf";
	
	private static final String FIND_AVAILABLE_SCSCF =
		"SELECT s._scscf.id FROM Subscription AS s  WHERE s._scscf.id != null GROUP BY s._scscf.id ORDER BY count(s)";
	
	private static final String FIND_UNUSE_SCSCF =
		"FROM Scscf AS s WHERE s._subscriptions IS EMPTY";
	
	public ScscfDaoImpl(SessionFactory sessionFactory) 
	{
		super(sessionFactory);
	}
	
	@Transactional  (readOnly = false, propagation = Propagation.REQUIRES_NEW)
	public void save(Scscf scscf)
	{
		currentSession().saveOrUpdate(scscf);
	}

	public Scscf findById(String id)
	{
		Query query = currentSession().createQuery(GET_BY_NAME);
		query.setParameter("key", id);
		return (Scscf) query.uniqueResult();
	}

	public long getNbSubscriptions(Scscf scscf)
	{
		Query query = currentSession().createQuery(NB_SUBSCRIPTIONS);
		query.setParameter("scscf", scscf.getId());
		return (Long) query.uniqueResult();
	}

	public Scscf findAvailableScscf()
	{
		Scscf scscf = (Scscf) currentSession().createQuery(FIND_UNUSE_SCSCF).setMaxResults(1).uniqueResult();
		if (scscf != null)
			return scscf;
		Long id =  (Long) currentSession().createQuery(FIND_AVAILABLE_SCSCF).setMaxResults(1).uniqueResult();
		if (id == null)
			return null;
		return get(id);
	}

	public Scscf findByUri(String uri)
	{
		Query query = currentSession().createQuery(GET_BY_URI);
		query.setParameter("key", uri);
		return (Scscf) query.uniqueResult();
	}
}
