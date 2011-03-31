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
package org.cipango.littleims.pcscf.oam;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.FeedbackPanel;

public abstract class BasePage extends WebPage
{
	public BasePage()
	{
		add(new HeaderPanel().setRenderBodyOnly(true));
		add(new FeedbackPanel("feedback").setOutputMarkupId(true));
		add(new WebMarkupContainer("contextMenu"));
	}

	public void setContextMenu(Component panel)
	{
		panel.setOutputMarkupId(true);
		addOrReplace(panel);
	}


	public abstract String getTitle();

	@Override
	protected void onBeforeRender()
	{
		super.onBeforeRender();
		// The title can need subclass construction done
		if (get("page.title") == null)
		{
			add(new Label("page.title", getString("prefix.pcscf") + getTitle()));
		}
	}

	public int getItemByPage()
	{
		return 20;
	}
}
