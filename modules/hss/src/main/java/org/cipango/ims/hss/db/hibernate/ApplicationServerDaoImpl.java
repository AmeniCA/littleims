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

import org.cipango.ims.hss.db.ApplicationServerDao;
import org.cipango.ims.hss.model.ApplicationServer;
import org.hibernate.Query;
import org.hibernate.SessionFactory;

public class ApplicationServerDaoImpl extends AbstractHibernateDao<ApplicationServer> implements ApplicationServerDao
{
	private static final String GET_BY_NAME =
		"FROM ApplicationServer WHERE _name = :key";
	
	private static final String NB_IFCS =
		"SELECT count(*) FROM InitialFilterCriteria AS ifc WHERE ifc._applicationServer.id = :as";
	
	public ApplicationServerDaoImpl(SessionFactory sessionFactory) 
	{
		super(sessionFactory);
	}
	
	public void save(ApplicationServer applicationServer)
	{
		currentSession().saveOrUpdate(applicationServer);
	}

	public ApplicationServer findById(String id)
	{
		Query query = currentSession().createQuery(GET_BY_NAME);
		query.setParameter("key", id);
		return (ApplicationServer) query.uniqueResult();
	}
	
	public long getNbIfcs(ApplicationServer applicationServer)
	{
		Query query = currentSession().createQuery(NB_IFCS);
		query.setParameter("as", applicationServer.getId());
		return (Long) query.uniqueResult();
	}

}
