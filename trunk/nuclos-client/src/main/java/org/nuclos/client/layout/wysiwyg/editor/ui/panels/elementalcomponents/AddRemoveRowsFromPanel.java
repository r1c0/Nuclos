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
 * This Class provides Add and Remove Buttons.<br>
 * They are put into a Panel and control the Method defined by implementing {@link AddRemoveButtonControllable}.<br>
 * 
 * @see SaveAndCancelButtonPanel
 * @see MovePanelUpAndDown
 * 
 * <br>
 * Created by Novabit Informationssysteme GmbH <br>
 * Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 * 
 * @author <a href="mailto:hartmut.beckschulze@novabit.de">hartmut.beckschulze</a>
 * @version 01.00.00
 */
public class AddRemoveRowsFromPanel extends JPanel {

	private String path = "org/nuclos/client/layout/wysiwyg/editor/ui/panels/icons/";

	private Icon iconAdd = new ImageIcon(this.getClass().getClassLoader().getResource(path + "list-add.png"));
	private Icon iconRemove = new ImageIcon(this.getClass().getClassLoader().getResource(path + "list-remove.png"));

	private JButton removeButton = null;
	private JButton addButton = null;
	
	private Component panel = null;
	
	public static int VERTICAL = 1;
	public static int HORIZONTAL = 2;

	/**
	 * 
	 * @param backgroundColor the Background Color to set
	 * @param panel The Panel that is using this {@link AddRemoveRowsFromPanel}
	 */
	public AddRemoveRowsFromPanel(Color backgroundColor, Component panel) {
		this(backgroundColor, panel, VERTICAL);
	}
	
	/**
	 * 
	 * @param backgroundColor the Background Color to set
	 * @param panel The Panel that is using this {@link AddRemoveRowsFromPanel}
	 * @param orientation the Orientation ( {@link #HORIZONTAL} or {@link #VERTICAL}
	 */
	public AddRemoveRowsFromPanel(Color backgroundColor, Component panel, int orientation) {
		this.panel = panel;
		this.setBackground(backgroundColor);

		double[][] layout;
		
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

		removeButton = new JButton(iconRemove);
		removeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				removeAction();
			}
		});
		this.add(removeButton, "0,0");

		addButton = new JButton(iconAdd);
		addButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				addAction();
			}
		});

		if (orientation == VERTICAL) {
			this.add(addButton, "0,2");
		}
		else {
			this.add(addButton, "2,0");
		}
	}

	/**
	 * Performs the add Action on the Panel
	 */
	private final void addAction() {
		if (panel instanceof AddRemoveButtonControllable){
			((AddRemoveButtonControllable)panel).performAddAction();
		}
	}

	/**
	 * Performs the remove Action on the Panel
	 */
	private final void removeAction() {
		if (panel instanceof AddRemoveButtonControllable){
			((AddRemoveButtonControllable)panel).performRemoveAction();
		}
	}

	/**
	 * Disable the add Button
	 */
	public void disableAddButton() {
		this.addButton.setEnabled(false);
	}

	/**
	 * Enable the add Button
	 */
	public void enableAddButton() {
		this.addButton.setEnabled(true);

	}

	/**
	 * Disable the delete Button
	 */
	public void disableDeleteButton() {
		this.removeButton.setEnabled(false);
	}

	/**
	 * Enable the delete Button
	 */
	public void enableDeleteButton() {
		this.removeButton.setEnabled(true);
	}

	/**
	 * @return true if the add Button is enabled, false if disabled
	 */
	public boolean isAddButtonEnabled() {
		return this.addButton.isEnabled();
	}
	
	/**
	 * The Interface that must be implemented to be able to perform Actions.
	 * 
	 * 
	 * <br>
	 * Created by Novabit Informationssysteme GmbH <br>
	 * Please visit <a href="http://www.novabit.de">www.novabit.de</a>
	 * 
	 * @author <a href="mailto:hartmut.beckschulze@novabit.de">hartmut.beckschulze</a>
	 * @version 01.00.00
	 */
	public interface AddRemoveButtonControllable{
		
		/**
		 * The Action that should be performed on Add
		 */
		public void performAddAction();
		
		/**
		 * The Action that should be performed on Remove
		 */
		public void performRemoveAction();
	}

}
