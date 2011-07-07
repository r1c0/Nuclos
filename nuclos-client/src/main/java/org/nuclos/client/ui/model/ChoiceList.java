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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang.NullArgumentException;

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
	
	private static final Class<?>[] NO_ARGS = new Class<?>[0];

	/**
	 * the list of available (currently not selected) fields
	 */
	private List<T> lstclctefAvailable = new ArrayList<T>();

	/**
	 * the list of selected fields
	 */
	private List<T> lstclctefSelected = new ArrayList<T>();
	
	private Comparator<? super T> compAvailable;
	
	public ChoiceList() {
	}
	
	public Object clone() throws CloneNotSupportedException {
		final ChoiceList<T> clone = (ChoiceList<T>) super.clone();
		clone.lstclctefAvailable = cloneList(lstclctefAvailable);
		clone.lstclctefSelected = cloneList(lstclctefSelected);
		return clone;
	}
	
	private static <T> List<T> cloneList(List<T> l) throws CloneNotSupportedException {
		if (l == null)
			return null;
		final Class<?> clazz = l.getClass();
		// Don't try this on the unmodifiable stuff
		if (clazz.getName().startsWith("java.util.Collections")) {
			return new ArrayList<T>(l);
		}
		final List<T> result;
		try {
			Method m = clazz.getMethod("clone", NO_ARGS);
			result = (List<T>) m.invoke(l);
		} catch (IllegalArgumentException e) {
			throw new CloneNotSupportedException(e.toString());
		} catch (IllegalAccessException e) {
			throw new CloneNotSupportedException(e.toString());
		} catch (InvocationTargetException e) {
			throw new CloneNotSupportedException(e.toString());
		} catch (SecurityException e) {
			throw new CloneNotSupportedException(e.toString());
		} catch (NoSuchMethodException e) {
			throw new CloneNotSupportedException(e.toString());
		}
		return result;
	}

	/**
	 * sets the available and selected fields, respectively.
	 * @param lstclctefAvailable available (currently not selected) fields
	 * @param lstclctefSelected selected fields
	 * @precondition lstclctefAvailable != null;
	 * @precondition lstclctefSelected != null;
	 */
	public void set(List<T> lstclctefAvailable, List<T> lstclctefSelected, Comparator<? super T> comp) {
		if (lstclctefAvailable == null) {
			throw new NullArgumentException("lstclctefAvailable");
		}
		if (lstclctefSelected == null) {
			throw new NullArgumentException("lstclctefSelected");
		}
		this.lstclctefAvailable = lstclctefAvailable;
		this.lstclctefSelected = lstclctefSelected;
		this.compAvailable = comp;
	}

	/**
	 * @return the available (currently not selected) fields
	 * @postcondition result != null
	 */
	public List<T> getAvailableFields() {
		return Collections.unmodifiableList(this.lstclctefAvailable);
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

}	// class ResultObjects
