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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.nuclos.client.layout.wysiwyg.WYSIWYGStringsAndLabels.PROPERTY_LABELS;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGComponent;
import org.nuclos.client.layout.wysiwyg.component.properties.ComponentProperties;
import org.nuclos.client.layout.wysiwyg.component.properties.PropertiesPanel;

/**
 * This class is sorting the {@link ComponentProperties} shown in the {@link PropertiesPanel}.<br>
 * Every {@link PropertiesPanel} should look "the same".<br>
 * So common Properties must be at the same Position in the dialog.
 * <br>
 * 
 * <br>
 * Created by Novabit Informationssysteme GmbH <br>
 * Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 * 
 * @author <a href="mailto:hartmut.beckschulze@novabit.de">hartmut.beckschulze</a>
 * @version 01.00.00
 */
public class PropertiesSorter {
	
	/** getting the labels for sorting */
	public static final String PROPERTY_NAME = PROPERTY_LABELS.NAME;
	public static final String PROPERTY_MNEMONIC = PROPERTY_LABELS.MNEMONIC;
	public static final String PROPERTY_COLLECTABLECOMPONENTPROPERTY = PROPERTY_LABELS.COLLECTABLECOMPONENTPROPERTY;
	public static final String CONSTRAINT_COL1 = PROPERTY_LABELS.CONSTRAINT_COL1;
	public static final String CONSTRAINT_COL2 = PROPERTY_LABELS.CONSTRAINT_COL2;
	public static final String CONSTRAINT_ROW1 = PROPERTY_LABELS.CONSTRAINT_ROW1;
	public static final String CONSTRAINT_ROW2 = PROPERTY_LABELS.CONSTRAINT_ROW2;
	public static final String CONSTRAINT_HALIGN = PROPERTY_LABELS.CONSTRAINT_HALIGN;
	public static final String CONSTRAINT_VALIGN = PROPERTY_LABELS.CONSTRAINT_VALIGN;
	public static final String PROPERTY_VALUELISTPROVIDER = PROPERTY_LABELS.VALUELISTPROVIDER;
	public static final String PROPERTY_BACKGROUNDCOLOR = PROPERTY_LABELS.BACKGROUNDCOLOR;
	public static final String PROPERTY_BORDER = PROPERTY_LABELS.BORDER;
	public static final String PROPERTY_FONT = PROPERTY_LABELS.FONT;
	public static final String PROPERTY_DESCRIPTION = PROPERTY_LABELS.DESCRIPTION;
	public static final String PROPERTY_PREFFEREDSIZE = PROPERTY_LABELS.PREFFEREDSIZE;
	public static final String PROPERTY_SIZE = PROPERTY_LABELS.SIZE;
	public static final String PROPERTY_ENABLED = PROPERTY_LABELS.ENABLED;
	public static final String PROPERTY_VISIBLE = PROPERTY_LABELS.VISIBLE;
	public static final String PROPERTY_OPAQUE = PROPERTY_LABELS.OPAQUE;
	public static final String PROPERTY_ROWS = PROPERTY_LABELS.ROWS;
	public static final String PROPERTY_COLUMNS = PROPERTY_LABELS.COLUMNS;

	/** this is the order every propertiesdialog should be structured. The null Value is the Point where values are put that could not be sorted */
	private static final String[] comparatorString = {
		PROPERTY_NAME, PROPERTY_FONT, PROPERTY_BACKGROUNDCOLOR, PROPERTY_BORDER, PROPERTY_ENABLED, PROPERTY_VISIBLE, 
		PROPERTY_OPAQUE, PROPERTY_PREFFEREDSIZE, PROPERTY_SIZE, PROPERTY_MNEMONIC, PROPERTY_DESCRIPTION, PROPERTY_VALUELISTPROVIDER, 
		PROPERTY_COLLECTABLECOMPONENTPROPERTY, null, 
		CONSTRAINT_HALIGN, CONSTRAINT_VALIGN, CONSTRAINT_COL1, CONSTRAINT_COL2, CONSTRAINT_ROW1, CONSTRAINT_ROW2};

	/**
	 * Static Method which does the sorting
	 * @param incoming unsorted PropertyNames
	 * @return sorted Propertynames
	 * 
	 * @see WYSIWYGComponent#getPropertyNames()
	 */
	public static String[] sortPropertyNames(String[] incoming) {
		Map<Integer, String> sortedMap = new HashMap<Integer, String>();
		String[] notAssigned = new String[incoming.length];
		String[] sorted = new String[incoming.length];
		ArrayList<String> sortedProperties = new ArrayList<String>(incoming.length);

		for (int i = 0; i < incoming.length; i++) {
			notAssigned[i] = new String(incoming[i]);
			sorted[i] = "";
		}

		for (int i = 0; i < notAssigned.length; i++) {
			for (int j = 0; j < comparatorString.length; j++) {
				if (notAssigned[i].equals(comparatorString[j])) {
					sortedMap.put(j, comparatorString[j]);
					notAssigned[i] = "";
				}
			}
		}
		Arrays.sort(notAssigned);

		int maxvalue = Integer.MIN_VALUE;
		for (Integer value : sortedMap.keySet()) {
			if (value.intValue() > maxvalue)
				maxvalue = value.intValue();
		}

		for (int i = 0; i < sorted.length; i++) {
			String value = sortedMap.get(i);
			int k = i;
			while (value == null) {
				k = k + 1;
				value = sortedMap.get(k);
				sortedMap.remove(k);
				if (k > maxvalue)
					break;
			}
			if (value != null)
				sortedProperties.add(value);
		}

		String nullvalue = "";

		for (int i = 0; i < comparatorString.length; i++) {
			if (comparatorString[i] == null) {
				if (comparatorString.length > (i + 1))
					nullvalue = comparatorString[i + 1];
				else
					nullvalue = comparatorString[i - 1];
				break;
			}
		}

		int pos = sortedProperties.indexOf(nullvalue);
		if (pos == -1)
			pos = sortedProperties.size();
		
		for (int i = 0; i < notAssigned.length; i++) {
			if (!notAssigned[i].equals("")) {
				sortedProperties.add(pos, notAssigned[i]);
			}
		}
		int i = 0;
		for (String string : sortedProperties) {
			sorted[i] = string;
			i++;
		}

		return (sorted);
	}
}
