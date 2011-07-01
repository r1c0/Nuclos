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

import org.nuclos.common2.StringUtils;
import org.nuclos.server.ruleengine.NuclosBusinessRuleException;



public class PointerException extends NuclosBusinessRuleException {

	private final PointerCollection pointerCollection;
	
	/**
	 * 
	 */
	public PointerException() {
		this(new PointerCollection(null));
	}

	/**
	 * 
	 * @param mainPointerMessage
	 * 		the pointer message
	 */
	public PointerException(String mainPointerMessage) {
		this(new PointerCollection(mainPointerMessage));
	}
	
	/**
	 * 
	 * @param mainPointerMessage
	 * 		the pointer message
	 * @param localizeParameter
	 * 		parameter for replacement in localization string
	 */
	public PointerException(String mainPointerMessage, Object... localizeParameter) {
		this(new PointerCollection(mainPointerMessage, localizeParameter));
	}
	
	/**
	 * 
	 * @param pointerCollection
	 */
	public PointerException(PointerCollection pointerCollection) {
		super(pointerCollection==null?null:
			StringUtils.getParameterizedExceptionMessage(pointerCollection.getMainPointer().message, pointerCollection.getMainPointer().localizeParameter));
		if (pointerCollection == null) {
			throw new IllegalArgumentException("pointerCollection must not be null");
		}
		this.pointerCollection = pointerCollection;
	}

	/**
	 * @return the pointerCollection
	 */
	public PointerCollection getPointerCollection() {
		return pointerCollection;
	}
	
	/**
	 * 
	 * @param ex
	 * @return
	 */
	public static PointerException extractPointerExceptionIfAny(Exception ex) {
		if (ex instanceof PointerException) {
			return (PointerException) ex;
		} else if (ex.getCause() != null && ex.getCause() instanceof Exception) {
			return extractPointerExceptionIfAny((Exception)ex.getCause());
		} else {
			return null;
		}
	}
}
