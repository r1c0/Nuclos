package org.nuclos.client.eventsupport;

import java.awt.event.ActionEvent;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.JTable;
import javax.swing.JTextField;

import org.apache.log4j.Logger;
import org.nuclos.client.eventsupport.model.EventSupportPropertiesTableModel;
import org.nuclos.client.eventsupport.panel.EventSupportSourceView;
import org.nuclos.client.eventsupport.panel.EventSupportTargetView;
import org.nuclos.client.eventsupport.panel.EventSupportView;
import org.nuclos.client.explorer.EventSupportExplorerView;
import org.nuclos.client.explorer.node.EventSupportExplorerNode;
import org.nuclos.client.explorer.node.EventSupportTargetExplorerNode;
import org.nuclos.client.explorer.node.eventsupport.EventSupportTargetTreeNode;
import org.nuclos.client.explorer.node.eventsupport.EventSupportTreeNode;
import org.nuclos.client.ui.Icons;
import org.nuclos.server.eventsupport.valueobject.EventSupportEventVO;
import org.nuclos.server.eventsupport.valueobject.EventSupportGenerationVO;
import org.nuclos.server.eventsupport.valueobject.EventSupportJobVO;
import org.nuclos.server.eventsupport.valueobject.EventSupportTransitionVO;
import org.nuclos.server.eventsupport.valueobject.EventSupportVO;

public class EventSupportActionHandler {

	private static final Logger LOG = Logger.getLogger(EventSupportActionHandler.class);	
	
	public static enum ACTIONS {
		ACTION_SAVE_EVENT, 
		ACTION_SAVE_ALL_EVENTS, 
		ACTION_DELETE_EVENT, 
		ACTION_MOVE_UP_EVENT, 
		ACTION_MOVE_DOWN_EVENT,
		
		ACTION_SAVE_STATETRANSITION,
		ACTION_SAVE_ALL_STATETRANSITION,
		ACTION_DELETE_STATETRANSITION, 
		ACTION_MOVE_UP_STATETRANSITION, 
		ACTION_MOVE_DOWN_STATETRANSITION,
		
		ACTION_SAVE_ALL_JOBS,
		ACTION_DELETE_JOB, 
		ACTION_MOVE_UP_JOB, 
		ACTION_MOVE_DOWN_JOB,
		
		ACTION_SAVE_ALL_GENERATIONS,
		ACTION_DELETE_GENERATION, 
		ACTION_MOVE_UP_GENERATION, 
		ACTION_MOVE_DOWN_GENERATION,
		
		ACTION_REFRESH_TARGETTREE, 
		ACTION_REFRESH_SOURCETREE,
		
		ACTION_RUN_SOURCETREE_SEARCH,
		ACTION_RUN_TARGETTREE_SEARCH
	};
		
	
	private final Map<ACTIONS, AbstractAction> MAP_ACTIONS = new HashMap<ACTIONS, AbstractAction>();
	
	EventSupportView esView;
	
	public EventSupportActionHandler(EventSupportView esView) {
		this.esView = esView;
		
		loadActions();
	}
	
	public EventSupportView getEventSupportView() {
		return esView;
	}

	public Map<ACTIONS, AbstractAction> getActionMaps() {
		return this.MAP_ACTIONS;
	}
	
	private void loadActions() {
		
		// Actions used in Entity - PropertyPanel 
		loadActionEventsEntites();
		
		// Actions used in StateModel - PropertyPanel 
		loadActionEventsStateModel();
		
		// Actions used in Jobs - PropertyPanel 
		loadActionEventsJobs();
		
		// Actions used in Generation - PropertyPanel 
		loadActionEventsGeneration();
		
		// Actions used in all PropertyPanels
		loadActionEventsGenerealUsage();
		
	}
	
