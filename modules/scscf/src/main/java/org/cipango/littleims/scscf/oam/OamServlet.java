package org.cipango.littleims.scscf.oam;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;

import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cipango.littleims.scscf.data.InitialFilterCriteria;
import org.cipango.littleims.scscf.data.ServiceProfile;
import org.cipango.littleims.scscf.data.UserProfile;
import org.cipango.littleims.scscf.registrar.Context;
import org.cipango.littleims.scscf.session.Session;
import org.cipango.littleims.scscf.session.SessionManager;
import org.springframework.beans.BeansException;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

public class OamServlet extends HttpServlet
{

	private SessionManager _sessionManager;
	
	public void init() throws ServletException
	{
		WebApplicationContext context = WebApplicationContextUtils
		.getWebApplicationContext(getServletContext());

		try 
		{
			_sessionManager = (SessionManager) context.getBean("sessionManager");
		} 
		catch (BeansException e) 
		{
			throw new UnavailableException("no session manager " + e);
		}
	}
	
	private void printSessions(PrintWriter out)
	{

		out.println("<h2>Sessions</h2>");
		out.println("<table border=\"1\" cellspacing=\"0\">" +
		"<th>AOR</th><th>Barred</th><th>Originating</th><th>Current IFC</th>");

		Iterator<Session> it = _sessionManager.getSessions();
		synchronized (it)
		{
			while (it.hasNext())
			{
				Session session = it.next();
				UserProfile profile = session.getProfile();
				
				out.println("<tr>");
				out.println("<td>" + profile.getURI() + "</td>");
				out.println("<td>" + profile.isBarred() +  "</td>");
				out.println("<td>" + session.isOriginating() + "</td>");
				out.println("<td>" + session.getCurrentIfc() + "</td>");
				out.println("</tr>");
			}
		}
		out.println("</table>");
	}

	
	private void printUsers(PrintWriter out)
	{
		out.println("<h2>Registered users</h2>");
		Iterator<Context> it = _sessionManager.getRegistrar().getRegContextsIt();
		synchronized (it)
		{
			out.println("<table border=\"1\" cellspacing=\"0\">" +
			"<th>AOR</th><th>State</th><th>Service Profile</th>");
			while (it.hasNext())
			{
				Context context = it.next();
							
				out.println("<tr>");
				out.println("<td>" + context.getRegInfo().getAor() + "</td>");
				out.println("<td>" + context.getState().getValue() +  "</td>");
				out.println("<td>");
				printProfile(context.getRegInfo().getAor(), out);
				out.println("</td>");
				out.println("</tr>");
			}
			out.println("</table>");
		}
	}
	
	private void printProfiles(Iterator<UserProfile> it, PrintWriter out)
	{
		synchronized (it)
		{
			out.println("<table border=\"1\" cellspacing=\"0\">" +
			"<th>AOR</th><th>Barred</th><th>Service Profile</th>");
			while (it.hasNext())
			{
				UserProfile profile = it.next();
				out.println("<tr>");
				out.println("<td>" + profile.getURI() + "</td>");
				out.println("<td>" + profile.isBarred() +  "</td>");
				out.println("<td>");
				printProfile(profile, out);
				out.println("</td>");
				out.println("</tr>");
				
			}
			out.println("</table>");
		}
	}
	
	private void printProfile(String aor, PrintWriter out)
	{
		printProfile(_sessionManager.getUserProfileCache().getProfile(aor, null), out);
	}
	
	private void printProfile(UserProfile userProfile, PrintWriter out)
	{
		if (userProfile == null) 
		{
			out.write("No user profile");
			return;
		}
		if (userProfile.isBarred())
		{
			out.write("Profile is barred");
			return;
		}
		
		ServiceProfile serviceProfile = userProfile.getServiceProfile();
		if (serviceProfile == null)
		{
			out.write("No service profile");
			return;
		}

		Iterator<InitialFilterCriteria> it = serviceProfile.getIFCsIterator();
		if (!it.hasNext())
		{
			out.write("No IFC defined");
			return;
		}
		out.println("<table border=\"1\" cellspacing=\"0\">" +
		"<th>Priority</th><th>Trigger point</th><th>AS</th>");
		while (it.hasNext())
		{
			InitialFilterCriteria ifc = (InitialFilterCriteria) it.next();
			out.println("<tr>");
			out.println("<td>" + ifc.getPriority() + "</td>");
			out.println("<td>" + ifc.getTriggerPoint() +  "</td>");
			out.println("<td>" + ifc.getAS().getURI() + "</td>");
			out.println("</tr>");
		}
		out.println("</table>");
	}



	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
			IOException
	{
		PrintWriter out = resp.getWriter();
		out.println("<html><head><title>OAM</title></head><body>");
		printSessions(out);
		printUsers(out);
		
		clearProfiles(req, out);
		out.println("<h2>Cache user profiles</h2>");
		printProfiles(_sessionManager.getRegistrar().getUserProfileCache().getUserProfiles().iterator(), out);
		out.println("<h2>Cache wilcard user profiles</h2>");
		printProfiles(_sessionManager.getRegistrar().getUserProfileCache().getWildcardUserProfiles().iterator(), out);
		
		out.println("</body></html>");
	}
	
	private void clearProfiles(HttpServletRequest req, PrintWriter out)
	{
		String action = req.getParameter("action");
		if (action != null && action.equals("clear user profiles cache"))
		{
			_sessionManager.getRegistrar().getUserProfileCache().clearAllProfiles();
		}
		out.print("<form method=\"get\" action=\"#\">");
		out.print("<input type=\"submit\" name=\"action\" value=\"clear user profiles cache\"/>");
		out.print("</form>");
		
	}
	
}
