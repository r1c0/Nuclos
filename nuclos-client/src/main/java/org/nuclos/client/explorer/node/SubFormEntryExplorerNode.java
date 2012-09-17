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
package org.nuclos.client.explorer.node;

import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JTree;
import javax.swing.KeyStroke;

import org.apache.log4j.Logger;
import org.nuclos.client.common.ClientParameterProvider;
import org.nuclos.client.common.MetaDataClientProvider;
import org.nuclos.client.common.security.SecurityCache;
import org.nuclos.client.explorer.ExplorerNode;
import org.nuclos.client.genericobject.GenericObjectDelegate;
import org.nuclos.client.genericobject.Modules;
import org.nuclos.client.main.Main;
import org.nuclos.client.main.mainframe.MainFrame;
import org.nuclos.client.masterdata.MasterDataDelegate;
import org.nuclos.client.resource.NuclosResourceCache;
import org.nuclos.client.resource.ResourceCache;
import org.nuclos.client.ui.Bubble;
import org.nuclos.client.ui.Errors;
import org.nuclos.client.ui.Icons;
import org.nuclos.client.ui.UIUtils;
import org.nuclos.client.ui.tree.TreeNodeAction;
import org.nuclos.common.ParameterProvider;
import org.nuclos.common.dal.vo.EntityFieldMetaDataVO;
import org.nuclos.common.dal.vo.EntityObjectVO;
import org.nuclos.common2.CommonRunnable;
import org.nuclos.common2.IdUtils;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.common2.exception.CommonPermissionException;
import org.nuclos.server.genericobject.valueobject.GenericObjectWithDependantsVO;
import org.nuclos.server.masterdata.valueobject.DependantMasterDataMap;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;
import org.nuclos.server.navigation.treenode.GenericObjectTreeNode;
import org.nuclos.server.navigation.treenode.SubFormEntryTreeNode;
import org.nuclos.server.navigation.treenode.SubFormTreeNode;
import org.nuclos.server.navigation.treenode.TreeNode;

/**
 * <code>ExplorerNode</code> presenting a <code>OldMasterDataTreeNode</code>.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */
public class SubFormEntryExplorerNode<TN extends SubFormEntryTreeNode> extends ExplorerNode<TN> {

	private static final Logger LOG = Logger.getLogger(SubFormEntryExplorerNode.class);

	public SubFormEntryExplorerNode(TreeNode treenode) {
		super(treenode);
	}

    @Override
	public List<TreeNodeAction> getTreeNodeActions(JTree tree) {
		final List<TreeNodeAction> result = new LinkedList<TreeNodeAction>(super.getTreeNodeActions(tree));

		result.add(TreeNodeAction.newSeparatorAction());
		result.add(this.newShowDetailsAction(tree, false));
		result.add(this.newShowDetailsAction(tree, true));

		Map<String, Integer> foreignReferences = getForeignReferences();
		for (String sEntity : foreignReferences.keySet()) {
			result.add(new ShowReferenceAction(
				sEntity,
				foreignReferences.get(sEntity),
				tree, sEntity));
		}

		result.add(new RemoveAction(tree));
		return result;
	}

    protected class RemoveAction extends TreeNodeAction {

		public static final int KEY = KeyEvent.VK_DELETE;

    	private Integer iObjectId = null;
    	private String sEntity = null;
    	private String sSubFormEntity = null;
    	private String sSubFormForeignField = null;

        public RemoveAction(JTree tree) {
			super(ACTIONCOMMAND_REMOVE, 
					getSpringLocaleDelegate().getMessage("MasterDataExplorerNode.1", "L\u00f6schen")+ "...", tree);

			javax.swing.tree.TreeNode tnParent = SubFormEntryExplorerNode.this.getParent();
			if (tnParent instanceof SubFormExplorerNode && ((SubFormExplorerNode) tnParent).getTreeNode() instanceof SubFormTreeNode) {
				SubFormTreeNode sfTreeNode = (SubFormTreeNode) ((SubFormExplorerNode) tnParent).getTreeNode();

				iObjectId = IdUtils.unsafeToId(sfTreeNode.getTreeNodeObject().getId());
				sEntity = sfTreeNode.getTreeNodeObject().getEntityName();
				sSubFormEntity = sfTreeNode.getMasterDataVO().getField("entity", String.class);
				sSubFormForeignField = sfTreeNode.getMasterDataVO().getField("field", String.class);

				if(Modules.getInstance().isModuleEntity(sEntity)) {
					Integer iStateId = ((GenericObjectTreeNode)sfTreeNode.getTreeNodeObject()).getStatusId();
					setEnabled(SecurityCache.getInstance().isWriteAllowedForModule(sEntity, iObjectId) &&
						SecurityCache.getInstance().getSubFormPermission(sSubFormEntity, iStateId).includesWriting());
				} else {
					// @todo have we not permissions on subform in a masterdata object?
					setEnabled(SecurityCache.getInstance().isWriteAllowedForModule(sEntity, iObjectId));
				}
			} else {
				setEnabled(false);
			}
		}

