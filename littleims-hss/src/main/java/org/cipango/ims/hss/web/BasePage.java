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
import org.apache.wicket.Page;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.cipango.ims.hss.CxManager;

public abstract class BasePage extends WebPage
{

	@SpringBean
	private CxManager _cxManager;

	public BasePage()
	{
		this(null);
	}
	
	public BasePage(PageParameters pageParameters)
	{
		super(pageParameters);
		add(new HeaderPanel().setRenderBodyOnly(true));
		add(new FeedbackPanel("feedback").setOutputMarkupId(true));
		add(new WebMarkupContainer("contextMenu"));
	}

	public void setContextMenu(Component panel)
	{
		panel.setOutputMarkupId(true);
		addOrReplace(panel);
	}

	public ImsApplication getImsApp()
	{
		return (ImsApplication) getApplication();
	}

	public ImsSession getImsSession()
	{
		return (ImsSession) getSession();
	}

	protected CxManager getCxManager()
	{
		return _cxManager;
	}

	public abstract String getTitle();

	@Override
	protected void onBeforeRender()
	{
		addOrReplace(new PprPanel("pprPanel", _cxManager));
		super.onBeforeRender();
		// The title can need subclass construction done
		if (get("page.title") == null)
		{
			add(new Label("page.title", getString("prefix.hss") + getTitle()));
		}
	}

	protected void goToBackPage(Class<? extends Page> defaultPage)
	{
		Page backPage = getImsSession().getBackPage(getClass());

		if (backPage == null)
		{
			setResponsePage(defaultPage);
		}
		else
		{
			setResponsePage(backPage);
		}
	}

	protected String getCopyName(String name)
	{
		String copy = getString("copyOf");
		if (name.startsWith(copy))
		{
			int index = name.lastIndexOf(' ');
			if (index == -1)
				return name + " 2";
			String end = name.substring(index);
			try
			{
				return name + ' ' + (Integer.parseInt(end) + 1);
			}
			catch (Exception e)
			{
				return name + " 2";
			}
		}
		else
			return copy + name;
	}
	
	public int getItemByPage()
	{
		return 20;
	}
}
