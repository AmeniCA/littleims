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
package org.cipango.littleims.scscf.session;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.sip.SipFactory;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipURI;

import org.cipango.diameter.api.DiameterServletAnswer;
import org.cipango.diameter.api.DiameterServletRequest;
import org.cipango.littleims.scscf.charging.CDF;
import org.cipango.littleims.scscf.data.UserProfileCache;
import org.cipango.littleims.scscf.registrar.Registrar;
import org.cipango.littleims.scscf.util.MessageSender;

public interface SessionManager
{
	public static final String SCSCF_ROLE 		= "scscf.role";
	public static final String ROLE_ORIGINATING = "originating";
	public static final String ROLE_TERMINATING = "terminating";
	
	public void doInitialRequest(SipServletRequest request) throws ServletException, IOException;
	
	public void doSubsequentRequest(SipServletRequest request);
	
	public void doResponse(SipServletResponse response) throws ServletException, IOException;
	
	public SipFactory getSipFactory();

	public EnumClient getEnumClient();

	public CDF getCdf();

	public Registrar getRegistrar();

	public UserProfileCache getUserProfileCache();

	public SipURI getIcscfUri();

	public SipURI getBgcfUri();
	
	public List<Session> getSessions();
	
	public Session getSession(String odi);
	
	public void handleSaa(DiameterServletAnswer saa);
	
	public void handlePpr(DiameterServletRequest ppr);
	
	public MessageSender getMessageSender();

}