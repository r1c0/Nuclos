package org.nuclos.client.main.mainframe.desktop;

import javax.swing.JComponent;

public class ApiDesktopItem extends DesktopItem {

	private final org.nuclos.api.ui.DesktopItem di;
	
	public ApiDesktopItem(org.nuclos.api.ui.DesktopItem di) {
		super();
		this.di = di;
	}

	@Override
	public JComponent getJComponent() {
		return di.getComponent();
	}

}
