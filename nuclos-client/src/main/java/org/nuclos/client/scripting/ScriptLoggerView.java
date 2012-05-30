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
package org.nuclos.client.scripting;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

@SuppressWarnings("serial")
public class ScriptLoggerView extends JPanel {

	private final JScrollPane sc;

	private final JTextArea ta;

	public ScriptLoggerView() {
		super();
		this.ta = new JTextArea();
		this.ta.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		this.sc = new JScrollPane(this.ta);

		this.setLayout(new BorderLayout());
		this.add(this.sc, BorderLayout.CENTER);
	}

	public void write(String s) {
		ta.append(s);
		Integer max = sc.getVerticalScrollBar().getMaximum();
		sc.getVerticalScrollBar().setValue(max);
		validate();
	}
}
