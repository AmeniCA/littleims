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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import javax.servlet.ServletException;
import javax.servlet.sip.Address;
import javax.servlet.sip.ServletParseException;
import javax.servlet.sip.SipFactory;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipURI;
import javax.servlet.sip.URI;

import org.apache.log4j.Logger;
import org.cipango.diameter.AVP;
import org.cipango.diameter.DiameterAnswer;
import org.cipango.diameter.base.Base;
import org.cipango.diameter.ims.IMS;
import org.cipango.littleims.cx.ServerAssignmentType;
import org.cipango.littleims.cx.data.userprofile.IMSSubscriptionDocument;
import org.cipango.littleims.cx.data.userprofile.TPublicIdentity;
import org.cipango.littleims.cx.data.userprofile.TServiceProfile;
import org.cipango.littleims.scscf.charging.CDF;
import org.cipango.littleims.scscf.cx.CxManager;
import org.cipango.littleims.scscf.data.InitialFilterCriteria;
import org.cipango.littleims.scscf.data.UserProfile;
import org.cipango.littleims.scscf.data.UserProfileCache;
import org.cipango.littleims.scscf.registrar.regevent.RegEvent;
import org.cipango.littleims.scscf.registrar.regevent.RegEventListener;
import org.cipango.littleims.scscf.registrar.regevent.RegInfo;
import org.cipango.littleims.scscf.registrar.regevent.RegState;
import org.cipango.littleims.util.Headers;
import org.cipango.littleims.util.LittleimsException;
import org.cipango.littleims.util.Methods;
import org.cipango.littleims.util.URIHelper;


public class Registrar
{
	public static final int DEFAULT_EXPIRES = 3600;
	private static final String ORIG_PARAM = "orig";
	private static final String MSG_SIP_CONTENT_TYPE = "message/sip";
	private static final String SERVICE_INFO_TYPE = "application/3gpp-ims+xml";
	
	private static final Logger __log = Logger.getLogger(Registrar.class);
	
	private Map<String, Context> _regContexts = new HashMap<String, Context>();
	private Timer _timer;

	private UserProfileCache _userProfileCache;
	private CDF _cdf;
	private List<String> _realms;
	private int _maxUsers;
	private SipFactory _sipFactory;

	private Address _serviceRoute;
	private SipURI _scscfUri;
	
	private int _minExpires;
	private int _maxExpires;
	
	private RegEventListener _regEventListener;
	private CxManager _cxManager;
	
	private boolean _permanentAssignation;
	
	public Registrar()
	{
		_timer = new Timer();
	}
	
	public void init()
	{
		SipURI uri = (SipURI) _scscfUri.clone();
		uri.setLrParam(true);
		uri.setParameter(ORIG_PARAM, "");

		_serviceRoute = _sipFactory.createAddress(uri);
	}

	public int getNbContexts()
	{
		synchronized (_regContexts)
		{
			return _regContexts.size();
		}
	}

	public Context getContext(URI uri)
	{
		synchronized (_regContexts)
		{
			return (Context) _regContexts.get(uri.toString());
		}
	}

	private SipURI getAor(SipServletRequest request)
	{
		SipURI to = (SipURI) request.getTo().getURI();
		return URIHelper.getCanonicalForm(_sipFactory, to);
	}
	
