// ========================================================================
// Copyright 2010 NEXCOM Systems
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
package org.cipango.ims.hss.web;

import java.io.IOException;
import java.util.List;

import javax.management.MBeanServer;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.cipango.console.SnmpManager;
import org.cipango.console.VelocityConsoleServlet;
import org.cipango.console.menu.Menu;
import org.cipango.console.menu.MenuFactory;
import org.cipango.console.menu.MenuImpl;
import org.cipango.console.menu.PageImpl;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

public class ConsoleProxyServlet extends HttpServlet
{
	private boolean _webAuthentication = true;
	private VelocityConsoleServlet _servlet = new VelocityConsoleServlet();
	private MBeanServer _server;
	private ObjectName _objectName;
	
	@Override
	public void init(ServletConfig config) throws ServletException
	{
		ApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(config.getServletContext());
		_webAuthentication = ((ImsApplication) context.getBean("webApplication")).isWebAuthentication();
		
		HssMenuFactory menuFactory = new HssMenuFactory();
		config.getServletContext().setAttribute(MenuFactory.class.getName(), menuFactory);
		_servlet.init(config);
	}
	
	private boolean isAuthenticated(HttpSession session)
	{
		if (!_webAuthentication)
			return true;
		if (session == null)
			return false;
		Object o = session.getAttribute("wicket:wicket:session");
		if (o == null || !(o instanceof ImsSession))
			return false;
		ImsSession imsSession = (ImsSession) o;
		return imsSession.isAuthenticated();
	}

	public void destroy()
	{
		if (_server != null && _objectName != null)
			try
			{
				_server.unregisterMBean(_objectName);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
	}

	public void service(ServletRequest request, ServletResponse response) throws IOException,
			ServletException
	{
		HttpServletRequest req = (HttpServletRequest) request;
		if (!_webAuthentication || isAuthenticated(req.getSession()))
		{
			_servlet.service(request, response);
		}
		else
		{
			req.getSession().setAttribute(SigninPage.REDIRECT_PAGE, req.getServletPath());
			((HttpServletResponse) response).sendRedirect("/signin");
		}
	}
	
	public static class HssMenuFactory implements MenuFactory
	{	

		public Menu getMenu(String command,  MBeanServerConnection mBeanServerConnection)
		{
			return new HssMenu(mBeanServerConnection, command);
		}
		
	}
	
	public static class HssMenu extends MenuImpl
	{
		private static final PageImpl PAGES = new PageImpl("");

		public static final PageImpl 
			IDENTITIES = PAGES.add(new PageImpl("subscriptions/browser", "Identities")),
			
			NETWORK = PAGES.add(new PageImpl("Network")),
			SCSCF = NETWORK.add(new PageImpl("s-cscf/browser", "S-CSCF")),
			CONFIG_HTTP = NETWORK.add(new PageImpl(MenuImpl.CONFIG_HTTP.getName(), "HSS :: HTTP Configuration", "HTTP Configuration")),
			CONFIG_DIAMETER = NETWORK.add(new PageImpl(MenuImpl.CONFIG_DIAMETER.getName(), "HSS :: Diameter Configuration", "Diameter Configuration")),
			CONFIG_SNMP = NETWORK.add(new PageImpl(MenuImpl.CONFIG_SNMP.getName(), "HSS :: SNMP Configuration", "SNMP Configuration")
			{
				@Override
				public boolean isEnabled(MBeanServerConnection c) throws IOException
				{
					return c.isRegistered(SnmpManager.AGENT);
				}
			}),
			STATISTICS_DIAMETER = NETWORK.add(new PageImpl(MenuImpl.STATISTICS_DIAMETER.getName(), "HSS :: Diameter Statistics", "Diameter Statistics")),
			LOGS_DIAMETER = NETWORK.add(new PageImpl(MenuImpl.DIAMETER_LOGS.getName(), "HSS :: Diameter Logs", "Diameter Logs")),
			
			SERVICES = PAGES.add(new PageImpl("service-profiles/browser", "Services")),
			ADMIN_USERS = PAGES.add(new PageImpl("admin/user/browser", "Admin users"));
		
		public HssMenu(MBeanServerConnection c, String command)
		{
			super(c, command);
		}

		@Override
		public List<PageImpl> getPages()
		{
			return PAGES.getPages();
		}

	}

}
