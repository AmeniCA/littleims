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
package org.cipango.littleims.cx;

public class ServerAssignmentType
{
	  public static final int NO_ASSIGNEMENT 							= 0;
	  public static final int REGISTRATION	 							= 1;
	  public static final int RE_REGISTRATION 							= 2;
	  public static final int UNREGISTERED_USER							= 3;
	  public static final int TIMEOUT_DEREGISTRATION					= 4;
	  public static final int USER_DEREGISTRATION 						= 5;
	  public static final int TIMEOUT_DEREGISTRATION_STORE_SERVER_NAME 	= 6;
	  public static final int USER_DEREGISTRATION_STORE_SERVER_NAME 	= 7;
	  public static final int ADMINISTRATIVE_DEREGISTRATION 			= 8;
	  public static final int AUTHENTICATION_FAILURE 					= 9;
	  public static final int AUTHENTICATION_TIMEOUT 					= 10;
	  public static final int DEREGISTRATION_TOO_MUCH_DATA 				= 11;
}
