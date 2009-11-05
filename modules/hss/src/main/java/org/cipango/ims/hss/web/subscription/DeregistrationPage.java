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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.wicket.Component;
import org.apache.wicket.PageParameters;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormChoiceComponentUpdatingBehavior;
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
import org.apache.wicket.util.collections.MicroMap;
import org.apache.wicket.util.string.interpolator.MapVariableInterpolator;
import org.cipango.ims.Cx;
import org.cipango.ims.hss.db.PrivateIdentityDao;
import org.cipango.ims.hss.db.PublicIdentityDao;
import org.cipango.ims.hss.model.PrivateIdentity;
import org.cipango.ims.hss.model.PublicIdentity;
import org.cipango.ims.hss.model.PublicUserIdentity;
import org.cipango.ims.hss.model.Subscription;
import org.cipango.ims.oam.util.AjaxFallbackButton;

public class DeregistrationPage extends SubscriptionPage
{
	private static final Logger __log = Logger.getLogger(DeregistrationPage.class);
	
	@SpringBean
	private PrivateIdentityDao _privateIdentityDao;
	
	@SpringBean
	private PublicIdentityDao _publicIdentityDao;
		
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
		deregistrationType.add(new AjaxFormChoiceComponentUpdatingBehavior()
		{
			@Override
			protected void onUpdate(AjaxRequestTarget target)
			{
				Component privateId = getPage().get("form:privateIdTr");
				Component publicIds = getPage().get("form:publicIdsTr");
				boolean privateVisible = !privateId.isVisible();
				privateId.setVisible(privateVisible);
				((ListMultipleChoice) getPage().get("form:privateIdTr:privateIds")).setRequired(privateVisible);
				((ListMultipleChoice) getPage().get("form:publicIdsTr:publicIds")).setRequired(!privateVisible);
				publicIds.setVisible(!privateVisible);
				target.addComponent(privateId);
				target.addComponent(publicIds);
			}
		});
				
		List<String> privateIds = subscription == null ? Collections.EMPTY_LIST
				: new ArrayList(subscription.getPrivateIds());
		WebMarkupContainer privateIdTr = new WebMarkupContainer("privateIdTr");
		privateIdTr.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true).setVisible(false);
		form.add(privateIdTr);
		privateIdTr.add(new ListMultipleChoice("privateIds", 
				new Model(new ArrayList()), 
				privateIds));
		
		List<String> publicIds = subscription == null ? Collections.EMPTY_LIST
				: new ArrayList(subscription.getPublicIds());
		WebMarkupContainer publicIdsTr = new WebMarkupContainer("publicIdsTr");
		publicIdsTr.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
		form.add(publicIdsTr);
		publicIdsTr.add(new ListMultipleChoice("publicIds", 
				new Model(new ArrayList()), 
				publicIds).setRequired(true));
		
		form.add(new DropDownChoice("reasonCode", 
				new Model<Integer>(),
				Arrays.asList(new Integer[]	{ 0, 1, 2, 3 }),
				new ChoiceRenderer<Integer>()
		{
			@Override
			public Object getDisplayValue(Integer id)
			{
				return Cx.ReasonCode.toString(id);
			}

		}).setRequired(true));

		form.add(new TextField<String>("reasonPhrase", new Model<String>()));

		form.add(new AjaxFallbackButton("submit", form)
		{
			@Override
			protected void doSubmit(AjaxRequestTarget target, Form<?> form)
					throws Exception
			{
				boolean deregisterPublic = (Boolean) getForm().get("deregistrationType").getDefaultModelObject();
				String reasonPhrase = (String) getForm().get("reasonPhrase").getDefaultModelObject();
				Integer reasonCode = (Integer) getForm().get("reasonCode").getDefaultModelObject();

				if (deregisterPublic)
				{
					List<String> publicIds = (List<String>) getForm().get("publicIdsTr:publicIds").getDefaultModelObject();
					Set<PublicIdentity> publicIdentities = new HashSet<PublicIdentity>(publicIds.size());
					Iterator<String> it = publicIds.iterator();
					while (it.hasNext())
						publicIdentities.add((PublicUserIdentity) _publicIdentityDao.findById(it.next()));
										
					try
					{
						getCxManager().sendRtr(publicIdentities, reasonCode, reasonPhrase);
						getSession().info(getString("subscription.deregistration.done"));
					}
					catch (Exception e)
					{
						__log.warn("Failed to send RTR", e);
						error(MapVariableInterpolator.interpolate(getString("subscription.error.deregistration"),
								new MicroMap("reason", e.toString())));
					}
				}
				else
				{					
					List<String> privateIds = (List<String>) getForm().get("privateIdTr:privateIds").getDefaultModelObject();
					Set<PrivateIdentity> privateIdentities = new HashSet<PrivateIdentity>(privateIds.size());
					Iterator<String> it = privateIds.iterator();
					while (it.hasNext())
						privateIdentities.add(_privateIdentityDao.findById(it.next()));
										
					try
					{
						getCxManager().sendRtrPrivate(privateIdentities, reasonCode, reasonPhrase);
						getSession().info(getString("subscription.deregistration.done"));
					}
					catch (Exception e)
					{
						__log.warn("Failed to send RTR", e);
						error(MapVariableInterpolator.interpolate(getString("subscription.error.deregistration"),
								new MicroMap("reason", e.getMessage())));
					}
					
				}
				if (target != null)
				{
					Component contextMenu = new ContextPanel(_dao.findById(_key));
					getPage().get("contextMenu").replaceWith(contextMenu);				
					target.addComponent(getPage().get("contextMenu"));
				}
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
	
	@Override
	public String getTitle()
	{
		return getString("subscription.deregistration.title");
	}

}