	private void loadActionEventsGeneration() {
		MAP_ACTIONS.put(ACTIONS.ACTION_SAVE_ALL_GENERATIONS, new EventSupportModifyAllAction() {
			@Override
			public EventSupportVO insertEventSupport(EventSupportVO entryByRowIndex) {
				EventSupportGenerationVO oldEntry = (EventSupportGenerationVO)entryByRowIndex; 
				EventSupportGenerationVO newEntry = new EventSupportGenerationVO(oldEntry.getOrder(), oldEntry.getGeneration(), oldEntry.getEventSupportClass(), oldEntry.getEventSupportClassType(), oldEntry.isRunAfterwards());
				return EventSupportDelegate.getInstance().createEventSupportGeneration(newEntry);
			}
			@Override
			public void deleteEventSupport(EventSupportTargetExplorerNode node) {
				try {
					Integer iGenerationId = Integer.parseInt(node.getTreeNode().getId().toString());
					Collection<EventSupportGenerationVO> eventSupportsByGenerationId = EventSupportRepository.getInstance().getEventSupportsByGenerationId(iGenerationId);
					for (EventSupportGenerationVO esgVO : eventSupportsByGenerationId) {
						EventSupportDelegate.getInstance().deleteEventSupportGeneration(esgVO);
					}
				} catch (Exception e) {
					LOG.error(e.getMessage(), e);
				}
			}
			@Override
			public JTable getPropertyTable(EventSupportTargetView targetView) {
				return targetView.getGenerationPropertiesPanel().getPropertyTable();
			}
		});
		
		MAP_ACTIONS.put(ACTIONS.ACTION_DELETE_GENERATION,new EventSupportDeleteAction() {
			@Override
			public JTable getPropertyTable(EventSupportTargetView targetView) {
				return targetView.getGenerationPropertiesPanel().getPropertyTable();
			}
			@Override
			public void saveActionModifications(
					EventSupportTargetView estvElement) {
				estvElement.getGenerationPropertiesPanel().getActionMapping().get(ACTIONS.ACTION_SAVE_ALL_GENERATIONS).actionPerformed(null);	
			}
		});
		MAP_ACTIONS.put(ACTIONS.ACTION_MOVE_DOWN_GENERATION, new EventSupportMoveDownAction() {
			@Override
			public JTable getPropertyTable(EventSupportTargetView targetView) {
				return targetView.getGenerationPropertiesPanel().getPropertyTable();
			}
			@Override
			public void saveActionModifications(
					EventSupportTargetView estvElement) {
				estvElement.getGenerationPropertiesPanel().getActionMapping().get(ACTIONS.ACTION_SAVE_ALL_GENERATIONS).actionPerformed(null);	
			}
		});
		MAP_ACTIONS.put(ACTIONS.ACTION_MOVE_UP_GENERATION, new EventSupportMoveUpAction() {
			@Override
			public JTable getPropertyTable(EventSupportTargetView targetView) {
				return targetView.getGenerationPropertiesPanel().getPropertyTable();
			}
			@Override
			public void saveActionModifications(
					EventSupportTargetView estvElement) {
				estvElement.getGenerationPropertiesPanel().getActionMapping().get(ACTIONS.ACTION_SAVE_ALL_GENERATIONS).actionPerformed(null);	
			}
		});
	}

