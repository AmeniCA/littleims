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

import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.wicket.Component;
import org.apache.wicket.PageParameters;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.collections.MicroMap;
import org.apache.wicket.util.string.interpolator.MapVariableInterpolator;
import org.cipango.ims.hss.db.ApplicationServerDao;
import org.cipango.ims.hss.db.ServiceProfileDao;
import org.cipango.ims.hss.model.ApplicationServer;
import org.cipango.ims.hss.model.PSI;
import org.cipango.ims.hss.model.PublicIdentity;
import org.cipango.ims.hss.model.PublicUserIdentity;
import org.cipango.ims.hss.model.ServiceProfile;
import org.cipango.ims.hss.model.PublicIdentity.IdentityType;
import org.cipango.ims.hss.web.util.AjaxFallbackButton;

public class EditPsiPage extends PublicIdentityPage
{

	private String _key;
	private String _title;
	
	private static final Logger __log = Logger.getLogger(EditPsiPage.class);
		
	@SpringBean
	private ServiceProfileDao _serviceProfileDao;
	
	@SpringBean
	private ApplicationServerDao _applicationServerDao;
	

	@SuppressWarnings("unchecked")
	public EditPsiPage(PageParameters pageParameters)
	{
		_key = pageParameters.getString("id");
		PublicIdentity publicIdentity = null;
		if (_key != null)
		{
			publicIdentity = _dao.findById(_key);
			if (publicIdentity == null)
			{
				error(MapVariableInterpolator.interpolate(getString(getPrefix() + ".error.notFound"),
						new MicroMap("identity", _key)));
				_key = null;
			}
		}
		
		if (publicIdentity instanceof PublicUserIdentity)
		{
			setResponsePage(EditPublicUserIdPage.class, new PageParameters("id=" + _key));
			return;
		}
		

		IModel model = new DaoDetachableModel(publicIdentity, true);
		
		if (isAdding()) {
			_title = getString(getPrefix() + ".add.title");
		} else {
			_title = getString(getPrefix() + ".edit.psi.title", model);
		}

		
		add(new Label("title", _title));
		Form form = new Form("form", new CompoundPropertyModel(model));
		add(form);
		form.add(new RequiredTextField<String>("identity", String.class));
		form.add(new RequiredTextField("privateServiceIdentity", String.class));
		form.add(new CheckBox("psiActivation"));

		form.add(new DropDownChoice("identityType",
				Arrays.asList(new Short[]{IdentityType.DISTINCT_PSI, IdentityType.WILDCARDED_PSI}),
				new ChoiceRenderer<Short>()
		{
			@Override
			public Object getDisplayValue(Short id)
			{
				return IdentityType.toString(id);
			}
			
		}));
		form.add(new TextField("displayName", String.class));
		form.add(new Label("stateAsString"));
	
		form.add(new DropDownChoice("applicationServer",
				new LoadableDetachableModel() {
			
					@Override
					protected Object load()
					{
						List list = _applicationServerDao.getAll();
						list.add(0, null);
						return list;
					}
			
				},
				new ChoiceRenderer<ApplicationServer>()
				{
					@Override
					public Object getDisplayValue(ApplicationServer as)
					{
						if (as == null)
							return getString("none");
						else
							return as.getName();
					}
					
				}));
		
		form.add(new DropDownChoice("serviceProfile",
				new LoadableDetachableModel() {
			
					@Override
					protected Object load()
					{
						return _serviceProfileDao.getAllServiceProfile();
					}
			
				},
				new ChoiceRenderer<ServiceProfile>()
				{
					@Override
					public Object getDisplayValue(ServiceProfile profile)
					{
						return profile.getName();
					}
					
				}));
		

		form.add(new AjaxFallbackButton("submit", form)
		{
			@Override
			protected void doSubmit(AjaxRequestTarget target, Form<?> form)
					throws Exception
			{
				try
				{
					PSI psi = (PSI) form.getModelObject();
			
					_dao.save(psi);

					getSession().info(getString("modification.success"));
					
					if (target != null)
					{
						Component contextMenu = new PsiContextPanel(psi);
						getPage().get("contextMenu").replaceWith(contextMenu);
						target.addComponent(contextMenu);
					}
				}
				catch (Exception e)
				{
					__log.debug("Failed to apply edit", e);
					getSession().error(getString(getPrefix() + ".error.duplicate", form.getModel()));
				}
			}
		});

		form.add(new Button("cancel")
		{
			@Override
			public void onSubmit()
			{
				getSession().info(getString("modification.cancel"));
				goToBackPage(PublicIdBrowserPage.class);
			}
		}.setDefaultFormProcessing(false));

		if (publicIdentity != null)
			setContextMenu(new PsiContextPanel((PSI) publicIdentity));
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

