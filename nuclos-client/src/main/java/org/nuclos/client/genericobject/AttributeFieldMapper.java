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
package org.nuclos.client.genericobject;

import java.util.List;
import java.util.Map;

import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.Pair;

/**
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:florian.speidel@novabit.de">Florian.Speidel</a>
 * @version 01.00.00
 */

public class AttributeFieldMapper {
//	Entity field 	- 	Attribut
//	--------------------------------------------------
//	name			-	[basisschluessel]
//	ordenumber		-	auftrags_nr_e_plus
//	ordernumberSupplier - 	auftrags_nr_lieferant
//	lszSupplier		- 	objektbez_lief_lsz
//	ordinalSupplier	- 	objektbez_lief_ordn_nr
//	additionalIdSupplier - 	objektbez_lief_zusbez
//	terminalASupplier	- 	objektbez_lief_endstelle_a
//	terminalBSupplier	- 	objektbez_lief_endstelle_b
//	vpszBszASupplier	- 	objektbez_lief_vpsz_bsz_a
//	vpszBszBSupplier	- 	objektbez_lief_vpsz_bsz_b
//	objectidSupplier	- 	objektbez_lief
//	uewTypeSupplier	- 	objektbez_lief_uew_typ

	private static Map<String, List<Pair<String, String>>> attributeFieldMapping = CollectionUtils.newHashMap();

	/**
	 *
	 *
	 * @param attributeName the atributename to be mapped on entityfields
	 * @return null if no mapping avaiable - a String[] of fieldnames otherwise
	 */
	public static List<Pair<String, String>> getFieldsForAttribute(String attributeName) {
		return attributeFieldMapping.get(attributeName);
	}
}
