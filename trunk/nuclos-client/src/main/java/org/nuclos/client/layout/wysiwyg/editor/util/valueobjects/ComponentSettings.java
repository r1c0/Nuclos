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

import java.awt.Component;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import org.apache.log4j.Logger;

import org.nuclos.client.layout.wysiwyg.component.WYSIWYGComponent;
import org.nuclos.client.layout.wysiwyg.component.properties.ComponentProperties;
import org.nuclos.client.layout.wysiwyg.editor.util.TableLayoutUtil;
import org.nuclos.client.layout.wysiwyg.editor.util.UndoRedoFunction;
import org.nuclos.client.layout.wysiwyg.editor.util.valueobjects.layoutmlrules.LayoutMLRules;

/**
 * This class is used by {@link UndoRedoFunction} to store the State of the {@link WYSIWYGComponent} Settings in a {@link Changes} Object.
 * 
 * 
 * <br>
 * Created by Novabit Informationssysteme GmbH <br>
 * Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 * 
 * @author <a href="mailto:hartmut.beckschulze@novabit.de">hartmut.beckschulze</a>
 * @version 01.00.00
 */
public class ComponentSettings {
	
	/** the component the settings are for */
	private WYSIWYGComponent c = null;
	/** the tablelayoutconstraints of this component */
	private TableLayoutConstraints constraint = null;
	/** the tablelayoututil related to the component */
	private TableLayoutUtil tableLayoutUtil = null;
	/** the component's properties */
	private ComponentProperties componentProperties = null;
	/** the cell where the component is lying */
	private LayoutCell cell = new LayoutCell();
	/** the rules attached to the component */
	private LayoutMLRules layoutMLRules = null;
	/** boolean to track the state */
	private boolean isInitialized = false;
	
	private static final Logger log = Logger.getLogger(ComponentSettings.class);

	/**
	 * @return the {@link WYSIWYGComponent} related to this {@link ComponentSettings}
	 */
	public WYSIWYGComponent getWYSIWYGComponent() {
		return c;
	}

	/**
	 * @param c setting the {@link WYSIWYGComponent} related to this {@link ComponentSettings}
	 */
	public void setWYSIWYGComponent(WYSIWYGComponent c) {
		this.c = c;
	}

	/**
	 * @param changesMade sets what kind of Change was done 
	 * @see Changes
	 */
	public void setChangesMade(int changesMade) {
		if (changesMade == Changes.DELETE_FROM_CELL) {
			Component component = (Component) c;
			MouseListener[] mouseListener = component.getMouseListeners();
			for (int i = 0; i < mouseListener.length; i++) {
				component.removeMouseListener(mouseListener[i]);
			}

			MouseMotionListener[] mouseMotionListener = component.getMouseMotionListeners();
			for (int i = 0; i < mouseMotionListener.length; i++) {
				component.removeMouseMotionListener(mouseMotionListener[i]);
			}

			this.c = (WYSIWYGComponent) component;
		}
	}

	public boolean isInitialized() {
		return this.isInitialized;
	}

	public void setInitialized() {
		this.isInitialized = true;
	}

	/**
	 * @return the {@link TableLayoutConstraints} related to the {@link WYSIWYGComponent} and {@link TableLayoutUtil} stored within
	 * @see #getWYSIWYGComponent()
	 * @see #getTableLayoutUtil()
	 */
	public TableLayoutConstraints getTableLayoutConstraint() {
		return constraint;
	}

	/**
	 * @param constraint to store. Related to the {@link WYSIWYGComponent} and the {@link TableLayoutUtil}
	 */
	public void setTableLayoutConstraint(TableLayoutConstraints constraint) {
		this.constraint = constraint;
	}

	/**
	 * @return the {@link TableLayoutUtil} related to the stored {@link WYSIWYGComponent}
	 * @see #getWYSIWYGComponent()
	 */
	public TableLayoutUtil getTableLayoutUtil() {
		return tableLayoutUtil;
	}

	/**
	 * @param tableLayoutUtil the {@link TableLayoutUtil} thats responsible for the {@link WYSIWYGComponent} in this {@link ComponentSettings}
	 * @see #setWYSIWYGComponent(WYSIWYGComponent)
	 */
	public void setTableLayoutUtil(TableLayoutUtil tableLayoutUtil) {
		this.tableLayoutUtil = tableLayoutUtil;
	}

	/**
	 * @return the stored {@link ComponentProperties}
	 */
	public ComponentProperties getComponentProperties() {
		return componentProperties;
	}

	/**
	 * @param componentProperties to store
	 */
	public void setComponentProperties(ComponentProperties componentProperties) {
		this.componentProperties = componentProperties;
	}

	/**
	 * @return the {@link LayoutCell} stored in this {@link ComponentSettings}
	 */
	public LayoutCell getLayoutCell() {
		return cell;
	}

