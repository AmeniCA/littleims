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
package org.cipango.ims.hss.web.serviceprofile;

import org.apache.wicket.PageParameters;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.cipango.ims.hss.db.ServiceProfileDao;
import org.cipango.ims.hss.model.ServiceProfile;
import org.cipango.ims.hss.web.BasePage;

public abstract class ServiceProfilePage extends BasePage
{

	@SpringBean
	protected ServiceProfileDao _dao;
	
	public ServiceProfilePage(PageParameters pageParameters)
	{
		super(pageParameters);
	}
	
	protected String getPrefix()
	{
		return "serviceProfile";
	}

	public class DaoDetachableModel extends LoadableDetachableModel<ServiceProfile>
	{
		private String key;

		public DaoDetachableModel(String key)
		{
			this.key = key;
		}

		public DaoDetachableModel(ServiceProfile serviceProfile)
		{
			super(serviceProfile);
			if (serviceProfile != null)
				this.key = serviceProfile.getName();
		}
		
		public DaoDetachableModel(ServiceProfile serviceProfile, boolean copy)
		{
			super(serviceProfile);
			if (!copy && serviceProfile != null)
				this.key = serviceProfile.getName();
		}

		@Override
		protected ServiceProfile load()
		{
			if (key == null)
				return new ServiceProfile();
			else
				return _dao.findById(key);
		}
	}

}
