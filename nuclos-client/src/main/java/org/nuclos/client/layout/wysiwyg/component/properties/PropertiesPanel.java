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
package org.nuclos.client.layout.wysiwyg.component.properties;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstraints;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.AbstractButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToggleButton;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.log4j.Logger;
import org.nuclos.client.layout.wysiwyg.WYSIWYGEditorModes;
import org.nuclos.client.layout.wysiwyg.WYSIWYGLayoutControllingPanel;
import org.nuclos.client.layout.wysiwyg.WYSIWYGStringsAndLabels;
import org.nuclos.client.layout.wysiwyg.WYSIWYGStringsAndLabels.ERROR_MESSAGES;
import org.nuclos.client.layout.wysiwyg.WYSIWYGStringsAndLabels.PROPERTIES_DIALOG;
import org.nuclos.client.layout.wysiwyg.WYSIWYGStringsAndLabels.PROPERTY_LABELS;
import org.nuclos.client.layout.wysiwyg.WYSIWYGStringsAndLabels.STATIC_BUTTON;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGComponent;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGStaticButton;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGStaticLabel;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGStaticTitledSeparator;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGSubForm;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGSubFormColumn;
import org.nuclos.client.layout.wysiwyg.editor.ui.panels.WYSIWYGLayoutEditorPanel;
import org.nuclos.client.layout.wysiwyg.editor.ui.panels.elementalcomponents.SaveAndCancelButtonPanel;
import org.nuclos.client.layout.wysiwyg.editor.ui.panels.elementalcomponents.SaveAndCancelButtonPanel.SaveAndCancelButtonPanelControllable;
import org.nuclos.client.layout.wysiwyg.editor.util.InterfaceGuidelines;
import org.nuclos.client.layout.wysiwyg.editor.util.TableLayoutUtil;
import org.nuclos.client.layout.wysiwyg.editor.util.popupmenu.TwoPartedAlignmentPanel;
import org.nuclos.client.layout.wysiwyg.editor.util.valueobjects.LayoutCell;
import org.nuclos.client.ui.Errors;
import org.nuclos.common.NuclosBusinessException;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.exception.CommonFatalException;

/**
 * This class is the Editor for {@link WYSIWYGComponent} {@link ComponentProperties}.<br>
 * It collects the Properties a {@link WYSIWYGComponent} can have.
 * 
 * 
 * 
 * <br>
 * Created by Novabit Informationssysteme GmbH <br>
 * Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 * 
 * @author <a href="mailto:hartmut.beckschulze@novabit.de">hartmut.beckschulze</a>
 * @version 01.00.00
 */
public class PropertiesPanel extends JPanel implements SaveAndCancelButtonPanelControllable, ActionListener {
	private static final long serialVersionUID = 7268088698639299981L;

	private ComponentProperties componentProperties;
	private TwoPartedAlignmentPanel alignment;
	
	private ListOrderedMap values;
	
	private WYSIWYGComponent c = null;

	private MapDisplayTableModel tableModel;
	private PropertiesTable properties;

	private final TableLayoutUtil tableLayoutUtil;
	private final SaveAndCancelButtonPanel saveAndCancel;
	// NUCLOSINT-681
	private static PropertiesPanel showingInstance = null;
	
	private static final Logger log = Logger.getLogger(PropertiesPanel.class);

