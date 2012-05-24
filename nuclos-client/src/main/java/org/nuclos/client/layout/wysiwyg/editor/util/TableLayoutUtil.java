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
package org.nuclos.client.layout.wysiwyg.editor.util;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstraints;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.apache.log4j.Logger;
import org.nuclos.api.ui.Alignment;
import org.nuclos.api.ui.DefaultAlignment;
import org.nuclos.client.layout.wysiwyg.CollectableWYSIWYGLayoutEditor.WYSIWYGLayoutEditorChangeDescriptor;
import org.nuclos.client.layout.wysiwyg.WYSIWYGLayoutControllingPanel;
import org.nuclos.client.layout.wysiwyg.WYSIWYGStringsAndLabels.TABLELAYOUT_UTIL;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGCollectableComponent;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGCollectableLabel;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGCollectableTextArea;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGComponent;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGScrollPane;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGSplitPane;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGSubForm;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGTabbedPane;
import org.nuclos.client.layout.wysiwyg.component.properties.PropertiesPanel;
import org.nuclos.client.layout.wysiwyg.editor.ui.panels.WYSIWYGLayoutEditorPanel;
import org.nuclos.client.layout.wysiwyg.editor.util.mouselistener.PropertiesDisplayMouseListener;
import org.nuclos.client.layout.wysiwyg.editor.util.popupmenu.AlignmentDialog;
import org.nuclos.client.layout.wysiwyg.editor.util.popupmenu.TwoPartedAlignmentPanel;
import org.nuclos.client.layout.wysiwyg.editor.util.valueobjects.LayoutCell;
import org.nuclos.client.layout.wysiwyg.editor.util.valueobjects.TableLayoutPanel;
import org.nuclos.client.layout.wysiwyg.editor.util.valueobjects.layoutmlrules.LayoutMLRule;
import org.nuclos.client.layout.wysiwyg.editor.util.valueobjects.layoutmlrules.LayoutMLRuleAction;
import org.nuclos.client.layout.wysiwyg.editor.util.valueobjects.layoutmlrules.LayoutMLRuleActions;
import org.nuclos.client.ui.Errors;
import org.nuclos.common.NuclosBusinessException;
import org.nuclos.common2.StringUtils;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.exception.CommonFatalException;

/**
 * This class does all Actions that can be done on a {@link TableLayout}
 * 
 * It gives access to:
 * <ul>
 * <li>{@link #addCol(LayoutCell)}</li>
 * <li>{@link #addRow(LayoutCell)}</li>
 * <li>{@link #delCol(LayoutCell)}</li>
 * <li>{@link #delRow(LayoutCell)}</li>
 * <li>{@link #modifyTableLayoutSizes(double, boolean, LayoutCell, boolean)}</li>
 * </ul>
 * 
 * Everything done on the {@link TableLayout} is performed through this class.<br>
 * It is also utilized from {@link UndoRedoFunction}<br>
 *
 * <br>
 * Created by Novabit Informationssysteme GmbH <br>
 * Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 * 
 * @author <a href="mailto:hartmut.beckschulze@novabit.de">hartmut.beckschulze</a>
 * @version 01.00.00
 */
public class TableLayoutUtil {

	private static final Logger LOG = Logger.getLogger(TableLayoutUtil.class);

	private TableLayout tableLayout;
	private TableLayoutPanel container;
	private LayoutCell cellForEditing = null;

	private double completeLayoutWidth = 0;
	private double completeLayoutHeight = 0;
	
	public static final int ACTION_TOGGLE_STANDARDBORDER = -1302;
	
	/**
	 * Constructor
	 * 
	 * @param tableLayout
	 * @param container
	 */
	public TableLayoutUtil(TableLayout tableLayout, TableLayoutPanel container) {
		this.tableLayout = tableLayout;
		this.container = container;
	}
	
	/**
	 * 
	 * @return the Number of Rows
	 */
	public int getNumRows(){
		return tableLayout.getNumRow();
	}

	/**
	 * 
	 * @return the Number of Columns
	 */
	public int getNumColumns(){
		return tableLayout.getNumColumn();
	}

	
	/**
	 * 
	 * @return UndoRedoFunction
	 */
	public UndoRedoFunction getUndoRedoFunction() {
		UndoRedoFunction undoRedo = container.getUndoRedoFunction();
		return undoRedo;
	}

	/**
	 * @return TableLayoutPanel
	 */
	public TableLayoutPanel getContainer() {
		return this.container;
	}

	/**
	 * 
	 * @param cellForEditing
	 */
	public void setCellForEditing(LayoutCell cellForEditing) {
		this.cellForEditing = cellForEditing;
	}

	/**
	 * 
	 * @param mousePosition
	 */
	public void setCellForEditing(Point mousePosition) {
		this.cellForEditing = this.getLayoutCell(mousePosition);
	}

	/**
	 * 
	 * @return
	 */
	public LayoutCell getCellForEditing() {
		return this.cellForEditing;
	}

	/**
	 * 
	 * @param currentLayoutCell
	 */
	public void setCurrentLayoutCell(LayoutCell currentLayoutCell) {
		container.setCurrentLayoutCell(currentLayoutCell, this);
	}

	/**
	 * 
	 * @param mousePosition
	 */
	public void setCurrentLayoutCell(Point mousePosition) {
		setCurrentLayoutCell(this.getLayoutCell(mousePosition));
	}
	
	/**
	 * 
	 * @return
	 */
	public LayoutCell getCurrentLayoutCell() {
		return container.getCurrentLayoutCell();
	}

	/**
	 * 
	 * @param mousePosition
	 * @return
	 */
	public LayoutCell getLayoutCell(Point mousePosition) {

		LayoutCell cell = new LayoutCell();

		if (mousePosition != null) {

			double[] calculatedColumns;
			double[] calculatedRows;

			double[] columns = tableLayout.getColumn();
			calculatedColumns = calculateAbsoluteSize(columns, container.getWidth(), true);

			double[] rows = tableLayout.getRow();
			calculatedRows = calculateAbsoluteSize(rows, container.getHeight(), false);

			double x = 0;
			for (int i = 0; i < calculatedColumns.length; i++) {
				if (mousePosition.x > x) {
					cell.setCellX(i);
					cell.setCellDimensionsX((int) x);
					cell.setCellDimensionsWidth((int) calculatedColumns[i]);
					x = x + calculatedColumns[i];
					cell.setCellWidth(columns[i]);
				}
			}

			double y = 0;
			for (int i = 0; i < calculatedRows.length; i++) {
				if (mousePosition.y > y) {
					cell.setCellY(i);
					cell.setCellDimensionsY((int) y);
					cell.setCellDimensionsHeight((int) calculatedRows[i]);
					y = y + calculatedRows[i];
					cell.setCellHeight(rows[i]);
				}
			}

			cell.setCell2X(cell.getCellX());
			cell.setCell2Y(cell.getCellY());
		}
		return cell;
	}

	/**
	 * Returns the {@link LayoutCell} at the Position of cellX and cellY
	 * 
	 * @param cellX
	 * @param cellY
	 * @return LayoutCell
	 */
	public LayoutCell getLayoutCellByPosition(int cellX, int cellY) {
		LayoutCell cell = new LayoutCell();

		double[] calculatedColumns;
		double[] calculatedRows;

		double[] columns = tableLayout.getColumn();
		
		calculatedColumns = calculateAbsoluteSize(columns, container.getWidth(), true);

		double[] rows = tableLayout.getRow();
		calculatedRows = calculateAbsoluteSize(rows, container.getHeight(), false);

		double x = 0;
		for (int i = 0; i < calculatedColumns.length; i++) {
			if (i <= cellX) {
				cell.setCellX(i);
				if (i < cellX)
					cell.setCellDimensionsX((int) x);
				cell.setCellDimensionsWidth((int) calculatedColumns[i]);
				x = x + calculatedColumns[i];
				cell.setCellWidth(columns[i]);
			}
		}

		double y = 0;
		for (int i = 0; i < calculatedRows.length; i++) {
			if (i <= cellY) {
				cell.setCellY(i);
				if (i < cellY)
					cell.setCellDimensionsY((int) y);
				cell.setCellDimensionsHeight((int) calculatedRows[i]);
				y = y + calculatedRows[i];
				cell.setCellHeight(rows[i]);
			}
		}

		cell.setCell2X(cell.getCellX());
		cell.setCell2Y(cell.getCellY());

		return cell;
	}

	/**
	 * This Method creates the Layout EVERY new {@link TableLayoutPanel} has.
	 * 
	 * @see WYSIWYGLayoutControllingPanel
	 * @see WYSIWYGLayoutControllingPanel#createStandartLayout()
	 */
	public void createStandardLayout() {
		LayoutCell init = new LayoutCell();
		init.setCellX(0);
		init.setCellY(0);
		init.setCellHeight(TableLayout.FILL);
		init.setCellWidth(TableLayout.FILL);
		/** adding cols and rows with methods for undo/redo purpose */
		addRow(init);
		addCol(init);
		init.setCellHeight(InterfaceGuidelines.MARGIN_TOP);
		init.setCellWidth(InterfaceGuidelines.MARGIN_LEFT);
		addRow(init);
		addCol(init);
	}

	/**
	 * This Method clears the Layout, every Column/ Row is deleted and removes the Components from the Layout
	 */
	public void clearTableLayout() {
		int colums = tableLayout.getNumColumn();
		int rows = tableLayout.getNumRow();

		for (int i = 0; i < colums; i++) {
			tableLayout.deleteColumn(0);
		}
		for (int i = 0; i < rows; i++) {
			tableLayout.deleteRow(0);
		}

		Component[] comps = container.getComponents();

		for (int i = 0; i < comps.length; i++) {
			container.remove(comps[i]);
		}
	}

	/**
	 * 
	 * @return the calculated Width of the Layout
	 */
	public int getCalculatedLayoutWidth() {
		return (int) completeLayoutWidth;
	}

