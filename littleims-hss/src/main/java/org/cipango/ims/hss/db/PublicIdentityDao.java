//========================================================================
//Copyright 2008-2009 NEXCOM Systems
//------------------------------------------------------------------------
//Licensed under the Apache License, Version 2.0 (the "License");
//you may not use this file except in compliance with the License.
//You may obtain a copy of the License at 
//http://www.apache.org/licenses/LICENSE-2.0
//Unless required by applicable law or agreed to in writing, software
//distributed under the License is distributed on an "AS IS" BASIS,
//WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//See the License for the specific language governing permissions and
//limitations under the License.
//========================================================================

package org.cipango.ims.hss.db;

import java.util.Iterator;
import java.util.List;

import org.cipango.ims.hss.model.PublicIdentity;

public interface PublicIdentityDao extends Dao, ImsDao<PublicIdentity>
{
	void save(PublicIdentity impu);
	PublicIdentity findById(String id);
	List<String> findLike(String id, int maxResults);
	List<PublicIdentity> findAll();
	PublicIdentity findWilcard(String id);	
	public Iterator<PublicIdentity> iterator(int first, int count,
			String sort, boolean sortAsc, String foreignKeyName, Long foreignKeyId);
	
	public Iterator<PublicIdentity> likeIterator(int first, int count, String sort,
			boolean sortAsc, String likeIdentity);
	
	public int countLike(String likeIdentity);
	
}
