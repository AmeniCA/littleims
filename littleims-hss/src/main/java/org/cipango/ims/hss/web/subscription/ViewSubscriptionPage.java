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
import org.apache.wicket.markup.html.WebMarkupContainer;
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
import org.cipango.ims.hss.model.PublicUserIdentity;
import org.cipango.ims.hss.model.Subscription;
import org.cipango.ims.hss.web.privateid.EditPrivateIdPage;
import org.cipango.ims.hss.web.publicid.EditPublicUserIdPage;
import org.cipango.ims.hss.web.scscf.EditScscfPage;
import org.cipango.ims.hss.web.serviceprofile.ViewServiceProfilePage;
import org.cipango.ims.oam.util.HideableLink;
import org.cipango.ims.oam.util.StringModelIterator;

public class ViewSubscriptionPage extends SubscriptionPage
{
	
	private String _key;
	private String _title;
	
	@SuppressWarnings("unchecked")
	public ViewSubscriptionPage(PageParameters pageParameters)
	{
		super(pageParameters);
		Subscription subscription = getSubscription(pageParameters);
		_key = (subscription == null) ? null : subscription.getName();
		
		_title = getString(getPrefix() + ".edit.title", new DaoDetachableModel(subscription));
		add(new Label("title", _title));
		
		IModel privateIdsModel = new LoadableDetachableModel(subscription == null ? Collections.EMPTY_SET : subscription.getPrivateIdentities()) {
			@Override
			protected Object load()
			{
				return _dao.findById(_key).getPrivateIdentities();
			}
			
		};
		
		add(new SvgMarkupContainer("svg", subscription));
			
		add(new RefreshingView("privateIds", privateIdsModel) {

			@Override
			protected Iterator getItemModels()
			{
				return new CompoundModelIterator((Collection) getDefaultModelObject());
			}

			@Override
			protected void populateItem(Item item)
			{
				PrivateIdentity privateIdentity = (PrivateIdentity) item.getModelObject();
				MarkupContainer link = new BookmarkablePageLink("identityLink", 
						EditPrivateIdPage.class, 
						new PageParameters("id=" + privateIdentity.getIdentity()));
				link.add(new Label("identity"));
				item.add(link);
				item.add(new Label("passwordAsString"));
				item.add(new Label("operatorId"));
				item.add(new Label("sqn"));
								
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
								EditPublicUserIdPage.class, 
								new PageParameters("id=" + item2.getModelObject()));
						item2.add(link);
						link.add(new Label("name", item2.getModel()));
					}
				});
				item.setOutputMarkupId(true);
				item.add(new HideableLink("hideLink", item));
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
				PublicUserIdentity publicIdentity = (PublicUserIdentity) item.getModelObject();
				MarkupContainer link = new BookmarkablePageLink("identityLink", 
						EditPublicUserIdPage.class, 
						new PageParameters("id=" + publicIdentity.getIdentity()));
				link.add(new Label("identity"));
				item.add(link);
				item.add(new Label("barred"));
				item.add(new Label("identityTypeAsString"));
				item.add(new Label("displayName"));
				item.add(new Label("implicitRegistrationSet.stateAsString"));
				MarkupContainer serviceProfileLink = new BookmarkablePageLink("serviceProfileLink", ViewServiceProfilePage.class, 
						new PageParameters("id=" + publicIdentity.getServiceProfile().getName()));
				item.add(serviceProfileLink);
				serviceProfileLink.add(new Label("serviceProfile", publicIdentity.getServiceProfile().getName()));
				item.setOutputMarkupId(true);
				item.add(new HideableLink("hideLink", item));
					
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
		
		IModel scscfModel = new CompoundPropertyModel(new LoadableDetachableModel(subscription == null ? null : subscription.getScscf()) {
			@Override
			protected Object load()
			{
				return _dao.findById(_key).getScscf();
			}		
		});
		
		WebMarkupContainer scscf = new WebMarkupContainer("scscf", scscfModel);
		add(scscf);
		if (scscf.getDefaultModelObject() == null)
			scscf.setVisible(false);
		else
		{
			MarkupContainer link = new BookmarkablePageLink("link", 
					EditScscfPage.class, 
					new PageParameters("id=" + subscription.getScscf().getName()));
			link.add(new Label("name"));
			scscf.add(link);
			scscf.add(new Label("uri"));
			scscf.add(new HideableLink("hideLink", scscf));
		}
		
		
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
