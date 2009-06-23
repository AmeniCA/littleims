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
package org.cipango.ims.hss.web.privateid;

import java.util.Iterator;

import org.apache.wicket.Component;
import org.apache.wicket.PageParameters;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DefaultDataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.filter.FilterForm;
import org.apache.wicket.extensions.markup.html.repeater.data.table.filter.FilteredAbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.filter.GoAndClearFilter;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.cipango.ims.hss.model.PrivateIdentity;
import org.cipango.ims.hss.model.ServiceProfile;

public class PrivateIdBrowserPage extends PrivateIdentityPage
{

	@SuppressWarnings("unchecked")
	public PrivateIdBrowserPage()
	{
		add(new BookmarkablePageLink("createLink", EditPrivateIdPage.class));

		IColumn[] columns = new IColumn[3];
		columns[0] = new PropertyColumn(new StringResourceModel(getPrefix() + ".name", this, null),
				"identity");
		columns[1] = new AbstractColumn(new StringResourceModel(getPrefix() + ".publicIds", this, null)) 
		{
			public void populateItem(Item cellItem, String componentId, IModel model)
			{
				PrivateIdentity identity = (PrivateIdentity) model.getObject();
				cellItem.add(new Label(componentId, String.valueOf(identity.getPublicIdentities().size())));
			}
			
		};
		columns[2] = new FilteredAbstractColumn(new Model("Actions"))
		{

			public void populateItem(Item cellItem, String componentId, IModel model)
			{
				final PrivateIdentity id = (PrivateIdentity) model.getObject();
				cellItem.add(new ActionsPanel(componentId, id));
			}

			// return the go-and-clear filter for the filter toolbar
			public Component getFilter(String componentId, FilterForm form)
			{
				return new GoAndClearFilter(componentId, form);
			}

		};

		DefaultDataTable table = new DefaultDataTable("browser", columns, new DaoDataProvider(
				"identity"), 15);
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

		public ActionsPanel(String id, PrivateIdentity privateIdentity)
		{
			super(id);
			String key = null;
			if (privateIdentity != null)
			{
				key = privateIdentity.getIdentity();
			}

			add(new BookmarkablePageLink("editLink", EditPrivateIdPage.class, new PageParameters(
					"id=" + key)));
			add(new BookmarkablePageLink("deleteLink", DeletePrivateIdPage.class,
					new PageParameters("id=" + key)));
		}

	}

	class DaoDataProvider extends SortableDataProvider<PrivateIdentity>
	{
		public DaoDataProvider(String sortProperty)
		{
			setSort(sortProperty, true);
		}

		public Iterator<PrivateIdentity> iterator(int first, int count)
		{
			return _dao.iterator(first, count, getSort().getProperty(), getSort()
					.isAscending());
		}

		public int size()
		{
			return _dao.count();
		}

		public IModel<PrivateIdentity> model(PrivateIdentity o)
		{
			return new CompoundPropertyModel<PrivateIdentity>(new DaoDetachableModel(o));
		}
	}

}
