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
package org.cipango.ims.hss.web;

import org.apache.wicket.Component;
import org.apache.wicket.RestartResponseAtInterceptPageException;
import org.apache.wicket.authorization.Action;
import org.apache.wicket.authorization.IAuthorizationStrategy;
import org.apache.wicket.authorization.IUnauthorizedComponentInstantiationListener;

public class AuthorizationStrategy implements IAuthorizationStrategy, IUnauthorizedComponentInstantiationListener
{

	public boolean isActionAuthorized(Component component, Action action)
	{
		return true;
	}

	public <T extends Component> boolean isInstantiationAuthorized(
			Class<T> componentClass)
	{
		if (Index.class.isAssignableFrom(componentClass))
			return true;
		if (BasePage.class.isAssignableFrom(componentClass))
			return ImsSession.get().isAuthenticated();
		return true;
	}

	public void onUnauthorizedInstantiation(Component component)
	{
		throw new RestartResponseAtInterceptPageException(SigninPage.class);
	}

}
