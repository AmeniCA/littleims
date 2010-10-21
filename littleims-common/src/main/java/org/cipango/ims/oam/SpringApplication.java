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
package org.cipango.ims.oam;

import java.util.Locale;

import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.resource.loader.BundleStringResourceLoader;
import org.apache.wicket.spring.injection.annot.SpringComponentInjector;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.locator.ResourceStreamLocator;

public abstract class SpringApplication extends WebApplication
{
	private SpringComponentInjector _injector;

	private boolean _wicketStarted = false;

	public void springStart()
	{
		// If Wicket not started could not addComponentInstantiationListener.
		// As Wicket is not managed, ensure a new injector is set after refresh.
		if (_wicketStarted)
		{
			_injector = new SpringComponentInjector(this);
			addComponentInstantiationListener(_injector);
		}
	}

	public void springStop()
	{
		removeComponentInstantiationListener(_injector);
	}

	@Override
	protected void init()
	{
		super.init();
		_wicketStarted = true;
		springStart();
		
		getResourceSettings().addStringResourceLoader(new BundleStringResourceLoader("org.cipango.ims.oam.common"));
	}

}
