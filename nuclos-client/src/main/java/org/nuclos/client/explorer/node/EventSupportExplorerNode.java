package org.nuclos.client.explorer.node;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeModel;

import org.jfree.util.Log;
import org.nuclos.client.customcode.CodeCollectController;
import org.nuclos.client.eventsupport.EventSupportManagementController;
import org.nuclos.client.eventsupport.EventSupportRepository;
import org.nuclos.client.explorer.ExplorerNode;
import org.nuclos.client.explorer.node.eventsupport.EventSupportTargetType;
import org.nuclos.client.explorer.node.eventsupport.EventSupportTreeNode;
import org.nuclos.client.masterdata.CollectableMasterDataWithDependants;
import org.nuclos.client.ui.Icons;
import org.nuclos.client.ui.collect.CollectControllerFactorySingleton;
import org.nuclos.client.ui.tree.TreeNodeAction;
import org.nuclos.common.collect.collectable.CollectableValueField;
import org.nuclos.common2.SpringLocaleDelegate;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.server.eventsupport.valueobject.EventSupportTypeVO;
import org.nuclos.server.navigation.treenode.TreeNode;

public class EventSupportExplorerNode extends ExplorerNode<EventSupportTreeNode> {

	public EventSupportExplorerNode(TreeNode treenode) {
		super(treenode);
	}

	public boolean importTransferData(Component parent, Transferable transferable, JTree tree) throws IOException, UnsupportedFlavorException {
		return true;
	}
	
	@Override
	public boolean isLeaf() {
		return ((EventSupportTreeNode)getTreeNode()).getSubNodes().isEmpty();
	}
	
	/**
	 * refreshes the current node (and its children) and notifies the given treemodel
	 * @param dtm the DefaultTreeModel to notify. Must contain this node.
	 */
	public void refreshParent(JTree tree) throws CommonFinderException {
		((ExplorerNode<?>) this.getParent()).refresh(tree);
	}
	
	@Override
	public int getDataTransferSourceActions() {

		return DnDConstants.ACTION_COPY_OR_MOVE;
	}
	
	public List<TreeNodeAction> getTreeNodeActions(JTree tree) {
		
		final List<TreeNodeAction> result = new LinkedList<TreeNodeAction>();
		EventSupportTreeNode node = (EventSupportTreeNode) getTreeNode();
		
		result.add(new RefreshAction(tree));
		
		if (EventSupportTargetType.EVENTSUPPORT_TYPE.equals(node.getTreeNodeType())) {
			result.add(new EventSupportEditor(tree));
			result.add(TreeNodeAction.newSeparatorAction());
		}

		final ShowInOwnTabAction actShowInOwnTab = new ShowInOwnTabAction(tree);
		actShowInOwnTab.setEnabled(!this.getTreeNode().needsParent());
		result.add(actShowInOwnTab);
		result.add(TreeNodeAction.newSeparatorAction());
		result.add(new ExpandAction(tree));
		result.add(new CollapseAction(tree));
		
		return result;
	}
	
