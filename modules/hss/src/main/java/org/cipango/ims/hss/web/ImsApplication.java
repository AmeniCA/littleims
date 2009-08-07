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
package org.cipango.ims.hss.web;

import java.util.Locale;

import javax.servlet.sip.SipFactory;

import org.apache.wicket.IConverterLocator;
import org.apache.wicket.Page;
import org.apache.wicket.Request;
import org.apache.wicket.Response;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.target.coding.MixedParamUrlCodingStrategy;
import org.apache.wicket.settings.ISecuritySettings;
import org.apache.wicket.spring.injection.annot.SpringComponentInjector;
import org.apache.wicket.util.convert.ConversionException;
import org.apache.wicket.util.convert.ConverterLocator;
import org.apache.wicket.util.convert.IConverter;
import org.cipango.ims.hss.db.AdminUserDao;
import org.cipango.ims.hss.util.HexString;
import org.cipango.ims.hss.web.adminuser.AdminUserBrowserPage;
import org.cipango.ims.hss.web.adminuser.DeleteAdminUserPage;
import org.cipango.ims.hss.web.adminuser.EditAdminUserPage;
import org.cipango.ims.hss.web.adminuser.SetPasswordPage;
import org.cipango.ims.hss.web.as.AsBrowserPage;
import org.cipango.ims.hss.web.as.DeleteAsPage;
import org.cipango.ims.hss.web.as.EditAsPage;
import org.cipango.ims.hss.web.debugsession.EditDebugSessionPage;
import org.cipango.ims.hss.web.ifc.DeleteIfcPage;
import org.cipango.ims.hss.web.ifc.EditIfcPage;
import org.cipango.ims.hss.web.ifc.IfcBrowserPage;
import org.cipango.ims.hss.web.ifc.ViewIfcPage;
import org.cipango.ims.hss.web.privateid.DeletePrivateIdPage;
import org.cipango.ims.hss.web.privateid.EditPrivateIdPage;
import org.cipango.ims.hss.web.privateid.EditPublicIdsPage;
import org.cipango.ims.hss.web.privateid.PrivateIdBrowserPage;
import org.cipango.ims.hss.web.publicid.DeletePublicIdPage;
import org.cipango.ims.hss.web.publicid.EditPsiPage;
import org.cipango.ims.hss.web.publicid.EditPublicUserIdPage;
import org.cipango.ims.hss.web.publicid.PublicIdBrowserPage;
import org.cipango.ims.hss.web.publicid.XmlSubscriptionPage;
import org.cipango.ims.hss.web.scscf.DeleteScscfPage;
import org.cipango.ims.hss.web.scscf.EditScscfPage;
import org.cipango.ims.hss.web.scscf.ScscfBrowserPage;
import org.cipango.ims.hss.web.serviceprofile.DeleteServiceProfilePage;
import org.cipango.ims.hss.web.serviceprofile.EditIfcsPage;
import org.cipango.ims.hss.web.serviceprofile.EditServiceProfilePage;
import org.cipango.ims.hss.web.serviceprofile.ServiceProfileBrowserPage;
import org.cipango.ims.hss.web.serviceprofile.ViewServiceProfilePage;
import org.cipango.ims.hss.web.spt.EditSptsPage;
import org.cipango.ims.hss.web.subscription.AddSubscriptionPage;
import org.cipango.ims.hss.web.subscription.DeleteSubscriptionPage;
import org.cipango.ims.hss.web.subscription.DeregistrationPage;
import org.cipango.ims.hss.web.subscription.EditImplicitSetPage;
import org.cipango.ims.hss.web.subscription.EditSubscriptionPage;
import org.cipango.ims.hss.web.subscription.SubscriptionBrowserPage;
import org.cipango.ims.hss.web.subscription.ViewSubscriptionPage;
import org.cipango.ims.hss.web.util.ClassResolver;
import org.cipango.ims.hss.web.util.SignOutPage;


public class ImsApplication extends WebApplication {

	private static ImsApplication __instance;
	private SpringComponentInjector _injector;

	private boolean _wicketStarted = false;
	private SipFactory _sipFactory;
	private boolean _webAuthentication = true;
	
	private AdminUserDao _adminUserDao;
	
	private ImsApplication() {	
	}
	
	public void springStart() {
		// If Wicket not started could not addComponentInstantiationListener.
		// As Wicket is not managed, ensure a new injector is set after refresh.
		if (_wicketStarted) {
			_injector = new SpringComponentInjector(this);
			addComponentInstantiationListener(_injector);	
			
			if (_webAuthentication)
			{
				AuthorizationStrategy authStrat = new AuthorizationStrategy();
			    ISecuritySettings securitySettings = getSecuritySettings();
			    securitySettings.setAuthorizationStrategy(authStrat);
			    securitySettings.setUnauthorizedComponentInstantiationListener(authStrat);
			}
		}
	}
	
	public void springStop() {
		removeComponentInstantiationListener(_injector);	
	}
	
