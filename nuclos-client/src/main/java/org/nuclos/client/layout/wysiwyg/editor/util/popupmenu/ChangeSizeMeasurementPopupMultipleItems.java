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
package org.nuclos.client.layout.wysiwyg.editor.util.popupmenu;

import info.clearthought.layout.TableLayout;

import java.awt.Component;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.Stack;
import java.util.Vector;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;

import org.nuclos.client.layout.wysiwyg.WYSIWYGStringsAndLabels.MEASUREMENT_DESCRIPTIONS;
import org.nuclos.client.layout.wysiwyg.WYSIWYGStringsAndLabels.MULTIPLE_SELECTION_ROWS_AND_COLUMNS;
import org.nuclos.client.layout.wysiwyg.editor.util.InterfaceGuidelines;
import org.nuclos.client.layout.wysiwyg.editor.util.TableLayoutUtil;
import org.nuclos.client.layout.wysiwyg.editor.util.valueobjects.LayoutCell;
import org.nuclos.client.layout.wysiwyg.editor.util.valueobjects.TableLayoutPanel;

/**
 * This class does nearly the same as:
 * <ul>
 * <li> {@link ChangeSizeMeasurementPopupColumn}</li>
 * <li> {@link ChangeSizeMeasurementPopupRows}</li>
 * </ul>
 * 
 * <b>but</b> for Rows <b>and</b> Columns.<br>
 * 
 * It can be used to change the Size of more than one Column and Row, deleting more than one Row or Column.<br>
 * 
 * The Columns and Rows are marked by CTRL Click on their Header in:<br>
 * {@link TableLayoutPanel#performActionsOnLeftClickAndControlPressedForMultiEdit(MouseEvent)}
 * 
 * <br>
 * Created by Novabit Informationssysteme GmbH <br>
 * Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 * 
 * @author <a href="mailto:hartmut.beckschulze@novabit.de">hartmut.beckschulze</a>
 * @version 01.00.00
 */
public class ChangeSizeMeasurementPopupMultipleItems implements ActionListener {

    private JPopupMenu changeSizeMeasurementPopupColumn;

    private TableLayoutUtil tableLayoutUtil;

    private String[] strValue = { "ABSOLUTE", "PERCENTUAL", "PREFERRED", "MINIMUM", "FILL" };

    private String[] strMenuItems = {  MEASUREMENT_DESCRIPTIONS.NAME_ABSOLUTE_SIZE,
    		MEASUREMENT_DESCRIPTIONS.NAME_PERCENTUAL_SIZE,
    		MEASUREMENT_DESCRIPTIONS.NAME_PREFERRED_SIZE
    		,MEASUREMENT_DESCRIPTIONS.NAME_MINIMUM_SIZE, MEASUREMENT_DESCRIPTIONS.NAME_FILL };

    private JMenuItem[] selectedItems;

    private JMenuItem delete;

    /** storing the Columns */
    private Vector<LayoutCell> columns = new Vector<LayoutCell>();
    /** storing the Rows */
    private Vector<LayoutCell> rows = new Vector<LayoutCell>();

    /**
     * 
     * @param tableLayoutUtil
     */
    public ChangeSizeMeasurementPopupMultipleItems(TableLayoutUtil tableLayoutUtil) {
	this.tableLayoutUtil = tableLayoutUtil;
	changeSizeMeasurementPopupColumn = new JPopupMenu();

	int number = strMenuItems.length;
	selectedItems = new JMenuItem[number];

	for (int i = 0; i < number; i++) {
	    selectedItems[i] = new JMenuItem(strMenuItems[i]);
	    selectedItems[i].setActionCommand(strValue[i]);
	    selectedItems[i].addActionListener(this);
	    changeSizeMeasurementPopupColumn.add(selectedItems[i]);

	    /** disable preferred and minimum, its enabled later */
	    if (strValue[i].equals("PREFERRED") || strValue[i].equals("MINIMUM")) {
		selectedItems[i].setEnabled(false);
	    }
	}

	for (int i = 0; i < selectedItems.length; i++) {
	    changeSizeMeasurementPopupColumn.add(selectedItems[i]);
	}

	delete = new JMenuItem(MULTIPLE_SELECTION_ROWS_AND_COLUMNS.TITLE_DELETE_ALL);
	delete.setActionCommand("DELETE");
	delete.addActionListener(this);
	changeSizeMeasurementPopupColumn.add(delete);
    }
    
