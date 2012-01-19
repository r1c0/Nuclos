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
package org.nuclos.client.layout.wysiwyg.editor.ui.panels;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstraints;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;

import javax.swing.AbstractButton;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;

import org.nuclos.client.layout.wysiwyg.WYSIWYGStringsAndLabels.INITIAL_FOCUS_EDITOR;
import org.nuclos.client.layout.wysiwyg.editor.ui.panels.WYSIWYGEditorsToolbar.WYSIWYGToolbarAttachable;
import org.nuclos.client.layout.wysiwyg.editor.ui.panels.elementalcomponents.SaveAndCancelButtonPanel;
import org.nuclos.client.layout.wysiwyg.editor.ui.panels.elementalcomponents.SaveAndCancelButtonPanel.SaveAndCancelButtonPanelControllable;
import org.nuclos.client.layout.wysiwyg.editor.util.InterfaceGuidelines;
import org.nuclos.client.layout.wysiwyg.editor.util.valueobjects.WYSIWYGInitialFocusComponent;

/**
 * This Class provides a GUI for setting the {@link WYSIWYGInitialFocusComponent}.<br>
 * It is attached to the {@link WYSIWYGEditorsToolbar}.<br>
 * 
 * 
 * <br>
 * Created by Novabit Informationssysteme GmbH <br>
 * Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 * 
 * @author <a href="mailto:thomas.schiffmann@novabit.de">thomas.schiffmann</a>
 * @version 01.00.00
 */
public class WYSIWYGInitialFocusComponentEditor implements WYSIWYGToolbarAttachable {
	
	private String path = "org/nuclos/client/layout/wysiwyg/editor/ui/panels/icons/";

	private Icon iconInitialFocus = new ImageIcon(this.getClass().getClassLoader().getResource(path + "initial-focus-component.png"));

	private WYSIWYGLayoutEditorPanel editorPanel = null;
	
	private JButton[] toolbarItems = null;
	
