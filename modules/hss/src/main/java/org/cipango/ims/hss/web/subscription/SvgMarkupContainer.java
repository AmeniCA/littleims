package org.cipango.ims.hss.web.subscription;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.Model;
import org.cipango.ims.hss.model.Subscription;
import org.cipango.ims.hss.web.ImsApplication;

public class SvgMarkupContainer extends WebMarkupContainer
{
	private String _encodedName;

	@SuppressWarnings("unchecked")
	public SvgMarkupContainer(String id, Subscription subscription)
	{
		super(id);
		if (subscription == null)
		{
			setVisible(false);
			return;
		}
		
		_encodedName = subscription.getName().replaceAll("\\:", "%3A");
		int maxElem = Math.max(subscription.getPrivateIdentities().size(), subscription.getPublicIdentities().size());
		add(new AttributeModifier("height", new Model(maxElem * 100 + 10)));
		setOutputMarkupId(true);
	}
	
	@Override
	protected void onBeforeRender() {
		super.onBeforeRender();
		if (isVisible())
		{
			String contextPath = ((ImsApplication) getApplication()).getContextPath();
			add(new AttributeModifier("src", true, new Model(contextPath + "/svg/subscription.svg?key=" + _encodedName)));
		}
	}

}
