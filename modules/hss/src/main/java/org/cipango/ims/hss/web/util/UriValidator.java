package org.cipango.ims.hss.web.util;

import java.util.Map;

import javax.servlet.sip.URI;

import org.apache.wicket.Component;
import org.apache.wicket.util.string.Strings;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidatorAddListener;
import org.apache.wicket.validation.validator.AbstractValidator;
import org.cipango.ims.hss.web.BasePage;


public class UriValidator extends AbstractValidator<String> implements IValidatorAddListener
{
	// As SIP factory is not serializable, we keep an indirect reference
	private Component _component;
	private boolean _requiresSipUri;
	private String _reason;
	
	public UriValidator()
	{
		_requiresSipUri = false;
	}
	
	public UriValidator(boolean requiresSipUri)
	{
		_requiresSipUri = requiresSipUri;
	}
	
	@Override
	protected void onValidate(IValidatable<String> validatable)
	{
		try
		{
			if (Strings.isEmpty(validatable.getValue()))
				return;
			
			BasePage basePage = (BasePage) _component.getPage();
			URI uri = basePage.getImsApp().getSipFactory().createURI(validatable.getValue());
			if (!uri.isSipURI() && _requiresSipUri)
			{
				_reason = "Not a SIP URI";
				error(validatable);
			}
		} catch (Exception e)
		{
			_reason = e.getLocalizedMessage();
			error(validatable);
		}
	}

	@Override
	protected String resourceKey()
	{
		return "validator.uri";
	}

	public void onAdded(Component component)
	{
		_component = component;
	}

	@Override
	protected Map<String, Object> variablesMap(IValidatable<String> validatable)
	{
		Map<String, Object> map =  super.variablesMap(validatable);
		map.put("reason", _reason);
		return map;
	}
	
	

}
