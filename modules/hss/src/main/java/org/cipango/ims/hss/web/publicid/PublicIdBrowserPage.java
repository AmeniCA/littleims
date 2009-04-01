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
package org.cipango.ims.hss.web.publicid;

import java.util.Iterator;

import org.apache.wicket.Component;
import org.apache.wicket.PageParameters;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DefaultDataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.filter.FilterForm;
import org.apache.wicket.extensions.markup.html.repeater.data.table.filter.FilteredAbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.filter.GoAndClearFilter;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.cipango.ims.hss.model.PublicIdentity;

public class PublicIdBrowserPage extends PublicIdentityPage
{

	@SuppressWarnings("unchecked")
	public PublicIdBrowserPage()
	{
		add(new BookmarkablePageLink("createLink", EditPublicIdPage.class));

		IColumn[] columns = new IColumn[5];
		columns[0] = new PropertyColumn(new StringResourceModel(getPrefix() + ".name", this, null),
				"identity", "identity");
		columns[1] = new PropertyColumn(new StringResourceModel(getPrefix() + ".barred", this, null),
				"barred", "barred");
		columns[2] = new PropertyColumn(new StringResourceModel(getPrefix() + ".identityType", this, null),
				"identityType", "identityTypeAsString");
		columns[3] = new PropertyColumn(new StringResourceModel(getPrefix() + ".state", this, null),
				"state","stateAsString");
		columns[4] = new FilteredAbstractColumn(new Model("Actions"))
		{

			public void populateItem(Item cellItem, String componentId, IModel model)
			{
				final PublicIdentity id = (PublicIdentity) model.getObject();
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

		public ActionsPanel(String id, PublicIdentity privateIdentity)
		{
			super(id);
			String key = null;
			if (privateIdentity != null)
			{
				key = privateIdentity.getIdentity();
			}

			add(new BookmarkablePageLink("editLink", EditPublicIdPage.class, new PageParameters(
					"id=" + key)));
			add(new BookmarkablePageLink("deleteLink", DeletePublicIdPage.class,
					new PageParameters("id=" + key)));
		}

	}

	class DaoDataProvider extends SortableDataProvider<PublicIdentity>
	{
		public DaoDataProvider(String sortProperty)
		{
			setSort(sortProperty, true);
		}

		public Iterator<PublicIdentity> iterator(int first, int count)
		{
			return _dao.iterator(first, count, getSort().getProperty(), getSort()
					.isAscending());
		}

		public int size()
		{
			return _dao.count();
		}

		public IModel<PublicIdentity> model(PublicIdentity o)
		{
			return new CompoundPropertyModel<PublicIdentity>(new DaoDetachableModel(o));
		}
	}

}
