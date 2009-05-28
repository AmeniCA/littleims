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
package org.cipango.ims.hss.web.as;

import java.util.Arrays;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.collections.MicroMap;
import org.apache.wicket.util.string.interpolator.MapVariableInterpolator;
import org.cipango.ims.hss.model.ApplicationServer;
import org.cipango.ims.hss.model.ApplicationServer.DefaultHandling;
import org.cipango.ims.hss.web.util.UriValidator;

public class EditAsPage extends AsPage
{

	private String _key;
	private String _title;
	
	@SuppressWarnings("unchecked")
	public EditAsPage(PageParameters pageParameters)
	{
		_key = pageParameters.getString("id");
		ApplicationServer applicationServer = null;
		if (_key != null)
		{
			applicationServer = _dao.findById(_key);
			if (applicationServer == null)
			{
				error(MapVariableInterpolator.interpolate(getString(getPrefix() + ".error.notFound"),
						new MicroMap("id", _key)));
				_key = null;
			}
		}
		
		IModel model = new DaoDetachableModel(applicationServer);
		
		if (isAdding()) {
			_title = getString(getPrefix() + ".add.title");
		} else {
			_title = getString(getPrefix() + ".edit.title", model);
		}
		
		add(new Label("title", getTitle()));
		Form form = new Form("form", new CompoundPropertyModel(model));
		add(form);
		form.add(new RequiredTextField<String>("name", String.class));
		form.add(new RequiredTextField<String>("serverName", String.class).add(new UriValidator(true)));
		form.add(new DropDownChoice("defaultHandling",
				Arrays.asList(new Short[]{0,1}),
				new ChoiceRenderer<Short>()
		{
			@Override
			public Object getDisplayValue(Short id)
			{
				return DefaultHandling.toString(id);
			}
			
		}));

		form.add(new CheckBox("includeRegisterRequest"));
		form.add(new CheckBox("includeRegisterResponse"));
		form.add(new TextField("serviceInformation", String.class));
		
		form.add(new Button("submit")
		{
			@Override
			public void onSubmit()
			{
				apply(getForm());
			}
		});
		form.add(new Button("ok")
		{
			@Override
			public void onSubmit()
			{
				apply(getForm());
				goToBackPage(AsBrowserPage.class);
			}
		});
		form.add(new Button("cancel")
		{
			@Override
			public void onSubmit()
			{
				getSession().info(getString("modification.cancel"));
				goToBackPage(AsBrowserPage.class);
			}
		}.setDefaultFormProcessing(false));

		if (applicationServer != null)
			setContextMenu(new ContextPanel(applicationServer));
	}

	@SuppressWarnings("unchecked")
	protected void apply(Form form)
	{
		try
		{	
			_dao.save((ApplicationServer) form.getModelObject());

			getSession().info(getString("modification.success"));
		}
		catch (Exception e)
		{
			getSession().error(getString(getPrefix() + ".error.duplicate", form.getModel()));
		}

	}

	private boolean isAdding()
	{
		return _key == null;
	}

	@Override
	public String getTitle()
	{
		return _title;
	}
}

