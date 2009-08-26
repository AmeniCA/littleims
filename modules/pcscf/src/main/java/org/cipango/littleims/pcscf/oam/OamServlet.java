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
package org.cipango.littleims.pcscf.oam;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.cipango.littleims.pcscf.debug.DebugConf;
import org.cipango.littleims.pcscf.debug.DebugIdService;
import org.cipango.littleims.pcscf.debug.DebugSession;
import org.cipango.littleims.pcscf.debug.DebugSubscription;
import org.springframework.beans.BeansException;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

public class OamServlet extends HttpServlet
{

	private DebugIdService _debugIdService;
	private static final Logger __log = Logger.getLogger(OamServlet.class);
	
	public void init() throws ServletException
	{
		WebApplicationContext context = WebApplicationContextUtils
		.getWebApplicationContext(getServletContext());

		try 
		{
			_debugIdService = (DebugIdService) context.getBean("debugIdService");
		} 
		catch (BeansException e) 
		{
			throw new UnavailableException("no debug service " + e);
		}
	}
	
	private void printDebugSubscriptions(PrintWriter out)
	{

		out.println("<h2>Debug subscriptions</h2>");
		out.println("<table border=\"1\" cellspacing=\"0\">" +
		"<th>Subscription AOR</th><th>Version</th><th>Conf AOR</th><th>Nb sessions</th>");

		Iterator<DebugSubscription> it = _debugIdService.getDebugSubscriptions();
		synchronized (it)
		{
			while (it.hasNext())
			{
				DebugSubscription debugSubscription = it.next();				
				out.println("<tr>");
				
				List<DebugConf> confs = debugSubscription.getConfigs();
				String tdRowspan = "<td rowspan=\"" + confs.size() + "\">" ;
				out.println(tdRowspan + debugSubscription.getAor() + "</td>");
				out.println(tdRowspan + debugSubscription.getVersion() + "</td>");
				Iterator<DebugConf> it2 = confs.iterator();
				boolean first = true;
				while (it2.hasNext())
				{
					DebugConf conf = it2.next();
					if (!first)
						out.println("<tr>");
					out.println("<td>" + conf.getAor() + "</td>");
					out.println("<td>" + conf.getSessions().size()  +  "</td>");
					if (first)
						first = false;
					out.println("</tr>");
				}
				out.println("</tr>");
			}
		}
		out.println("</table>");
	}
	
	private void printDebugSessions(PrintWriter out)
	{

		out.println("<h2>Debug sessions</h2>");
		out.println("<table border=\"1\" cellspacing=\"0\">" +
		"<th>AOR</th><th>Session ID</th><th>Start trigger</th><th>Stop trigger</th><th>Debug ID</th>");

		Iterator<DebugConf> it = _debugIdService.getDebugConfs();
		synchronized (it)
		{
			while (it.hasNext())
			{
				DebugConf debugConf = it.next();				
				out.println("<tr>");
				
				List<DebugSession> sessions = debugConf.getSessions();
				String tdRowspan = "<td rowspan=\"" + sessions.size() + "\">" ;
				out.println(tdRowspan + debugConf.getAor() + "</td>");
				Iterator<DebugSession> it2 = sessions.iterator();
				boolean first = true;
				while (it2.hasNext())
				{
					DebugSession session = it2.next();
					if (!first)
						out.println("<tr>");
					out.println("<td>" + session.getId() + "</td>");
					out.println("<td>" + session.getStartTriggerAsString()  +  "</td>");
					out.println("<td>" + session.getStoptTriggerAsString() +  "</td>");
					out.println("<td>" + session.getDebugId()  +  "</td>");
					if (first)
					{
						first = false;
					}
					out.println("</tr>");
				}
				out.println("</tr>");
			}
		}
		out.println("</table>");
	}
	
	

	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
			IOException
	{
		
		PrintWriter out = resp.getWriter();
		out.println("<html><head><title>littleIMS :: P-CSCF: OAM</title></head><body>");
		out.println("<h1>littleIMS :: P-CSCF: OAM</h1>");
		printDebugSubscriptions(out);		
		printDebugSessions(out);
		out.println("</body></html>");
	}
	

	
}
