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
import java.util.Set;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.PageParameters;
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
import org.apache.wicket.markup.repeater.RefreshingView;
import org.apache.wicket.markup.repeater.util.ModelIteratorAdapter;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.collections.MicroMap;
import org.apache.wicket.util.string.Strings;
import org.apache.wicket.util.string.interpolator.MapVariableInterpolator;
import org.cipango.ims.hss.db.ScscfDao;
import org.cipango.ims.hss.model.Scscf;
import org.cipango.ims.hss.model.Subscription;
import org.cipango.ims.hss.web.privateid.EditPrivateIdPage;
import org.cipango.ims.hss.web.publicid.EditPublicUserIdPage;
import org.cipango.ims.hss.web.scscf.ContextPanel;

public class SubscriptionBrowserPage extends SubscriptionPage
{
	@SpringBean
	private ScscfDao _scscfDao;
	
	private String _title;
	
	@SuppressWarnings("unchecked")
	public SubscriptionBrowserPage(PageParameters pageParameters)
	{		
		String scscf = pageParameters.getString("scscf");
		add(new BookmarkablePageLink("createLink", AddSubscriptionPage.class));
		
		IColumn[] columns = new IColumn[5];
		columns[0] = new PropertyColumn(new StringResourceModel(getPrefix() + ".name", this, null),
				"name", "name");
		columns[1] = new PropertyColumn(new StringResourceModel(getPrefix() + ".scscf", this, null),
				"scscf", "scscf");
		columns[2] = new PropertyColumn(new Model(getString("contextPanel.privateIdentities")), null)
		{
			public void populateItem(Item cellItem, String componentId, IModel model)
			{
				final Subscription id = (Subscription) model.getObject();
				cellItem.add(new IdentityPanel(componentId, id.getPrivateIds(), false));
			}
		};
		columns[3] = new PropertyColumn(new Model(getString("contextPanel.publicIdentities")), null)
		{
			public void populateItem(Item cellItem, String componentId, IModel model)
			{
				final Subscription id = (Subscription) model.getObject();
				cellItem.add(new IdentityPanel(componentId, id.getPublicIds(), true));
			}
		};
		columns[4] = new FilteredAbstractColumn(new Model(getString("actions")))
		{

			public void populateItem(Item cellItem, String componentId, IModel model)
			{
				final Subscription id = (Subscription) model.getObject();
				cellItem.add(new ActionsPanel(componentId, id));
			}

			public Component getFilter(String componentId, FilterForm form)
			{
				return new GoAndClearFilter(componentId, form);
			}

		};
		DaoDataProvider daoDataProvider = new DaoDataProvider("name", scscf);
		DefaultDataTable table = new DefaultDataTable("browser", columns, daoDataProvider, getItemByPage());
		table.setOutputMarkupId(true);
		add(table);
		
		scscf = daoDataProvider.getScscf();
		if (!Strings.isEmpty(scscf))
		{
			setContextMenu(new ContextPanel(_scscfDao.findById(scscf)));
			_title = MapVariableInterpolator.interpolate(getString( "scscf.subscriptions.browser.title"),
						new MicroMap("name", scscf));
		}
		else
			_title = getString(getPrefix() + ".browser.title");
		
		add(new Label("title", _title));
	}

	@Override
	public String getTitle()
	{
		return _title;
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
	

	private static class IdentityPanel extends Panel
	{
		@SuppressWarnings("unchecked")
		public IdentityPanel(String id, final Set<String> identities, final boolean publicId)
		{
			super(id);
			add(new RefreshingView("identity")
			{

				@Override
				protected Iterator getItemModels()
				{
					return new ModelIteratorAdapter<String>(identities.iterator()) {

						@Override
						protected IModel<String> model(String id)
						{
							return new Model<String>(id);
						}
						
					};
				}

				@Override
				protected void populateItem(Item item)
				{
					MarkupContainer link = new BookmarkablePageLink("link", 
							publicId ? EditPublicUserIdPage.class : EditPrivateIdPage.class, 
							new PageParameters("id=" + item.getModelObject()));
					item.add(link);
					link.add(new Label("identity", item.getModel()));
				}
				
			});
		}
	}

	class DaoDataProvider extends SortableDataProvider<Subscription>
	{
		private String _scscf;
		
		public DaoDataProvider(String sortProperty, String scscf)
		{
			setSort(sortProperty, true);
			setScscf(scscf);
		}

		public Iterator<Subscription> iterator(int first, int count)
		{
			if (Strings.isEmpty(_scscf))
				return _dao.iterator(first, count, getSort().getProperty(), getSort()
					.isAscending());
			return _dao.iterator(first, count, getSort().getProperty(), getSort()
					.isAscending(), _scscfDao.findById(_scscf));
		}

		public int size()
		{
			if (Strings.isEmpty(_scscf))
				return _dao.count();		
			return _scscfDao.findById(_scscf).getSubscriptions().size();
		}

		public IModel<Subscription> model(Subscription o)
		{
			return new CompoundPropertyModel<Subscription>(new DaoDetachableModel(o));
		}
		
		public void setScscf(String name)
		{
			_scscf = name;
			if (Strings.isEmpty(_scscf))
			{
				_scscf = "";
			}
			else
			{
				Scscf scscf = _scscfDao.findById(_scscf);
				if (scscf == null)
				{
					error(MapVariableInterpolator.interpolate(getString("scscf.error.notFound"),
							new MicroMap("id", _scscf)));
					_scscf = "";
				}
			}
		}
		
		public String getScscf()
		{
			return _scscf;
		}
	}

}
