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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableCellRenderer;

import org.nuclos.client.ui.collect.component.CollectableTextArea;
import org.nuclos.common.collect.collectable.CollectableEntityField;

/**
 * A <code>CollectableComponent</code> that presents a value in a <code>JTextArea</code>.
 * The difference to the CollectableTextArea is its behaviour as a table cell renderer.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 * @author	<a href="mailto:uwe.allner@novabit.de">uwe.allner</a>
 * @version 01.00.00
 */
public class NuclosCollectableTextArea extends CollectableTextArea {

	public NuclosCollectableTextArea(CollectableEntityField clctef, Boolean bSearchable) {
		super(clctef, bSearchable);
		overrideActionMap();
	}

	// For behaviour as table cell renderer
	@Override
	public void setEnabled(boolean bEnabled) {
		this.getJTextArea().setEditable(bEnabled);
	}

	// Override the tab key
	private void overrideActionMap() {
		JTextArea component = getJTextArea();

		// The actions
		Action nextFocusAction = new AbstractAction("insert-tab") {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
            public void actionPerformed(ActionEvent evt) {
				((Component) evt.getSource()).transferFocus();
			}
		};
		Action prevFocusAction = new AbstractAction("Move Focus Backwards") {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
            public void actionPerformed(ActionEvent evt) {
				((Component) evt.getSource()).transferFocusBackward();
			}
		};
		// Add actions
		component.getActionMap().put(nextFocusAction.getValue(Action.NAME), nextFocusAction);
		component.getKeymap().addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, KeyEvent.SHIFT_MASK), prevFocusAction);
	}

	/**
	 * This cell renderer is limited to 3 lines of text, when it is enabled.
	 * @return special cell renderer
	 */
	@Override
	public TableCellRenderer getTableCellRenderer() {
		return new TableCellRenderer() {
			@Override
            public Component getTableCellRendererComponent(JTable tbl, Object oValue, boolean bSelected, boolean bHasFocus, int iRow, int iColumn) {

				NuclosCollectableTextArea.this.setObjectValue(oValue);

				final JTextArea ta = NuclosCollectableTextArea.this.getJTextArea();
				ta.setBackground(bSelected ? tbl.getSelectionBackground() : tbl.getBackground());
				ta.setForeground(bSelected ? tbl.getSelectionForeground() : tbl.getForeground());
				ta.setCaretPosition(0);

				JScrollPane sp = (JScrollPane) NuclosCollectableTextArea.this.getControlComponent();
				sp.setBorder(new EmptyBorder(0, 0, 0, 0));

				// Calculate the correct line count (as JTextArea only counts \n characters)
				int lineCount = NuclosCollectableTextArea.this.getLineCount();
				if (lineCount > 3) {
					if (tbl.getModel().isCellEditable(iRow, iColumn)) {
						// Limit lines to 3
						lineCount = 3;
						sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
					}
				}
				else {
					sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
				}
				ta.setRows(lineCount);
				

				return NuclosCollectableTextArea.this.getControlComponent();
			}
		};
	}

	/**
	 * Calculate the real number of lines in the text area after it has wrapped the lines.
	 * The TextArea.getLineCount() just returns the number of '\n' characters in the text.
	 * Here we emulate the original word wrap algorithm of the text area.
	 *
	 * @return the line count
	 */
	private int getLineCount() {
		int result = 1;

		final JTextArea ta = this.getJTextArea();
		final String text = ta.getText();
		final Dimension d = ta.getPreferredSize();

		final Font font = ta.getFont();
		final FontRenderContext frc = new FontRenderContext(new AffineTransform(), false, false);
		Rectangle2D rect;

		// Scan the text for blanks and newlines.
		int iStart = 0;
		int iLastBlankPos = 0;
		int iTotal = 1;
		for (; iTotal < text.length(); iTotal++) {
			char ch = text.charAt(iTotal);
			switch (ch) {
				case ' ':
					// Check for each blank if the line still fits. If not, begin the next line at the last blank position and continue
					rect = font.getStringBounds(text, iStart, iTotal, frc);
					if (rect.getWidth() > d.getWidth()) {
						result++;
						iStart = iLastBlankPos;
					}
					iLastBlankPos = iTotal;
					break;
				case '\n':
					result++;
					iStart = iTotal + 1;
					break;
			}
		}

		// Examine the (possible) rest after the last blank
		if (iLastBlankPos > 0 && iLastBlankPos < text.length()) {
			rect = font.getStringBounds(text, iStart, text.length(), frc);
			if (rect.getWidth() > d.getWidth()) {
				result++;
			}
		}

		return result;
	}

}	// class NuclosCollectableTextArea
