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

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

import sun.misc.BASE64Encoder;

public class NonceManager
{

	public NonceManager()
	{
		random = new Random(System.currentTimeMillis());
		try
		{
			md = MessageDigest.getInstance("MD5");
		}
		catch (NoSuchAlgorithmException _)
		{
		}
	}

	public synchronized String newNonce()
	{
		String sRandom = Long.toString(random.nextLong());
		md.update(sRandom.getBytes());
		return base64encoder.encode(md.digest());
	}

	public synchronized byte[] newRand()
	{
		byte[] rand = new byte[16];
		random.nextBytes(rand);
		return rand;
	}
	
	private Random random;
	private BASE64Encoder base64encoder = new BASE64Encoder();
	private MessageDigest md;

}
