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
package org.cipango.ims.hss.web.spt;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.extensions.ajax.markup.html.autocomplete.AutoCompleteTextField;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.string.Strings;
import org.cipango.ims.hss.model.spt.SPT;
import org.cipango.littleims.util.Headers;

public class HeaderSptPanel extends Panel
{
	@SuppressWarnings("unchecked")
	public HeaderSptPanel(String id, IModel<SPT> sptModel)
	{
		super(id, sptModel);
		add(new AutoCompleteTextField<String>("header", String.class)
				{

					@Override
					protected Iterator<String> getChoices(String input)
					{
						if (Strings.isEmpty(input))
				        {
				            return Headers.ALL_HEADERS.iterator();
				        }
				        input = input.trim().toLowerCase();
				        List<String> headers = new ArrayList<String>();
				        Iterator<String> it = Headers.ALL_HEADERS.iterator();
				        while (it.hasNext())
						{
							String name = (String) it.next();
							if (name.toLowerCase().startsWith(input))
								headers.add(name);
						}
				        return headers.iterator();
					}
			
				}.setRequired(true));
		add(new RequiredTextField("content", String.class));
	}
}
