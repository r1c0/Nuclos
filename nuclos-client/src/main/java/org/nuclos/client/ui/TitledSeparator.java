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

import java.awt.Dimension;

import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JSeparator;

/**
 * A titled (horizontal) separator.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version	01.00.00
 */

public class TitledSeparator extends JLayeredPane {
	private final JSeparator separator = new JSeparator();
	private final JLabel lab = new JLabel();
	private final int OFFSET_LABEL = 5;

	public TitledSeparator() {
		this(null);
	}

	public TitledSeparator(String sTitle) {
		super();
		this.setTitle(sTitle);

		this.add(this.lab);
		this.add(this.separator);

		this.lab.setOpaque(true);
	}

	public void setTitle(String sTitle) {
		this.lab.setText(" " + sTitle + " ");
	}

	public String getTitle() {
		if (this.lab != null)
			if (this.lab.getText() != null) 
		return this.lab.getText();
		
		return "";
	}
	
	public JLabel getJLabel() {
		return lab;
	}

	@Override
	public void doLayout() {
		this.lab.setLocation(OFFSET_LABEL, 0);
		this.lab.setSize(this.lab.getPreferredSize());
		this.separator.setLocation(0, this.lab.getHeight() / 2);
		this.separator.setSize(this.getWidth(), this.separator.getPreferredSize().height);
	}

	@Override
	public Dimension getMinimumSize() {
		return new Dimension(this.lab.getMinimumSize().width + 2 * OFFSET_LABEL, this.lab.getMinimumSize().height);
	}

	@Override
	public Dimension getPreferredSize() {
		return new Dimension(this.lab.getPreferredSize().width + 2 * OFFSET_LABEL, this.lab.getPreferredSize().height);
	}

	@Override
	public Dimension getMaximumSize() {
		return new Dimension(this.separator.getMaximumSize().width, this.lab.getPreferredSize().height);
	}

}  // class TitledSeparator