	/**
	 * The Constructor 
	 * @param c the {@link WYSIWYGComponent}
	 * @param tableLayoutUtil the {@link TableLayoutUtil} for the {@link WYSIWYGComponent}
	 * @param additionalButtons a List for adding addititional Buttons (needed for adding the "default" Button for {@link WYSIWYGSubFormColumn})
	 */
	private PropertiesPanel(final WYSIWYGComponent c, final TableLayoutUtil tableLayoutUtil, List<AbstractButton> additionalButtons) {
		
		this.tableLayoutUtil = tableLayoutUtil;
		String componentName = ((Component) c).getName();
		if (componentName == null || "".equals(componentName))
			componentName = PROPERTIES_DIALOG.LABEL_NO_NAME_SPECIFIED;
		double[][] layout = {{InterfaceGuidelines.MARGIN_LEFT, TableLayout.FILL, 30, TableLayout.FILL, InterfaceGuidelines.MARGIN_RIGHT}, {InterfaceGuidelines.MARGIN_TOP, TableLayout.FILL, InterfaceGuidelines.MARGIN_BETWEEN, TableLayout.PREFERRED, InterfaceGuidelines.MARGIN_BETWEEN, TableLayout.PREFERRED}};

		setLayout(new TableLayout(layout));
		values = new ListOrderedMap();
		componentProperties = c.getProperties();
		this.c = c;

		TableLayoutConstraints compConstraints = null;
		try{
			if (tableLayoutUtil != null && !(c instanceof WYSIWYGSubFormColumn)) {
				compConstraints = tableLayoutUtil.getConstraintForComponent(c);
			}
			
		} catch (CommonFatalException e) {
			log.error(e);
		}
		
		if(compConstraints != null)
			copyComponentPropertiesToValueList(componentProperties, values, compConstraints);
		else
			copyComponentPropertiesToValueList(componentProperties, values, null);

		tableModel = new MapDisplayTableModel(values);

		properties = new PropertiesTable(tableModel);
		properties.getColumnModel().getColumn(0).setWidth(100);
		properties.getColumnModel().getColumn(0).setMinWidth(100);
		properties.getColumnModel().getColumn(1).setMinWidth(150);
		properties.setMinimumSize(new Dimension(300, 200));

		TableLayoutConstraints conProperties = new TableLayoutConstraints();
		conProperties.col1 = 1;
		conProperties.row1 = 1;
		conProperties.col2 = 3;
		conProperties.row2 = 1;
		conProperties.hAlign = TableLayout.FULL;
		conProperties.vAlign = TableLayout.FULL;
		
		properties.getTableHeader().setVisible(false);
		
		JScrollPane scrollpane = new JScrollPane(properties);
		add(scrollpane, conProperties);
		try {
			if (c.getParentEditor() != null) {
				this.c.getParentEditor().getController().setPreferencesPanel(this, WYSIWYGStringsAndLabels.partedString(PROPERTIES_DIALOG.DIALOG_TITLE, componentName));
			} else if (c instanceof WYSIWYGLayoutEditorPanel){
				((WYSIWYGLayoutEditorPanel)c).getController().setPreferencesPanel(this, WYSIWYGStringsAndLabels.partedString(PROPERTIES_DIALOG.DIALOG_TITLE, componentName));
			}
		}
		catch(NuclosBusinessException e) {
			log.error(e);
		}
		
		TableLayoutConstraints constraint;
		// no need to change the alignment of a subform column
		if(!(c instanceof WYSIWYGSubFormColumn) && tableLayoutUtil != null && (!(c instanceof WYSIWYGLayoutEditorPanel) || ((WYSIWYGLayoutEditorPanel)c).getParentEditor() != null)) {
			constraint = tableLayoutUtil.getConstraintForComponent(c);
			if(constraint != null) {
				alignment = new TwoPartedAlignmentPanel(c);

				alignment.setComponentAlignment(constraint.hAlign, constraint.vAlign);
				constraint = new TableLayoutConstraints(0, 3, 4, 3);
				add(alignment, constraint);
			}
		}

		constraint = new TableLayoutConstraints(0, 5, 4, 5);
		saveAndCancel = new SaveAndCancelButtonPanel(this.getBackground(), this, additionalButtons);
//		saveAndCancel.setSaveButtonEnable(false);
		add(saveAndCancel, constraint);

	}
	
	/*
	 * (non-Javadoc)
	 * @see org.nuclos.client.layout.wysiwyg.editor.ui.panels.elementalcomponents.SaveAndCancelButtonPanel.SaveAndCancelButtonPanelControllable#performCancelAction()
	 */
	@Override
	public void performCancelAction() {
		WYSIWYGLayoutControllingPanel controller = null;
		try {
			controller = tableLayoutUtil.getContainer().getParentEditorPanel().getController();
		}
		catch(Exception e) {
			try {
				controller = ((WYSIWYGLayoutEditorPanel)c).getController();
			}
			catch(NuclosBusinessException e1) {
				log.error(e1);
				Errors.getInstance().showExceptionDialog(null, e1);
			}
		} 
		// NUCLOSINT-681
		showingInstance = null;
		controller.hidePreferencesPanel();	
	}

