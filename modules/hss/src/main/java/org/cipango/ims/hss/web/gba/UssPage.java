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
package org.cipango.ims.hss.web.gba;

import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.cipango.ims.hss.db.UssDao;
import org.cipango.ims.hss.model.uss.Uss;
import org.cipango.ims.hss.web.BasePage;

public abstract class UssPage extends BasePage
{

	@SpringBean
	protected UssDao _dao;
	
	protected String getPrefix()
	{
		return "uss";
	}

	public class DaoDetachableModel extends LoadableDetachableModel<Uss>
	{
		private Long _key;

		public DaoDetachableModel(Long key)
		{
			_key = key;
		}

		public DaoDetachableModel(Uss uss)
		{
			super(uss);
			if (uss != null)
				_key = uss.getId();
		}

		@Override
		protected Uss load()
		{
			if (_key == null)
			{
				return new Uss();
			}
			else
				return _dao.findById(_key);
		}
	}

}
