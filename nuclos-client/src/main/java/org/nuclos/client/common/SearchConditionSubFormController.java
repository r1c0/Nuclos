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
package org.nuclos.client.common;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.prefs.Preferences;

import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import org.nuclos.client.ui.collect.CollectController;
import org.nuclos.client.ui.collect.FixedColumnRowHeader;
import org.nuclos.client.ui.collect.SubForm;
import org.nuclos.client.ui.collect.component.model.CollectableComponentModelProvider;
import org.nuclos.common.collect.collectable.Collectable;
import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common.collect.collectable.CollectableField;
import org.nuclos.common.collect.collectable.CollectableFieldsProviderFactory;
import org.nuclos.common.collect.collectable.CollectableValueIdField;
import org.nuclos.common.collect.collectable.searchcondition.AtomicCollectableSearchCondition;
import org.nuclos.common.collect.collectable.searchcondition.CollectableIdCondition;
import org.nuclos.common.collect.collectable.searchcondition.CollectableIdListCondition;
import org.nuclos.common.collect.collectable.searchcondition.CollectableSearchCondition;
import org.nuclos.common.collect.collectable.searchcondition.CollectableSubCondition;
import org.nuclos.common.collect.collectable.searchcondition.CompositeCollectableSearchCondition;
import org.nuclos.common.collect.collectable.searchcondition.LogicalOperator;
import org.nuclos.common.collect.collectable.searchcondition.ReferencingCollectableSearchCondition;
import org.nuclos.common.collect.collectable.searchcondition.SearchConditionUtils;
import org.nuclos.common.collect.collectable.searchcondition.TrueCondition;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.Predicate;
import org.nuclos.common2.CommonLocaleDelegate;
import org.nuclos.common2.LangUtils;
import org.nuclos.common2.PreferencesUtils;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.common2.exception.PreferencesException;

/**
 * Controller for searching for dependant data (in a one-to-many relationship) in a subform.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */
public class SearchConditionSubFormController extends SubFormController {
	
	static String[] sEditFields = {"createdBy", "createdAt", "changedBy", "changedAt" };

	/**
	 * A <code>TableModel</code> representing a <code>CollectableSearchCondition</code>.
	 */
	private static interface SearchConditionTableModel extends SubForm.SubFormTableModel {
		/**
		 * @return the search condition represented by this table model. May be <code>null</code>.
		 */
		CollectableSearchCondition getCollectableSearchCondition();
		
		List<CollectableSearchCondition> getCollectableSearchConditions();

		/**
		 * @param clctcond may be <code>null</code>.
		 */
		void setCollectableSearchCondition(CollectableSearchCondition clctcond) throws CommonBusinessException;

		/**
		 * adds the given search condition as a new last row.
		 * @param clctcond
		 * @throws CommonBusinessException if the search condition cannot be represented in one row.
		 */
		void addSearchCondition(CollectableSearchCondition clctcond) throws CommonBusinessException;

		/**
		 * removes the given row.
		 * @param iRow
		 */
		@Override
		void remove(int iRow);

	}	// class SearchConditionTableModel

	/**
	 * implementation of <code>SearchConditionTableModel</code>.
	 */
	private class SearchConditionTableModelImpl extends DefaultTableModel implements SearchConditionTableModel {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		/**
		 * List<CollectableEntityField>
		 */
		private List<CollectableEntityField> lstclctefColumns;

		/**
		 * @param lstclctefColumns List<CollectableEntityField>
		 * @precondition lstclctefColumns != null
		 * @postcondition this.getColumnCount() == lstclctefColumns.size()
		 */
		@Override
		public void setColumns(List<? extends CollectableEntityField> lstclctefColumns) {
			this.lstclctefColumns = new ArrayList<CollectableEntityField>(lstclctefColumns);

			super.fireTableStructureChanged();

			assert this.getColumnCount() == lstclctefColumns.size();
		}

		@Override
		public int getColumnCount() {
			return this.lstclctefColumns.size();
		}

		@Override
		public Object getValueAt(int row, int column) {
			return (column >= 0) ? super.getValueAt(row, column) : null;
		}

