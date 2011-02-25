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
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.cipango.console.ConsoleFilter;
import org.cipango.console.Menu;
import org.cipango.console.MenuFactory;
import org.cipango.console.PageImpl;
import org.cipango.console.printer.MenuPrinter;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

public class ConsoleProxyFilter implements Filter
{
	private boolean _webAuthentication = true;
	private ConsoleFilter _filter = new ConsoleFilter();
	private MBeanServer _server;
	private ObjectName _objectName;
	
	public void init(FilterConfig config) throws ServletException
	{
		ApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(config.getServletContext());
		_webAuthentication = ((ImsApplication) context.getBean("webApplication")).isWebAuthentication();
		
		HssMenuFactory menuFactory = new HssMenuFactory();
		config.getServletContext().setAttribute(MenuFactory.class.getName(), menuFactory);
		_filter.init(config);
		menuFactory.setMBeanServer(_filter.getMbsc());
		
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

	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
			ServletException
	{
		HttpServletRequest req = (HttpServletRequest) request;
		if (!_webAuthentication || isAuthenticated(req.getSession()))
		{
			_filter.doFilter(request, response, chain);
		}
		else
		{
			req.getSession().setAttribute(SigninPage.REDIRECT_PAGE, req.getServletPath());
			((HttpServletResponse) response).sendRedirect("/signin");
		}
	}
	
	public static class HssMenuFactory implements MenuFactory
	{	
		private MBeanServerConnection _c;
		
		public Menu getMenu(String command, String contextPath)
		{
			return new HssMenu(_c, command, contextPath);
		}
		
		public void setMBeanServer(MBeanServerConnection c)
		{
			_c = c;
		}
	}
	
	public static class HssMenu extends MenuPrinter
	{
		private static final PageImpl PAGES = new PageImpl("");

		public static final PageImpl 
			IDENTITIES = PAGES.add(new PageImpl("subscriptions/browser", "Identities")),
			NETWORK = PAGES.add(new PageImpl("Network")),
			SCSCF = NETWORK.add(new PageImpl("s-cscf/browser", "S-CSCF")),
			CONFIG_HTTP = NETWORK.add(new PageImpl("configuration-http", "HSS :: HTTP Configuration", "HTTP Configuration")),
			CONFIG_DIAMETER = NETWORK.add(new PageImpl("configuration-diameter", "HSS :: Diameter Configuration", "Diameter Configuration")),
			CONFIG_SNMP = NETWORK.add(new PageImpl("configuration-snmp", "HSS :: SNMP Configuration", "SNMP Configuration")
			{
				@Override
				public boolean isEnabled(MBeanServerConnection c) throws IOException
				{
					return c.isRegistered(ConsoleFilter.SNMP_AGENT);
				}
			}),
			STATISTICS_DIAMETER = NETWORK.add(new PageImpl("statistics-diameter", "HSS :: Diameter Statistics", "Diameter Statistics")),
			SERVICES = PAGES.add(new PageImpl("service-profiles/browser", "Services")),
			ADMIN_USERS = PAGES.add(new PageImpl("admin/user/browser", "Admin users"));
		
		public HssMenu(MBeanServerConnection c, String command, String contextPath)
		{
			super(c, command, contextPath);
		}

		@Override
		protected List<PageImpl> getPages()
		{
			return PAGES.getPages();
		}

	}

}
