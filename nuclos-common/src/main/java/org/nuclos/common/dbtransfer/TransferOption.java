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
package org.nuclos.common.dbtransfer;

import java.io.Serializable;

/**
 * Keys for the option maps used by the <code>TransferFacade</code>.
 * The keys that represent simple flags will be added with
 * <code>null</code> values to the maps.
 */
public enum TransferOption {

	INCLUDES_USER,
	INCLUDES_LDAP,
	INCLUDES_IMPORTFILE,
	IMPORT_USER,
	IMPORT_LDAP,
	IMPORT_IMPORTFILE,
	FREEZE_CONFIGURATION,
	DBADMIN,								// string
	DBADMIN_PASSWORD,					// string
	IS_NUCLON_IMPORT_ALLOWED,
	IS_NUCLOS_INSTANCE	
	;

	public static Map copyOptionMap(Map src) {
		return src.isEmpty() ? new HashMap() : new HashMap(src);
	}
	
	public static interface Map extends java.util.Map<TransferOption, Serializable> {}
	
	public static class HashMap extends java.util.HashMap<TransferOption, Serializable> implements TransferOption.Map {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		public HashMap() {
			super();
		}
		public HashMap(Map src) {
			super(src);
		}}
}
