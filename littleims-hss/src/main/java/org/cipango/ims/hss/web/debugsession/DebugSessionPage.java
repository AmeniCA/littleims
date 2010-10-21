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
package org.cipango.ims.hss.web.debugsession;

import org.apache.wicket.PageParameters;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.cipango.ims.hss.db.DebugSessionDao;
import org.cipango.ims.hss.model.DebugSession;
import org.cipango.ims.hss.web.BasePage;
import org.cipango.ims.oam.util.ID;

public abstract class DebugSessionPage extends BasePage
{

	@SpringBean
	protected DebugSessionDao _dao;
	
	public DebugSessionPage(PageParameters pageParameters)
	{
		super(pageParameters);
	}
	
	protected String getPrefix()
	{
		return "debugSession";
	}

	public class DaoDetachableModel extends LoadableDetachableModel<DebugSession>
	{
		private Long _key;

		public DaoDetachableModel(Long key)
		{
			_key = key;
		}

		public DaoDetachableModel(DebugSession debugSession)
		{
			super(debugSession);
			if (debugSession != null)
				_key = debugSession.getId();
		}

		@Override
		protected DebugSession load()
		{
			if (_key == null)
			{
				DebugSession debugSession = new DebugSession();
				debugSession.setDebugId(ID.newDebugId());
				return debugSession;
			}
			else
				return _dao.findById(_key);
		}
	}

}
