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
package org.nuclos.common.genericobject;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.nuclos.common.ParameterProvider;

public class GenericObjectImportUtils {
	public static final int STATE_STORAGE_IMPORT = 0;
	public static final int STATE_STORAGE_ARCHIV = 1;
	public static final int STATE_STORAGE_MISSED = -1;

	public static final int OVERWRITE_UPDATE_REPLENISH = 0;
	public static final int OVERWRITE_UPDATE_OVERWRITE = 1;

	public static final int DOUBLET_UPDATE_REPLENISH = 0;
	public static final int DOUBLET_UPDATE_OVERWRITE = 1;

	private GenericObjectImportUtils() {
	}

	public static Set<String> getForbiddenAttributeNames(ParameterProvider paramProvider, boolean isUpdate) {
		final Set<String> stExcludedAttributes = new HashSet<String>();
		String sForbiddenAttributes = "";
		if(isUpdate) {
			// Update existing object
			sForbiddenAttributes = paramProvider.getValue(ParameterProvider.KEY_GENERICOBJECT_IMPORT_EXCLUDED_ATTRIBUTES_FOR_UPDATE);
		}
		else {
			// Create new object
			sForbiddenAttributes = paramProvider.getValue(ParameterProvider.KEY_GENERICOBJECT_IMPORT_EXCLUDED_ATTRIBUTES_FOR_CREATE);
		}

		stExcludedAttributes.clear();
		if(sForbiddenAttributes != null) {
			stExcludedAttributes.addAll(Arrays.asList(sForbiddenAttributes.split(",")));
		}

		return stExcludedAttributes;
	}

	public static Set<String> getForbiddenAttributeNames(ParameterProvider paramProvider, int iImportTypeId) {
		return getForbiddenAttributeNames(paramProvider, iImportTypeId == 1);
	}
}
