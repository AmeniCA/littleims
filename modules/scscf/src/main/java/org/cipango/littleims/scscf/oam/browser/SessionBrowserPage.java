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
import org.cipango.littleims.scscf.session.Session;
import org.cipango.littleims.scscf.session.SessionManager;

public class SessionBrowserPage extends BasePage
{
	@SpringBean
	private SessionManager _sessionManager;
		
	@SuppressWarnings("unchecked")
	public SessionBrowserPage(PageParameters pageParameters)
	{		
		
		add(new Label("title", getTitle()));
				
		IDataProvider provider = new AbstractListDataProvider<Session>()
		{
			
			public IModel<Session> model(Session o)
			{
				return new CompoundPropertyModel<Session>(new LoadableSession(o));
			}

			@Override
			public List<Session> load()
			{
				return _sessionManager.getSessions();
			}		
		};
		
		DataView dataView = new DataView("sessions", provider)
		{

			@Override
			protected void populateItem(final Item item)
			{
				Session session = (Session) item.getModelObject();
				
				item.add(new AorLink("aorLink", session.getProfile().getUri()));
				item.add(new Label("barred", String.valueOf(session.getProfile().isBarred())));
				item.add(new Label("sessionCase"));
				item.add(new Label("currentIfc"));
				
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
	
	class LoadableSession extends LoadableDetachableModel<Session>
	{
		private String _key;
	
		public LoadableSession(Session o)
		{
			super(o);
			_key = o.getOwnURI().getParameter(Session.ORIGINAL_DIALOG_IDENTIFIER_PARAM);
		}
	
		@Override
		protected Session load()
		{
			return _sessionManager.getSession(_key);
		}
	}
}
