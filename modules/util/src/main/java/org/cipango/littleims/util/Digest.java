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

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class Digest
{
	private static final String ENC = "ISO-8859-1";
	public static final String QOP_PARAM = "qop";
	public static final String USERNAME_PARAM = "username";
	public static final String REALM_PARAM = "realm";
	public static final String URI_PARAM = "uri";
	public static final String NONCE_PARAM = "nonce";
	public static final String NC_PARAM = "nc";
	public static final String CNONCE_PARAM = "cnonce";
	public static final String ALGORITHM_PARAM = "algorithm";
	public static final String RESPONSE_PARAM = "response";

	public static final String AUTH_VALUE = "auth";
	public static final String AUTS = "auts";

	public static String calculateResponse(AuthorizationHeader header, String method, byte[] passwd)
	{
		String user = header.getParameter(USERNAME_PARAM);
		String realm = header.getParameter(REALM_PARAM);
		return calculateResponseWithHa1(header, method, calculateHa1(user, realm, passwd));
	}
	
	public static String calculateHa1(String user, String realm, byte[] passwd)
	{
		try
		{
			return H(concat((user + ":" + realm + ":").getBytes(ENC), passwd));
		}
		catch (UnsupportedEncodingException e)
		{
			throw new RuntimeException(e);
		}
	}
	
	public static String calculateResponseWithHa1(AuthorizationHeader header, String method, String ha1)
	{
		String qop = header.getParameter(QOP_PARAM);
		String user = header.getParameter(USERNAME_PARAM);
		String realm = header.getParameter(REALM_PARAM);
		String uri = header.getParameter(URI_PARAM);
		String nonce = header.getParameter(NONCE_PARAM);
		String nc = header.getParameter(NC_PARAM);
		String cnonce = header.getParameter(CNONCE_PARAM);
		String response = header.getParameter(RESPONSE_PARAM);

		if (user == null || realm == null || uri == null || nonce == null || response == null)
		{
			throw new IllegalArgumentException("Invalid Authorization header: " + user + "/"
					+ realm + "/" + uri + "/" + nonce + "/" + response);
		}

		
		String a2 = method + ":" + uri;

		if (qop != null)
		{
			if (!qop.equals(AUTH_VALUE))
			{
				throw new IllegalArgumentException("Invalid qop: " + qop);
			}
			if (nc == null || cnonce == null)
			{
				throw new IllegalArgumentException("Invalid Authorization header: " + nc + "/"
						+ cnonce);
			}
			return KD(ha1, nonce + ":" + nc + ":" + cnonce + ":" + qop + ":" + H(a2));

		}
		else
		{
			return KD(ha1, nonce + ":" + H(a2));
		}
	}

	public static String calculateResponse(String user, String realm, String passwd, String method,
			String uri, String nonce, String nc, String cnonce, String qop)
	{
		String a1 = user + ":" + realm + ":" + passwd;
		String a2 = method + ":" + uri;
		return KD(H(a1), nonce + ":" + nc + ":" + cnonce + ":" + qop + ":" + H(a2));
	}
	
    public static byte[] concat(byte[] b1, byte[] b2)
    {
      byte[] b3 = new byte[b1.length + b2.length];
      
      System.arraycopy(b1, 0, b3, 0, b1.length);
      System.arraycopy(b2, 0, b3, b1.length, b2.length);
      
      return b3;
    }

	private static String KD(String secret, String data)
	{
		return H(secret + ":" + data);
	}
	
	private static String H(String s) {
		try {
			return H(s.getBytes(ENC));
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}
	
	private static String H(byte[] bytes) {
		try
		{
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(bytes);
			return HexString.bufferToHex(md.digest());
		}
		catch (NoSuchAlgorithmException e)
		{
			throw new RuntimeException(e);
		}
	}

}