	private void loadActionEventsJobs() {
		
		MAP_ACTIONS.put(ACTIONS.ACTION_DELETE_JOB, new EventSupportDeleteAction() {
			@Override
			public JTable getPropertyTable(EventSupportTargetView targetView) {
				return targetView.getJobPropertiesPanel().getPropertyTable();
			}
			@Override
			public void saveActionModifications(
					EventSupportTargetView estvElement) {
				estvElement.getGenerationPropertiesPanel().getActionMapping().get(ACTIONS.ACTION_SAVE_ALL_JOBS).actionPerformed(null);	
			}
		});		
		MAP_ACTIONS.put(ACTIONS.ACTION_SAVE_ALL_JOBS, new EventSupportModifyAllAction() {
			@Override
			public EventSupportVO insertEventSupport(EventSupportVO entryByRowIndex) {
				EventSupportJobVO oldEntry = (EventSupportJobVO)entryByRowIndex; 
				EventSupportJobVO newEntry = new EventSupportJobVO(oldEntry.getDescription(), oldEntry.getEventSupportClass(), oldEntry.getEventSupportClassType(), oldEntry.getOrder(), oldEntry.getJobControllerId());
				return EventSupportDelegate.getInstance().createEventSupportJob(newEntry);
			}
			@Override
			public void deleteEventSupport(EventSupportTargetExplorerNode node) {
				try {
					Integer iJobId = Integer.parseInt(node.getTreeNode().getId().toString());
					Collection<EventSupportJobVO> eventSupportsForJob = EventSupportRepository.getInstance().getEventSupportsForJob(iJobId);
					
					for (EventSupportJobVO esjVO : eventSupportsForJob) {
						if (esjVO.getEventSupportClassType().equals(node.getTreeNode().getEntityName()))  {		
							EventSupportDelegate.getInstance().deleteEventSupportJob(esjVO);												
						}
					}
					
				} catch (Exception e) {
					LOG.error(e.getMessage(), e);
				}
			}
			@Override
			public JTable getPropertyTable(EventSupportTargetView targetView) {
				return targetView.getJobPropertiesPanel().getPropertyTable();
			}
		});
		
		MAP_ACTIONS.put(ACTIONS.ACTION_MOVE_DOWN_JOB, new EventSupportMoveDownAction() {
			@Override
			public JTable getPropertyTable(EventSupportTargetView targetView) {
				return targetView.getJobPropertiesPanel().getPropertyTable();
			}
			@Override
			public void saveActionModifications(
					EventSupportTargetView estvElement) {
				estvElement.getGenerationPropertiesPanel().getActionMapping().get(ACTIONS.ACTION_SAVE_ALL_JOBS).actionPerformed(null);	
			}
		});
		MAP_ACTIONS.put(ACTIONS.ACTION_MOVE_UP_JOB, new EventSupportMoveUpAction() {
			@Override
			public JTable getPropertyTable(EventSupportTargetView targetView) {
				return targetView.getJobPropertiesPanel().getPropertyTable();
			}
			@Override
			public void saveActionModifications(
					EventSupportTargetView estvElement) {
				estvElement.getGenerationPropertiesPanel().getActionMapping().get(ACTIONS.ACTION_SAVE_ALL_JOBS).actionPerformed(null);	
			}
		});
	}

