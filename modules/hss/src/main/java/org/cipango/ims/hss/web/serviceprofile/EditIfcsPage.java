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

import org.apache.wicket.Component;
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
import org.cipango.ims.hss.db.IfcDao;
import org.cipango.ims.hss.model.InitialFilterCriteria;
import org.cipango.ims.hss.model.ServiceProfile;


public class EditIfcsPage extends ServiceProfilePage
{


	private String _key;
	@SpringBean
	private IfcDao _ifcDao;
	
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
		
		form.add(new AjaxFallbackButton("join", form) {
			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form1)
			{
				apply(target, form1, form1.get("available"), false);
			}
		});
		form.add(new ListMultipleChoice(
				"available",  
				new Model(new ArrayList()),
				new Model((Serializable) _dao.getAvailableIfc(serviceProfile))));
		
		
		form.add(new AjaxFallbackButton("leave", form) {
			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form1)
			{
				apply(target, form1, form1.get("used"), true);			
			}
		});
		
		List used = new ArrayList();
		if (serviceProfile != null)
		{
			Iterator<InitialFilterCriteria> it = serviceProfile.getIfcs().iterator();
			while (it.hasNext()) {
				used.add(it.next().getName());
			}	
		}
		form.add(new ListMultipleChoice(
				"used", 
				new Model(new ArrayList()),
				new Model((Serializable) used)));
		
	}
	
	
	@SuppressWarnings("unchecked")
	private void apply(AjaxRequestTarget target, Form<?> form1, Component component, boolean remove)
	{
		Iterator it = ((List) component.getDefaultModelObject()).iterator();
		List choosen = ((AbstractChoice) component).getChoices();
		ServiceProfile profile = _dao.findById(_key);

		ListView ifcs = (ListView) getPage().get("contextMenu:ifcs");
		Collection<String> contextModel = (Collection<String>) ifcs.getDefaultModelObject();
		while (it.hasNext()) {
			String ifcName = (String) it.next();
			InitialFilterCriteria ifc = _ifcDao.findById(ifcName);
			if (remove)
			{
				profile.removeIfc(ifc);
				contextModel.remove(ifcName);
				((AbstractChoice) form1.get("available")).getChoices().add(ifcName);
			}
			else
			{
				profile.addIfc(_ifcDao.findById(ifcName));
				contextModel.add(ifcName);
				((AbstractChoice) form1.get("used")).getChoices().add(ifcName);
			}
			choosen.remove(ifcName);
			it.remove();
		}
		_dao.save(profile);
				
		if (target != null)
		{
			target.addComponent(form1);
			target.addComponent(getPage().get("contextMenu"));
		}
	}
	
	@Override
	public String getTitle() {
		return getString(getPrefix() + ".ifcs.title", new DaoDetachableModel(_key));
	}
}
