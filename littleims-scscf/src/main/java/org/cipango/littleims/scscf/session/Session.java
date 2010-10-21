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
package org.cipango.littleims.scscf.session;

import java.io.IOException;
import java.util.Iterator;

import javax.servlet.ServletException;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipURI;
import javax.servlet.sip.TelURL;
import javax.servlet.sip.URI;

import org.apache.log4j.Logger;
import org.cipango.littleims.scscf.data.InitialFilterCriteria;
import org.cipango.littleims.scscf.data.UserProfile;
import org.cipango.littleims.scscf.data.InitialFilterCriteria.SessionCase;
import org.cipango.littleims.util.Headers;


public abstract class Session
{

	private SessionManager _sessionManager;
	private UserProfile _profile;

	private Iterator<InitialFilterCriteria> _ifcIterator;
	private SipURI _ownURI;
	private InitialFilterCriteria _currentIFc;
	
	private static final Logger __log = Logger.getLogger(Session.class);

	public static final String ORIGINAL_DIALOG_IDENTIFIER_PARAM = "nxid";

	public Session(UserProfile profile)
	{
		_profile = profile;
		_ifcIterator = profile.getServiceProfile().getIFCsIterator();
	}

	public void setSessionManager(SessionManager sessionManager)
	{
		_sessionManager = sessionManager;
	}

	public abstract boolean handleInitialRequest(SipServletRequest request) throws IOException,
			ServletException;
	
	public abstract boolean isOriginating();
	
	public abstract SessionCase getSessionCase();
	
	public UserProfile getProfile()
	{
		return _profile;
	}

	protected InitialFilterCriteria nextIFC()
	{
		if (_ifcIterator.hasNext())
		{
			_currentIFc = _ifcIterator.next();
		}
		else
		{
			_currentIFc = null;
		}
		return _currentIFc;
	}

	public void setOwnURI(SipURI uri)
	{
		_ownURI = uri;
	}

	public SipURI getOwnURI()
	{
		return _ownURI;
	}

	protected void routeRequest(SipServletRequest request) throws IOException, ServletException
	{
		URI requestURI = request.getRequestURI();

		if (requestURI instanceof TelURL)
		{
			__log.debug("Called Party: " + requestURI + " is a tel URL. Trying ENUM translation");
			SipURI translatedRURI = null;
			try
			{
				translatedRURI = _sessionManager.getEnumClient().translate((TelURL) requestURI);
				if (translatedRURI != null)
				{
					request.setRequestURI(translatedRURI);
				}
			}
			catch (Exception e)
			{
				__log.info("ENUM translation failed", e);
			}
		}
		request.getProxy().setRecurse(false);
		request.getProxy().setRecordRoute(true);

		if (request.getAddressHeader(Headers.ROUTE) != null)
		{
			__log.info("Routing on Route: " + request.getAddressHeader(Headers.ROUTE));
			// request.getProxy().proxyTo(request.getRequestURI());
		}
		else
		{
			requestURI = request.getRequestURI();
			if (!requestURI.isSipURI())
			{
				if (requestURI instanceof TelURL)
				{
					if (_sessionManager.getBgcfUri() == null)
					{
						__log.info("TelURI: " + requestURI + " and no BGCF. Sending 404.");
						_sessionManager.getMessageSender().sendResponse(request, SipServletResponse.SC_NOT_FOUND);
						return;
					}
					else
					{
						__log.info("TelURI: " + requestURI + ". Forwarding to BGCF");
						request.pushRoute(_sessionManager.getBgcfUri());
					}
				}
				else
				{
					__log.info("Unsupported URI scheme: " + requestURI);
					_sessionManager.getMessageSender().sendResponse(request, SipServletResponse.SC_UNSUPPORTED_URI_SCHEME);
					return;
				}
			}
			else
			{
				if (__log.isDebugEnabled())
					__log.debug("Forwarding to terminating network: " + ((SipURI) requestURI).getHost());

				if (_sessionManager.getIcscfUri() != null)
				{
					__log.trace("Forwarding request to ICSCF");
					request.pushRoute(_sessionManager.getIcscfUri());
				}
				else
					__log.debug("Forwarding request for " + request.getRequestURI()
							+ " using request URI");

			}
		}
		request.removeHeader(Headers.P_SERVED_USER);
		request.getProxy().proxyTo(request.getRequestURI());
	}

	protected SessionManager getSessionManager()
	{
		return _sessionManager;
	}
	
	public InitialFilterCriteria getCurrentIfc()
	{
		return _currentIFc;
	}
}
