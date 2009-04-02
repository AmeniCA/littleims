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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.ListMultipleChoice;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.collections.MicroMap;
import org.apache.wicket.util.string.interpolator.MapVariableInterpolator;
import org.cipango.ims.hss.db.PublicIdentityDao;
import org.cipango.ims.hss.model.PrivateIdentity;
import org.cipango.ims.hss.model.PublicPrivate;


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
		form.add(new Button("join.noSub") {
			@Override
			public void onSubmit() {
				Iterator it = ((List)getForm().get("publics.available.noSub").getDefaultModelObject()).iterator();
				PrivateIdentity id = _dao.findById(_key);
				List choosen = ((ListMultipleChoice) getForm().get("publics.available.noSub")).getChoices();
				while (it.hasNext()) {
					String publicId = (String) it.next();
					id.addPublicId(_publicIdentityDao.findById(publicId));
					choosen.remove(publicId);
					it.remove();
				}
				_dao.save(id);
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
		
		form.add(new Button("join") {
			@Override
			public void onSubmit() {
				Iterator it = ((List)getForm().get("publics.available.sub").getDefaultModelObject()).iterator();
				PrivateIdentity id = _dao.findById(_key);
				List choosen = ((ListMultipleChoice) getForm().get("publics.available.sub")).getChoices();
				while (it.hasNext()) {
					String publicId = (String) it.next();
					id.addPublicId(_publicIdentityDao.findById(publicId));
					choosen.remove(publicId);
					it.remove();
				}
				_dao.save(id);
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
		
		
		form.add(new Button("leave") {
			@Override
			public void onSubmit() {
				Iterator it = ((List)getForm().get("publics").getDefaultModelObject()).iterator();
				List choosen = ((ListMultipleChoice) getForm().get("publics")).getChoices();
				while (it.hasNext()) {
					String publicId = (String) it.next();
					_dao.delete(_dao.getPublicPrivate(publicId, _key));
					choosen.remove(publicId);
					it.remove();
				}
			}
		});
		form.add(new ListMultipleChoice(
				"publics", 
				new Model(new ArrayList()),
				new LoadableDetachableModel() {
					@Override
					protected Object load() {
						List list = new ArrayList();
						Iterator<PublicPrivate> it = _dao.findById(_key).getPublicIdentities().iterator();
						while (it.hasNext()) {
							list.add(it.next().getPublicId());
						}
						return list;
					}
				}));
		
	}
	
	
	@Override
	public String getTitle() {
		return getString("privateId.publicIds.title", new DaoDetachableModel(_key));
	}
}
