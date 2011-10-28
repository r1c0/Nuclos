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
// Copyright (C) 2010 Novabit Informationssysteme GmbH
//
// This file is part of Nuclos.
//
// Nuclos is free software: you can redistribute it and/or modify
// it under the terms of the GNU Affero General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// Nuclos is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU Affero General Public License for more details.
//
// You should have received a copy of the GNU Affero General Public License
// along with Nuclos. If not, see <http://www.gnu.org/licenses/>.
package org.nuclos.server.transfer.ejb3;

import java.io.IOException;
import java.util.Map;

import org.nuclos.common2.File;
import org.nuclos.common2.exception.CommonCreateException;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.common2.exception.CommonPermissionException;
import org.nuclos.server.ruleengine.NuclosBusinessRuleException;

// @Local
public interface XmlExportFacadeLocal {

	/**
	 * 
	 * @param exportEntities map of ids and entitynames to export
	 * @return Zip File with Xml output and Document files
	 * @throws IOException
	 * @throws CommonPermissionException
	 * @throws CommonFinderException
	 * @throws CreateException
	 * @throws CommonCreateException
	 * @throws NuclosBusinessRuleException
	 * @throws Exception
	 * @jboss.method-attributes read-only = "true"
	 */
	File xmlExport(
		Map<Integer, String> exportEntities, boolean deepexport, String sFileName)
		throws CommonFinderException, CommonPermissionException, IOException,
		CommonCreateException, NuclosBusinessRuleException;

	File xmlExport(
		Map<Integer, String> exportEntities, boolean deepexport,
		boolean withDependants, String sFileName) throws CommonFinderException,
		CommonPermissionException, IOException, CommonCreateException,
		NuclosBusinessRuleException;

	/**
	 * get the count of the processed exported entities
	 */
	Integer getProcessedEntities();

}
