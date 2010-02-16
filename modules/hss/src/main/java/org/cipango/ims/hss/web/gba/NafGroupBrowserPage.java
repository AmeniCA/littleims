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
package org.cipango.ims.hss.web.gba;

import java.util.List;

import org.apache.log4j.Logger;
import org.apache.wicket.Component;
import org.apache.wicket.PageParameters;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.cipango.ims.hss.db.NafGroupDao;
import org.cipango.ims.hss.model.NafGroup;
import org.cipango.ims.hss.web.BasePage;
import org.cipango.ims.oam.util.AbstractListDataProvider;
import org.cipango.ims.oam.util.AjaxFallbackButton;
import org.cipango.ims.oam.util.EditableLabel;
import org.cipango.ims.oam.util.OddEvenAttrModifier;

public class NafGroupBrowserPage extends BasePage
{
	private static final Logger __log = Logger.getLogger(NafGroupBrowserPage.class);
	
	@SpringBean
	private NafGroupDao _dao;
	
	@SuppressWarnings("unchecked")
	public NafGroupBrowserPage(PageParameters pageParameters)
	{				
		
		IDataProvider<NafGroup> provider = new AbstractListDataProvider<NafGroup>()
		{

			@Override
			public List<NafGroup> load()
			{
				return _dao.getAllGroups();
			}

			public IModel<NafGroup> model(NafGroup nafGroup)
			{
				return new CompoundPropertyModel<NafGroup>(new DaoDetachableModel(nafGroup))
				{
					@Override
					protected String propertyExpression(Component component)
					{
						if (component.getParent() instanceof EditableLabel)
							return component.getParent().getId();
						return component.getId();
					}
				};
			}
			
		};
		
		WebMarkupContainer container = new WebMarkupContainer("browser");
		container.setOutputMarkupId(true);
		add(container);
		
		container.add(new DataView<NafGroup>("nafGroup", provider, getItemByPage())
		{

			@Override
			protected void populateItem(final Item<NafGroup> item)
			{
				NafGroup nafGroup = item.getModelObject();
				Form form = new Form("form", item.getModel());
				item.add(form);
				form.add(new EditableLabel("name"));
				form.add(new AjaxFallbackButton("apply", form)
				{

					@Override
					protected void doSubmit(AjaxRequestTarget target,
							Form<?> form) throws Exception
					{
						NafGroup nafGroup = (NafGroup) form.getDefaultModelObject();
						_dao.save(nafGroup);
						EditableLabel name = (EditableLabel) form.get("name");
						name.setEditable(false);
						setVisible(false);
						if (target != null)
							target.addComponent(item);
						
					}
					
				}.setVisible(false));
				
				item.add(new Label("nbUss", String.valueOf(nafGroup.getUssSet().size())));
				item.add(new AjaxFallbackLink("editLink")
				{

					@Override
					public void onClick(AjaxRequestTarget target)
					{
						EditableLabel name = (EditableLabel) item.get("form:name");
						item.get("form:apply").setVisible(true);
						name.setEditable(true);
						if (target != null)
							target.addComponent(item);
					}
					
				});
				
				item.add(new AjaxFallbackLink("deleteLink")
				{

					@Override
					public void onClick(AjaxRequestTarget target)
					{
						NafGroup nafGroup = (NafGroup) getParent().getDefaultModelObject();
						_dao.delete(nafGroup);
						if (target != null)
							target.addComponent(getPage().get("browser"));
					}
					
				});
				item.setOutputMarkupId(true);
				item.add(new OddEvenAttrModifier(item.getIndex()));
			}
			
		});
		
		Form form = new Form("form", new CompoundPropertyModel(new DaoDetachableModel(null)));
		add(form);
		form.add(new RequiredTextField("name", String.class));
		form.add(new AjaxFallbackButton("submit", form)
		{

			@Override
			protected void doSubmit(AjaxRequestTarget target, Form<?> form)
					throws Exception
			{
				try
				{
					_dao.save((NafGroup) form.getDefaultModelObject());
					if (target != null)
					{
						target.addComponent(getPage().get("browser"));
					}
				}
				catch (Exception e)
				{
					__log.debug("Failed to apply edit", e);
					getSession().error(getString("nafGroup.error.duplicate", getForm().getModel()));
				}
			}
			
		});
	}

	@Override
	public String getTitle()
	{
		return getString("nafGroup.browser.title");
	}
	
	public class DaoDetachableModel extends LoadableDetachableModel<NafGroup>
	{
		private String _key;

		public DaoDetachableModel(NafGroup nafGroup)
		{
			super(nafGroup);
			if (nafGroup != null)
				_key = nafGroup.getName();
		}

		@Override
		protected NafGroup load()
		{
			if (_key == null)
				return new NafGroup();
			else
				return _dao.findById(_key);
		}

		@Override
		public void detach()
		{
			NafGroup nafGroup = getObject();
			if (nafGroup != null)
				_key = nafGroup.getName();
			super.detach();
		}
	}
}
