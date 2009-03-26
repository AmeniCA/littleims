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
package org.cipango.littleims.scscf.data;

import java.io.ByteArrayInputStream;

import junit.framework.TestCase;

import org.cipango.littleims.cx.data.userprofile.SharedIFCsDocument;
import org.cipango.littleims.cx.data.userprofile.TSharedIFC;

public class SharedIFCsTest extends TestCase
{

	/**
	 * @param args
	 */
	public void testIfc() throws Exception
	{
		SharedIFCsDocument ifcs = SharedIFCsDocument.Factory.parse(new ByteArrayInputStream(
				getUserProfile("toto", "titi").getBytes()));
		TSharedIFC ifc = ifcs.getSharedIFCs().getSharedIFCArray(0);
		assertEquals(-1, ifc.getID());
	}

	private static String getUserProfile(String privateID, String publicID)
	{
		return "<?xml version=\"1.0\"?>" 
				+ "<SharedIFCs>" 
				+ "	<SharedIFC>" 
				+ "		<ID>-1</ID>"
				+ "		<InitialFilterCriteria>" 
				+ "		<Priority>1</Priority>" 
				+ "		<TriggerPoint>"
				+ "			<ConditionTypeCNF>1</ConditionTypeCNF>" 
				+ "			<SPT>"
				+ "				<ConditionNegated>1</ConditionNegated>" 
				+ "				<Group>0</Group>"
				+ "				<SessionCase>0</SessionCase>" 
				+ "			</SPT>" 
				+ "		</TriggerPoint>"
				+ "		<ApplicationServer>" 
				+ "			<ServerName>sip:127.0.0.1:5080;lr</ServerName>"
				+ "			<DefaultHandling>0</DefaultHandling>" 
				+ "		</ApplicationServer>"
				+ "		</InitialFilterCriteria>" 
				+ "	</SharedIFC>" 
				+ "</SharedIFCs>";
	}

}
