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
package org.cipango.littleims.scscf.session;

import java.io.IOException;
import java.util.Iterator;

import javax.servlet.ServletException;
import javax.servlet.sip.Address;
import javax.servlet.sip.ServletParseException;
import javax.servlet.sip.SipFactory;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipSession;
import javax.servlet.sip.SipURI;
import javax.servlet.sip.URI;

import org.apache.log4j.Logger;
import org.cipango.diameter.AVP;
import org.cipango.diameter.DiameterAnswer;
import org.cipango.diameter.DiameterRequest;
import org.cipango.diameter.base.Base;
import org.cipango.diameter.ims.IMS;
import org.cipango.littleims.cx.ServerAssignmentType;
import org.cipango.littleims.cx.data.userprofile.IMSSubscriptionDocument;
import org.cipango.littleims.scscf.charging.CDF;
import org.cipango.littleims.scscf.cx.CxManager;
import org.cipango.littleims.scscf.data.UserProfile;
import org.cipango.littleims.scscf.data.UserProfileCache;
import org.cipango.littleims.scscf.media.Policy;
import org.cipango.littleims.scscf.registrar.Context;
import org.cipango.littleims.scscf.registrar.Registrar;
import org.cipango.littleims.scscf.util.IDGenerator;
import org.cipango.littleims.scscf.util.MessageSender;
import org.cipango.littleims.util.Headers;
import org.cipango.littleims.util.Methods;
import org.cipango.littleims.util.URIHelper;

public class SessionManagerImpl implements SessionManager
{
	public static final String ORIG_PARAM = "orig";
	public static final String TERM_PARAM = "term";
	private static final String ODI = "odi";
	
	private final Logger _log = Logger.getLogger(SessionManagerImpl.class);

	
	private boolean _terminatingDefault;
	private IDGenerator _idgen = new IDGenerator();
	private SessionMap _sessionMap;
	private SipFactory _sipFactory;
	private EnumClient _enumClient;

	private Policy _mediaPolicy;
	private CDF _cdf;
	private Registrar _registrar;
	private UserProfileCache _userProfileCache;
	private SipURI _icscfUri;
	private SipURI _scscfUri;
	private SipURI _bgcfUri;
	private CxManager _cxManager;
	private MessageSender _messageSender;
	
	
	public void init()
	{
		if (_icscfUri != null)
			_icscfUri.setLrParam(true);
		
		if (!_terminatingDefault)
		{
			_log.info("Using Originating as default mode");
			if (_icscfUri == null)
				_log.warn("Terminating is set as default mode and I-CSCF uri is defined");
			else
				_icscfUri.setParameter(TERM_PARAM, "");
		}
		else
		{
			_log.info("Using Terminating as default mode");
		}
		_bgcfUri.setLrParam(true);
		_sessionMap = new SessionMap();
	}
	
	public void doInitialRequest(SipServletRequest request) throws ServletException, IOException
	{
		doInitialRequest(request, false);
	}
	
