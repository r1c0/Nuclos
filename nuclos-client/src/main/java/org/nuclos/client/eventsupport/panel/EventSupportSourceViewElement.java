package org.nuclos.client.eventsupport.panel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.table.AbstractTableModel;

import org.nuclos.client.explorer.ExplorerView;
import org.nuclos.client.explorer.ExplorerViewFactory;
import org.nuclos.server.navigation.treenode.TreeNode;

public class EventSupportSourceViewElement extends JPanel {
	private TreeNode treenode;
	private AbstractTableModel model;
	
	public EventSupportSourceViewElement(EventSupportManagementView view, Border b)
	{
		super(new BorderLayout());
		
		this.treenode = view.getTreeEventSupports();
		this.model = view.getPropertyModel();
		
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
	
	protected JPanel createPropertiesPanel(Border b) {
		
		JPanel pnlPropertiers = new JPanel();
		pnlPropertiers.setLayout(new GridBagLayout());
	
		GridBagConstraints gbc = new GridBagConstraints();
		
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
						
		JTable propTable = new JTable(model);
					
		propTable.setFillsViewportHeight(true);
		propTable.setRowSelectionAllowed(false);
		propTable.setCellSelectionEnabled(false);
		propTable.setColumnSelectionAllowed(false);
		
		JScrollPane scrollPane = new JScrollPane(propTable);
		scrollPane.setPreferredSize(new Dimension(0, 250));
		
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
