package org.nuclos.client.eventsupport;

import java.rmi.RemoteException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;

import org.apache.log4j.Logger;
import org.jfree.util.Log;
import org.nuclos.client.eventsupport.model.EventSupportEntityPropertiesTableModel;
import org.nuclos.client.eventsupport.model.EventSupportPropertiesTableModel;
import org.nuclos.client.eventsupport.model.EventSupportStatePropertiesTableModel;
import org.nuclos.client.eventsupport.panel.EventSupportManagementView;
import org.nuclos.client.explorer.node.eventsupport.EventSupportTargetTreeNode;
import org.nuclos.client.explorer.node.eventsupport.EventSupportTargetType;
import org.nuclos.client.explorer.node.eventsupport.EventSupportTreeNode;
import org.nuclos.client.main.Main;
import org.nuclos.client.main.mainframe.MainFrameTab;
import org.nuclos.client.main.mainframe.MainFrameTabbedPane;
import org.nuclos.client.masterdata.MasterDataDelegate;
import org.nuclos.client.statemodel.StateDelegate;
import org.nuclos.client.ui.Controller;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.SearchConditionUtils;
import org.nuclos.common.collect.collectable.searchcondition.CollectableIsNullCondition;
import org.nuclos.common.dal.vo.EntityObjectVO;
import org.nuclos.common2.SpringLocaleDelegate;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.common2.exception.CommonPermissionException;
import org.nuclos.server.eventsupport.valueobject.EventSupportEventVO;
import org.nuclos.server.eventsupport.valueobject.EventSupportVO;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;
import org.nuclos.server.statemodel.valueobject.StateGraphVO;
import org.nuclos.server.statemodel.valueobject.StateTransitionVO;
import org.nuclos.server.statemodel.valueobject.StateVO;

public class EventSupportManagementController extends Controller<MainFrameTabbedPane> {

	private static final Logger LOG = Logger.getLogger(EventSupportManagementController.class);	
	
	private EventSupportManagementView viewEventSupportManagement;
	
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

	public void showSupportProperties(EventSupportTreeNode node)
	{
		EventSupportPropertiesTableModel propertyModel = viewEventSupportManagement.getPropertyModel();
		try {
			propertyModel.clear();
			EventSupportVO eventSupportByClassname;
			switch (node.getTreeNodeType())
			{
				case EVENTSUPPORT:
					eventSupportByClassname = EventSupportRepository.getInstance().getEventSupportByClassname(node.getEntityName());
					break;
				case EVENTSUPPORT_TYPE:
					eventSupportByClassname = EventSupportRepository.getInstance().getEventSupportTypeByName(node.getEntityName());
					break;
				default:
					eventSupportByClassname = null;
			}
			
			if (eventSupportByClassname != null)
			{
				DateFormat dateInstance = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
				
				propertyModel.addEntry(EventSupportPropertiesTableModel.ELM_ES_NAME, eventSupportByClassname.getName());
				propertyModel.addEntry(EventSupportPropertiesTableModel.ELM_ES_DESCRIPTION, eventSupportByClassname.getDescription());
				propertyModel.addEntry(EventSupportPropertiesTableModel.ELM_ES_TYPE, eventSupportByClassname.getInterface());
				propertyModel.addEntry(EventSupportPropertiesTableModel.ELM_ES_NUCLET, "<todo>");
				propertyModel.addEntry(EventSupportPropertiesTableModel.ELM_ES_PATH, eventSupportByClassname.getPackage());
				propertyModel.addEntry(EventSupportPropertiesTableModel.ELM_ES_CREATION_DATE, 
						eventSupportByClassname.getDateOfCompilation() != null ? dateInstance.format(eventSupportByClassname.getDateOfCompilation()) : null);
				
			}
			
		} catch (RemoteException e) {
			LOG.error(e.getMessage(), e);
		}	
	}
	
