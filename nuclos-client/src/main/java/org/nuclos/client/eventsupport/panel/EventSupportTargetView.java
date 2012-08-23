package org.nuclos.client.eventsupport.panel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.border.Border;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.nuclos.client.eventsupport.EventSupportActionHandler.ACTIONS;
import org.nuclos.client.eventsupport.model.EventSupportEntityPropertiesTableModel;
import org.nuclos.client.eventsupport.model.EventSupportGenerationPropertiesTableModel;
import org.nuclos.client.eventsupport.model.EventSupportJobPropertiesTableModel;
import org.nuclos.client.eventsupport.model.EventSupportPropertiesTableModel;
import org.nuclos.client.eventsupport.model.EventSupportStatePropertiesTableModel;
import org.nuclos.client.explorer.EventSupportExplorerView;
import org.nuclos.client.explorer.EventSupportTargetExplorerView;
import org.nuclos.client.explorer.ExplorerView;

public class EventSupportTargetView extends JPanel {

	JSplitPane splitPanelEventSupport;
	EventSupportExplorerView explorerView;
	EventSupportView esView;

	JTable propTableEntites;
	JTable propTableStateModels;
	JTable propTableJobModels;
	JTable propTableGenerationModels;
	
	JPanel entityPropertiesPanel;
	JPanel statePropertiesPanel;
	JPanel jobPropertiesPanel;
	JPanel generationPropertiesPanel;
	
	JTable eventSupportPropertiesTable;
	
	public EventSupportTargetView(EventSupportView view, Border b)
	{
		super(new BorderLayout());
		
		esView = view;
		explorerView = new EventSupportTargetExplorerView(esView.getTreeEventSupportTargets(), view.getActionsMap());
		
		splitPanelEventSupport = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		
		// Tree		
		splitPanelEventSupport.setTopComponent(createTreePanel(explorerView, b));
		// Properties
		splitPanelEventSupport.setBottomComponent(getTargetPropertyByType(esView.getTargetEntityModel()));
		splitPanelEventSupport.setResizeWeight(1d);
		
		this.add(splitPanelEventSupport, BorderLayout.CENTER);
	}
	
	public ExplorerView getExplorerView() {
		return this.explorerView;
	}
	
	public JTable getPropertyTableEntites(){
		return this.propTableEntites;
	}
	public JTable getPropertyTableStatemodels(){
		return this.propTableStateModels;
	}
	public JTable getPropTableGenerationModels() {
		return propTableGenerationModels;
	}

	public JTree getTree()
	{
		return this.explorerView.getJTree();
	}
	
	public void loadPropertyPanelByModelType(EventSupportPropertiesTableModel model)
	{
		// Remove old Property Panel 
		if (splitPanelEventSupport.getBottomComponent() != null)
			splitPanelEventSupport.remove(splitPanelEventSupport.getBottomComponent());
		
		// ...and load the new according to the given modeltype
		splitPanelEventSupport.setBottomComponent(getTargetPropertyByType(model));
		splitPanelEventSupport.repaint();

	}
	
	public EventSupportPropertiesTableModel getEventSupportPropertiesTableModel() {
		return (EventSupportPropertiesTableModel) this.eventSupportPropertiesTable.getModel();
	}
	
	public JTable getEventSupportPropertiesTable() {
		return this.eventSupportPropertiesTable;
	}
	
	public JPanel getTargetPropertyByType(EventSupportPropertiesTableModel model)
	{
		JPanel retVal = null;
		
		Border b = BorderFactory.createMatteBorder(1, 1, 0, 1, Color.LIGHT_GRAY);
		
		if (model instanceof EventSupportEntityPropertiesTableModel)
		{
			retVal = createEntityPropertiesPanel(b);
		}
		else if ( model instanceof EventSupportStatePropertiesTableModel)
		{
			retVal = createStatePropertiesPanel(b);
		}
		else if ( model instanceof EventSupportJobPropertiesTableModel)
		{
			retVal = createJobPropertiesPanel(b);
		}
		else if ( model instanceof EventSupportGenerationPropertiesTableModel)
		{
			retVal = createGenerationPropertiesPanel(b);
		}
		
		return retVal;
	}
	
