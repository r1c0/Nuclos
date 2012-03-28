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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.swing.AbstractCellEditor;
import javax.swing.DefaultCellEditor;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.nuclos.client.common.MetaDataClientProvider;
import org.nuclos.client.ui.DateChooser;
import org.nuclos.client.ui.collect.SubForm;
import org.nuclos.client.ui.gc.ListenerUtil;
import org.nuclos.client.wizard.model.ValueList;
import org.nuclos.client.wizard.util.NuclosWizardUtils;
import org.nuclos.common.dal.vo.EntityMetaDataVO;
import org.nuclos.common2.exception.CommonValidationException;
import org.pietschy.wizard.InvalidStateException;
import org.springframework.beans.factory.annotation.Configurable;

/**
* <br>
* Created by Novabit Informationssysteme GmbH <br>
* Please visit <a href="http://www.novabit.de">www.novabit.de</a>
* 
* @author <a href="mailto:marc.finke@novabit.de">Marc Finke</a>
* @version 01.00.00
*/
@Configurable
public class NuclosEntityAttributeValueListShipStep extends NuclosEntityAttributeAbstractStep {
	
	private static final Logger LOG = Logger.getLogger(NuclosEntityAttributeValueListShipStep.class);

	private JLabel lbName;
	private JTextField tfName;
	private JPanel pnlName;
	private JLabel lbInfo;

	private SubForm subform = new SubForm("Werteliste", JToolBar.VERTICAL);	
	private List<ValueList> lstValues;
	

	public NuclosEntityAttributeValueListShipStep() {	
		// initComponents();		
	}

	public NuclosEntityAttributeValueListShipStep(String name, String summary) {
		super(name, summary);
		// initComponents();
	}

	public NuclosEntityAttributeValueListShipStep(String name, String summary, Icon icon) {
		super(name, summary, icon);
		// initComponents();
	}
	
