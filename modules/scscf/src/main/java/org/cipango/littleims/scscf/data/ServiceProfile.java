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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import javax.servlet.sip.SipServletResponse;

import org.cipango.littleims.util.LittleimsException;


public class ServiceProfile
{

	private static Comparator<InitialFilterCriteria> ifcComparator = new Comparator<InitialFilterCriteria>()
	{

		public int compare(InitialFilterCriteria o1, InitialFilterCriteria o2)
		{
			int p1 = o1.getPriority();
			int p2 = o2.getPriority();

			if (p1 < p2)
			{
				return -1;
			}
			else if (p1 == p2)
			{
				return 0;
			}
			else
			{
				return 1;
			}
		}

	};

	public static final int DEFAULT_MEDIA_PROFILE = -1;

	public void setMediaProfile(int mediaProfile)
	{
		this.mediaProfile = mediaProfile;
	}

	public void addIFC(InitialFilterCriteria ifc) throws LittleimsException
	{
		Iterator<InitialFilterCriteria> it = ifcs.iterator();
		while (it.hasNext())
		{
			InitialFilterCriteria otherIFC = it.next();
			if (otherIFC.getPriority() == ifc.getPriority())
			{
				throw new LittleimsException("IFCs already contain an IFC with priority: "
						+ ifc.getPriority(), SipServletResponse.SC_SERVER_INTERNAL_ERROR);
			}
		}
		ifcs.add(ifc);
		Collections.sort(ifcs, ifcComparator);
	}

	public Iterator<InitialFilterCriteria> getIFCsIterator()
	{
		return ifcs.iterator();
	}

	public String toString()
	{
		return ifcs.toString();
	}

	private List<InitialFilterCriteria> ifcs = new ArrayList<InitialFilterCriteria>();
	private int mediaProfile = DEFAULT_MEDIA_PROFILE;
}
