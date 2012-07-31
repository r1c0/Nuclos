package org.nuclos.client.explorer.node;

import javax.swing.Icon;

import org.nuclos.client.common.MetaDataClientProvider;
import org.nuclos.client.explorer.node.eventsupport.EventSupportTargetType;
import org.nuclos.client.explorer.node.eventsupport.EventSupportTreeNode;
import org.nuclos.client.main.Main;
import org.nuclos.client.main.mainframe.IconResolverConstants;
import org.nuclos.client.ui.Icons;
import org.nuclos.common.MetaDataProvider;
import org.nuclos.common.NuclosEntity;
import org.nuclos.server.navigation.treenode.TreeNode;

public class EventSupportExplorerNode extends AbstractRuleExplorerNode {

	public EventSupportExplorerNode(TreeNode treenode) {
		super(treenode);
	}

	@Override
	public Icon getIcon() {

		Icon result = null;
		
		EventSupportTargetType treeNodeType = ((EventSupportTreeNode) getUserObject()).getTreeNodeType();
		
		if (treeNodeType == null)
			 return null;
		
		
		switch (treeNodeType) 
		{
		case EVENTSUPPORT:
			result = Icons.getInstance().getIconRuleUsage16();
			break;
		case STATE_TRANSITION:
			result = Icons.getInstance().getIconState();
			break;
		case ENTITY:
			result = Icons.getInstance().getIconGenericObject16();
			break;
		default:
			break;
		}

		return result;
	}
}
