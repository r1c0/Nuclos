package org.nuclos.client.eventsupport.panel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
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
import javax.swing.table.AbstractTableModel;

import org.nuclos.client.eventsupport.EventSupportManagementController.ACTIONS;
import org.nuclos.client.eventsupport.model.EventSupportEntityPropertiesTableModel;
import org.nuclos.client.eventsupport.model.EventSupportStatePropertiesTableModel;
import org.nuclos.client.explorer.EventSupportExplorerView;
import org.nuclos.client.explorer.EventSupportTargetExplorerView;
import org.nuclos.client.explorer.ExplorerView;

public class EventSupportTargetView extends JPanel {

	JSplitPane splitPanelEventSupport;
	EventSupportExplorerView explorerView;
	EventSupportView esView;

	JTable propTable;
	
	JPanel entityPropertiesPanel;
	JPanel statePropertiesPanel;
	
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
	
	public JTable getPropertyTable(){
		return this.propTable;
	}
	
	public JTree getTree()
	{
		return this.explorerView.getJTree();
	}
	
	public void loadPropertyPanelByModelType(AbstractTableModel model)
	{
		// Remove old Property Panel 
		if (splitPanelEventSupport.getBottomComponent() != null)
			splitPanelEventSupport.remove(splitPanelEventSupport.getBottomComponent());
		
		// ...and load the new according to the given modeltype
		splitPanelEventSupport.setBottomComponent(getTargetPropertyByType(model));
		splitPanelEventSupport.repaint();

	}
	
	public JPanel getTargetPropertyByType(AbstractTableModel model)
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
	
	protected JPanel createStatePropertiesPanel(Border b) {
		if (statePropertiesPanel == null) {
			statePropertiesPanel = new JPanel();
			statePropertiesPanel.setLayout(new BorderLayout());
			
			JTable propTable = new JTable(this.esView.getTargetStateModel());
			propTable.setFillsViewportHeight(true);
			propTable.setRowSelectionAllowed(true);
			propTable.setCellSelectionEnabled(false);
			propTable.setColumnSelectionAllowed(false);
			
			propTable.getColumnModel().getColumn(0).setMaxWidth(120);
			propTable.getColumnModel().getColumn(0).setMinWidth(20);
			propTable.getColumnModel().getColumn(0).setPreferredWidth(100);
			
			JScrollPane scrollPane = new JScrollPane(propTable);
			
			JToolBar toolBar = new JToolBar(JToolBar.VERTICAL);
			
			toolBar.setBorderPainted(false);
			
			Map<ACTIONS, AbstractAction> actionsMap = esView.getActionsMap();;
			
			if (actionsMap.containsKey(ACTIONS.ACTION_DELETE))
				toolBar.add(new JButton(actionsMap.get(ACTIONS.ACTION_DELETE)));
			
			toolBar.addSeparator();
			
			if (actionsMap.containsKey(ACTIONS.ACTION_MOVE_UP))
				toolBar.add(new JButton(actionsMap.get(ACTIONS.ACTION_MOVE_UP)));
			
			if (actionsMap.containsKey(ACTIONS.ACTION_MOVE_DOWN))
				toolBar.add(new JButton(actionsMap.get(ACTIONS.ACTION_MOVE_DOWN)));
					
			statePropertiesPanel.setBorder(b);
			statePropertiesPanel.add(scrollPane, BorderLayout.CENTER);
			statePropertiesPanel.add(toolBar, BorderLayout.EAST);
			statePropertiesPanel.setPreferredSize(new Dimension(0, 250));
		}
		
		return this.statePropertiesPanel;
	}
	
	protected JPanel createEntityPropertiesPanel(Border b) {
		
		// Use cached panel if exists
		String[] status = this.esView.getTargetEntityModel().getStatusAsArray();
		String[] process = this.esView.getTargetEntityModel().getProcessAsArray();
		
		if (this.entityPropertiesPanel == null) {
			this.entityPropertiesPanel = new JPanel();
			entityPropertiesPanel.setLayout(new BorderLayout());
			
			propTable = new JTable(this.esView.getTargetEntityModel());
			
			propTable.setFillsViewportHeight(true);
			propTable.setRowSelectionAllowed(true);
			propTable.addMouseMotionListener(new StatusModelMouseEventAdapter());
			
			
			propTable.getColumnModel().getColumn(0).setMaxWidth(120);
			propTable.getColumnModel().getColumn(0).setMinWidth(20);
			propTable.getColumnModel().getColumn(0).setPreferredWidth(100);
			
			propTable.getColumnModel().getColumn(1).setMaxWidth(220);
			propTable.getColumnModel().getColumn(1).setMinWidth(20);
			propTable.getColumnModel().getColumn(1).setPreferredWidth(200);
			
			propTable.getColumnModel().getColumn(2).setCellEditor(new DefaultCellEditor(new JComboBox(status)));
			propTable.getColumnModel().getColumn(3).setCellEditor(new DefaultCellEditor(new JComboBox(process)));
			
			JScrollPane scrollPane = new JScrollPane(propTable);
			
			JToolBar toolBar = new JToolBar(JToolBar.VERTICAL);
			
			toolBar.setBorderPainted(false);
			
			Map<ACTIONS, AbstractAction> actionsMap = esView.getActionsMap();;
			
			if (actionsMap.containsKey(ACTIONS.ACTION_SAVE))
				toolBar.add(new JButton(actionsMap.get(ACTIONS.ACTION_SAVE)));
			
			if (actionsMap.containsKey(ACTIONS.ACTION_DELETE))
				toolBar.add(new JButton(actionsMap.get(ACTIONS.ACTION_DELETE)));
			
			toolBar.addSeparator();
			
			if (actionsMap.containsKey(ACTIONS.ACTION_MOVE_UP))
				toolBar.add(new JButton(actionsMap.get(ACTIONS.ACTION_MOVE_UP)));
			
			if (actionsMap.containsKey(ACTIONS.ACTION_MOVE_DOWN))
				toolBar.add(new JButton(actionsMap.get(ACTIONS.ACTION_MOVE_DOWN)));
			
			entityPropertiesPanel.setBorder(b);
			entityPropertiesPanel.add(scrollPane, BorderLayout.CENTER);
			entityPropertiesPanel.add(toolBar, BorderLayout.EAST);
			entityPropertiesPanel.setPreferredSize(new Dimension(0, 250));
		}
		else
		{
			propTable.getColumnModel().getColumn(2).setCellEditor(new DefaultCellEditor(new JComboBox(status)));
			propTable.getColumnModel().getColumn(3).setCellEditor(new DefaultCellEditor(new JComboBox(process)));
		}
		
		return entityPropertiesPanel;	
	}
	
	public class StatusModelMouseEventAdapter extends MouseMotionAdapter {
		public void mouseMoved(MouseEvent e) {
			JTable aTable = (JTable)e.getSource();
			int itsRow = aTable.rowAtPoint(e.getPoint());
			int itsColumn = aTable.columnAtPoint(e.getPoint());
			aTable.repaint();
		}
	}
}
