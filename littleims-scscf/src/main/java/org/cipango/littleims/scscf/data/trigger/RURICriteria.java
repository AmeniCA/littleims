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

import javax.servlet.sip.SipServletRequest;

import org.cipango.littleims.scscf.data.InitialFilterCriteria.SessionCase;
import org.cipango.littleims.util.RegexUtil;

public class RURICriteria implements CriteriaMatch
{

	private String _ruri;
	private boolean _regex;
	
	public RURICriteria(String ruri)
	{
		if (ruri.indexOf('!') != -1)
		{
			_regex = true;
			_ruri = RegexUtil.extendedRegexToJavaRegex(ruri);
		}
		else
		{
			_ruri = ruri;
			_regex = false;
		}
	}

	public boolean matches(SipServletRequest request, SessionCase sessionCase)
	{
		if (_regex)
			return request.getRequestURI().toString().matches(_ruri);
		else
			return request.getRequestURI().toString().equals(_ruri);	
	}

	public String getExpression()
	{
		return "RequestURI = " + _ruri;
	}

}
