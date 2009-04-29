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

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.RefreshingView;
import org.apache.wicket.markup.repeater.util.ModelIteratorAdapter;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.cipango.ims.hss.model.PrivateIdentity;
import org.cipango.ims.hss.model.PublicIdentity;
import org.cipango.ims.hss.model.Subscription;
import org.cipango.ims.hss.web.privateid.EditPrivateIdPage;
import org.cipango.ims.hss.web.publicid.EditPublicIdPage;
import org.cipango.ims.hss.web.util.HideableLink;
import org.cipango.ims.hss.web.util.StringModelIterator;

public class ViewSubscriptionPage extends SubscriptionPage
{
	
	private String _key;
	private String _title;
	
	@SuppressWarnings("unchecked")
	public ViewSubscriptionPage(PageParameters pageParameters)
	{
		Subscription subscription = getSubscription(pageParameters);
		_key = subscription == null ? null : subscription.getName();
		
		_title = getString("view.subscription.title", new DaoDetachableModel(subscription));
		add(new Label("title", _title));
		
		IModel privateIdsModel = new LoadableDetachableModel(subscription == null ? Collections.EMPTY_SET : subscription.getPrivateIdentities()) {
			@Override
			protected Object load()
			{
				return _dao.findById(_key).getPrivateIdentities();
			}
			
		};
		
		add(new RefreshingView("privateIds", privateIdsModel) {

			@Override
			protected Iterator getItemModels()
			{
				return new CompoundModelIterator((Collection) getDefaultModelObject());
			}

			@Override
			protected void populateItem(Item item)
			{
				item.add(new Label("identity"));
				item.add(new Label("passwordAsString"));
				item.add(new Label("operatorId"));
				
				PrivateIdentity privateIdentity = (PrivateIdentity) item.getModelObject();
								
				item.add(new RefreshingView("publicIds", new Model((Serializable) privateIdentity.getPublicIds())){

					@Override
					protected Iterator getItemModels()
					{
						return new StringModelIterator(((Collection)getDefaultModelObject()));
					}

					@Override
					protected void populateItem(Item item2)
					{
						MarkupContainer link = new BookmarkablePageLink("identity", 
								EditPublicIdPage.class, 
								new PageParameters("id=" + item2.getModelObject()));
						item2.add(link);
						link.add(new Label("name", item2.getModel()));
					}
				});
				item.setOutputMarkupId(true);
				item.add(new HideableLink("hideLink", item.getMarkupId()));
			}
			
		});
		
		
		IModel publicsModel = new LoadableDetachableModel(subscription == null ? Collections.EMPTY_SET : subscription.getPublicIdentities()) {
			@Override
			protected Object load()
			{
				return _dao.findById(_key).getPublicIdentities();
			}
			
		};
		
		add(new RefreshingView("publicIds", publicsModel) {

			@Override
			protected Iterator getItemModels()
			{
				return new CompoundModelIterator((Collection) getDefaultModelObject());
			}

			@Override
			protected void populateItem(Item item)
			{
				item.add(new Label("identity"));
				item.add(new Label("barred"));
				item.add(new Label("identityTypeAsString"));
				item.add(new Label("displayName"));
				item.add(new Label("implicitRegistrationSet.stateAsString"));
				item.setOutputMarkupId(true);
				item.add(new HideableLink("hideLink", item.getMarkupId()));
				
				PublicIdentity publicIdentity = (PublicIdentity) item.getModelObject();
				
				item.add(new RefreshingView("privateIds", new Model((Serializable) publicIdentity.getPrivateIds())){

					@Override
					protected Iterator getItemModels()
					{
						return new StringModelIterator(((Collection)getDefaultModelObject()));
					}

					@Override
					protected void populateItem(Item item2)
					{
						MarkupContainer link = new BookmarkablePageLink("identity", 
								EditPrivateIdPage.class, 
								new PageParameters("id=" + item2.getModelObject()));
						item2.add(link);
						link.add(new Label("name", item2.getModel()));
					}
				});
			}
			
		});
		
		if (subscription != null)
			setContextMenu(new ContextPanel(subscription));
	}

	@Override
	public String getTitle()
	{
		return _title;
	}
	
	@SuppressWarnings("unchecked")
	class CompoundModelIterator extends ModelIteratorAdapter implements Serializable {
		public CompoundModelIterator(Collection modelObject) {
			super(modelObject.iterator());
		}
		
		@Override
		protected IModel model(Object id)
		{
			return new CompoundPropertyModel(new LoadableDetachableModel(id) {

				@Override
				protected Object load()
				{
					// TODO implements
					return null;
				}
				
			});
		}
	}

}
