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

import org.nuclos.common2.SpringLocaleDelegate;
import org.nuclos.server.report.valueobject.DatasourceVO;
import java.util.ArrayList;
import java.util.List;

/**
 * Treenode representing an datasource in the tree node
 * @author <a href="mailto:rainer.schneider@novabit.de">rainer.schneider</a>
 */
public class DatasourceNode extends AbstractDatasourceTreeNode {

	/**
	 * Usage of an Datasource node or an datasource directory node
	 */
	public enum DatasourceUsage {
		USING, // datasource or directory below an parent node used by the parent
		USED, // datasource or directory below an parent using the parent
		PARENT, // parent datasource node with reports and other datasources as child
		REPORT,	// report or report directory below an parent node
		FORMULAR	// formular or formular directory below an parent node
	}

	private final DatasourceVO datasourceVo;

	/** kind of usage of the node */
	private final DatasourceUsage usage;

	/** parent id, if the node is a child of an user defined datasource group node */
	private final Integer parentGroupId;

	public DatasourceNode(DatasourceVO aSourceVo, DatasourceUsage aUsage) {
		super(aSourceVo.getId(), aSourceVo.getName(), aSourceVo.getDescription(), null);
		assert(aUsage != DatasourceUsage.REPORT);
		assert(aSourceVo != null);
		this.datasourceVo = aSourceVo;
		this.usage = aUsage;
		this.parentGroupId = null;
	}

	public DatasourceNode(DatasourceVO aSourceVo, Integer aParentGroupId) {
		super(aSourceVo.getId(), aSourceVo.getName(), aSourceVo.getDescription(), null);
		assert(aSourceVo != null);
		this.datasourceVo = aSourceVo;
		this.usage = DatasourceUsage.PARENT;
		this.parentGroupId = aParentGroupId;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void refresh() {
		final List<DatasourceUsageNode> subNodeList = new ArrayList<DatasourceUsageNode>();
		if (usage == DatasourceUsage.PARENT) {
			final SpringLocaleDelegate localeDelegate = SpringLocaleDelegate.getInstance();
			subNodeList.add(new DatasourceUsageNode(datasourceVo, localeDelegate.getMessage("DatasourceNode.1", "verwendet"), 
					localeDelegate.getMessage("DatasourceNode.2", "Alle Datenquellen, die von der Datenquelle verwendet werden"), DatasourceUsage.USING));
			subNodeList.add(new DatasourceUsageNode(datasourceVo, localeDelegate.getMessage("DatasourceNode.3", "wird verwendet von "), 
					localeDelegate.getMessage("DatasourceNode.4", "Alle Datenquellen, von denen die Datenquelle verwendet wird"), DatasourceUsage.USED));
			subNodeList.add(new DatasourceUsageNode(datasourceVo, localeDelegate.getMessage("DatasourceNode.5", "Reports"), 
					localeDelegate.getMessage("DatasourceNode.6", "Alle Reports, welche die Datenquelle verwenden"), DatasourceUsage.REPORT));
			subNodeList.add(new DatasourceUsageNode(datasourceVo, localeDelegate.getMessage("DatasourceNode.7", "Formulare"), 
					localeDelegate.getMessage("DatasourceNode.8", "Alle Formulare, welche die Datenquelle verwenden"), DatasourceUsage.FORMULAR));
		}
		setSubNodes(subNodeList);
	}

	public DatasourceVO getDatasourceVo() {
		return datasourceVo;
	}

	public DatasourceUsage getUsage() {
		return usage;
	}

	public Integer getParentGroupId() {
		return parentGroupId;
	}

}	// class DatasourceNode
