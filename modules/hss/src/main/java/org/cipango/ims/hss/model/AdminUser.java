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
package org.cipango.ims.hss.model;

import java.security.MessageDigest;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.cipango.ims.hss.util.HexString;

@Entity
public class AdminUser
{

	@Id @GeneratedValue
	private Long _id;
	
	@Column (unique = true)
	private String _login;
	private String _password;
	
	public Long getId()
	{
		return _id;
	}
	public void setId(Long id)
	{
		_id = id;
	}
	public String getLogin()
	{
		return _login;
	}
	public void setLogin(String login)
	{
		_login = login;
	}
	protected String getPassword()
	{
		return _password;
	}
	protected void setPassword(String password)
	{
		_password = password;
	}
	public void setClearPassword(String password)
	{
		_password = getObfuscatedPassword(password);
	}
	
	public boolean equalsPassword(String clearPassword)
	{
		return _password.equals(getObfuscatedPassword(clearPassword));
	}
	
	private String getObfuscatedPassword(String password)
	{
		try {
			MessageDigest md = MessageDigest.getInstance("SHA");
			md.update(_id.byteValue());
			md.update(password.getBytes("UTF-8"));
			return HexString.toHexString(md.digest());
		} catch (Exception e) {
			throw new RuntimeException("Unable to obfuscate password", e);
		}
	}
	
}
