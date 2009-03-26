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
package org.cipango.littleims.scscf.registrar;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

public class ServiceInfoGenerator
{

	private static final String IMS_3GPP = "ims-3gpp";
	private static final String SERVICE_INFO = "service-info";

	public static final String MIME_TYPE = "application/3gpp-ims+xml";

	public ServiceInfoGenerator()
	{
		try
		{
			OutputFormat format = OutputFormat.createPrettyPrint();
			xmlWriter = new XMLWriter(format);
		}
		catch (UnsupportedEncodingException e)
		{
			throw new RuntimeException(e);
		}
	}

	public String generateXML(String serviceInfo)
	{

		Document document = DocumentHelper.createDocument();
		Element root = document.addElement(IMS_3GPP);
		root.addElement(SERVICE_INFO).setText(serviceInfo);

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
			log.error("Cannot create ims-3gpp XML document: ", ex);
			return "";
		}
	}

	private XMLWriter xmlWriter;
	private static final Logger log = Logger.getLogger(ServiceInfoGenerator.class);
}