	/**
	 * 
	 * @return the calculated Height of the Layout
	 */
	public int getCalculatedLayoutHeight() {
		return (int) completeLayoutHeight;
	}

	/**
	 * This Method inserts a Component to the Layout.
	 * 
	 * @param c {@link WYSIWYGComponent} to Insert
	 * @param toInsertTo {@link LayoutCell} to insert Component in
	 */
	public void insertComponentTo(WYSIWYGComponent c, LayoutCell toInsertTo) {
		TableLayoutConstraints constraint = new TableLayoutConstraints();

		if (c instanceof DefaultAlignment) {
			final Alignment def = ((DefaultAlignment) c).getDefaultAlignment();
			constraint.hAlign = def.h.getInt();
			constraint.vAlign = def.v.getInt();
		} else {
			int[] alignment = new int[]{2, 1};
			// special behavior for subform's
			if(c instanceof WYSIWYGSubForm)
				alignment[1] = 2;
			
			constraint.hAlign = alignment[AlignmentDialog.HORIZONTAL_ALIGN];
			constraint.vAlign = alignment[AlignmentDialog.VERTICAL_ALIGN];
		}
		
		constraint.col1 = toInsertTo.getCellX();
		constraint.row1 = toInsertTo.getCellY();
		constraint.col2 = toInsertTo.getCell2X();
		constraint.row2 = toInsertTo.getCell2Y();

		insertComponentTo(c, constraint);
	}

	/**
	 * @see #insertComponentTo(WYSIWYGComponent, LayoutCell)
	 * @param c
	 * @param constraints
	 */
	public void insertComponentTo(WYSIWYGComponent c, TableLayoutConstraints constraints) {
		((Component) c).addMouseListener(container.getPropertiesMouseListener());
		//NUCLEUSINT-556
		((Component) c).addMouseListener(new PropertiesDisplayMouseListener( c, this));
		
//		LayoutCell cellComponentIsInserted = getLayoutCellByPosition(constraints.col1, constraints.row1);

		if (getUndoRedoFunction() != null)
			getUndoRedoFunction().loggingInsertComponentToCell(constraints, c, this);

		if (c instanceof WYSIWYGLayoutEditorPanel) {
			((WYSIWYGLayoutEditorPanel) c).setWYSIWYGLayoutEditorChangeDescriptor(getWYSIWYGLayoutEditorChangeDescriptor());
			/** enable some extra features that dont make sense for the main panel (like visible etc) */
			if (((WYSIWYGLayoutEditorPanel) c).getParentEditor() != null)
				((WYSIWYGLayoutEditorPanel) c).enablePropertiesForInlinePanels();
			constraints.hAlign = TableLayout.FULL;
			constraints.vAlign = TableLayout.FULL;
		} else if (c instanceof WYSIWYGTabbedPane) {
			((WYSIWYGTabbedPane) c).setWYSIWYGLayoutEditorChangeDescriptor(getWYSIWYGLayoutEditorChangeDescriptor());
			constraints.hAlign = TableLayout.FULL;
			constraints.vAlign = TableLayout.FULL;
		} else if (c instanceof WYSIWYGSplitPane) {
			((WYSIWYGSplitPane) c).setWYSIWYGLayoutEditorChangeDescriptor(getWYSIWYGLayoutEditorChangeDescriptor());
			constraints.hAlign = TableLayout.FULL;
			constraints.vAlign = TableLayout.FULL;
		} else if (c instanceof WYSIWYGScrollPane) {
			((WYSIWYGScrollPane) c).setWYSIWYGLayoutEditorChangeDescriptor(getWYSIWYGLayoutEditorChangeDescriptor());
			constraints.hAlign = TableLayout.FULL;
			constraints.vAlign = TableLayout.FULL;
		} else if (c instanceof WYSIWYGCollectableTextArea) {
			// textarea set to fill direct on insert
			constraints.hAlign = TableLayout.FULL;
			constraints.vAlign = TableLayout.FULL;
		}
		
		container.add((Component) c, constraints);
		
//		//NUCLEUSINT-280 adjusting the preferred size of container elements, this avoids the jumping cells on resize
//		if (c instanceof WYSIWYGTabbedPane || c instanceof WYSIWYGSubForm || c instanceof WYSIWYGSplitPane || c instanceof WYSIWYGScrollPane || c instanceof WYSIWYGLayoutEditorPanel) {
//			try {
//				c.setProperty(WYSIWYGComponent.PROPERTY_PREFFEREDSIZE, new PropertyValueDimension(cellComponentIsInserted.getCellDimensions().getSize()), Dimension.class);
//			} catch (CommonBusinessException e) {
//				log.error(e);
//			}
//		}

		notifyThatSomethingChanged();
	}

	/**
	 * This Method is used to move a {@link WYSIWYGComponent} to Position
	 * 
	 * @param c
	 * @param toInsertTo
	 */
	public void moveComponentTo(WYSIWYGComponent c, LayoutCell toInsertTo) {
		TableLayoutConstraints constraint = getConstraintForComponent(c);
		
		if (constraint == null)
			return;

		/** checking if constraints fit to target layout */
		constraint = checkIfConstraintContainesIllegalValues(c, constraint);
		/**
		 * check if there is something to do in the panel where the component
		 * actual lies
		 */
		changeToAbsoluteSizeForCellWithPreferredSize(c, constraint);

		constraint.col1 = toInsertTo.getCellX();
		constraint.col2 = toInsertTo.getCellX();
		constraint.row1 = toInsertTo.getCellY();
		constraint.row2 = toInsertTo.getCellY();

		moveComponentTo(c, constraint);
	}

	/**
	 * @see #moveComponentTo(WYSIWYGComponent, TableLayoutConstraints)
	 * @param c
	 * @param constraints
	 */
	public void moveComponentTo(WYSIWYGComponent c, TableLayoutConstraints constraints) {
		try {
			WYSIWYGLayoutEditorPanel parentEditor = c.getParentEditor();
			/**
			 * moving the component to the actual selected cell (could be the
			 * original panel or a nested one
			 */

			moveComponentTo(c, constraints, c.getParentEditor().getCurrentTableLayoutUtil());
			if (parentEditor != null)
				parentEditor.updateUI();

			notifyThatSomethingChanged();
		} catch (IllegalArgumentException ex) {
			// trying to add the parent to the child container... just do
			// nothing...
		}
	}
	
	/**
	 * @see #moveComponentTo(WYSIWYGComponent, TableLayoutConstraints)
	 * @param c
	 * @param constraints
	 * @param tableLayoutUtil
	 */
	public void moveComponentTo(WYSIWYGComponent c, TableLayoutConstraints constraints, TableLayoutUtil tableLayoutUtil) {
		/** logging the origin of the component */
		if (c.getParentEditor() != null) {
			getUndoRedoFunction().loggingMoveComponentToCell(getConstraintForComponent(c), c, c.getParentEditor().getTableLayoutUtil());
		}
		tableLayoutUtil.getContainer().add((Component) c, constraints);
		/** logging the destination of the component */
		getUndoRedoFunction().loggingMoveComponentToCell(constraints, c, tableLayoutUtil);
		
		WYSIWYGLayoutControllingPanel controller = null;
		try {
			controller = tableLayoutUtil.getContainer().getParentEditorPanel().getController();
		}
		catch(NuclosBusinessException e) {
			LOG.info("moveComponentTo: " + e);
			try {
				controller = ((WYSIWYGLayoutEditorPanel)c).getController();
			}
			catch(NuclosBusinessException e1) {
				Errors.getInstance().showExceptionDialog(null, e1);
			}
		}

		if (controller.preferencesForThisComponentShown(c)) {
				PropertiesPanel.showPropertiesForComponent(c, tableLayoutUtil);
		}
	}

	/**
	 * This Method changes the Alignment of a {@link WYSIWYGComponent}.
	 * <br>
	 * This is done {@link TwoPartedAlignmentPanel} or {@link AlignmentDialog}.
	 * 
	 * Changing the Alignment is done by changing {@link TableLayoutConstraints}
	 * @param c
	 * @param constraints
	 */
	public void changeComponentsAlignment(WYSIWYGComponent c, TableLayoutConstraints constraints) {
		try {
			/** logging the alignment before change */
			getUndoRedoFunction().loggingChangeComponentsAlignment(getConstraintForComponent(c), c, c.getParentEditor().getTableLayoutUtil());
			c.getParentEditor().getTableLayoutUtil().getContainer().add((Component) c, constraints);
			/** logging the change */
			getUndoRedoFunction().loggingChangeComponentsAlignment(constraints, c, c.getParentEditor().getTableLayoutUtil());
			notifyThatSomethingChanged();
		} catch (IllegalArgumentException ex) {
		}
	}

	/**
	 * This method removes a {@link WYSIWYGComponent} from the {@link TableLayoutPanel}.
	 * @param c
	 */
	public void removeComponentFromLayout(WYSIWYGComponent c) {
		removeComponentFromLayout(c, false);
	}

