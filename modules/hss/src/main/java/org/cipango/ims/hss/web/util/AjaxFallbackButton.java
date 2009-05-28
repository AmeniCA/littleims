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
package org.cipango.ims.hss.web.util;

import org.apache.log4j.Logger;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.IModel;


public abstract class AjaxFallbackButton extends org.apache.wicket.ajax.markup.html.form.AjaxFallbackButton
{

	private static final Logger __log = Logger.getLogger(AjaxFallbackButton.class);
	
	public AjaxFallbackButton(String id, Form<?> form)
	{
		super(id, form);
	}
	
	public AjaxFallbackButton(String id, IModel<String> model, Form<?> form)
	{
		super(id, model, form);
	}
	
	@Override
	protected void onError(AjaxRequestTarget target, Form<?> form)
	{
		super.onError(target, form);
		if (target != null)
			target.addComponent(getPage().get("feedback"));
	}
	
	@Override
	protected void onSubmit(AjaxRequestTarget target, Form<?> form)
	{
		if (target != null)
			target.addComponent(getPage().get("feedback"));
		try
		{
			doSubmit(target, form);			
		}
		catch (Throwable e)
		{
			__log.debug(e.getMessage(), e);
			getSession().warn(getString("modification.failure") + e.getLocalizedMessage());
		}
		
	}	
	
	protected abstract void doSubmit(AjaxRequestTarget target, Form<?> form) throws Exception;

}
