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

import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;

import org.apache.log4j.Logger;
import org.nuclos.client.layout.wysiwyg.WYSIWYGStringsAndLabels.CHANGE_SIZE_ROW_POPUP;
import org.nuclos.client.layout.wysiwyg.WYSIWYGStringsAndLabels.MEASUREMENT_DESCRIPTIONS;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGComponent;
import org.nuclos.client.layout.wysiwyg.editor.util.InterfaceGuidelines;
import org.nuclos.client.layout.wysiwyg.editor.util.TableLayoutUtil;
import org.nuclos.client.layout.wysiwyg.editor.util.valueobjects.LayoutCell;
import org.nuclos.client.layout.wysiwyg.editor.util.valueobjects.TableLayoutPanel;

/**
 * This PopupMenu shows up if the Mouse is on the left border ( {@link InterfaceGuidelines#MARGIN_LEFT} of the Layout.<br>
 * It handles:
 * <ul>
 * <li> setting the Size of the Row, inkl. relative Sizes like {@link TableLayoutConstants#FILL}, {@link TableLayoutConstants#PREFERRED}, {@link TableLayoutConstants#MINIMUM}</li>
 * <li> Adding a new Row below the actual Row</li>
 * <li> Adding a new Row over the actual Row </li>
 * <li> Adding a default Border (with the Size of {@link InterfaceGuidelines#MARGIN_BOTTOM}</li>
 * </ul>
 * 
 * 
 * <br>
 * Created by Novabit Informationssysteme GmbH <br>
 * Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 * 
 * @author <a href="mailto:hartmut.beckschulze@novabit.de">hartmut.beckschulze</a>
 * @version 01.00.00
 */
public class ChangeSizeMeasurementPopupRows implements ActionListener {

	private static final Logger LOG = Logger.getLogger(ChangeSizeMeasurementPopupRows.class);

	private JPopupMenu changeSizeMeasurementPopupRow;

	private TableLayoutUtil tableLayoutUtil;

	private String[] strValue = {"ABSOLUTE", "PERCENTUAL", "PREFERRED", "MINIMUM", "FILL"};

	private String[] strMenuItems = {MEASUREMENT_DESCRIPTIONS.NAME_ABSOLUTE_SIZE, MEASUREMENT_DESCRIPTIONS.NAME_PERCENTUAL_SIZE, MEASUREMENT_DESCRIPTIONS.NAME_PREFERRED_SIZE, MEASUREMENT_DESCRIPTIONS.NAME_MINIMUM_SIZE, MEASUREMENT_DESCRIPTIONS.NAME_FILL};

	private JMenuItem[] rows;

	long timeStart = -1;

	private LayoutCell cellForEditing;

	private JMenuItem delete;

	/**
	 * 
	 * @param tableLayoutUtil
	 */
	public ChangeSizeMeasurementPopupRows(TableLayoutUtil tableLayoutUtil) {
		this.tableLayoutUtil = tableLayoutUtil;
		changeSizeMeasurementPopupRow = new JPopupMenu();

		int number = strMenuItems.length;
		rows = new JMenuItem[number];

		for (int i = 0; i < number; i++) {
			rows[i] = new JMenuItem(strMenuItems[i]);
			rows[i].setActionCommand(strValue[i]);
			rows[i].addActionListener(this);

			/** disable preferred and minimum by default, is enabled later (preventing 0 size when no component is in the row */
			if (strValue[i].equals("PREFERRED") || strValue[i].equals("MINIMUM")) {
				rows[i].setEnabled(false);
			}
		}

		for (int i = 0; i < rows.length; i++) {
			changeSizeMeasurementPopupRow.add(rows[i]);
		}

		changeSizeMeasurementPopupRow.addSeparator();
		changeSizeMeasurementPopupRow.add(new JLabel("<html><b>"+CHANGE_SIZE_ROW_POPUP.TITLE_SUBMENU_ADD_OR_DELETE+"</b></html>"));
		JMenuItem addBottom = new JMenuItem(CHANGE_SIZE_ROW_POPUP.TITLE_ADD_ROW_BELOW);
		addBottom.setActionCommand("ADDBOTTOM");
		addBottom.addActionListener(this);
		JMenuItem addTop = new JMenuItem(CHANGE_SIZE_ROW_POPUP.TITLE_ADD_ROW_ON_TOP);
		addTop.setActionCommand("ADDTOP");
		addTop.addActionListener(this);
		//NUCLEUSINT-966
		JMenuItem addMultipleBottom = new JMenuItem(CHANGE_SIZE_ROW_POPUP.TITLE_ADD_MULTIPLE_ROWS_BELOW);
		addMultipleBottom.setActionCommand("ADDMULTIPLEBOTTOM");
		addMultipleBottom.addActionListener(this);
		JMenuItem addMultipleTop = new JMenuItem(CHANGE_SIZE_ROW_POPUP.TITLE_ADD_MULTIPLE_ROWS_ON_TOP);
		addMultipleTop.setActionCommand("ADDMULTIPLETOP");
		addMultipleTop.addActionListener(this);
		JMenuItem addDefaultBorderBottom = new JMenuItem(CHANGE_SIZE_ROW_POPUP.TITLE_ADD_DEFAULT_BORDER);
		addDefaultBorderBottom.setActionCommand("ADDBORDER");
		addDefaultBorderBottom.addActionListener(this);
		delete = new JMenuItem(CHANGE_SIZE_ROW_POPUP.TITLE_DELETE_ROW); //, Icons.getInstance().getIconDelete16());
		delete.setActionCommand("DELETE");
		delete.addActionListener(this);
		changeSizeMeasurementPopupRow.add(addTop);
		changeSizeMeasurementPopupRow.add(addBottom);
		//NUCLEUSINT-966
		changeSizeMeasurementPopupRow.add(addMultipleTop);
		changeSizeMeasurementPopupRow.add(addMultipleBottom);
		changeSizeMeasurementPopupRow.add(delete);
		changeSizeMeasurementPopupRow.addSeparator();
		changeSizeMeasurementPopupRow.add(addDefaultBorderBottom);
		
	}
	
