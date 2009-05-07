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
package org.cipango.ims.hss.web.publicid;

import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.cipango.ims.hss.db.PublicIdentityDao;
import org.cipango.ims.hss.model.PSI;
import org.cipango.ims.hss.model.PublicIdentity;
import org.cipango.ims.hss.model.PublicUserIdentity;
import org.cipango.ims.hss.web.BasePage;

public abstract class PublicIdentityPage extends BasePage
{

	@SpringBean
	protected PublicIdentityDao _dao;
	
	protected String getPrefix()
	{
		return "publicId";
	}

	public class DaoDetachableModel extends LoadableDetachableModel<PublicIdentity>
	{
		private String _key;
		private boolean _psi;

		public DaoDetachableModel(String key)
		{
			_key = key;
			_psi = false;
		}

		public DaoDetachableModel(PublicIdentity publicIdentity)
		{
			this(publicIdentity, false);
		}
		
		public DaoDetachableModel(PublicIdentity publicIdentity, boolean psi)
		{
			// Create new PublicIdentity() if null, to have right default value.
			super(publicIdentity == null ? (psi ? new PSI() : new PublicUserIdentity()) : publicIdentity);
			_psi = psi;
			if (publicIdentity != null)
				_key = publicIdentity.getIdentity();

		}

		@Override
		protected PublicIdentity load()
		{
			if (_key == null && _psi)
				return new PSI();
			else if (_key == null)
				return new PublicUserIdentity();
			else
				return _dao.findById(_key);
		}
	}

}
