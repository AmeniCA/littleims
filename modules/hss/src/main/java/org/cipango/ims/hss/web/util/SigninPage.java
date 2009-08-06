package org.cipango.ims.hss.web.util;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.StatelessForm;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.cipango.ims.hss.db.AdminUserDao;
import org.cipango.ims.hss.model.AdminUser;
import org.cipango.ims.hss.web.ImsSession;

public class SigninPage extends WebPage
{
	@SpringBean
	private AdminUserDao _dao;
	
	public SigninPage()
	{
		add(new SignInForm("signInForm"));
		add(new FeedbackPanel("feedback"));
	}
	
	private class SignInForm extends StatelessForm
	{

		private String _password;

		private String _username;

		@SuppressWarnings("unchecked")
		public SignInForm(final String id)
		{
			super(id);
			setModel(new CompoundPropertyModel(this));
			add(new TextField("username"));
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
