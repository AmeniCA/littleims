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
package org.cipango.littleims.pcscf.subscription.debug;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipSession;

import org.cipango.ims.pcscf.debug.data.DebugconfigDocument.Debugconfig;
import org.cipango.ims.pcscf.debug.data.DebuginfoDocument;
import org.cipango.ims.pcscf.debug.data.DebuginfoDocument.Debuginfo;
import org.cipango.littleims.pcscf.subscription.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DebugSubscription implements Subscription
{
	
	private static final Logger __log = LoggerFactory.getLogger(DebugSubscription.class);
	
	private int _version = -1;
	private List<DebugConf> _configs = new ArrayList<DebugConf>();
	private DebugIdService _debugIdService;
	private SipSession _session;
	private String _aor;
	
	public DebugSubscription(DebugIdService service, SipSession session, String aor)
	{
		_debugIdService = service;
		_session = session;
		_aor = aor;
	}
		
	public void handleNotify(SipServletRequest notify)
	{
		try
		{
			DebuginfoDocument doc = DebuginfoDocument.Factory.parse(new String(notify.getRawContent()));
			Debuginfo debuginfo = doc.getDebuginfo();
			
			int version = debuginfo.getVersion().intValue();

			__log.debug("Received NOTIFY No " + version + " for debug event for user " + _aor);
			if (version <= _version)
			{
				__log.warn("Discard notify request to " + notify.getFrom().getURI() + ": invalid version number: " + 
						version + " <= " + _version);
				return;
			}
			if (version > _version + 1 && debuginfo.getState() != Debuginfo.State.FULL)
			{
				//TODO request new full state.
			}
			_version = version;
			
			
			List<DebugConf> toRemove = new ArrayList<DebugConf>(_configs);
			
			for (Debugconfig debugConfig : debuginfo.getDebugconfigArray())
			{
				String aor = debugConfig.getAor();
				DebugConf debugConf = getDebugconfig(aor);
				if (debugConf != null)
				{
					debugConf.updateConfig(debugConfig);
					toRemove.remove(debugConf);
				}
				else
				{
					debugConf = new DebugConf(debugConfig);
					_configs.add(debugConf);
					_debugIdService.addDebugConf(debugConf);
				}
			}
			
			if (debuginfo.getState() == Debuginfo.State.FULL)
			{
				for (DebugConf debugConf : toRemove)
				{
					_debugIdService.removeDebugConf(debugConf);
					_configs.remove(debugConf);
				}
			}
		}
		catch (Exception e)
		{
			__log.warn("Failed to handle NOTIFY\n" + notify, e);
		}
	}
	
	public void invalidate()
	{
		__log.debug("Remove debug subscription for user " + _aor);
		_session.invalidate();
		for (DebugConf debugConf : _configs)
			_debugIdService.removeDebugConf(debugConf);
		_debugIdService.removeSubscription(this);
	}
	
	public SipSession getSession()
	{
		return _session;
	}

	private DebugConf getDebugconfig(String aor)
	{
		Iterator<DebugConf> it = _configs.iterator();
		while (it.hasNext())
		{
			DebugConf debugconfig = it.next();
			if (aor.equals(debugconfig.getAor()))
				return debugconfig;
		}
		return null;
	}

	public String getAor()
	{
		return _aor;
	}

	public int getVersion()
	{
		return _version;
	}

	public List<DebugConf> getConfigs()
	{
		return _configs;
	}

	public DebugIdService getDebugIdService()
	{
		return _debugIdService;
	}

	public String getUserAgent()
	{
		return _debugIdService.getUserAgent();
	}
}
