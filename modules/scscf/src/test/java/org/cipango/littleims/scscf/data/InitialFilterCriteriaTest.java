package org.cipango.littleims.scscf.data;

import java.io.IOException;

import junit.framework.TestCase;

import org.apache.xmlbeans.XmlException;
import org.cipango.littleims.cx.data.userprofile.InitialFilterCriteriaDocument;
import org.cipango.littleims.cx.data.userprofile.TInitialFilterCriteria;
import org.cipango.littleims.scscf.data.trigger.TriggerPointCompiler;

public class InitialFilterCriteriaTest extends TestCase
{
	
	private TriggerPointCompiler _tpCompiler = new TriggerPointCompiler();

	public void testHeaderCriteria() throws XmlException, IOException
	{
		InitialFilterCriteria ifc = getIfc("IfcHeader.xml");
						
		assertEquals(1, ifc.getPriority());
		AS as = ifc.getAs();
		assertEquals("sip:tispan@cipango.org;lr", as.getURI());
		assertEquals(0, as.getDefaultHandling());
		assertEquals("", as.getServiceInfo());
		assertTrue(as.getIncludeRegisterRequest());
		assertFalse(as.getIncludeRegisterResponse());
	}
	
	private InitialFilterCriteria getIfc(String resourceName) throws XmlException, IOException
	{
		InitialFilterCriteriaDocument ifcDoc = InitialFilterCriteriaDocument.Factory.parse(getClass().getResourceAsStream(resourceName));
		TInitialFilterCriteria tIfc = ifcDoc.getInitialFilterCriteria();
		return new InitialFilterCriteria(
				tIfc.getPriority(),
				_tpCompiler.compile(tIfc.getTriggerPoint()),
				new AS(tIfc.getApplicationServer()));
		
	}
}
