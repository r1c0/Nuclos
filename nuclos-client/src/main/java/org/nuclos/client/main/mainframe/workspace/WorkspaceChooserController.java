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
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.TransferHandler;

import org.nuclos.client.common.security.SecurityCache;
import org.nuclos.client.main.Main;
import org.nuclos.client.main.mainframe.MainFrame;
import org.nuclos.client.resource.NuclosResourceCache;
import org.nuclos.client.ui.Bubble;
import org.nuclos.client.ui.DefaultSelectObjectsPanel;
import org.nuclos.client.ui.Errors;
import org.nuclos.client.ui.Icons;
import org.nuclos.client.ui.SelectObjectsController;
import org.nuclos.client.ui.model.ChoiceList;
import org.nuclos.common.Actions;
import org.nuclos.common.WorkspaceDescription;
import org.nuclos.common.WorkspaceDescriptionDefaultsFactory;
import org.nuclos.common.WorkspaceVO;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.Predicate;
import org.nuclos.common.collection.Transformer;
import org.nuclos.common.dal.vo.EntityObjectVO;
import org.nuclos.common2.CommonLocaleDelegate;
import org.nuclos.common2.LangUtils;
import org.nuclos.common2.ServiceLocator;
import org.nuclos.common2.StringUtils;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.server.common.ejb3.PreferencesFacadeRemote;

public class WorkspaceChooserController {
	
	public static final int ICON_SIZE = 16;
	private static WorkspaceVO selectedWorkspace = null;
	private static final List<WorkspaceVO> workspaces = new ArrayList<WorkspaceVO>();
	private static final WorkspacePanel workspacePanel = new WorkspacePanel();
	private static final ContentPanel contentPanel = new ContentPanel();
	private static JLabel jlbLeft;
	private static JLabel jlbRight;

	private static boolean enabled;
	
	private static List<Long> workspaceOrderIds;
	private static List<String> workspaceOrder;
	
	public WorkspaceChooserController() {
		super();
		initWorkspaceChooser();
	}
	
