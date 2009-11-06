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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.servlet.sip.SipFactory;
import javax.servlet.sip.URI;

import org.apache.wicket.PageParameters;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.RefreshingView;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.cipango.ims.oam.util.HideableLink;
import org.cipango.littleims.scscf.data.ServiceProfile;
import org.cipango.littleims.scscf.data.UserProfile;
import org.cipango.littleims.scscf.data.UserProfileCache;
import org.cipango.littleims.scscf.data.UserProfileListener;
import org.cipango.littleims.scscf.debug.DebugSession;
import org.cipango.littleims.scscf.oam.AorLink;
import org.cipango.littleims.scscf.oam.BasePage;
import org.cipango.littleims.scscf.oam.browser.ServiceProfilePanel;
import org.cipango.littleims.scscf.registrar.Binding;
import org.cipango.littleims.scscf.registrar.Context;
import org.cipango.littleims.scscf.registrar.Registrar;
import org.cipango.littleims.scscf.registrar.regevent.RegEventManager;
import org.cipango.littleims.scscf.registrar.regevent.RegSubscription;

public class UserPage extends BasePage
{
	@SpringBean
	private UserProfileCache _userProfileCache;
	
	@SpringBean
	private Registrar _registrar;
	
	@SpringBean
	private RegEventManager _regEventManager;
	
	@SpringBean
	private SipFactory _sipFactory;
	
	private String _publicIdentity;
	
	private IModel<UserProfile> _userProfileModel;
	
	public UserPage(PageParameters pageParameters)
	{		
		_publicIdentity = pageParameters.getString("id");
		
		_userProfileModel = new LoadableDetachableModel<UserProfile>()
		{

			@Override
			protected UserProfile load()
			{
				UserProfile userProfile = _userProfileCache.getUserProfiles().get(_publicIdentity);
				if (userProfile == null)
					userProfile = _userProfileCache.getWildcardUserProfiles().get(_publicIdentity);
				return userProfile;
			}
			
		};
		
		addUserProfile();
		addRegistration();
		addSubscriptions();
				
		add(new Label("title", getTitle()));
	}
	
	@SuppressWarnings("unchecked")
	private void addRegistration()
	{
		Context context = _registrar.getContext(_publicIdentity);
		if (context == null)
		{
			add(new WebMarkupContainer("registration").setVisible(false));
			add(new WebMarkupContainer("bindings").setVisible(false));
			info("User is not registered");
			return;
		}
		
		
		
		WebMarkupContainer markup = new WebMarkupContainer("registration");
		add(markup);
		markup.add(new Label("aor", _publicIdentity));
		markup.add(new Label("state", context.getState().toString()));
		markup.add(new ListView("associated", context.getAssociatedURIs())
		{

			@Override
			protected void populateItem(ListItem item)
			{
				item.add(new AorLink("aorLink", (String) item.getModelObject()));
			}
			
		});
		markup.add(new HideableLink("hideLink", markup));
		
		add(new RefreshingView("bindings")
		{
			@Override
			protected Iterator getItemModels()
			{
				Iterator<Binding> it = _registrar.getContext(_publicIdentity).getBindings().iterator();
				List l = new ArrayList();
				while (it.hasNext())
					l.add(new CompoundPropertyModel(new LoadableBinding(it.next())));
				return l.iterator();
			}

			@Override
			protected void populateItem(final Item item)
			{
				item.add(new Label("contact"));
				item.add(new Label("path"));
				item.add(new Label("state"));
				item.add(new Label("expires"));
				item.add(new Label("privateUserIdentity"));
				item.add(new Label("event"));
				item.add(new HideableLink("hideLink", item));
				item.add(new AjaxFallbackLink("reAuthLink")
				{

					@Override
					public void onClick(AjaxRequestTarget target)
					{
						if (target != null)
							target.addComponent(getPage().get("feedback"));
						
						try
						{
							Binding binding = (Binding) item.getModelObject();
							URI aor = _sipFactory.createURI(_publicIdentity);
							_registrar.requestReauthentication(aor, 
									binding.getPrivateUserIdentity());
							info("Network intiated re-authentication send for identity: " + _publicIdentity);
							if (target != null)
								target.addComponent(getParent());
						}
						catch (Exception e)
						{
							warn("Failed to request re-authentication: " + e);
						}
					}
					
				});
				item.setOutputMarkupId(true);
			}
			
		});
		
		
	}
	
