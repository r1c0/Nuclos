package org.nuclos.client.explorer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.JTree;

import org.nuclos.client.eventsupport.EventSupportActionHandler.ACTIONS;
import org.nuclos.client.explorer.node.eventsupport.EventSupportDropListener;
import org.nuclos.client.synthetica.NuclosThemeSettings;
import org.nuclos.client.ui.CenteringPanel;
import org.nuclos.client.ui.ColoredLabel;
import org.nuclos.client.ui.Icons;
import org.nuclos.common2.SpringLocaleDelegate;
import org.nuclos.server.navigation.treenode.TreeNode;

public class EventSupportTargetExplorerView  extends EventSupportExplorerView {


	public EventSupportTargetExplorerView(TreeNode tn, Map<ACTIONS, AbstractAction> actions) {
		super(tn, actions);
	}
	
	protected void addDNDFunctionality(JTree pTree) {
		EventSupportDropListener dndListener = new EventSupportDropListener();
		DropTarget drpTaget = new DropTarget(pTree, 
		        DnDConstants.ACTION_MOVE, dndListener);
	}
	
	protected void addToolBarComponents(JToolBar toolbar) {
		if (getActions().containsKey(ACTIONS.ACTION_REFRESH_TARGETTREE))
			toolbar.add(new JButton(getActions().get(ACTIONS.ACTION_REFRESH_TARGETTREE)));
		setSearchTextField(new JTextField());
		final AbstractAction abstractAction = getActions().get(ACTIONS.ACTION_RUN_TARGETTREE_SEARCH);
		getSearchTextField().addKeyListener(new KeyListener() {
			
			@Override
			public void keyTyped(KeyEvent e) {}
			
			@Override
			public void keyReleased(KeyEvent e) {}
			
			@Override
			public void keyPressed(KeyEvent e) {
				JTextField txt = (JTextField) e.getSource();
				if (txt.getText().length() > 3 || txt.getText().length() == 0) {
					abstractAction.actionPerformed(new ActionEvent(e.getSource(), e.getID(), null));
				}
			}
		});
			
		
		getSearchTextField().setPreferredSize(new Dimension(150,20));
		
		JPanel pnlSearch = new JPanel();
		pnlSearch.setLayout(new BorderLayout());
		pnlSearch.setOpaque(false);
//		pnlSearch.setBackground(NuclosThemeSettings.BACKGROUND_COLOR3);
		pnlSearch.add(getSearchTextField(), BorderLayout.WEST);
//		JButton jButton = new JButton( Icons.getInstance().getIconTextFieldButtonLOV());
//		jButton.setPreferredSize(new Dimension(20, 20));
//		jButton.setOpaque(true);
//		jButton.setBackground(Color.WHITE);
//		
//		pnlSearch.add(jButton, BorderLayout.CENTER);
				
		CenteringPanel cpSearchFilter = new CenteringPanel(pnlSearch) {

			@Override
			public Dimension getMinimumSize() {
				return this.getCenteredComponent().getMinimumSize();
			}

			@Override
			public Dimension getMaximumSize() {
				return this.getCenteredComponent().getPreferredSize();
			}

		};
		cpSearchFilter.setOpaque(false);
		ColoredLabel bl = new ColoredLabel(cpSearchFilter, SpringLocaleDelegate.getInstance().getMessage("CollectController.Search.Filter","Filter"));
		bl.setName("blChooseFilter");
		

		toolbar.add(bl);
	}

}
