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

import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlException;
import org.cipango.littleims.cx.data.userprofile.IMSSubscriptionDocument;
import org.cipango.littleims.cx.data.userprofile.SharedIFCsDocument;
import org.cipango.littleims.cx.data.userprofile.TCoreNetworkServicesAuthorization;
import org.cipango.littleims.cx.data.userprofile.TInitialFilterCriteria;
import org.cipango.littleims.cx.data.userprofile.TPublicIdentity;
import org.cipango.littleims.cx.data.userprofile.TServiceProfile;
import org.cipango.littleims.cx.data.userprofile.TSharedIFC;
import org.cipango.littleims.scscf.data.trigger.CriteriaMatch;
import org.cipango.littleims.scscf.data.trigger.TriggerPointCompiler;
import org.cipango.littleims.util.LittleimsException;
import org.cipango.littleims.util.RegexUtil;


public class UserProfileCache
{

	private URL _sharedIfcsUrl;
	private ConcurrentHashMap<String, UserProfile> _serviceProfiles = new ConcurrentHashMap<String, UserProfile>();
	private ConcurrentHashMap<String, UserProfile> _wildcardServiceProfiles = new ConcurrentHashMap<String, UserProfile>();
	
	private TriggerPointCompiler _tpCompiler = new TriggerPointCompiler();
	private Map<Integer, InitialFilterCriteria> _sharedIFCs;

	private static final Logger __log = Logger.getLogger(UserProfileCache.class);


	public void init() throws XmlException, IOException
	{
		refreshSharedIFCs();
	}

	public void cacheUserProfile(IMSSubscriptionDocument imsSub)
	{
		try
		{
			// check all Service Profiles
			TServiceProfile[] profiles = imsSub.getIMSSubscription().getServiceProfileArray();
			for (TServiceProfile profile :profiles)
			{
				// Build a single ServiceProfile
				// May contain several IFCs and be associated to several Public
				// Identities
				ServiceProfile serviceProfile = new ServiceProfile();

				// Core Network Service Authorization
				TCoreNetworkServicesAuthorization cnsa = profile
						.getCoreNetworkServicesAuthorization();
				if (cnsa != null)
					serviceProfile.setMediaProfile(cnsa.getSubscribedMediaProfileId());

				// Initial Filter Criterias
				for (TInitialFilterCriteria tIFC : profile.getInitialFilterCriteriaArray())
				{
					InitialFilterCriteria ifc = createIFC(tIFC);
					try
					{
						serviceProfile.addIFC(ifc);
					}
					catch (LittleimsException e)
					{
						__log.error("Cannot add IFC: " + ifc + " because " + e.getMessage());
					}
				}

				// Shared IFC
				if (profile.getExtension() != null)
				{
					int[] sIFCs = profile.getExtension().getSharedIFCSetIDArray();

					if (_sharedIFCs == null)
						refreshSharedIFCs();
					
					for (int j = 0; j < sIFCs.length; j++)
					{
						Integer ifcID = new Integer(sIFCs[j]);
						
						if (_sharedIFCs == null)
						{
							__log.warn("Could not found shared IFC with ID: " + ifcID);
							continue;
						}
						InitialFilterCriteria ifc = (InitialFilterCriteria) _sharedIFCs.get(ifcID);
						if (ifc != null)
						{
							try
							{
								serviceProfile.addIFC(ifc);
							}
							catch (LittleimsException e)
							{
								__log.error("Cannot add IFC: " + ifc + " because " + e.getMessage());
							}
						}
						else
							__log.warn("Could not found shared IFC with ID: " + ifcID);
					}
				}

				// Public Identities that use this ServiceProfile
				TPublicIdentity[] publicIDs = profile.getPublicIdentityArray();
				for (TPublicIdentity publicID : publicIDs)
				{
					boolean wildcard = publicID.getExtension() != null && publicID.getExtension().getWildcardedPSI() != null; 
					String identity = wildcard ? publicID.getExtension().getWildcardedPSI() : publicID.getIdentity();
					
					UserProfile newProfile = new UserProfile(identity);
					UserProfile userProfile;
					if (wildcard)
					{
						String regex = RegexUtil.extendedRegexToJavaRegex(identity);
						userProfile = _wildcardServiceProfiles.putIfAbsent(regex, newProfile);
					}
					else
						userProfile = _serviceProfiles.putIfAbsent(identity, newProfile);
	
					if (userProfile == null)
						userProfile = newProfile;
					
					userProfile.setBarred(publicID.getBarringIndication());
					userProfile.setServiceProfile(serviceProfile);
					if (publicID.getExtension() != null
							&& publicID.getExtension().getExtension() != null
							&& publicID.getExtension().getExtension().getExtension() != null)
						userProfile.setServiceLevelTraceInfo(publicID.getExtension().getExtension().getExtension().getServiceLevelTraceInfo());
					else
						userProfile.setServiceLevelTraceInfo(null);
					__log.debug("Cache user profile for identity " + identity);
					// TODO add UserProfileListener to unregister user if no debug subscription is set. 
				}
			}
		}
		catch (Exception e)
		{
			__log.warn("Failed to cache profile for user: " + imsSub.getIMSSubscription().getPrivateID(), e);
		}
	}
	
