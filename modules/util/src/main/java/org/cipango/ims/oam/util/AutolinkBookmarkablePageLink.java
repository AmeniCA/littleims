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
package org.cipango.ims.oam.util;

import org.apache.wicket.Page;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;

public class AutolinkBookmarkablePageLink<T> extends BookmarkablePageLink<T>
{

	public <C extends Page> AutolinkBookmarkablePageLink(String id, Class<C> pageClass)
	{
		super(id, pageClass);
	}
	
	public <C extends Page> AutolinkBookmarkablePageLink(String id, Class<C> pageClass, PageParameters pageParameters)
	{
		super(id, pageClass, pageParameters);
	}

	
	@Override
	protected void onComponentTag(final ComponentTag tag)
	{
		// Default handling for tag
		super.onComponentTag(tag);
		if (linksTo(getPage()) && samePageParameters())
		{
			tag.put("class", "selected");
		}
	}
	
	public boolean samePageParameters()
	{
		PageParameters pageParameters = getPage().getPageParameters();
		if (parameters == null)
			return pageParameters == null;
		
		if (pageParameters == null)
			return false;
				
		return pageParameters.equals(getPageParameters());
	}
}
