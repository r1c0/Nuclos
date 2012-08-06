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

import java.lang.reflect.Constructor;
import java.util.Map;

import org.apache.log4j.Logger;
import org.nuclos.client.explorer.node.CodeExplorerNode;
import org.nuclos.client.explorer.node.DatasourceDirectoryExplorerNode;
import org.nuclos.client.explorer.node.DatasourceExplorerNode;
import org.nuclos.client.explorer.node.DatasourceReportExplorerNode;
import org.nuclos.client.explorer.node.EventSupportExplorerNode;
import org.nuclos.client.explorer.node.EventSupportTargetExplorerNode;
import org.nuclos.client.explorer.node.GenericObjectExplorerNode;
import org.nuclos.client.explorer.node.GenericObjectSearchResultExplorerNode;
import org.nuclos.client.explorer.node.GroupExplorerNode;
import org.nuclos.client.explorer.node.LibraryExplorerNode;
import org.nuclos.client.explorer.node.MasterDataExplorerNode;
import org.nuclos.client.explorer.node.NucletContentCustomComponentExplorerNode;
import org.nuclos.client.explorer.node.NucletContentEntityExplorerNode;
import org.nuclos.client.explorer.node.NucletContentEntryExplorerNode;
import org.nuclos.client.explorer.node.NucletContentExplorerNode;
import org.nuclos.client.explorer.node.NucletExplorerNode;
import org.nuclos.client.explorer.node.PersonalSearchFiltersByEntityExplorerNode;
import org.nuclos.client.explorer.node.RuleEntityExplorerNode;
import org.nuclos.client.explorer.node.RuleExplorerNode;
import org.nuclos.client.explorer.node.RuleObjectGenerationExplorerNode;
import org.nuclos.client.explorer.node.RuleStateModelExplorerNode;
import org.nuclos.client.explorer.node.SearchFilterExplorerNode;
import org.nuclos.client.explorer.node.SubFormEntryExplorerNode;
import org.nuclos.client.explorer.node.SubFormExplorerNode;
import org.nuclos.client.explorer.node.datasource.AllDatasourceNode;
import org.nuclos.client.explorer.node.datasource.DatasourceNode;
import org.nuclos.client.explorer.node.datasource.DatasourceReportFormularNode;
import org.nuclos.client.explorer.node.datasource.DatasourceUsageNode;
import org.nuclos.client.explorer.node.datasource.DirectoryDatasourceNode;
import org.nuclos.client.explorer.node.datasource.OwnDatasourceNode;
import org.nuclos.client.explorer.node.eventsupport.EventSupportTargetTreeNode;
import org.nuclos.client.explorer.node.eventsupport.EventSupportTreeNode;
import org.nuclos.client.explorer.node.rule.CodeTreeNode;
import org.nuclos.client.explorer.node.rule.DirectoryRuleNode;
import org.nuclos.client.explorer.node.rule.EntityRuleNode;
import org.nuclos.client.explorer.node.rule.LibraryTreeNode;
import org.nuclos.client.explorer.node.rule.RuleGenerationNode;
import org.nuclos.client.explorer.node.rule.RuleNode;
import org.nuclos.client.explorer.node.rule.StateModelNode;
import org.nuclos.client.explorer.node.rule.TimelimitNode;
import org.nuclos.client.searchfilter.SearchFilterTreeNode;
import org.nuclos.common.ApplicationProperties;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common2.LangUtils;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.server.navigation.treenode.DefaultMasterDataTreeNode;
import org.nuclos.server.navigation.treenode.EntitySearchResultTreeNode;
import org.nuclos.server.navigation.treenode.GenericObjectTreeNode;
import org.nuclos.server.navigation.treenode.GroupTreeNode;
import org.nuclos.server.navigation.treenode.RelationTreeNode;
import org.nuclos.server.navigation.treenode.SubFormEntryTreeNode;
import org.nuclos.server.navigation.treenode.SubFormTreeNode;
import org.nuclos.server.navigation.treenode.TreeNode;
import org.nuclos.server.navigation.treenode.nuclet.NucletTreeNode;
import org.nuclos.server.navigation.treenode.nuclet.content.DefaultNucletContentEntryTreeNode;
import org.nuclos.server.navigation.treenode.nuclet.content.NucletContentCustomComponentTreeNode;
import org.nuclos.server.navigation.treenode.nuclet.content.NucletContentEntityTreeNode;
import org.nuclos.server.navigation.treenode.nuclet.content.NucletContentRuleTreeNode;
import org.nuclos.server.navigation.treenode.nuclet.content.NucletContentTreeNode;
import org.nuclos.server.navigation.treenode.nuclet.content.ReportNucletContentTreeNode;

/**
 * Factory for creating <code>ExplorerNode</code>s out of <code>TreeNode</code>s.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */
public class ExplorerNodeFactory {

	private final Logger log = Logger.getLogger(this.getClass());

	private static ExplorerNodeFactory singleton;

	/**
	 * maps TreeNodes (classes) to ExplorerNode constructors
	 */
	private final Map<Class<? extends TreeNode>, Constructor<? extends ExplorerNode>> mp = CollectionUtils.newHashMap();

	public static synchronized ExplorerNodeFactory getInstance() {
		if (singleton == null) {
			singleton = newFactory();
		}
		return singleton;
	}

