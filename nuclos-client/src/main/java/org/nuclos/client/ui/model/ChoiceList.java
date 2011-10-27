//Copyright (C) 2011  Novabit Informationssysteme GmbH
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
package org.nuclos.client.ui.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.lang.NullArgumentException;
import org.nuclos.common.CloneUtils;

/**
 * Encapsulates the lists of available and selected objects, resp.
 * The selected fields are shown as columns in the result table.
 * The selected fields are always in sync with the table column model, but not necessarily
 * with the table model's columns.
 * <p>
 * Formerly known as ResultObjects.
 * </p>
 * @since Nuclos 3.1.01 this is a top-level class.
 */
public class ChoiceList<T> implements Cloneable {

	/**
	 * the list of available (currently not selected) fields
	 */
	private SortedSet<T> lstclctefAvailable;

	/**
	 * the list of selected fields
	 */
	private List<T> lstclctefSelected;

	private Comparator<? super T> compAvailable;

	private Collection<T> fixed;

	public ChoiceList() {
	}

	public Object clone() {
		try {
			final ChoiceList<T> clone = (ChoiceList<T>) super.clone();
			clone.lstclctefAvailable = (SortedSet<T>) CloneUtils.cloneCollection(lstclctefAvailable);
			clone.lstclctefSelected = (List<T>) CloneUtils.cloneCollection(lstclctefSelected);
			return clone;
		} catch (CloneNotSupportedException e) {
			throw new IllegalStateException("clone() not supported", e);
		}
	}

	/**
	 * sets the available and selected fields, respectively.
	 * @param lstclctefAvailable available (currently not selected) fields
	 * @param lstclctefSelected selected fields
	 * @precondition lstclctefAvailable != null;
	 * @precondition lstclctefSelected != null;
	 */
	public void set(SortedSet<T> lstclctefAvailable, List<T> lstclctefSelected, Comparator<? super T> comp) {
		if (lstclctefAvailable == null) {
			throw new NullArgumentException("lstclctefAvailable");
		}
		if (lstclctefSelected == null) {
			throw new NullArgumentException("lstclctefSelected");
		}
		if (comp == null) {
			throw new NullArgumentException("compAvailable");
		}
		this.lstclctefAvailable = lstclctefAvailable;
		this.lstclctefSelected = lstclctefSelected;
		this.fixed = new ArrayList<T>();
		this.compAvailable = comp;
	}

	public void set(Collection<T> available, Comparator<? super T> comp) {
		if (comp == null) {
			throw new NullArgumentException("compAvailable");
		}
		final SortedSet<T> set = new TreeSet<T>(comp);
		set.addAll(available);
		set(set, new ArrayList<T>(), comp);
	}

	public Collection<T> getFixed() {
		return fixed;
	}

	public void setFixed(Collection<T> fixed) {
		this.fixed = fixed;
	}

	/**
	 * @return the available (currently not selected) fields
	 * @postcondition result != null
	 */
	public SortedSet<T> getAvailableFields() {
		return Collections.unmodifiableSortedSet(this.lstclctefAvailable);
	}

	public Comparator<? super T> getComparatorForAvaible() {
		return compAvailable;
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

}	// class ChoiceList