	public void clearUserProfile(String publicID)
	{
		if (_serviceProfiles.remove(publicID) == null)
			__log.debug("Could not remove profile for public identity " + publicID 
					+ ": Profile not found");

	}
	
	public UserProfile getProfile(String publicID, String pProfileKey)
	{
		UserProfile userProfile;
		
		if (pProfileKey != null)
		{
			userProfile = _wildcardServiceProfiles.get(pProfileKey);
			if (userProfile != null)
				return userProfile;
		}
		userProfile = _serviceProfiles.get(publicID);
		if (userProfile != null)
		{
			return userProfile;
		}
		else
		{
			Iterator<String> it = _wildcardServiceProfiles.keySet().iterator();
			while (it.hasNext())
			{
				String wilcard = (String) it.next();
				if (publicID.matches(wilcard))
					return _wildcardServiceProfiles.get(wilcard);
			}
		}

		__log.debug("Could not found profile for public identity " + publicID);
		return  null;
	}
	
	private InitialFilterCriteria createIFC(TInitialFilterCriteria tifc)
	{
		CriteriaMatch trigger = _tpCompiler.compile(tifc.getTriggerPoint());
		AS as = new AS(tifc.getApplicationServer());

		InitialFilterCriteria ifc = new InitialFilterCriteria(tifc.getPriority(), trigger, as);
		return ifc;
	}

	public URL getSharedIfcsUrl()
	{
		return _sharedIfcsUrl;
	}

	public void setSharedIfcsUrl(URL sharedIfcsUrl)
	{
		_sharedIfcsUrl = sharedIfcsUrl;
	}
	
	public Map<String, UserProfile> getUserProfiles()
	{
		return _serviceProfiles;
	}
	
	public Map<String, UserProfile> getWildcardUserProfiles()
	{
		return _wildcardServiceProfiles;
	}
	
	public void clearAllProfiles()
	{
		_wildcardServiceProfiles.clear();
		_serviceProfiles.clear();
	}

	public Map<Integer, InitialFilterCriteria> getSharedIFCs()
	{
		return _sharedIFCs;
	}
	
	public void refreshSharedIFCs() throws XmlException, IOException
	{
		if (_sharedIfcsUrl != null)
		{
			try
			{
				SharedIFCsDocument sharedIFCs = SharedIFCsDocument.Factory.parse(_sharedIfcsUrl);
				_sharedIFCs = new ConcurrentHashMap<Integer, InitialFilterCriteria>();
				TSharedIFC[] sifcs = sharedIFCs.getSharedIFCs().getSharedIFCArray();
				for (int i = 0; i < sifcs.length; i++)
				{
					TSharedIFC sifc = sifcs[i];
					int id = sifc.getID();
					TInitialFilterCriteria tifc = sifc.getInitialFilterCriteria();
					InitialFilterCriteria ifc = createIFC(tifc);
					__log.info("Added Shared IFC: " + ifc);
					_sharedIFCs.put(new Integer(id), ifc);
				}
			}
			catch (IOException e)
			{
				__log.warn("Failed to get shared IFCs from URL: " 
						+ _sharedIfcsUrl + ": " + e);
				__log.trace("Failed to get shared IFCs from URL: " + _sharedIfcsUrl, e);
			}
		}
	}

}
