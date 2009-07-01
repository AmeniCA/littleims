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
package org.cipango.ims.hss.web.debugsession;

import java.util.Arrays;

import org.apache.log4j.Logger;
import org.apache.wicket.PageParameters;
import org.apache.wicket.extensions.yui.calendar.DateTimeField;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.collections.MicroMap;
import org.apache.wicket.util.string.interpolator.MapVariableInterpolator;
import org.cipango.ims.hss.db.PublicIdentityDao;
import org.cipango.ims.hss.model.DebugSession;
import org.cipango.ims.hss.model.PSI;
import org.cipango.ims.hss.model.PublicIdentity;
import org.cipango.ims.hss.model.PublicUserIdentity;
import org.cipango.ims.hss.web.publicid.ContextPanel;
import org.cipango.ims.hss.web.publicid.PsiContextPanel;
import org.cipango.ims.hss.web.publicid.PublicIdBrowserPage;
import org.cipango.ims.hss.web.util.MethodField;
import org.cipango.ims.hss.web.util.UriValidator;

public class EditDebugSessionPage extends DebugSessionPage
{

	private static final Logger __log = Logger.getLogger(EditDebugSessionPage.class);
	
	private Long _key;
	private String _publicIdKey;
	private String _title;
	
	@SpringBean
	private PublicIdentityDao _publicIdentityDao;

	@SuppressWarnings("unchecked")
	public EditDebugSessionPage(PageParameters pageParameters)
	{
		String sKey = pageParameters.getString("id");
		PublicIdentity publicIdentity = null;
		if (sKey != null)
			_key = Long.decode(sKey);
		else
		{
			_publicIdKey = pageParameters.getString("publicId");
			publicIdentity = _publicIdentityDao.findById(_publicIdKey);
		}
			
		DebugSession debugSession = null;
		if (_key != null)
		{
			debugSession = _dao.findById(_key);
			if (debugSession == null)
			{
				error(MapVariableInterpolator.interpolate(getString(getPrefix() + ".error.notFound"),
						new MicroMap("identity", _key)));
				_key = null;
			}
			else
				publicIdentity = debugSession.getPublicIdentity();
		}
		
		DaoDetachableModel model = new DaoDetachableModel(debugSession);
		
		if (isAdding()) {
			_title = getString(getPrefix() + ".add.title");
		} else {
			_title = getString(getPrefix() + ".edit.title", model);
		}
		
		Form form = new Form("form", new CompoundPropertyModel(model));
		add(form);
		form.add(new TextField<String>("from", String.class).add(new UriValidator()));
		form.add(new TextField<String>("to", String.class).add(new UriValidator()));
		form.add(new MethodField("method"));
		form.add(new TextField<String>("iari", String.class));
		form.add(new TextField<String>("icsi", String.class));
		
        form.add(new DateTimeField("startDate"));
                
        form.add(new TextField<String>("duration", String.class));
        form.add(new DropDownChoice("reason",
				Arrays.asList(new Boolean[]{true, false}),
				new ChoiceRenderer<Boolean>()
		{
			@Override
			public Object getDisplayValue(Boolean id)
			{
				return DebugSession.getReasonAsString(id);
			}
			
		}));
        form.add(new DateTimeField("stopDate"));
        
        form.add(new RequiredTextField<String>("debugId", String.class));
        form.add(new DropDownChoice("traceDepth",
				Arrays.asList(new Boolean[]{true, false}),
				new ChoiceRenderer<Boolean>()
		{
			@Override
			public Object getDisplayValue(Boolean id)
			{
				return DebugSession.getTraceDepthAsString(id);
			}
			
		}));
		
		form.add(new Button("submit")
		{
			@Override
			public void onSubmit()
			{
				try
				{
					DebugSession debugSession = (DebugSession) getForm().getModelObject();
					
					if (_publicIdKey != null)
					{
						PublicIdentity publicIdentity = _publicIdentityDao.findById(_publicIdKey);
						if (publicIdentity != null)
							debugSession.setPublicIdentity(publicIdentity);
					}

					_dao.save(debugSession);
					getSession().info(getString("modification.success"));
					if (!debugSession.getId().equals(_key))
						setResponsePage(EditDebugSessionPage.class, 
								new PageParameters("id=" + debugSession.getId()));
				}
				catch (Exception e)
				{
					__log.debug("Failed to apply edit", e);
					getSession().error(getString(getPrefix() + ".error.duplicate", getForm().getModel()));
				}
			}
		});

		form.add(new Button("cancel")
		{
			@Override
			public void onSubmit()
			{
				getSession().info(getString("modification.cancel"));
				goToBackPage(PublicIdBrowserPage.class);
			}
		}.setDefaultFormProcessing(false));

		if (publicIdentity != null)
		{
			if (publicIdentity instanceof PublicUserIdentity)
				setContextMenu(new ContextPanel((PublicUserIdentity) publicIdentity));
			else
				setContextMenu(new PsiContextPanel((PSI) publicIdentity));
		}
			
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

