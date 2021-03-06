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
import javax.servlet.sip.Address;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipURI;

import org.cipango.littleims.scscf.data.InitialFilterCriteria;
import org.cipango.littleims.scscf.data.InitialFilterCriteria.SessionCase;
import org.cipango.littleims.scscf.data.UserProfile;
import org.cipango.littleims.util.Headers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class OriginatingSession extends Session
{
	private static final Logger __log = LoggerFactory.getLogger(OriginatingSession.class);
	private SessionCase _sessionCase;
	
	public OriginatingSession(UserProfile profile, boolean registered)
	{
		super(profile);
		if (registered)
			_sessionCase = SessionCase.ORIGINATING_SESSION;
		else
			_sessionCase = SessionCase.ORIGINATING_UNREGISTERED;
	}

	public boolean handleInitialRequest(SipServletRequest request) throws IOException,
			ServletException
	{
		// 1. Check that user is not barred
		if (getProfile() == null || getProfile().isBarred())
		{
			__log.info("Barred user: " + getProfile().getUri() + ". Sending 403 response");
			request.createResponse(SipServletResponse.SC_NOT_FOUND, "Barred identity").send();
			return true;
		}

		// 2. Remove its own SIP URI from topmost Route => already done by SIP AS

		// 3. Check if original dialog identifier is present
		String odi = request.getParameter(ORIGINAL_DIALOG_IDENTIFIER_PARAM);

		// 4. Check whether the initial request matches the next unexecuted
		// initial filter criteria
		InitialFilterCriteria ifc = null;
		boolean ifcMatched = false;

		while ((ifc = nextIFC()) != null)
		{
			__log.trace("Evaluating filter criteria with priority: " + ifc.getPriority());
			ifcMatched = ifc.matches(request, getSessionCase());
			if (ifcMatched)
			{
				SipURI asURI = (SipURI) getSessionManager().getSipFactory().createURI(ifc.getAs().getURI());

				__log.debug("IFC " +  ifc + " matched for user " + getProfile().getUri() + ". Forwarding request to: " + asURI);
				request.pushRoute(getOwnURI());
				request.pushRoute(asURI);
				request.getProxy().setRecordRoute(false);
				Address served = getSessionManager().getSipFactory().createAddress(getProfile().getUri());
				served.setParameter(InitialFilterCriteria.SE_CASE_PARAM, getSessionCase().getSeCaseParam());
				served.setParameter(InitialFilterCriteria.REG_STATE_PARAM, getSessionCase().getRegStateParam());
				request.setAddressHeader(Headers.P_SERVED_USER, served);
				
				request.getProxy().proxyTo(request.getRequestURI());
				return false;
			}
		}
		routeRequest(request);
		return false;
	}

	@Override
	public boolean isOriginating()
	{
		return true;
	}

	@Override
	public SessionCase getSessionCase()
	{
		return _sessionCase;
	}

}
