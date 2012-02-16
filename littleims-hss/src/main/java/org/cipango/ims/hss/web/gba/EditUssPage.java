// ========================================================================
// Copyright 2010 NEXCOM Systems
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
package org.cipango.ims.hss.web.gba;

import java.util.Arrays;
import java.util.Collections;

import org.apache.wicket.Component;
import org.apache.wicket.PageParameters;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.collections.MicroMap;
import org.apache.wicket.util.string.interpolator.MapVariableInterpolator;
import org.cipango.ims.hss.db.NafGroupDao;
import org.cipango.ims.hss.db.PrivateIdentityDao;
import org.cipango.ims.hss.model.NafGroup;
import org.cipango.ims.hss.model.PrivateIdentity;
import org.cipango.ims.hss.model.uss.Uss;
import org.cipango.ims.hss.web.privateid.ContextPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EditUssPage extends UssPage
{

	private static final Logger __log = LoggerFactory.getLogger(EditUssPage.class);
	
	private Long _key;
	private String _privateIdKey;
	private String _title;
	
	@SpringBean
	private PrivateIdentityDao _privateIdentityDao;
	
	@SpringBean
	private NafGroupDao _nafGroupDao;

	@SuppressWarnings("unchecked")
	public EditUssPage(PageParameters pageParameters)
	{
		super(pageParameters);
		String sKey = pageParameters.getString("id");
		PrivateIdentity privateIdentity = null;
		if (sKey != null)
			_key = Long.decode(sKey);
		else
		{
			_privateIdKey = pageParameters.getString("privateId");
			privateIdentity = _privateIdentityDao.findById(_privateIdKey);
		}
			
		Uss uss = null;
		if (_key != null)
		{
			uss = _dao.findById(_key);
			if (uss == null)
			{
				error(MapVariableInterpolator.interpolate(getString(getPrefix() + ".error.notFound"),
						new MicroMap("identity", _key)));
				_key = null;
			}
			else
			{
				privateIdentity = uss.getPrivateIdentity();
				_privateIdKey = privateIdentity.getIdentity();
			}
		}
		
		if (uss == null)
		{
			uss = new Uss();
		}
		
		DaoDetachableModel model = new DaoDetachableModel(uss);
		
		if (isAdding()) {
			_title = MapVariableInterpolator.interpolate(getString(getPrefix() + ".add.title"),
					new MicroMap("identity", _privateIdKey));
		} else {
			_title = getString(getPrefix() + ".edit.title", model);
		}
		
		Form form = new Form("form", new CompoundPropertyModel(model));
		add(form);
                
		form.add(new Label("title", String.valueOf(uss.getId())));
        form.add(new DropDownChoice("nafGroup",
        		new LoadableDetachableModel()
				{
					@Override
					protected Object load()
					{
						return _nafGroupDao.getAllGroups();
					}
				},
				new ChoiceRenderer<NafGroup>()
		{
			@Override
			public Object getDisplayValue(NafGroup nafGroup)
			{
				return nafGroup.getName();
			}
			
		}));
        
        		
        DropDownChoice typeChoice = new DropDownChoice("type",
				Arrays.asList(new Integer[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12}),
				new ChoiceRenderer<Integer>()
		{
			@Override
			public Object getDisplayValue(Integer type)
			{
				return Uss.getTypeAsString(type);
			}
			
		});
        form.add(typeChoice);
        
        typeChoice.setRequired(true);
        typeChoice.add(new AjaxFormComponentUpdatingBehavior("onChange")
		{
			
			@Override
			protected void onUpdate(AjaxRequestTarget target)
			{
				final Integer type = (Integer) getPage().get("form:type").getDefaultModelObject();
				DropDownChoice flagSelect = (DropDownChoice) getPage().get("form:flagSelect");
				Component flagInput =  getPage().get("form:flagInput");

				Integer flag = getFlag2(flagInput, flagSelect);
				
				if (Uss.getDefinedFlagList(type) != null)
				{
					flagInput.setVisible(false);
					flagSelect.setChoiceRenderer(new ChoiceRenderer<Integer>()
							{
								@Override
								public Object getDisplayValue(Integer flag)
								{
									return Uss.getFlagAsString(type, flag);
								}
								
							});
				    flagSelect.setChoices(Arrays.asList(Uss.getDefinedFlagList(type)));
					flagSelect.setVisible(true);
				}
				else
				{
					flagInput.setVisible(true);
					flagInput.setDefaultModelObject(flag);
					flagSelect.setVisible(false);
				}
				target.addComponent(flagInput);
				target.addComponent(flagSelect);
			}
		});
        
        final Integer type = uss.getType();
        
        Component flagInput = new TextField<Integer>("flagInput",new Model<Integer>(uss.getFlag()), Integer.class);
        form.add(flagInput);
        flagInput.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        
        DropDownChoice<Integer> flagSelect = new DropDownChoice<Integer>("flagSelect",
    			new Model<Integer>(uss.getFlag()), Collections.EMPTY_LIST);
        form.add(flagSelect);
		flagSelect.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        
        if (Uss.getDefinedFlagList(type) != null)
        {
        	flagSelect.setChoiceRenderer(new ChoiceRenderer<Integer>()
			{
				@Override
				public Object getDisplayValue(Integer flag)
				{
					return Uss.getFlagAsString(type, flag);
				}
				
			});
        	flagSelect.setChoices(Arrays.asList(Uss.getDefinedFlagList(type)));
        	flagInput.setVisible(false);
        }
        else
        {
            flagSelect.setVisible(false);
        }
		
		form.add(new Button("submit")
		{
			@Override
			public void onSubmit()
			{
				try
				{
					Uss uss = (Uss) getForm().getModelObject();
					
					if (_privateIdKey != null)
					{
						PrivateIdentity privateIdentity = _privateIdentityDao.findById(_privateIdKey);
						if (privateIdentity != null)
							uss.setPrivateIdentity(privateIdentity);
					}
					uss.setFlag(getFlag2(getForm().get("flagInput"), getForm().get("flagSelect")));
					
					_dao.save(uss);
									
					getSession().info(getString("modification.success"));
					setResponsePage(UssBrowserPage.class, 
								new PageParameters("privateId=" + uss.getPrivateIdentity().getIdentity()));
				}
				catch (Exception e)
				{
					__log.debug("Failed to apply edit", e);
					getSession().error(getString("uss.error", getForm().getModel()));
				}
			}
		});

		form.add(new Button("cancel")
		{
			@Override
			public void onSubmit()
			{
				getSession().info(getString("modification.cancel"));
				setResponsePage(UssBrowserPage.class, 
						new PageParameters("privateId=" + _privateIdKey));
			}
		}.setDefaultFormProcessing(false));
		
		form.add(new Button("delete")
		{
			@Override
			public void onSubmit()
			{
				Uss uss = _dao.findById(_key);
				setResponsePage(UssBrowserPage.class, 
						new PageParameters("privateId=" + _privateIdKey));
				_dao.delete(uss);
				getSession().info(getString(getPrefix() + ".delete.done", new DaoDetachableModel(uss)));
				
			}
		}.setDefaultFormProcessing(false).setVisible(!isAdding()));

		if (privateIdentity != null)
		{
			setContextMenu(new ContextPanel(privateIdentity));
		}
			
	}
	
	private Integer getFlag2(Component flagInput, Component flagSelect)
	{
		if (flagInput.isVisible())
			return (Integer) flagInput.getDefaultModelObject();
		return (Integer) flagSelect.getDefaultModelObject();
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

