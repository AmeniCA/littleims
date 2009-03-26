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

public class DigestAuthenticate
{
	private String _realm;
	private String _qop;
	private String _ha1;
	private String _nonce;
	private String _algorithm;
	
	public DigestAuthenticate(String realm, String qop, String ha1, String algorithm)
	{
		_realm = realm;
		_qop = qop;
		_ha1 = ha1;
		_algorithm = algorithm;
	}
	
	public String getRealm()
	{
		return _realm;
	}
	public String getQop()
	{
		return _qop;
	}
	public String getHa1()
	{
		return _ha1;
	}

	public String getNonce()
	{
		return _nonce;
	}

	public void setNonce(String nonce)
	{
		_nonce = nonce;
	}

	public String getAlgorithm()
	{
		return _algorithm;
	} 
	
}
