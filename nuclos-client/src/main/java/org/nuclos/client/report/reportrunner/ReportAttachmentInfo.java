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
package org.nuclos.client.report.reportrunner;

/**
 * Store information for attaching reports to objects.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:thomas.schiffmann@novabit.de">thomas.schiffmann</a>
 * @version 00.01.000
 */
public class ReportAttachmentInfo {

	private final Integer genericObjectId;
	private final String genericObjectIdentifier;
	private final String documentEntityName;
	private final String[] documentFieldNames;
	private final String directory;

	public ReportAttachmentInfo(Integer genericObjectId, String genericObjectIdentifier, String documentEntityName, String[] documentFieldNames, String directory) {
		super();
		this.genericObjectId = genericObjectId;
		this.genericObjectIdentifier = genericObjectIdentifier;
		this.documentEntityName = documentEntityName;
		this.documentFieldNames = documentFieldNames;
		this.directory = directory;
	}

	public Integer getGenericObjectId() {
		return genericObjectId;
	}

	public String getGenericObjectIdentifier() {
		return genericObjectIdentifier;
	}

	public String getDocumentEntityName() {
		return documentEntityName;
	}
	
	public String[] getDocumentFieldNames() {
		return documentFieldNames;
	}

	public String getDirectory() {
		return directory;
	}

}