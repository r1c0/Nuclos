package org.nuclos.client.eventsupport;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;
import org.nuclos.client.explorer.EventSupportManagementExplorerView;
import org.nuclos.client.explorer.ExplorerViewFactory;
import org.nuclos.client.explorer.node.eventsupport.EventSupportTargetTreeNode;
import org.nuclos.client.explorer.node.eventsupport.EventSupportTargetType;
import org.nuclos.client.explorer.node.eventsupport.EventSupportTreeNode;
import org.nuclos.client.main.Main;
import org.nuclos.client.main.mainframe.MainFrameTab;
import org.nuclos.client.main.mainframe.MainFrameTabbedPane;
import org.nuclos.client.masterdata.MasterDataDelegate;
import org.nuclos.client.statemodel.EventSupportRepository;
import org.nuclos.client.statemodel.StateDelegate;
import org.nuclos.client.ui.Controller;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.SearchConditionUtils;
import org.nuclos.common.collect.collectable.searchcondition.CollectableComparison;
import org.nuclos.common.collect.collectable.searchcondition.CollectableIsNullCondition;
import org.nuclos.common.collect.collectable.searchcondition.ComparisonOperator;
import org.nuclos.common.dal.vo.EntityObjectVO;
import org.nuclos.common2.SpringLocaleDelegate;
import org.nuclos.server.eventsupport.valueobject.EventSupportVO;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;
import org.nuclos.server.navigation.ejb3.TreeNodeFacadeRemote;
import org.nuclos.server.statemodel.valueobject.StateTransitionVO;
import org.nuclos.server.statemodel.valueobject.StateVO;
import org.springframework.beans.factory.annotation.Autowired;

public class EventSupportManagementController extends Controller<MainFrameTabbedPane> {

	private static final Logger LOG = Logger.getLogger(EventSupportManagementController.class);	
	
	private EventSupportManagementView viewEventSupportManagement;
	
	private TreeNodeFacadeRemote treeNodeFacadeRemote;
	
	@Autowired
	final void setTreeNodeFacadeRemote(TreeNodeFacadeRemote treeNodeFacadeRemote) {
		this.treeNodeFacadeRemote = treeNodeFacadeRemote;
	}
	
	public EventSupportManagementController(MainFrameTabbedPane parent) {
		super(parent);
	}
	
	public static class NuclosESMRunnable implements Runnable {
	
		private final MainFrameTabbedPane desktopPane;
		
		public NuclosESMRunnable(MainFrameTabbedPane desktopPane) {
			this.desktopPane = desktopPane;
		}
		
		@Override
		public void run() {
			try {
				EventSupportManagementController w = new EventSupportManagementController(this.desktopPane);
				w.showManagementPane(desktopPane);
			}
			catch (Exception e) {
				LOG.error("showWizard failed: " + e, e);
			}
		}
		
	}

	public void showManagementPane(MainFrameTabbedPane desktopPane)
	{
		final SpringLocaleDelegate localeDelegate = SpringLocaleDelegate.getInstance();
		
		// Explorer Panel including Toolbar and tree for eventsupports
		final EventSupportTreeNode treenodeRoot = new EventSupportTreeNode(null, null,
				localeDelegate.getMessage("ExplorerController.24","Regelverwendungen"),
				localeDelegate.getMessage("ExplorerController.24","Regelverwendungen"),
				localeDelegate.getMessage("ExplorerController.24","Regelverwendungen"),
				EventSupportTargetType.ROOT, true);
		
		final EventSupportTargetTreeNode treenodeRootTargets = 
				new EventSupportTargetTreeNode(this, null,  
						getSpringLocaleDelegate().getMessage("ExplorerController.32","Regelzuweisung"),
						getSpringLocaleDelegate().getMessage("ExplorerController.32","Regelzuweisung"), 
						getSpringLocaleDelegate().getMessage("ExplorerController.32","Regelzuweisung"), EventSupportTargetType.ROOT, false);
		
		
		if (viewEventSupportManagement == null) {
			viewEventSupportManagement = new EventSupportManagementView(treenodeRoot,treenodeRootTargets);
		}
		
		final MainFrameTab ifrm = Main.getInstance().getMainController().newMainFrameTab(null, 
				localeDelegate.getMessage("nuclos.entity.eventsupportmangagement.label", "Nucleus Entit\u00e4tenwizard <Neue Entit\u00e4t>"));
		
		EventSupportManagementExplorerView newExplorerView = 
				(EventSupportManagementExplorerView) ExplorerViewFactory.getInstance().newExplorerView(treenodeRoot);
		
		ifrm.add(viewEventSupportManagement);
		
		ifrm.setTabIconFromSystem("getIconTree16");
		ifrm.setTitle(localeDelegate.getMessage("nuclos.entity.eventsupportmangagement.label", "Nucleus Entit\u00e4tenwizard <Neue Entit\u00e4t>"));
		ifrm.setLayeredComponent(newExplorerView.getViewComponent());
	//	ifrm.setTabStoreController(new ExplorerTabStoreController(treenodeRoot, newExplorerView));
		
		desktopPane.add(ifrm);
		ifrm.setVisible(true);
	}

