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
package org.cipango.ims.hss.web.scscf;


import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.cipango.ims.hss.model.Scscf;
import org.cipango.ims.hss.web.publicid.PublicIdBrowserPage;
import org.cipango.ims.hss.web.subscription.SubscriptionBrowserPage;

@SuppressWarnings("unchecked")
public class ContextPanel extends Panel {

	
	public ContextPanel(Scscf scscf) {
		super("contextMenu");
		add(new BookmarkablePageLink("editLink", EditScscfPage.class, new PageParameters("id=" + scscf.getName())));
		add(new BookmarkablePageLink("deleteLink", DeleteScscfPage.class, new PageParameters("id=" + scscf.getName())));
		add(new BookmarkablePageLink("psiLink", PublicIdBrowserPage.class, new PageParameters("scscf=" + scscf.getName())));
		add(new BookmarkablePageLink("subscriptionLink", SubscriptionBrowserPage.class, new PageParameters("scscf=" + scscf.getName())));
	}


}