	public void doRegister(SipServletRequest request, String privateUserIdentity) throws ServletException, IOException
	{
		// 24.229 1. Identify the user
		// Since the Private User Identity may not be present,
		// we use the Public User Identity
		SipURI aor = getAor(request);

		__log.debug("Received REGISTER request for " + aor);

		// check that we are configured to handle the user's domain
		String realm = aor.getHost();
		if (!_realms.contains(realm))
		{
			__log.warn("Realm " + realm + " is not authorized to register");
			request.createResponse(SipServletResponse.SC_FORBIDDEN).send();
			request.getApplicationSession().invalidate();
			return;
		}

		// check that max registered users is not reached
		if (_maxUsers > 0 && (getNbContexts() >= _maxUsers))
		{
			__log.warn("Max registered users has been reached. Sending 503 response");
			request.createResponse(SipServletResponse.SC_SERVICE_UNAVAILABLE).send();
			request.getApplicationSession().invalidate();
			return;
		}

	
		Address contact = request.getAddressHeader(Headers.CONTACT_HEADER);

		int expires = contact.getExpires();
		if (expires == -1)
		{
			expires = request.getExpires();
			if (expires == -1)
			{
				// no expires values specified, use default value
				expires = DEFAULT_EXPIRES;
			}
		}
		
		int serverAssignmentType;
		boolean userDataAlreadyAvailable = false;
		if (expires == 0)
		{
			if (_permanentAssignation)
				serverAssignmentType = ServerAssignmentType.USER_DEREGISTRATION_STORE_SERVER_NAME;
			else
				serverAssignmentType = ServerAssignmentType.USER_DEREGISTRATION;
		}
		else if ( _regContexts.get(aor.toString()) == null)
			serverAssignmentType = ServerAssignmentType.REGISTRATION; 
		else
		{
			serverAssignmentType = ServerAssignmentType.RE_REGISTRATION;
			userDataAlreadyAvailable = true;
		}

		// check that expires is shorter than minimum value
		if (expires != 0 && _minExpires != -1 && expires < _minExpires)
		{
			__log.info("Registration expiration (" + expires + ") is shorter"
					+ " than minimum value (" + _minExpires + "). Sending 423 response");
			SipServletResponse response = request
					.createResponse(SipServletResponse.SC_INTERVAL_TOO_BRIEF);
			response.setHeader(Headers.MIN_EXPIRES_HEADER, String.valueOf(_minExpires));
			response.send();
			request.getApplicationSession().invalidate();
			return;
		}

		// if expires is longer than maximum value, reduce it
		if (_maxExpires != -1 && expires > _maxExpires)
		{
			__log.info("Registration expiration (" + expires + ") is greater"
					+ " than maximum value (" + _maxExpires
					+ "). Setting expires to max expires");
			expires = _maxExpires;
		}

		if (contact == null)
		{
			request.createResponse(SipServletResponse.SC_BAD_REQUEST).send();
			request.getApplicationSession().invalidate();
			return;
		}
		

		_cxManager.sendSAR(aor.toString(), 
				privateUserIdentity, 
				request.getHeader(Headers.P_PROFILE_KEY), 
				serverAssignmentType, 
				userDataAlreadyAvailable, 
				request);
	}

