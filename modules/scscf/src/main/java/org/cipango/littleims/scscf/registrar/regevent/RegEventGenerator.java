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
package org.cipango.littleims.scscf.registrar.regevent;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Iterator;

import org.cipango.littleims.scscf.registrar.regevent.RegInfo.ContactInfo;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;


public class RegEventGenerator
{

	private static final String AOR_ATTR = "aor";
	private static final String STATE_ATTR = "state";
	private static final String ID_ATTR = "id";
	private static final String EVENT_ATTR = "event";

	public RegEventGenerator() throws Exception
	{
		OutputFormat format = OutputFormat.createPrettyPrint();
		xmlWriter = new XMLWriter(format);
	}

	private XMLWriter xmlWriter;

	public String generateRegInfo(RegEvent e, String state, int version)
	{

		Document document = DocumentHelper.createDocument();
		Element reginfo = document.addElement("reginfo", "urn:ietf:params:xml:ns:reginfo");
		reginfo.addAttribute("state", state);
		reginfo.addAttribute("version", String.valueOf(version));

		Iterator<RegInfo> it = e.getRegInfos().iterator();
		while (it.hasNext())
		{
			RegInfo regInfo = it.next();
			Element registration = reginfo.addElement("registration");
			registration.addAttribute(AOR_ATTR, regInfo.getAor());
			registration.addAttribute(STATE_ATTR, regInfo.getAorState().getValue());
			registration.addAttribute(ID_ATTR, "123");

			Iterator<ContactInfo> it2 = regInfo.getContacts().iterator();
			while (it2.hasNext())
			{
				ContactInfo contactInfo = it2.next();
				Element contactE = registration.addElement("contact");
				contactE.addAttribute(STATE_ATTR, contactInfo.getContactState().getValue());
				contactE.addAttribute(EVENT_ATTR, contactInfo.getContactEvent().getValue());
				contactE.addElement("uri").addText(contactInfo.getContact());
			}
		}

		StringWriter sw = new StringWriter();

		try
		{
			synchronized (xmlWriter)
			{
				xmlWriter.setWriter(sw);
				xmlWriter.write(document);
			}
			return sw.toString();
		}
		catch (IOException ex)
		{
			ex.printStackTrace();
			return "";
		}
	}
}
