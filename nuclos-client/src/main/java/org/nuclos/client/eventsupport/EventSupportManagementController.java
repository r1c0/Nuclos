package org.nuclos.client.eventsupport;

import java.awt.event.ActionEvent;
import java.rmi.RemoteException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import org.apache.log4j.Logger;
import org.jfree.util.Log;
import org.nuclos.client.eventsupport.model.EventSupportEntityPropertiesTableModel;
import org.nuclos.client.eventsupport.model.EventSupportPropertiesTableModel;
import org.nuclos.client.eventsupport.model.EventSupportStatePropertiesTableModel;
import org.nuclos.client.eventsupport.panel.EventSupportSourceView;
import org.nuclos.client.eventsupport.panel.EventSupportView;
import org.nuclos.client.eventsupport.panel.EventSupportTargetView;
import org.nuclos.client.explorer.node.EventSupportTargetExplorerNode;
import org.nuclos.client.explorer.node.eventsupport.EventSupportTargetTreeNode;
import org.nuclos.client.explorer.node.eventsupport.EventSupportTargetType;
import org.nuclos.client.explorer.node.eventsupport.EventSupportTreeNode;
import org.nuclos.client.main.Main;
import org.nuclos.client.main.MainController;
import org.nuclos.client.main.mainframe.MainFrameTab;
import org.nuclos.client.main.mainframe.MainFrameTabbedPane;
import org.nuclos.client.masterdata.MasterDataDelegate;
import org.nuclos.client.statemodel.StateDelegate;
import org.nuclos.client.ui.Controller;
import org.nuclos.client.ui.Icons;
import org.nuclos.client.ui.OvOpAdapter;
import org.nuclos.client.ui.OverlayOptionPane;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.SearchConditionUtils;
import org.nuclos.common.collect.collectable.searchcondition.CollectableIsNullCondition;
import org.nuclos.common.dal.vo.EntityObjectVO;
import org.nuclos.common2.SpringLocaleDelegate;
import org.nuclos.common2.exception.CommonCreateException;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.common2.exception.CommonPermissionException;
import org.nuclos.common2.exception.CommonValidationException;
import org.nuclos.server.eventsupport.valueobject.EventSupportEventVO;
import org.nuclos.server.eventsupport.valueobject.EventSupportTransitionVO;
import org.nuclos.server.eventsupport.valueobject.EventSupportVO;
import org.nuclos.server.eventsupport.valueobject.ProcessVO;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;
import org.nuclos.server.ruleengine.NuclosBusinessRuleException;
import org.nuclos.server.statemodel.valueobject.StateTransitionVO;
import org.nuclos.server.statemodel.valueobject.StateVO;

public class EventSupportManagementController extends Controller<MainFrameTabbedPane> {

	private static final Logger LOG = Logger.getLogger(EventSupportManagementController.class);	
	MainFrameTab ifrm;
	
	public enum ACTIONS {
		ACTION_SAVE_EVENT, 
		ACTION_SAVE_ALL_EVENTS, 
		ACTION_DELETE_EVENT, 
		ACTION_MOVE_UP_EVENT, 
		ACTION_MOVE_DOWN_EVENT,
		ACTION_SAVE_ALL_STATETRANSITION,
		ACTION_DELETE_STATETRANSITION, 
		ACTION_MOVE_UP_STATETRANSITION, 
		ACTION_MOVE_DOWN_STATETRANSITION,
		ACTION_REFRESH_TARGETTREE, 
		ACTION_REFRESH_SOURCETREE};
		
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
	
	private EventSupportView viewEventSupportManagement;
	public final Map<ACTIONS, AbstractAction> MAP_ACTIONS = new HashMap<ACTIONS, AbstractAction>();
	
	public EventSupportManagementController(MainFrameTabbedPane pParent) {
		super(pParent);
	
		loadActions();
	}
	
