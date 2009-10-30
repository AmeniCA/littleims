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
package org.cipango.littleims.pcscf.oam.browser;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.cipango.littleims.pcscf.oam.HideableLink;
import org.cipango.littleims.pcscf.subscription.debug.DebugSession;

public class DebugSessionPanel extends Panel
{
	private String _debugId;
	private File _file;
	private boolean _viewLog = false;
	
	@SuppressWarnings("unchecked")
	public DebugSessionPanel(String id, final DebugSession debugSession)
	{
		super(id);
		add(new Label("debugId", debugSession.getDebugId()));
		add(new Label("startTrigger", debugSession.getStartTriggerAsString()));
		add(new Label("stopTrigger", debugSession.getStoptTriggerAsString()));
		add(new HideableLink("hideLink", getMarkupId()));
		_debugId = debugSession.getDebugId();
		refreshLog();
		
		AjaxFallbackLink viewLink = new AjaxFallbackLink("viewLog")
		{

			@Override
			public void onClick(AjaxRequestTarget target)
			{
				_viewLog = true;
				refreshLog();
				Component hideLink = getParent().get("hideLog");
				hideLink.setVisible(true);
				Component refresh = new Label("view", "Refresh log").setOutputMarkupId(true);
				replace(refresh);
				if (target != null)
				{
					target.addComponent(getParent().get("content"));
					target.addComponent(hideLink);
					target.addComponent(refresh);
				}
			}
		};
		add(viewLink);
		viewLink.setOutputMarkupId(true);
		viewLink.add(new Label("view", "View log").setOutputMarkupId(true));
		
		AjaxFallbackLink hideLink = new AjaxFallbackLink("hideLog")
		{

			@Override
			public void onClick(AjaxRequestTarget target)
			{
				_viewLog = false;
				setVisible(false);
				Component refresh = new Label("view", "View log").setOutputMarkupId(true);
				((WebMarkupContainer) getParent().get("viewLog")).replace(refresh);
				refreshLog();
				if (target != null)
				{
					target.addComponent(getParent().get("content"));
					target.addComponent(this);
					target.addComponent(refresh);
				}
			}
		};
		add(hideLink);
		hideLink.setVisible(false);
		hideLink.setOutputMarkupId(true);
		hideLink.setOutputMarkupPlaceholderTag(true);
	}
	
	private void refreshLog()
	{
		String content;
		if (_file == null)
			_file = new File(System.getProperty("jetty.home") + "/logs/" + _debugId + ".log");
		if (!_file.exists())
			content = "No logs found";
		else if (_viewLog)
		{
			try
			{
				InputStream is = new FileInputStream(_file);
				ByteArrayOutputStream os = new ByteArrayOutputStream();
				int read;
				byte[] b = new byte[512];
				while ((read = is.read(b)) != -1)
					os.write(b, 0, read);
				content = new String(os.toByteArray());
			}
			catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
				content = e.toString();
			}
			
		}
		else
		{
			content = "Logs found";
		}
		addOrReplace(new Label("content", content).setOutputMarkupId(true));
	}
}
