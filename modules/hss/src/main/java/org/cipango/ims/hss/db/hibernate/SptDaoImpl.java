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

import org.cipango.ims.hss.db.SptDao;
import org.cipango.ims.hss.model.spt.SPT;
import org.hibernate.SessionFactory;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

public class SptDaoImpl extends AbstractHibernateDao<SPT> implements SptDao
{

	public SptDaoImpl(SessionFactory sessionFactory) 
	{
		super(sessionFactory);
	}
	
	public SPT findById(Long id)
	{
		return (SPT) get(id);
	}

	@Transactional  (readOnly = false, propagation = Propagation.REQUIRES_NEW)
	public void save(SPT spt)
	{
		currentSession().saveOrUpdate(spt);
	}


}
