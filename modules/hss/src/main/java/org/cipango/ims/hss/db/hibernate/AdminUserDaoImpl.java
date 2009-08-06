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

import org.cipango.ims.hss.db.AdminUserDao;
import org.cipango.ims.hss.model.AdminUser;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

public class AdminUserDaoImpl extends AbstractHibernateDao<AdminUser> implements AdminUserDao
{
	private static final String GET_BY_NAME =
		"FROM AdminUser WHERE _login = :key";
	
	public AdminUserDaoImpl(SessionFactory sessionFactory)
	{
		super(sessionFactory);
	}
	
	@Transactional  (readOnly = false, propagation = Propagation.REQUIRES_NEW)
	public void save(AdminUser adminUser)
	{
		currentSession().saveOrUpdate(adminUser);
	}

	public AdminUser findById(String id)
	{
		Query query = currentSession().createQuery(GET_BY_NAME);
		query.setParameter("key", id);
		return (AdminUser) query.uniqueResult();
	}
	
	@Transactional  (readOnly = false, propagation = Propagation.REQUIRES_NEW)
	public void insertDefaultUserIfNone()
	{
		if (count() == 0)
		{
			AdminUser user = new AdminUser();
			user.setLogin("littleims");
			save(user); // The id is required to set password.
			user.setClearPassword("littleims");
			save(user);
		}
	}

}