	/**
	 * This method removes a {@link WYSIWYGComponent} from the {@link TableLayoutPanel}.
	 * @param c
	 */
	public void removeComponentFromLayout(WYSIWYGComponent c, boolean bShowExceptions) {
		TableLayoutConstraints constraint = c.getParentEditor().getTableLayoutUtil().getConstraintForComponent(c);
		changeToAbsoluteSizeForCellWithPreferredSize(c, constraint );
		TableLayoutUtil tableLayoutUtil = c.getParentEditor().getTableLayoutUtil();

		WYSIWYGLayoutEditorPanel root = c.getParentEditor();
		while (root.getParentEditor() != null) {
			root = root.getParentEditor();
		}
		
		// Check initial focus component
		try {
			if (root.getInitialFocusComponent() != null) {
				if (c instanceof WYSIWYGSubForm) {
					if (!StringUtils.isNullOrEmpty(root.getInitialFocusComponent().getEntity())) {
						if (((String)c.getProperties().getProperty(WYSIWYGSubForm.PROPERTY_ENTITY).getValue()).equals(root.getInitialFocusComponent().getEntity())) {
							if (bShowExceptions)
								throw new Exception(TABLELAYOUT_UTIL.ERRORMESSAGE_SUBFORM_NOT_DELETABLE_TARGET_OF_INITIAL_FOCUS);
							root.setInitialFocusComponent(null); // @see NUCLOSINT-1468
						}
					}
				}
				else if (c instanceof WYSIWYGCollectableComponent) {
					if (((String)c.getProperties().getProperty(WYSIWYGCollectableComponent.PROPERTY_NAME).getValue()).equals(root.getInitialFocusComponent().getName())) {
						if (bShowExceptions)
							throw new Exception(TABLELAYOUT_UTIL.ERRORMESSAGE_COMPONENT_NOT_DELETABLE_TARGET_OF_INITIAL_FOCUS);
						root.setInitialFocusComponent(null); // @see NUCLOSINT-1468
					}
				}
			}
			
			// Check rules
			List<Object> wysiwygComponents = new ArrayList<Object>();
			root.getWYSIWYGComponents(WYSIWYGComponent.class, root, wysiwygComponents);
			
			for (Object o : wysiwygComponents) {
				if (o instanceof WYSIWYGComponent) {
					WYSIWYGComponent comp = (WYSIWYGComponent)o;
					if (comp.getLayoutMLRulesIfCapable() != null) {
						for (LayoutMLRule rule : new Vector<LayoutMLRule>(comp.getLayoutMLRulesIfCapable().getRules())) {
							LayoutMLRuleActions actions = rule.getLayoutMLRuleActions();
							for (LayoutMLRuleAction action : new Vector<LayoutMLRuleAction>(actions.getSingleActions())) {
								if (c instanceof WYSIWYGSubForm) {
									WYSIWYGSubForm subForm = (WYSIWYGSubForm)c;
									// NUCLEUSINT-436 action.getEntity may be null - but still valid
									if (action.getEntity() != null) { 
										if (action.getEntity().equals(subForm.getProperties().getProperty(WYSIWYGSubForm.PROPERTY_ENTITY).getValue())) {
											if (bShowExceptions)
												throw new Exception(TABLELAYOUT_UTIL.ERRORMESSAGE_SUBFORM_NOT_DELETEABLE_TARGET_OF_RULE);
											actions.removeActionFromActions(action); // @see NUCLOSINT-1468
										}
									}
								}
								else if (c instanceof WYSIWYGCollectableComponent) {
									WYSIWYGCollectableComponent collectableComponent = (WYSIWYGCollectableComponent)c;
									// a label is never a valid target of a rule, it has just the same entity as the rule component
									if(!(c instanceof WYSIWYGCollectableLabel)) {
										if(action.getTargetComponent().equals(
											collectableComponent.getProperties().getProperty(WYSIWYGCollectableComponent.PROPERTY_NAME).getValue())) {
											if (bShowExceptions)
												throw new Exception(TABLELAYOUT_UTIL.ERRORMESSAGE_COMPONENT_NOT_DELETABLE_TARGET_OF_RULE);
											actions.removeActionFromActions(action); // @see NUCLOSINT-1468
										}
									}
								}
							}

							if (rule.getLayoutMLRuleActions().getSingleActions().isEmpty()) {
								comp.getLayoutMLRulesIfCapable().clearRulesForComponent();
							}
						}
					}
				}
			}
		}
		catch (Exception ex) {
			LOG.error(ex);
			Errors.getInstance().showExceptionDialog((Component)c, ex);
			return;
		}
		
		getUndoRedoFunction().loggingDeleteComponentFromCell(constraint, tableLayoutUtil, c);
		c.getParentEditor().getTableLayoutUtil().container.remove((Component) c);

		notifyThatSomethingChanged();
	}

	/**
	 * This Method returns the {@link TableLayoutConstraints} for the {@link WYSIWYGComponent}.
	 * <br>
	 * In the {@link TableLayoutConstraints} the alignment of the {@link WYSIWYGComponent} is stored and its position in the {@link TableLayoutPanel}.
	 * @param c WYSIWYGComponent
	 * @return TableLayoutConstraints
	 */
	public TableLayoutConstraints getConstraintForComponent(WYSIWYGComponent c) throws CommonFatalException {
		TableLayoutConstraints constraints = c.getParentEditor().getTableLayoutUtil().tableLayout.getConstraints((Component) c);

		return constraints;
	}

	/**
	 * This Method corrects invalid {@link TableLayoutConstraints}.
	 * 
	 * It:
	 * <ul>
	 * <li> corrects invalid columns/ rows (<0 and > layoutcolumns/ rows)</li>
	 * <li> swaps the values if the starting cell is higher than the ending cell</li>
	 * </ul>
	 * 
	 * @param c
	 * @param constraints
	 * @return TableLayoutConstraints
	 */
	public TableLayoutConstraints checkIfConstraintContainesIllegalValues(WYSIWYGComponent c, TableLayoutConstraints constraints) {
		TableLayout componentsLayout = c.getParentEditor().getTableLayoutUtil().tableLayout;
		int numCols = componentsLayout.getNumColumn() - 1;
		int numRows = componentsLayout.getNumRow() - 1;
		int minCols = 0;
		int minRows = 0;

		int temp;

		if (numCols > 0)
			minCols = 1;
		if (numRows > 0)
			minRows = 1;

		//FIX NUCLEUSINT-279 <= 0
		if (constraints.col1 <= 0)
			constraints.col1 = minCols;

		//FIX NUCLEUSINT-279 <= 0
		if (constraints.col2 <= 0)
			constraints.col2 = minCols;

		if (constraints.col1 > numCols)
			constraints.col1 = numCols;

		if (constraints.col2 > numCols)
			constraints.col2 = numCols;

		if (constraints.col2 < constraints.col1) {
			temp = constraints.col1;
			constraints.col1 = constraints.col2;
			constraints.col2 = temp;
		}

		//FIX NUCLEUSINT-279 <= 0
		if (constraints.row1 <= 0)
			constraints.row1 = minRows;

		//FIX NUCLEUSINT-279 <= 0
		if (constraints.row2 <= 0)
			constraints.row2 = minRows;

		if (constraints.row1 > numRows)
			constraints.row1 = numRows;

		if (constraints.row2 > numRows)
			constraints.row2 = numRows;

		if (constraints.row2 < constraints.row1) {
			temp = constraints.row1;
			constraints.row1 = constraints.row2;
			constraints.row2 = temp;
		}

		return constraints;
	}

	/**
	 * This Method creates a {@link LayoutCell} when no {@link LayoutCell} is existing on that position, means outside the {@link TableLayout}
	 * 
	 * @see #createColumnBeside(Point, Rectangle, LayoutCell)
	 * @see #createRowBeneath(Point, Rectangle, LayoutCell)
	 * 
	 * @param mousePosition
	 * @param componentBounds
	 * @param cell
	 * @return
	 */
	public LayoutCell createCell(Point mousePosition, Rectangle componentBounds, LayoutCell cell) {
		LayoutCell toAdd = createColumnBeside(mousePosition, componentBounds, cell);
		toAdd = createRowBeneath(mousePosition, componentBounds, toAdd);

		mousePosition.x = mousePosition.x + 2;
		mousePosition.y = mousePosition.y + 2;

		return getLayoutCell(mousePosition);
	}

	/**
	 * This Method is called when a {@link WYSIWYGComponent} is dropped on the Layout and there is no {@link LayoutCell} at this position.
	 * Expands the {@link TableLayout} on two Columns, one to get to the Position and the one that fits the {@link WYSIWYGComponent}.
	 * 
	 * @see TableLayoutPanel#drop(java.awt.dnd.DropTargetDropEvent)
	 * @param mousePosition
	 * @param componentBounds
	 * @param cell
	 * @return LayoutCell
	 */
	public LayoutCell createCellOnTheRightSideWhereNoLayoutIs(Point mousePosition, Rectangle componentBounds, LayoutCell cell) {

		int heightCell1;
		int heightCell2;
		int heightCell3;

		LayoutCell toAdd = new LayoutCell();

		int width = mousePosition.x - getCalculatedLayoutWidth();

		if (tableLayout.getNumColumn() != 0)
			toAdd.setCellX(cell.getCellX() + 1);
		else
			toAdd.setCellX(cell.getCellX());
		toAdd.setCellWidth(width);

		//NUCLEUSINT-942 prevent massive cutting
		LayoutCell existing = checkIfThereIsAFittingCell(mousePosition, componentBounds);
		if (existing != null)
			return existing;
		
		addCol(toAdd);

		mousePosition.x = mousePosition.x + 2;
		mousePosition.y = mousePosition.y + 2;

		toAdd = getLayoutCell(mousePosition);

		heightCell1 = toAdd.getCellDimensions().height;

		toAdd.setCellX(toAdd.getCellX() + 1);
		toAdd.setCellWidth(componentBounds.getWidth());
		
		//NUCLEUSINT-942 prevent massive cutting
		existing = checkIfThereIsAFittingCell(mousePosition, componentBounds);
		if (existing != null)
			return existing;
		
		addCol(toAdd);

		//NUCLEUSINT-942 prevent massive cutting
		existing = checkIfThereIsAFittingCell(mousePosition, componentBounds);
		if (existing != null)
			return existing;
		
		toAdd.setCellHeight(componentBounds.y - toAdd.getCellDimensions().y);

		addRow(toAdd);

		heightCell2 = componentBounds.y - toAdd.getCellDimensions().y;

		toAdd.setCellY(toAdd.getCellY() + 1);
		toAdd.setCellHeight(componentBounds.height);

		//NUCLEUSINT-942 prevent massive cutting
		existing = checkIfThereIsAFittingCell(mousePosition, componentBounds);
		if (existing != null)
			return existing;
		
		addRow(toAdd);

		mousePosition.x = mousePosition.x + 2;
		mousePosition.y = mousePosition.y + 2;

		toAdd = getLayoutCell(mousePosition);
		toAdd.setCellY(toAdd.getCellY() + 1);

		heightCell3 = heightCell1 - heightCell2 - componentBounds.height;

		if (heightCell3 >= InterfaceGuidelines.MINIMUM_SIZE) {
			modifyTableLayoutSizes(heightCell3, false, toAdd, true);
		}

		mousePosition.x = mousePosition.x + 2;
		mousePosition.y = mousePosition.y + 2;

		return getLayoutCell(mousePosition);
	}
	
