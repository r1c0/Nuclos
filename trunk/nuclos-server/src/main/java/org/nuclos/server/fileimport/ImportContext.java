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
package org.nuclos.server.fileimport;

/**
 * ValueObject for launching imports with a given context.
 * This class enables a quartz job to interrupt the import execution.
 *
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 */
public class ImportContext {

	private final Integer importfileId;
	private final String correlationId;
	private final Integer localeId;
	private final String username;

	private boolean interrupted;

	public ImportContext(Integer importfileId, String correlationId, Integer localeId, String username) {
		this.importfileId = importfileId;
		this.correlationId = correlationId;
		this.localeId = localeId;
		this.username = username;
	}

	public boolean isInterrupted() {
		return interrupted;
	}

	public void interrupt() {
		this.interrupted = true;
	}

	public Integer getImportfileId() {
		return importfileId;
	}

	public String getCorrelationId() {
		return correlationId;
	}

	public Integer getLocaleId() {
		return localeId;
	}

	public String getUsername() {
		return username;
	}
}
