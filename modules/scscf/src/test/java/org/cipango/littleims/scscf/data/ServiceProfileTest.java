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

import java.util.Iterator;

import org.cipango.littleims.scscf.data.InitialFilterCriteria;
import org.cipango.littleims.scscf.data.ServiceProfile;

import junit.framework.TestCase;

public class ServiceProfileTest extends TestCase
{


	public void testOrderIFCs() throws Exception
	{
		ServiceProfile sp = new ServiceProfile();
		sp.addIFC(new InitialFilterCriteria(1, null, null));
		sp.addIFC(new InitialFilterCriteria(8, null, null));
		sp.addIFC(new InitialFilterCriteria(4, null, null));
		Iterator<InitialFilterCriteria> it = sp.getIFCsIterator();
		int p1 = it.next().getPriority();
		int p2 = it.next().getPriority();
		int p3 = it.next().getPriority();

		assertEquals(1, p1);
		assertEquals(4, p2);
		assertEquals(8, p3);
	}

}