	/**
	 * This method checks if there is a {@link LayoutCell} that fits the needs of the {@link WYSIWYGComponent}.
	 * 
	 * Is meant for preventing too much cutting
	 * 
	 * @param mousePosition
	 * @param componentBounds
	 * @return {@link LayoutCell} is there is a fitting one, null if not
	 * NUCLEUSINT-942
	 */
	private LayoutCell checkIfThereIsAFittingCell(Point mousePosition, Rectangle componentBounds) {
		LayoutCell existing = getLayoutCell(mousePosition);
		if ((existing.getCellDimensions().getWidth() >= componentBounds.getWidth() - InterfaceGuidelines.TOLERANCE_WIDTH_EXISTING_CELL) &&
			(existing.getCellDimensions().getWidth() <= componentBounds.getWidth() + InterfaceGuidelines.TOLERANCE_WIDTH_EXISTING_CELL) &&
			(existing.getCellDimensions().getHeight() >= componentBounds.getHeight() - InterfaceGuidelines.TOLERANCE_HEIGHT_EXISTING_CELL) &&
			(existing.getCellDimensions().getHeight() <= componentBounds.getHeight() + InterfaceGuidelines.TOLERANCE_HEIGHT_EXISTING_CELL) )
			return existing;
		
		return null;
	}

	/**
	 * This Method expands the {@link TableLayout} on the Bottom Side.<br>
	 * If a {@link WYSIWYGComponent} is dropped to the end of the Layout and there is no {@link LayoutCell} at this position
	 * two new rows are added. One to reach the point where the {@link WYSIWYGComponent} starts and one that fits the Component.
	 * 
	 * @see TableLayoutPanel#drop(java.awt.dnd.DropTargetDropEvent)
	 * @param mousePosition
	 * @param componentBounds
	 * @param cell
	 * @return LayoutCell
	 */
	public LayoutCell createCellBelowWhereNoLayoutIs(Point mousePosition, Rectangle componentBounds, LayoutCell cell) {
		LayoutCell toAdd = new LayoutCell();

		if (tableLayout.getNumRow() != 0)
			toAdd.setCellY(cell.getCellY() + 1);
		else
			toAdd.setCellY(cell.getCellY());

		toAdd.setCellHeight(mousePosition.y - getCalculatedLayoutHeight());

		addRow(toAdd);

		toAdd.setCellY(toAdd.getCellY() + 1);
		toAdd.setCellHeight(componentBounds.height);

		addRow(toAdd);

		mousePosition.x = mousePosition.x + 2;
		mousePosition.y = mousePosition.y + 2;

		toAdd = getLayoutCell(mousePosition);

		if (toAdd.getCellDimensions().width + InterfaceGuidelines.TOLERANCE_WIDTH_EXISTING_CELL < componentBounds.width) {
			LOG.debug("cell is not huge enough - slicing");

			/** cell to the right */
			toAdd = getLayoutCellByPosition(toAdd.getCellX() + 1, toAdd.getCellY());

			if (toAdd.getCellDimensions().width + InterfaceGuidelines.TOLERANCE_WIDTH_EXISTING_CELL < componentBounds.width) {
				LOG.debug("cell to the right is also too small");

				toAdd.setCellX(toAdd.getCellX());
				toAdd.setCellWidth(componentBounds.getWidth());

				addCol(toAdd);

				toAdd = getLayoutCellByPosition(toAdd.getCellX(), toAdd.getCellY());

			}
		} else if (toAdd.getCellDimensions().width > componentBounds.width + InterfaceGuidelines.TOLERANCE_WIDTH_EXISTING_CELL) {
			LOG.debug("cell is oversized");

			if (!containesItemInColumn(toAdd.getCellX())) {
				LOG.debug("cell can be modified without messing up the layout");

				int cellsizeOriginal = toAdd.getCellDimensions().width;

				int cell1 = mousePosition.x - toAdd.getCellDimensions().x;

				if (cell1 < InterfaceGuidelines.MINIMUM_SIZE) {
					LOG.debug("cell to the left is smaller than minimum size");
					if (cell1 < 0) {
						LOG.debug("the cell is negative size");
						cell1 = 0;
					} else {
						LOG.debug("the cell is just smaller than the minimum size, correcting");
						cell1 = InterfaceGuidelines.MINIMUM_SIZE;
					}
				}

				int cell2 = cellsizeOriginal - cell1 - componentBounds.width;

				if (cell1 > 0) {
					LOG.debug("normal slicing");
					modifyTableLayoutSizes(cell1, true, toAdd, true);

					toAdd.setCellWidth(componentBounds.width);
					toAdd.setCellX(toAdd.getCellX() + 1);
					addCol(toAdd);

					toAdd.setCellWidth(cell2);
					toAdd.setCellX(toAdd.getCellX() + 1);
					addCol(toAdd);

					toAdd = getLayoutCellByPosition(toAdd.getCellX() - 2, toAdd.getCellY());
				} else {
					LOG.debug("just modify the cell and add another");
					modifyTableLayoutSizes(componentBounds.width, true, toAdd, true);

					toAdd.setCellWidth(cell2);
					toAdd.setCellX(toAdd.getCellX() + 1);
					addCol(toAdd);

					toAdd = getLayoutCellByPosition(toAdd.getCellX() - 1, toAdd.getCellY());
				}
			}
		}
		return toAdd;
	}

	/**
	 * This Method creates a Column on the right side.<br>
	 * This is called if the {@link LayoutCell} is needed inside the {@link TableLayout}
	 * 
	 * @param mousePosition
	 * @param componentBounds
	 * @param cell
	 * @return LayoutCell
	 */ 
	public LayoutCell createColumnBeside(Point mousePosition, Rectangle componentBounds, LayoutCell cell) {
		LayoutCell toAdd = cell;

		toAdd.setCellWidth(componentBounds.getWidth());
		addCol(toAdd);

		toAdd.setCellWidth(componentBounds.x - cell.getCellDimensions().x);
		toAdd.setCellY(toAdd.getCellY() + 1);
		addCol(toAdd);

		mousePosition.x = mousePosition.x + 2;
		mousePosition.y = mousePosition.y + 2;

		setCurrentLayoutCell(mousePosition);
		return getCurrentLayoutCell();
	}

	/**
	 * This Method creates a Row below.<br>
	 * Its called if the {@link LayoutCell} is inside the {@link TableLayout}.
	 * 
	 * @param mousePosition
	 * @param componentBounds
	 * @param cell
	 * @return
	 */
	public LayoutCell createRowBeneath(Point mousePosition, Rectangle componentBounds, LayoutCell cell) {
		LayoutCell toAdd = cell;

		toAdd.setCellHeight(componentBounds.getHeight());
		addRow(toAdd);

		toAdd.setCellHeight(componentBounds.y - cell.getCellDimensions().y);
		toAdd.setCellX(toAdd.getCellX() + 1);
		addRow(toAdd);

		mousePosition.x = mousePosition.x + 2;
		mousePosition.y = mousePosition.y + 2;

		setCurrentLayoutCell(mousePosition);
		return getCurrentLayoutCell();
	}

	/**
	 * This Method creates a Row.<br>
	 * The Height is read from a {@link JOptionPane#showInputDialog(Object)}
	 * Calls {@link #addRow(LayoutCell)}
	 * @param mousePosition
	 */
	public void addRow(Point mousePosition) {
		String strHeight = JOptionPane.showInputDialog(TABLELAYOUT_UTIL.MESSAGE_INPUT_DIALOG_ENTER_HEIGHT);
		double height = Double.parseDouble(strHeight);

		LayoutCell currentCell = getLayoutCell(mousePosition);

		LayoutCell toAdd = currentCell;
		toAdd.setCellHeight(height);
		addRow(toAdd);
	}

	/**
	 * This Method simply adds a Row and logs the Change with {@link UndoRedoFunction#loggingAddRow(LayoutCell, TableLayoutUtil)}
	 * @param toAdd
	 */
	public void addRow(LayoutCell toAdd) {
		tableLayout.insertRow(toAdd.getCellY(), toAdd.getCellHeight());
		if (getUndoRedoFunction() != null)
			getUndoRedoFunction().loggingAddRow(toAdd, this);

		setCurrentLayoutCell(new Point());
		notifyThatSomethingChanged();
	}

	/**
	 * This Method creates a Column.<br>
	 * The width is read from a {@link JOptionPane#showInputDialog(Object)};
	 * Calls {@link #addCol(LayoutCell)}
	 * @param mousePosition
	 */
	public void addCol(Point mousePosition) {
		String strWidth = JOptionPane.showInputDialog(TABLELAYOUT_UTIL.MESSAGE_INPUT_DIALOG_ENTER_WIDTH);
		double width = Double.parseDouble(strWidth);

		LayoutCell currentCell = getLayoutCell(mousePosition);

		LayoutCell toAdd = currentCell;
		toAdd.setCellWidth(width);
		addCol(toAdd);
	}

	/**
	 * This method adds a Column and logs the Change with {@link UndoRedoFunction#loggingAddColumn(LayoutCell, TableLayoutUtil)}
	 * @param toAdd
	 */
	public void addCol(LayoutCell toAdd) {
		tableLayout.insertColumn(toAdd.getCellX(), toAdd.getCellWidth());
		if (getUndoRedoFunction() != null)
			getUndoRedoFunction().loggingAddColumn(toAdd, this);

		setCurrentLayoutCell(new Point());
		notifyThatSomethingChanged();
	}