	public void doInitialRequest(SipServletRequest request, boolean sarAnswer) throws ServletException, IOException
	{
		if (!sarAnswer && _log.isDebugEnabled())
			_log.debug("Received initial request: " + request.getMethod() + " " + request.getRequestURI());
		
		if (!isComeFromTrustedDomain(request))
			request.removeHeader(Headers.P_SERVED_USER);
		
		// check if original dialog identifier is present
		String odi = request.getParameter(Session.ORIGINAL_DIALOG_IDENTIFIER_PARAM);
		if (odi == null)
		{
			// no original dialog identifier
			// indicates that the request is visiting the S-CSCF for the
			// first time

			odi = generateODI();
			_log.trace("No original dialog identifier. Generating ODI: " + odi);

			// new session, we need to determine session case
			Session session = null;
			UserProfile profile = null;
			boolean isOrig = isOriginating(request);
			_log.debug("Processing request in " + (isOrig ? "originating" : "terminating")
					+ " mode.");
			String pProfileKey = request.getHeader(Headers.P_PROFILE_KEY);
			
			if (isOrig)
			{
				URI served = getServerUser(request, true);

				Context context = _registrar.getContext(served);
				
				profile = _userProfileCache.getProfile(served.toString(), pProfileKey);
				
				if (profile == null && !sarAnswer)
				{
					// S-CSCF does not have user profile, download user profile
					_cxManager.sendSAR(served.toString(), 
							null, 
							pProfileKey, 
							ServerAssignmentType.UNREGISTERED_USER, 
							false, 
							request);
					return;
				}
				
				// TODO add support to P-Asserted-Service
				session = new OriginatingSession(profile, context != null);
				
				session.setSessionManager(this);
				_log.debug("Creating originating session for served user: " + served);

			}
			else
			{
				URI served = getServerUser(request, false);

				// find out whether the user is registered or not
				Context context = _registrar.getContext(served);
				boolean registered = context != null;
				
				_log.debug("Creating terminating session for served user: " + served
						+ (registered ? " (registered)" : " (unregistered)"));

				// check if we have user profile and, if not, download it
				profile = _userProfileCache.getProfile(served.toString(), pProfileKey);
				if (profile == null && !sarAnswer)
				{
					// S-CSCF does not have user profile, download user profile
					_cxManager.sendSAR(served.toString(), 
							null, 
							pProfileKey, 
							ServerAssignmentType.UNREGISTERED_USER, 
							false, 
							request);
					return;
				}

				// ---------- Mobility modification

				if (profile == null)
				{

					_log.info("Called user: " + served + " is not known.");
					if (isCSUser(served))
					{
						if (_bgcfUri != null)
						{

							String number = ((SipURI) served).getUser();
							SipURI telSip = _sipFactory.createSipURI(number, _bgcfUri.getHost());
							if (_bgcfUri.getPort() != -1)
							{
								telSip.setPort(_bgcfUri.getPort());
							}
							_log.info("Target: " + served + " is CS user. Routing to "
									+ telSip);
							request.getSession().setAttribute("scscf.role", "terminating");
							request.getProxy().setRecordRoute(true);

							request.getProxy().proxyTo(telSip);
							return;
						}
						else
						{
							_log.warn("CS user but no BGCF configured.");
							_messageSender.sendResponse(request, SipServletResponse.SC_NOT_FOUND);
							return;
						}
					}
					else
					{
						_log.info("Not CS user. Sending 404 response");
						_messageSender.sendResponse(request, SipServletResponse.SC_NOT_FOUND);
						
						return;
					}

				}
				// ----------- End
				

				session = new TerminatingSession(profile, context);
				session.setSessionManager(this);

				// log.info("Scheduling BYE");
				// timer.schedule(new ReleaseTask(request.getSession()),
				// 10000);
			}

			// first check media policy
			if (!_mediaPolicy.isAcceptable(request, profile))
			{
				_log.info("Media is not accepted. Sending 488 response");
				_messageSender.sendResponse(request, SipServletResponse.SC_NOT_ACCEPTABLE);
				return;
			}

			SipURI odiURI = (SipURI) _scscfUri.clone();
			odiURI.setParameter(Session.ORIGINAL_DIALOG_IDENTIFIER_PARAM, odi);
			odiURI.setLrParam(true);
			session.setOwnURI(odiURI);

			_sessionMap.addSession(odi, session);

			request.getSession().setAttribute(ODI, odi);
			boolean processingOver = session.handleInitialRequest(request);
			if (processingOver) 
				_sessionMap.removeSession(odi);
			 
		}
		else
		{
			_log.debug("ODI: " + odi + " found in request. Invoking session.");
			Session session = _sessionMap.getSession(odi);
			boolean processingOver = session.handleInitialRequest(request);
			if (processingOver) 
				_sessionMap.removeSession(odi);
		}
	}
	
	
	public void handleSaa(DiameterAnswer saa)
	{
		SipServletRequest request = (SipServletRequest) saa.getRequest().getAttribute(SipServletRequest.class.getName());
		try
		{
			
			if (saa.getResultCode() >= 3000)
			{
				// FIXME what to do ????
				_log.debug("Diameter SAA answer is not valid: " + saa.getResultCode());
			}
			else
			{
				AVP userData = saa.getAVP(IMS.IMS_VENDOR_ID, IMS.USER_DATA);
				IMSSubscriptionDocument subscription = null;
				if (userData != null)
				{
					subscription = IMSSubscriptionDocument.Factory.parse(userData.getString());
					_userProfileCache.cacheUserProfile(subscription);
				}
			}

			doInitialRequest(request, true);
		}
		catch (Exception e) 
		{
			_log.warn("Unable to process SAA answer", e);
		}

	}
	
