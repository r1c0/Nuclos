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
import info.clearthought.layout.TableLayoutConstants;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;

import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;

import org.nuclos.client.layout.wysiwyg.WYSIWYGStringsAndLabels.CHANGE_SIZE_COLUMN_POPUP;
import org.nuclos.client.layout.wysiwyg.WYSIWYGStringsAndLabels.MEASUREMENT_DESCRIPTIONS;
import org.nuclos.client.layout.wysiwyg.editor.util.InterfaceGuidelines;
import org.nuclos.client.layout.wysiwyg.editor.util.TableLayoutUtil;
import org.nuclos.client.layout.wysiwyg.editor.util.valueobjects.LayoutCell;
import org.nuclos.client.layout.wysiwyg.editor.util.valueobjects.TableLayoutPanel;

/**
 * This PopupMenu shows up if the Mouse is on the top border ( {@link InterfaceGuidelines#MARGIN_TOP} of the Layout.<br>
 * It handles:
 * <ul>
 * <li> setting the Size of the Column, inkl. relative Sizes like {@link TableLayoutConstants#FILL}, {@link TableLayoutConstants#PREFERRED}, {@link TableLayoutConstants#MINIMUM}</li>
 * <li> Adding a new Row below the actual Column</li>
 * <li> Adding a new Row over the actual Column </li>
 * <li> Adding a default Border (with the Size of {@link InterfaceGuidelines#MARGIN_RIGHT}</li>
 * </ul>
 * 
 * <br>
 * Created by Novabit Informationssysteme GmbH <br>
 * Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 * 
 * @author <a href="mailto:hartmut.beckschulze@novabit.de">hartmut.beckschulze</a>
 * @version 01.00.00
 */
@SuppressWarnings("serial")
public class ChangeSizeMeasurementPopupColumn implements ActionListener, Serializable {

    private JPopupMenu changeSizeMeasurementPopupColumn;

    private TableLayoutUtil tableLayoutUtil;

    private String[] strValue = { "ABSOLUTE", "PERCENTUAL", "PREFERRED", "MINIMUM", "FILL" };

    private String[] strMenuItems = { MEASUREMENT_DESCRIPTIONS.NAME_ABSOLUTE_SIZE,
    		MEASUREMENT_DESCRIPTIONS.NAME_PERCENTUAL_SIZE,
    		MEASUREMENT_DESCRIPTIONS.NAME_PREFERRED_SIZE
    		,MEASUREMENT_DESCRIPTIONS.NAME_MINIMUM_SIZE, MEASUREMENT_DESCRIPTIONS.NAME_FILL};

    private JMenuItem[] columns;

    private LayoutCell cellForEditing;

    private JMenuItem delete;

