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
package org.cipango.ims.hss.web.privateid;

import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.cipango.ims.hss.db.PrivateIdentityDao;
import org.cipango.ims.hss.model.PrivateIdentity;
import org.cipango.ims.hss.web.BasePage;

public abstract class PrivateIdentityPage extends BasePage
{

	@SpringBean
	protected PrivateIdentityDao _dao;
	
	protected String getPrefix()
	{
		return "privateId";
	}

	public class DaoDetachableModel extends LoadableDetachableModel<PrivateIdentity>
	{
		private String key;

		public DaoDetachableModel(String key)
		{
			this.key = key;
		}

		public DaoDetachableModel(PrivateIdentity privateIdentity)
		{
			super(privateIdentity);
			if (privateIdentity != null)
				this.key = privateIdentity.getIdentity();
		}

		@Override
		protected PrivateIdentity load()
		{
			if (key == null)
				return new PrivateIdentity();
			else
				return _dao.findById(key);
		}
	}

}
