package org.nuclos.client.layout.wysiwyg.palette;

import javax.swing.Icon;

import org.nuclos.api.ui.LayoutComponentFactory;
import org.nuclos.client.layout.wysiwyg.WYSIWYGStringsAndLabels.PaletteItemElement;

public class LayoutComponentPaletteItem implements PaletteItemElement {
	
	private final LayoutComponentFactory lcf;

	public LayoutComponentPaletteItem(LayoutComponentFactory lcf) {
		super();
		this.lcf = lcf;
	}

	@Override
	public String getToolTip() {
		return lcf.getClass().getName();
	}

	@Override
	public String getLabel() {
		return lcf.getName();
	}

	@Override
	public Icon getIcon() {
		return lcf.getIcon();
	}

	@Override
	public boolean displayLabelAndIcon() {
		return true;
	}

	@Override
	public boolean isLabeledComponent() {
		return false;
	}
}
