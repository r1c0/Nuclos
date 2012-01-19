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

import org.nuclos.server.report.NuclosReportException;
import org.nuclos.server.report.valueobject.ReportOutputVO;
import org.nuclos.server.report.valueobject.ReportVO;
import org.nuclos.server.report.valueobject.ResultVO;

/**
 * Exports (or "runs") a report. Must be implemented for all ReportFormats.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:uwe.allner@novabit.de">Uwe Allner</a>
 * @version 02.00.00
 */
public interface ReportExporter {

	/**
	 * Exports a report in any different format than Jasper.
	 * @param resultvo
	 * @param reportvo
	 * @param outputvo
	 * @throws NuclosReportException
	 */
	public void export(ResultVO resultvo, ReportVO reportvo, ReportOutputVO outputvo) throws NuclosReportException;

	/**
	 * Set the identifier for calling leased object for the form to report
	 * @param sGenericObjectIdentifier
	 */
	public void setGenericObjectIdentifier(String sGenericObjectIdentifier);
	
	public void setReportFileName(String filename);

}	// interface ReportExporter
