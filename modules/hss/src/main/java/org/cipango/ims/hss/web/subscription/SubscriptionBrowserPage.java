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
package org.cipango.ims.hss.web.subscription;

import java.util.Iterator;

import org.apache.wicket.Component;
import org.apache.wicket.PageParameters;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
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
import org.cipango.ims.hss.model.Subscription;
import org.cipango.ims.hss.web.privateid.EditPrivateIdPage;

public class SubscriptionBrowserPage extends SubscriptionPage
{
	
	@SuppressWarnings("unchecked")
	public SubscriptionBrowserPage()
	{
		add(new AjaxFallbackLink("createLink") {
			@Override
			public void onClick(AjaxRequestTarget target)
			{
				Subscription subscription = new Subscription();
				_dao.save(subscription);
				info(getString("add.success"));	
				if (target != null) {
					target.addComponent(getPage().get("feedback"));
					target.addComponent(getPage().get("browser"));
				}
			}	
		});
		
		add(new BookmarkablePageLink("createLink2", AddSubscriptionPage.class));
		
		IColumn[] columns = new IColumn[3];
		columns[0] = new PropertyColumn(new StringResourceModel(getPrefix() + ".name", this, null),
				"name", "name");
		columns[1] = new PropertyColumn(new StringResourceModel(getPrefix() + ".scscf", this, null),
				"scscf", "scscf");
		columns[2] = new FilteredAbstractColumn(new Model("Actions"))
		{

			public void populateItem(Item cellItem, String componentId, IModel model)
			{
				final Subscription id = (Subscription) model.getObject();
				cellItem.add(new ActionsPanel(componentId, id));
			}

			// return the go-and-clear filter for the filter toolbar
			public Component getFilter(String componentId, FilterForm form)
			{
				return new GoAndClearFilter(componentId, form);
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

		public ActionsPanel(String id, Subscription subscription)
		{
			super(id);
			String key = null;
			if (subscription != null)
			{
				key = subscription.getName();
			}
			add(new BookmarkablePageLink("viewLink", ViewSubscriptionPage.class, new PageParameters(
					"id=" + key)));
			add(new BookmarkablePageLink("editLink", EditSubscriptionPage.class, new PageParameters(
					"id=" + key)));
			add(new BookmarkablePageLink("addPrivateLink", EditPrivateIdPage.class, new PageParameters(
					"subscription=" + key)));
			add(new BookmarkablePageLink("deleteLink", DeleteSubscriptionPage.class,
					new PageParameters("id=" + key)));
		}

	}

	class DaoDataProvider extends SortableDataProvider<Subscription>
	{
		public DaoDataProvider(String sortProperty)
		{
			setSort(sortProperty, true);
		}

		public Iterator<Subscription> iterator(int first, int count)
		{
			return _dao.iterator(first, count, getSort().getProperty(), getSort()
					.isAscending());
		}

		public int size()
		{
			return _dao.count();
		}

		public IModel<Subscription> model(Subscription o)
		{
			return new CompoundPropertyModel<Subscription>(new DaoDetachableModel(o));
		}
	}

}
