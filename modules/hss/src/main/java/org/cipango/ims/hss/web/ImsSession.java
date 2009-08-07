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
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.protocol.http.WebSession;
import org.cipango.ims.hss.model.AdminUser;

public class ImsSession extends WebSession
{

	private Hashtable<Class<?>, Page> backPages = new Hashtable<Class<?>, Page>();

	private DaoDetachableModel _adminModel;

	public ImsSession(Request request)
	{
		super(request);
	}

	public static ImsSession get()
	{
		return (ImsSession) WebSession.get();
	}

	@Override
	protected void detach()
	{
		if (_adminModel != null)
			_adminModel.detach();
		super.detach();
	}

	public void setBackPage(Class<?> clazz, Page page)
	{
		backPages.put(clazz, page);
	}

	public Page getBackPage(Class<?> clazz)
	{
		return backPages.get(clazz);
	}

	public boolean isAuthenticated()
	{
		return _adminModel != null;
	}
	
	public String getLogin()
	{
		return _adminModel == null ? null : _adminModel.getLogin();
	}
	
	public DaoDetachableModel getAdminUserModel()
	{
		return _adminModel;
	}

	public AdminUser getAdminUser()
	{
		return _adminModel.getObject();
	}

	public void setAdminUser(AdminUser adminUser)
	{
		_adminModel = new DaoDetachableModel(adminUser);
	}
	
	

	public class DaoDetachableModel extends LoadableDetachableModel<AdminUser>
	{
		private String _key;

		public DaoDetachableModel(AdminUser o)
		{
			super(o);
			if (o != null)
				_key = o.getLogin();
		}
		
		public String getLogin()
		{
			return _key;
		}

		@Override
		protected AdminUser load()
		{
			if (_key == null)
				return new AdminUser();
			else
				return ((ImsApplication) ImsSession.this.getApplication()).getAdminUserDao().findById(_key);
		}
	}

	@Override
	public void invalidate()
	{
		super.invalidate();
		_adminModel = null;
	}

}
