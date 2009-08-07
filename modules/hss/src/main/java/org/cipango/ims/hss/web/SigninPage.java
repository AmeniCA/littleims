package org.cipango.ims.hss.web;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.html.form.StatelessForm;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.cipango.ims.hss.db.AdminUserDao;
import org.cipango.ims.hss.model.AdminUser;

@SuppressWarnings("unused")
public class SigninPage extends WebPage
{
	@SpringBean
	private AdminUserDao _dao;
	
	public SigninPage()
	{
		add(new Label("title", getString("signin.title")));
		add(new SignInForm("signInForm"));
		add(new FeedbackPanel("feedback"));
	}
	
	@SuppressWarnings("unchecked")
	private class SignInForm extends StatelessForm
	{

		private String _password;
		private String _username;

		public SignInForm(final String id)
		{
			super(id);
			setModel(new CompoundPropertyModel(this));
			add(new RequiredTextField("username"));
			add(new PasswordTextField("password"));
		}

		@Override
		public final void onSubmit()
		{
			if (signIn(_username, _password))
			{
				if (!continueToOriginalDestination())
				{
					setResponsePage(getApplication().getHomePage());
				}
			}
			else
			{
				error("Unknown username/ password");
			}
		}

		private boolean signIn(String username, String password)
		{
			if (username != null && password != null)
			{
				AdminUser user = _dao.findById(username);
				if (user != null)
				{
					if (user.equalsPassword(password))
					{
						((ImsSession) getSession()).setAdminUser(user);
						return true;
					}
				}
			}
			return false;
		}

		public String getPassword()
		{
			return _password;
		}

		public void setPassword(String password)
		{
			_password = password;
		}

		public String getUsername()
		{
			return _username;
		}

		public void setUsername(String username)
		{
			_username = username;
		}
	}

}
