package org.nuclos.client.eventsupport.panel;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.border.Border;

import org.nuclos.client.explorer.EventSupportExplorerView;
import org.nuclos.client.explorer.ExplorerView;


public class EventSupportSourceView extends JPanel {
	
	private EventSupportExplorerView explorerView;
	private EventSupportSourcePropertyPanel sourcePanel;
	
	private JSplitPane splitPanelEventSupport;
	
	public EventSupportSourceView(EventSupportView view, Border b)
	{
		super(new BorderLayout());
		
		explorerView = new EventSupportExplorerView(view.getTreeEventSupports(), view.getActionsMap());
		explorerView.getJTree().setRootVisible(false);
		
		splitPanelEventSupport = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		
		JPanel pnlEventTypes = new JPanel();			
		pnlEventTypes.setLayout(new BorderLayout());
		pnlEventTypes.add(explorerView.getViewComponent(), BorderLayout.CENTER);
		pnlEventTypes.setBorder(b);
		
		// Tree		
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

	public void showSourcePropertyPanel(EventSupportSourcePropertyPanel propertyPanelToShow)
	{
		// Remove old Property Panel 
		if (splitPanelEventSupport.getBottomComponent() != null)
			splitPanelEventSupport.remove(splitPanelEventSupport.getBottomComponent());
		
		// ...and load the new according to the given modeltype
		splitPanelEventSupport.setBottomComponent(propertyPanelToShow);
		splitPanelEventSupport.repaint();
	}
	
	public EventSupportSourcePropertyPanel getSourcePropertiesPanel() {
		// Use cached panel if exists
		if (sourcePanel == null) {
			sourcePanel = new EventSupportSourcePropertyPanel();
		}
		
		return this.sourcePanel;
	}
}
