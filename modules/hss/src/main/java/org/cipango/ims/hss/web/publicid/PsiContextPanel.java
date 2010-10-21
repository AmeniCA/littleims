package org.cipango.ims.hss.web.publicid;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.cipango.ims.hss.model.PSI;
import org.cipango.ims.hss.web.as.EditAsPage;
import org.cipango.ims.hss.web.scscf.EditScscfPage;
import org.cipango.ims.hss.web.serviceprofile.EditServiceProfilePage;
import org.cipango.ims.oam.util.AutolinkBookmarkablePageLink;

public class PsiContextPanel extends Panel
{

	@SuppressWarnings("unchecked")
	public PsiContextPanel(PSI psi)
	{
		super("contextMenu");
		setOutputMarkupId(true);
		add(new AutolinkBookmarkablePageLink("editLink", EditPsiPage.class, 
				new PageParameters("id=" + psi.getIdentity())));
		add(new AutolinkBookmarkablePageLink("deleteLink", DeletePublicIdPage.class, 
				new PageParameters("id=" + psi.getIdentity())));
			
		if (psi.getScscf() != null)
			add(new AutolinkBookmarkablePageLink("scscfLink", EditScscfPage.class, 
					new PageParameters("id=" + psi.getScscf().getName())));
		else
			add(new WebMarkupContainer("scscfLink").setVisible(false));
		
		if (psi.getApplicationServer()!= null)
			add(new AutolinkBookmarkablePageLink("asLink", EditAsPage.class, 
					new PageParameters("id=" + psi.getApplicationServer().getName())));
		else
			add(new WebMarkupContainer("asLink").setVisible(false));
		
		if (psi.getServiceProfile()!= null)
		{
			add(new AutolinkBookmarkablePageLink("serviceProfileLink", EditServiceProfilePage.class, 
					new PageParameters("id=" + psi.getServiceProfile().getName())));
			add(new AutolinkBookmarkablePageLink("xmlSubscriptionLink", XmlSubscriptionPage.class, 
					new PageParameters("id=" + psi.getIdentity())));
		}
		else
		{
			add(new WebMarkupContainer("serviceProfileLink").setVisible(false));
			add(new WebMarkupContainer("xmlSubscriptionLink").setVisible(false));	
		}
	}

	
}
