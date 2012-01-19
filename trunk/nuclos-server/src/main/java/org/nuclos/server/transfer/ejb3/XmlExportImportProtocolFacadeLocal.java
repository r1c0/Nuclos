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
package org.nuclos.server.transfer.ejb3;

import java.util.Date;

import org.nuclos.common2.exception.CommonCreateException;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.common2.exception.CommonPermissionException;
import org.nuclos.common2.exception.CommonRemoveException;
import org.nuclos.common2.exception.CommonStaleVersionException;
import org.nuclos.common2.exception.CommonValidationException;
import org.nuclos.server.ruleengine.NuclosBusinessRuleException;

// @Local
public interface XmlExportImportProtocolFacadeLocal {

	/**
	 * creates the protocol header for an export/import transaction
	 *
	 * @param sType
	 * @param sUserName
	 * @param dImportDate
	 * @param sImportEntityName
	 * @param sImportFileName
	 * @return id of protocol header
	 * @throws CommonCreateException
	 * @throws CommonPermissionException
	 * @throws CreateException
	 * @throws NuclosBusinessRuleException
	 */
	Integer writeExportImportLogHeader(String sType,
		String sUserName, Date dImportDate, String sImportEntityName,
		String sImportFileName) throws CommonCreateException,
		CommonPermissionException, NuclosBusinessRuleException;

	/**
	 * creates a protocol entry for the given protocol header id
	 *
	 * @param iParentId
	 * @param sMessageLevel
	 * @param sImportAction
	 * @param sImportMessage
	 * @throws CommonCreateException
	 * @throws CommonPermissionException
	 * @throws CreateException
	 * @throws NuclosBusinessRuleException
	 */
	void writeExportImportLogEntry(Integer iParentId,
		String sMessageLevel, String sImportAction, String sImportMessage,
		Integer iActionNumber) throws CommonCreateException,
		CommonPermissionException, NuclosBusinessRuleException;

	/**
	 * add content of xml file to protocol header of given id
	 *
	 * @param iParentId
	 * @param file
	 * @throws CommonFinderException
	 * @throws CommonPermissionException
	 * @throws CreateException
	 * @throws CommonCreateException
	 * @throws CommonRemoveException
	 * @throws CommonStaleVersionException
	 * @throws CommonValidationException
	 */
	void addFile(Integer iParentId, byte[] ba);

}