	/**
	 * Storing the {@link LayoutCell} Information
	 * @param otherCell the {@link LayoutCell} where the Information is gathered
	 */
	public void setLayoutCell(LayoutCell otherCell) {
		setLayoutCell(otherCell.getCellX(), otherCell.getCell2X(), otherCell.getCellY(), otherCell.getCell2Y(), otherCell.getCellWidth(), otherCell.getCellHeight());
	}

	/**
	 * Storing the {@link LayoutCell} Information
	 * @param col1
	 * @param col2
	 * @param row1
	 * @param row2
	 * @param width
	 * @param height
	 * @see Changes#setChanges(int, int, double, double, int)
	 * @see #setLayoutCell(int, int, int, int, double, double)
	 */
	private void setLayoutCell(int col1, int col2, int row1, int row2, double width, double height) {
		this.cell = new LayoutCell();
		cell.setCellX(col1);
		cell.setCell2X(col2);
		cell.setCellY(row1);
		cell.setCell2Y(row2);
		cell.setCellHeight(height);
		cell.setCellWidth(width);
	}

	/** 
	 * @return the attaches {@link LayoutMLRules}
	 */
	public LayoutMLRules getLayoutMLRules() {
		return layoutMLRules;
	}

	/**
	 * @param layoutMLRules attaching the {@link LayoutMLRules}
	 */
	public void setLayoutMLRules(LayoutMLRules layoutMLRules) {
		try {
			this.layoutMLRules = (LayoutMLRules) layoutMLRules.clone();
		} catch (CloneNotSupportedException e) {
			log.error(e);
		}
	}

	/**
	 * Overwritten toString Method. Turns this Object into a readable Form on the Console.
	 */
	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();

		if (c != null)
			buffer.append("WYSIWYGComponent: " + c.toString() + "\n");
		else
			buffer.append("WYSIWYGComponent: null" + "\n");

		if (constraint != null)
			buffer.append("TableLayoutConstraint: " + constraint.toString() + "\n");
		else
			buffer.append("TableLayoutConstraint: null" + "\n");

		if (componentProperties != null)
			buffer.append("ComponentProperties: " + componentProperties.toString() + "\n");
		else
			buffer.append("ComponentProperties: null" + "\n");

		if (cell != null)
			buffer.append("LayoutCell: " + cell.toString() + "\n");
		else
			buffer.append("LayoutCell: null" + "\n");

		buffer.append("is initialized: " + isInitialized);

		return buffer.toString();
	}

	/**
	 * Overwritten equals Method for compairing two {@link ComponentSettings}Objects
	 */
	@Override
	public boolean equals(Object obj) {
		boolean equal = true;

		if (obj instanceof ComponentSettings) {
			ComponentSettings temp = (ComponentSettings) obj;

			if (temp.getLayoutCell() != null) {
				if (!temp.getLayoutCell().equals(this.getLayoutCell()))
					equal = false;
			} else {
				if (this.getLayoutCell() != null)
					if (!this.getLayoutCell().equals(temp.getLayoutCell()))
						equal = false;
			}

			if (temp.getWYSIWYGComponent() != null) {
				if (!temp.getWYSIWYGComponent().equals(this.getWYSIWYGComponent()))
					equal = false;
			} else {
				if (this.getWYSIWYGComponent() != null)
					if (!this.getWYSIWYGComponent().equals(temp.getWYSIWYGComponent()))
						equal = false;
			}

			if (temp.getComponentProperties() != null) {
				if (!temp.getComponentProperties().equals(this.getComponentProperties()))
					equal = false;
			} else {
				if (this.getComponentProperties() != null)
					if (!this.getComponentProperties().equals(temp.getComponentProperties()))
						equal = false;
			}

			if (temp.getTableLayoutConstraint() != null) {
				if (!temp.getTableLayoutConstraint().equals(this.getTableLayoutConstraint()))
					equal = false;
			} else {
				if (this.getTableLayoutConstraint() != null)
					if (!this.getTableLayoutConstraint().equals(temp.getTableLayoutConstraint()))
						equal = false;
			}

			if (temp.getTableLayoutUtil() != null) {
				if (!temp.getTableLayoutUtil().equals(this.getTableLayoutUtil()))
					equal = false;
			} else {
				if (this.getTableLayoutUtil() != null)
					if (!this.getTableLayoutUtil().equals(temp.getTableLayoutUtil()))
						equal = false;
			}
			
			if (temp.getLayoutMLRules() != null) {
				if (!temp.getLayoutMLRules().equals(this.getLayoutMLRules()))
					equal = false;
			} else {
				if (this.getLayoutMLRules() != null)
					if (!this.getLayoutMLRules().equals(temp.getLayoutMLRules()))
						equal = false;
			}

			if (temp.isInitialized != this.isInitialized)
				equal = false;

		} else {
			equal = false;
		}
		return equal;
	}

}
