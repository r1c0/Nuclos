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
package org.nuclos.client.wizard.steps;

import static org.nuclos.common2.CommonLocaleDelegate.getMessage;
import info.clearthought.layout.TableLayout;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;

import org.nuclos.client.main.mainframe.MainFrameTab;
import org.nuclos.client.masterdata.CollectableMasterDataWithDependants;
import org.nuclos.client.ui.DateChooser;
import org.nuclos.client.ui.Errors;
import org.nuclos.client.ui.collect.CollectControllerFactorySingleton;
import org.nuclos.client.wizard.DataTypeCollectController;
import org.nuclos.client.wizard.NuclosEntityWizardStaticModel;
import org.nuclos.client.wizard.model.Attribute;
import org.nuclos.client.wizard.model.DataTyp;
import org.nuclos.client.wizard.util.DoubleFormatDocument;
import org.nuclos.client.wizard.util.NuclosWizardUtils;
import org.nuclos.client.wizard.util.NumericFormatDocument;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.common2.exception.CommonPermissionException;
import org.nuclos.common2.exception.CommonValidationException;
import org.pietschy.wizard.InvalidStateException;

/**
* <br>
* Created by Novabit Informationssysteme GmbH <br>
* Please visit <a href="http://www.novabit.de">www.novabit.de</a>
* 
* @author <a href="mailto:marc.finke@novabit.de">Marc Finke</a>
* @version 01.00.00
*/

