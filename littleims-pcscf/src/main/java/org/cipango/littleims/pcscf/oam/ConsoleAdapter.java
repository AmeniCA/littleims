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
package org.cipango.littleims.pcscf.oam;

import java.util.Iterator;
import java.util.List;

import javax.management.MBeanServer;
import javax.management.MBeanServerConnection;
import javax.management.MBeanServerFactory;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.cipango.console.ConsoleFilter;
import org.cipango.console.Menu;
import org.cipango.console.MenuFactory;
import org.cipango.console.PageImpl;
import org.cipango.console.printer.MenuPrinter;

public class ConsoleAdapter implements ServletContextListener
{		
	public void contextInitialized(ServletContextEvent sce)
	{
		HssMenuFactory menuFactory = new HssMenuFactory();
		sce.getServletContext().setAttribute(MenuFactory.class.getName(), menuFactory);
		menuFactory.setMBeanServer(getMBeanServer());
	}
	
	@SuppressWarnings("unchecked")
	public MBeanServer getMBeanServer()
	{
		List<MBeanServer> l = MBeanServerFactory.findMBeanServer(null);
		Iterator<MBeanServer> it = l.iterator();
		while (it.hasNext())
		{
			MBeanServer server = it.next();
			for (int j = 0; j < server.getDomains().length; j++)
			{
				if (server.isRegistered(ConsoleFilter.SERVER))
				{
					return server;
				}
			}
		}
		return null;
	}

	public void contextDestroyed(ServletContextEvent sce)
	{	
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
			REGISTRATIONS = PAGES.add(new PageImpl("registrations", "Registrations")),
			
			CONFIGURATION = PAGES.add(new PageImpl("Configuration")),
			CONFIG_SIP = CONFIGURATION.add(new PageImpl("configuration-sip", "P-CSCF :: SIP Configuration", "SIP")),
			CONFIG_HTTP = CONFIGURATION.add(new PageImpl("configuration-http", "P-CSCF :: HTTP Configuration", "HTTP")),
			
			STATISTICS = PAGES.add(new PageImpl("Statistics")),
			STATISTICS_SIP = STATISTICS.add(new PageImpl("statistics-sip", "P-CSCF :: SIP Statistics", "SIP")),
			STATISTICS_HTTP = STATISTICS.add(new PageImpl("statistics-http", "P-CSCF :: HTTP Statistics", "HTTP")),

			LOGS = PAGES.add(new PageImpl("Logs")),
			SIP_LOGS = LOGS.add(new PageImpl("logs-sip", "P-CSCF :: SIP Logs", "SIP")),
			DEBUG_ID = LOGS.add(new PageImpl("debug-id", "Debug ID"));
		
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
