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

import java.util.Iterator;

import junit.framework.TestCase;

public class SessionDescriptionTest extends TestCase
{

	
	static final String SDP = 
		"v=0\r\n" +
		"o=alice 2890844526 2890844526 IN IP4 host.anywhere.com\r\n" +
		"s=\r\n" +
		"c=IN IP4 host.anywhere.com\r\n" +
		"t=0 0\r\n" +
		"m=audio 49170 RTP/AVP 0\r\n" +
		"a=rtpmap:0 PCMU/8000\r\n" +
		"m=video 51372 RTP/AVP 31\r\n" +
		"a=rtpmap:31 H261/90000\r\n" +
		"m=video 53000 RTP/AVP 32\r\n" +
		"a=rtpmap:32 MPV/90000\r\n";
	
	public void testSdp()
	{
		SessionDescription sdp = new SessionDescription(SDP.getBytes());
		Iterator<String> it = sdp.getContents("v");
		assertTrue(it.hasNext());
		assertEquals("0", it.next());
		assertFalse(it.hasNext());
		
		it = sdp.getContents("m");
		assertTrue(it.hasNext());
		assertEquals("audio 49170 RTP/AVP 0", it.next());
		assertTrue(it.hasNext());
		assertEquals("video 51372 RTP/AVP 31", it.next());
		assertTrue(it.hasNext());
		assertEquals("video 53000 RTP/AVP 32", it.next());
		assertFalse(it.hasNext());
		
		it = sdp.getContents("s");
		assertTrue(it.hasNext());
		assertEquals("", it.next());
		
		it = sdp.getContents("d");
		assertFalse(it.hasNext());
		
	}

}