	/*
	 * (non-Javadoc)
	 * @see org.nuclos.client.layout.wysiwyg.editor.ui.panels.elementalcomponents.SaveAndCancelButtonPanel.SaveAndCancelButtonPanelControllable#performSaveAction()
	 */
	@Override
	public void performSaveAction() {
		if (properties.isEditing()) {
			if (!properties.getCellEditor().stopCellEditing()) {
				return;
			}
		}
		boolean propertiesChanged = changePropertyValues(false);
		if (propertiesChanged) {
			if (tableLayoutUtil != null) {
				tableLayoutUtil.notifyThatSomethingChanged();
			}
			else 
			if (c instanceof WYSIWYGLayoutEditorPanel) {
				((WYSIWYGLayoutEditorPanel)c).getTableLayoutUtil().notifyThatSomethingChanged();
			}
			
			WYSIWYGLayoutControllingPanel controller = null;
			try {
				controller = tableLayoutUtil.getContainer().getParentEditorPanel().getController();
			}
			catch(Exception e) {
				try {
					controller = ((WYSIWYGLayoutEditorPanel)c).getController();
				}
				catch(NuclosBusinessException e1) {
					log.error(e1);
					Errors.getInstance().showExceptionDialog(null, e1);
				}
			} 
			// NUCLOSINT-681
			showingInstance = null;
			controller.hidePreferencesPanel();
		}
	}

	/**
	 * This Method is called in the ContextMenu
	 * @param c
	 * @param tableLayoutUtil
	 */
	public static void showPropertiesForComponent(final WYSIWYGComponent c, TableLayoutUtil tableLayoutUtil) {
		try {
			// NUCLOSINT-681
			showingInstance = new PropertiesPanel(c, tableLayoutUtil, null);
			//NUCLEUSINT-987
		} catch (CommonFatalException e) {
			log.info(e);
		} catch (NullPointerException e) {
			//NUCLEUSINT-1022
			log.info(e);
		}
	}
	// NUCLOSINT-681
	/**
	 * Check if there is already a Propertiespanel shown for this Component
	 */
	public static boolean checkIfAlreadyShowingForComponent(WYSIWYGComponent c) {
		if (showingInstance == null)
			return false;
		
		if (showingInstance.c.equals(c))
			return true;
		
		return false;
	}
	
	public WYSIWYGComponent getWYSIWYGComponent(){
		return this.c;
	}
	
	public TableLayoutUtil getTableLayoutUtil() {
		return this.tableLayoutUtil;
	}
	