	private JPanel createTreePanel(ExplorerView newExplorerView, Border b)
	{
		JPanel pnlEventTypes = new JPanel();			
		pnlEventTypes.setLayout(new BorderLayout());
		
		pnlEventTypes.add(newExplorerView.getViewComponent(), BorderLayout.CENTER);
		
		pnlEventTypes.setBorder(b);
		
		return pnlEventTypes;
	}
	
	
	protected JPanel createGenerationPropertiesPanel(Border b) {
		if (generationPropertiesPanel == null) {
			generationPropertiesPanel = new JPanel();
			generationPropertiesPanel.setLayout(new BorderLayout());
			
			final JToolBar toolBar = new JToolBar(JToolBar.VERTICAL);
			Map<ACTIONS, AbstractAction> actionsMap = esView.getActionsMap();
			
			final JButton btnSaveAll = new JButton(actionsMap.get(ACTIONS.ACTION_SAVE_ALL_GENERATIONS));
			final JButton btnDelete = new JButton(actionsMap.get(ACTIONS.ACTION_DELETE_GENERATION));
			final JButton btnMoveUp = new JButton(actionsMap.get(ACTIONS.ACTION_MOVE_UP_GENERATION));
			final JButton btnMovedown = new JButton(actionsMap.get(ACTIONS.ACTION_MOVE_DOWN_GENERATION));
			
			btnDelete.setEnabled(false);
			btnMoveUp.setEnabled(false);
			btnMovedown.setEnabled(false);
			
			propTableGenerationModels = new JTable(this.esView.getTargetGenerationModel());
			propTableGenerationModels.setFillsViewportHeight(true);
			propTableGenerationModels.setRowSelectionAllowed(true);
			
			propTableGenerationModels.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
				
				@Override
				public void valueChanged(ListSelectionEvent e) {
					if(e.getValueIsAdjusting()) {
						boolean enableButtons = false;
						if (propTableGenerationModels.getSelectedRow() >= 0) {
							enableButtons = true;
						}
						
						toolBar.getComponent(toolBar.getComponentIndex(btnDelete)).setEnabled(enableButtons);
						toolBar.getComponent(toolBar.getComponentIndex(btnMoveUp)).setEnabled(enableButtons);
						toolBar.getComponent(toolBar.getComponentIndex(btnMovedown)).setEnabled(enableButtons);
					}
				}
			});

			JScrollPane scrollPane = new JScrollPane(propTableGenerationModels);
			
			toolBar.setBorderPainted(false);
						
			if (actionsMap.containsKey(ACTIONS.ACTION_SAVE_ALL_GENERATIONS))
				toolBar.add(btnSaveAll);
			if (actionsMap.containsKey(ACTIONS.ACTION_DELETE_GENERATION))
				toolBar.add(btnDelete);
			
			toolBar.addSeparator();
			
			if (actionsMap.containsKey(ACTIONS.ACTION_MOVE_UP_GENERATION))
				toolBar.add(btnMoveUp);
			
			if (actionsMap.containsKey(ACTIONS.ACTION_MOVE_DOWN_GENERATION))
				toolBar.add(btnMovedown);
					
