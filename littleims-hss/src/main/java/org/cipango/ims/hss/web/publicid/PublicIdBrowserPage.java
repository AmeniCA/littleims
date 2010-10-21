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

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.PageParameters;
import org.apache.wicket.extensions.ajax.markup.html.autocomplete.AutoCompleteTextField;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DefaultDataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.filter.FilterForm;
import org.apache.wicket.extensions.markup.html.repeater.data.table.filter.FilteredAbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.filter.GoAndClearFilter;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
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
import org.cipango.ims.hss.db.ScscfDao;
import org.cipango.ims.hss.db.ServiceProfileDao;
import org.cipango.ims.hss.model.ApplicationServer;
import org.cipango.ims.hss.model.PSI;
import org.cipango.ims.hss.model.PublicIdentity;
import org.cipango.ims.hss.model.PublicUserIdentity;
import org.cipango.ims.hss.model.Scscf;
import org.cipango.ims.hss.model.ServiceProfile;
import org.cipango.ims.hss.web.serviceprofile.ContextPanel;
import org.cipango.ims.hss.web.subscription.IdentitiesContextPanel;

public class PublicIdBrowserPage extends PublicIdentityPage
{
	@SpringBean
	private ServiceProfileDao _serviceProfileDao;
	
	@SpringBean
	private ApplicationServerDao _applicationServerDao;
	
	@SpringBean
	private ScscfDao _scscfDao;
	
	private String _title;

	@SuppressWarnings("unchecked")
	public PublicIdBrowserPage(PageParameters pageParameters)
	{
		super(pageParameters);
		String serviceProfile = pageParameters.getString("serviceProfile");
		String applicationServer = pageParameters.getString("applicationServer");
		String scscf = pageParameters.getString("scscf");
		String search = pageParameters.getString("search");
		
		addSearchField(search);
		add(new BookmarkablePageLink("createLink", EditPublicUserIdPage.class));
		add(new BookmarkablePageLink("createPsiLink", EditPsiPage.class));

		IColumn[] columns = new IColumn[5];
		columns[0] = new PropertyColumn(new StringResourceModel(getPrefix() + ".name", this, null),
				"identity", "identity");
		columns[1] = new PropertyColumn(new StringResourceModel(getPrefix() + ".barred", this, null),
				"barred", "barred");
		columns[2] = new PropertyColumn(new StringResourceModel(getPrefix() + ".identityType", this, null),
				"TYPE,regex", "identityTypeAsString");
		columns[3] = new PropertyColumn(new StringResourceModel(getPrefix() + ".state", this, null),
				"stateAsString");
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
		DaoDataProvider daoDataProvider = new DaoDataProvider("identity", serviceProfile, applicationServer, scscf, search);

		DefaultDataTable table = new DefaultDataTable("browser", columns, daoDataProvider, getItemByPage());
		add(table);
		
		serviceProfile = daoDataProvider.getServiceProfile();
		applicationServer = daoDataProvider.getApplicationServer();
		scscf = daoDataProvider.getScscf();
		
		if (!Strings.isEmpty(serviceProfile))
		{
			setContextMenu(new ContextPanel(_serviceProfileDao.findById(serviceProfile)));
			_title = MapVariableInterpolator.interpolate(getString( "serviceProfile.publicIds.browser.title"),
						new MicroMap("name", serviceProfile));
		}
		else if (!Strings.isEmpty(applicationServer))
		{
			setContextMenu(new org.cipango.ims.hss.web.as.ContextPanel(_applicationServerDao.findById(applicationServer)));
			_title = MapVariableInterpolator.interpolate(getString( "as.psi.browser.title"),
						new MicroMap("name", applicationServer));
		}
		else if (!Strings.isEmpty(scscf))
		{
			setContextMenu(new org.cipango.ims.hss.web.scscf.ContextPanel(_scscfDao.findById(scscf)));
			_title = MapVariableInterpolator.interpolate(getString( "scscf.psi.browser.title"),
						new MicroMap("name", scscf));
		}
		else
			_title = getString(getPrefix() + ".browser.title");
		
		add(new Label("title", _title));
		setContextMenu(new IdentitiesContextPanel());
	}
	
	@SuppressWarnings("unchecked")
	private void addSearchField(String search)
	{
		Form form = new Form("form");
        add(form);

        AutoCompleteTextField<String> field = new AutoCompleteTextField<String>("searchInput",
            new Model<String>(search))
        {
            @Override
            protected Iterator<String> getChoices(String input)
            {
                if (input == null || input.length() < 2 && input.trim().length() < 2 )
                {
                    List<String> emptyList = Collections.emptyList();
                    return emptyList.iterator();
                }

                List<String> choices = _dao.findLike("%" + input + "%", 10);
                return choices.iterator();
            }
        };
        form.add(field);

        form.add(new Button("search")
		{
			@Override
			public void onSubmit()
			{
				String id = (String) getForm().get("searchInput").getDefaultModelObject();
				if (!Strings.isEmpty(id))
				{
					PublicIdentity publicIdentity = _dao.findById(id);
					if (publicIdentity == null)
						setResponsePage(PublicIdBrowserPage.class, new PageParameters("search=" + id));
					else if (publicIdentity instanceof PSI)
						setResponsePage(EditPsiPage.class, new PageParameters("id=" + id));
					else
						setResponsePage(EditPublicUserIdPage.class, new PageParameters("id=" + id));
				}
			}
		});
	}

