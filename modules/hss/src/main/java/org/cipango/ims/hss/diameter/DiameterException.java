package org.cipango.ims.hss.diameter;

import java.util.ArrayList;
import java.util.List;

import org.cipango.diameter.AVP;
import org.cipango.diameter.base.Base;
import org.cipango.diameter.ims.IMS;

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
	
	public DiameterException(int vendorId, int resultCode, String message) 
	{ 
		super(message);
		_vendorId = vendorId;
		_resultCode = resultCode;
	}
	
	public DiameterException(int resultCode, String message)
	{
		this(Base.IETF_VENDOR_ID, resultCode, message);
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
	
	public static DiameterException newMissingDiameterAvp(int vendorId, int code)
	{
		AVP failedAvp = AVP.ofAVPs(Base.FAILED_AVP, 
				AVP.ofBytes(vendorId, code, new byte[10]));
		StringBuilder sb = new StringBuilder();
		sb.append("Missing Mandatory AVP: ");
		if (vendorId != Base.IETF_VENDOR_ID)
		{
			sb.append("vendor ID: ");
			if (vendorId == IMS.IMS_VENDOR_ID)
				sb.append("IMS vendor ID (").append(vendorId).append(") ");
			else
				sb.append(vendorId + " ");
		}
		sb.append("code ").append(code);
		return new DiameterException(Base.IETF_VENDOR_ID, Base.DIAMETER_MISSING_AVP, sb.toString()).addAvp(failedAvp);

	}
}
