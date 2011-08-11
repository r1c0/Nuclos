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
package org.nuclos.client.common;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.prefs.Preferences;

import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import org.apache.log4j.Logger;
import org.nuclos.client.desktop.DesktopUtils;
import org.nuclos.client.genericobject.CollectableGenericObjectFileChooser;
import org.nuclos.client.ui.Bubble;
import org.nuclos.client.ui.Errors;
import org.nuclos.client.ui.UIUtils;
import org.nuclos.client.ui.collect.component.custom.AbstractCollectableFileChooser;
import org.nuclos.client.ui.popupmenu.DefaultJPopupMenuListener;
import org.nuclos.common.NuclosBusinessException;
import org.nuclos.common.PointerException;
import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common.collect.collectable.CollectableValueField;
import org.nuclos.common.collect.exception.CollectableFieldFormatException;
import org.nuclos.common2.CommonLocaleDelegate;
import org.nuclos.common2.CommonRunnable;
import org.nuclos.common2.IOUtils;
import org.nuclos.common2.exception.CommonBusinessException;

/**
 * Base class for collectable file choosers.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 * @author	<a href="mailto:uwe.allner@novabit.de">uwe.allner</a>
 * @version 01.00.00
 */
public abstract class CollectableDocumentFileChooserBase extends AbstractCollectableFileChooser implements MouseListener, NuclosDropTargetVisitor {
	private static final Logger log = Logger.getLogger(CollectableGenericObjectFileChooser.class);
	private static final String PREFS_KEY_LAST_DIRECTORY = "lastDirectory";
	private static final String PREFS_NODE_COLLECTABLEFILECHOOSER = "CollectableFileChooser";

