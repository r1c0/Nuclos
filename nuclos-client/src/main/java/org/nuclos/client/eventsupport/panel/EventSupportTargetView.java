package org.nuclos.client.eventsupport.panel;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.border.Border;

import org.nuclos.client.explorer.EventSupportExplorerView;
import org.nuclos.client.explorer.EventSupportTargetExplorerView;
import org.nuclos.client.explorer.ExplorerView;

public class EventSupportTargetView extends JPanel {

	EventSupportTargetExplorerView explorerView;
	EventSupportView esView;
	JSplitPane splitPanelEventSupport;

	EventSupportStatePropertyPanel 		statePropertiesPanel;
	EventSupportEntityPropertyPanel 	entityPropertiesPanel;
	EventSupportJobProperyPanel 		jobPropertiesPanel;
	EventSupportGenerationPropertyPanel generationPropertiesPanel;
	
	public EventSupportTargetView(EventSupportView view, Border b)
	{
		super(new BorderLayout());
		
		esView = view;
		explorerView = new EventSupportTargetExplorerView(esView.getTreeEventSupportTargets(), view.getActionsMap());
		
		splitPanelEventSupport = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		
		// Tree		
		JPanel pnlEventTypes = new JPanel();			
		pnlEventTypes.setLayout(new BorderLayout());
		pnlEventTypes.add(explorerView.getViewComponent(), BorderLayout.CENTER);
		pnlEventTypes.setBorder(b);
				
		splitPanelEventSupport.setTopComponent(pnlEventTypes);
		// Properties
		splitPanelEventSupport.setBottomComponent(null);
		splitPanelEventSupport.setResizeWeight(1d);
		
		this.add(splitPanelEventSupport, BorderLayout.CENTER);
	}
	
	public ExplorerView getExplorerView() {
		return this.explorerView;
	}
	
	public JTree getTree()
	{
		return this.explorerView.getJTree();
	}
	
	public void showPropertyPanel(AbstractEventSupportPropertyPanel propertyPanelToShow)
	{
		Border b = BorderFactory.createMatteBorder(1, 1, 0, 1, Color.LIGHT_GRAY);
		
		// Remove old Property Panel 
		if (splitPanelEventSupport.getBottomComponent() != null)
			splitPanelEventSupport.remove(splitPanelEventSupport.getBottomComponent());
		
		// ...and load the new according to the given modeltype
		splitPanelEventSupport.setBottomComponent(propertyPanelToShow);
		splitPanelEventSupport.repaint();
	}
	
	public EventSupportGenerationPropertyPanel getGenerationPropertiesPanel() {
		// Use cached panel if exists
		if (generationPropertiesPanel == null) {
			generationPropertiesPanel = new EventSupportGenerationPropertyPanel(this.esView.getActionsMap());
		}
		
		return this.generationPropertiesPanel;
	}
	
	public EventSupportJobProperyPanel getJobPropertiesPanel() {
		// Use cached panel if exists
		if (jobPropertiesPanel == null) {
			jobPropertiesPanel = new EventSupportJobProperyPanel(this.esView.getActionsMap());
		}
		
		return this.jobPropertiesPanel;
	}
	
	
	public EventSupportStatePropertyPanel getStatePropertiesPanel() {
		// Use cached panel if exists
		if (statePropertiesPanel == null) {
			statePropertiesPanel = new EventSupportStatePropertyPanel(this.esView.getActionsMap());
		}
		else {
			statePropertiesPanel.reloadTransitions();
		}
		return this.statePropertiesPanel;
	}
	
	public EventSupportEntityPropertyPanel getEntityPropertiesPanel() {
		// Use cached panel if exists
		if (entityPropertiesPanel == null) {
			this.entityPropertiesPanel = new EventSupportEntityPropertyPanel(this.esView.getActionsMap());
		}
		else
		{
			entityPropertiesPanel.reloadValueLists();
		}
		return entityPropertiesPanel;	
	}	
}
