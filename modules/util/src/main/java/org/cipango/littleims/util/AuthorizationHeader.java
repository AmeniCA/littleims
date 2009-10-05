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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class AuthorizationHeader
{

	public static final String QOP_PARAM = "qop";
	public static final String USERNAME_PARAM = "username";
	public static final String REALM_PARAM = "realm";
	public static final String URI_PARAM = "uri";
	public static final String NONCE_PARAM = "nonce";
	public static final String NC_PARAM = "nc";
	public static final String CNONCE_PARAM = "cnonce";
	public static final String ALGORITHM_PARAM = "algorithm";
	public static final String RESPONSE_PARAM = "response";
	public static final String OPAQUE_PARAM = "opaque";

	public static final String AUTH_VALUE = "auth";
	public static final String AUTS_PARAM = "auts";
	
	private String _scheme;
	private Map<String, String> _parameters = new HashMap<String, String>();

	
	public AuthorizationHeader(String auth)
	{
		int beginIndex = auth.indexOf(' ');
		int endIndex;
		_scheme = auth.substring(0, beginIndex).trim();
		while (beginIndex > 0)
		{
			endIndex = auth.indexOf('=', beginIndex);
			String name = auth.substring(beginIndex, endIndex).trim();
			if (auth.charAt(endIndex + 1) == '"')
			{
				beginIndex = endIndex + 2;
				endIndex = auth.indexOf('"', beginIndex);
			}
			else
			{
				beginIndex = endIndex + 1;
				endIndex = auth.indexOf(',', beginIndex);
				if (endIndex == -1)
					endIndex = auth.length(); 
			}

			String value = auth.substring(beginIndex, endIndex);	
			_parameters.put(name, value);
			beginIndex = auth.indexOf(',', endIndex) + 1;
		}
	}

	public String getScheme()
	{
		return _scheme;
	}
	
	public String getUsername()
	{
		return getParameter(USERNAME_PARAM);
	}
	
	public void setUsername(String username)
	{
		setParameter(USERNAME_PARAM, username);
	}
	
	public String getNonce()
	{
		return getParameter(NONCE_PARAM);
	}

	public String getResponse()
	{
		return getParameter(RESPONSE_PARAM);
	}
	
	public void setResponse(String response)
	{
		setParameter(RESPONSE_PARAM, response);
	}
	
	public String getOpaque()
	{
		return getParameter(OPAQUE_PARAM);
	}
	
	public String getAuts()
	{
		return getParameter(AUTS_PARAM);
	}
	
	public String getRealm()
	{
		return getParameter(REALM_PARAM);
	}
	
	public String getQop()
	{
		return getParameter(QOP_PARAM);
	}
	
	public String getUri()
	{
		return getParameter(URI_PARAM);
	}
	
	public String getNonceCount()
	{
		return getParameter(NC_PARAM);
	}
	
	public String getCnonce()
	{
		return getParameter(CNONCE_PARAM);
	}
	
	public String getAlgorithm()
	{
		return getParameter(ALGORITHM_PARAM);
	}
	
	public String getParameter(String name)
	{
		return _parameters.get(name);
	}
	
	public void setParameter(String name, String value)
	{
		_parameters.put(name, value);
	}

	@Override
	public String toString()
	{
		StringBuffer sb = new StringBuffer();
		sb.append(getScheme()).append(' ');
		boolean first = true;
		Iterator<String> it = _parameters.keySet().iterator();
		while (it.hasNext())
		{
			String key = (String) it.next();
			if (!first)
				sb.append(", ");
			first = false;
			sb.append(key);
			sb.append("=\"").append(_parameters.get(key)).append('"');
		}
		return sb.toString();
	}
}
