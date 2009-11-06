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
package org.cipango.littleims.scscf.registrar.regevent;

import java.util.TimerTask;

import org.apache.log4j.Logger;

public class ExpiryTask extends TimerTask
{
	/**
	 * Logger for this class
	 */
	private static final Logger log = Logger.getLogger(ExpiryTask.class);
	
	private RegSubscription _subscription;
	private RegEventManager manager;
	private String aor;

	public ExpiryTask(RegSubscription subscription, RegEventManager manager)
	{
		_subscription = subscription;
		this.manager = manager;
	}

	public void run()
	{
		try
		{
			log.info("Reg Subscription " + _subscription + " has expired");
			_subscription.sendNotification(_subscription.getLastEvent(), "full");
			manager.removeSubscription(aor, _subscription);
		}
		catch (Throwable e)
		{
			log.error("Unable to send NOTIFY", e);
		}
	}



}