	/**
	 * This Method deletes a Column <br>
	 * calls {@link #delCol(LayoutCell)}
	 * @param mousePosition
	 */
	public void delCol(Point mousePosition) {
		LayoutCell toDelete = getLayoutCell(mousePosition);
		delCol(toDelete);
	}

	/**
	 * This method deletes a Column from the Layout.<br>
	 * Logs the Change with {@link UndoRedoFunction#loggingDeleteColumn(LayoutCell, TableLayoutUtil)}
	 * @param toDelete
	 */
	public void delCol(LayoutCell toDelete) {
		int x = toDelete.getCellX();
		if (tableLayout.getNumColumn() > 0) {
			if (!containesItemInColumn(x)) {
				tableLayout.deleteColumn(x);
				getUndoRedoFunction().loggingDeleteColumn(toDelete, this);
			}
		}
		notifyThatSomethingChanged();
	}

	/**
	 * This Method deletes a Row<br>
	 * calls {@link #delRow(LayoutCell)}
	 * @param mousePosition
	 */
	public void delRow(Point mousePosition) {
		LayoutCell toDelete = getLayoutCell(mousePosition);
		delRow(toDelete);
	}

	/**
	 * This Method deletes a Row from the Layout.<br>
	 * Logs the Changes with {@link UndoRedoFunction#loggingDeleteRow(LayoutCell, TableLayoutUtil)}
	 * @param toDelete
	 */
	public void delRow(LayoutCell toDelete) {
		int y = toDelete.getCellY();
		if (tableLayout.getNumRow() > 0) {
			if (!containesItemInRow(y)) {
				tableLayout.deleteRow(y);
				if(getUndoRedoFunction() != null)
					getUndoRedoFunction().loggingDeleteRow(toDelete, this);
			}
		}
		notifyThatSomethingChanged();
	}

	/**
	 * This Method is used to modify the {@link TableLayout} Columns and Rows.
	 * 
	 * {@link #modifyTableLayoutSizesOnlyColumns(LayoutCell, double, boolean)}
	 * {@link #modifyTableLayoutSizesOnlyRows(LayoutCell, double, boolean)}
	 * @param value
	 * @param col
	 * @param cellToChange
	 * @param automatic
	 */
	public void modifyTableLayoutSizes(double value, boolean col, LayoutCell cellToChange, boolean automatic) {
		if (col) {
			modifyTableLayoutSizesOnlyColumns(cellToChange, value, automatic, true);
		} else {
			modifyTableLayoutSizesOnlyRows(cellToChange, value, automatic, true);
		}
		setCurrentLayoutCell(new Point());
		container.updateUI();
		notifyThatSomethingChanged();
	}
	
	public void modifyTableLayoutSizes(double value, boolean col, LayoutCell cellToChange, boolean automatic, boolean undoredo) {
		if (col) {
			modifyTableLayoutSizesOnlyColumns(cellToChange, value, automatic, undoredo);
		} else {
			modifyTableLayoutSizesOnlyRows(cellToChange, value, automatic, undoredo);
		}
		setCurrentLayoutCell(new Point());
		container.updateUI();
		notifyThatSomethingChanged();
	}
	
	/**
	 * Externalized Method, changes the Size of Columns<br>
	 * Logs the Change with {@link UndoRedoFunction#loggingChangeWidthOfColumn(LayoutCell, TableLayoutUtil, boolean)}
	 * @see TableLayoutUtil#modifyTableLayoutSizes(double, boolean, LayoutCell, boolean)
	 * @param cellToChange
	 * @param value
	 * @param automatic
	 */
	private void modifyTableLayoutSizesOnlyColumns(LayoutCell cellToChange, double value, boolean automatic, boolean undoredo){
		double[] newCols = new double[tableLayout.getNumColumn()];
		for (int i = 0; i < tableLayout.getNumColumn(); i++) {
			newCols[i] = tableLayout.getColumn(i);
			if (i == cellToChange.getCellX()) {
				if (i == 0) {
					if (value == ACTION_TOGGLE_STANDARDBORDER) {
						// override standardsizes
						value = 0;
					}else if (value < InterfaceGuidelines.MARGIN_LEFT)
						value = InterfaceGuidelines.MARGIN_LEFT;
				}
				cellToChange.setCellWidth(newCols[i]);
				if(undoredo)
					getUndoRedoFunction().loggingChangeWidthOfColumn(cellToChange, this, automatic);
				newCols[i] = value;
				cellToChange.setCellWidth(value);
			}
		}
		tableLayout.setColumn(newCols);
		if(undoredo)
			getUndoRedoFunction().loggingChangeWidthOfColumn(cellToChange, this, automatic);
	}
	
	/**
	 * Externalized Method, changes the Size of Rows<br>
	 * Logs the Change with {@link UndoRedoFunction#loggingChangeHeightOfRow(LayoutCell, TableLayoutUtil, boolean)}
	 * @see TableLayoutUtil#modifyTableLayoutSizes(double, boolean, LayoutCell, boolean)
	 * @param cellToChange
	 * @param value
	 * @param automatic
	 */
	private void modifyTableLayoutSizesOnlyRows(LayoutCell cellToChange, double value, boolean automatic, boolean undoredo){
		double[] newRows = new double[tableLayout.getNumRow()];
		for (int i = 0; i < tableLayout.getNumRow(); i++) {
			newRows[i] = tableLayout.getRow(i);
			if (i == cellToChange.getCellY()) {
				if (i == 0) {
					if (value == ACTION_TOGGLE_STANDARDBORDER) {
						// override standardsizes
						value = 0;
					}else if (value < InterfaceGuidelines.MARGIN_TOP)
						value = InterfaceGuidelines.MARGIN_TOP;
				}
				cellToChange.setCellHeight(newRows[i]);
				if(undoredo)
					getUndoRedoFunction().loggingChangeHeightOfRow(cellToChange, this, automatic);
				newRows[i] = value;
				cellToChange.setCellHeight(value);
			}
		}
		tableLayout.setRow(newRows);
		if(undoredo)
			getUndoRedoFunction().loggingChangeHeightOfRow(cellToChange, this, automatic);
	}

	/**
	 * This Method calculates the "real" Size of the colums or rows.<br>
	 * Everything shown on the {@link TableLayoutPanel} is calculated
	 * @param elements
	 * @param completeSpace
	 * @param colums
	 * @return double[] with calculated sizes
	 */
	public double[] calculateAbsoluteSize(double[] elements, int completeSpace, boolean colums) {
		double[] values = elements.clone();
		double restSpace = completeSpace;
		int i = 0;

		int elementFound = -1;

		for (i = 0; i < values.length; i++) {
			if (values[i] == TableLayout.PREFERRED || values[i] == TableLayout.MINIMUM)
				elementFound = i;
		}

		while (elementFound != -1) {
			if (container.getComponents().length > 0) {
				int prefSizeComponent = Integer.MIN_VALUE;
				int minSizeComponent = Integer.MAX_VALUE;
				Component[] items = container.getComponents();
				TableLayoutConstraints constraint;

				if (colums) {
					for (i = 0; i < items.length; i++) {
						constraint = tableLayout.getConstraints(items[i]);
						if (constraint.col1 == elementFound) {
							Dimension prefSize = items[i].getPreferredSize();
							Dimension minSize = items[i].getMinimumSize();
							if (prefSizeComponent < prefSize.width) {
								prefSizeComponent = prefSize.width;
							}
							if (minSizeComponent > minSize.width) {
								minSizeComponent = minSize.width;
							}
						}
					}
				} else {
					for (i = 0; i < items.length; i++) {
						constraint = tableLayout.getConstraints(items[i]);
						if (constraint.row1 == elementFound) {
							Dimension prefSize = items[i].getPreferredSize();
							Dimension minSize = items[i].getMinimumSize();
							if (prefSizeComponent < prefSize.height) {
								prefSizeComponent = prefSize.height;
							}
							if (minSizeComponent > minSize.height) {
								minSizeComponent = minSize.height;
							}
						}
					}
				}
				if (values[elementFound] == TableLayout.PREFERRED) {
					values[elementFound] = prefSizeComponent;
				} else if (values[elementFound] == TableLayout.MINIMUM) {
					values[elementFound] = minSizeComponent;
				}

			} else {
				/**
				 * there is no component, but a preferred size set for
				 * column/row, size is 0
				 */
				values[elementFound] = 0;
			}

			elementFound = -1;
			for (i = 0; i < values.length; i++) {
				if (values[i] == TableLayout.PREFERRED || values[i] == TableLayout.MINIMUM)
					elementFound = i;
			}
		}

		for (i = 0; i < values.length; i++) {
			if (values[i] >= 1)
				restSpace = restSpace - values[i];
		}

		for (i = 0; i < values.length; i++) {
			if (values[i] > 0 && values[i] < 1) {
				values[i] = values[i] * restSpace;
			}
		}

		restSpace = completeSpace;

		for (i = 0; i < values.length; i++) {
			if (values[i] > 0)
				restSpace = restSpace - values[i];
		}

		/**
		 * if there are more than one FILL every fill gets the equal percentual
		 * size of the restspace
		 */
		int fillCount = 0;

		for (i = 0; i < values.length; i++) {
			if (values[i] == TableLayout.FILL)
				fillCount++;
		}
		if (fillCount > 0) {
			for (i = 0; i < values.length; i++) {
				if (values[i] == TableLayout.FILL)
					values[i] = restSpace / fillCount;
			}
		}

		return values;
	}

