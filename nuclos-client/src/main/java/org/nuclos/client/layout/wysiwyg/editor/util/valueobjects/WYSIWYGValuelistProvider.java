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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;

import org.nuclos.common.collect.collectable.CollectableFieldsProvider;
import org.nuclos.common2.layoutml.LayoutMLConstants;

/**
 * This class stores {@link LayoutMLConstants#ELEMENT_VALUELISTPROVIDER}.<br>
 * It may contain several {@link WYSIYWYGParameter}.<br>
 *
 * <br>
 * Created by Novabit Informationssysteme GmbH <br>
 * Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author <a href="mailto:hartmut.beckschulze@novabit.de">hartmut.beckschulze</a>
 * @version 01.00.00
 */
@SuppressWarnings("serial")
public class WYSIWYGValuelistProvider implements Cloneable, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * <!ELEMENT valuelist-provider (parameter*)> <!ATTLIST valuelist-provider
	 * type CDATA #REQUIRED >
	 */

	private String type = "";

	/** this is where the Parameters are stored */
	private Vector<WYSIYWYGParameter> parameter = null;

	/**
	 * Default Constructor
	 */
	public WYSIWYGValuelistProvider(){
		parameter = new Vector<WYSIYWYGParameter>(1);
	}

	/**
	 * Constructor, setting {@link LayoutMLConstants#ATTRIBUTE_TYPE}
	 * @param type the Name of the {@link CollectableFieldsProvider}
	 */
	public WYSIWYGValuelistProvider(String type){
		this();
		setType(type);
	}

	/**
	 * @return the Name of the {@link CollectableFieldsProvider}
	 */
	public String getType() {
		return type;
	}

	/**
	 * @param type the {@link CollectableFieldsProvider} to set as {@link LayoutMLConstants#ATTRIBUTE_TYPE}
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * @param wysiwygParameter the {@link WYSIYWYGParameter} to add
	 */
	public void addWYSIYWYGParameter(WYSIYWYGParameter wysiwygParameter) {
		if (!parameter.contains(wysiwygParameter))
			this.parameter.add(wysiwygParameter);
	}

	/**
	 * @param wysiwygParameter the {@link WYSIYWYGParameter} to remove from this {@link CollectableFieldsProvider}
	 */
	public void removeWYSIYWYGParameter(WYSIYWYGParameter wysiwygParameter){
		this.parameter.remove(wysiwygParameter);
	}

	/**
	 * @return all {@link WYSIYWYGParameter} set for this {@link WYSIWYGValuelistProvider}
	 */
	public Vector<WYSIYWYGParameter> getAllWYSIYWYGParameter(){
		return this.parameter;
	}
	
	public List<WYSIYWYGParameter> getAllWYSIYWYGParameterSorted(final boolean descend){
		List<WYSIYWYGParameter> result = new ArrayList<WYSIYWYGParameter>(getAllWYSIYWYGParameter());
		Collections.sort(result, new Comparator<WYSIYWYGParameter>() {
			@Override
			public int compare(WYSIYWYGParameter o1, WYSIYWYGParameter o2) {
				if (o1 == null) {
					return descend?1:-1;
				}
				if (o2 == null) {
					return descend?-1:1;
				}
				return descend?o2.getParameterName().compareToIgnoreCase(o1.getParameterName())
					:o1.getParameterName().compareToIgnoreCase(o2.getParameterName());
			}
		});
		return result;
	}

	/**
	 * Overwritten clone Method. Creating a new Instance of this {@link WYSIWYGValuelistProvider}<br>
	 * calls {@link WYSIYWYGParameter#clone()}
	 */
	@Override
	public Object clone() throws CloneNotSupportedException {
		WYSIWYGValuelistProvider clonedValuelistProvider = new WYSIWYGValuelistProvider();
		if (this.getType() != null) {
			String clonedType = new String(this.getType());
			clonedValuelistProvider.setType(clonedType);
		}

		for (WYSIYWYGParameter parameter : getAllWYSIYWYGParameter()) {
			clonedValuelistProvider.addWYSIYWYGParameter((WYSIYWYGParameter)parameter.clone());
		}

		return clonedValuelistProvider;
	}
}
