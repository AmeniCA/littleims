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
package org.cipango.ims.hss.web.as;


import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.panel.Panel;
import org.cipango.ims.hss.model.ApplicationServer;
import org.cipango.ims.hss.web.ifc.IfcBrowserPage;
import org.cipango.ims.hss.web.publicid.PublicIdBrowserPage;
import org.cipango.ims.oam.util.AutolinkBookmarkablePageLink;

@SuppressWarnings("unchecked")
public class ContextPanel extends Panel {

	
	public ContextPanel(ApplicationServer applicationServer) {
		super("contextMenu");
		setOutputMarkupId(true);
		add(new AutolinkBookmarkablePageLink("editLink", EditAsPage.class, new PageParameters("id=" + applicationServer.getName())));
		add(new AutolinkBookmarkablePageLink("deleteLink", DeleteAsPage.class, new PageParameters("id=" + applicationServer.getName())));
		add(new AutolinkBookmarkablePageLink("ifcsLink", IfcBrowserPage.class, new PageParameters("applicationServer=" + applicationServer.getName())));
		add(new AutolinkBookmarkablePageLink("psiLink", PublicIdBrowserPage.class, new PageParameters("applicationServer=" + applicationServer.getName())));
	}


}
