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
import org.apache.wicket.model.Model;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.validator.AbstractValidator;
import org.cipango.ims.hss.model.PrivateIdentity;
import org.cipango.ims.hss.model.PublicIdentity;
import org.cipango.ims.hss.model.Subscription;
import org.cipango.ims.hss.model.PublicIdentity.IdentityType;

public class AddSubscriptionPage extends SubscriptionPage
{

	@SuppressWarnings("unchecked")
	public AddSubscriptionPage()
	{
		add(new Label("title", getTitle()));
		Form form = new Form("form");
		add(form);
		form.add(new RequiredTextField<String>("privateId.identity", new Model()));
		form.add(new TextField("password", new Model(), byte[].class));
		form.add(new TextField("operatorId", new Model(PrivateIdentity.DEFAULT_OPERATOR_ID.clone()), byte[].class).add(new AbstractValidator<byte[]>()
		{
			@Override
			protected void onValidate(IValidatable<byte[]> validatable)
			{
				if (validatable.getValue().length != 16)
					error(validatable, "validator.byteArray.length");
			}
		}));
		form.add(new RequiredTextField<String>("publicId.identity", new Model()));
		form.add(new CheckBox("barred", new Model()));

		form.add(new DropDownChoice("identityType",
				new Model(),
				Arrays.asList(new Short[]{0,1,2,3}),
				new ChoiceRenderer<Short>()
		{
			@Override
			public Object getDisplayValue(Short id)
			{
				return IdentityType.toString(id);
			}
			
		}));
		form.add(new TextField("displayName", new Model(), String.class));
		form.add(new Button("ok")
		{
			@Override
			public void onSubmit()
			{
				Form form = getForm();
				try
				{
					Subscription subscription = new Subscription();
					PrivateIdentity privateIdentity = new PrivateIdentity();
					privateIdentity.setIdentity((String) form.get("privateId.identity").getDefaultModelObject()); 
					privateIdentity.setPassword((byte[]) form.get("password").getDefaultModelObject()); 
					privateIdentity.setPassword((byte[]) form.get("operatorId").getDefaultModelObject()); 
					privateIdentity.setSubscription(subscription);
					
					PublicIdentity publicIdentity = new PublicIdentity();
					publicIdentity.setIdentity((String) form.get("publicId.identity").getDefaultModelObject()); 
					publicIdentity.setBarred((Boolean) form.get("barred").getDefaultModelObject());
					publicIdentity.setIdentityType((Short) form.get("identityType").getDefaultModelObject()); 
					privateIdentity.addPublicId(publicIdentity);
					
					_dao.saveWithCascade(subscription);

					getSession().info(getString("modification.success"));

					setResponsePage(ViewSubscriptionPage.class, new PageParameters("id=" + subscription.getId()));
				}
				catch (Exception e)
				{
					getSession().error(getString(getPrefix() + ".error.duplicate"));
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
		
		form.add(new CheckBox("anotherUser", new Model<Boolean>()));
	}
	
	
	@Override
	public String getTitle()
	{
		return getString("subscription.add.title");
	}

}