    private TableLayoutPanel getContainer() {
    	return tableLayoutUtil.getContainer();
    }

    /**
     * helper method to interact with the user to get values for the size of the
     * column
     * 
     * @param message
     * @return
     */
    public double getValueFromUser(String message, boolean absolute) {
	String strValue = JOptionPane.showInputDialog(message);
	double value = -100;
	try {
	    value = Double.parseDouble(strValue);
	} catch (NumberFormatException e) {
	    value = -1;
	} catch (NullPointerException e) {
		//NUCLEUSINT-281
		value = -1;
	}

	if (absolute) {
	    if (value < InterfaceGuidelines.MINIMUM_SIZE)
		value = InterfaceGuidelines.MINIMUM_SIZE;
	} else {
	    if (value < 1)
		value = 1;
	    if (value > 99)
		value = 99;
	}
	return value;
    }

    /**
     * method that displays the popup menu+ called on right click when in range
     */
    public void showChangeSizeMeasurementPopupForColumns(Stack<LayoutCell> cellsToEdit) {
	columns = new Vector<LayoutCell>();
	rows = new Vector<LayoutCell>();

	while (!cellsToEdit.isEmpty()) {
	    LayoutCell cell = cellsToEdit.pop();
	    if (cell.getCellX() == 0)
		rows.add(cell);
	    if (cell.getCellY() == 0)
		columns.add(cell);
	}
	enablePreferredAndMinimumIfComponentAvaible();
	enableDeleteIfNoItemContained();
	Point loc = getContainer().getMousePosition().getLocation();
	changeSizeMeasurementPopupColumn.show(getContainer(), loc.x, loc.y);
    }

    //NUCLEUSINT-999
	private boolean enableDeleteIfNoItemContained() {
		boolean doesContain = false;
		for(LayoutCell cell : columns) {
			if(tableLayoutUtil.containesItemInColumn(cell.getCellX())) {
				doesContain = true;
			}
		}
		for(LayoutCell cell : rows) {
			if(tableLayoutUtil.containesItemInRow(cell.getCellY())) {
				doesContain = true;
			}
		}
		if(doesContain)
			delete.setEnabled(false);
		else
			delete.setEnabled(true);
		
		return !doesContain;
	}

    /**
     * checks if a component is in this column. there is no sense to enable
     * minimum or preferred size if there is no component inside.
     * 
     * @param modifiyCell
     */
	private void enablePreferredAndMinimumIfComponentAvaible() {
		boolean doesContain = true;
		for(LayoutCell cell : columns) {
			if(!tableLayoutUtil.containesItemInColumn(cell.getCellX())) {
				doesContain = false;
			}
		}
		for(LayoutCell cell : rows) {
			if(!tableLayoutUtil.containesItemInRow(cell.getCellY())) {
				doesContain = false;
			}
		}
		JMenuItem temp = null;
		for(int i = 0; i < changeSizeMeasurementPopupColumn.getComponentCount(); i++) {
			Component c = changeSizeMeasurementPopupColumn.getComponent(i);

			if (c instanceof JMenuItem) {
				temp = (JMenuItem) changeSizeMeasurementPopupColumn.getComponent(i);
				if(doesContain) {
					if(temp.getActionCommand().equals("PREFERRED")
						|| temp.getActionCommand().equals("MINIMUM")) {
						temp.setEnabled(true);
					}
				}
				else if(temp.getActionCommand().equals("PREFERRED")
					|| temp.getActionCommand().equals("MINIMUM")) {
					temp.setEnabled(false);
				}
			}
		}
	}

