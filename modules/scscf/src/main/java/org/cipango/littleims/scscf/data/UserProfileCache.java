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


public class UserProfileCache
{

	private URL _sharedIfcsUrl;
	private Map<String, UserProfile> _serviceProfiles = new ConcurrentHashMap<String, UserProfile>();
	private TriggerPointCompiler _tpCompiler = new TriggerPointCompiler();
	private Map<Integer, InitialFilterCriteria> _sharedIFCs = new ConcurrentHashMap<Integer, InitialFilterCriteria>();

	private static final Logger __log = Logger.getLogger(UserProfileCache.class);


	public void init() throws XmlException, IOException
	{
		if (_sharedIfcsUrl != null)
		{
			SharedIFCsDocument sharedIFCs = SharedIFCsDocument.Factory.parse(_sharedIfcsUrl);
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
	}

	public void cacheUserProfile(IMSSubscriptionDocument imsSub)
	{
		try
		{
			// check all Service Profiles
			TServiceProfile[] profiles = imsSub.getIMSSubscription().getServiceProfileArray();
			for (int i = 0; i < profiles.length; i++)
			{
				// Build a single ServiceProfile
				// May contain several IFCs and be associated to several Public
				// Identities
				ServiceProfile serviceProfile = new ServiceProfile();

				TServiceProfile profile = (TServiceProfile) profiles[i];

				// Core Network Service Authorization
				TCoreNetworkServicesAuthorization cnsa = profile
						.getCoreNetworkServicesAuthorization();
				if (cnsa != null)
				{
					serviceProfile.setMediaProfile(cnsa.getSubscribedMediaProfileId());
				}

				// Initial Filter Criterias
				TInitialFilterCriteria[] iFCs = profile.getInitialFilterCriteriaArray();
				for (int k = 0; k < iFCs.length; k++)
				{
					TInitialFilterCriteria tIFC = (TInitialFilterCriteria) iFCs[k];
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
					for (int j = 0; j < sIFCs.length; j++)
					{
						Integer ifcID = new Integer(sIFCs[j]);
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
					}
				}

				// Public Identities that use this ServiceProfile
				TPublicIdentity[] publicIDs = profile.getPublicIdentityArray();
				for (int j = 0; j < publicIDs.length; j++)
				{
					TPublicIdentity publicID = publicIDs[j];
					UserProfile userProfile = new UserProfile(publicID.getIdentity());
					userProfile.setBarred(publicID.getBarringIndication());
					userProfile.setServiceProfile(serviceProfile);

					__log.debug("Cache user profile for identity " + publicID.getIdentity());
					_serviceProfiles.put(publicID.getIdentity(), userProfile);
				}
			}
		}
		catch (Exception e)
		{
			__log.warn("Failed to cache profile for user: " + imsSub.getIMSSubscription().getPrivateID(), e);
		}
	}

	public UserProfile getProfile(String publicID)
	{
		UserProfile userProfile = _serviceProfiles.get(publicID);
		
		if (userProfile != null)
		{
			return userProfile;
		}

		__log.debug("Could not found profile for public identity " + publicID);
		return  null;
	}
	
	private InitialFilterCriteria createIFC(TInitialFilterCriteria tifc)
	{
		CriteriaMatch trigger = _tpCompiler.compile(tifc.getTriggerPoint());
		AS as = new AS(tifc.getApplicationServer().getServerName(), tifc.getApplicationServer()
				.getDefaultHandling(), tifc.getApplicationServer().getServiceInfo());

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

}