	public void showTargetSupportProperties(EventSupportTreeNode node) {
		try {
				switch (node.getTreeNodeType()) {
				case EVENTSUPPORT_TYPE:
					EventSupportEntityPropertiesTableModel targetEntityModel = viewEventSupportManagement.getTargetEntityModel();
					targetEntityModel.clear();
					if (node.getParentNode().getTreeNodeType().equals(EventSupportTargetType.ENTITY))
					{	
						Integer iEntityId = Integer.parseInt(node.getParentNode().getId().toString());
						Collection<EventSupportEventVO> eventSupportsForEntity = EventSupportRepository.getInstance().getEventSupportsForEntity(iEntityId);
					
						for (EventSupportEventVO esevo : eventSupportsForEntity)
						{
							if (esevo.getEventSupportType().equals(node.getEntityName()))
							{
								Integer iOrder = esevo.getOrder();
								String  sState = esevo.getStateId() != null ? esevo.getStateId().toString() : null;
								String  sProcess = esevo.getStateId() != null ? esevo.getProcessId().toString() : null;
								targetEntityModel.addEntry(esevo.getEventSupportClass(), iOrder, sState, sProcess);
							}
						}
						// show Property Panel for this supporttype
						viewEventSupportManagement.getTargetViewPanel().loadPropertyPanelByModelType(targetEntityModel);
					}
					break;
				case STATE_TRANSITION:
					EventSupportStatePropertiesTableModel targetStateModel = viewEventSupportManagement.getTargetStateModel();
					MasterDataVO masterDataVO = MasterDataDelegate.getInstance().get(NuclosEntity.STATETRANSITION.getEntityName(), node.getId());
					
					Integer state1 = masterDataVO.getField("state1Id") != null ? Integer.parseInt(masterDataVO.getField("state1Id").toString()) : null;
					Integer state2 = masterDataVO.getField("state2Id") != null ? Integer.parseInt(masterDataVO.getField("state2Id").toString()) : null;

					if (node.getParentNode() != null && node.getParentNode().getId() != null)
					{
						Integer litFrom = null;
						String nameFrom = null;
						Integer litTo = null;
						String nameTo = null;
						
						if (state1 != null) {
							
							MasterDataVO mdVo = MasterDataDelegate.getInstance().get(NuclosEntity.STATE.getEntityName(), state1, true);
							litFrom = Integer.parseInt(mdVo.getField("numeral").toString());
							nameFrom = mdVo.getField("name").toString();
						}
						if (state2 != null) {
							MasterDataVO mdVo = MasterDataDelegate.getInstance().get(NuclosEntity.STATE.getEntityName(), state2, true);
							litTo = Integer.parseInt(mdVo.getField("numeral").toString());
							nameTo = mdVo.getField("name").toString();
						}
						
						targetStateModel.clear();
						targetStateModel.addEntry(litFrom, nameFrom, litTo, nameTo);

					}
					viewEventSupportManagement.getTargetViewPanel().loadPropertyPanelByModelType(targetStateModel);
					
					break;
				default:
					break;
				}
			
			
		} catch (RemoteException e) {
			LOG.error(e.getMessage(), e);
		} catch (CommonPermissionException e) {
			LOG.error(e.getMessage(), e);
		} catch (CommonFinderException e) {
			LOG.error(e.getMessage(), e);
		}	
	}
	
