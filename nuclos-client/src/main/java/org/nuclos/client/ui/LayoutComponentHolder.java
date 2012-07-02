//Copyright (C) 2010  Novabit Informationssysteme GmbH
//
//This file is part of Nuclos.
//
//Nuclos is free software: you can redistribute it and/or modify
//it under the terms of the GNU Affero General Public License as published by
//the Free Software Foundation, either version 3 of the License, or
//(at your option) any later version.
//
//Nuclos is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU Affero General Public License for more details.
//
//You should have received a copy of the GNU Affero General Public License
//along with Nuclos.  If not, see <http://www.gnu.org/licenses/>.
package org.nuclos.client.ui;

import java.awt.BorderLayout;

import javax.swing.JComponent;
import javax.swing.JPanel;

import org.nuclos.api.ui.LayoutComponent;

public class LayoutComponentHolder extends JPanel {

	private final LayoutComponent lc;
	
	private final JComponent c;
	
	private boolean transferName = true;

	public LayoutComponentHolder(LayoutComponent lc, boolean design) {
		super(new BorderLayout());
		this.lc = lc;
		this.c = design ? lc.getDesignComponent() : lc.getComponent();
		super.add(this.c, BorderLayout.CENTER);
	}
	
	public JComponent getHoldingComponent() {
		return c;
	}
	
	public LayoutComponent getLayoutComponent() {
		return lc;
	}
	
	@Override
	public void setName(String name) {
		super.setName(name);
		if (transferName) {
			lc.setName(name);
		}
	}

}
