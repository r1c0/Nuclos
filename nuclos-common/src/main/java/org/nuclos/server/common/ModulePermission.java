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
package org.nuclos.server.common;

import org.nuclos.common2.KeyEnum;
import org.nuclos.common2.Localizable;

/**
 * Module permission.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:christoph.radig@novabit.de">christoph.radig</a>
 * @version 01.00.00
 */
public enum ModulePermission implements KeyEnum<Integer>, Localizable {
	
	/**
	 * Added in order to enable ConcurrentHashMaps for caching. Normally, having no rights
	 * is represented by the <code>null</code> value. (tp)
	 */
	NO(0x00, "no"),

	READONLY(0x01, "read"),

	READWRITE(0x03, "write"),

	DELETE_LOGICALLY(0x07, "delete"),

	DELETE_PHYSICALLY(0x0F, "delete.physical");

	private final Integer value;
	private final String name;
	
	private ModulePermission(int value, String name) {
		this.value = value;
		this.name = name;
	}
	
	@Override
	public Integer getValue() {
		return value;
	}
	
	@Override
	public String getResourceId() {
		return "module.permission." + name;
	}
	
	public ModulePermission max(ModulePermission other) {
		return (other == null || ordinal() >= other.ordinal()) ? this : other;
	}	
	
	/**
	 * @return Does this permission include the right to read?
	 */
	public static boolean includesReading(ModulePermission permission) {
		return permission != null && permission != NO;
	}

	/**
	 * @return Does this permission include the right to write?
	 */
	public static boolean includesWriting(ModulePermission permission) {
		return includesReading(permission) && permission != READONLY;
	}

	/**
	 * @return Does this permission include the right to write?
	 */
	public static boolean includesDeletingLogically(ModulePermission permission) {
		return permission == DELETE_LOGICALLY || permission == DELETE_PHYSICALLY;
	}

	/**
	 * @return Does this permission include the right to write?
	 */
	public static boolean includesDeletingPhysically(ModulePermission permission) {
		return permission == DELETE_PHYSICALLY;
	}

	public static ModulePermission getInstance(Integer iValue) {
		if (iValue == null) {
			return null;
		}
		switch (iValue) {
			case 0x00:
				return NO;
			case 0x01:
				return READONLY;
			case 0x03:
				return READWRITE;
			case 0x07:
				return DELETE_LOGICALLY;
			case 0x0F:
				return DELETE_PHYSICALLY;
			default:
				throw new IllegalArgumentException("Invalid permission: " + iValue);
		}
	}

}	// enum ModulePermission
