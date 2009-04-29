package org.cipango.ims;

import java.util.HashMap;
import java.util.Map;

public class Cx 
{
	public static class AuthenticationScheme
	{
		public static final int SIP_DIGEST_ORDINAL = 0;
		public static final int DIGEST_AKA_MD5_ORDINAL = 1;
		
		private static Map<String, AuthenticationScheme> __schemes = new HashMap<String, AuthenticationScheme>();
		private static Map<String, AuthenticationScheme> __algoritms = new HashMap<String, AuthenticationScheme>();
		
		public static final AuthenticationScheme SIP_DIGEST = add(SIP_DIGEST_ORDINAL, "SIP Digest", "MD5");
		public static final AuthenticationScheme DIGEST_AKA_MD5 = add(DIGEST_AKA_MD5_ORDINAL, "Digest-AKAv1-MD5", "AKAv1-MD5");
		
		static AuthenticationScheme add(int ordinal, String name, String algorithm)
		{
			AuthenticationScheme scheme = new AuthenticationScheme(ordinal, name, algorithm);
			__algoritms.put(scheme._algorithm, scheme);
			__schemes.put(scheme._name, scheme);
			return scheme;
		}
		
		public static AuthenticationScheme get(String name)
		{
			return __schemes.get(name);
		}
		
		public static AuthenticationScheme getFromAlgorithm(String algorithm)
		{
			return __algoritms.get(algorithm);
		}
		
		private String _name;
		private String _algorithm;
		private int _ordinal;
		
		public AuthenticationScheme(int ordinal, String name, String algorithm)
		{
			_ordinal = ordinal;
			_name = name;
			_algorithm = algorithm;
		}
		
		public int getOrdinal()
		{
			return _ordinal;
		}
		
		public String getName()
		{
			return _name;
		}
		
		public String getAlgorithm()
		{
			return _algorithm;
		}
	}
}