    /**
     * the action listener that deals with the selected popup menu type
     */
    @Override
	public void actionPerformed(ActionEvent e) {
    	

		String actionCommand = e.getActionCommand();
		//NUCLEUSINT-367
//		tableLayoutUtil.getUndoRedoFunction().beginTransaction();
		if (actionCommand.equals("PERCENTUAL")) {
			double sizeValue = getValueFromUser(MEASUREMENT_DESCRIPTIONS.MESSAGE_INPUT_DIALOG_PERCENTUAL, false);
			if (sizeValue != -1) { //NUCLEUSINT-281 if -1 there is no change made
				sizeValue = sizeValue / 100.0;
				for (LayoutCell cell : columns) {
					tableLayoutUtil.modifyTableLayoutSizes(sizeValue, true, cell, true);
				}
				for (LayoutCell cell : rows) {
					tableLayoutUtil.modifyTableLayoutSizes(sizeValue, false, cell, true);
				}
			}
		} else if (actionCommand.equals("ABSOLUTE")) {
			double sizeValue = getValueFromUser(MEASUREMENT_DESCRIPTIONS.MESSAGE_INPUT_DIALOG_ABSOLUTE, true);
			if (sizeValue != -1) { //NUCLEUSINT-281 if -1 there is no change made
				int referenceValue = -1;
				for (LayoutCell cell : columns) {
					referenceValue = -1;
					referenceValue = tableLayoutUtil.getMinimumWidthForColumn(cell.getCellX());
					if (referenceValue > sizeValue)
						tableLayoutUtil.modifyTableLayoutSizes(referenceValue, true, cell, true);
					else
						tableLayoutUtil.modifyTableLayoutSizes(sizeValue, true, cell, true);
				}
				for (LayoutCell cell : rows) {
					referenceValue = -1;
					referenceValue = tableLayoutUtil.getMinimumWidthForColumn(cell.getCellY());
					if (referenceValue > sizeValue)
						tableLayoutUtil.modifyTableLayoutSizes(referenceValue, false, cell, true);
					else
						tableLayoutUtil.modifyTableLayoutSizes(sizeValue, false, cell, true);
				}
			}
		} else if (actionCommand.equals("MINIMUM")) {
			for (LayoutCell cell : columns) {
				tableLayoutUtil.modifyTableLayoutSizes(TableLayout.MINIMUM, true, cell, true);
			}
			for (LayoutCell cell : rows) {
				tableLayoutUtil.modifyTableLayoutSizes(TableLayout.MINIMUM, false, cell, true);
			}
		} else if (actionCommand.equals("PREFERRED")) {
			for (LayoutCell cell : columns) {
				tableLayoutUtil.modifyTableLayoutSizes(TableLayout.PREFERRED, true, cell, true);
			}
			for (LayoutCell cell : rows) {
				tableLayoutUtil.modifyTableLayoutSizes(TableLayout.PREFERRED, false, cell, true);
			}
		} else if (actionCommand.equals("FILL")) {
			for (LayoutCell cell : columns) {
				tableLayoutUtil.modifyTableLayoutSizes(TableLayout.FILL, true, cell, true);
			}
			for (LayoutCell cell : rows) {
				tableLayoutUtil.modifyTableLayoutSizes(TableLayout.FILL, false, cell, true);
			}
		} else if (actionCommand.equals("DELETE")) {
			////NUCLEUSINT-999
			deleteCellsAndRows();
		}
		//NUCLEUSINT-367
//		tableLayoutUtil.getUndoRedoFunction().commitTransaction();
	}
    //NUCLEUSINT-999
    public void deleteCellsAndRowsWithShortcut(Stack<LayoutCell> cellsToEdit) {
   		while (!cellsToEdit.isEmpty()) {
   		    LayoutCell cell = cellsToEdit.pop();
   		    if (cell.getCellX() == 0)
   			rows.add(cell);
   		    if (cell.getCellY() == 0)
   			columns.add(cell);
   		}
   	 if (enableDeleteIfNoItemContained()) {
   		 deleteCellsAndRows();
   	 }
    }

    /**
     * //NUCLEUSINT-999
     */
	private void deleteCellsAndRows() {
	
		int min = Integer.MIN_VALUE;
		LayoutCell highestCell = null;
		while (columns.size() > 0) {
			min = Integer.MIN_VALUE;
			for (LayoutCell cell : columns) {
				if (min < cell.getCellX()) {
					min = cell.getCellX();
					highestCell = cell;
				}
			}
			tableLayoutUtil.delCol(highestCell);
			columns.remove(highestCell);
		}

		while (rows.size() > 0) {
			min = Integer.MIN_VALUE;
			for (LayoutCell cell : rows) {
				if (min < cell.getCellY()) {
					min = cell.getCellY();
					highestCell = cell;
				}
			}
			tableLayoutUtil.delRow(highestCell);
			rows.remove(highestCell);
		}
	}
}
