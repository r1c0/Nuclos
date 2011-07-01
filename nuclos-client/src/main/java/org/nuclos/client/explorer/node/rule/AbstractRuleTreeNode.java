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
package org.nuclos.client.explorer.node.rule;

import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.client.masterdata.datatransfer.RuleAndRuleUsageEntity;
import org.nuclos.client.rule.RuleDelegate;
import org.nuclos.common.NuclosBusinessException;
import org.nuclos.server.navigation.treenode.TreeNode;
import org.nuclos.server.ruleengine.valueobject.RuleVO;
import java.text.Collator;
import java.util.*;

/**
 * Treenode representing an Node in the Ruletree
 * @author <a href="mailto:rainer.schneider@novabit.de">rainer.schneider</a>
 */
public abstract class AbstractRuleTreeNode implements TreeNode {

	protected static final Collator textCollator = Collator.getInstance();

	private final Integer iId;
	private String sLabel;
	private String sDescription;
	private List<? extends TreeNode> lstSubNodes;
	private RuleNodeType nodeType;

	static {
		textCollator.setStrength(Collator.TERTIARY);
	}

	public AbstractRuleTreeNode(Integer iId, String sLabel, String sDescription, List<? extends TreeNode> lstSubNodes,
			RuleNodeType aNodeType) {

		this.iId = iId;
		this.sLabel = sLabel;
		this.sDescription = sDescription;
		this.lstSubNodes = lstSubNodes;
		this.nodeType = aNodeType;
	}

	/**
	 * Is the insert of an RuleNode as Subnode allowed at this position in the tree
	 * @return
	 */
	public abstract boolean isInsertRuleAllowd();

	/**
	 * Insert the ruleToInsert after the ruleBefore in the subnodes
	 * @param ruleToInsert
	 * @param ruleBefore
	 * @throws NuclosBusinessException
	 * @throws CommonBusinessException
	 */
	public void insertRule(List<RuleAndRuleUsageEntity>	ruleUsageEntityList, RuleVO ruleBefore)
			throws CommonBusinessException {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Integer getId() {
		return this.iId;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getLabel() {
		return this.sLabel;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getDescription() {
		return this.sDescription;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getIdentifier() {
		if (this.getId() != null) {
			return this.getId()+"#"+this.getLabel()+"#"+getNodeType();
		}
		else {
			return "0#"+this.getLabel()+"#"+getNodeType();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("deprecation")
	public List<? extends TreeNode> getSubNodes() {
		if (this.lstSubNodes == null) {
			refresh();
			if (this.lstSubNodes == null) {
				this.lstSubNodes = Collections.<TreeNode>emptyList();
			}
		}
		return this.lstSubNodes;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Boolean hasSubNodes() {
		//return null;
		return (this.lstSubNodes == null) ? null : !this.lstSubNodes.isEmpty();
	}

	@Override
	public void removeSubNodes() {
		this.setSubNodes(null);

		// @todo the postcondition is broken: assert this.hasSubNodes() == null;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setSubNodes(List<? extends TreeNode> lstSubNodes) {
		this.lstSubNodes = lstSubNodes;
	}

	@Override
	public boolean implementsNewRefreshMethod() {
		return false;
	}

	/**
	 * not supported.
	 */
	@Override
	public TreeNode refreshed() {
		//throw new UnsupportedOperationException("refreshed");
		return this;
	}

	@Override
	public boolean needsParent() {
		return false;
	}

	public RuleNodeType getNodeType() {
		return nodeType;
	}

	/**
	 * return the rule represented by this node
	 * @return null if it is no rule node
	 */
	public RuleAndRuleUsageEntity getRuleEntity() {
		return null;
	}

	/**
	 * @return
	 */
	protected Map<Integer, RuleVO> getAllRuleId2RuleMap() {
		final Collection<RuleVO> allRules = RuleDelegate.getInstance().getAllRules();
		final Map<Integer, RuleVO> result = new HashMap<Integer, RuleVO>(allRules.size() * 2);
		for (RuleVO curRule : allRules) {
			result.put(curRule.getId(), curRule);
		}
		return result;
	}

	/**
	 * sort the given Node List by the Label of the treenodes
	 * @param aNodeList
	 */
	protected static void sortNodeListByLabel(List<? extends TreeNode> aNodeList) {
		Collections.sort(aNodeList, new Comparator<TreeNode>() {
			@Override
			public int compare(TreeNode o1, TreeNode o2) {
				return textCollator.compare(o1.getLabel(), o2.getLabel());
			}
		});
	}

}	// class AbstractRuleTreeNode
