// ========================================================================
// Copyright 2009 NEXCOM Systems
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
package org.cipango.littleims.pcscf.oam;

import javax.servlet.sip.Address;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.cipango.littleims.pcscf.oam.browser.UserPage;

@SuppressWarnings("unchecked")
public class AorLink extends BookmarkablePageLink
{

	public AorLink(String id, String aor)
	{
		super("aorLink", UserPage.class, new PageParameters("id=" + aor));
		add(new Label("aor", aor));
	}
	
	public AorLink(String id, Address aor)
	{
		super("aorLink", UserPage.class, new PageParameters("id=" + aor.getURI()));
		add(new Label("aor", aor.toString()));
	}

}
