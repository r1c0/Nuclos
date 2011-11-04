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
package org.nuclos.client.ui.multiaction;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.TableModel;

import org.apache.log4j.Logger;
import org.nuclos.client.common.MetaDataClientProvider;
import org.nuclos.client.main.Main;
import org.nuclos.client.main.mainframe.MainFrameTab;
import org.nuclos.client.ui.Controller;
import org.nuclos.client.ui.Errors;
import org.nuclos.client.ui.MainFrameTabAdapter;
import org.nuclos.client.ui.UIUtils;
import org.nuclos.client.ui.collect.CollectController;
import org.nuclos.client.ui.popupmenu.JPopupMenuFactory;
import org.nuclos.client.ui.popupmenu.JTableJPopupMenuListener;
import org.nuclos.common.collect.collectable.Collectable;
import org.nuclos.common.collect.collectable.CollectableFieldFormat;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.csvparser.ExcelCSVPrinter;
import org.nuclos.common2.CommonLocaleDelegate;
import org.nuclos.common2.exception.CommonBusinessException;

/**
 * Controller for actions (delete, change etc.) on multiple <code>Collectable</code>s.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version	01.00.00
 */
public class MultiCollectablesActionController <T,R> extends Controller {
	
	private static final Logger LOG = Logger.getLogger(MultiCollectablesActionController.class);

	/**
	 * defines the action to perform on each object.
	 */
	public static interface Action <T,R> {
		/**
		 * performs the action on the given object.
		 * @param t
		 * @throws CommonBusinessException
		 * @precondition t != null
		 */
		R perform(T t) throws CommonBusinessException;

		/**
		 * @param t the object on which the action is to be performed.
		 * @return the text to display for the action on the given object.
		 * @precondition t != null
		 */
		String getText(T t);

		/**
		 * @param t the object on which the action was performed.
		 * @param rResult the result of the action.
		 * @return the text to display after successful execution of the action for the given object.
		 * @precondition t != null
		 */
		String getSuccessfulMessage(T t, R rResult);

		/**
		 * @param t
		 * @param ex
		 * @return the error message to display if the given exception occured during the execution of the action
		 * on the given object.
		 * @precondition t != null
		 * @precondition ex != null
		 */
		String getExceptionMessage(T t, Exception ex);

		/**
		 * @return the confirmation message to display if the user clicked "Stop".
		 */
		String getConfirmStopMessage();

		/**
		 * the final action to execute after the loop.
		 * @throws CommonBusinessException
		 */
		void executeFinalAction() throws CommonBusinessException;

	}  // interface Action


	protected final Collection<T> coll;
	protected final String sTitle;
	protected final Icon iconFrame;
	protected final Action<T, R> action;
	protected boolean error = false;
	protected boolean closable = false;

	/**
	 * @param parent must be a descendant of a JDesktopPane.
	 * @param coll Collection<Object>
	 * @param sTitle
	 * @param iconFrame
	 * @param action
	 */
	public MultiCollectablesActionController(MainFrameTab parent, Collection<T> coll, String sTitle, Icon iconFrame, Action<T, R> action) {
		super(parent);

		this.coll = coll;
		this.sTitle = sTitle;
		this.iconFrame = iconFrame;
		this.action = action;

		if (CollectionUtils.isNullOrEmpty(this.coll)) {
			throw new IllegalArgumentException();
		}
	}

	public MultiCollectablesActionController(CollectController<? extends Collectable> ctl, String sTitle, Action<T,R> action, Collection<T> collclct) {
		this(ctl.getFrame(), collclct, sTitle, ctl.getFrame().getTabIcon(), action);
	}

