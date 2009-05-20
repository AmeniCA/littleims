package org.cipango.ims.hss.web.serviceprofile;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.RefreshingView;
import org.apache.wicket.markup.repeater.util.ModelIteratorAdapter;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.cipango.ims.hss.model.InitialFilterCriteria;
import org.cipango.ims.hss.util.XML;
import org.cipango.ims.hss.util.XML.Output;
import org.cipango.ims.hss.web.as.EditAsPage;
import org.cipango.ims.hss.web.ifc.EditIfcPage;
import org.cipango.ims.hss.web.spt.EditSptsPage;
import org.cipango.ims.hss.web.util.HideableLink;

@SuppressWarnings("unchecked")
public class IfcViewPanel extends Panel
{
	private boolean _shared;
	
	public IfcViewPanel(String id, IModel model, boolean shared)
	{
		super(id, model);
		_shared = shared;
		add(new RefreshingView("ifc", model)
		{
			@Override
			protected Iterator getItemModels()
			{
				return new CompoundModelIterator((Collection) getDefaultModelObject());
			}

			@Override
			protected void populateItem(Item item)
			{
				InitialFilterCriteria ifc = (InitialFilterCriteria) item.getDefaultModelObject();
				MarkupContainer link = new BookmarkablePageLink("elementLink", 
						EditIfcPage.class, 
						new PageParameters("id=" + ifc.getName()));
				item.add(link);
				link.add(new Label("title", _shared ? getString("view.ifc.shared.title") : getString("view.ifc.title")));
				link.add(new Label("name", ifc.getName()));
				
				item.add(new Label("id").setVisible(_shared));
				
				item.add(new Label("priority"));
				item.add(new Label("profilePartIndicatorAsString"));
				
				MarkupContainer asLink = new BookmarkablePageLink("asLink",
						EditAsPage.class,
						new PageParameters("id=" + ifc.getApplicationServerName()));
				item.add(asLink);
				asLink.add(new Label("applicationServer", ifc.getApplicationServerName()));

				MarkupContainer sptLink = new BookmarkablePageLink("sptLink", 
						EditSptsPage.class, 
						new PageParameters("id=" + ifc.getName()));
				item.add(sptLink);
				item.add(new Label("expression"));
				
				Output out = XML.getPretty().newOutput();
				ifc.print(out);
				String xml = out.toString();
				item.add(new Label("xml", xml));
												
				item.setOutputMarkupId(true);
				item.add(new HideableLink("hideLink", item.getMarkupId()));
			}
		});
	}


	
	class CompoundModelIterator extends ModelIteratorAdapter implements Serializable {
		public CompoundModelIterator(Collection modelObject) {
			super(modelObject.iterator());
		}
		
		@Override
		protected IModel model(Object id)
		{
			return new CompoundPropertyModel(new LoadableDetachableModel(id) {

				@Override
				protected Object load()
				{
					// TODO implements
					return null;
				}
				
			});
		}
	}
}
