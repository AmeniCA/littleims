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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.cipango.ims.oam.util.AbstractListDataProvider;
import org.cipango.littleims.scscf.data.ServiceProfile;
import org.cipango.littleims.scscf.data.UserProfile;
import org.cipango.littleims.scscf.data.UserProfileCache;
import org.cipango.littleims.scscf.oam.AorLink;

public class UserProfilePanel extends Panel
{
	@SpringBean
	private UserProfileCache _userProfileCache;
	
	private boolean _wilcard;
		
	@SuppressWarnings("unchecked")
	public UserProfilePanel(String id, boolean wilcard, int itemPerPage)
	{		
		super(id);
		_wilcard = wilcard;
		
		
		IDataProvider provider = new AbstractListDataProvider<UserProfile>()
		{
			
			public IModel<UserProfile> model(UserProfile o)
			{
				return new CompoundPropertyModel<UserProfile>(new LoadableProfile(o));
			}

			@Override
			public List<UserProfile> load()
			{
				synchronized (getProfiles())
				{
					List l = new ArrayList(getProfiles().values());
					Collections.sort(l, new Comparator<UserProfile>()
					{

						public int compare(UserProfile o1, UserProfile o2)
						{
							return o1.getUri().compareTo(o2.getUri());
						}
					});
					return l;
				}
			}		
		};
		
		DataView dataView = new DataView("userProfiles", provider)
		{

			@Override
			protected void populateItem(final Item item)
			{
				UserProfile userProfile = (UserProfile) item.getModelObject();
				item.add(new AorLink("aorLink", userProfile.getUri()));
				item.add(new Label("barred"));
				
				ServiceProfile serviceProfile = userProfile.getServiceProfile();
				if (serviceProfile == null)
					item.add(new Label("serviceProfile", "No service profile"));
				else
					item.add(new ServiceProfilePanel("serviceProfile", serviceProfile));
				
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

		dataView.setItemsPerPage(itemPerPage);
		add(dataView);
		
	}
	
		
	public Map<String, UserProfile> getProfiles()
	{
		if (_wilcard)
			return _userProfileCache.getWildcardUserProfiles();
		return _userProfileCache.getUserProfiles();
	}
	
	class LoadableProfile extends LoadableDetachableModel<UserProfile>
	{
		private String _key;
	
		public LoadableProfile(UserProfile o)
		{
			super(o);
			_key = o.getUri();
		}
	
		@Override
		protected UserProfile load()
		{
			return getProfiles().get(_key);
		}
	}
}