	private TableLayoutPanel getContainer() {
		return tableLayoutUtil.getContainer();
	}

	/**
	 * helper method to interact with the user to get values for the size of the row
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
			// NUCLEUSINT-281 
			return -1;
		} catch (NullPointerException e) {
			// NUCLEUSINT-281
			return -1;
		}
		if (absolute) {
			if (value < InterfaceGuidelines.MINIMUM_SIZE)
				value = InterfaceGuidelines.MINIMUM_SIZE;

			if (cellForEditing.getCellY() == 0) {
				if (value < InterfaceGuidelines.MARGIN_TOP)
					value = InterfaceGuidelines.MARGIN_TOP;
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
	public void showChangeSizeMeasurementPopupForRows(LayoutCell cellForEditing) {
		this.cellForEditing = cellForEditing;
		enablePreferredAndMinimumIfComponentAvaible();
		enableDeleteIfNoItemContained();
		Point loc = getContainer().getMousePosition().getLocation();
		changeSizeMeasurementPopupRow.show(getContainer(), loc.x, loc.y);
	}

	/**
	 * This Method enabled delete only if no Item is contained in this row.<br>
	 * Deleting without this would result in {@link WYSIWYGComponent} outside the Layout with no chance to address them.
	 */
	private void enableDeleteIfNoItemContained() {
		boolean itemInRow = tableLayoutUtil.containesItemInRow(cellForEditing.getCellY());
		if (itemInRow)
			delete.setEnabled(false);
		else
			delete.setEnabled(true);
	}

