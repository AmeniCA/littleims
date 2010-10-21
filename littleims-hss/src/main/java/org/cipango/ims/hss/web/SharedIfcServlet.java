package org.cipango.ims.hss.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.cipango.ims.hss.db.IfcDao;
import org.cipango.ims.hss.model.InitialFilterCriteria;
import org.cipango.ims.hss.util.XML;
import org.cipango.ims.hss.util.XML.Output;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

public class SharedIfcServlet extends HttpServlet
{

	private IfcDao _dao;
	
	public void init()
	{
		ApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(getServletContext());
		_dao = (IfcDao) context.getBean("ifcDao");
	}
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException
	{
		
		resp.setContentType("text/xml");
		Output out = XML.getPretty().newOutput();
		out.open("SharedIFCs");
		Iterator<InitialFilterCriteria> it = _dao.getAllSharedIfcs().iterator();
		while (it.hasNext())
		{
			InitialFilterCriteria ifc = it.next();
			out.open("SharedIFC");
			out.add("ID", ifc.getId());
			out.add("InitialFilterCriteria", ifc);
			out.close("SharedIFC");
		}
		out.close("SharedIFCs");
		resp.getWriter().print(out.toString());
	}
	
}
