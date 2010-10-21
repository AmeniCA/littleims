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
package org.cipango.littleims.scscf.data;

import javax.servlet.sip.SipServletRequest;

public class InitialRequest
{

	public InitialRequest(SipServletRequest request, short sessionCase)
	{
		this.request = request;
		this.sessionCase = sessionCase;
	}

	public SipServletRequest getRequest()
	{
		return request;
	}

	public short getSessionCase()
	{
		return sessionCase;
	}

	private SipServletRequest request;
	private short sessionCase;

}
