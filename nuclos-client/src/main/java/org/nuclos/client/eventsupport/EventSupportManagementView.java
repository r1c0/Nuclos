package org.nuclos.client.eventsupport;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableModel;

import org.nuclos.client.explorer.ExplorerView;
import org.nuclos.client.explorer.ExplorerViewFactory;
import org.nuclos.client.explorer.node.eventsupport.EventSupportTargetTreeNode;
import org.nuclos.client.explorer.node.eventsupport.EventSupportTreeNode;
import org.nuclos.server.navigation.treenode.TreeNode;

public class EventSupportManagementView extends JPanel {

	EventSupportTreeNode treeEventSupports;
	EventSupportTargetTreeNode treeEventSupportTargets;
	
	public EventSupportManagementView(EventSupportTreeNode pTreeEventSupports, EventSupportTargetTreeNode pTreeEventSupportTargets)
	{
		super(new BorderLayout());
		
		if (this.treeEventSupports == null)
		{
			this.treeEventSupports = pTreeEventSupports;
		}

		if (this.treeEventSupportTargets == null)
		{
			this.treeEventSupportTargets = pTreeEventSupportTargets;
		}
		
		final JSplitPane splitpn = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
	
		Border b1 = BorderFactory.createMatteBorder(0, 0, 0, 1, Color.LIGHT_GRAY);
		Border b2 = BorderFactory.createMatteBorder(0, 1, 0, 0, Color.LIGHT_GRAY);
		
		splitpn.setTopComponent(
				new EventSupportViewElement(EventSupportManagementView.this.treeEventSupports, b1));
		splitpn.setBottomComponent(
				new EventSupportViewElement(EventSupportManagementView.this.treeEventSupportTargets, b2));
		
		splitpn.setResizeWeight(0.5d);
		splitpn.setDividerSize(5);
		this.add(splitpn, BorderLayout.CENTER);
		
	}
	

	class EventSupportViewElement extends JPanel
	{
		private TreeNode treenode;
		
		public EventSupportViewElement(TreeNode pTreenode, Border b)
		{
			super(new BorderLayout());
			
			this.treenode = pTreenode;
			
			ExplorerView newExplorerView = ExplorerViewFactory.getInstance().newExplorerView(this.treenode);
			
			final JSplitPane splitpn = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
			
			Border b2 = BorderFactory.createMatteBorder(1, 1, 0, 1, Color.LIGHT_GRAY);
			
			// Tree		
			splitpn.setTopComponent(createTreePanel(newExplorerView, b));
			// Properties
			splitpn.setBottomComponent(createPropertiesPanel(b2));
			
			splitpn.setResizeWeight(1d);
			
			this.add(splitpn, BorderLayout.CENTER);
			
		}
		
		private JPanel createPropertiesPanel(Border b) {
			
			JPanel pnlPropertiers = new JPanel();
			pnlPropertiers.setLayout(new GridBagLayout());
		
			GridBagConstraints gbc = new GridBagConstraints();
			
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.weightx = 1.0;
		
			String[] columnNames = {"Column1", "Column2", "Column3"};
			 
			Object[][] data = {
					{ "A1", "A2", "A3" },
					{ "B1", "B2", "B3" },
					{ "C1", "C2", "C3" },
					{ "D1", "D2", "D3" } 
				};
			
			DefaultTableModel tblmdl = new DefaultTableModel(data, columnNames);
			
			JTable propTable = new JTable(tblmdl);
			propTable.setFillsViewportHeight(true);
			propTable.setRowSelectionAllowed(false);
			propTable.setCellSelectionEnabled(false);
			propTable.setColumnSelectionAllowed(false);
			
			JScrollPane scrollPane = new JScrollPane(propTable);
		
			pnlPropertiers.add(scrollPane, gbc);
			pnlPropertiers.setBorder(b);
			
			return pnlPropertiers;
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
	}
	
}
