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
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.awt.geom.AffineTransform;
import java.text.AttributedString;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.table.TableCellRenderer;

import org.nuclos.client.ui.collect.DynamicRowHeightChangeListener;
import org.nuclos.client.ui.collect.DynamicRowHeightChangeProvider;
import org.nuclos.client.ui.collect.DynamicRowHeightSupport;
import org.nuclos.client.ui.collect.component.CollectableTextArea;
import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common2.StringUtils;

/**
 * A <code>CollectableComponent</code> that presents a value in a <code>JTextArea</code>.
 * The difference to the CollectableTextArea is its behaviour as a table cell renderer.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 * @author	<a href="mailto:uwe.allner@novabit.de">uwe.allner</a>
 * @version 01.00.00
 */
public class NuclosCollectableTextArea extends CollectableTextArea implements DynamicRowHeightChangeProvider {

	private static class FocusForward extends AbstractAction {

		private FocusForward() {
			super("insert-tab");
		}

		@Override
        public void actionPerformed(ActionEvent evt) {
			((Component) evt.getSource()).transferFocus();
		}
	}

	public static class FocusBackward extends AbstractAction {

		private FocusBackward() {
			super("Move Focus Backwards");
		}

		@Override
        public void actionPerformed(ActionEvent evt) {
			((Component) evt.getSource()).transferFocusBackward();
		}
	}

	private static final FocusForward FOCUS_FORWARD = new FocusForward();

	private static final FocusBackward FOCUS_BACKWORD = new FocusBackward();

	public NuclosCollectableTextArea(CollectableEntityField clctef, Boolean bSearchable) {
		super(clctef, bSearchable);
		overrideActionMap();
		getJTextArea().addComponentListener(new ComponentListener() {

			@Override
			public void componentShown(ComponentEvent e) {
			}

			@Override
			public void componentResized(ComponentEvent e) {
				fireHeightChanged(getJTextArea().getPreferredSize().height);
			}

			@Override
			public void componentMoved(ComponentEvent e) {
			}

			@Override
			public void componentHidden(ComponentEvent e) {
			}
		});
	}

	// For behaviour as table cell renderer
	@Override
	protected void setEnabledState(boolean bEnabled) {
		this.getJTextArea().setEditable(bEnabled);
	}

