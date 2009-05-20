package org.cipango.ims.hss.web.publicid;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.cipango.ims.hss.model.PSI;
import org.cipango.ims.hss.web.as.EditAsPage;
import org.cipango.ims.hss.web.scscf.EditScscfPage;
import org.cipango.ims.hss.web.serviceprofile.EditServiceProfilePage;

public class PsiContextPanel extends Panel
{

	@SuppressWarnings("unchecked")
	public PsiContextPanel(PSI psi)
	{
		super("contextMenu");
		setOutputMarkupId(true);
		add(new BookmarkablePageLink("editLink", EditPsiPage.class, 
				new PageParameters("id=" + psi.getIdentity())));
		add(new BookmarkablePageLink("deleteLink", DeletePublicIdPage.class, 
				new PageParameters("id=" + psi.getIdentity())));
		add(new BookmarkablePageLink("xmlSubscriptionLink", XmlSubscriptionPage.class, 
				new PageParameters("id=" + psi.getIdentity())));
		
		if (psi.getScscf() != null)
			add(new BookmarkablePageLink("scscfLink", EditScscfPage.class, 
					new PageParameters("id=" + psi.getScscf().getName())));
		else
			add(new WebMarkupContainer("scscfLink").setVisible(false));
		
		if (psi.getApplicationServer()!= null)
			add(new BookmarkablePageLink("asLink", EditAsPage.class, 
					new PageParameters("id=" + psi.getApplicationServer().getName())));
		else
			add(new WebMarkupContainer("asLink").setVisible(false));
		
		if (psi.getServiceProfile()!= null)
			add(new BookmarkablePageLink("serviceProfileLink", EditServiceProfilePage.class, 
					new PageParameters("id=" + psi.getServiceProfile().getName())));
		else
			add(new WebMarkupContainer("serviceProfileLink").setVisible(false));	
	}

	
}
