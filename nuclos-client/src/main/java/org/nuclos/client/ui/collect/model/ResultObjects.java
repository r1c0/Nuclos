package org.nuclos.client.ui.collect.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.NullArgumentException;

/**
 * Encapsulates the lists of available and selected fields, resp.
 * The selected fields are shown as columns in the result table.
 * The selected fields are always in sync with the table column model, but not necessarily
 * with the table model's columns.
 */
public class ResultObjects<T> {

	/**
	 * the list of available (currently not selected) fields
	 */
	private List<T> lstclctefAvailable = new ArrayList<T>();

	/**
	 * the list of selected fields
	 */
	private List<T> lstclctefSelected = new ArrayList<T>();
	
	public ResultObjects() {
	}

	/**
	 * sets the available and selected fields, respectively.
	 * @param lstclctefAvailable available (currently not selected) fields
	 * @param lstclctefSelected selected fields
	 * @precondition lstclctefAvailable != null;
	 * @precondition lstclctefSelected != null;
	 */
	public void set(List<T> lstclctefAvailable, List<T> lstclctefSelected) {
		if (lstclctefAvailable == null) {
			throw new NullArgumentException("lstclctefAvailable");
		}
		if (lstclctefSelected == null) {
			throw new NullArgumentException("lstclctefSelected");
		}
		this.lstclctefAvailable = lstclctefAvailable;
		this.lstclctefSelected = lstclctefSelected;

	}

	/**
	 * @return the available (currently not selected) fields
	 * @postcondition result != null
	 */
	public List<T> getAvailableFields() {
		return Collections.unmodifiableList(this.lstclctefAvailable);
	}

	/**
	 * @return List<CollectableEntityField> the selected fields that are shown as columns in the result table
	 * @postcondition result != null
	 */
	public List<T> getSelectedFields() {
		return Collections.unmodifiableList(this.lstclctefSelected);
	}

	/**
	 * sets the selected fields. The available fields are adjusted accordingly.
	 * @param lstclctefSelected
	 */
	public void setSelectedFields(List<T> lstclctefSelected) {
		this.lstclctefAvailable.addAll(this.lstclctefSelected);
		this.lstclctefSelected.clear();
		this.moveToSelectedFields(lstclctefSelected);
	}

	/**
	 * moves the given fields from the available to the selected fields.
	 * @param lstclctef
	 */
	public void moveToSelectedFields(List<T> lstclctef) {
		for (T clctef : lstclctef) {
			this.moveToSelectedFields(clctef);
		}
	}

	/**
	 * moves the given field from the available to the selected fields.
	 * @param clctef
	 */
	private void moveToSelectedFields(T clctef) {
		this.lstclctefAvailable.remove(clctef);
		this.lstclctefSelected.add(clctef);
	}

	/**
	 * moves the given field from the available to the selected fields, inserting it at the given position.
	 * @param clctef
	 */
	public void moveToSelectedFields(int iColumn, T clctef) {
		this.lstclctefSelected.add(iColumn, clctef);
		this.lstclctefAvailable.remove(clctef);
	}

	/**
	 * moves the given field from the selected to the available fields.
	 * @param clctef
	 */
	public void moveToAvailableFields(T clctef) {
		this.lstclctefSelected.remove(clctef);
		this.lstclctefAvailable.add(clctef);
	}

}	// class ResultObjects
