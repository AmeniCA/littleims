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

import org.cipango.ims.hss.db.ServiceProfileDao;
import org.cipango.ims.hss.model.ServiceProfile;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

public class ServiceProfileDaoImpl extends AbstractHibernateDao<ServiceProfile> implements ServiceProfileDao
{
	private static final String GET_BY_NAME =
		"FROM ServiceProfile WHERE _name = :key";
	
	private static final String GET_AVAILABLE_IFCS =
		"SELECT i._name FROM InitialFilterCriteria AS i WHERE i.id NOT IN (" +
			"SELECT i.id FROM InitialFilterCriteria AS i JOIN i._serviceProfiles AS s WITH s.id = :profileId) ORDER BY i._name";
	
	
	private static final String GET_AVAILABLE_GROUPS =
		"SELECT g FROM Group AS g WHERE g.id NOT IN (" +
			"SELECT g.id FROM Group AS g JOIN g.users AS u WITH u.id = :user) ORDER BY g.name";
	
	public ServiceProfileDaoImpl(SessionFactory sessionFactory) 
	{
		super(sessionFactory);
	}
	
	@Transactional (readOnly = false, propagation = Propagation.REQUIRES_NEW)
	public void save(ServiceProfile serviceProfile)
	{
		currentSession().saveOrUpdate(serviceProfile);
	}

	public ServiceProfile findById(String id)
	{
		Query query = currentSession().createQuery(GET_BY_NAME);
		query.setParameter("key", id);
		return (ServiceProfile) query.uniqueResult();
	}

	@SuppressWarnings("unchecked")
	public List<String> getAvailableIfc(ServiceProfile serviceProfile)
	{
		Query query = currentSession().createQuery(GET_AVAILABLE_IFCS);
    	query.setLong("profileId", serviceProfile.getId());
    	return query.list();
	}

}
