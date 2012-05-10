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
package org.nuclos.client.report.reportrunner.source;

import java.util.Map;

import org.nuclos.client.report.ReportDelegate;
import org.nuclos.client.report.reportrunner.ReportSource;
import org.nuclos.common.NuclosFile;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.server.report.NuclosReportException;
import org.nuclos.server.report.valueobject.ReportOutputVO;

public class DefaultReportSource implements ReportSource {

	private final ReportOutputVO output;
	private final Map<String, Object> params;
	private final Integer maxrows;
	
	public DefaultReportSource(ReportOutputVO output, Map<String, Object> params, Integer maxrows) {
		super();
		this.output = output;
		this.params = params;
		this.maxrows = maxrows;
	}

	@Override
	public NuclosFile getReport() throws NuclosReportException {
		try {
			return ReportDelegate.getInstance().prepareReport(output.getId(), params, maxrows);
		}
		catch (CommonBusinessException e) {
			throw new NuclosReportException(e);
		}
	}

}
