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

import java.util.Arrays;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.RadioChoice;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.collections.MicroMap;
import org.apache.wicket.util.string.interpolator.MapVariableInterpolator;
import org.cipango.ims.hss.db.ApplicationServerDao;
import org.cipango.ims.hss.model.ApplicationServer;
import org.cipango.ims.hss.model.InitialFilterCriteria;
import org.cipango.ims.hss.model.InitialFilterCriteria.ProfilePartIndicator;
import org.cipango.ims.hss.web.spt.EditSptsPage;

public class EditIfcPage extends IfcPage
{

	private String _key;
	private String _title;
	@SpringBean
	private ApplicationServerDao _applicationServerDao;
	
	@SuppressWarnings("unchecked")
	public EditIfcPage(PageParameters pageParameters)
	{
		_key = pageParameters.getString("id");
		InitialFilterCriteria ifc = null;
		if (_key != null)
		{
			ifc = _dao.findById(_key);
			if (ifc == null)
			{
				error(MapVariableInterpolator.interpolate(getString(getPrefix() + ".error.notFound"),
						new MicroMap("id", _key)));
				_key = null;
			}
		}
		
		IModel model = new DaoDetachableModel(ifc);
		
		if (isAdding()) {
			_title = getString(getPrefix() + ".add.title");
		} else {
			_title = getString(getPrefix() + ".edit.title", model);
		}
		
		add(new Label("title", getTitle()));
		Form form = new Form("form", new CompoundPropertyModel(model));
		add(form);
		form.add(new RequiredTextField<String>("name", String.class));
		form.add(new RequiredTextField<Integer>("priority", Integer.class));
		form.add(new RadioChoice("profilePartIndicator",
				Arrays.asList(new Short[]{0,1, null}),
				new ChoiceRenderer<Short>()
		{
			@Override
			public Object getDisplayValue(Short id)
			{
				if (id == null)
					return getString("ifc.profilePartIndicator.none");
				return ProfilePartIndicator.toString(id);
			}
			
		}));
		
		form.add(new RadioChoice("conditionTypeCnf",
				Arrays.asList(new Boolean[]{Boolean.FALSE,Boolean.TRUE}),
				new ChoiceRenderer<Boolean>()
		{
			@Override
			public Object getDisplayValue(Boolean cnf)
			{
				if (cnf)
					return getString("ifc.conditionTypeCnf");
				else
					return getString("ifc.conditionTypeDnf");
			}
			
		}));
	
		form.add(new DropDownChoice("applicationServer",
				new LoadableDetachableModel()
				{
					@Override
					protected Object load()
					{
						return _applicationServerDao.getAll();
					}
					
				},
				new ChoiceRenderer<ApplicationServer>("name", "id")));
		
		form.add(new Button("submit")
		{
			@Override
			public void onSubmit()
			{
				apply(getForm());
			}
		});
		form.add(new Button("ok")
		{
			@Override
			public void onSubmit()
			{
				apply(getForm());
				goToBackPage(IfcBrowserPage.class);
			}
		});
		form.add(new Button("cancel")
		{
			@Override
			public void onSubmit()
			{
				getSession().info(getString("modification.cancel"));
				goToBackPage(IfcBrowserPage.class);
			}
		}.setDefaultFormProcessing(false));

		if (ifc != null)
			setContextMenu(new ContextPanel(ifc));
	}

	@SuppressWarnings("unchecked")
	protected void apply(Form form)
	{
		try
		{	
			InitialFilterCriteria ifc = (InitialFilterCriteria) form.getModelObject();
			ApplicationServer as = (ApplicationServer) form.get("applicationServer").getDefaultModelObject();
			ifc.setApplicationServer(as);
			_dao.save(ifc);

			getSession().info(getString("modification.success"));
		}
		catch (Exception e)
		{
			getSession().error(getString(getPrefix() + ".error.duplicate", form.getModel()));
		}

	}

	private boolean isAdding()
	{
		return _key == null;
	}

	@Override
	public String getTitle()
	{
		return _title;
	}
}

