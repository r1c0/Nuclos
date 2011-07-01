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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.nuclos.common2.CommonLocaleDelegate;
import org.nuclos.common2.exception.CommonPermissionException;
import org.nuclos.client.datasource.DatasourceDelegate;
import org.nuclos.client.explorer.node.datasource.DatasourceNode.DatasourceUsage;
import org.nuclos.client.ui.Errors;
import org.nuclos.server.report.valueobject.DatasourceVO;

/**
 * Treenode representing an special group directory wth all Datasources
 * Could not be deleted by the user
 * @author <a href="mailto:rainer.schneider@novabit.de">rainer.schneider</a>
 *
 */
public class AllDatasourceNode extends DirectoryDatasourceNode {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public AllDatasourceNode() {
		super(false, CommonLocaleDelegate.getMessage("AllDatasourceNode.1", "Alle Datenquellen"), CommonLocaleDelegate.getMessage("AllDatasourceNode.1", "Alle Datenquellen"), null);
	}

	@Override
	public void refresh() {
		Collection<DatasourceVO> allDatasources;
		try {
			allDatasources = DatasourceDelegate.getInstance().getAllDatasources();
			List<DatasourceNode> subNodeList = new ArrayList<DatasourceNode>(allDatasources.size());
	
			for (DatasourceVO curSource : allDatasources) {
				subNodeList.add(new DatasourceNode(curSource, DatasourceUsage.PARENT));
			}
			sortNodeListByLabel(subNodeList);
			setSubNodes(subNodeList);
		} catch (CommonPermissionException e) {
		  Errors.getInstance().showExceptionDialog(null, e);
		}
	}

	@Override
	public boolean equals(Object obj) {
		return obj != null && obj instanceof AllDatasourceNode
				&& getLabel().equals(((AllDatasourceNode) obj).getLabel());
	}

	@Override
	public int hashCode() {
		return getLabel().hashCode();
	}

}	// class AllDatasourceNode
