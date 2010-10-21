package org.cipango.littleims.util;

public class RegexUtil
{
	
	private static final String SPECIAL_REGEX_CHARACTERS = ".?$+|[]*";

	public static String extendedRegexToSqlRegex(String s)
	{
		StringBuilder sb = new StringBuilder(s.length());
		boolean regexPart = false;
		int indexExclamation = s.indexOf('!');
		
		for (int i = 0; i < s.length(); i++)
		{
			if (i == indexExclamation)
			{
				regexPart = !regexPart;
				indexExclamation = s.indexOf('!', indexExclamation + 1);
			}
			else
			{
				char c = s.charAt(i);
				if (c == '.' && s.charAt(i + 1) == '*')
				{
					sb.append("%");
					i++;
				}
				else if (c == '?')
					sb.append('_');
				else
					sb.append(c);	
			}
		}

		return sb.toString();
	}
	
	public static String extendedRegexToJavaRegex(String s)
	{
		StringBuilder sb = new StringBuilder(s.length());
		boolean regexPart = false;
		int indexExclamation = s.indexOf('!');
		
		for (int i = 0; i < s.length(); i++)
		{
			if (i == indexExclamation)
			{
				regexPart = !regexPart;
				indexExclamation = s.indexOf('!', indexExclamation + 1);
			}
			else
			{
				char c = s.charAt(i);
				if (!regexPart && SPECIAL_REGEX_CHARACTERS.indexOf(c) != -1)
					sb.append('\\');	
				sb.append(c);			
			}
		}

		return sb.toString();
	}
}
