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


import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang.NullArgumentException;
import org.nuclos.client.common.MetaDataClientProvider;
import org.nuclos.client.masterdata.datatransfer.RuleAndRuleUsageEntity;
import org.nuclos.client.rule.RuleDelegate;
import org.nuclos.client.statemodel.StateDelegate;
import org.nuclos.client.ui.Errors;
import org.nuclos.common.NuclosBusinessException;
import org.nuclos.common2.SpringLocaleDelegate;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.server.genericobject.valueobject.GeneratorActionVO;
import org.nuclos.server.navigation.treenode.TreeNode;
import org.nuclos.server.ruleengine.valueobject.RuleVO;
import org.nuclos.server.statemodel.valueobject.StateModelVO;

/**
 * Treenode representing an Node in the Ruletree
 * @author <a href="mailto:rainer.schneider@novabit.de">rainer.schneider</a>
 */
public class DirectoryRuleNode extends AbstractRuleTreeNode {

	private static final long serialVersionUID = -7394680832135719670L;

	private boolean bRoot;
	private MainDirectory mainDirectory;
	private final boolean isAllRuleSubnode;

	public DirectoryRuleNode(boolean bRoot, String sLabel, String aDescription,
			List<? extends TreeNode> aSubNodeList, boolean aIsAllRuleSubnodeFlag) {

		super(null, sLabel, aDescription, aSubNodeList, RuleNodeType.DIRECTORY);

		if (sLabel == null) {
			throw new NullArgumentException("sLabel");
		}

		this.bRoot = bRoot;
		this.isAllRuleSubnode = aIsAllRuleSubnodeFlag;
	}

	public DirectoryRuleNode(boolean blnIsRoot, MainDirectory aMainDirectory, List<? extends TreeNode> aSubNodeList,
			boolean aIsAllRuleSubnodeFlag) {

		super(null, aMainDirectory.getLabel(), aMainDirectory.getDescription(), aSubNodeList, RuleNodeType.DIRECTORY);

		this.bRoot = blnIsRoot;
		this.mainDirectory = aMainDirectory;
		this.isAllRuleSubnode = aIsAllRuleSubnodeFlag;
	}

	@Override
	public Boolean hasSubNodes() {
		return bRoot ? Boolean.TRUE : null;
	}

	@Override
	public void refresh() {
		if (this.bRoot) {
			try {
				setSubNodes(getRootSubNodeList());
			}
			catch (NuclosBusinessException ex) {
				final String sMessage = SpringLocaleDelegate.getInstance().getMessage("DirectoryRuleNode.4","Fehler beim Erzeugen des Baumes");
				Errors.getInstance().showExceptionDialog(null, sMessage, ex);
			}
			catch (CommonFinderException ex) {
				final String sMessage = SpringLocaleDelegate.getInstance().getMessage("DirectoryRuleNode.5","Fehler beim Erzeugen des Baumes");
				Errors.getInstance().showExceptionDialog(null, sMessage, ex);
			}
		}
		else if (!isAllRuleSubnode) {
			switch (mainDirectory) {
				case SAVE_ENTITY:
					setSubNodes(createEventEntityRulesNode(RuleTreeModel.SAVE_EVENT_NAME));
					break;
				case SAVE_AFTER_ENTITY:
					setSubNodes(createEventEntityRulesNode(RuleTreeModel.SAVE_AFTER_EVENT_NAME));
					break;
				case DELETE_ENTITY:
					setSubNodes(createEventEntityRulesNode(RuleTreeModel.DELETE_EVENT_NAME));
					break;
				case DELETE_AFTER_ENTITY:
					setSubNodes(createEventEntityRulesNode(RuleTreeModel.DELETE_AFTER_EVENT_NAME));
					break;
				case USEREVENT_ENTITY:
					setSubNodes(createEventEntityRulesNode(RuleTreeModel.USER_EVENT_NAME));
					break;
				case OBJECT_GENERATION:
					setSubNodes(createAllGenerateObjectRulesNode());
					break;
				case STATE_TRANSITION:
					setSubNodes(createStateTransitionRulesNodes());
					break;
				case ALL_RULES:
					setSubNodes(createAllRulesNode());
					break;
			}
		}
	}

	@Override
	public RuleAndRuleUsageEntity getRuleEntity() {
		return null;
	}

	@Override
	public boolean isInsertRuleAllowd() {
		return false;
	}

	public boolean isAllRuleSubnode() {
		return isAllRuleSubnode;
	}

	@Override
	public boolean equals(Object obj) {
		return obj != null && obj instanceof DirectoryRuleNode
				&& getLabel().equals(((TreeNode) obj).getLabel());
	}