	/**
	 * This Method shows the Properties of a {@link WYSIWYGSubFormColumn}
	 * @param c
	 * @param tableLayoutUtil
	 */
	public static void showPropertiesForSubFormColumn(final WYSIWYGSubFormColumn c, TableLayoutUtil tableLayoutUtil) {
		final JToggleButton button = new JToggleButton(PROPERTIES_DIALOG.LABEL_DEFAULT_VALUES);
		List<AbstractButton> buttonlist = new ArrayList<AbstractButton>();
		buttonlist.add(button);
		
		final PropertiesPanel dialog = new PropertiesPanel(c, tableLayoutUtil, buttonlist);
		
		if ((Boolean)c.getProperties().getProperty(WYSIWYGSubFormColumn.PROPERTY_DEFAULTVALUES).getValue()) {
			button.setSelected(true);
			dialog.properties.setEnabled(false);
		}
		
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {				
				if (button.isSelected()) {
					if (dialog.properties.isEditing()) {
						dialog.properties.getCellEditor().stopCellEditing();
					}
					
					for (String s : c.getPropertyNames()) {
						try {
							if ((PropertyUtils.getPropertyMode(c, s) & c.getParentEditor().getController().getMode()) == c.getParentEditor().getController().getMode()) {
								dialog.getModel().setValueAt(c.getDefaultPropertyValue(s), dialog.values.indexOf(s), 1);
							}
						} catch (NuclosBusinessException e1) {
							Errors.getInstance().showExceptionDialog(null, e1);
							log.error(e1);
						}
					}
					dialog.properties.setEnabled(false);
				}
				else {
					dialog.properties.setEnabled(true);
				}
			}
		});

		dialog.setVisible(true);
	}

	/**
	 * This Method copies the {@link ComponentProperties} to the {@link ListOrderedMap} for use with the {@link TableModel}.
	 * @param properties the {@link ComponentProperties}
	 * @param values the {@link ListOrderedMap} for display
	 * @param constraints the {@link TableLayoutConstraints}
	 */
	@SuppressWarnings("unchecked")
	private void copyComponentPropertiesToValueList(ComponentProperties properties, ListOrderedMap values, TableLayoutConstraints constraints) {
		Map<String, PropertyValue> propertyValues = properties.getFilteredProperties();
		Set<String> keys = propertyValues.keySet();

		for (java.util.Iterator<String> it = keys.iterator(); it.hasNext();) {
			String key = it.next();
			try {
				if (propertyValues.get(key) != null) {
					values.put(key, propertyValues.get(key).clone());
				}
			} catch (CloneNotSupportedException ex) {
				throw new NuclosFatalException(WYSIWYGStringsAndLabels.partedString(ERROR_MESSAGES.CLONE_NOT_SUPPORTED, propertyValues.get(key).getClass().toString()));
			}
		}

		// add at the end the constraints!
		if (constraints != null) {
			values.put(WYSIWYGComponent.CONSTRAINT_COL1, new PropertyValueInteger(constraints.col1));
			values.put(WYSIWYGComponent.CONSTRAINT_COL2, new PropertyValueInteger(constraints.col2));
			values.put(WYSIWYGComponent.CONSTRAINT_ROW1, new PropertyValueInteger(constraints.row1));
			values.put(WYSIWYGComponent.CONSTRAINT_ROW2, new PropertyValueInteger(constraints.row2));
		}
	}

	/**
	 * This Method takes the Values from the {@link ListOrderedMap} used in the Dialog to list the {@link PropertyValue} and applies them to the {@link WYSIWYGComponent}
	 * @param checkOnly if true only checks for changes
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private boolean changePropertyValues(boolean checkOnly) {
		ComponentProperties properties = this.componentProperties;
		Map<String, PropertyValue> propertyValues = properties.getClonedProperties();
		Set<String> keys = propertyValues.keySet();

		int i = 0;
		boolean valuesChanged = false;
		boolean exceptionOccured = false;
		
		try {
			c.validateProperties(values);
		} catch (CommonBusinessException e) {
			Errors.getInstance().showExceptionDialog(this, e);
			log.error(e);
			exceptionOccured = true;
		}
		
		for (java.util.Iterator<String> it = keys.iterator(); it.hasNext();) {
			String key = it.next();

			if (values.containsKey(key)) {
				PropertyValue originValue = propertyValues.get(key);
				PropertyValue changedValue = (PropertyValue) values.get(key);
	
				if (changedValue != null && !originValue.equals(changedValue)) {
					if(!checkOnly) propertyValues.put(key, changedValue);
					valuesChanged = true;
				}
				i++;
			}
		}
		
		if (c instanceof WYSIWYGSubFormColumn) {
			if (this.properties.isEnabled() == (Boolean)propertyValues.get(WYSIWYGSubFormColumn.PROPERTY_DEFAULTVALUES).getValue()) {
				if(!checkOnly) propertyValues.put(WYSIWYGSubFormColumn.PROPERTY_DEFAULTVALUES, new PropertyValueBoolean(!this.properties.isEnabled()));
				valuesChanged = true;
			}	
		}
		
		if (!checkOnly && tableLayoutUtil != null) {
			tableLayoutUtil.getUndoRedoFunction().beginTransaction();
		}
		
		if (!checkOnly && !exceptionOccured && valuesChanged) {
			try {
				properties.setProperties(propertyValues);
			} catch (CommonBusinessException e) {
				Errors.getInstance().showExceptionDialog(this, e);
				log.error(e);
				exceptionOccured = true;
			}
		}
		
		boolean mainEditorPanel = false;
		if (c instanceof WYSIWYGLayoutEditorPanel) {
			if (((WYSIWYGLayoutEditorPanel)c).getParentEditor() == null)
				mainEditorPanel = true;
		}
		
		if(!checkOnly && !(c instanceof WYSIWYGSubFormColumn) && !mainEditorPanel && values.size() > i +1) {
			TableLayoutConstraints constraints = new TableLayoutConstraints();
			try {
				constraints.col1 = getValueForConstraint(values.getValue(i++));
				constraints.col2 = getValueForConstraint(values.getValue(i++));
				constraints.row1 = getValueForConstraint(values.getValue(i++));
				constraints.row2 = getValueForConstraint(values.getValue(i++));
				if (alignment != null) {
					constraints.hAlign = alignment.getAlignmentConstraints()[TwoPartedAlignmentPanel.HORIZONTAL_ALIGN];
					constraints.vAlign = alignment.getAlignmentConstraints()[TwoPartedAlignmentPanel.VERTICAL_ALIGN];
				}
				if(!tableLayoutUtil.areConstraintsEqual(
					tableLayoutUtil.getConstraintForComponent(c), constraints)) {

					// NUCLEUSINT-277
					constraints = tableLayoutUtil.checkIfConstraintContainesIllegalValues(this.c, constraints);

					// FIX NUCLEUSINT-284
					if((c.getParentEditor().getController().getMode() & WYSIWYGEditorModes.EXPERT_MODE) == WYSIWYGEditorModes.EXPERT_MODE) {
						tableLayoutUtil.changeComponentsAlignment(c, constraints);
					}
					else {
						if(tableLayoutUtil.isCellEmpty(c, new LayoutCell(constraints)))
							tableLayoutUtil.changeComponentsAlignment(c, constraints);
						else
							throw new CommonBusinessException(WYSIWYGStringsAndLabels.WYSIWYGLAYOUT_EDITOR_PANEL.ERRORMESSAGE_INTERNAL_PANEL_CELL_NOT_EMPTY);
					}
				}
			} catch(CommonBusinessException e) {
				Errors.getInstance().showExceptionDialog(this, e);
				log.error(e);
				exceptionOccured = true;
			}
		}

		if (!checkOnly && tableLayoutUtil != null) {
			tableLayoutUtil.getUndoRedoFunction().commitTransaction();
		}

		return exceptionOccured ? false : valuesChanged;
	}
	
	/**
	 * Validation and Parsing for the constraint Values 
	 * 
	 * @param value
	 * @return
	 * @throws CommonBusinessException
	 */
	private int getValueForConstraint(Object value) throws CommonBusinessException {
		PropertyValueInteger integerValue = null;
		
		if (value instanceof PropertyValueInteger)
			integerValue = (PropertyValueInteger)value;
		else
			throw new CommonBusinessException("ParseException");
		
		if (integerValue.getValue() == null)
			throw new CommonBusinessException("NullValue");

		return integerValue.getValue();
		
	}
	
	/**
	 * @return the {@link TableModel} for the {@link PropertiesPanel}
	 */
	public TableModel getModel() {
		return tableModel;
	}

	/**
	 * The Label for the {@link PropertyValue} (left side of the Table)
	 * @param value
	 * @return
	 */
	public static Component getCellComponent(String value) {
		JPanel result = new JPanel();
		result.setOpaque(true);
		result.setBackground(Color.WHITE);
		TableLayout layout = new TableLayout(new double[][]{{InterfaceGuidelines.CELL_MARGIN_LEFT, TableLayout.FILL}, {InterfaceGuidelines.CELL_MARGIN_TOP, TableLayout.PREFERRED}});
		result.setLayout(layout);
		result.add(new JLabel(value), new TableLayoutConstraints(1, 1));
		return result;
	}

	/**
	 * 
	 * 
	 * <br>
	 * Created by Novabit Informationssysteme GmbH <br>
	 * Please visit <a href="http://www.novabit.de">www.novabit.de</a>
	 * 
	 * @author <a href="mailto:hartmut.beckschulze@novabit.de">hartmut.beckschulze</a>
	 * @version 01.00.00
	 */
	public class MapDisplayTableModel extends AbstractTableModel {
		private static final long serialVersionUID = 7918174220784510714L;
		private ListOrderedMap map;

		/**
		 * The Constructor
		 * @param map  the map with the values ( {@link PropertiesPanel#values}
		 */
		public MapDisplayTableModel(ListOrderedMap map) {
			this.map = map;
		}

		/*
		 * (non-Javadoc)
		 * @see javax.swing.table.TableModel#getColumnCount()
		 */
		@Override
		public int getColumnCount() {
			return 2;
		}

		/*
		 * (non-Javadoc)
		 * @see javax.swing.table.TableModel#getRowCount()
		 */
		@Override
		public int getRowCount() {
			return this.map.size();
		}

		/*
		 * (non-Javadoc)
		 * @see javax.swing.table.TableModel#getValueAt(int, int)
		 */
		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			switch (columnIndex) {
				case 0 :
					return (map.keySet().toArray())[rowIndex].toString();
				case 1 :
					return (map.values().toArray())[rowIndex] == null ? null : (map.values().toArray())[rowIndex];
			}
			return null;
		}

		/*
		 * (non-Javadoc)
		 * @see javax.swing.table.AbstractTableModel#isCellEditable(int, int)
		 */
		@Override
		public boolean isCellEditable(int row, int col) {
			////NUCLOSINT-743 enable disable Rule selection depending on Actioncommand
			if (c instanceof WYSIWYGStaticButton) {
				
				Object value = map.get(PROPERTY_LABELS.ACTIONCOMMAND);
				boolean executeRuleAction = false;
				if (value instanceof PropertyValueString) {
					String actioncommand = ((PropertyValueString)value).getValue();
					if (!STATIC_BUTTON.EXECUTE_RULE_ACTION_LABEL.equals(actioncommand)) {
						// if not execute rule action - reset maybe set values for rule
						int index = map.indexOf(PROPERTY_LABELS.RULE);
						if (index != -1)
							map.setValue(index, new PropertyValueString());
					} else if (STATIC_BUTTON.EXECUTE_RULE_ACTION_LABEL.equals(actioncommand)){
						// its the execute rule action
						executeRuleAction = true;
								
					}
					// is the field clicked the rule field?
					String propertyName = (String)getValueAt(row, 0);
					if (PROPERTY_LABELS.RULE.equals(propertyName)) {
						if (executeRuleAction) {
							// action command is execute rule, so a rule may be selected
							return true;
						} else {
							// false action command, no selection possible
							return false;
						}
					}
				}
			}
				
			if (col == 1)
					return true;
		
			return false;
		}

		/*
		 * (non-Javadoc)
		 * @see javax.swing.table.AbstractTableModel#setValueAt(java.lang.Object, int, int)
		 */
		@Override
		public void setValueAt(Object value, int row, int col) {
			map.setValue(row, value);
			fireTableCellUpdated(row, col);
		}

	}

	/**
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
	private class PropertiesTable extends JTable {

		/**
		 * @param tableModel the {@link TableModel} for the {@link PropertiesTable}
		 */
		public PropertiesTable(MapDisplayTableModel tableModel) {
			super(tableModel);
			this.setRowHeight(25);
		}

		/*
		 * (non-Javadoc)
		 * @see javax.swing.JTable#getCellEditor(int, int)
		 */
		@SuppressWarnings("unchecked")
		@Override
		public TableCellEditor getCellEditor(int row, int column) {
			TableCellEditor tableCellEditor = null;
			if (getModel().getValueAt(row, column) instanceof PropertyValue) {
				tableCellEditor = ((PropertyValue) getModel().getValueAt(row, column)).getTableCellEditor(c, (String) getModel().getValueAt(row, 0), PropertiesPanel.this);
			} else
				tableCellEditor = super.getCellEditor(row, column);
			
			tableCellEditor.addCellEditorListener(new CellEditorListener() {
				
				@Override
				public void editingStopped(ChangeEvent e) {
//					saveAndCancel.setSaveButtonEnable(changePropertyValues(true));
				}
				
				@Override
				public void editingCanceled(ChangeEvent e) {
					
				}
			});
			
			return tableCellEditor;
		}

		/*
		 * (non-Javadoc)
		 * @see javax.swing.JTable#getCellRenderer(int, int)
		 */
		@SuppressWarnings("unchecked")
		@Override
		public TableCellRenderer getCellRenderer(int row, int column) {
			if (getModel().getValueAt(row, column) instanceof PropertyValue) {
				return ((PropertyValue) getModel().getValueAt(row, column)).getTableCellRenderer(c, (String) getModel().getValueAt(row, 0), PropertiesPanel.this);
			} else if (column == 0) {
				return new LabelTableCellRenderer();
			}
			return super.getCellRenderer(row, column);
		}
	}

	/**
	 * 
	 * <br>
	 * Created by Novabit Informationssysteme GmbH <br>
	 * Please visit <a href="http://www.novabit.de">www.novabit.de</a>
	 * 
	 * @author <a href="mailto:hartmut.beckschulze@novabit.de">hartmut.beckschulze</a>
	 * @version 01.00.00
	 */
	private class LabelTableCellRenderer implements TableCellRenderer {

		/*
		 * (non-Javadoc)
		 * @see javax.swing.table.TableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
		 */
		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			return getCellComponent(PropertiesPanel.this.componentProperties.getPropertyLabel((String) value));
		}

	}

	/*
	 * (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 * NUCLEUSINT-274
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		ComponentProperties properties = PropertyUtils.getEmptyProperties(c, c.getParentEditor().getMetaInformation());
		try {
			/** remember essential things like entity, label etc */
			if (c instanceof WYSIWYGStaticTitledSeparator) {
				properties.setProperty(PROPERTY_LABELS.SEPERATOR_TITLE, c.getProperties().getProperty(PROPERTY_LABELS.SEPERATOR_TITLE), String.class);
			} else  {
				properties.setProperty(PROPERTY_LABELS.NAME, c.getProperties().getProperty(WYSIWYGStringsAndLabels.PROPERTY_LABELS.NAME), String.class);
			}
			
			if (c instanceof WYSIWYGStaticLabel){
				properties.setProperty(PROPERTY_LABELS.TEXT, c.getProperties().getProperty(PROPERTY_LABELS.TEXT), String.class);
				
			} else if (c instanceof WYSIWYGStaticButton){
				properties.setProperty(PROPERTY_LABELS.LABEL, c.getProperties().getProperty(PROPERTY_LABELS.LABEL), String.class);
			}  else if (c instanceof WYSIWYGSubForm){
				properties.setProperty(PROPERTY_LABELS.ENTITY, c.getProperties().getProperty(PROPERTY_LABELS.ENTITY), String.class);
				properties.setProperty(PROPERTY_LABELS.FOREIGNKEY, c.getProperties().getProperty(PROPERTY_LABELS.FOREIGNKEY), String.class);
			} 

		} catch (CommonBusinessException e1) {
			log.error(e1);
		}
		
		/** clear the valuelist to refill it */
		values.clear();
		TableLayoutConstraints compConstraints = null;
		try {
			if(tableLayoutUtil != null) {
				compConstraints = tableLayoutUtil.getConstraintForComponent(c);
			}

		}
		catch(CommonFatalException e1) {
			log.error(e1);
		}
		if(compConstraints != null) {
			copyComponentPropertiesToValueList(properties, values, compConstraints);
		}
		else {
			copyComponentPropertiesToValueList(properties, values, null);
		}
		
		/** resetting to default does a save */
		performSaveAction();
	}

}
