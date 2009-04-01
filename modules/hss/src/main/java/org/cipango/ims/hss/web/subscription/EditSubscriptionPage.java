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
package org.cipango.ims.hss.web.subscription;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.util.collections.MicroMap;
import org.apache.wicket.util.string.interpolator.MapVariableInterpolator;
import org.cipango.ims.hss.model.Subscription;

public class EditSubscriptionPage extends SubscriptionPage
{

	private Long _key;
	private DaoDetachableModel _model;
	
	@SuppressWarnings("unchecked")
	public EditSubscriptionPage(PageParameters pageParameters)
	{
		_key = pageParameters.getLong("id");
		Subscription subscription = null;
		if (_key != null)
		{
			subscription = _dao.findById(_key);
			if (subscription == null)
			{
				error(MapVariableInterpolator.interpolate(getString(getPrefix() + ".error.notFound"),
						new MicroMap("id", _key)));
				_key = null;
			}
		}
		_model = new DaoDetachableModel(subscription);

		add(new Label("title", getTitle()));
		Form form = new Form("form", new CompoundPropertyModel(_model));
		add(form);
		form.add(new Label("id"));
		form.add(new Label("scscf"));
		
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
				goToBackPage(SubscriptionBrowserPage.class);
			}
		});
		form.add(new Button("cancel")
		{
			@Override
			public void onSubmit()
			{
				getSession().info(getString("modification.cancel"));
				goToBackPage(SubscriptionBrowserPage.class);
			}
		}.setDefaultFormProcessing(false));

		if (subscription != null)
			setContextMenu(new ContextPanel(subscription));
	}

	@SuppressWarnings("unchecked")
	protected void apply(Form form)
	{
		try
		{	
			_dao.save((Subscription) form.getModelObject());

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
		if (isAdding()) {
			return getString(getPrefix() + ".add.title");
		} else {
			return getString(getPrefix() + ".edit.title", _model);
		}
	}
}

