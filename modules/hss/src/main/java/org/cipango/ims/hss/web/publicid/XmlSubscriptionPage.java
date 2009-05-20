package org.cipango.ims.hss.web.publicid;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.util.collections.MicroMap;
import org.apache.wicket.util.string.interpolator.MapVariableInterpolator;
import org.cipango.ims.hss.model.PSI;
import org.cipango.ims.hss.model.PublicIdentity;
import org.cipango.ims.hss.model.PublicUserIdentity;

public class XmlSubscriptionPage extends PublicIdentityPage
{
	private String _key;
	private String _title;
	
	public XmlSubscriptionPage(PageParameters pageParameters)
	{
		_key = pageParameters.getString("id");
		PublicIdentity publicIdentity = null;
		if (_key != null)
		{
			publicIdentity = _dao.findById(_key);
			if (publicIdentity == null)
			{
				error(MapVariableInterpolator.interpolate(getString(getPrefix() + ".error.notFound"),
						new MicroMap("identity", _key)));
				_key = null;
			}
		}
	
		_title = getString("publicId.subscritionAsXml", new DaoDetachableModel(publicIdentity));
		add(new Label("title", _title));
		add(new Label("xml", publicIdentity.getImsSubscriptionAsXml(null, null, true)));
		
		if (publicIdentity instanceof PublicUserIdentity)
			setContextMenu(new ContextPanel((PublicUserIdentity) publicIdentity));
		else if (publicIdentity instanceof PSI)
			setContextMenu(new PsiContextPanel((PSI) publicIdentity));
	}

	@Override
	public String getTitle()
	{
		return _title;
	}

}
