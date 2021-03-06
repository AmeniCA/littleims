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
@DiscriminatorValue("H")
public class HeaderSpt extends SPT
{

	private String _header;
	private String _content;

	
	public String getHeader()
	{
		return _header;
	}
	public void setHeader(String header)
	{
		_header = header;
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
		out.open("SIPHeader");
		out.add("Header", _header);
		out.add("Content", _content);
		out.close("SIPHeader");
	}
	
	@Override
	public String getExpression()
	{
		StringBuffer sb = new StringBuffer();
		sb.append("getHeader(\"" + _header + "\")");
		if (_content != null)
		{
			sb.append(" = \"" + _content + "\"");
		}
		else
			sb.append(" != null");
		return sb.toString();
	}
	

}
