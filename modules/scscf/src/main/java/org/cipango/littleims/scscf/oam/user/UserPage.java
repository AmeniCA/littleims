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
package org.cipango.littleims.scscf.oam.user;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.RefreshingView;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.cipango.ims.oam.util.HideableLink;
import org.cipango.littleims.scscf.data.ServiceProfile;
import org.cipango.littleims.scscf.data.UserProfile;
import org.cipango.littleims.scscf.data.UserProfileCache;
import org.cipango.littleims.scscf.oam.AorLink;
import org.cipango.littleims.scscf.oam.BasePage;
import org.cipango.littleims.scscf.oam.browser.ServiceProfilePanel;
import org.cipango.littleims.scscf.registrar.Binding;
import org.cipango.littleims.scscf.registrar.Context;
import org.cipango.littleims.scscf.registrar.Registrar;
import org.cipango.littleims.scscf.registrar.regevent.RegEventManager;

public class UserPage extends BasePage
{
	@SpringBean
	private UserProfileCache _userProfileCache;
	
	@SpringBean
	private Registrar _registrar;
	
	@SpringBean
	private RegEventManager _regEventManager;
	
	private String _publicIdentity;
	
	public UserPage(PageParameters pageParameters)
	{		
		_publicIdentity = pageParameters.getString("id");
		
		addUserProfile();
		addRegistration();
				
		add(new Label("title", getTitle()));
	}
	
	@SuppressWarnings("unchecked")
	private void addRegistration()
	{
		final Context context = _registrar.getContext(_publicIdentity);
		if (context == null)
		{
			add(new WebMarkupContainer("registration").setVisible(false));
			add(new WebMarkupContainer("bindings").setVisible(false));
			info("User is not registered");
			return;
		}
		
		WebMarkupContainer markup = new WebMarkupContainer("registration", 
				new CompoundPropertyModel(context));
		add(markup);
		markup.add(new Label("aor", _publicIdentity));
		markup.add(new Label("state"));
		markup.add(new ListView("associated", context.getAssociatedURIs())
		{

			@Override
			protected void populateItem(ListItem item)
			{
				item.add(new AorLink("aorLink", (String) item.getModelObject()));
			}
			
		});
		markup.add(new HideableLink("hideLink", getMarkupId()));
		
		add(new RefreshingView("bindings")
		{
			@Override
			protected Iterator getItemModels()
			{
				Iterator<Binding> it = context.getBindings().iterator();
				List l = new ArrayList();
				while (it.hasNext())
					l.add(new CompoundPropertyModel(new LoadableBinding(it.next())));
				return l.iterator();
			}

			@Override
			protected void populateItem(Item item)
			{
				item.add(new Label("contact"));
				item.add(new Label("path"));
				item.add(new Label("state"));
				item.add(new Label("expires"));
				item.add(new Label("privateUserIdentity"));
				item.add(new Label("event"));
				item.add(new HideableLink("hideLink", getMarkupId()));
			}
			
		});
		
		
	}

	@SuppressWarnings("unchecked")
	private void addUserProfile()
	{
		UserProfile userProfile = _userProfileCache.getUserProfiles().get(_publicIdentity);
		if (userProfile == null)
			userProfile = _userProfileCache.getWildcardUserProfiles().get(_publicIdentity);
		
		if (userProfile == null)
		{
			add(new WebMarkupContainer("userProfile").setVisible(false));
			info("No profile is cache");
		}
		else
		{
			WebMarkupContainer markup = new WebMarkupContainer("userProfile", new CompoundPropertyModel(userProfile));
			add(markup);
			markup.add(new Label("aor", _publicIdentity));
			markup.add(new Label("uri"));
			markup.add(new Label("barred"));
			markup.add(new Label("serviceLevelTraceInfo"));
			
			markup.add(new HideableLink("hideLink", getMarkupId()));
			
			ServiceProfile serviceProfile = userProfile.getServiceProfile();
			
			
			if (serviceProfile == null)
				markup.add(new Label("serviceProfile", "No service profile"));
			else
				markup.add(new ServiceProfilePanel("serviceProfile", serviceProfile));
			
		}
	}
	
	private void addSubscriptions()
	{
		//_regEventManager.
	}

	@Override
	public String getTitle()
	{
		return "public identity: " + _publicIdentity;
	}
	
	class LoadableBinding extends LoadableDetachableModel<Binding>
	{
		private String _key;
	
		public LoadableBinding(Binding o)
		{
			super(o);
			_key = o.getPrivateUserIdentity();
		}
	
		@Override
		protected Binding load()
		{
			return _registrar.getContext(_publicIdentity).getBinding(_key);
		}
	}
	
}