	@SuppressWarnings("unchecked")
	private void addUserProfile()
	{
		UserProfile userProfile = _userProfileModel.getObject();
		if (userProfile == null)
		{
			add(new WebMarkupContainer("userProfile").setVisible(false));
			info("No profile is cache");
		}
		else
		{
			IModel model = new CompoundPropertyModel(_userProfileModel);
			WebMarkupContainer markup = new WebMarkupContainer("userProfile", model);
			add(markup);
			markup.add(new Label("aor", _publicIdentity));
			markup.add(new Label("uri"));
			markup.add(new Label("barred"));
			markup.add(new Label("serviceLevelTraceInfo"));
			
			markup.add(new HideableLink("hideLink", markup));
			
			ServiceProfile serviceProfile = userProfile.getServiceProfile();
			
			
			if (serviceProfile == null)
				markup.add(new Label("serviceProfile", "No service profile"));
			else
				markup.add(new ServiceProfilePanel("serviceProfile", serviceProfile));
			
		}
	}
	
	@SuppressWarnings("unchecked")
	private void addSubscriptions()
	{
		
		add(new RefreshingView("regSubscription")
		{
			@Override
			protected Iterator getItemModels()
			{
				List<RegSubscription> l1 = _regEventManager.getSubscriptions(_publicIdentity);
				if (l1 == null || l1.isEmpty())
					return Collections.EMPTY_LIST.iterator();
				List l = new ArrayList();
				for (int i = 0; i < l1.size(); i++)
					l.add(new CompoundPropertyModel(new LoadableRegSub(l1.get(i), i)));
				return l.iterator();
			}

			@Override
			protected void populateItem(Item item)
			{
				item.add(new Label("version"));
				item.add(new Label("subscriberUri"));
				item.add(new Label("expires"));
				item.add(new HideableLink("hideLink", item));
			}
			
		});
		
		add(new RefreshingView("debugSubscription")
		{
			@Override
			protected Iterator getItemModels()
			{
				UserProfile profile = _userProfileModel.getObject();
				if (profile == null)
					return Collections.EMPTY_LIST.iterator();
				
				List<UserProfileListener> l1 = profile.getListeners();
				if (l1 == null || l1.isEmpty())
					return Collections.EMPTY_LIST.iterator();
				List l = new ArrayList();
				for (int i = 0; i < l1.size(); i++)
				{
					UserProfileListener listener = l1.get(i);
					if (listener instanceof DebugSession)
						l.add(new CompoundPropertyModel(new LoadableDebugSub((DebugSession) listener, i)));
				}
				return l.iterator();
			}

			@Override
			protected void populateItem(Item item)
			{
				item.add(new Label("version"));
				item.add(new Label("subscriberUri"));
				item.add(new Label("expires"));
				item.add(new HideableLink("hideLink", item));
			}
			
		});
	}

	@Override
	public String getTitle()
	{
		return "public identity: " + _publicIdentity;
	}
	
	class LoadableRegSub extends LoadableDetachableModel<RegSubscription>
	{
		private int _i;
	
		public LoadableRegSub(RegSubscription o, int i)
		{
			super(o);
			_i = i;
		}
	
		@Override
		protected RegSubscription load()
		{
			return _regEventManager.getSubscriptions(_publicIdentity).get(_i);
		}
	}
	
	class LoadableDebugSub extends LoadableDetachableModel<DebugSession>
	{
		private int _i;
	
		public LoadableDebugSub(DebugSession o, int i)
		{
			super(o);
			_i = i;
		}
	
		@Override
		protected DebugSession load()
		{
			return (DebugSession) _userProfileModel.getObject().getListeners().get(_i);
		}
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

	@Override
	protected void detachModel()
	{
		super.detachModel();
		_userProfileModel.detach();
	}

}