	private void loadActionEventsGenerealUsage() {

		MAP_ACTIONS.put(ACTIONS.ACTION_REFRESH_TARGETTREE, 
				new AbstractAction("", Icons.getInstance().getIconRefresh16()) {
					@Override
					public void actionPerformed(ActionEvent e) {
						try {
							EventSupportTargetView estvElement = getEventSupportView().getTargetViewPanel();
							EventSupportRepository.getInstance().updateEventSupports();
							
							EventSupportExplorerView explView = (EventSupportExplorerView) getEventSupportView().getTargetViewPanel().getExplorerView();
							explView.getSearchTextField().setText("");
							EventSupportTargetTreeNode node= (EventSupportTargetTreeNode) estvElement.getExplorerView().getRootNode().getTreeNode();
							node.setSearchString(null);
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
							EventSupportSourceView estvElement = getEventSupportView().getSourceViewPanel();
							
							EventSupportExplorerView explView = (EventSupportExplorerView) getEventSupportView().getSourceViewPanel().getExplorerView();
							explView.getSearchTextField().setText(null);
							EventSupportTreeNode node= (EventSupportTreeNode) estvElement.getExplorerView().getRootNode().getTreeNode();
							node.setSearchString(null);
							EventSupportDelegate.getInstance().forceEventSupportCompilation();
							EventSupportRepository.getInstance().updateEventSupports();
							estvElement.getExplorerView().getRootNode().refresh(estvElement.getTree());
							
						} catch (Exception err) {
							LOG.error(err.getMessage(), err);
						}
					}
				});
		
		MAP_ACTIONS.put(ACTIONS.ACTION_RUN_TARGETTREE_SEARCH, 
				new AbstractAction("", null) {
					@Override
					public void actionPerformed(ActionEvent e) {
						try {
							EventSupportTargetView estvElement = getEventSupportView().getTargetViewPanel();
							JTextField searchField = (JTextField) e.getSource();
							String sSearchText = searchField.getText().trim();
							EventSupportTargetExplorerNode rootNode = (EventSupportTargetExplorerNode) estvElement.getExplorerView().getRootNode();
							EventSupportTargetTreeNode treeNode = (EventSupportTargetTreeNode) rootNode.getTreeNode();
							
//							 Check wether results can be found for this searchstring
//							 if not, no need to refreh the tree
							if (sSearchText.length() == 0) {
								treeNode.setSearchString(null);
								rootNode.removeAllChildren();
								rootNode.refresh(estvElement.getTree());	
								rootNode.expandAllChildren(estvElement.getTree());
							}
							else {
								treeNode.setSearchString(sSearchText);
								rootNode.removeAllChildren();
								rootNode.refresh(estvElement.getTree());
								
								int row = 0;
								 while (row < estvElement.getTree().getRowCount()) {
									 estvElement.getTree().expandRow(row);
								      row++;
								      }
								    
							}
							
						} catch (Exception err) {
							LOG.error(err.getMessage(), err);
						}
					}
				});
		
		MAP_ACTIONS.put(ACTIONS.ACTION_RUN_SOURCETREE_SEARCH, 
				new AbstractAction("", null) {
					@Override
					public void actionPerformed(ActionEvent e) {
						try {
							EventSupportSourceView estvElement = getEventSupportView().getSourceViewPanel();
							JTextField searchField = (JTextField) e.getSource();
							String sSearchText = searchField.getText().trim();
							EventSupportExplorerNode rootNode = (EventSupportExplorerNode) estvElement.getExplorerView().getRootNode();
							EventSupportTreeNode treeNode = rootNode.getTreeNode();
							
							// Check wether results can be found for this searchstring
							// if not, no need to refreh the tree
							if (sSearchText.length() == 0) {
								treeNode.setSearchString(null);
								rootNode.removeAllChildren();
								rootNode.refresh(estvElement.getTree());	
								rootNode.expandAllChildren(estvElement.getTree());
							}
							else if (EventSupportRepository.getInstance().searchForEventSupports(sSearchText).size() > 0 ) {
								treeNode.setSearchString(sSearchText);
								rootNode.removeAllChildren();
								rootNode.refresh(estvElement.getTree());
								
								int row = 0;
								 while (row < estvElement.getTree().getRowCount()) {
									 estvElement.getTree().expandRow(row);
								      row++;
								      }
							}
							
						} catch (Exception err) {
							LOG.error(err.getMessage(), err);
						}
					}
				});
	}

