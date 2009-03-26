// ========================================================================
// Copyright 2009 NEXCOM Systems
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
package org.cipango.littleims.hss;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.sip.SipServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.cipango.littleims.cx.ResultCode;
import org.cipango.littleims.cx.data.userprofile.IMSSubscriptionDocument;
import org.cipango.littleims.cx.data.userprofile.TPublicIdentity;
import org.cipango.littleims.cx.data.userprofile.TServiceProfile;
import org.cipango.littleims.util.LittleimsException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class FileHssDao implements HssDao
{

	private static final Logger __log = Logger.getLogger(FileHssDao.class);
	
	private static final String DEFAULT_PROFILE = "<?xml version=\"1.0\"?>" + "<IMSSubscription>"
	+ "  <PrivateID>%private</PrivateID>" + "    <ServiceProfile>"
	+ "      <PublicIdentity>" + "        <Identity>%public</Identity>"
	+ "      </PublicIdentity>" + "    </ServiceProfile>" + "</IMSSubscription>";

	public static final String HSS_DIR = "hss.dir";
	public static final String BASE_URL = "base.url";
	
	private Map<String, Credentials> _users = new HashMap<String, Credentials>();
	private Map<String, IMSSubscriptionDocument> _profiles = new HashMap<String, IMSSubscriptionDocument>();
	private Map<String, IMSSubscriptionDocument> _wilcardProfiles = new HashMap<String, IMSSubscriptionDocument>();

	private File _hssDirectory;
	
	public void init() throws Exception
	{
		if (!_hssDirectory.exists() || !_hssDirectory.isDirectory())
		{
			throw new LittleimsException("Invalid HSS direcory: " + _hssDirectory, SipServletResponse.SC_SERVER_INTERNAL_ERROR);
		}

		File f = new File(_hssDirectory, "users.xml");
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document doc = db.parse(f);

		Element rootE = (Element) doc.getElementsByTagName("users").item(0);
		loadUsers(rootE);

		File profilesDir = new File(_hssDirectory, "profiles");
		File[] files = profilesDir.listFiles(new FilenameFilter()
		{
			public boolean accept(File file, String name)
			{
				return name.endsWith(".xml");
			}
		});
		
		int i;
		for (i = 0; i < files.length; i++)
		{
			File profile = files[i];
			loadProfile(new FileInputStream(profile));
		}
		
		// check that all declared users have a userprofile, if not create a
		// default one
		Iterator<String> usersIt = _users.keySet().iterator();
		while (usersIt.hasNext())
		{
			String publicID = usersIt.next();
			if (!_profiles.containsKey(publicID))
			{
				__log.warn("No profile for " + publicID + ". Creating default profile");
				Credentials c = _users.get(publicID);
				String user = c.getUserNoAuth();
				String defaultProfile = DEFAULT_PROFILE.replaceAll("%private", user).replaceAll(
						"%public", publicID);
				loadProfile(new ByteArrayInputStream(defaultProfile.getBytes()));
			}
		}
	}
	
	private void loadProfile(InputStream profile) throws Exception
	{
		IMSSubscriptionDocument imsSub = null;

		imsSub = IMSSubscriptionDocument.Factory.parse(profile);

		// get all identities
		TServiceProfile[] serviceProfiles = imsSub.getIMSSubscription().getServiceProfileArray();
		for (int i = 0; i < serviceProfiles.length; i++)
		{
			TServiceProfile sp = (TServiceProfile) serviceProfiles[i];
			TPublicIdentity[] publicIdentities = sp.getPublicIdentityArray();
			for (int j = 0; j < publicIdentities.length; j++)
			{
				TPublicIdentity identity = publicIdentities[j];
				String publicID = identity.getIdentity();
				//identity.getExtension().getIdentityType()
				if (publicID.indexOf("!") > -1) // Wilcard profile
				{
					if (_wilcardProfiles.containsKey(publicID))
					{
						__log.warn("Wilcard Profile for " + publicID + " has already been loaded.");
					}
					else
					{
						_wilcardProfiles.put(publicID, imsSub);
						
						__log.debug("Loaded wilcard profile for " + publicID);
					}
				}
				else
				{
					if (_profiles.containsKey(publicID))
					{
						__log.warn("Profile for " + publicID + " has already been loaded.");
					}
					else
					{
						_profiles.put(publicID, imsSub);
						__log.debug("Loaded profile for " + publicID);
					}
				}
			}
		}
	}

	private void loadUsers(Element e)
	{
		NodeList nl = e.getElementsByTagName("public");
		for (int i = 0; i < nl.getLength(); i++)
		{
			Element userE = (Element) nl.item(i);
			Credentials credentials = new Credentials();
			String publicID = userE.getAttribute("id");
			NodeList privateIDs = userE.getElementsByTagName("private");
			for (int j = 0; j < privateIDs.getLength(); j++)
			{
				Element privateE = (Element) privateIDs.item(j);
				String privateID = privateE.getAttribute("id");
				String password = privateE.getAttribute("password");
				String operatorId = privateE.getAttribute("operatorId");
				credentials.addPassword(privateID, password, operatorId);
			}
			_users.put(publicID, credentials);
			__log.debug("Adding user " + publicID + " with credentials " + credentials);
		}
	}
	
	public Credentials getCredentials(String publicUserIdentity)
	{
		// TODO wilcard
		return _users.get(publicUserIdentity);
	}
	
	public IMSSubscriptionDocument getUserProfile(String publicUserIdentity)
	{
		return getUserProfile(publicUserIdentity, null);
	}

	public IMSSubscriptionDocument getUserProfile(String publicUserIdentity, String wilcardPublicId)
	{
		IMSSubscriptionDocument userProfile = null;
		
		if (wilcardPublicId != null)
		{
			userProfile = _wilcardProfiles.get(wilcardPublicId);
			// TODO clone 
		}
		
		if (userProfile == null)
			userProfile = (IMSSubscriptionDocument) _profiles.get(publicUserIdentity);
		
		if (userProfile != null)
			return userProfile;

		Iterator<String> i = _wilcardProfiles.keySet().iterator();
		while (i.hasNext())
		{
			String publicID = i.next();

			// wildcard psi
			if (publicUserIdentity.matches(publicID.replaceAll("!", "")))
			{
				__log.debug(publicUserIdentity + " matched wildcard PSI " + publicID);
				userProfile = _wilcardProfiles.get(publicID);
				__log.debug("userProfile: " + userProfile);
				return userProfile;
			}
		}
		return null;
	}
	
	public String getWilcardPublicIdentity(String publicUserIdentity) throws HssException
	{
		if (_profiles.containsKey(publicUserIdentity))
		{
			return null;
		}
		Iterator<String> i = _wilcardProfiles.keySet().iterator();
		while (i.hasNext())
		{
			String publicID = i.next();

			// wildcard psi
			if (publicUserIdentity.matches(publicID.replaceAll("!", "")))
			{
				__log.debug(publicUserIdentity + " matched wildcard PSI " + publicID);
				return publicID;
			}
		}
		throw new HssException(ResultCode.DIAMETER_ERROR_USER_UNKNOWN, "unknown user: " + publicUserIdentity);
	}

	public TPublicIdentity getIdentity(String publicUserIdentity, String wilcardPublicId) throws HssException
	{
		IMSSubscriptionDocument doc = getUserProfile(publicUserIdentity, wilcardPublicId);
		TServiceProfile[] profiles = doc.getIMSSubscription().getServiceProfileArray();
		String key = wilcardPublicId == null ? publicUserIdentity : wilcardPublicId;
		for (TServiceProfile profile: profiles)
		{
			TPublicIdentity[] identities = profile.getPublicIdentityArray();
			for (TPublicIdentity identity : identities)
			{	
				if (identity.getIdentity().equals(key))
					return identity;
			}
		}
		throw new HssException(ResultCode.DIAMETER_ERROR_USER_UNKNOWN, "unknown user: " + publicUserIdentity);
	}

	public File getHssDirectory()
	{
		return _hssDirectory;
	}

	public void setHssDirectory(File hssDirecory)
	{
		_hssDirectory = hssDirecory;
	}

}
