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
package org.cipango.littleims.cx;

public class UAA extends Answer
{

	private String _scscfName;
	private String _wilcardPublicId;
	
	public UAA(int resultCode)
	{
		super(resultCode);
	}

	public String getScscfName()
	{
		return _scscfName;
	}

	public void setScscfName(String scscfName)
	{
		_scscfName = scscfName;
	}

	public String getWilcardPublicId()
	{
		return _wilcardPublicId;
	}

	public void setWilcardPublicId(String wilcardPublicId)
	{
		_wilcardPublicId = wilcardPublicId;
	}

}
