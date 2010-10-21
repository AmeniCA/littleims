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
package org.cipango.ims.hss.web.adminuser;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.validation.EqualPasswordInputValidator;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.collections.MicroMap;
import org.apache.wicket.util.string.Strings;
import org.apache.wicket.util.string.interpolator.MapVariableInterpolator;
import org.cipango.ims.hss.model.AdminUser;

public class SetPasswordPage extends AdminUserPage
{

	private String _key;
	private String _title;
	
	@SuppressWarnings("unchecked")
	public SetPasswordPage(PageParameters pageParameters)
	{
		super(pageParameters);
		_key = pageParameters.getString("id");
		AdminUser adminUser = null;
		if (_key != null)
		{
			adminUser = _dao.findById(_key);
			if (adminUser == null)
			{
				error(MapVariableInterpolator.interpolate(getString(getPrefix() + ".error.notFound"),
						new MicroMap("id", _key)));
				_key = null;
			}
		}
		
		IModel model = new DaoDetachableModel(adminUser);
		

		_title = getString(getPrefix() + ".setPassword.title", model);


		Form form = new Form("form", new CompoundPropertyModel(model));
		add(form);
		form.add(new Label("title", adminUser == null ? "" : adminUser.getLogin()));
		final PasswordTextField password1 = new PasswordTextField("password1", new Model<String>());
		form.add(password1);
		final PasswordTextField password2 = new PasswordTextField("password2", new Model<String>());
		form.add(password2);
		form.add(new EqualPasswordInputValidator(password1, password2)); 
		
		form.add(new Button("submit")
		{
			@Override
			public void onSubmit()
			{
				if (!Strings.isEmpty(password1.getModelObject()) && password1.getModelObject().equals(password2.getModelObject()))
				{
					AdminUser admin = (AdminUser) getForm().getModelObject();
					admin.setClearPassword(password1.getModelObject());
					_dao.save(admin);
					getSession().info(getString("adminUser.passwordUpdated", getForm().getModel()));
					setResponsePage(EditAdminUserPage.class, new PageParameters("id=" + admin.getLogin()));
				}
				else
					getSession().warn(getString("adminUser.error.passwordsDontMatch", getForm().getModel()));

			}
		});
		form.add(new Button("cancel")
		{
			@Override
			public void onSubmit()
			{
				getSession().info(getString("modification.cancel"));
				AdminUser admin = (AdminUser) getForm().getModelObject();
				setResponsePage(EditAdminUserPage.class, new PageParameters("id=" + admin.getLogin()));
			}
		}.setDefaultFormProcessing(false));

		if (adminUser != null)
			setContextMenu(new ContextPanel(adminUser));
	}

	@Override
	public String getTitle()
	{
		return _title;
	}
}

