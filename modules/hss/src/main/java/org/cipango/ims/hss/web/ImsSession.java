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

import java.util.Hashtable;

import org.apache.wicket.Page;
import org.apache.wicket.Request;
import org.apache.wicket.protocol.http.WebSession;
import org.cipango.ims.hss.model.AdminUser;



public class ImsSession extends WebSession {

	private Hashtable<Class<?>, Page> backPages = new Hashtable<Class<?>, Page>();

	private AdminUser _adminUser;
	
	public ImsSession(Request request) {
		super(request);
	}

	public static ImsSession get()
	{
		return (ImsSession) WebSession.get();
	}

	@Override
	protected void detach() {
		super.detach();
	}

	public void setBackPage(Class<?> clazz, Page page) {
		backPages.put(clazz, page);
	}
	
	public Page getBackPage(Class<?> clazz) {
		return backPages.get(clazz);
	}

	public boolean isAuthenticated()
	{
		return _adminUser != null;
	}

	public AdminUser getAdminUser()
	{
		return _adminUser;
	}


	public void setAdminUser(AdminUser adminUser)
	{
		_adminUser = adminUser;
	}
	
}
