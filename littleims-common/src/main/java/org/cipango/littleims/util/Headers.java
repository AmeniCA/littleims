// ========================================================================
// Copyright 2008-2009 NEXCOM Systems
// ------------------------------------------------------------------------
// Licensed under the Apache License, Version 2.0 (the "License"),
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at 
// http://www.apache.org/licenses/LICENSE-2.0
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
// ========================================================================
package org.cipango.littleims.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class Headers
{

	private static final List<String> H = new ArrayList<String>();
	public static final List<String> ALL_HEADERS = Collections.unmodifiableList(H);

	private static String add(String header)
	{
		H.add(header);
		return header;
	}
	
		
	public static final String
		ACCEPT 							= add("Accept"), 
		ACCEPT_CONTACT 					= add("Accept-Contact"),
		ACCEPT_ENCODING 				= add("Accept-Encoding"),
		ACCEPT_LANGUAGE 				= add("Accept-Language"),
		ACCEPT_RESOURCE_PRIORITY 		= add("Accept-Resource-Priority"),
		ALERT_INFO 						= add("Alert-Info"),
		ALLOW 							= add("Allow"),
		ALLOW_EVENTS 					= add("Allow-Events"),	
		AUTHENTICATION_INFO 			= add("Authentication-Info"),
		AUTHORIZATION 					= add("Authorization"), 
		CALL_ID 						= add("Call-ID"),
		CALL_INFO 						= add("Call-Info"),
		CONTACT 						= add("Contact"),
		CONTENT_DISPOSITION 			= add("Content-Disposition"),
		CONTENT_ENCODING 				= add("Content-Encoding"),
		CONTENT_LANGUAGE 				= add("Content-Language"),
		CONTENT_LENGTH 					= add("Content-Length"),
		CONTENT_TYPE 					= add("Content-Type"),
		CSEQ 							= add("CSeq"),
		DATE 							= add("Date"),
		ERROR_INFO 						= add("Error-Info"),
		EVENT 							= add("Event"),
		EXPIRES 						= add("Expires"),
		FROM 							= add("From"),
		HISTORY_INFO 					= add("History-Info"),
		IDENTITY 						= add("Identity"),
		IDENTITY_INFO					= add("Identity-Info"),
		IN_REPLY_TO 					= add("In-Reply-To"),
		JOIN 							= add("Join"), 
		MAX_FORWARDS 					= add("Max-Forwards"),
		MIME_VERSION					= add("MIME-Version"),
		MIN_EXPIRES 					= add("Min-Expires"),
		MIN_SE 							= add("Min-SE"),
		ORGANIZATION 					= add("Organization"),
		P_ACCESS_NETWORK_INFO 			= add("P-Access-Network-Info"),
		P_ASSERTED_IDENTITY		 		= add("P-Asserted-Identity"),
		P_ASSERTED_SERVICE		 		= add("P-Asserted-Service"),
		P_ASSOCIATED_URI 				= add("P-Associated-URI"),
		P_CALLED_PARTY_ID 				= add("P-Called-Party-ID"),
		P_CHARGING_FUNCTION_ADDRESSES 	= add("P-Charging-Function-Addresses"),
		P_CHARGING_VECTOR 				= add("P-Charging-Vector"),
		P_DEBUG_ID						= add("P-Debug-ID"),
		P_MEDIA_AUTHORIZATION 			= add("P-Media-Authorization"), 
		P_PREFERRED_IDENTITY 			= add("P-Preferred-Identity"),
		P_PREFERRED_SERVICE 			= add("P-Preferred-Service"),
		P_PROFILE_KEY 					= add("P-Profile-Key"),
		P_SERVED_USER 					= add("P-Served-User"),
		P_USER_DATABASE					= add("P-User-Database"),
		P_VISITED_NETWORK_ID 			= add("P-Visited-Network-ID"),
		PATH 							= add("Path"),
		PRIVACY 						= add("Privacy"),
		PRIORITY 						= add("Priority"),
		PROXY_AUTHENTICATE 				= add("Proxy-Authenticate"),
		PROXY_AUTHORIZATION 			= add("Proxy-Authorization"),
		PROXY_REQUIRE 					= add("Proxy-Require"),
		RACK 							= add("RAck"),
		REASON 							= add("Reason"),
		RECORD_ROUTE 					= add("Record-Route"),
		REFER_SUB 						= add("Refer-Sub"),
		REFER_TO 						= add("Refer-To"),
		REFERRED_BY 					= add("Referred-By"),
		REJECT_CONTACT 					= add("Reject-Contact"),
		REPLACES 						= add("Replaces"),
		REPLY_TO 						= add("Reply-To"),
		REQUEST_DISPOSITION 			= add("Request-Disposition"),
		REQUIRE 						= add("Require"), 
		RESOURCE_PRIORITY 				= add("Resource-Priority"),
		RETRY_AFTER 					= add("Retry-After"), 
		ROUTE 							= add("Route"),
		RSEQ 							= add("RSeq"),
		SECURITY_CLIENT 				= add("Secury-Client"), 
		SECURITY_SERVER 				= add("Security-Server"),
		SECURITY_VERIFY 				= add("Security-Verify"),
		SERVER 							= add("Server"),
		SERVICE_ROUTE 					= add("Service-Route"), 
		SESSION_EXPIRES 				= add("Session-Expires"),
		SIP_ETAG 						= add("SIP-ETag"),
		SIP_IF_MATCH 					= add("SIP-If-Match"),
		SUBJECT 						= add("Subject"),
		SUBSCRIPTION_STATE 				= add("Subscription-State"),
		SUPPORTED 						= add("Supported"),
		TARGET_DIALOG 					= add("Target-Dialog"),
		TIMESTAMP 						= add("Timestamp"),
		TO 								= add("To"),
		UNSUPPORTED 					= add("Unsupported"),
		USER_AGENT 						= add("User-Agent"),
		VIA 							= add("Via"),
		WARNING 						= add("Warning"),
		WWW_AUTHENTICATE 				= add("WWW-Authenticate");
	
}
