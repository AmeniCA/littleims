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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.RefreshingView;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.markup.repeater.data.ListDataProvider;
import org.apache.wicket.markup.repeater.util.ModelIteratorAdapter;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.cipango.littleims.pcscf.oam.AbstractListDataProvider;
import org.cipango.littleims.pcscf.oam.BasePage;
import org.cipango.littleims.pcscf.subscription.debug.DebugConf;
import org.cipango.littleims.pcscf.subscription.debug.DebugIdService;
import org.cipango.littleims.pcscf.subscription.debug.DebugSession;
import org.cipango.littleims.pcscf.subscription.debug.DebugSubscription;

public class DebugBrowserPage extends BasePage
{
	@SpringBean
	private DebugIdService _service;
		
	public DebugBrowserPage(PageParameters pageParameters)
	{		
		addDebugConfs();
		addSubscriptions();	
		add(new Label("title", getTitle()));
	}
	
	@SuppressWarnings("unchecked")
	private void addDebugConfs()
	{
		List l;
		synchronized (_service.getDebugConfs())
		{
			l = new ArrayList(_service.getDebugConfs().values());
			Collections.sort(l, new Comparator<DebugConf>()
			{
				public int compare(DebugConf o1, DebugConf o2)
				{
					return o1.getAor().compareTo(o2.getAor());
				}
			});
		}

		
		DataView dataView = new DataView("debugConfs", new ListDataProvider(l))
		{

			@Override
			protected void populateItem(final Item item)
			{
				DebugConf debugConf = (DebugConf) item.getModelObject();
				item.add(new Label("aor", debugConf.getAor()));
				item.add(new ListView("debugSessions", debugConf.getSessions())
				{

					@Override
					protected void populateItem(ListItem item2)
					{
						item2.add(new DebugSessionPanel("session", (DebugSession) item2.getModelObject()));
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
		List l;
		synchronized (_service.getDebugSubscriptions())
		{
			l = new ArrayList(_service.getDebugSubscriptions().values());
		}
		IDataProvider provider = new AbstractListDataProvider<DebugSubscription>(l)
		{
			
			public IModel<DebugSubscription> model(DebugSubscription o)
			{
				return new CompoundPropertyModel<DebugSubscription>(new LoadableSubscription(o));
			}		
		};
		
		DataView dataView = new DataView<DebugSubscription>("subscriptions", 
				provider)
		{

			@Override
			protected void populateItem(final Item<DebugSubscription> item)
			{
				item.add(new Label("aor"));
				item.add(new Label("version"));
				
				item.add(new RefreshingView("configs")
				{

					@Override
					protected Iterator getItemModels()
					{
						List l2 = item.getModelObject().getConfigs();
						return new ModelIteratorAdapter(l2.iterator()) {

							@Override
							protected IModel model(Object id)
							{
								return new Model((Serializable) id);
							}
							
						};
					}

					@Override
					protected void populateItem(Item item2)
					{
						DebugConf debugConf = (DebugConf) item2.getModelObject();
						item2.add(new Label("aor", debugConf.getAor()));
						item2.add(new Label("nbSessions", String.valueOf(debugConf.getSessions().size())));
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

	@Override
	public String getTitle()
	{
		return "Debug ID";
	}
	
	class LoadableSubscription extends LoadableDetachableModel<DebugSubscription>
	{
		private String _key;
	
		public LoadableSubscription(DebugSubscription o)
		{
			super(o);
			_key = o.getAor();
		}
	
		@Override
		protected DebugSubscription load()
		{
			return _service.getDebugSubscriptions().get(_key);
		}
	}
}