	/**
	 * <pre>
	 * <Server-Assignment-Answer> ::=	< Diameter Header: 301, PXY, 16777216 >
	 *     < Session-Id >
	 *     { Vendor-Specific-Application-Id }
	 *     [ Result-Code ]
	 *     [ Experimental-Result ]
	 *     { Auth-Session-State }
	 *     { Origin-Host }
	 *     { Origin-Realm }
	 *     [ User-Name ]
	 *    *[ Supported-Features ]
	 *     [ User-Data ]
	 *     [ Charging-Information ]
	 *     [ Associated-Identities ]
	 *     [ Loose-Route-Indication ]
	 *    *[ SCSCF-Restoration-Info ]
	 *     [ Associated-Registered-Identities ]
	 *     [ Server-Name ]
	 *    *[ AVP ]
	 *    *[ Failed-AVP ]
	 *    *[ Proxy-Info ]
	 *    *[ Route-Record ]
	 * </pre>
	 * @param saa
	 */
	public void handleSaa(DiameterAnswer saa)
	{
		SipServletRequest request = (SipServletRequest) saa.getRequest().getAttribute(SipServletRequest.class.getName());
		if (request == null)
		{
			String publicIdentity = saa.getRequest().getAVP(IMS.IMS_VENDOR_ID, IMS.PUBLIC_IDENTITY).getString();
			__log.debug("Received SAA answer for timeout registration of " + publicIdentity);
			return;
		}
		try
		{
			if (saa.getResultCode() >= 3000)
			{
				__log.debug("Diameter SAA answer is not valid: " + saa.getResultCode() + ". Sending 403 response");
				try
				{
					request.createResponse(SipServletResponse.SC_FORBIDDEN).send();
				}
				catch (IOException e)
				{
					__log.trace(e.getMessage(), e);
				}
				return;
			}
			
			String privateUserIdentity = saa.getRequest().getAVP(Base.USER_NAME).getString();

			SipURI aor = getAor(request);
			RegistrationInfo regInfo;	
	
			Address contact = request.getAddressHeader(Headers.CONTACT_HEADER);
			int expires = contact.getExpires();
			if (expires == -1)
			{
				expires = request.getExpires();
				if (expires == -1)
				{
					// no expires values specified, use default value
					expires = DEFAULT_EXPIRES;
				}
			}
			
			if (expires != 0)
			{
				AVP userData = saa.getAVP(IMS.IMS_VENDOR_ID, IMS.USER_DATA);
				IMSSubscriptionDocument subscription = null;
				if (userData != null)
				{
					subscription = IMSSubscriptionDocument.Factory.parse(userData.getString());
				}
							
				regInfo = register(aor, contact, privateUserIdentity, expires, getPath(request), subscription);
			}
			else
			{
				regInfo = unregister(aor, privateUserIdentity, ContactEvent.UNREGISTERED);
			}
			
			
			SipServletResponse response = request.createResponse(SipServletResponse.SC_OK);
	
			if (expires != 0)
			{
				List<Address> contacts = regInfo.getContacts();
				for (int i = 0; i < contacts.size(); i++)
				{
					Address c = contacts.get(i);
					c.setExpires(expires);
					response.addAddressHeader(Headers.CONTACT_HEADER,
							(Address) contacts.get(i), false);
				}
			}
	
			Iterator<String> it = regInfo.getAssociatedURIs().iterator();
			while (it.hasNext())
			{
				Address associatedURI = _sipFactory.createAddress(it.next());
				response.addAddressHeader(Headers.P_ASSOCIATED_URI_HEADER, associatedURI, false);
			}
	
			response.addAddressHeader(Headers.SERVICE_ROUTE_HEADER, _serviceRoute, true);
			
			UserProfile profile = _userProfileCache.getProfile(aor.toString(), null);
			if (profile != null && profile.getServiceLevelTraceInfo() != null)
				response.setHeader(Headers.P_DEBUG_ID, "");
			//TODO check also associated URI for serviceLevelTraceInfo
			
			response.send();
	
			if (_cdf.isEnabled())
				_cdf.event(request, CDF.ROLE_NODE_TERMINATING);
	
			sendThirdPartyRegister(request, response, expires, regInfo);
			
			if (expires == 0 && !_permanentAssignation 
					&& regInfo.getContacts() != null && regInfo.getContacts().isEmpty())
				_userProfileCache.clearUserProfile(aor.toString());
			
		}
		catch (LittleimsException e) {
			__log.warn(e.getMessage(), e);
			if (!request.isCommitted())
				try { request.createResponse(e.getStatusCode()).send(); } catch (IOException e2) { }
		}
		catch (Exception e)
		{
			__log.warn(e.getMessage(), e);
			if (!request.isCommitted())
				try { request.createResponse(SipServletResponse.SC_SERVER_INTERNAL_ERROR).send();} catch (IOException e2) { }
		}
		finally
		{
			if (request.getApplicationSession().isValid())
				request.getApplicationSession().invalidate();
		}
	}

	public RegistrationInfo register(SipURI to, Address contact,
			String privateUserIdentity, int expires, SipURI path, IMSSubscriptionDocument subscription)
	{
		List<String> associatedURIs = null;
		synchronized (_regContexts)
		{
			Context regContext = _regContexts.get(to.toString());
			if (regContext != null)
			{
				associatedURIs = regContext.getAssociatedURIs();
			}
		}

		String aor = to.toString();
		
		__log.debug("Updating binding: " + aor + " / " + contact + " / " + privateUserIdentity + " / "
				+ expires);
				
		if (associatedURIs == null)
		{
			associatedURIs = new ArrayList<String>();
			TServiceProfile[] profiles = subscription.getIMSSubscription().getServiceProfileArray();
			_userProfileCache.cacheUserProfile(subscription);
			
			for (TServiceProfile profile: profiles)
			{
				TPublicIdentity[] identities = profile.getPublicIdentityArray();
				for (TPublicIdentity identity : identities)
				{	
					if (!identity.getBarringIndication())
					{
						associatedURIs.add(identity.getIdentity());
					}
				}
			}
		}

		
		RegEvent regEvent = new RegEvent();

		synchronized (_regContexts)
		{

			// create all registration contexts if they are null
			Iterator<String> it = associatedURIs.iterator();
			while (it.hasNext())
			{
				String publicID = (String) it.next();
				boolean explicit = publicID.equals(aor);
				Context regContext = (Context) _regContexts.get(publicID);
				if (regContext == null)
				{
					regContext = new Context(publicID);
					regContext.setAssociatedURIs(associatedURIs);
					_regContexts.put(publicID, regContext);
					__log.info("User " + publicID + " has been registered");
				}
				RegInfo regInfo = regContext.updateBinding(contact, privateUserIdentity, expires,
						explicit, path);
				if (regInfo != null)
				{
					regEvent.addRegInfo(regInfo);
				}
			}
			notifyListeners(regEvent);

			RegistrationInfo info = new RegistrationInfo();
			info.setAssociatedURIs(associatedURIs);

			Context explicit = (Context) _regContexts.get(to.toString());
			// start reg timer
			RegTimerTask regTimer = new RegTimerTask(to, privateUserIdentity);
			_timer.schedule(regTimer, expires * 1000);
			explicit.setRegTimer(privateUserIdentity, regTimer);

			info.setContacts(explicit.getContacts());

			return info;
		}
	}
	