    /**
     * 
     * @param tableLayoutUtil
     */
    public ChangeSizeMeasurementPopupColumn(TableLayoutUtil tableLayoutUtil) {
	this.tableLayoutUtil = tableLayoutUtil;
	changeSizeMeasurementPopupColumn = new JPopupMenu();

	int number = strMenuItems.length;
	columns = new JMenuItem[number];

	for (int i = 0; i < number; i++) {
	    columns[i] = new JMenuItem(strMenuItems[i]);
	    columns[i].setActionCommand(strValue[i]);
	    columns[i].addActionListener(this);
	    changeSizeMeasurementPopupColumn.add(columns[i]);

	    if (strValue[i].equals("PREFERRED") || strValue[i].equals("MINIMUM")) {
		columns[i].setEnabled(false);
	    }
	}

	for (int i = 0; i < columns.length; i++) {
	    changeSizeMeasurementPopupColumn.add(columns[i]);
	}

	changeSizeMeasurementPopupColumn.addSeparator();
	changeSizeMeasurementPopupColumn.add(new JLabel("<html><b>"+CHANGE_SIZE_COLUMN_POPUP.TITLE_SUBMENU_ADD_OR_DELETE+"</b></html>"));
	JMenuItem addRight = new JMenuItem(CHANGE_SIZE_COLUMN_POPUP.TITLE_ADD_COLUMN_RIGHT);
	addRight.setActionCommand("ADDRIGHT");
	addRight.addActionListener(this);
	JMenuItem addLeft = new JMenuItem(CHANGE_SIZE_COLUMN_POPUP.TITLE_ADD_COLUMN_LEFT);
	addLeft.setActionCommand("ADDLEFT");
	addLeft.addActionListener(this);
	//NUCLEUSINT-966
	JMenuItem addMultipleRight = new JMenuItem(CHANGE_SIZE_COLUMN_POPUP.TITLE_ADD_MULTIPLE_COLUMNS_RIGHT);
	addMultipleRight.setActionCommand("ADDMULTIPLERIGHT");
	addMultipleRight.addActionListener(this);
	JMenuItem addMultipleLeft = new JMenuItem(CHANGE_SIZE_COLUMN_POPUP.TITLE_ADD_MULTIPLE_COLUMNS_LEFT);
	addMultipleLeft.setActionCommand("ADDMULTIPLELEFT");
	addMultipleLeft.addActionListener(this);
	JMenuItem addDefaultBorderRight = new JMenuItem(CHANGE_SIZE_COLUMN_POPUP.TITLE_ADD_DEFAULT_BORDER);
	addDefaultBorderRight.setActionCommand("ADDBORDER");
	addDefaultBorderRight.addActionListener(this);
	delete = new JMenuItem(CHANGE_SIZE_COLUMN_POPUP.TITLE_DELETE_COLUMN); //, Icons.getInstance().getIconDelete16());
	delete.setActionCommand("DELETE");
	delete.addActionListener(this);
	changeSizeMeasurementPopupColumn.add(addLeft);
	changeSizeMeasurementPopupColumn.add(addRight);
	//NUCLEUSINT-966
	changeSizeMeasurementPopupColumn.add(addMultipleLeft);
	changeSizeMeasurementPopupColumn.add(addMultipleRight);
	changeSizeMeasurementPopupColumn.add(delete);
	changeSizeMeasurementPopupColumn.addSeparator();
	changeSizeMeasurementPopupColumn.add(addDefaultBorderRight);
	
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
			// NUCLEUSINT-281 returning old value
			return -1;
		} catch (NullPointerException e) {
			// NUCLEUSINT-281 returning old value
			return -1;
		}

