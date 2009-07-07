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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.PageParameters;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxFallbackButton;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.AbstractChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.ListMultipleChoice;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.collections.MicroMap;
import org.apache.wicket.util.string.interpolator.MapVariableInterpolator;
import org.cipango.ims.hss.HssException;
import org.cipango.ims.hss.db.IfcDao;
import org.cipango.ims.hss.model.InitialFilterCriteria;
import org.cipango.ims.hss.model.ServiceProfile;


public class EditIfcsPage extends ServiceProfilePage
{


	private String _key;
	@SpringBean
	private IfcDao _ifcDao;
	
	private enum Action 
	{
		REMOVE_FROM_IFC,
		REMOVE_FROM_SHARED,
		ADD_TO_IFC,
		ADD_TO_SHARED,
		MOVE_TO_IFC,
		MOVE_TO_SHARED;
	}
	
	@SuppressWarnings("unchecked")
	public EditIfcsPage(PageParameters pageParameters) {
		_key = pageParameters.getString("id");
		ServiceProfile serviceProfile = _dao.findById(_key);

		if (serviceProfile == null)
		{
			error(MapVariableInterpolator.interpolate(getString(getPrefix() + ".error.notFound"),
					new MicroMap("identity", _key)));
			_key = null;
		}
		else
			setContextMenu(new ContextPanel(serviceProfile));
					
		add(new Label("title", getTitle()));
		
		Form form = new Form("form");
		form.setOutputMarkupId(true);
		add(form);

		addAvailable(form, serviceProfile);
		addUsed(form, serviceProfile);
		addShared(form, serviceProfile);
		
	}
	
	@SuppressWarnings({"unchecked" })
	private void addAvailable(Form form, ServiceProfile serviceProfile)
	{
		form.add(new AjaxFallbackButton("useAsIFC", form) {
			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form1)
			{
				apply(target, form1, Action.ADD_TO_IFC);
			}
		});
		form.add(new AjaxFallbackButton("useAsShared", form) {
			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form1)
			{
				apply(target, form1, Action.ADD_TO_SHARED);
			}
		});
		form.add(new ListMultipleChoice(
				"available",  
				new Model(new ArrayList()),
				new Model((Serializable) _dao.getAvailableIfc(serviceProfile))));
	}
	
	@SuppressWarnings({"unchecked" })
	private void addUsed(Form form, ServiceProfile serviceProfile)
	{
		form.add(new AjaxFallbackButton("leave", form) {
			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form1)
			{
				apply(target, form1, Action.REMOVE_FROM_IFC);			
			}
		});
		
		form.add(new AjaxFallbackButton("moveToShared", form) {
			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form1)
			{
				apply(target, form1, Action.MOVE_TO_SHARED);			
			}
		});
		
		List used = new ArrayList();
		if (serviceProfile != null)
		{
			Iterator<InitialFilterCriteria> it = serviceProfile.getIfcs(false).iterator();
			while (it.hasNext()) {
				used.add(it.next().getName());
			}	
		}
		form.add(new ListMultipleChoice(
				"used", 
				new Model(new ArrayList()),
				new Model((Serializable) used)));
	}
	
	@SuppressWarnings({"unchecked" })
	private void addShared(Form form, ServiceProfile serviceProfile)
	{
		List shared = new ArrayList();
		if (serviceProfile != null)
		{
			Iterator<InitialFilterCriteria> it = serviceProfile.getIfcs(true).iterator();
			while (it.hasNext()) {
				shared.add(it.next().getName());
			}	
		}
		form.add(new ListMultipleChoice(
				"shared", 
				new Model(new ArrayList()),
				new Model((Serializable) shared)));
		
		form.add(new AjaxFallbackButton("moveToIFC", form) {
			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form1)
			{
				apply(target, form1, Action.MOVE_TO_IFC);
			}
		});
		
		form.add(new AjaxFallbackButton("sharedLeave", form) {
			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form1)
			{
				apply(target, form1, Action.REMOVE_FROM_SHARED);
			}
		});
	}
	
	@SuppressWarnings("unchecked")
	private void apply(AjaxRequestTarget target, Form<?> form1, Action action)
	{
		AbstractChoice available = (AbstractChoice) form1.get("available");
		AbstractChoice used = (AbstractChoice) form1.get("used");
		AbstractChoice shared = (AbstractChoice) form1.get("shared");
		
		Iterator it; 
		List choosen;
		switch (action)
		{
		case ADD_TO_IFC:
		case ADD_TO_SHARED:
			it = ((List) available.getDefaultModelObject()).iterator();
			choosen = available.getChoices();
			break;
		case MOVE_TO_SHARED:
		case REMOVE_FROM_IFC:
			it = ((List) used.getDefaultModelObject()).iterator();
			choosen = used.getChoices();
			break;
		case MOVE_TO_IFC:
		case REMOVE_FROM_SHARED:
			it = ((List) shared.getDefaultModelObject()).iterator();
			choosen = shared.getChoices();
			break;
		default:
			throw new IllegalStateException("Unknown action " + action);
		}
		
		ServiceProfile profile = _dao.findById(_key);

		ListView ifcs = (ListView) getPage().get("contextMenu:ifcs");
		Collection<String> contextModel = (Collection<String>) ifcs.getDefaultModelObject();
		while (it.hasNext()) {
			String ifcName = (String) it.next();
			InitialFilterCriteria ifc = _ifcDao.findById(ifcName);
			try
			{
				switch (action)
				{
				case ADD_TO_IFC:
					profile.addIfc(ifc, false);
					used.getChoices().add(ifcName);
					contextModel.add(ifcName);
					break;
				case ADD_TO_SHARED:
					profile.addIfc(ifc, true);
					shared.getChoices().add(ifcName);
					contextModel.add(ifcName);
					break;
				case MOVE_TO_IFC:
					profile.moveIfc(ifc, false);
					used.getChoices().add(ifcName);
					break;
				case MOVE_TO_SHARED:
					profile.moveIfc(ifc, true);
					shared.getChoices().add(ifcName);
					break;
				case REMOVE_FROM_IFC:
				case REMOVE_FROM_SHARED:
					_dao.unlink(profile, ifc);
					contextModel.remove(ifcName);
					available.getChoices().add(ifcName);
					break;
				default:
					break;
				}
				choosen.remove(ifcName);
			}
			catch (HssException e) 
			{
				error(e.getMessage());
			}
			it.remove();
		}
		_dao.save(profile);
		
		getCxManager().profileUpdated(profile);
				
		if (target != null)
		{
			target.addComponent(form1);
			target.addComponent(getPage().get("feedback"));
			target.addComponent(getPage().get("contextMenu"));
			target.addComponent(getPage().get("pprPanel").setVisible(true));
		}
	}
	
	
	@Override
	public String getTitle() {
		return getString(getPrefix() + ".ifcs.title", new DaoDetachableModel(_key));
	}
}
