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

import java.util.Iterator;
import java.util.List;

import org.cipango.ims.hss.db.SubscriptionDao;
import org.cipango.ims.hss.model.PrivateIdentity;
import org.cipango.ims.hss.model.PublicPrivate;
import org.cipango.ims.hss.model.Subscription;
import org.hibernate.SessionFactory;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

public class SubscriptionDaoImpl extends AbstractHibernateDao<Subscription> implements SubscriptionDao
{
	public SubscriptionDaoImpl(SessionFactory sessionFactory)
	{
		super(sessionFactory);
	}
	
	public void save(Subscription subscription)
	{
		currentSession().saveOrUpdate(subscription);
	}
	
	@Transactional (readOnly = false, propagation = Propagation.REQUIRES_NEW)
	public void saveWithCascade(Subscription subscription)
	{
		currentSession().saveOrUpdate(subscription);
		Iterator<PrivateIdentity> it = subscription.getPrivateIdentities().iterator();
		while (it.hasNext())
		{
			PrivateIdentity privateIdentity = it.next();
			currentSession().saveOrUpdate(privateIdentity);
			Iterator<PublicPrivate> it2 = privateIdentity.getPublicIdentities().iterator();
			while (it2.hasNext())
				currentSession().saveOrUpdate(it2.next().getPublicIdentity());
		}
		
	}
	
	public Subscription findById(long id)
	{
		return get(id);
	}
	
	public List<Subscription> findAll()
	{
		return all();
	}
}
