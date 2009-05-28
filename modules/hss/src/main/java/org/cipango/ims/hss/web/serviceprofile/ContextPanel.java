package org.cipango.ims.hss.web.serviceprofile;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.cipango.ims.hss.model.InitialFilterCriteria;
import org.cipango.ims.hss.model.ServiceProfile;
import org.cipango.ims.hss.web.ifc.EditIfcPage;
import org.cipango.ims.hss.web.ifc.ViewIfcPage;
import org.cipango.ims.hss.web.publicid.PublicIdBrowserPage;

@SuppressWarnings("unchecked")
public class ContextPanel extends Panel {

	
	public ContextPanel(ServiceProfile serviceProfile) {
		super("contextMenu");
		setOutputMarkupId(true);
		
		add(new BookmarkablePageLink("viewLink", ViewServiceProfilePage.class, 
				new PageParameters("id=" + serviceProfile.getName())));
		
		add(new BookmarkablePageLink("editLink", EditServiceProfilePage.class, 
				new PageParameters("id=" + serviceProfile.getName())));
		
		add(new BookmarkablePageLink("deleteLink", DeleteServiceProfilePage.class, 
				new PageParameters("id=" + serviceProfile.getName())));
		
		add(new BookmarkablePageLink("ifcsLink", EditIfcsPage.class, 
				new PageParameters("id=" + serviceProfile.getName())));
		
		add(new BookmarkablePageLink("publicIdsLink", PublicIdBrowserPage.class, 
				new PageParameters("serviceProfile=" + serviceProfile.getName())));

		final List<String> ifcs = new ArrayList<String>();
		Iterator<InitialFilterCriteria> it = serviceProfile.getIfcs().iterator();
		while (it.hasNext())
			ifcs.add(it.next().getName());
		
		it = serviceProfile.getSharedIfcs().iterator();
		while (it.hasNext())
			ifcs.add(it.next().getName());
		
		add(new ListView("ifcs", ifcs){

			@Override
			protected void populateItem(ListItem item)
			{
				MarkupContainer link = new BookmarkablePageLink("identity", 
						ViewIfcPage.class, 
						new PageParameters("id=" + item.getModelObject()));
				item.add(link);
				link.add(new Label("name", item.getModel()));
			}
		});
		add(new BookmarkablePageLink("newIfcLink", EditIfcPage.class, new PageParameters("serviceProfile=" + serviceProfile.getName())));
	}


}
