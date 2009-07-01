package org.cipango.littleims.pcscf.debug;

import java.util.Calendar;

import junit.framework.TestCase;

import org.cipango.ims.pcscf.debug.data.DebuginfoDocument;
import org.cipango.ims.pcscf.debug.data.DebuginfoDocument.Debuginfo;

public class DebugConfTest extends TestCase
{

	
	public void testDebugConf() throws Exception
	{
		DebuginfoDocument doc = DebuginfoDocument.Factory.parse(getClass().getResourceAsStream("debugInfo.xml"));
		Debuginfo debuginfo = doc.getDebuginfo();
		//System.out.println(debuginfo);
		/*System.out.println("Debug-ID: " + debuginfo.getDebugconfigArray(0).getSessionArray(0).getControl().getDebugId());
		System.out.println("AOR: " + debuginfo.getDebugconfigArray(0).getAor());
		System.out.println("Attrs:" + debuginfo.getDomNode().getAttributes());
		System.out.println("State:" + debuginfo.getDomNode().getAttributes().getNamedItem("state").getNodeValue());*/
		assertEquals(org.cipango.ims.pcscf.debug.data.DebuginfoDocument.Debuginfo.State.FULL, debuginfo.getState());
		Calendar c = debuginfo.getDebugconfigArray(0).getSessionArray(0).getStartTrigger().getTime();
		System.out.println(c);
	}
}