	public void run(final MultiActionProgressPanel pnl) {
		final MainFrameTab ifrm = new MainFrameTab(sTitle);
		ifrm.addMainFrameTabListener(new MainFrameTabAdapter() {
			@Override
			public boolean tabClosing(MainFrameTab tab)	throws CommonBusinessException {
				return closable;
			}
			@Override
			public void tabClosed(MainFrameTab tab) {
				tab.removeMainFrameTabListener(this);
				try {
					action.executeFinalAction();
				} catch (CommonBusinessException ex) {
					final String sMessage = CommonLocaleDelegate.getMessage("MultiCollectablesActionController.4","Nach dem Abschluss der Operation ist ein Fehler aufgetreten.");
					Errors.getInstance().showExceptionDialog(getParent(), sMessage, ex);
				}
			}

		});

		//ifrm.setContentPane(pnl);
		ifrm.setLayeredComponent(pnl);
		ifrm.setTabIcon(this.iconFrame);
		pnl.setStatus(CommonLocaleDelegate.getMessage("MultiCollectablesActionController.1","Vorgang l\u00e4uft..."));

		final MultiObjectsActionRunnable runnable = new MultiObjectsActionRunnable(this.coll.size(), ifrm, pnl);

//		if (getParent() instanceof CommonJInternalFrame) {
//			Main.getMainController().getControllerForInternalFrame((CommonJInternalFrame)getParent()).lockFrame(true);
//		}

		pnl.btnPause.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ev) {
				if (pnl.btnPause.isSelected()) {
					pnl.setStatus(CommonLocaleDelegate.getMessage("MultiCollectablesActionController.2","Wird angehalten..."));
					// disallow input:
					//setModalGlassPane(ifrm);
					ifrm.lockLayer();
					runnable.pause();
				}
				else {
					pnl.setStatus(CommonLocaleDelegate.getMessage("MultiCollectablesActionController.1","Vorgang l\u00e4uft..."));
					runnable.play();
				}
			}
		});

		pnl.btnStop.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ev) {
				// pause the action while the user thinks about it...
				runnable.pause();
				final int iBtn = JOptionPane.showConfirmDialog(ifrm, action.getConfirmStopMessage(), CommonLocaleDelegate.getMessage("MultiCollectablesActionController.3","Operation beenden"),
						JOptionPane.YES_NO_OPTION);
				if (iBtn == JOptionPane.YES_OPTION) {
					runnable.stop();
				}
				else {
					// continue with the action if it isn't paused by the user:
					if (!pnl.btnPause.isSelected()) {
						runnable.play();
					}
				}
			}
		});

		pnl.btnClose.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ev) {
				ifrm.dispose();
//				UIUtils.runCommand(MultiCollectablesActionController.this.getParent(), new Runnable() {
//					@Override
//					public void run() {
						try {
							if (getParent() instanceof MainFrameTab) {
								Main.getMainController().getControllerForInternalFrame((MainFrameTab)getParent()).lockFrame(false);
							}
							action.executeFinalAction();
						}
						catch (CommonBusinessException ex) {
							final String sMessage = CommonLocaleDelegate.getMessage("MultiCollectablesActionController.4","Nach dem Abschluss der Operation ist ein Fehler aufgetreten.");
							Errors.getInstance().showExceptionDialog(getParent(), sMessage, ex);
						}
//					}
//				});
			}
		});

		pnl.btnSaveResult.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				final TableModel mdl = pnl.tblResult.getModel();
				UIUtils.runCommandLater(pnl, new Runnable() {
					@Override
					public void run() {
						JFileChooser filechooser = new JFileChooser();
						FileFilter filefilter = new FileFilter() {
							@Override
							public boolean accept(File file) {
								return file.isDirectory() || file.getName().toLowerCase().endsWith(".csv");
							}

							@Override
							public String getDescription() {
								return CommonLocaleDelegate.getText("MultiCollectablesActionController.csv", "CSV-Dateien") + " (*.csv)";
							}
						};
						filechooser.addChoosableFileFilter(filefilter);
						filechooser.setFileFilter(filefilter);
						if(filechooser.showSaveDialog(MultiCollectablesActionController.this.getParent()) == JFileChooser.APPROVE_OPTION) {
							File file = filechooser.getSelectedFile();
							if(file != null) {
								if (file.isDirectory()) {
									file = new File(file,"log.csv");
								}
								String filename = file.getPath();
								if (!filename.toLowerCase().endsWith(".csv")) {
									filename += ".csv";
									file = new File(filename);
								}
								try {
									FileWriter writer = new FileWriter(file);
									ExcelCSVPrinter printer = new ExcelCSVPrinter(writer, 2, ';', '\"', true);
									printer.write(mdl.getColumnName(MultiActionProgressTableModel.COLUMN_STATE));
									printer.write(mdl.getColumnName(MultiActionProgressTableModel.COLUMN_RESULT));
									printer.writeln();
									for(int i = 0; i < mdl.getRowCount(); i++) {
										printer.write(mdl.getValueAt(i, MultiActionProgressTableModel.COLUMN_STATE).toString());
										printer.write(mdl.getValueAt(i, MultiActionProgressTableModel.COLUMN_RESULT).toString());
										printer.writeln();
									}
									writer.write("");
									writer.close();
								}
								catch (IOException ex) {
									Errors.getInstance().showExceptionDialog(MultiCollectablesActionController.this.getParent(), "MultiCollectablesActionController.saveresulterror", ex);
								}
							}
						}
					}

				});
			}
		});

		//pnl.setResultHandler(new MultiActionProgressResultHandler(this));
		pnl.tblResult.addMouseListener(new JTableJPopupMenuListener(pnl.tblResult, new JPopupMenuFactory() {
			@Override
			public JPopupMenu newJPopupMenu() {
				final List<MultiActionProgressLine> selection = pnl.getSelection();
				final JPopupMenu result = new JPopupMenu();
				final String text = (selection.size() > 1) ? pnl.getMultiSelectionMenuLabel() : pnl.getSingleSelectionMenuLabel();
				final JMenuItem item = new JMenuItem(text);
				item.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent ev) {
						pnl.handleMultiSelection(selection);
					}
				});
				result.add(item);
				return result;
			}
		}));

		((MainFrameTab) getParent()).add(ifrm);
		ifrm.setVisible(true);

		new Thread(runnable).start();
	}

	public static String getCollectableLabel(String entityname, Collectable clct) {
		String label = CommonLocaleDelegate.getTreeViewLabel(clct, entityname, MetaDataClientProvider.getInstance());

		String tmp = label != null ? label : clct.getIdentifierLabel();

		int idx = -1;
		while ((idx = tmp.indexOf("[$" + CollectableFieldFormat.class.getName() + ",")) != -1)
		{
			tmp = tmp.substring(0, idx) + tmp.substring(tmp.indexOf("$]") + 2);
		}
		return tmp;
	}

	private class MultiObjectsActionRunnable implements Runnable {
		private final int iCount;
		private final MultiActionProgressPanel pnl;
		private final MainFrameTab ifrm;
		private volatile boolean bStopped;
		private volatile boolean bPaused;

		private MultiObjectsActionRunnable(int iCount, MainFrameTab ifrm, MultiActionProgressPanel pnl) {
			this.iCount = iCount;
			this.pnl = pnl;
			this.ifrm = ifrm;
		}

		@Override
		public void run() {
			try {
				final Iterator<T> iter = coll.iterator();
				for (int i = 0; i < iCount; ++i) {
					while (this.bPaused && !this.bStopped) {
						// reset wait cursor:
						SwingUtilities.invokeLater(new Runnable() {
							@Override
							public void run() {
								try {
								ifrm.unlockLayer();
								pnl.setStatus(CommonLocaleDelegate.getMessage("MultiCollectablesActionController.5","Angehalten."));
								}
								catch (Exception e) {
									LOG.error("MultiObjectsActionRunnable.run failed: " + e, e);
								}
							}
						});
						synchronized (this) {
							try {
								this.wait();
							}
							catch (InterruptedException ex) {
								LOG.error(ex);
								// ignored
							}
						}
					}
					if (this.bStopped) {
						break;
					}
					final T t = iter.next();
					final String tId;
	
					final int j = i + 1;
	
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							try {
								pnl.setActionText(action.getText(t));
							}
							catch (Exception e) {
								LOG.error("MultiObjectsActionRunnable.run failed: " + e, e);
							}
						}
					});
	
					String result;
					String state;
					R oResult = null;
					try {
						oResult = action.perform(t);
						result = action.getSuccessfulMessage(t, oResult);
						state = pnl.getSuccessLabel();
					}
					catch (final Exception ex) {
						MultiCollectablesActionController.this.error = true;
						result = action.getExceptionMessage(t, ex);
						state = pnl.getExceptionLabel();
					}
					finally {
						SwingUtilities.invokeLater(new Runnable() {
							@Override
							public void run() {
								try {
									pnl.setProgress(j);
								}
								catch (Exception e) {
									LOG.error("MultiObjectsActionRunnable.run failed: " + e, e);
								}
							}
						});
					}
					addProtocolLineRun(t, oResult, result, state);
				}  // for
	
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						try {
							if (MultiCollectablesActionController.this.error) {
								JOptionPane.showMessageDialog(MultiCollectablesActionController.this.getParent(), CommonLocaleDelegate.getMessageFromResource("MultiCollectablesActionController.erroroccurred"), Errors.getInstance().getAppName(), JOptionPane.ERROR_MESSAGE);
							}
							closable = true;
							pnl.setActionText(" ");
							pnl.setCloseButton();
							pnl.setStatus(CommonLocaleDelegate.getMessage("MultiCollectablesActionController.6","Beendet."));
						}
						catch (Exception e) {
							LOG.error("MultiObjectsActionRunnable.run failed: " + e, e);
						}
					}
				});
			}
			catch (Exception e) {
				LOG.error("MultiObjectsActionRunnable.run failed: " + e, e);
			}
		}

		public void addProtocolLineRun(final Object source, final Object oResult, final String result, final String state){
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					try {
						pnl.addProtocolLine(source, oResult, result, state);
					}
					catch (Exception e) {
						LOG.error("addProtocolLineRun.run failed: " + e, e);
					}
				}
			});
		}

		/**
		 * pause
		 */
		public void pause() {
			this.bPaused = true;
		}

		/**
		 * continue
		 */
		public synchronized void play() {
			this.bPaused = false;
			this.notifyAll();
		}

		/**
		 * stop
		 */
		public synchronized void stop() {
			this.bStopped = true;
			this.notifyAll();
		}

	}  // inner class MultiObjectsActionRunnable

}  // class MultiCollectablesActionController

