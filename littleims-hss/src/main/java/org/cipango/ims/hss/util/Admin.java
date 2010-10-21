package org.cipango.ims.hss.util;

import java.util.Arrays;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.PropertyConfigurator;
import org.cipango.ims.hss.db.PrivateIdentityDao;
import org.cipango.ims.hss.db.ScscfDao;
import org.cipango.ims.hss.db.SubscriptionDao;
import org.cipango.ims.hss.model.PrivateIdentity;
import org.cipango.ims.hss.model.Scscf;
import org.cipango.ims.hss.model.Subscription;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.orm.hibernate3.HibernateTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

public class Admin 
{
	public static void main(String[] args)
	{
		//BasicConfigurator.configure();
		
		ApplicationContext context = new ClassPathXmlApplicationContext("hss.xml");
		HibernateTransactionManager tm = (HibernateTransactionManager) context.getBean("transactionManager");
		TransactionTemplate tt = new TransactionTemplate(tm);
		
		final SubscriptionDao dao = (SubscriptionDao) context.getBean("subscriptionDao");
		final ScscfDao scscfDao = (ScscfDao) context.getBean("scscfDao");
		final PrivateIdentityDao impiDao = (PrivateIdentityDao) context.getBean("privateIdentityDao");
		
		try
		{
		tt.execute(new TransactionCallbackWithoutResult() 
		{
		    protected void doInTransactionWithoutResult(TransactionStatus status) 
		    {
		        try {
		        	Scscf scscf = new Scscf();
		        	scscf.setName("thomas");
		        	scscf.setUri("sip:192.168.1.77");
		        	scscfDao.save(scscf);
		        	
		        	Subscription sub = new Subscription();
		        	sub.setScscf(scscf);
		        	dao.save(sub);
		        	PrivateIdentity impi = new PrivateIdentity();
		        	byte[] b = new byte[16];
		        	Arrays.fill(b, (byte) 0);

		        	impi.setOperatorId(b);
		        	
		        	impi.setIdentity("thomas@cipango.org");
		        	impi.setPassword("thomas".getBytes());
		        	impi.setSqn(b);
		        	impi.setSubscription(sub);
		        	sub.getPrivateIdentities().add(impi);
		        	
		        	impiDao.save(impi);
		        	//dao.save(sub);
		        } catch (Exception ex) {
		        	
		            status.setRollbackOnly();
		        }
		    }
		});
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
