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

package org.cipango.ims.hss.auth;

import org.cipango.diameter.AVP;
import org.cipango.diameter.AVPList;
import org.cipango.diameter.base.Base;
import org.cipango.diameter.ims.IMS;
import org.cipango.ims.Cx;

public class DigestAuthenticationVector implements AuthenticationVector
{
	private String _realm;
	private String _ha1;
	
	public DigestAuthenticationVector()
	{	
	}
	
	public void setRealm(String realm)
	{
		_realm = realm;
	}
	
	public String getRealm()
	{
		return _realm;
	}
	
	public void setHA1(String ha1)
	{
		_ha1 = ha1;
	}
	
	public String getHA1()
	{
		return _ha1;
	}
	
	public AVPList asAuthItem()
	{
		AVPList list = new AVPList();
		
		list.add(AVP.ofString(IMS.IMS_VENDOR_ID, IMS.SIP_AUTHENTICATION_SCHEME, Cx.AuthenticationScheme.SIP_DIGEST.getName()));
		
		AVP authenticate = AVP.ofAVPs(IMS.IMS_VENDOR_ID, IMS.SIP_DIGEST_AUTHENTICATE,
				AVP.ofString(Base.DIGEST_REALM, getRealm()),
				AVP.ofString(Base.DIGEST_QOP, "auth"),
				AVP.ofString(Base.DIGEST_HA1, getHA1()));
			
		list.add(authenticate);
		return list;
	}
}
