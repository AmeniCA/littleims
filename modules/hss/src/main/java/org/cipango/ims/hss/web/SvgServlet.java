package org.cipango.ims.hss.web;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.log4j.Logger;
import org.cipango.ims.hss.db.SubscriptionDao;
import org.cipango.ims.hss.model.ImplicitRegistrationSet;
import org.cipango.ims.hss.model.PrivateIdentity;
import org.cipango.ims.hss.model.PublicUserIdentity;
import org.cipango.ims.hss.model.ServiceProfile;
import org.cipango.ims.hss.model.Subscription;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.w3c.dom.Node;

public class SvgServlet extends HttpServlet 
{

	private SubscriptionDao _dao;
	private static final Logger __log = Logger.getLogger(SvgServlet.class);
	
	public void init()
	{
		ApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(getServletContext());
		_dao = (SubscriptionDao) context.getBean("subscriptionDao");
	}
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		
		resp.setContentType("image/svg+xml");
		PrintWriter out;
		
		boolean shouldApplyXsl = false;
		ByteArrayOutputStream os = null;
		// Only Firefox and Chrome applies XSL on a XML document.
		String userAgent = req.getHeader("User-Agent");
		if (userAgent == null || (userAgent.indexOf("Firefox") == -1 && userAgent.indexOf("Chrome") == -1))
		{
			shouldApplyXsl = true;
			os = new ByteArrayOutputStream();
			out = new PrintWriter(os);
		}
		else
			 out = resp.getWriter();
		
		String key = req.getParameter("key");
		Subscription subscription = _dao.findById(key);
		
		out.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		out.append("<?xml-stylesheet type=\"text/xsl\" href=\"subscriptionToSvg.xsl\"?>\n");
		out.append("<subscription>\n");
		
		if (subscription == null)
		{
			out.append("\t<name>").append(key).append("</name>\n");
			out.append("</subscription>\n");
			return;
		}
		
		out.append("\t<name>").append(subscription.getName()).append("</name>\n");
		
		List<PrivateIdentity> privateIds = new ArrayList<PrivateIdentity>(subscription.getPrivateIdentities());
		out.append("\t<PrivateIDs>\n");
		for (PrivateIdentity privateIdentity : privateIds)
		{
			out.append("\t\t<PrivateID>").append(privateIdentity.getIdentity()).append("</PrivateID>\n");
		}
		out.append("\t</PrivateIDs>\n");
		
		List<ImplicitRegistrationSet> implicitSets = new ArrayList<ImplicitRegistrationSet>();
		List<ServiceProfile> serviceProfiles = new ArrayList<ServiceProfile>();
		
		for (PublicUserIdentity publicIdentity : subscription.getPublicIdentities())
		{
			if (!implicitSets.contains(publicIdentity.getImplicitRegistrationSet()))
				implicitSets.add(publicIdentity.getImplicitRegistrationSet());
			ServiceProfile sp = publicIdentity.getServiceProfile();
			if (sp != null && !serviceProfiles.contains(sp))
				serviceProfiles.add(sp);
		}
		
		List<PublicUserIdentity> publicIds = new ArrayList<PublicUserIdentity>();
		
		out.append("\t<ImplicitSets>\n");
		for (ImplicitRegistrationSet set : implicitSets)
		{
			int min = Integer.MAX_VALUE;
			int max = Integer.MIN_VALUE;
			for (PublicUserIdentity publicIdentity : set.getPublicIdentities())
			{
				if (!publicIds.contains(publicIdentity))
					publicIds.add(publicIdentity);
				int index = publicIds.indexOf(publicIdentity);
				if (index < min)
					min = index;
				if (index > max)
					max = index;
			}
			out.append("\t\t<ImplicitSet>\n");
			out.append("\t\t\t<Id>").append(set.getId().toString()).append("</Id>\n");
			out.append("\t\t\t<From>").append(Integer.toString(min + 1)).append("</From>\n");
			out.append("\t\t\t<To>").append(Integer.toString(max + 1)).append("</To>\n");
			out.append("\t\t</ImplicitSet>\n");
		}
		out.append("\t</ImplicitSets>\n");
		
		out.append("\t<PublicIdentities>\n");
		for (PublicUserIdentity publicIdentity : publicIds)
		{
			out.append("\t\t<PublicIdentity>\n");
			out.append("\t\t\t<Identity>").append(publicIdentity.getIdentity()).append("</Identity>\n");
			out.append("\t\t\t<ServiceProfile>");
			out.append(Integer.toString(serviceProfiles.indexOf(publicIdentity.getServiceProfile()) + 1));
			out.append("</ServiceProfile>\n");
			out.append("\t\t\t<Privates>\n");
			for (PrivateIdentity privateIdentity : publicIdentity.getPrivateIdentities())
			{
				out.append("\t\t\t\t<Id>");
				out.append(Integer.toString(privateIds.indexOf(privateIdentity) + 1));
				out.append("</Id>\n");
			}
			out.append("\t\t\t</Privates>\n");
			out.append("\t\t</PublicIdentity>\n");
		}
		out.append("\t</PublicIdentities>\n");
		
		out.append("\t<ServiceProfiles>\n");
		for (ServiceProfile serviceProfile : serviceProfiles)
		{
			out.append("\t\t<ServiceProfile>").append(serviceProfile.getName()).append("</ServiceProfile>\n");
		}
		out.append("\t</ServiceProfiles>\n");
		
		out.append("</subscription>\n");
		
		if (shouldApplyXsl)
		{
			out.flush();
			resp.getOutputStream().write(doXsl(os.toByteArray()));
			out.close();
		}
	}


	private byte[] doXsl(byte[] source)
	{
		try
		{
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			StreamResult result = new StreamResult(os);
			TransformerFactory factory = TransformerFactory.newInstance();
			DocumentBuilderFactory documentBuilderFactory = 
				DocumentBuilderFactory.newInstance();
			DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
			
			Node doc = documentBuilder.parse(new ByteArrayInputStream(source));
			
			Transformer transformer = factory.newTransformer(
					new StreamSource(getServletContext().getResourceAsStream("/svg/subscriptionToSvg.xsl")));
			transformer.transform(new DOMSource(doc), result);
			return os.toByteArray();
		}
		catch (Throwable e)
		{
			__log.warn("Unable to do XSL transformation", e);
			return source;
		}
	}

	
}
