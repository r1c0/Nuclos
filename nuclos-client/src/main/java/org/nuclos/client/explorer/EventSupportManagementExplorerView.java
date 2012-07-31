package org.nuclos.client.explorer;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;

import org.nuclos.client.main.Main;
import org.nuclos.client.ui.Icons;
import org.nuclos.client.ui.PopupButton;
import org.nuclos.client.ui.UIUtils;
import org.nuclos.common2.CommonRunnable;
import org.nuclos.common2.SpringLocaleDelegate;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.server.navigation.treenode.TreeNode;

public class EventSupportManagementExplorerView extends DefaultExplorerView 
{
	
	public EventSupportManagementExplorerView(TreeNode tn)
	{
		super(tn);
	}
	
	protected List<JComponent> getToolBarComponents() {
		List<JComponent> components = new ArrayList<JComponent>();
		components.add(new JButton(new AbstractAction("", Icons.getInstance().getIconRefresh16()) {

			@Override
			public void actionPerformed(ActionEvent e) {
				UIUtils.runCommand(EventSupportManagementExplorerView.this.getParent(), new CommonRunnable() {
					@Override
		            public void run() throws CommonFinderException {
						Main.getInstance().getMainController().getExplorerController().refreshTab(EventSupportManagementExplorerView.this);
					}
				});
			}
		}));
		
		components.add(new JButton(new AbstractAction("", Icons.getInstance().getIconNew16()) {

			@Override
			public void actionPerformed(ActionEvent e) {
				UIUtils.runCommand(EventSupportManagementExplorerView.this.getParent(), new CommonRunnable() {
					@Override
		            public void run() throws CommonFinderException {
						Main.getInstance().getMainController().getExplorerController().refreshTab(EventSupportManagementExplorerView.this);
					}
				});
			}
		}));
		
		components.add(new PopupButton(SpringLocaleDelegate.getInstance().getMessage("PopupButton.Extras","Extras")));
	
	
		return components;
	}
}
