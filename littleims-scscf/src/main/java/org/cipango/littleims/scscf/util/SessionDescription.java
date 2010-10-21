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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public class SessionDescription
{
	public static final byte CR = 0x0D;
	public static final byte LF = 0x0A;
	public static final byte EQUAL = (byte) '=';
	private Map<String, List<String>> _lines = new HashMap<String, List<String>>();
	
	private enum State { ANNONCEMENT, CONTENT }
	
	public SessionDescription(byte[] sdp)
	{
		StringBuilder sbAnnoncement = new StringBuilder();
		StringBuilder sbContent = new StringBuilder();
		State state = State.ANNONCEMENT;
		for (int i = 0; i < sdp.length; i++)
		{
			switch (state)
			{
			case ANNONCEMENT:
				if (sdp[i] == EQUAL)
					state = State.CONTENT;
				else
					sbAnnoncement.append((char) sdp[i]);
				break;
			case CONTENT:
			if (sdp[i] == CR)
			{
				if (sdp[++i] == LF)
				{
					addLine(sbAnnoncement, sbContent);
					sbAnnoncement = new StringBuilder();
					sbContent = new StringBuilder();
					state = State.ANNONCEMENT;
				}
				else
					throw new IllegalStateException("Got CR without LF for line: " + sbAnnoncement + "=" + sbContent);
			}
			else if (sdp[i] != CR)
				sbContent.append((char) sdp[i]);

			default:
				break;
			}
		}
	}
	
	private void addLine(StringBuilder sbAnnoncement, StringBuilder sbContent)
	{
		String annoncement = sbAnnoncement.toString().trim();
		String content = sbContent.toString().trim();
		List<String> contents = _lines.get(annoncement);
		if (contents == null)
		{
			contents = new ArrayList<String>();
			contents.add(content);
			_lines.put(annoncement, contents);
		}
		else
			contents.add(content);
	}
	
	public Iterator<String> getLines()
	{
		return _lines.keySet().iterator();
	}
	
	@SuppressWarnings("unchecked")
	public Iterator<String> getContents(String annoncement)
	{
		List<String> contents = _lines.get(annoncement);
		if (contents == null)
			return Collections.EMPTY_LIST.iterator();
		else
			return contents.iterator();
	}
	
	
}
