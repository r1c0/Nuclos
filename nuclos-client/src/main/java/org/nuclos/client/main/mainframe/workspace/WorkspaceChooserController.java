//Copyright (C) 2010  Novabit Informationssysteme GmbH
//
//This file is part of Nuclos.
//
//Nuclos is free software: you can redistribute it and/or modify
//it under the terms of the GNU Affero General Public License as published by
//the Free Software Foundation, either version 3 of the License, or
//(at your option) any later version.
//
//Nuclos is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU Affero General Public License for more details.
//
//You should have received a copy of the GNU Affero General Public License
//along with Nuclos.  If not, see <http://www.gnu.org/licenses/>.
package org.nuclos.client.main.mainframe.workspace;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;

import org.nuclos.client.main.Main;
import org.nuclos.client.main.mainframe.MainFrame;
import org.nuclos.client.resource.NuclosResourceCache;
import org.nuclos.client.ui.Errors;
import org.nuclos.client.ui.Icons;
import org.nuclos.common.WorkspaceDescription;
import org.nuclos.common2.CommonLocaleDelegate;
import org.nuclos.common2.LangUtils;
import org.nuclos.common2.ServiceLocator;
import org.nuclos.common2.StringUtils;
import org.nuclos.server.common.ejb3.PreferencesFacadeRemote;

public class WorkspaceChooserController {
	
	public static final int ICON_SIZE = 16;
	private static WorkspaceDescription selectedWorkspace = null;
	private static final List<WorkspaceDescription> workspaces = new ArrayList<WorkspaceDescription>();
	private static final WorkspacePanel workspacePanel = new WorkspacePanel();
	private static final ContentPanel contentPanel = new ContentPanel();

	private static boolean enabled;
	
	public WorkspaceChooserController() {
		super();
		initWorkspaceChooser();
	}
	
