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
package org.nuclos.common;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.nuclos.common.collection.multimap.MultiListHashMap;
import org.nuclos.common2.SpringLocaleDelegate;
import org.nuclos.common2.StringUtils;

public class PointerCollection implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final Pointer mainPointer;
	private final MultiListHashMap<String, Pointer> fieldPointers = new MultiListHashMap<String, Pointer>();
	
	/**
	 * 
	 * @param message
	 * 		the pointer message
	 */
	public PointerCollection(String message) {
		this.mainPointer = new Pointer(message);
	}
	
	/**
	 * 
	 * @param message
	 * 		the pointer message
	 * @param localizeParameter
	 * 		parameter for replacement in localization string
	 */
	public PointerCollection(String message, Object... localizeParameter) {
		this.mainPointer = new Pointer(message, localizeParameter);
	}
	
	/**
	 * 
	 * @return
	 */
	public String getLocalizedMainPointer() {
		return localizePointer(mainPointer);
	}
	
	/**
	 * 
	 * @return
	 */
	public Pointer getMainPointer() {
		return mainPointer;
	}
	
	/**
	 * 
	 * @param message
	 * 		the pointer message
	 */
	public void setMainPointer(String message) {
		this.mainPointer.message = message;
	}
	
	/**
	 * 
	 * @param message
	 * 		the pointer message
	 * @param localizeParameter
	 * 		parameter for replacement in localization string
	 */
	public void setMainPointer(String message, Object... localizeParameter) {
		this.mainPointer.message = message; 
		this.mainPointer.localizeParameter = localizeParameter;
	}
	
	/**
	 * 
	 * @param field
	 * 		name of the field (not label!)
	 */
	public void addEmptyFieldPointer(String field) {
		fieldPointers.addValue(field, new Pointer(null));
	}
	
	/**
	 * 
	 * @param field 
	 * 		name of the field (not label!)
	 * @param message
	 * 		the pointer message
	 */
	public void addFieldPointer(String field, String message) {
		this.addFieldPointer(field, new Pointer(message));
	}
	
	/**
	 * 
	 * @param field
	 * 		name of the field (not label!)
	 * @param message
	 * 		this pointer message
	 * @param localizeParameter
	 * 		parameter for replacement in localization string
	 */
	public void addFieldPointer(String field, String message, Object... localizeParameter) {
		this.addFieldPointer(field, new Pointer(message, localizeParameter));
	}
	
	private void addFieldPointer(String field, Pointer pointer) {
		fieldPointers.addValue(field, pointer);
	}
	
	/**
	 * 
	 * @param field
	 * 		name of the field (not label!)
	 * @return
	 */
	public boolean hasFieldPointers(String field) {
		boolean result = false;
		for (Pointer pointer : getFieldPointers(field)) {
			if (pointer != null && !StringUtils.looksEmpty(pointer.message)) {
				result = true;
			}
		}
		return result;
	}
	
	/**
	 * 
	 * @param field
	 * 		name of the field (not label!)
	 * @return
	 */
	public List<String> getLocalizedFieldPointers(String field) {
		List<String> result = new ArrayList<String>();
		for (Pointer p : getFieldPointers(field)) {
			result.add(localizePointer(p));
		}
		return result;
	}
	
	/**
	 * 
	 * @param field
	 * 		name of the field (not label!)
	 * @return
	 */
	public List<Pointer> getFieldPointers(String field) {
		return fieldPointers.getValues(field);
	}
	
	public Set<String> getFields() {
		return fieldPointers.asMap().keySet();
	}
	
	public Map<String, List<Pointer>> getAllFieldPointers() {
		return fieldPointers.asMap();
	}
	
	public static String localizePointer(Pointer p) {
		if (p == null)
			return null;
		if (p.message == null)
			return null;
		
		return SpringLocaleDelegate.getInstance().getMessage(p.message, p.message, p.localizeParameter);
	}
	
	public static class Pointer implements Serializable{

		public String message;
		public Object[] localizeParameter;
		
		public Pointer(String message) {
			this.message = message;
		}
		
		public Pointer(String message, Object... localizeParameter) {
			this.message = message;
			this.localizeParameter = localizeParameter;
		}
	}
	
}
