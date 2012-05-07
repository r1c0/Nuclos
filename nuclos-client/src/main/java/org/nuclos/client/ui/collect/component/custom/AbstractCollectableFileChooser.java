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
package org.nuclos.client.ui.collect.component.custom;

import java.awt.Component;
import java.util.prefs.Preferences;

import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import org.nuclos.client.ui.FileIcons;
import org.nuclos.client.ui.collect.component.AbstractCollectableComponent;
import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common.collect.collectable.CollectableField;
import org.nuclos.common.collect.collectable.CollectableValueField;
import org.nuclos.common.collect.exception.CollectableFieldFormatException;
import org.nuclos.common2.File;
import org.nuclos.common2.exception.CommonFatalException;

/**
 * A <code>FileChooserComponent</code> as a <code>CollectableComponent</code>.
 * The mechanism of selecting/loading/storing must be implemented in a concrete subclass.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version	01.00.00
 */
public abstract class AbstractCollectableFileChooser extends AbstractCollectableComponent {

	private Preferences prefs;
	private File file;

	protected AbstractCollectableFileChooser(CollectableEntityField clctef, boolean bSearchable) {
		super(clctef, new FileChooserComponent(), bSearchable);
		if (!org.nuclos.common2.File.class.isAssignableFrom(clctef.getJavaClass())) {
			throw new CommonFatalException("collectable.file.chooser.exception");//"CollectableFileChooser ben\u00f6tigt ein org.nuclos.common2.File-Objekt.");
		}

		this.getFileChooser().setToolTipTextProviderForLabel(this);
	}

	@Override
	public JComponent getFocusableComponent() {
		return getFileChooser().getFileNameComponent();
	}

	protected FileChooserComponent getFileChooser() {
		return (FileChooserComponent) this.getJComponent();
	}

	protected abstract org.nuclos.common2.File newFile(String sFileName, byte[] abContents);

	@Override
	public CollectableField getFieldFromView() throws CollectableFieldFormatException {
		return new CollectableValueField(this.file);
	}

	@Override
	protected void updateView(CollectableField clctfValue) {
		this.setFile((File) clctfValue.getValue());
	}

	private void setFile(org.nuclos.common2.File file) {
		this.file = file;
		if (file == null) {
			this.getFileChooser().setFileName(null);
			this.getFileChooser().setIcon(null);
		}
		else {
			this.getFileChooser().setFileName(file.getFilename());
			this.getFileChooser().setIcon(FileIcons.getIcon(file.getFiletype()));
			this.getFileChooser().setToolTipText(
					getSpringLocaleDelegate().getMessage(
							"collectable.file.chooser.tooltip", "Sie k\u00f6nnen die Datei \u00fcber das Kontextmen\u00fc \u00f6ffnen."));
		}
	}

	@Override
	public void setInsertable(boolean bInsertable) {
		// do nothing
	}

	@Override
	public Preferences getPreferences() {
		return this.prefs;
	}

	@Override
	public void setPreferences(Preferences prefs) {
		this.prefs = prefs;
	}

	/**
	 * @return a <code>CollectableComponentDefaultTableCellRenderer</code> even for non-searchable components.
	 */
	@Override
	public TableCellRenderer getTableCellRenderer() {
		return new AttachmentTableCellRenderer();
	}

	/** 
	 * TODO: This REALLY should be static - but how to archive this? (tp)
	 */
	protected class AttachmentTableCellRenderer extends CollectableComponentDefaultTableCellRenderer {
		@Override
        public Component getTableCellRendererComponent(JTable tbl, Object oValue, boolean bSelected, boolean bHasFocus, int iRow, int iColumn) {
			Component comp = super.getTableCellRendererComponent(tbl, oValue, bSelected, bHasFocus, iRow, iColumn);
			return comp;
		}
	}
}  // class AbstractCollectableFileChooser
