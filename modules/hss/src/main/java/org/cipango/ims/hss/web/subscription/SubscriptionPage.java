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
package org.cipango.ims.hss.web.subscription;

import org.apache.wicket.PageParameters;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.collections.MicroMap;
import org.apache.wicket.util.string.interpolator.MapVariableInterpolator;
import org.cipango.ims.hss.db.SubscriptionDao;
import org.cipango.ims.hss.model.Subscription;
import org.cipango.ims.hss.web.BasePage;

public abstract class SubscriptionPage extends BasePage
{

	@SpringBean
	protected SubscriptionDao _dao;
	
	@SuppressWarnings("unchecked")
	protected Subscription getSubscription(PageParameters pageParameters)
	{
		String key = pageParameters.getString("id");
		Subscription subscription = null;
		if (key != null)
		{
			subscription = _dao.findById(key);
			if (subscription == null)
			{
				error(MapVariableInterpolator.interpolate(getString(getPrefix() + ".error.notFound"),
						new MicroMap("id", key)));
				key = null;
			}
		}
		return subscription;
	}
	
	
	protected String getPrefix()
	{
		return "subscription";
	}

	public class DaoDetachableModel extends LoadableDetachableModel<Subscription>
	{
		private String key;

		public DaoDetachableModel(String key)
		{
			this.key = key;
		}

		public DaoDetachableModel(Subscription o)
		{
			super(o);
			if (o != null)
				this.key = o.getName();
		}

		@Override
		protected Subscription load()
		{
			if (key == null)
				return new Subscription();
			else
				return _dao.findById(key);
		}
	}

}