	public void showManagementPane(MainFrameTabbedPane desktopPane)
	{
		final SpringLocaleDelegate localeDelegate = SpringLocaleDelegate.getInstance();
		
		// Explorer Panel including Toolbar and tree for eventsupports
		final EventSupportTreeNode treenodeRoot = new EventSupportTreeNode(this, null, null,
				localeDelegate.getMessage("ExplorerController.24","Regelverwendungen"),
				localeDelegate.getMessage("ExplorerController.24","Regelverwendungen"),
				localeDelegate.getMessage("ExplorerController.24","Regelverwendungen"),
				EventSupportTargetType.ROOT, true);
		
		final EventSupportTargetTreeNode treenodeRootTargets = new EventSupportTargetTreeNode(this, null, null, 
				getSpringLocaleDelegate().getMessage("ExplorerController.32","Regelzuweisung"),
				getSpringLocaleDelegate().getMessage("ExplorerController.32","Regelzuweisung"), 
				getSpringLocaleDelegate().getMessage("ExplorerController.32","Regelzuweisung"), 
				EventSupportTargetType.ROOT, false);
		
		
		if (viewEventSupportManagement == null) {
			viewEventSupportManagement = new EventSupportManagementView(treenodeRoot,treenodeRootTargets);
		}
		
		final MainFrameTab ifrm = Main.getInstance().getMainController().newMainFrameTab(null, 
				localeDelegate.getMessage("nuclos.entity.eventsupportmangagement.label", "Nucleus Entit\u00e4tenwizard <Neue Entit\u00e4t>"));
	
		ifrm.add(viewEventSupportManagement);
		
		ifrm.setTabIconFromSystem("getIconTree16");
		ifrm.setTitle(localeDelegate.getMessage("nuclos.entity.eventsupportmangagement.label", "Nucleus Entit\u00e4tenwizard <Neue Entit\u00e4t>"));
		
		desktopPane.add(ifrm);
		ifrm.setVisible(true);
	}

	public List<EventSupportTreeNode> createSubNodesByType(EventSupportTreeNode esNode)
	{
		List<EventSupportTreeNode> retVal = new ArrayList<EventSupportTreeNode>();
		
		try {
			List<EventSupportVO> eventSupportTypes;
			
			switch (esNode.getTreeNodeType()) 
			{
				case ROOT:
					Collection<MasterDataVO> masterData = MasterDataDelegate.getInstance().getMasterData(NuclosEntity.NUCLET.getEntityName());
					
					retVal.add(new EventSupportTreeNode(this, esNode, null, "<Default>", "<Default>", "Alle nicht zugewiesenen Elemente", EventSupportTargetType.NUCLET, false));
					for (MasterDataVO msvo : masterData)
					{
						retVal.add(new EventSupportTreeNode(this, esNode, msvo.getId(), msvo.getField("package").toString(), msvo.getField("name").toString(), msvo.getField("description").toString(), EventSupportTargetType.NUCLET, false));
					}
					break;
				case EVENTSUPPORT_TYPE:
					eventSupportTypes = EventSupportRepository.getInstance().getEventSupportsByType(esNode.getEntityName());
					masterData = MasterDataDelegate.getInstance().getMasterData(NuclosEntity.NUCLET.getEntityName());
					
					for (EventSupportVO s : eventSupportTypes)
					{
						boolean isNucletEventSupport = false;
						for (MasterDataVO msvo : masterData)
						{
							if (msvo.getField("package").equals(s.getPackage()))
							{
								isNucletEventSupport = true;
								break;
							}
						}
						
						if (esNode.getEntityName() != null && (esNode.getParentNode().getEntityName().equals(s.getPackage()) || (!isNucletEventSupport && esNode.getParentNode().getEntityName().equals("<Default>"))))
						{
							EventSupportTreeNode eventSupportTreeNode = new EventSupportTreeNode(this, esNode, null, s.getClassname(), s.getName(), s.getDescription(), EventSupportTargetType.EVENTSUPPORT, false);
							retVal.add(eventSupportTreeNode); 								
						}
					}	
					break;
				case NUCLET:
					eventSupportTypes = EventSupportRepository.getInstance().getEventSupportTypes();
					for (EventSupportVO s : eventSupportTypes)
					{
						EventSupportTreeNode eventSupportTreeNode = new EventSupportTreeNode(this, esNode, null, s.getClassname(), s.getName(), s.getDescription(), EventSupportTargetType.EVENTSUPPORT_TYPE, false);
						retVal.add(eventSupportTreeNode);
					}
					break;
				default:
					break;
			}
			
		} catch (RemoteException e) {
			Log.error(e.getMessage(), e);
		}
		
		return retVal;
	}
	
