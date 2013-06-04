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
package org.cipango.littleims.icscf.oam;

import java.util.List;

import javax.management.MBeanServerConnection;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.cipango.console.menu.Menu;
import org.cipango.console.menu.MenuFactory;
import org.cipango.console.menu.MenuImpl;
import org.cipango.console.menu.PageImpl;

public class ConsoleAdapter implements ServletContextListener
{		
	public void contextInitialized(ServletContextEvent sce)
	{
		HssMenuFactory menuFactory = new HssMenuFactory();
		sce.getServletContext().setAttribute(MenuFactory.class.getName(), menuFactory);
	}
	
	public void contextDestroyed(ServletContextEvent sce)
	{	
	}
	
	public static class HssMenuFactory implements MenuFactory
	{			
		public Menu getMenu(String command, MBeanServerConnection c)
		{
			return new HssMenu(c, command);
		}		
	}
	
	public static class HssMenu extends MenuImpl
	{
		private static final PageImpl PAGES = new PageImpl("");

		public static final PageImpl 
			ABOUT = PAGES.add(new PageImpl(MenuImpl.ABOUT.getName(), "I-CSCF :: About", "About")),
			
			CONFIGURATION = PAGES.add(new PageImpl("Configuration")),
			CONFIG_SIP = CONFIGURATION.add(new PageImpl(MenuImpl.CONFIG_SIP.getName(), "I-CSCF :: SIP Configuration", "SIP")),
			CONFIG_HTTP = CONFIGURATION.add(new PageImpl(MenuImpl.CONFIG_HTTP.getName(), "I-CSCF :: HTTP Configuration", "HTTP")),
			CONFIG_DIAMETER = CONFIGURATION.add(new PageImpl(MenuImpl.CONFIG_DIAMETER.getName(), "I-CSCF :: Diameter Configuration", "Diameter")),
			
			STATISTICS = PAGES.add(new PageImpl("Statistics")),
			STATISTICS_SIP = STATISTICS.add(new PageImpl(MenuImpl.STATISTICS_SIP.getName(), "I-CSCF :: SIP Statistics", "SIP")),
			STATISTICS_HTTP = STATISTICS.add(new PageImpl(MenuImpl.STATISTICS_HTTP.getName(), "I-CSCF :: HTTP Statistics", "HTTP")),
			STATISTICS_DIAMETER = STATISTICS.add(new PageImpl(MenuImpl.STATISTICS_DIAMETER.getName(), "I-CSCF :: Diameter Statistics", "Diameter")),

			LOGS = PAGES.add(new PageImpl("Logs")),
			SIP_LOGS = LOGS.add(new PageImpl(MenuImpl.SIP_LOGS.getName(), "I-CSCF :: SIP Logs", "SIP")),
			DIAMETER_LOGS = LOGS.add(new PageImpl(MenuImpl.DIAMETER_LOGS.getName(), "I-CSCF :: Diameter Logs", "Diameter"));
		
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
