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
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.cipango.console.ConsoleFilter;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

public class ConsoleProxyFilter implements Filter, ConsoleProxyFilterMBean
{
	private boolean _webAuthentication = true;
	private ConsoleFilter _filter = new ConsoleFilter();
	private MBeanServer _server;
	private ObjectName _objectName;
	
	public void init(FilterConfig config) throws ServletException
	{
		ApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(config.getServletContext());
		_webAuthentication = ((ImsApplication) context.getBean("webApplication")).isWebAuthentication();
		_filter.init(config);
		
		try
		{
			List<MBeanServer> l = MBeanServerFactory.findMBeanServer(null);
			if (l.size() >=1)
			{
				_server = l.get(0);
				_objectName = new ObjectName("org.cipango.console", "page", getPage());
				_server.registerMBean(this, _objectName);
			}
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}
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

	public String getPage()
	{
		return "..";
	}
	
	public String getTitle()
	{
		return "HSS";
	}

}