	private void loadActionEventsStateModel() {
		
		MAP_ACTIONS.put(ACTIONS.ACTION_SAVE_STATETRANSITION, new EventSupportModifyAction() {
			@Override
			public EventSupportVO modifyEventSupport(EventSupportVO entryByRowIndex) {
				return EventSupportDelegate.getInstance().modifyEventSupportTransition((EventSupportTransitionVO) entryByRowIndex);
			}		
			@Override
			public JTable getPropertyTable(EventSupportTargetView targetView) {
				return targetView.getStatePropertiesPanel().getPropertyTable();
			}
		});
			
		MAP_ACTIONS.put(ACTIONS.ACTION_SAVE_ALL_STATETRANSITION, new EventSupportModifyAllAction() {
			@Override
			public EventSupportVO insertEventSupport(EventSupportVO entryByRowIndex) {
				EventSupportTransitionVO oldEntry = (EventSupportTransitionVO)entryByRowIndex; 
				EventSupportTransitionVO newEntry = new EventSupportTransitionVO(oldEntry.getEventSupportClass(), oldEntry.getEventSupportClassType(), oldEntry.getTransitionId(), oldEntry.getOrder(), oldEntry.isRunAfterwards());
				
				try {
					EventSupportRepository.getInstance().updateEventSupports();
				} catch (Exception e) {
					LOG.error(e.getMessage(), e);
				}
				
				return EventSupportDelegate.getInstance().createEventSupportTransition(newEntry);
			}			
			@Override
			public void deleteEventSupport(EventSupportTargetExplorerNode node) {
				try {
					Integer iModelId = Integer.parseInt(node.getTreeNode().getId().toString());
					List<EventSupportTransitionVO> stateTransitionsBySupportType = EventSupportDelegate.getInstance().getStateTransitionsBySupportType(iModelId, node.getTreeNode().getEntityName());
					
					for (EventSupportTransitionVO estVO : stateTransitionsBySupportType) {
						if (estVO.getEventSupportClassType().equals(node.getTreeNode().getEntityName()))  {									
							EventSupportDelegate.getInstance().deleteEventSupportTransition(estVO);						
						}
					}
					
					EventSupportRepository.getInstance().updateEventSupports();
				} catch (Exception e) {
					LOG.error(e.getMessage(), e);
				}
			}
			@Override
			public JTable getPropertyTable(EventSupportTargetView targetView) {
				return targetView.getStatePropertiesPanel().getPropertyTable();
			}
		});
	
		MAP_ACTIONS.put(ACTIONS.ACTION_DELETE_STATETRANSITION, new EventSupportDeleteAction() {
			@Override
			public JTable getPropertyTable(EventSupportTargetView targetView) {
				return targetView.getStatePropertiesPanel().getPropertyTable();
			}
			@Override
			public void saveActionModifications(
					EventSupportTargetView estvElement) {
				estvElement.getGenerationPropertiesPanel().getActionMapping().get(ACTIONS.ACTION_SAVE_ALL_STATETRANSITION).actionPerformed(null);	
			}
		});			
		MAP_ACTIONS.put(ACTIONS.ACTION_MOVE_DOWN_STATETRANSITION, new EventSupportMoveDownAction() {
			@Override
			public JTable getPropertyTable(EventSupportTargetView targetView) {
				return targetView.getStatePropertiesPanel().getPropertyTable();
			}
			@Override
			public void saveActionModifications(
					EventSupportTargetView estvElement) {
				estvElement.getGenerationPropertiesPanel().getActionMapping().get(ACTIONS.ACTION_SAVE_ALL_STATETRANSITION).actionPerformed(null);	
			}
		});
		MAP_ACTIONS.put(ACTIONS.ACTION_MOVE_UP_STATETRANSITION, new EventSupportMoveUpAction() {
			@Override
			public JTable getPropertyTable(EventSupportTargetView targetView) {
				return targetView.getStatePropertiesPanel().getPropertyTable();
			}
			@Override
			public void saveActionModifications(
					EventSupportTargetView estvElement) {
				estvElement.getGenerationPropertiesPanel().getActionMapping().get(ACTIONS.ACTION_SAVE_ALL_STATETRANSITION).actionPerformed(null);	
			}
		});
	}

