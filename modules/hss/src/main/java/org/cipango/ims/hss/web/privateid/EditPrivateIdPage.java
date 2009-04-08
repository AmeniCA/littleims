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
package org.cipango.ims.hss.web.privateid;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.collections.MicroMap;
import org.apache.wicket.util.string.interpolator.MapVariableInterpolator;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.validator.AbstractValidator;
import org.cipango.ims.hss.db.SubscriptionDao;
import org.cipango.ims.hss.model.PrivateIdentity;
import org.cipango.ims.hss.model.Subscription;

public class EditPrivateIdPage extends PrivateIdentityPage
{

	private String _key;
	private String _subscriptionId;
	private DaoDetachableModel _model;
	
	@SpringBean
	private SubscriptionDao _subscriptionDao;

	@SuppressWarnings("unchecked")
	public EditPrivateIdPage(PageParameters pageParameters)
	{
		_key = pageParameters.getString("id");
		_subscriptionId = pageParameters.getString("subscription");
		PrivateIdentity privateIdentity = null;
		if (_key != null)
		{
			privateIdentity = _dao.findById(_key);
			if (privateIdentity == null)
			{
				error(MapVariableInterpolator.interpolate(getString(getPrefix() + ".error.notFound"),
						new MicroMap("identity", _key)));
				_key = null;
			}
		}
		_model = new DaoDetachableModel(privateIdentity);

		add(new Label("title", getTitle()));
		Form form = new Form("form", new CompoundPropertyModel(_model));
		add(form);
		form.add(new RequiredTextField<String>("identity", String.class));
		form.add(new TextField("password", byte[].class));
		form.add(new TextField("operatorId", byte[].class).add(new AbstractValidator<byte[]>()
		{
			@Override
			protected void onValidate(IValidatable<byte[]> validatable)
			{
				if (validatable.getValue().length != 16)
					error(validatable, "validator.byteArray.length");
			}
		}));

		form.add(new CheckBox("anotherUser", new Model<Boolean>()).setVisible(isAdding()));

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
				goToBackPage(PrivateIdBrowserPage.class);
			}
		});
		form.add(new Button("cancel")
		{
			@Override
			public void onSubmit()
			{
				getSession().info(getString("modification.cancel"));
				goToBackPage(PrivateIdBrowserPage.class);
			}
		}.setDefaultFormProcessing(false));

		if (privateIdentity != null)
			setContextMenu(new ContextPanel(privateIdentity));
	}

	@SuppressWarnings("unchecked")
	protected void apply(Form form)
	{
		try
		{
			PrivateIdentity privateIdentity = (PrivateIdentity) form.getModelObject();
			if (_subscriptionId != null)
			{
				Subscription subscription = _subscriptionDao.findById(_subscriptionId);
				if (subscription != null)
				{
					privateIdentity.setSubscription(subscription);
				}
			}
			_dao.save(privateIdentity);
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
