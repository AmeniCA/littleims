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

import javax.servlet.ServletException;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipURI;
import javax.servlet.sip.URI;

import org.apache.log4j.Logger;
import org.cipango.littleims.scscf.data.InitialFilterCriteria;
import org.cipango.littleims.scscf.data.UserProfile;
import org.cipango.littleims.scscf.data.InitialFilterCriteria.SessionCase;


public class OriginatingSession extends Session
{
	private static final Logger __log = Logger.getLogger(OriginatingSession.class);
	
	public OriginatingSession(UserProfile profile)
	{
		super(profile);
	}

	public boolean handleInitialRequest(SipServletRequest request) throws IOException,
			ServletException
	{

		URI requestURI = request.getRequestURI();

		// 1. Check that user is not barred
		if (getProfile() == null || getProfile().isBarred())
		{
			__log.debug("Barred user: " + getProfile().getURI() + ". Sending 403 response");
			request.createResponse(SipServletResponse.SC_NOT_FOUND, "Barred identity").send();
			return true;
		}

		// 2. Remove its own SIP URI from topmost Route => already done by SIP
		// AS

		// 3. Check if original dialog identifier is present
		String odi = request.getParameter(ORIGINAL_DIALOG_IDENTIFIER_PARAM);

		// 4. Check whether the initial request matches the next unexecuted
		// initial filter criteria
		__log.debug("Checking for next filter criteria");
		InitialFilterCriteria ifc = null;
		boolean ifcMatched = false;

		while ((ifc = nextIFC()) != null)
		{

			__log.debug("Evaluating filter criteria with priority: " + ifc.getPriority());
			ifcMatched = ifc.matches(request, SessionCase.ORIGINATING_SESSION);
			if (ifcMatched)
			{
				SipURI asURI = (SipURI) getSessionManager().getSipFactory().createURI(ifc.getAS().getURI());

				__log.debug("IFC matched. Forwarding request to: " + asURI);
				request.pushRoute(getOwnURI());
				request.pushRoute(asURI);
				request.getProxy().setRecordRoute(false);
				request.getProxy().proxyTo(request.getRequestURI());
				return false;
			}
		}
		routeRequest(request);
		return true;
	}

	@Override
	public boolean isOriginating()
	{
		return true;
	}

}
