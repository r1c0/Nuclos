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
package org.nuclos.server.report;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.nuclos.common.NuclosFile;
import org.nuclos.server.report.valueobject.ReportOutputVO;
import org.nuclos.server.report.valueobject.ResultVO;

public interface Export {

	public NuclosFile test(ReportOutputVO output) throws NuclosReportException;
	
	public NuclosFile export(ReportOutputVO output, Map<String, Object> params, Locale locale, int maxrows) throws NuclosReportException;
	
	public NuclosFile export(ResultVO result, List<ReportFieldDefinition> fields) throws NuclosReportException;
}