	@Override
	public int hashCode() {
		return getLabel().hashCode();
	}

	public boolean isRoot() {
		return bRoot;
	}

	private List<EntityRuleNode> createEventEntityRulesNode(String sEventName) {
		final List<EntityRuleNode> result = new ArrayList<EntityRuleNode>();
		/*for (MasterDataMetaVO mdmetaVO : MasterDataDelegate.getInstance().getMetaData()) {
			if (MasterDataDelegate.getInstance().getUsesRuleEngine(mdmetaVO.getEntityName())) {
				result.add(new EntityRuleNode(sEventName, mdmetaVO.getEntityName(), SpringLocaleDelegate.getLabelFromMetaDataVO(mdmetaVO), null, false));//mdmetaVO.getLabel(), null, false));
			}
		}*/
		Collection<String> ruleUsageEntitiesByEvent = RuleDelegate.getInstance().getRuleUsageEntityNamesByEvent(sEventName);
		for (String entityName : ruleUsageEntitiesByEvent) {
			result.add(new EntityRuleNode(sEventName, entityName,
				SpringLocaleDelegate.getInstance().getLabelFromMetaDataVO(MetaDataClientProvider.getInstance().getEntity(entityName)),
				null, false));
		}

		Collections.sort(result, new Comparator<EntityRuleNode>() {
			@Override
			public int compare(EntityRuleNode o1, EntityRuleNode o2) {
				return textCollator.compare(o1.getLabel(), o2.getLabel());
			}
		});

		return result;
	}

	/**
	 * Create all Nodes for the Object generation main node
	 */
	private List<RuleGenerationNode> createAllGenerateObjectRulesNode() {
		final Collection<GeneratorActionVO> collAllAdGenerations = RuleDelegate.getInstance().getAllAdGenerationsWithRule();

		final List<GeneratorActionVO> sortedGenerations = new ArrayList<GeneratorActionVO>(collAllAdGenerations);
		Collections.sort(sortedGenerations, new Comparator<GeneratorActionVO>() {
			@Override
			public int compare(GeneratorActionVO o1, GeneratorActionVO o2) {
				return textCollator.compare(o1.getName(), o2.getName());
			}
		});

		final List<RuleGenerationNode> result = new ArrayList<RuleGenerationNode>();
		for (GeneratorActionVO generationAd : sortedGenerations) {
			result.add(new RuleGenerationNode(generationAd, null, false));
		}

		return result;
	}

	private List<StateModelNode> createStateTransitionRulesNodes() {
		final Collection<StateModelVO> allStateModel = StateDelegate.getInstance().getAllStateModels();

		final List<StateModelVO> sortedStateModel = new ArrayList<StateModelVO>(allStateModel);
		Collections.sort(sortedStateModel, new Comparator<StateModelVO>() {
			@Override
			public int compare(StateModelVO o1, StateModelVO o2) {
				return textCollator.compare(o1.getName(), o2.getName());
			}
		});

		final List<StateModelNode> result = new ArrayList<StateModelNode>();

		for (StateModelVO curModel : sortedStateModel) {
			result.add(new StateModelNode(curModel, null, false, null));
		}
		return result;
	}

	private List<RuleNode> createAllRulesNode() {
		final List<RuleVO> sortedRules = new ArrayList<RuleVO>(RuleDelegate.getInstance().getAllRules());
		Collections.sort(sortedRules, new Comparator<RuleVO>() {
			@Override
			public int compare(RuleVO o1, RuleVO o2) {
				return textCollator.compare(o1.getRule(), o2.getRule());
			}
		});

		final List<RuleNode> result = new ArrayList<RuleNode>();

		for (RuleVO curRule : sortedRules) {
			result.add(new RuleNode(curRule, true));
		}

		return result;
	}

	public List<AbstractRuleTreeNode> getRootSubNodeList() throws CommonFinderException, NuclosBusinessException {
		final List<AbstractRuleTreeNode> result = new ArrayList<AbstractRuleTreeNode>();

		result.add(new DirectoryRuleNode(false, DirectoryRuleNode.MainDirectory.ALL_RULES, null, false));
		result.add(new DirectoryRuleNode(false, DirectoryRuleNode.MainDirectory.SAVE_ENTITY, null, false));
		result.add(new DirectoryRuleNode(false, DirectoryRuleNode.MainDirectory.SAVE_AFTER_ENTITY, null, false));
		result.add(new DirectoryRuleNode(false, DirectoryRuleNode.MainDirectory.DELETE_ENTITY, null, false));
		result.add(new DirectoryRuleNode(false, DirectoryRuleNode.MainDirectory.DELETE_AFTER_ENTITY, null, false));
		result.add(new DirectoryRuleNode(false, DirectoryRuleNode.MainDirectory.USEREVENT_ENTITY, null, false));
		result.add(new DirectoryRuleNode(false, DirectoryRuleNode.MainDirectory.STATE_TRANSITION, null, false));
		result.add(new DirectoryRuleNode(false, DirectoryRuleNode.MainDirectory.OBJECT_GENERATION, null, false));
		final SpringLocaleDelegate localeDelegate = SpringLocaleDelegate.getInstance();
		result.add(new TimelimitNode(
				localeDelegate.getMessage("DirectoryRuleNode.6","Fristen"), 
				localeDelegate.getMessage("DirectoryRuleNode.14","Regeln die t\u00e4glich vom System ausgef\u00fchrt werden"), true));
		result.add(new LibraryTreeNode(
				localeDelegate.getText("treenode.rules.library.label"), 
				localeDelegate.getText("treenode.rules.library.description")));

		return result;
	}

