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

package org.cipango.ims.oam.util;

import java.util.Random;

import org.cipango.littleims.util.HexString;


public abstract class ID 
{
    private static Random __random = new Random();
    

    public static String newDebugId()
    {
    	return newID(3);
    }
    
    public static String newID(int length) 
    {
    	byte[] b = new byte[length];
    	synchronized (__random) 
    	{
        	__random.nextBytes(b);
		}
    	return HexString.bufferToHex(b);
    }

    public static String newTag() 
    {
    	return newID(4); // RFC 3261: at least 32 bits of randomness
    }
    
}