	public EventSupportEventVO addEventSupportToEntity(EventSupportTreeNode sourceNode, EventSupportTreeNode targetNode)
	{
		EventSupportEventVO retVal = null;
		
		if (targetNode != null && EventSupportTargetType.ENTITY.equals(targetNode.getTreeNodeType()))
		{
			String sEventSupportClassname = sourceNode.getEntityName();
			String sEventSupportClassType = sourceNode.getParentNode().getEntityName();
			
			Integer entId = Integer.parseInt(targetNode.getId().toString());
			EventSupportEventVO eseVO = 
					new EventSupportEventVO(sEventSupportClassname,
											sEventSupportClassType,
											entId,
											null, null, new Integer(1),
											null, null, null);
											
			EventSupportEventVO savedESEntity = EventSupportDelegate.getInstance().create(eseVO);
			
			if (savedESEntity != null) {
				retVal = savedESEntity;
			}			
		}
		
		return retVal;
	}
	
	public List<EventSupportTargetTreeNode> createTargetSubNodesByType(EventSupportTargetTreeNode esNode)
	{
		EventSupportTargetTreeNode node = (EventSupportTargetTreeNode) esNode;
		List<EventSupportTargetTreeNode> lstSubNodes = new ArrayList<EventSupportTargetTreeNode> ();
		Collection<MasterDataVO> masterData = null;
		EventSupportTargetType type = node.getTreeNodeType();
		Object nodeId = node.getId();
		
		switch (type) {
			case ROOT:
				masterData = MasterDataDelegate.getInstance().getMasterData(NuclosEntity.NUCLET.getEntityName());
				
				lstSubNodes.add(new EventSupportTargetTreeNode(this, node, null, "<Default>", "<Default>", "Alle nicht zugewiesenen Elemente", EventSupportTargetType.NUCLET, false));
				for (MasterDataVO msvo : masterData)
				{
					lstSubNodes.add(new EventSupportTargetTreeNode(this, node, msvo.getId(), msvo.getField("name").toString(), msvo.getField("name").toString(), msvo.getField("description").toString(), EventSupportTargetType.NUCLET, false));
				}
				break;
			case ENTITY_CATEGORIE:
				if (nodeId != null)
				{
					Collection<EntityObjectVO> dependantMasterData = MasterDataDelegate.getInstance().getDependantMasterData(NuclosEntity.ENTITY.getEntityName(), "nuclet", nodeId);					
					for (EntityObjectVO eoVO :dependantMasterData)
					{
						lstSubNodes.add(new EventSupportTargetTreeNode(this, node, eoVO.getId(), eoVO.getField("entity").toString(), eoVO.getField("entity").toString(), eoVO.getField("entity").toString(), EventSupportTargetType.ENTITY, false));
					}
				}
				else
				{
					masterData = MasterDataDelegate.getInstance().getMasterData(
							NuclosEntity.ENTITY.getEntityName(), new CollectableIsNullCondition(SearchConditionUtils.newEntityField(NuclosEntity.ENTITY.getEntityName(), "nuclet")));
					
					for (MasterDataVO msvo : masterData)
					{
						lstSubNodes.add(new EventSupportTargetTreeNode(this, node ,msvo.getId(), msvo.getField("entity").toString(), msvo.getField("entity").toString(), msvo.getField("entity").toString(), EventSupportTargetType.ENTITY, false));
					}
				}
				
				break;
			case NUCLET: 
				lstSubNodes.add(new EventSupportTargetTreeNode(this, node, nodeId, "Entitäten", "Entitäten",  "Entitäten des aktuellen Nuclets", EventSupportTargetType.ENTITY_CATEGORIE, false));
				lstSubNodes.add(new EventSupportTargetTreeNode(this, node,nodeId, "Statusmodelle", "Statusmodelle",  "Statusmodelle des aktuellen Nuclets", EventSupportTargetType.STATEMODEL_CATEGORIE, false));
				break;
			case STATEMODEL_CATEGORIE:
				if (nodeId != null)
				{
					Collection<EntityObjectVO> dependantMasterData = MasterDataDelegate.getInstance().getDependantMasterData(NuclosEntity.STATEMODEL.getEntityName(), "nuclet", nodeId);					
					for (EntityObjectVO eoVO :dependantMasterData)
					{
						lstSubNodes.add(new EventSupportTargetTreeNode(this, node,eoVO.getId(), eoVO.getField("name").toString(), eoVO.getField("name").toString(), eoVO.getField("description").toString(), EventSupportTargetType.STATEMODEL, false));
					}
				}
				else
				{
					masterData = MasterDataDelegate.getInstance().getMasterData(
							NuclosEntity.STATEMODEL.getEntityName(), new CollectableIsNullCondition(SearchConditionUtils.newEntityField(NuclosEntity.STATEMODEL.getEntityName(), "nuclet")));
					
					for (MasterDataVO msvo : masterData)
					{
						lstSubNodes.add(new EventSupportTargetTreeNode(this, node ,msvo.getId(), msvo.getField("name").toString(), msvo.getField("name").toString(), msvo.getField("description").toString(), EventSupportTargetType.STATEMODEL, false));
					}
				}
				break;
			case STATEMODEL:
				int moduleId = ((Long)nodeId).intValue();
				
				List<StateTransitionVO> orderedTrans = StateDelegate.getInstance().getOrderedStateTransitionsByStatemodel(moduleId);
				
				String sStateSource =  null;
				String sStateTarget =  null;
				
				for (StateTransitionVO stVO : orderedTrans)
				{
					if (stVO.getStateSource() != null)
					{
						StateVO stateSource = StateDelegate.getInstance().getStatesByModel(moduleId, stVO.getStateSource());
						sStateSource = stateSource.getNumeral() + " (" + stateSource.getStatename() + ")";
					}
					StateVO stateTarget = StateDelegate.getInstance().getStatesByModel(moduleId, stVO.getStateTarget());
					sStateTarget = stateTarget.getNumeral() + " ( " + stateTarget.getStatename() + ")";
					
					String transitionLabel =  sStateSource + " -> " +  sStateTarget;
					
					lstSubNodes.add(new EventSupportTargetTreeNode(this, node ,stVO.getId(), transitionLabel, transitionLabel, transitionLabel, EventSupportTargetType.STATE_TRANSITION, false));
				}
			
				break;
			case ENTITY:
				List<EventSupportVO> eventSupportTypes;
				try {
					eventSupportTypes = EventSupportRepository.getInstance().getEventSupportTypes();
					// Only display supporttypes if there are support classes with same typ attached
					Integer iEntityId = Integer.parseInt(node.getId().toString());
					Collection<EventSupportEventVO> eventSupportsForEntity = EventSupportRepository.getInstance().getEventSupportsForEntity(iEntityId);
					
					for (EventSupportVO s : eventSupportTypes)
					{
						boolean typeAlreadyInserted = false;
						for (EventSupportEventVO eseVO : eventSupportsForEntity)
						{
							if (eseVO.getEventSupportType().equals(s.getClassname()) && !typeAlreadyInserted)
							{
								lstSubNodes.add(new EventSupportTargetTreeNode(this, node, nodeId, s.getClassname(), s.getName(),  s.getDescription(), EventSupportTargetType.EVENTSUPPORT_TYPE, false));
								typeAlreadyInserted = true;
							}
						}
					}
				} catch (RemoteException e) {
					LOG.error(e.getMessage(),e);
				}
				break;
			case STATE_TRANSITION:
				break;
		default:
			break;
		}
		return lstSubNodes;
	}

}
