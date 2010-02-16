// ========================================================================
// Copyright 2010 NEXCOM Systems
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
package org.cipango.ims.hss.model.uss;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.cipango.ims.hss.model.NafGroup;
import org.cipango.ims.hss.model.PrivateIdentity;
import org.cipango.ims.hss.util.XML.Convertible;
import org.cipango.ims.hss.util.XML.Output;

@Entity
public class Uss implements Convertible
{
	@Id @GeneratedValue
	private Long _id;
	
	private Integer _type;
	
	@ManyToOne
	@JoinColumn (nullable = false)
	private PrivateIdentity _privateIdentity;
	
	private Integer _flag;
	
	@ManyToOne
	@JoinColumn (nullable = true)
	private NafGroup _nafGroup;
	
	public void print(Output out)
	{
		out.open("uids");
		out.add("uid", _privateIdentity.getPublicIds());
		out.close("uids");
		if (_flag != null)
		{
			out.open("flags");
			out.add("flag", _flag);
			out.close("flags");
		}
	}
	
	public Map<String, String> getXmlAttributes()
	{
		Map<String, String> attributes = new HashMap<String, String>();
		attributes.put("id", String.valueOf(_id));
		attributes.put("type", String.valueOf(_type));
		if (_nafGroup != null)
			attributes.put("nafGroup", _nafGroup.getName());
		
		return attributes;
	}

	public Long getId()
	{
		return _id;
	}

	public void setId(Long id)
	{
		_id = id;
	}

	public Integer getType()
	{
		return _type;
	}
	
	public String getTypeAsString()
	{
		return getTypeAsString(_type);
	}

	public void setType(Integer type)
	{
		_type = type;
	}

	public PrivateIdentity getPrivateIdentity()
	{
		return _privateIdentity;
	}

	public void setPrivateIdentity(PrivateIdentity privateIdentity)
	{
		_privateIdentity = privateIdentity;
	}

	public NafGroup getNafGroup()
	{
		return _nafGroup;
	}

	public void setNafGroup(NafGroup nafGroup)
	{
		_nafGroup = nafGroup;
	}
	
	public Integer getFlag()
	{
		return _flag;
	}

	public void setFlag(Integer flag)
	{
		_flag = flag;
	}
	
	public String getFlagAsString()
	{
		return getFlagAsString(_type, _flag);
	}
	
	public static Integer[] getDefinedFlagList(Integer type)
	{
		if (type == null)
			return null;
		switch (type)
		{
		case 1:
			return new Integer[] {1, 2};
		case 8:
		case 11:
			return new Integer[] {0, 1};
		default:
			return null;
		}
	}
	
	public static String getFlagAsString(Integer type, Integer flag)
	{
		if (type == null || flag == null)
			return null;
		
		switch (type)
		{
		case 1:
			switch (flag)
			{
			case 1:
				return "Authentication allowed";
			case 2:
				return "Non-repudiation allowed";	
			}
			break;
		case 8:
			switch (flag)
			{
			case 0:
				return "Not authorized";
			case 1:
				return "Authorized according to policy stored in ANDSF server";	
			}
			break;
		case 11:
			switch (flag)
			{
			case 0:
				return "Not authorized";
			case 1:
				return "Authorized";	
			}
			break;
		}
		return String.valueOf(flag);
	}
	
	
	public static String getTypeAsString(int type)
	{
		switch (type)
		{
		case 0:
			return "Unspecific service";
		case 1:
			return "PKI-Portal";
		case 2:
			return "Authentication Proxy";
		case 3:
			return "Presence";
		case 4:
			return "MBMS";
		case 5:
			return "Liberty Alliance Project";
		case 6:
			return "UICC - Terminal Key Establishment";
		case 7:
			return "Terminal – Remote Device Key Establishment";
		case 8:
			return "ANDSF";
		case 9:
			return "GBA Push";
		case 10:
			return "IMS based PSS MBMS";
		case 11:
			return "OpenID GBA Interworking";
		case 12:
			return "Generic Push Layer";
		default:
			return String.valueOf(type);
		}
	}

	
}