	// Override the tab key
	private void overrideActionMap() {
		JTextArea component = getJTextArea();
		// Add actions
		component.getActionMap().put(FOCUS_FORWARD.getValue(Action.NAME), FOCUS_FORWARD);
		component.getKeymap().addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, KeyEvent.SHIFT_MASK), FOCUS_BACKWORD);
	}

	/**
	 * This cell renderer is limited to 3 lines of text, when it is enabled.
	 * @return special cell renderer
	 */
	@Override
	public TableCellRenderer getTableCellRenderer(boolean subform) {
		return new TextAreaCellRenderer(subform);
	}

	/**
	 *
	 *
	 */
	private class TextAreaCellRenderer implements TableCellRenderer, DynamicRowHeightSupport {

		final boolean subform;

		public TextAreaCellRenderer(boolean subform) {
			super();
			this.subform = subform;
		}

		@Override
        public Component getTableCellRendererComponent(JTable tbl, Object oValue, boolean bSelected, boolean bHasFocus, int iRow, int iColumn) {

			NuclosCollectableTextArea.this.setObjectValue(oValue);

			final JTextArea ta = NuclosCollectableTextArea.this.getJTextArea();
			ta.setCaretPosition(0);

			final JScrollPane sp = (JScrollPane) NuclosCollectableTextArea.this.getControlComponent();
			sp.setBorder(BorderFactory.createEmptyBorder());
			sp.setViewportBorder(BorderFactory.createEmptyBorder());

			// Calculate the correct line count (as JTextArea only counts \n characters)
			final int columnWidth = tbl.getColumnModel().getColumn(iColumn).getWidth()-tbl.getColumnModel().getColumnMargin();
			final int scrollBarWidth = sp.getVerticalScrollBar().getWidth();
			final boolean scrollBarVisible = sp.getVerticalScrollBar().isVisible();
			final int lineCount = NuclosCollectableTextArea.this.getLineCount(columnWidth-(scrollBarVisible?scrollBarWidth:0));

			ta.setRows(lineCount);

			setBackgroundColor(ta, tbl, oValue, bSelected, bHasFocus, iRow, iColumn);
			return NuclosCollectableTextArea.this.getControlComponent();
		}

		@Override
		public int getHeight(Component cellRendererComponent) {
			return ((JScrollPane) cellRendererComponent).getViewport().getComponent(0).getPreferredSize().height+2;
		}
	}

	/**
	 * Calculate the real number of lines in the text area after it has wrapped the lines.
	 * The TextArea.getLineCount() just returns the number of '\n' characters in the text.
	 * Here we emulate the original word wrap algorithm of the text area.
	 *
	 * @return the line count
	 */
	private int getLineCount(int iWidth) {
		int result = 0;

		final JTextArea ta = this.getJTextArea();
		final String text = ta.getText();

		if (!StringUtils.looksEmpty(text)) {
			final Font font = ta.getFont();
			final FontRenderContext frc = new FontRenderContext(new AffineTransform(), true, false);

			final LineBreakMeasurer measurer = new LineBreakMeasurer(new AttributedString(text, font.getAttributes()).getIterator(), frc);

			while (measurer.getPosition() < text.length()) {
		         measurer.nextLayout(iWidth);
		         result++;
		    }
		}

//		Rectangle2D rect;
//
//		// Scan the text for blanks and newlines.
//		int iStart = 0;
//		int iLastBlankPos = 0;
//		int iTotal = 1;
//		for (; iTotal < text.length(); iTotal++) {
//			char ch = text.charAt(iTotal);
//			switch (ch) {
//				case ' ':
//					// Check for each blank if the line still fits. If not, begin the next line at the last blank position and continue
//					rect = font.getStringBounds(text, iStart, iTotal, frc);
//					if (rect.getWidth() > iWidth) {
//						result++;
//						iStart = iLastBlankPos;
//					}
//					iLastBlankPos = iTotal;
//					break;
//				case '\n':
//					result++;
//					iStart = iTotal + 1;
//					break;
//			}
//		}
//
//		// Examine the (possible) rest after the last blank
//		if (iLastBlankPos > 0 && iLastBlankPos < text.length()) {
//			rect = font.getStringBounds(text, iStart, text.length(), frc);
//			if (rect.getWidth() > iWidth) {
//				result++;
//			}
//		}

		return Math.max(1, result);
	}

	@Override
	public void setProperty(String sName, Object oValue) {
		super.setProperty(sName, oValue);
		if ("font-family".equals(sName)) {
			Map<TextAttribute, Object> fontAttributes = new HashMap<TextAttribute, Object>(getJTextArea().getFont().getAttributes());
			fontAttributes.put(TextAttribute.FAMILY, oValue);
			final Font newFont = new Font(fontAttributes);
			getJTextArea().setFont(newFont);
		}
	}

	public void fireHeightChanged(int height) {
		for (DynamicRowHeightChangeListener drhcl : dynamicRowHeightChangeListener) {
			drhcl.heightChanged(height);
		}
	}

	@Override
	public void addDynamicRowHeightChangeListener(DynamicRowHeightChangeListener drhcl) {
		dynamicRowHeightChangeListener.add(drhcl);
	}

	@Override
	public void removeDynamicRowHeightChangeListener(DynamicRowHeightChangeListener drhcl) {
		dynamicRowHeightChangeListener.remove(drhcl);
	}

	private final Collection<DynamicRowHeightChangeListener> dynamicRowHeightChangeListener = new ArrayList<DynamicRowHeightChangeListener>();

}	// class NuclosCollectableTextArea