	private static ExplorerNodeFactory newFactory() {
		try {
			final String sClassName = LangUtils.defaultIfNull(
					ApplicationProperties.getInstance().getExplorerNodeFactoryClassName(),
					ExplorerNodeFactory.class.getName());

			return (ExplorerNodeFactory) Class.forName(sClassName).newInstance();
		}
		catch (Exception ex) {
			throw new CommonFatalException("ExplorerNodeFactory cannot be created.", ex);
		}
	}

	protected ExplorerNodeFactory() {
		this.putConstructor(GenericObjectTreeNode.class, GenericObjectExplorerNode.class);
		this.putConstructor(EntitySearchResultTreeNode.class, GenericObjectSearchResultExplorerNode.class);
		this.putConstructor(PersonalSearchFiltersByEntityTreeNode.class, PersonalSearchFiltersByEntityExplorerNode.class);
		this.putConstructor(SearchFilterTreeNode.class, SearchFilterExplorerNode.class);
		this.putConstructor(GroupTreeNode.class, GroupExplorerNode.class);
		this.putConstructor(RelationTreeNode.class, RelationExplorerNode.class);
		this.putConstructor(SubFormTreeNode.class, SubFormExplorerNode.class);
		this.putConstructor(SubFormEntryTreeNode.class, SubFormEntryExplorerNode.class);

		this.putConstructor(DefaultMasterDataTreeNode.class, MasterDataExplorerNode.class);
		this.putConstructor(RuleNode.class, RuleExplorerNode.class);
		this.putConstructor(StateModelNode.class, RuleStateModelExplorerNode.class);
		this.putConstructor(RuleGenerationNode.class, RuleObjectGenerationExplorerNode.class);
		this.putConstructor(EntityRuleNode.class, RuleEntityExplorerNode.class);
		this.putConstructor(EntityRuleNode.EntityRuleUsageStatusNode.class, RuleEntityExplorerNode.class);
		this.putConstructor(EntityRuleNode.EntityRuleUsageProcessNode.class, RuleEntityExplorerNode.class);
		this.putConstructor(EventSupportTreeNode.class, EventSupportExplorerNode.class);
		this.putConstructor(EventSupportTargetTreeNode.class, EventSupportTargetExplorerNode.class);
		this.putConstructor(TimelimitNode.class, RuleEntityExplorerNode.class);
		this.putConstructor(DirectoryRuleNode.class, RuleEntityExplorerNode.class);
		this.putConstructor(CodeTreeNode.class, CodeExplorerNode.class);
		this.putConstructor(LibraryTreeNode.class, LibraryExplorerNode.class);
		this.putConstructor(DatasourceNode.class, DatasourceExplorerNode.class);
		this.putConstructor(DatasourceReportFormularNode.class, DatasourceReportExplorerNode.class);
		this.putConstructor(DirectoryDatasourceNode.class, DatasourceDirectoryExplorerNode.class);
		this.putConstructor(AllDatasourceNode.class, DatasourceDirectoryExplorerNode.class);
		this.putConstructor(DatasourceUsageNode.class, DatasourceDirectoryExplorerNode.class);
		this.putConstructor(OwnDatasourceNode.class, DatasourceDirectoryExplorerNode.class);

		this.putConstructor(NucletTreeNode.class, NucletExplorerNode.class);
		
		this.putConstructor(NucletContentTreeNode.class, NucletContentExplorerNode.class);
		this.putConstructor(ReportNucletContentTreeNode.class, NucletContentExplorerNode.class);
		
		this.putConstructor(DefaultNucletContentEntryTreeNode.class, NucletContentEntryExplorerNode.class);
		this.putConstructor(NucletContentEntityTreeNode.class, NucletContentEntityExplorerNode.class);
		this.putConstructor(NucletContentCustomComponentTreeNode.class, NucletContentCustomComponentExplorerNode.class);
		this.putConstructor(NucletContentRuleTreeNode.class, NucletContentEntryExplorerNode.class);
	}

	protected void putConstructor(Class<? extends TreeNode> clsTreeNode, Class<? extends ExplorerNode> clsExplorerNode) {
		try {
			this.mp.put(clsTreeNode, clsExplorerNode.getDeclaredConstructor(TreeNode.class));
		}
		catch (NoSuchMethodException ex) {
			throw new CommonFatalException("ExplorerNode must define ctor(TreeNode).", ex);
		}
	}

	/**
	 * @param treenode
	 * @param doRefresh TODO
	 * @return a new <code>ExplorerNode</code> that is appropriate to present the given <code>TreeNode</code>.
	 */
	public ExplorerNode<?> newExplorerNode(TreeNode treenode, boolean doRefresh) {
		final ExplorerNode<?> result;

		// refresh content of treenode
		if(doRefresh) {
			try {
				treenode = treenode.refreshed();
			} catch (CommonFinderException e) {
				throw new ExplorerNodeRefreshException("Der Knoten "+treenode.getLabel()+" in der Baumansicht konnte nicht wiederhergestellt werden.\n" + e.getMessage());
			}
		}

		final Class<? extends TreeNode> clsTreeNode = treenode.getClass();
		final Constructor<? extends ExplorerNode> ctorExplorerNode = this.mp.get(clsTreeNode);
		if (ctorExplorerNode == null) {
			// default: create plain ExplorerNode for unregistered TreeNodes:
			result = new ExplorerNode<TreeNode>(treenode);
			log.warn("Unspecific ExplorerNode created for class " + clsTreeNode.getName());
		}
		else {
			try {
				result = ctorExplorerNode.newInstance(treenode);
			}
			catch (Exception ex) {
				throw new NuclosFatalException(ex);
			}
		}
		return result;
	}

}	// class ExplorerNodeFactory
