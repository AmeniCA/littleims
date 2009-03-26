package org.cipango.ims.hss.diameter;

import org.cipango.diameter.base.Base;

public class DiameterException extends Exception
{
	private int _vendorId;
	private int _resultCode;
	
	public DiameterException(int vendorId, int resultCode) 
	{ 
		_vendorId = vendorId;
		_resultCode = resultCode;
	}
	
	public DiameterException(int resultCode)
	{
		this(Base.IETF_VENDOR_ID, resultCode);
	}
	
	public int getVendorId()
	{
		return _vendorId;
	}
	
	public int getResultCode()
	{
		return _resultCode;
	}
}
