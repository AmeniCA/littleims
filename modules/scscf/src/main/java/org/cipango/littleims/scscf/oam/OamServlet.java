// ========================================================================
// Copyright 2009 NEXCOM Systems
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
package org.cipango.littleims.scscf.oam;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.sip.SipFactory;
import javax.servlet.sip.URI;

import org.apache.log4j.Logger;
import org.cipango.littleims.scscf.data.InitialFilterCriteria;
import org.cipango.littleims.scscf.data.ServiceProfile;
import org.cipango.littleims.scscf.data.UserProfile;
import org.cipango.littleims.scscf.registrar.Binding;
import org.cipango.littleims.scscf.registrar.Context;
import org.cipango.littleims.scscf.session.Session;
import org.cipango.littleims.scscf.session.SessionManager;
import org.springframework.beans.BeansException;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

public class OamServlet extends HttpServlet
{

	private SessionManager _sessionManager;
	private SipFactory _sipFactory;
	private static final Logger __log = Logger.getLogger(OamServlet.class);
	
	public void init() throws ServletException
	{
		WebApplicationContext context = WebApplicationContextUtils
		.getWebApplicationContext(getServletContext());
		_sipFactory = (SipFactory) getServletContext().getAttribute(SipFactory.class.getName());
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

		Iterator<Session> it = _sessionManager.getSessions().iterator();
		synchronized (it)
		{
			while (it.hasNext())
			{
				Session session = it.next();
				UserProfile profile = session.getProfile();
				
				out.println("<tr>");
				out.println("<td>" + profile.getUri() + "</td>");
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
		Iterator<Context> it = _sessionManager.getRegistrar().getRegContexts().iterator();
		synchronized (it)
		{
			out.println("<table border=\"1\" cellspacing=\"0\">" +
				"<th>AOR</th><th>State</th><th>Contact</th><th>Private identity</th>" +
				"<th>Expires</th><th>Path</th><th>Service Profile</th><th>Action</th>");
			while (it.hasNext())
			{
				Context context = it.next();
							
				out.println("<tr>");
				List<Binding> bindings = context.getBindings();
				String tdRowspan = "<td rowspan=\"" + bindings.size() + "\">" ;
				out.println(tdRowspan + context.getPublicIdentity() + "</td>");
				out.println(tdRowspan + context.getState().getValue() +  "</td>");
				Iterator<Binding> it2 = bindings.iterator();
				boolean first = true;
				while (it2.hasNext())
				{
					Binding binding = (Binding) it2.next();
					if (!first)
						out.println("<tr>");
					out.println("<td>" + binding.getContact().getURI()  +  "</td>");
					out.println("<td>" + binding.getPrivateUserIdentity()  +  "</td>");
					out.println("<td>" + binding.getExpires()  +  "</td>");
					out.println("<td>" + binding.getPath()  +  "</td>");
					if (first)
					{
						first = false;
						out.println(tdRowspan);
						printProfile(context.getPublicIdentity(), out);
						out.println("</td>");
					}
					out.println("<td><form method=\"get\" action=\"#\">");
					out.println("<input type=\"hidden\" name=\"aor\" value=\"" + context.getPublicIdentity() + "\"/>");
					out.println("<input type=\"hidden\" name=\"privateIdentity\" value=\"" + binding.getPrivateUserIdentity() + "\"/>");
					out.println("<input type=\"submit\" name=\"action\" value=\"Network-initiated reauthentication\"/>");
					out.println("</form></td>");
					out.println("</tr>");
				}
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
				out.println("<td>" + profile.getUri() + "</td>");
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
	
	private void printSharedIfc(PrintWriter out)
	{
		Map<Integer, InitialFilterCriteria> sharedIfc = _sessionManager.getUserProfileCache().getSharedIFCs();
		if (sharedIfc == null)
		{
			out.println("<b>Shared IFCs have not been loaded</b>");
			return;
		}
		
		Iterator<Integer> it = sharedIfc.keySet().iterator();
		out.println("<table border=\"1\" cellspacing=\"0\">" +
		"<th>ID</th><th>Priority</th><th>Trigger point</th><th>AS</th>");
		while (it.hasNext())
		{
			Integer id = (Integer) it.next();
			InitialFilterCriteria ifc = sharedIfc.get(id);
			out.println("<tr>");
			out.println("<td>" + id + "</td>");
			out.println("<td>" + ifc.getPriority() + "</td>");
			out.println("<td>" + ifc.getTriggerPoint() +  "</td>");
			out.println("<td>" + ifc.getAs().getURI() + "</td>");
			out.println("</tr>");
		}
		out.println("</table>");
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
			out.println("<td>" + ifc.getAs().getURI() + "</td>");
			out.println("</tr>");
		}
		out.println("</table>");
	}



	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
			IOException
	{
		String action = req.getParameter("action");
		if (action != null)
		{
			if (action.equals("clear user profiles cache"))
				_sessionManager.getRegistrar().getUserProfileCache().clearAllProfiles();
			else if (action.equals("refresh shared IFCs"))
			{
				try
				{
					_sessionManager.getRegistrar().getUserProfileCache().refreshSharedIFCs();
				} catch (Throwable e)
				{
					__log.warn("Failed to refesh shared IFCs", e);
				}
			}
			else if (action.equals("Network-initiated reauthentication"))
			{
				URI aor = _sipFactory.createURI(req.getParameter("aor"));
				_sessionManager.getRegistrar().requestReauthentication(aor, 
						req.getParameter("privateIdentity"));
			}
			resp.sendRedirect(req.getRequestURI());
		}
		PrintWriter out = resp.getWriter();
		out.println("<html><head><title>littleIMS :: S-CSCF: OAM</title></head><body>");
		printSessions(out);
		printUsers(out);
		
		out.println("<h2>Cache user profiles</h2>");
		printProfiles(_sessionManager.getRegistrar().getUserProfileCache().getUserProfiles().values().iterator(), out);
		out.println("<h2>Cache wilcard user profiles</h2>");
		printProfiles(_sessionManager.getRegistrar().getUserProfileCache().getWildcardUserProfiles().values().iterator(), out);
		
		out.println("<h2>Shared IFCs</h2>");
		printSharedIfc(out);
		
		actions(req, out);
		
		out.println("</body></html>");
	}
	
	private void actions(HttpServletRequest req, PrintWriter out)
	{
		out.print("<form method=\"get\" action=\"#\">");
		out.print("<input type=\"submit\" name=\"action\" value=\"clear user profiles cache\"/>");
		out.print("<input type=\"submit\" name=\"action\" value=\"refresh shared IFCs\"/>");
		out.print("</form>");
		
	}
	
}
