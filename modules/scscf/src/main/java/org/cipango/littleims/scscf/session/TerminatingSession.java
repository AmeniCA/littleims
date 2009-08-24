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
import java.util.*;

import javax.servlet.ServletException;
import javax.servlet.sip.*;

import org.apache.log4j.Logger;
import org.cipango.littleims.scscf.data.InitialFilterCriteria;
import org.cipango.littleims.scscf.data.UserProfile;
import org.cipango.littleims.scscf.data.InitialFilterCriteria.SessionCase;
import org.cipango.littleims.scscf.registrar.Binding;
import org.cipango.littleims.scscf.registrar.Context;
import org.cipango.littleims.util.Headers;


public class TerminatingSession extends Session
{
	public static final String PRIVACY_ID = "id";
	
	private static final Logger __log = Logger.getLogger(TerminatingSession.class);
	private URI _originalURI;
	private List<Binding> _contacts;

	private Context _context;

	public TerminatingSession(UserProfile profile, Context context)
	{
		super(profile);
		_context = context;
	}

	@Override
	public boolean handleInitialRequest(SipServletRequest request) throws IOException,
			ServletException
	{

		URI requestURI = request.getRequestURI();

		// get contact list if user is registered

		if (_context != null)
		{
			// user is registered, get contact information
			_contacts = _context.getBindings();
		}

		// 1. Check that user is not barred
		if (getProfile() == null || getProfile().isBarred())
		{
			__log.info("Barred user: " + requestURI + ". Sending 404 response");
			request.createResponse(SipServletResponse.SC_NOT_FOUND, "Barred identity").send();
			return true;
		}

		// 2. Remove own URI from route => done by the SIP AS

		// 3. Check if original dialog identifier is present
		String odi = request.getParameter(ORIGINAL_DIALOG_IDENTIFIER_PARAM);
		if (odi == null)
		{
			// S-CSCF is visited for the first time, save the Request-URI
			setOriginalURI(requestURI);
		}

		// 4. If original dialog identifier and URI has changed
		// forward request to target
		if (odi != null && !isOriginalURI(requestURI))
		{

			// save the Contact, CSeq and Record-Route
			// not needed yet, information should be given by SIP AS

			// forward the request
			if (__log.isDebugEnabled())
				__log.debug("Request URI has changed (new is " + requestURI
								+ "). Forwarding request");
			routeRequest(request);
			return true;

		}
		else
		{
			// Evaluate filter criteria
			InitialFilterCriteria ifc = null;
			boolean ifcMatched = false;

			while ((ifc = nextIFC()) != null)
			{
				SessionCase sessionCase;
				if (_context != null)
					sessionCase = SessionCase.TERMINATING_REGISTERED;
				else
					sessionCase = SessionCase.TERMINATING_UNREGISTERED;
				
				__log.debug("Evaluating filter criteria with priority: " + ifc.getPriority());
				ifcMatched = ifc.matches(request, sessionCase);
				if (ifcMatched)
				{
					SipURI asURI = (SipURI) getSessionManager().getSipFactory().createURI(ifc.getAS().getURI());

					if (__log.isDebugEnabled())
						__log.debug("IFC " + ifc + " matched for user " + getProfile().getURI() 
								+ ". Forwarding request to: " + asURI);
					if (asURI.getLrParam() == true)
					{
						request.pushRoute(getOwnURI());
						request.pushRoute(asURI);
					}
					else
					{
						request.setRequestURI(asURI);
					}
					// TODO confirm if should be record route.
					// may be something configurable
					request.getProxy().setRecordRoute(true);
					request.getProxy().proxyTo(request.getRequestURI());
					return false;
				}
			}
			__log.debug("No more Initial Filter Criterias");
			if (_context == null)
			{
				// No AS or only proxy AS, send 480 (Temporarily unavailable)
				__log.info("User is not registered. Sending 480 response");
				getSessionManager().getMessageSender().sendResponse(request, 
						SipServletResponse.SC_TEMPORARLY_UNAVAILABLE);
			}
			else
			{
				// 9. Forward the request to served user

				// insert a P-Called-Party header with original request URI
				request.setAddressHeader(Headers.P_CALLED_PARTY_ID, 
						getSessionManager().getSipFactory().createAddress(_originalURI));

				// 11. apply privacy to P-Asserted-ID
				String privacy = request.getHeader(Headers.PRIVACY);
				if (privacy != null && privacy.equalsIgnoreCase(PRIVACY_ID))
				{
					__log.debug("Privacy required, removing P-Asserted-Identity");
					request.removeHeader(Headers.PRIVACY);
				}

				if (_contacts.size() == 0)
				{
					// should not happen
					__log.warn("No Contact found for registered user! Sending 480 response");
					getSessionManager().getMessageSender().sendResponse(request, 
							SipServletResponse.SC_TEMPORARLY_UNAVAILABLE);
				}
				else if (_contacts.size() == 1)
				{
					Binding b = (Binding) _contacts.get(0);
					__log.debug("Forwarding request to contact: " + b);

					if (b.getPath() != null)
					{
						// Use P-CSCF if indicated during registration
						request.pushRoute(b.getPath());
					}
					// request is now forwarded to terminating part
					// mark session to be able to correlate responses
					request.getSession().setAttribute(
							SessionManager.SCSCF_ROLE, SessionManager.ROLE_TERMINATING);

					request.getProxy().setRecordRoute(true);
					request.getProxy().proxyTo(b.getContact().getURI());
				}
				else
				{
					// request is now forwarded to terminating part
					// mark session to be able to correlate responses
					request.getSession().setAttribute(
							SessionManager.SCSCF_ROLE, SessionManager.ROLE_TERMINATING);
					List<URI> targets = new ArrayList<URI>();
					Iterator<Binding> it = _contacts.iterator();
					while (it.hasNext())
					{
						Binding b = (Binding) it.next();
						targets.add(b.getContact().getURI());
					}
					// Cannot use different routes for forked requests
					Binding b = (Binding) _contacts.get(0);
					if (b.getPath() != null)
					{
						request.pushRoute(b.getPath());
					}
					request.getProxy().setRecordRoute(true);
					__log.debug("Forking request to contacts: " + targets);
					request.getProxy().proxyTo(targets);
				}
			}
			return true;
		}
	}

	public void setOriginalURI(URI uri)
	{
		this._originalURI = uri;
	}

	public boolean isOriginalURI(URI uri)
	{
		if (_originalURI.isSipURI())
		{
			SipURI origSipURI = (SipURI) _originalURI;
			if (!uri.isSipURI())
			{
				return false;
			}
			SipURI sipURI = (SipURI) uri;
			return (origSipURI.getUser().equals(sipURI.getUser()) && origSipURI.getHost().equals(
					sipURI.getHost()));
		}
		else
		{
			return _originalURI.toString().equals(uri.toString());
		}
	}

	@Override
	public boolean isOriginating()
	{
		return false;
	}
}
