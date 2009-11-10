package org.cipango.ims.hss.web.subscription;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.RefreshingView;
import org.apache.wicket.markup.repeater.util.ModelIteratorAdapter;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.cipango.ims.hss.model.Subscription;
import org.cipango.ims.hss.web.privateid.EditPrivateIdPage;
import org.cipango.ims.hss.web.publicid.EditPublicUserIdPage;
import org.cipango.ims.hss.web.scscf.EditScscfPage;

@SuppressWarnings("unchecked")
public class ContextPanel extends Panel {

	
	public ContextPanel(Subscription subscription) {
		super("contextMenu");
		setOutputMarkupId(true);
		add(new BookmarkablePageLink("viewLink", ViewSubscriptionPage.class, 
				new PageParameters("id=" + subscription.getName())));
		add(new BookmarkablePageLink("editLink", EditSubscriptionPage.class, 
				new PageParameters("id=" + subscription.getName())));
		add(new BookmarkablePageLink("deleteLink", DeleteSubscriptionPage.class, 
				new PageParameters("id=" + subscription.getName())));
		add(new BookmarkablePageLink("deregistrationLink", DeregistrationPage.class, 
				new PageParameters("id=" + subscription.getName())).setVisible(subscription.getScscf() != null));
		add(new BookmarkablePageLink("implicitSetLink", EditImplicitSetPage.class, 
				new PageParameters("id=" + subscription.getName())));
		
		if (subscription.getScscf() != null)
			add(new BookmarkablePageLink("scscfLink", EditScscfPage.class, 
					new PageParameters("id=" + subscription.getScscf().getName())));
		else
			add(new WebMarkupContainer("scscfLink").setVisible(false));
		
		final Set<String> privateIds = subscription.getPrivateIds();
		
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
		add(new BookmarkablePageLink("newPrivateIdLink", EditPrivateIdPage.class, new PageParameters("subscription=" + subscription.getName())));
		
			
		add(new ListView("publicIds", new ArrayList(subscription.getPublicIds())) {

			@Override
			protected void populateItem(ListItem item)
			{
				MarkupContainer link = new BookmarkablePageLink("identity", 
						EditPublicUserIdPage.class, 
						new PageParameters("id=" + item.getModelObject()));
				item.add(link);
				link.add(new Label("name", item.getModel()));
			}
		});
	}


}
