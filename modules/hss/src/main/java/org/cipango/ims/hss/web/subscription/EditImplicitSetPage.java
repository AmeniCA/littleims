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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.PageParameters;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.ListMultipleChoice;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.RefreshingView;
import org.apache.wicket.markup.repeater.util.ModelIteratorAdapter;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.collections.MicroMap;
import org.apache.wicket.util.string.interpolator.MapVariableInterpolator;
import org.cipango.ims.hss.db.ImplicitRegistrationSetDao;
import org.cipango.ims.hss.db.PublicIdentityDao;
import org.cipango.ims.hss.model.ImplicitRegistrationSet;
import org.cipango.ims.hss.model.PrivateIdentity;
import org.cipango.ims.hss.model.PublicUserIdentity;
import org.cipango.ims.hss.model.Subscription;
import org.cipango.ims.hss.web.publicid.EditPublicUserIdPage;
import org.cipango.ims.hss.web.util.AjaxFallbackButton;
import org.cipango.ims.hss.web.util.StringModelIterator;


public class EditImplicitSetPage extends SubscriptionPage
{

	@SpringBean
	private PublicIdentityDao _publicIdentityDao;
	
	@SpringBean
	private ImplicitRegistrationSetDao _implicitRegistrationSetDao;
	
	private Long _key;
	private String _title;
	
	private static final Logger __log = Logger.getLogger(EditImplicitSetPage.class);
	
	@SuppressWarnings("unchecked")
	public EditImplicitSetPage(PageParameters pageParameters) {
		String key = pageParameters.getString("id");
		Subscription subscription = _dao.findById(key);

		if (subscription == null)
		{
			error(MapVariableInterpolator.interpolate(getString(getPrefix() + ".error.notFound"),
					new MicroMap("id", key)));
			_key = null;
		}
					
		add(new SvgMarkupContainer("svg", subscription));
		
		final Form form = new Form("form");
		form.setOutputMarkupId(true);
		add(form);
		
		_title = MapVariableInterpolator.interpolate(
					getString("susbcription.implicititRegistrationSets.title"),
					new MicroMap("name", key));
		add(new Label("title", _title));
		
		if (subscription == null)
		{
			form.setVisible(false);
			return;
		}
		_key = subscription.getId();
		
			
		IModel<List<ImplicitRegistrationSet>> implicitSets = new LoadableDetachableModel<List<ImplicitRegistrationSet>>()
		{
			@Override
			protected List<ImplicitRegistrationSet> load()
			{
				return _implicitRegistrationSetDao.getImplicitRegistrationSet(_key);
			}
			
		};
		
		form.add(new ListMultipleChoice(
				"publicIds",  
				new Model(new ArrayList()),
				new Model(new ArrayList(subscription.getPublicIds()))));
		
		form.add(new RefreshingView("actions", implicitSets)
		{

			@Override
			protected Iterator getItemModels()
			{
				return new CompoundModelIterator((Collection<ImplicitRegistrationSet>) getDefaultModelObject());
			}

			@Override
			protected void populateItem(final Item item)
			{
				Model<String> buttonModel = new Model<String>(getString("button.implicitSet.changeTo", item.getModel()));
				item.add(new AjaxFallbackButton("button", buttonModel, form) {
					
					
					@Override
					protected void doSubmit(AjaxRequestTarget target, Form<?> form1)
							throws Exception
					{
						ImplicitRegistrationSet implicitRegistrationSet = (ImplicitRegistrationSet) item.getDefaultModelObject();
						apply(target, form1, implicitRegistrationSet);
					}

				});
			}
			
		});
		
		form.add(new AjaxFallbackButton("addButton", form) {
			
			@Override
			protected void doSubmit(AjaxRequestTarget target, Form<?> form1)
					throws Exception
			{
				if (!((List) form1.get("publicIds").getDefaultModelObject()).isEmpty())
				{
					ImplicitRegistrationSet implicitRegistrationSet = new ImplicitRegistrationSet();
					_implicitRegistrationSetDao.save(implicitRegistrationSet);
					apply(target, form1, implicitRegistrationSet);
				}
			}

		});

		form.add(new RefreshingView("implicitSet", implicitSets)
		{

			@Override
			protected Iterator getItemModels()
			{
				return new CompoundModelIterator((Collection<ImplicitRegistrationSet>) getDefaultModelObject());
			}

			@Override
			protected void populateItem(Item item)
			{
				ImplicitRegistrationSet implicitRegistrationSet = (ImplicitRegistrationSet) item.getDefaultModelObject();
				item.add(new Label("id"));
				item.add(new RefreshingView("publicIds", new Model((Serializable) implicitRegistrationSet.getPublicIds())){

					@Override
					protected Iterator getItemModels()
					{
						return new StringModelIterator(((Collection) getDefaultModelObject()));
					}

					@Override
					protected void populateItem(Item item2)
					{
						MarkupContainer link = new BookmarkablePageLink("identity", 
								EditPublicUserIdPage.class, 
								new PageParameters("id=" + item2.getModelObject()));
						item2.add(link);
						link.add(new Label("name", item2.getModel()));
					}
				});
			}
			
		});
		
		setContextMenu(new ContextPanel(subscription));
	}
	
