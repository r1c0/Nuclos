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
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import org.nuclos.common2.CommonLocaleDelegate;

/**
 * Panel for selecting objects from a list of available objects.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version	01.00.00
 */

public class DefaultSelectObjectsPanel<T> extends SelectObjectsPanel<T> {

	private static final long serialVersionUID = 1L;
	
	protected final JComponent header;

	public DefaultSelectObjectsPanel() {
		this(null);
	}
	
	public DefaultSelectObjectsPanel(JComponent header) {
		this.header = header;
		init();
	}

	protected void init() {
		this.pnlMain.setLayout(new GridBagLayout());
		this.pnlAvailableObjects.setLayout(new BorderLayout());
		this.labAvailableColumns.setText(CommonLocaleDelegate.getMessage("DefaultSelectObjectsPanel.6","Verf\u00fcgbar"));
		this.pnlMiddleButtons.setLayout(new GridBagLayout());
		this.pnlSelectedColumns.setLayout(new BorderLayout());
		this.pnlRightButtons.setLayout(new GridBagLayout());
		this.labSelectedColumns.setText(CommonLocaleDelegate.getMessage("DefaultSelectObjectsPanel.1","Ausgew\u00e4hlt"));
		this.pnlMain.setBorder(BorderFactory.createEmptyBorder(5, 5, 10, 5));
		this.scrlpnAvailableColumns.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		this.scrlpnAvailableColumns.setPreferredSize(new Dimension(200, 300));
		this.btnRight.setEnabled(false);
		this.btnLeft.setEnabled(false);
		this.btnUp.setEnabled(false);
		this.btnDown.setEnabled(false);
		this.scrlpnSelectedColumns.setPreferredSize(new Dimension(200, 300));
		if (header != null) {
			// add(header, BorderLayout.NORTH);
			final JTabbedPane tabbed = new JTabbedPane();
			tabbed.add(CommonLocaleDelegate.getMessage("select.panel.column", "Spalten"), pnlMain);
			final JPanel panel = new JPanel();
			panel.setLayout(new BorderLayout());
			panel.add(header, BorderLayout.NORTH);
			tabbed.add(CommonLocaleDelegate.getMessage("select.panel.pivot", "Pivot"), panel);
			add(tabbed, BorderLayout.CENTER);
		}
		else {
			this.add(pnlMain, BorderLayout.CENTER);
		}
		this.pnlMain.add(pnlTitleAvailableObjects, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
																		  , GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		this.pnlMain.add(pnlAvailableObjects, new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0
																	 , GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		this.pnlTitleAvailableObjects.add(labAvailableColumns, null);
		this.pnlMain.add(pnlMiddleButtons, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0
																  , GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 10, 0, 10), 0, 0));
		this.pnlMiddleButtons.add(btnRight, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
																   , GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(5, 0, 5, 0), 0, 0));
		this.pnlMiddleButtons.add(btnLeft, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
																  , GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(5, 0, 5, 0), 0, 0));
		this.pnlMain.add(pnlSelectedColumns, new GridBagConstraints(2, 1, 1, 1, 1.0, 1.0
																	, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		this.pnlMain.add(pnlRightButtons, new GridBagConstraints(3, 1, 1, 1, 0.0, 0.0
																 , GridBagConstraints.CENTER, GridBagConstraints.VERTICAL, new Insets(0, 10, 0, 0), 0, 0));
		this.pnlRightButtons.add(btnUp, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
															   , GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 0, 5, 0), 0, 0));
		this.pnlRightButtons.add(btnDown, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
																 , GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 0, 5, 0), 0, 0));
		this.pnlMain.add(pnlTitleSelectedColumns, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0
																		 , GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		this.pnlTitleSelectedColumns.add(labSelectedColumns, null);
		this.pnlAvailableObjects.add(scrlpnAvailableColumns, BorderLayout.CENTER);
		this.scrlpnAvailableColumns.getViewport().add(jlstAvailableColumns, null);
		this.pnlSelectedColumns.add(scrlpnSelectedColumns, BorderLayout.CENTER);
		this.scrlpnSelectedColumns.getViewport().add(jlstSelectedColumns, null);

		this.btnLeft.setIcon(Icons.getInstance().getIconLeft16());
		this.btnRight.setIcon(Icons.getInstance().getIconRight16());
		this.btnUp.setIcon(Icons.getInstance().getIconUp16());
		this.btnDown.setIcon(Icons.getInstance().getIconDown16());

		this.btnLeft.setToolTipText(CommonLocaleDelegate.getMessage("DefaultSelectObjectsPanel.3","Markierte Objekte nicht ausw\u00e4hlen"));
		this.btnRight.setToolTipText(CommonLocaleDelegate.getMessage("DefaultSelectObjectsPanel.2","Markierte Objekte ausw\u00e4hlen"));
		this.btnUp.setToolTipText(CommonLocaleDelegate.getMessage("DefaultSelectObjectsPanel.4","Markiertes Objekt nach oben verschieben"));
		this.btnDown.setToolTipText(CommonLocaleDelegate.getMessage("DefaultSelectObjectsPanel.5","Markiertes Objekt nach unten verschieben"));
		this.btnUp.setVisible(false);
		this.btnDown.setVisible(false);
	}
	
	protected final JComponent getHeaderComponent() {
		return header;
	}

}  // class DefaultSelectObjectsPanel
