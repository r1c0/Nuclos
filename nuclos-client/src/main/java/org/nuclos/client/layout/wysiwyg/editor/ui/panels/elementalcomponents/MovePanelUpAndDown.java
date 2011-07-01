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
package org.nuclos.client.layout.wysiwyg.editor.ui.panels.elementalcomponents;

import info.clearthought.layout.TableLayout;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;

import org.nuclos.client.layout.wysiwyg.editor.util.InterfaceGuidelines;

/**
 * This Class provides two Buttons for moving something up or down.<br>
 * It provides a Interface {@link MovePanelUpAndDownControllable}.<br>
 * 
 * @see SaveAndCancelButtonPanel 
 * @see AddRemoveRowsFromPanel
 * 
 * <br>
 * Created by Novabit Informationssysteme GmbH <br>
 * Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 * 
 * @author <a href="mailto:hartmut.beckschulze@novabit.de">hartmut.beckschulze</a>
 * @version 01.00.00
 */
@SuppressWarnings("serial")
public class MovePanelUpAndDown extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String path = "org/nuclos/client/layout/wysiwyg/editor/ui/panels/icons/";

	private Icon iconUp = new ImageIcon(this.getClass().getClassLoader().getResource(path + "go-up.png"));
	private Icon iconDown = new ImageIcon(this.getClass().getClassLoader().getResource(path + "go-down.png"));

	private JButton moveUpButton = null;
	private JButton moveDownButton = null;

	private Component panel = null;
	
	public static int VERTICAL = 1;
	public static int HORIZONTAL = 2;

	/**
	 * Constructor
	 * @param backgroundColor
	 * @param panel
	 * @param orientation the Orientation of the Buttons {@link #HORIZONTAL} or {@link #VERTICAL}
	 */
	public MovePanelUpAndDown(Color backgroundColor, Component panel, int orientation) {
		this.panel = panel;
		this.setBackground(backgroundColor);
		
		double[][] layout;
		
		/** creating the layoutdefinition, horizontal or vertical */
		if (orientation == VERTICAL) {
			layout = new double[][] {
				{
					TableLayout.PREFERRED
				}, 
				{
					TableLayout.PREFERRED, 
					InterfaceGuidelines.MARGIN_BETWEEN, 
					TableLayout.PREFERRED
				}
			};
		}
		else {
			layout = new double[][] {
					{
						TableLayout.PREFERRED, 
						InterfaceGuidelines.MARGIN_BETWEEN, 
						TableLayout.PREFERRED
					}, 
					{
						TableLayout.PREFERRED
					}
				};
		}
				
		this.setLayout(new TableLayout(layout));

		moveUpButton = new JButton(iconUp);
		moveUpButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				moveUpAction();
			}
		});
		
		this.add(moveUpButton, "0,0");

		moveDownButton = new JButton(iconDown);
		moveDownButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				moveDownAction();
			}
		});
		
		if (orientation == VERTICAL) {
			this.add(moveDownButton, "0,2");
		}
		else {
			this.add(moveDownButton, "2,0");
		}
		
	}

	/**
	 * This Method performs the Move Up Action by calling the performMoveUpAction from the Panel which is {@link MovePanelUpAndDownControllable}
	 */
	private final void moveUpAction() {
		if (panel instanceof MovePanelUpAndDownControllable) {
			((MovePanelUpAndDownControllable) panel).performMoveUpAction();
		}
	}

	/**
	 * This Method performs the Move Down Action by calling the performMoveDownAction from the Panel which is {@link MovePanelUpAndDownControllable}
	 */
	private final void moveDownAction() {
		if (panel instanceof MovePanelUpAndDownControllable) {
			((MovePanelUpAndDownControllable) panel).performMoveDownAction();
		}
	}

	/**
	 * Disable Move Down
	 */
	public void disableMoveDownButton() {
		this.moveDownButton.setEnabled(false);
	}

	/**
	 * Enable Move Down
	 */
	public void enableMoveDownButton() {
		this.moveDownButton.setEnabled(true);
	}

	/**
	 * Disable Move Up
	 */
	public void disableMoveUpButton() {
		this.moveUpButton.setEnabled(false);
	}

	/**
	 * Enable Move Up
	 */
	public void enableMoveUpButton() {
		this.moveUpButton.setEnabled(true);
	}

	/**
	 * This Interface must be implemented to make a Panel controllable.
	 * 
	 * 
	 * <br>
	 * Created by Novabit Informationssysteme GmbH <br>
	 * Please visit <a href="http://www.novabit.de">www.novabit.de</a>
	 * 
	 * @author <a href="mailto:hartmut.beckschulze@novabit.de">hartmut.beckschulze</a>
	 * @version 01.00.00
	 */
	public interface MovePanelUpAndDownControllable {

		/**
		 * This Method defines the Move Up Action
		 */
		public void performMoveUpAction();
		
		/**
		 * This Method defines the Move Down Action
		 */
		public void performMoveDownAction();
	}
}
