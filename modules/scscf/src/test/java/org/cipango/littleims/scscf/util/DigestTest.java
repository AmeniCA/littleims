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
package org.cipango.littleims.scscf.util;

import org.cipango.littleims.util.AuthorizationHeader;
import org.cipango.littleims.util.Digest;

import junit.framework.TestCase;

public class DigestTest extends TestCase
{

	static String rfc2617 = "Digest username=\"Mufasa\"," + "realm=\"testrealm@host.com\","
			+ "nonce=\"dcd98b7102dd2f0e8b11d0f600bfb0c093\"," + "uri=\"/dir/index.html\","
			+ "qop=auth," + "nc=00000001," + "cnonce=\"0a4f113b\","
			+ "response=\"6629fae49393a05397450978507c4ef1\","
			+ "opaque=\"5ccc069c403ebaf9f0171e9517f40e41\"";

	/*
	 * Test method for
	 * 'com.nexcom.scscf.util.Digest.calculateResponse(AuthorizationHeader,
	 * String, String)'
	 */
	public void testCalculateResponse()
	{
		AuthorizationHeader ah = new AuthorizationHeader(rfc2617);
		String res = Digest.calculateResponse(ah, "GET", "Circle Of Life".getBytes());
		assertEquals("6629fae49393a05397450978507c4ef1", res);
	}

}