		@Override
        protected void customizeComponent(JComponent comp, boolean bDefault) {
	        super.customizeComponent(comp, bDefault);
	        comp.addKeyListener(new KeyAdapter() {
				@Override
                public void keyTyped(KeyEvent e) {
					if(e.getKeyChar() == KEY) {
						askAndRemove();
					}
                }
	        });

	        if (comp instanceof JMenuItem) {
	        	((JMenuItem)comp).setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
	        }
        }

		@Override
		public void actionPerformed(ActionEvent ev) {
			askAndRemove();
		}

		public void askAndRemove() {
			if (isEnabled()) {
				final String sName = getTreeNode().getLabel();
				final String sMessage = getSpringLocaleDelegate().getMessage("SubFormEntryExplorerNode.3", "Wollen Sie den Unterformulareintrag \"{0}\" wirklich l\u00f6schen?", sName);
				final int iBtn = JOptionPane.showConfirmDialog(this.getParent(), sMessage, 
						getSpringLocaleDelegate().getMessage("SubFormEntryExplorerNode.2", "Unterformulareintrag l\u00f6schen"),
						JOptionPane.OK_CANCEL_OPTION);
				if (iBtn == JOptionPane.OK_OPTION) {
					remove(getJTree());
				}
			}
		}

	    private void remove(JTree tree){
			if (iObjectId != null && sEntity != null && sSubFormEntity != null) {
				try {
					if(Modules.getInstance().isModuleEntity(sEntity)) {
						GenericObjectWithDependantsVO gowdVO = GenericObjectDelegate.getInstance().getWithDependants(iObjectId, ClientParameterProvider.getInstance().getValue(ParameterProvider.KEY_LAYOUT_CUSTOM_KEY));
						for (EntityObjectVO mdVO : gowdVO.getDependants().getData(sSubFormEntity)) {
							if (mdVO.getId().equals(getTreeNode().getId())) {
								mdVO.flagRemove();
							}
						}
						GenericObjectDelegate.getInstance().update(gowdVO, ClientParameterProvider.getInstance().getValue(ParameterProvider.KEY_LAYOUT_CUSTOM_KEY));
					} else {
						if (sSubFormForeignField != null) {
							MasterDataVO mdvo = MasterDataDelegate.getInstance().get(sEntity, iObjectId);
							List<EntityObjectVO> dependants = new ArrayList<EntityObjectVO>(MasterDataDelegate.getInstance().getDependantMasterData(sSubFormEntity, sSubFormForeignField, iObjectId));
							for (EntityObjectVO mdVO : dependants) {
								if (mdVO.getId().equals(getTreeNode().getId())) {
									mdVO.flagRemove();
								}
							}
							MasterDataDelegate.getInstance().update(sEntity, mdvo, new DependantMasterDataMap(sSubFormEntity, dependants), ClientParameterProvider.getInstance().getValue(ParameterProvider.KEY_LAYOUT_CUSTOM_KEY));
						}
					}
					
					javax.swing.tree.TreeNode tnParent = SubFormEntryExplorerNode.this.getParent();
					if (tnParent instanceof SubFormExplorerNode){
						((SubFormExplorerNode)tnParent).refresh(tree);
					}
	            }
	            catch(CommonBusinessException e) {
	            	LOG.warn("remove failed: " + e);
		            showBubble(tree, "<html>"+Errors.formatErrorForBubble(e.getMessage())+"</html>");
	            }
			}
	    }
	}

