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

import org.apache.wicket.IConverterLocator;
import org.apache.wicket.Page;
import org.apache.wicket.Request;
import org.apache.wicket.Response;
import org.apache.wicket.markup.resolver.AutoLinkResolver.AutolinkBookmarkablePageLink;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.target.coding.MixedParamUrlCodingStrategy;
import org.apache.wicket.spring.injection.annot.SpringComponentInjector;
import org.apache.wicket.util.convert.ConversionException;
import org.apache.wicket.util.convert.ConverterLocator;
import org.apache.wicket.util.convert.IConverter;
import org.cipango.ims.hss.db.PrivateIdentityDao;
import org.cipango.ims.hss.util.HexString;
import org.cipango.ims.hss.web.privateid.DeletePrivateIdPage;
import org.cipango.ims.hss.web.privateid.EditPrivateIdPage;
import org.cipango.ims.hss.web.privateid.PrivateIdBrowserPage;
import org.cipango.ims.hss.web.publicid.DeletePublicIdPage;
import org.cipango.ims.hss.web.publicid.EditPublicIdPage;
import org.cipango.ims.hss.web.publicid.PublicIdBrowserPage;
import org.cipango.ims.hss.web.subscription.EditSubscriptionPage;
import org.cipango.ims.hss.web.subscription.SubscriptionBrowserPage;


public class ImsApplication extends WebApplication {

	private static ImsApplication instance;
	private PrivateIdentityDao _privateIdentityDao;
	private SpringComponentInjector injector;

	private boolean wicketStarted = false;
	
	private ImsApplication() {	
	}
	
	public void springStart() {
		// If Wicket not started could not addComponentInstantiationListener.
		// As Wicket is not managed, ensure a new injector is set after refresh.
		if (wicketStarted) {
			injector = new SpringComponentInjector(this);
			addComponentInstantiationListener(injector);	
		}
	}
	
	public void springStop() {
		removeComponentInstantiationListener(injector);	
	}
	
	@Override
	protected void init() {
		super.init();		
		
		// Need to change class resolver due a weird ClassNotFound (on Java 6 only) thrown on
		// Classes.resolveClass("[B]")		
		getApplicationSettings().setClassResolver(new ClassResolver());
		
		getMarkupSettings().setStripWicketTags(true);
		AutolinkBookmarkablePageLink.autoEnable=false;
		
		String[] id = new String[] {"id"};
		
		mountBookmarkablePage("/subscriptions/browser", SubscriptionBrowserPage.class); 
		mount(new MixedParamUrlCodingStrategy("/subscription/edit", EditSubscriptionPage.class, id));
		
		mount(new MixedParamUrlCodingStrategy("/private-identity/edit", EditPrivateIdPage.class, id));
		mount(new MixedParamUrlCodingStrategy("/private-identity/delete", DeletePrivateIdPage.class, id));
		mountBookmarkablePage("/private-identities/browser", PrivateIdBrowserPage.class); 
		
		mount(new MixedParamUrlCodingStrategy("/public-identity/edit", EditPublicIdPage.class, id));
		mount(new MixedParamUrlCodingStrategy("/public-identity/delete", DeletePublicIdPage.class, id));
		mountBookmarkablePage("/public-identities/browser", PublicIdBrowserPage.class); 
		
		/* 
		mountBookmarkablePage("/config/database/view", ViewDatabaseConfig.class);
		mountBookmarkablePage("/config/database/edit", EditDatabaseConfig.class);
		mountBookmarkablePage("/config/database/wizard", WizardDatabaseConfig.class);
		
		mount(new MixedParamUrlCodingStrategy("/status/registrations", Index.class, id));*/
		
		wicketStarted = true;
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
		
	@Override
	public org.apache.wicket.Session newSession(Request request, Response response) {
		return new ImsSession(request);
	}

	public PrivateIdentityDao getPrivateIdentityDao()
	{
		return _privateIdentityDao;
	}

	public void setPrivateIdentityDao(PrivateIdentityDao privateIdentityDao)
	{
		_privateIdentityDao = privateIdentityDao;
	}
	
	/**
	 * As Wicket is not managed, ensure only one bean is created even if refresh is done.
	 * @return
	 */
	public static ImsApplication getInstance() {
		if (instance == null)
			instance = new ImsApplication();
		return instance;
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
}