			generationPropertiesPanel.setBorder(b);
			generationPropertiesPanel.add(scrollPane, BorderLayout.CENTER);
			generationPropertiesPanel.add(toolBar, BorderLayout.EAST);
			generationPropertiesPanel.setPreferredSize(new Dimension(0, 250));
		}
		this.eventSupportPropertiesTable = propTableGenerationModels;
		return this.generationPropertiesPanel;
	}
	
	protected JPanel createJobPropertiesPanel(Border b) {
		if (jobPropertiesPanel == null) {
			jobPropertiesPanel = new JPanel();
			jobPropertiesPanel.setLayout(new BorderLayout());
			
			final JToolBar toolBar = new JToolBar(JToolBar.VERTICAL);
			Map<ACTIONS, AbstractAction> actionsMap = esView.getActionsMap();
			
			final JButton btnSaveAll = new JButton(actionsMap.get(ACTIONS.ACTION_SAVE_ALL_JOBS));
			final JButton btnDelete = new JButton(actionsMap.get(ACTIONS.ACTION_DELETE_JOB));
			final JButton btnMoveUp = new JButton(actionsMap.get(ACTIONS.ACTION_MOVE_UP_JOB));
			final JButton btnMovedown = new JButton(actionsMap.get(ACTIONS.ACTION_MOVE_DOWN_JOB));
		
			btnDelete.setEnabled(false);
			btnMoveUp.setEnabled(false);
			btnMovedown.setEnabled(false);
			
			propTableJobModels = new JTable(this.esView.getTargetJobModel());
			propTableJobModels.setFillsViewportHeight(true);
			propTableJobModels.setRowSelectionAllowed(true);
			
			propTableJobModels.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
				
				@Override
				public void valueChanged(ListSelectionEvent e) {
					if(e.getValueIsAdjusting()) {
						boolean enableButtons = false;
						if (propTableJobModels.getSelectedRow() >= 0) {
							enableButtons = true;
						}
					
						toolBar.getComponent(toolBar.getComponentIndex(btnDelete)).setEnabled(enableButtons);
						toolBar.getComponent(toolBar.getComponentIndex(btnMoveUp)).setEnabled(enableButtons);
						toolBar.getComponent(toolBar.getComponentIndex(btnMovedown)).setEnabled(enableButtons);
					}
				}
			});
	
			JScrollPane scrollPane = new JScrollPane(propTableJobModels);
		
			toolBar.setBorderPainted(false);
		
			if (actionsMap.containsKey(ACTIONS.ACTION_SAVE_ALL_JOBS))
				toolBar.add(btnSaveAll);
			
			if (actionsMap.containsKey(ACTIONS.ACTION_DELETE_JOB))
				toolBar.add(btnDelete);
			
			toolBar.addSeparator();
			
			if (actionsMap.containsKey(ACTIONS.ACTION_MOVE_UP_JOB))
				toolBar.add(btnMoveUp);
			
			if (actionsMap.containsKey(ACTIONS.ACTION_MOVE_DOWN_JOB))
				toolBar.add(btnMovedown);
					
			jobPropertiesPanel.setBorder(b);
			jobPropertiesPanel.add(scrollPane, BorderLayout.CENTER);
			jobPropertiesPanel.add(toolBar, BorderLayout.EAST);
			jobPropertiesPanel.setPreferredSize(new Dimension(0, 250));
		}
		
		this.eventSupportPropertiesTable = propTableJobModels;
		return this.jobPropertiesPanel;
	}
	protected JPanel createStatePropertiesPanel(Border b) {
		
		String[] transitions = this.esView.getTargetStateModel().getTransitionsAsArray();
		
		if (statePropertiesPanel == null) {
			statePropertiesPanel = new JPanel();
			statePropertiesPanel.setLayout(new BorderLayout());
			
			final JToolBar toolBar = new JToolBar(JToolBar.VERTICAL);
			Map<ACTIONS, AbstractAction> actionsMap = esView.getActionsMap();
			
			final JButton btnSaveAll = new JButton(actionsMap.get(ACTIONS.ACTION_SAVE_ALL_STATETRANSITION));
			final JButton btnDelete = new JButton(actionsMap.get(ACTIONS.ACTION_DELETE_STATETRANSITION));
			final JButton btnMoveUp = new JButton(actionsMap.get(ACTIONS.ACTION_MOVE_UP_STATETRANSITION));
			final JButton btnMovedown = new JButton(actionsMap.get(ACTIONS.ACTION_MOVE_DOWN_STATETRANSITION));
			
			
			btnDelete.setEnabled(false);
			btnMoveUp.setEnabled(false);
			btnMovedown.setEnabled(false);
			
			propTableStateModels = new JTable(this.esView.getTargetStateModel());
			propTableStateModels.setFillsViewportHeight(true);
			propTableStateModels.setRowSelectionAllowed(true);
			
			propTableStateModels.getColumnModel().getColumn(1).setCellEditor(new DefaultCellEditor(new JComboBox(transitions)));
			
			propTableStateModels.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
				
				@Override
				public void valueChanged(ListSelectionEvent e) {
					if(e.getValueIsAdjusting()) {
						boolean enableButtons = false;
						if (propTableStateModels.getSelectedRow() >= 0) {
							enableButtons = true;
						}
					
						toolBar.getComponent(toolBar.getComponentIndex(btnDelete)).setEnabled(enableButtons);
						toolBar.getComponent(toolBar.getComponentIndex(btnMoveUp)).setEnabled(enableButtons);
						toolBar.getComponent(toolBar.getComponentIndex(btnMovedown)).setEnabled(enableButtons);
					}
				}
			});

			JScrollPane scrollPane = new JScrollPane(propTableStateModels);
			
			toolBar.setBorderPainted(false);
			
			if (actionsMap.containsKey(ACTIONS.ACTION_SAVE_ALL_STATETRANSITION))
				toolBar.add(btnSaveAll);
			
			if (actionsMap.containsKey(ACTIONS.ACTION_DELETE_STATETRANSITION))
				toolBar.add(btnDelete);
			
			toolBar.addSeparator();
			
			if (actionsMap.containsKey(ACTIONS.ACTION_MOVE_UP_STATETRANSITION))
				toolBar.add(btnMoveUp);
			
			if (actionsMap.containsKey(ACTIONS.ACTION_MOVE_DOWN_STATETRANSITION))
				toolBar.add(btnMovedown);
					
			statePropertiesPanel.setBorder(b);
			statePropertiesPanel.add(scrollPane, BorderLayout.CENTER);
			statePropertiesPanel.add(toolBar, BorderLayout.EAST);
			statePropertiesPanel.setPreferredSize(new Dimension(0, 250));
		}
		else
		{
			propTableStateModels.getColumnModel().getColumn(1).setCellEditor(new DefaultCellEditor(new JComboBox(transitions)));
			
		}
		this.eventSupportPropertiesTable = propTableStateModels;
		return this.statePropertiesPanel;
	}
	
	protected JPanel createEntityPropertiesPanel(Border b) {
		
		// Use cached panel if exists
		String[] status = this.esView.getTargetEntityModel().getStatusAsArray();
		String[] process = this.esView.getTargetEntityModel().getProcessAsArray();
		
		if (this.entityPropertiesPanel == null) {
			this.entityPropertiesPanel = new JPanel();
			entityPropertiesPanel.setLayout(new BorderLayout());
			
			Map<ACTIONS, AbstractAction> actionsMap = esView.getActionsMap();
			
			final JToolBar toolBar = new JToolBar(JToolBar.VERTICAL);
			
			final JButton btnSaveAll = new JButton(actionsMap.get(ACTIONS.ACTION_SAVE_ALL_EVENTS));
			final JButton btnDelete = new JButton(actionsMap.get(ACTIONS.ACTION_DELETE_EVENT));
			final JButton btnMoveUp = new JButton(actionsMap.get(ACTIONS.ACTION_MOVE_UP_EVENT));
			final JButton btnMovedown = new JButton(actionsMap.get(ACTIONS.ACTION_MOVE_DOWN_EVENT));
			
		
			btnDelete.setEnabled(false);
			btnMoveUp.setEnabled(false);
			btnMovedown.setEnabled(false);
			
			propTableEntites = new JTable(this.esView.getTargetEntityModel());
			
			propTableEntites.setFillsViewportHeight(true);
			propTableEntites.setRowSelectionAllowed(true);
			
			propTableEntites.getColumnModel().getColumn(0).setMaxWidth(220);
			propTableEntites.getColumnModel().getColumn(0).setMinWidth(20);
			propTableEntites.getColumnModel().getColumn(0).setPreferredWidth(200);
			
			propTableEntites.getColumnModel().getColumn(1).setCellEditor(new DefaultCellEditor(new JComboBox(status)));
			propTableEntites.getColumnModel().getColumn(2).setCellEditor(new DefaultCellEditor(new JComboBox(process)));
					
			propTableEntites.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
				
				@Override
				public void valueChanged(ListSelectionEvent e) {
					if(e.getValueIsAdjusting()) {
						boolean enableButtons = false;
						if (propTableEntites.getSelectedRow() >= 0) {
							enableButtons = true;
						}
						
						toolBar.getComponent(toolBar.getComponentIndex(btnDelete)).setEnabled(enableButtons);
						toolBar.getComponent(toolBar.getComponentIndex(btnMoveUp)).setEnabled(enableButtons);
						toolBar.getComponent(toolBar.getComponentIndex(btnMovedown)).setEnabled(enableButtons);
					}
				}
			});
			
			JScrollPane scrollPane = new JScrollPane(propTableEntites);
			
			toolBar.setBorderPainted(false);	
			
			if (actionsMap.containsKey(ACTIONS.ACTION_SAVE_ALL_EVENTS))
				toolBar.add(btnSaveAll);
			
			if (actionsMap.containsKey(ACTIONS.ACTION_DELETE_EVENT))
				toolBar.add(btnDelete);
			
			toolBar.addSeparator();
			
			if (actionsMap.containsKey(ACTIONS.ACTION_MOVE_UP_EVENT))
				toolBar.add(btnMoveUp);
			
			if (actionsMap.containsKey(ACTIONS.ACTION_MOVE_DOWN_EVENT))
				toolBar.add(btnMovedown);
			
			entityPropertiesPanel.setBorder(b);
			entityPropertiesPanel.add(scrollPane, BorderLayout.CENTER);
			entityPropertiesPanel.add(toolBar, BorderLayout.EAST);
			entityPropertiesPanel.setPreferredSize(new Dimension(0, 250));
		}
		else
		{
			propTableEntites.getColumnModel().getColumn(1).setCellEditor(new DefaultCellEditor(new JComboBox(status)));
			propTableEntites.getColumnModel().getColumn(2).setCellEditor(new DefaultCellEditor(new JComboBox(process)));
		}
		
		this.eventSupportPropertiesTable = propTableEntites;
		return entityPropertiesPanel;	
	}
	
}
