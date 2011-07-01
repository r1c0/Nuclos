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

import org.nuclos.common2.exception.*;
import org.nuclos.client.datasource.DatasourceDelegate;
import org.nuclos.client.explorer.node.datasource.DatasourceNode.DatasourceUsage;
import org.nuclos.client.report.ReportDelegate;
import org.nuclos.server.report.valueobject.DatasourceVO;
import org.nuclos.server.report.valueobject.ReportVO;
import java.util.*;

/**
 * Usage directory node below an datasource parent node
 * @author <a href="mailto:rainer.schneider@novabit.de">rainer.schneider</a>
 */
public class DatasourceUsageNode extends DirectoryDatasourceNode {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final DatasourceNode.DatasourceUsage usage;
	private final DatasourceVO parentDatasource;

	public DatasourceUsageNode(DatasourceVO aParentDatasourceVo, String aLabel,
			String aDescription, DatasourceNode.DatasourceUsage aUsage) {

		super(false, aLabel, aDescription, null);

		assert(aUsage != DatasourceNode.DatasourceUsage.PARENT);
		this.usage = aUsage;
		this.parentDatasource = aParentDatasourceVo;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void refresh() {
		try {
			List<AbstractDatasourceTreeNode> subNodeList = new ArrayList<AbstractDatasourceTreeNode>();
			if (usage == DatasourceUsage.USED) {
				final List<DatasourceVO> lstDatasources = DatasourceDelegate.getInstance().getUsagesForDatasource(parentDatasource.getId());
				for (DatasourceVO curSource : lstDatasources) {
					subNodeList.add(new DatasourceNode(curSource, DatasourceUsage.USING));
				}
			}
			else if (usage == DatasourceUsage.USING) {
				final List<DatasourceVO> lstDatasources = DatasourceDelegate.getInstance().getUsingByForDatasource(parentDatasource.getId());
				for (DatasourceVO curSource : lstDatasources) {
					subNodeList.add(new DatasourceNode(curSource, DatasourceUsage.USED));
				}
			}
			else if (usage == DatasourceUsage.REPORT) {
				final Collection<ReportVO> lstDatasources = ReportDelegate.getInstance().getReportsForDatasourceId(parentDatasource.getId());
				for (ReportVO reportVO : lstDatasources) {
					subNodeList.add(new DatasourceReportFormularNode(reportVO, DatasourceUsage.REPORT));
				}
			}
			else if (usage == DatasourceUsage.FORMULAR) {
				final Collection<ReportVO> lstDatasources = ReportDelegate.getInstance().getFormularsForDatasourceId(parentDatasource.getId());
				for (ReportVO reportVO : lstDatasources) {
					subNodeList.add(new DatasourceReportFormularNode(reportVO, DatasourceUsage.FORMULAR));
				}
			}

			sortNodeListByLabel(subNodeList);
			setSubNodes(subNodeList);
		}
		catch (CommonFinderException ex) {
			throw new CommonRemoteException(ex);
		}
		catch (CommonPermissionException ex) {
			throw new CommonRemoteException(ex);
		}
	}

	public DatasourceNode.DatasourceUsage getUsage() {
		return usage;
	}

}	// class DatasourceUsageNode