	@PostConstruct
	@Override
	protected void initComponents() {		
		
		lstValues = new ArrayList<ValueList>();
		
		this.setLayout(new BorderLayout(5,5));
		pnlName = new JPanel();
		pnlName.setLayout(new BorderLayout(5, 5));
		
		lbName = new JLabel(localeDelegate.getMessage("wizard.step.attributevaluelist.7", "Name"));
		tfName = new JTextField();
		
		tfName.setDocument(new LimitSpecialCharacterDocument(25));
		
		pnlName.add(lbName, BorderLayout.WEST);
		pnlName.add(tfName, BorderLayout.CENTER);
		
		lbInfo = new JLabel(localeDelegate.getMessage(
				"wizard.step.attributevaluelist.1","Entität ist schon vorhanden. Bitte anderen Namen vergeben!"));
		lbInfo.setForeground(Color.RED);
		lbInfo.setVisible(false);
		
		this.add(pnlName, BorderLayout.NORTH);
		this.add(subform, BorderLayout.CENTER);
		this.add(lbInfo, BorderLayout.SOUTH);
		subform.getSubformTable().setModel(new ValuelistTableModel());
		
		JTextField textField = new JTextField();
		textField.addFocusListener(NuclosWizardUtils.createWizardFocusAdapter());
		DefaultCellEditor editor = new DefaultCellEditor(textField);
		editor.setClickCountToStart(1);
		subform.getSubformTable().setDefaultEditor(String.class, editor);
		subform.getSubformTable().setDefaultEditor(Date.class, new DateEditor());
		
		ListenerUtil.registerSubFormToolListener(subform, new SubForm.SubFormToolListener() {
			@Override
			public void toolbarAction(String actionCommand) {
				if(SubForm.ToolbarFunction.fromCommandString(actionCommand) == SubForm.ToolbarFunction.NEW) {
					ValuelistTableModel model = (ValuelistTableModel)subform.getSubformTable().getModel();
					lstValues.add(new ValueList());
					model.fireTableDataChanged();
				}
			}
		});
		
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
			
			private void doSomeWork(DocumentEvent e) {
				try {
					String s = e.getDocument().getText(0, e.getDocument().getLength());
	                
	                model.getAttribute().setValueListName(s);
	                if(s.length() == 0)
	                	NuclosEntityAttributeValueListShipStep.this.setComplete(false);
	                else 
	                	NuclosEntityAttributeValueListShipStep.this.setComplete(true);

	                if(model.getAttribute().isValueListNew()) {
		                for(EntityMetaDataVO voEntity : MetaDataClientProvider.getInstance().getAllEntities()) {
		                	if(s.equals(voEntity.getEntity()) || ("V_EO_"+s).equalsIgnoreCase(voEntity.getDbEntity())) {
		                		NuclosEntityAttributeValueListShipStep.this.setComplete(false);
		                		lbInfo.setVisible(true);
		                		break;
		                	}		                	
		                	NuclosEntityAttributeValueListShipStep.this.setComplete(true);
		                	lbInfo.setVisible(false);
		                }
	                }
                }
                catch(BadLocationException e1) { 
                	LOG.info("doSomeWork failed: " + e1, e1);
                }
			}
		});
		
	
	}

	@Override
	public void prepare() {
		super.prepare();
		
		lstValues.clear();
		if(model.getAttribute().getValueList() != null) {
			for(ValueList s : model.getAttribute().getValueList()){
				lstValues.add(s);
			}
			ValuelistTableModel model = (ValuelistTableModel)subform.getSubformTable().getModel();
			model.fireTableDataChanged();					
		}
		
		if(model.getAttribute().getId() != null){
			model.getAttribute().setValueListNew(false);
		}
		else {
			model.getAttribute().setValueListNew(true);
		}
		
		tfName.setText(model.getAttribute().getValueListName());
		tfName.setEnabled(model.getAttribute().getValueListName() == null);
		
	}

	@Override
	public void close() {
		lbName = null;
		tfName = null;
		pnlName = null;
		lbInfo = null;

		if (subform != null) {
			subform.close();
		}
		subform = null;
		lstValues = null;
				
		super.close();
	}

	@Override
	public void applyState() throws InvalidStateException {
		super.applyState();
		this.model.nextStep();
		this.model.nextStep();
		this.model.refreshModelState();
		
		if(subform.getJTable().getCellEditor() != null)
			subform.getJTable().getCellEditor().stopCellEditing();
		
		this.model.getAttribute().setValueList(new ArrayList<ValueList>(lstValues));
		this.model.getAttribute().setValueListName(tfName.getText());
		
		// close Subform support
		subform.close();
		subform = null;
		
		super.applyState();
	}
	
	
	class ValuelistTableModel extends AbstractTableModel {
		
		ValuelistTableModel() {
			lstValues = new ArrayList<ValueList>();
		}
		
		public List<ValueList> getValues() {
			return lstValues;
		}

		@Override
        public String getColumnName(int column) {
	        switch(column) {
            case 0:
	            return localeDelegate.getMessage("wizard.step.attributevaluelist.2", "Wert");
            case 1:
	            return localeDelegate.getMessage("wizard.step.attributevaluelist.3", "Mnemonic");
            case 2:
	            return localeDelegate.getMessage("wizard.step.attributevaluelist.4", "Beschreibung");
            case 3:
	            return localeDelegate.getMessage("wizard.step.attributevaluelist.5", "Gültig von");
            case 4:
	            return localeDelegate.getMessage("wizard.step.attributevaluelist.6", "Gültig bis");
            default:
            	return super.getColumnName(column);
            }
			
			
        }

		@Override
        public int getColumnCount() {
			return 5;
        }

		@Override
        public int getRowCount() {
	       return lstValues.size();
        }

		@Override
        public Object getValueAt(int rowIndex, int columnIndex) {
			ValueList value = lstValues.get(rowIndex);
	        switch(columnIndex) {
            case 0:
	            return value.getLabel();
            case 1:
	            return value.getMnemonic();
            case 2:
	            return value.getDescription();
            case 3:
	            return value.getValidFrom();
            case 4:
	            return value.getValidUntil();
            default:
            	return "";
            }	        
        }

		@Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
			ValueList value = lstValues.get(rowIndex);
			switch(columnIndex) {
            case 0:
	            value.setLabel((String)aValue);
	            break;
            case 1:
            	value.setMnemonic((String)aValue);
            	break;
            case 2:
            	value.setDescription((String)aValue);            	
            	break;
            case 3:
            	value.setValidFrom((Date)aValue);
            	break;
            case 4:
            	value.setValidUntil((Date)aValue);
            	break;
            default:
            	break;
            }
			lstValues.set(rowIndex, value);        
        }

		@Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
	        return true;
        }

		@Override
        public Class<?> getColumnClass(int columnIndex) {
	        if(columnIndex < 3)
	        	return String.class;
	        else
	        	return Date.class;
        }
		
	}
	
	class DateEditor extends AbstractCellEditor implements TableCellEditor {

		private DateChooser dateChooser;
		
		public DateEditor() {
			dateChooser = new DateChooser();
		}
		
		@Override
        public Object getCellEditorValue() {	        
	        try {
	            return dateChooser.getDate();
            }
            catch(CommonValidationException e) {
            	LOG.info("getCellEditorValue: " + e);
	            return null;
            }
        }

		@Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
	        if(value instanceof Date) {
	        	Date date = (Date)table.getModel().getValueAt(row, column);
	        	if(date != null)
	        		dateChooser.setDate((Date)value);
	        }
	        else {
	        	dateChooser.setDate(null);
	        }
			return dateChooser;
        }
		
	}
	
	private class LimitSpecialCharacterDocument extends PlainDocument {
		
		private int max;
		
		public LimitSpecialCharacterDocument(int max) {
	        this.max = max;
        }

		@Override
		public void insertString(int offs, String str, AttributeSet a)	throws BadLocationException {		
			if(str == null)
				return;
			if(getLength() + str.length() < max) {
				str = StringUtils.replace(str, "\u00e4", "ae");
				str = StringUtils.replace(str, "\u00f6", "oe");
				str = StringUtils.replace(str, "\u00fc", "ue");
				str = StringUtils.replace(str, "\u00c4", "ae");
				str = StringUtils.replace(str, "\u00d6", "oe");
				str = StringUtils.replace(str, "\u00dc", "ue");
				str = StringUtils.replace(str, "\u00df", "ss");
				str = str.replaceAll("[^\\w]", "");
				super.insertString(offs, str, a);
			}
		}
	}
	
}