	/**
	 * This Method ensures that a {@link LayoutCell} with a {@link WYSIWYGComponent} with set relative Size does not get a size of 0 when the Component is removed/ moved
	 * 
	 * @see TableLayoutUtil#moveComponentTo(WYSIWYGComponent, LayoutCell)
	 * @see TableLayoutUtil#removeComponentFromLayout(WYSIWYGComponent)
	 * @param c
	 * @param constraint
	 */
	public void changeToAbsoluteSizeForCellWithPreferredSize(WYSIWYGComponent c, TableLayoutConstraints constraint) {
		LayoutCell cellToChange = null;

		TableLayout componentsLayout = c.getParentEditor().getTableLayoutUtil().tableLayout;
		TableLayoutUtil componentsUtil = c.getParentEditor().getTableLayoutUtil();

		boolean actionCol1 = isChangingCellSizeActionRequiredColumn(componentsLayout, constraint.col1);
		boolean actionCol2 = isChangingCellSizeActionRequiredColumn(componentsLayout, constraint.col2);
		boolean actionRow1 = isChangingCellSizeActionRequiredRow(componentsLayout, constraint.row1);
		boolean actionRow2 = isChangingCellSizeActionRequiredRow(componentsLayout, constraint.row2);

		if (actionCol1) {
			cellToChange = componentsUtil.getLayoutCellByPosition(constraint.col1, 0);
			componentsUtil.modifyTableLayoutSizes(cellToChange.getCellDimensions().width, true, cellToChange, true);
		}
		if (actionCol2) {
			cellToChange = componentsUtil.getLayoutCellByPosition(constraint.col2, 0);
			componentsUtil.modifyTableLayoutSizes(cellToChange.getCellDimensions().width, true, cellToChange, true);
		}
		if (actionRow1) {
			cellToChange = componentsUtil.getLayoutCellByPosition(0, constraint.row2);
			componentsUtil.modifyTableLayoutSizes(cellToChange.getCellDimensions().height, false, cellToChange, true);
		}
		if (actionRow2) {
			cellToChange = componentsUtil.getLayoutCellByPosition(0, constraint.row2);
			componentsUtil.modifyTableLayoutSizes(cellToChange.getCellDimensions().height, false, cellToChange, true);
		}
	}

	/**
	 * This Method checks if the Column has a relative Size and should be modified.<br>
	 * called by {@link #changeToAbsoluteSizeForCellWithPreferredSize(WYSIWYGComponent, TableLayoutConstraints)}
	 * @param layout
	 * @param col
	 * @return true if relative Layoutsize found
	 */
	private boolean isChangingCellSizeActionRequiredColumn(TableLayout layout, int col) {
		boolean actionRequired = false;
		if (layout.getColumn(col) == TableLayout.PREFERRED)
			actionRequired = true;
		if (layout.getColumn(col) == TableLayout.MINIMUM)
			actionRequired = true;
		return actionRequired;
	}

	/**
	 * This Method checks if the Row has a relative Size and should be modified.<br>
	 * called by {@link #changeToAbsoluteSizeForCellWithPreferredSize(WYSIWYGComponent, TableLayoutConstraints)}
	 * @param layout
	 * @param row
	 * @return true if relative Layoutsize found
	 */
	private boolean isChangingCellSizeActionRequiredRow(TableLayout layout, int row) {
		boolean actionRequired = false;
		if (layout.getRow(row) == TableLayout.PREFERRED)
			actionRequired = true;
		if (layout.getRow(row) == TableLayout.MINIMUM)
			actionRequired = true;
		return actionRequired;
	}

	/**
	 * This Method gets the minimum Height for a Row.<br>
	 * It checks if {@link WYSIWYGComponent} are contained and gets the smallest Size
	 * @param row
	 * @return the minimum Height for this Row
	 */
	public int getMinimumHeightForRow(int row) {
		int size = Integer.MAX_VALUE;

		TableLayoutConstraints constraint;
		Component[] comp = container.getComponents();
		for (int i = 0; i < comp.length; i++) {
			constraint = tableLayout.getConstraints(comp[i]);
			if (constraint.row1 == row || constraint.row2 == row) {
				if (comp[i].getMinimumSize().height < size) {
					size = comp[i].getMinimumSize().height;
				}
			}
		}

		size = size - getRealSizeForComponentOverMultipleRows(row);

		if (size == Integer.MAX_VALUE || size < InterfaceGuidelines.MINIMUM_SIZE)
			size = InterfaceGuidelines.MINIMUM_SIZE;

		return size;
	}

	/**
	 * This Method gets the minimum Width for a Column.<br>
	 * It checks if {@link WYSIWYGComponent} are contained and gets the smallest Size
	 * @param column
	 * @return the minimum width for this column
	 */
	public int getMinimumWidthForColumn(int column) {
		int size = Integer.MAX_VALUE;

		TableLayoutConstraints constraint;
		Component[] comp = container.getComponents();

		for (int i = 0; i < comp.length; i++) {
			constraint = tableLayout.getConstraints(comp[i]);
			if (constraint.col1 == column || constraint.col2 == column) {
				if (comp[i].getMinimumSize().width < size) {
					size = comp[i].getMinimumSize().width;
				}
			}
		}

		size = size - getRealSizeForComponentOverMultipleColumns(column);

		if (size == Integer.MAX_VALUE || size < InterfaceGuidelines.MINIMUM_SIZE)
			size = InterfaceGuidelines.MINIMUM_SIZE;

		return size;
	}

	/**
	 * This Method returns the preferred Size for this Row.<br>
	 * Checks if there are {@link WYSIWYGComponent} in this row and calculates their preferred Size.
	 * 
	 * @param row
	 * @return the preferred height for this row
	 */
	public int getPreferredHeightForRow(int row) {
		int size = Integer.MIN_VALUE;

		TableLayoutConstraints constraint;
		Component[] comp = container.getComponents();
		if (tableLayout.getNumRow() >= row) {
			for (int i = 0; i < comp.length; i++) {
				constraint = tableLayout.getConstraints(comp[i]);
				if (constraint.row1 == row || constraint.row2 == row) {
					if (comp[i].getPreferredSize().height > size) {
						size = comp[i].getPreferredSize().height;
					}
				}
			}
		}

		size = size - getRealSizeForComponentOverMultipleRows(row);

		if (size == Integer.MAX_VALUE || size < InterfaceGuidelines.MINIMUM_SIZE)
			size = InterfaceGuidelines.MINIMUM_SIZE;

		int multipleItemSize = getHeightForMultipleComponentsInRow(row);
		if (multipleItemSize > size)
			size = multipleItemSize;

		return size;
	}

	/**
	 * This Method returns the preferred Size for this Column.<br>
	 * Checks if there are {@link WYSIWYGComponent} in this column and calculates their preferred Size.
	 * 
	 * @param column
	 * @return the preferred width for this column
	 */
	public int getPreferredWidthForColumn(int column) {
		int size = Integer.MIN_VALUE;

		TableLayoutConstraints constraint;
		Component[] comp = container.getComponents();

		for (int i = 0; i < comp.length; i++) {
			constraint = tableLayout.getConstraints(comp[i]);
			if (constraint.col1 == column || constraint.col2 == column) {
				if (comp[i].getPreferredSize().width > size) {
					size = comp[i].getPreferredSize().width;
				}
			}
		}

		size = size - getRealSizeForComponentOverMultipleColumns(column);

		if (size == Integer.MAX_VALUE || size < InterfaceGuidelines.MINIMUM_SIZE)
			size = InterfaceGuidelines.MINIMUM_SIZE;

		int multipleItemSize = getWidthForMultipleComponentsInColumn(column);
		if (multipleItemSize > size)
			size = multipleItemSize;

		return size;
	}

	/**
	 * This Method calculates the height for a row containing multiple {@link WYSIWYGComponent}
	 * @param row
	 * @return the calculated height
	 */
	public int getHeightForMultipleComponentsInRow(int row) {
		final int TOP = 0;
		final int CENTER = 1;
		final int BOTTOM = 2;
		final int FULL = 3;
		final int COUNTER = 4;

		int[][] dimensions = new int[tableLayout.getNumRow()][5];

		for (int i = 0; i < dimensions.length; i++) {
			for (int j = 0; j < dimensions[i].length; j++) {
				dimensions[i][j] = -InterfaceGuidelines.MARGIN_BETWEEN;
			}
			dimensions[i][COUNTER] = 0;
		}

		TableLayoutConstraints constraint;
		Component[] comp = container.getComponents();

		for (int i = 0; i < comp.length; i++) {
			constraint = tableLayout.getConstraints(comp[i]);
			if (constraint.row1 == row && constraint.row2 == row) {
				if (constraint.col1 == constraint.col2) {
					switch (constraint.vAlign) {
						case TableLayout.TOP :
							dimensions[constraint.row1][COUNTER]++;
							if (comp[i].getPreferredSize().height > dimensions[constraint.row1][TOP])
								dimensions[constraint.row1][TOP] = comp[i].getPreferredSize().height;
							break;
						case TableLayout.CENTER :
							dimensions[constraint.row1][COUNTER]++;
							if (comp[i].getPreferredSize().height > dimensions[constraint.row1][CENTER])
								dimensions[constraint.row1][CENTER] = comp[i].getPreferredSize().height;
							break;
						case TableLayout.BOTTOM :
							dimensions[constraint.row1][COUNTER]++;
							if (comp[i].getPreferredSize().height > dimensions[constraint.row1][BOTTOM])
								dimensions[constraint.row1][BOTTOM] = comp[i].getPreferredSize().height;
							break;
						case TableLayout.FULL :
							dimensions[constraint.row1][COUNTER]++;
							if (comp[i].getPreferredSize().height > dimensions[constraint.row1][FULL])
								dimensions[constraint.row1][FULL] = comp[i].getPreferredSize().height;
							break;
					}
				}
			}
		}

		int value = Integer.MIN_VALUE;

		for (int i = 0; i < dimensions.length; i++) {
			value = Integer.MIN_VALUE;

			if (dimensions[i][COUNTER] == 1) {
				if (dimensions[i][TOP] > value)
					value = dimensions[i][TOP];
				if (dimensions[i][CENTER] > value)
					value = dimensions[i][CENTER];
				if (dimensions[i][BOTTOM] > value)
					value = dimensions[i][BOTTOM];
				if (dimensions[i][FULL] > value)
					value = dimensions[i][FULL];
			} else if (dimensions[i][COUNTER] > 1) {
				if (dimensions[i][CENTER] > 0) {
					if (dimensions[i][TOP] < 1 && dimensions[i][BOTTOM] < 1) {
						if (dimensions[i][FULL] > dimensions[i][CENTER])
							value = dimensions[i][FULL];
						else
							value = dimensions[i][CENTER];
					} else {
						if (dimensions[i][TOP] > dimensions[i][BOTTOM])
							value = dimensions[i][TOP];
						else
							value = dimensions[i][BOTTOM];

						if (value < dimensions[i][FULL])
							value = dimensions[i][FULL];

						value = dimensions[i][CENTER] + (2 * value) + 2 * InterfaceGuidelines.MARGIN_BETWEEN;
					}
				} else {
					value = dimensions[i][TOP] + dimensions[i][CENTER] + dimensions[i][BOTTOM] + dimensions[i][FULL] + (--dimensions[i][COUNTER] * InterfaceGuidelines.MARGIN_BETWEEN);
				}
			}
			dimensions[i][COUNTER] = value;
		}

		value = Integer.MIN_VALUE;
		for (int i = 0; i < dimensions.length; i++) {
			if (value < dimensions[i][COUNTER])
				value = dimensions[i][COUNTER];
		}
		return value;
	}

