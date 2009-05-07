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

import org.apache.log4j.Logger;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.collections.MicroMap;
import org.apache.wicket.util.string.interpolator.MapVariableInterpolator;
import org.cipango.ims.hss.db.ImplicitRegistrationSetDao;
import org.cipango.ims.hss.db.PrivateIdentityDao;
import org.cipango.ims.hss.db.ServiceProfileDao;
import org.cipango.ims.hss.model.ImplicitRegistrationSet;
import org.cipango.ims.hss.model.PSI;
import org.cipango.ims.hss.model.PrivateIdentity;
import org.cipango.ims.hss.model.PublicIdentity;
import org.cipango.ims.hss.model.PublicUserIdentity;
import org.cipango.ims.hss.model.ServiceProfile;
import org.cipango.ims.hss.model.PublicIdentity.IdentityType;

public class EditPublicUserIdPage extends PublicIdentityPage
{

	private String _key;
	private DaoDetachableModel _model;
	private String _privateIdKey;
	
	private static final Logger __log = Logger.getLogger(EditPublicUserIdPage.class);
	
	@SpringBean
	private PrivateIdentityDao _privateIdentityDao;
	
	@SpringBean
	private ServiceProfileDao _serviceProfileDao;
	
	@SpringBean
	private ImplicitRegistrationSetDao _implicitRegistrationSetDao;

	@SuppressWarnings("unchecked")
	public EditPublicUserIdPage(PageParameters pageParameters)
	{
		_key = pageParameters.getString("id");
		_privateIdKey = pageParameters.getString("privateId");
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
		
		if (publicIdentity instanceof PSI)
		{
			setResponsePage(EditPsiPage.class, new PageParameters("id=" + _key));
			return;
		}
		
		_model = new DaoDetachableModel(publicIdentity);
		
		add(new Label("title", getTitle()));
		Form form = new Form("form", new CompoundPropertyModel(_model));
		add(form);
		form.add(new RequiredTextField<String>("identity", String.class));
		form.add(new CheckBox("barred"));

		form.add(new DropDownChoice("identityType",
				Arrays.asList(new Short[]{IdentityType.PUBLIC_USER_IDENTITY, IdentityType.WILDCARDED_IMPU}),
				new ChoiceRenderer<Short>()
		{
			@Override
			public Object getDisplayValue(Short id)
			{
				return IdentityType.toString(id);
			}
			
		}));
		form.add(new TextField("displayName", String.class));
		form.add(new Label("implicitRegistrationSet.stateAsString"));
		
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
		
		form.add(new CheckBox("anotherUser", new Model<Boolean>()).setVisible(isAdding()));

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
				goToBackPage(PublicIdBrowserPage.class);
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
			setContextMenu(new ContextPanel((PublicUserIdentity) publicIdentity));
	}

	@SuppressWarnings("unchecked")
	protected void apply(Form form)
	{
		try
		{
			PublicUserIdentity publicIdentity = (PublicUserIdentity) form.getModelObject();

			if (publicIdentity.getImplicitRegistrationSet() == null)
			{
				ImplicitRegistrationSet implicitRegistrationSet = new ImplicitRegistrationSet();
				_implicitRegistrationSetDao.save(implicitRegistrationSet);
				publicIdentity.setImplicitRegistrationSet(implicitRegistrationSet);
			}
			
			_dao.save(publicIdentity);
			if (_privateIdKey != null)
			{
				PrivateIdentity privateIdentity = _privateIdentityDao.findById(_privateIdKey);
				if (privateIdentity != null)
				{
					_dao.save(privateIdentity.addPublicId(publicIdentity));
				}
			}

			getSession().info(getString("modification.success"));
		}
		catch (Exception e)
		{
			__log.debug("Failed to apply edit", e);
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
		if (isAdding()) {
			return getString(getPrefix() + ".add.title");
		} else {
			return getString(getPrefix() + ".edit.title", _model);
		}
	}
}
