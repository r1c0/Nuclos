//Copyright (C) 2011  Novabit Informationssysteme GmbH
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
package org.nuclos.client.ui.collect.search;

import java.util.Arrays;
import java.util.Collection;

import org.nuclos.client.masterdata.MetaDataCache;
import org.nuclos.client.report.ReportDelegate;
import org.nuclos.common.CollectableEntityFieldWithEntity;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common.collect.collectable.CollectableValueField;
import org.nuclos.common.collect.collectable.searchcondition.CollectableComparison;
import org.nuclos.common.collect.collectable.searchcondition.CollectableSearchCondition;
import org.nuclos.common.collect.collectable.searchcondition.ComparisonOperator;
import org.nuclos.common.collect.collectable.searchcondition.CompositeCollectableSearchCondition;
import org.nuclos.common.collect.collectable.searchcondition.LogicalOperator;
import org.nuclos.common.collect.exception.CollectableFieldFormatException;
import org.nuclos.common.masterdata.CollectableMasterDataEntity;
import org.nuclos.server.report.valueobject.ReportVO.ReportType;

public class ReportSearchStrategy extends MasterDataSearchStrategy {

	private final NuclosEntity entity;
	private final CollectableEntityField clctefReportType;
	private final ReportDelegate reportdelegate = new ReportDelegate();

	public ReportSearchStrategy(NuclosEntity entity) {
		this.entity = entity;

		this.clctefReportType = new CollectableEntityFieldWithEntity(
				new CollectableMasterDataEntity(MetaDataCache.getInstance().getMetaData(entity)), "type");
	}

	@Override
	public CollectableSearchCondition getCollectableSearchCondition() throws CollectableFieldFormatException {
		CollectableSearchCondition searchCondition = reportdelegate.getCollectableSearchCondition(
				getMasterCollectDataController().getCollectableEntity(), super.getCollectableSearchCondition());
		
		ReportType reportType = entity.equals(NuclosEntity.REPORT) ? ReportType.REPORT : ReportType.FORM;
		
		CollectableComparison clctCompReportType
			= new CollectableComparison(clctefReportType, ComparisonOperator.EQUAL, new CollectableValueField(reportType.getValue()));
		
		if (searchCondition != null) {
			final Collection<CollectableSearchCondition> collOperands = Arrays
					.asList(new CollectableSearchCondition[] { searchCondition, clctCompReportType });
			searchCondition = new CompositeCollectableSearchCondition(LogicalOperator.AND, collOperands);
		} else {
			searchCondition = clctCompReportType;
		}
		return searchCondition;
	}
}
