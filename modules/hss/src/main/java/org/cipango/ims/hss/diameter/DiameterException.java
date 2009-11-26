package org.cipango.ims.hss.diameter;

import java.util.ArrayList;
import java.util.List;

import org.cipango.diameter.AVP;
import org.cipango.diameter.AVPList;
import org.cipango.diameter.ResultCode;
import org.cipango.diameter.Type;
import org.cipango.diameter.base.Base;

public class DiameterException extends Exception
{
	private ResultCode _resultCode;
	private List<AVP> _avpsToAdd;
	
	
	public DiameterException(ResultCode resultCode, String message) 
	{ 
		super(message);
		_resultCode = resultCode;
	}

	public DiameterException(ResultCode resultCode)
	{
		_resultCode = resultCode;
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
		
	public ResultCode getResultCode()
	{
		return _resultCode;
	}
	
	public static DiameterException newMissingDiameterAvp(Type type)
	{
		AVP<AVPList> failedAvp = new AVP<AVPList>(Base.FAILED_AVP, new AVPList());
		failedAvp.getValue().add(new AVP(type, new byte[10]));
				
		StringBuilder sb = new StringBuilder();
		sb.append("Missing Mandatory AVP: ").append(type);
		return new DiameterException(Base.DIAMETER_MISSING_AVP, sb.toString()).addAvp(failedAvp);

	}
}
