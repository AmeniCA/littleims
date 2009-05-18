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
package org.cipango.littleims.scscf.data.trigger;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import javax.servlet.sip.SipServletRequest;

import org.cipango.littleims.cx.data.userprofile.THeader;
import org.cipango.littleims.scscf.data.InitialFilterCriteria.SessionCase;
import org.cipango.littleims.util.RegexUtil;

public class HeaderCriteria implements CriteriaMatch
{

	private String _header;
	private boolean _headerRegex;
	private String _content;
	private boolean _contentRegex;
	
	public HeaderCriteria(THeader header)
	{
		_header = header.getHeader();
		if (_header.indexOf('!') != -1)
		{
			_headerRegex = true;
			_header = RegexUtil.extendedRegexToJavaRegex(_header);
		}
		_content = header.getContent();
		if (_content != null && _content.indexOf('!') != -1)
		{
			_contentRegex = true;
			_content = RegexUtil.extendedRegexToJavaRegex(_content);
		}
	}

	public boolean matches(SipServletRequest request, SessionCase sessionCase)
	{
		ListIterator<String> contents = null;
		if (_headerRegex)
		{
			Iterator<String> it = request.getHeaderNames();
			List<String> headers = new ArrayList<String>();
			while (it.hasNext())
			{
				String header = (String) it.next();
				if (header.matches(_header))
				{
					Iterator<String> it2 = request.getHeaders(header);
					while (it2.hasNext())
						headers.add(it2.next());		
				}
			}
			contents = headers.listIterator();
		}
		else
			contents = request.getHeaders(_header);
		
		if (contents == null || !contents.hasNext())
			return _content == null;
		while (contents.hasNext())
		{
			String content = (String) contents.next();
			boolean matches;
			if (_contentRegex)
				matches = content.matches(_content);
			else
				matches = _content.equals(content);
			if (matches)
				return true;
		}
		return false;	
	}

	public String getExpression()
	{
		StringBuffer sb = new StringBuffer();
		sb.append("Header ");
		sb.append(_headerRegex ? "like" : "=");
		sb.append(" \"").append(_header).append("\"");
		
		sb.append(" Content ");
		if (_content == null)
			sb.append("is null");
		else
		{
			sb.append(_contentRegex ? "like" : "=");
			sb.append(" \"").append(_content).append("\"");
		}
		
		return sb.toString();
	}

}
