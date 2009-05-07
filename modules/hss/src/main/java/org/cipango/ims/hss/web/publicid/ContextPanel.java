package org.cipango.ims.hss.web.publicid;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.RefreshingView;
import org.apache.wicket.markup.repeater.util.ModelIteratorAdapter;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.cipango.ims.hss.model.PublicPrivate;
import org.cipango.ims.hss.model.PublicUserIdentity;
import org.cipango.ims.hss.model.Subscription;
import org.cipango.ims.hss.web.privateid.EditPrivateIdPage;
import org.cipango.ims.hss.web.serviceprofile.EditServiceProfilePage;
import org.cipango.ims.hss.web.serviceprofile.ViewServiceProfilePage;
import org.cipango.ims.hss.web.subscription.EditImplicitSetPage;
import org.cipango.ims.hss.web.subscription.ViewSubscriptionPage;

@SuppressWarnings("unchecked")
public class ContextPanel extends Panel {

	
	public ContextPanel(PublicUserIdentity publicIdentity) {
		super("contextMenu");
		add(new BookmarkablePageLink("editLink", EditPublicUserIdPage.class, 
				new PageParameters("id=" + publicIdentity.getIdentity())));
		add(new BookmarkablePageLink("deleteLink", DeletePublicIdPage.class, 
				new PageParameters("id=" + publicIdentity.getIdentity())));
		
		boolean foundSub = false;
		if (!publicIdentity.getPrivateIdentities().isEmpty())
		{
			Subscription subscription = 
				publicIdentity.getPrivateIdentities().iterator().next().getPrivateIdentity().getSubscription();
			if (subscription != null)
			{
				add(new BookmarkablePageLink("subscriptionLink", ViewSubscriptionPage.class, 
						new PageParameters("id=" + subscription.getName())));
				add(new BookmarkablePageLink("implicitSetLink", EditImplicitSetPage.class, 
						new PageParameters("id=" + subscription.getName())));
				foundSub = true;
			}
		}
		
		if (!foundSub)
		{
			add(new BookmarkablePageLink("subscriptionLink", ViewSubscriptionPage.class).setVisible(false));
			add(new BookmarkablePageLink("implicitSetLink", EditImplicitSetPage.class)).setVisible(false);
		}
		
		
		if (publicIdentity.getServiceProfile() == null)
			add(new BookmarkablePageLink("serviceProfileLink", EditServiceProfilePage.class));
		else
			add(new BookmarkablePageLink("serviceProfileLink", ViewServiceProfilePage.class, 
				new PageParameters("id=" + publicIdentity.getServiceProfile().getName())));

		final List<String> privateIds = new ArrayList<String>();
		Iterator<PublicPrivate> it = publicIdentity.getPrivateIdentities().iterator();
		while (it.hasNext())
			privateIds.add(it.next().getPrivateId());
		
		add(new RefreshingView("privateIds"){

			@Override
			protected Iterator getItemModels()
			{
				return new ModelIteratorAdapter<String>(privateIds.iterator()) {

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
				MarkupContainer link = new BookmarkablePageLink("identity", 
						EditPrivateIdPage.class, 
						new PageParameters("id=" + item.getModelObject()));
				item.add(link);
				link.add(new Label("name", item.getModel()));
			}
		});
		add(new BookmarkablePageLink("newPrivateIdLink", EditPrivateIdPage.class, new PageParameters("publicId=" + publicIdentity.getIdentity())));
	}


}
