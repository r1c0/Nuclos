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
package org.nuclos.client.layout.wysiwyg.editor.ui.panels;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JToolBar;

import org.nuclos.client.layout.wysiwyg.WYSIWYGLayoutControllingPanel;
import org.nuclos.client.layout.wysiwyg.editor.util.UndoRedoFunction;
import org.nuclos.client.ui.UIUtils;

/**
 * This Class is the Toolbar attached to the {@link WYSIWYGLayoutControllingPanel}.<br>
 * It is dynamically expandable with additional Buttons<br>
 * {@link UndoRedoFunction#getToolbarItems()}<br>
 * {@link WYSIWYGInitialFocusComponentEditor#getToolbarItems()}<br>
 * 
 * It provides a Interface {@link WYSIWYGToolbarAttachable} which must be implemented.<br>
 * 
 * <br>
 * Created by Novabit Informationssysteme GmbH <br>
 * Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 * 
 * @author <a href="mailto:hartmut.beckschulze@novabit.de">hartmut.beckschulze</a>
 * @version 01.00.00
 */
@SuppressWarnings("serial")
public class WYSIWYGEditorsToolbar extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JToolBar wysiwygToolbar = null;

	/**
	 * Default Constructor
	 */
	public WYSIWYGEditorsToolbar() {
		this.setLayout(new BorderLayout());

		wysiwygToolbar = UIUtils.createNonFloatableToolBar();
		this.add(wysiwygToolbar);
	}

	/**
	 * This Method adds items to the {@link WYSIWYGEditorsToolbar}.<br>
	 * The {@link Class} must implement {@link WYSIWYGToolbarAttachable}
	 * @param obj a Class which implements {@link WYSIWYGToolbarAttachable}
	 */
	public void addComponentToToolbar(Object obj) {
		if (obj instanceof WYSIWYGToolbarAttachable) {
			JComponent[] items = ((WYSIWYGToolbarAttachable) obj).getToolbarItems();
			
			if (items != null) {
				for (int i = 0; i < items.length; i++) {
					if (items[i] != null)
						this.wysiwygToolbar.add(items[i]);
				}
			}

			if (items != null) {
				this.wysiwygToolbar.addSeparator();
			}
			this.wysiwygToolbar.updateUI();
		}
	}

	/**
	 * Removing everything from the {@link WYSIWYGEditorsToolbar}
	 */
	public void clear() {
		this.wysiwygToolbar.removeAll();
	}
	
	/**
	 * This Class must be implemented to make a {@link Component}
	 * attachable to the {@link WYSIWYGEditorsToolbar}.<br>
	 * 
	 * <br>
	 * Created by Novabit Informationssysteme GmbH <br>
	 * Please visit <a href="http://www.novabit.de">www.novabit.de</a>
	 * 
	 * @author <a href="mailto:hartmut.beckschulze@novabit.de">hartmut.beckschulze</a>
	 * @version 01.00.00
	 */
	public interface WYSIWYGToolbarAttachable {
		public JComponent[] getToolbarItems();
	}
}
