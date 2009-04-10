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
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.extensions.ajax.markup.html.autocomplete.AutoCompleteTextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.string.Strings;
import org.cipango.ims.hss.model.spt.SPT;

public class MethodSptPanel extends Panel
{
	private static final List<String> METHODS =
		Arrays.asList("INFO", "INVITE", "MESSAGE", "PUBLISH", "OPTIONS", "REFER", "REGISTER", "SUBSCRIBE");
	

	public MethodSptPanel(String id, IModel<SPT> sptModel)
	{
		super(id, sptModel);
		add(new AutoCompleteTextField<String>("method")
		        {
		            @Override
		            protected Iterator<String> getChoices(String input)
		            {
		                if (Strings.isEmpty(input))
		                {
		                    return METHODS.iterator();
		                }
		                input = input.trim().toUpperCase();
		                List<String> methods = new ArrayList<String>();
		                Iterator<String> it = METHODS.iterator();
		                while (it.hasNext())
						{
							String method = (String) it.next();
							if (method.startsWith(input))
								methods.add(method);
						}
		                return methods.iterator();
		            }
		        }.setRequired(true));
	}
}
