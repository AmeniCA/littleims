package org.cipango.littleims.pcscf.debug;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.sip.SipServletRequest;

import org.apache.log4j.Logger;
import org.cipango.ims.pcscf.debug.data.DebuginfoDocument;
import org.cipango.ims.pcscf.debug.data.DebugconfigDocument.Debugconfig;
import org.cipango.ims.pcscf.debug.data.DebuginfoDocument.Debuginfo;

public class DebugSubscription
{
	
	private static final Logger __log = Logger.getLogger(DebugSubscription.class);
	
	private int _version = -1;
	private List<DebugConf> _configs = new ArrayList<DebugConf>();
	private DebugIdService _debugIdService;
	
	public DebugSubscription(DebugIdService service)
	{
		_debugIdService = service;
	}
	
	public void handleNotify(SipServletRequest notify)
	{
		try
		{
			DebuginfoDocument doc = DebuginfoDocument.Factory.parse(new String(notify.getRawContent()));
			Debuginfo debuginfo = doc.getDebuginfo();
			
			int version = debuginfo.getVersion().intValue();
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
			
			if (debuginfo.getState() == Debuginfo.State.FULL)
				_configs.clear();
			
			for (Debugconfig debugConfig :debuginfo.getDebugconfigArray())
			{
				String aor = debugConfig.getAor();
				DebugConf debugConf = getDebugconfig(aor);
				if (debugConf != null)
					debugConf.updateConfig(debugConfig);
				else
				{
					debugConf = new DebugConf(debugConfig);
					_configs.add(debugConf);
					_debugIdService.addDebugConf(debugConf);
				}
			}
		}
		catch (Exception e)
		{
			__log.warn("Failed to handle NOTIFY\n" + notify, e);
		}
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
}
