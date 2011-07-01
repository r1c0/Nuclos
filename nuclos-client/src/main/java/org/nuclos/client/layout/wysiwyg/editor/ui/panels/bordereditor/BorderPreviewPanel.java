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
package org.nuclos.client.layout.wysiwyg.editor.ui.panels.bordereditor;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstraints;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.Border;

import org.nuclos.client.layout.wysiwyg.editor.util.InterfaceGuidelines;

/**
 * This class creates a Preview on the Borders created.
 * 
 * 
 * <br>
 * Created by Novabit Informationssysteme GmbH <br>
 * Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 * 
 * @author <a href="mailto:hartmut.beckschulze@novabit.de">hartmut.beckschulze</a>
 * @version 01.00.00
 */
@SuppressWarnings("serial")
public class BorderPreviewPanel extends JPanel {

	private JTextField toDecorate = null;

	public static int PREVIEW_BORDER_SIZE = 200;

	private Color backgroundColorExample = Color.WHITE;

	/**
	 * Constructor for the preview Panel
	 */
	public BorderPreviewPanel() {
		this.toDecorate = new JTextField();
		this.toDecorate.setEditable(false);
		this.toDecorate.setPreferredSize(new Dimension(PREVIEW_BORDER_SIZE, PREVIEW_BORDER_SIZE));
		this.toDecorate.setBackground(backgroundColorExample);
		double[][] layout = {{InterfaceGuidelines.MARGIN_LEFT, TableLayout.FILL, InterfaceGuidelines.MARGIN_RIGHT}, {InterfaceGuidelines.MARGIN_TOP, TableLayout.FILL, InterfaceGuidelines.MARGIN_BOTTOM}};
		this.setLayout(new TableLayout(layout));

		TableLayoutConstraints constraint = new TableLayoutConstraints(1, 1, 1, 1, TableLayout.CENTER, TableLayout.CENTER);
		this.add(toDecorate, constraint);
	}

	/**
	 * This Method applies the Borders to the Preview Component
	 * @param bordersForComponent the Border(s) to set 
	 */
	public void setBordersForComponent(Border bordersForComponent) {
		toDecorate.setBorder(null);
		if (bordersForComponent != null)
			createBorder(toDecorate, bordersForComponent);

		this.updateUI();
	}

	/**
	 * method is a copy of private void addBorder(final Border borderNew) { in
	 * the layoutml parser
	 * @param c
	 * @param newBorder
	 */
	private void createBorder(JComponent c, Border newBorder) {
		c.setBorder(newBorder);
	}

}
