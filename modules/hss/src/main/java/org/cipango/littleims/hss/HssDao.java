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

import org.cipango.littleims.cx.ResultCode;
import org.cipango.littleims.cx.data.userprofile.IMSSubscriptionDocument;
import org.cipango.littleims.cx.data.userprofile.TPublicIdentity;

public interface HssDao
{

	@Deprecated
	public IMSSubscriptionDocument getUserProfile(String publicUserIdentity);
	
	public IMSSubscriptionDocument getUserProfile(String publicUserIdentity, String wilcardPublicId);
	
	public TPublicIdentity getIdentity(String publicUserIdentity, String wilcardPublicId) throws HssException;
	
	public Credentials getCredentials(String publicUserIdentity);
	
	/**
	 * if public user identity is matching returns null
	 * else if a wilcard public identity is matching, returns the wilcard
	 * else throw an HssException with resultCode {@link ResultCode#DIAMETER_ERROR_USER_UNKNOWN}
	 */
	public String getWilcardPublicIdentity(String publicUserIdentity) throws HssException;
}
