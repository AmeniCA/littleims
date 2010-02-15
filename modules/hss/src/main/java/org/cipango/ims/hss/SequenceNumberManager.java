// ========================================================================
// Copyright 2010 NEXCOM Systems
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
package org.cipango.ims.hss;

import org.cipango.littleims.util.HexString;

public class SequenceNumberManager
{
	private static final int D = 65536;
	private static final int SEQ_LENGTH = 48;
	private static final int SEQ2_LENGTH = 24;
	private static final int IND_LENGTH = 5;
	private static final int A = (int) Math.pow(2, IND_LENGTH);
	private static final int P = (int) Math.pow(2, SEQ2_LENGTH);

	private long _glcStart = System.currentTimeMillis();


	public byte[] getNextSqn(byte[] sqn) {
		long sqnLong;
		if (sqn == null)
			sqnLong = 0;
		else
			sqnLong = HexString.byteArrayToLong(sqn);
		
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
		return HexString.longToByteArray((seq << IND_LENGTH) + ind, SEQ_LENGTH/8);
	}



	private long getGlc() {
		return ((System.currentTimeMillis() - _glcStart) / 1000)%P;
	}
}