	private void loadActionEventsEntites() {
		MAP_ACTIONS.put(ACTIONS.ACTION_SAVE_EVENT, new EventSupportModifyAction() {
					@Override
					public EventSupportVO modifyEventSupport(EventSupportVO entryByRowIndex) {
						return EventSupportDelegate.getInstance().modifyEventSupportEvent((EventSupportEventVO) entryByRowIndex);
					}		
					@Override
					public JTable getPropertyTable(EventSupportTargetView targetView) {
						return targetView.getEntityPropertiesPanel().getPropertyTable();
					}
		});
			
		MAP_ACTIONS.put(ACTIONS.ACTION_SAVE_ALL_EVENTS, new EventSupportModifyAllAction() {
					@Override
					public EventSupportVO insertEventSupport(EventSupportVO entryByRowIndex) {
						EventSupportEventVO oldEntry = (EventSupportEventVO)entryByRowIndex; 
						EventSupportEventVO newEntry = new EventSupportEventVO(oldEntry.getEventSupportClass(), oldEntry.getEventSupportClassType(), oldEntry.getEntity(), oldEntry.getProcessId(), oldEntry.getStateId(), oldEntry.getOrder(),oldEntry.getEntityName(), oldEntry.getStateName(), oldEntry.getProcessName());
						return EventSupportDelegate.getInstance().createEventSupportEvent(newEntry);
					}	
					@Override
					public void deleteEventSupport(EventSupportTargetExplorerNode node) {
						try {
							Integer iEntityId = Integer.parseInt(node.getTreeNode().getId().toString());
							Collection<EventSupportEventVO> eventSupportsForEntity = EventSupportDelegate.getInstance().getEventSupportsForEntity(iEntityId);
							for (EventSupportEventVO eseVO : eventSupportsForEntity) {
								if (eseVO.getEventSupportClassType().equals(node.getTreeNode().getEntityName()))  {
									EventSupportDelegate.getInstance().deleteEventSupportEvent(eseVO);			
								}
							}
						} catch (Exception e) {
							LOG.error(e.getMessage(), e);
						}
					}
					@Override
					public JTable getPropertyTable(EventSupportTargetView targetView) {
						return targetView.getEntityPropertiesPanel().getPropertyTable();
					}
		});
			
		MAP_ACTIONS.put(ACTIONS.ACTION_DELETE_EVENT, new EventSupportDeleteAction() {
			@Override
			public JTable getPropertyTable(EventSupportTargetView targetView) {
				return targetView.getEntityPropertiesPanel().getPropertyTable();
			}
			@Override
			public void saveActionModifications(
					EventSupportTargetView estvElement) {
				estvElement.getEntityPropertiesPanel().getActionMapping().get(ACTIONS.ACTION_SAVE_ALL_EVENTS).actionPerformed(null);	
			}
		});
		MAP_ACTIONS.put(ACTIONS.ACTION_MOVE_UP_EVENT, new EventSupportMoveUpAction() {
			@Override
			public JTable getPropertyTable(EventSupportTargetView targetView) {
				return targetView.getEntityPropertiesPanel().getPropertyTable();
			}
			@Override
			public void saveActionModifications(
					EventSupportTargetView estvElement) {
				estvElement.getEntityPropertiesPanel().getActionMapping().get(ACTIONS.ACTION_SAVE_ALL_EVENTS).actionPerformed(null);	
			}
		});
		MAP_ACTIONS.put(ACTIONS.ACTION_MOVE_DOWN_EVENT, new EventSupportMoveDownAction() {
			@Override
			public JTable getPropertyTable(EventSupportTargetView targetView) {
				return targetView.getEntityPropertiesPanel().getPropertyTable();
			}
			@Override
			public void saveActionModifications(
					EventSupportTargetView estvElement) {
				estvElement.getEntityPropertiesPanel().getActionMapping().get(ACTIONS.ACTION_SAVE_ALL_EVENTS).actionPerformed(null);	
			}
		});
	}

	public void fireAction(ACTIONS act) {
		MAP_ACTIONS.get(act).actionPerformed(null);
	}
	
	public abstract class EventSupportModifyAction extends AbstractAction {

		public abstract EventSupportVO modifyEventSupport(EventSupportVO entryByRowIndex);
		public abstract JTable getPropertyTable(EventSupportTargetView targetView);
		
		public EventSupportModifyAction() 
		{
			super("" , Icons.getInstance().getIconSave16());
		}
		
		@Override
		public void actionPerformed(ActionEvent actEv) {
			
			EventSupportTargetView estvElement = getEventSupportView().getTargetViewPanel();
			
			if (estvElement != null)
			{
				EventSupportPropertiesTableModel model = 
						(EventSupportPropertiesTableModel) getPropertyTable(estvElement).getModel();
				
				int selectedRow = getPropertyTable(estvElement).getSelectedRow();
				
				EventSupportVO entryByRowIndex = model.getEntryByRowIndex(selectedRow);
					
				// Modify entry
				if (entryByRowIndex.getId() != null) {
					try {
						entryByRowIndex.setVersion(modifyEventSupport(entryByRowIndex).getVersion());
						
					} catch (Exception e) {
						LOG.error(e.getMessage(), e);
					}
				}
				model.setModelModified(false);
			}
		}		
	}
	
	public abstract class EventSupportModifyAllAction extends AbstractAction {

		public abstract EventSupportVO insertEventSupport(EventSupportVO entryByRowIndex);
		public abstract void deleteEventSupport(EventSupportTargetExplorerNode explorerNode);
		public abstract JTable getPropertyTable(EventSupportTargetView targetView);
		
		public EventSupportModifyAllAction() 
		{
			super("" , Icons.getInstance().getIconSave16());
		}
		
