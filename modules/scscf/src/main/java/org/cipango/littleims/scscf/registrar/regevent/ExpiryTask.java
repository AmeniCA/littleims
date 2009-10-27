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

import org.apache.log4j.Logger;

import java.util.TimerTask;

import javax.servlet.sip.SipSession;

public class ExpiryTask extends TimerTask
{
	/**
	 * Logger for this class
	 */
	private static final Logger log = Logger.getLogger(ExpiryTask.class);

	public ExpiryTask(SipSession session, RegEventManager manager)
	{
		this.session = session;
		this.manager = manager;
	}

	public void run()
	{
		try
		{
			log.info("Subscription session has expired");
			RegEvent e = (RegEvent) session.getAttribute(LAST_EVENT);
			manager.sendNotification(e, session, "full");
			manager.removeSubscription(aor, session);
		}
		catch (Throwable e)
		{
			log.error("Unable to send NOTIFY", e);
		}
	}

	private SipSession session;
	private RegEventManager manager;
	private String aor;

	public static final String LAST_EVENT = "lastEvent";

}