	/**
	 * 
	 */
	private void initWorkspaceChooser() {
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
	
	/**
	 * 
	 * @param workspaceOrderIds
	 * @param workspaceOrder
	 */
	public static void setupWorkspaces(List<Long> workspaceOrderIds, List<String> workspaceOrder) {
		WorkspaceChooserController.workspaceOrderIds = workspaceOrderIds;
		WorkspaceChooserController.workspaceOrder = workspaceOrder;
		
		setupWorkspaces();
	}
	
	/**
	 * 
	 */
	public static void setupWorkspaces() {
		Collection<WorkspaceVO> allWorkspaces = getPrefsFacade().getWorkspaceHeaderOnly(); 
			
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
	private static void refreshWorkspaces() {
		workspacePanel.removeAll();
		
		for (final WorkspaceVO wovo : workspaces) {
			
			final WorkspaceLabel wl = new WorkspaceLabel(wovo);
			wl.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					if (enabled && SwingUtilities.isLeftMouseButton(e) &&
							!wl.isSelected()) {
						try {
							RestoreUtils.storeWorkspace(getSelectedWorkspace());
						} catch (CommonBusinessException e1) {
							if ("Workspace.not.found".equals(e1.getMessage())) {
								workspaces.remove(getSelectedWorkspace());
								refreshWorkspaces();
							} else {
								Errors.getInstance().showExceptionDialog(Main.getMainFrame(), e1);
							}
						}
						try {
							RestoreUtils.clearAndRestoreWorkspace(getPrefsFacade().getWorkspace(wl.getWorkspaceVO().getId()));
						} catch (CommonBusinessException e1) {
							if ("Workspace.not.found".equals(e1.getMessage())) {
								workspaces.remove(wl.getWorkspaceVO());
								refreshWorkspaces();
							}
							Errors.getInstance().showExceptionDialog(Main.getMainFrame(), e1);
						}
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
	
	private static void addWorkspace(WorkspaceVO wovo, boolean select) {
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
	
	public static List<WorkspaceVO> getWorkspaceHeaders() {
		return new ArrayList<WorkspaceVO>(workspaces);
	}
	
	public static WorkspaceVO getSelectedWorkspace() {
		return selectedWorkspace;
	}
	
	public static void setSelectedWorkspace(WorkspaceVO wovo) {
		for (WorkspaceVO wovoI : workspaces) {
			if (wovoI.equals(wovo)) {
				selectedWorkspace = wovoI;
				
				// preferences may be changed (e.g. restore to assigned)...
				if (selectedWorkspace != wovo) {
					selectedWorkspace.getWoDesc().removeAllEntityPreferences();
					selectedWorkspace.getWoDesc().addAllEntityPreferences(wovo.getWoDesc().getEntityPreferences());
				}
				
				contentPanel.repaint();
				
				MainFrame.setSplittingEnabled(
						wovo.getAssignedWorkspace() == null || 
						(wovo.getAssignedWorkspace() != null && SecurityCache.getInstance().isActionAllowed(Actions.ACTION_WORKSPACE_ASSIGN)));
			}
		}
	}

	public JComponent getChooserComponent() {
		return contentPanel;
	}
	
	public static void setEnabled(boolean b) {
		enabled = b;
	}
	
	public static boolean isEnabled() {
		return enabled;
	}
	
	private static void setupContextMenu(final WorkspaceLabel wl) {		
		wl.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (enabled && SwingUtilities.isRightMouseButton(e)) {
					
					/**
					 * CHECK 
					 */
					try {
						getPrefsFacade().getWorkspace(wl.getWorkspaceVO().getId());
					} catch (Exception ex) {
						if ("Workspace.not.found".equals(ex.getMessage())) {
							workspaces.remove(wl.getWorkspaceVO());
							refreshWorkspaces();
						}
						Errors.getInstance().showExceptionDialog(Main.getMainFrame(), ex);
						return;
					}
					
					final JPopupMenu popup = new JPopupMenu();
					popup.add(new JLabel("<html><b>"+CommonLocaleDelegate.getMessage("WorkspaceChooserController.2","Arbeitsumgebung")+"</b></html>"));
					
					/**
					 * REFRESH
					 */
					final JMenuItem miRefresh = new JMenuItem(new AbstractAction(
						CommonLocaleDelegate.getMessage("WorkspaceChooserController.13","Leiste aktualisieren"), 
						MainFrame.resizeAndCacheIcon(Icons.getInstance().getIconRefresh16(), ICON_SIZE)) {

							private static final long serialVersionUID = 1L;

							@Override
							public void actionPerformed(ActionEvent e) {
								workspaceOrder.clear();
								workspaceOrderIds.clear();
								for (WorkspaceVO wovo : workspaces) {
									workspaceOrderIds.add(wovo.getId());
								}
								workspaces.clear();
								setupWorkspaces();
							}
					});
					popup.add(miRefresh);
					
					if (wl.getWorkspaceVO().getAssignedWorkspace() == null ||
							SecurityCache.getInstance().isActionAllowed(Actions.ACTION_WORKSPACE_ASSIGN) || 
							SecurityCache.getInstance().isActionAllowed(Actions.ACTION_WORKSPACE_CREATE_NEW))
						popup.addSeparator();
					
					/**
					 * NEW
					 */
					if (SecurityCache.getInstance().isActionAllowed(Actions.ACTION_WORKSPACE_ASSIGN) || 
							SecurityCache.getInstance().isActionAllowed(Actions.ACTION_WORKSPACE_CREATE_NEW)) {
						final JMenuItem miNew = new JMenuItem(new AbstractAction(
							CommonLocaleDelegate.getMessage("WorkspaceChooserController.1","Neu"), 
							MainFrame.resizeAndCacheIcon(Icons.getInstance().getIconNew16(), ICON_SIZE)) {
	
								private static final long serialVersionUID = 1L;
	
							@Override
							public void actionPerformed(ActionEvent e) {
								if (!RestoreUtils.closeTabs(true)) {
									return;
								}
								
								try {
									WorkspaceVO wovo = new WorkspaceVO(); // only for header information
									WorkspaceEditor we = new WorkspaceEditor(wovo);
									if (we.isSaved()) {
										try {
											RestoreUtils.storeWorkspace(getSelectedWorkspace());
										} catch (Exception e2) {
											Errors.getInstance().showExceptionDialog(Main.getMainFrame(), e2);
										}
										addWorkspace(RestoreUtils.clearAndRestoreToDefaultWorkspace(wovo.getWoDesc()), true);
									}
								} catch (Exception e1) {
									Errors.getInstance().showExceptionDialog(Main.getMainFrame(), e1);
								} 
							}
						});
						popup.add(miNew);
					}
					
					/**
					 * EDIT
					 */
					if (wl.getWorkspaceVO().getAssignedWorkspace() == null || SecurityCache.getInstance().isActionAllowed(Actions.ACTION_WORKSPACE_ASSIGN)) {
						final JMenuItem miEdit = new JMenuItem(new AbstractAction(
								CommonLocaleDelegate.getMessage("WorkspaceChooserController.3","Eigenschaften"), 
								MainFrame.resizeAndCacheIcon(Icons.getInstance().getIconEdit16(), ICON_SIZE)) {

								private static final long serialVersionUID = 1L;
	
								@Override
								public void actionPerformed(ActionEvent e) {
									WorkspaceEditor we = new WorkspaceEditor(wl.getWorkspaceVO());
									try {
										if (we.isSaved()) {
											getPrefsFacade().storeWorkspaceHeaderOnly(wl.getWorkspaceVO());
											refreshWorkspaces();
										}
									} catch (Exception e1) {
										we.revertChanges();
										Errors.getInstance().showExceptionDialog(Main.getMainFrame(), e1);
									} 
								}
							});
						popup.add(miEdit);
					}
					
					/**
					 * CLONE
					 */
					if (SecurityCache.getInstance().isActionAllowed(Actions.ACTION_WORKSPACE_ASSIGN) || 
							SecurityCache.getInstance().isActionAllowed(Actions.ACTION_WORKSPACE_CREATE_NEW)) {
						final JMenuItem miSaveAs = new JMenuItem(new AbstractAction(
							CommonLocaleDelegate.getMessage("WorkspaceChooserController.4","Klonen"), 
							MainFrame.resizeAndCacheIcon(Icons.getInstance().getIconClone16(), ICON_SIZE)) {
	
							private static final long serialVersionUID = 1L;
	
							@Override
							public void actionPerformed(ActionEvent e) {
								String newName = JOptionPane.showInputDialog(Main.getMainFrame(), 
									CommonLocaleDelegate.getMessage("WorkspaceChooserController.6","Arbeitsumgebung \"{0}\" klonen",wl.getWorkspaceVO().getWoDesc().getName()) + ":", 
									CommonLocaleDelegate.getMessage("WorkspaceChooserController.5","Neuer Name"), 
									JOptionPane.INFORMATION_MESSAGE);
								
								if (!StringUtils.looksEmpty(newName)) {
									if (getSelectedWorkspace().equals(wl.getWorkspaceVO())) {
										WorkspaceVO wovo = new WorkspaceVO();
										wovo.importHeader(wl.getWorkspaceVO().getWoDesc());
										wovo.setName(newName);
										wovo.getWoDesc().addAllEntityPreferences(wl.getWorkspaceVO().getWoDesc().getEntityPreferences());
	
										try {
											addWorkspace(RestoreUtils.storeWorkspace(wovo), false);
										} catch (CommonBusinessException e1) {
											Errors.getInstance().showExceptionDialog(Main.getMainFrame(), e1);									
										}
									} else {
										try {
											PreferencesFacadeRemote prefsFac = getPrefsFacade();
											WorkspaceVO wovo = prefsFac.getWorkspace(wl.getWorkspaceVO().getId());
											wovo.setId(null);
											wovo.setName(newName);
											wovo = prefsFac.storeWorkspace(wovo);
											addWorkspace(wovo, false);
										} catch (Exception e1) {
											Errors.getInstance().showExceptionDialog(Main.getMainFrame(), e1);
										} 
									}
								}
							}
						});
						popup.add(miSaveAs);
					}
					
					/**
					 * REMOVE
					 */
					if (wl.getWorkspaceVO().getAssignedWorkspace() == null || SecurityCache.getInstance().isActionAllowed(Actions.ACTION_WORKSPACE_ASSIGN)) {
						popup.addSeparator();
						final JMenuItem miRemove = new JMenuItem(new AbstractAction(
							CommonLocaleDelegate.getMessage("WorkspaceChooserController.7","Löschen"), 
							MainFrame.resizeAndCacheIcon(Icons.getInstance().getIconRealDelete16(), ICON_SIZE)) {
							
							private static final long serialVersionUID = 1L;
	
							@Override
							public void actionPerformed(ActionEvent e) {
								if (wl.getWorkspaceVO().equals(getSelectedWorkspace()) && !RestoreUtils.closeTabs(true)) {
									return;
								}
								
								if (JOptionPane.YES_OPTION == 
									JOptionPane.showConfirmDialog(Main.getMainFrame(), 
										CommonLocaleDelegate.getMessage("WorkspaceChooserController.8","Möchten Sie wirklich die Arbeitsumgebung \"{0}\" löschen?",
												wl.getWorkspaceVO().getWoDesc().getName()), 
										CommonLocaleDelegate.getMessage("WorkspaceChooserController.9","Sind Sie sicher?"), 
										JOptionPane.YES_NO_OPTION)) {
									
									if (wl.getWorkspaceVO().equals(getSelectedWorkspace()) && !RestoreUtils.clearWorkspace()) {
										return;
									}
									
									getPrefsFacade().removeWorkspace(wl.getWorkspaceVO().getAssignedWorkspace()==null?
											wl.getWorkspaceVO().getId():
												wl.getWorkspaceVO().getAssignedWorkspace());
									workspaces.remove(wl.getWorkspaceVO());
									
									if (wl.getWorkspaceVO().equals(getSelectedWorkspace())) {
										for (WorkspaceVO wovo : workspaces) {
											try {
												RestoreUtils.clearAndRestoreWorkspace(getPrefsFacade().getWorkspace(wovo.getId()));
												setSelectedWorkspace(wovo);
												refreshWorkspaces();
												return;
											} catch (Exception ex) {
												// deleted workspaces
											}
										}
										
										if (workspaces.isEmpty()) {
											try {
												addWorkspace(RestoreUtils.clearAndRestoreToDefaultWorkspace(), true);
											} catch (CommonBusinessException e1) {
												Errors.getInstance().showExceptionDialog(Main.getMainFrame(), e1);
											}
										} 
									} else {
										refreshWorkspaces();
									}
								}
							}
						});
						popup.add(miRemove);
					}
					
					if (wl.getWorkspaceVO().getAssignedWorkspace() != null || 
							SecurityCache.getInstance().isActionAllowed(Actions.ACTION_WORKSPACE_ASSIGN))
						popup.addSeparator();
					
					if(SecurityCache.getInstance().isActionAllowed(Actions.ACTION_WORKSPACE_ASSIGN)) {
						/**
						 * ASSIGN
						 */
						final JMenuItem miAssign = new JMenuItem(new AbstractAction(
								CommonLocaleDelegate.getMessage("WorkspaceChooserController.10","Benutzergruppen zuweisen"), 
								MainFrame.resizeAndCacheIcon(Icons.getInstance().getIconRelate(), ICON_SIZE)) {
								
								private static final long serialVersionUID = 1L;

								@Override
								public void actionPerformed(ActionEvent e) {
									
									// assigned and assignable roles
									final Collection<Long> assignedRoleIds = getPrefsFacade().getAssignedRoleIds(wl.getWorkspaceVO().getAssignedWorkspace());
									final Collection<RoleAssignment> assignableRoles = CollectionUtils.transform(getPrefsFacade().getAssignableRoles(), new Transformer<EntityObjectVO, RoleAssignment>() {
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
											Main.getMainFrame(), 
											new DefaultSelectObjectsPanel<RoleAssignment>());
									selectCtrl.setModel(cl);
									
									// result ok=true
									if (selectCtrl.run(CommonLocaleDelegate.getMessage("WorkspaceChooserController.11","Arbeitsumgebung Benutzergruppen zuweisen"))) {
										final Icon icoBackup = wl.getIcon();
										final String toolTipBackup = wl.getToolTipText();
										
										wl.setIcon(MainFrame.resizeAndCacheIcon(Icons.getInstance().getIconSaveS16(), ICON_SIZE));
										wl.setToolTipText(CommonLocaleDelegate.getResource("WorkspaceChooserController.12","Zuweisung wird gespeichert") + "...");
										
										WorkspaceChooserController.setEnabled(false);
										
										SwingWorker<WorkspaceVO, WorkspaceVO> worker = new SwingWorker<WorkspaceVO, WorkspaceVO>() {
											@Override
											protected WorkspaceVO doInBackground() throws Exception {
												Thread.sleep(500); // otherwise eyesore flash of save icon
												if (getSelectedWorkspace().equals(wl.getWorkspaceVO()))
													RestoreUtils.storeWorkspace(wl.getWorkspaceVO());
												return getPrefsFacade().assignWorkspace(wl.getWorkspaceVO(), CollectionUtils.transform(selectCtrl.getSelectedObjects(), new Transformer<RoleAssignment, Long>() {
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
													final int index = workspaces.indexOf(wl.getWorkspaceVO());
													workspaces.remove(wl.getWorkspaceVO());
													workspaces.add(index, assignedWorkspace);
													if (wl.getWorkspaceVO().equals(getSelectedWorkspace())) {
														setSelectedWorkspace(assignedWorkspace);
													}
													refreshWorkspaces();
													
												} catch (ExecutionException e) {
													if (e.getCause() != null && e.getCause() instanceof CommonBusinessException) {
														Errors.getInstance().showExceptionDialog(Main.getMainFrame(), e.getCause());
													} else {
														Errors.getInstance().showExceptionDialog(Main.getMainFrame(), e);
													}
												} catch (Exception e) {
													Errors.getInstance().showExceptionDialog(Main.getMainFrame(), e);
												} finally {
													wl.setIcon(icoBackup);
													wl.setToolTipText(toolTipBackup);
													WorkspaceChooserController.setEnabled(true);
												} 
											}
										};
										
										worker.execute();
									}
									
								}
							});
							popup.add(miAssign);
							
							if (wl.getWorkspaceVO().getAssignedWorkspace() != null) {
								/**
								 * PUBLISH
								 */
								final JMenuItem miUpdate = new JMenuItem(new AbstractAction(
										CommonLocaleDelegate.getMessage("WorkspaceChooserController.14","Änderungen in Vorlage publizieren"), 
										MainFrame.resizeAndCacheIcon(Icons.getInstance().getIconRedo16(), ICON_SIZE)) {
	
											private static final long serialVersionUID = 1L;
	
											@Override
											public void actionPerformed(ActionEvent e) {
												try {
													if (getSelectedWorkspace().equals(wl.getWorkspaceVO())) {
														RestoreUtils.storeWorkspace(wl.getWorkspaceVO());
													}
													boolean structureChanged = getPrefsFacade().isWorkspaceStructureChanged(wl.getWorkspaceVO().getAssignedWorkspace(), wl.getWorkspaceVO().getId());
													
													WorkspacePublisher wp = new WorkspacePublisher(structureChanged);
													if (wp.isSaved()) {
														getPrefsFacade().publishWorkspaceChanges(wl.getWorkspaceVO(), 
																wp.isPublishStructureChange(),
																wp.isPublishStructureUpdate(),
																wp.isPublishStarttabConfiguration(),
																wp.isPublishTableColumnConfiguration(),
																wp.isPublishToolbarConfiguration());
														
														final String message = CommonLocaleDelegate.getMessage("WorkspaceChooserController.16","Änderungen erfolgreich publiziert.");
														SwingUtilities.invokeLater(new Runnable() {
															@Override
															public void run() {
																try {
																	(new Bubble(wl, message, 8, Bubble.Position.SE)).setVisible(true);
																} catch (IllegalComponentStateException e) {
																	JOptionPane.showMessageDialog(Main.getMainFrame(), message);
																}
															}
														});
													}
												} catch (Exception e1) {
													Errors.getInstance().showExceptionDialog(Main.getMainFrame(), e1);
												}
											}
								});
								popup.add(miUpdate);
							}
					}
					
					/**
					 * RESTORE
					 */
					if(wl.getWorkspaceVO().getAssignedWorkspace() != null) {
						final JMenuItem miUpdate = new JMenuItem(new AbstractAction(
								CommonLocaleDelegate.getMessage("WorkspaceChooserController.15","Auf Vorlage zurücksetzen"), 
								MainFrame.resizeAndCacheIcon(Icons.getInstance().getIconUndo16(), ICON_SIZE)) {

									private static final long serialVersionUID = 1L;

									@Override
									public void actionPerformed(ActionEvent e) {
										restoreToAssigned(wl.getWorkspaceVO());
									}
						});
						popup.add(miUpdate);
					}
					
					popup.show(wl, 10, 10);
				}
			}
		});
	}
	
	/**
	 * restore selected workspace
	 */
	public static void restoreSelectedWorkspace() {
		if (!RestoreUtils.isRestoreRunning()) {
			if (getSelectedWorkspace().getAssignedWorkspace() == null) {
				if (!RestoreUtils.closeTabs(true)) {
					return;
				}
				if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(Main.getMainFrame(),
					CommonLocaleDelegate.getMessage("MainFrame.restoreDefaultWorkspace.2","Möchten Sie wirklich die Arbeitsumgebung auf den Ausgangszustand zurücksetzen?"),
					CommonLocaleDelegate.getMessage("MainFrame.restoreDefaultWorkspace.1","Arbeitsumgebung zurücksetzen"),
					JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE)) {
					try {
						WorkspaceDescription wdBackup = getSelectedWorkspace().getWoDesc();
						getSelectedWorkspace().setWoDesc(WorkspaceDescriptionDefaultsFactory.createOldMdiStyle());
						getSelectedWorkspace().getWoDesc().importHeader(wdBackup);
						
						RestoreUtils.clearAndRestoreWorkspace(getSelectedWorkspace());
						RestoreUtils.storeWorkspace(getSelectedWorkspace());
						
						getSelectedWorkspace().getWoDesc().getFrames().clear();
					} catch (CommonBusinessException e) {
						Errors.getInstance().showExceptionDialog(Main.getMainFrame(), e);
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
	private static void restoreToAssigned(WorkspaceVO wovo) {
		if (getSelectedWorkspace().equals(wovo)) {
			if (!RestoreUtils.closeTabs(true)) {
				return;
			}
		}
		if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(Main.getMainFrame(),
				CommonLocaleDelegate.getMessage("MainFrame.restoreDefaultWorkspace.2","Möchten Sie wirklich die Arbeitsumgebung auf den Ausgangszustand zurücksetzen?"),
				CommonLocaleDelegate.getMessage("MainFrame.restoreDefaultWorkspace.1","Arbeitsumgebung zurücksetzen"),
				JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE)) {
			try {
				WorkspaceVO restoredWovo = getPrefsFacade().restoreWorkspace(wovo);
				wovo.importHeader(restoredWovo.getWoDesc());
				refreshWorkspaces();
				if (getSelectedWorkspace().equals(wovo)) {
					RestoreUtils.clearAndRestoreWorkspace(restoredWovo);
				}
			} catch (Exception e1) {
				Errors.getInstance().showExceptionDialog(Main.getMainFrame(), e1);
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
	
	private static class WorkspaceLabel extends JLabel implements DragGestureListener {
		
		private final WorkspaceVO wovo;
		
		private final static ImageIcon imgBG = Icons.getInstance().getWorkspaceChooser_buttonBG();
		private final static ImageIcon imgBG_left = Icons.getInstance().getWorkspaceChooser_buttonLeft();
		private final static ImageIcon imgBG_right = Icons.getInstance().getWorkspaceChooser_buttonRight();
		
		public  final static int imgBG_h = imgBG.getIconHeight();
		private final static int imgBG_w = imgBG.getIconWidth();
		private final static int imgBG_left_w = imgBG_left.getIconWidth();
		private final static int imgBG_right_w = imgBG_right.getIconWidth();
		
		private boolean mouseOver = false;
		
		public WorkspaceLabel(WorkspaceVO wovo) {
			super(
					wovo.getWoDesc().isHideName()?
							null:
							wovo.getName(), 
					MainFrame.resizeAndCacheIcon(wovo.getWoDesc().getNuclosResource()==null?
							Icons.getInstance().getIconTabGeneric():
							NuclosResourceCache.getNuclosResourceIcon(wovo.getWoDesc().getNuclosResource()), ICON_SIZE), 
					SwingConstants.LEFT);
			this.wovo = wovo;
			setBorder(BorderFactory.createEmptyBorder(imgBG_left_w, imgBG_left_w, imgBG_left_w, imgBG_left_w));
			if (wovo.getWoDesc().isHideName()) {
				setToolTipText(wovo.getName());
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
		
		public WorkspaceVO getWorkspaceVO() {
			return wovo;
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
			
			if (wovo.getAssignedWorkspace() != null) {
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
	
	private static PreferencesFacadeRemote getPrefsFacade() {
		return ServiceLocator.getInstance().getFacade(PreferencesFacadeRemote.class);
	}
}
