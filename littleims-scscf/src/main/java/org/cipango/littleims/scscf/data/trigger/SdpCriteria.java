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
package org.cipango.littleims.scscf.data.trigger;

import java.util.Iterator;

import javax.servlet.sip.SipServletRequest;

import org.apache.log4j.Logger;
import org.cipango.littleims.cx.data.userprofile.TSessionDescription;
import org.cipango.littleims.scscf.data.InitialFilterCriteria.SessionCase;
import org.cipango.littleims.scscf.util.SessionDescription;
import org.cipango.littleims.util.RegexUtil;

public class SdpCriteria implements CriteriaMatch
{
	private static final Logger __log = Logger.getLogger(SdpCriteria.class);
	private String _line;
	private boolean _lineRegex;
	private String _content;
	private boolean _contentRegex;

	public SdpCriteria(TSessionDescription sessionDescription)
	{
		_line = sessionDescription.getLine();
		if (_line != null && _line.indexOf('!') != -1)
		{
			_lineRegex = true;
			_line = RegexUtil.extendedRegexToJavaRegex(_line);
		}
		_content = sessionDescription.getContent();
		if (_content != null && _content.indexOf('!') != -1)
		{
			_contentRegex = true;
			_content = RegexUtil.extendedRegexToJavaRegex(_content);
		}
	}
	
	public String getExpression()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("SDP line ");
		sb.append(_lineRegex ? "like" : "=");
		sb.append(" \"").append(_line).append("\"");
		
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

	public boolean matches(SipServletRequest request, SessionCase sessionCase)
	{
		try
		{
			if (!"application/sdp".equalsIgnoreCase(request.getContentType()))
					return false;
			byte[] content = request.getRawContent();
			if (content == null || content.length == 0)
				return false;
			
			SessionDescription sdp = new SessionDescription(content);
			if (_lineRegex)
			{
				Iterator<String> it = sdp.getLines();
				while (it.hasNext())
				{
					String line = (String) it.next();
					if (line.matches(_line) && match(sdp.getContents(line)))
						return true;
				}
			}
			else
			{
				return match(sdp.getContents(_line));
			}
		}
		catch (Throwable e)
		{
			__log.warn("Failed to parse SDP", e);
		}
		
		return false;
	}
	
	private boolean match(Iterator<String> it)
	{
		while (it.hasNext())
		{
			String content = (String) it.next();
			if ((_contentRegex && content.matches(_content))
					|| (!_contentRegex && content.equals(_content)))
			{
				return true;
			}
		}
		return false;
	}
}
