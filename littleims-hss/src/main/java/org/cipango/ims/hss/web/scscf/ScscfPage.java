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
package org.cipango.ims.hss.web.scscf;

import org.apache.wicket.PageParameters;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.cipango.ims.hss.db.ScscfDao;
import org.cipango.ims.hss.model.Scscf;
import org.cipango.ims.hss.web.BasePage;

public abstract class ScscfPage extends BasePage
{

	@SpringBean
	protected ScscfDao _dao;
		
	public ScscfPage(PageParameters pageParameters)
	{
		super(pageParameters);
	}
	
	protected String getPrefix()
	{
		return "scscf";
	}

	public class DaoDetachableModel extends LoadableDetachableModel<Scscf>
	{
		private String key;

		public DaoDetachableModel(String key)
		{
			this.key = key;
		}

		public DaoDetachableModel(Scscf o)
		{
			super(o);
			if (o != null)
				this.key = o.getName();
		}

		@Override
		protected Scscf load()
		{
			if (key == null)
				return new Scscf();
			else
				return _dao.findById(key);
		}
	}

}