	public List<EventSupportTargetTreeNode> createSubNodesByTargetType(EventSupportTreeNode node)
	{
		List<EventSupportTargetTreeNode> lstSubNodes = new ArrayList<EventSupportTargetTreeNode> ();
		Collection<MasterDataVO> masterData = null;
		EventSupportTargetType type = node.getTreeNodeType();
		Object nodeId = node.getId();
		
		switch (type) {
			case ROOT:
				masterData = MasterDataDelegate.getInstance().getMasterData(NuclosEntity.NUCLET.getEntityName());
				
				lstSubNodes.add(new EventSupportTargetTreeNode(EventSupportManagementController.this, null, "<Default>", "<Default>", "Alle nicht zugewiesenen Elemente", EventSupportTargetType.NUCLET, false));
				for (MasterDataVO msvo : masterData)
				{
					lstSubNodes.add(new EventSupportTargetTreeNode(EventSupportManagementController.this,msvo.getId(), msvo.getField("name").toString(), msvo.getField("name").toString(), msvo.getField("description").toString(), EventSupportTargetType.NUCLET, false));
				}
				break;
			case ENTITY_CATEGORIE:
				if (nodeId != null)
				{
					Collection<EntityObjectVO> dependantMasterData = MasterDataDelegate.getInstance().getDependantMasterData(NuclosEntity.ENTITY.getEntityName(), "nuclet", nodeId);					
					for (EntityObjectVO eoVO :dependantMasterData)
					{
						lstSubNodes.add(new EventSupportTargetTreeNode(EventSupportManagementController.this,eoVO.getId(), eoVO.getField("entity").toString(), eoVO.getField("entity").toString(), eoVO.getField("entity").toString(), EventSupportTargetType.ENTITY, false));
					}
				}
				else
				{
					masterData = MasterDataDelegate.getInstance().getMasterData(
							NuclosEntity.ENTITY.getEntityName(), new CollectableIsNullCondition(SearchConditionUtils.newEntityField(NuclosEntity.ENTITY.getEntityName(), "nuclet")));
					
					for (MasterDataVO msvo : masterData)
					{
						lstSubNodes.add(new EventSupportTargetTreeNode(EventSupportManagementController.this,msvo.getId(), msvo.getField("entity").toString(), msvo.getField("entity").toString(), msvo.getField("entity").toString(), EventSupportTargetType.ENTITY, false));
					}
				}
				
				break;
			case NUCLET:
				lstSubNodes.add(new EventSupportTargetTreeNode(EventSupportManagementController.this, nodeId, "Entitäten", "Entitäten",  "Entitäten des aktuellen Nuclets", EventSupportTargetType.ENTITY_CATEGORIE, false));
				lstSubNodes.add(new EventSupportTargetTreeNode(EventSupportManagementController.this, nodeId, "Statusmodelle", "Statusmodelle",  "Statusmodelle des aktuellen Nuclets", EventSupportTargetType.STATEMODEL_CATEGORIE, false));
				break;
			case STATEMODEL_CATEGORIE:
				if (nodeId != null)
				{
					Collection<EntityObjectVO> dependantMasterData = MasterDataDelegate.getInstance().getDependantMasterData(NuclosEntity.STATEMODEL.getEntityName(), "nuclet", nodeId);					
					for (EntityObjectVO eoVO :dependantMasterData)
					{
						lstSubNodes.add(new EventSupportTargetTreeNode(EventSupportManagementController.this,eoVO.getId(), eoVO.getField("name").toString(), eoVO.getField("name").toString(), eoVO.getField("description").toString(), EventSupportTargetType.STATE_TRANSITION, false));
					}
				}
				else
				{
					masterData = MasterDataDelegate.getInstance().getMasterData(
							NuclosEntity.STATEMODEL.getEntityName(), new CollectableIsNullCondition(SearchConditionUtils.newEntityField(NuclosEntity.STATEMODEL.getEntityName(), "nuclet")));
					
					for (MasterDataVO msvo : masterData)
					{
						lstSubNodes.add(new EventSupportTargetTreeNode(EventSupportManagementController.this,msvo.getId(), msvo.getField("name").toString(), msvo.getField("name").toString(), msvo.getField("description").toString(), EventSupportTargetType.STATE_TRANSITION, false));
					}
				}
				break;
			case STATE_TRANSITION:
				
				Collection<StateVO> statesByModel = StateDelegate.getInstance().getStatesByModel((Integer)nodeId);
				for (StateVO svo : statesByModel)
				{
					// Initial statetransitions
					StateTransitionVO stVO = StateDelegate.getInstance().getStateTransitionByNullAndTargetState(svo.getId());
					if (stVO != null)
					{
						String transitionLabel =  " -> " +  svo.getNumeral();
						lstSubNodes.add(new EventSupportTargetTreeNode(EventSupportManagementController.this,stVO.getId(), transitionLabel, transitionLabel, transitionLabel, null, false));						
					}
				}
				StateVO[] array = statesByModel.toArray(new StateVO[statesByModel.size()]);
				if (array.length >= 2)
				{
					StateTransitionVO stVO = StateDelegate.getInstance().getStateTransitionBySourceAndTargetState(array[0].getId(), array[1].getId());
					if (stVO != null)
					{
						String transitionLabel =  array[0].getNumeral() + " -> " + array[1].getNumeral();
						lstSubNodes.add(new EventSupportTargetTreeNode(EventSupportManagementController.this,stVO.getId(), transitionLabel, transitionLabel, transitionLabel, null, false));
					}
				}				
				break;
			case ENTITY:
				List<EventSupportVO> eventSupportTypes;
				try {
					eventSupportTypes = EventSupportRepository.getInstance().getEventSupportTypes();
					for (EventSupportVO s : eventSupportTypes)
					{
						lstSubNodes.add(new EventSupportTargetTreeNode(EventSupportManagementController.this, nodeId, s.getName(), s.getName(),  s.getDescription(), EventSupportTargetType.EVENTSUPPORT_TYPE, false));					
					}
				} catch (RemoteException e) {
					LOG.error(e.getMessage(),e);
				}
				break;
		default:
			break;
		}
		return lstSubNodes;
	}
}
