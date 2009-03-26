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
package org.cipango.littleims.util;

import javax.servlet.sip.SipFactory;
import javax.servlet.sip.SipURI;

public class URIHelper
{

	public static SipURI getCanonicalForm(SipFactory sipFactory, SipURI sipURI)
	{
		SipURI canonicalURI = sipFactory.createSipURI(sipURI.getUser(), sipURI.getHost());
		canonicalURI.setPort(sipURI.getPort());
		return canonicalURI;
	}

}
