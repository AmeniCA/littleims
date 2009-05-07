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

package org.cipango.ims.hss.db;

import java.util.List;

import org.cipango.ims.hss.model.PrivateIdentity;

public interface PrivateIdentityDao extends ImsDao<PrivateIdentity>
{
	void save(PrivateIdentity impi);
	
	PrivateIdentity findById(String id);
	List<PrivateIdentity> findAll();
	
	public void delete(PrivateIdentity privateIdentity);
	
	 /**
     * Returns the public identities that NOT belong the private identity with identity id
     * but have the same subscription.
     */
	List<String> getAvalaiblePublicIds(PrivateIdentity privateIdentity);
	
	 /**
     * Returns the public identities that are linked to any private identity.
     */
	List<String> getAvalaiblePublicIdsNoPrivate();
	
}
