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

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.RefreshingView;
import org.apache.wicket.markup.repeater.util.ModelIteratorAdapter;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.util.collections.MicroMap;
import org.apache.wicket.util.string.interpolator.MapVariableInterpolator;
import org.cipango.ims.hss.model.ServiceProfile;
import org.cipango.ims.hss.web.ifc.ViewIfcPanel;

public class ViewServiceProfilePage extends ServiceProfilePage
{
	
	private String _key;
	private String _title;

	@SuppressWarnings("unchecked")
	public ViewServiceProfilePage(PageParameters pageParameters)
	{
		_key = pageParameters.getString("id");
		ServiceProfile serviceProfile = null;
		if (_key != null)
		{
			serviceProfile = _dao.findById(_key);
			if (serviceProfile == null)
			{
				error(MapVariableInterpolator.interpolate(getString(getPrefix() + ".error.notFound"),
						new MicroMap("identity", _key)));
				_key = null;
			}
		}
		
		_title = getString("view.serviceProfile.title", new DaoDetachableModel(serviceProfile));
		add(new Label("title", _title));
		
		IModel ifcsModel = new LoadableDetachableModel(serviceProfile == null ? Collections.EMPTY_SET : serviceProfile.getIfcs()) {
			@Override
			protected Object load()
			{
				return _dao.findById(_key).getIfcs();
			}
			
		};
		add(new RefreshingView("ifcs", ifcsModel)
		{
			@Override
			protected Iterator getItemModels()
			{
				return new CompoundModelIterator((Collection) getDefaultModelObject());
			}

			@Override
			protected void populateItem(Item item)
			{
				item.add(new ViewIfcPanel("ifc", item.getModel(), false));
			}
		});
		
		IModel sharedIfcsModel = new LoadableDetachableModel(serviceProfile == null ? Collections.EMPTY_SET : serviceProfile.getSharedIfcs()) {
			@Override
			protected Object load()
			{
				return _dao.findById(_key).getSharedIfcs();
			}
			
		};

		add(new RefreshingView("sharedIfcs", sharedIfcsModel)
		{
			@Override
			protected Iterator getItemModels()
			{
				return new CompoundModelIterator((Collection) getDefaultModelObject());
			}

			@Override
			protected void populateItem(Item item)
			{
				item.add(new ViewIfcPanel("ifc", item.getModel(), true));
			}
		});
		
		if (serviceProfile != null)
			setContextMenu(new ContextPanel(serviceProfile));
	}
	
	@Override
	public String getTitle()
	{
		return _title;
	}
		
	class CompoundModelIterator extends ModelIteratorAdapter implements Serializable {
		public CompoundModelIterator(Collection modelObject) {
			super(modelObject.iterator());
		}
		
		@Override
		protected IModel model(Object id)
		{
			return new CompoundPropertyModel(new LoadableDetachableModel(id) {

				@Override
				protected Object load()
				{
					// TODO implements
					return null;
				}
				
			});
		}
	}

}