	@Override
	public String getTitle() {
		return _title;
	}
	
	@SuppressWarnings("unchecked")
	private void apply(AjaxRequestTarget target,Form form, ImplicitRegistrationSet implicitRegistrationSet)
	{
		Set<PublicUserIdentity> publics = implicitRegistrationSet.getPublicIdentities();
		Set<PrivateIdentity> privates = null;
		if (!publics.isEmpty())
			privates = publics.iterator().next().getPrivateIdentities();
		
		Iterator it = ((List) form.get("publicIds").getDefaultModelObject()).iterator();
		while (it.hasNext()) {
			PublicUserIdentity publicIdentity = (PublicUserIdentity) _publicIdentityDao.findById((String) it.next());
			
			if (privates != null && !privates.equals(publicIdentity.getPrivateIdentities()))
			{
				Map map = new HashMap();
				map.put("identity", publicIdentity.getIdentity());
				map.put("implicitRegistrationSet", implicitRegistrationSet.getId());
				error(MapVariableInterpolator.interpolate(
						getString("subscription.error.implicitSet.privates"), map));
			}
			else
			{
				ImplicitRegistrationSet previous = publicIdentity.getImplicitRegistrationSet();
				publicIdentity.setImplicitRegistrationSet(implicitRegistrationSet);
				if (previous != null && previous.getPublicIdentities().isEmpty())
					_implicitRegistrationSetDao.delete(previous);
				_publicIdentityDao.save(publicIdentity);
			}
		}	
		if (target != null)
		{
			target.addComponent(form);
			target.addComponent(getPage().get("svg"));
		}
	}
	
	
	
	class CompoundModelIterator extends ModelIteratorAdapter<ImplicitRegistrationSet> implements Serializable {
		public CompoundModelIterator(Collection<ImplicitRegistrationSet> modelObject) {
			super(modelObject.iterator());
		}
		
		@Override
		protected IModel<ImplicitRegistrationSet> model(ImplicitRegistrationSet irs)
		{
			return new CompoundPropertyModel<ImplicitRegistrationSet>(new ImplicitSetModel(irs));
		}
	}
	
	class ImplicitSetModel extends LoadableDetachableModel<ImplicitRegistrationSet>
	{
		private Long _id;
		
		public ImplicitSetModel(ImplicitRegistrationSet implicitRegistrationSet)
		{
			super(implicitRegistrationSet);
			if (implicitRegistrationSet != null)
				_id = implicitRegistrationSet.getId();
		}

		@Override
		protected ImplicitRegistrationSet load()
		{
			return _implicitRegistrationSetDao.findById(_id);
		}
		
	}
}
