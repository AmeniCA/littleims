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
package org.cipango.ims.hss.model.spt;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import org.cipango.ims.hss.util.XML.Output;

@Entity
@DiscriminatorValue("SC")
public class SessionDescriptionSpt extends SPT
{
	private String _line;
	private String _content;
	public String getLine()
	{
		return _line;
	}
	public void setLine(String line)
	{
		_line = line;
	}
	public String getContent()
	{
		return _content;
	}
	public void setContent(String content)
	{
		_content = content;
	}

	@Override
	protected void doPrint(Output out)
	{
		out.open("SessionDescription");
		out.add("Line", _line);
		out.add("Content", _content);
		out.close("SessionDescription");
	}
	
	@Override
	public String doExpression()
	{
		StringBuffer sb = new StringBuffer();
		sb.append("Line = \"" + _line + "\"");
		if (_content != null)
		{
			sb.append(" Content = \"" + _content + "\"");
		}
		return sb.toString();
	}

}
