/* GNU Lesser General Public License Version 2.1
 * =============================================
 * Copyright (C) 2006-2007  Portugal Telecom Inovacao
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 * =============================================
 * 
 * Initial developer(s): Miguel Freitas <it-j-freitas@ptinovacao.pt>, <a23875@gmail.com>
 */

package org.cipango.littleims.hss;

import java.security.InvalidKeyException;



/**
 * A sample implementation of the f1, f1*, f2, f3, f4, f5, f5* algorithms, 
 * as defined by 3GPP TS 35.206 (MILENAGE Algorithm Set)
 * 
 * @author Miguel Freitas (IT) PT-Inovacao
 * 
 */

public class Milenage {
    
    private static Rijndael32Bit rijndael = new Rijndael32Bit();
    
    /**
     * Algorithm f1.
     * Computes network authentication code MAC-A from key K, random
     * challenge RAND, sequence number SQN and authentication management
     * field AMF.
     * 
     * @param secretKey
     * @param rand
     * @param op_c
     * @param sqn
     * @param amf
     * @return MAC-A
     * @throws InvalidKeyException
     * @throws ArrayIndexOutOfBoundsException
     */
	public static byte[] f1(byte[] secretKey, byte[] rand, byte[] op_c,
            byte[] sqn, byte[] amf) 
		throws InvalidKeyException, ArrayIndexOutOfBoundsException
    {
		try {
			//console.logEntry();
		
			rijndael.init(secretKey);
			// op is set in the properties file (OPERATOR_ID) 
	        byte[] temp = new byte[16];
	        byte[] in1 = new byte[16];
	        byte[] out1 = new byte[16];
	        byte[] rijndaelInput = new byte[16];
	        byte[] mac = new byte[8];
	
	        for (int i = 0; i < rand.length && i < op_c.length; i++)
	            rijndaelInput[i] = (byte) (rand[i] ^ op_c[i]);
	
	        temp = rijndael.encrypt(rijndaelInput);
	
	        for (int i = 0; i < sqn.length; i++) {
	            in1[i] = sqn[i];
	            in1[i + 8] = sqn[i];
	        }
	        for (int i = 0; i < amf.length; i++) {
	            in1[i + 6] = amf[i];
	            in1[i + 14] = amf[i];
	        }
	
	        /* XOR op_c and in1, rotate by r1=64, and XOR 
	         * on the constant c1 (which is all zeroes) */
	        for (int i = 0; i < in1.length; i++)
	            rijndaelInput[(i + 8) % 16] = (byte) (in1[i] ^ op_c[i]);
	
	        /* XOR on the value temp computed before */
	        for (int i = 0; i < temp.length; i++)
	            rijndaelInput[i] ^= temp[i];
	
	        out1 = rijndael.encrypt(rijndaelInput);
	        for (int i = 0; i < out1.length && i < op_c.length; i++)
	            out1[i] = (byte) (out1[i] ^op_c[i]);
	
	        for (int i = 0; i < 8; i++)
	            mac[i] = (byte) out1[i];
	
	        return mac;
		}
		finally {
			//console.logExit();
		}
    } /* end of function f1 */

	
	/**
	 * Algorithm f2.
     * Takes key K and random challenge RAND, and returns response RES.
     * 
	 * @param secretKey
	 * @param rand
	 * @param op_c
	 * @return RES (or XRES)
	 * @throws InvalidKeyException
	 * @throws ArrayIndexOutOfBoundsException
	 */
    public static byte[] f2(byte[] secretKey, byte[] rand, byte[] op_c)
    	throws InvalidKeyException, ArrayIndexOutOfBoundsException
    {
    	try{
    		//console.logEntry();
    		
	    	rijndael.init(secretKey);
	        byte[] temp = new byte[16];
	        byte[] out = new byte[16];
	        byte[] rijndaelInput = new byte[16];
	        byte[] res = new byte[8];
	
	        for (int i = 0; i < rand.length && i < op_c.length; i++)
	            rijndaelInput[i] = (byte) (rand[i] ^ op_c[i]);
	        temp = rijndael.encrypt(rijndaelInput);
	
	        /* To obtain output block OUT2: XOR OPc and TEMP, 
	         * rotate by r2=0, and XOR on the constant c2 (which 
	         * is all zeroes except that the last bit is 1). */
	
	        for (int i = 0; i < temp.length && i < op_c.length; i++)
	            rijndaelInput[i] = (byte) (temp[i] ^ op_c[i]);
	        rijndaelInput[15] ^= 1;
	
	        out = rijndael.encrypt(rijndaelInput);
	        for (int i = 0; i < out.length && i < op_c.length; i++)
	            out[i] = (byte) (out[i] ^op_c[i]);
	
	        for (int i = 0; i < res.length; i++)
	            res[i] = (byte) out[i + 8];
	
	        return res;
    	}
    	finally {
    		//console.logExit();
    	}
    } /* end of function f2 */

