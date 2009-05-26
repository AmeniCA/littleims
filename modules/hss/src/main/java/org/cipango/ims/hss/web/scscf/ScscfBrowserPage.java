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

import java.util.Iterator;

import org.apache.wicket.PageParameters;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DefaultDataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.cipango.ims.hss.model.Scscf;
import org.cipango.ims.hss.web.publicid.PublicIdBrowserPage;
import org.cipango.ims.hss.web.subscription.SubscriptionBrowserPage;

public class ScscfBrowserPage extends ScscfPage
{
	
	@SuppressWarnings("unchecked")
	public ScscfBrowserPage()
	{		
		add(new BookmarkablePageLink("createLink", EditScscfPage.class));
		
		IColumn[] columns = new IColumn[5];
		columns[0] = new PropertyColumn(new StringResourceModel(getPrefix() + ".name", this, null),
				"name", "name");
		columns[1] = new PropertyColumn(new StringResourceModel(getPrefix() + ".uri", this, null),
				"uri", "uri");
		columns[2] = new AbstractColumn(new StringResourceModel(getPrefix() + ".nbSubscriptions", this, null)) 
		{
			public void populateItem(Item cellItem, String componentId, IModel model)
			{
				cellItem.add(new NbElemsPanel(componentId, (Scscf) model.getObject(), false));
			}
			
		};
		columns[3] = new AbstractColumn(new StringResourceModel(getPrefix() + ".nbPsi", this, null)) 
		{
			public void populateItem(Item cellItem, String componentId, IModel model)
			{
				cellItem.add(new NbElemsPanel(componentId, (Scscf) model.getObject(), true));
			}
			
		};
		columns[4] = new AbstractColumn(new Model("Actions"))
		{
			public void populateItem(Item cellItem, String componentId, IModel model)
			{
				cellItem.add(new ActionsPanel(componentId, (Scscf) model.getObject()));
			}
		};

		DefaultDataTable table = new DefaultDataTable("browser", columns, new DaoDataProvider(
				"name"), 15);
		table.setOutputMarkupId(true);
		add(table);
	}

	@Override
	public String getTitle()
	{
		return getString(getPrefix() + ".browser.title");
	}

	@SuppressWarnings("unchecked")
	private static class ActionsPanel extends Panel
	{

		public ActionsPanel(String id, Scscf scscf)
		{
			super(id);
			String key = null;
			if (scscf != null)
			{
				key = scscf.getName();
			}
			add(new BookmarkablePageLink("editLink", EditScscfPage.class, new PageParameters(
					"id=" + key)));
			add(new BookmarkablePageLink("deleteLink", DeleteScscfPage.class,
					new PageParameters("id=" + key)));
		}

	}
	
	private static class NbElemsPanel extends Panel
	{

		@SuppressWarnings("unchecked")
		public NbElemsPanel(String id, Scscf scscf, boolean psi)
		{
			super(id);
			int nb = psi ? scscf.getPsis().size() : scscf.getSubscriptions().size();
			add(new Label("nb", String.valueOf(nb)));
			add(new BookmarkablePageLink("link", psi ? PublicIdBrowserPage.class : SubscriptionBrowserPage.class,
					new PageParameters("scscf=" + scscf.getName())));
		}
	}

	class DaoDataProvider extends SortableDataProvider<Scscf>
	{
		public DaoDataProvider(String sortProperty)
		{
			setSort(sortProperty, true);
		}

		public Iterator<Scscf> iterator(int first, int count)
		{
			return _dao.iterator(first, count, getSort().getProperty(), getSort()
					.isAscending());
		}

		public int size()
		{
			return _dao.count();
		}

		public IModel<Scscf> model(Scscf o)
		{
			return new CompoundPropertyModel<Scscf>(new DaoDetachableModel(o));
		}
	}

}
