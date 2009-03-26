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

import java.util.List;

import org.cipango.littleims.cx.data.userprofile.IMSSubscriptionDocument;

public class SAA extends Answer
{

	private String _privateIdentity;
	private IMSSubscriptionDocument _userProfile;
	private List<String> _associatedPrivateIdentities;

	public SAA(int resultCode)
	{
		super(resultCode);
	}

	public String getPrivateIdentity()
	{
		return _privateIdentity;
	}
	public IMSSubscriptionDocument getUserProfile()
	{
		return _userProfile;
	}
	public List<String> getAssociatedPrivateIdentities()
	{
		return _associatedPrivateIdentities;
	}

	public void setPrivateIdentity(String privateIdentity)
	{
		_privateIdentity = privateIdentity;
	}
	public void setUserProfile(IMSSubscriptionDocument userProfile)
	{
		_userProfile = userProfile;
	}
	public void setAssociatedPrivateIdentities(List<String> associatedPrivateIdentities)
	{
		_associatedPrivateIdentities = associatedPrivateIdentities;
	}

	
	
}
