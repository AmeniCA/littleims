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

import java.util.Arrays;

import org.apache.wicket.Component;
import org.apache.wicket.PageParameters;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormChoiceComponentUpdatingBehavior;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.RadioChoice;
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
	private String _title;
	
	@SpringBean
	private SubscriptionDao _subscriptionDao;
	
	private boolean _pwdDisplayable;

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
		DaoDetachableModel model = new DaoDetachableModel(privateIdentity);
		
		if (isAdding()) {
			_title = getString(getPrefix() + ".add.title");
		} else {
			_title = getString(getPrefix() + ".edit.title", model);
		}

		Form form = new Form("form", new CompoundPropertyModel(model));
		add(form);

		form.add(new Label("title", privateIdentity == null ? "" : privateIdentity.getIdentity()));
		form.add(new RequiredTextField<String>("identity", String.class));
		
		_pwdDisplayable = isPwdDisplayableAsString(privateIdentity);
		RadioChoice passwordEdit = new RadioChoice("passwordEdit",
				new Model(_pwdDisplayable),
				Arrays.asList(new Boolean[] {true, false}),
				new ChoiceRenderer<Boolean>()
		{
			@Override
			public Object getDisplayValue(Boolean id)
			{
				if (id)
					return getString("privateId.passwordEdit.string");
				else
					return getString("privateId.passwordEdit.hexadecimal");
			}
		});
		form.add(passwordEdit);
		passwordEdit.add(new AjaxFormChoiceComponentUpdatingBehavior()
		{
			@Override
			protected void onUpdate(AjaxRequestTarget target)
			{
				Component passwordAsString = getPage().get("form:passwordAsString");
				Component password = getPage().get("form:password");
				if (!passwordAsString.isVisible() && !_pwdDisplayable)
				{
					getSession().warn(getString("privateId.error.passwordNotDisplayableAsString"));
					Component passwordEdit = getPage().get("form:passwordEdit");
					passwordEdit.setDefaultModelObject(false);
					target.addComponent(passwordEdit);
					target.addComponent(getPage().get("feedback"));
				}
				else
				{
					passwordAsString.setVisible(!passwordAsString.isVisible());
					password.setVisible(!password.isVisible());
					target.addComponent(passwordAsString);
					target.addComponent(password);
				}
			}
		});
		
		form.add(new TextField("passwordAsString", String.class).setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true).setVisible(_pwdDisplayable));
		form.add(new TextField("password", byte[].class).setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true).setVisible(!_pwdDisplayable));
		
		form.add(new TextField("operatorId", byte[].class).add(new AbstractValidator<byte[]>()
		{
			@Override
			protected void onValidate(IValidatable<byte[]> validatable)
			{
				if (validatable.getValue().length != 16)
					error(validatable, "validator.byteArray.length");
			}
		}));

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
				goToBackPage(PrivateIdBrowserPage.class);
			}
		}.setDefaultFormProcessing(false));

		if (privateIdentity != null)
			setContextMenu(new ContextPanel(privateIdentity));
	}
	
	private boolean isPwdDisplayableAsString(PrivateIdentity privateIdentity)
	{
		if (privateIdentity == null)
			return true;
		
		byte[] data = privateIdentity.getPassword();
		for (int i = 0; i < data.length; i++)
		{
			if (data[i] < 0x20 || data[i] > 0x7E)
				return false;
		}
		return true;
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
					privateIdentity.setSubscription(subscription);
				else
					error(MapVariableInterpolator.interpolate(getString("subscription.error.notFound"),
						new MicroMap("name", _subscriptionId)));
			}
			_dao.save(privateIdentity);
			_pwdDisplayable = isPwdDisplayableAsString(privateIdentity);
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
