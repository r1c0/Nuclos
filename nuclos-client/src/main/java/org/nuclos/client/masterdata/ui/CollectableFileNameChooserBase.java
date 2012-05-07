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
package org.nuclos.client.masterdata.ui;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.prefs.Preferences;

import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import org.nuclos.client.ui.collect.component.AbstractCollectableComponent;
import org.nuclos.client.ui.collect.component.custom.FileChooserComponent;
import org.nuclos.client.ui.popupmenu.JPopupMenuListener;
import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common.collect.collectable.CollectableField;
import org.nuclos.common.collect.collectable.CollectableValueField;
import org.nuclos.common.collect.exception.CollectableFieldFormatException;
import org.nuclos.common2.SpringLocaleDelegate;

/**
 * Base class for collectable file choosers in master data, storing file name and path instead of a file object.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 * @author	<a href="mailto:uwe.allner@novabit.de">uwe.allner</a>
 * @version 01.00.00
 */
public abstract class CollectableFileNameChooserBase extends AbstractCollectableComponent {

	private static final String PREFS_KEY_LAST_DIRECTORY = "lastDirectory";
	private static final String PREFS_NODE_COLLECTABLEFILECHOOSER = "CollectableFileChooser";

	private String sFileName;
	private Preferences prefs;

	public CollectableFileNameChooserBase(CollectableEntityField clctef, Boolean bSearchable) {
		this(clctef, bSearchable.booleanValue());
	}

	public CollectableFileNameChooserBase(CollectableEntityField clctef, boolean bSearchable) {
		super(clctef, new FileChooserComponent(), bSearchable);

		this.getFileChooser().setToolTipTextProviderForLabel(this);

		this.getFileChooser().getBrowseButton().addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ev) {
				cmdBrowse();
			}
		});
	}

	@Override
	public Preferences getPreferences() {
		return this.prefs;
	}

	@Override
	public void setPreferences(Preferences prefs) {
		this.prefs = prefs;
	}

	protected void cmdBrowse() {
		final Preferences prefs = this.getPreferences();
		final String sLastDir = (prefs == null) ? null : prefs.node(PREFS_NODE_COLLECTABLEFILECHOOSER).get(PREFS_KEY_LAST_DIRECTORY, null);
		final JFileChooser filechooser = new JFileChooser(sLastDir);

		configureFileSelection(filechooser);

		final int iBtn = filechooser.showOpenDialog(getJComponent());
		if (iBtn == JFileChooser.APPROVE_OPTION) {
			final java.io.File file = filechooser.getSelectedFile();
			if (file != null) {
				if (prefs != null) {
					prefs.node(PREFS_NODE_COLLECTABLEFILECHOOSER).put(PREFS_KEY_LAST_DIRECTORY, filechooser.getCurrentDirectory().getAbsolutePath());
				}
				final String sFileName = file.getAbsolutePath();
				final CollectableValueField clctf = new CollectableValueField(sFileName);
				this.setField(clctf);
			}
		}
	}

	protected abstract void configureFileSelection(JFileChooser filechooser);

	protected FileChooserComponent getFileChooser() {
		return (FileChooserComponent) this.getJComponent();
	}

	@Override
	public JComponent getFocusableComponent() {
		return getFileChooser().getFileNameComponent();
	}

	@Override
	public CollectableField getFieldFromView() throws CollectableFieldFormatException {
		return new CollectableValueField(this.sFileName);
	}

	@Override
	protected void updateView(CollectableField clctfValue) {
		this.sFileName = (String) clctfValue.getValue();
		this.getFileChooser().setFileName(sFileName);
		this.getFileChooser().setIcon(null);
		this.getFileChooser().setToolTipText(sFileName);
	}

	@Override
	public void setInsertable(boolean bInsertable) {
		// do nothing
	}

	@Override
	public TableCellRenderer getTableCellRenderer() {
		 /* 
		  * TODO: This REALLY should be static - but how to archive this? (tp)
		  */
		return new CollectableComponentDefaultTableCellRenderer() {
			@Override
			public Component getTableCellRendererComponent(JTable tbl, Object oValue, boolean bSelected, boolean bHasFocus, int iRow, int iColumn) {
				Component c = super.getTableCellRendererComponent(tbl, oValue, bSelected, bHasFocus, iRow, iColumn);
				getFileChooser().setToolTipText(sFileName);
				return c;
			}
		};
	}

	@Override
	protected void setupJPopupMenuListener(JPopupMenuListener popupmenulistener) {
		this.getFileChooser().getFileNameComponent().addMouseListener(popupmenulistener);
		// add a listener to the button because the textfield may not be visible for some instances:
		this.getFileChooser().getBrowseButton().addMouseListener(popupmenulistener);
	}

	/**
	 * adds a "Clear" entry to the popup menu.
	 */
	@Override
	public JPopupMenu newJPopupMenu() {
		// Note that the default entries (as specified in AbstractCollectableComponent) are ignored.
		final JPopupMenu result = new JPopupMenu();

		final JMenuItem miClear = new JMenuItem(
				SpringLocaleDelegate.getInstance().getMessage("CollectableFileNameChooserBase.1","Zur\u00fccksetzen"));
		boolean bClearEnabled;
		try {
			bClearEnabled = this.getField().getValue() != null;
		}
		catch (CollectableFieldFormatException ex) {
			bClearEnabled = false;
		}
		miClear.setEnabled(bClearEnabled);

		miClear.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ev) {
				CollectableFileNameChooserBase.this.clear();
			}
		});
		result.add(miClear);

		return result;
	}

}	// class CollectableFileNameChooserBase
