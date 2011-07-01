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
package org.nuclos.client.explorer.node.datasource;

import org.nuclos.client.explorer.node.datasource.DatasourceNode.DatasourceUsage;
import org.nuclos.server.report.valueobject.ReportVO;
import java.util.Collections;
import java.util.List;

/**
 * Treenode representing an report or a formular
 * @author <a href="mailto:rainer.schneider@novabit.de">rainer.schneider</a>
 */
public class DatasourceReportFormularNode extends AbstractDatasourceTreeNode {

	private final ReportVO reportVo;
	private final DatasourceNode.DatasourceUsage usage;

	public DatasourceReportFormularNode(ReportVO aReportVo, DatasourceUsage aUsage) {
		super(aReportVo.getId(), aReportVo.getName(), aReportVo.getDescription(), null);
		assert(aReportVo != null);
		this.reportVo = aReportVo;
		this.usage = aUsage;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void refresh() {
		final List<DatasourceUsageNode> subNodeList = Collections.emptyList();//new ArrayList<DatasourceUsageNode>();
		setSubNodes(subNodeList);
	}

	/**
	 * get the report data the node represents
	 * @return
	 */
	public ReportVO getReportVo() {
		return reportVo;
	}

	@Override
	public Boolean hasSubNodes() {
		return false;
	}

	public DatasourceNode.DatasourceUsage getUsage() {
		return usage;
	}

}	// class DatasourceReportFormularNode
