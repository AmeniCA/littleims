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
package org.cipango.littleims.hss;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;

import org.apache.log4j.Logger;
import org.cipango.littleims.cx.AuthenticationVector;
import org.cipango.littleims.util.Base64;
import org.cipango.littleims.util.HexString;



public class AuthenticationVectorImpl implements AuthenticationVector {

	private static final Logger logger = Logger.getLogger(AuthenticationVectorImpl.class);
	
	protected static byte[] amf = { 0, 0};

	private String nonce;
	private byte[] rand;
	private byte[] xres;
	private byte[] ck;
	private byte[] ik;
	private byte[] opC;

	
	public AuthenticationVectorImpl(byte[] k, byte[] sqn, byte[] random, byte[] operatorId) throws InvalidKeyException, ArrayIndexOutOfBoundsException, UnsupportedEncodingException
	{
		rand = random;		
		opC = Milenage.computeOpC(k, operatorId);
		byte[] mac = Milenage.f1(k, rand, opC, sqn, amf);
		xres = Milenage.f2(k, rand, opC);
		ck = Milenage.f3(k, rand, opC);
		ik = Milenage.f4(k, rand, opC);
		byte[] ak = Milenage.f5(k, rand, opC);
		nonce = computeNonce(random, sqn, amf, mac, ak);
		
		if (logger.isDebugEnabled()) {
			logger.debug("Random " + HexString.bufferToHex(rand));
			logger.debug("OP " + HexString.bufferToHex(operatorId));
			logger.debug("OPC " + HexString.bufferToHex(opC));
			logger.debug("MAC " + HexString.bufferToHex(mac));
			logger.debug("XRES " + HexString.bufferToHex(xres));
			logger.debug("CK " + HexString.bufferToHex(rand));
			logger.debug("SQN " + HexString.bufferToHex(sqn));
		}	
	}
	
	protected static String computeNonce(byte[] rand, byte[] sqn, byte[] amf, byte[] mac, byte[] ak) throws UnsupportedEncodingException
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
		return Base64.encode(bNonce);
	}
		
	public byte[] getRand() {
		return this.rand;
	}
	public String getNonce() {
		return nonce;
	}
	public byte[] getXres() {
		return this.xres;
	}
	public byte[] getCk() {
		return this.ck;
	}
	public byte[] getIk() {
		return this.ik;
	} 
	public byte[] getAkStar(byte[] k) throws InvalidKeyException, ArrayIndexOutOfBoundsException {
		return Milenage.f5star(k, rand, opC);
	}
	public byte[] getMacs(byte[] sqn, byte[] k) throws InvalidKeyException, ArrayIndexOutOfBoundsException {
		return Milenage.f1star(k, rand, opC, sqn, amf);
	}
	public static byte[] getAmf()
	{
		return amf;
	}
	
}
