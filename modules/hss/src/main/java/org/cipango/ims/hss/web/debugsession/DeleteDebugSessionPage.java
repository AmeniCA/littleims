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
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.util.collections.MicroMap;
import org.apache.wicket.util.string.interpolator.MapVariableInterpolator;
import org.cipango.ims.hss.model.PSI;
import org.cipango.ims.hss.model.PublicIdentity;
import org.cipango.ims.hss.model.PublicUserIdentity;


public class DeleteDebugSessionPage extends DebugSessionPage {
	
	@SuppressWarnings("unchecked")
	public DeleteDebugSessionPage(PageParameters pageParameters) {
		/*final String key = pageParameters.getString("id");
		PublicIdentity publicIdentity = _dao.findById(key);
		if (publicIdentity == null) {
			error(MapVariableInterpolator.interpolate(getString(getPrefix() + ".error.notFound"),
					new MicroMap("name", key)));
			goToBackPage(PublicIdBrowserPage.class);
			return;
		}

		add(new Label("delete.confirm", 
				getString(getPrefix() + ".delete.confirm", new DaoDetachableModel(publicIdentity))));


		Form form = new Form("confirmForm");

		form.add(new Button("delete") {
			public void onSubmit() {
				PublicIdentity id = _dao.findById(key);

				_dao.delete(id);

				getSession().info(getString(getPrefix() + ".delete.done", new DaoDetachableModel(id)));
				
				goToBackPage(PublicIdBrowserPage.class);
			}
		}.setDefaultFormProcessing(false));

		form.add(new Button("cancel") {
			public void onSubmit() {
				getSession().info(getString(getPrefix() + ".delete.canceled", new DaoDetachableModel(key)));
				goToBackPage(PublicIdBrowserPage.class);
			}
		}.setDefaultFormProcessing(false));

		add(form);
		
		if (publicIdentity != null)
		{
			if (publicIdentity instanceof PublicUserIdentity)
				setContextMenu(new ContextPanel((PublicUserIdentity) publicIdentity));
			else
				setContextMenu(new PsiContextPanel((PSI) publicIdentity));
		}*/
	}

	@Override
	public String getTitle() {
		return getString(getPrefix() + ".delete.title");
	}
}