		if (absolute) {
			if (value < InterfaceGuidelines.MINIMUM_SIZE)
				value = InterfaceGuidelines.MINIMUM_SIZE;
			if (cellForEditing.getCellX() == 0) {
				if (value < InterfaceGuidelines.MARGIN_LEFT)
					value = InterfaceGuidelines.MARGIN_LEFT;
			}
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
     * 
     * @param cellForEditing
     */
    public void showChangeSizeMeasurementPopupForColumns(LayoutCell cellForEditing) {
	this.cellForEditing = cellForEditing;
	enablePreferredAndMinimumIfComponentAvaible();
	enableDeleteIfNoItemContained();
	Point loc = getContainer().getMousePosition().getLocation();
	changeSizeMeasurementPopupColumn.show(getContainer(), loc.x, loc.y);
    }

    private void enableDeleteIfNoItemContained() {
	boolean itemInColum = tableLayoutUtil.containesItemInColumn(cellForEditing.getCellX());
	if (itemInColum)
	    delete.setEnabled(false);
	else
	    delete.setEnabled(true);
    }

    /**
     * checks if a component is in this column. there is no sense to enable
     * minimum or preferred size if there is no component inside.
     * 
     * @param modifiyCell
     */
    private void enablePreferredAndMinimumIfComponentAvaible() {
	boolean itemInColum = tableLayoutUtil.containesItemInColumn(cellForEditing.getCellX());
	JMenuItem temp;

	for (int i = 0; i < changeSizeMeasurementPopupColumn.getComponentCount(); i++) {
		Component c = changeSizeMeasurementPopupColumn.getComponent(i);
		if (c instanceof JMenuItem) {
			temp = (JMenuItem) changeSizeMeasurementPopupColumn.getComponent(i);
			if (itemInColum) {
				if (temp.getActionCommand().equals("PREFERRED") || temp.getActionCommand().equals("MINIMUM")) {
				    temp.setEnabled(true);
				}
			    } else if (temp.getActionCommand().equals("PREFERRED") || temp.getActionCommand().equals("MINIMUM")) {
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
	boolean colum = true;

	String actionCommand = e.getActionCommand();

	if (actionCommand.equals("PERCENTUAL")) {
		/** percentual size handling - reads value from User with a Input dialog, entered value is divided by 100 to get the right percentual size */
		double sizeValue = getValueFromUser(MEASUREMENT_DESCRIPTIONS.MESSAGE_INPUT_DIALOG_PERCENTUAL, false);
		//FIX NUCLEUSINT-281 if invalid value, nothing is done
		if (sizeValue != -1) {
			sizeValue = sizeValue / 100.0;
			tableLayoutUtil.modifyTableLayoutSizes(sizeValue, colum, cellForEditing, true);
		}
	} else if (actionCommand.equals("ABSOLUTE")) {
		/** absolute size, reading the value from the user */
		double sizeValue = getValueFromUser(MEASUREMENT_DESCRIPTIONS.MESSAGE_INPUT_DIALOG_ABSOLUTE, true);
		//FIX NUCLEUSINT-281 if invalid value, nothing is done
		if (sizeValue != -1)
			tableLayoutUtil.modifyTableLayoutSizes(sizeValue, colum, cellForEditing, true);
	} else if (actionCommand.equals("MINIMUM")) {
		/** sets minimum size, is only active if a component was found in this column */
		tableLayoutUtil.modifyTableLayoutSizes(TableLayout.MINIMUM, colum, cellForEditing, true);
	} else if (actionCommand.equals("PREFERRED")) {
		/** sets preferred size, is only active if a component was found in this column */
		tableLayoutUtil.modifyTableLayoutSizes(TableLayout.PREFERRED, colum, cellForEditing, true);
	} else if (actionCommand.equals("FILL")) {
		/** sets the relative size fill, uses all the rest space that is not filled by absolute size */
		tableLayoutUtil.modifyTableLayoutSizes(TableLayout.FILL, colum, cellForEditing, true);
	} else if (actionCommand.equals("ADDRIGHT")) {
		/** adds a new column on the right side of the active column - uses a default value for size. commented out getting the value from the user, other way is quicker */
		double sizeValue = InterfaceGuidelines.DEFAULT_COLUMN_WIDTH;//getValueFromUser(TABLELAYOUT_UTIL.MESSAGE_INPUT_DIALOG_ENTER_WIDTH, true);
	    cellForEditing.setCellWidth(sizeValue);
	    /** adjustment so the col will be added to the right side */
	    if (tableLayoutUtil.getNumColumns() > 0)
	    cellForEditing.setCellX(cellForEditing.getCellX() + 1);
	    tableLayoutUtil.addCol(cellForEditing);
	}  else if (actionCommand.equals("ADDMULTIPLERIGHT")) {
	//NUCLEUSINT-966
		String count = JOptionPane.showInputDialog(CHANGE_SIZE_COLUMN_POPUP.MESSAGE_INPUT_DIALOG_AMOUNT_COLS, 2);
		if (count != null) {
			try {
			Integer amount = Integer.parseInt(count);
				for( int i = 0; i < amount; i++) {
				actionPerformed(new ActionEvent(e.getSource(), e.getID(), "ADDCOLBORDER"));
				cellForEditing.setCellX(cellForEditing.getCellX() - 1);
				actionPerformed(new ActionEvent(e.getSource(), e.getID(), "ADDRIGHT"));
				cellForEditing.setCellX(cellForEditing.getCellX() - 1);
				}
				actionPerformed(new ActionEvent(e.getSource(), e.getID(), "ADDCOLBORDER"));
			} catch (NumberFormatException e1) {
				//nothing to do
			}
		}
	} else if (actionCommand.equals("ADDLEFT")) {
		/** adds a new column on the left of the active column - uses a default value for size. commented out getting the value from the user, other way is quicker */
		double sizeValue = InterfaceGuidelines.DEFAULT_COLUMN_WIDTH;//getValueFromUser(TABLELAYOUT_UTIL.MESSAGE_INPUT_DIALOG_ENTER_WIDTH, true);
	    cellForEditing.setCellWidth(sizeValue);
	    tableLayoutUtil.addCol(cellForEditing);
	} else if (actionCommand.equals("ADDMULTIPLELEFT")) {
	//NUCLEUSINT-966
		String count = JOptionPane.showInputDialog(CHANGE_SIZE_COLUMN_POPUP.MESSAGE_INPUT_DIALOG_AMOUNT_COLS, 2);
		if (count != null) {
			try {
				Integer amount = Integer.parseInt(count);
				for( int i = 0; i < amount; i++) {
					cellForEditing.setCellX(cellForEditing.getCellX() - 1);	
					actionPerformed(new ActionEvent(e.getSource(), e.getID(), "ADDCOLBORDER"));
					actionPerformed(new ActionEvent(e.getSource(), e.getID(), "ADDLEFT"));
				}
				cellForEditing.setCellX(cellForEditing.getCellX() - 1);	
				actionPerformed(new ActionEvent(e.getSource(), e.getID(), "ADDCOLBORDER"));
			} catch (NumberFormatException e1) {
				//nothing to do
			}
		}
	} else if (actionCommand.equals("DELETE")) {
		/** delete the active column - is only possible if the column does not contain any component */
		tableLayoutUtil.delCol(cellForEditing);
	} else if (actionCommand.equals("ADDBORDER")){
		/** "shortcut" to add a new border on the right side, used for creating standardborders */
		cellForEditing.setCellWidth(InterfaceGuidelines.MARGIN_RIGHT);
	    /** adjustment so the col will be added to the right side */
	    if (tableLayoutUtil.getNumColumns() > 0)
	    	cellForEditing.setCellX(cellForEditing.getCellX() + 1);
	    tableLayoutUtil.addCol(cellForEditing);
	} else if (actionCommand.equals("ADDCOLBORDER")){
	//NUCLEUSINT-966
		/** "shortcut" to add a new border on the right side, used for creating standardborders */
		cellForEditing.setCellWidth(InterfaceGuidelines.MARGIN_BETWEEN);
	    /** adjustment so the col will be added to the right side */
	    if (tableLayoutUtil.getNumColumns() > 0)
	    	cellForEditing.setCellX(cellForEditing.getCellX() + 1);
	    tableLayoutUtil.addCol(cellForEditing);
	}

    }

    /**
     * draws the box around the row that could be modified does the highlighting
     * job
     * 
     * @param g
     */
    public void drawModifyingBox(Graphics g) {
	Graphics2D g2d = (Graphics2D) g;

		int x = -1;
		int y = -1;
		int width = -1;
		int height = -1;

		int highlightboxwidth = 0;
		int highlightboxheight = 0;

		LayoutCell current = tableLayoutUtil.getCurrentLayoutCell();
		if (current != null) {
		    x = current.getCellDimensions().x;
		    y = 0;
		    width = current.getCellDimensions().width;
		    height = InterfaceGuidelines.MARGIN_TOP;

		    highlightboxheight = tableLayoutUtil.getCalculatedLayoutHeight();
		    highlightboxwidth = width;

		}
		
		/** drawing a filled box on the top as "active indicator" */
		g2d.setColor(Color.GRAY);
		g2d.fillRect(x, y, width, height);

		/** highlight the column */
		g2d.setColor(Color.GRAY);
		BasicStroke stroke = new BasicStroke(1.0f);
		g2d.setStroke(stroke);
		schraffiertesRechteckZeichnen(g2d, x, y, highlightboxwidth, highlightboxheight);
		
		/** drawing the column number on the gray box */
		g2d.setColor(Color.WHITE);
		g2d.setFont(new Font("SansSerif", Font.BOLD, 10));
		String info = current.getCellX() + "";
		FontMetrics metrics = g2d.getFontMetrics();
		int fontHeight = metrics.getHeight();
		int fontWidth = metrics.stringWidth(info);
		g2d.drawString(info, ((width) / 2) + x - (fontWidth / 2), fontHeight - 2);
    }

    /**
     * 
     * @param g
     * @param x
     * @param y
     * @param width
     * @param height
     * @see TableLayoutUtil#schraffiertesRechteckZeichnen(Graphics, int, int, int, int)
	 */
    public void schraffiertesRechteckZeichnen(Graphics g, int x, int y, int width, int height) {
	final int DST = 8;

	g.drawRect(x, y, width, height);
	for (int i = DST; i < width + height; i += DST) {
	    int p1x = (i <= height) ? x : x + i - height;
	    int p1y = (i <= height) ? y + i : y + height;
	    int p2x = (i <= width) ? x + i : x + width;
	    int p2y = (i <= width) ? y : y + i - width;
	    g.drawLine(p1x, p1y, p2x, p2y);
	}
    }
}