    /**
     * Algorithm f3.
     * Takes key K and random challenge RAND, and returns 
     * confidentiality key CK.
     * 
     * @param secretKey
     * @param rand
     * @param op_c
     * @return CK confidentiality key
     * @throws InvalidKeyException
     * @throws ArrayIndexOutOfBoundsException
     */
    public static byte[] f3(byte[] secretKey, byte[] rand, byte[] op_c)
		throws InvalidKeyException, ArrayIndexOutOfBoundsException
    {
    	rijndael.init(secretKey);
        byte[] temp = new byte[16];
        byte[] out = new byte[16];
        byte[] rijndaelInput = new byte[16];
        byte[] ck = new byte[16];

        for (int i = 0; i < 16; i++)
            rijndaelInput[i] = (byte) (rand[i] ^ op_c[i]);

        temp = rijndael.encrypt(rijndaelInput);

        /* To obtain output block OUT3: XOR OPc and TEMP, 
         * rotate by r3=32, and XOR on the constant c3 (which
         * is all zeroes except that the next to last bit is 1). */

        for (int i = 0; i < 16; i++)
            rijndaelInput[(i + 12) % 16] = (byte) (temp[i] ^ op_c[i]);
        rijndaelInput[15] ^= 2;

        out = rijndael.encrypt(rijndaelInput);
        for (int i = 0; i < 16; i++)
            out[i] = (byte) (out[i] ^op_c[i]);

        for (int i = 0; i < 16; i++)
            ck[i] = (byte) out[i];
        
        return ck;
    }/* end of function f3 */

