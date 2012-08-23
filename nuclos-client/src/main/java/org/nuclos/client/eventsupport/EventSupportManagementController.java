package org.nuclos.client.eventsupport;

import java.rmi.RemoteException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.nuclos.client.eventsupport.model.EventSupportEntityPropertiesTableModel;
import org.nuclos.client.eventsupport.model.EventSupportGenerationPropertiesTableModel;
import org.nuclos.client.eventsupport.model.EventSupportJobPropertiesTableModel;
import org.nuclos.client.eventsupport.model.EventSupportSourcePropertiesTableModel;
import org.nuclos.client.eventsupport.model.EventSupportStatePropertiesTableModel;
import org.nuclos.client.eventsupport.panel.EventSupportTargetView;
import org.nuclos.client.eventsupport.panel.EventSupportView;
import org.nuclos.client.explorer.node.eventsupport.EventSupportTargetTreeNode;
import org.nuclos.client.explorer.node.eventsupport.EventSupportTargetType;
import org.nuclos.client.explorer.node.eventsupport.EventSupportTreeNode;
import org.nuclos.client.explorer.node.rule.DirectoryRuleNode;
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
import org.nuclos.common2.IdUtils;
import org.nuclos.common2.SpringLocaleDelegate;
import org.nuclos.server.eventsupport.valueobject.EventSupportEventVO;
import org.nuclos.server.eventsupport.valueobject.EventSupportGenerationVO;
import org.nuclos.server.eventsupport.valueobject.EventSupportJobVO;
import org.nuclos.server.eventsupport.valueobject.EventSupportSourceVO;
import org.nuclos.server.eventsupport.valueobject.EventSupportTransitionVO;
import org.nuclos.server.eventsupport.valueobject.ProcessVO;
import org.nuclos.server.genericobject.valueobject.GeneratorActionVO;
import org.nuclos.server.job.valueobject.JobVO;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;
import org.nuclos.server.navigation.treenode.TreeNode;
import org.nuclos.server.statemodel.valueobject.StateModelVO;
import org.nuclos.server.statemodel.valueobject.StateTransitionVO;
import org.nuclos.server.statemodel.valueobject.StateVO;

public class EventSupportManagementController extends Controller<MainFrameTabbedPane> {

	public static final Map<String, String> MAP_EVENTTYPES = new HashMap<String, String>();
	
	static {
		MAP_EVENTTYPES.put("CustomSupport", "User");
		MAP_EVENTTYPES.put("InsertSupport", "Insert");
		MAP_EVENTTYPES.put("InsertFinalSupport", "Insert.after");
		MAP_EVENTTYPES.put("UpdateSupport", "Update");
		MAP_EVENTTYPES.put("UpdateFinalSupport", "Update.after");
		MAP_EVENTTYPES.put("DeleteSupport", "Delete");
		MAP_EVENTTYPES.put("DeleteFinalSupport", "DeleteAfter");
		MAP_EVENTTYPES.put("GenerateSupport", "User");
		MAP_EVENTTYPES.put("GenerateFinalSupport", "User");
	}
	
