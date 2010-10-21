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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.wicket.PageParameters;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
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
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.validator.AbstractValidator;
import org.apache.wicket.validation.validator.MinimumValidator;
import org.cipango.ims.hss.HssException;
import org.cipango.ims.hss.db.ApplicationServerDao;
import org.cipango.ims.hss.db.ServiceProfileDao;
import org.cipango.ims.hss.model.ApplicationServer;
import org.cipango.ims.hss.model.InitialFilterCriteria;
import org.cipango.ims.hss.model.ServiceProfile;
import org.cipango.ims.hss.model.InitialFilterCriteria.ProfilePartIndicator;
import org.cipango.ims.hss.model.spt.SPT;
import org.cipango.ims.hss.web.spt.EditSptsPage;
import org.cipango.ims.oam.util.AjaxFallbackButton;

public class EditIfcPage extends IfcPage
{

	private String _key;
	private boolean _copy;
	private String _title;
	@SpringBean
	private ApplicationServerDao _applicationServerDao;
	@SpringBean
	private ServiceProfileDao _serviceProfileDao;
	
	private String _serviceProfileKey;
	
	private static final Logger __log = Logger.getLogger(EditIfcPage.class);
	
	@SuppressWarnings("unchecked")
	public EditIfcPage(PageParameters pageParameters)
	{
		super(pageParameters);
		_key = pageParameters.getString("id");
		_copy = pageParameters.getBoolean("copy");
		_serviceProfileKey = pageParameters.getString("serviceProfile");

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
		
		IModel model = new DaoDetachableModel(ifc, _copy);
		
		if (_copy)
			ifc.setName(getCopyName(ifc.getName()));	
		
		if (isAdding()) {
			_title = getString(getPrefix() + ".add.title");
		} else {
			_title = getString(getPrefix() + ".edit.title", model);
		}
		
		Form form = new Form("form", new CompoundPropertyModel(model));
		add(form);
		form.add(new Label("title", isAdding() ? "" : ifc.getName()));
		form.add(new RequiredTextField<String>("name", String.class));
		form.add(new RequiredTextField<Integer>("priority", Integer.class)
				.add(new MinimumValidator<Integer>(0))
				.add(new PriorityValidator())
				.setRequired(true));
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
			
		}).setRequired(true));
	
		form.add(new DropDownChoice("applicationServer",
				new LoadableDetachableModel()
				{
					@Override
					protected Object load()
					{
						return _applicationServerDao.getAll();
					}
					
				},
				new ChoiceRenderer<ApplicationServer>("name", "id")).setRequired(true));
		

		form.add(new AjaxFallbackButton("ok", form)
		{
			@Override
			protected void doSubmit(AjaxRequestTarget target, Form<?> form1)
			{
				boolean adding = isAdding();
				try
				{	
					InitialFilterCriteria ifc = (InitialFilterCriteria) getForm().getModelObject();
					ApplicationServer as = (ApplicationServer) getForm().get("applicationServer").getDefaultModelObject();
					ifc.setApplicationServer(as);
					
					if (_copy)
					{
						InitialFilterCriteria template = _dao.findById(_key);
						for (SPT spt: template.getSpts())
						{
							ifc.addSpt(spt.clone());
						}
					}
					_key = ifc.getName();
					_dao.save(ifc);
					
					if (_serviceProfileKey != null)
					{
						ServiceProfile profile = _serviceProfileDao.findById(_serviceProfileKey);
						if (profile != null)
						{
							try 
							{
								profile.addIfc(ifc, false);
							}
							catch (HssException e) {
								error(e.getMessage());
								return;
							}
						}
					}
					
					getCxManager().ifcUpdated(ifc);
					
					if (target != null)
					{
						target.addComponent(getPage().get("pprPanel").setVisible(true));
					}
					
					getSession().info(getString("modification.success"));
				}
				catch (Exception e)
				{
					__log.debug(e.getMessage(), e);
					getSession().error(getString(getPrefix() + ".error.duplicate", getForm().getModel()));
				}
				if (adding)
					setResponsePage(EditSptsPage.class, new PageParameters("id=" + _key));
				else
					setResponsePage(EditIfcPage.class, new PageParameters("id=" + _key));
			}
		});
		form.add(new AjaxFallbackButton("cancel", form)
		{
			@Override
			protected void doSubmit(AjaxRequestTarget target, Form<?> form1)
			{
				getSession().info(getString("modification.cancel"));
			}
		}.setDefaultFormProcessing(false));

		if (ifc != null)
			setContextMenu(new ContextPanel(ifc));
	}
	


	private boolean isAdding()
	{
		return _key == null || _copy;
	}

	@Override
	public String getTitle()
	{
		return _title;
	}
	
	class PriorityValidator extends AbstractValidator<Integer>
	{

		private String _ifcs;
		@Override
		protected void onValidate(IValidatable<Integer> validatable)
		{
			if (_key == null)
				return;
			
			List<InitialFilterCriteria> list = 
				_dao.getIfcsWithSamePriority(_dao.findById(_key), validatable.getValue());
			if (!list.isEmpty())
			{
				if (list.size() == 1)
					_ifcs = list.get(0).getName();
				else
				{
					StringBuilder sb = new StringBuilder();
					Iterator<InitialFilterCriteria> it = list.iterator();
					while (it.hasNext())
					{
						InitialFilterCriteria ifc = it.next();
						sb.append(ifc.getName());
						if (it.hasNext())
							sb.append(", ");
					}
					_ifcs = sb.toString();
				}
				error(validatable);
			}
		}
		
		@Override
		protected String resourceKey()
		{
			return "validator.ifc.priority";
		}
		
		@Override
		protected Map<String, Object> variablesMap(IValidatable<Integer> validatable)
		{
			Map<String, Object> map =  super.variablesMap(validatable);
			map.put("ifc", _ifcs);
			return map;
		}
	}
}