		/**
		 * @param iColumn
		 * @return the name of column <code>iColumn</code>, as shown in the table header
		 */
		@Override
		public String getColumnName(int iColumn) {
			String sLabel = SearchConditionSubFormController.this.getSubForm().getColumnLabel(this.getCollectableEntityField(iColumn).getName());
			if (sLabel != null) {
				return sLabel;
			}
			return this.getCollectableEntityField(iColumn).getLabel();
		}

		@Override
		public String getColumnFieldName(int columnIndex) {
			return this.getCollectableEntityField(columnIndex).getName();
		}

		@Override
		public Class<?> getColumnClass(int iColumn) {
			return CollectableSearchCondition.class;
		}

		/**
		 * {$inheritDoc}
		 */
		@Override
		public CollectableSearchCondition getCollectableSearchCondition() {
			final CompositeCollectableSearchCondition condOr = new CompositeCollectableSearchCondition(LogicalOperator.OR);
			for (int iRow = 0; iRow < this.getRowCount(); ++iRow) {
				final CompositeCollectableSearchCondition condAnd = new CompositeCollectableSearchCondition(LogicalOperator.AND);
				for (int iColumn = 0; iColumn < this.getColumnCount(); iColumn++) {
					final CollectableSearchCondition clctcond = (CollectableSearchCondition) this.getValueAt(iRow, iColumn);
					if (clctcond != null) {
						condAnd.addOperand(clctcond);
					}
				}
				if (condAnd.getOperandCount() > 0) {
					condOr.addOperand(SearchConditionUtils.simplified(condAnd));
				}
			}

			return SearchConditionUtils.simplified(condOr);
		}
		
		@Override
		public List<CollectableSearchCondition> getCollectableSearchConditions() {
			List<CollectableSearchCondition> lst = new ArrayList<CollectableSearchCondition>();
			for (int iRow = 0; iRow < this.getRowCount(); ++iRow) {
				final CompositeCollectableSearchCondition condAnd = new CompositeCollectableSearchCondition(LogicalOperator.AND);
				for (int iColumn = 0; iColumn < this.getColumnCount(); iColumn++) {
					final CollectableSearchCondition clctcond = (CollectableSearchCondition) this.getValueAt(iRow, iColumn);
					if (clctcond != null) {
						condAnd.addOperand(clctcond);
					}
				}
				if (condAnd.getOperandCount() > 0) {
					lst.add(SearchConditionUtils.simplified(condAnd));
					//condOr.addOperand(SearchConditionUtils.simplified(condAnd));
				}
			}
			
			return lst;
		}

		/**
		 * tries to display the search condition in this controller's subform.
		 * @throws CommonBusinessException if the given condition can't be displayed.
		 */
		@Override
		public void setCollectableSearchCondition(CollectableSearchCondition cond) throws CommonBusinessException {
			this.clear();

			SearchConditionUtils.trueIfNull(cond).accept(new SetSearchConditionVisitor());
		}

		@Override
		public void addSearchCondition(CollectableSearchCondition clctcond) throws CommonBusinessException {
			this.addRow(new Vector<Object>());
			this.fillLastRowWithSearchCondition(clctcond);
		}

		@Override
		public void remove(int iRow) {
			this.removeRow(iRow);
		}

		/**
		 * @param clctcond may be <code>null</code>.
		 */
		@SuppressWarnings("deprecation")
		private void fillLastRowWithSearchCondition(CollectableSearchCondition clctcond) throws CommonBusinessException {
			if (clctcond != null) {
				/** @todo check if the search condition can be displayed in the fields at all,
				 * eg. isBasicSearchCondition() - isComplexSearchCondition().
				 * For the moment, we assume isBasicSearchCondition(). A basic search condition would be a
				 * conjunction, which may be nested. Prohibited are NOT, OR.
				 */

				switch (clctcond.getType()) {
					case CollectableSearchCondition.TYPE_ATOMIC:
						this.fillLastRowWithAtomicSearchCondition((AtomicCollectableSearchCondition) clctcond);
						break;

					case CollectableSearchCondition.TYPE_COMPOSITE:
						this.fillLastRowWithFlatConjunction((CompositeCollectableSearchCondition) clctcond);
						break;

					default:
						throw new CommonFatalException(CommonLocaleDelegate.getMessage("SearchConditionSubFormController.1", "Unbekannter Knotentyp: {0}", clctcond.getClass().getName()));
				}
			}
		}