	private static final Logger LOG = Logger.getLogger(EventSupportManagementController.class);	
	private EventSupportView viewEventSupportManagement;
	private EventSupportActionHandler esActionHandler;
	private MainFrameTab ifrm;
	
	
	public EventSupportManagementController(MainFrameTabbedPane pParent) {
		super(pParent);
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
		EventSupportSourcePropertiesTableModel propertyModel = viewEventSupportManagement.getPropertyModel();
		try {
			propertyModel.clear();
			EventSupportSourceVO eventSupportByClassname;
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
				
				propertyModel.addEntry(EventSupportSourcePropertiesTableModel.ELM_ES_NAME, eventSupportByClassname.getName());
				propertyModel.addEntry(EventSupportSourcePropertiesTableModel.ELM_ES_DESCRIPTION, eventSupportByClassname.getDescription());
				propertyModel.addEntry(EventSupportSourcePropertiesTableModel.ELM_ES_TYPE, eventSupportByClassname.getInterface());
				propertyModel.addEntry(EventSupportSourcePropertiesTableModel.ELM_ES_NUCLET, node.getParentNode().getParentNode().getEntityName());
				propertyModel.addEntry(EventSupportSourcePropertiesTableModel.ELM_ES_PATH, eventSupportByClassname.getPackage());
				propertyModel.addEntry(EventSupportSourcePropertiesTableModel.ELM_ES_CREATION_DATE, 
						eventSupportByClassname.getDateOfCompilation() != null ? dateInstance.format(eventSupportByClassname.getDateOfCompilation()) : null);
				
			}
			
		} catch (RemoteException e) {
			LOG.error(e.getMessage(), e);
		}	
	}
	
	public void showTargetStateSupportPropertiesNotModified(final EventSupportTreeNode node) {
		final EventSupportStatePropertiesTableModel targetStateModel = viewEventSupportManagement.getTargetStateModel();
		
		Integer iStatemodelId = Integer.parseInt(node.getId().toString());
		
		try {
			List<StateTransitionVO> lstTranses = StateDelegate.getInstance().getOrderedStateTransitionsByStatemodel(iStatemodelId);
			List<EventSupportTransitionVO> lstSupportsForTransition = EventSupportRepository.getInstance().getEventSupportsByStateModelId(iStatemodelId);
			
			targetStateModel.clear();
			
			targetStateModel.addTransitions(lstTranses);
			
			for(EventSupportTransitionVO estVO : lstSupportsForTransition) {
				EventSupportSourceVO esVO = EventSupportRepository.getInstance().getEventSupportByClassname(estVO.getEventSupportClass());
				if (node.getEntityName().equals(esVO.getInterface()))
					targetStateModel.addEntry(estVO);
			}
			
			viewEventSupportManagement.getTargetViewPanel().loadPropertyPanelByModelType(targetStateModel);
			
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}
		
	}
	
	public void showTargetGenerationSupportPropertiesNotModified(final EventSupportTreeNode node) {
		EventSupportGenerationPropertiesTableModel targetGenerationModel = viewEventSupportManagement.getTargetGenerationModel();
		
		Collection<EventSupportGenerationVO> lstEseGVOs;
		
		try {
			
			lstEseGVOs = EventSupportRepository.getInstance().getEventSupportsByGenerationId((Integer) node.getId());
			targetGenerationModel.clear();
			
			for (EventSupportGenerationVO esgVO : lstEseGVOs) {
				 EventSupportSourceVO esVO = EventSupportRepository.getInstance().getEventSupportByClassname(esgVO.getEventSupportClass());
				 if (esVO.getInterface().equals(node.getEntityName()))
					 targetGenerationModel.addEntry(esgVO);
			}
			
			viewEventSupportManagement.getTargetViewPanel().loadPropertyPanelByModelType(targetGenerationModel);
			
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}
	}
	
	public void showTargetJobSupportPropertiesNotModified (final EventSupportTreeNode node) {
		final EventSupportJobPropertiesTableModel targetJobModel = viewEventSupportManagement.getTargetJobModel();
		targetJobModel.clear();
		
		try {
			if (node.getParentNode().getTreeNodeType().equals(EventSupportTargetType.JOB))
			{
				Integer iJobControllerId = Integer.parseInt(node.getParentNode().getId().toString());
				Collection<EventSupportJobVO> eventSupportsForJob = EventSupportRepository.getInstance().getEventSupportsForJob(iJobControllerId);
				
				for (EventSupportJobVO esjVO : eventSupportsForJob) {
					targetJobModel.addEntry(esjVO);
				}
				
				viewEventSupportManagement.getTargetViewPanel().loadPropertyPanelByModelType(targetJobModel);
				
			}			
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}
	}
	
	public void showTargetEntitySupportPropertiesNotModified(final EventSupportTreeNode node) {
		
		final EventSupportEntityPropertiesTableModel targetEntityModel = viewEventSupportManagement.getTargetEntityModel();
		targetEntityModel.clear();
		
		try {
			if (node.getParentNode().getTreeNodeType().equals(EventSupportTargetType.ENTITY))
			{	
				Integer iEntityId = Integer.parseInt(node.getParentNode().getId().toString());
				Collection<EventSupportEventVO> eventSupportsForEntity = EventSupportRepository.getInstance().getEventSupportsForEntity(iEntityId);
				
				for (EventSupportEventVO esevo : eventSupportsForEntity)
				{
					if (esevo.getEventSupportType().equals(node.getEntityName()))
					{
						targetEntityModel.addEntry(esevo);
					}
				}
				// show Property Panel for this supporttype
				EventSupportTargetView targetViewPanel = viewEventSupportManagement.getTargetViewPanel();
				
				// States for the given entities
				Collection<StateVO> statesByModule = StateDelegate.getInstance().getStatesByModule(iEntityId);
				targetEntityModel.getStatus().clear();
				for (StateVO svo : statesByModule)
				{
					targetEntityModel.addStatus(svo);
				}
				
				
				// Processes for the given entities
				Collection<ProcessVO> processesByModuleId = EventSupportRepository.getInstance().getProcessesByModuleId(iEntityId);
				targetEntityModel.getProcess().clear();
				for (ProcessVO svo : processesByModuleId)
				{
					targetEntityModel.addProcess(svo);
				}
				
				targetViewPanel.loadPropertyPanelByModelType(targetEntityModel);
			}
		} catch (RemoteException e) {
			LOG.error(e.getMessage(), e);
		}
	}
		
	public void showTargetSupportProperties(final EventSupportTreeNode node) {
		
		switch (node.getTreeNodeType()) {
				case EVENTSUPPORT_TYPE:
					showTargetEntitySupportPropertiesNotModified(node);
					break;
				case STATE_TRANSITION:
					showTargetStateSupportPropertiesNotModified(node);
					break;
				case JOB_EVENT:
					showTargetJobSupportPropertiesNotModified(node);
					break;
				case GENERATION_EVENT:
					showTargetGenerationSupportPropertiesNotModified(node);
					break;
				default:
					viewEventSupportManagement.getTargetViewPanel().loadPropertyPanelByModelType(null);
					break;
			}
	}
	
	public void showManagementPane(MainFrameTabbedPane desktopPane)
	{
		final SpringLocaleDelegate localeDelegate = SpringLocaleDelegate.getInstance();
		
		// Explorer Panel including Toolbar and tree for eventsupports
		final EventSupportTreeNode treenodeRoot = new EventSupportTreeNode(this, null, null,
				null, null, null,
				EventSupportTargetType.ROOT, true);
		
		final EventSupportTargetTreeNode treenodeRootTargets = new EventSupportTargetTreeNode(this, null, null, 
				getSpringLocaleDelegate().getMessage("ExplorerController.32","Regelzuweisung"),
				getSpringLocaleDelegate().getMessage("ExplorerController.32","Regelzuweisung"), 
				getSpringLocaleDelegate().getMessage("ExplorerController.32","Regelzuweisung"), 
				EventSupportTargetType.ROOT, false);
		
		
		if (viewEventSupportManagement == null) {
			
			viewEventSupportManagement = new EventSupportView(treenodeRoot, treenodeRootTargets);
			
			this.esActionHandler = 
					new EventSupportActionHandler(viewEventSupportManagement);
			
			viewEventSupportManagement.setActionMap(this.esActionHandler.getActionMaps());
			
			viewEventSupportManagement.showGui();			
		}
		
		ifrm = Main.getInstance().getMainController().newMainFrameTab(null, 
				localeDelegate.getMessage("nuclos.entity.eventsupportmangagement.label", "Nucleus Entit\u00e4tenwizard <Neue Entit\u00e4t>"));
	
		ifrm.add(viewEventSupportManagement);
		
		ifrm.setTabIconFromSystem("getIconTree16");
		ifrm.setTitle(localeDelegate.getMessage("nuclos.entity.eventsupportmangagement.label", "Nucleus Entit\u00e4tenwizard <Neue Entit\u00e4t>"));
		
		desktopPane.add(ifrm);
		ifrm.setVisible(true);
	}

	public List<TreeNode> createSubNodesByType(EventSupportTreeNode esNode)
	{
		List<TreeNode> retVal = new ArrayList<TreeNode>();
		
		try {
			List<EventSupportSourceVO> eventSupportTypes;
			
			switch (esNode.getTreeNodeType()) 
			{
				case RULEVERSION:
					// Default Node for all nuclos elements that are not attached to a nuclet
					retVal.add(new EventSupportTreeNode(this, esNode, null, "<Default>", "<Default>", "Alle nicht zugewiesenen Elemente", EventSupportTargetType.NUCLET, false));
					// Nuclet Nodes
					Collection<MasterDataVO> masterData = MasterDataDelegate.getInstance().getMasterData(NuclosEntity.NUCLET.getEntityName());
					for (MasterDataVO msvo : masterData)
					{
						retVal.add(new EventSupportTreeNode(this, esNode, msvo.getId(), msvo.getField("package").toString(), msvo.getField("name").toString(), msvo.getField("description").toString(), EventSupportTargetType.NUCLET, false));
					}
					break;
				case ROOT:
						retVal.add(new EventSupportTreeNode(this, esNode, null, getSpringLocaleDelegate().getMessage("ExplorerController.33","Regelbibliothek"), getSpringLocaleDelegate().getMessage("ExplorerController.33","Regelbibliothek"), getSpringLocaleDelegate().getMessage("ExplorerController.33","Regelbibliothek"), EventSupportTargetType.RULEVERSION, false));
						retVal.add(new DirectoryRuleNode(true, getSpringLocaleDelegate().getMessage(
								"ExplorerController.24","Regelverwendungen"), 
								getSpringLocaleDelegate().getMessage("ExplorerController.25","Regelverwendungen"), null, false));
					break;
				case NUCLET:
					// EventSupportTypes
					eventSupportTypes = EventSupportRepository.getInstance().getEventSupportTypes();
					for (EventSupportSourceVO s : eventSupportTypes)
					{
						EventSupportTreeNode eventSupportTreeNode = new EventSupportTreeNode(this, esNode, null, s.getClassname(), s.getName(), s.getDescription(), EventSupportTargetType.EVENTSUPPORT_TYPE, false);
						retVal.add(eventSupportTreeNode);
					}
					break;
				case EVENTSUPPORT_TYPE:
					eventSupportTypes = EventSupportRepository.getInstance().getEventSupportsByType(esNode.getEntityName());
					masterData = MasterDataDelegate.getInstance().getMasterData(NuclosEntity.NUCLET.getEntityName());
					
					for (EventSupportSourceVO s : eventSupportTypes)
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
				default:
					break;
			}
			
		} catch (RemoteException e) {
			LOG.error(e.getMessage(), e);
		}
		
		return retVal;
	}
	
	public EventSupportJobVO addEventSupportToJob(EventSupportTreeNode sourceNode, EventSupportTreeNode targetNode) {
		EventSupportJobVO retVal = null;
		
		if (targetNode != null && EventSupportTargetType.JOB.equals(targetNode.getTreeNodeType()))
		{
			String  sSupportClass = sourceNode.getEntityName();
			String  sDescription = sourceNode.getDescription();
			Integer iJobControllerId = Integer.parseInt(targetNode.getId().toString());
			Integer iOrder = targetNode.getSubNodes().size() + 1;
			try {
				Collection<EventSupportJobVO> eventSupportsForJob = EventSupportRepository.getInstance().getEventSupportsForJob(iJobControllerId);
				iOrder = eventSupportsForJob != null ? eventSupportsForJob.size() + 1: 1;
				retVal = EventSupportDelegate.getInstance().createEventSupportJob(
						new EventSupportJobVO(sDescription,sSupportClass, iOrder, iJobControllerId));
			} catch (Exception e) {
				LOG.error(e.getMessage(), e);
			}
		}
		
		return retVal;
	}
	
	public EventSupportGenerationVO addEventSupportToGeneration(EventSupportTreeNode sourceNode, EventSupportTreeNode targetNode) {
		EventSupportGenerationVO retVal = null;
		
		if (targetNode != null && EventSupportTargetType.GENERATION.equals(targetNode.getTreeNodeType()))
		{
			try {
				Integer genId = Integer.parseInt(targetNode.getId().toString());
				int newOrder = 1;
				
				EventSupportSourceVO eseVO = EventSupportRepository.getInstance().getEventSupportByClassname(sourceNode.getEntityName());				
				Collection<EventSupportGenerationVO> lsteseg = EventSupportRepository.getInstance().getEventSupportsForGeneration(genId);
				
				for (EventSupportGenerationVO esgVO : lsteseg) {
					if (EventSupportRepository.getInstance().getEventSupportByClassname(
							esgVO.getEventSupportClass()).getInterface().equals(eseVO.getInterface()))
							 newOrder++;
				}
				
				Boolean bRunAfterwards = Boolean.FALSE;
				if ("org.nuclos.api.eventsupport.GenerateFinalSupport".equals(eseVO.getInterface())) {
					bRunAfterwards = Boolean.TRUE;
				}
				
				String  sSupportClass = sourceNode.getEntityName();
				Integer iGenerationId = Integer.parseInt(targetNode.getId().toString());
				Integer iOrder = newOrder;
				
				retVal = EventSupportDelegate.getInstance().createEventSupportGeneration(
						new EventSupportGenerationVO(iOrder,iGenerationId,sSupportClass, bRunAfterwards));
								
			} catch (Exception e) {
				LOG.error(e.getMessage(), e);
			}
		}
		return retVal;
	}
	
	public EventSupportTransitionVO addEventSupportToStateTransition(EventSupportTreeNode sourceNode, EventSupportTreeNode targetNode) {
		EventSupportTransitionVO retVal = null;
		
		if (targetNode != null && EventSupportTargetType.STATEMODEL.equals(targetNode.getTreeNodeType()))
		{
			try {
				String sSupportClass = sourceNode.getEntityName();
				Integer iModuleId = Integer.parseInt(targetNode.getId().toString());
				Boolean bRunAfterwards = true;
				
				String sEventSupportType = EventSupportRepository.getInstance().getEventSupportByClassname(sSupportClass).getInterface();
				
				List<EventSupportTransitionVO> stateTransitionsBySupportType = 
						EventSupportDelegate.getInstance().getStateTransitionsBySupportType(iModuleId, sEventSupportType);
				
				List<StateTransitionVO> orderedStateTransitionsByStatemodel = StateDelegate.getInstance().getOrderedStateTransitionsByStatemodel(iModuleId);
				
				Integer iOrder = stateTransitionsBySupportType.size() + 1;
				Integer iTransId =  orderedStateTransitionsByStatemodel.size() > 0 ? orderedStateTransitionsByStatemodel.get(0).getId() : 0;
				
				retVal = EventSupportDelegate.getInstance().createEventSupportTransition(
						new EventSupportTransitionVO(sSupportClass, iTransId, iOrder, bRunAfterwards));
				
			} catch (Exception e) {
				LOG.error(e.getMessage(), e);
			}
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
			
			int idx = 0;
			int id = 0;

			if (targetNode.getId() instanceof Integer)
				id = ((Integer) targetNode.getId()).intValue();
			
			
			Collection<EventSupportEventVO> eventSupportsForEntity = EventSupportDelegate.getInstance().getEventSupportsForEntity(id);
			for (EventSupportEventVO eseVO : eventSupportsForEntity) {
				if (sourceNode.getParentNode().getEntityName().equals(eseVO.getEventSupportType()))
						idx++;
			}
			
			Integer entId = Integer.parseInt(targetNode.getId().toString());
			EventSupportEventVO eseVO = 
					new EventSupportEventVO(sEventSupportClassname,
											sEventSupportClassType,
											entId,
											null, null, ++idx,
											null, null, null);
						
			
			// attach eventsupport to entity
			EventSupportEventVO savedESEntity = null;
			try {
				savedESEntity = EventSupportDelegate.getInstance().createEventSupportEvent(eseVO);
			} catch (Exception e) {
				LOG.error(e.getMessage(), e);
			}
			
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
		
		String strStatuwechsel = getSpringLocaleDelegate().getMessage("RuleNode.12","Statuswechsel");
		String strTimelimits = getSpringLocaleDelegate().getMessage("RuleNode.3","Fristen");
		String strWorksteps = getSpringLocaleDelegate().getMessage("RuleNode.7","Arbeitsschritt");
		String strEntitaet = getSpringLocaleDelegate().getMessage("nuclos.entity.entity.label","Entität");
		String strAllRules = getSpringLocaleDelegate().getMessage("DirectoryRuleNode.1","All Regeln");
		
		
		if (type != null) {
			switch (type) {
			case ROOT:
				masterData = MasterDataDelegate.getInstance().getMasterData(NuclosEntity.NUCLET.getEntityName());
				lstSubNodes.add(new EventSupportTargetTreeNode(this, node, null, strAllRules, strAllRules, strAllRules, EventSupportTargetType.ALL_EVENTSUPPORTS, false));
				lstSubNodes.add(new EventSupportTargetTreeNode(this, node, null, "<Default>", "<Default>", "Alle nicht zugewiesenen Elemente", EventSupportTargetType.NUCLET, false));
				for (MasterDataVO msvo : masterData)
				{
					lstSubNodes.add(new EventSupportTargetTreeNode(this, node, msvo.getId(), msvo.getField("name").toString(), msvo.getField("name").toString(), msvo.getField("description").toString(), EventSupportTargetType.NUCLET, false));
				}
				break;
			case ALL_EVENTSUPPORTS:
				try {
					for (EventSupportSourceVO eseVO : EventSupportRepository.getInstance().getAllEventSupports()) {
						lstSubNodes.add(new EventSupportTargetTreeNode(this, node, eseVO.getId(), eseVO.getClassname(), eseVO.getName(), eseVO.getDescription(), EventSupportTargetType.EVENTSUPPORT, false));					
					}					
				} catch (Exception e) {
					LOG.error(e.getMessage(), e);
				}
				break;
			case ALL_ENTITIES:
				try {
					List<EventSupportEventVO> eventSupportEventsByClassname = EventSupportRepository.getInstance().getEventSupportEventsByClassname(node.getParentNode().getEntityName());
					for (EventSupportEventVO eseVO : eventSupportEventsByClassname) {
						lstSubNodes.add(new EventSupportTargetTreeNode(this, node, eseVO.getId(), eseVO.getEntityName(), eseVO.getEntityName(), eseVO.getEntityName(), EventSupportTargetType.ALL_ENTITIES_ENTITY, false));
					}					
				} catch (Exception ex) {
					LOG.error(ex.getMessage(), ex);
				}
				break;
			case ALL_STATEMODELS:
				try {
					List<StateModelVO> stateModelByEventSupportClassname = EventSupportRepository.getInstance().getStateModelByEventSupportClassname(node.getParentNode().getEntityName());
					for (StateModelVO eseVO : stateModelByEventSupportClassname) {
						lstSubNodes.add(new EventSupportTargetTreeNode(this, node, eseVO.getId(), eseVO.getName(), eseVO.getName(), eseVO.getDescription(), EventSupportTargetType.ALL_STATEMODELS_STATEMODEL, false));
					}					
				} catch (Exception e) {
					LOG.error(e.getMessage(), e);
				}
				break;
			case ALL_JOBS:
				try {
					List<JobVO> jobsByClassname = EventSupportRepository.getInstance().getJobsByEventSupportClassname(node.getParentNode().getEntityName());
					for (JobVO eseVO : jobsByClassname) {
						lstSubNodes.add(new EventSupportTargetTreeNode(this, node, eseVO.getId(), eseVO.getName(), eseVO.getName(), eseVO.getDescription(), EventSupportTargetType.ALL_JOBS_JOB, false));
					}					
				} catch (Exception e) {
					LOG.error(e.getMessage(), e);
				}
				break;
			case ALL_GENERATIONS:
				try {
					List<GeneratorActionVO> gensByClassname = EventSupportRepository.getInstance().getGenerationsByEventSupportClassname(node.getParentNode().getEntityName());
					for (GeneratorActionVO eseVO : gensByClassname) {
						lstSubNodes.add(new EventSupportTargetTreeNode(this, node, eseVO.getId(), eseVO.getName(), eseVO.getName(), eseVO.getName(), EventSupportTargetType.ALL_GENERATIONS_GENERATION, false));
					}					
				} catch (Exception e) {
					LOG.error(e.getMessage(), e);
				}
				break;
			case EVENTSUPPORT:
				
				 try {
					 // Entites
					List<EventSupportEventVO> lsteseByClassname = EventSupportRepository.getInstance().getEventSupportEventsByClassname(node.getEntityName());
					if (lsteseByClassname.size() > 0)
						lstSubNodes.add(new EventSupportTargetTreeNode(this, node, null, strEntitaet, strEntitaet, strEntitaet, EventSupportTargetType.ALL_ENTITIES, false));
									
					// StateModels
					List<StateModelVO> lstSmVOByClassname = EventSupportRepository.getInstance().getStateModelByEventSupportClassname(node.getEntityName());
					if (lstSmVOByClassname.size() > 0) {
						lstSubNodes.add(new EventSupportTargetTreeNode(this, node, null, strStatuwechsel,strStatuwechsel, strStatuwechsel, EventSupportTargetType.ALL_STATEMODELS, false));
					}
					
					// Job
					List<JobVO> lstJobVOByClassname = EventSupportRepository.getInstance().getJobsByEventSupportClassname(node.getEntityName());
					if (lstJobVOByClassname.size() > 0) {
						lstSubNodes.add(new EventSupportTargetTreeNode(this, node, null, strTimelimits, strTimelimits,strTimelimits, EventSupportTargetType.ALL_JOBS, false));
					}
					
					// Generation
					List<GeneratorActionVO> lstGaVOByClassname = EventSupportRepository.getInstance().getGenerationsByEventSupportClassname(node.getEntityName());
					if (lstGaVOByClassname.size() > 0) {
						lstSubNodes.add(new EventSupportTargetTreeNode(this, node, null, strWorksteps, strWorksteps,strWorksteps, EventSupportTargetType.ALL_GENERATIONS, false));
					}
					
					
				} catch (RemoteException e1) {
					LOG.error(e1.getMessage(), e1);
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
			
				lstSubNodes.add(new EventSupportTargetTreeNode(this, node, nodeId, strEntitaet, strEntitaet, strEntitaet, EventSupportTargetType.ENTITY_CATEGORIE, false));
				lstSubNodes.add(new EventSupportTargetTreeNode(this, node,nodeId, strStatuwechsel, strStatuwechsel, strStatuwechsel, EventSupportTargetType.STATEMODEL_CATEGORIE, false));
				lstSubNodes.add(new EventSupportTargetTreeNode(this, node,nodeId, strTimelimits, strTimelimits,  strTimelimits, EventSupportTargetType.JOB_CATEGORIE, false));
				lstSubNodes.add(new EventSupportTargetTreeNode(this, node,nodeId, strWorksteps,strWorksteps, strWorksteps, EventSupportTargetType.GENERATION_CATEGORIE, false));
				
				break;
			case GENERATION_CATEGORIE:
				if (nodeId != null) {
					Collection<EntityObjectVO> jobsByNuclet = MasterDataDelegate.getInstance().getDependantMasterData(NuclosEntity.GENERATION.getEntityName(), "nuclet", nodeId);
					for (EntityObjectVO eoVO : jobsByNuclet) {
						lstSubNodes.add(new EventSupportTargetTreeNode(this, node,eoVO.getId(), eoVO.getField("name").toString(), eoVO.getField("name").toString(), eoVO.getField("description").toString(), EventSupportTargetType.GENERATION, false));
					}
				}
				else {
					masterData = MasterDataDelegate.getInstance().getMasterData(
							NuclosEntity.GENERATION.getEntityName(), new CollectableIsNullCondition(SearchConditionUtils.newEntityField(NuclosEntity.ENTITY.getEntityName(), "nuclet")));
					
					for (MasterDataVO msvo : masterData)
					{
						lstSubNodes.add(new EventSupportTargetTreeNode(this, node ,msvo.getId(), msvo.getField("name").toString(), msvo.getField("label").toString(), msvo.getField("description").toString(), EventSupportTargetType.GENERATION, false));
					}
				}
				break;
			case GENERATION:
				if (nodeId != null)
				{
					EventSupportSourceVO eventSupportTypeByName;
					try {
						Integer genId = Integer.parseInt(nodeId.toString());
						
						List<String> lstesTypes = EventSupportRepository.getInstance().getEventSupportTypesByGenerationId(genId);
						for (String s: lstesTypes) {
							eventSupportTypeByName = EventSupportRepository.getInstance().getEventSupportTypeByName(s);
							lstSubNodes.add(new EventSupportTargetTreeNode(this, node , nodeId, eventSupportTypeByName.getClassname(), eventSupportTypeByName.getName(), eventSupportTypeByName.getDescription(), EventSupportTargetType.GENERATION_EVENT, false));							
						}
						
					} catch (Exception e) {
						LOG.error(e.getMessage(), e);
					}
				}
				break;
			case JOB_CATEGORIE:
				if (nodeId != null) {
					Collection<EntityObjectVO> jobsByNuclet = MasterDataDelegate.getInstance().getDependantMasterData(NuclosEntity.JOBCONTROLLER.getEntityName(), "nuclet", nodeId);
					for (EntityObjectVO eoVO : jobsByNuclet) {
						lstSubNodes.add(new EventSupportTargetTreeNode(this, node,eoVO.getId(), eoVO.getField("name").toString(), eoVO.getField("name").toString(), eoVO.getField("description").toString(), EventSupportTargetType.JOB, false));
					}
				}
				else {
					masterData = MasterDataDelegate.getInstance().getMasterData(
							NuclosEntity.JOBCONTROLLER.getEntityName(), new CollectableIsNullCondition(SearchConditionUtils.newEntityField(NuclosEntity.ENTITY.getEntityName(), "nuclet")));
					
					for (MasterDataVO msvo : masterData)
					{
						lstSubNodes.add(new EventSupportTargetTreeNode(this, node ,msvo.getId(), msvo.getField("name").toString(), msvo.getField("name").toString(), msvo.getField("description").toString(), EventSupportTargetType.JOB, false));
					}
				}
				
				break;
			case JOB:
				if (nodeId != null)
				{
					EventSupportSourceVO eventSupportTypeByName;
					try {
						eventSupportTypeByName = EventSupportRepository.getInstance().getEventSupportTypeByName("org.nuclos.api.eventsupport.TimelimitSupport");
						lstSubNodes.add(new EventSupportTargetTreeNode(this, node , nodeId, eventSupportTypeByName.getName(), eventSupportTypeByName.getName(), eventSupportTypeByName.getDescription(), EventSupportTargetType.JOB_EVENT, false));
					} catch (RemoteException e) {
						LOG.error(e.getMessage(), e);
					}
				}
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
				
				Integer modelId = IdUtils.unsafeToId(nodeId);
				
				try {
					List<String> eventSupportTypesByStateModelId = EventSupportRepository.getInstance().getEventSupportTypesByStateModelId(modelId);
					
					for (String s: eventSupportTypesByStateModelId) {
						EventSupportSourceVO eventSupportTypeByName = EventSupportRepository.getInstance().getEventSupportTypeByName(s);
						lstSubNodes.add(new EventSupportTargetTreeNode(this, node, nodeId, eventSupportTypeByName.getClassname(), eventSupportTypeByName.getName(), eventSupportTypeByName.getDescription(), EventSupportTargetType.STATE_TRANSITION, false));
					}
				} catch (Exception e) {
					LOG.error(e.getMessage(), e);
				}
				
				break;
			case ENTITY:
				List<EventSupportSourceVO> eventSupportTypes;
				try {
					eventSupportTypes = EventSupportRepository.getInstance().getEventSupportTypes();
					// Only display supporttypes if there are support classes with same typ attached
					Integer iEntityId = Integer.parseInt(node.getId().toString());
					Collection<EventSupportEventVO> eventSupportsForEntity = EventSupportRepository.getInstance().getEventSupportsForEntity(iEntityId);
					
					for (EventSupportSourceVO s : eventSupportTypes)
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
			default:
				break;
			}
			
		}
		return lstSubNodes;
	}

}
