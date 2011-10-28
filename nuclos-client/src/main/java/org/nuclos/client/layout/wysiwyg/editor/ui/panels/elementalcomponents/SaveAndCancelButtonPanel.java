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
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JPanel;

import org.nuclos.client.layout.wysiwyg.WYSIWYGStringsAndLabels.BUTTON_LABELS;
import org.nuclos.client.layout.wysiwyg.editor.util.InterfaceGuidelines;

/**
 * This Panel is used to add Save and Cancel Buttons.<br>
 * It may be extended by providing AdditionalButtons.<br>
 * It provides a interface class that has to be implemented:<br>
 * {@link SaveAndCancelButtonPanelControllable}
 * 
 * 
 * @see AddRemoveRowsFromPanel 
 * @see MovePanelUpAndDown
 * 
 * <br>
 * Created by Novabit Informationssysteme GmbH <br>
 * Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 * 
 * @author <a href="mailto:hartmut.beckschulze@novabit.de">hartmut.beckschulze</a>
 * @version 01.00.00
 */
public class SaveAndCancelButtonPanel extends JPanel {
	
	private JButton saveButton = null;
	private JButton cancelButton = null;
	
	private String strSave = BUTTON_LABELS.LABEL_SAVE;
	private String strCancel = BUTTON_LABELS.LABEL_CANCEL;

	private Component panel = null;
	
	private int saveButtonLocation = 1;
	private int cancelButtonLocation = 3;
	
	/**
	 * The Constructor
	 * @param backgroundColor the BackgroundColor for this Panel
	 * @param panel The Panel where the {@link SaveAndCancelButtonPanel} is put in (for getting the Actions)
	 * @param additionalButtons a List with some Buttons to be added
	 */
	public SaveAndCancelButtonPanel(Color backgroundColor, Component panel, List<AbstractButton> additionalButtons) {
		this.panel = panel;
		this.setBackground(backgroundColor);
		this.setOpaque(false);

		double[][] layout = {{InterfaceGuidelines.MARGIN_LEFT,TableLayout.PREFERRED,TableLayout.FILL, TableLayout.PREFERRED, InterfaceGuidelines.MARGIN_RIGHT}, 
				{InterfaceGuidelines.MARGIN_TOP, TableLayout.PREFERRED,InterfaceGuidelines.MARGIN_BOTTOM}};
		TableLayout tableLayout = new TableLayout(layout);
		this.setLayout(tableLayout);

		saveButton = new JButton(strSave);
		saveButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				saveAction();
			}
		});
		
		/**
		 * Adding provided Buttons
		 */
		if (additionalButtons != null) {
			for (AbstractButton button : additionalButtons) {
				tableLayout.insertColumn(saveButtonLocation + 1, InterfaceGuidelines.MARGIN_BETWEEN);
				tableLayout.insertColumn(saveButtonLocation + 1, TableLayout.PREFERRED);
				
				add(button, (saveButtonLocation + 1) + ",1");
				
				cancelButtonLocation = cancelButtonLocation + 2;
			}
		}

		cancelButton = new JButton(strCancel);
		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				cancelAction();
			}
		});
		this.add(saveButton, "1,1");
		this.add(cancelButton, cancelButtonLocation + ",1");
	}

	/**
	 * Performing the SaveAction registered in the Panel
	 * 
	 * @see SaveAndCancelButtonPanelControllable
	 */
	private final void saveAction() {
		if (panel instanceof SaveAndCancelButtonPanelControllable) {
			((SaveAndCancelButtonPanelControllable) panel).performSaveAction();
		}
	}

	/**
	 * Performing the CancelAction registerd int the Panel
	 * 
	 * @see SaveAndCancelButtonPanelControllable
	 */
	private final void cancelAction() {
		if (panel instanceof SaveAndCancelButtonPanelControllable) {
			((SaveAndCancelButtonPanelControllable) panel).performCancelAction();
		}
	}

	/**
	 * Interface that must be implemented to use {@link SaveAndCancelButtonPanel}.<br>
	 * 
	 * <br>
	 * Created by Novabit Informationssysteme GmbH <br>
	 * Please visit <a href="http://www.novabit.de">www.novabit.de</a>
	 * 
	 * @author <a href="mailto:hartmut.beckschulze@novabit.de">hartmut.beckschulze</a>
	 * @version 01.00.00
	 */
	public interface SaveAndCancelButtonPanelControllable {

		/**
		 * This Method must be implemented to start the Save Action
		 */
		public void performSaveAction();
		
		/**
		 * This Method must be implemented to start teh Cancel Action
		 */
		public void performCancelAction();
	}
	
	/**
	 * 
	 * @param enable
	 */
	public void setSaveButtonEnable(boolean enable) {
		this.saveButton.setEnabled(enable);
	}
}
