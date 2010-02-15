package org.cipango.ims.hss.db.hibernate;

import java.util.List;

import org.cipango.ims.hss.db.NafGroupDao;
import org.cipango.ims.hss.model.NafGroup;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

public class NafGroupDaoImpl extends AbstractHibernateDao<NafGroup> implements NafGroupDao
{
	private static final String GET_BY_NAME =
		"FROM NafGroup WHERE _name = :key";
	
	public NafGroupDaoImpl(SessionFactory sessionFactory)
	{
		super(sessionFactory);
	}

	public NafGroup findById(String id)
	{
		Query query = currentSession().createQuery(GET_BY_NAME);
		query.setParameter("key", id);
		return (NafGroup) query.uniqueResult();
	}
	
	@Transactional (readOnly = false, propagation = Propagation.REQUIRES_NEW)
	public void save(NafGroup nafGroup)
	{
		currentSession().saveOrUpdate(nafGroup);
	}

	@SuppressWarnings("unchecked")
	public List<NafGroup> getAllGroups()
	{
		return criteria().list();
	}

}
