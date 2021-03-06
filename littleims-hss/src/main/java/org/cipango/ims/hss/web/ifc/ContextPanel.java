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
package org.cipango.ims.hss.web.ifc;


import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.panel.Panel;
import org.cipango.ims.hss.model.InitialFilterCriteria;
import org.cipango.ims.hss.web.serviceprofile.ServiceProfileBrowserPage;
import org.cipango.ims.hss.web.spt.EditSptsPage;
import org.cipango.ims.oam.util.AutolinkBookmarkablePageLink;

@SuppressWarnings("unchecked")
public class ContextPanel extends Panel {

	
	public ContextPanel(InitialFilterCriteria ifc) {
		super("contextMenu");
		setOutputMarkupId(true);
		add(new AutolinkBookmarkablePageLink("editLink", EditIfcPage.class, 
				new PageParameters("id=" + ifc.getName())));
		
		add(new AutolinkBookmarkablePageLink("deleteLink", DeleteIfcPage.class, 
				new PageParameters("id=" + ifc.getName())));
		
		add(new AutolinkBookmarkablePageLink("editSptsLink", EditSptsPage.class, 
				new PageParameters("id=" + ifc.getName())));
		
		add(new AutolinkBookmarkablePageLink("viewLink", ViewIfcPage.class, 
				new PageParameters("id=" + ifc.getName())));
		
		add(new AutolinkBookmarkablePageLink("serviceProfileLink", ServiceProfileBrowserPage.class, 
				new PageParameters("ifc=" + ifc.getName())));
	}


}