	private void initWorkspaceChooser() {
		contentPanel.setOpaque(false);
		contentPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 1, 5));
		
		workspacePanel.setOpaque(false);
		
		final JLabel jlbLeft = new JLabel(Icons.getInstance().getWorkspaceChooser_left());
		final JLabel jlbRight = new JLabel(Icons.getInstance().getWorkspaceChooser_right());
		jlbLeft.setBorder(BorderFactory.createEmptyBorder());
		jlbRight.setBorder(BorderFactory.createEmptyBorder());
		contentPanel.add(jlbLeft);
		contentPanel.add(workspacePanel);
		contentPanel.add(jlbRight);
	}
	
	public void setupWorkspaces(ArrayList<String> workspaceOrder) {
		Collection<WorkspaceDescription> allWorkspaces = ServiceLocator.getInstance().getFacade(PreferencesFacadeRemote.class).getWorkspaceMetadataOnly(); 
		for (String wo : workspaceOrder) {
			for (WorkspaceDescription wd : allWorkspaces) {
				if (LangUtils.equals(wo, wd.getName())) {
					workspaces.add(wd);
				}
			}
		}
		for (WorkspaceDescription wd : allWorkspaces) {
			if (!workspaces.contains(wd)) {
				workspaces.add(wd);
			}
		}
		
		refreshWorkspaces();
	}
	
	private static void refreshWorkspaces() {
		workspacePanel.removeAll();
		
		for (final WorkspaceDescription workspace : workspaces) {
			
			final WorkspaceLabel wl = new WorkspaceLabel(workspace);
			wl.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					if (enabled && SwingUtilities.isLeftMouseButton(e) &&
							!wl.isSelected()) {
						RestoreUtils.storeWorkspace(getSelectedWorkspace());
						RestoreUtils.clearAndRestoreWorkspace(wl.getWorkspaceDescription().getName());
					}
				}
			});
			setupContextMenu(wl);
			workspacePanel.add(wl);
		}
		
		contentPanel.invalidate();
		contentPanel.revalidate();
		contentPanel.repaint();
	}
	
	private static void addAndSelectNewWorkspace(WorkspaceDescription wd) {
		workspaces.add(wd);
		
		refreshWorkspaces();
		setSelectedWorkspace(wd.getName());
	}
	
	public static List<String> getWorkspaceOrder() {
		List<String> result = new ArrayList<String>();
		for (WorkspaceDescription wd : workspaces) {
			result.add(wd.getName());
		}
		return result;
	}
	
	public static WorkspaceDescription getSelectedWorkspace() {
		return selectedWorkspace;
	}
	
	public static void setSelectedWorkspace(String name) {
		for (WorkspaceDescription wd : workspaces) {
			if (LangUtils.equals(name, wd.getName())) {
				selectedWorkspace = wd;
				contentPanel.repaint();
			}
		}
	}

	public JComponent getChooserComponent() {
		return contentPanel;
	}
	
	public void setEnabled(boolean b) {
		enabled = b;
	}
	
	public boolean isEnabled() {
		return enabled;
	}
	
	private static void setupContextMenu(final WorkspaceLabel wl) {		
		wl.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (enabled && SwingUtilities.isRightMouseButton(e)) {
					
					final JPopupMenu popup = new JPopupMenu();
					popup.add(new JLabel("<html><b>"+CommonLocaleDelegate.getMessage("WorkspaceChooserController.2","Arbeitsumgebung")+"</b></html>"));
					
					final JMenuItem miNew = new JMenuItem(new AbstractAction(
						CommonLocaleDelegate.getMessage("WorkspaceChooserController.1","Neu"), 
						MainFrame.resizeAndCacheIcon(Icons.getInstance().getIconNew16(), ICON_SIZE)) {

						@Override
						public void actionPerformed(ActionEvent e) {
							try {
								WorkspaceEditor we = new WorkspaceEditor(new WorkspaceDescription());
								if (we.isSaved()) {
									RestoreUtils.storeWorkspace(getSelectedWorkspace());
									RestoreUtils.clearAndRestoreToDefaultWorkspace(we.getResult().getName());
									addAndSelectNewWorkspace(we.getResult());
									refreshWorkspaces();
								}
							} catch (Exception e1) {
								Errors.getInstance().showExceptionDialog(Main.getMainFrame(), e1);
							} 
						}
					});
					popup.add(miNew);
					
					final JMenuItem miEdit = new JMenuItem(new AbstractAction(
							CommonLocaleDelegate.getMessage("WorkspaceChooserController.3","Bearbeiten"), 
							MainFrame.resizeAndCacheIcon(Icons.getInstance().getIconEdit16(), ICON_SIZE)) {

							@Override
							public void actionPerformed(ActionEvent e) {
								try {
									WorkspaceEditor we = new WorkspaceEditor(wl.getWorkspaceDescription());
									if (we.isSaved()) {
										ServiceLocator.getInstance().getFacade(PreferencesFacadeRemote.class).storeWorkspaceMetadataOnly(
												wl.getWorkspaceDescription().getName(), 
												we.getResult());
										wl.getWorkspaceDescription().importMetadata(we.getResult());
										refreshWorkspaces();
									}
								} catch (Exception e1) {
									Errors.getInstance().showExceptionDialog(Main.getMainFrame(), e1);
								} 
							}
						});
					popup.add(miEdit);
					
					final JMenuItem miSaveAs = new JMenuItem(new AbstractAction(
						CommonLocaleDelegate.getMessage("WorkspaceChooserController.4","Klonen"), 
						MainFrame.resizeAndCacheIcon(Icons.getInstance().getIconClone16(), ICON_SIZE)) {

						@Override
						public void actionPerformed(ActionEvent e) {
							String newName = JOptionPane.showInputDialog(Main.getMainFrame(), 
								CommonLocaleDelegate.getMessage("WorkspaceChooserController.6","Arbeitsumgebung \"{0}\" klonen",wl.getWorkspaceDescription().getName()) + ":", 
								CommonLocaleDelegate.getMessage("WorkspaceChooserController.5","Neuer Name"), 
								JOptionPane.INFORMATION_MESSAGE);
							
							if (!StringUtils.looksEmpty(newName)) {
								if (LangUtils.equals(getSelectedWorkspace().getName(), wl.getWorkspaceDescription().getName())) {
									WorkspaceDescription wd = wl.getWorkspaceDescription().copyMetadata();
									wd.setName(newName);
									RestoreUtils.storeWorkspace(wd);
									workspaces.add(wd);
									refreshWorkspaces();
								} else {
									try {
										PreferencesFacadeRemote prefsFac = ServiceLocator.getInstance().getFacade(PreferencesFacadeRemote.class);
										WorkspaceDescription wd = prefsFac.getWorkspace(wl.getWorkspaceDescription().getName());
										wd.setName(newName);
										prefsFac.storeWorkspace(wd);
										workspaces.add(wd.copyMetadata());
										refreshWorkspaces();
									} catch (Exception e1) {
										Errors.getInstance().showExceptionDialog(Main.getMainFrame(), e1);
									} 
								}
							}
						}
					});
					popup.add(miSaveAs);
					
					popup.addSeparator();
					
					final JMenuItem miRemove = new JMenuItem(new AbstractAction(
						CommonLocaleDelegate.getMessage("WorkspaceChooserController.7","Löschen"), 
						MainFrame.resizeAndCacheIcon(Icons.getInstance().getIconRealDelete16(), ICON_SIZE)) {

						@Override
						public void actionPerformed(ActionEvent e) {
							if (JOptionPane.YES_OPTION == 
								JOptionPane.showConfirmDialog(Main.getMainFrame(), 
									CommonLocaleDelegate.getMessage("WorkspaceChooserController.8","Möchten Sie wirklich die Arbeitsumgebung \"{0}\" löschen?",wl.getWorkspaceDescription().getName()), 
									CommonLocaleDelegate.getMessage("WorkspaceChooserController.9","Sind Sie sicher?"), 
									JOptionPane.YES_NO_OPTION)) {
								
								ServiceLocator.getInstance().getFacade(PreferencesFacadeRemote.class).removeWorkspace(wl.getWorkspaceDescription().getName());
								workspaces.remove(wl.getWorkspaceDescription());
								
								if (LangUtils.equals(getSelectedWorkspace().getName(), wl.getWorkspaceDescription().getName())) {
									final String defaultWorkspace = MainFrame.getDefaultWorkspace();
									RestoreUtils.clearAndRestoreWorkspace(defaultWorkspace);
									
									WorkspaceDescription wd = new WorkspaceDescription();
									wd.setName(defaultWorkspace);
									addAndSelectNewWorkspace(wd);
								} else {
									refreshWorkspaces();
								}
							}
						}
					});
					popup.add(miRemove);
					
					popup.show(wl, 10, 10);
				}
			}
		});
	}
	
	private static class ContentPanel extends JPanel {
		
		public ContentPanel() {
			super(new FlowLayout(FlowLayout.RIGHT, 0, 0));
		}
	}
	
	private static class WorkspacePanel extends JPanel{
		
		private static final ImageIcon imgBG = Icons.getInstance().getWorkspaceChooser_bg();
		private static final int imgBG_w = imgBG.getIconWidth();
		public  static final int imgBG_h = imgBG.getIconHeight();

		public WorkspacePanel() {
			super(new FlowLayout(FlowLayout.CENTER, 0, 0));
		}

		@Override
		public void paintComponent(Graphics g) {
			Graphics2D g2 = (Graphics2D) g;
			RenderingHints oldRH = g2.getRenderingHints();
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
			
			final Dimension size = getSize();			
			for (int i = 0; i < ((size.width / imgBG_w)+1); i++) {
				g2.drawImage(imgBG.getImage(), i * imgBG_w, 0, null);
			}
			
	        g2.setRenderingHints(oldRH);
		}
		
		@Override
		public Dimension getPreferredSize() {
			return new Dimension (super.getPreferredSize().width, imgBG_h);
		}
	}
	
	private static class WorkspaceLabel extends JLabel implements DragGestureListener {
		
		private final WorkspaceDescription workspaceDescription;
		
		private final static ImageIcon imgBG = Icons.getInstance().getWorkspaceChooser_buttonBG();
		private final static ImageIcon imgBG_left = Icons.getInstance().getWorkspaceChooser_buttonLeft();
		private final static ImageIcon imgBG_right = Icons.getInstance().getWorkspaceChooser_buttonRight();
		
		public  final static int imgBG_h = imgBG.getIconHeight();
		private final static int imgBG_w = imgBG.getIconWidth();
		private final static int imgBG_left_w = imgBG_left.getIconWidth();
		private final static int imgBG_right_w = imgBG_right.getIconWidth();
		
		private boolean mouseOver = false;
		
		public WorkspaceLabel(WorkspaceDescription workspaceDescription) {
			super(
					workspaceDescription.isHideName()?
							null:
							workspaceDescription.getName(), 
					MainFrame.resizeAndCacheIcon(workspaceDescription.getNuclosResource()==null?
							Icons.getInstance().getIconTabGeneric():
							NuclosResourceCache.getNuclosResourceIcon(workspaceDescription.getNuclosResource()), ICON_SIZE), 
					SwingConstants.LEFT);
			this.workspaceDescription = workspaceDescription;
			setBorder(BorderFactory.createEmptyBorder(imgBG_left_w, imgBG_left_w, imgBG_left_w, imgBG_left_w));
			if (workspaceDescription.isHideName()) {
				setToolTipText(workspaceDescription.getName());
			}
			addMouseListener(new MouseAdapter() {
				@Override
				public void mouseEntered(MouseEvent e) {
					if (!mouseOver) {
						mouseOver = true;
						repaint();
					} else {
						mouseOver = true;
					}
				}
				@Override
				public void mouseExited(MouseEvent e) {
					if (mouseOver) {
						mouseOver = false;
						repaint();
					} else {
						mouseOver = false;
					}
				}
			});
			DragSource dragSource = DragSource.getDefaultDragSource();
		    dragSource.createDefaultDragGestureRecognizer(WorkspaceLabel.this, DnDConstants.ACTION_COPY_OR_MOVE, WorkspaceLabel.this);
		    setTransferHandler(new TransferHandler() {
				@Override
				public boolean importData(JComponent comp, Transferable t) {
					try {
						WorkspaceDescription other = (WorkspaceDescription) t.getTransferData(workspaceFlavor);
						if (other != null) {
							WorkspaceDescription wdToRemove = null;
							for (WorkspaceDescription wd : workspaces) {
								if (LangUtils.equals(wd.getName(), other.getName())) {
									wdToRemove = wd;
								}
							}
							if (wdToRemove != null)	workspaces.remove(wdToRemove);
							
							int indexToAdd = 0;
							for (WorkspaceDescription wd : workspaces) {
								if (LangUtils.equals(wd.getName(), WorkspaceLabel.this.workspaceDescription.getName())) {
									indexToAdd = workspaces.indexOf(wd);
								}
							}
							workspaces.add(indexToAdd, other);
							
							WorkspaceChooserController.refreshWorkspaces();
							return true;
						}
					} catch (Exception e) {} 
					
					return super.importData(comp, t);
				}
				@Override
				public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
					for (DataFlavor dataFlavor : transferFlavors) {
						if (workspaceFlavor.equals(dataFlavor)) {
							return true;
						}
					}
					return false;
				}
		    });
		}
		
		public WorkspaceDescription getWorkspaceDescription() {
			return workspaceDescription;
		}

		@Override
		protected void paintComponent(Graphics g) {
			Graphics2D g2 = (Graphics2D) g;

			RenderingHints oldRH = g2.getRenderingHints();
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

			BufferedImage biBG = drawBuffImage(
					new BufferedImage(imgBG_w, imgBG_h, BufferedImage.TYPE_INT_ARGB),
					imgBG.getImage());
			BufferedImage biBG_left = drawBuffImage(
					new BufferedImage(imgBG_left_w, imgBG_h, BufferedImage.TYPE_INT_ARGB),
					imgBG_left.getImage());
			BufferedImage biBG_right = drawBuffImage(
					new BufferedImage(imgBG_right_w, imgBG_h, BufferedImage.TYPE_INT_ARGB),
					imgBG_right.getImage());

			// 50% opaque
			float[] scales = { 1f, 1f, 1f, (mouseOver || isSelected())?1f:0.5f };
			float[] offsets = new float[4];
			RescaleOp rop = new RescaleOp(scales, offsets, null);
		   	
			g2.drawImage(biBG_left, rop, -1, 0);
		   	for (int i = 0; i < (getWidth()-imgBG_left_w-imgBG_right_w) + 2; i++) {
		   		g2.drawImage(biBG, rop, imgBG_left_w -1 + (i * imgBG_w), 0);
		   	}
		   	g2.drawImage(biBG_right, rop, getWidth()-imgBG_right_w +1, 0);

			g2.setRenderingHints(oldRH);
			
			super.paintComponent(g);
		}
		
		private BufferedImage drawBuffImage(BufferedImage bi, Image img) {
			final Graphics gbi = bi.getGraphics();
			gbi.drawImage(img, 0, 0, null);
			gbi.dispose();
			return bi;
		}
		
		@Override
		public Dimension getPreferredSize() {
			return new Dimension (super.getPreferredSize().width, // + imgBG_left_w + imgBG_right_w,
					imgBG_h);
		}

		public boolean isSelected() {
			return LangUtils.equals(getSelectedWorkspace().getName(), workspaceDescription.getName());
		}

		@Override
		public void dragGestureRecognized(DragGestureEvent dge) {
			Transferable transferable = new WorkspaceLabelTransferable(WorkspaceLabel.this.workspaceDescription);
		    dge.startDrag(null, transferable, null);
		    mouseOver = false;
			repaint();
		}
	}
	
	private static class WorkspaceLabelTransferable implements Transferable {
		
		final WorkspaceDescription wd;
		
		public WorkspaceLabelTransferable(WorkspaceDescription wd) {
			this.wd = wd;
		}

		@Override
		public DataFlavor[] getTransferDataFlavors() {
			return flavors;
		}

		@Override
		public boolean isDataFlavorSupported(DataFlavor fl) {
			if (workspaceFlavor.equals(fl))
				return true;
			return false;
		}
		
		@Override
		public Object getTransferData(DataFlavor fl) {
		    if (workspaceFlavor.equals(fl)) {
		      return wd;
		    }
		    return null;
		}
	}
	
	public static final DataFlavor workspaceFlavor = new DataFlavor(WorkspaceDescription.class, "WorkspaceDescription");
	private static final DataFlavor[] flavors = new DataFlavor[] {workspaceFlavor};
}