	/**
	 * This Method calculates the Width for a Colum containing multiple {@link WYSIWYGComponent}
	 * @param column
	 * @return the calulated width for this column 
	 */
	public int getWidthForMultipleComponentsInColumn(int column) {
		final int LEFT = 0;
		final int CENTER = 1;
		final int RIGHT = 2;
		final int FULL = 3;
		final int COUNTER = 4;

		int[][] dimensions = new int[tableLayout.getNumColumn()][5];

		for (int i = 0; i < dimensions.length; i++) {
			for (int j = 0; j < dimensions[i].length; j++) {
				dimensions[i][j] = 0;
			}
		}

		TableLayoutConstraints constraint;
		Component[] comp = container.getComponents();

		for (int i = 0; i < comp.length; i++) {
			constraint = tableLayout.getConstraints(comp[i]);
			if (constraint.col1 == column && constraint.col2 == column && (constraint.row1 == constraint.row2)) {
				switch (constraint.hAlign) {
					case TableLayout.LEFT :
						dimensions[constraint.col1][COUNTER]++;
						if (comp[i].getPreferredSize().width > dimensions[constraint.col1][LEFT])
							dimensions[constraint.col1][LEFT] = comp[i].getPreferredSize().width;
						break;
					case TableLayout.CENTER :
						dimensions[constraint.col1][COUNTER]++;
						if (comp[i].getPreferredSize().width > dimensions[constraint.col1][CENTER])
							dimensions[constraint.col1][CENTER] = comp[i].getPreferredSize().width;
						break;
					case TableLayout.RIGHT :
						dimensions[constraint.col1][COUNTER]++;
						if (comp[i].getPreferredSize().width > dimensions[constraint.col1][RIGHT])
							dimensions[constraint.col1][RIGHT] = comp[i].getPreferredSize().width;
						break;
					case TableLayout.FULL :
						dimensions[constraint.col1][COUNTER]++;
						if (comp[i].getPreferredSize().width > dimensions[constraint.col1][FULL])
							dimensions[constraint.col1][FULL] = comp[i].getPreferredSize().width;
						break;
				}
			}
		}

		int value = Integer.MIN_VALUE;

		for (int i = 0; i < dimensions.length; i++) {
			value = Integer.MIN_VALUE;
			if (dimensions[i][COUNTER] == 1) {
				if (dimensions[i][LEFT] > 0) {
					value = dimensions[i][LEFT];
				} else if (dimensions[i][CENTER] > 0) {
					value = dimensions[i][CENTER];
				} else if (dimensions[i][RIGHT] > 0) {
					value = dimensions[i][RIGHT];
				} else if (dimensions[i][FULL] > 0) {
					value = dimensions[i][FULL];
				}
			} else if (dimensions[i][COUNTER] > 1) {
				if (dimensions[i][CENTER] > 0) {
					int temp = 0;
					if (dimensions[i][LEFT] >= dimensions[i][RIGHT])
						temp = dimensions[i][LEFT];
					else
						temp = dimensions[i][RIGHT];

					temp = dimensions[i][CENTER] + (2 * temp) + (2 * InterfaceGuidelines.MARGIN_BETWEEN);

					if (value < temp)
						value = temp;
				} else {
					if (dimensions[i][LEFT] > 0 && dimensions[i][RIGHT] > 0)
						value = dimensions[i][LEFT] + dimensions[i][RIGHT] + InterfaceGuidelines.MARGIN_BETWEEN;
					else {
						if (dimensions[i][RIGHT] > dimensions[i][FULL])
							value = dimensions[i][RIGHT];
						else
							value = dimensions[i][FULL];
						if (value < dimensions[i][LEFT])
							value = dimensions[i][LEFT];
					}
				}
			}
			dimensions[i][COUNTER] = value;
		}

		value = Integer.MIN_VALUE;
		for (int i = 0; i < dimensions.length; i++) {
			if (value < dimensions[i][COUNTER])
				value = dimensions[i][COUNTER];
		}

		return value;
	}

	/**
	 * This Method calculates the real size for a {@link WYSIWYGComponent} which spans over more than one row
	 * @param row
	 * @return the calculated height
	 */
	public int getRealSizeForComponentOverMultipleRows(int row) {
		double sum = 0;
		TableLayoutConstraints constraint;
		Component[] comp = container.getComponents();
		for (int i = 0; i < comp.length; i++) {
			constraint = tableLayout.getConstraints(comp[i]);
			if ((constraint.row2 > constraint.row1) && (constraint.row1 == row || constraint.row2 == row)) {
				double[] rows = tableLayout.getRow();
				rows = calculateAbsoluteSize(rows, getCalculatedLayoutHeight(), false);
				for (int j = constraint.row1; j <= constraint.row2; j++) {
					if (j != row)
						sum = sum + rows[j];
				}
			}
		}
		return (int) sum;
	}

	/**
	 * This Method calculates the Size of a {@link WYSIWYGComponent} spanning over multiple columns
	 * @param column
	 * @return the calculated width
	 */
	public int getRealSizeForComponentOverMultipleColumns(int column) {
		double sum = 0;
		TableLayoutConstraints constraint;
		Component[] comp = container.getComponents();
		for (int i = 0; i < comp.length; i++) {
			constraint = tableLayout.getConstraints(comp[i]);
			if ((constraint.col2 > constraint.col1) && (constraint.col1 == column || constraint.col2 == column)) {
				double[] cols = tableLayout.getColumn();
				cols = calculateAbsoluteSize(cols, getCalculatedLayoutWidth(), true);
				for (int j = (constraint.col1); j <= (constraint.col2) && j < cols.length; j++) {
					if (column != j)
						sum = sum + cols[j];
				}
			}
		}
		return (int) sum;
	}

	/**
	 * This Method checks if a Row contains a {@link WYSIWYGComponent}
	 * 
	 * @param row
	 * @return true if {@link WYSIWYGComponent} found
	 */
	public boolean containesItemInRow(int row) {
		boolean doescontain = false;

		TableLayoutConstraints constraint;
		Component[] comp = container.getComponents();
		if (tableLayout.getNumRow() >= row) {
			for (int i = 0; i < comp.length; i++) {
				constraint = tableLayout.getConstraints(comp[i]);
				if (constraint.row1 == row || constraint.row2 == row)
					doescontain = true;
			}
		}
		return doescontain;
	}

	/**
	 * This Method checks if a Column contains a {@link WYSIWYGComponent}
	 * @param column
	 * @return true if {@link WYSIWYGComponent} found
	 */
	public boolean containesItemInColumn(int column) {
		boolean doescontain = false;

		TableLayoutConstraints constraint;
		Component[] comp = container.getComponents();
		if (tableLayout.getNumColumn() >= column) {
			for (int i = 0; i < comp.length; i++) {
				constraint = tableLayout.getConstraints(comp[i]);
				if (constraint.col1 == column || constraint.col2 == column)
					doescontain = true;
			}
		}
		return doescontain;
	}

	/**
	 * This Method checks if the {@link LayoutCell} is empty
	 * @param cell to check
	 * @return true if the {@link LayoutCell} is empty
	 */
	public boolean isCellEmpty(LayoutCell cell) {
		return isCellEmpty(null, cell);
	}
	
	/**
	 * NUCLEUSINT-426
	 * This Method checks if the {@link LayoutCell} is empty
	 * It returns true if the cell is empty, ignoring the component itself
	 * @param the {@link WYSIWYGComponent} to check
	 * @param cell to check
	 * @return true if the {@link LayoutCell} is empty
	 */
	public boolean isCellEmpty(WYSIWYGComponent c, LayoutCell cell) {
		boolean isEmpty = true;
		
		Component[] comp = container.getComponents();

		TableLayoutConstraints constraint;
		for (int i = 0; i < comp.length; i++) {
			if (!comp[i].equals(c)) {
				constraint = tableLayout.getConstraints(comp[i]);
				if ((constraint.col1 <= cell.getCellX() && cell.getCellX() <= constraint.col2) && (constraint.row1 <= cell.getCellY() && cell.getCellY() <= constraint.row2)) {
					isEmpty = false;
				}
			}
		}
		return isEmpty;
	}

	/**
	 * Bridge to the {@link UndoRedoFunction}<br>
	 * calling {@link UndoRedoFunction#undoChanges()}
	 */
	public void undoChanges() {
		getUndoRedoFunction().undoChanges();
	}

	/**
	 * Bridge to the {@link UndoRedoFunction}<br>
	 * calling {@link UndoRedoFunction#redoChanges()}
	 */
	public void redoChanges() {
		getUndoRedoFunction().redoChanges();
	}

