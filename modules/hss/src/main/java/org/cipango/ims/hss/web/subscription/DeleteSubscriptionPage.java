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
package org.cipango.ims.hss.web.subscription;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.CheckGroup;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.cipango.ims.hss.db.PrivateIdentityDao;
import org.cipango.ims.hss.db.PublicIdentityDao;
import org.cipango.ims.hss.model.PrivateIdentity;
import org.cipango.ims.hss.model.PublicUserIdentity;
import org.cipango.ims.hss.model.Subscription;


public class DeleteSubscriptionPage extends SubscriptionPage {
	
	@SpringBean
	PrivateIdentityDao _privateIdentityDao;
	
	@SpringBean
	PublicIdentityDao _publicIdentityDao;
	
	@SuppressWarnings("unchecked")
	public DeleteSubscriptionPage(PageParameters pageParameters) {
		Subscription subscription = getSubscription(pageParameters);
		final String key = subscription == null ? null : subscription.getName();

		add(new Label("delete.confirm", 
				getString(getPrefix() + ".delete.confirm", new DaoDetachableModel(subscription))));

		/*
		 * Use a form to hold the buttons, but set the default form processing
		 * off as there's no point it trying to do anything, as all we're
		 * interested in are the button clicks.
		 */
		Form form = new Form("confirmForm");
		
		List<String> privateIds = subscription  == null ? Collections.EMPTY_LIST : new ArrayList(subscription.getPrivateIds());
		
		CheckGroup privateIdsGroup = new CheckGroup("privateIdsGroup", new ArrayList(privateIds));
		form.add(privateIdsGroup);
		privateIdsGroup.add(new ListView("list", privateIds){
			@Override
			protected void populateItem(ListItem item)
			{
				//item.add(new Check("delete", item.getModel()));
				item.add(new Label("identity", item.getModel()));
			}
		}.setReuseItems(true));
		//privateIdsGroup.add(new CheckGroupSelector("groupSelector"));
		
		List<String> publicIds = subscription  == null ? Collections.EMPTY_LIST : new ArrayList(subscription.getPublicIds());
		CheckGroup publicIdsGroup = new CheckGroup("publicIdsGroup", new ArrayList(publicIds));
		form.add(publicIdsGroup);
		publicIdsGroup.add(new ListView("list", publicIds){
			@Override
			protected void populateItem(ListItem item)
			{
				//item.add(new Check("delete", item.getModel()));
				item.add(new Label("identity", item.getModel()));
			}
		}.setReuseItems(true));
		//publicIdsGroup.add(new CheckGroupSelector("groupSelector"));
		

		form.add(new Button("delete") {
			public void onSubmit() {
				/*CheckGroup publicIdsGroup = (CheckGroup) getForm().get("privateIdsGroup");
				Iterator<String> it = ((Collection<String>) publicIdsGroup.getModelObject()).iterator();
				while (it.hasNext())
				{
					String id = it.next();
					PublicIdentity publicIdentity = _publicIdentityDao.findById(id);
					if (publicIdentity != null)
					{
						_publicIdentityDao.delete(publicIdentity);
						getSession().info(getString("publicId.delete.done", new LoadableDetachableModel(publicIdentity) {
							protected Object load()
							{
								return null;
							}								
						}));
					}
				}
				CheckGroup privateIdsGroup = (CheckGroup) getForm().get("privateIdsGroup");
				it = ((Collection) privateIdsGroup.getModelObject()).iterator();
				while (it.hasNext())
				{
					String id = it.next();
					PrivateIdentity privateIdentity = _privateIdentityDao.findById(id);
					if (privateIdentity != null)
					{
						_privateIdentityDao.delete(privateIdentity);
						getSession().info(getString("privateId.delete.done", new LoadableDetachableModel(privateIdentity) {
							protected Object load()
							{
								return null;
							}								
						}));
					}
				}*/

				Subscription id = _dao.findById(key);
				Iterator<PrivateIdentity> it2 = id.getPrivateIdentities().iterator();
				while (it2.hasNext())
				{
					PrivateIdentity privateIdentity = it2.next();
					Iterator<PublicUserIdentity> it3 = privateIdentity.getPublicIdentities().iterator();
					while (it3.hasNext())
					{
						_publicIdentityDao.delete(it3.next());	
					}
					_privateIdentityDao.delete(privateIdentity);
					
				}
				_dao.delete(id);
				getSession().info(getString(getPrefix() + ".delete.done", new DaoDetachableModel(id)));
				
				goToBackPage(SubscriptionBrowserPage.class);
			}
		});

		form.add(new Button("cancel") {
			public void onSubmit() {
				getSession().info(getString(getPrefix() + ".delete.canceled", new DaoDetachableModel(key)));
				goToBackPage(SubscriptionBrowserPage.class);
			}
		}.setDefaultFormProcessing(false));

		add(form);
		if (subscription != null)
			setContextMenu(new ContextPanel(subscription));
	}

	@Override
	public String getTitle() {
		return getString(getPrefix() + ".delete.title");
	}
}
