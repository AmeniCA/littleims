package org.cipango.littleims.util;

import org.cipango.littleims.util.RegexUtil;

import junit.framework.TestCase;

public class RegexUtilTest extends TestCase
{

	public void testRegex()
	{
		assertMatches("sip:chatlist!.*!@example.com", "sip:chatlist1@example.com");
		assertMatches("sip:chatlist.1!.*!@example.com", "sip:chatlist.12@example.com");
		assertNotMatches("sip:chatlist1-3!.*!@example.com", "sip:chatlist2@example.com");
		assertMatches("sip:chatlist1-3!.*!@example.com", "sip:chatlist1-32@example.com");
		
		assertMatches("sip:?chatlist!.*!@example.com", "sip:?chatlist1@example.com");
		assertNotMatches("sip:?chatlist!.*!@example.com", "sip:Zchatlist1@example.com");
		
		assertMatches("sip:.chatlist!.*!@example.com", "sip:.chatlist1@example.com");
		assertNotMatches("sip:.chatlist!.*!@example.com", "sip:Zchatlist1@example.com");
		
		assertMatches("sip:$chatlist!.*!@example.com", "sip:$chatlist1@example.com");
		assertNotMatches("sip:$chatlist!.*!@example.com", "sip:chatlist1@example.com");
		
		assertMatches("sip:+chatlist!.*!@example.com", "sip:+chatlist1@example.com");
		assertNotMatches("sip:+chatlist!.*!@example.com", "sip::chatlist1@example.com");
		
		assertMatches("sip:|chatlist!.*!@example.com", "sip:|chatlist1@example.com");
		assertNotMatches("sip:|chatlist!.*!@example.com", "sip:");
		
		assertMatches("sip:[chat]list!.*!@example.com", "sip:[chat]list1@example.com");
		assertNotMatches("sip:[chat]list!.*!@example.com", "sip:clist1@example.com");
		
		assertMatches("sip:[a-z]list!.*!@example.com", "sip:[a-z]list1@example.com");
		assertNotMatches("sip:[a-z]list!.*!@example.com", "sip:flist1@example.com");
		
		assertMatches("sip:*chatlist!.*!@example.com", "sip:*chatlist1@example.com");
		assertNotMatches("sip:*chatlist!.*!@example.com", "sip::chatlist1@example.com");
	}

	public void assertMatches(String regex, String s)
	{

		assertTrue(s.matches(RegexUtil.extendedRegexToJavaRegex(regex)));
	}
	
	public void assertNotMatches(String regex, String s)
	{
		assertFalse(s.matches(RegexUtil.extendedRegexToJavaRegex(regex)));
	}
	

}