		/**
		 * @param atomiccond
		 */
		private void fillLastRowWithAtomicSearchCondition(AtomicCollectableSearchCondition atomiccond) {
			final int iRow = this.getRowCount() - 1;
			if (iRow < 0) {
				throw new CommonFatalException(CommonLocaleDelegate.getMessage("SearchConditionSubFormController.2", "Subform ist leer."));
			}
			final int iColumn = this.findColumnByFieldName(atomiccond.getFieldName());
			if (iColumn == -1) {
				throw new CommonFatalException(CommonLocaleDelegate.getMessage("SearchConditionSubFormController.3", "Spalte {0} nicht vorhanden.", atomiccond.getFieldName()));
			}

			this.setValueAt(atomiccond, iRow, iColumn);
		}

		/**
		 * @param compositecond
		 * @precondition compositecond.getLogicalOperator() == LogicalOperator.AND
		 */
		@SuppressWarnings("deprecation")
		private void fillLastRowWithFlatConjunction(CompositeCollectableSearchCondition compositecond) throws CommonBusinessException {
			if (compositecond.getLogicalOperator() != LogicalOperator.AND) {
				throw new CommonFatalException(CommonLocaleDelegate.getMessage("SearchConditionSubFormController.4", "Dieser logische Operator ist hier nicht erlaubt: {0}", compositecond.getLogicalOperator()));
			}
			for (CollectableSearchCondition condOperand : compositecond.getOperands()) {
				if (condOperand.getType() != CollectableSearchCondition.TYPE_ATOMIC) {
					throw new CommonBusinessException(CommonLocaleDelegate.getMessage("SearchConditionSubFormController.5", "Geschachtelte Bedingung kann in Unterformular nicht dargestellt werden."));
				}
				this.fillLastRowWithAtomicSearchCondition((AtomicCollectableSearchCondition) condOperand);
			}
		}

		private void clear() {
			this.setRowCount(0);
		}

		/**
		 * @param iColumn
		 * @return
		 * @precondition iColumn >= 0 && iColumn < this.getColumnCount()
		 */
		@Override
		public CollectableEntityField getCollectableEntityField(int iColumn) {
			return this.lstclctefColumns.get(iColumn);
		}

		@Override
		public CollectableField getValueAsCollectableField(Object oValue) {
			final CollectableField clctf = getComparand((CollectableSearchCondition) oValue);
			return (clctf == null) ? CollectableValueIdField.NULL : clctf;
		}

		/**
		 * @param sFieldName
		 * @return the index of the column with the given fieldname. -1 if none was found.
		 */
		@Override
		public int findColumnByFieldName(String sFieldName) {
			int result = -1;
			for (int iColumn = 0; iColumn < this.getColumnCount(); ++iColumn) {
				if (this.getCollectableEntityField(iColumn).getName().equals(sFieldName)) {
					result = iColumn;
					break;
				}
			}
			return result;
		}

		/**
		 * @param clctef
		 * @return a null value that can be set in a table cell for the given entity field.
		 */
		@Override
		public Object getNullValue(CollectableEntityField clctef) {
			return null;
		}

		@Override
		public boolean isCellEditable(int iRow, int iColumn) {
			final String sColumnName = this.getCollectableEntityField(iColumn).getName();
			return SearchConditionSubFormController.this.isColumnEnabled(sColumnName);
		}

		private class SetSearchConditionVisitor implements CollectableSearchCondition.Visitor<Void, CommonBusinessException> {
			@Override
			public Void visitTrueCondition(TrueCondition truecond) {
				// do nothing
				return null;
			}

			@Override
			public Void visitAtomicCondition(AtomicCollectableSearchCondition atomiccond) {
				addRow(new Vector<Object>());
				fillLastRowWithAtomicSearchCondition(atomiccond);
				return null;
			}

