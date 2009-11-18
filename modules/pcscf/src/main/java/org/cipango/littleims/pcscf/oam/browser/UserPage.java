// ========================================================================
// Copyright 2008-2009 NEXCOM Systems
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
package org.cipango.littleims.pcscf.oam.browser;

import java.util.List;

import javax.servlet.sip.Address;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.cipango.littleims.pcscf.RegContext;
import org.cipango.littleims.pcscf.oam.AorLink;
import org.cipango.littleims.pcscf.oam.BasePage;
import org.cipango.littleims.pcscf.subscription.debug.DebugConf;
import org.cipango.littleims.pcscf.subscription.debug.DebugIdService;
import org.cipango.littleims.pcscf.subscription.debug.DebugSession;
import org.cipango.littleims.pcscf.subscription.reg.RegEventService;

public class UserPage extends BasePage
{
	@SpringBean
	private DebugIdService _debugIdService;
	@SpringBean
	private RegEventService _regEventService;
	
	private String _publicIdentity;
	
	@SuppressWarnings("unchecked")
	public UserPage(PageParameters pageParameters)
	{		
		_publicIdentity = pageParameters.getString("id");
		
		RegContext context = _regEventService.getRegisteredUsers().get(_publicIdentity);
		if (context == null)
		{
			add(new WebMarkupContainer("associated").setVisible(false));
			info("User is not registered");
		}
		else
		{
			add(new ListView("associated", context.getAssociatedUris())
			{
	
				@Override
				protected void populateItem(ListItem item)
				{
					item.add(new AorLink("aorLink", (Address) item.getModelObject()));
				}
				
			});
		}
		
		DebugConf debugConf = _debugIdService.getDebugConfs().get(_publicIdentity);
		if (debugConf == null)
		{
			WebMarkupContainer markup = new WebMarkupContainer("debugSessions");
			markup.add(new Label("session", "No debug configuration found"));
			add(markup);
		}	
		else
		{
			add(new ListView("debugSessions", debugConf.getSessions())
			{
	
				@Override
				protected void populateItem(ListItem item2)
				{
					item2.add(new DebugSessionPanel("session", (DebugSession) item2.getModelObject()));
				}
				
			});
		}
				
		add(new Label("title", getTitle()));
	}
	

	@Override
	public String getTitle()
	{
		return "public identity: " + _publicIdentity;
	}
	
}