	/**
	 * checks if a component is in this row. there is no sense to enable minimum or preferred size if there is no component inside.
	 * 
	 * @param modifiyCell
	 */
	private void enablePreferredAndMinimumIfComponentAvaible() {
		boolean itemInRow = tableLayoutUtil.containesItemInRow(cellForEditing.getCellY());
		JMenuItem temp;
		for (int i = 0; i < changeSizeMeasurementPopupRow.getComponentCount(); i++) {
			Component c = changeSizeMeasurementPopupRow.getComponent(i);
			if (c instanceof JMenuItem) {
				temp = (JMenuItem) changeSizeMeasurementPopupRow.getComponent(i);
				if (itemInRow) {
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
		boolean colum = false;

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
			/** sets minimum size, is only active if a component was found in this row */
			tableLayoutUtil.modifyTableLayoutSizes(TableLayout.MINIMUM, colum, cellForEditing, true);
		} else if (actionCommand.equals("PREFERRED")) {
			/** sets preferred size, is only active if a component was found in this row */
			tableLayoutUtil.modifyTableLayoutSizes(TableLayout.PREFERRED, colum, cellForEditing, true);
		} else if (actionCommand.equals("FILL")) {
			/** sets the relative size fill, uses all the rest space that is not filled by absolute size */
			tableLayoutUtil.modifyTableLayoutSizes(TableLayout.FILL, colum, cellForEditing, true);
		} else if (actionCommand.equals("ADDBOTTOM")) {
			/** adds a new row below the active row - uses a default value for size. commented out getting the value from the user, other way is quicker */
			double sizeValue = InterfaceGuidelines.DEFAULT_ROW_HEIGHT;
			// double sizeValue = getValueFromUser(CHANGE_SIZE_ROW_POPUP.MESSAGE_INPUT_DIALOG_ENTER_HEIGHT, true);
			cellForEditing.setCellHeight(sizeValue);
			/** adjustment so adding a row will be under the selected cell */
			if (tableLayoutUtil.getNumRows() > 0)
				cellForEditing.setCellY(cellForEditing.getCellY() + 1);
			tableLayoutUtil.addRow(cellForEditing);
		}else if (actionCommand.equals("ADDMULTIPLEBOTTOM")) {
			//NUCLEUSINT-966
			String count = JOptionPane.showInputDialog(CHANGE_SIZE_ROW_POPUP.MESSAGE_INPUT_DIALOG_AMOUNT_ROWS, 2);
			if (count != null) {
				try {
					Integer amount = Integer.parseInt(count);
					for( int i = 0; i < amount; i++) {
						actionPerformed(new ActionEvent(e.getSource(), e.getID(), "ADDROWBORDER"));
						actionPerformed(new ActionEvent(e.getSource(), e.getID(), "ADDBOTTOM"));
					}
					actionPerformed(new ActionEvent(e.getSource(), e.getID(), "ADDROWBORDER"));
				} catch (NumberFormatException e1) {
					//nothing to do
				}
			}
		} else if (actionCommand.equals("ADDTOP")) {
			/** adds a new row over the active row - uses a default value for size. commented out getting the value from the user, other way is quicker */
			double sizeValue = InterfaceGuidelines.DEFAULT_ROW_HEIGHT;
			// double sizeValue = getValueFromUser(CHANGE_SIZE_ROW_POPUP.MESSAGE_INPUT_DIALOG_ENTER_HEIGHT, true);
			cellForEditing.setCellHeight(sizeValue);
			tableLayoutUtil.addRow(cellForEditing);
		} else if (actionCommand.equals("ADDMULTIPLETOP")) {
			//NUCLEUSINT-966
			String count = JOptionPane.showInputDialog(
				CHANGE_SIZE_ROW_POPUP.MESSAGE_INPUT_DIALOG_AMOUNT_ROWS, 2);
			if(count != null) {
				try {
					Integer amount = Integer.parseInt(count);
					for(int i = 0; i < amount; i++) {
						actionPerformed(new ActionEvent(e.getSource(), e.getID(), "ADDTOP"));
						cellForEditing.setCellY(cellForEditing.getCellY() - 1);
						actionPerformed(new ActionEvent(e.getSource(), e.getID(), "ADDROWBORDER"));
					}
					cellForEditing.setCellY(cellForEditing.getCellY() + amount * 2 -1);
					actionPerformed(new ActionEvent(e.getSource(), e.getID(), "ADDROWBORDER"));
				}
				catch(NumberFormatException e1) {
					// nothing to do
					LOG.debug("actionPerformed: " + e1);
				}
			}
		} else if (actionCommand.equals("DELETE")) {
			/** delete the active row - is only possible if the row does not contain any component */
			tableLayoutUtil.delRow(cellForEditing);
		} else if (actionCommand.equals("ADDBORDER")) {
			/** "shortcut" to add a new border below, used for creating standardborders */
			cellForEditing.setCellHeight(InterfaceGuidelines.MARGIN_BOTTOM);
			/** adjustment so adding a row will be under the selected cell */
			if (tableLayoutUtil.getNumRows() > 0)
				cellForEditing.setCellY(cellForEditing.getCellY() + 1);
			tableLayoutUtil.addRow(cellForEditing);
		} else if (actionCommand.equals("ADDROWBORDER")) {
			//NUCLEUSINT-966
			/** "shortcut" to add a new border below, used for creating standardborders */
			cellForEditing.setCellHeight(InterfaceGuidelines.MARGIN_BETWEEN);
			/** adjustment so adding a row will be under the selected cell */
			if (tableLayoutUtil.getNumRows() > 0)
				cellForEditing.setCellY(cellForEditing.getCellY() + 1);
			tableLayoutUtil.addRow(cellForEditing);
		}
	}

	/**
	 * draws the box around the row that could be modified does the highlighting job
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

		/** getting the needed informations of the real pixel size of this row */
		LayoutCell current = tableLayoutUtil.getCurrentLayoutCell();
		if (current != null) {
			x = 0;
			y = current.getCellDimensions().y;
			height = current.getCellDimensions().height;
			width = InterfaceGuidelines.MARGIN_LEFT;

			highlightboxheight = height;
			highlightboxwidth = tableLayoutUtil.getCalculatedLayoutWidth();

		}

		/** drawing a filled box on the left side as "active indicator" */
		g2d.setColor(Color.GRAY);
		g2d.fillRect(x, y, width, height);

		/** highlight the row */
		g2d.setColor(Color.GRAY);
		BasicStroke stroke = new BasicStroke(1.0f);
		g2d.setStroke(stroke);
		g2d.drawRect(x, y, highlightboxwidth, highlightboxheight);
		schraffiertesRechteckZeichnen(g2d, x, y, highlightboxwidth, highlightboxheight);

		/** drawing the row number on the gray box */
		g2d.setColor(Color.WHITE);
		g2d.setFont(new Font("SansSerif", Font.BOLD, 10));
		String info = current.getCellY() + "";
		FontMetrics metrics = g2d.getFontMetrics();
		int fontHeight = metrics.getHeight();
		g2d.drawString(info, 1, ((height) / 2) + y + (fontHeight / 2));
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
