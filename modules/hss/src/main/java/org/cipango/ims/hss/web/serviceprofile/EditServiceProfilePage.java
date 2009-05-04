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
package org.cipango.ims.hss.web.serviceprofile;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.collections.MicroMap;
import org.apache.wicket.util.string.interpolator.MapVariableInterpolator;
import org.cipango.ims.hss.model.InitialFilterCriteria;
import org.cipango.ims.hss.model.ServiceProfile;

public class EditServiceProfilePage extends ServiceProfilePage
{

	private String _key;
	private String _title;
	private boolean _copy;


	@SuppressWarnings("unchecked")
	public EditServiceProfilePage(PageParameters pageParameters)
	{
		_key = pageParameters.getString("id");
		_copy = pageParameters.getBoolean("copy");
		ServiceProfile serviceProfile = null;
		if (_key != null)
		{
			serviceProfile = _dao.findById(_key);
			if (serviceProfile == null)
			{
				error(MapVariableInterpolator.interpolate(getString(getPrefix() + ".error.notFound"),
						new MicroMap("identity", _key)));
				_key = null;
			}
		}
		DaoDetachableModel model = new DaoDetachableModel(serviceProfile, _copy);

		if (_copy)
			serviceProfile.setName(getCopyName(serviceProfile.getName()));	
		
		if (isAdding()) {
			_title = getString(getPrefix() + ".add.title");
		} else {
			_title = getString(getPrefix() + ".edit.title", model);
		}
		
		add(new Label("title", _title));
		Form form = new Form("form", new CompoundPropertyModel(model));
		add(form);
		form.add(new RequiredTextField<String>("name", String.class));
		
		form.add(new CheckBox("anotherUser", new Model()).setVisible(isAdding()));

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
				goToBackPage(ServiceProfileBrowserPage.class);
			}
		});
		form.add(new Button("cancel")
		{
			@Override
			public void onSubmit()
			{
				getSession().info(getString("modification.cancel"));
				goToBackPage(ServiceProfileBrowserPage.class);
			}
		}.setDefaultFormProcessing(false));

		if (serviceProfile != null)
			setContextMenu(new ContextPanel(serviceProfile));
	}

	@SuppressWarnings("unchecked")
	protected void apply(Form form)
	{
		try
		{
			ServiceProfile serviceProfile = (ServiceProfile) form.getModelObject();

			if (_copy)
			{
				ServiceProfile original = _dao.findById(_key);
				for (InitialFilterCriteria ifc : original.getIfcs())
					serviceProfile.addIfc(ifc);
			}
			
			_dao.save(serviceProfile);
		
			getSession().info(getString("modification.success"));
		}
		catch (Exception e)
		{
			getSession().error(getString(getPrefix() + ".error.duplicate", form.getModel()));
		}

	}

	private boolean isAdding()
	{
		return _copy || _key == null;
	}

	@Override
	public String getTitle()
	{
		return _title;
	}
}

