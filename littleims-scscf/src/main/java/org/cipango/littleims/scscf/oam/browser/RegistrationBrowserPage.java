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
package org.cipango.littleims.scscf.oam.browser;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.sip.Address;
import javax.servlet.sip.SipFactory;
import javax.servlet.sip.URI;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.PageParameters;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.RefreshingView;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.cipango.ims.oam.util.AbstractListDataProvider;
import org.cipango.littleims.scscf.oam.AorLink;
import org.cipango.littleims.scscf.oam.BasePage;
import org.cipango.littleims.scscf.registrar.Binding;
import org.cipango.littleims.scscf.registrar.Context;
import org.cipango.littleims.scscf.registrar.Registrar;

public class RegistrationBrowserPage extends BasePage
{
	@SpringBean
	private Registrar _registrar;
	
	@SpringBean
	private SipFactory _sipFactory;
		
	@SuppressWarnings("unchecked")
	public RegistrationBrowserPage(PageParameters pageParameters)
	{		
		
		add(new Label("title", getTitle()));
				
		IDataProvider provider = new AbstractListDataProvider()
		{
			
			public IModel model(Object o)
			{
				return new CompoundPropertyModel(new LoadableContext((Context) o));
			}

			@Override
			public List load()
			{
				return _registrar.getRegContexts();
			}		
		};
		
		DataView dataView = new DataView("registrations", provider)
		{

			@Override
			protected void populateItem(final Item item)
			{
				Context context = (Context) item.getModelObject();
				
				item.add(new AorLink("aorLink", context.getPublicIdentity()));
				item.add(new Label("state"));
				
				item.add(new ListView("associated", context.getAssociatedUris())
				{

					@Override
					protected void populateItem(ListItem item)
					{
						item.add(new AorLink("aorLink", ((Address) item.getModelObject()).getURI().toString()));
					}
					
				});
				
				item.add(new RefreshingView("bindings")
				{

					@Override
					protected void populateItem(Item item)
					{
						Binding binding = (Binding) item.getModelObject();
						item.add(new Label("uri", binding.getContact().toString()));
					}

					@Override
					protected Iterator getItemModels()
					{
						Context context = (Context) item.getModelObject();
						Iterator<Binding> it = context.getBindings().iterator();
						List l = new ArrayList();
						while (it.hasNext())
							l.add(new LoadableBinding(it.next(), context.getPublicIdentity()));
						return l.iterator();
					}
					
				});
				item.add(new AjaxFallbackLink("reAuthLink")
				{

					@Override
					public void onClick(AjaxRequestTarget target)
					{
						if (target != null)
							target.addComponent(getPage().get("feedback"));
						
						try
						{
							Context context = (Context) item.getModelObject();
							URI aor = _sipFactory.createURI(context.getPublicIdentity());
							_registrar.requestReauthentication(aor, 
									context.getBindings().get(0).getPrivateUserIdentity());
							info("Network intiated re-authentication send for identity: " + aor);
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
				
				item.add(new AttributeModifier("class", true, new AbstractReadOnlyModel<String>()
				{
					@Override
					public String getObject()
					{
						return (item.getIndex() % 2 == 1) ? "even" : "odd";
					}
				}));
			}
		};

		dataView.setItemsPerPage(getItemByPage());
		add(dataView);
	}
	
	
	@Override
	public String getTitle()
	{
		return "Registrations";
	}
	
	class LoadableContext extends LoadableDetachableModel<Context>
	{
		private String _key;
	
		public LoadableContext(Context o)
		{
			super(o);
			_key = o.getPublicIdentity();
		}
	
		@Override
		protected Context load()
		{
			return _registrar.getContext(_key);
		}
	}
	
	class LoadableBinding extends LoadableDetachableModel<Binding>
	{
		private String _key;
		private String _aor;
	
		public LoadableBinding(Binding o, String aor)
		{
			super(o);
			_key = o.getPrivateUserIdentity();
			_aor = aor;
		}
	
		@Override
		protected Binding load()
		{
			return _registrar.getContext(_aor).getBinding(_key);
		}
	}

}
