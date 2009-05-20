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
package org.cipango.ims.hss.web.spt;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.wicket.PageParameters;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
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
import org.cipango.ims.hss.db.IfcDao;
import org.cipango.ims.hss.db.SptDao;
import org.cipango.ims.hss.model.InitialFilterCriteria;
import org.cipango.ims.hss.model.spt.HeaderSpt;
import org.cipango.ims.hss.model.spt.MethodSpt;
import org.cipango.ims.hss.model.spt.RequestUriSpt;
import org.cipango.ims.hss.model.spt.SPT;
import org.cipango.ims.hss.model.spt.SessionCaseSpt;
import org.cipango.ims.hss.model.spt.SessionDescriptionSpt;
import org.cipango.ims.hss.web.BasePage;
import org.cipango.ims.hss.web.ifc.ContextPanel;
import org.cipango.ims.hss.web.util.AjaxFallbackButton;
import org.cipango.ims.hss.web.util.HideableLink;


@SuppressWarnings("unchecked")
public class EditSptsPage extends BasePage
{
	private static final Logger __log = Logger.getLogger(EditSptsPage.class);
	public static final List<Class<? extends SPT>> SPT_CLASSES = 
		Arrays.asList(HeaderSpt.class, RequestUriSpt.class, MethodSpt.class, SessionCaseSpt.class, SessionDescriptionSpt.class);
	
	@SpringBean
	private SptDao _dao;
	@SpringBean
	private IfcDao _ifcDao;
	
	private Integer _ifcId;
	
	private List<Integer> groupIds;
	private String _title;
	
	public EditSptsPage(PageParameters pageParameters)
	{
		String ifcKey = pageParameters.getString("id");
		InitialFilterCriteria ifc = null;
		if (ifcKey != null)
		{
			ifc = _ifcDao.findById(ifcKey);
			if (ifc == null)
			{
				error(MapVariableInterpolator.interpolate(getString("ifc.error.notFound"),
						new MicroMap("id", ifcKey)));
			}
			else
				_ifcId = ifc.getId();
		}
		setOutputMarkupId(true);

		_title = MapVariableInterpolator.interpolate(getString("spt.edit.title"),
				new MicroMap("name", ifcKey));
		
		add(new Label("title", _title));
		
		groupIds = _dao.getGroups(_ifcId);
		final Form sptsForm = new Form("sptsForm");
		add(sptsForm);
		
		sptsForm.add(new ListView<Integer>("groups", groupIds) {


			@Override
			protected void populateItem(final ListItem<Integer> item)
			{
				final Integer groupId = item.getModelObject();
				WebMarkupContainer panel = new WebMarkupContainer("panel");
				item.add(panel);
				panel.setOutputMarkupId(true);		
				panel.add(new HideableLink("hideLink", panel.getMarkupId()));
				panel.add(new Label("groupId", item.getModel()));
				panel.add(getSpts(groupId));

				panel.add(new DropDownChoice("sptType",
						new Model(),
						SPT_CLASSES,
						new ChoiceRenderer<Class<SPT>>() {
							public Object getDisplayValue(Class<SPT> clazz)
							{
								return getString("spt.type." + clazz.getSimpleName());
							}
				}));
				
				panel.add(new AjaxFallbackButton("add", sptsForm) {

					@Override
					protected void doSubmit(AjaxRequestTarget target, Form<?> form) throws InstantiationException, IllegalAccessException
					{
						saveSpts(form);
						Class<SPT> clazz = (Class<SPT>) item.get("panel:sptType").getDefaultModelObject();
						if (clazz == null)
						{
							getPage().warn("No SPT selected");
							return;
						}
						SPT spt = clazz.newInstance();
						spt.setConditionNegated(false);
						spt.setGroupId(groupId);
						spt.setInitialFilterCriteria(_ifcDao.findByRealKey(_ifcId));
						_dao.save(spt);
						
						if (target != null)
						{
							target.addComponent(form);
						}	
					}
					
				});
				
				boolean conditonType =  _ifcDao.findByRealKey(_ifcId).isConditionTypeCnf();
				item.add(new Label("conditionType", conditonType ? "AND" : "OR"));
			}
		});	
		
		sptsForm.add(new AjaxFallbackButton("ok", sptsForm) {

			@Override
			protected void doSubmit(AjaxRequestTarget target, Form<?> form)
			{
				saveSpts(form);
				getSession().info(getString("modification.success"));
				if (target != null)
				{
					target.addComponent(form);
					target.addComponent(getPage().get("feedback"));
				}
			}			
		});
		
		sptsForm.add(new AjaxFallbackButton("add", sptsForm) {

			@Override
			protected void doSubmit(AjaxRequestTarget target, Form<?> form)
			{
				saveSpts(form);
				for (int i = groupIds.size(); i >= 0; i--)
				{
					boolean found = false;
					for (int j = 0; j < groupIds.size(); j++)
					{
						if (groupIds.get(j) == i)
						{
							found = true;
							break;
						}
					}
					if (!found)
					{
						groupIds.add(i);
						break;
					}
				}
				if (target != null)
				{
					target.addComponent(form);
				}
			}		
		}.setDefaultFormProcessing(false));
		
		if (ifc != null)
			setContextMenu(new ContextPanel(ifc));
	}
	
