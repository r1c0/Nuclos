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
import java.awt.IllegalComponentStateException;
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
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.TransferHandler;

import org.nuclos.client.common.security.SecurityCache;
import org.nuclos.client.main.ActionWithMenuPath;
import org.nuclos.client.main.GenericAction;
import org.nuclos.client.main.Main;
import org.nuclos.client.main.MainController;
import org.nuclos.client.main.mainframe.MainFrame;
import org.nuclos.client.resource.NuclosResourceCache;
import org.nuclos.client.ui.Bubble;
import org.nuclos.client.ui.DefaultSelectObjectsPanel;
import org.nuclos.client.ui.Errors;
import org.nuclos.client.ui.Icons;
import org.nuclos.client.ui.SelectObjectsController;
import org.nuclos.client.ui.UIUtils;
import org.nuclos.client.ui.model.ChoiceList;
import org.nuclos.common.Actions;
import org.nuclos.common.WorkspaceDescription;
import org.nuclos.common.WorkspaceDescriptionDefaultsFactory;
import org.nuclos.common.WorkspaceParameter;
import org.nuclos.common.WorkspaceVO;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.Predicate;
import org.nuclos.common.collection.Transformer;
import org.nuclos.common.dal.vo.EntityObjectVO;
import org.nuclos.common2.LangUtils;
import org.nuclos.common2.SpringLocaleDelegate;
import org.nuclos.common2.StringUtils;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.server.common.ejb3.PreferencesFacadeRemote;
import org.springframework.beans.factory.InitializingBean;

// @Component
public class WorkspaceChooserController implements InitializingBean {
	
	public static final int ICON_SIZE = 16;
	private static final List<WorkspaceVO> workspaces = new ArrayList<WorkspaceVO>();
	private static final WorkspacePanel workspacePanel = new WorkspacePanel();
	private static final ContentPanel contentPanel = new ContentPanel();
	
	//
	
	private static WorkspaceChooserController INSTANCE;
	
	// 
	
	private WorkspaceVO selectedWorkspace = null;
	private JLabel jlbLeft;
	private JLabel jlbRight;

	private boolean enabled;
	
	private List<Long> workspaceOrderIds;
	private List<String> workspaceOrder;
	
	// Spring injection
	
	private SpringLocaleDelegate localeDelegate;
	
	private PreferencesFacadeRemote preferencesFacadeRemote;
	
	private RestoreUtils restoreUtils;
	
	// end of Spring injection
	
	WorkspaceChooserController() {
		INSTANCE = this;
		// initWorkspaceChooser();
	}
	
	public static WorkspaceChooserController getInstance() {
		if (INSTANCE == null) {
			throw new IllegalStateException("too early");
		}
		return INSTANCE;
	}
	
