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
package org.cipango.littleims.util;

import java.beans.PropertyEditorSupport;

import javax.servlet.sip.ServletParseException;
import javax.servlet.sip.SipFactory;
import javax.servlet.sip.URI;
/**
 * Sping Sip URI Editor.
 * Convert text to SipURI
 * 
 */
public class SipUriEditor extends PropertyEditorSupport
{
	private SipFactory _sipFactory;
	
	public SipFactory getSipFactory()
	{
		return _sipFactory;
	}

	public void setSipFactory(SipFactory sipFactory)
	{
		_sipFactory = sipFactory;
	}

	@Override
	public void setAsText(String text) throws IllegalArgumentException
	{
		try
		{
			URI uri = _sipFactory.createURI(text);
			setValue(uri);
		}
		catch (ServletParseException e)
		{
			throw new IllegalArgumentException("Not a SIP URI", e);
		}
	}
	
}
