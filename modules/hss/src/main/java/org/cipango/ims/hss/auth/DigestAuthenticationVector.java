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
