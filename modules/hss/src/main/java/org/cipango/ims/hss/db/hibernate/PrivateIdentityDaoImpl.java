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

import org.cipango.ims.hss.db.PrivateIdentityDao;
import org.cipango.ims.hss.model.PrivateIdentity;
import org.cipango.ims.hss.model.PublicPrivate;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

public class PrivateIdentityDaoImpl extends AbstractHibernateDao<PrivateIdentity> implements PrivateIdentityDao
{
	private static final String GET_AVAILABLE_PUBLIC_IDS =
		"SELECT p._publicIdentity.id FROM PublicPrivate AS p WHERE p._publicIdentity NOT IN (" +
			"SELECT p._publicIdentity.id FROM PublicPrivate AS p WHERE p._privateIdentity = :privateId)" +
			" AND p._privateIdentity._subscription.id = :subscription ORDER BY p._publicIdentity.id";
	
	private static final String GET_AVAILABLE_PUBLIC_IDS_NO_SUB =
		"SELECT p.id FROM PublicIdentity AS p WHERE p._privateIdentities IS EMPTY";

	
	
	public PrivateIdentityDaoImpl(SessionFactory sessionFactory)
	{
		super(sessionFactory);
	}
	
	@Transactional  (readOnly = false, propagation = Propagation.REQUIRES_NEW)
	public void save(PrivateIdentity privateIdentity)
	{
		currentSession().saveOrUpdate(privateIdentity);
	}
	
	public PrivateIdentity findById(String id)
	{
		return get(id);
	}
	
	public List<PrivateIdentity> findAll()
	{
		return all();
	}
	
	@SuppressWarnings("unchecked")
	public List<String> getAvalaiblePublicIds(PrivateIdentity privateIdentity)
	{
		Query query = currentSession().createQuery(GET_AVAILABLE_PUBLIC_IDS);
    	query.setParameter("privateId", privateIdentity);
    	query.setLong("subscription", privateIdentity.getSubscription().getId());
    	return query.list();
	}
	
	@SuppressWarnings("unchecked")
	public List<String> getAvalaiblePublicIdsNoPrivate()
	{
		Query query = currentSession().createQuery(GET_AVAILABLE_PUBLIC_IDS_NO_SUB);
    	return query.list();
	}
	
	
	@Transactional  (readOnly = false, propagation = Propagation.REQUIRES_NEW)
	public void delete(PublicPrivate publicPrivate) {
	  	publicPrivate.getPrivateIdentity().getPublicIdentities().remove(publicPrivate);
	  	publicPrivate.getPublicIdentity().getPrivateIdentities().remove(publicPrivate);
		currentSession().delete(publicPrivate);
	}

	public PublicPrivate getPublicPrivate(String publicId, String privateId)
	{
		return (PublicPrivate) currentSession().get(PublicPrivate.class, new PublicPrivate.Id(publicId, privateId));
	}
}
