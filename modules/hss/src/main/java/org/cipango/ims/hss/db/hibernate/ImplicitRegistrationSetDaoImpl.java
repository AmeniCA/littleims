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

import org.cipango.ims.hss.db.ImplicitRegistrationSetDao;
import org.cipango.ims.hss.model.ImplicitRegistrationSet;
import org.hibernate.Query;
import org.hibernate.SessionFactory;

public class ImplicitRegistrationSetDaoImpl extends AbstractHibernateDao<ImplicitRegistrationSet> implements ImplicitRegistrationSetDao
{
	
	private static final String GET_IMPLICIT_REGISTRATION_SET =
		"SELECT DISTINCT i FROM ImplicitRegistrationSet  AS i INNER JOIN i._publicIdentities AS pu " +
		"INNER JOIN pu._privateIdentities AS pr WHERE pr._privateIdentity._subscription.id = :key";
	
	public ImplicitRegistrationSetDaoImpl(SessionFactory sessionFactory)
	{
		super(sessionFactory);
	}
	
	public void save(ImplicitRegistrationSet implicitRegistrationSet)
	{
		currentSession().saveOrUpdate(implicitRegistrationSet);
	}
	
	@SuppressWarnings("unchecked")
	public List<ImplicitRegistrationSet> getImplicitRegistrationSet(Long subscriptionId)
	{
		Query query = currentSession().createQuery(GET_IMPLICIT_REGISTRATION_SET);
		query.setParameter("key", subscriptionId);
		return query.list();
	}

	public ImplicitRegistrationSet findById(Long id)
	{
		return get(id);
	}
}