    /**
     * Algorithm f4.
     * Takes key K and random challenge RAND, and returns 
     * integrity key IK and anonymity key AK.
     * 
     * @param secretKey
     * @param rand
     * @param op_c
     * @return IK integrity key
     * @throws InvalidKeyException
     * @throws ArrayIndexOutOfBoundsException
     */
    public static byte[] f4(byte[] secretKey, byte[] rand, byte[] op_c)
    	throws InvalidKeyException, ArrayIndexOutOfBoundsException
    {
    	try {
    		//console.logEntry();
	    		
	    	rijndael.init(secretKey);
	        byte[] temp = new byte[16];
	        byte[] out = new byte[16];
	        byte[] rijndaelInput = new byte[16];
	        byte[] ik = new byte[16];
	
	        for (int i = 0; i < 16; i++)
	            rijndaelInput[i] = (byte) (rand[i] ^ op_c[i]);
	
	        temp = rijndael.encrypt(rijndaelInput);
	
	        /* To obtain output block OUT4: XOR OPc and TEMP, 
	         * rotate by r4=64, and XOR on the constant c4 (which
	         * is all zeroes except that the 2nd from last bit is 1). */
	
	        for (int i = 0; i < 16; i++)
	            rijndaelInput[(i + 8) % 16] = (byte) (temp[i] ^ op_c[i]);
	        rijndaelInput[15] ^= 4;
	
	        out = rijndael.encrypt(rijndaelInput);
	        for (int i = 0; i < 16; i++)
	            out[i] = (byte) (out[i] ^op_c[i]);
	
	        for (int i = 0; i < 16; i++)
	            ik[i] = (byte) out[i];
	        
	        return ik;
    	}
    	finally {
    		//console.logExit();
    	}
    } /* end of function f4 */

    
    /**
     * Algorithm f5.
     * Takes key K and random challenge RAND, and returns 
     * anonymity key AK.
     * 
     * @param secretKey
     * @param rand
     * @param op_c
     * @return AK anonymity key
     * @throws InvalidKeyException
     * @throws ArrayIndexOutOfBoundsException
     */
    public static byte[] f5(byte[] secretKey, byte[] rand, byte[] op_c)
    	throws InvalidKeyException, ArrayIndexOutOfBoundsException
    {
    	try {
    		//console.logEntry();

    		rijndael.init(secretKey);
	        byte[] temp = new byte[16];
	        byte[] out = new byte[16];
	        byte[] rijndaelInput = new byte[16];
	        byte[] ak = new byte[6];
	
	        for (int i = 0; i < 16; i++)
	            rijndaelInput[i] = (byte) (rand[i] ^ op_c[i]);
	        temp = rijndael.encrypt(rijndaelInput);
	
	        /* To obtain output block OUT2: XOR OPc and TEMP, 
	         * rotate by r2=0, and XOR on the constant c2 (which 
	         * is all zeroes except that the last bit is 1). */
	
	        for (int i = 0; i < 16; i++)
	            rijndaelInput[i] = (byte) (temp[i] ^ op_c[i]);
	        rijndaelInput[15] ^= 1;
	
	        out = rijndael.encrypt(rijndaelInput);
	        for (int i = 0; i < 16; i++)
	            out[i] = (byte) (out[i] ^op_c[i]);
	
	        for (int i = 0; i < 6; i++)
	            ak[i] = (byte) out[i];
	
	        return ak;
    	}
    	finally {
    		//console.logExit();
    	}
    } /* end of function f5 */

    
    /**
     * Algorithm f1*.
     * Computes resynch authentication code MAC-S from key K, random
     * challenge RAND, sequence number SQN and authentication management
     * field AMF.
     * 
     * @param secretKey
     * @param rand
     * @param op_c
     * @param sqn
     * @param amf
     * @return MAC-S resynch authentication code
     * @throws InvalidKeyException
     * @throws ArrayIndexOutOfBoundsException
     */
    public static byte[] f1star(byte[] secretKey, byte[] rand, byte[] op_c, byte[] sqn, byte[] amf)
    	throws InvalidKeyException, ArrayIndexOutOfBoundsException
    {
    	rijndael.init(secretKey);
        byte[] temp = new byte[16];
        byte[] in1 = new byte[16];
        byte[] out1 = new byte[16];
        byte[] rijndaelInput = new byte[16];
        byte[] mac = new byte[8];

        for (int i = 0; i < 16; i++)
            rijndaelInput[i] = (byte) (rand[i] ^ op_c[i]);
        temp = rijndael.encrypt(rijndaelInput);

        for (int i = 0; i < 6; i++)
        {
            in1[i] = sqn[i];
            in1[i + 8] = sqn[i];
        }
        for (int i = 0; i < 2; i++)
        {
            in1[i + 6] = amf[i];
            in1[i + 14] = amf[i];
        }

         /* XOR op_c and in1, rotate by r1=64, and XOR 
          * on the constant c1 (which is all zeroes)*/

        for (int i = 0; i < 16; i++)
            rijndaelInput[(i + 8) % 16] = (byte) (in1[i] ^ op_c[i]);

         /* XOR on the value temp computed before */ 

        for (int i = 0; i < 16; i++)
            rijndaelInput[i] ^= temp[i];

        out1 = rijndael.encrypt(rijndaelInput);
        for (int i = 0; i < 16; i++)
            out1[i] ^= op_c[i];

        for (int i = 0; i < 8; i++)
            mac[i] = (byte) out1[i+8];

        return mac;
    } /* end of function f1star */

    
    /**
     * Algorithm f5*.
     * Takes key K and random challenge RAND, and return resynch
     * anonymity key AK
     * 
     * @param secretKey
     * @param rand
     * @param op_c
     * @return AK
     * @throws InvalidKeyException
     * @throws ArrayIndexOutOfBoundsException
     */
    public static byte[] f5star(byte[] secretKey, byte[] rand, byte[] op_c)
    	throws InvalidKeyException, ArrayIndexOutOfBoundsException
    {
    	rijndael.init(secretKey);
        byte[] temp = new byte[16];
        byte[] out = new byte[16];
        byte[] rijndaelInput = new byte[16];
        byte[] ak = new byte[6];

        for (int i = 0; i < 16; i++)
            rijndaelInput[i] = (byte) (rand[i] ^ op_c[i]);
        temp = rijndael.encrypt(rijndaelInput);

        /* To obtain output block OUT5: XOR OPc and TEMP, 
         * rotate by r5=96, and XOR on the constant c5 (which
         * is all zeroes except that the 3rd from last bit is 1). */

        for (int i = 0; i < 16; i++)
            rijndaelInput[(i + 4) % 16] = (byte) (temp[i] ^ op_c[i]);
        rijndaelInput[15] ^= 8;

        out = rijndael.encrypt(rijndaelInput);
        for (int i = 0; i < 16; i++)
            out[i] = (byte) (out[i] ^op_c[i]);

        for (int i = 0; i < 6; i++)
            ak[i] = (byte) out[i];

        return ak;
    } /* end of function f5star */

    
    /**
     * Function to compute OPc from OP and K.
     * Assumes key schedule has already been performed
     * 
     * @param secretKey
     * @param op
     * @return OPc
     * @throws InvalidKeyException
     * @throws ArrayIndexOutOfBoundsException
     */
    public static byte[] computeOpC(byte[] secretKey, byte[] op)
    	throws InvalidKeyException, ArrayIndexOutOfBoundsException       
    {
    	try {
	    	//console.logEntry();
	    	
	    	rijndael.init(secretKey);        
	        byte[] byteOp_c = rijndael.encrypt(op);
	        byte[] op_c = new byte[byteOp_c.length];
	        for (int i = 0; i < byteOp_c.length; i++)
	            op_c[i] ^= (byte) (byteOp_c[i] ^ op[i]);
	
	        return op_c;
    	}
    	finally{
    		//console.logExit();
    	}
    } /* end of function computeOpC */
}

