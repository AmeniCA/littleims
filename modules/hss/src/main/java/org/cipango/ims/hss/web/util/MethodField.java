package org.cipango.ims.hss.web.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.extensions.ajax.markup.html.autocomplete.AutoCompleteTextField;
import org.apache.wicket.util.string.Strings;

public class MethodField extends AutoCompleteTextField<String>
{

	private static final List<String> METHODS =
		Arrays.asList("INFO", "INVITE", "MESSAGE", "PUBLISH", "OPTIONS", "REFER", "REGISTER", "SUBSCRIBE");

	
	public MethodField(String id)
	{
		super(id);
	}

	@Override
	protected Iterator<String> getChoices(String input)
	{
		if (Strings.isEmpty(input))
        {
            return METHODS.iterator();
        }
        input = input.trim().toUpperCase();
        List<String> methods = new ArrayList<String>();
        Iterator<String> it = METHODS.iterator();
        while (it.hasNext())
		{
			String method = (String) it.next();
			if (method.startsWith(input))
				methods.add(method);
		}
        return methods.iterator();
	}

}
