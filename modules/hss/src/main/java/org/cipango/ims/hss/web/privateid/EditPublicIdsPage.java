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
package org.cipango.ims.hss.web.privateid;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.wicket.Component;
import org.apache.wicket.PageParameters;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.AbstractChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.ListMultipleChoice;
import org.apache.wicket.markup.repeater.RefreshingView;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.collections.MicroMap;
import org.apache.wicket.util.string.interpolator.MapVariableInterpolator;
import org.cipango.ims.hss.db.PublicIdentityDao;
import org.cipango.ims.hss.model.ImplicitRegistrationSet;
import org.cipango.ims.hss.model.PrivateIdentity;
import org.cipango.ims.hss.model.PublicUserIdentity;
import org.cipango.ims.hss.web.util.AjaxFallbackButton;


public class EditPublicIdsPage extends PrivateIdentityPage
{


	private String _key;
	@SpringBean
	private PublicIdentityDao _publicIdentityDao;
	
	@SuppressWarnings("unchecked")
	public EditPublicIdsPage(PageParameters pageParameters) {
		_key = pageParameters.getString("id");
		PrivateIdentity privateIdentity = _dao.findById(_key);

		if (privateIdentity == null)
		{
			error(MapVariableInterpolator.interpolate(getString(getPrefix() + ".error.notFound"),
					new MicroMap("identity", _key)));
			_key = null;
		}
		else
			setContextMenu(new ContextPanel(privateIdentity));
					
		add(new Label("title", getTitle()));
		
		Form form = new Form("form");
		add(form);
		form.setVisible(privateIdentity != null);
		form.setOutputMarkupId(true);
		
		form.add(new AjaxFallbackButton("join.noSub", form) {
			@Override
			protected void doSubmit(AjaxRequestTarget target, Form<?> form1)
			{
				apply(target, form1, form1.get("publics.available.noSub"), false);
			}
		});
		form.add(new ListMultipleChoice(
				"publics.available.noSub",  
				new Model(new ArrayList()),
				new LoadableDetachableModel() {
					@Override
					protected List load() {
						return _dao.getAvalaiblePublicIdsNoPrivate();
					}
				}));
		
		form.add(new AjaxFallbackButton("join", form) {
			@Override
			protected void doSubmit(AjaxRequestTarget target, Form<?> form1)
			{
				apply(target, form1, form1.get("publics.available.sub"), false);
			}
		});
		form.add(new ListMultipleChoice(
				"publics.available.sub",  
				new Model(new ArrayList()),
				new LoadableDetachableModel() {
					@Override
					protected List load() {
						return _dao.getAvalaiblePublicIds(_dao.findById(_key));
					}
				}));
		
		
		form.add(new AjaxFallbackButton("leave", form) {
			@Override
			protected void doSubmit(AjaxRequestTarget target, Form<?> form1)
			{
				apply(target, form1, form1.get("publics"), true);			
			}
		});
		
		List publics = new ArrayList();
		if (privateIdentity != null)
		{
			Iterator<PublicUserIdentity> it = privateIdentity.getPublicIdentities().iterator();
			while (it.hasNext()) {
				publics.add(it.next().getIdentity());
			}
		}
		form.add(new ListMultipleChoice(
				"publics", 
				new Model(new ArrayList()),
				new Model((Serializable) publics)));
		
	}
	
	
	@SuppressWarnings("unchecked")
	private void apply(AjaxRequestTarget target, Form<?> form1, Component component, boolean remove)
	{
		Iterator it = ((List) component.getDefaultModelObject()).iterator();
		List choosen = ((AbstractChoice) component).getChoices();
		PrivateIdentity privateIdentity = _dao.findById(_key);

		RefreshingView publics = (RefreshingView) getPage().get("contextMenu:publicIds");
		Collection<String> publicsModel = (Collection<String>) publics.getDefaultModelObject();
		while (it.hasNext()) {
			String publicId = (String) it.next();
			PublicUserIdentity publicIdentity = (PublicUserIdentity) _publicIdentityDao.findById(publicId);
			if (remove)
			{
				privateIdentity.removePublicId(publicIdentity);
				publicsModel.remove(publicId);
			}
			else
			{
				privateIdentity.addPublicId(publicIdentity);
				publicsModel.add(publicId);
				((AbstractChoice) form1.get("publics")).getChoices().add(publicId);
			}
			choosen.remove(publicId);
		}
		
		// Check for consistency
		boolean revertDone = false;
		it = ((List) component.getDefaultModelObject()).iterator();
		while (it.hasNext()) {
			String publicId = (String) it.next();
			PublicUserIdentity publicIdentity = (PublicUserIdentity) _publicIdentityDao.findById(publicId);
			ImplicitRegistrationSet set = publicIdentity.getImplicitRegistrationSet();
			Iterator<PublicUserIdentity> it2 = set.getPublicIdentities().iterator();
			while (it2.hasNext())
			{
				PublicUserIdentity publicUserIdentity = it2.next();
				if (!publicIdentity.getPrivateIdentities().equals(publicUserIdentity.getPrivateIdentities()))
				{
					Map map = new HashMap();
					map.put("identity", publicIdentity.getIdentity());
					map.put("implicitRegistrationSet", set.getId());
					error(MapVariableInterpolator.interpolate(
							getString("subscription.error.implicitSet.privates"), map));
					
					// Revert 
					if (!revertDone)
					{
						Iterator it3 = ((List) component.getDefaultModelObject()).iterator();
						while (it3.hasNext()) {
							publicId = (String) it3.next();
							PublicUserIdentity pubId = (PublicUserIdentity) _publicIdentityDao.findById(publicId);
							if (!remove)
							{
								privateIdentity.removePublicId(pubId);
								publicsModel.remove(publicId);
							}
							else
							{
								privateIdentity.addPublicId(pubId);
								publicsModel.add(publicId);
								((AbstractChoice) form1.get("publics")).getChoices().add(publicId);
							}
						}
						revertDone = true;
					}
				}
			}
		}
		
		_dao.save(privateIdentity);		
		if (target != null)
		{
			target.addComponent(form1);
			target.addComponent(getPage().get("contextMenu"));
		}
	}
		
	@Override
	public String getTitle() {
		return getString("privateId.publicIds.title", new DaoDetachableModel(_key));
	}
}
