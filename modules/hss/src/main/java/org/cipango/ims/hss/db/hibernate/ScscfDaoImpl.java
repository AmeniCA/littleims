package org.cipango.ims.hss.db.hibernate;

import org.cipango.ims.hss.db.ScscfDao;
import org.cipango.ims.hss.model.Scscf;
import org.hibernate.SessionFactory;

public class ScscfDaoImpl extends AbstractHibernateDao<Scscf> implements ScscfDao
{
	public ScscfDaoImpl(SessionFactory sessionFactory) 
	{
		super(sessionFactory);
	}
	
	public void save(Scscf scscf)
	{
		currentSession().saveOrUpdate(scscf);
	}
}