		@Override
		public void actionPerformed(ActionEvent actEv) {
			EventSupportTargetView estvElement = getEventSupportView().getTargetViewPanel();
			if (estvElement != null)
			{
				EventSupportPropertiesTableModel model = 
						(EventSupportPropertiesTableModel) getPropertyTable(estvElement).getModel();
				
				// First remove all db-entries for a clear update			
				EventSupportTargetExplorerNode node = (EventSupportTargetExplorerNode) 
						estvElement.getExplorerView().getJTree().getLastSelectedPathComponent();
				deleteEventSupport(node);
				
				for(int selectedRow = 0; selectedRow < model.getRowCount(); selectedRow++){
					
					EventSupportVO entryByRowIndex = model.getEntryByRowIndex(selectedRow);
					
					// Modify entry
					try {
						
						EventSupportVO insertEventSupport = insertEventSupport(entryByRowIndex);
						model.removeEntry(selectedRow);
						model.addEntry(selectedRow, insertEventSupport);
						
					} catch (Exception e) {
						LOG.error(e.getMessage(), e);
					}
			
				}
			
				model.setModelModified(false);
			}
		}		
	}
	
	public abstract class EventSupportDeleteAction extends AbstractAction {

		public abstract JTable getPropertyTable(EventSupportTargetView targetView);
		public abstract void saveActionModifications(EventSupportTargetView targetView);
		
		public EventSupportDeleteAction() 
		{
			super("" , Icons.getInstance().getIconDelete16());
		}
		
		@Override
		public void actionPerformed(ActionEvent actEv) {
			EventSupportTargetView estvElement = getEventSupportView().getTargetViewPanel();
			if (estvElement != null)
			{
				EventSupportPropertiesTableModel model = 
						(EventSupportPropertiesTableModel) getPropertyTable(estvElement).getModel();
				
				int selectedRow = getPropertyTable(estvElement).getSelectedRow();
				EventSupportVO entryByRowIndex = model.getEntryByRowIndex(selectedRow);
				
				int order= entryByRowIndex.getOrder();
				
				// remove it from tablemodel
				model.removeEntry(selectedRow);
				
				// and refresh new order and store it in db
				for (int idx = selectedRow; idx < model.getRowCount(); idx++) {
					EventSupportVO nextEntry = model.getEntryByRowIndex(idx);
					nextEntry.setOrder(order++);
				}
				saveActionModifications(estvElement);
			}
		}		
	}
	
	public abstract class EventSupportMoveUpAction extends AbstractAction {
		public abstract JTable getPropertyTable(EventSupportTargetView targetView);
		public abstract void saveActionModifications(EventSupportTargetView targetView);
		public EventSupportMoveUpAction() 
		{
			super("" , Icons.getInstance().getIconUp16());
		}
		
		@Override
		public void actionPerformed(ActionEvent actEv) {
			EventSupportTargetView estvElement = getEventSupportView().getTargetViewPanel();
			if (estvElement != null)
			{
				int selectedRow = getPropertyTable(estvElement).getSelectedRow();
				((EventSupportPropertiesTableModel) getPropertyTable(estvElement).getModel()).moveUp(selectedRow);
				
				saveActionModifications(estvElement);
			}
		}
	}
	
	public abstract class EventSupportMoveDownAction extends AbstractAction {

		public abstract JTable getPropertyTable(EventSupportTargetView targetView);
		public abstract void saveActionModifications(EventSupportTargetView targetView);
		
		public EventSupportMoveDownAction() 
		{
			super("" , Icons.getInstance().getIconDown16());
		}
		
		@Override
		public void actionPerformed(ActionEvent actEv) {
			EventSupportTargetView estvElement = getEventSupportView().getTargetViewPanel();
			if (estvElement != null)
			{
				EventSupportPropertiesTableModel model = 
						(EventSupportPropertiesTableModel) getPropertyTable(estvElement).getModel();
				
				int selectedRow = getPropertyTable(estvElement).getSelectedRow();
				model.moveDown(selectedRow);
				
				saveActionModifications(estvElement);
			}
			
		}
	}
}
