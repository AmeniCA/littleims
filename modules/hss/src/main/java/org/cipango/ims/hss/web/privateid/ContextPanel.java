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
package org.cipango.ims.hss.web.privateid;


import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.RefreshingView;
import org.apache.wicket.markup.repeater.util.ModelIteratorAdapter;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.cipango.ims.hss.model.PrivateIdentity;
import org.cipango.ims.hss.web.gba.UssBrowserPage;
import org.cipango.ims.hss.web.publicid.EditPublicUserIdPage;
import org.cipango.ims.hss.web.subscription.DeregistrationPage;
import org.cipango.ims.hss.web.subscription.ViewSubscriptionPage;
import org.cipango.ims.oam.util.AutolinkBookmarkablePageLink;

@SuppressWarnings("unchecked")
public class ContextPanel extends Panel {

	
	public ContextPanel(PrivateIdentity privateIdentity) {
		super("contextMenu");
		setOutputMarkupId(true);
		if (privateIdentity.getSubscription() != null)
		{
			add(new AutolinkBookmarkablePageLink("subscriptionLink", ViewSubscriptionPage.class, new PageParameters("id=" + privateIdentity.getSubscription().getName())));
			add(new AutolinkBookmarkablePageLink("deregistrationLink", DeregistrationPage.class, 
					new PageParameters("id=" + privateIdentity.getSubscription().getName())));
		}
		else
		{
			add(new AutolinkBookmarkablePageLink("subscriptionLink", ViewSubscriptionPage.class).setVisible(false));
			add(new AutolinkBookmarkablePageLink("deregistrationLink", DeregistrationPage.class)).setVisible(false);
		}
		add(new AutolinkBookmarkablePageLink("editLink", EditPrivateIdPage.class, new PageParameters("id=" + privateIdentity.getIdentity())));
		add(new AutolinkBookmarkablePageLink("deleteLink", DeletePrivateIdPage.class, new PageParameters("id=" + privateIdentity.getIdentity())));
		add(new AutolinkBookmarkablePageLink("gbaLink", UssBrowserPage.class,
				new PageParameters("privateId=" + privateIdentity.getIdentity())));		
		add(new RefreshingView("publicIds", new Model((Serializable) privateIdentity.getPublicIds())){

			@Override
			protected Iterator getItemModels()
			{
				return new ModelIteratorAdapter<String>(((Collection)getDefaultModelObject()).iterator()) {

					@Override
					protected IModel<String> model(String id)
					{
						return new Model<String>(id);
					}
					
				};
			}

			@Override
			protected void populateItem(Item item)
			{
				MarkupContainer link = new AutolinkBookmarkablePageLink("identity", 
						EditPublicUserIdPage.class, 
						new PageParameters("id=" + item.getModelObject()));
				item.add(link);
				link.add(new Label("name", item.getModel()));
			}
		});
		add(new AutolinkBookmarkablePageLink("editPublicIdsLink", EditPublicIdsPage.class, new PageParameters("id=" + privateIdentity.getIdentity())));
		add(new AutolinkBookmarkablePageLink("newPublicIdLink", EditPublicUserIdPage.class, new PageParameters("privateId=" + privateIdentity.getIdentity())));
	}


}
