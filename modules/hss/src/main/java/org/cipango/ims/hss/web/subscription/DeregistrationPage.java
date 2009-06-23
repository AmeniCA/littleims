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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.PageParameters;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.ListMultipleChoice;
import org.apache.wicket.markup.html.form.RadioChoice;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.cipango.ims.hss.db.PrivateIdentityDao;
import org.cipango.ims.hss.db.PublicIdentityDao;
import org.cipango.ims.hss.model.PrivateIdentity;
import org.cipango.ims.hss.model.PublicIdentity;
import org.cipango.ims.hss.model.PublicUserIdentity;
import org.cipango.ims.hss.model.Subscription;
import org.cipango.ims.hss.model.ImplicitRegistrationSet.State;

public class DeregistrationPage extends SubscriptionPage
{
	@SpringBean
	PrivateIdentityDao _privateIdentityDao;
	
	@SpringBean
	PublicIdentityDao _publicIdentityDao;
	
	private String _key;

	@SuppressWarnings("unchecked")
	public DeregistrationPage(PageParameters pageParameters)
	{
		Subscription subscription = getSubscription(pageParameters);
		_key = subscription == null ? null : subscription.getName();

		Form form = new Form("form");
		form.add(new Label("title", _key));
		
		RadioChoice deregistrationType = new RadioChoice("deregistrationType",
				new Model(true),
				Arrays.asList(new Boolean[] {true, false}),
				new ChoiceRenderer<Boolean>()
		{
			@Override
			public Object getDisplayValue(Boolean id)
			{
				if (id)
					return "Public identities";
				else
					return "Private identities";
			}
		});
		form.add(deregistrationType);
		deregistrationType.add(new AjaxFormComponentUpdatingBehavior("onChange")
		{
			@Override
			protected void onUpdate(AjaxRequestTarget target)
			{
				Component privateId = getPage().get("form:privateIdTr");
				Component publicIds = getPage().get("form:publicIdsTr");
				privateId.setVisible(!privateId.isVisible());
				publicIds.setVisible(!publicIds.isVisible());
				target.addComponent(privateId);
				target.addComponent(publicIds);
			}
		});
				
		List<String> privateIds = subscription == null ? Collections.EMPTY_LIST
				: new ArrayList(subscription.getPrivateIds());
		WebMarkupContainer privateIdTr = new WebMarkupContainer("privateIdTr");
		privateIdTr.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true).setVisible(false);
		form.add(privateIdTr);
		privateIdTr.add(new DropDownChoice("privateId", new Model(), privateIds));
		
		List<String> publicIds = subscription == null ? Collections.EMPTY_LIST
				: new ArrayList(subscription.getPublicIds());
		WebMarkupContainer publicIdsTr = new WebMarkupContainer("publicIdsTr");
		publicIdsTr.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
		form.add(publicIdsTr);
		publicIdsTr.add(new ListMultipleChoice("publicIds", 
				new Model(new ArrayList()), 
				publicIds));
		
		form.add(new DropDownChoice("reasonCode", 
				new Model<Integer>(),
				Arrays.asList(new Integer[]	{ 0, 1, 2, 3 }),
				new ChoiceRenderer<Integer>()
		{
			@Override
			public Object getDisplayValue(Integer id)
			{
				return ReasonCode.toString(id);
			}

		}).setRequired(true));

		form.add(new TextField<String>("reasonPhrase", new Model<String>()));

		form.add(new Button("submit")
		{
			public void onSubmit()
			{
				// TODO send RTR
				boolean deregisterPublic = (Boolean) getForm().get("deregistrationType").getDefaultModelObject();
				if (deregisterPublic)
				{
					List<String> publicIds = (List<String>) getForm().get("publicIdsTr:publicIds").getDefaultModelObject();
					Iterator<String> it = publicIds.iterator();
					while (it.hasNext())
					{
						PublicUserIdentity id = (PublicUserIdentity) _publicIdentityDao.findById(it.next());
						id.getImplicitRegistrationSet().deregister();
					}
				}
				else
				{
					String privateId = (String) getForm().get("privateIdTr:privateId").getDefaultModelObject();
					PrivateIdentity privateIdentity = _privateIdentityDao.findById(privateId);
					Iterator<PublicUserIdentity> it = privateIdentity.getPublicIdentities().iterator();
										
					while (it.hasNext())
						it.next().updateState(privateId, State.NOT_REGISTERED);
				}
				
				checkClearScscf();
			}
		});

		form.add(new Button("cancel")
		{
			public void onSubmit()
			{
				getSession().info(
						getString(getPrefix() + ".delete.canceled",
								new DaoDetachableModel(_key)));
				goToBackPage(SubscriptionBrowserPage.class);
			}
		}.setDefaultFormProcessing(false));

		add(form);
		if (subscription != null)
			setContextMenu(new ContextPanel(subscription));
	}
	
	private void checkClearScscf()
	{
		boolean activePublic = false;
		Subscription subscription = _dao.findById(_key);
		for (PublicIdentity publicId : subscription.getPublicIdentities())
		{
			Short state = publicId.getState();
			if (State.NOT_REGISTERED != state)
				activePublic = true;
		}
		if (!activePublic)
			subscription.setScscf(null);
	}

	@Override
	public String getTitle()
	{
		return getString("subscription.deregistration.title");
	}

	static class ReasonCode
	{
		public static final int PERMANENT_TERMINATION = 0,
				NEW_SERVER_ASSIGNED = 1, SERVER_CHANGE = 2, REMOVE_SCSCF = 3;

		public static String toString(int reasonCode)
		{
			switch (reasonCode)
			{
			case PERMANENT_TERMINATION:
				return "PERMANENT_TERMINATION";
			case NEW_SERVER_ASSIGNED:
				return "NEW_SERVER_ASSIGNED";
			case SERVER_CHANGE:
				return "SERVER_CHANGE";
			case REMOVE_SCSCF:
				return "REMOVE_SCSCF";
			default:
				return "Unknown reason: " + reasonCode;
			}
		}
	}
}
