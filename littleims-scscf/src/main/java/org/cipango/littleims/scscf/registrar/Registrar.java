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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

import org.cipango.diameter.AVP;
import org.cipango.diameter.AVPList;
import org.cipango.diameter.api.DiameterServletAnswer;
import org.cipango.diameter.api.DiameterServletRequest;
import org.cipango.diameter.base.Common;
import org.cipango.diameter.ims.Cx;
import org.cipango.diameter.ims.Cx.ReasonCode;
import org.cipango.diameter.ims.Cx.ServerAssignmentType;
import org.cipango.diameter.ims.Cx.UserDataAlreadyAvailable;
import org.cipango.diameter.ims.Sh;
import org.cipango.littleims.cx.data.userprofile.IMSSubscriptionDocument;
import org.cipango.littleims.cx.data.userprofile.TPublicIdentity;
import org.cipango.littleims.cx.data.userprofile.TServiceProfile;
import org.cipango.littleims.scscf.charging.CDF;
import org.cipango.littleims.scscf.cx.CxManager;
import org.cipango.littleims.scscf.data.InitialFilterCriteria;
import org.cipango.littleims.scscf.data.UserProfile;
import org.cipango.littleims.scscf.data.UserProfileCache;
import org.cipango.littleims.scscf.registrar.Context.ContactEvent;
import org.cipango.littleims.scscf.registrar.Context.RegState;
import org.cipango.littleims.scscf.registrar.regevent.RegEvent;
import org.cipango.littleims.scscf.registrar.regevent.RegEventListener;
import org.cipango.littleims.scscf.registrar.regevent.RegInfo;
import org.cipango.littleims.scscf.util.MessageSender;
import org.cipango.littleims.util.Headers;
import org.cipango.littleims.util.LittleimsException;
import org.cipango.littleims.util.Methods;
import org.cipango.littleims.util.URIHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Registrar
{
	public static final int DEFAULT_EXPIRES = 3600;
	private static final String ORIG_PARAM = "orig";
	private static final String MSG_SIP_CONTENT_TYPE = "message/sip";
	private static final String SERVICE_INFO_TYPE = "application/3gpp-ims+xml";
	
	private static final Logger __log = LoggerFactory.getLogger(Registrar.class);
	
	private Map<String, Context> _regContexts = new HashMap<String, Context>();
	private Timer _timer;

	private UserProfileCache _userProfileCache;
	private CDF _cdf;
	private int _maxUsers;
	private SipFactory _sipFactory;

	private Address _serviceRoute;
	private SipURI _scscfUri;
	
	private int _minExpires;
	private int _maxExpires;
	
	private RegEventListener _regEventListener;
	private CxManager _cxManager;
	
	private boolean _permanentAssignation;
	private MessageSender _messageSender;
	private int _reauthicationExpires = 600;
	
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
			return _regContexts.get(uri.toString());
		}
	}
	
	public Context getContext(String uri)
	{
		synchronized (_regContexts)
		{
			return _regContexts.get(uri);
		}
	}

	private URI getAor(SipServletRequest request)
	{
		return URIHelper.getCanonicalForm(_sipFactory, request.getTo().getURI());
	}
	
	public void doRegister(SipServletRequest request, String privateUserIdentity) throws ServletException, IOException
	{
		// 24.229 1. Identify the user
		// Since the Private User Identity may not be present,
		// we use the Public User Identity
		URI aor = getAor(request);

		__log.debug("Received REGISTER request for " + aor);

		// check that max registered users is not reached
		if (_maxUsers > 0 && (getNbContexts() >= _maxUsers))
		{
			__log.warn("Max registered users has been reached. Sending 503 response");
			_messageSender.sendResponse(request, SipServletResponse.SC_SERVICE_UNAVAILABLE);
			return;
		}

	
		Address contact = request.getAddressHeader(Headers.CONTACT);

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
		
		ServerAssignmentType serverAssignmentType;
		UserDataAlreadyAvailable userDataAlreadyAvailable =  UserDataAlreadyAvailable.USER_DATA_NOT_AVAILABLE;
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
			userDataAlreadyAvailable = UserDataAlreadyAvailable.USER_DATA_ALREADY_AVAILABLE;
		}

		// check that expires is shorter than minimum value
		if (expires != 0 && _minExpires != -1 && expires < _minExpires)
		{
			__log.info("Registration expiration (" + expires + ") is shorter"
					+ " than minimum value (" + _minExpires + "). Sending 423 response");
			_messageSender.sendResponse(request, SipServletResponse.SC_INTERVAL_TOO_BRIEF,
							Headers.MIN_EXPIRES, String.valueOf(_minExpires));
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
			_messageSender.sendResponse(request, SipServletResponse.SC_BAD_REQUEST);
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
	public void handleSaa(DiameterServletAnswer saa)
	{
		SipServletRequest request = (SipServletRequest) saa.getRequest().getAttribute(SipServletRequest.class.getName());
		if (request == null)
		{
			String publicIdentity = saa.getRequest().get(Cx.PUBLIC_IDENTITY);
			__log.debug("Received SAA answer for timeout registration of " + publicIdentity);
			return;
		}
		try
		{
			if (!saa.getResultCode().isSuccess())
			{
				__log.debug("Diameter SAA answer is not valid: " + saa.getResultCode() + ". Sending 403 response");
				_messageSender.sendResponse(request, SipServletResponse.SC_FORBIDDEN);
				return;
			}
			
			String privateUserIdentity = saa.getRequest().get(Common.USER_NAME);

			URI aor = getAor(request);
			RegistrationInfo regInfo;	
	
			Address contact = request.getAddressHeader(Headers.CONTACT);
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
				byte[] userData = saa.get(Sh.USER_DATA);
				IMSSubscriptionDocument subscription = null;
				if (userData != null)
				{
					subscription = IMSSubscriptionDocument.Factory.parse(new String(userData));
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
					response.addAddressHeader(Headers.CONTACT,
							(Address) contacts.get(i), false);
				}
			}
			else
				response.setExpires(0);
	
			Iterator<Address> it = regInfo.getAssociatedURIs().iterator();
			while (it.hasNext())
				response.addAddressHeader(Headers.P_ASSOCIATED_URI, it.next(), false);
	
			response.addAddressHeader(Headers.SERVICE_ROUTE, _serviceRoute, true);
			
			UserProfile profile = _userProfileCache.getProfile(aor.toString(), null);
			if (profile != null && profile.getServiceLevelTraceInfo() != null)
				response.setHeader(Headers.P_DEBUG_ID, "");
			//TODO check also associated URI for serviceLevelTraceInfo
			
			if (_messageSender.getUserAgent() != null)
				response.setHeader(Headers.SERVER, _messageSender.getUserAgent());
			
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
				_messageSender.sendResponse(request, e.getStatusCode());
		}
		catch (Exception e)
		{
			__log.warn(e.getMessage(), e);
			if (!request.isCommitted())
				_messageSender.sendResponse(request, SipServletResponse.SC_SERVER_INTERNAL_ERROR);
		}
		finally
		{
			if (request.getApplicationSession().isValid())
				request.getApplicationSession().invalidate();
		}
	}

	public RegistrationInfo register(URI to, Address contact,
			String privateUserIdentity, int expires, SipURI path, IMSSubscriptionDocument subscription) throws ServletParseException
	{
		List<Address> associatedURIs = null;
		synchronized (_regContexts)
		{
			Context regContext = _regContexts.get(to.toString());
			if (regContext != null)
			{
				associatedURIs = regContext.getAssociatedUris();
			}
		}

		String aor = to.toString();
		
		__log.debug("Updating binding: " + aor + " / " + contact + " / " + privateUserIdentity + " / "
				+ expires);
				
		if (associatedURIs == null)
		{
			associatedURIs = new ArrayList<Address>();
			TServiceProfile[] profiles = subscription.getIMSSubscription().getServiceProfileArray();
			_userProfileCache.cacheUserProfile(subscription);
			
			for (TServiceProfile profile: profiles)
			{
				TPublicIdentity[] identities = profile.getPublicIdentityArray();
				for (TPublicIdentity identity : identities)
				{	
					if (!identity.getBarringIndication())
					{
						Address addr = _sipFactory.createAddress(identity.getIdentity());
						if (identity.getExtension() != null && identity.getExtension().getExtension() != null)
							addr.setDisplayName(identity.getExtension().getExtension().getDisplayName());
						associatedURIs.add(addr);
					}
				}
			}
		}

		
		RegEvent regEvent = new RegEvent();

		synchronized (_regContexts)
		{

			// create all registration contexts if they are null
			Iterator<Address> it = associatedURIs.iterator();
			while (it.hasNext())
			{
				URI publicID = it.next().getURI();
				boolean explicit = publicID.equals(to);
				Context regContext = (Context) _regContexts.get(publicID.toString());
				if (regContext == null)
				{
					regContext = new Context(publicID.toString());
					regContext.setAssociatedUris(associatedURIs);
					_regContexts.put(publicID.toString(), regContext);
					__log.info("User " + publicID + " has been registered");
				}
				RegInfo regInfo = regContext.updateBinding(contact, privateUserIdentity, expires,
						explicit, path);
				if (regInfo != null)
				{
					regEvent.addRegInfo(regInfo);
				}
			}
			regEvent.setTerminated(false);
			notifyListeners(regEvent);

			RegistrationInfo info = new RegistrationInfo();
			info.setAssociatedUris(associatedURIs);

			Context explicit = (Context) _regContexts.get(to.toString());
			// start reg timer
			RegTimerTask regTimer = new RegTimerTask(to, privateUserIdentity);
			_timer.schedule(regTimer, expires * 1000);
			explicit.setRegTimer(privateUserIdentity, regTimer);

			info.setContacts(explicit.getContacts());

			return info;
		}
	}
	
	public void requestReauthentication(URI aor, String privateIdentity)
	{
		__log.debug("Network-initiated reauthentication for private identity: " + privateIdentity);
		List<Address> associatedURIs = null;
		synchronized (_regContexts)
		{
			Context regContext = _regContexts.get(aor.toString());
			if (regContext == null)
			{
				__log.warn("Network-initiated reauthentication failed: no context for " + aor);
				return;
			}
			associatedURIs = regContext.getAssociatedUris();
			
			RegEvent regEvent = new RegEvent();
			Iterator<Address> it = associatedURIs.iterator();
			while (it.hasNext())
			{
				String publicID = it.next().getURI().toString();
				Context context = _regContexts.get(publicID);
				RegInfo regInfo = context.requestReauthentication(privateIdentity, _reauthicationExpires);
				if (regInfo != null)
					regEvent.addRegInfo(regInfo);
				
			}
			regEvent.setTerminated(false);
			notifyListeners(regEvent);
			
			// start reg timer
			RegTimerTask regTimer = new RegTimerTask(aor, privateIdentity);
			_timer.schedule(regTimer, _reauthicationExpires * 1000);
			regContext.setRegTimer(privateIdentity, regTimer);
		}
		
		
	}
	
	public RegEvent getFullRegEvent(String aor)
	{
		synchronized (_regContexts)
		{
			RegEvent regEvent = new RegEvent();
			Context regContext = (Context) _regContexts.get(aor);
			if (regContext == null)
			{
				regEvent.addRegInfo(new RegInfo(aor, RegState.TERMINATED));
				regEvent.setTerminated(true);
			}
			else
			{
				Iterator<Address> it = regContext.getAssociatedUris().iterator();
				while (it.hasNext())
				{
					Context context = _regContexts.get(it.next().getURI().toString());
					if (context != null)
						regEvent.addRegInfo(context.getRegInfo());
				}
				regEvent.setTerminated(false);
			}
			return regEvent;
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
				info.setAssociatedUris(new ArrayList<Address>());
				return info;
			}
			List<Address> associatedURIs = regContext.getAssociatedUris();
			Iterator<Address> it = associatedURIs.iterator();
			boolean allTerminating = true;
			while (it.hasNext())
			{
				String publicID = it.next().getURI().toString();
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
					allTerminating = allTerminating && regContext.getState() == RegState.TERMINATED;
				}
			}
			regEvent.setTerminated(allTerminating);
			notifyListeners(regEvent);

			RegistrationInfo info = new RegistrationInfo();
			info.setAssociatedUris(associatedURIs);
			info.setContacts(regContext.getContacts());
			
			return info;
		}
	}

	protected void regTimerExpired(URI uri, String privateID)
	{
		unregister(uri, privateID, ContactEvent.EXPIRED);
		__log.debug("Registration expired due to timeout for URI " + uri);
		ServerAssignmentType serverAssignmentType;
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
					UserDataAlreadyAvailable.USER_DATA_ALREADY_AVAILABLE, 
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
		Iterator<Address> it = regInfo.getAssociatedURIs().iterator();
		while (it.hasNext())
		{
			URI uri = it.next().getURI();
			UserProfile profile = _userProfileCache.getProfile(uri.toString(), null);

			Iterator<InitialFilterCriteria> ifcs = profile.getServiceProfile().getIFCsIterator();

			while (ifcs.hasNext())
			{
				URI fromUri = _scscfUri;
				InitialFilterCriteria ifc = (InitialFilterCriteria) ifcs.next();
				__log.debug("Evaluating ifc: " + ifc);
				if (ifc.matches(request,
						InitialFilterCriteria.SessionCase.ORIGINATING_SESSION))
				{
					__log.debug("IFC matches for URI: " + uri + ". Sending Third-Party REGISTER");
					SipServletRequest register = _sipFactory.createRequest(request
							.getApplicationSession(), Methods.REGISTER, fromUri, uri);
					register.setExpires(expires);
					register.setRequestURI(_sipFactory.createURI(ifc.getAs().getURI()));
					
					String pAccessNetworkInfo = request.getHeader(Headers.P_ACCESS_NETWORK_INFO);
					if (pAccessNetworkInfo != null)
						register.setHeader(Headers.P_ACCESS_NETWORK_INFO, pAccessNetworkInfo);
					
					String pChargingVector = request.getHeader(Headers.P_CHARGING_VECTOR);
					if (pChargingVector != null)
						register.setHeader(Headers.P_CHARGING_VECTOR, pChargingVector);
					
					String serviceInfo = ifc.getAs().getServiceInfo();
					if (serviceInfo != null && !serviceInfo.trim().equals(""))
					{
						register.setContent(
								generateXML(serviceInfo).getBytes(),
								SERVICE_INFO_TYPE);
					}
					if (ifc.getAs().getIncludeRegisterRequest())
						register.setContent(request.toString().getBytes(), MSG_SIP_CONTENT_TYPE);
					if (ifc.getAs().getIncludeRegisterResponse())
						register.setContent(response.toString().getBytes(), MSG_SIP_CONTENT_TYPE);
					
					// TODO support multipart
					register.setAddressHeader(Headers.CONTACT, _sipFactory.createAddress(_scscfUri));
					register.send();
				}
			}
		}
	}
	
	private void sendThirdPartyRegister(String aor,RegistrationInfo regInfo)
	{
		try
		{
			// send third-party registration to relevant application servers
			// @see 24.229 5.4.1.7
			SipServletRequest request = _sipFactory.createRequest(
					_sipFactory.createApplicationSession(),
					Methods.REGISTER,
					aor,
					aor);
			request.setExpires(0);
			
			Iterator<Address> it = regInfo.getAssociatedURIs().iterator();
			while (it.hasNext())
			{
				URI uri = it.next().getURI();
				UserProfile profile = _userProfileCache.getProfile(uri.toString(), null);

				Iterator<InitialFilterCriteria> ifcs = profile.getServiceProfile().getIFCsIterator();

				while (ifcs.hasNext())
				{
					URI fromUri = _scscfUri;
					InitialFilterCriteria ifc = (InitialFilterCriteria) ifcs.next();
					__log.debug("Evaluating ifc: " + ifc);
					if (ifc.matches(request,
							InitialFilterCriteria.SessionCase.ORIGINATING_SESSION))
					{
						__log.debug("IFC matches for URI: " + uri + ". Sending Third-Party REGISTER");
						SipServletRequest register = _sipFactory.createRequest(request
								.getApplicationSession(), Methods.REGISTER, fromUri, uri);
						register.setExpires(0);
						register.setRequestURI(_sipFactory.createURI(ifc.getAs().getURI()));
						
						String pAccessNetworkInfo = request.getHeader(Headers.P_ACCESS_NETWORK_INFO);
						if (pAccessNetworkInfo != null)
							register.setHeader(Headers.P_ACCESS_NETWORK_INFO, pAccessNetworkInfo);
						
						String pChargingVector = request.getHeader(Headers.P_CHARGING_VECTOR);
						if (pChargingVector != null)
							register.setHeader(Headers.P_CHARGING_VECTOR, pChargingVector);
						
						String serviceInfo = ifc.getAs().getServiceInfo();
						if (serviceInfo != null && !serviceInfo.trim().equals(""))
						{
							register.setContent(
									generateXML(serviceInfo).getBytes(),
									SERVICE_INFO_TYPE);
						}

						// TODO support multipart
						register.setAddressHeader(Headers.CONTACT, _sipFactory.createAddress(_scscfUri));
						register.send();
					}
				}
			}
		}
		catch (Throwable e)
		{
			__log.warn("Failed to send third party REGISTER", e);
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
	
	
	public void handleRtr(DiameterServletRequest rtr) throws IOException, ServletException
	{
		// Deregister + send NOTIFY + send third party register
		Iterator<AVP<String>> it = rtr.getAVPs().getAVPs(Cx.PUBLIC_IDENTITY);
		
		ContactEvent contactEvent;
		ReasonCode deregistrationReason = getDeregistrationReason(rtr);
		switch (deregistrationReason)
		{
		case NEW_SERVER_ASSIGNED:
		case REMOVE_SCSCF:
		case SERVER_CHANGE:
			contactEvent = ContactEvent.DEACTIVATED;
			break;
		case PERMANENT_TERMINATION:
			contactEvent = ContactEvent.REJECTED;
			break;
		default:
			DiameterServletAnswer answer = rtr.createAnswer(Common.DIAMETER_MISSING_AVP);
			AVPList l = new AVPList();
			l.add(Cx.DERISTRATION_REASON, new AVPList());
			answer.getAVPs().add(new AVP<AVPList>(Common.FAILED_AVP, l));
			answer.send();
			return;
		}
		
		if (it.hasNext())
		{
			while (it.hasNext())
			{
				String publicId = it.next().getValue();
				Context regContext = _regContexts.get(publicId); // FIXME case wilcard 
				
				if (regContext == null)
				{
					__log.warn("Unable deregister identity " + publicId + " after RTR: no context found");
					continue;
				}
				
				RegistrationInfo info = new RegistrationInfo();
				info.setAssociatedUris(regContext.getAssociatedUris());
				sendThirdPartyRegister(publicId, info);
				
				RegEvent regEvent = new RegEvent();
				Iterator<Address> it2 = regContext.getAssociatedUris().iterator();
				while (it2.hasNext())
				{
					String associatedIdentity = it2.next().getURI().toString();
					Context context = _regContexts.get(associatedIdentity);

					if (context != null)
					{
						// Remove all bindings
						RegInfo regInfo = context.removeAllBindings(contactEvent);
						regEvent.addRegInfo(regInfo);
						__log.info("User " + associatedIdentity + " has been network deregistered");
						_regContexts.remove(associatedIdentity);
						_userProfileCache.clearUserProfile(associatedIdentity);
						
					}
				}
				regEvent.setTerminated(true);
				notifyListeners(regEvent);
			}
		}
		else
		{
			List<String> privateIds = getPrivateIdentities(rtr);
			if (privateIds == null)
				return;
			
			Set<String> contextsToRemove = new HashSet<String>();
			Iterator<Context> it2 = _regContexts.values().iterator();
			while (it2.hasNext())
			{
				Context context = it2.next();
				if (context.hasPrivateIdentityRegistered(privateIds))
				{
					String publicId = context.getPublicIdentity();
					RegEvent regEvent = new RegEvent();
						
					List<Address> associatedURIs = context.getAssociatedUris();
					Iterator<Address> it3 = associatedURIs.iterator();
					while (it3.hasNext())
					{
						String publicID = it3.next().getURI().toString();
						context = (Context) _regContexts.get(publicID);
						if (context != null)
						{
							// should never be null unlike wrong HSS configuration
							for (String privateId : privateIds)
							{
								RegInfo regInfo = context.removeBinding(privateId, contactEvent);
								if (regInfo != null)
									regEvent.addRegInfo(regInfo);
							}
							
							if (context.getState() == RegState.TERMINATED)
								contextsToRemove.add(publicID);
						}
						else
							__log.warn("Unable deregister identity " + publicId + " after RTR: no context found");
					}
					notifyListeners(regEvent);

					RegistrationInfo info = new RegistrationInfo();
					info.setAssociatedUris(associatedURIs);
					info.setContacts(context.getContacts());
											
					sendThirdPartyRegister(publicId, info);
					
					if (info.getContacts().isEmpty())
					{
						
					}
				}
			}
			for (String publicId : contextsToRemove)
			{
				__log.info("User " + publicId + " has been network deregistered");
				_userProfileCache.clearUserProfile(publicId);
				_regContexts.remove(publicId);
			}
		}
		DiameterServletAnswer answer = rtr.createAnswer(Common.DIAMETER_SUCCESS);
		answer.send();
	}
	
	private List<String> getPrivateIdentities(DiameterServletRequest rtr) throws IOException
	{
		String privateIdentity = rtr.get(Common.USER_NAME);
		if (privateIdentity == null)
		{
			DiameterServletAnswer answer = rtr.createAnswer(Common.DIAMETER_MISSING_AVP);
			AVPList l = new AVPList();
			l.add(Common.USER_NAME, "");
			answer.add(Common.FAILED_AVP, l);
			answer.send();
			return null;
		}

		List<String> privateIds = new ArrayList<String>();
		privateIds.add(privateIdentity);
		AVPList associatedIdentites = rtr.get(Cx.ASSOCIATED_IDENTITIES);
		if (associatedIdentites != null)
		{
			Iterator<AVP<String>> it = associatedIdentites.getAVPs(Common.USER_NAME);
			while (it.hasNext())
				privateIds.add(it.next().toString());
		}
		return privateIds;
	}
	
	private ReasonCode getDeregistrationReason(DiameterServletRequest rtr)
	{
		AVPList avpList = rtr.get(Cx.DERISTRATION_REASON);
		if (avpList == null)
		{
			__log.warn("Missing required AVP: Deregistration reason " + Cx.DERISTRATION_REASON);
			return null;
		}	
		return avpList.getValue(Cx.REASON_CODE);
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

	public List<Context> getRegContexts()
	{
		synchronized (_regContexts)
		{
			return new ArrayList<Context>(_regContexts.values());
		}
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
			try
			{
				regTimerExpired(_uri, _privateID);
			}
			catch (Throwable e)
			{
				__log.warn("Unexpected exception in RegTimerTask", e);
			}
		}

	}

	public MessageSender getMessageSender()
	{
		return _messageSender;
	}

	public void setMessageSender(MessageSender messageSender)
	{
		_messageSender = messageSender;
	}

	public int getReauthicationExpires()
	{
		return _reauthicationExpires;
	}

	public void setReauthicationExpires(int reauthicationExpires)
	{
		_reauthicationExpires = reauthicationExpires;
	}



}
