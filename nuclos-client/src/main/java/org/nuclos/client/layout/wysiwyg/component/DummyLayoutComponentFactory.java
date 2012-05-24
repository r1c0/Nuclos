package org.nuclos.client.layout.wysiwyg.component;

import javax.swing.Icon;

import org.nuclos.api.ui.Alignment;
import org.nuclos.api.ui.LayoutComponent;
import org.nuclos.api.ui.LayoutComponentFactory;

public class DummyLayoutComponentFactory implements LayoutComponentFactory {

	private final String label;
	
	public DummyLayoutComponentFactory(String label) {
		super();
		this.label = label;
	}

	@Override
	public LayoutComponent newInstance() {
		return new DummyLayoutComponent(label);
	}

	@Override
	public String getName() {
		return "Dummy";
	}

	@Override
	public Icon getIcon() {
		return null;
	}

	@Override
	public Alignment getDefaulAlignment() {
		return null;
	}

	@Override
	public Object getDefaultPropertyValue(String property) {
		return null;
	}

}
