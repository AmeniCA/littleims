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
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxFallbackButton;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.cipango.ims.hss.model.Subscription;

public class EditSubscriptionPage extends SubscriptionPage
{

	private String _key;
	private String _title;
	
	@SuppressWarnings("unchecked")
	public EditSubscriptionPage(PageParameters pageParameters)
	{
		Subscription subscription = getSubscription(pageParameters);
		_key = subscription == null ? null : subscription.getName();
		
		IModel model = new DaoDetachableModel(subscription);
		
		Form form = new Form("form", new CompoundPropertyModel(model));
		add(form);
		
		if (isAdding()) 
		{
			_title = getString(getPrefix() + ".add.title");
			form.add(new Label("title", "")).setVisible(false);
		} 
		else 
		{
			_title = getString(getPrefix() + ".edit.title", model);
			form.add(new Label("title", subscription.getName()));
		}
		
		form.add(new RequiredTextField<String>("name"));
		form.add(new Label("scscf"));
		
		form.add(new AjaxFallbackButton("submit", form)
		{
			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> f)
			{
				try
				{	
					Subscription s = (Subscription) f.getModelObject();
					_dao.save(s);

					getSession().info(getString("modification.success"));

					setResponsePage(ViewSubscriptionPage.class, new PageParameters("id=" + s.getName()));
				}
				catch (Exception e)
				{
					getSession().error(getString(getPrefix() + ".error.duplicate", f.getModel()));
				}
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

