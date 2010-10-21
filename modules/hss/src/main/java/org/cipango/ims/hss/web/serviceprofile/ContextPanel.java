package org.cipango.ims.hss.web.serviceprofile;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.cipango.ims.hss.model.ServiceProfile;
import org.cipango.ims.hss.model.SpIfc;
import org.cipango.ims.hss.web.ifc.EditIfcPage;
import org.cipango.ims.hss.web.ifc.ViewIfcPage;
import org.cipango.ims.hss.web.publicid.PublicIdBrowserPage;
import org.cipango.ims.oam.util.AutolinkBookmarkablePageLink;

@SuppressWarnings("unchecked")
public class ContextPanel extends Panel {

	
	public ContextPanel(ServiceProfile serviceProfile) {
		super("contextMenu");
		setOutputMarkupId(true);
		
		add(new AutolinkBookmarkablePageLink("viewLink", ViewServiceProfilePage.class, 
				new PageParameters("id=" + serviceProfile.getName())));
		
		add(new AutolinkBookmarkablePageLink("editLink", EditServiceProfilePage.class, 
				new PageParameters("id=" + serviceProfile.getName())));
		
		add(new AutolinkBookmarkablePageLink("deleteLink", DeleteServiceProfilePage.class, 
				new PageParameters("id=" + serviceProfile.getName())));
		
		add(new AutolinkBookmarkablePageLink("ifcsLink", EditIfcsPage.class, 
				new PageParameters("id=" + serviceProfile.getName())));
		
		add(new AutolinkBookmarkablePageLink("publicIdsLink", PublicIdBrowserPage.class, 
				new PageParameters("serviceProfile=" + serviceProfile.getName())));

		final List<String> ifcs = new ArrayList<String>();
		Iterator<SpIfc> it = serviceProfile.getAllIfcs().iterator();
		while (it.hasNext())
			ifcs.add(it.next().getIfc().getName());
				
		add(new ListView("ifcs", ifcs){

			@Override
			protected void populateItem(ListItem item)
			{
				MarkupContainer link = new AutolinkBookmarkablePageLink("identity", 
						ViewIfcPage.class, 
						new PageParameters("id=" + item.getModelObject()));
				item.add(link);
				link.add(new Label("name", item.getModel()));
			}
		});
		add(new AutolinkBookmarkablePageLink("newIfcLink", EditIfcPage.class, new PageParameters("serviceProfile=" + serviceProfile.getName())));
	}


}
