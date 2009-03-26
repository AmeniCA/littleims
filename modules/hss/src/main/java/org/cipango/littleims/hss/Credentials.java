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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.cipango.littleims.util.HexString;


public class Credentials
{

	private static final byte[] DEFAULT_OPERATOR_IP = new byte[16];
	private Map<String, Credential> passwords = new HashMap<String, Credential>();
	private SequenceNumberManager sequenceNumberManager = new SequenceNumberManager();
	
	public Credentials()
	{
	}

	public Credentials(String privateID, String password, String operatorId)
	{
		addPassword(privateID, password, operatorId);
	}

	public void addPassword(String privateID, String password, String operatorId)
	{
		Credential private1 = new Credential(privateID, password, operatorId, sequenceNumberManager);
		passwords.put(privateID, private1);
	}

	public byte[] getPassword(String privateID)
	{
		Credential credential = passwords.get(privateID);
		if (credential == null) {
			return null;
		}
		return credential.getPassword();
	}
	
	public Credential getCredential(String privateID)
	{
		return passwords.get(privateID);
	}
	
	/**
	 * Returns the first credential.
	 * Required when no authorization header set in first register.
	 * @return
	 */
	public Credential getDefaultCredential()
	{
		return passwords.values().iterator().next();
	}
	
	public String toString()
	{
		return passwords.toString();
	}

	public String getUserNoAuth()
	{
		Iterator<String> it = passwords.keySet().iterator();
		if (it.hasNext())
		{
			return it.next();
		}
		return null;
	}
	
	public static long byteArrayToLong(byte[] array) {
		long l = 0;
		for (int i = 0; i < array.length; i++) {
			l = (l << 8) + (array[i] & 0xff);
		}
		return l;
	}

	public static byte[] longToByteArray(long l, int lentgh) {
		byte[] array = new byte[lentgh];
		for (int i = array.length; i-- > 0;) {
			array[i] = (byte) (l & 0xff);
			l = l >> 8;
		}
		return array;
	}

	
	public static class Credential {
		private byte[] _password;
		private byte[] _operatorId;
		private SequenceNumberManager _sqnManager;
		private byte[] _sqn;
		private String _privateUri;
		
		public Credential(String privateUri, String password, String operatorId, SequenceNumberManager sequenceNumberManager) {
			this._password = password.getBytes();
			_privateUri = privateUri;
			_sqnManager = sequenceNumberManager;
			if (operatorId == null || operatorId.trim().equals("")) {
				this._operatorId = DEFAULT_OPERATOR_IP;
			} else {
				this._operatorId = HexString.hexToBuffer(operatorId);
			}
			if (this._operatorId.length != 16)
				throw new IllegalArgumentException("Invalid operator ID. Must be 32 hexadecimal characters");
		}
		
		public String getPrivateUri()
		{
			return _privateUri;
		}

		public byte[] getPassword()
		{
			return _password;
		}

		public byte[] getAkaPassword()
		{
			byte[] k = new byte[16];
			for (int i = 0; i < k.length; i++)
			{
				if (i < _password.length)
					k[i] = _password[i];
				else
					k[i] = 0;
			}
			return k;
		}
		
		public byte[] getOperatorId()
		{
			return _operatorId;
		}

		public byte[] getNextSqn()
		{
			_sqn = _sqnManager.getNextSqn(_sqn);
			return _sqn;
		}

		public void setSqn(byte[] sqn)
		{
			this._sqn = sqn;
		}
	}
	
	/**
	 * Based on C.3.1	Profile 1: management of sequence numbers which are partly time-based
	 * @author nicolas
	 *
	 */
	static class SequenceNumberManager {

		private static final int D = 65536;
		private static final int SEQ_LENGTH = 48;
		private static final int SEQ2_LENGTH = 24;
		private static final int IND_LENGTH = 5;
		private static final int A = (int) Math.pow(2, IND_LENGTH);
		private static final int P = (int) Math.pow(2, SEQ2_LENGTH);

		private long glcStart = System.currentTimeMillis();


		public byte[] getNextSqn(byte[] sqn) {
			long sqnLong;
			if (sqn == null)
				sqnLong = 0;
			else
				sqnLong = byteArrayToLong(sqn);
			
			long seq1 = sqnLong >> (SEQ2_LENGTH + IND_LENGTH);
			long seq2 = (sqnLong - (seq1 << (SEQ2_LENGTH + IND_LENGTH))) >> IND_LENGTH;
			long ind = sqnLong - (seq1 << (SEQ2_LENGTH + IND_LENGTH)) - (seq2 << IND_LENGTH);
			long glc = getGlc();
			
			long seq;
			if (seq2 < glc && glc < (seq2 + P - D + 1)) {
				// If SEQ2HE < GLC < SEQ2HE + p – D + 1 then HE sets SEQ= SEQ1HE || GLC
				seq = (seq1 << SEQ2_LENGTH) + glc;
			} else if ((glc <= seq2 && seq2 <= (glc + D - 1))
					|| ((seq2 + P - D + 1) <= glc )) {
				// if GLC <= SEQ2HE <= GLC+D - 1 or SEQ2HE + p – D + 1 <= GLC then HE sets SEQ = SEQHE +1;
				seq = (seq1 << SEQ2_LENGTH) + seq2 + 1;
			} else if ((glc + D + 1) < seq2) {
				// if GLC+D - 1 <  SEQ2HE then HE sets SEQ = (SEQ1HE +1) || GLC.
				seq = ((seq1 + 1) << SEQ2_LENGTH) + glc;
			} else
				seq = (seq1 << SEQ2_LENGTH) + seq2;
			
			ind = (ind + 1)%A;
			return longToByteArray((seq << IND_LENGTH) + ind, SEQ_LENGTH/8);
		}



		private long getGlc() {
			return ((System.currentTimeMillis() - glcStart) / 1000)%P;
		}
	}
}
