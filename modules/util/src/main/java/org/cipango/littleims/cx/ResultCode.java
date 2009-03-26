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
package org.cipango.littleims.cx;


public abstract class ResultCode {

	public static final int	DIAMETER_MULTI_ROUND_AUTH 					= 1001;
	public static final int DIAMETER_SUCCESS 							= 2001;
	public static final int DIAMETER_AUTHENTICATION_REJECTED 			= 4001;
	public static final int DIAMETER_USER_NAME_REQUIRED					= 4991;
	public static final int DIAMETER_ERROR_USER_UNKNOWN 				= 5991;
	public static final int DIAMETER_ERROR_IDENTITIES_DONT_MATCH		= 5992;
	public static final int DIAMETER_AUTHORIZATION_REJECTED				= 5003;
	public static final int DIAMETER_ERROR_AUTH_SCHEME_NOT_SUPPORTED	= 5996;
	public static final int DIAMETER_UNABLE_TO_COMPLY          			= 5012;

}
