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

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 * Base class for panels, in which data can be moved from one list to another to choose from a selection
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 * @author	<a href="mailto:uwe.allner@novabit.de">uwe.allner</a>
 * @version	01.00.00
 */
public abstract class SelectObjectsPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	
	/*
	 * TODO: Make these protected (Thomas Pasch). 
	 */
	public final JButton btnRight = new JButton();
	public final JButton btnLeft = new JButton();
	public final JButton btnUp = new JButton();
	public final JButton btnDown = new JButton();
	
	protected final JLabel labAvailableColumns = new JLabel();
	protected final JLabel labSelectedColumns = new JLabel();
	protected final JPanel pnlMain = new JPanel();
	protected final JPanel pnlTitleAvailableObjects = new JPanel();
	protected final JPanel pnlAvailableObjects = new JPanel();
	protected final JPanel pnlMiddleButtons = new JPanel();
	protected final JPanel pnlSelectedColumns = new JPanel();
	protected final JPanel pnlRightButtons = new JPanel();
	protected final JPanel pnlTitleSelectedColumns = new JPanel();
	protected final JScrollPane scrlpnAvailableColumns = new JScrollPane();
	protected final JScrollPane scrlpnSelectedColumns = new JScrollPane();
	protected final JList jlstAvailableColumns = newList();
	protected final JList jlstSelectedColumns = newList();

	protected SelectObjectsPanel() {
		super(new BorderLayout());
	}

	/**
	 * @return the <code>JList</code> containing available columns
	 */
	public JList getJListAvailableObjects() {
		return this.jlstAvailableColumns;
	}

	/**
	 * @return the <code>JList</code> containing selected columns
	 */
	public JList getJListSelectedObjects() {
		return this.jlstSelectedColumns;
	}

	protected JList newList() {
		return new JList();
	}
}
