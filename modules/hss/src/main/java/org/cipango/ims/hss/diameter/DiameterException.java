package org.cipango.ims.hss.diameter;

import java.util.ArrayList;
import java.util.List;

import org.cipango.diameter.AVP;
import org.cipango.diameter.base.Base;

public class DiameterException extends Exception
{
	private int _vendorId;
	private int _resultCode;
	private List<AVP> _avpsToAdd;
	
	public DiameterException(int vendorId, int resultCode) 
	{ 
		_vendorId = vendorId;
		_resultCode = resultCode;
	}
	
	public DiameterException(int resultCode)
	{
		this(Base.IETF_VENDOR_ID, resultCode);
	}
	
	public DiameterException addAvp(AVP avp)
	{
		if (_avpsToAdd == null)
			_avpsToAdd = new ArrayList<AVP>();
		_avpsToAdd.add(avp);
		return this;
	}
	
	public List<AVP> getAvps()
	{
		return _avpsToAdd;
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