	@Override
	public final void afterPropertiesSet() {
		contentPanel.setOpaque(false);
		contentPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 1, 5));
		
		workspacePanel.setOpaque(false);
		
		jlbLeft = new JLabel(Icons.getInstance().getWorkspaceChooser_left());
		jlbRight = new JLabel(Icons.getInstance().getWorkspaceChooser_right());
		jlbLeft.setBorder(BorderFactory.createEmptyBorder());
		jlbRight.setBorder(BorderFactory.createEmptyBorder());
		contentPanel.add(jlbLeft);
		contentPanel.add(workspacePanel);
		contentPanel.add(jlbRight);
		workspacePanel.setVisible(false);
	}
	
	// @Autowired
	public final void setSpringLocaleDelegate(SpringLocaleDelegate cld) {
		this.localeDelegate = cld;
	}
	
	// @Autowired
	public final void setPreferencesFacadeRemote(PreferencesFacadeRemote preferencesFacadeRemote) {
		this.preferencesFacadeRemote = preferencesFacadeRemote;
	}
	
	// @Autowired
	public final void setRestoreUtils(RestoreUtils restoreUtils) {
		this.restoreUtils = restoreUtils;
	}
	
	/**
	 * 
	 * @param workspaceOrderIds
	 * @param workspaceOrder
	 */
	public void setupWorkspaces(List<Long> workspaceOrderIds, List<String> workspaceOrder) {
		this.workspaceOrderIds = workspaceOrderIds;
		this.workspaceOrder = workspaceOrder;
		
		setupWorkspaces();
	}
	
	/**
	 * 
	 */
	public void setupWorkspaces() {
		Collection<WorkspaceVO> allWorkspaces = preferencesFacadeRemote.getWorkspaceHeaderOnly(); 
			
		Set<Long> addedIds = new HashSet<Long>();
		Set<String> addedNames = new HashSet<String>();
		boolean atLeastOneWorkspaceIdFound = false; //may be user is transferred on other system with different ids...
		for (Long woid : workspaceOrderIds) {
			for (WorkspaceVO wovo : allWorkspaces) {
				if (LangUtils.equals(woid, wovo.getId())) {
					if (!addedIds.contains(woid)) {
						addedIds.add(woid);
						atLeastOneWorkspaceIdFound = true;
						workspaces.add(wovo);
					}
				}
			}
		}			
		
		if (!atLeastOneWorkspaceIdFound) {
			// no id matches, try to restore order by names
			for (String woname : workspaceOrder) {
				for (WorkspaceVO wovo : allWorkspaces) {
					if (LangUtils.equals(woname, wovo.getName())) {
						if (!addedNames.contains(woname)) {
							addedNames.add(woname);
							workspaces.add(wovo);
						}
					}
				}
			}
		}
		
		// add workspaces which are not in order
		for (WorkspaceVO wovo : allWorkspaces) {
			if (!workspaces.contains(wovo)) {
				workspaces.add(wovo);
			}
		}
		
		refreshWorkspaces();
		
		final boolean visible = workspaces.size() > 1 || SecurityCache.getInstance().isActionAllowed(Actions.ACTION_WORKSPACE_CREATE_NEW);
		workspacePanel.setVisible(visible);
		jlbLeft.setVisible(visible);
		jlbRight.setVisible(visible);
	}
	
	/**
	 * 
	 */
	private void refreshWorkspaces() {
		workspacePanel.removeAll();
		
		final List<WorkspaceVO> hidden = new ArrayList<WorkspaceVO>();
		for (final WorkspaceVO wovo : workspaces) {
			
			if (wovo.getWoDesc().isHide()) {
				if (SecurityCache.getInstance().isActionAllowed(Actions.ACTION_WORKSPACE_ASSIGN)) {
					hidden.add(wovo);
				}
			} else {
				final WorkspaceLabel wl = new WorkspaceLabel(wovo);
				wl.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseClicked(MouseEvent e) {
						if (enabled && SwingUtilities.isLeftMouseButton(e) && !wl.isSelected()) {
							restoreWorkspace(wl.getWorkspaceVO());
						}
					}
				});
				setupContextMenu(wl);
				workspacePanel.add(wl);
			}
		}
		
		if (!hidden.isEmpty()) {
			final Label lbHidden = new Label(" + ", 
					null, //MainFrame.resizeAndCacheIcon(NuclosResourceCache.getNuclosResourceIcon("org.nuclos.client.resource.icon.glyphish-blue.174-imac.png"), ICON_SIZE)
					SwingConstants.LEFT) {
				@Override
				boolean isSelected() {
					return false;
				}
				@Override
				boolean drawDogEar() {
					return false;
				}
			};
			lbHidden.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					UIUtils.runCommand(Main.getInstance().getMainFrame(), new Runnable() {
						
						@Override
						public void run() {
							if (enabled) {
								final JPopupMenu popup = new JPopupMenu();
								for (WorkspaceVO hiddenWovo : CollectionUtils.sorted(hidden,
										new Comparator<WorkspaceVO>() {
											@Override
											public int compare(WorkspaceVO o1, WorkspaceVO o2) {
												return StringUtils.emptyIfNull(o1.getName()).compareToIgnoreCase(o2.getName());
											}
										})) {
									final JMenu menuHiddenWorkspace = new JMenu(hiddenWovo.getName());
									addMenuItems(new MenuItemContainer() {
										@Override
										public void addSeparator() {
											menuHiddenWorkspace.addSeparator();
										}
										@Override
										public void addLabel(String label) {
											menuHiddenWorkspace.add(new JLabel(label));
										}
										@Override
										public void add(JMenuItem mi) {
											menuHiddenWorkspace.add(mi);
										}
									}, hiddenWovo, lbHidden);
									popup.add(menuHiddenWorkspace);
								}
								popup.show(lbHidden, 10, 10);
							}
						}
					});
				}
			});
			workspacePanel.add(lbHidden);
		}
		
		contentPanel.invalidate();
		contentPanel.revalidate();
		contentPanel.repaint();
	}
	
	private void addWorkspace(WorkspaceVO wovo, boolean select) {
		if (wovo != null) {
			// add header only
			wovo.getWoDesc().removeAllEntityPreferences();
			wovo.getWoDesc().getFrames().clear();
			
			workspaces.add(wovo);
			
			refreshWorkspaces();
			if (select)
				setSelectedWorkspace(wovo);
		}
	}
	
	public List<WorkspaceVO> getWorkspaceHeaders() {
		return new ArrayList<WorkspaceVO>(workspaces);
	}
	
	public void addGenericActions(List<GenericAction> genericActions) {
		if (genericActions != null) {
			for (final WorkspaceVO wovo : workspaces) {
				final Action action = new AbstractAction(wovo.getName(), getWorkspaceIcon(wovo, MainFrame.TAB_CONTENT_ICON_MAX)) {
					@Override
					public void actionPerformed(ActionEvent e) {
						restoreWorkspace(wovo);
					}
				};
				WorkspaceDescription.Action wa = new WorkspaceDescription.Action();
				wa.setAction(MainController.GENERIC_RESTORE_WORKSPACE_ACTION);
				wa.putStringParameter("workspace", (String) action.getValue(Action.NAME));
				wa.putBooleanParameter("assigned", wovo.isAssigned());
				genericActions.add(new GenericAction(wa, new ActionWithMenuPath(
						new String[]{
								SpringLocaleDelegate.getInstance().getMessage("nuclos.entity.workspace.label", "Arbeitsumgebung")
						}, action)));
			}
		}
	}
	
	private void restoreWorkspace(final WorkspaceVO wovo) {
		if (wovo != null && !wovo.equals(getSelectedWorkspace())) {
			final MainFrame mf = Main.getInstance().getMainFrame();
			try {
				restoreUtils.storeWorkspace(getSelectedWorkspace());
			} catch (CommonBusinessException e1) {
				if ("Workspace.not.found".equals(e1.getMessage())) {
					workspaces.remove(getSelectedWorkspace());
					refreshWorkspaces();
				} else {
					Errors.getInstance().showExceptionDialog(mf, e1);
				}
			}
			try {
				restoreUtils.clearAndRestoreWorkspace(preferencesFacadeRemote.getWorkspace(wovo.getId()));
			} catch (CommonBusinessException e1) {
				if ("Workspace.not.found".equals(e1.getMessage())) {
					workspaces.remove(wovo);
					refreshWorkspaces();
				}
				Errors.getInstance().showExceptionDialog(mf, e1);
			}
		}
	}
	
	public WorkspaceVO getSelectedWorkspace() {
		return selectedWorkspace;
	}
	
	public void setSelectedWorkspace(WorkspaceVO wovo) {
		for (WorkspaceVO wovoI : workspaces) {
			if (wovoI.equals(wovo)) {
				selectedWorkspace = wovoI;
				
				// preferences may be changed (e.g. restore to assigned)...
				if (selectedWorkspace != wovo) {
					selectedWorkspace.importHeader(wovo.getWoDesc());
					selectedWorkspace.getWoDesc().removeAllEntityPreferences();
					selectedWorkspace.getWoDesc().addAllEntityPreferences(wovo.getWoDesc().getEntityPreferences());
					selectedWorkspace.getWoDesc().removeAllTasklistPreferences();
					selectedWorkspace.getWoDesc().addAllTasklistPreferences(wovo.getWoDesc().getTasklistPreferences());
					selectedWorkspace.getWoDesc().removeAllParameters();
					selectedWorkspace.getWoDesc().setAllParameters(wovo.getWoDesc().getParameters());
				}
				
				contentPanel.repaint();
				
				Main.getInstance().getMainFrame().setSplittingEnabled(
						wovo.getAssignedWorkspace() == null || 
						(wovo.getAssignedWorkspace() != null && SecurityCache.getInstance().isActionAllowed(Actions.ACTION_WORKSPACE_ASSIGN)));
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
	
	private void setupContextMenu(final WorkspaceLabel wl) {
		wl.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(final MouseEvent e) {
				UIUtils.runCommand(Main.getInstance().getMainFrame(), new Runnable() {
					
					@Override
					public void run() {
						if (enabled && SwingUtilities.isRightMouseButton(e)) {
							final JPopupMenu popup = new JPopupMenu();
							addMenuItems(new MenuItemContainer() {
								@Override
								public void addSeparator() {
									popup.addSeparator();
								}
								@Override
								public void addLabel(String label) {
									popup.add(new JLabel(label));
								}
								@Override
								public void add(JMenuItem mi) {
									popup.add(mi);
								}
							}, wl.getWorkspaceVO(), wl);
							popup.show(wl, 10, 10);
						}
					}
				});
			}
		});
	}
	
	private static interface MenuItemContainer {
		void addLabel(String label);
		void add(JMenuItem mi);
		void addSeparator();
	}
	
	private void addMenuItems(final MenuItemContainer menu, final WorkspaceVO wovo, final Label lb) {		
		/**
		 * CHECK 
		 */
		try {
			preferencesFacadeRemote.getWorkspace(wovo.getId());
		} catch (Exception ex) {
			if ("Workspace.not.found".equals(ex.getMessage())) {
				workspaces.remove(wovo);
				refreshWorkspaces();
			}
			Errors.getInstance().showExceptionDialog(Main.getInstance().getMainFrame(), ex);
			return;
		}
		
		menu.addLabel("<html><b>"
				+ SpringLocaleDelegate.getInstance().getMessage("WorkspaceChooserController.2","Arbeitsumgebung")
				+ "</b></html>");
		
		/**
		 * REFRESH
		 */
		final JMenuItem miRefresh = new JMenuItem(newRefreshAction());
		menu.add(miRefresh);
		
		if (!wovo.isAssigned() ||
				SecurityCache.getInstance().isActionAllowed(Actions.ACTION_WORKSPACE_ASSIGN) || 
				SecurityCache.getInstance().isActionAllowed(Actions.ACTION_WORKSPACE_CREATE_NEW))
			menu.addSeparator();
		
		/**
		 * NEW
		 */
		if (SecurityCache.getInstance().isActionAllowed(Actions.ACTION_WORKSPACE_ASSIGN) || 
				SecurityCache.getInstance().isActionAllowed(Actions.ACTION_WORKSPACE_CREATE_NEW)) {
			final JMenuItem miNew = new JMenuItem(newNewWorkspaceAction());
			menu.add(miNew);
		}
		
		/**
		 * EDIT
		 */
		if (!wovo.isAssigned() || SecurityCache.getInstance().isActionAllowed(Actions.ACTION_WORKSPACE_ASSIGN)) {
			final JMenuItem miEdit = new JMenuItem(newEditAction(wovo));
			menu.add(miEdit);
		}
		
		/**
		 * CLONE
		 */
		if (SecurityCache.getInstance().isActionAllowed(Actions.ACTION_WORKSPACE_ASSIGN) || 
				SecurityCache.getInstance().isActionAllowed(Actions.ACTION_WORKSPACE_CREATE_NEW)) {
			final JMenuItem miSaveAs = new JMenuItem(newCloneAction(wovo));
			menu.add(miSaveAs);
		}
		
		/**
		 * REMOVE
		 */
		if (!wovo.isAssigned() || SecurityCache.getInstance().isActionAllowed(Actions.ACTION_WORKSPACE_ASSIGN)) {
			menu.addSeparator();
			final JMenuItem miRemove = new JMenuItem(newRemoveAction(wovo));
			menu.add(miRemove);
		}
		
		if (wovo.getAssignedWorkspace() != null || 
				SecurityCache.getInstance().isActionAllowed(Actions.ACTION_WORKSPACE_ASSIGN))
			menu.addSeparator();
		
		if(SecurityCache.getInstance().isActionAllowed(Actions.ACTION_WORKSPACE_ASSIGN)) {
			/**
			 * ASSIGN
			 */
			final JMenuItem miAssign = new JMenuItem(newAssignAction(wovo, lb));
				menu.add(miAssign);
				
				if (wovo.getAssignedWorkspace() != null) {
					/**
					 * PUBLISH
					 */
					final JMenuItem miUpdate = new JMenuItem(newPublishAction(wovo, lb));
					menu.add(miUpdate);
				}
		}
		
		/**
		 * RESTORE
		 */
		if(wovo.getAssignedWorkspace() != null) {
			final JMenuItem miUpdate = new JMenuItem(newRestoreAction(wovo));
			menu.add(miUpdate);
		}				
	}
	
	private Action newRefreshAction() {
		return new AbstractAction(
				SpringLocaleDelegate.getInstance().getMessage(
						"WorkspaceChooserController.13","Leiste aktualisieren"), 
				MainFrame.resizeAndCacheIcon(Icons.getInstance().getIconRefresh16(), ICON_SIZE)) {

					private static final long serialVersionUID = 1L;

					@Override
					public void actionPerformed(ActionEvent e) {
						UIUtils.runCommand(Main.getInstance().getMainFrame(), new Runnable() {
							@Override
							public void run() {
								workspaceOrder.clear();
								workspaceOrderIds.clear();
								for (WorkspaceVO wovo : workspaces) {
									workspaceOrderIds.add(wovo.getId());
								}
								workspaces.clear();
								setupWorkspaces();
							}
						});
					}
			};
	}
	
	private Action newNewWorkspaceAction() {
		return new AbstractAction(
				SpringLocaleDelegate.getInstance().getMessage("WorkspaceChooserController.1","Neu"), 
				MainFrame.resizeAndCacheIcon(Icons.getInstance().getIconNew16(), ICON_SIZE)) {

				@Override
				public void actionPerformed(ActionEvent e) {
					if (!restoreUtils.closeTabs(true)) {
						return;
					}
					final MainFrame mf = Main.getInstance().getMainFrame();
					try {
						WorkspaceVO wovo = new WorkspaceVO(); // only for header information
						WorkspaceEditor we = new WorkspaceEditor(wovo);
						if (we.isSaved()) {
							try {
								restoreUtils.storeWorkspace(getSelectedWorkspace());
							} catch (Exception e2) {
								Errors.getInstance().showExceptionDialog(mf, e2);
							}
							addWorkspace(restoreUtils.clearAndRestoreToDefaultWorkspace(wovo.getWoDesc()), true);
						}
					} catch (Exception e1) {
						Errors.getInstance().showExceptionDialog(mf, e1);
					} 
				}
			};
	}
	
	private Action newEditAction(final WorkspaceVO wovo) {
		return new AbstractAction(
				SpringLocaleDelegate.getInstance().getMessage("WorkspaceChooserController.3","Eigenschaften"), 
				MainFrame.resizeAndCacheIcon(Icons.getInstance().getIconEdit16(), ICON_SIZE)) {

				@Override
				public void actionPerformed(ActionEvent e) {
					boolean hideMenuBar = wovo.getWoDesc().isHideMenuBar();
					String hideMenuItems = wovo.getWoDesc().getParameter(WorkspaceParameter.HIDE_MENU_ITEMS);
					WorkspaceEditor we = new WorkspaceEditor(wovo);
					try {
						if (we.isSaved()) {
							preferencesFacadeRemote.storeWorkspaceHeaderOnly(wovo);
							refreshWorkspaces();
							if (wovo == restoreUtils.getMainFrame().getWorkspace()) {
								if (hideMenuBar != wovo.getWoDesc().isHideMenuBar() || 
										!LangUtils.equals(hideMenuItems, wovo.getWoDesc().getParameter(WorkspaceParameter.HIDE_MENU_ITEMS))) {
									Main.getInstance().getMainController().setupMenuBar();
								}
							}

						}
					} catch (Exception e1) {
						we.revertChanges();
						Errors.getInstance().showExceptionDialog(Main.getInstance().getMainFrame(), e1);
					} 
				}
			};
	}
	
	private Action newCloneAction(final WorkspaceVO wovo) {
		return new AbstractAction(
				SpringLocaleDelegate.getInstance().getMessage("WorkspaceChooserController.4","Klonen"), 
				MainFrame.resizeAndCacheIcon(Icons.getInstance().getIconClone16(), ICON_SIZE)) {

				@Override
				public void actionPerformed(ActionEvent e) {
					final MainFrame mf = Main.getInstance().getMainFrame();
					String newName = JOptionPane.showInputDialog(mf, 
						SpringLocaleDelegate.getInstance().getMessage(
								"WorkspaceChooserController.6","Arbeitsumgebung \"{0}\" klonen",wovo.getWoDesc().getName()) + ":", 
						SpringLocaleDelegate.getInstance().getMessage("WorkspaceChooserController.5","Neuer Name"), 
						JOptionPane.INFORMATION_MESSAGE);
					
					if (!StringUtils.looksEmpty(newName)) {
						if (getSelectedWorkspace().equals(wovo)) {
							WorkspaceVO wovo = new WorkspaceVO();
							wovo.importHeader(wovo.getWoDesc());
							wovo.setName(newName);
							wovo.getWoDesc().addAllEntityPreferences(wovo.getWoDesc().getEntityPreferences());
							wovo.getWoDesc().addAllTasklistPreferences(wovo.getWoDesc().getTasklistPreferences());
							wovo.getWoDesc().setAllParameters(wovo.getWoDesc().getParameters());

							try {
								addWorkspace(restoreUtils.storeWorkspace(wovo), false);
							} catch (CommonBusinessException e1) {
								Errors.getInstance().showExceptionDialog(mf, e1);									
							}
						} else {
							try {
								WorkspaceVO wovo2 = preferencesFacadeRemote.getWorkspace(wovo.getId());
								wovo2.setId(null);
								wovo2.setName(newName);
								wovo2 = preferencesFacadeRemote.storeWorkspace(wovo2);
								addWorkspace(wovo2, false);
							} catch (Exception e1) {
								Errors.getInstance().showExceptionDialog(mf, e1);
							} 
						}
					}
				}
			};
	}
	
	private Action newRemoveAction(final WorkspaceVO wovo) {
		return new AbstractAction(
				SpringLocaleDelegate.getInstance().getMessage("WorkspaceChooserController.7","Löschen"), 
				MainFrame.resizeAndCacheIcon(Icons.getInstance().getIconRealDelete16(), ICON_SIZE)) {
				
				private static final long serialVersionUID = 1L;

				@Override
				public void actionPerformed(ActionEvent e) {
					if (wovo.equals(getSelectedWorkspace()) && !restoreUtils.closeTabs(true)) {
						return;
					}
					final MainFrame mf = Main.getInstance().getMainFrame();
					if (JOptionPane.YES_OPTION == 
						JOptionPane.showConfirmDialog(mf, 
							SpringLocaleDelegate.getInstance().getMessage(
									"WorkspaceChooserController.8","Möchten Sie wirklich die Arbeitsumgebung \"{0}\" löschen?",
									wovo.getWoDesc().getName()), 
							SpringLocaleDelegate.getInstance().getMessage("WorkspaceChooserController.9","Sind Sie sicher?"), 
							JOptionPane.YES_NO_OPTION)) {
						
						if (wovo.equals(getSelectedWorkspace()) && !restoreUtils.clearWorkspace()) {
							return;
						}
						
						preferencesFacadeRemote.removeWorkspace(wovo.getAssignedWorkspace()==null?
								wovo.getId():
									wovo.getAssignedWorkspace());
						workspaces.remove(wovo);
						
						if (wovo.equals(getSelectedWorkspace())) {
							for (WorkspaceVO wovo : workspaces) {
								try {
									restoreUtils.clearAndRestoreWorkspace(preferencesFacadeRemote.getWorkspace(wovo.getId()));
									setSelectedWorkspace(wovo);
									refreshWorkspaces();
									return;
								} catch (Exception ex) {
									// deleted workspaces
								}
							}
							
							if (workspaces.isEmpty()) {
								try {
									addWorkspace(restoreUtils.clearAndRestoreToDefaultWorkspace(), true);
								} catch (CommonBusinessException e1) {
									Errors.getInstance().showExceptionDialog(mf, e1);
								}
							} 
						} else {
							refreshWorkspaces();
						}
					}
				}
			};
	}
	
	private Action newAssignAction(final WorkspaceVO wovo, final Label lb) {
		return new AbstractAction(
				SpringLocaleDelegate.getInstance().getMessage(
						"WorkspaceChooserController.10","Benutzergruppen zuweisen"), 
				MainFrame.resizeAndCacheIcon(Icons.getInstance().getIconRelate(), ICON_SIZE)) {
				
				private static final long serialVersionUID = 1L;

				@Override
				public void actionPerformed(ActionEvent e) {
					final MainFrame mf = Main.getInstance().getMainFrame();
					// assigned and assignable roles
					final Collection<Long> assignedRoleIds = preferencesFacadeRemote.getAssignedRoleIds(wovo.getAssignedWorkspace());
					final Collection<RoleAssignment> assignableRoles = CollectionUtils.transform(preferencesFacadeRemote.getAssignableRoles(), 
							new Transformer<EntityObjectVO, RoleAssignment>() {
						@Override
						public RoleAssignment transform(EntityObjectVO eovo) {
							return new RoleAssignment(eovo.getField("name", String.class), eovo.getId());
						}
					});
					
					// select controller
					ChoiceList<RoleAssignment> cl = new ChoiceList<RoleAssignment>();
					Comparator<RoleAssignment> comp = new Comparator<RoleAssignment>(){
						@Override
						public int compare(RoleAssignment o1,
								RoleAssignment o2) {
							return o1.role.compareToIgnoreCase(o2.role);
						}};
					cl.set(assignableRoles, comp);
					cl.setSelectedFields(CollectionUtils.sorted(CollectionUtils.select(assignableRoles, new Predicate<RoleAssignment>() {
						@Override
						public boolean evaluate(RoleAssignment ra) {
							return assignedRoleIds.contains(ra.id);
						}
					}), comp));
					final SelectObjectsController<RoleAssignment> selectCtrl = new SelectObjectsController<RoleAssignment>(
							mf, 
							new DefaultSelectObjectsPanel<RoleAssignment>());
					selectCtrl.setModel(cl);
					
					// result ok=true
					if (selectCtrl.run(SpringLocaleDelegate.getInstance().getMessage(
							"WorkspaceChooserController.11","Arbeitsumgebung Benutzergruppen zuweisen"))) {
						final Icon icoBackup = lb.getIcon();
						final String toolTipBackup = lb.getToolTipText();
						
						lb.setIcon(MainFrame.resizeAndCacheIcon(Icons.getInstance().getIconSaveS16(), ICON_SIZE));
						lb.setToolTipText(SpringLocaleDelegate.getInstance().getResource(
								"WorkspaceChooserController.12","Zuweisung wird gespeichert") + "...");
						
						WorkspaceChooserController.this.setEnabled(false);
						
						SwingWorker<WorkspaceVO, WorkspaceVO> worker = new SwingWorker<WorkspaceVO, WorkspaceVO>() {
							@Override
							protected WorkspaceVO doInBackground() throws Exception {
								Thread.sleep(500); // otherwise eyesore flash of save icon
								if (getSelectedWorkspace().equals(wovo))
									restoreUtils.storeWorkspace(wovo);
								return preferencesFacadeRemote.assignWorkspace(wovo, CollectionUtils.transform(selectCtrl.getSelectedObjects(), new Transformer<RoleAssignment, Long>() {
									@Override
									public Long transform(RoleAssignment ra) {
										return ra.id;
									}
								}));
							}
							@Override
							protected void done() {
								try {
									final WorkspaceVO assignedWorkspace = get();
									final int index = workspaces.indexOf(wovo);
									workspaces.remove(wovo);
									workspaces.add(index, assignedWorkspace);
									if (wovo.equals(getSelectedWorkspace())) {
										setSelectedWorkspace(assignedWorkspace);
									}
									refreshWorkspaces();
									
								} catch (ExecutionException e) {
									if (e.getCause() != null && e.getCause() instanceof CommonBusinessException) {
										Errors.getInstance().showExceptionDialog(mf, e.getCause());
									} else {
										Errors.getInstance().showExceptionDialog(mf, e);
									}
								} catch (Exception e) {
									Errors.getInstance().showExceptionDialog(mf, e);
								} finally {
									lb.setIcon(icoBackup);
									lb.setToolTipText(toolTipBackup);
									WorkspaceChooserController.this.setEnabled(true);
								} 
							}
						};
						
						worker.execute();
					}
					
				}
			};
	}
	
	private Action newPublishAction(final WorkspaceVO wovo, final Label lb) {
		final MainFrame mf = Main.getInstance().getMainFrame();
		return new AbstractAction(
				SpringLocaleDelegate.getInstance().getMessage("WorkspaceChooserController.14","Änderungen in Vorlage publizieren"), 
				MainFrame.resizeAndCacheIcon(Icons.getInstance().getIconRedo16(), ICON_SIZE)) {

					private static final long serialVersionUID = 1L;

					@Override
					public void actionPerformed(ActionEvent e) {
						try {
							if (getSelectedWorkspace().equals(wovo)) {
								restoreUtils.storeWorkspace(wovo);
							}
							boolean structureChanged = preferencesFacadeRemote.isWorkspaceStructureChanged(wovo.getAssignedWorkspace(), wovo.getId());
							WorkspacePublisher wp = new WorkspacePublisher(structureChanged);
							if (wp.isSaved()) {
								preferencesFacadeRemote.publishWorkspaceChanges(wovo, 
										wp.isPublishStructureChange(),
										wp.isPublishStructureUpdate(),
										wp.isPublishStarttabConfiguration(),
										wp.isPublishTableColumnConfiguration(),
										wp.isPublishToolbarConfiguration());
								
								final String message = SpringLocaleDelegate.getInstance().getMessage(
										"WorkspaceChooserController.16","Änderungen erfolgreich publiziert.");
								SwingUtilities.invokeLater(new Runnable() {
									@Override
									public void run() {
										try {
											(new Bubble(lb, message, 8, Bubble.Position.SE)).setVisible(true);
										} catch (IllegalComponentStateException e) {
											JOptionPane.showMessageDialog(mf, message);
										}
									}
								});
							}
						} catch (Exception e1) {
							Errors.getInstance().showExceptionDialog(mf, e1);
						}
					}
		};
	}
	
	public Action newRestoreAction(final WorkspaceVO wovo) {
		return new AbstractAction(
				SpringLocaleDelegate.getInstance().getMessage("WorkspaceChooserController.15","Auf Vorlage zurücksetzen"), 
				MainFrame.resizeAndCacheIcon(Icons.getInstance().getIconUndo16(), ICON_SIZE)) {

					private static final long serialVersionUID = 1L;

					@Override
					public void actionPerformed(ActionEvent e) {
						restoreToAssigned(wovo);
					}
		};
	}
	
	/**
	 * restore selected workspace
	 */
	public void restoreSelectedWorkspace() {
		if (!restoreUtils.isRestoreRunning()) {
			if (getSelectedWorkspace().getAssignedWorkspace() == null) {
				if (!restoreUtils.closeTabs(true)) {
					return;
				}
				final MainFrame mf = Main.getInstance().getMainFrame();
				if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(mf,
					SpringLocaleDelegate.getInstance().getMessage(
							"MainFrame.restoreDefaultWorkspace.2","Möchten Sie wirklich die Arbeitsumgebung auf den Ausgangszustand zurücksetzen?"),
					SpringLocaleDelegate.getInstance().getMessage(
							"MainFrame.restoreDefaultWorkspace.1","Arbeitsumgebung zurücksetzen"),
					JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE)) {
					try {
						WorkspaceDescription wdBackup = getSelectedWorkspace().getWoDesc();
						getSelectedWorkspace().setWoDesc(WorkspaceDescriptionDefaultsFactory.createOldMdiStyle());
						getSelectedWorkspace().getWoDesc().importHeader(wdBackup);
						
						restoreUtils.clearAndRestoreWorkspace(getSelectedWorkspace());
						restoreUtils.storeWorkspace(getSelectedWorkspace());
						
						getSelectedWorkspace().getWoDesc().getFrames().clear();
					} catch (CommonBusinessException e) {
						Errors.getInstance().showExceptionDialog(mf, e);
					}
				}
			} else {
				restoreToAssigned(getSelectedWorkspace());
			}
		}
	}
	
	/**
	 * 
	 * @param wovo
	 */
	private void restoreToAssigned(WorkspaceVO wovo) {
		if (getSelectedWorkspace().equals(wovo)) {
			if (!restoreUtils.closeTabs(true)) {
				return;
			}
		}
		final MainFrame mf = Main.getInstance().getMainFrame();
		if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(mf,
				SpringLocaleDelegate.getInstance().getMessage(
						"MainFrame.restoreDefaultWorkspace.2","Möchten Sie wirklich die Arbeitsumgebung auf den Ausgangszustand zurücksetzen?"),
				SpringLocaleDelegate.getInstance().getMessage(
						"MainFrame.restoreDefaultWorkspace.1","Arbeitsumgebung zurücksetzen"),
				JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE)) {
			try {
				WorkspaceVO restoredWovo = preferencesFacadeRemote.restoreWorkspace(wovo);
				wovo.importHeader(restoredWovo.getWoDesc());
				refreshWorkspaces();
				if (getSelectedWorkspace().equals(wovo)) {
					restoreUtils.clearAndRestoreWorkspace(restoredWovo);
				}
			} catch (Exception e1) {
				Errors.getInstance().showExceptionDialog(mf, e1);
			}
		}
	}
	
	private static class ContentPanel extends JPanel {
		
		public ContentPanel() {
			super(new FlowLayout(FlowLayout.RIGHT, 0, 0));
		}
		
		@Override
		public Dimension getPreferredSize() {
			return new Dimension (super.getPreferredSize().width, WorkspacePanel.imgBG_h);
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
	
	protected final static ImageIcon imgBG = Icons.getInstance().getWorkspaceChooser_buttonBG();
	protected final static ImageIcon imgBG_left = Icons.getInstance().getWorkspaceChooser_buttonLeft();
	protected final static ImageIcon imgBG_right = Icons.getInstance().getWorkspaceChooser_buttonRight();

	public  final static int imgBG_h = imgBG.getIconHeight();
	protected final static int imgBG_w = imgBG.getIconWidth();
	protected final static int imgBG_left_w = imgBG_left.getIconWidth();
	protected final static int imgBG_right_w = imgBG_right.getIconWidth();

	private abstract class Label extends JLabel {
		
		protected boolean mouseOver = false;
		
		public Label(String text, Icon icon, int horizontalAlignment) {
			super(text, icon, horizontalAlignment);
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
			float[] scales = { 1f, 1f, 1f, ((mouseOver && enabled) || isSelected())?1f:0.5f };
			float[] offsets = new float[4];
			RescaleOp rop = new RescaleOp(scales, offsets, null);
		   	
			g2.drawImage(biBG_left, rop, -1, 0);
		   	for (int i = 0; i < (getWidth()-imgBG_left_w-imgBG_right_w) + 2; i++) {
		   		g2.drawImage(biBG, rop, imgBG_left_w -1 + (i * imgBG_w), 0);
		   	}
		   	g2.drawImage(biBG_right, rop, getWidth()-imgBG_right_w +1, 0);

			g2.setRenderingHints(oldRH);
			
			super.paintComponent(g);
			
			if (drawDogEar()) {
				ImageIcon assignedIcon = Icons.getInstance().getWorkspaceChooser_assigned();
				g2.drawImage(assignedIcon.getImage(),
					getWidth()-assignedIcon.getIconWidth(),
					0, null);
			}
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
		
		abstract boolean isSelected();
		
		abstract boolean drawDogEar();
	}
	
	private Icon getWorkspaceIcon(final WorkspaceVO wovo, int size) {
		final Icon ico = wovo.getWoDesc().getNuclosResource()==null?
				Icons.getInstance().getIconTabGeneric():
				NuclosResourceCache.getNuclosResourceIcon(wovo.getWoDesc().getNuclosResource());
		return MainFrame.resizeAndCacheIcon(ico, size);
	}
	
	private class WorkspaceLabel extends Label implements DragGestureListener {
		
		private final WorkspaceVO wovo;		
		
		public WorkspaceLabel(WorkspaceVO wovo) {
			super(
					wovo.getWoDesc().isHideName()?
							null:
							wovo.getName(), 
					getWorkspaceIcon(wovo, ICON_SIZE), 
					SwingConstants.LEFT);
			this.wovo = wovo;
			setBorder(BorderFactory.createEmptyBorder(imgBG_left_w, imgBG_left_w, imgBG_left_w, imgBG_left_w));
			if (wovo.getWoDesc().isHideName()) {
				setToolTipText(wovo.getName());
			}
			DragSource dragSource = DragSource.getDefaultDragSource();
		    dragSource.createDefaultDragGestureRecognizer(WorkspaceLabel.this, DnDConstants.ACTION_COPY_OR_MOVE, WorkspaceLabel.this);
		    setTransferHandler(new TransferHandler() {
				@Override
				public boolean importData(JComponent comp, Transferable t) {
					try {
						WorkspaceVO other = (WorkspaceVO) t.getTransferData(workspaceFlavor);
						if (other != null &&
								!other.equals(WorkspaceLabel.this.wovo)) {
							WorkspaceVO wovoToRemove = null;
							for (WorkspaceVO wovoI : workspaces) {
								if (wovoI.equals(other)) {
									wovoToRemove = wovoI;
								}
							}
							int indexToAdd = 0;
							for (WorkspaceVO wovoI : workspaces) {
								if (wovoI.equals(WorkspaceLabel.this.wovo)) {
									indexToAdd = workspaces.indexOf(wovoI);
								}
							}
							if (wovoToRemove != null) workspaces.remove(wovoToRemove);
							workspaces.add(indexToAdd, other);
							
							WorkspaceChooserController.this.refreshWorkspaces();
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
		
		public WorkspaceVO getWorkspaceVO() {
			return wovo;
		}

		public boolean isSelected() {
			return wovo.equals(getSelectedWorkspace());
		}

		@Override
		public void dragGestureRecognized(DragGestureEvent dge) {
			Transferable transferable = new WorkspaceLabelTransferable(WorkspaceLabel.this.wovo);
		    dge.startDrag(null, transferable, null);
		    mouseOver = false;
			repaint();
		}

		@Override
		boolean drawDogEar() {
			return wovo.getAssignedWorkspace() != null;
		}
	}
	
	private static class RoleAssignment {
		final String role;
		final Long id;
		public RoleAssignment(String role, Long id) {
			super();
			this.role = role;
			this.id = id;
		}
		@Override
		public int hashCode() {
			if (role == null) return 0;
			return role.hashCode();
		}
		@Override
		public boolean equals(Object obj) {
			if (obj instanceof RoleAssignment)
				return this.id.equals(((RoleAssignment) obj).id);
			return super.equals(obj);
		}
		@Override
		public String toString() {
			return role;
		}
	}
	
	private static class WorkspaceLabelTransferable implements Transferable {
		
		final WorkspaceVO wovo;
		
		public WorkspaceLabelTransferable(WorkspaceVO wovo) {
			this.wovo = wovo;
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
		      return wovo;
		    }
		    return null;
		}
	}
	
	public static final DataFlavor workspaceFlavor = new DataFlavor(WorkspaceVO.class, "WorkspaceVO");
	private static final DataFlavor[] flavors = new DataFlavor[] {workspaceFlavor};
	
}
