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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.apache.wicket.PageParameters;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
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
import org.cipango.ims.oam.util.AjaxFallbackButton;
import org.cipango.ims.oam.util.HideableLink;


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
	
	private IModel<InitialFilterCriteria> _ifcModel;
	
	private String _title;
	private Map<Integer, List<SPT>> _spts;
	private List<SPT> _sptsToDelete = new ArrayList<SPT>();
	
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
				_ifcModel = new IfcModel(ifc);
		}
		setOutputMarkupId(true);

		_title = MapVariableInterpolator.interpolate(getString("spt.edit.title"),
				new MicroMap("name", ifcKey));
		
		add(new Label("title", _title));
		
		final Form sptsForm = new Form("sptsForm");
		add(sptsForm);
		_spts = getSpts(ifc);
		
		
		
		sptsForm.add(new ListView<Integer>("groups", new LoadableDetachableModel()
		{
			@Override
			protected Object load()
			{
				return new ArrayList<Integer>(_spts.keySet());
			}
		}) {


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
						Class<SPT> clazz = (Class<SPT>) item.get("panel:sptType").getDefaultModelObject();
						if (clazz == null)
						{
							getPage().warn("No SPT selected");
							return;
						}
						SPT spt = clazz.newInstance();
						spt.setConditionNegated(false);
						spt.setGroupId(groupId);
						List<SPT> l = _spts.get(groupId);
						l.add(spt);
						
						warn(MapVariableInterpolator.interpolate(getString("spt.modification"),
								new MicroMap("spt", getExpression())));
						
						if (target != null)
						{
							target.addComponent(item.get("panel"));
						}	
					}
					
				});
				
				boolean conditonType = _ifcModel.getObject().isConditionTypeCnf();
				item.add(new Label("conditionType", conditonType ? "AND" : "OR"));
			}
		});	
		
		sptsForm.add(new AjaxFallbackButton("ok", sptsForm) {

			@Override
			protected void doSubmit(AjaxRequestTarget target, Form<?> form)
			{
				saveSpts(form);

				getCxManager().ifcUpdated(_ifcModel.getObject());
				
				info(MapVariableInterpolator.interpolate(getString("spt.modification.done"),
						new MicroMap("spt", getExpression())));
				
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
				for (int i = _spts.size(); i >= 0; i--)
				{
					boolean found = false;
					for (Integer j : _spts.keySet())
					{
						if (j == i)
						{
							found = true;
							break;
						}
					}
					if (!found)
					{
						_spts.put(i, new ArrayList<SPT>());
						break;
					}
				}
				
				warn(MapVariableInterpolator.interpolate(getString("spt.modification"),
						new MicroMap("spt", getExpression())));
				if (target != null)
				{
					target.addComponent(form);
				}
			}		
		});
		
		if (ifc != null)
			setContextMenu(new ContextPanel(ifc));
	}
	
	private void saveSpts(Form form)
	{
		ListView groups = (ListView) form.get("groups");
		Iterator<ListItem<?>> it = groups.iterator();
		InitialFilterCriteria ifc = _ifcModel.getObject();
		while (it.hasNext())
		{
			ListItem<?> listItem = (ListItem<?>) it.next();
			Collection<SPT> spts = (Collection<SPT>) listItem.get("panel:spts").getDefaultModelObject();
			Iterator<SPT> it2 = spts.iterator();
			while (it2.hasNext())
			{
				SPT spt = (SPT) it2.next();
				spt.setInitialFilterCriteria(ifc);
				_dao.save(spt);
			}
		}
		for (SPT spt : _sptsToDelete)
		{
			spt.setInitialFilterCriteria(ifc);
			_dao.delete(spt);
		}
		_sptsToDelete.clear();
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
		IModel<Collection<SPT>> ifcsModel = new Model((Serializable) _spts.get(groupId));
		
		return new RefreshingView<SPT>("spts", ifcsModel) {

			@Override
			protected Iterator<IModel<SPT>> getItemModels()
			{
				return new CompoundModelIterator((Collection<SPT>) getDefaultModelObject());
			}

			@Override
			protected void populateItem(final Item<SPT> item)
			{
				item.add(new CheckBox("conditionNegated").add(new SptUpdatingBehaviour()));
				item.add(getPanel(item.getModel()));
				item.add(new AjaxFallbackButton("delete", (Form) getPage().get("sptsForm")) {
					@Override
					protected void doSubmit(AjaxRequestTarget target, Form<?> form)
					{
						SPT spt = (SPT) item.getDefaultModelObject();
						_sptsToDelete.add(spt);
						List<SPT> l = _spts.get(spt.getGroupId());
						if (l != null)
						{
							l.remove(spt);
							if (l.isEmpty())
								_spts.remove(spt.getGroupId());
						}
						
						warn(MapVariableInterpolator.interpolate(getString("spt.modification"),
								new MicroMap("spt", getExpression())));
						if (target != null)
							target.addComponent(form);
					}
				});
				boolean conditonType = _ifcModel.getObject().isConditionTypeCnf();
				item.add(new Label("conditionType", conditonType ? "OR" : "AND"));
			}
			
		};
	}
	
	@Override
	public void detachModels()
	{
		if (_ifcModel != null)
			_ifcModel.detach();
		for (List<SPT> l : _spts.values())
		{
			for (SPT spt : l)
				spt.detach();
		}
		super.detachModels();
	}
	
	protected String getExpression()
	{
		if (_spts.isEmpty())
			return "";

		Iterator<List<SPT>> it = _spts.values().iterator();
		StringBuilder sb = new StringBuilder();
		boolean conditionTypeCnf = _ifcModel.getObject().isConditionTypeCnf();
		boolean first = true;
		while (it.hasNext())
		{
			List<SPT> l = it.next();
			if (!l.isEmpty())
			{
				if (!first)
					sb.append(conditionTypeCnf ? " && " : " || ");
				first = false;
				sb.append('(');
				Iterator<SPT> it2 = l.iterator();
				while (it2.hasNext())
				{
					SPT spt = (SPT) it2.next();
					sb.append(spt.getExpression());
					if (it2.hasNext())
						sb.append(conditionTypeCnf ? " || " : " && ");
				}
				sb.append(')');
			}
		}
		return sb.toString();
	}
	
	@Override
	public String getTitle()
	{
		return _title;
	}
	
	private Map<Integer, List<SPT>> getSpts(InitialFilterCriteria ifc)
	{
		Set<SPT> set = ifc.getSpts();
		Map<Integer, List<SPT>> map = new TreeMap<Integer, List<SPT>>();
		for (SPT spt : set)
		{
			List<SPT> l = map.get(spt.getGroupId());
			if (l == null)
			{
				l = new ArrayList<SPT>();
				map.put(spt.getGroupId(), l);
				spt.detach();
			}
			l.add(spt);
		}
		return map;
	}
		
	protected static boolean isRequired(Form form) {
		if (form == null || form.getRootForm().findSubmittingButton() == null)
			return false;
        return "ok".equals(form.getRootForm().findSubmittingButton().getInputName());
    }
	
	class CompoundModelIterator extends ModelIteratorAdapter<SPT> implements Serializable {
		public CompoundModelIterator(Collection<SPT> modelObject) {
			super(modelObject.iterator());
		}
		
		@Override
		protected IModel<SPT> model(SPT spt)
		{
			return new CompoundPropertyModel<SPT>(spt);
		}
	}
	
	class IfcModel extends LoadableDetachableModel<InitialFilterCriteria>
	{
		private Integer _ifcId;
		
		public IfcModel(InitialFilterCriteria ifc)
		{
			super(ifc);
			_ifcId = ifc.getId();
		}
		
		@Override
		protected InitialFilterCriteria load()
		{
			return _ifcDao.findByRealKey(_ifcId);
		}
		
	}
	
	public static class SptUpdatingBehaviour extends AjaxFormComponentUpdatingBehavior
	{
		public SptUpdatingBehaviour()
		{
			super("onChange");
		}
		
		@Override
		protected void onUpdate(AjaxRequestTarget target)
		{
			EditSptsPage page = (EditSptsPage) getComponent().getPage();
			page.warn(MapVariableInterpolator.interpolate(page.getString("spt.modification"),
					new MicroMap("spt", page.getExpression())));
			target.addComponent(page.get("feedback"));
			target.addComponent(getFormComponent());
		}
	}

}