			@Override
			public Void visitCompositeCondition(CompositeCollectableSearchCondition compositecond) throws CommonBusinessException {
				final LogicalOperator logicalop = compositecond.getLogicalOperator();
				if (logicalop == LogicalOperator.AND) {
					addRow(new Vector<Object>());
					fillLastRowWithFlatConjunction(compositecond);
				}
				else if (logicalop == LogicalOperator.OR) {
					for (CollectableSearchCondition condOperand : compositecond.getOperands()) {
						addRow(new Vector<Object>());
						fillLastRowWithSearchCondition(condOperand);
					}
				}
				else {
					throw new IllegalArgumentException(CommonLocaleDelegate.getMessage("SearchConditionSubFormController.6", "Der logische Operator {0} kann in einem Unterformular nicht dargestellt werden.", CommonLocaleDelegate.getMessage(logicalop.getResourceIdForLabel(), null)));
				}
				return null;
			}

			@Override
			public Void visitIdCondition(CollectableIdCondition idcond) {
				throw new IllegalArgumentException(CommonLocaleDelegate.getMessage("SearchConditionSubFormController.7", "Eine Id-Bedingung kann in einem Unterformular nicht dargestellt werden."));
			}

			@Override
			public Void visitSubCondition(CollectableSubCondition subcond) {
				throw new IllegalArgumentException(CommonLocaleDelegate.getMessage("SearchConditionSubFormController.8", "Eine geschachtelte Unterbedingung kann in einem Unterformular nicht dargestellt werden."));
			}

			@Override
			public Void visitReferencingCondition(ReferencingCollectableSearchCondition refcond) {
				throw new IllegalArgumentException(CommonLocaleDelegate.getMessage("SearchConditionSubFormController.9", "Eine referenzierende Bedingung kann in einem Unterformular nicht dargestellt werden."));
			}

			@Override
            public Void visitIdListCondition(CollectableIdListCondition collectableIdListCondition) throws CommonBusinessException {
	            throw new IllegalArgumentException(CommonLocaleDelegate.getMessage("SearchConditionSubFormController.7", "Eine Id-Bedingung kann in einem Unterformular nicht dargestellt werden."));
            }
		}

	}	// class SearchConditionTableModelImpl

	private SearchConditionTableModel tblmdl;
	
	public SearchConditionSubFormController(Component parent, JComponent parentMdi,
			CollectableComponentModelProvider clctcompmodelproviderParent, String sParentEntityName, final SubForm subform,
			Preferences prefsUserParent, CollectableFieldsProviderFactory clctfproviderfactory) {
		super(parent, parentMdi, clctcompmodelproviderParent, sParentEntityName, subform, true, prefsUserParent,
				clctfproviderfactory);

		// there is no multiedit for a SearchConditionSubFormController:
		subform.setToolbarFunctionState(SubForm.ToolbarFunction.MULTIEDIT.name(), SubForm.ToolbarFunctionState.HIDDEN);

		// initialize table model:
		this.tblmdl = new SearchConditionTableModelImpl();
		this.getSubFormTableModel().setColumns(getColumnsFromPrefs());

		final JTable tbl = subform.getJTable();
		tbl.setModel(this.tblmdl);

		this.setupTableCellRenderers(this.tblmdl);
		this.setupRowHeight(this.tblmdl);
		this.setupStaticTableCellEditors(tbl);

		this.setColumnWidths();

		this.setupTableModelListener();
		this.setupColumnModelListener();
		
	}
	