	@Override
	protected void init() {
		super.init();
		
		// Need to change class resolver due a weird ClassNotFound (on Java 6 only) thrown on
		// Classes.resolveClass("[B]")		
		getApplicationSettings().setClassResolver(new ClassResolver());
				
		String[] id = new String[] {"id"};
		
		mountBookmarkablePage("/subscriptions/browser", SubscriptionBrowserPage.class); 
		mount(new MixedParamUrlCodingStrategy("/subscription/add", AddSubscriptionPage.class, id));
		mount(new MixedParamUrlCodingStrategy("/subscription/edit", EditSubscriptionPage.class, id));
		mount(new MixedParamUrlCodingStrategy("/subscription/delete", DeleteSubscriptionPage.class, id));
		mount(new MixedParamUrlCodingStrategy("/subscription", ViewSubscriptionPage.class, id));
		mount(new MixedParamUrlCodingStrategy("/network-deregistration", DeregistrationPage.class, id));
		
		mount(new MixedParamUrlCodingStrategy("/private-identity/edit", EditPrivateIdPage.class, id));
		mount(new MixedParamUrlCodingStrategy("/private-identity/delete", DeletePrivateIdPage.class, id));
		mount(new MixedParamUrlCodingStrategy("/private-identity/public-ids", EditPublicIdsPage.class, id));
		mountBookmarkablePage("/private-identities/browser", PrivateIdBrowserPage.class); 
		
		mount(new MixedParamUrlCodingStrategy("/public-user-identity/edit", EditPublicUserIdPage.class, id));
		mount(new MixedParamUrlCodingStrategy("/public-identity/delete", DeletePublicIdPage.class, id));
		mount(new MixedParamUrlCodingStrategy("/public-service-identity/edit", EditPsiPage.class, id));
		mount(new MixedParamUrlCodingStrategy("/public-identity/xml", XmlSubscriptionPage.class, id));
		mountBookmarkablePage("/public-identities/browser", PublicIdBrowserPage.class); 
		
		mount(new MixedParamUrlCodingStrategy("/s-cscf/edit", EditScscfPage.class, id));
		mount(new MixedParamUrlCodingStrategy("/s-cscf/delete", DeleteScscfPage.class, id));
		mountBookmarkablePage("/s-cscf/browser", ScscfBrowserPage.class);
		
		mount(new MixedParamUrlCodingStrategy("/admin/user/edit", EditAdminUserPage.class, id));
		mount(new MixedParamUrlCodingStrategy("/admin/user/set-password", SetPasswordPage.class, id));
		mount(new MixedParamUrlCodingStrategy("/admin/user/delete", DeleteAdminUserPage.class, id));
		mountBookmarkablePage("/admin/user/browser", AdminUserBrowserPage.class);
		
		mount(new MixedParamUrlCodingStrategy("/application-server/edit", EditAsPage.class, id));
		mount(new MixedParamUrlCodingStrategy("/application-server/delete", DeleteAsPage.class, id));
		mountBookmarkablePage("/application-server/browser", AsBrowserPage.class);
		
		mount(new MixedParamUrlCodingStrategy("/ifc/edit", EditIfcPage.class, id));
		mount(new MixedParamUrlCodingStrategy("/ifc/delete", DeleteIfcPage.class, id));
		mount(new MixedParamUrlCodingStrategy("/ifc/view", ViewIfcPage.class, id));
		mountBookmarkablePage("/icfs/browser", IfcBrowserPage.class);
		
		mount(new MixedParamUrlCodingStrategy("/service-profile", ViewServiceProfilePage.class, id));
		mount(new MixedParamUrlCodingStrategy("/service-profile/edit", EditServiceProfilePage.class, id));
		mount(new MixedParamUrlCodingStrategy("/service-profile/ifcs", EditIfcsPage.class, id));
		mount(new MixedParamUrlCodingStrategy("/service-profile/delete", DeleteServiceProfilePage.class, id));
		mountBookmarkablePage("/service-profiles/browser", ServiceProfileBrowserPage.class);
		
		mount(new MixedParamUrlCodingStrategy("/spt/edit", EditSptsPage.class, id));
		mount(new MixedParamUrlCodingStrategy("/implicit-registration-state/edit", EditImplicitSetPage.class, id));
			
		mount(new MixedParamUrlCodingStrategy("/debug-session/edit", EditDebugSessionPage.class, id));
		mountBookmarkablePage("/signin", SigninPage.class);
		mountBookmarkablePage("/signout", SignOutPage.class);
				
		_wicketStarted = true;
		springStart();
	}
	
	@Override
	protected IConverterLocator newConverterLocator() {
		ConverterLocator converterLocator = (ConverterLocator) super.newConverterLocator();
	    converterLocator.set(byte[].class, new ByteArrayConverter());
	    return converterLocator;
	}
		
	@Override
	public Class<? extends Page> getHomePage() {
		return Index.class;
	}
	
	public SipFactory getSipFactory()
	{
		return _sipFactory;
	}
	
	public String getContextPath()
	{
		return getServletContext().getContextPath();
	}
		
	@Override
	public org.apache.wicket.Session newSession(Request request, Response response) {
		return new ImsSession(request);
	}
	
	/**
	 * As Wicket is not managed, ensure only one bean is created even if refresh is done.
	 * @return
	 */
	public static ImsApplication getInstance() {
		if (__instance == null)
			__instance = new ImsApplication();
		return __instance;
	}
	
	class ByteArrayConverter implements IConverter {

		public Object convertToObject(String value, Locale locale) {
			try {
				return HexString.fromHexString(value);
			} catch (Exception e) {
				throw new ConversionException("'" + value + "' is not a valid hexadecimal characters sequence");
			}
		}

		public String convertToString(Object value, Locale locale) {
			return HexString.toHexString((byte[]) value);
		}
		
	}

	public boolean isWebAuthentication()
	{
		return _webAuthentication;
	}

	public void setWebAuthentication(boolean webAuthentication)
	{
		_webAuthentication = webAuthentication;
	}

	public void setSipFactory(SipFactory sipFactory)
	{
		_sipFactory = sipFactory;
	}

	public AdminUserDao getAdminUserDao()
	{
		return _adminUserDao;
	}

	public void setAdminUserDao(AdminUserDao adminUserDao)
	{
		_adminUserDao = adminUserDao;
	}
}
