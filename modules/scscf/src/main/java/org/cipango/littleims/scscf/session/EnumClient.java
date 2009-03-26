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
package org.cipango.littleims.scscf.session;

import java.util.StringTokenizer;

import javax.servlet.sip.SipFactory;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipURI;
import javax.servlet.sip.TelURL;

import org.apache.log4j.Logger;
import org.cipango.littleims.util.LittleimsException;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.NAPTRRecord;
import org.xbill.DNS.Record;
import org.xbill.DNS.TextParseException;
import org.xbill.DNS.Type;


public class EnumClient
{

	private String _domain;
	private SipFactory _sipFactory;

	private static final Logger __log = Logger.getLogger(EnumClient.class);


	public SipURI translate(TelURL telURL) throws LittleimsException
	{
		if (!telURL.isGlobal())
		{
			throw new LittleimsException("Global Tel URL only", SipServletResponse.SC_BAD_REQUEST);
		}
		__log.debug("Trying to translate tel: " + telURL);
		String number = telURL.getPhoneNumber();
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < number.length(); i++)
		{
			sb.append(number.charAt(number.length() - 1 - i));
			sb.append('.');
		}
		sb.append(_domain);
		String dnsNumber = sb.toString();

		Lookup l = null;
		try
		{
			l = new Lookup(dnsNumber, Type.NAPTR);
		}
		catch (TextParseException e)
		{
			throw new LittleimsException("Cannot build ENUM request", e, SipServletResponse.SC_SERVER_INTERNAL_ERROR);
		}
		Record[] records = l.run();

		__log.debug("Enum result: " + l.getResult());

		if (records == null)
		{
			return null;
		}

		__log.debug("Got " + records.length + " records.");

		for (int i = 0; i < records.length; i++)
		{
			NAPTRRecord r = (NAPTRRecord) records[i];
			__log.debug(r.getService() + "/" + r.getRegexp());
			if (r.getService().equalsIgnoreCase("e2u+sip"))
			{
				String naptr = r.getRegexp();
				char sep = naptr.charAt(0);

				StringTokenizer st = new StringTokenizer(naptr, "" + sep);

				String ere = st.nextToken();
				String repl = st.nextToken();

				if (telURL.isGlobal())
				{
					number = "+" + number;
				}
				ere = ere.replaceAll("\\\\\\\\", "\\\\");

				String sipURI = number.replaceFirst(ere, repl);

				try
				{
					return (SipURI) _sipFactory.createURI(sipURI);
				}
				catch (Exception e)
				{
					throw new LittleimsException("Invalid ENUM SIP URI", e, SipServletResponse.SC_SERVER_INTERNAL_ERROR);
				}
			}
		}
		return null;
	}


	public String getDomain()
	{
		return _domain;
	}


	public void setDomain(String domain)
	{
		_domain = domain;
	}


	public SipFactory getSipFactory()
	{
		return _sipFactory;
	}


	public void setSipFactory(SipFactory sipFactory)
	{
		_sipFactory = sipFactory;
	}

}
