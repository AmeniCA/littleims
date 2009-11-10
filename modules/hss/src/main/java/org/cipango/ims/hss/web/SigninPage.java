package org.cipango.ims.hss.web;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.apache.wicket.markup.html.CSSPackageResource;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.html.form.StatelessForm;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.protocol.http.WebRequest;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.string.Strings;
import org.cipango.ims.hss.db.AdminUserDao;
import org.cipango.ims.hss.model.AdminUser;
import org.cipango.ims.oam.SpringApplication;

@SuppressWarnings("unused")
public class SigninPage extends WebPage
{
	private static final Logger __log = Logger.getLogger(SigninPage.class);
	
	@SpringBean
	private AdminUserDao _dao;
	
	public SigninPage()
	{
		add(new Label("title", getString("signin.title")));
		add(new SignInForm("signInForm"));
		add(new FeedbackPanel("feedback"));
		add(CSSPackageResource.getHeaderContribution(SpringApplication.class, "style.css"));
	}
	
	@SuppressWarnings("unchecked")
	private class SignInForm extends StatelessForm
	{

		private String _password;
		private String _login;

		public SignInForm(final String id)
		{
			super(id);
			setModel(new CompoundPropertyModel(this));
			add(new RequiredTextField("login"));
			add(new PasswordTextField("password"));
		}

		@Override
		public final void onSubmit()
		{
			if (signIn(_login, _password))
			{
				if (!continueToOriginalDestination())
					setResponsePage(getApplication().getHomePage());
			}
			else
				error(getString("signin.error.invalidLoginPassword"));
		}

		private boolean signIn(String username, String password)
		{
			if (!Strings.isEmpty(username) && !Strings.isEmpty(password))
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
				HttpServletRequest request = ((WebRequest)getRequestCycle().getRequest()).getHttpServletRequest();
				__log.warn("Invalid login / password from IP address " 
						+ request.getRemoteAddr() +  " and host " + request.getRemoteHost()
						+ " for login " + username);
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

		public String getLogin()
		{
			return _login;
		}

		public void setLogin(String login)
		{
			_login = login;
		}

	}

}
