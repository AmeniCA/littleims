// ========================================================================
// Copyright 2009 NEXCOM Systems
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
package org.cipango.littleims.pcscf.oam;

import org.apache.wicket.Page;
import org.apache.wicket.request.target.coding.MixedParamUrlCodingStrategy;
import org.cipango.ims.oam.SpringApplication;
import org.cipango.littleims.pcscf.oam.browser.DebugBrowserPage;
import org.cipango.littleims.pcscf.oam.browser.RegistrationBrowserPage;
import org.cipango.littleims.pcscf.oam.browser.UserPage;

public class OamApplication extends SpringApplication
{
	private static OamApplication __instance;


	@Override
	public Class<? extends Page> getHomePage()
	{
		return RegistrationBrowserPage.class;
	}



	@Override
	protected void init()
	{
		super.init();
		mountBookmarkablePage("/registrations", RegistrationBrowserPage.class); 
		mountBookmarkablePage("/debug-id", DebugBrowserPage.class); 
		String[] id = new String[] {"id"};
		
		mount(new MixedParamUrlCodingStrategy("/user", UserPage.class, id));
	}

	/**
	 * As Wicket is not managed, ensure only one bean is created even if refresh
	 * is done.
	 * 
	 * @return
	 */
	public static OamApplication getInstance()
	{
		if (__instance == null)
			__instance = new OamApplication();
		return __instance;
	}

}
