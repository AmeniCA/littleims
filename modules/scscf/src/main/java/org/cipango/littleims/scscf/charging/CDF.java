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
package org.cipango.littleims.scscf.charging;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import javax.servlet.sip.SipServletRequest;

import org.apache.log4j.Logger;
import org.cipango.littleims.scscf.util.IDGenerator;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;


public class CDF
{

	public static final int ROLE_NODE_ORIGINATING = 0;
	public static final int ROLE_NODE_TERMINATING = 1;
	public static final int ROLE_NODE_PROXY = 2;
	
	private static final Logger log = Logger.getLogger(CDF.class);

	private Map<String, Document> cdrs = new HashMap<String, Document>();
	private File _chargingDirectory;
	private XMLWriter xmlWriter;
	private IDGenerator idGenerator = new IDGenerator();
	private SimpleDateFormat dateFormat;
	private boolean _enabled;

	public CDF() throws Exception
	{
		OutputFormat format = OutputFormat.createPrettyPrint();
		xmlWriter = new XMLWriter(format);

		dateFormat = new SimpleDateFormat("yyMMddHHmmss");
		dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
	}

	public String start(SipServletRequest event, int role)
	{
		log.debug("Start Session CDR");
		Document document = DocumentHelper.createDocument();
		Element root = document.addElement("ims-cdr");
		root.addElement("sip-method").setText(event.getMethod());
		root.addElement("role-of-node").setText(String.valueOf(role));
		root.addElement("session-id").setText(event.getCallId());
		root.addElement("calling-party-address").setText(event.getFrom().getURI().toString());
		root.addElement("called-party-address").setText(event.getTo().getURI().toString());
		root.addElement("serviceDeliveryStartTimeStamp").setText(getDate());

		String cdrID = idGenerator.newRandomID();
		cdrs.put(cdrID, document);
		return cdrID;
	}

	public void stop(String id)
	{
		log.debug("Stop Session CDR");
		Document document = (Document) cdrs.remove(id);
		if (document != null)
		{
			document.getRootElement().addElement("serviceDeliveryStopTimeStamp").setText(getDate());
			try
			{
				File cdr = new File(_chargingDirectory, idGenerator.newRandomID() + ".xml");
				FileOutputStream cdrOut = new FileOutputStream(cdr);
				synchronized (xmlWriter)
				{
					xmlWriter.setOutputStream(cdrOut);
					xmlWriter.write(document);
					xmlWriter.flush();
				}
				cdrOut.close();
			}
			catch (IOException e)
			{
				log.error("Error while writing CDR", e);
			}
		}
		else
		{
			log.warn("Cannot find Start Event for id " + id);
		}
	}

	private String getDate()
	{
		return dateFormat.format(new Date());
	}

	public void event(SipServletRequest event, int role)
	{
		log.debug("Generate Event CDR");
		Document document = DocumentHelper.createDocument();
		Element root = document.addElement("ims-cdr");
		root.addElement("sip-method").setText(event.getMethod());
		root.addElement("role-of-node").setText(String.valueOf(role));
		root.addElement("session-id").setText(event.getCallId());
		root.addElement("calling-party-address").setText(event.getFrom().getURI().toString());
		root.addElement("called-party-address").setText(event.getTo().getURI().toString());

		try
		{
			File cdr = new File(_chargingDirectory, idGenerator.newRandomID() + ".xml");
			FileOutputStream cdrOut = new FileOutputStream(cdr);
			synchronized (xmlWriter)
			{
				xmlWriter.setOutputStream(cdrOut);
				xmlWriter.write(document);
				xmlWriter.flush();
			}
			cdrOut.close();
		}
		catch (IOException e)
		{
			log.error("Error while writing CDR", e);
		}
	}

	public File getChargingDirectory()
	{
		return _chargingDirectory;
	}

	public void setChargingDirectory(File chargingDirectory)
	{
		_chargingDirectory = chargingDirectory;
	}

	public SimpleDateFormat getDateFormat()
	{
		return dateFormat;
	}

	public void setDateFormat(SimpleDateFormat dateFormat)
	{
		this.dateFormat = dateFormat;
	}

	public boolean isEnabled()
	{
		return _enabled;
	}

	public void setEnabled(boolean enabled)
	{
		_enabled = enabled;
	}
}
