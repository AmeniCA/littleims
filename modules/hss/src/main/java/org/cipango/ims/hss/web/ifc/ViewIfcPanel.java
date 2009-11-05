package org.cipango.ims.hss.web.ifc;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;
import org.cipango.ims.hss.model.InitialFilterCriteria;
import org.cipango.ims.hss.util.XML;
import org.cipango.ims.hss.util.XML.Output;
import org.cipango.ims.hss.web.as.EditAsPage;
import org.cipango.ims.hss.web.serviceprofile.ServiceProfileBrowserPage;
import org.cipango.ims.hss.web.spt.EditSptsPage;
import org.cipango.ims.oam.util.HideableLink;

@SuppressWarnings("unchecked")
public class ViewIfcPanel extends Panel
{
	
	public ViewIfcPanel(String id, IModel<InitialFilterCriteria> model, boolean shared)
	{
		super(id, model);
		InitialFilterCriteria ifc = model.getObject();
		
		MarkupContainer link = new BookmarkablePageLink("elementLink", 
				ViewIfcPage.class, 
				new PageParameters("id=" + ifc.getName()));
		add(link);
		link.add(new Label("title", shared ? new ResourceModel("view.ifc.shared.title") : new ResourceModel("view.ifc.title")));
		link.add(new Label("name", ifc.getName()));
		
		add(new Label("id").setVisible(shared));
		
		add(new Label("priority"));
		add(new Label("profilePartIndicatorAsString"));
		
		MarkupContainer asLink = new BookmarkablePageLink("asLink",
				EditAsPage.class,
				new PageParameters("id=" + ifc.getApplicationServerName()));
		add(asLink);
		asLink.add(new Label("applicationServer", ifc.getApplicationServerName()));

		MarkupContainer sptLink = new BookmarkablePageLink("sptLink", 
				EditSptsPage.class, 
				new PageParameters("id=" + ifc.getName()));
		add(sptLink);
		add(new Label("expression"));
		
		MarkupContainer serviceProfilesLink = new BookmarkablePageLink("serviceProfilesLink",
				ServiceProfileBrowserPage.class,
				new PageParameters("ifc=" + ifc.getName()));
		add(serviceProfilesLink);
		serviceProfilesLink.add(new Label("serviceProfiles", 
				String.valueOf(ifc.getServiceProfiles().size())));
		
		Output out = XML.getPretty().newOutput();
		ifc.print(out);
		String xml = out.toString();
		add(new Label("xml", xml));
										
		setOutputMarkupId(true);
		add(new HideableLink("hideLink", getMarkupId()));
	}

}