	@Override
	public boolean getAllowsChildren() {
		return isLeaf() ? false : super.getAllowsChildren();
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
		case RULE:
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
	
	@Override
	public Action getTreeNodeActionOnMouseClick(JTree tree) {
		return new EventSupportShowPropertyAction(tree);
	}
	
	private class EventSupportShowPropertyAction extends AbstractAction
	{
		JTree tree;
		
		public EventSupportShowPropertyAction(JTree tree) {
			this.tree = tree;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			// selected tree element			
			final EventSupportExplorerNode node= (EventSupportExplorerNode) tree.getSelectionPath().getLastPathComponent();
			// show infos and properties for this node
			EventSupportManagementController controller = node.getTreeNode().getController();
			controller.showSourceSupportProperties(node.getTreeNode());
		}
	}
	
	/**
	 * refreshes the current node (and its children) and notifies the given treemodel
	 * @param dtm the DefaultTreeModel to notify. Must contain this node.
	 * @throws CommonFinderException if the object presented by this node no longer exists.
	 */
	public void refresh(final JTree tree, boolean fullRefreshCurrent) throws CommonFinderException {
	
		DefaultTreeModel dtm = (DefaultTreeModel) tree.getModel();
		unloadChildren();

		this.getTreeNode().refresh();
		loadChildren(true);
		dtm.nodeStructureChanged(this);
		tree.setSelectionRow(0);
	}
	

	protected class EventSupportEditor extends TreeNodeAction {
		
		private static final String ACTIONCOMMAND_SHOW_DETAILS = "SHOW DETAILS";
		
		public EventSupportEditor(JTree tree) {
			super(ACTIONCOMMAND_SHOW_DETAILS, "Neue Regel anlegen", tree);
		}

		private void runCodeCollectController (EventSupportTypeVO eventSupportTypeByName, String sDescription, String sClassname) {
			
			try {
				final CollectControllerFactorySingleton factory = CollectControllerFactorySingleton.getInstance();
				
				CodeCollectController rcc = factory.newCodeCollectController(null);
				CollectableMasterDataWithDependants clltable = rcc.newCollectable();
				
				clltable.setField("description", new CollectableValueField(sDescription));
				clltable.setField("active", new CollectableValueField(Boolean.TRUE));
				clltable.setField("source", new CollectableValueField(
						createEventsupportByType(eventSupportTypeByName, sDescription, sClassname)));
				
				rcc.runNewWith(clltable);
				
			} catch (Exception e) {
				Log.error(e.getMessage(), e);
			}
		}
		
		private String createEventsupportByType(EventSupportTypeVO eventSupportTypeByName, String sDescription, String sClassname) {
			
			StringBuilder sBuilder = new StringBuilder();
				
			// Imports
			for (String sImport : eventSupportTypeByName.getImports()) {
				sBuilder.append("import " + sImport + "; \n");
			}
			sBuilder.append("import org.nuclos.api.annotation.NuclosEvent; \n");
			
			// Methods
			String sMethodsToImplement = "";
			for (String m : eventSupportTypeByName.getMethods()) {
				sMethodsToImplement += "\n\t" + m + " { \n\t}";
			}
			
			sBuilder.append("\n/** @name        \n");
			sBuilder.append("  * @description \n");
			sBuilder.append("  * @usage       \n");
			sBuilder.append("  * @change      \n");
			sBuilder.append("*/\n");
			sBuilder.append("@NuclosEvent(name=\"" + sClassname+ "\", description=\"" + sDescription +"\")\n");
			sBuilder.append("public class " + sClassname.replace(" ", "") + " implements " + 
					eventSupportTypeByName.getClassname().substring(eventSupportTypeByName.getClassname().lastIndexOf(".") + 1) + 
					" {\n" + sMethodsToImplement + "\n}");
			
			return sBuilder.toString();
		}
		
		@Override
		public void actionPerformed(ActionEvent actEvent) {
			final JTree tree = this.getJTree();
			final EventSupportExplorerNode currentNode = (EventSupportExplorerNode) tree.getSelectionPath().getLastPathComponent();
			final EventSupportTreeNode treeNode = currentNode.getTreeNode();
			if (EventSupportTargetType.EVENTSUPPORT_TYPE.equals(treeNode.getTreeNodeType())) {
				
				final JTextField txtClassname = new JTextField();
				final JTextField txtDescription = new JTextField();
			
				try {
					EventSupportTypeVO eventSupportTypeByName = 
							EventSupportRepository.getInstance().getEventSupportTypeByName(treeNode.getEntityName());
			    
					int answer = JOptionPane.showConfirmDialog(  
							null, createPanel(txtClassname, txtDescription), SpringLocaleDelegate.getInstance().getMessage("nuclos.eventsupport.newInstance.headline", "Neue Regel anlegen", eventSupportTypeByName.getName()), JOptionPane.OK_CANCEL_OPTION,  
							JOptionPane.PLAIN_MESSAGE);  
			    
					if (answer == JOptionPane.YES_OPTION)  
					{  
			    		runCodeCollectController(eventSupportTypeByName, txtDescription.getText(), txtClassname.getText());
			    		
			    		currentNode.refresh(tree);
					}	
				} catch (Exception e) {
						Log.error(e.getMessage(), e);
				}	
			}
		}
		
		private JPanel createPanel(JTextField txtClassname, JTextField txtDescription) {
			
			JPanel retVal = new JPanel();
			retVal.setLayout(new BorderLayout());
			retVal.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 5));
			
			final JPanel pnlHeadline = new JPanel();
			pnlHeadline.setLayout(new BorderLayout());
			
			final JPanel pnlInputFields = new JPanel();
			pnlInputFields.setLayout(new GridBagLayout());
			GridBagConstraints c = new GridBagConstraints();
			
			c.fill = GridBagConstraints.HORIZONTAL;
			c.weightx = 0.0;
			c.gridx = 0;
			c.gridy = 0;
			c.gridwidth = 2;
			c.insets = new Insets(10,5,10,5);
			pnlHeadline.add(new JLabel(SpringLocaleDelegate.getInstance().getMessage("nuclos.eventsupport.newInstance.infotext.1", "")), BorderLayout.NORTH);
			pnlHeadline.add(new JLabel(SpringLocaleDelegate.getInstance().getMessage("nuclos.eventsupport.newInstance.infotext.2", "")), BorderLayout.CENTER);
			pnlHeadline.add(new JLabel(SpringLocaleDelegate.getInstance().getMessage("nuclos.eventsupport.newInstance.infotext.3", "")), BorderLayout.SOUTH);
			pnlInputFields.add(pnlHeadline, c);
			c.insets = new Insets(10,5,0,5);
			c.gridy = 1;
			c.gridwidth = 1;
			c.anchor = GridBagConstraints.PAGE_START;	
			
			pnlInputFields.add(new JLabel(SpringLocaleDelegate.getInstance().getMessage("R00012179", "Name")), c);
			c.gridy = 2;
			pnlInputFields.add(new JLabel(SpringLocaleDelegate.getInstance().getMessage("R00012263", "Beschreibung")), c);
			c.weightx = 1.0;
			c.gridx = 1;
			pnlInputFields.add(txtDescription, c);
			c.gridy = 1;
			pnlInputFields.add(txtClassname, c);
			c.gridy = 3;
			pnlInputFields.add(new JLabel(), c);
			
			retVal.add(pnlInputFields, BorderLayout.NORTH);
			
			return retVal;
		}
	}	
}
