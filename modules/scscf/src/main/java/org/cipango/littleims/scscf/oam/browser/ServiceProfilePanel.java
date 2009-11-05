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
package org.cipango.littleims.scscf.oam.browser;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.cipango.littleims.scscf.data.ServiceProfile;

public class ServiceProfilePanel extends Panel
{

	@SuppressWarnings("unchecked")
	public ServiceProfilePanel(String id, ServiceProfile serviceProfile)
	{
		super(id);
		new CompoundPropertyModel(serviceProfile);
		List l = new ArrayList();
		Iterator it = serviceProfile.getIFCsIterator();
		while (it.hasNext())
			l.add(it.next());
		add(new ListView("ifc", l)
		{

			@Override
			protected void populateItem(ListItem item)
			{
				item.setModel(new CompoundPropertyModel(item.getModelObject()));
				item.add(new Label("priority"));
				item.add(new Label("triggerPoint"));
				item.add(new Label("as"));
			}
			
		});
		
	}

}
