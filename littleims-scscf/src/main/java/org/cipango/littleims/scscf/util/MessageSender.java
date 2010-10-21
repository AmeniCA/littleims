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
package org.cipango.littleims.scscf.util;

import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;

import org.apache.log4j.Logger;
import org.cipango.littleims.util.Headers;

public class MessageSender
{
	private final Logger _log = Logger.getLogger(MessageSender.class);
	private String _userAgent = "littleIMS :: S-CSCF";
	
	public void sendResponse(SipServletRequest request, int statusCode)
	{
		sendResponse(request, statusCode, null, null);
	}
		
	public void sendResponse(SipServletRequest request, int statusCode, String headerName, String headerValue)
	{
		try
		{
			SipServletResponse response = request.createResponse(statusCode);
			String pDebugId = request.getHeader(Headers.P_DEBUG_ID);
			if (pDebugId != null)
				response.setHeader(Headers.P_DEBUG_ID, pDebugId);
			if (_userAgent != null)
				response.setHeader(Headers.SERVER, _userAgent);
			if (headerName != null && headerValue != null)
				response.setHeader(headerName, headerValue);
			response.send();
		}
		catch (Throwable e)
		{
			_log.warn("Failed to send " + statusCode + "/" + request.getMethod(), e);
		}
		finally
		{
			if (request.getApplicationSession().isValid())
				request.getApplicationSession().invalidate();
		}
	}

	public String getUserAgent()
	{
		return _userAgent;
	}

	public void setUserAgent(String userAgent)
	{
		_userAgent = userAgent;
	}
}
