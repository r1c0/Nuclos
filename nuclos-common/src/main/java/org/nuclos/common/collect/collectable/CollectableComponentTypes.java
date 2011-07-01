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
package org.nuclos.common.collect.collectable;

/**
 * Constants for <code>CollectableComponent</code> types.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version	01.00.00
 */

public interface CollectableComponentTypes {
	/**
	 * Component type: textfield
	 */
	public static final int TYPE_TEXTFIELD = 1;
	/**
	 * Component type: combobox
	 */
	public static final int TYPE_COMBOBOX = 2;
	/**
	 * Component type: textarea
	 */
	public static final int TYPE_TEXTAREA = 3;
	/**
	 * Component type: checkbox
	 */
	public static final int TYPE_CHECKBOX = 4;
	/**
	 * Component type: datechooser
	 */
	public static final int TYPE_DATECHOOSER = 5;
	/**
	 * Component type: radiogroup
	 */
	public static final int TYPE_OPTIONGROUP = 6;
	/**
	 * Component type: list of values (lov)
	 */
	public static final int TYPE_LISTOFVALUES = 7;
	/**
	 * Component type: textfield
	 */
	public static final int TYPE_IDTEXTFIELD = 8;
	/**
	 * Component type: date chooser
	 */
	public static final int TYPE_FILECHOOSER = 9;
	/**
	 * Component type: image
	 */
	public static final int TYPE_IMAGE = 10;
	
	/**
	 * Component type: password
	 * NUCLEUSINT-1142
	 */
	public static final int TYPE_PASSWORDFIELD = 11;
}  // class CollectableComponentTypes
