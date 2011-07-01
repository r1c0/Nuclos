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
package org.nuclos.client.relation;

import static org.nuclos.common2.CommonLocaleDelegate.getMessage;
import info.clearthought.layout.TableLayout;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;

import org.nuclos.client.common.LocaleDelegate;
import org.nuclos.client.common.MetaDataClientProvider;
import org.nuclos.client.wizard.model.EntityAttributeTranslationTableModel;
import org.nuclos.client.wizard.steps.NuclosEntityAttributeTranslationStep;
import org.nuclos.common.TranslationVO;
import org.nuclos.common.dal.vo.EntityFieldMetaDataVO;
import org.nuclos.common.dal.vo.EntityMetaDataVO;
import org.nuclos.common2.CommonLocaleDelegate;
import org.nuclos.common2.LocaleInfo;


public class RelationAttributePanel extends JPanel {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	static String[] labels = {getMessage("wizard.step.entitytranslationstable.1", "Anzeigename"), getMessage("wizard.step.entitytranslationstable.2", "Beschreibung")};

	JLabel lbLabel;
	JTextField tfLabel;	
	
	JLabel lbDescription;
	JTextField tfDescription;
	
	JLabel lbFieldName;
	JTextField tfFieldName;
	
	JLabel lbUnique;
	JCheckBox cbxUnique;
	
	JLabel lbNullable;
	JCheckBox cbxNullable;
	
	JLabel lbLogbookTracking;
	JCheckBox cbxLogbookTracking;
	
	JLabel lbSearchable;
	JCheckBox cbxSearchable;
	
	JLabel lbInsertable;
	JCheckBox cbxInsertable;
	
	JLabel lbModfiable;
	JCheckBox cbxModfiable;
	
	JLabel lbForeignField;
	JComboBox cbForeignField;
	JTextField tfForeignField;
	
	JLabel lbDefaultValue;
	JComboBox cbDefaultValue;
	
	JScrollPane scrolPane;
	JTable tblAttributes;
	
	EntityMetaDataVO voEntity;
	EntityMetaDataVO voEntitySource;
	
	JDialog dialog;
	
	EntityAttributeTranslationTableModel tablemodel;
	
	List<EntityFieldMetaDataVO> lstFields;
	
	boolean blnEditMode;
	
	Long fieldId;
	String sDBColumn;
	String sField;
	
	boolean blnOkay;
	int state;
	
	public static int TYPE_SUBFORM = 0;
	public static int TYPE_ENTITY = 1;
	
	int type;
	
	JButton btOk;
	JButton btAbort;
	
	public RelationAttributePanel(int type) {
		this.type = type;
		blnEditMode = false;
		blnOkay = false;
		init();		
	}
	
	public void setEntityFields(Collection<EntityFieldMetaDataVO> lst) {
		this.lstFields = new ArrayList<EntityFieldMetaDataVO>(lst);
	}
	
	public void setDialog(JDialog dia) {
		this.dialog = dia;
	}
	
	public int getState(){
		return state;
	}
	
	public void setEntity(EntityMetaDataVO vo) {
		voEntity = vo;
		cbForeignField.removeAllItems();
		cbForeignField.addItem("");
		for(EntityFieldMetaDataVO voField : MetaDataClientProvider.getInstance().getAllEntityFieldsByEntity(voEntity.getEntity()).values()) {
			if(voField.getId() > 0 && voField.getForeignEntity() == null)
				cbForeignField.addItem(voField.getField());
		}		
	}
	
	public void setEntitySource(EntityMetaDataVO vo) {
		voEntitySource = vo;	
	}