	/**
	 * This Method slices a row into a existing row.
	 * @param mouseLocation
	 * @param bounds
	 * @param current
	 * @return the resulting {@link LayoutCell}
	 */
	public LayoutCell sliceAFittingRowInIt(Point mouseLocation, Rectangle bounds, LayoutCell current) {
		int row1 = mouseLocation.y - current.getCellDimensions().y;
		int row2 = bounds.height;
		int row3 = current.getCellDimensions().height - row1 - row2;
		// avoid too rows that are to small to handle with
		if (row3 < InterfaceGuidelines.MINIMUM_SIZE) {
			row1 = row1 + row3;
			row3 = -1;
		}

		if (row3 == -1) {
			modifyTableLayoutSizes(row2, false, current, true);
		} else {
			modifyTableLayoutSizes(row3, false, current, true);

			LayoutCell cell2 = current;
			cell2.setCellHeight(row2);
			addRow(cell2);
		}

		LayoutCell cell3 = current;
		cell3.setCellHeight(row1);
		addRow(cell3);

		return getLayoutCell(mouseLocation);
	}

	/**
	 * This Method slices a {@link LayoutCell} into a existing colum
	 * @param mouseLocation
	 * @param bounds
	 * @param current
	 * @return the resulting {@link LayoutCell}
	 */
	public LayoutCell sliceAFittingColumnInIt(Point mouseLocation, Rectangle bounds, LayoutCell current) {
		int col1 = mouseLocation.x - current.getCellDimensions().x;
		int col2 = bounds.width;
		int col3 = current.getCellDimensions().width - col1 - col2;
		// avoid cols that are too small to handle with
		if (col3 < InterfaceGuidelines.MINIMUM_SIZE) {
			col1 = col1 + col3;
			col3 = -1;
		}

		if (col3 == -1) {
			modifyTableLayoutSizes(col2, true, getLayoutCell(mouseLocation), true);
		} else {
			modifyTableLayoutSizes(col3, true, getLayoutCell(mouseLocation), true);

			LayoutCell cell2 = current;
			cell2.setCellWidth(col2);
			addCol(cell2);
		}

		LayoutCell cell3 = current;
		cell3.setCellWidth(col1);
		addCol(cell3);

		return getLayoutCell(mouseLocation);
	}

	/**
	 * This Method cuts a new {@link LayoutCell} into a bigger existing cell on the {@link TableLayoutPanel}.<br>
	 * calls 
	 * <ul>
	 * <li>{@link #sliceAFittingColumnInIt(Point, Rectangle, LayoutCell)}</li>
	 * <li>{@link #sliceAFittingRowInIt(Point, Rectangle, LayoutCell)}</li>
	 * </ul>
	 * @param mouseLocation
	 * @param bounds
	 * @param current
	 * @return
	 */
	public LayoutCell sliceCellToPieces(Point mouseLocation, Rectangle bounds, LayoutCell current) {
		LayoutCell created = sliceAFittingColumnInIt(mouseLocation, bounds, current);
		created = sliceAFittingRowInIt(mouseLocation, bounds, created);

		return created;
	}

	/**
	 * This Method collects all Point needed to make the {@link TableLayout} Columns and Rows visible.
	 * @see #drawTableLayoutCells(Graphics)
	 * @return Iterator<Point> iterator with the Endpoints (x == -1 if row, y == -1 if column)
	 */
	public Iterator<Point> showTableLayout() {
		Vector<Point> cells = new Vector<Point>();

		double x = 0;
		double y = 0;

		Point p = new Point();

		double[] columns = tableLayout.getColumn();
		columns = calculateAbsoluteSize(columns, container.getWidth(), true);

		double[] rows = tableLayout.getRow();
		rows = calculateAbsoluteSize(rows, container.getHeight(), false);

		completeLayoutWidth = 0;
		completeLayoutHeight = 0;

		for (int i = 0; i < columns.length; i++) {
			p = new Point();
			x = x + columns[i];
			p.x = (int) x;
			p.y = -1;
			cells.add(p);
			completeLayoutWidth = completeLayoutWidth + columns[i];
		}

		for (int i = 0; i < rows.length; i++) {
			p = new Point();
			y = y + rows[i];
			p.y = (int) y;
			p.x = -1;
			cells.add(p);
			completeLayoutHeight = completeLayoutHeight + rows[i];
		}

		return cells.iterator();
	}

	/**
	 * This Method makes the {@link TableLayout} visible
	 * 
	 * @param g
	 */
	public void drawTableLayoutCells(Graphics g) {
		Graphics2D g2d = (Graphics2D) g;

		Iterator<Point> lines = showTableLayout();

		while (lines.hasNext()) {
			Point p = lines.next();

			g2d.setStroke(new BasicStroke(1.f));
			g2d.setColor(Color.LIGHT_GRAY);
			if (p.x == -1) {
				g2d.drawLine(0, p.y, getCalculatedLayoutWidth(), p.y);
			}
			if (p.y == -1) {
				g2d.drawLine(p.x, 0, p.x, getCalculatedLayoutHeight());
			}
		}
	}

	/**
	 * Small Helpermethod for filling Cells with hatched lines
	 * 
	 * @param g
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 */
	public void schraffiertesRechteckZeichnen(Graphics g, int x, int y, int width, int height) {
		final int DST = 3;

		g.drawRect(x, y, width, height);
		for (int i = DST; i < width + height; i += DST) {
			int p1x = (i <= height) ? x : x + i - height;
			int p1y = (i <= height) ? y + i : y + height;
			int p2x = (i <= width) ? x + i : x + width;
			int p2y = (i <= width) ? y : y + i - width;
			g.drawLine(p1x, p1y, p2x, p2y);
		}
	}

	/**
	 * This Method highlights the acutal {@link LayoutCell}
	 * @param g
	 */
	public void drawCurrentCell(Graphics g) {
		Graphics2D g2d = (Graphics2D) g;
		if (tableLayout.getNumRow() > 1 && tableLayout.getNumColumn() > 1) {
			
			if (container.getParentEditorPanel().getParent() instanceof JPanel) {
				//TODO peformanceleck! rekursives endloszeichnen bei panel im panel
				((JPanel) container.getParentEditorPanel().getParent()).updateUI();
			}
			if (container.getCurrentLayoutCell() != null && (container.getCurrentLayoutCell().getCellX() != 0 && container.getCurrentLayoutCell().getCellY() != 0)) {
				g2d.setColor(Color.GREEN);
				schraffiertesRechteckZeichnen(g2d, container.getCurrentLayoutCell().getCellDimensions().x + 1, container.getCurrentLayoutCell().getCellDimensions().y + 1, container.getCurrentLayoutCell().getCellDimensions().width - 2, container.getCurrentLayoutCell().getCellDimensions().height - 2);
			}
		}
	}

	/**
	 * Handling the ChangeEvent so that the SaveButton is enabled etc..
	 */
	public void notifyThatSomethingChanged() {
		WYSIWYGLayoutEditorChangeDescriptor changeDescriptor = container.getEditorChangeDescriptor();

		if (changeDescriptor != null)
			changeDescriptor.setContentChanged();
	}

	/**
	 * Helpermethod for getting the WYSIWYGLayoutEditorChangeDescriptor
	 * @return WYSIWYGLayoutEditorChangeDescriptor
	 */
	private WYSIWYGLayoutEditorChangeDescriptor getWYSIWYGLayoutEditorChangeDescriptor() {
		return container.getEditorChangeDescriptor();
	}
	
	/**
	 * Method determines whether two instances of {@link TableLayoutConstraints} are equal.
	 * 
	 * @param c1
	 * @param c2
	 * @return true if constraints are equal, otherwise false
	 */
	public boolean areConstraintsEqual(TableLayoutConstraints c1, TableLayoutConstraints c2) {
		if (c1 != null && c2 != null) {
			if (c1.col1 != c2.col1 || c1.col2 != c2.col2 || c1.row1 != c2.row1 || c1.row2 != c2.row2
					|| c1.hAlign != c2.hAlign || c1.vAlign != c2.vAlign) {
				return false;
			}
			else {
				return true;
			}
		}
		else if (c1 == null && c2 == null) {
			return true;
		}
		else {
			return false;
		}
	}

	/**
	 * NUCLEUSINT-311
	 * 
	 * This Method revalidates the Layout.
	 * 
	 * This is needed to fix "jumping" {@link WYSIWYGComponent}. <br>
	 * This does happen if a {@link WYSIWYGComponent} gets a PreferredSize that is larger than the {@link LayoutCell}
	 * 
	 * @param c
	 * @param newPreferredSize
	 * @throws CommonBusinessException
	 */
	public void revalidateLayoutCellForComponent(WYSIWYGComponent c, Dimension newPreferredSize) throws CommonBusinessException {
		TableLayoutConstraints constraint = getConstraintForComponent(c);

		double hAlign = tableLayout.getColumn(constraint.col1);
		double vAlign = tableLayout.getRow(constraint.row1);
		
		boolean hAlignRelative = false;
		if (hAlign == TableLayout.MINIMUM || hAlign == TableLayout.PREFERRED || hAlign == TableLayout.FULL)
			hAlignRelative = true;
		
		boolean vAlignRelative = false;
		if (vAlign == TableLayout.MINIMUM || vAlign == TableLayout.PREFERRED || vAlign == TableLayout.FULL)
			vAlignRelative = true;
		
		if (!hAlignRelative) {
			double[] columns = calculateAbsoluteSize(tableLayout.getColumn(), getCalculatedLayoutWidth(), true);
			
			if (columns[constraint.col1] < newPreferredSize.getWidth()){
				modifyTableLayoutSizes(newPreferredSize.getWidth(), true, new LayoutCell(constraint), true);
			}
		}
		
		if (!vAlignRelative) {
			double[] rows = calculateAbsoluteSize(tableLayout.getRow(), getCalculatedLayoutHeight(), false);
			
			if (rows[constraint.row1] < newPreferredSize.getHeight()){
				modifyTableLayoutSizes(newPreferredSize.getHeight(), false, new LayoutCell(constraint), true);
			}
		}

		container.validate();
	}
}
