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
package org.nuclos.client.wizard;

import static org.nuclos.common2.CommonLocaleDelegate.getMessage;
import info.clearthought.layout.TableLayout;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

import org.nuclos.client.wizard.model.DataTyp;

public class DataTypDialog extends JDialog{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	DataTyp dataTyp;
	
	JLabel lbBaseDatatyp;
	JComboBox cbxBaseDatatyp;
	JLabel lbDatatyp;
	JTextField tfDatatyp;
	JLabel lbFieldWidth;
	JTextField tfFieldWidth;
	JLabel lbFieldPrecision;
	JTextField tfFieldPrecision;
	JLabel lbInputFormat;
	JTextField tfInputFormat;
	JLabel lbOutputFormat;
	JTextField tfOutputFormat;
	List<DataTyp> lstDataTyp;
	
	JLabel lbInfo;
	
	JButton btnOK;
	JButton btnCancel;
	
	boolean blnOK;
	
	
	public DataTypDialog (Dialog owner, String title, boolean modal) {
		super(owner, title, modal);
		this.init();
	}
	
	public DataTypDialog(JFrame owner, String title, boolean modal) {
		super(owner, title, modal);
		this.init();
	}
	
	protected void init() {
		double size [][] = {{10,130,130, 30, TableLayout.FILL}, {10, 20, 20,20,20,20,20,20, TableLayout.FILL}};
		
		TableLayout layout = new TableLayout(size);
		layout.setVGap(3);
		layout.setHGap(5);
		this.setLayout(layout);
		
		lbBaseDatatyp = new JLabel(getMessage("wizard.datatype.1", "Grunddatentyp")+": ");
		lstDataTyp = DataTyp.getAllDataTyps();
		cbxBaseDatatyp = new JComboBox(lstDataTyp.toArray());
		lbDatatyp = new JLabel(getMessage("wizard.datatype.2", "Datentyp-Bezeichnung")+": ");
		tfDatatyp = new JTextField();
		lbFieldWidth = new JLabel(getMessage("wizard.datatype.3", "Feldbreite")+": ");
		tfFieldWidth = new JTextField();
		tfFieldWidth.setHorizontalAlignment(SwingConstants.RIGHT);
		
		tfDatatyp.getDocument().addDocumentListener(new DocumentListener() {
			
			@Override
			public void removeUpdate(DocumentEvent e) {
				doSomeWork(e);				
			}
			
			@Override
			public void insertUpdate(DocumentEvent e) {
				doSomeWork(e);
			}
			
			@Override
			public void changedUpdate(DocumentEvent e) {
				doSomeWork(e);
			}
			
			public void doSomeWork(DocumentEvent e) {
				 try {
					String sText = e.getDocument().getText(0, e.getDocument().getLength());
					for(DataTyp typ : lstDataTyp) {
						if(typ.getName().equals(sText)) {
							lbInfo.setText(getMessage("wizard.datatype.10", "Datentyp existiert schon"));
							btnOK.setEnabled(false);
							break;
						}
						else {
							lbInfo.setText("");
							btnOK.setEnabled(true);
						}
					}
				}
				catch(BadLocationException e1) {
				}
			}
		});
		
		tfFieldWidth.setDocument(new PlainDocument() {
			
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void insertString(int offset, String str, AttributeSet a) throws BadLocationException {
				if (str.matches("[0-9]")) {
					super.insertString(offset, str, a);					
				}			
			}
					
		});
		lbFieldPrecision = new JLabel(getMessage("wizard.datatype.4", "Nachkommastellen")+": ");
		tfFieldPrecision = new JTextField();
		tfFieldPrecision.setHorizontalAlignment(SwingConstants.RIGHT);
		tfFieldPrecision.setDocument(new PlainDocument() {
			
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void insertString(int offset, String str, AttributeSet a) throws BadLocationException {
				DataTyp typ =  (DataTyp)cbxBaseDatatyp.getSelectedItem();
				if(typ.getName().equals("Text")) {
					return;
				}
				if (str.matches("[0-9]")) {
					super.insertString(offset, str, a);					
				}			
			}
					
		});
		lbInputFormat = new JLabel(getMessage("wizard.datatype.5", "Eingabeformat")+": ");
		tfInputFormat = new JTextField();
		lbOutputFormat = new JLabel(getMessage("wizard.datatype.6", "Ausgabeformat")+": ");
		tfOutputFormat = new JTextField();
		
		btnOK = new JButton(getMessage("wizard.datatype.7", "\u00dcbernehmen"));
		btnOK.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				DataTyp base = (DataTyp)cbxBaseDatatyp.getSelectedItem();
				String newName = tfDatatyp.getText();
				if(newName.length() < 1) {
					lbInfo.setText(getMessage("wizard.datatype.8", "Bitte geben Sie einen Namen ein"));
					return;
				}
				String sFieldPrecision = tfFieldPrecision.getText();
				if(sFieldPrecision.length() < 1) {
					sFieldPrecision = "0";
				}
				String sFieldWidth = tfFieldWidth.getText();
				if(sFieldWidth.length() < 1) {
					sFieldWidth = "0";
				}
				
				dataTyp = new DataTyp(newName, tfInputFormat.getText(), tfOutputFormat.getText(),
					base.getDatabaseTyp(), Integer.parseInt(sFieldWidth), Integer.parseInt(sFieldPrecision), 
					base.getJavaType());			
				blnOK = true;
				DataTypDialog.this.setVisible(false);
				DataTypDialog.this.dispose();
			}
		});
		
		btnCancel = new JButton(getMessage("wizard.datatype.9", "Verwerfen"));
		btnCancel.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				dataTyp = null;
				blnOK = false;
				DataTypDialog.this.setVisible(false);
				DataTypDialog.this.dispose();
			}
		});
		
		cbxBaseDatatyp.addItemListener(new ItemListener() {
			
			@Override
			public void itemStateChanged(ItemEvent e) {
				if(e.getStateChange() == ItemEvent.SELECTED) {
					DataTyp typ = (DataTyp)((JComboBox)e.getSource()).getSelectedItem();
					tfFieldWidth.setText(String.valueOf(typ.getScale()));
					tfFieldPrecision.setText(String.valueOf(typ.getPrecision()));
					tfInputFormat.setText(typ.getInputFormat());
					tfOutputFormat.setText(typ.getOutputFormat());
				}
			}
		});
		
		lbInfo = new JLabel();
		
		this.add(lbBaseDatatyp, "1,1");
		this.add(cbxBaseDatatyp, "2,1");
		this.add(lbDatatyp, "1,2");
		this.add(tfDatatyp, "2,2");
		this.add(lbFieldWidth, "1,3");
		this.add(tfFieldWidth, "2,3");
		this.add(lbFieldPrecision, "1,4");
		this.add(tfFieldPrecision, "2,4");
		this.add(lbInputFormat, "1,5");
		this.add(tfInputFormat, "2,5");
		this.add(lbOutputFormat, "1,6");
		this.add(tfOutputFormat, "2,6");
		this.add(btnOK, "1,7");
		this.add(btnCancel, "2,7");
		this.add(lbInfo, "1,8, 2,8");
		
		this.setSize(300, 250);
		
		
	}
	
	public DataTyp getDataTyp() {
		return dataTyp;
	}
		
}