	public RegInfo getBindings(String aor)
	{
		synchronized (_regContexts)
		{
			if (!_regContexts.containsKey(aor))
			{
				return new RegInfo(aor, RegState.INIT);
			}
			else
			{
				Context regContext = (Context) _regContexts.get(aor);
				return regContext.getRegInfo();
			}
		}
	}

	public RegistrationInfo unregister(URI to, String privateUserIdentity,
			ContactEvent event)
	{

		RegEvent regEvent = new RegEvent();

		synchronized (_regContexts)
		{

			Context regContext = (Context) _regContexts.get(to.toString());
			if (regContext == null)
			{
				RegistrationInfo info = new RegistrationInfo();
				info.setAssociatedURIs(new ArrayList<String>());
				return info;
			}
			List<String> associatedURIs = regContext.getAssociatedURIs();
			Iterator<String> it = associatedURIs.iterator();
			while (it.hasNext())
			{
				String publicID = (String) it.next();
				regContext = (Context) _regContexts.get(publicID);
				if (regContext != null)
				{
					// should never be null unlike wrong HSS configuration

					RegInfo regInfo = regContext.removeBinding(privateUserIdentity, event);
					if (regInfo != null)
					{
						regEvent.addRegInfo(regInfo);
					}
					if (regContext.getState() == RegState.TERMINATED)
					{
						__log.info("User " + publicID + " has been deregistered");
						_regContexts.remove(publicID);
					}
				}
			}
			notifyListeners(regEvent);

			RegistrationInfo info = new RegistrationInfo();
			info.setAssociatedURIs(associatedURIs);
			info.setContacts(regContext.getContacts());
			
			return info;
		}
	}

	protected void regTimerExpired(URI uri, String privateID)
	{
		unregister(uri, privateID, ContactEvent.EXPIRED);
		__log.debug("Registration expired due to timeout for URI " + uri);
		int serverAssignmentType;
		if (_permanentAssignation)
			serverAssignmentType = ServerAssignmentType.TIMEOUT_DEREGISTRATION_STORE_SERVER_NAME;
		else
			serverAssignmentType = ServerAssignmentType.TIMEOUT_DEREGISTRATION;
		
		try
		{
			_cxManager.sendSAR(
					uri.toString(), 
					privateID, 
					null, 
					serverAssignmentType, 
					true, 
					null);
		} 
		catch (IOException e)
		{
			__log.debug("Failed to notify HSS about timeout registration for " + uri, e);
		}
	}

	public void setListener(RegEventListener listener)
	{
		_regEventListener = listener;
	}

	private void notifyListeners(RegEvent e)
	{
		if (_regEventListener != null)
		{
			_regEventListener.registrationEvent(e);
		}
	}
	
	private SipURI getPath(SipServletRequest request) throws LittleimsException
	{
		try
		{
			Address pathAddress = request.getAddressHeader(Headers.PATH);
			if (pathAddress == null)
				return null;
			return (SipURI) pathAddress.getURI();
		}
		catch (Exception e)
		{
			throw new LittleimsException("Invalid path header: " + request.getHeader(Headers.PATH), e, 
					SipServletResponse.SC_BAD_REQUEST);
		}
		
	}
	
