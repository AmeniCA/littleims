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
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.cipango.ims.oam.util.AbstractListDataProvider;
import org.cipango.littleims.scscf.data.InitialFilterCriteria;
import org.cipango.littleims.scscf.data.UserProfileCache;
import org.cipango.littleims.scscf.oam.BasePage;

public class SharedIfcBrowserPage extends BasePage
{
	@SpringBean
	private UserProfileCache _userProfileCache;
		
	@SuppressWarnings("unchecked")
	public SharedIfcBrowserPage(PageParameters pageParameters)
	{		
		
		add(new Label("title", getTitle()));
		add(new Label("url", _userProfileCache.getSharedIfcsUrl().toString()));
		
		add(new Link("refreshLink")
		{

			@Override
			public void onClick()
			{
				try
				{
					_userProfileCache.refreshSharedIFCs();
					info("Shared iFCs are refreshed");
				}
				catch (Exception e)
				{
					warn("Failed to refresh shared iFCs: " + e);
				}
			}
			
		});
		
		IDataProvider provider = new AbstractListDataProvider()
		{
			
			public IModel model(Object o)
			{
				return new Model((Integer) o);
			}

			@Override
			public List load()
			{
				return new ArrayList(_userProfileCache.getSharedIFCs().keySet());
			}		
		};
				
		DataView dataView = new DataView("ifcs", provider)
		{

			@Override
			protected void populateItem(final Item item)
			{
				Integer id = (Integer) item.getModelObject();
				InitialFilterCriteria ifc = _userProfileCache.getSharedIFCs().get(id);
				item.add(new Label("id", id.toString()));
				item.add(new Label("priority", String.valueOf(ifc.getPriority())));
				item.add(new Label("trigger", ifc.getTriggerPoint()));
				item.add(new Label("as", ifc.getAs().getURI()));
				
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
		dataView.setOutputMarkupId(true);
		add(dataView);
	}
	
	
	@Override
	public String getTitle()
	{
		return "Shared iFCs";
	}
	
}