	@Override
	public String getTitle()
	{
		return _title;
	}

	@SuppressWarnings("unchecked")
	private static class ActionsPanel extends Panel
	{

		public ActionsPanel(String id, PublicIdentity identity)
		{
			super(id);
			String key = null;
			if (identity != null)
			{
				key = identity.getIdentity();
			}
			if (identity instanceof PublicUserIdentity)
				add(new BookmarkablePageLink("editLink", EditPublicUserIdPage.class, new PageParameters(
						"id=" + key)));
			else
				add(new BookmarkablePageLink("editLink", EditPsiPage.class, new PageParameters(
						"id=" + key)));
			add(new BookmarkablePageLink("deleteLink", DeletePublicIdPage.class,
					new PageParameters("id=" + key)));
		}

	}

	class DaoDataProvider extends SortableDataProvider<PublicIdentity>
	{
		private String _serviceProfile;
		private String _applicationServer;	
		private String _scscf;
		private String _likeIdentity;
		private Long _key;
		
		public DaoDataProvider(String sortProperty, 
				String serviceProfile, 
				String applicationServer,
				String scscf,
				String likeIdentity)
		{
			setSort(sortProperty, true);
			setServiceProfile(serviceProfile);
			if (Strings.isEmpty(_serviceProfile))
			{
				setApplicationServer(applicationServer);
				if (Strings.isEmpty(_applicationServer))
					setScscf(scscf);
			}
			if (!Strings.isEmpty(likeIdentity))
				_likeIdentity = likeIdentity;
		}	

		public Iterator<PublicIdentity> iterator(int first, int count)
		{		
			if (!Strings.isEmpty(_serviceProfile))
				return _dao.iterator(first, count, getSort().getProperty(), getSort()
						.isAscending(), "_serviceProfile", _key);
			if (!Strings.isEmpty(_applicationServer))
				return _dao.iterator(first, count, getSort().getProperty(), getSort()
						.isAscending(), "_applicationServer", _key);
			if (!Strings.isEmpty(_scscf))
				return _dao.iterator(first, count, getSort().getProperty(), getSort()
						.isAscending(), "_applicationServer", _key);
			if (!Strings.isEmpty(_likeIdentity))
				return _dao.likeIterator(first, count, getSort().getProperty(), getSort()
						.isAscending(), _likeIdentity);
			return _dao.iterator(first, count, getSort().getProperty(), getSort()
					.isAscending());	
		}

		public int size()
		{
			if (!Strings.isEmpty(_serviceProfile))
				return _serviceProfileDao.findById(_serviceProfile).getPublicIdentites().size();
			if (!Strings.isEmpty(_applicationServer))
				return _applicationServerDao.findById(_applicationServer).getPsis().size();
			if (!Strings.isEmpty(_scscf))
				return _scscfDao.findById(_scscf).getPsis().size();
			if (!Strings.isEmpty(_likeIdentity))
				return _dao.countLike(_likeIdentity);
			return _dao.count();
		}
		
		public void setServiceProfile(String name)
		{
			_serviceProfile = name;
			if (Strings.isEmpty(_serviceProfile))
			{
				_serviceProfile = "";
				_key = null;
			}
			else
			{
				ServiceProfile serviceProfile = _serviceProfileDao.findById(_serviceProfile);
				if (serviceProfile == null)
				{
					error(MapVariableInterpolator.interpolate(getString("serviceProfile.error.notFound"),
							new MicroMap("id", _serviceProfile)));
					_serviceProfile = "";
					_key = null;
				}
				else
					_key = serviceProfile.getId();
			}
		}
		
		public void setApplicationServer(String name)
		{
			_applicationServer = name;
			if (Strings.isEmpty(_applicationServer))
			{
				_applicationServer = "";
				_key = null;
			}
			else
			{
				ApplicationServer as = _applicationServerDao.findById(_applicationServer);
				if (as == null)
				{
					error(MapVariableInterpolator.interpolate(getString("as.error.notFound"),
							new MicroMap("id", _applicationServer)));
					_applicationServer = "";
					_key = null;
				}
				else
					_key = as.getId();
			}
		}
		
		public void setScscf(String name)
		{
			_scscf = name;
			if (Strings.isEmpty(_scscf))
			{
				_scscf = "";
				_key = null;
			}
			else
			{
				Scscf scscf = _scscfDao.findById(_scscf);
				if (scscf == null)
				{
					error(MapVariableInterpolator.interpolate(getString("scscf.error.notFound"),
							new MicroMap("id", _scscf)));
					_scscf = "";
					_key = null;
				}
				else
					_key = scscf.getId();
			}
		}
		
		public String getScscf()
		{
			return _scscf;
		}
		
		public String getServiceProfile()
		{
			return _serviceProfile;
		}
		
		public String getApplicationServer()
		{
			return _applicationServer;
		}

		public IModel<PublicIdentity> model(PublicIdentity o)
		{
			return new CompoundPropertyModel<PublicIdentity>(new DaoDetachableModel(o));
		}
	}

}
