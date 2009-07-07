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
package org.cipango.ims.hss.web;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.util.collections.MicroMap;
import org.apache.wicket.util.string.interpolator.MapVariableInterpolator;
import org.cipango.ims.hss.CxManager;
import org.cipango.ims.hss.model.PublicIdentity;

public class PprPanel extends Panel
{

	private static final Logger __log = Logger.getLogger(PprPanel.class);
	private int _nbPublicsToUpdate;
	
	public PprPanel(String id, final CxManager cxManager)
	{
		super(id);
		setOutputMarkupId(true);
		setOutputMarkupPlaceholderTag(true);

		_nbPublicsToUpdate = cxManager.getNbPublicIdsToUpdate();
		
		if (_nbPublicsToUpdate != 0)
			createForm(cxManager);
		else
			setVisible(false);	
	}
	
	@SuppressWarnings("unchecked")
	private void createForm(final CxManager cxManager)
	{
		Form form = new Form("form");
		add(form);
		form.add(new Button("submit")
		{
			@Override
			public void onSubmit()
			{
				StringBuilder sb = new StringBuilder();
				StringBuilder errorSb = new StringBuilder();
				for (PublicIdentity publicIdentity : cxManager.getPublicIdsToUpdate())
				{
					try
					{
						cxManager.sendPpr(publicIdentity);
						if (sb.length() != 0)
							sb.append(", ");
						sb.append(publicIdentity.getIdentity());
					}
					catch (IOException e)
					{
						if (sb.length() != 0)
							errorSb.append(", ");
						__log.warn("Failed to send PPR for identity " + publicIdentity, e);
						errorSb.append(publicIdentity.getIdentity());
					}	
				}
				if (sb.length() != 0)
					getSession().info(MapVariableInterpolator.interpolate(getString("pprPanel.info.pprSended"),
							new MicroMap("publicIds", sb.toString())));
				if (errorSb.length() != 0)
					getSession().warn(MapVariableInterpolator.interpolate(getString("pprPanel.error.pprSendedFailed"),
							new MicroMap("publicIds", errorSb.toString())));
			}
		});	
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void onBeforeRender()
	{
		super.onBeforeRender();
		BasePage page = (BasePage) getPage();
		int nbPublicsToUpdate = page.getCxManager().getNbPublicIdsToUpdate();
		if (nbPublicsToUpdate != _nbPublicsToUpdate)
		{
			setVisible(nbPublicsToUpdate != 0);
			if (get("form") == null)
				createForm(page.getCxManager());
			_nbPublicsToUpdate = nbPublicsToUpdate;
		}
		
		if (nbPublicsToUpdate > 0)
		{
			String title = MapVariableInterpolator.interpolate(getString("pprPanel.title"),
					new MicroMap("nb", _nbPublicsToUpdate));
			((WebMarkupContainer) get("form")).addOrReplace(new Label("title", title));
		}
	}
	
	

}
