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

import java.util.List;

import org.nuclos.client.report.ReportDelegate;
import org.nuclos.client.report.reportrunner.ReportSource;
import org.nuclos.common.NuclosFile;
import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.server.genericobject.searchcondition.CollectableSearchExpression;
import org.nuclos.server.report.NuclosReportException;
import org.nuclos.server.report.valueobject.ReportOutputVO;
import org.nuclos.server.report.valueobject.ReportOutputVO.Format;

public class SearchExpressionReportSource implements ReportSource {

	private final CollectableSearchExpression expr;
	private final List<? extends CollectableEntityField> lstclctefweSelected;
	private final Integer iModuleId;
	private final boolean bIncludeSubModules;
	private final ReportOutputVO.Format format;
	private final String customUsage;
	
	public SearchExpressionReportSource(CollectableSearchExpression expr, List<? extends CollectableEntityField> lstclctefweSelected, Integer iModuleId, boolean bIncludeSubModules, Format format, String customUsage) {
		super();
		this.expr = expr;
		this.lstclctefweSelected = lstclctefweSelected;
		this.iModuleId = iModuleId;
		this.bIncludeSubModules = bIncludeSubModules;
		this.format = format;
		this.customUsage = customUsage;
	}

	@Override
	public NuclosFile getReport() throws NuclosReportException {
		try {
			return ReportDelegate.getInstance().prepareSearchResult(expr, lstclctefweSelected, iModuleId, bIncludeSubModules, format, customUsage);
		}
		catch (CommonBusinessException e) {
			throw new NuclosReportException(e);
		}
	}
}