	public void handlePpr(DiameterRequest ppr)
	{
		try
		{
			AVP userData = ppr.getAVP(IMS.IMS_VENDOR_ID, IMS.USER_DATA);
			IMSSubscriptionDocument subscription = null;
			if (userData != null)
			{
				subscription = IMSSubscriptionDocument.Factory.parse(userData.getString());
				_userProfileCache.cacheUserProfile(subscription);
				_log.info("Update user profile of " 
						+ subscription.getIMSSubscription().getPrivateID()
						+ " after PPR request");
			}

			ppr.createAnswer(Base.DIAMETER_SUCCESS).send();
		}
		catch (Exception e) 
		{
			_log.warn("Unable to process PPR request", e);
			try { ppr.createAnswer(Base.DIAMETER_UNABLE_TO_COMPLY).send(); } catch (Exception e1) { }
		}
	}

	private boolean isCSUser(URI uri)
	{
		if (!uri.isSipURI())
		{
			return false;
		}
		String user = ((SipURI) uri).getUser();
		if (user == null)
		{
			return false;
		}
		char firstLetter = user.charAt(0);
		if (firstLetter == '+' || (firstLetter >= '0' && firstLetter <= '9'))
		{
			return true;
		}
		return false;
	}

	public void doResponse(SipServletResponse response) throws ServletException, IOException
	{
		String odi = (String) response.getSession().getAttribute(ODI);
		if (odi != null && response.getStatus() >= SipServletResponse.SC_OK)
		{
			_sessionMap.removeSession(odi);
			response.getSession().removeAttribute(ODI);
		}
		
		String method = response.getMethod();
		int statusCode = response.getStatus();
		if (method.equals(Methods.INVITE) && response.getRequest().isInitial()
				&& statusCode >= SipServletResponse.SC_OK && statusCode < SipServletResponse.SC_MULTIPLE_CHOICES)
		{
			// only responses to initial INVITE
			SipSession session = response.getSession();
			String role = (String) session.getAttribute(SCSCF_ROLE);

			if (ROLE_ORIGINATING.equals(role))
			{
				_log.info("Received 200 OK for originating session");
			}
			else if (ROLE_TERMINATING.equals(role))
			{
				_log.info("Received 200 OK for terminating session");
			}
			if (_cdf.isEnabled())
			{
				String id = _cdf.start(response.getRequest(), CDF.ROLE_NODE_PROXY);
				session.setAttribute("cdrid", id);
			}
		}	
	}


	public void doSubsequentRequest(SipServletRequest request)
	{
		SipSession session = request.getSession();
		String role = (String) session.getAttribute(SCSCF_ROLE);
		String method = request.getMethod();
		if (_log.isTraceEnabled())
		{
			if (ROLE_ORIGINATING.equals(role))
				_log.trace("Received " + method + " for originating session: " + session);
			else if (ROLE_TERMINATING.equals(role))
				_log.trace("Received  " + method + " for terminating session: " + session);
		}
		
		if (Methods.BYE.equals(method) && _cdf.isEnabled())
		{
			String id = (String) session.getAttribute("cdrid");
			_cdf.stop(id);
		}
	}

	private String generateODI()
	{
		return _idgen.newRandomID();
	}


	private boolean isOriginating(SipServletRequest request)
	{
		String orig = request.getParameter(ORIG_PARAM);
		String term = request.getParameter(TERM_PARAM);
		if (_terminatingDefault) // default standard mode
			return orig != null;
		else
			return term != null;
	}
	
