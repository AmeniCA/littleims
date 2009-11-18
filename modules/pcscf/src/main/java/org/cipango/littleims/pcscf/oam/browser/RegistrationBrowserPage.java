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
package org.cipango.littleims.pcscf.oam.browser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.servlet.sip.Address;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.markup.repeater.data.ListDataProvider;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.cipango.ims.oam.util.AbstractListDataProvider;
import org.cipango.littleims.pcscf.RegContext;
import org.cipango.littleims.pcscf.oam.AorLink;
import org.cipango.littleims.pcscf.oam.BasePage;
import org.cipango.littleims.pcscf.subscription.reg.RegEventService;
import org.cipango.littleims.pcscf.subscription.reg.RegSubscription;

public class RegistrationBrowserPage extends BasePage
{
	@SpringBean
	private RegEventService _service;
		
	public RegistrationBrowserPage(PageParameters pageParameters)
	{		
		addRegistrations();
		addSubscriptions();	
		add(new Label("title", getTitle()));
	}
	
	@SuppressWarnings("unchecked")
	private void addRegistrations()
	{
		List l;
		synchronized (_service.getRegisteredUsers())
		{
			l = new ArrayList(_service.getRegisteredUsers().keySet());
			Collections.sort(l);
		}
		
		DataView dataView = new DataView("registrations", new ListDataProvider(l))
		{

			@Override
			protected void populateItem(final Item item)
			{
				item.add(new AorLink("aorLink", (String) item.getModelObject()));
				
				RegContext regContext = _service.getRegisteredUsers().get(item.getModelObject().toString());
				List<Address> l2 = regContext.getAssociatedUris(); 
				item.add(new ListView("associated", l2)
				{

					@Override
					protected void populateItem(ListItem item)
					{
						item.add(new AorLink("aorLink", (Address) item.getModelObject()));
					}
					
				});
								
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
	
	@SuppressWarnings("unchecked")
	private void addSubscriptions()
	{
		IDataProvider provider = new AbstractListDataProvider<RegSubscription>()
		{
			
			public IModel<RegSubscription> model(RegSubscription o)
			{
				return new CompoundPropertyModel<RegSubscription>(new LoadableSubscription(o));
			}

			@Override
			public List<RegSubscription> load()
			{
				synchronized (_service.getRegSubscriptions())
				{
					return new ArrayList(_service.getRegSubscriptions().values());
				}
			}		
		};
		
		DataView dataView = new DataView<RegSubscription>("subscriptions", 
				provider)
		{

			@Override
			protected void populateItem(final Item<RegSubscription> item)
			{
				item.add(new AorLink("aorLink", item.getModelObject().getAor()));
				item.add(new Label("privateIdentity"));
				item.add(new Label("version"));

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
	
	class LoadableSubscription extends LoadableDetachableModel<RegSubscription>
	{
		private String _key;
	
		public LoadableSubscription(RegSubscription o)
		{
			super(o);
			_key = o.getPrivateIdentity();
		}
	
		@Override
		protected RegSubscription load()
		{
			return _service.getRegSubscriptions().get(_key);
		}
	}
}
