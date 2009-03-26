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

import java.util.TimerTask;


public class SipAuthDataItem
{
	
	public enum AuthenticationScheme
	{
		DIGEST_AKA_MD5("DIGEST-AKAv1-MD5", "AKAv1-MD5"),
		DIGEST("SIP Digest", "MD5"),
		NASS_BUNDLED("NASS-Bundled","");
		
		private String _scheme;
		private String _algorithm;
		
		private AuthenticationScheme(String scheme, String algorithm)
		{
			_scheme = scheme;
			_algorithm = algorithm;
		}

		public String getScheme()
		{
			return _scheme;
		}

		public String getAlgorithm()
		{
			return _algorithm;
		}
		
	}
	
	private int _number;
	private AuthenticationScheme _authenticationScheme;
	private AuthenticationVector _authenticationVector;
	private DigestAuthenticate _digestAuthenticate;
	private TimerTask _timerTask;
	
	public SipAuthDataItem(AuthenticationVector authenticationVector)
	{
		this(authenticationVector, 0);
	}
	
	public SipAuthDataItem(AuthenticationVector authenticationVector, int itemNumber)
	{
		_number = itemNumber;
		_authenticationVector = authenticationVector;
		_authenticationScheme = AuthenticationScheme.DIGEST_AKA_MD5;
	}
	
	public SipAuthDataItem(DigestAuthenticate digestAuthenticate)
	{
		this(digestAuthenticate, 0);
	}
	
	public SipAuthDataItem(DigestAuthenticate digestAuthenticate, int itemNumber)
	{
		_number = itemNumber;
		_digestAuthenticate = digestAuthenticate;
		_authenticationScheme = AuthenticationScheme.DIGEST;
	}
		
	public AuthenticationScheme getAuthenticationScheme()
	{
		return _authenticationScheme;
	}
	
	public AuthenticationVector getAuthenticationVector()
	{
		return _authenticationVector;
	}
	
	public String getAlgorithm()
	{
		if (_authenticationScheme == AuthenticationScheme.DIGEST
				&& _digestAuthenticate.getAlgorithm() != null)
			return _digestAuthenticate.getAlgorithm();
		return _authenticationScheme.getAlgorithm();
	}
	
	public String getNonce()
	{
		switch (_authenticationScheme)
		{
		case DIGEST:
			return _digestAuthenticate.getNonce();
		case DIGEST_AKA_MD5:
			return _authenticationVector.getNonce();
		default:
			return null;
		}
	}
	
	public DigestAuthenticate getDigestAuthenticate()
	{
		return _digestAuthenticate;
	}
		
	public int getItemNumber()
	{
		return _number;
	}
	
	public void setTimer(TimerTask timerTask)
	{
		_timerTask = timerTask;
	}

	public TimerTask getTimerTask()
	{
		return _timerTask;
	}
	
}
