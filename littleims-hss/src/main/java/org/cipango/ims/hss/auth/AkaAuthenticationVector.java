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

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;

import org.cipango.diameter.AVP;
import org.cipango.diameter.AVPList;
import org.cipango.diameter.ims.Cx;
import org.cipango.ims.AuthenticationScheme;
import org.cipango.littleims.util.HexString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class AkaAuthenticationVector implements AuthenticationVector {

	private static final Logger __log = LoggerFactory.getLogger(AkaAuthenticationVector.class);
	
	protected static byte[] __amf = { 0, 0};

	private byte[] _sipAuthenticate;
	private byte[] _rand;
	private byte[] _xres;
	private byte[] _ck;
	private byte[] _ik;
	private byte[] _opC;

	
	public AkaAuthenticationVector(byte[] k, byte[] sqn, byte[] random, byte[] operatorId) throws InvalidKeyException, ArrayIndexOutOfBoundsException, UnsupportedEncodingException
	{
		_rand = random;		
		_opC = Milenage.computeOpC(k, operatorId);
		byte[] mac = Milenage.f1(k, _rand, _opC, sqn, __amf);
		_xres = Milenage.f2(k, _rand, _opC);
		_ck = Milenage.f3(k, _rand, _opC);
		_ik = Milenage.f4(k, _rand, _opC);
		byte[] ak = Milenage.f5(k, _rand, _opC);
		_sipAuthenticate = computeSipAuthenticate(random, sqn, __amf, mac, ak);
		
		if (__log.isDebugEnabled()) {
			__log.debug("Random " + HexString.bufferToHex(_rand));
			__log.debug("OP " + HexString.bufferToHex(operatorId));
			__log.debug("OPC " + HexString.bufferToHex(_opC));
			__log.debug("MAC " + HexString.bufferToHex(mac));
			__log.debug("XRES " + HexString.bufferToHex(_xres));
			__log.debug("CK " + HexString.bufferToHex(_rand));
			__log.debug("SQN " + HexString.bufferToHex(sqn));
		}	
	}
	
	protected static byte[] computeSipAuthenticate(byte[] rand, byte[] sqn, byte[] amf, byte[] mac, byte[] ak) throws UnsupportedEncodingException
	{
		byte[] bNonce = new byte[rand.length + sqn.length + amf.length + mac.length];
		for (int i = 0; i < bNonce.length; i++)
		{
			if (i < rand.length) {
				bNonce[i] = rand[i];
			} else if (i < (rand.length + sqn.length)) {
				bNonce[i] = (byte) (sqn[i - rand.length] ^ ak[i - rand.length]);
			} else if (i < (rand.length + sqn.length + amf.length)) {
				bNonce[i] = amf[i - rand.length - sqn.length];
			} else {
				bNonce[i] = mac[i - rand.length - sqn.length - amf.length];
			}
		}
		return bNonce;
	}
		
	public byte[] getRand() {
		return this._rand;
	}
	public byte[] getSipAuthenticate() {
		return _sipAuthenticate;
	}
	public byte[] getXres() {
		return this._xres;
	}
	public byte[] getCk() {
		return this._ck;
	}
	public byte[] getIk() {
		return this._ik;
	} 
	public byte[] getAkStar(byte[] k) throws InvalidKeyException, ArrayIndexOutOfBoundsException {
		return Milenage.f5star(k, _rand, _opC);
	}
	public byte[] getMacs(byte[] sqn, byte[] k) throws InvalidKeyException, ArrayIndexOutOfBoundsException {
		return Milenage.f1star(k, _rand, _opC, sqn, __amf);
	}
	public static byte[] getAmf()
	{
		return __amf;
	}

	public AVP<AVPList> asAuthItem() {
		AVPList avps = new AVPList();
		
		avps.add(Cx.SIP_AUTHENTICATION_SCHEME, AuthenticationScheme.DIGEST_AKA_MD5.getName());
		avps.add(Cx.SIP_AUTHENTICATE, _sipAuthenticate);
		avps.add(Cx.SIP_AUTHORIZATION, _xres);
		avps.add(Cx.CONFIDENTIALITY_KEY, _ck);
		avps.add(Cx.INTEGRITY_KEY, _ik);
		return new AVP<AVPList>(Cx.SIP_AUTH_DATA_ITEM, avps);
	}
	
}
