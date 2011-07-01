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
package org.nuclos.client.layout.wysiwyg.editor.util.valueobjects;

import info.clearthought.layout.TableLayoutConstraints;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGComponent;
import org.nuclos.client.layout.wysiwyg.component.properties.ComponentProperties;
import org.nuclos.client.layout.wysiwyg.component.properties.PropertiesPanel;
import org.nuclos.client.layout.wysiwyg.editor.util.TableLayoutUtil;
import org.nuclos.client.layout.wysiwyg.editor.util.UndoRedoFunction;
import org.nuclos.client.layout.wysiwyg.editor.util.valueobjects.layoutmlrules.LayoutMLRules;

/**
 * This Valueobject collects the Changes done by:
 * <ul>
 * <li> {@link TableLayoutUtil} </li>
 * <li> {@link PropertiesPanel} </li>
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
public class Changes {

	private long timestamp = System.currentTimeMillis();

	/** every {@link Changes} Object has two {@link ComponentSettings} to log the state before and the state after the Change */
	private ComponentSettings initialSettings = new ComponentSettings();
	private ComponentSettings finalSettings = new ComponentSettings();

	/** the Change that was made */
	private int changesMade;

	/** the internal Number of the Change */
	private int changeCounter;

	/** time in miliseconds between two actions */
	private static final int TOLERANCE_BETWEEN_ACTIONS = 750;

	/** possible Changes that can be made */
	public static final int DELETEROW = 0;
	public static final int ADDROW = 1;
	public static final int DELETECOL = 2;
	public static final int ADDCOL = 3;
	public static final int MODIFY_ROW = 4;
	public static final int MODIFY_COL = 5;
	public static final int MOVE_TO_CELL = 6;
	public static final int INSERT_TO_CELL = 7;
	public static final int DELETE_FROM_CELL = 8;
	public static final int CHANGE_ALIGNMENT = 9;
	public static final int CHANGE_PROPERTIES = 10;
	public static final int CHANGE_LAYOUTMLRULES = 11;

	/**
	 * This Method checks if a action is a new Action or is the old one.<br>
	 * Resizing the Layout with the Mouse did log every Pixel that the Layout was moved.<br>
	 * This is prevented by this method.
	 * 
	 * @return true if the Change did happen after some time 
	 */
	public boolean isAnotherAction() {
		if (System.currentTimeMillis() - TOLERANCE_BETWEEN_ACTIONS > this.timestamp) {
			return true;
		} else
			return false;
	}

	/**
	 * Logging the Time the Change did happen, needed for {@link #isAnotherAction()}
	 */
	public void setTimeStamp() {
		this.timestamp = System.currentTimeMillis();
	}

	/**
	 * @param changeCounter the number of this change
	 */
	public Changes(int changeCounter) {
		this.changeCounter = changeCounter;
	}

	/**
	 * This Method stores the provided Information in this {@link Changes} Object.<br>
	 * The information is done by the Scheme:
	 * <ul>
	 * <li> If this is the fist time (initialSettings are not initialized)- put the Information in {@link ComponentSettings} for the <b>initial</b> State</li>
	 * <li> If this is the second time (initialSettings are initialized)  - put the Information in {@link ComponentSettings} for the <b>final</b> State</li>
	 * </ul>
	 * 
	 * @param c
	 * @param componentProperties
	 * @param tableLayoutUtil
	 * @param changesMade
	 */
	public void setChanges(WYSIWYGComponent c, ComponentProperties componentProperties,TableLayoutUtil tableLayoutUtil, int changesMade) {
		this.changesMade = changesMade;

		if (!initialSettings.isInitialized()) {
			initialSettings.setWYSIWYGComponent(c);
			initialSettings.setComponentProperties(componentProperties);
			initialSettings.setTableLayoutUtil(tableLayoutUtil);
			initialSettings.setInitialized();
		} else {
			finalSettings.setWYSIWYGComponent(c);
			finalSettings.setComponentProperties(componentProperties);
			finalSettings.setTableLayoutUtil(tableLayoutUtil);
			finalSettings.setInitialized();
		}
	}
	
	/**
	 * This Method stores the provided Information in this {@link Changes} Object.<br>
	 * The information is done by the Scheme:
	 * <ul>
	 * <li> If this is the fist time (initialSettings are not initialized)- put the Information in {@link ComponentSettings} for the <b>initial</b> State</li>
	 * <li> If this is the second time (initialSettings are initialized)  - put the Information in {@link ComponentSettings} for the <b>final</b> State</li>
	 * </ul>
	 * 
	 * @param c
	 * @param layoutMLRules
	 * @param tableLayoutUtil
	 * @param changesMade
	 */
	public void setChanges(WYSIWYGComponent c, LayoutMLRules layoutMLRules,TableLayoutUtil tableLayoutUtil, int changesMade) {
		this.changesMade = changesMade;

		if (!initialSettings.isInitialized()) {
			initialSettings.setWYSIWYGComponent(c);
			initialSettings.setLayoutMLRules(layoutMLRules);
			initialSettings.setTableLayoutUtil(tableLayoutUtil);
			initialSettings.setInitialized();
		} else {
			finalSettings.setWYSIWYGComponent(c);
			finalSettings.setLayoutMLRules(layoutMLRules);
			finalSettings.setTableLayoutUtil(tableLayoutUtil);
			finalSettings.setInitialized();
		}
	}

	/**
	 * This Method stores the provided Information in this {@link Changes} Object.<br>
	 * The information is done by the Scheme:
	 * <ul>
	 * <li> If this is the fist time (initialSettings are not initialized)- put the Information in {@link ComponentSettings} for the <b>initial</b> State</li>
	 * <li> If this is the second time (initialSettings are initialized)  - put the Information in {@link ComponentSettings} for the <b>final</b> State</li>
	 * </ul>
	 * 
	 * @param cell
	 * @param c
	 * @param changesMade
	 * @param tableLayoutUtil
	 */
	public void setChanges(LayoutCell cell, WYSIWYGComponent c, int changesMade, TableLayoutUtil tableLayoutUtil) {
		this.changesMade = changesMade;

		if (!initialSettings.isInitialized()) {
			initialSettings.setLayoutCell(cell);
			initialSettings.setTableLayoutUtil(tableLayoutUtil);
			initialSettings.setWYSIWYGComponent(c);
			initialSettings.setInitialized();
		} else {
			finalSettings.setLayoutCell(cell);
			finalSettings.setTableLayoutUtil(tableLayoutUtil);
			finalSettings.setWYSIWYGComponent(c);
			finalSettings.setInitialized();
		}
	}

	/**
	 * This Method stores the provided Information in this {@link Changes} Object.<br>
	 * The information is done by the Scheme:
	 * <ul>
	 * <li> If this is the fist time (initialSettings are not initialized)- put the Information in {@link ComponentSettings} for the <b>initial</b> State</li>
	 * <li> If this is the second time (initialSettings are initialized)  - put the Information in {@link ComponentSettings} for the <b>final</b> State</li>
	 * </ul>
	 * 
	 * @param constraints
	 * @param c
	 * @param changesMade
	 * @param tableLayoutUtil
	 */
	public void setChanges(TableLayoutConstraints constraints, WYSIWYGComponent c, int changesMade, TableLayoutUtil tableLayoutUtil) {
		this.changesMade = changesMade;

		if (!initialSettings.isInitialized()) {
			initialSettings.setWYSIWYGComponent(c);
			initialSettings.setTableLayoutConstraint(constraints);
			initialSettings.setTableLayoutUtil(tableLayoutUtil);
			initialSettings.setInitialized();
		} else {
			finalSettings.setWYSIWYGComponent(c);
			finalSettings.setTableLayoutConstraint(constraints);
			finalSettings.setTableLayoutUtil(tableLayoutUtil);
			finalSettings.setInitialized();
		}
	}
	
	/**
	 * This Method stores the provided Information in this {@link Changes} Object.<br>
	 * The information is done by the Scheme:
	 * <ul>
	 * <li> If this is the fist time (initialSettings are not initialized)- put the Information in {@link ComponentSettings} for the <b>initial</b> State</li>
	 * <li> If this is the second time (initialSettings are initialized)  - put the Information in {@link ComponentSettings} for the <b>final</b> State</li>
	 * </ul>
	 * 
	 * @param cell
	 * @param changesMade
	 * @param tableLayoutUtil
	 */
	public void setChanges(LayoutCell cell, int changesMade, TableLayoutUtil tableLayoutUtil) {
		this.changesMade = changesMade;

		if (!initialSettings.isInitialized()) {
			initialSettings.setLayoutCell(cell);
			initialSettings.setTableLayoutUtil(tableLayoutUtil);
			initialSettings.setInitialized();
		} else {
			finalSettings.setLayoutCell(cell);
			finalSettings.setTableLayoutUtil(tableLayoutUtil);
			finalSettings.setInitialized();
		}
	}

	/**
	 * This Method does transfer the initial {@link ComponentSettings} to the final {@link ComponentSettings}<br>
	 * This is needed for {@link Changes} like {@link TableLayoutUtil#addCol(LayoutCell)} or {@link TableLayoutUtil#delRow(LayoutCell)}
	 */
	public void copySettings() {
		if (initialSettings.isInitialized()) {
			finalSettings.setComponentProperties(initialSettings.getComponentProperties());
			finalSettings.setLayoutCell(initialSettings.getLayoutCell());
			finalSettings.setTableLayoutUtil(initialSettings.getTableLayoutUtil());
			finalSettings.setTableLayoutConstraint(initialSettings.getTableLayoutConstraint());
			finalSettings.setWYSIWYGComponent(initialSettings.getWYSIWYGComponent());
			finalSettings.setInitialized();
		}
	}

	/**
	 * @return the action that was performed
	 */
	public int getChangesMade() {
		return this.changesMade;
	}

	/**
	 * @param changesMade the Action that was performed, stored for every {@link ComponentSettings} 
	 */
	public void setChangesMade(int changesMade) {
		this.changesMade = changesMade;
		if (!initialSettings.isInitialized()) {
			initialSettings.setChangesMade(changesMade);
		} else {
			finalSettings.setChangesMade(changesMade);
		}
	}

	/**
	 * @return this is the State the {@link WYSIWYGComponent} had before the Change
	 */
	public ComponentSettings getInitialSettings() {
		return this.initialSettings;
	}

	/**
	 * @return this is the State the {@link WYSIWYGComponent} had after the Change
	 */
	public ComponentSettings getFinalSettings() {
		return this.finalSettings;
	}

	/**
	 * This Method is needed for resetting the Final Settings.<br>
	 * Its called if the size of the Layout is changed by Mouse
	 * @see #isAnotherAction()
	 * @see UndoRedoFunction#loggingChangeHeightOfRow(LayoutCell, TableLayoutUtil, boolean)
	 * @see UndoRedoFunction#loggingChangeWidthOfColumn(LayoutCell, TableLayoutUtil, boolean)
	 */
	public void resetFinalSettings() {
		this.finalSettings = new ComponentSettings();
	}

	/**
	 * Overwritten Method for nice printout of the contents of this Class.
	 * Calls {@link ComponentSettings#toString()}
	 */
	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("\nChangeset: " + changeCounter + "\n");
		buffer.append("Changeset for Action: " + changesMade + "\n");
		buffer.append("Tolerance between Actions: " + TOLERANCE_BETWEEN_ACTIONS + "\n");
		buffer.append("Initial Settings:" + "\n");
		buffer.append(initialSettings.toString() + "\n\n");
		buffer.append("Final Settings:" + "\n");
		buffer.append(finalSettings.toString() + "\n");
		return buffer.toString();
	}
}