	@Override
    public void handleKeyEvent(JTree tree, KeyEvent ev) {
		if(ev.getKeyChar() == RemoveAction.KEY) {
			(new RemoveAction(tree)).askAndRemove();

	    }
    }

    private Map<String, Integer> getForeignReferences(){
		Map<String, Integer> result = new HashMap<String, Integer>();

		javax.swing.tree.TreeNode tnParent = getParent();
		if (tnParent instanceof SubFormExplorerNode && ((SubFormExplorerNode) tnParent).getTreeNode() instanceof SubFormTreeNode) {
			SubFormTreeNode sfTreeNode = (SubFormTreeNode) ((SubFormExplorerNode) tnParent).getTreeNode();

			String sModuleEntity = sfTreeNode.getTreeNodeObject().getEntityName();
			String sSubFormEntity = sfTreeNode.getMasterDataVO().getField("entity", String.class);

			for (EntityFieldMetaDataVO efMeta : MetaDataClientProvider.getInstance().getAllEntityFieldsByEntity(sSubFormEntity).values()) {
				if (efMeta.getForeignEntity() != null && !efMeta.getForeignEntity().equals(sModuleEntity)) {
					// Reference to other entity:
                    try {
                    	MasterDataVO mdVO = MasterDataDelegate.getInstance().get(sSubFormEntity, getTreeNode().getId());
                    	Object oId = mdVO.getField(efMeta.getField()+"Id");
                    	if (oId != null)
                    		result.put(efMeta.getForeignEntity(), (Integer) oId);
                    } catch (CommonFinderException e) {
	                    // do nothing
    	            	LOG.warn("getForeignReferences failed: " + e);
                   } catch (CommonPermissionException e) {
	                    // do nothing
                	   LOG.warn("getForeignReferences failed: " + e);
                    }
				}
			}
		}

		return result;
	}

	private class ShowReferenceAction extends TreeNodeAction {

		private final String entityName;
		private final Integer id;

		ShowReferenceAction(final String entityName, final Integer id, JTree tree, String action) {
			super(action, 
					getSpringLocaleDelegate().getMessage("SubFormEntryExplorerNode.1", "{0} öffnen", 
					getSpringLocaleDelegate().getLabelFromMetaDataVO(MetaDataClientProvider.getInstance().getEntity(entityName))), tree);
			this.entityName = entityName;
			this.id = id;
		}

		@Override
		public void actionPerformed(ActionEvent ev) {
			this.cmdShowDetails(this.entityName, this.id);
		}

		/**
		 * command: show the details of the leased object represented by the given explorernode
		 * @param explorernode
		 */
		private void cmdShowDetails(final String entityName, final Integer id) {
			UIUtils.runCommand(this.getParent(), new CommonRunnable() {
				@Override
				public void run() throws CommonBusinessException {
					Main.getInstance().getMainController().showDetails(entityName, id);
				}
			});
		}
	}

	@Override
	public String getDefaultTreeNodeActionCommand(JTree tree) {
		if (newShowDetailsAction(tree, false).isEnabled()) {
			return getDefaultObjectNodeAction();
		}
		else if (getForeignReferences().size() == 1) {
			return getForeignReferences().keySet().iterator().next();
		}
		return ACTIONCOMMAND_EXPAND;
	}

	private void showBubble(JComponent component, String message) {
		new Bubble(
			component,
			message,
			10,
			Bubble.Position.NW)
		.setVisible(true);
	}

	@Override
	public Icon getIcon() {
		Integer resId = MetaDataClientProvider.getInstance().getEntity(getTreeNode().getEntityName()).getResourceId();
		String nuclosResource = MetaDataClientProvider.getInstance().getEntity(getTreeNode().getEntityName()).getNuclosResource();
		if(resId != null) {
			ImageIcon standardIcon = ResourceCache.getInstance().getIconResource(resId);
			return MainFrame.resizeAndCacheTabIcon(standardIcon);
		} else if (nuclosResource != null){
			ImageIcon nuclosIcon = NuclosResourceCache.getNuclosResourceIcon(nuclosResource);
			if (nuclosIcon != null) return MainFrame.resizeAndCacheTabIcon(nuclosIcon);
		}
		return Icons.getInstance().getIconGenericObject16();
	}
}
