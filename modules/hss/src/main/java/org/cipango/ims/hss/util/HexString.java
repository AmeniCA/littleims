//====================================================================
//Copyright (C) 2007 NEXCOM Systems
//
//This program is free software: you can redistribute it and/or modify
//it under the terms of the GNU Affero General Public License as
//published by the Free Software Foundation, either version 3 of the
//License, or (at your option) any later version.
//
//This program is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU Affero General Public License for more details.
//
//You should have received a copy of the GNU Affero General Public License
//along with this program.  If not, see <http://www.gnu.org/licenses/>.
//====================================================================

package org.cipango.ims.hss.util;

public class HexString 
{
	private static final char __hexChars[] =
		{ '0','1','2','3','4','5','6','7','8','9','a','b','c','d','e','f' };
	
	private static final char __hexCharsUC[] =
		{ '0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F' };
    
    public static void appendHex(StringBuffer buf, int c)
    {
    	buf.append(__hexCharsUC[(c&0xf0)>>4]);
		buf.append(__hexCharsUC[c&0x0f]);
    }
    
	public static String toHexString(String s)
	{
	    return toHexString(s.getBytes());
	}
	    
	public static String toHexString(byte buffer[])
	{
		return toHexString(buffer, 0, buffer.length);
	}
	
    public static String toHexString(byte b[], int offset, int length)
    {
    	StringBuffer hex = new StringBuffer(2*length);
    	for (int i = offset; i < offset + length; i++)
    	{
    		hex.append(__hexChars[(b[i]&0xf0)>>4]);
    		hex.append(__hexChars[b[i]&0x0f]);
    	}
    	
    	return hex.toString();
    }
   
    public static byte[] fromHexString(String hex) throws NumberFormatException
    {
    	int length = hex.length();
    	
    	byte[] buf = new byte[(length + 1) / 2];
		boolean	even = true;
		byte b = 0;
		int	offset = 0;

		if ((length % 2) == 1)
			even = false;
    	
    	for (int i = 0; i < length; i++)
    	{
    		char c = hex.charAt(i);
    		int	nibble;	
    		
    		if ((c >= '0') && (c <= '9'))
    			nibble = c - '0';
    		else if ((c >= 'A') && (c <= 'F'))
    			nibble = c - 'A' + 0x0A;
    		else if ((c >= 'a') && (c <= 'f'))
    			nibble = c - 'a' + 0x0A;
    		else
    			throw new NumberFormatException("Invalid hex digit '" + c + "'.");
    		
    		if (even)
    		{
    			b = (byte) (nibble << 4);
    		}
    		else
    		{
    			b += (byte) nibble;
    			buf[offset++] = b;
    		}
    		
    		even = ! even;
    	}
    	return buf;
    }
    
    public static String toDetailedHexString(byte[] b, int length)
    {
    	StringBuffer sb = new StringBuffer(5*length);
    	
		int lines = (length >>> 4) + 1;
		
		for (int i = 0; i < lines; i++)
		{
		    sb.append(__hexChars[(i>>>4&0xf0)>>4]);
			sb.append(__hexChars[i>>>4&0x0f]);
			sb.append(__hexChars[(i<<4&0xf0)>>4]);
			sb.append(__hexChars[i<<4&0x0f]);
			sb.append(' ');
			
			int n = (i == lines - 1) ? length & 0xf : 16;
			
			for (int j = 0; j < 16; j++)
			{
				if (j < n)
				{
					int c = b[i*16+j];
				    sb.append(__hexChars[(c&0xf0)>>4]);
					sb.append(__hexChars[c&0x0f]);
					sb.append(' ');
				}
				else
					sb.append("   ");
			}
			
			for (int j = 0; j < n; j++)
			{
				int c = b[i*16+j] & 0xff;
				if (c > 0x1f && c < 0x7f)
					sb.append((char) c);
				else
					sb.append('.');
			}
				
			sb.append('\r'); sb.append('\n');
		}
		return sb.toString();
    }
}