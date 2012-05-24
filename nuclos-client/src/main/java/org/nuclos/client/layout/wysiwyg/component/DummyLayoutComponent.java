package org.nuclos.client.layout.wysiwyg.component;

import javax.swing.JComponent;
import javax.swing.JLabel;

import org.nuclos.api.Preferences;
import org.nuclos.api.Property;
import org.nuclos.api.ui.LayoutComponent;

public class DummyLayoutComponent extends JLabel implements LayoutComponent {
	
	public DummyLayoutComponent(String label) {
		super("LayoutComponent " + label);
	}

	@Override
	public JComponent getComponent() {
		return this;
	}

	@Override
	public JComponent getDesignComponent() {
		return this;
	}

	@Override
	public void setPreferences(Preferences prefs) {
	}

	@Override
	public void setProperty(String name, Object value) {
	}

	@Override
	public Property[] getComponentProperties() {
		return null;
	}

	@Override
	public String[] getComponentPropertyLabels() {
		return null;
	}

}
