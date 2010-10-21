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
package org.cipango.ims;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;

import org.cipango.server.SipConnection;
import org.cipango.server.SipMessage;
import org.cipango.server.SipResponse;
import org.cipango.server.log.AbstractMessageLog;
import org.eclipse.jetty.io.Buffer;
import org.eclipse.jetty.util.StringUtil;

public class DebugIdMessageLog extends AbstractMessageLog
{
	
	private static final String P_DEBUG_ID = "P-Debug-ID";
	
	private Object _lock = new Object();
	private Map<String, OutputStream> _outputstreams = new HashMap<String, OutputStream>();
	
	private File _logDirectory;
	
	@Override
	public void doLog(SipMessage message, int direction, SipConnection connection) throws IOException
	{
		String debugId = message.getHeader(P_DEBUG_ID);
		if (debugId == null && message instanceof SipServletResponse)
		{
			SipServletRequest request = ((SipServletResponse) message).getRequest();
			if (request != null)
				debugId = request.getHeader(P_DEBUG_ID);
		}
		if (debugId == null || debugId.trim().equals(""))
			return;
		OutputStream os = getOutputStream(debugId);
		synchronized (os)
		{
			os.write(generateInfoLine(direction, connection, System.currentTimeMillis()).getBytes()); 
            Buffer buffer = generateMessage(message);
    		os.write(buffer.array(), 0, buffer.length());
    		os.write(StringUtil.__LINE_SEPARATOR.getBytes());
    		os.flush();
		}
		if (message instanceof SipServletResponse)
		{
			synchronized (_lock)
			{

				SipResponse response = (SipResponse) message;
				if (response.isBye() || (!response.isBranchResponse() && response.getStatus() >= 300))
				{
					os.close();
					_outputstreams.remove(debugId);
				}
			}
				
		}
	}
	
	private OutputStream getOutputStream(String debugId) throws IOException
	{
		synchronized (_lock)
		{
			OutputStream os = _outputstreams.get(debugId);
			if (os == null)
			{
				File file = new File(_logDirectory, debugId + ".log");
				if (!file.exists())
					file.createNewFile();
				os = new FileOutputStream(file, true);
				_outputstreams.put(debugId, os);
			}
			return os;
		}
	}

	@Override
	protected void doStart() throws Exception
	{
		super.doStart();
		if (_logDirectory == null)
			throw new IllegalStateException("No logs directory set");
		_logDirectory.mkdirs();
	}

	public File getLogDirectory()
	{
		return _logDirectory;
	}

	public void setLogDirectory(File logDirectory)
	{
		_logDirectory = logDirectory;
	}

}
