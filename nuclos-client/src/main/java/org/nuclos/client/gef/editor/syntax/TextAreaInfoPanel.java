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
package org.nuclos.client.gef.editor.syntax;

import org.nuclos.common2.CommonLocaleDelegate;

import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * A panel used as a status line for the JavaEditorPanel
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 * @author	<a href="mailto:uwe.allner@novabit.de">uwe.allner</a>
 * @version 01.00.00
 */
public class TextAreaInfoPanel extends JPanel {
	private final JLabel labelOverwrite;
	private final JLabel labelCaretPos;

	public TextAreaInfoPanel() {
		super(new FlowLayout(FlowLayout.LEFT));

		labelOverwrite = new JLabel();
		labelCaretPos = new JLabel();
		labelOverwrite.setMinimumSize(new Dimension(100, 20));
		labelCaretPos.setMinimumSize(new Dimension(50, 20));

		this.add(new JLabel(CommonLocaleDelegate.getMessage("TextAreaInfoPanel.4","\u00dcberschreibmodus:")));
		this.add(labelOverwrite);
		this.add(new JLabel(CommonLocaleDelegate.getMessage("TextAreaInfoPanel.1","Cursorposition:")));
		this.add(labelCaretPos);
	}

	public void setOverwrite(boolean bOverwrite) {
		labelOverwrite.setText(bOverwrite ? CommonLocaleDelegate.getMessage("TextAreaInfoPanel.3","\u00dcberschreiben") : CommonLocaleDelegate.getMessage("TextAreaInfoPanel.2","Einf\u00fcgen"));
	}

	public void setCaretPos(int line, int column) {
		labelCaretPos.setText(" " + line + ":" + column);
	}
}
