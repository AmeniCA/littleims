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
			throw new LittleimsException("Global Tel URL only",
					SipServletResponse.SC_BAD_REQUEST);
		}
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
			throw new LittleimsException("Cannot build ENUM request", e,
					SipServletResponse.SC_SERVER_INTERNAL_ERROR);
		}
		Record[] records = l.run();

		__log.debug("Enum result for tel URL: " + telURL + " is "
				+ l.getResult() + " and got "
				+ (records == null ? 0 : records.length) + " records.");

		if (records == null)
			return null;

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
					SipURI uri = (SipURI) _sipFactory.createURI(sipURI);
					__log.info("Enum resolution successful: able to transform " + telURL + " to " + uri);
					return uri;
				}
				catch (Exception e)
				{
					throw new LittleimsException("Invalid ENUM SIP URI: "
							+ sipURI, e,
							SipServletResponse.SC_SERVER_INTERNAL_ERROR);
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

	public static void main(String[] args) throws LittleimsException
	{
		/*
		String number = "882990086330";
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < number.length(); i++)
		{
			sb.append(number.charAt(number.length() - 1 - i));
			sb.append('.');
		}
		sb.append("e164.org");
		String dnsNumber = sb.toString();

		Lookup l = null;
		try
		{
			l = new Lookup(dnsNumber, Type.NAPTR);
		}
		catch (TextParseException e)
		{
			throw new LittleimsException("Cannot build ENUM request", e,
					SipServletResponse.SC_SERVER_INTERNAL_ERROR);
		}
		Record[] records = l.run();

		__log.debug("Enum result for tel URL: " + number + " is "
				+ l.getResult() + " and got "
				+ (records == null ? 0 : records.length) + " records.");
*/
		String number = "+1002";
		String naptr = "!^\\+1002$sip:carol@cipango.org!";
		char sep = naptr.charAt(0);

		StringTokenizer st = new StringTokenizer(naptr, "" + sep);

		String ere = st.nextToken();
		String repl = st.nextToken();
		System.out.println("ere: " + ere + " / repl " + repl);

		ere = ere.replaceAll("\\\\\\\\", "\\\\");

		String sipURI = number.replaceFirst(ere, repl);
		System.out.println(sipURI);
		
	}
}
