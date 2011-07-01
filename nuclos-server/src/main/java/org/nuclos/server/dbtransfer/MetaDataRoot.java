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
package org.nuclos.server.dbtransfer;

import java.util.Date;

import org.nuclos.common.ApplicationProperties.Version;
import org.nuclos.common.dbtransfer.TransferOption;

class MetaDataRoot {

	final Integer transferVersion;
	final String nucletUID;
	final String appName;
	final Version version;
	final String database;
	final Date exportDate;
	
	final TransferOption.Map exportOptions;

	MetaDataRoot(
		Integer transferVersion,
		String nucletUID,
		String appName,
		Version version,
		String database,
		Date exportDate,
		TransferOption.Map exportOptions)
	{
		this.transferVersion = transferVersion;
		this.nucletUID = nucletUID;
		this.appName = appName;
		this.version = version;
		this.database = database;
		this.exportDate = exportDate;
		this.exportOptions = exportOptions;
	}
}
