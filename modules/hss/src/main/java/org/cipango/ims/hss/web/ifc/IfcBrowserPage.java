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
package org.cipango.ims.hss.web.ifc;

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
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.collections.MicroMap;
import org.apache.wicket.util.string.Strings;
import org.apache.wicket.util.string.interpolator.MapVariableInterpolator;
import org.cipango.ims.hss.db.ApplicationServerDao;
import org.cipango.ims.hss.model.ApplicationServer;
import org.cipango.ims.hss.model.InitialFilterCriteria;
import org.cipango.ims.hss.web.as.ContextPanel;
import org.cipango.ims.hss.web.serviceprofile.ServiceProfileBrowserPage;
import org.cipango.ims.hss.web.spt.EditSptsPage;

public class IfcBrowserPage extends IfcPage
{
	@SpringBean
	private ApplicationServerDao _applicationServerDao;
	
	private String _title;
	
	@SuppressWarnings("unchecked")
	public IfcBrowserPage(PageParameters pageParameters)
	{		
		String asName = pageParameters.getString("applicationServer");
		add(new BookmarkablePageLink("createLink", EditIfcPage.class));
		
		IColumn[] columns = new IColumn[5];
		columns[0] = new PropertyColumn(new StringResourceModel(getPrefix() + ".name", this, null),
				"name", "name");
		columns[1] = new PropertyColumn(new StringResourceModel(getPrefix() + ".priority", this, null),
				"priority", "priority");
		columns[2] = new PropertyColumn(new StringResourceModel(getPrefix() + ".applicationServer", this, null),
				"application_server", "applicationServerName");
		columns[3] = new AbstractColumn(new Model(getString("contextPanel.serviceProfiles")))
		{
			public void populateItem(Item cellItem, String componentId, IModel model)
			{
				cellItem.add(new NbElemsPanel(componentId, (InitialFilterCriteria) model.getObject()));
			}
		};
		columns[4] = new AbstractColumn(new Model(getString("actions")))
		{
			public void populateItem(Item cellItem, String componentId, IModel model)
			{
				cellItem.add(new ActionsPanel(componentId, (InitialFilterCriteria) model.getObject()));
			}
		};

		DaoDataProvider daoDataProvider = new DaoDataProvider("name", asName);
		DefaultDataTable table = new DefaultDataTable("browser", columns, daoDataProvider, getItemByPage());
		table.setOutputMarkupId(true);
		add(table);
		
		asName = daoDataProvider.getAsName();
		if (!Strings.isEmpty(asName))
		{
			setContextMenu(new ContextPanel(_applicationServerDao.findById(asName)));
			_title = MapVariableInterpolator.interpolate(getString( "as.ifcs.browser.title"),
						new MicroMap("id", asName));
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

		public ActionsPanel(String id, InitialFilterCriteria ifc)
		{
			super(id);
			String key = null;
			if (ifc != null)
			{
				key = ifc.getName();
			}
			add(new BookmarkablePageLink("viewLink", ViewIfcPage.class, new PageParameters(
					"id=" + key)));
			add(new BookmarkablePageLink("editLink", EditIfcPage.class, new PageParameters(
					"id=" + key)));
			add(new BookmarkablePageLink("editSptsLink", EditSptsPage.class, new PageParameters(
					"id=" + key)));
			add(new BookmarkablePageLink("copyLink", EditIfcPage.class, new PageParameters(
					"id=" + key + ",copy=true")));
			add(new BookmarkablePageLink("deleteLink", DeleteIfcPage.class,
					new PageParameters("id=" + key)));
		}

	}
	
	private static class NbElemsPanel extends Panel
	{

		@SuppressWarnings("unchecked")
		public NbElemsPanel(String id, InitialFilterCriteria ifc)
		{
			super(id);
			add(new Label("nb", String.valueOf(ifc.getServiceProfiles().size())));
			add(new BookmarkablePageLink("link", ServiceProfileBrowserPage.class,
					new PageParameters("ifc=" + ifc.getName())));
		}
	}


	@SuppressWarnings("unchecked")
	class DaoDataProvider extends SortableDataProvider
	{
		private Long _asKey;
		private String _asName;
		
		public DaoDataProvider(String sortProperty, String asName)
		{
			setSort(sortProperty, true);
			setAS(asName);
		}

		public Iterator iterator(int first, int count)
		{
			return _dao.iterator(first, count, getSort().getProperty(), getSort()
					.isAscending(), _asKey);
		}

		public int size()
		{
			return _dao.count(_asKey);
		}
		
		public void setAS(String name)
		{
			_asName = name;
			if (Strings.isEmpty(_asName))
			{
				_asName = "";
				_asKey = null;
			}
			else
			{
				ApplicationServer as = _applicationServerDao.findById(_asName);
				if (as == null)
				{
					error(MapVariableInterpolator.interpolate(getString("as.error.notFound"),
							new MicroMap("id", _asName)));
					_asName = "";
					_asKey = null;
				}
				else
					_asKey = as.getId();
			}
		}

		public String getAsName()
		{
			return _asName;
		}
		
		public IModel model(Object o)
		{
			return new CompoundPropertyModel(new DaoDetachableModel((InitialFilterCriteria) o));
		}
	}

}
