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
package org.nuclos.client.ui;

import java.awt.event.KeyEvent;

import javax.swing.Icon;
import javax.swing.KeyStroke;
import javax.swing.text.DefaultEditorKit;

import org.nuclos.common2.CommonLocaleDelegate;

/**
 * <br>Clipboard utility methods.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version	01.00.00
 */

public class ClipboardUtils {
	/**
	 * icon "cut" 16x16 (from Java Look&Feel Graphics Repository)
	 */
	private static final Icon iconCut16 = Icons.getInstance().getIconCut16();

	/**
	 * icon "copy" 16x16 (from Java Look&Feel Graphics Repository)
	 */
	private static final Icon iconCopy16 = Icons.getInstance().getIconCopy16();

	/**
	 * icon "paste" 16x16 (from Java Look&Feel Graphics Repository)
	 */
	private static final Icon iconPaste16 = Icons.getInstance().getIconPaste16();

	/**
	 * inner class CutAction
	 */
	public static class CutAction extends DefaultEditorKit.CutAction {

		public CutAction() {
			this.putValue(CutAction.SMALL_ICON, iconCut16);
			this.putValue(SHORT_DESCRIPTION, CommonLocaleDelegate.getMessage("ClipboardUtils.Cut", "Ausschneiden"));
			this.putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_X, KeyEvent.CTRL_DOWN_MASK));
		}
	}

	/**
	 * inner class CopyAction
	 */
	public static class CopyAction extends DefaultEditorKit.CopyAction {

		public CopyAction() {
			this.putValue(CopyAction.SMALL_ICON, iconCopy16);
			this.putValue(SHORT_DESCRIPTION, CommonLocaleDelegate.getMessage("ClipboardUtils.Copy", "Kopieren"));
			this.putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.CTRL_DOWN_MASK));
		}
	}

	/**
	 * inner class PasteAction
	 */
	public static class PasteAction extends DefaultEditorKit.PasteAction {

		public PasteAction() {
			this.putValue(PasteAction.SMALL_ICON, iconPaste16);
			this.putValue(SHORT_DESCRIPTION, CommonLocaleDelegate.getMessage("ClipboardUtils.Paste", "Einf\u00fcgen"));
			this.putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_V, KeyEvent.CTRL_DOWN_MASK));
		}
	}

	protected ClipboardUtils() {
	}

}  // class ClipboardUtils
