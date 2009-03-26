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
import java.util.Map;
import java.util.StringTokenizer;

public class AuthorizationHeader
{

	public AuthorizationHeader(String header)
	{
		int i = header.indexOf(' ');
		scheme = header.substring(0, i).trim();
		String rest = header.substring(i);
		StringTokenizer st = new StringTokenizer(rest, ",");
		while (st.hasMoreTokens())
		{
			String token = st.nextToken();
			int index = token.indexOf("=");
			String name = token.substring(0, index).trim();
			String value = token.substring(index + 1, token.length()).trim();
			if (value.startsWith("\""))
			{
				value = value.substring(1, value.length() - 1);
			}
			parameters.put(name, value);
		}
	}

	public String getScheme()
	{
		return scheme;
	}

	public String getParameter(String name)
	{
		return (String) parameters.get(name);
	}

	private String scheme;
	private Map<String, String> parameters = new HashMap<String, String>();

}
