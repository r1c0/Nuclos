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

import java.awt.Container;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.commons.lang.StringUtils;
import org.nuclos.client.layout.wysiwyg.WYSIWYGStringsAndLabels.BUTTON_LABELS;
import org.nuclos.client.layout.wysiwyg.WYSIWYGStringsAndLabels.TABLELAYOUT_PANEL;
import org.nuclos.client.layout.wysiwyg.editor.util.InterfaceGuidelines;
import org.nuclos.client.layout.wysiwyg.editor.util.valueobjects.TableLayoutPanel;
import org.nuclos.common2.CommonLocaleDelegate;

/**
 * This Class displays a Dialog for picking Metainformation.
 * 
 * It contains a textfield for typing and filters the list for the fitting metainformation.
 * Cursor up and down selects the next or previous entry in the list.
 * Enter is like clicking on the Save Button and ESC does cancel.
 * NUCLEUSINT-465
 * @author hartmut.beckschulze
 *
 */
public class WYSIWYGMetaInformationPicker extends JDialog {
	
	private static final long serialVersionUID = 1695463517589418674L;

	private double[][] layoutDefinition = {
			{InterfaceGuidelines.MARGIN_LEFT,TableLayout.FILL, InterfaceGuidelines.MARGIN_BETWEEN, TableLayout.FILL,  InterfaceGuidelines.MARGIN_RIGHT},
			{InterfaceGuidelines.MARGIN_TOP, TableLayout.PREFERRED, TableLayout.PREFERRED, InterfaceGuidelines.MARGIN_BETWEEN, TableLayout.FILL, InterfaceGuidelines.MARGIN_BETWEEN, TableLayout.PREFERRED, InterfaceGuidelines.MARGIN_BOTTOM}};
	
	private JCheckBox freeExpr;
	
	private JList itemList = null;
	
	private DefaultListModel listModel = new DefaultListModel();
	
	private List<String> originalValues = null;

	protected JTextField filter;
	
	private String selectedEntity = null;
	
	/**
	 * 
	 * @param values
	 * @param parent
	 */
	private WYSIWYGMetaInformationPicker(Window owner, List<String> values, TableLayoutPanel parent){
		super(owner);
		Container contentPane = this.getContentPane();
		contentPane.setLayout(new TableLayout(layoutDefinition));
		
		originalValues = values;
		
		filter = new JTextField();
		
		/** the keybinding*/
		filter.addKeyListener(new KeyListener(){

			@Override
			public void keyPressed(KeyEvent e) {}

			@Override
			public void keyReleased(KeyEvent e) {
				/** up and down cursor for selecting the next or previous entry */
				if (e.getKeyCode() == KeyEvent.VK_DOWN || e.getKeyCode() == KeyEvent.VK_UP){
					int selectedItem = itemList.getSelectedIndex();
					if (e.getKeyCode() == KeyEvent.VK_UP)
						selectedItem--;
					else if (e.getKeyCode() == KeyEvent.VK_DOWN)
						selectedItem++;
					
					if (selectedItem < 0){
						selectedItem = listModel.getSize() - 1;
					} else if (selectedItem == listModel.getSize()) {
						selectedItem = 0;
					}
					
					itemList.setSelectedIndex(selectedItem);
					itemList.ensureIndexIsVisible(selectedItem);
				} else if(e.getKeyCode() == KeyEvent.VK_ENTER || e.getKeyCode() == KeyEvent.VK_ESCAPE){
					/** enter for saving, esc for cancel */
					if(e.getKeyCode() == KeyEvent.VK_ESCAPE)
						selectedEntity = null;
					dispose();
					
				}else {
					/** every other key is used for filtering the entries */
					filterItems(WYSIWYGMetaInformationPicker.this.filter.getText());
				}
			}

			@Override
			public void keyTyped(KeyEvent e) {}
			
		});
		/** request focus in the filter window */
		filter.requestFocus();
		
		this.add(filter, new TableLayoutConstraints(1, 1, 3, 1));
		itemList = new JList(listModel);
		itemList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		itemList.addListSelectionListener(new ListSelectionListener(){

			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (itemList.getSelectedValue() != null)
					selectedEntity = (String) itemList.getSelectedValue();
			}
			
		});
		freeExpr = new JCheckBox(
				CommonLocaleDelegate.getMessageFromResource("wysiwg.metainformation.picker.freeexpr"));
		this.add(freeExpr, new TableLayoutConstraints(1, 2, 3, 2));
		freeExpr.setVisible(true);
		
		// double click on item to select
		itemList.addMouseListener(new MouseListener(){

			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1){
					performSaveAction();
				}
			}

			@Override
			public void mouseEntered(MouseEvent e) {}

			@Override
			public void mouseExited(MouseEvent e) {}

			@Override
			public void mousePressed(MouseEvent e) {}

			@Override
			public void mouseReleased(MouseEvent e) {}
			
		});
		
		filterItems(null);
		
		JScrollPane scrollpane = new JScrollPane(itemList);
		
		this.add(scrollpane, new TableLayoutConstraints(1, 4, 3, 4));
		
		JButton apply = new JButton(BUTTON_LABELS.LABEL_APPLY);
		apply.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				performSaveAction();
			}
			
		});
		this.add(apply, new TableLayoutConstraints(1,6));
		
		JButton cancel = new JButton(BUTTON_LABELS.LABEL_CANCEL);
		cancel.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				performCancelAction();
			}
			
		});
		
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				performCancelAction();
			}});
		
		this.add(cancel, new TableLayoutConstraints(3,6));
		
		this.setBounds(this.getBounds().x, this.getBounds().y, 300, 400);
		this.setTitle(TABLELAYOUT_PANEL.SELECT_FIELD_FOR_METAINFORMATION);
		this.setModal(true);
	}
	
	public String getSelectedEntity() {
		if (StringUtils.isEmpty(selectedEntity) && freeExpr.isSelected()) {
			selectedEntity = filter.getText();
		}
		return selectedEntity;
	}
		
	/**
	 * This Method displays the Dialog
	 * @param wysiwygLayoutEditorPanel 
	 * @param values the List of valid Entitys
	 * @param parent 
	 * @return the picked entity - is null if canceled
	 */
	public static String showPickDialog(WYSIWYGLayoutEditorPanel editorPanel, List<String> values, TableLayoutPanel parent){
		WYSIWYGMetaInformationPicker picker;
		picker = new WYSIWYGMetaInformationPicker(SwingUtilities.getWindowAncestor(editorPanel), values, parent);
		picker.pack();
		picker.setLocationRelativeTo(parent);
		picker.setVisible(true);
		return picker.getSelectedEntity();
	}
	
	/**
	 * This Method filters the the list of entitys
	 * @param filterText
	 */
	private void filterItems(String filterText){
		listModel.removeAllElements();
		if (filterText == null){
			/** no text entered, display all */
			for (String item : originalValues) {
				listModel.addElement(item);
			}
		} else {
			/** filter the entrys by the given text (everything to lowercase) */
			for (String item : originalValues) {
				if (item.toLowerCase().indexOf(filterText.toLowerCase()) != -1){
					listModel.addElement(item);
				}
			}
		}
		
		/** set the first item selected */
		if (listModel.getSize() > 0) {
			itemList.setSelectedIndex(0);
		}
		selectedEntity = (String) itemList.getSelectedValue();
	}

	/**
	 * This Method performs the Cancel
	 */
	public void performCancelAction() {
		selectedEntity = null;
		this.dispose();
	}

	/**
	 * Performs the Save Action
	 */
	public void performSaveAction() {
		this.dispose();
	}

}
