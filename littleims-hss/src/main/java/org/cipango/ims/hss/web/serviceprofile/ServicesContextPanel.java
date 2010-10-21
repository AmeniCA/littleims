// ========================================================================
// Copyright 2010 NEXCOM Systems
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
package org.cipango.ims.hss.web.serviceprofile;

import org.apache.wicket.markup.html.panel.Panel;
import org.cipango.ims.hss.web.as.AsBrowserPage;
import org.cipango.ims.hss.web.gba.NafGroupBrowserPage;
import org.cipango.ims.hss.web.ifc.IfcBrowserPage;
import org.cipango.ims.hss.web.subscription.SubscriptionBrowserPage;
import org.cipango.ims.oam.util.AutolinkBookmarkablePageLink;

public class ServicesContextPanel extends Panel
{

	@SuppressWarnings("unchecked")
	public ServicesContextPanel()
	{
		super("contextMenu");
		add(new AutolinkBookmarkablePageLink("IfcBrowserPage", IfcBrowserPage.class));
		add(new AutolinkBookmarkablePageLink("ServiceProfileBrowserPage", ServiceProfileBrowserPage.class));
		add(new AutolinkBookmarkablePageLink("AsBrowserPage", AsBrowserPage.class));
		add(new AutolinkBookmarkablePageLink("NafGroupBrowserPage", NafGroupBrowserPage.class));
	}

}
