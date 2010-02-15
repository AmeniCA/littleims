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
package org.cipango.ims.hss.web.serviceprofile;

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
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.collections.MicroMap;
import org.apache.wicket.util.string.Strings;
import org.apache.wicket.util.string.interpolator.MapVariableInterpolator;
import org.cipango.ims.hss.db.IfcDao;
import org.cipango.ims.hss.model.InitialFilterCriteria;
import org.cipango.ims.hss.model.PublicIdentity;
import org.cipango.ims.hss.model.ServiceProfile;
import org.cipango.ims.hss.web.ifc.ContextPanel;
import org.cipango.ims.hss.web.publicid.PublicIdBrowserPage;

public class ServiceProfileBrowserPage extends ServiceProfilePage
{
	@SpringBean
	private IfcDao _ifcDao;
	
	private String _title;
	
	@SuppressWarnings("unchecked")
	public ServiceProfileBrowserPage(PageParameters pageParameters)
	{
		String ifcName = pageParameters.getString("ifc");
		
		add(new BookmarkablePageLink("createLink", EditServiceProfilePage.class));

		IColumn[] columns = new IColumn[4];
		columns[0] = new PropertyColumn(new StringResourceModel(getPrefix() + ".name", this, null),
				"name", "name");
		columns[1] = new AbstractColumn(new StringResourceModel(getPrefix() + ".nbPublicIds", this, null)) 
		{
			public void populateItem(Item cellItem, String componentId, IModel model)
			{
				cellItem.add(new NbPublicIdsPanel(componentId, (ServiceProfile) model.getObject()));
			}
			
		};
		columns[2] = new AbstractColumn(new StringResourceModel(getPrefix() + ".ifcs", this, null)) 
		{
			public void populateItem(Item cellItem, String componentId, IModel model)
			{
				ServiceProfile sp = (ServiceProfile) model.getObject();
				cellItem.add(new Label(componentId, String.valueOf(sp.getAllIfcs().size())));
			}
			
		};
		columns[3] = new FilteredAbstractColumn(new Model("Actions"))
		{

			public void populateItem(Item cellItem, String componentId, IModel model)
			{
				ServiceProfile id = (ServiceProfile) model.getObject();
				cellItem.add(new ActionsPanel(componentId, id));
			}

			public Component getFilter(String componentId, FilterForm form)
			{
				return new GoAndClearFilter(componentId, form);
			}

		};
		
		DaoDataProvider daoDataProvider = new DaoDataProvider("name", ifcName);

		DefaultDataTable table = new DefaultDataTable("browser", columns, daoDataProvider, getItemByPage());
		add(table);
		
		ifcName = daoDataProvider.getIfc();
		if (!Strings.isEmpty(ifcName))
		{
			setContextMenu(new ContextPanel(_ifcDao.findById(ifcName)));
			_title = MapVariableInterpolator.interpolate(getString( "ifc.serviceProfile.browser.title"),
						new MicroMap("name", ifcName));
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

		public ActionsPanel(String id, ServiceProfile serviceProfile)
		{
			super(id);
			String key = null;
			if (serviceProfile != null)
				key = serviceProfile.getName();

			add(new BookmarkablePageLink("viewLink", ViewServiceProfilePage.class, new PageParameters(
					"id=" + key)));
			add(new BookmarkablePageLink("editLink", EditServiceProfilePage.class, new PageParameters(
					"id=" + key)));
			add(new BookmarkablePageLink("copyLink", EditServiceProfilePage.class, new PageParameters(
					"id=" + key + ",copy=true")));
			add(new BookmarkablePageLink("deleteLink", DeleteServiceProfilePage.class,
					new PageParameters("id=" + key)));
		}
	}
	
	private static class NbPublicIdsPanel extends Panel
	{

		@SuppressWarnings("unchecked")
		public NbPublicIdsPanel(String id, ServiceProfile serviceProfile)
		{
			super(id);
			add(new Label("nb", String.valueOf(serviceProfile.getPublicIdentites().size())));
			add(new BookmarkablePageLink("link", PublicIdBrowserPage.class, 
					new PageParameters("serviceProfile=" + serviceProfile.getName())));
		}
	}

	@SuppressWarnings("unchecked")
	class DaoDataProvider extends SortableDataProvider
	{
		private String _ifcName;
		private Integer _key;
		
		public DaoDataProvider(String sortProperty, String ifcName)
		{
			setSort(sortProperty, true);
			setIfc(ifcName);
		}

		public Iterator iterator(int first, int count)
		{
			return _dao.iterator(first, count, getSort().getProperty(), getSort()
					.isAscending(), _key);
		}

		public int size()
		{
			return _dao.count(_key);
		}
		
		public void setIfc(String name)
		{
			_ifcName = name;
			if (Strings.isEmpty(_ifcName))
			{
				_ifcName = "";
				_key = null;
			}
			else
			{
				InitialFilterCriteria ifc = _ifcDao.findById(_ifcName);
				if (ifc == null)
				{
					error(MapVariableInterpolator.interpolate(getString("as.error.notFound"),
							new MicroMap("id", _ifcName)));
					_ifcName = "";
					_key = null;
				}
				else
					_key = ifc.getId();
			}
		}
		
		public String getIfc()
		{
			return _ifcName;
		}

		public IModel model(Object o)
		{
			return new CompoundPropertyModel<PublicIdentity>(new DaoDetachableModel((ServiceProfile) o));
		}
	}

}
