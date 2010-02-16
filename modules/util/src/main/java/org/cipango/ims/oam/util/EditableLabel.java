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
package org.cipango.ims.oam.util;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

public class EditableLabel<T> extends Panel
{

	private FormComponent<T> _editor;
	private Component _label;

	public EditableLabel(String id)
	{
		this(id, null);
	}
	
	public EditableLabel(String id, IModel<T> model)
	{
		super(id, model);
		setOutputMarkupId(true);
		_editor = newEditor(this, "editor", getDefaultModel());
		add(_editor);
		_label = newLabel(this, "label", getDefaultModel());
		add(_label);
	}
	
	@Override
	public final MarkupContainer setDefaultModel(IModel<?> model)
	{
		super.setDefaultModel(model);
		getLabel().setDefaultModel(model);
		getEditor().setDefaultModel(model);
		return this;
	}
		
	public void setEditable(boolean editable)
	{
		getEditor().setVisible(editable);
		getLabel().setVisible(!editable);
	}

	protected FormComponent<T> newEditor(MarkupContainer parent,
			String componentId, IModel model)
	{
		TextField<T> editor = new TextField<T>(componentId, model);
		editor.setOutputMarkupId(true);
		editor.setVisible(false);
		editor.setOutputMarkupPlaceholderTag(true);
		return editor;
	}

	protected Component newLabel(MarkupContainer parent, String componentId,
			IModel model)
	{
		Label label = new Label(componentId, model);
		label.setOutputMarkupId(true);
		label.setOutputMarkupPlaceholderTag(true);
		return label;
	}

	protected final FormComponent<T> getEditor()
	{
		return _editor;
	}

	protected final Component getLabel()
	{
		return _label;
	}
}