	/**
	 * @param e the {@link WYSIWYGLayoutEditorPanel}
	 */
	public WYSIWYGInitialFocusComponentEditor(WYSIWYGLayoutEditorPanel e) {
		this.editorPanel = e;
		
		JButton button = new JButton(iconInitialFocus);
		button.setToolTipText(INITIAL_FOCUS_EDITOR.TOOLBAR_TOOLTIP);
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				new WYSIWYGInitialFocusComponentDialog();
			}
		});
		toolbarItems = new JButton[] {button};
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.nuclos.client.layout.wysiwyg.editor.ui.panels.WYSIWYGEditorsToolbar.WYSIWYGToolbarAttachable#getToolbarItems()
	 */
	@Override
	public JComponent[] getToolbarItems() {
		return toolbarItems;
	}

	/**
	 * This Class provides the Editordialog for {@link WYSIWYGInitialFocusComponentEditor}.<br>
	 * Its called from the Click action in {@link WYSIWYGInitialFocusComponentEditor#WYSIWYGInitialFocusComponentEditor(WYSIWYGLayoutEditorPanel)}
	 * 
	 * 
	 * <br>
	 * Created by Novabit Informationssysteme GmbH <br>
	 * Please visit <a href="http://www.novabit.de">www.novabit.de</a>
	 * 
	 * @author <a href="mailto:hartmut.beckschulze@novabit.de">hartmut.beckschulze</a>
	 * @version 01.00.00
	 */
	private class WYSIWYGInitialFocusComponentDialog extends JDialog implements SaveAndCancelButtonPanelControllable {
		
		private JComboBox comboBoxEntity;
		private JComboBox comboBoxName;
		
		/** the layoutdefinition for the Dialog */
		private double[][] layout = new double[][]{
				{
					InterfaceGuidelines.MARGIN_LEFT,
					TableLayout.PREFERRED,
					InterfaceGuidelines.MARGIN_BETWEEN,
					TableLayout.FILL,
					InterfaceGuidelines.MARGIN_RIGHT
				},
				{
					InterfaceGuidelines.MARGIN_TOP,
					TableLayout.PREFERRED,
					InterfaceGuidelines.MARGIN_BETWEEN,
					TableLayout.PREFERRED,
					InterfaceGuidelines.MARGIN_BETWEEN,
					TableLayout.PREFERRED,
					InterfaceGuidelines.MARGIN_BOTTOM
				}
			};
		
		private int width = 400;
		private int height = 150;
		
		/**
		 * Constructor
		 */
		private WYSIWYGInitialFocusComponentDialog() {
			this.setTitle(INITIAL_FOCUS_EDITOR.TITLE_INITIAL_FOCUS_EDITOR);
			
			this.setLayout(new TableLayout(layout));
			
			JLabel lblAttribute = new JLabel(INITIAL_FOCUS_EDITOR.LABEL_ATTRIBUTE);
			this.add(lblAttribute, "1,3");
			
			this.comboBoxName = new JComboBox();
			this.add(comboBoxName, "3,3");
			
			this.comboBoxEntity = new JComboBox();
			
			/** handling the change of the entity to find subentity (case Subform/ Subform Column */
			this.comboBoxEntity.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {
					if (e.getStateChange() == ItemEvent.SELECTED) {
						if (comboBoxEntity.getSelectedItem().equals("")) {
							comboBoxName.removeAllItems();
							for (String name : WYSIWYGInitialFocusComponentEditor.this.editorPanel.getCollectableComponents()) {
								comboBoxName.addItem(name);
							}
						}
						else {
							comboBoxName.removeAllItems();
							for (String name : WYSIWYGInitialFocusComponentEditor.this.editorPanel.getMetaInformation().getSubFormColumns((String)comboBoxEntity.getSelectedItem())) {
								comboBoxName.addItem(name);
							}
						}
					}
				}
			});
			
			this.comboBoxEntity.addItem("");
			for (String entity : WYSIWYGInitialFocusComponentEditor.this.editorPanel.getSubFormEntityNames()) {
				comboBoxEntity.addItem(entity);
			}
			
			JLabel lblEntity  = new JLabel(INITIAL_FOCUS_EDITOR.LABEL_ENTITY);
			this.add(lblEntity , "1,1");
			this.add(comboBoxEntity, "3,1");
			
			/** restoring values that may be set */
			if (WYSIWYGInitialFocusComponentEditor.this.editorPanel.getInitialFocusComponent() != null) {
				this.comboBoxEntity.setSelectedItem(WYSIWYGInitialFocusComponentEditor.this.editorPanel.getInitialFocusComponent().getEntity());
				this.comboBoxName.setSelectedItem(WYSIWYGInitialFocusComponentEditor.this.editorPanel.getInitialFocusComponent().getName());
			}
			
			JButton remove = new JButton(INITIAL_FOCUS_EDITOR.LABEL_BUTTON_REMOVE_INITIAL_FOCUS);
			remove.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					WYSIWYGInitialFocusComponentEditor.this.editorPanel.setInitialFocusComponent(null);
					WYSIWYGInitialFocusComponentDialog.this.setVisible(false);
				}
			});
			
			ArrayList<AbstractButton> additional = new ArrayList<AbstractButton>();
			additional.add(remove);
			
			this.add(new SaveAndCancelButtonPanel(this.getBackground(), this, additional), new TableLayoutConstraints(0,5,4,5));
			
			Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
			int x = (screenSize.width - width) / 2;
			int y = (screenSize.height - height) / 2;
			this.setBounds(x, y, width, height);
			this.setResizable(true);
			this.setModal(true);
			this.setVisible(true);
		}
		
		/*
		 * (non-Javadoc)
		 * @see org.nuclos.client.layout.wysiwyg.editor.ui.panels.elementalcomponents.SaveAndCancelButtonPanel.SaveAndCancelButtonPanelControllable#performCancelAction()
		 */
		@Override
		public void performCancelAction() {
			this.setVisible(false);
		}

		/*
		 * (non-Javadoc)
		 * @see org.nuclos.client.layout.wysiwyg.editor.ui.panels.elementalcomponents.SaveAndCancelButtonPanel.SaveAndCancelButtonPanelControllable#performSaveAction()
		 */
		@Override
		public void performSaveAction() {
			WYSIWYGInitialFocusComponent initFocus = new WYSIWYGInitialFocusComponent((String)comboBoxEntity.getSelectedItem(), (String)comboBoxName.getSelectedItem());
			WYSIWYGInitialFocusComponentEditor.this.editorPanel.setInitialFocusComponent(initFocus);
			this.setVisible(false);
		}
	}

}