	private URI getServerUser(SipServletRequest request, boolean originating) throws ServletParseException
	{	
		Address served = request.getAddressHeader(Headers.P_SERVED_USER);
		if (served == null)
		{
			if (originating)
			{
				// Couple of sanity checks first
				served = request.getAddressHeader(Headers.P_ASSERTED_IDENTITY);
				if (served != null)
					_log.debug("No P-Served-User. Using P-Asserted-Identity");
				else
				{
					served = request.getAddressHeader(Headers.P_PREFERRED_IDENTITY);
					if (served != null)
						_log.debug("No P-Asserted-Identity. Using P-Preferred-Identity");
					else
					{
						served = request.getFrom();
						_log.debug("No P-Asserted-Identity. Using From identity");
					}
				}
			}
			else
				return URIHelper.getCanonicalForm(_sipFactory, request.getRequestURI());
		}
		return URIHelper.getCanonicalForm(_sipFactory, served.getURI());
	}
	
	private boolean isComeFromTrustedDomain(SipServletRequest request)
	{
		// TODO implements
		return true;
	}

	public SipFactory getSipFactory()
	{
		return _sipFactory;
	}


	public void setSipFactory(SipFactory sipFactory)
	{
		this._sipFactory = sipFactory;
	}


	/* (non-Javadoc)
	 * @see org.cipango.littleims.scscf.session.SessionManager#getEnumClient()
	 */
	public EnumClient getEnumClient()
	{
		return _enumClient;
	}


	public void setEnumClient(EnumClient enumClient)
	{
		this._enumClient = enumClient;
	}


	/* (non-Javadoc)
	 * @see org.cipango.littleims.scscf.session.SessionManager#getCdf()
	 */
	public CDF getCdf()
	{
		return _cdf;
	}


	public void setCdf(CDF cdf)
	{
		_cdf = cdf;
	}


	/* (non-Javadoc)
	 * @see org.cipango.littleims.scscf.session.SessionManager#getRegistrar()
	 */
	public Registrar getRegistrar()
	{
		return _registrar;
	}


	public CxManager getCxManager()
	{
		return _cxManager;
	}

	public void setCxManager(CxManager cxManager)
	{
		_cxManager = cxManager;
	}

	public void setRegistrar(Registrar registrar)
	{
		_registrar = registrar;
	}


	/* (non-Javadoc)
	 * @see org.cipango.littleims.scscf.session.SessionManager#getUserProfileCache()
	 */
	public UserProfileCache getUserProfileCache()
	{
		return _userProfileCache;
	}


	public void setUserProfileCache(UserProfileCache userProfileCache)
	{
		_userProfileCache = userProfileCache;
	}




	/* (non-Javadoc)
	 * @see org.cipango.littleims.scscf.session.SessionManager#getBgcfUri()
	 */
	public SipURI getBgcfUri()
	{
		return _bgcfUri;
	}


	public void setBgcfUri(SipURI bgcfUri)
	{
		_bgcfUri = bgcfUri;
	}

	public boolean isTerminatingDefault()
	{
		return _terminatingDefault;
	}

	public void setTerminatingDefault(boolean terminatingDefault)
	{
		_terminatingDefault = terminatingDefault;
	}

	public Policy getMediaPolicy()
	{
		return _mediaPolicy;
	}

	public void setMediaPolicy(Policy mediaPolicy)
	{
		this._mediaPolicy = mediaPolicy;
	}

	public SipURI getIcscfUri()
	{
		return _icscfUri;
	}

	public void setIcscfUri(SipURI icscfUri)
	{
		_icscfUri = icscfUri;
		_log.info("I-CSCF URI: " + _icscfUri);
	}

	public SipURI getScscfUri()
	{
		return _scscfUri;
	}

	public void setScscfUri(SipURI scscfUri)
	{
		_scscfUri = scscfUri;
		_log.info("S-CSCF URI: " + _scscfUri);
	}

	public Iterator<Session> getSessions()
	{
		return _sessionMap.getSessions();
	}

	public MessageSender getMessageSender()
	{
		return _messageSender;
	}

	public void setMessageSender(MessageSender messageSender)
	{
		_messageSender = messageSender;
	}

}