	/**
	 * @return the table columns widths. If there are stored user preferences, the sizes will be restored.
	 * Size and order of list entries is determined by number and order of visible columns
	 */
	@Override
	protected List<Integer> getTableColumnWidthsFromPreferences() {
		List<Integer> result;
		try {
			result = PreferencesUtils.getIntegerList(this.getPrefs(), FixedColumnRowHeader.PREFS_NODE_FIXEDFIELDS_WIDTHS);
			result.addAll(PreferencesUtils.getIntegerList(this.getPrefs(), PREFS_NODE_SELECTEDFIELDWIDTHS));
		}
		catch (PreferencesException ex) {
			log.error("Failed to retrieve table column widths from the preferences. They are reset.");
			result = new ArrayList<Integer>();
		}

		if (log.isDebugEnabled()) {
			log.debug("getTableColumnWidthsFromPreferences for entity " + this.getSubForm().getEntityName());
			for (Object o : result) {
				log.debug("getTableColumnWidthsFromPreferences: column width = " + o);
			}
		}

		return result;
	}
	
	private boolean isEditField(CollectableEntityField field) {
		for(String s : this.sEditFields) {
			if (s.equals(field.getName())) {
				return true;
			}
		}
		return false;
	}
	
	private List<CollectableEntityField> getColumnsFromPrefs() {
		List<CollectableEntityField> lstFieldsToDisplay = new ArrayList<CollectableEntityField>();
		Preferences pref = this.getPrefs();
		try {
			List<String> lstFields = PreferencesUtils.getStringList(pref, FixedColumnRowHeader.PREFS_NODE_FIXEDFIELDS);
			lstFields.addAll(PreferencesUtils.getStringList(pref, CollectController.PREFS_NODE_SELECTEDFIELDS));
			if(lstFields.size() == 0) {
				lstFieldsToDisplay = getTableColumns();
				List<CollectableEntityField> lstTMP = new ArrayList<CollectableEntityField>();
				for(CollectableEntityField field : lstFieldsToDisplay) {
					if(!isEditField(field))
						lstTMP.add(field);
				}
				lstFieldsToDisplay.clear();
				lstFieldsToDisplay.addAll(lstTMP);
			}
			for(String strField : lstFields) {
				for(CollectableEntityField field : this.getTableColumns()) {
					if(field.getName().equals(strField)) {
						lstFieldsToDisplay.add(field);
					}
				}
			}
		}
		catch(PreferencesException e) {
			// display all columns on Exception
			lstFieldsToDisplay = getTableColumns();
		}
		
		return lstFieldsToDisplay;
		
	}

	@Override
	public void close() {
		this.removeColumnModelListener();
		this.removeTableModelListener();

		super.close();
	}
	
	@Override
	protected CollectableField getFieldFromParentSubform(String sFieldName) {
		return null;
	}

	/**
	 * removes all rows from this subform.
	 * @postcondition this.getCollectableSearchCondition() == null
	 */
	public void clear() {
		try {
			this.setCollectableSearchCondition(null);
			// special requirement: always have an empty row in the subform:
			this.insertNewRow();
		}
		catch (CommonBusinessException ex) {
			throw new CommonFatalException(ex);
		}
		assert this.getCollectableSearchCondition() == null;
	}

	/**
	 * @return the table model that contains the data for the subform.
	 */
	public SearchConditionTableModel getSearchConditionTableModel() {
		return this.tblmdl;
	}

	/**
	 * @return the table model that contains the data for the subform.
	 */
	@Override
	protected SubForm.SubFormTableModel getSubFormTableModel() {
		return this.getSearchConditionTableModel();
	}

	public void setCollectableSearchCondition(CollectableSearchCondition clctcond) throws CommonBusinessException {
		/** @todo stopEditing? */
		this.getSearchConditionTableModel().setCollectableSearchCondition(clctcond);
	}

	public CollectableSearchCondition getCollectableSearchCondition() {
		/** @todo stopEditing? */
		return this.getSearchConditionTableModel().getCollectableSearchCondition();
	}
	
	public List<CollectableSearchCondition> getCollectableSubformSearchConditions() {		
		return this.getSearchConditionTableModel().getCollectableSearchConditions();		
	}

	@Override
	protected Collectable insertNewRow() throws CommonBusinessException {
		this.getSearchConditionTableModel().addSearchCondition(null);
		return null;
	}

	/**
	 * @param subcond
	 * @return true if the search condition can be displayed in the mask (instead of the search editor)
	 */
	public static boolean canSubConditionBeDisplayed(CollectableSubCondition subcond) {
		return canSearchConditionBeDisplayed(subcond.getSubCondition(), 0);
	}

