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

import java.io.Serializable;
import java.util.ArrayList;

import org.nuclos.common2.layoutml.LayoutMLConstants;

/**
 * This class Handles the {@link LayoutMLConstants#ELEMENT_OPTIONS}
 * Every {@link WYSIWYGOptions} contains none or more {@link WYSIWYGOption}
 * 
 * <br>
 * Created by Novabit Informationssysteme GmbH <br>
 * Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 * 
 * @author <a href="mailto:hartmut.beckschulze@novabit.de">hartmut.beckschulze</a>
 * @version 01.00.00
 */
@SuppressWarnings("serial")
public class WYSIWYGOptions implements Cloneable, Serializable {

	private String name = "";
	private String defaultValue = "";
	private String orientation = "";

	/** the orientation possible */
	public static final String ORIENTATION_HORIZONTAL = LayoutMLConstants.ATTRIBUTEVALUE_HORIZONTAL;
	public static final String ORIENTATION_VERTICAL = LayoutMLConstants.ATTRIBUTEVALUE_VERTICAL;

	/** storing the option values */
	private ArrayList<WYSIWYGOption> allOptions = null;

	/**
	 * Default Constructor, does some "init stuff"
	 */
	public WYSIWYGOptions() {
		allOptions = new ArrayList<WYSIWYGOption>();
		setOrientation(ORIENTATION_HORIZONTAL);
	}

	/**
	 * 
	 * @param name the name of this {@link WYSIWYGOptions}
	 * @param defaultValue the defaultvalue that should be selected
	 * @param orientation the orientation for this {@link WYSIWYGOptions}: 
	 * <ul>
	 * <li>{@link #ORIENTATION_HORIZONTAL}</li>
	 * <li>{@link #ORIENTATION_VERTICAL}</li>
	 * </ul>
	 */
	public WYSIWYGOptions(String name, String defaultValue, String orientation) {
		this();
		setName(name);
		setDefaultValue(defaultValue);
		setOrientation(orientation);
	}
	
	/**
	 * @return the Name of this {@link WYSIWYGOptions}
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the Name for this {@link WYSIWYGOptions}
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return returns the set Defaultvalue
	 */
	public String getDefaultValue() {
		return defaultValue;
	}

	/**
	 * @param defaultValue to set, should be a value contained in {@link WYSIWYGOption}
	 */
	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}

	/**
	 * @return the Orientation of the Buttons
	 * @see #ORIENTATION_HORIZONTAL
	 * @see #ORIENTATION_VERTICAL
	 */
	public String getOrientation() {
		return orientation;
	}

	/**
	 * @param orientation the Orientation zu set
	 * @see #ORIENTATION_HORIZONTAL
	 * @see #ORIENTATION_VERTICAL
	 */
	public void setOrientation(String orientation) {
		if (ORIENTATION_HORIZONTAL.equals(orientation) || ORIENTATION_VERTICAL.equals(orientation))
			this.orientation = orientation;
	}

	/**
	 * @param option the {@link WYSIWYGOption} to add
	 */
	public void addOptionToOptionsGroup(WYSIWYGOption option) {
		allOptions.add(option);
	}

	/**
	 * @param option remove a {@link WYSIWYGOption} from this {@link WYSIWYGOptions}
	 */
	public void removeOptionFromOptionsGroup(WYSIWYGOption option) {
		allOptions.remove(option);
	}

	/**
	 * @return all {@link WYSIWYGOption}
	 */
	public ArrayList<WYSIWYGOption> getAllOptionValues() {
		return allOptions;
	}

	/**
	 * Overwritten clone Method
	 * calling {@link WYSIWYGOption#clone()}
	 */
	@Override
	public Object clone() throws CloneNotSupportedException {
		WYSIWYGOptions clonedOptions = new WYSIWYGOptions();
		clonedOptions.setDefaultValue(new String(defaultValue));
		clonedOptions.setName(new String(defaultValue));
		clonedOptions.setOrientation(new String(orientation));

		for (WYSIWYGOption option : allOptions) {
			clonedOptions.addOptionToOptionsGroup((WYSIWYGOption) option.clone());
		}

		return clonedOptions;
	}
}
