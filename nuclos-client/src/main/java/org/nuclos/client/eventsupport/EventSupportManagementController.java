package org.nuclos.client.eventsupport;

import java.rmi.RemoteException;
import java.text.Collator;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.nuclos.client.eventsupport.model.EventSupportEntityPropertiesTableModel;
import org.nuclos.client.eventsupport.model.EventSupportGenerationPropertiesTableModel;
import org.nuclos.client.eventsupport.model.EventSupportJobPropertiesTableModel;
import org.nuclos.client.eventsupport.model.EventSupportSourcePropertiesTableModel;
import org.nuclos.client.eventsupport.model.EventSupportStatePropertiesTableModel;
import org.nuclos.client.eventsupport.panel.EventSupportEntityPropertyPanel;
import org.nuclos.client.eventsupport.panel.EventSupportGenerationPropertyPanel;
import org.nuclos.client.eventsupport.panel.EventSupportJobProperyPanel;
import org.nuclos.client.eventsupport.panel.EventSupportSourceView;
import org.nuclos.client.eventsupport.panel.EventSupportStatePropertyPanel;
import org.nuclos.client.eventsupport.panel.EventSupportTargetView;
import org.nuclos.client.eventsupport.panel.EventSupportView;
import org.nuclos.client.explorer.node.eventsupport.EventSupportTargetTreeNode;
import org.nuclos.client.explorer.node.eventsupport.EventSupportTargetType;
import org.nuclos.client.explorer.node.eventsupport.EventSupportTreeNode;
import org.nuclos.client.explorer.node.rule.RuleNode;
import org.nuclos.client.main.Main;
import org.nuclos.client.main.mainframe.MainFrameTab;
import org.nuclos.client.main.mainframe.MainFrameTabbedPane;
import org.nuclos.client.masterdata.MasterDataCache;
import org.nuclos.client.masterdata.MasterDataDelegate;
import org.nuclos.client.rule.RuleCache;
import org.nuclos.client.statemodel.StateDelegate;
import org.nuclos.client.ui.Controller;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.SearchConditionUtils;
import org.nuclos.common.collect.collectable.searchcondition.CollectableIsNullCondition;
import org.nuclos.common.dal.vo.EntityObjectVO;
import org.nuclos.common2.IdUtils;
import org.nuclos.common2.TruncatableCollection;
import org.nuclos.server.customcode.valueobject.CodeVO;
import org.nuclos.server.eventsupport.valueobject.EventSupportEventVO;
import org.nuclos.server.eventsupport.valueobject.EventSupportGenerationVO;
import org.nuclos.server.eventsupport.valueobject.EventSupportJobVO;
import org.nuclos.server.eventsupport.valueobject.EventSupportSourceVO;
import org.nuclos.server.eventsupport.valueobject.EventSupportTransitionVO;
import org.nuclos.server.eventsupport.valueobject.EventSupportTypeVO;
import org.nuclos.server.eventsupport.valueobject.ProcessVO;
import org.nuclos.server.genericobject.valueobject.GeneratorActionVO;
import org.nuclos.server.job.valueobject.JobVO;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;
import org.nuclos.server.navigation.treenode.TreeNode;
import org.nuclos.server.ruleengine.valueobject.RuleVO;
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
	private static final Collator textCollator = Collator.getInstance();
	
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

	public void showSourceSupportProperties(EventSupportTreeNode node)
	{
		try {
			switch (node.getTreeNodeType())
			{
				case EVENTSUPPORT:
					EventSupportSourceVO essVO = 
						EventSupportRepository.getInstance().getEventSupportByClassname(node.getEntityName());
					loadProperty(essVO.getName(), essVO.getDescription(), essVO.getPackage(), essVO.getClassname(), essVO.getDateOfCompilation(), node, false);
					break;
				case EVENTSUPPORT_TYPE:
					EventSupportTypeVO estVO = 
						EventSupportRepository.getInstance().getEventSupportTypeByName(node.getEntityName());
					loadProperty(estVO.getName(), estVO.getDescription(), estVO.getPackage(), estVO.getClassname(), estVO.getDateOfCompilation(), node, false);
					
					break;
				case RULE:
					loadPropertyRule(RuleCache.getInstance().get(IdUtils.unsafeToId(node.getId())));
					break;
				default:
					viewEventSupportManagement.getSourceViewPanel().showSourcePropertyPanel(null);
					break;
			}			
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}	
	}
	
	private void loadPropertyRule(RuleVO ruleVO) {
		
		EventSupportSourceView sourceViewPanel = viewEventSupportManagement.getSourceViewPanel();
		EventSupportSourcePropertiesTableModel propertyModel = (EventSupportSourcePropertiesTableModel) 
				sourceViewPanel.getSourcePropertiesPanel().getPropertyTable().getModel();
		
		propertyModel.clear();
		
		if (ruleVO != null) {
			DateFormat dateInstance = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
			
			propertyModel.addEntry(EventSupportSourcePropertiesTableModel.ELM_ES_NAME, ruleVO.getRule());
			propertyModel.addEntry(EventSupportSourcePropertiesTableModel.ELM_ES_DESCRIPTION, ruleVO.getDescription());
			propertyModel.addEntry(EventSupportSourcePropertiesTableModel.ELM_ES_TYPE, getSpringLocaleDelegate().getMessage("nuclos.entity.rule.label","Geschäftsregel"));
			
			try {
				String sNuclet = null;
				String sPackage = null;
				
				if (ruleVO.getNucletId() != null) {
					MasterDataVO masterDataVO = MasterDataDelegate.getInstance().get(NuclosEntity.NUCLET.getEntityName(), ruleVO.getNucletId());
					sNuclet = masterDataVO.getField("name").toString();
					sPackage = masterDataVO.getField("package").toString();	
				}
				propertyModel.addEntry(EventSupportSourcePropertiesTableModel.ELM_ES_NUCLET, sNuclet);
				propertyModel.addEntry(EventSupportSourcePropertiesTableModel.ELM_ES_PATH, sPackage);
			} catch (Exception e) {
				LOG.error(e.getMessage(), e);
			}
			
			propertyModel.addEntry(EventSupportSourcePropertiesTableModel.ELM_ES_CREATION_DATE, 
					ruleVO.getCreatedAt() != null ? dateInstance.format(ruleVO.getCreatedAt()) : null);
			
			// Reload after chaning data 
			sourceViewPanel.showSourcePropertyPanel(sourceViewPanel.getSourcePropertiesPanel());
		}
	}

	private void loadProperty(String sName, String sDescription, String sPackage, String sClassname, Date dCompilation, EventSupportTreeNode node, boolean isType) {
		EventSupportSourceView sourceViewPanel = viewEventSupportManagement.getSourceViewPanel();
		EventSupportSourcePropertiesTableModel propertyModel = (EventSupportSourcePropertiesTableModel) 
				sourceViewPanel.getSourcePropertiesPanel().getPropertyTable().getModel();
		
		propertyModel.clear();
		
		
			DateFormat dateInstance = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
			String sNuclet = null;
			try {
				Collection<MasterDataVO> masterData = MasterDataDelegate.getInstance().getMasterData(NuclosEntity.NUCLET.getEntityName());
				for (MasterDataVO mdVO : masterData) {
					String sNucletPackage = mdVO.getField("package", String.class);
					if (sPackage.contains(sNucletPackage)) {
						sNuclet = mdVO.getField("name", String.class);
						break;
					}
				}				
			} catch (Exception e) {
				LOG.error(e.getMessage(), e);
			}
			String sEventName = sName;
			String sEventDesc = sDescription;
			
			if (isType) {
				sEventName = getSpringLocaleDelegate().getMessage(sEventName, sEventName);
				sEventDesc = getSpringLocaleDelegate().getMessage(sEventDesc, sEventDesc);
			}
			
			propertyModel.addEntry(EventSupportSourcePropertiesTableModel.ELM_ES_NAME, sEventName);
			propertyModel.addEntry(EventSupportSourcePropertiesTableModel.ELM_ES_DESCRIPTION, sEventDesc);
			propertyModel.addEntry(EventSupportSourcePropertiesTableModel.ELM_ES_TYPE,sClassname);
			propertyModel.addEntry(EventSupportSourcePropertiesTableModel.ELM_ES_NUCLET, sNuclet);
			propertyModel.addEntry(EventSupportSourcePropertiesTableModel.ELM_ES_PATH, sPackage);
			propertyModel.addEntry(EventSupportSourcePropertiesTableModel.ELM_ES_CREATION_DATE, 
					dCompilation != null ? dateInstance.format(dCompilation) : null);
			
			// Reload after chaning data 
			sourceViewPanel.showSourcePropertyPanel(sourceViewPanel.getSourcePropertiesPanel());
		
	}
	
	public void showTargetStateSupportPropertiesNotModified(final EventSupportTreeNode node) {
		
		EventSupportTargetView targetViewPanel = viewEventSupportManagement.getTargetViewPanel();
		EventSupportStatePropertyPanel statePropertiesPanel = 
				targetViewPanel.getStatePropertiesPanel();
		
		final EventSupportStatePropertiesTableModel targetStateModel = 
				(EventSupportStatePropertiesTableModel) statePropertiesPanel.getPropertyTable().getModel();
		
		Integer iStatemodelId = IdUtils.unsafeToId(node.getId());
		
		try {
			List<StateTransitionVO> lstTranses = StateDelegate.getInstance().getOrderedStateTransitionsByStatemodel(iStatemodelId);
			List<EventSupportTransitionVO> lstSupportsForTransition = EventSupportRepository.getInstance().getEventSupportsByStateModelId(iStatemodelId);
			
			targetStateModel.clear();
			targetStateModel.addTransitions(lstTranses);
			
			String sEventSupportType = node.getEntityName();
			
			for(EventSupportTransitionVO estVO : lstSupportsForTransition) {
				if (sEventSupportType.equals(estVO.getEventSupportClassType()))
					targetStateModel.addEntry(estVO);
			}
			
			// Reload after changing data
			targetViewPanel.showPropertyPanel(targetViewPanel.getStatePropertiesPanel());
			
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}
		
	}
	
	public void showTargetGenerationSupportPropertiesNotModified(final EventSupportTreeNode node) {
		
		EventSupportTargetView targetViewPanel = viewEventSupportManagement.getTargetViewPanel();
		EventSupportGenerationPropertyPanel genPropertiesPanel = 
				targetViewPanel.getGenerationPropertiesPanel();
		
		final EventSupportGenerationPropertiesTableModel targetGenerationModel = 
				(EventSupportGenerationPropertiesTableModel) genPropertiesPanel.getPropertyTable().getModel();
			
		Collection<EventSupportGenerationVO> lstEseGVOs;
		String sEventSupportType = node.getEntityName();
		
		try {
			
			lstEseGVOs = EventSupportRepository.getInstance().getEventSupportsByGenerationId((Integer) node.getId());
			targetGenerationModel.clear();
			
			for (EventSupportGenerationVO esgVO : lstEseGVOs) {
				if (sEventSupportType.equals(esgVO.getEventSupportClassType()))
					 targetGenerationModel.addEntry(esgVO);
			}
			
			// Reload after changing data
			targetViewPanel.showPropertyPanel(targetViewPanel.getGenerationPropertiesPanel());
			
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}
	}
	
	public void showTargetJobSupportPropertiesNotModified (final EventSupportTreeNode node) {
		
		EventSupportTargetView targetViewPanel = viewEventSupportManagement.getTargetViewPanel();
		EventSupportJobProperyPanel jobPropertiesPanel = 
				targetViewPanel.getJobPropertiesPanel();
		
		final EventSupportJobPropertiesTableModel targetJobModel = 
				(EventSupportJobPropertiesTableModel) jobPropertiesPanel.getPropertyTable().getModel();
		
		String sEventSupportType = node.getEntityName();
		
		targetJobModel.clear();
		
		try {
			if (node.getParentNode().getTreeNodeType().equals(EventSupportTargetType.JOB))
			{
				Collection<EventSupportJobVO> eventSupportsForJob = 
						EventSupportRepository.getInstance().getEventSupportsForJob(IdUtils.unsafeToId(node.getParentNode().getId()));
				
				for (EventSupportJobVO esjVO : eventSupportsForJob) {
					if (sEventSupportType.equals(esjVO.getEventSupportClassType()))
						targetJobModel.addEntry(esjVO);
				}
				
				// Reload after chaning data 
				targetViewPanel.showPropertyPanel(targetViewPanel.getJobPropertiesPanel());
				
			}			
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}
	}
	
	public void showTargetEntitySupportPropertiesNotModified(final EventSupportTreeNode node) {
		
		EventSupportTargetView targetViewPanel = viewEventSupportManagement.getTargetViewPanel();
		EventSupportEntityPropertyPanel entityPropertiesPanel = 
				targetViewPanel.getEntityPropertiesPanel();
		
		final EventSupportEntityPropertiesTableModel targetEntityModel = 
				(EventSupportEntityPropertiesTableModel) entityPropertiesPanel.getPropertyTable().getModel();
		
		targetEntityModel.clear();
		
		try {
			if (node.getParentNode().getTreeNodeType().equals(EventSupportTargetType.ENTITY))
			{	
			    Integer iEntityId = IdUtils.unsafeToId(node.getParentNode().getId());
				Collection<EventSupportEventVO> eventSupportsForEntity = 
						EventSupportRepository.getInstance().getEventSupportsForEntity(iEntityId);
				
				for (EventSupportEventVO esevo : eventSupportsForEntity)
				{
					if (esevo.getEventSupportClassType().equals(node.getEntityName()))
					{
						targetEntityModel.addEntry(esevo);
					}
				}
				
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
				
				targetViewPanel.showPropertyPanel(targetViewPanel.getEntityPropertiesPanel());
				
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
					viewEventSupportManagement.getTargetViewPanel().showPropertyPanel(null);
					break;
			}
	}
	
	public void showManagementPane(MainFrameTabbedPane desktopPane)
	{
		
		
		// Explorer Panel including Toolbar and tree for eventsupports
		final EventSupportTreeNode treenodeRoot = new EventSupportTreeNode(this, null, null,
				null, null, null,
				EventSupportTargetType.ROOT, true,null);
		
		final EventSupportTargetTreeNode treenodeRootTargets = new EventSupportTargetTreeNode(this, null, null, 
				getSpringLocaleDelegate().getMessage("ExplorerController.32","Regelzuweisung"),
				getSpringLocaleDelegate().getMessage("ExplorerController.32","Regelzuweisung"), 
				getSpringLocaleDelegate().getMessage("ExplorerController.32","Regelzuweisung"), 
				EventSupportTargetType.ROOT, false,null);
		
		
		if (viewEventSupportManagement == null) {
			
			viewEventSupportManagement = new EventSupportView(treenodeRoot, treenodeRootTargets);
			
			this.esActionHandler = 
					new EventSupportActionHandler(viewEventSupportManagement);
			
			viewEventSupportManagement.setActionMap(this.esActionHandler.getActionMaps());
			
			viewEventSupportManagement.showGui();			
		}
		
		ifrm = Main.getInstance().getMainController().newMainFrameTab(null, 
				getSpringLocaleDelegate().getMessage("nuclos.entity.eventsupportmangagement.label", "Nucleus Entit\u00e4tenwizard <Neue Entit\u00e4t>"));
	
		ifrm.add(viewEventSupportManagement);
		
		ifrm.setTabIconFromSystem("getIconTree16");
		ifrm.setTitle(getSpringLocaleDelegate().getMessage("nuclos.entity.eventsupportmangagement.label", "Nucleus Entit\u00e4tenwizard <Neue Entit\u00e4t>"));
		
		desktopPane.add(ifrm);
		ifrm.setVisible(true);
	}

	public List<TreeNode> createSubNodesByType(EventSupportTreeNode esNode, String sSearchText)
	{
		List<TreeNode> retVal = new ArrayList<TreeNode>();
		
		try {
			List<EventSupportTypeVO> eventSupportTypes;
			
			switch (esNode.getTreeNodeType()) 
			{
				case ROOT:
						retVal.add(new EventSupportTreeNode(this, esNode, null, getSpringLocaleDelegate().getMessage("ExplorerController.33","Regelbibliothek"), getSpringLocaleDelegate().getMessage("ExplorerController.33","Regelbibliothek"), getSpringLocaleDelegate().getMessage("ExplorerController.33","Regelbibliothek"), EventSupportTargetType.NUCLETS, true, sSearchText));
						retVal.add(new EventSupportTreeNode(this, esNode, null, "Altes Regelwerk", "Altes Regelwerk", "Altes Regelwerk", EventSupportTargetType.ALL_RULES, true,sSearchText));
					break;
				case ALL_RULES:
					Collection<RuleVO> allRules = RuleCache.getInstance().getAllRules();
					for (RuleVO rVO: allRules) {
						EventSupportTreeNode eventSupportTreeNode = new EventSupportTreeNode(this, esNode, rVO.getId(), rVO.getRule(), rVO.getRule(), rVO.getDescription(), EventSupportTargetType.RULE, false, sSearchText);
						retVal.add(eventSupportTreeNode);
					}
					break;
				case NUCLETS:
					// Default Node for all nuclos elements that are not attached to a nuclet
					if (EventSupportRepository.getInstance().getEventSupportSourcesByPackage("<Default>", sSearchText).size() > 0) 
						retVal.add(new EventSupportTreeNode(this, esNode, null, "<Default>", "<Default>", "Alle nicht zugewiesenen Elemente", EventSupportTargetType.NUCLET, false,sSearchText));
					// Nuclet Nodes
					try {
						List<MasterDataVO> masterData = MasterDataCache.getInstance().get(NuclosEntity.NUCLET.getEntityName());
//						Collection<MasterDataVO> masterData = MasterDataDelegate.getInstance().getMasterData(NuclosEntity.NUCLET.getEntityName());
						for (MasterDataVO msvo : masterData)
						{
							if (EventSupportRepository.getInstance().getEventSupportSourcesByPackage(msvo.getField("package").toString(), sSearchText).size() > 0)
								retVal.add(new EventSupportTreeNode(this, esNode, msvo.getId(), msvo.getField("package").toString(), msvo.getField("name").toString(), msvo.getField("description").toString(), EventSupportTargetType.NUCLET, false,sSearchText));
						}
					} catch (Exception e) {
						LOG.error(e.getMessage(), e);
					}
					break;
				case NUCLET:	
					eventSupportTypes = EventSupportRepository.getInstance().getEventSupportTypes();
					try {
						for (EventSupportTypeVO s : eventSupportTypes) {
							for (EventSupportSourceVO eseVO : EventSupportRepository.getInstance().getEventSupportSourcesByPackage(esNode.getEntityName(), sSearchText)) {
								if (eseVO.getInterface().contains(s.getClassname())) {
									EventSupportTreeNode eventSupportTreeNode = new EventSupportTreeNode(this, esNode, null, s.getClassname(), s.getName(),s.getDescription(), EventSupportTargetType.EVENTSUPPORT_TYPE, false,sSearchText);
									retVal.add(eventSupportTreeNode);
									break;
								}
							}
						}
					} catch (Exception e) {
						LOG.error(e.getMessage(), e);
					}
					break;
				case EVENTSUPPORT_TYPE:
					List<EventSupportSourceVO> eventSupport = EventSupportRepository.getInstance().getEventSupportsByType(esNode.getEntityName());
					try {
						List<EventSupportSourceVO> lstSupportsOfNuclet = 
								EventSupportRepository.getInstance().getEventSupportSourcesByPackage(esNode.getParentNode().getEntityName(),sSearchText);
					
						for (EventSupportSourceVO s : eventSupport)
						{
							if (lstSupportsOfNuclet.contains(s)) {
								EventSupportTreeNode eventSupportTreeNode = new EventSupportTreeNode(this, esNode, null, s.getClassname(), s.getName(), s.getDescription(), EventSupportTargetType.EVENTSUPPORT, false,sSearchText);
								if (sSearchText == null || containsIgnorCaseSensifity(s.getName(), sSearchText)) {
									retVal.add(eventSupportTreeNode); 																	
								}
							}
						}							
					} catch (Exception e) {
						LOG.error(e.getMessage(), e);
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
			String  sSupportClassType = sourceNode.getParentNode().getEntityName();
			String  sDescription = sourceNode.getDescription();
			Integer iJobControllerId = IdUtils.unsafeToId(targetNode.getId());
			Integer iOrder = targetNode.getSubNodes().size() + 1;
			try {
				Collection<EventSupportJobVO> eventSupportsForJob = EventSupportRepository.getInstance().getEventSupportsForJob(iJobControllerId);
				iOrder = eventSupportsForJob != null ? eventSupportsForJob.size() + 1: 1;
				retVal = EventSupportDelegate.getInstance().createEventSupportJob(
						new EventSupportJobVO(sDescription,sSupportClass, sSupportClassType, iOrder, iJobControllerId));
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
				Integer genId = IdUtils.unsafeToId(targetNode.getId());
				String eventsupportClassType = sourceNode.getParentNode().getEntityName();
				
				int newOrder = 1;
						
				Collection<EventSupportGenerationVO> lsteseg = EventSupportRepository.getInstance().getEventSupportsForGeneration(genId);
				
				for (EventSupportGenerationVO esgVO : lsteseg) {
					if (esgVO.getEventSupportClassType().equals(eventsupportClassType))
						newOrder++;
				}
				
				Boolean bRunAfterwards = Boolean.FALSE;
				if ("org.nuclos.api.eventsupport.GenerateFinalSupport".equals(eventsupportClassType)) {
					bRunAfterwards = Boolean.TRUE;
				}
				
				String  sSupportClass = sourceNode.getEntityName();
				String  sSupportClassType = sourceNode.getParentNode().getEntityName();
				Integer iGenerationId = IdUtils.unsafeToId(targetNode.getId());
				Integer iOrder = newOrder;
				
				retVal = EventSupportDelegate.getInstance().createEventSupportGeneration(
						new EventSupportGenerationVO(iOrder,iGenerationId,sSupportClass, sSupportClassType, bRunAfterwards));
								
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
				String sSupportInterface = sourceNode.getParentNode().getEntityName();
				Integer iModuleId = IdUtils.unsafeToId(targetNode.getId());
				
				Boolean bRunAfterwards = true;
				
				List<EventSupportTransitionVO> stateTransitionsBySupportType = 
						EventSupportDelegate.getInstance().getStateTransitionsBySupportType(iModuleId, sSupportInterface);
				
				List<StateTransitionVO> orderedStateTransitionsByStatemodel = StateDelegate.getInstance().getOrderedStateTransitionsByStatemodel(iModuleId);
				
				Integer iOrder = stateTransitionsBySupportType.size() + 1;
				Integer iTransId =  orderedStateTransitionsByStatemodel.size() > 0 ? orderedStateTransitionsByStatemodel.get(0).getId() : 0;
				
				retVal = EventSupportDelegate.getInstance().createEventSupportTransition(
						new EventSupportTransitionVO(sSupportClass,sSupportInterface, iTransId, iOrder, bRunAfterwards));
				
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

			Collection<EventSupportEventVO> eventSupportsForEntity = 
					EventSupportDelegate.getInstance().getEventSupportsForEntity(IdUtils.unsafeToId(targetNode.getId()));
			for (EventSupportEventVO eseVO : eventSupportsForEntity) {
				if (sourceNode.getParentNode().getEntityName().equals(eseVO.getEventSupportClassType()))
						idx++;
			}
			
			Integer entId = IdUtils.unsafeToId(targetNode.getId());
			
			EventSupportEventVO eseVO = 
					new EventSupportEventVO(sEventSupportClassname, sEventSupportClassType,
											entId,null, null, ++idx, null, null, null);
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
	
	public List<TreeNode> createTargetSubNodesByType(EventSupportTargetTreeNode esNode, String sSearchText)
	{
		EventSupportTargetTreeNode node = (EventSupportTargetTreeNode) esNode;
		List<TreeNode> lstSubNodes = new ArrayList<TreeNode> ();
		EventSupportTargetType type = node.getTreeNodeType();
		Object nodeId = node.getId();
		
		String strStatechange = getSpringLocaleDelegate().getMessage("RuleNode.12","Statuswechsel");
		String strTimelimits = getSpringLocaleDelegate().getMessage("RuleNode.3","Fristen");
		String strWorksteps = getSpringLocaleDelegate().getMessage("RuleNode.7","Arbeitsschritt");
		String strEntity = getSpringLocaleDelegate().getMessage("nuclos.entity.entity.label","Entität");
		String strAllRules = getSpringLocaleDelegate().getMessage("DirectoryRuleNode.1","All Regeln");
		
		
		if (type != null) {
			switch (type) {
			case ROOT:
				try {
					List<MasterDataVO> masterData = MasterDataCache.getInstance().get(NuclosEntity.NUCLET.getEntityName());
					lstSubNodes.add(new EventSupportTargetTreeNode(this, node, null, strAllRules, strAllRules, strAllRules, EventSupportTargetType.ALL_EVENTSUPPORTS, false,sSearchText));
					if (EventSupportRepository.getInstance().getEventSupportSourcesByPackage("<Default>", sSearchText).size() > 0) 
						lstSubNodes.add(new EventSupportTargetTreeNode(this, node, null, "<Default>", "<Default>", "Alle nicht zugewiesenen Elemente", EventSupportTargetType.NUCLET, false,sSearchText));
					
					for (MasterDataVO msvo : masterData)
					{
						lstSubNodes.add(new EventSupportTargetTreeNode(this, node, msvo.getId(), msvo.getField("package").toString(), msvo.getField("name").toString(), msvo.getField("description").toString(), EventSupportTargetType.NUCLET, false,sSearchText));
					}					
				} catch (Exception e) {
					LOG.error(e.getMessage(), e);
				}
				break;
			case ALL_EVENTSUPPORTS:
				try {
					List<EventSupportSourceVO> allEventSupports = EventSupportRepository.getInstance().getAllEventSupports();
					Collections.sort(allEventSupports, new Comparator<EventSupportSourceVO>() {
						@Override
						public int compare(EventSupportSourceVO o1, EventSupportSourceVO o2) {
							return textCollator.compare(o1.getName(), o2.getName());
						}
					});
					
					for (EventSupportSourceVO eseVO : EventSupportRepository.getInstance().getAllEventSupports()) {
						
						if (sSearchText == null ||  containsIgnorCaseSensifity(eseVO.getName(), sSearchText)) {
							lstSubNodes.add(new EventSupportTargetTreeNode(this, node, eseVO.getId(), eseVO.getClassname(), eseVO.getName(), eseVO.getDescription(), EventSupportTargetType.EVENTSUPPORT, false,sSearchText));					
						}
					}	
					
					// Add all eventupports
					final List<RuleVO> sortedRules = new ArrayList<RuleVO>(RuleCache.getInstance().getAllRules());
					Collections.sort(sortedRules, new Comparator<RuleVO>() {
						@Override
						public int compare(RuleVO o1, RuleVO o2) {
							return textCollator.compare(o1.getRule(), o2.getRule());
						}
					});

					// add additionally all old rules
					final Collection<CodeVO> codes = RuleCache.getInstance().getAllCodes();
					for (RuleVO curRule : sortedRules) {
						if (sSearchText == null || containsIgnorCaseSensifity(curRule.getRule(), sSearchText)) 
							lstSubNodes.add(new RuleNode(curRule, true, (codes != null && codes.size() > 0)));
					}

				} catch (Exception e) {
					LOG.error(e.getMessage(), e);
				}
				break;
			case ALL_ENTITIES:
				try {
					List<EventSupportEventVO> eventSupportEventsByClassname = EventSupportRepository.getInstance().getEventSupportEntitiesByClassname(node.getParentNode().getEntityName(), null);
					for (EventSupportEventVO eseVO : eventSupportEventsByClassname) {
						if (sSearchText == null || containsIgnorCaseSensifity(eseVO.getEntityName(), sSearchText)) 
							lstSubNodes.add(new EventSupportTargetTreeNode(this, node, eseVO.getId(), eseVO.getEntityName(), eseVO.getEntityName(), eseVO.getEntityName(), EventSupportTargetType.ALL_ENTITIES_ENTITY, false,sSearchText));
					}					
				} catch (Exception ex) {
					LOG.error(ex.getMessage(), ex);
				}
				break;
			case ALL_STATEMODELS:
				try {
					List<StateModelVO> stateModelByEventSupportClassname = EventSupportRepository.getInstance().getStateModelByEventSupportClassname(node.getParentNode().getEntityName());
					for (StateModelVO eseVO : stateModelByEventSupportClassname) {
						lstSubNodes.add(new EventSupportTargetTreeNode(this, node, eseVO.getId(), eseVO.getName(), eseVO.getName(), eseVO.getDescription(), EventSupportTargetType.ALL_STATEMODELS_STATEMODEL, false,sSearchText));
					}					
				} catch (Exception e) {
					LOG.error(e.getMessage(), e);
				}
				break;
			case ALL_JOBS:
				try {
					List<JobVO> jobsByClassname = EventSupportRepository.getInstance().getJobsByEventSupportClassname(node.getParentNode().getEntityName());
					for (JobVO eseVO : jobsByClassname) {
						lstSubNodes.add(new EventSupportTargetTreeNode(this, node, eseVO.getId(), eseVO.getName(), eseVO.getName(), eseVO.getDescription(), EventSupportTargetType.ALL_JOBS_JOB, false,sSearchText));
					}					
				} catch (Exception e) {
					LOG.error(e.getMessage(), e);
				}
				break;
			case ALL_GENERATIONS:
				try {
					List<GeneratorActionVO> gensByClassname = EventSupportRepository.getInstance().getGenerationsByEventSupportClassname(node.getParentNode().getEntityName());
					for (GeneratorActionVO eseVO : gensByClassname) {
						lstSubNodes.add(new EventSupportTargetTreeNode(this, node, eseVO.getId(), eseVO.getName(), eseVO.getName(), eseVO.getName(), EventSupportTargetType.ALL_GENERATIONS_GENERATION, false,sSearchText));
					}					
				} catch (Exception e) {
					LOG.error(e.getMessage(), e);
				}
				break;
			case EVENTSUPPORT:
				
				 try {
					 // Entites
					List<EventSupportEventVO> lsteseByClassname = EventSupportRepository.getInstance().getEventSupportEntitiesByClassname(node.getEntityName(), null);
					if (lsteseByClassname.size() > 0 )
						lstSubNodes.add(new EventSupportTargetTreeNode(this, node, null, strEntity, strEntity, strEntity, EventSupportTargetType.ALL_ENTITIES, false,sSearchText));
									
					// StateModels
					List<StateModelVO> lstSmVOByClassname = EventSupportRepository.getInstance().getStateModelByEventSupportClassname(node.getEntityName());
					if (lstSmVOByClassname.size() > 0) {
						lstSubNodes.add(new EventSupportTargetTreeNode(this, node, null, strStatechange,strStatechange, strStatechange, EventSupportTargetType.ALL_STATEMODELS, false,sSearchText));
					}
					
					// Job
					List<JobVO> lstJobVOByClassname = EventSupportRepository.getInstance().getJobsByEventSupportClassname(node.getEntityName());
					if (lstJobVOByClassname.size() > 0) {
						lstSubNodes.add(new EventSupportTargetTreeNode(this, node, null, strTimelimits, strTimelimits,strTimelimits, EventSupportTargetType.ALL_JOBS, false,sSearchText));
					}
					
					// Generation
					List<GeneratorActionVO> lstGaVOByClassname = EventSupportRepository.getInstance().getGenerationsByEventSupportClassname(node.getEntityName());
					if (lstGaVOByClassname.size() > 0) {
						lstSubNodes.add(new EventSupportTargetTreeNode(this, node, null, strWorksteps, strWorksteps,strWorksteps, EventSupportTargetType.ALL_GENERATIONS, false,sSearchText));
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
						if (sSearchText == null || containsIgnorCaseSensifity(eoVO.getField("entity").toString(), sSearchText))
							lstSubNodes.add(new EventSupportTargetTreeNode(this, node, eoVO.getId(), eoVO.getField("entity").toString(), eoVO.getField("entity").toString(), eoVO.getField("entity").toString(), EventSupportTargetType.ENTITY, false,sSearchText));
					}
				}
				else
				{
					TruncatableCollection<MasterDataVO> lstSupportsforNuclet = MasterDataDelegate.getInstance().getMasterData(
							NuclosEntity.ENTITY.getEntityName(), new CollectableIsNullCondition(SearchConditionUtils.newEntityField(NuclosEntity.ENTITY.getEntityName(), "nuclet")));
					
					for (MasterDataVO msvo : lstSupportsforNuclet)
					{
						if (sSearchText == null || containsIgnorCaseSensifity(msvo.getField("entity").toString(), sSearchText)) 
							lstSubNodes.add(new EventSupportTargetTreeNode(this, node ,msvo.getId(), msvo.getField("entity").toString(), msvo.getField("entity").toString(), msvo.getField("entity").toString(), EventSupportTargetType.ENTITY, false,sSearchText));
					}
				}
				
				break;
			case NUCLET: 
			
					try {
						if (sSearchText == null || (sSearchText != null && EventSupportRepository.getInstance().getEntitiesByUsage(node.getEntityName()).size() > 0))
							lstSubNodes.add(new EventSupportTargetTreeNode(this, node, nodeId, strEntity, strEntity, strEntity, EventSupportTargetType.ENTITY_CATEGORIE, false,sSearchText));
						if (sSearchText == null || (sSearchText != null && EventSupportRepository.getInstance().getTransitionsByUsage(node.getEntityName()).size() > 0))
							lstSubNodes.add(new EventSupportTargetTreeNode(this, node,nodeId, strStatechange, strStatechange, strStatechange, EventSupportTargetType.STATEMODEL_CATEGORIE, false,sSearchText));
						if (sSearchText == null || (sSearchText != null && EventSupportRepository.getInstance().getGenerationsByUsage(node.getEntityName()).size() > 0))
							lstSubNodes.add(new EventSupportTargetTreeNode(this, node,nodeId, strWorksteps,strWorksteps, strWorksteps, EventSupportTargetType.GENERATION_CATEGORIE, false,sSearchText));
						if (sSearchText == null || (sSearchText != null && EventSupportRepository.getInstance().getJobsByUsage(node.getEntityName()).size() > 0))
							lstSubNodes.add(new EventSupportTargetTreeNode(this, node,nodeId, strTimelimits, strTimelimits,  strTimelimits, EventSupportTargetType.JOB_CATEGORIE, false,sSearchText));
						
					} catch (Exception e) {
						LOG.error(e.getMessage(), e);
					}
					
				break;
			case GENERATION_CATEGORIE:
				if (nodeId != null) {
					Collection<EntityObjectVO> jobsByNuclet = MasterDataDelegate.getInstance().getDependantMasterData(NuclosEntity.GENERATION.getEntityName(), "nuclet", nodeId);
					for (EntityObjectVO eoVO : jobsByNuclet) {
						if (sSearchText == null || containsIgnorCaseSensifity(eoVO.getField("entity").toString(), sSearchText)) 
							lstSubNodes.add(new EventSupportTargetTreeNode(this, node,eoVO.getId(), eoVO.getField("entity").toString(), eoVO.getField("entity").toString(), eoVO.getField("entity").toString(), EventSupportTargetType.GENERATION, false,sSearchText));
					}
				}
				else {
					TruncatableCollection<MasterDataVO> masterData = MasterDataDelegate.getInstance().getMasterData(
							NuclosEntity.GENERATION.getEntityName(), new CollectableIsNullCondition(SearchConditionUtils.newEntityField(NuclosEntity.ENTITY.getEntityName(), "nuclet")));
					
					for (MasterDataVO msvo : masterData)
					{
						if (sSearchText == null || containsIgnorCaseSensifity(msvo.getField("label").toString(), sSearchText)) 
							lstSubNodes.add(new EventSupportTargetTreeNode(this, node ,msvo.getId(), msvo.getField("name").toString(), msvo.getField("label").toString(), msvo.getField("description").toString(), EventSupportTargetType.GENERATION, false,sSearchText));
					}
				}
				break;
			case GENERATION:
				if (nodeId != null)
				{
					EventSupportTypeVO eventSupportTypeByName;
					try {
						Integer genId = IdUtils.unsafeToId(nodeId);
						
						List<String> lstesTypes = EventSupportRepository.getInstance().getEventSupportTypesByGenerationId(genId);
						for (String s: lstesTypes) {
							eventSupportTypeByName = EventSupportRepository.getInstance().getEventSupportTypeByName(s);						
							lstSubNodes.add(new EventSupportTargetTreeNode(this, node , nodeId, eventSupportTypeByName.getClassname(), eventSupportTypeByName.getName(), eventSupportTypeByName.getDescription(), EventSupportTargetType.GENERATION_EVENT, false,sSearchText));
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
						if (sSearchText == null ||  containsIgnorCaseSensifity(eoVO.getField("entity").toString(), sSearchText)) 
							lstSubNodes.add(new EventSupportTargetTreeNode(this, node,eoVO.getId(), eoVO.getField("entity").toString(), eoVO.getField("entity").toString(), eoVO.getField("entity").toString(), EventSupportTargetType.JOB, false,sSearchText));
					}
				}
				else {
					TruncatableCollection<MasterDataVO> lstSupportsforNuclet = MasterDataDelegate.getInstance().getMasterData(
							NuclosEntity.JOBCONTROLLER.getEntityName(), new CollectableIsNullCondition(SearchConditionUtils.newEntityField(NuclosEntity.ENTITY.getEntityName(), "nuclet")));
					
					for (MasterDataVO msvo : lstSupportsforNuclet)
					{
						if (sSearchText == null || containsIgnorCaseSensifity(msvo.getField("name").toString(), sSearchText)) 
							lstSubNodes.add(new EventSupportTargetTreeNode(this, node ,msvo.getId(), msvo.getField("name").toString(), msvo.getField("name").toString(), msvo.getField("description").toString(), EventSupportTargetType.JOB, false,sSearchText));
					}
				}
				
				break;
			case JOB:
				if (nodeId != null)
				{
					EventSupportTypeVO eventSupportTypeByName;
					try {
						Collection<EventSupportJobVO> eventSupportsForJob = EventSupportRepository.getInstance().getEventSupportsForJob(IdUtils.unsafeToId(nodeId));
						if (eventSupportsForJob.size() > 0) {
							eventSupportTypeByName = EventSupportRepository.getInstance().getEventSupportTypeByName("org.nuclos.api.eventsupport.JobSupport");
							String sTypeName = this.getSpringLocaleDelegate().getMessage(eventSupportTypeByName.getName(), eventSupportTypeByName.getName());
							String sTypeDesc = this.getSpringLocaleDelegate().getMessage(eventSupportTypeByName.getDescription(), eventSupportTypeByName.getDescription());
							
							lstSubNodes.add(new EventSupportTargetTreeNode(this, node , nodeId, eventSupportTypeByName.getClassname(), sTypeName, sTypeDesc, EventSupportTargetType.JOB_EVENT, false,sSearchText));							
 
						}
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
						if (sSearchText == null ||  containsIgnorCaseSensifity(eoVO.getField("name").toString(), sSearchText)) {
							String sName = eoVO.getField("name") != null ? eoVO.getField("name").toString() : null;
							String sDescription = eoVO.getField("description") != null ? eoVO.getField("description").toString() : null;
							lstSubNodes.add(new EventSupportTargetTreeNode(this, node,eoVO.getId(),sName, sName, sDescription, EventSupportTargetType.STATEMODEL, false,sSearchText));
						}
					}
				}
				else
				{
					TruncatableCollection<MasterDataVO> lstSupportsforNuclet = MasterDataDelegate.getInstance().getMasterData(
							NuclosEntity.STATEMODEL.getEntityName(), new CollectableIsNullCondition(SearchConditionUtils.newEntityField(NuclosEntity.ENTITY.getEntityName(), "nuclet")));
					
					for (MasterDataVO msvo : lstSupportsforNuclet) {
						if (sSearchText == null || containsIgnorCaseSensifity(msvo.getField("name").toString(), sSearchText))  {
							String sName = msvo.getField("name") != null ? msvo.getField("name").toString() : null;
							String sDescription = msvo.getField("description") != null ? msvo.getField("description").toString() : null;
							lstSubNodes.add(new EventSupportTargetTreeNode(this, node ,msvo.getId(), sName, sName, sDescription, EventSupportTargetType.STATEMODEL, false,sSearchText));							
						}
					}
				}
				break;
			case STATEMODEL:
				try {
					List<String> eventSupportTypesByStateModelId = 
							EventSupportRepository.getInstance().getEventSupportTypesByStateModelId(IdUtils.unsafeToId(nodeId));
					
					for (String s: eventSupportTypesByStateModelId) {
						EventSupportTypeVO eventSupportTypeByName = EventSupportRepository.getInstance().getEventSupportTypeByName(s);
						lstSubNodes.add(new EventSupportTargetTreeNode(this, node, nodeId, eventSupportTypeByName.getClassname(), eventSupportTypeByName.getName(), eventSupportTypeByName.getDescription(), EventSupportTargetType.STATE_TRANSITION, false,sSearchText));
					}
				} catch (Exception e) {
					LOG.error(e.getMessage(), e);
				}
				break;
			case ENTITY:
				List<EventSupportTypeVO> eventSupportTypes;
				try {
					eventSupportTypes = EventSupportRepository.getInstance().getEventSupportTypes();
					// Only display supporttypes if there are support classes with same typ attached
					Collection<EventSupportEventVO> eventSupportsForEntity = 
							EventSupportRepository.getInstance().getEventSupportsForEntity(IdUtils.unsafeToId(node.getId()));
					
					for (EventSupportTypeVO s : eventSupportTypes)
					{
						boolean typeAlreadyInserted = false;
						for (EventSupportEventVO eseVO : eventSupportsForEntity)
						{
							if (eseVO.getEventSupportClassType().equals(s.getClassname()) && !typeAlreadyInserted)
							{
								lstSubNodes.add(new EventSupportTargetTreeNode(this, node, nodeId, s.getClassname(),s.getName(), s.getDescription(), EventSupportTargetType.EVENTSUPPORT_TYPE, false,sSearchText));									typeAlreadyInserted = true;

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

	private boolean containsIgnorCaseSensifity(String sCompleteString, String sSearchingFor) {
		boolean retVal = false;
		
		if (sCompleteString != null && sSearchingFor != null) {
			if (sCompleteString.toLowerCase().contains(sSearchingFor.toLowerCase()))
				retVal = true;
		}
		return retVal;
	}
	
	private boolean checkGenerationsBySearch(String nucletpackage, String searchString) {
		boolean retVal = false;
		try {
			List<EventSupportSourceVO> eventSupportSourcesByPackage = EventSupportRepository.getInstance().getEventSupportSourcesByPackage(nucletpackage, searchString);
			for (EventSupportSourceVO eseVO : eventSupportSourcesByPackage) {
				if (eseVO.getInterface().contains("org.nuclos.api.eventsupport.GenerateFinalSupport") || 
						eseVO.getInterface().contains("org.nuclos.api.eventsupport.GenerateSupport"))
					retVal=  true;
			}
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}
		return retVal;
	}
	
	private boolean checkEntitysBySearch(String nucletpackage, String searchString) {
		boolean retVal = false;
		try {
			List<EventSupportSourceVO> eventSupportSourcesByPackage = EventSupportRepository.getInstance().getEventSupportSourcesByPackage(nucletpackage, searchString);
			for (EventSupportSourceVO eseVO : eventSupportSourcesByPackage) {
				if (eseVO.getInterface().contains("org.nuclos.api.eventsupport.InsertFinalSupport") || 
					eseVO.getInterface().contains("org.nuclos.api.eventsupport.InsertSupport") || 
					eseVO.getInterface().contains("org.nuclos.api.eventsupport.UpdateFinalSupport") ||
					eseVO.getInterface().contains("org.nuclos.api.eventsupport.UpdateSupport") ||
					eseVO.getInterface().contains("org.nuclos.api.eventsupport.DeleteFinalSupport") ||
					eseVO.getInterface().contains("org.nuclos.api.eventsupport.DeleteSupport"))
					retVal=  true;
			}
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}
		return retVal;
	}
	
	private boolean checkStatemodelsBySearch(String nucletpackage, String searchString) {
		boolean retVal = false;
		try {
			List<EventSupportSourceVO> eventSupportSourcesByPackage = EventSupportRepository.getInstance().getEventSupportSourcesByPackage(nucletpackage, searchString);
			for (EventSupportSourceVO eseVO : eventSupportSourcesByPackage) {
				if (eseVO.getInterface().contains("org.nuclos.api.eventsupport.StateChangeSupport") || 
					eseVO.getInterface().contains("org.nuclos.api.eventsupport.StateChangeFinalSupport"))
					retVal=  true;
			}
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}
		return retVal;
	}
	
	private boolean checkJobsBySearch(String nucletpackage, String searchString) {
		boolean retVal = false;
		try {
			List<EventSupportSourceVO> eventSupportSourcesByPackage = EventSupportRepository.getInstance().getEventSupportSourcesByPackage(nucletpackage, searchString);
			for (EventSupportSourceVO eseVO : eventSupportSourcesByPackage) {
				if (eseVO.getInterface().contains("org.nuclos.api.eventsupport.TimelimitSupport") || 
					eseVO.getInterface().contains("org.nuclos.api.eventsupport.TimelimitFinalSupport"))
					retVal=  true;
			}
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}
		return retVal;
	}
	
}
