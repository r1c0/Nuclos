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
import org.nuclos.common2.CommonRunnable;
import org.nuclos.common2.IOUtils;
import org.nuclos.common2.SpringLocaleDelegate;
import org.nuclos.common2.SystemUtils;
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
	
	private static final Logger LOG = Logger.getLogger(CollectableGenericObjectFileChooser.class);
	public static final String PREFS_KEY_LAST_DIRECTORY = "lastDirectory";
	public static final String PREFS_NODE_COLLECTABLEFILECHOOSER = "CollectableFileChooser";

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
		final JMenuItem miOpen = new JMenuItem(SpringLocaleDelegate.getInstance().getMessage(
				"CollectableDocumentFileChooserBase.1","\u00d6ffnen"));
		popupmenu.add(miOpen);
		miOpen.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ev) {
				cmdOpenFile();
			}
		});
		final JMenuItem miSaveAs = new JMenuItem(SpringLocaleDelegate.getInstance().getMessage(
				"CollectableDocumentFileChooserBase.2","Speichern unter..."));
		popupmenu.add(miSaveAs);
		miSaveAs.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ev) {
				cmdSaveAs();
			}
		});
		final JMenuItem miReset = new JMenuItem(SpringLocaleDelegate.getInstance().getMessage(
				"CollectableFileNameChooserBase.1","Zur\u00fccksetzen"));
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
							throw new NuclosBusinessException(SpringLocaleDelegate.getInstance().getMessage(
									"CollectableDocumentFileChooserBase.3","Die Datei ist leer."));
						}
						String sFileName = file1.getFilename();
						sFileName = sFileName.replaceAll("\\s", "");
						final java.io.File file = new java.io.File(IOUtils.getDefaultTempDir(), sFileName);
						LOG.debug("Schreibe Dokument in tempor\u00e4re Datei " + file.getAbsolutePath() + ".");
						IOUtils.writeToBinaryFile(file, abContents);
						LOG.debug("Schreiben der tempor\u00e4ren Datei war erfolgreich.");
						file.deleteOnExit();
						SystemUtils.open(file);
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
						throw new NuclosBusinessException(SpringLocaleDelegate.getInstance().getMessage(
								"CollectableDocumentFileChooserBase.3","Die Datei ist leer."));
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
								LOG.debug("Schreibe Dokument in Datei " + file.getAbsolutePath() + ".");
								IOUtils.writeToBinaryFile(file, abContents);
								LOG.debug("Schreiben der Datei war erfolgreich.");
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
		
		for (int i = 0; i < flavors.length; i++) {
			if (flavors[i].isFlavorJavaFileListType()) {
				dtde.acceptDrag(dtde.getDropAction());
				return;
			}
		}
		
		for(int i = 0; i < flavors.length; i++) {
			try {
				Object obj = trans.getTransferData(flavors[i]);
				if(obj instanceof String) {
					String strRow = (String) obj;
					if(strRow.indexOf("Betreff") != -1) {
						dtde.acceptDrag(dtde.getDropAction());
						return;
					}
				}
			}
			catch(Exception e) {
				// do nothing here
	        	LOG.warn("visitDragOver fails on Betreff: " + e);
			}
		}
		dtde.rejectDrag();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void visitDrop(DropTargetDropEvent dtde) {
		try {
			dtde.acceptDrop(dtde.getDropAction());
			Transferable trans = dtde.getTransferable();
			DataFlavor flavors[] = trans.getTransferDataFlavors();
			
			for(int i = 0; i < flavors.length; i++) {
				if (flavors[i].isFlavorJavaFileListType()) {
					dtde.acceptDrop(dtde.getDropAction());
					List<File> files = (List<File>) trans.getTransferData(flavors[i]);
					
					for (File file : files) {
						if (file != null) {
							loadFile(file);
							dtde.dropComplete(true);
							return;
						}
					}
				}
			}
			
			List<File> lstFile = DragAndDropUtils.mailHandling();
			if(lstFile.size() == 1) {
				loadFile(lstFile.get(0));
			}
		}
		catch(PointerException e){
        	LOG.warn("visitDrop fails with PointerException: " + e);
			Bubble bubble = new Bubble(CollectableDocumentFileChooserBase.this.getControlComponent(), 
					SpringLocaleDelegate.getInstance().getMessage(
							"details.subform.controller.2", "Diese Funktion wird nur unter Microsoft Windows unterstützt!"),
					5, Bubble.Position.NW);
			bubble.setVisible(true);
		}
		catch (Exception e) {
			// ignore this
        	LOG.warn("visitDrop fails: " + e);
		}
	}

	@Override
	public void visitDropActionChanged(DropTargetDragEvent dtde) {}



}
