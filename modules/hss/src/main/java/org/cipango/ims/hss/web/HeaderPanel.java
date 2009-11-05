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
package org.cipango.ims.hss.web;

import org.apache.wicket.Component;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.util.collections.MicroMap;
import org.apache.wicket.util.string.interpolator.MapVariableInterpolator;
import org.cipango.ims.hss.web.ImsSession.DaoDetachableModel;
import org.cipango.ims.hss.web.adminuser.EditAdminUserPage;
import org.cipango.ims.oam.util.SignOutPage;

public class HeaderPanel extends Panel
{

	@SuppressWarnings("unchecked")
	public HeaderPanel()
	{
		super("header");
		ImsSession session = ((ImsSession) getSession());
		if (session.isAuthenticated())
		{
			DaoDetachableModel adminModel = ((ImsSession) getSession()).getAdminUserModel();
			add(new Label("user.current", ""));
			add(new BookmarkablePageLink("profileLink",
					EditAdminUserPage.class, new PageParameters("id="
							+ adminModel.getLogin())));
			add(new BookmarkablePageLink("signout",
					SignOutPage.class/*, new PageParameters(SignOutPage.REDIRECTPAGE_PARAM + "="
							+ SigninPage.class.getName())*/));
		}
		else
		{
			add(new Label("user.current", "User: none").setVisible(false));
			add(new WebMarkupContainer("profileLink").setVisible(false));
			add(new WebMarkupContainer("signout").setVisible(false));
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected void onBeforeRender()
	{
		Component component = get("user.current");
		if (component.isVisible())
		{
			component.setDefaultModelObject(MapVariableInterpolator.interpolate(getString("headerPanel.user.current"),
				new MicroMap("login", ((ImsSession) getSession()).getLogin())));
		}
		super.onBeforeRender();
	}

}
