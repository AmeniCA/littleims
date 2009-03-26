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

import org.cipango.littleims.util.HexString;

public class IDGenerator
{

	public IDGenerator()
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

	public synchronized String newRandomID()
	{
		String sRandom = Long.toString(random.nextLong());
		md.update(sRandom.getBytes());
		return HexString.bufferToHex(md.digest());
	}

	public synchronized String generateID(String s)
	{
		md.update(s.getBytes());
		return HexString.bufferToHex(md.digest());
	}

	public static void main(String[] args)
	{
		IDGenerator idgen = new IDGenerator();
		System.out.println(idgen.newRandomID());
	}

	private MessageDigest md;
	private Random random;
}
