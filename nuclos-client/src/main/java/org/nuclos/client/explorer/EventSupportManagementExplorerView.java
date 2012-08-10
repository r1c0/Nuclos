package org.nuclos.client.explorer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureRecognizer;
import java.awt.dnd.DragSource;
import java.awt.dnd.DropTarget;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.ToolTipManager;

import org.nuclos.client.explorer.node.eventsupport.EventSupportDragListener;
import org.nuclos.client.explorer.node.eventsupport.EventSupportDropListener;
import org.nuclos.client.explorer.ui.ExplorerNodeRenderer;
import org.nuclos.client.main.Main;
import org.nuclos.client.synthetica.NuclosThemeSettings;
import org.nuclos.client.ui.Icons;
import org.nuclos.client.ui.PopupButton;
import org.nuclos.client.ui.UIUtils;
import org.nuclos.common2.CommonRunnable;
import org.nuclos.common2.SpringLocaleDelegate;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.server.navigation.treenode.TreeNode;

public class EventSupportManagementExplorerView extends JPanel implements ExplorerView 
{
	public static final int FADE = 2;
	private final JToolBar toolBar = UIUtils.createNonFloatableToolBar();
	private final JScrollPane scrlpn = new JScrollPane();
	private final JTree tree;
	private List<JComponent> additionalToolBarContents;

	private final JPanel content = new JPanel(new BorderLayout()) {

		@Override
		public void paint(Graphics g) {
			Graphics2D g2 = (Graphics2D) g;
			g2.setPaint(new GradientPaint(0, 0, NuclosThemeSettings.BACKGROUND_PANEL, 0, FADE, Color.WHITE));
			g2.fillRect(0, 0, getWidth(), getHeight());

			super.paint(g);
		}
	};
	
	public EventSupportManagementExplorerView(TreeNode tn, boolean isSourceTree)
	{
		super(new BorderLayout());
		this.setOpaque(false);
		this.setBorder(BorderFactory.createEmptyBorder());

		this.scrlpn.setBorder(BorderFactory.createEmptyBorder());
		this.scrlpn.setOpaque(false);
		this.scrlpn.getViewport().setOpaque(false);

		this.content.setOpaque(true);
		this.content.setBackground(new Color (0, 0, 0, 0));
		this.content.setBorder(BorderFactory.createEmptyBorder(FADE, 0, 0, 0));
		this.content.add(this.scrlpn, BorderLayout.CENTER);

		this.add(this.toolBar, BorderLayout.NORTH);
		this.add(this.content, BorderLayout.CENTER);

		final ExplorerNode<?> explorernodeRoot = ExplorerController.newExplorerTree(tn);
		this.tree = new JTree(explorernodeRoot, true);

		this.tree.putClientProperty("JTree.lineStyle", "Angled");
		this.tree.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 5));
		this.tree.setBackground(Color.WHITE);
		this.tree.setRootVisible(true);
		this.tree.setShowsRootHandles(true);

		if (isSourceTree)
		{
			initDragFunctionality(this.tree);			
		}
		else
		{
			initDropFunctionality(this.tree);
		}

		// don't expand on double click:
		this.tree.setToggleClickCount(0);

		this.tree.addKeyListener(new DefaultKeyListener(tree));
		this.tree.addMouseListener(new DefaultMouseListener(tree));

		// enable tool tips:
		ToolTipManager.sharedInstance().registerComponent(this.tree);

		this.tree.setCellRenderer(new ExplorerNodeRenderer());
		this.tree.addTreeWillExpandListener(new DefaultTreeWillExpandListener(tree));

		this.scrlpn.getViewport().add(tree, null);	
	}
	
	private void initDropFunctionality(JTree pTree) {
		EventSupportDropListener dndListener = new EventSupportDropListener();
		DropTarget drpTaget = new DropTarget(pTree, 
		        DnDConstants.ACTION_MOVE, dndListener);
		
	}

	private void initDragFunctionality(JTree pTree) {
		EventSupportDragListener dndListener = new EventSupportDragListener();
		DragSource src = new DragSource();
		DragGestureRecognizer gestRec = src.
                createDefaultDragGestureRecognizer(pTree, 
                DnDConstants.ACTION_MOVE, dndListener);
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


	@Override
	public JComponent getViewComponent() {
		return this;
	}

	@Override
	public JTree getJTree() {
		return this.tree;
	}

	@Override
	public ExplorerNode<?> getRootNode() {
		return (ExplorerNode<?>) this.getJTree().getModel().getRoot();
	}
	
}
