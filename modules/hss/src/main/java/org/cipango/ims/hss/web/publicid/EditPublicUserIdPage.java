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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

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
import org.cipango.ims.hss.web.util.UriValidator;

public class EditPublicUserIdPage extends PublicIdentityPage
{

	private static final Logger __log = Logger.getLogger(EditPublicUserIdPage.class);
	
	private String _key;
	private String _title;
	private String _privateIdKey;
	
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
		
		DaoDetachableModel model = new DaoDetachableModel(publicIdentity);
		
		if (isAdding()) {
			_title = getString(getPrefix() + ".add.title");
		} else {
			_title = getString(getPrefix() + ".edit.title", model);
		}
		
		Form form = new Form("form", new CompoundPropertyModel(model));
		add(form);
		form.add(new Label("title", publicIdentity == null ? "" : publicIdentity.getIdentity()));
		form.add(new RequiredTextField<String>("identity", String.class).add(new UriValidator()));
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
					
				}).setRequired(true));
		
		form.add(new DropDownChoice("implicitRegistrationSet",
				new LoadableDetachableModel() {
			
					@Override
					protected Object load()
					{
						List<ImplicitRegistrationSet> set = null;
						if (!isAdding())
						{
							PublicUserIdentity publicUserIdentity = (PublicUserIdentity) _dao.findById(_key);
							set = _implicitRegistrationSetDao.getImplicitRegistrationSet(
									publicUserIdentity.getSubscription().getId());
							if (publicUserIdentity.getImplicitRegistrationSet().getPublicIds().size() > 1)
								set.add(new ImplicitRegistrationSet());
						}
						else if (_privateIdKey != null)
						{
							PrivateIdentity privateIdentity = _privateIdentityDao.findById(_privateIdKey);
							if (privateIdentity != null)
							{
								set = _implicitRegistrationSetDao.getImplicitRegistrationSet(
										privateIdentity.getSubscription().getId());
								set.add(new ImplicitRegistrationSet());
							}
						}
						if (set == null)
						{
							set = new ArrayList<ImplicitRegistrationSet>();
							set.add(new ImplicitRegistrationSet());
						}
						return set;
					}
			
				},
				new ChoiceRenderer<ImplicitRegistrationSet>()
				{
					@Override
					public Object getDisplayValue(ImplicitRegistrationSet set)
					{
						if (set.getPublicIds().size() == 0)
							return "new set";
						
						StringBuilder sb = new StringBuilder();
						sb.append(set.getId());
						Iterator<String> it = set.getPublicIds().iterator();
						sb.append(" {");
						int index = 0;
						while (it.hasNext())
						{
							sb.append(it.next());
							if (it.hasNext())
								sb.append(", ");
							if (++index == 2)
							{
								sb.append("...");
								break;
							}
						}
						sb.append('}');
						return sb.toString();
					}
					
				}).setRequired(true));
		
		form.add(new Button("submit")
		{
			@Override
			public void onSubmit()
			{
				try
				{
					PublicUserIdentity publicIdentity = (PublicUserIdentity) getForm().getModelObject();

					if (publicIdentity.getImplicitRegistrationSet() == null)
					{
						ImplicitRegistrationSet implicitRegistrationSet = new ImplicitRegistrationSet();
						_implicitRegistrationSetDao.save(implicitRegistrationSet);
						publicIdentity.setImplicitRegistrationSet(implicitRegistrationSet);
					}
					else if (publicIdentity.getImplicitRegistrationSet().getId() == null)
						_implicitRegistrationSetDao.save(publicIdentity.getImplicitRegistrationSet());
					
					
					if (_privateIdKey != null)
					{
						PrivateIdentity privateIdentity = _privateIdentityDao.findById(_privateIdKey);
						if (privateIdentity != null)
						{
							privateIdentity.addPublicId(publicIdentity);
						}
					}

					_dao.save(publicIdentity);
					getSession().info(getString("modification.success"));
					
					getCxManager().identityUpdated(publicIdentity);
					
					if (!publicIdentity.getIdentity().equals(_key))
						setResponsePage(EditPublicUserIdPage.class, new PageParameters("id=" + publicIdentity.getIdentity()));
					
				}
				catch (Exception e)
				{
					__log.debug("Failed to apply edit", e);
					getSession().error(getString(getPrefix() + ".error.duplicate", getForm().getModel()));
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
			setContextMenu(new ContextPanel((PublicUserIdentity) publicIdentity));
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