	private void sendThirdPartyRegister(SipServletRequest request, SipServletResponse response, int expires, RegistrationInfo regInfo)
	throws ServletParseException, IOException
	{
		// send third-party registration to relevant application servers
		// @see 24.229 5.4.1.7
		Iterator<String> it = regInfo.getAssociatedURIs().iterator();
		while (it.hasNext())
		{
			String sURI = it.next();
			UserProfile profile = _userProfileCache.getProfile(sURI, null);

			Iterator<InitialFilterCriteria> ifcs = profile.getServiceProfile().getIFCsIterator();

			while (ifcs.hasNext())
			{
				URI toUri = _sipFactory.createURI(sURI);
				URI fromUri = _scscfUri;
				InitialFilterCriteria ifc = (InitialFilterCriteria) ifcs.next();
				__log.debug("Evaluating ifc: " + ifc);
				if (ifc.matches(request,
						InitialFilterCriteria.SessionCase.ORIGINATING_SESSION))
				{
					__log.debug("IFC matches for URI: " + sURI + ". Sending Third-Party REGISTER");
					SipServletRequest register = _sipFactory.createRequest(request
							.getApplicationSession(), Methods.REGISTER, fromUri, toUri);
					register.setExpires(expires);
					register.setRequestURI(_sipFactory.createURI(ifc.getAS().getURI()));
					
					String pAccessNetworkInfo = request.getHeader(Headers.P_ACCESS_NETWORK_INFO);
					if (pAccessNetworkInfo != null)
						register.setHeader(Headers.P_ACCESS_NETWORK_INFO, pAccessNetworkInfo);
					
					String pChargingVector = request.getHeader(Headers.P_CHARCHING_VECTOR);
					if (pChargingVector != null)
						register.setHeader(Headers.P_CHARCHING_VECTOR, pChargingVector);
					
					String serviceInfo = ifc.getAS().getServiceInfo();
					if (serviceInfo != null && !serviceInfo.trim().equals(""))
					{
						register.setContent(
								generateXML(serviceInfo).getBytes(),
								SERVICE_INFO_TYPE);
					}
					if (ifc.getAS().getIncludeRegisterRequest())
						register.setContent(request.toString().getBytes(), MSG_SIP_CONTENT_TYPE);
					if (ifc.getAS().getIncludeRegisterResponse())
						register.setContent(response.toString().getBytes(), MSG_SIP_CONTENT_TYPE);
					
					// TODO support multipart
					register.setAddressHeader(Headers.CONTACT_HEADER, _sipFactory.createAddress(_scscfUri));
					register.send();
				}
			}
		}
	}
	
	private String generateXML(String serviceInfo)
	{
		StringBuilder sb = new StringBuilder();
		sb.append("<?xml version=\"1.0\"?>\n");
		sb.append("<ims-3gpp>\n");
		sb.append("<service-info>").append(serviceInfo).append("</service-info>\n");
		sb.append("</ims-3gpp>\n");
		return sb.toString();
	}
	
	public UserProfileCache getUserProfileCache()
	{
		return _userProfileCache;
	}

	public void setUserProfileCache(UserProfileCache userProfileCache)
	{
		_userProfileCache = userProfileCache;
	}
	
	public CDF getCdf()
	{
		return _cdf;
	}

	public void setCdf(CDF cdf)
	{
		_cdf = cdf;
	}

	public List<String> getRealms()
	{
		return _realms;
	}

	public void setRealms(List<String> realms)
	{
		_realms = realms;
	}

	public int getMaxUsers()
	{
		return _maxUsers;
	}

	public void setMaxUsers(int maxUsers)
	{
		_maxUsers = maxUsers;
	}

	public SipFactory getSipFactory()
	{
		return _sipFactory;
	}

	public void setSipFactory(SipFactory sipFactory)
	{
		_sipFactory = sipFactory;
	}

	public SipURI getScscfUri()
	{
		return _scscfUri;
	}

	public void setScscfUri(SipURI scscfUri)
	{
		_scscfUri = scscfUri;
	}

	public int getMinExpires()
	{
		return _minExpires;
	}

	public void setMinExpires(int minExpires)
	{
		_minExpires = minExpires;
	}

	public int getMaxExpires()
	{
		return _maxExpires;
	}

	public void setMaxExpires(int maxExpires)
	{
		_maxExpires = maxExpires;
	}

	public Iterator<Context> getRegContextsIt()
	{
		return _regContexts.values().iterator();
	}


	public CxManager getCxManager()
	{
		return _cxManager;
	}

	public void setCxManager(CxManager cxManager)
	{
		_cxManager = cxManager;
	}

	public boolean isPermanentAssignation()
	{
		return _permanentAssignation;
	}

	public void setPermanentAssignation(boolean permanentAssignation)
	{
		_permanentAssignation = permanentAssignation;
	}

	class RegTimerTask extends TimerTask
	{

		private URI _uri;
		private String _privateID;
		
		public RegTimerTask(URI uri, String privateUserIdentity)
		{
			_uri = uri;
			_privateID = privateUserIdentity;
		}

		public void run()
		{
			regTimerExpired(_uri, _privateID);
		}

	}



}
