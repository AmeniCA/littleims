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
package org.cipango.littleims.scscf.data;

import org.cipango.littleims.cx.data.userprofile.TApplicationServer;

public class AS
{

	private String _uri;
	private int _defaultHandling;
	private String _serviceInfo;
	private boolean _includeRegisterRequest;
	private boolean _includeRegisterResponse;
	
	public AS(TApplicationServer as)
	{
		_uri = as.getServerName();
		_defaultHandling = as.getDefaultHandling();
		_serviceInfo = as.getServiceInfo();
		if (as.getExtension() != null)
		{
			_includeRegisterRequest = as.getExtension().getIncludeRegisterRequest() != null;
			_includeRegisterResponse = as.getExtension().getIncludeRegisterResponse() != null;
		}
		else
			_includeRegisterRequest = _includeRegisterResponse = false;
	}
	
	public int getDefaultHandling()
	{
		return _defaultHandling;
	}

	public String getServiceInfo()
	{
		return _serviceInfo;
	}

	public String getURI()
	{
		return _uri;
	}

	public boolean getIncludeRegisterRequest()
	{
		return _includeRegisterRequest;
	}

	public boolean getIncludeRegisterResponse()
	{
		return _includeRegisterResponse;
	}

}