	protected void init() {
		double size [][] = {{TableLayout.PREFERRED,130, TableLayout.PREFERRED,30, TableLayout.FILL}, {20,20,20,20,20,20,20,20,20,20,20,100,5,30, TableLayout.FILL}};
		
		TableLayout layout = new TableLayout(size);
		layout.setVGap(3);
		layout.setHGap(5);
		this.setLayout(layout);
		
		
		btOk = new JButton("OK");
		btAbort = new JButton("Abbrechen");
		btOk.setPreferredSize(btAbort.getPreferredSize());
		
		
		double sizeButtonPanel [][] = {{5,TableLayout.PREFERRED, 5, TableLayout.PREFERRED, 5}, {25}};
		JPanel buttonPanel = new JPanel(new TableLayout(sizeButtonPanel));
		buttonPanel.add(btOk, "1,0");
		buttonPanel.add(btAbort, "3,0");
		
		btOk.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(type == TYPE_ENTITY) {
					if(!(cbForeignField.getSelectedIndex() > 0 || tfForeignField.getText().length() > 0)) {
						JOptionPane.showMessageDialog(RelationAttributePanel.this, "Bitte geben Sie einen Fremdschl\u00fcssel an!");
						return;
					}
					
				}
				state = 1;
				dialog.setVisible(false);
				dialog.dispose();
			}
		});
		
		btAbort.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				state = 0;
				dialog.setVisible(false);
				dialog.dispose();
			}
		});
		
		lbLabel = new JLabel("Beschriftung");
		lbDescription = new JLabel("Beschreibung");
		lbFieldName = new JLabel("Feldname");
		lbUnique = new JLabel("Eindeutig");
		lbNullable = new JLabel("Pflichtfeld");
		lbLogbookTracking = new JLabel("Logbuch");
		lbForeignField = new JLabel("Fremdschl\u00fcssel");
		lbDefaultValue = new JLabel("Standardwert");
		lbSearchable = new JLabel("Suchfeld");
		lbInsertable = new JLabel("Eingabe");
		lbModfiable = new JLabel("Modifizierbar");
		tfLabel = new JTextField();
		tfDescription = new JTextField();
		tfFieldName = new JTextField();
		cbxUnique = new JCheckBox();
		cbxNullable = new JCheckBox();
		cbxLogbookTracking = new JCheckBox();
		cbxInsertable = new JCheckBox();
		cbxSearchable = new JCheckBox();
		cbxModfiable = new JCheckBox();
		cbForeignField = new JComboBox();
		tfForeignField = new JTextField();
		cbDefaultValue = new JComboBox();
		if(type == TYPE_SUBFORM){
			tfForeignField.setVisible(false);
		}
		btOk.setEnabled(false);
		
		cbxSearchable.setSelected(true);
		cbxModfiable.setSelected(true);
		
		tablemodel = new EntityAttributeTranslationTableModel();
		List<TranslationVO> lstTranslation = new ArrayList<TranslationVO>();
		
		for(LocaleInfo voLocale : LocaleDelegate.getInstance().getAllLocales(false)) {
			String sLocaleLabel = voLocale.language; 
			Integer iLocaleID = voLocale.localeId;  
			String sCountry = voLocale.title;
			Map<String, String> map = new HashMap<String, String>();
			
			TranslationVO translation = new TranslationVO(iLocaleID, sCountry, sLocaleLabel, map);
			for(String sLabel : labels) {									
				translation.getLabels().put(sLabel, "");
			}
			lstTranslation.add(translation);
		}
		
		tablemodel.setRows(lstTranslation);	
		
		tblAttributes = new JTable(tablemodel);
		scrolPane = new JScrollPane(tblAttributes);
		
		DefaultCellEditor editor = new DefaultCellEditor(new JTextField());
		editor.setClickCountToStart(0);
		tblAttributes.setCellEditor(editor);
		tblAttributes.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		
		this.add(lbLabel, "0,0");
		this.add(tfLabel, "1,0");
		this.add(lbDescription, "0,1");
		this.add(tfDescription, "1,1");
		this.add(lbFieldName, "0,2");
		this.add(tfFieldName, "1,2");
		this.add(lbUnique, "0,3");
		this.add(cbxUnique, "1,3");
		this.add(lbNullable, "0,4");
		this.add(cbxNullable, "1,4");
		this.add(lbLogbookTracking, "0,5");
		this.add(cbxLogbookTracking, "1,5");
		this.add(lbSearchable, "0,6");
		this.add(cbxSearchable, "1,6");
		this.add(lbModfiable, "0,7");
		this.add(cbxModfiable, "1,7");
		this.add(lbInsertable, "0,8");
		this.add(cbxInsertable, "1,8");
		this.add(lbForeignField, "0,9");
		this.add(cbForeignField, "1,9");
		this.add(tfForeignField, "2,9");
		this.add(lbDefaultValue, "0,10");		
		this.add(cbDefaultValue, "1,10");		
		this.add(scrolPane, "0,11, 3,11");
		this.add(buttonPanel, "0,13, 3,13");
		
		
		tfLabel.getDocument().addDocumentListener(new DocumentListener() {
			
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
			
			private void doSomeWork(DocumentEvent e) {
				try {
					if(!blnEditMode) {
						tfDescription.setText(e.getDocument().getText(0, e.getDocument().getLength()));
						tfFieldName.setText(e.getDocument().getText(0, e.getDocument().getLength()));
						for(TranslationVO vo :tablemodel.getRows()) {
							vo.getLabels().put(NuclosEntityAttributeTranslationStep.labels[0], e.getDocument().getText(0, e.getDocument().getLength()));
							vo.getLabels().put(NuclosEntityAttributeTranslationStep.labels[1], e.getDocument().getText(0, e.getDocument().getLength()) );
						}
						tablemodel.fireTableDataChanged();
					}
				}
				catch(BadLocationException e1) {					
				}
			}
			
		});
		
		tfFieldName.getDocument().addDocumentListener(new DocumentListener() {
			
			@Override
			public void removeUpdate(DocumentEvent e) {
				doSomeWork(e);			}
			
			@Override
			public void insertUpdate(DocumentEvent e) {
				doSomeWork(e);			}
			
			@Override
			public void changedUpdate(DocumentEvent e) {
				doSomeWork(e);
			}
			
			private void doSomeWork(DocumentEvent e) {
				try {
					String sField = e.getDocument().getText(0, e.getDocument().getLength());
					if(voEntitySource != null) {
						for(EntityFieldMetaDataVO voField : lstFields) {
							if(voField.getField().equals(sField) && !sField.equals(RelationAttributePanel.this.sField)) {								
								btOk.setEnabled(false);
								return;
							}
							else {
								btOk.setEnabled(true);
							}
						}
					}
					if(btOk != null){
						if(e.getDocument().getLength() > 0){
							btOk.setEnabled(true);
						}
						else {
							btOk.setEnabled(false);
						}
					}
					
				}
				catch(BadLocationException e1) {}
			}
		});
		
	}
	
	public void setFieldValues(EntityFieldMetaDataVO voField) {
		sField = voField.getField();
		blnEditMode = true;
		tfFieldName.setText(voField.getField());
		cbxUnique.setSelected(voField.isUnique());
		cbxNullable.setSelected(!voField.isNullable());
		cbxLogbookTracking.setSelected(voField.isLogBookTracking());
		cbxSearchable.setSelected(voField.isSearchable());
		cbxInsertable.setSelected(voField.isInsertable());
		cbxModfiable.setSelected(voField.isModifiable());
		sDBColumn = voField.getDbColumn();
		
		
		tfLabel.setText(CommonLocaleDelegate.getResource(voField.getLocaleResourceIdForLabel(), voField.getFallbacklabel()));
		tfDescription.setText(CommonLocaleDelegate.getResource(voField.getLocaleResourceIdForDescription(), ""));
		
		cbForeignField.setSelectedItem(voField.getForeignEntityField());
		if(type == TYPE_ENTITY){
			tfForeignField.setText(voField.getForeignEntityField());
		}
		else {
			tfForeignField.setVisible(false);
		}
		
		fieldId = voField.getId();
		
	}
	
	public EntityFieldMetaDataVO getField() {
		EntityFieldMetaDataVO voField = new EntityFieldMetaDataVO();
		
		voField.setField(tfFieldName.getText());
		voField.setId(fieldId);
		
		voField.setUnique(cbxUnique.isSelected());
		voField.setNullable(!cbxNullable.isSelected());
		voField.setLogBookTracking(cbxLogbookTracking.isSelected());
		voField.setDataType("java.lang.String");
		String sDbFieldName = tfFieldName.getText();
		sDbFieldName = sDbFieldName.replaceAll("[^A-Za-z0-9_]", "_");
		if(fieldId == null) {
			if(this.type == TYPE_SUBFORM) {
				if(tfFieldName.getText().toUpperCase().startsWith("INTID_")){
					voField.setDbColumn(sDbFieldName);
				}
				else {
					voField.setDbColumn("INTID_" + sDbFieldName);
				}
			}
			else {
				if(tfFieldName.getText().toUpperCase().startsWith("STRVALUE_")){
					voField.setDbColumn(sDbFieldName);
				}
				else {
					voField.setDbColumn("STRVALUE_" + sDbFieldName);
				}
			}
		}
		else {
			voField.setDbColumn(sDBColumn);
		}

		voField.setForeignEntity(voEntity.getEntity());
		voField.setForeignEntityField((String)cbForeignField.getSelectedItem());
		if(tfForeignField.getText().length() > 0) {
			voField.setForeignEntityField(tfForeignField.getText().trim());
		}
		voField.setInsertable(cbxInsertable.isSelected());
		voField.setModifiable(cbxModfiable.isSelected());
		voField.setReadonly(false);
		voField.setScale(255);
		voField.setSearchable(cbxSearchable.isSelected());
		voField.setShowMnemonic(true);
		voField.setEntityId(voEntitySource.getId());

		return voField;
	}
	
	public EntityAttributeTranslationTableModel getTranslation() {
		return this.tablemodel;
	}
	
	public void setTranslation(List<TranslationVO> lstTranslation) {
		tablemodel.setRows(lstTranslation);
		tablemodel.fireTableDataChanged();
	}
	
	public void setTranslationAndMore(List<TranslationVO> lstTranslation) {
		this.setTranslation(lstTranslation);		
		for(TranslationVO vo : lstTranslation) {
			tfLabel.setText(vo.getLabels().get(NuclosEntityAttributeTranslationStep.labels[0]));
			tfDescription.setText(vo.getLabels().get(NuclosEntityAttributeTranslationStep.labels[1]));
		}
	}

}