	public CollectableDocumentFileChooserBase(CollectableEntityField clctef, boolean bSearchable) {
		super(clctef, bSearchable);

		this.setupPopupMenu();
		//NUCLEUSINT-512
		getFileChooser().addMouseListener(this);

		getFileChooser().getBrowseButton().addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ev) {
				cmdBrowse();
			}
		});
		setupDragDrop();
	}

	protected void setupPopupMenu() {
		JPopupMenu popupmenu = newJPopupMenu();
		this.getFileChooser().getFileNameComponent().addMouseListener(new DefaultJPopupMenuListener(popupmenu));
	}

	@Override
	public JPopupMenu newJPopupMenu() {
		final JPopupMenu popupmenu = new JPopupMenu();
		final JMenuItem miOpen = new JMenuItem(CommonLocaleDelegate.getMessage("CollectableDocumentFileChooserBase.1","\u00d6ffnen"));
		popupmenu.add(miOpen);
		miOpen.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ev) {
				cmdOpenFile();
			}
		});
		final JMenuItem miSaveAs = new JMenuItem(CommonLocaleDelegate.getMessage("CollectableDocumentFileChooserBase.2","Speichern unter..."));
		popupmenu.add(miSaveAs);
		miSaveAs.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ev) {
				cmdSaveAs();
			}
		});
		final JMenuItem miReset = new JMenuItem(CommonLocaleDelegate.getMessage("CollectableFileNameChooserBase.1","Zur\u00fccksetzen"));
		popupmenu.add(miReset);
		miReset.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ev) {
				cmdReset();
			}
		});

		popupmenu.addPopupMenuListener(new PopupMenuListener() {
			@Override
			public void popupMenuWillBecomeVisible(PopupMenuEvent ev) {
				boolean bEnabled = false;
				try {
					final org.nuclos.common2.File file = (org.nuclos.common2.File) getField().getValue();
					bEnabled = (file != null);
				}
				catch (CollectableFieldFormatException ex) {
					// ignore
				}
				miOpen.setEnabled(bEnabled);
				miSaveAs.setEnabled(bEnabled);

				if (!getJComponent().isEnabled()) {
					miReset.setEnabled(false);
				}
				else {
					miReset.setEnabled(bEnabled);
				}
			}

			@Override
			public void popupMenuWillBecomeInvisible(PopupMenuEvent ev) {
			}

			@Override
			public void popupMenuCanceled(PopupMenuEvent ev) {
			}
		});

		return popupmenu;
	}

	private void cmdOpenFile() {
		UIUtils.runCommandLater(this.getJComponent(), new CommonRunnable() {
			@Override
			public void run() throws CommonBusinessException {
				try {
					final org.nuclos.common2.File file1 = (org.nuclos.common2.File) getField().getValue();
					if (file1 != null) {
						final byte[] abContents = file1.getContents();
						if (abContents == null) {
							throw new NuclosBusinessException(CommonLocaleDelegate.getMessage("CollectableDocumentFileChooserBase.3","Die Datei ist leer."));
						}
						String sFileName = file1.getFilename();
						sFileName = sFileName.replaceAll("\\s", "");
						final java.io.File file = new java.io.File(IOUtils.getDefaultTempDir(), sFileName);
						log.debug("Schreibe Dokument in tempor\u00e4re Datei " + file.getAbsolutePath() + ".");
						IOUtils.writeToBinaryFile(file, abContents);
						log.debug("Schreiben der tempor\u00e4ren Datei war erfolgreich.");
						file.deleteOnExit();
						DesktopUtils.open(file);
					}
				}
				catch (IOException ex) {
					throw new CommonBusinessException(ex);
				}
			}
		});
	}

	private void cmdSaveAs() {
		UIUtils.runCommandLater(this.getJComponent(), new CommonRunnable() {
			@Override
			public void run() throws CommonBusinessException {
				final org.nuclos.common2.File file1 = (org.nuclos.common2.File) getField().getValue();
				if (file1 != null) {
					final byte[] abContents = file1.getContents();
					if (abContents == null) {
						throw new NuclosBusinessException(CommonLocaleDelegate.getMessage("CollectableDocumentFileChooserBase.3","Die Datei ist leer."));
					}

					final Preferences prefs = getPreferences();
					final String sLastDir = (prefs == null) ? null : prefs.node(PREFS_NODE_COLLECTABLEFILECHOOSER).get(PREFS_KEY_LAST_DIRECTORY, null);
					final JFileChooser filechooser = new JFileChooser(sLastDir);
					filechooser.setSelectedFile(new java.io.File(sLastDir, file1.getFilename()));
					final int iBtn = filechooser.showSaveDialog(getJComponent());
					if (iBtn == JFileChooser.APPROVE_OPTION) {
						final java.io.File file = filechooser.getSelectedFile();
						if (file != null) {
							if (prefs != null) {
								prefs.node(PREFS_NODE_COLLECTABLEFILECHOOSER).put(PREFS_KEY_LAST_DIRECTORY,
										filechooser.getCurrentDirectory().getAbsolutePath());
							}
							try {
								log.debug("Schreibe Dokument in Datei " + file.getAbsolutePath() + ".");
								IOUtils.writeToBinaryFile(file, abContents);
								log.debug("Schreiben der Datei war erfolgreich.");
							}
							catch (IOException ex) {
								Errors.getInstance().showExceptionDialog(getJComponent(), ex);
							}
						}
					}
				}
			}
		});
	}

	private void cmdReset() {
		UIUtils.runCommandLater(this.getJComponent(), new CommonRunnable() {
			@Override
			public void run() throws CommonBusinessException {
				clear();
			}
		});
	}

	protected void setupDragDrop() {
		DropTarget targetFileChooserControl = new DropTarget(this.getFileChooser().getFileNameComponent(), new NuclosDropTargetListener(this));
		targetFileChooserControl.setActive(true);
		DropTarget targetFileChooser = new DropTarget(this.getFileChooser(), new NuclosDropTargetListener(this));
		targetFileChooser.setActive(true);
	}

	protected void cmdBrowse() {
		final Preferences prefs = this.getPreferences();
		final String sLastDir = (prefs == null) ? null : prefs.node(PREFS_NODE_COLLECTABLEFILECHOOSER).get(PREFS_KEY_LAST_DIRECTORY, null);
		final JFileChooser filechooser = new JFileChooser(sLastDir);

		// Customer's wish (B1149) / UA
		filechooser.setFileHidingEnabled(false);

		final int iBtn = filechooser.showOpenDialog(getJComponent());
		if (iBtn == JFileChooser.APPROVE_OPTION) {
			final java.io.File file = filechooser.getSelectedFile();
			if (file != null) {
				if (prefs != null) {
					prefs.node(PREFS_NODE_COLLECTABLEFILECHOOSER).put(PREFS_KEY_LAST_DIRECTORY, filechooser.getCurrentDirectory().getAbsolutePath());
				}
				loadFile(file);
			}
		}
	}

	@Override
	protected abstract org.nuclos.common2.File newFile(String sFileName, byte[] abContents);

	//NUCLEUSINT-512
	@Override
	public void mouseClicked(MouseEvent e) {
	    if (e.getButton() == MouseEvent.BUTTON1) {
	    	if (e.getClickCount() == 2) {
	    		cmdOpenFile();
	    	}
	    }
	}

	protected void loadFile(File file) {
		if (file != null) {
			final String sFileName = file.getName();
			try {
				final byte[] abContents = IOUtils.readFromBinaryFile(file);
				final CollectableValueField clctf = new CollectableValueField(newFile(sFileName, abContents));
				this.setField(clctf);
			}
			catch (IOException ex) {
				Errors.getInstance().showExceptionDialog(this.getJComponent(), ex);
			}
		}
	}

	@Override
	public void visitDragEnter(DropTargetDragEvent dtde) {}

	@Override
	public void visitDragExit(DropTargetEvent dte) {}

	@Override
	public void visitDragOver(DropTargetDragEvent dtde) {
		Transferable trans = dtde.getTransferable();
		DataFlavor flavors[] = trans.getTransferDataFlavors();

		flavors = (flavors.length == 0) ? dtde.getCurrentDataFlavors() : flavors;

        // Select best data flavor
        DataFlavor flavor = DataFlavor.selectBestTextFlavor(flavors);

        // Flavor will be null on Windows
        // In which case use the 1st available flavor
        flavor = (flavor == null) ? flavors[0] : flavor;

        // Flavors to check
        DataFlavor Linux = null;
        try {
        	Linux = new DataFlavor("text/uri-list;class=java.io.Reader");
        }
        catch(Exception e) { }

        if(flavor.equals(Linux)) {
        	dtde.acceptDrag(dtde.getDropAction());
        }
        else {
			if(flavors != null && flavors.length > 0) {
				try {
					int index = DragAndDropUtils.getIndexOfFileList(flavors, trans);
					if(trans.getTransferData(flavors[index]) instanceof List) {
						List files = (List) trans.getTransferData(flavors[index]);
						if(files.size() == 1) {
							if(files.get(0) instanceof File) {
								File fileDrag = (File)files.get(0);
								String filename = fileDrag.getName().toUpperCase();
								if(true) {
									dtde.acceptDrag(dtde.getDropAction());
								}
							}
							else {
								dtde.rejectDrag();
							}
						}
						else {
							dtde.rejectDrag();
						}
					}
					else {
						boolean blnAcceptEmail = false;
						if(flavors != null && flavors.length > 0) {
							int count = flavors.length;
							for(int i = 0; i < count; i++) {
								try {
									Object obj = trans.getTransferData(flavors[i]);
									if(obj instanceof String) {
										String strRow = (String)obj;
										if(strRow.indexOf("Betreff") != -1) {
											blnAcceptEmail = true;
										}
									}
								}
								catch(Exception ex) {
									// do nothing here
								}
							}
						}
						if(blnAcceptEmail)
							dtde.acceptDrag(dtde.getDropAction());
						else
							dtde.rejectDrag();
					}
				}
				catch(Exception ex) {
					// do nothing here
				}
			}
        }
	}

	@Override
	public void visitDrop(DropTargetDropEvent dtde) {
		try {
			dtde.acceptDrop(dtde.getDropAction());
			Transferable trans = dtde.getTransferable();

			DataFlavor flavors[] = trans.getTransferDataFlavors();

			flavors = (flavors.length == 0) ? dtde.getCurrentDataFlavors() : flavors;

	        // Select best data flavor
	        DataFlavor flavor = DataFlavor.selectBestTextFlavor(flavors);

	        // Flavor will be null on Windows
	        // In which case use the 1st available flavor
	        flavor = (flavor == null) ? flavors[0] : flavor;

	        // Flavors to check
	        DataFlavor Linux = new DataFlavor("text/uri-list;class=java.io.Reader");


			if(flavor.equals(Linux)) {

                BufferedReader read = new BufferedReader(flavor.getReaderForText(trans));
                // Remove 'file://' from file name
                String fileName = read.readLine().substring(7).replace("%20"," ");
                // Remove 'localhost' from OS X file names
                if(fileName.substring(0,9).equals("localhost")) {
                        fileName = fileName.substring(9);
                }
                read.close();

                if(fileName != null && fileName.length() > 0) {
                	loadFile(new File(fileName));
                }

			}
			else {

				for(int i = 0; i < flavors.length; i++) {
					Object obj = trans.getTransferData(flavors[i]);
					if(obj instanceof List) {
						List files = (List) trans.getTransferData(flavors[i]);
						if(files.size() == 1) {
							if(files.get(0) instanceof File) {
								File file = (File)files.get(0);
								loadFile(file);
							}
						}
					}
					else {
						List<File> lstFile = DragAndDropUtils.mailHandling();
						if(lstFile.size() == 1) {
							loadFile(lstFile.get(0));
						}
					}
				}
			}
		}
		catch(PointerException e){
			Bubble bubble = new Bubble(CollectableDocumentFileChooserBase.this.getControlComponent(), CommonLocaleDelegate.getMessage("details.subform.controller.2", "Diese Funktion wird nur unter Microsoft Windows unterstützt!"),5, Bubble.Position.NW);
			bubble.setVisible(true);
		}
		catch (Exception e) {
			// ignore this
		}
	}

	@Override
	public void visitDropActionChanged(DropTargetDragEvent dtde) {}



}
