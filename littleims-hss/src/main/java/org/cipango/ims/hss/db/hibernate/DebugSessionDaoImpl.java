package org.cipango.ims.hss.db.hibernate;

import org.cipango.ims.hss.db.DebugSessionDao;
import org.cipango.ims.hss.model.DebugSession;
import org.hibernate.SessionFactory;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

public class DebugSessionDaoImpl extends AbstractHibernateDao<DebugSession> implements DebugSessionDao
{

	public DebugSessionDaoImpl(SessionFactory sessionFactory)
	{
		super(sessionFactory);
	}

	public DebugSession findById(Long id)
	{
		return get(id);
	}
	
	@Transactional (readOnly = false, propagation = Propagation.REQUIRES_NEW)
	public void save(DebugSession debugSession)
	{
		currentSession().saveOrUpdate(debugSession);
	}

}
