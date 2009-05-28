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

import org.apache.log4j.Logger;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.validator.AbstractValidator;
import org.cipango.ims.hss.db.ImplicitRegistrationSetDao;
import org.cipango.ims.hss.db.ServiceProfileDao;
import org.cipango.ims.hss.model.ImplicitRegistrationSet;
import org.cipango.ims.hss.model.PrivateIdentity;
import org.cipango.ims.hss.model.PublicUserIdentity;
import org.cipango.ims.hss.model.ServiceProfile;
import org.cipango.ims.hss.model.Subscription;
import org.cipango.ims.hss.model.PublicIdentity.IdentityType;
import org.cipango.ims.hss.web.util.UriValidator;

public class AddSubscriptionPage extends SubscriptionPage
{
	@SpringBean
	private ServiceProfileDao _serviceProfileDao;
	
	@SpringBean
	private ImplicitRegistrationSetDao _implicitRegistrationSetDao;
	
	private static final Logger __log = Logger.getLogger(AddSubscriptionPage.class);
	
	@SuppressWarnings("unchecked")
	public AddSubscriptionPage()
	{
		add(new Label("title", getTitle()));
		Form form = new Form("form");
		add(form);
		
		WebMarkupContainer privateId = new WebMarkupContainer("privateIdentity", 
				new CompoundPropertyModel( new LoadableDetachableModel(new PrivateIdentity()) {
			@Override
			protected Object load()
			{
				return new PrivateIdentity();
			}
			
		}));
		form.add(privateId);
		
		privateId.add(new RequiredTextField<String>("identity"));
		privateId.add(new TextField("passwordAsString", String.class));
		privateId.add(new TextField("operatorId", byte[].class).add(new AbstractValidator<byte[]>()
		{
			@Override
			protected void onValidate(IValidatable<byte[]> validatable)
			{
				if (validatable.getValue().length != 16)
					error(validatable, "validator.byteArray.length");
			}
		}));
		
		WebMarkupContainer publicId = new WebMarkupContainer("publicIdentity", 
				new CompoundPropertyModel( new LoadableDetachableModel(new PublicUserIdentity()) {
			@Override
			protected Object load()
			{
				return new PublicUserIdentity();
			}
			
		}));
		form.add(publicId);
		publicId.add(new RequiredTextField<String>("identity").add(new UriValidator()));
		publicId.add(new CheckBox("barred"));

		publicId.add(new DropDownChoice("identityType",
				Arrays.asList(new Short[]{IdentityType.PUBLIC_USER_IDENTITY, IdentityType.WILDCARDED_IMPU}),
				new ChoiceRenderer<Short>()
		{
			@Override
			public Object getDisplayValue(Short id)
			{
				return IdentityType.toString(id);
			}
			
		}));
		publicId.add(new TextField("displayName", String.class));
		publicId.add(new DropDownChoice("serviceProfile",
				new LoadableDetachableModel() {
			
					@Override
					protected Object load()
					{
						return _serviceProfileDao.getAllServiceProfile();
					}
			
				},
				new ChoiceRenderer<ServiceProfile>()
				{
					@Override
					public Object getDisplayValue(ServiceProfile profile)
					{
						return profile.getName();
					}
					
				}));
		
		WebMarkupContainer subscriptionMarkup = new WebMarkupContainer("subscription", new CompoundPropertyModel( new LoadableDetachableModel() {
			@Override
			protected Object load()
			{
				return new Subscription();
			}
			
		}));
		form.add(subscriptionMarkup);
		subscriptionMarkup.add(new RequiredTextField<String>("name"));
		
		form.add(new Button("ok")
		{
			@Override
			public void onSubmit()
			{
				Form form = getForm();
				try
				{
					Subscription subscription = (Subscription) form.get("subscription").getDefaultModelObject();
					
					PrivateIdentity privateIdentity = (PrivateIdentity) form.get("privateIdentity").getDefaultModelObject();  
					privateIdentity.setSubscription(subscription);
					
					PublicUserIdentity publicIdentity = (PublicUserIdentity) form.get("publicIdentity").getDefaultModelObject(); 
					privateIdentity.addPublicId(publicIdentity);
					
					ImplicitRegistrationSet implicitRegistrationSet = new ImplicitRegistrationSet();
					_implicitRegistrationSetDao.save(implicitRegistrationSet);
					publicIdentity.setImplicitRegistrationSet(implicitRegistrationSet);
					
					_dao.saveWithCascade(subscription);

					getSession().info(getString("modification.success"));

					setResponsePage(ViewSubscriptionPage.class, new PageParameters("id=" + subscription.getName()));
				}
				catch (Exception e)
				{
					__log.debug("Unable to apply add subscription", e);
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
