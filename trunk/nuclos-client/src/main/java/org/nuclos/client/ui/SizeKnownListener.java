// Copyright (C) 2011 Novabit Informationssysteme GmbH
//
// This file is part of Nuclos.
//
// Nuclos is free software: you can redistribute it and/or modify
// it under the terms of the GNU Affero General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// Nuclos is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU Affero General Public License for more details.
//
// You should have received a copy of the GNU Affero General Public License
// along with Nuclos. If not, see <http://www.gnu.org/licenses/>.
package org.nuclos.client.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * A Listener to a certain tab of a JInfoTabbedPane.
 * 
 * @see JInfoTabbedPane
 * @author Thomas Pasch
 * @since Nuclos 3.1.00
 */
public class SizeKnownListener implements ActionListener {

	private final JInfoTabbedPane	pane;

	private final int				tab;

	public SizeKnownListener(JInfoTabbedPane pane, int tab) {
		this.pane = pane;
		this.tab = tab;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e instanceof SizeKnownEvent) {
			final SizeKnownEvent ske = (SizeKnownEvent) e;
			pane.setTabInfoAt(tab, ske.getSize());
			pane.setDisplayTabInfoAt(tab, true);
		}
	}

}
