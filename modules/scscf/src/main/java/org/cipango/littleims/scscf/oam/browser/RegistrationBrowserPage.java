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

import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.repeater.Item;
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
				
				item.add(new ListView("associated", context.getAssociatedURIs())
				{

					@Override
					protected void populateItem(ListItem item)
					{
						item.add(new AorLink("aorLink", (String) item.getModelObject()));
					}
					
				});
				
				item.add(new ListView("bindings", context.getBindings())
				{

					@Override
					protected void populateItem(ListItem item)
					{
						Binding binding = (Binding) item.getModelObject();
						item.add(new Label("uri", binding.getContact().toString()));
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
		return "Cache user profiles";
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
}
