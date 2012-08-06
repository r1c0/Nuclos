package org.nuclos.client.eventsupport.panel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.TransferHandler;
import javax.swing.border.Border;
import javax.swing.table.AbstractTableModel;

import org.nuclos.client.eventsupport.model.EventSupportEntityPropertiesTableModel;
import org.nuclos.client.eventsupport.model.EventSupportStatePropertiesTableModel;
import org.nuclos.client.explorer.ExplorerView;
import org.nuclos.client.explorer.ExplorerViewFactory;
import org.nuclos.client.masterdata.datatransfer.RuleAndRuleUsageEntity;
import org.nuclos.client.masterdata.datatransfer.RuleCVOTransferable;
import org.nuclos.client.rule.admin.CollectableRule;
import org.nuclos.client.ui.Icons;
import org.nuclos.common.collect.collectable.Collectable;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.server.navigation.treenode.TreeNode;

public class EventSupportTargetViewElement extends JPanel {
	
	EventSupportEntityPropertiesTableModel entModel;
	EventSupportStatePropertiesTableModel stateModel;	
	TreeNode treenode;
	JSplitPane splitPanelEventSupport;
	
	JPanel entityPropertiesPanel;
	JPanel statePropertiesPanel;
	
	public EventSupportTargetViewElement(EventSupportManagementView view, Border b)
	{
		super(new BorderLayout());
		
		this.treenode = view.getTreeEventSupportTargets();
		this.entModel = view.getTargetEntityModel();
		this.stateModel = view.getTargetStateModel();
		
		ExplorerView newExplorerView = ExplorerViewFactory.getInstance().newExplorerView(this.treenode);
		
		splitPanelEventSupport = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		
		// Tree		
		splitPanelEventSupport.setTopComponent(createTreePanel(newExplorerView, b));
		// Properties
		splitPanelEventSupport.setBottomComponent(getTargetPropertyByType(entModel));
		
		splitPanelEventSupport.setResizeWeight(1d);
		
		this.add(splitPanelEventSupport, BorderLayout.CENTER);
	}
	
	public void loadPropertyPanelByModelType(AbstractTableModel model)
	{
		// Remove old Property Panel 
		if (splitPanelEventSupport.getBottomComponent() != null)
			splitPanelEventSupport.remove(splitPanelEventSupport.getBottomComponent());
		
		// ...and load the new according to the given modeltype
		splitPanelEventSupport.setBottomComponent(getTargetPropertyByType(model));

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
		pnlEventTypes.setLayout(new GridBagLayout());
		
		GridBagConstraints gbc = new GridBagConstraints();
		
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		
		pnlEventTypes.add(newExplorerView.getViewComponent(), gbc);
		pnlEventTypes.setBorder(b);
		
		return pnlEventTypes;
	}
	
	protected JPanel createStatePropertiesPanel(Border b) {
		if (this.statePropertiesPanel == null)
		{
			statePropertiesPanel = new JPanel();
			statePropertiesPanel.setLayout(new GridBagLayout());
			
			GridBagConstraints gbc = new GridBagConstraints();
			
			gbc.fill = GridBagConstraints.BOTH;
			gbc.weightx = 1.0;
			gbc.weighty = 1.0;
			
			JTable propTable = new JTable(this.stateModel);
			propTable.setFillsViewportHeight(true);
			propTable.setRowSelectionAllowed(false);
			propTable.setCellSelectionEnabled(false);
			propTable.setColumnSelectionAllowed(false);
			
			JScrollPane scrollPane = new JScrollPane(propTable);
			
			statePropertiesPanel.setBorder(b);
			statePropertiesPanel.add(scrollPane, gbc);
			statePropertiesPanel.setPreferredSize(new Dimension(0, 250));
			
		}
		return this.statePropertiesPanel;
	}
	
	protected JPanel createEntityPropertiesPanel(Border b) {
		
		// Use cached panel if exists
		if (this.entityPropertiesPanel == null)
		{
			this.entityPropertiesPanel = new JPanel();
			entityPropertiesPanel.setLayout(new BorderLayout());
			
			JTable propTable = new JTable(this.entModel);
						
			propTable.setFillsViewportHeight(true);
			propTable.setRowSelectionAllowed(false);
			propTable.setCellSelectionEnabled(false);
			propTable.setColumnSelectionAllowed(false);
			propTable.setDragEnabled(true);
			
			JScrollPane scrollPane = new JScrollPane(propTable);
			
			JToolBar toolBar = new JToolBar(JToolBar.VERTICAL);
			
			toolBar.setBorderPainted(false);
		
			toolBar.add(new JButton(new AbstractAction("", Icons.getInstance().getIconSaveS16()) {

				@Override
				public void actionPerformed(ActionEvent e) {
					e.getClass();
				}
			}));

			toolBar.add(new JButton(new AbstractAction("", Icons.getInstance().getIconDelete16()) {

				@Override
				public void actionPerformed(ActionEvent e) {
					e.getClass();
				}
			}));	
			toolBar.addSeparator();
			toolBar.add(new JButton(new AbstractAction("", Icons.getInstance().getIconUp16()) {

				@Override
				public void actionPerformed(ActionEvent e) {
					e.getClass();
				}
			}));
			toolBar.add(new JButton(new AbstractAction("", Icons.getInstance().getIconDown16()) {

				@Override
				public void actionPerformed(ActionEvent e) {
					e.getClass();
				}
			}));
			
			entityPropertiesPanel.setBorder(b);
			entityPropertiesPanel.add(scrollPane, BorderLayout.CENTER);
			entityPropertiesPanel.add(toolBar, BorderLayout.EAST);
			entityPropertiesPanel.setPreferredSize(new Dimension(0, 250));
		}
		
		return entityPropertiesPanel;	
	}
}