	private void loadActions() {
		MAP_ACTIONS.put(ACTIONS.ACTION_SAVE_EVENT, 
				new AbstractAction("", Icons.getInstance().getIconSaveS16()) {
					@Override
					public void actionPerformed(ActionEvent actEvent) {
						EventSupportTargetView estvElement = EventSupportManagementController.this.viewEventSupportManagement.getTargetViewPanel();
						if (estvElement != null)
						{
							EventSupportEntityPropertiesTableModel tblModel = (EventSupportEntityPropertiesTableModel) 
									estvElement.getPropertyTable().getModel();
							int selectedRow = estvElement.getPropertyTable().getSelectedRow();
							
							EventSupportEventVO entryByRowIndex = tblModel.getEntryByRowIndex(selectedRow);
								
							// Modify entry
							if (entryByRowIndex.getId() != null) {
								try {
									EventSupportDelegate.getInstance().modifyEventSupportEvent(entryByRowIndex);
									
								} catch (Exception e) {
									LOG.error(e.getMessage(), e);
								}
							}
							tblModel.setModelModified(false);
						}
					}
				});
		MAP_ACTIONS.put(ACTIONS.ACTION_SAVE_ALL_EVENTS, 
				new AbstractAction("", Icons.getInstance().getIconDelete16()) {
			@Override
			public void actionPerformed(ActionEvent actEv) {
				EventSupportTargetView estvElement = EventSupportManagementController.this.viewEventSupportManagement.getTargetViewPanel();
				if (estvElement != null)
				{
					EventSupportEntityPropertiesTableModel tblModel = (EventSupportEntityPropertiesTableModel) 
							estvElement.getPropertyTable().getModel();

					for(int selectedRow = 0; selectedRow < tblModel.getRowCount(); selectedRow++){
						
						EventSupportEventVO entryByRowIndex = tblModel.getEntryByRowIndex(selectedRow);
						
						// Modify entry
						if (entryByRowIndex.getId() != null) {
							try {
								EventSupportDelegate.getInstance().modifyEventSupportEvent(entryByRowIndex);
								
							} catch (Exception e) {
								LOG.error(e.getMessage(), e);
							}
						}
					}
				
					tblModel.setModelModified(false);
				}
			}
		});
		MAP_ACTIONS.put(ACTIONS.ACTION_SAVE_ALL_STATETRANSITION, 
				new AbstractAction("", Icons.getInstance().getIconDelete16()) {
			@Override
			public void actionPerformed(ActionEvent actEv) {
				EventSupportTargetView estvElement = EventSupportManagementController.this.viewEventSupportManagement.getTargetViewPanel();
				if (estvElement != null)
				{
					EventSupportStatePropertiesTableModel  tblModel = (EventSupportStatePropertiesTableModel) 
							estvElement.getPropertyTable().getModel();
					
					for(int selectedRow = 0; selectedRow < tblModel.getRowCount(); selectedRow++){
						
						EventSupportTransitionVO entryByRowIndex = tblModel.getEntryByRowIndex(selectedRow);
						
						// Modify entry
						if (entryByRowIndex.getId() != null) {
							try {
								EventSupportDelegate.getInstance().modifyEventSupportTransition(entryByRowIndex);
								
							} catch (Exception e) {
								LOG.error(e.getMessage(), e);
							}
						}
					}

					tblModel.setModelModified(false);
				}
			}
		});
		MAP_ACTIONS.put(ACTIONS.ACTION_DELETE_STATETRANSITION, 
				new AbstractAction("", Icons.getInstance().getIconDelete16()) {
			@Override
			public void actionPerformed(ActionEvent actEv) {
				EventSupportTargetView  estvElement = EventSupportManagementController.this.viewEventSupportManagement.getTargetViewPanel();
				if (estvElement != null)
				{
					EventSupportStatePropertiesTableModel tblModel = (EventSupportStatePropertiesTableModel)	 
							estvElement.getPropertyTable().getModel();
					
					int selectedRow = estvElement.getPropertyTable().getSelectedRow();
					EventSupportTransitionVO entryByRowIndex = tblModel.getEntryByRowIndex(selectedRow);
					
					// Delete element
					if (entryByRowIndex.getId() != null) {
						EventSupportDelegate.getInstance().deleteEventSupportTransition(entryByRowIndex);
					}
					
					// remove it from tablemodel
					tblModel.removeEntry(selectedRow);
					
					// and refresh new order and store it in db
					for (int idx = selectedRow; idx < tblModel.getRowCount(); idx++) {
						EventSupportDelegate.getInstance().modifyEventSupportTransition(tblModel.getEntryByRowIndex(idx));
					}
					
					estvElement.getPropertyTable().repaint();
					estvElement.getPropertyTable().revalidate();
				}
			}
		});
		MAP_ACTIONS.put(ACTIONS.ACTION_MOVE_DOWN_STATETRANSITION, 
				new AbstractAction("", Icons.getInstance().getIconDown16()) {
			@Override
			public void actionPerformed(ActionEvent actEv) {
				EventSupportTargetView estvElement = EventSupportManagementController.this.viewEventSupportManagement.getTargetViewPanel();
				if (estvElement != null)
				{
					EventSupportStatePropertiesTableModel tblModel = (EventSupportStatePropertiesTableModel) 
							estvElement.getPropertyTable().getModel();
					
					int selectedRow = estvElement.getPropertyTable().getSelectedRow();
					tblModel.moveDown(selectedRow);
				}
			}
		});
		MAP_ACTIONS.put(ACTIONS.ACTION_MOVE_UP_STATETRANSITION, 
				new AbstractAction("", Icons.getInstance().getIconUp16()) {
			@Override
			public void actionPerformed(ActionEvent actEv) {
				EventSupportTargetView estvElement = EventSupportManagementController.this.viewEventSupportManagement.getTargetViewPanel();
				if (estvElement != null)
				{
					EventSupportStatePropertiesTableModel tblModel = (EventSupportStatePropertiesTableModel) 
							estvElement.getPropertyTable().getModel();
					
					int selectedRow = estvElement.getPropertyTable().getSelectedRow();
					tblModel.moveUp(selectedRow);
					estvElement.getPropertyTable().repaint();
					estvElement.getPropertyTable().revalidate();
				}
			}
		});
		MAP_ACTIONS.put(ACTIONS.ACTION_DELETE_EVENT, 
				new AbstractAction("", Icons.getInstance().getIconDelete16()) {
					@Override
					public void actionPerformed(ActionEvent e) {
						EventSupportTargetView estvElement = EventSupportManagementController.this.viewEventSupportManagement.getTargetViewPanel();
						if (estvElement != null)
						{
							EventSupportEntityPropertiesTableModel tblModel = (EventSupportEntityPropertiesTableModel)	 
									estvElement.getPropertyTable().getModel();
							
							int selectedRow = estvElement.getPropertyTable().getSelectedRow();
							EventSupportEventVO entryByRowIndex = tblModel.getEntryByRowIndex(selectedRow);
							
							// Delete element
							if (entryByRowIndex.getId() != null) {
								EventSupportDelegate.getInstance().deleteEventSupportEvent(entryByRowIndex);
							}
							
							// remove it from tablemodel
							tblModel.removeEntry(selectedRow);
							
							// and refresh new order and store it in db
							for (int idx = selectedRow; idx < tblModel.getRowCount(); idx++) {
								EventSupportDelegate.getInstance().modifyEventSupportEvent(tblModel.getEntryByRowIndex(idx));
							}
							
							estvElement.getPropertyTable().repaint();
							estvElement.getPropertyTable().revalidate();
						}
					}
				});
		MAP_ACTIONS.put(ACTIONS.ACTION_MOVE_UP_EVENT, 
				new AbstractAction("", Icons.getInstance().getIconUp16()) {
					@Override
					public void actionPerformed(ActionEvent e) {
						EventSupportTargetView estvElement = EventSupportManagementController.this.viewEventSupportManagement.getTargetViewPanel();
						if (estvElement != null)
						{
							EventSupportEntityPropertiesTableModel tblModel = (EventSupportEntityPropertiesTableModel) 
									estvElement.getPropertyTable().getModel();
							
							int selectedRow = estvElement.getPropertyTable().getSelectedRow();
							tblModel.moveUp(selectedRow);
							estvElement.getPropertyTable().repaint();
							estvElement.getPropertyTable().revalidate();
						}
					}
				});
		MAP_ACTIONS.put(ACTIONS.ACTION_MOVE_DOWN_EVENT, 
				new AbstractAction("", Icons.getInstance().getIconDown16()) {
					@Override
					public void actionPerformed(ActionEvent e) {
						EventSupportTargetView estvElement = EventSupportManagementController.this.viewEventSupportManagement.getTargetViewPanel();
						if (estvElement != null)
						{
							EventSupportEntityPropertiesTableModel tblModel = (EventSupportEntityPropertiesTableModel) 
									estvElement.getPropertyTable().getModel();
							
							int selectedRow = estvElement.getPropertyTable().getSelectedRow();
							tblModel.moveDown(selectedRow);
						}
					}
				});
		MAP_ACTIONS.put(ACTIONS.ACTION_REFRESH_TARGETTREE, 
				new AbstractAction("", Icons.getInstance().getIconRefresh16()) {
					@Override
					public void actionPerformed(ActionEvent e) {
						try {
							EventSupportTargetView estvElement = EventSupportManagementController.this.viewEventSupportManagement.getTargetViewPanel();
							estvElement.getExplorerView().getRootNode().refresh(estvElement.getTree());							
						} catch (Exception err) {
							LOG.error(err.getMessage(), err);
						}
					}
				});
		MAP_ACTIONS.put(ACTIONS.ACTION_REFRESH_SOURCETREE, 
				new AbstractAction("", Icons.getInstance().getIconRefresh16()) {
					@Override
					public void actionPerformed(ActionEvent e) {
						try {
							EventSupportSourceView estvElement = EventSupportManagementController.this.viewEventSupportManagement.getSourceViewPanel();
							estvElement.getExplorerView().getRootNode().refresh(estvElement.getTree());
						} catch (CommonFinderException err) {
							LOG.error(err.getMessage(), err);
						}
					}
				});
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
				propertyModel.addEntry(EventSupportPropertiesTableModel.ELM_ES_NUCLET, node.getParentNode().getParentNode().getEntityName());
				propertyModel.addEntry(EventSupportPropertiesTableModel.ELM_ES_PATH, eventSupportByClassname.getPackage());
				propertyModel.addEntry(EventSupportPropertiesTableModel.ELM_ES_CREATION_DATE, 
						eventSupportByClassname.getDateOfCompilation() != null ? dateInstance.format(eventSupportByClassname.getDateOfCompilation()) : null);
				
			}
			
		} catch (RemoteException e) {
			LOG.error(e.getMessage(), e);
		}	
	}
	
	public void showTargetStateSupportPropertiesNotModified(final EventSupportTreeNode node) {
		final EventSupportStatePropertiesTableModel targetStateModel = viewEventSupportManagement.getTargetStateModel();
		
		Collection<EventSupportTransitionVO> EventSupportTransitionVO;
		
		try {
			EventSupportTransitionVO = EventSupportRepository.getInstance().getEventSupportsByTransitionId((Integer) node.getId());
			targetStateModel.clear();
			
			for(EventSupportTransitionVO estVO : EventSupportTransitionVO) {
				targetStateModel.addEntry(estVO);
			}
			viewEventSupportManagement.getTargetViewPanel().loadPropertyPanelByModelType(targetStateModel);
			
		} catch (RemoteException e) {
			LOG.error(e.getMessage(), e);
		}
		
	}
	
	public void showTargetSupportPropertiesNotModified(final EventSupportTreeNode node) {
		
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
					final EventSupportEntityPropertiesTableModel targetEntityModel = viewEventSupportManagement.getTargetEntityModel();
					if (targetEntityModel.isModelModified() ) {
						
						final String sMsg = getSpringLocaleDelegate().getMessage(
								"CollectController.14","Der Datensatz wurde ge\u00e4ndert.") + "\n" + getSpringLocaleDelegate().getMessage(
										"CollectController.32","Wenn Sie jetzt nicht speichern, werden diese \u00c4nderungen verloren gehen.") + "\n" +
										getSpringLocaleDelegate().getMessage("CollectController.20","Jetzt speichern?");
						int result = JOptionPane.showConfirmDialog(viewEventSupportManagement, sMsg, "", JOptionPane.YES_NO_OPTION);
						if (result == OverlayOptionPane.YES_OPTION) {
							MAP_ACTIONS.get(ACTIONS.ACTION_SAVE_ALL_EVENTS).actionPerformed(null);
						}	
						targetEntityModel.setModelModified(false);
					}
					
					showTargetSupportPropertiesNotModified(node);
					
					break;
				case STATE_TRANSITION:
					EventSupportStatePropertiesTableModel targetStateModel = viewEventSupportManagement.getTargetStateModel();
					
					if (targetStateModel.isModelModified()) {
						final String sMsg = getSpringLocaleDelegate().getMessage(
								"CollectController.14","Der Datensatz wurde ge\u00e4ndert.") + "\n" + getSpringLocaleDelegate().getMessage(
										"CollectController.32","Wenn Sie jetzt nicht speichern, werden diese \u00c4nderungen verloren gehen.") + "\n" +
										getSpringLocaleDelegate().getMessage("CollectController.20","Jetzt speichern?");
						int result = JOptionPane.showConfirmDialog(viewEventSupportManagement, sMsg, "", JOptionPane.YES_NO_OPTION);
						if (result == OverlayOptionPane.YES_OPTION) {
							MAP_ACTIONS.get(ACTIONS.ACTION_SAVE_ALL_STATETRANSITION).actionPerformed(null);
						}	
						targetStateModel.setModelModified(false);
					}
					
					showTargetStateSupportPropertiesNotModified(node);
					
					break;
				default:
					break;
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
			viewEventSupportManagement = new EventSupportView(treenodeRoot, treenodeRootTargets);
			viewEventSupportManagement.setActionMap(MAP_ACTIONS);
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

	public List<EventSupportTreeNode> createSubNodesByType(EventSupportTreeNode esNode)
	{
		List<EventSupportTreeNode> retVal = new ArrayList<EventSupportTreeNode>();
		
		try {
			List<EventSupportVO> eventSupportTypes;
			
			switch (esNode.getTreeNodeType()) 
			{
				case ROOT:
					// Default Node for all nuclos elements that are not attached to a nuclet
					retVal.add(new EventSupportTreeNode(this, esNode, null, "<Default>", "<Default>", "Alle nicht zugewiesenen Elemente", EventSupportTargetType.NUCLET, false));
					// Nuclet Nodes
					Collection<MasterDataVO> masterData = MasterDataDelegate.getInstance().getMasterData(NuclosEntity.NUCLET.getEntityName());
					for (MasterDataVO msvo : masterData)
					{
						retVal.add(new EventSupportTreeNode(this, esNode, msvo.getId(), msvo.getField("package").toString(), msvo.getField("name").toString(), msvo.getField("description").toString(), EventSupportTargetType.NUCLET, false));
					}
					break;
				case NUCLET:
					// EventSupportTypes
					eventSupportTypes = EventSupportRepository.getInstance().getEventSupportTypes();
					for (EventSupportVO s : eventSupportTypes)
					{
						EventSupportTreeNode eventSupportTreeNode = new EventSupportTreeNode(this, esNode, null, s.getClassname(), s.getName(), s.getDescription(), EventSupportTargetType.EVENTSUPPORT_TYPE, false);
						retVal.add(eventSupportTreeNode);
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
				default:
					break;
			}
			
		} catch (RemoteException e) {
			Log.error(e.getMessage(), e);
		}
		
		return retVal;
	}
	
	public EventSupportTransitionVO addEventSupportToStateTransition(EventSupportTreeNode sourceNode, EventSupportTreeNode targetNode) {
		EventSupportTransitionVO retVal = null;
		
		if (targetNode != null && EventSupportTargetType.STATE_TRANSITION.equals(targetNode.getTreeNodeType()))
		{
			String sSupportClass = sourceNode.getEntityName();
			Integer iTransitionId = Integer.parseInt(targetNode.getId().toString());
			Integer iOrder = targetNode.getSubNodes().size() + 1;
			Boolean bRunAfterwards = false;
	
			EventSupportDelegate.getInstance().createEventSupportTransition(
					new EventSupportTransitionVO(sSupportClass, iTransitionId, iOrder, bRunAfterwards));
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
			
//			if (targetNode.getId() instanceof Long)
//				id = ((Long) targetNode.getId()).intValue();
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
				int moduleId = 0;
				
				if (nodeId instanceof Integer) 
					moduleId = ((Integer)nodeId).intValue();
				if (nodeId instanceof Long) 
					moduleId = ((Long)nodeId).intValue();
				
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
