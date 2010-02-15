package org.cipango.ims.hss.db.hibernate;

import org.cipango.ims.hss.db.UssDao;
import org.cipango.ims.hss.model.uss.Uss;
import org.hibernate.SessionFactory;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

public class UssDaoImpl extends AbstractHibernateDao<Uss> implements UssDao
{

	public UssDaoImpl(SessionFactory sessionFactory)
	{
		super(sessionFactory);
	}

	public Uss findById(Long id)
	{
		return get(id);
	}
	
	@Transactional (readOnly = false, propagation = Propagation.REQUIRES_NEW)
	public void save(Uss uss)
	{
		currentSession().saveOrUpdate(uss);
	}

}