public class NuclosEntityAttributePropertiesStep extends NuclosEntityAttributeAbstractStep implements ChangeListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	JLabel lbName;
	JTextField tfName;
	JLabel lbDesc;
	JTextField tfDesc;
	JLabel lbDatatyp;
	JComboBox cbxDatatyp;
	JLabel lbFieldWidth;
	JTextField tfFieldWidth;
	JLabel lbFieldPrecision;
	JTextField tfFieldPrecision;
	
	JLabel lbOutputFormat;
	JTextField tfOutputFormat;
	JLabel lbValueList;
	JCheckBox cbxValueList;
	JLabel lbInfo;
	
	JLabel lbMinValue;
	JLabel lbMaxValue;
	JTextField tfMinValue;
	JTextField tfMaxValue;
	
	DateChooser datMinValue;
	DateChooser datMaxValue;	
	
	boolean columnTypeChangeAllowed;
	
	JButton btnDataTyp;
	
	boolean blnDescModified;
	
	NuclosEntityWizardStaticModel parentWizardModel;
	
	JComponent parent;
	
	
	public NuclosEntityAttributePropertiesStep() {	
		initComponents();		
	}

	public NuclosEntityAttributePropertiesStep(String name, String summary) {
		super(name, summary);
		initComponents();
	}

	public NuclosEntityAttributePropertiesStep(String name, String summary, Icon icon) {
		super(name, summary, icon);
		initComponents();
	}
	
	@Override
	protected void initComponents() {		
		double size [][] = {{TableLayout.PREFERRED,130, 30, TableLayout.FILL}, {20,20,20,20,20,20,20,20,20,20,20, TableLayout.FILL}};
		
		TableLayout layout = new TableLayout(size);
		layout.setVGap(3);
		layout.setHGap(5);
		this.setLayout(layout);
		
		lbName = new JLabel(getMessage("wizard.step.attributeproperties.1", "Anzeigename")+": ");
		tfName = new JTextField();
		tfName.setToolTipText(getMessage("wizard.step.attributeproperties.tooltip.1", "Anzeigename"));
		tfName.addFocusListener(NuclosWizardUtils.createWizardFocusAdapter());
		lbDesc = new JLabel(getMessage("wizard.step.attributeproperties.2", "Beschreibung")+": ");		
		tfDesc = new JTextField();
		tfDesc.setToolTipText(getMessage("wizard.step.attributeproperties.tooltip.2", "Beschreibung"));
		tfDesc.addFocusListener(NuclosWizardUtils.createWizardFocusAdapter());
		lbDatatyp = new JLabel(getMessage("wizard.step.attributeproperties.3", "Datentyp")+": ");
		
		List<DataTyp> lstTypes = DataTyp.getAllDataTyps();
		Collections.sort(lstTypes, new Comparator<DataTyp>() {

			@Override
			public int compare(DataTyp o1, DataTyp o2) {
				return o1.getName().toUpperCase().compareTo(o2.getName().toUpperCase());
			}			
			
		});
		
		cbxDatatyp = new JComboBox(lstTypes.toArray());
		cbxDatatyp.setToolTipText(getMessage("wizard.step.attributeproperties.tooltip.3", "Datentyp"));
		lbFieldWidth = new JLabel(getMessage("wizard.datatype.3", "Feldbreite")+": ");
		tfFieldWidth = new JTextField();
		tfFieldWidth.setEnabled(false);
		tfFieldWidth.addFocusListener(NuclosWizardUtils.createWizardFocusAdapter());
		lbFieldPrecision = new JLabel(getMessage("wizard.datatype.4", "Nachkommastellen")+": ");
		tfFieldPrecision = new JTextField();
		tfFieldPrecision.setEnabled(false);
		tfFieldPrecision.addFocusListener(NuclosWizardUtils.createWizardFocusAdapter());
		lbOutputFormat = new JLabel(getMessage("wizard.datatype.6", "Ausgabeformat")+": ");
		tfOutputFormat = new JTextField();
		tfOutputFormat.setEnabled(false);		
		tfOutputFormat.addFocusListener(NuclosWizardUtils.createWizardFocusAdapter());
		
		lbMinValue = new JLabel(getMessage("wizard.step.attributeproperties.21", "Mindestwert"));
		lbMaxValue = new JLabel(getMessage("wizard.step.attributeproperties.22", "Maximalwert"));
		tfMinValue = new JTextField();
		tfMinValue.addFocusListener(NuclosWizardUtils.createWizardFocusAdapter());
		tfMinValue.setToolTipText(getMessage("wizard.step.attributeproperties.tooltip.23", "Bestimmen Sie einen Mindeswert, der in der Eingabemaske validiert wird."));
		tfMinValue.setEnabled(false);
		tfMaxValue = new JTextField();
		tfMaxValue.addFocusListener(NuclosWizardUtils.createWizardFocusAdapter());
		tfMaxValue.setToolTipText(getMessage("wizard.step.attributeproperties.tooltip.24", "Bestimmen Sie einen Maximalwert, der in der Eingabemaske validiert wird."));
		tfMaxValue.setEnabled(false);
		
		datMinValue = new DateChooser();
		datMinValue.setVisible(false);
		datMinValue.setToolTipText(getMessage("wizard.step.attributeproperties.tooltip.23", "Bestimmen Sie einen Mindeswert, der in der Eingabemaske validiert wird."));
		datMaxValue = new DateChooser();
		datMaxValue.setToolTipText(getMessage("wizard.step.attributeproperties.tooltip.24", "Bestimmen Sie einen Maximalwert, der in der Eingabemaske validiert wird."));
		datMaxValue.setVisible(false);		
		
		btnDataTyp = new JButton("...");
		btnDataTyp.setToolTipText(getMessage("wizard.step.attributeproperties.4", "Datentypen konfigurieren"));
		
		lbValueList = new JLabel(getMessage("wizard.step.attributeproperties.77", "Werteliste:"));
		cbxValueList = new JCheckBox();
		cbxValueList.setToolTipText(getMessage("wizard.step.attributeproperties.tooltip.22", "In einer Werteliste definieren Sie fest definierte Werte"));
		
		lbInfo = new JLabel();
				
		tfName.getDocument().addDocumentListener(new DocumentListener() {
			
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
			
			protected void doSomeWork(DocumentEvent e) {
				int size = e.getDocument().getLength();
				if(size > 0) {
					NuclosEntityAttributePropertiesStep.this.setComplete(true);
				}
				else  {
					NuclosEntityAttributePropertiesStep.this.setComplete(false);
				}
				
				try {
					String value = e.getDocument().getText(0, e.getDocument().getLength());
					NuclosEntityAttributePropertiesStep.this.model.setName(value);
					if(!blnDescModified) {
						tfDesc.setText(e.getDocument().getText(0, e.getDocument().getLength()));
					}					
					if(parentWizardModel.getAttributeModel() != null && 
						!NuclosEntityAttributePropertiesStep.this.model.isEditMode()) {
						for(Attribute attr : parentWizardModel.getAttributeModel().getAttributes()) {
							if(attr.getLabel().equals(value)) {
								lbInfo.setForeground(Color.RED);
								lbInfo.setText(getMessage("wizard.step.attributeproperties.5", "Der Name wurde schon einmal vergeben"));
								lbInfo.setVisible(true);
								NuclosEntityAttributePropertiesStep.this.setComplete(false);
								break;
							}
							else {
								lbInfo.setVisible(false);
								NuclosEntityAttributePropertiesStep.this.setComplete(true);
							}
						}
					}
				} catch (BadLocationException ex) {
					Errors.getInstance().showExceptionDialog(NuclosEntityAttributePropertiesStep.this, ex);
				}
								
			}
		});
		
		tfDesc.addKeyListener(new KeyListener() {
			
			@Override
			public void keyTyped(KeyEvent e) {
				doSomeWork();
			}
			
			@Override
			public void keyReleased(KeyEvent e) {
				doSomeWork();
			}
			
			@Override
			public void keyPressed(KeyEvent e) {
				doSomeWork();				
			}
			
			protected void doSomeWork() {
				blnDescModified = true;
			}
		});
		
		tfDesc.getDocument().addDocumentListener(new DocumentListener() {
			
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
			
			protected void doSomeWork(DocumentEvent e) {
				try {
					NuclosEntityAttributePropertiesStep.this.model.setDesc(e.getDocument().getText(0, e.getDocument().getLength()));
				} catch (BadLocationException ex) {
					Errors.getInstance().showExceptionDialog(NuclosEntityAttributePropertiesStep.this, ex);
				}
								
			}
		});
		
		cbxDatatyp.addItemListener(new ItemListener() {
			
			@Override
			public void itemStateChanged(final ItemEvent e) {
				if(e.getStateChange() == ItemEvent.SELECTED) {
					SwingUtilities.invokeLater(new Runnable() {
							
							@Override
							public void run() {
								final Object obj = e.getItem();
								if(obj instanceof DataTyp) {
									DataTyp typ = (DataTyp)obj;									
									NuclosEntityAttributePropertiesStep.this.model.getAttribute().setDatatyp(typ);
									NuclosEntityAttributePropertiesStep.this.tfOutputFormat.setText(typ.getOutputFormat());
									NuclosEntityAttributePropertiesStep.this.tfFieldWidth.setText(typ.getScale() != null ? typ.getScale().toString(): "");
									NuclosEntityAttributePropertiesStep.this.tfFieldPrecision.setText(typ.getPrecision() != null ? typ.getPrecision().toString(): "");
									enableMinMaxValue(typ);
									if(typ.isRefenceTyp()){
										NuclosEntityAttributePropertiesStep.this.model.setReferenzTyp(true);
										NuclosEntityAttributePropertiesStep.this.model.getAttribute().setModifiable(false);
										cbxValueList.setEnabled(false);										
									}									
									else {
										cbxValueList.setEnabled(true);
										NuclosEntityAttributePropertiesStep.this.model.setReferenzTyp(false);
										if(NuclosEntityAttributePropertiesStep.this.model.getAttribute().isValueList()) {
											NuclosEntityAttributePropertiesStep.this.model.setValueListTyp(true);
										}
										else {
											NuclosEntityAttributePropertiesStep.this.model.setValueListTyp(false);
										}
										NuclosEntityAttributePropertiesStep.this.model.getAttribute().setModifiable(true);
									}
								}
							}
							
						});						
					}				
				}
			});
		
		btnDataTyp.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				final CollectControllerFactorySingleton factory = CollectControllerFactorySingleton.getInstance();
				final MainFrameTab tabDataType = new MainFrameTab(getMessage("wizard.step.attributeproperties.23", "Datentypen verwalten"));
				final DataTypeCollectController dtcc = factory.newDataTypeCollectController(parent, tabDataType);
				dtcc.addChangeListener(NuclosEntityAttributePropertiesStep.this);
				parent.add(tabDataType);
				
				try {
					dtcc.runNew();
				} catch(CommonBusinessException e1) {
					Errors.getInstance().showExceptionDialog(parent, e1);
				}
			}
		});
		
		cbxValueList.addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent e) {
				if(e.getSource() instanceof JCheckBox) {
					if(!model.isEditMode()) {
						JCheckBox cbx = (JCheckBox)e.getSource();
						if(cbx.isSelected()) {
							NuclosEntityAttributePropertiesStep.this.model.setValueListTyp(true);
							cbxDatatyp.setSelectedItem(DataTyp.getDefaultStringTyp());
							cbxDatatyp.setEnabled(false);
						}
						else {
							NuclosEntityAttributePropertiesStep.this.model.setValueListTyp(false);
							cbxDatatyp.setEnabled(true);
						}
					}
				}
			}
		});
		
		
		this.add(lbName, "0,0");
		this.add(tfName, "1,0");
		this.add(lbDesc, "0,1");
		this.add(tfDesc, "1,1");
		this.add(lbDatatyp, "0,2");
		this.add(cbxDatatyp, "1,2");
		this.add(btnDataTyp, "2,2");
		this.add(lbFieldWidth, "0,3");
		this.add(tfFieldWidth, "1,3");
		this.add(lbFieldPrecision, "0,4");
		this.add(tfFieldPrecision, "1,4");
		this.add(lbOutputFormat, "0,5");
		this.add(tfOutputFormat, "1,5");
		this.add(lbValueList, "0,6");
		this.add(cbxValueList, "1,6");
		this.add(lbMinValue, "0,7");
		this.add(tfMinValue, "1,7");
		this.add(datMinValue, "1,7");
		this.add(lbMaxValue, "0,8");
		this.add(tfMaxValue, "1,8");
		this.add(datMaxValue, "1,8");
		this.add(lbInfo, "0,9, 1,9");
		
		
		selectDefaultDataTyp();
		
	}
	
	@Override
	public void prepare() {
		super.prepare();
		if(this.model.isEditMode()) {
			SwingUtilities.invokeLater(new Runnable() {
				
				@Override
				public void run() {
					blnDescModified = true;
					tfName.setText(NuclosEntityAttributePropertiesStep.this.model.getAttribute().getLabel());
					tfDesc.setText(NuclosEntityAttributePropertiesStep.this.model.getAttribute().getDescription());
					tfFieldWidth.setText(String.valueOf(NuclosEntityAttributePropertiesStep.this.model.getAttribute().getDatatyp().getScale()));
					tfFieldPrecision.setText(String.valueOf(NuclosEntityAttributePropertiesStep.this.model.getAttribute().getDatatyp().getPrecision()));
					if(NuclosEntityAttributePropertiesStep.this.model.getAttribute().getDatatyp().getName().equals("Unknown")) {
						cbxDatatyp.addItem(NuclosEntityAttributePropertiesStep.this.model.getAttribute().getDatatyp());
					}
					
					String currentType = NuclosEntityAttributePropertiesStep.this.model.getAttribute().getDatatyp().getJavaType();
					cbxValueList.setSelected(NuclosEntityAttributePropertiesStep.this.model.getAttribute().isValueList());
					SwingUtilities.invokeLater(new Runnable() {						
						@Override
						public void run() {
							cbxValueList.setEnabled(!NuclosEntityAttributePropertiesStep.this.model.getAttribute().isValueList());							
						}
					});
					
					if(NuclosEntityAttributePropertiesStep.this.model.getAttribute().getDatatyp().equals(DataTyp.getReferenzTyp())) {
						cbxDatatyp.setEnabled(columnTypeChangeAllowed);
					}
					else if("java.lang.String".equals(NuclosEntityAttributePropertiesStep.this.model.getAttribute().getDatatyp().getJavaType()) ||
						NuclosEntityAttributePropertiesStep.this.model.getAttribute().getMetaVO() != null) {
						if(columnTypeChangeAllowed)
							cbxDatatyp.setEnabled(true);
						else {
							cbxDatatyp.removeAllItems();
							for(DataTyp typ : DataTyp.getSameDataTyps(currentType, NuclosEntityAttributePropertiesStep.this.model.getAttribute().getDatatyp())) {
								cbxDatatyp.addItem(typ);
							}
						}						
					}
					else {						
						if(!columnTypeChangeAllowed) {
							cbxDatatyp.removeAllItems();
							for(DataTyp typ : DataTyp.getSameDataTyps(currentType)) {
								cbxDatatyp.addItem(typ);
							}
							try {
								cbxDatatyp.addItem(DataTyp.getDefaultDataTyp());
							}
							catch(CommonFinderException e) {
								throw new CommonFatalException(e);
							}
							catch(CommonPermissionException e) {
								throw new CommonFatalException(e);
							}
						}
						
						cbxDatatyp.setEnabled(true);
						cbxDatatyp.setSelectedItem(NuclosEntityAttributePropertiesStep.this.model.getAttribute().getDatatyp());
					}
					cbxDatatyp.setSelectedItem(NuclosEntityAttributePropertiesStep.this.model.getAttribute().getDatatyp());
					
					SwingUtilities.invokeLater(new Runnable() {
						
						@Override
						public void run() {
							setInputValidation();
						}
					});
					
				}
				
			});		
			
		}
		else {
			if(parentWizardModel.isStateModel()) {
				ItemListener listerner[] = cbxDatatyp.getItemListeners();
				for(ItemListener l : listerner){
					cbxDatatyp.removeItemListener(l);
				}
				cbxDatatyp.removeAllItems();
				for(DataTyp typ : DataTyp.getAllDataTyps()) {
					if(!typ.getJavaType().equals("org.nuclos.server.genericobject.valueobject.GenericObjectDocumentFile"))
					cbxDatatyp.addItem(typ);
				}
				for(ItemListener l : listerner){
					cbxDatatyp.addItemListener(l);
				}
				
			}
			SwingUtilities.invokeLater(new Runnable() {
				
				@Override
				public void run() {
					if(model.getAttribute().getDatatyp() == null)
						cbxDatatyp.setSelectedItem(DataTyp.getDefaultStringTyp());
					else{
						cbxDatatyp.setSelectedItem(model.getAttribute().getDatatyp());
					}					
				}
			});
		}
		SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				tfName.requestFocus();
			}
		});
		
	}
	
	private void setInputValidation() {
		ItemListener li[] = cbxDatatyp.getItemListeners();
		for(ItemListener l : li) {
			cbxDatatyp.removeItemListener(l);
		}
		Attribute attr = this.model.getAttribute();
        String sInputValidation = attr.getInputValidation();
		if(sInputValidation != null && sInputValidation.length() > 0) {
			String s[] = sInputValidation.split(" ");
			if(attr.getDatatyp().getJavaType().equals("java.lang.Integer") || attr.getDatatyp().getJavaType().equals("java.lang.Double")) {
				tfMinValue.setText(s[0]);
				tfMaxValue.setText(s[1]);
			}
			else if(attr.getDatatyp().getJavaType().equals("java.util.Date")) {
				Date dateMin = new Date(Long.parseLong(s[0]));
				Date dateMax = new Date(Long.parseLong(s[1]));
				datMinValue.setDate(dateMin);
				datMaxValue.setDate(dateMax);				
			}			
		}
		for(ItemListener l : li) {
			cbxDatatyp.addItemListener(l);
		}
    }

	@Override
	public void applyState() throws InvalidStateException {
		try {
	        validateMinMaxValues();
        }
        catch(CommonValidationException e) {
	        Errors.getInstance().showExceptionDialog(this, e);
	        throw new InvalidStateException();
        }
        catch (Exception e) {
        	Errors.getInstance().showExceptionDialog(this, e);
        	throw new InvalidStateException();
		}
		super.applyState();
		if(!this.model.isValueListTyp() && !this.model.isRefernzTyp()) {
			model.getAttribute().setMetaVO(null);
			model.getAttribute().setField(null);
			model.nextStep();
			model.nextStep();
			model.refreshModelState();
		}
		else if(this.model.isRefernzTyp()) {
			model.nextStep();
			model.refreshModelState();
		}		
		
	}
	
	private void validateMinMaxValues() throws CommonValidationException {
		if(model.getAttribute().getDatatyp().getJavaType().equals("java.util.Date")) {
			Date dMin = datMinValue.getDate();
			Date dMax = datMaxValue.getDate();
			if(dMin == null && dMax == null) {
				model.getAttribute().setInputValidation(null);
				return;
			}
			if(!(dMin != null && dMax != null)) {
				throw new CommonValidationException(getMessage("wizard.step.attributeproperties.24", "Mindest- und Maximalwert müssen gesetzt sein."));
			}
			long l1 = dMin.getTime();
			long l2 = dMax.getTime();
			if(l1 > l2) {
				throw new CommonValidationException(getMessage("wizard.step.attributeproperties.25", "Der Maximalwert ist kleiner als der Mindestwert."));
			}
			model.getAttribute().setInputValidation(l1 + " " + l2);
		}
		else if(model.getAttribute().getDatatyp().getJavaType().equals("java.lang.Integer") || model.getAttribute().getDatatyp().getJavaType().equals("java.lang.Double")) {
			String s1 = tfMinValue.getText();
			String s2 = tfMaxValue.getText();
			if(s1.length() == 0 && s2.length() == 0) {
				model.getAttribute().setInputValidation(null);
				return;
			}
				
			if(!(s1.length() > 0 && s2.length() > 0)) {
				throw new CommonValidationException(getMessage("wizard.step.attributeproperties.24", "Mindest- und Maximalwert müssen gesetzt sein."));
			}
			if(model.getAttribute().getDatatyp().getJavaType().equals("java.lang.Integer")) {			
				Integer i1 = Integer.parseInt(s1);
				Integer i2 = Integer.parseInt(s2);
				if(i1 > i2) {
					throw new CommonValidationException(getMessage("wizard.step.attributeproperties.25", "Der Maximalwert ist kleiner als der Mindestwert."));
				}
				model.getAttribute().setInputValidation(s1 + " " + s2);		
			}
			else if(model.getAttribute().getDatatyp().getJavaType().equals("java.lang.Double")) {
				s1 = s1.replace(',', '.');
				s2 = s2.replace(',', '.');
				Double d1 = Double.parseDouble(s1);
				Double d2 = Double.parseDouble(s2);
				if(d1 > d2) {
					throw new CommonValidationException(getMessage("wizard.step.attributeproperties.25", "Der Maximalwert ist kleiner als der Mindestwert."));
				}
				model.getAttribute().setInputValidation(s1 + " " + s2);
			}
		}
		
	}
	
	public void setParent(JComponent comp) {
		this.parent = comp;
	}

	private void selectDefaultDataTyp() {
		SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				cbxDatatyp.setSelectedIndex(1);
				cbxDatatyp.setSelectedIndex(0);
			}
		});
	}
	
	public void setParentWizardModel(NuclosEntityWizardStaticModel model) {
		this.parentWizardModel = model;
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		if(!this.model.isEditMode()) {
			CollectableMasterDataWithDependants clct = (CollectableMasterDataWithDependants)e.getSource();
			final DataTyp typ = new DataTyp(clct.getMasterDataCVO());
			
			cbxDatatyp.addItem(typ);
			
			List<DataTyp> lst = new ArrayList<DataTyp>();
			for(int i = 0; i < cbxDatatyp.getModel().getSize(); i++) {
				lst.add((DataTyp)cbxDatatyp.getModel().getElementAt(i));
			}
			Collections.sort(lst, new Comparator<DataTyp>() {

				@Override
				public int compare(DataTyp o1, DataTyp o2) {
					return o1.getName().toUpperCase().compareTo(o2.getName().toUpperCase());
				}			
				
			});
			
			cbxDatatyp.removeAllItems();
			for(DataTyp type : lst) {
				cbxDatatyp.addItem(type);
			}
			
			SwingUtilities.invokeLater(new Runnable() {
				
				@Override
				public void run() {
					cbxDatatyp.setSelectedItem(typ);					
				}
			});
			 
		}
	}
	
	private void enableMinMaxValue(DataTyp dataTyp) {
		if(dataTyp.getJavaType().equals("java.lang.Integer") || dataTyp.getJavaType().equals("java.lang.Double")) {
			tfMinValue.setEnabled(true);
			tfMaxValue.setEnabled(true);
			tfMinValue.setVisible(true);
			tfMaxValue.setVisible(true);
			datMinValue.setVisible(false);
			datMaxValue.setVisible(false);			
			
			if(dataTyp.getJavaType().equals("java.lang.Integer")) {
				tfMinValue.setDocument(new NumericFormatDocument());
				tfMaxValue.setDocument(new NumericFormatDocument());
			}
			else {
				tfMinValue.setDocument(new DoubleFormatDocument());
				tfMaxValue.setDocument(new DoubleFormatDocument());
			}
		}
		else if(dataTyp.getJavaType().equals("java.util.Date")) {
			tfMinValue.setVisible(false);
			tfMaxValue.setVisible(false);
			datMinValue.setVisible(true);
			datMaxValue.setVisible(true);
		}
		else {
			tfMinValue.setEnabled(false);
			tfMaxValue.setEnabled(false);
			tfMinValue.setVisible(true);
			tfMaxValue.setVisible(true);
			datMinValue.setVisible(false);
			datMaxValue.setVisible(false);
		}
					
		tfMinValue.setText("");
		tfMaxValue.setText("");
		datMinValue.setDate(null);
		datMaxValue.setDate(null);
	}

	public void setColumnTypeChangeAllowed(boolean blnAllowed)  {
		this.columnTypeChangeAllowed = blnAllowed;
	}
	
}