	private static boolean canSearchConditionBeDisplayed(CollectableSearchCondition cond, final int iLevel) {
		return SearchConditionUtils.trueIfNull(cond).accept(new CanSearchConditionBeDisplayedVisitor(iLevel));
	}

	/**
	 * Currently, every atomic condition can be displayed.
	 * @param atomiccond
	 * @return true
	 */
	private static boolean canAtomicConditionBeDisplayed(AtomicCollectableSearchCondition atomiccond) {
		return true;
	}

	private static boolean canCompositeConditionBeDisplayed(CompositeCollectableSearchCondition compositecond, int iLevel) {
		final boolean result;
		final LogicalOperator logicalop = compositecond.getLogicalOperator();
		if (logicalop == LogicalOperator.AND) {

			class IsAtomicAndCanBeDisplayed implements Predicate<CollectableSearchCondition> {
				@Override
				@SuppressWarnings("deprecation")
				public boolean evaluate(CollectableSearchCondition cond) {
					return (cond.getType() == CollectableSearchCondition.TYPE_ATOMIC) && canAtomicConditionBeDisplayed((AtomicCollectableSearchCondition) cond);
				}
			}

			// all operands must be atomic and displayable and must have unique field names:
			result = CollectionUtils.forall(compositecond.getOperands(), new IsAtomicAndCanBeDisplayed()) &&
					SearchConditionUtils.areAtomicConditionsUnique(compositecond.getOperands());
		}
		else if (logicalop == LogicalOperator.OR) {
			if (iLevel > 0) {
				result = false;
			}
			else {
				result = CollectionUtils.forall(compositecond.getOperands(), new CanSearchConditionBeDisplayed(iLevel + 1));
			}
		}
		else if (logicalop == LogicalOperator.NOT) {
			result = false;
		}
		else {
			throw new IllegalArgumentException(CommonLocaleDelegate.getMessage("SearchConditionSubFormController.10", "Unbekannter logischer Operator: {0}", LangUtils.toString(logicalop)));
		}
		return result;
	}

	@Override
	public boolean isColumnEnabled(String sColumnName) {
		return this.isEnabled();
	}

	private static class CanSearchConditionBeDisplayed implements Predicate<CollectableSearchCondition> {
		private final int iLevel;

		CanSearchConditionBeDisplayed(int iLevel) {
			this.iLevel = iLevel;
		}

		@Override
		public boolean evaluate(CollectableSearchCondition cond) {
			return canSearchConditionBeDisplayed(cond, iLevel);
		}
	}

	private static class CanSearchConditionBeDisplayedVisitor implements CollectableSearchCondition.Visitor<Boolean, RuntimeException> {
		private final int iLevel;

		CanSearchConditionBeDisplayedVisitor(int iLevel) {
			this.iLevel = iLevel;
		}

		@Override
		public Boolean visitTrueCondition(TrueCondition truecond) throws RuntimeException {
			return true;
		}

		@Override
		public Boolean visitAtomicCondition(AtomicCollectableSearchCondition atomiccond) throws RuntimeException {
			return canAtomicConditionBeDisplayed(atomiccond);
		}

		@Override
		public Boolean visitCompositeCondition(CompositeCollectableSearchCondition compositecond) throws RuntimeException {
			return canCompositeConditionBeDisplayed(compositecond, iLevel);
		}

		@Override
		public Boolean visitIdCondition(CollectableIdCondition idcond) throws RuntimeException {
			return false;
		}

		@Override
		public Boolean visitSubCondition(CollectableSubCondition subcond) throws RuntimeException {
			return false;
		}

		@Override
		public Boolean visitReferencingCondition(ReferencingCollectableSearchCondition refcond) throws RuntimeException {
			return false;
		}

		@Override
        public Boolean visitIdListCondition(CollectableIdListCondition collectableIdListCondition) throws RuntimeException {
	        return false;
        }
	}	// inner class CanSearchConditionBeDisplayedVisitor

}	// class SearchConditionSubFormController