	private void saveSpts(Form form)
	{
		ListView groups = (ListView) form.get("groups");
		Iterator<ListItem<?>> it = groups.iterator();
		
		while (it.hasNext())
		{
			ListItem<?> listItem = (ListItem<?>) it.next();
			Collection<SPT> spts = (Collection<SPT>) listItem.get("panel:spts").getDefaultModelObject();
			Iterator<SPT> it2 = spts.iterator();
			while (it2.hasNext())
			{
				SPT spt = (SPT) it2.next();
				_dao.save(spt);
			}
		}
	}
	
	private Panel getPanel(IModel<SPT> sptModel)
	{
		SPT spt = sptModel.getObject();
		if (spt instanceof HeaderSpt)
			return new HeaderSptPanel("spt.specific", sptModel);
		if (spt instanceof MethodSpt)
			return new MethodSptPanel("spt.specific", sptModel);
		if (spt instanceof RequestUriSpt)
			return new RequestUriSptPanel("spt.specific", sptModel);
		if (spt instanceof SessionCaseSpt)
			return new SessionCaseSptPanel("spt.specific", sptModel);
		if (spt instanceof SessionDescriptionSpt)
			return new SessionDescriptionSptPanel("spt.specific", sptModel);
		__log.warn("Unknown SPT type: " + spt.getClass() + "/" + spt);
		return null;
	}
	
	private RefreshingView<SPT> getSpts(final Integer groupId)
	{
		IModel<Collection<SPT>> ifcsModel = new LoadableDetachableModel<Collection<SPT>>() {
			@Override
			protected Collection<SPT> load()
			{
				return _dao.getSptsByIfc(_ifcId, groupId);
			}
			
		};
		
		return new RefreshingView<SPT>("spts", ifcsModel) {

			@Override
			protected Iterator<IModel<SPT>> getItemModels()
			{
				return new CompoundModelIterator((Collection<SPT>) getDefaultModelObject());
			}

			@Override
			protected void populateItem(Item<SPT> item)
			{
				Form<SPT> form = new Form<SPT>("form", item.getModel());
				item.add(form);
				form.add(new CheckBox("conditionNegated"));
				form.add(getPanel(item.getModel()));
				form.add(new AjaxFallbackButton("delete", form) {
					@Override
					protected void doSubmit(AjaxRequestTarget target, Form<?> form)
					{
						SPT spt = (SPT) form.getDefaultModelObject();
						_dao.delete(spt);
						if (target != null)
						{
							target.addComponent(EditSptsPage.this);
						}
					}
				}.setDefaultFormProcessing(false));
				boolean conditonType = item.getModelObject().getInitialFilterCriteria().isConditionTypeCnf();
				item.add(new Label("conditionType", conditonType ? "OR" : "AND"));
			}
			
		};
	}
	
	class CompoundModelIterator extends ModelIteratorAdapter<SPT> implements Serializable {
		public CompoundModelIterator(Collection<SPT> modelObject) {
			super(modelObject.iterator());
		}
		
		@Override
		protected IModel<SPT> model(SPT spt)
		{
			return new CompoundPropertyModel<SPT>(new DaoLoadableMode(spt));
		}
	}
	
	class DaoLoadableMode extends LoadableDetachableModel<SPT>
	{
		private Long _id;
		
		public DaoLoadableMode(SPT spt)
		{
			super(spt);
			if (spt != null)
				_id = spt.getId();
		}
		

		@Override
		protected SPT load()
		{
			return _dao.findById(_id);
		}
		
	}

	@Override
	public String getTitle()
	{
		return _title;
	}
}
