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
package org.nuclos.client.explorer;

import org.apache.log4j.Logger;
import org.nuclos.client.explorer.node.eventsupport.EventSupportTreeNode;
import org.nuclos.common.ApplicationProperties;
import org.nuclos.common2.LangUtils;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.server.navigation.treenode.TreeNode;
import org.nuclos.server.navigation.treenode.nuclet.NucletTreeNode;

/**
 * Factory for creating {@link ExplorerView}s out of {@link TreeNode}s.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 */
public class ExplorerViewFactory {

	private static final Logger LOG = Logger.getLogger(ExplorerViewFactory.class);

	private static ExplorerViewFactory singleton;

	public static synchronized ExplorerViewFactory getInstance() {
		if (singleton == null) {
			singleton = newFactory();
		}
		return singleton;
	}

	private static ExplorerViewFactory newFactory() {
		try {
			final String sClassName = LangUtils.defaultIfNull(
					ApplicationProperties.getInstance().getExplorerViewFactoryClassName(),
					ExplorerViewFactory.class.getName());
			LOG.info("ExplorerViewFactory implementation: " + sClassName);
			return (ExplorerViewFactory) Class.forName(sClassName).newInstance();
		}
		catch (Exception ex) {
			throw new CommonFatalException("ExplorerViewFactory cannot be created.", ex);
		}
	}

	protected ExplorerViewFactory() {

	}

	public ExplorerView newExplorerView(TreeNode treenode) {
		final DefaultExplorerView result;
		if (treenode instanceof NucletTreeNode) {
			result = new NucletExplorerView((NucletTreeNode) treenode);
		}
		else if (treenode instanceof EventSupportTreeNode)	{
			result = new EventSupportManagementExplorerView(treenode);
		}
		else {
			result = new DefaultExplorerView(treenode);
		}
		result.init();
		return result;
	}

}	// class ExplorerNodeFactory
