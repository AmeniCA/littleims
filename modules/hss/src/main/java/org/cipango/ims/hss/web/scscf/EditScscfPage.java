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
package org.cipango.ims.hss.web.scscf;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.collections.MicroMap;
import org.apache.wicket.util.string.interpolator.MapVariableInterpolator;
import org.cipango.ims.hss.model.Scscf;
import org.cipango.ims.hss.web.util.UriValidator;

public class EditScscfPage extends ScscfPage
{

	private String _key;
	private String _title;
	
	@SuppressWarnings("unchecked")
	public EditScscfPage(PageParameters pageParameters)
	{
		_key = pageParameters.getString("id");
		Scscf scscf = null;
		if (_key != null)
		{
			scscf = _dao.findById(_key);
			if (scscf == null)
			{
				error(MapVariableInterpolator.interpolate(getString(getPrefix() + ".error.notFound"),
						new MicroMap("id", _key)));
				_key = null;
			}
		}
		
		IModel model = new DaoDetachableModel(scscf);
		
		if (isAdding()) {
			_title = getString(getPrefix() + ".add.title");
		} else {
			_title = getString(getPrefix() + ".edit.title", model);
		}
		
		
		Form form = new Form("form", new CompoundPropertyModel(model));
		add(form);
		form.add(new Label("title", scscf == null ? "" : scscf.getName()));
		form.add(new RequiredTextField<String>("diameterHost", String.class));
		form.add(new RequiredTextField<String>("name", String.class));
		form.add(new RequiredTextField<String>("uri", String.class).add(new UriValidator(true)));
		if (isAdding())
			form.add(new Label("nbSubscriptions", new Model()).setVisible(false));
		else
			form.add(new Label("nbSubscriptions", new Model(_dao.getNbSubscriptions(scscf))));
		
		form.add(new Button("submit")
		{
			@Override
			public void onSubmit()
			{
				apply(getForm());
			}
		});
		form.add(new Button("cancel")
		{
			@Override
			public void onSubmit()
			{
				getSession().info(getString("modification.cancel"));
				goToBackPage(ScscfBrowserPage.class);
			}
		}.setDefaultFormProcessing(false));

		if (scscf != null)
			setContextMenu(new ContextPanel(scscf));
	}

	@SuppressWarnings("unchecked")
	protected void apply(Form form)
	{
		try
		{	
			_dao.save((Scscf) form.getModelObject());

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