	public enum MainDirectory {
		OBJECT_GENERATION(SpringLocaleDelegate.getInstance().getMessage("DirectoryRuleNode.9","Objektgenerierung"), 
				SpringLocaleDelegate.getInstance().getMessage("DirectoryRuleNode.10","Regeln die automatisch bei der Objektgenerierung ausgef\u00fchrt werden")),
		STATE_TRANSITION(SpringLocaleDelegate.getInstance().getMessage("DirectoryRuleNode.18","Status\u00fcberg\u00e4nge"), 
				SpringLocaleDelegate.getInstance().getMessage("DirectoryRuleNode.16","Reglen die automatisch bei Status\u00fcberg\u00e4ngen ausgef\u00fchrt werden")),
		SAVE_ENTITY(SpringLocaleDelegate.getInstance().getMessage("DirectoryRuleNode.17","Speichern pro Entit\u00e4t"), 
				SpringLocaleDelegate.getInstance().getMessage("DirectoryRuleNode.12","Regeln die automatisch beim Speichern einer Entit\u00e4t ausgef\u00fchrt werden")),
		SAVE_AFTER_ENTITY(SpringLocaleDelegate.getInstance().getMessage("DirectoryRuleNode.20","Speichern (Im Anschluss) pro Entit\u00e4t"), 
				SpringLocaleDelegate.getInstance().getMessage("DirectoryRuleNode.19","Regeln die automatisch im Anschluss beim Speichern einer Entit\u00e4t ausgef\u00fchrt werden")),
		DELETE_ENTITY(SpringLocaleDelegate.getInstance().getMessage("DirectoryRuleNode.8","L\u00f6schen pro Entit\u00e4t"), 
				SpringLocaleDelegate.getInstance().getMessage("DirectoryRuleNode.11","Regeln die automatisch beim L\u00f6schen einer Entit\u00e4t ausgef\u00fchrt werden")),
		DELETE_AFTER_ENTITY(SpringLocaleDelegate.getInstance().getMessage("DirectoryRuleNode.21","L\u00f6schen (Im Anschluss) pro Entit\u00e4t"), 
				SpringLocaleDelegate.getInstance().getMessage("DirectoryRuleNode.22","Regeln die automatisch im Anschluss beim L\u00f6schen einer Entit\u00e4t ausgef\u00fchrt werden")),
		USEREVENT_ENTITY(SpringLocaleDelegate.getInstance().getMessage("DirectoryRuleNode.3","Benutzeraktion pro Entit\u00e4t"), 
				SpringLocaleDelegate.getInstance().getMessage("DirectoryRuleNode.13","Regeln die manuell durch den Benutzer gestartet werden k\u00f6nnen")),
		ALL_RULES(SpringLocaleDelegate.getInstance().getMessage("DirectoryRuleNode.1","Alle Regeln"), 
				SpringLocaleDelegate.getInstance().getMessage("DirectoryRuleNode.2","Alle Regeln")),
		TIMELIMIT(SpringLocaleDelegate.getInstance().getMessage("DirectoryRuleNode.7","Fristen"), 
				SpringLocaleDelegate.getInstance().getMessage("DirectoryRuleNode.15","Regeln die t\u00e4glich vom System ausgef\u00fchrt werden")),
		LIBRARY(SpringLocaleDelegate.getInstance().getText("treenode.rules.library.label"), 
				SpringLocaleDelegate.getInstance().getText("treenode.rules.library.description"));

		private final String label;
		private final String descripton;

		MainDirectory(String aLabel, String aDescription) {
			this.label = aLabel;
			this.descripton = aDescription;
		}

		public String getLabel() {
			return label;
		}

		public String getDescription() {
			return descripton;
		}

	}	// enum MainDirectory


}	// class DirectoryRuleNode
