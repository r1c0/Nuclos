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
package org.nuclos.client.ui.collect.component;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.table.TableCellRenderer;
import javax.swing.text.JTextComponent;

import org.apache.log4j.Logger;
import org.nuclos.client.ui.HyperlinkTextFieldWithButton;
import org.nuclos.client.ui.labeled.LabeledComponentSupport;
import org.nuclos.client.ui.labeled.LabeledHyperlink;
import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common.collect.collectable.CollectableField;
import org.nuclos.common.collect.collectable.CollectableUtils;
import org.nuclos.common.collect.collectable.searchcondition.ComparisonOperator;
import org.nuclos.common.collect.exception.CollectableFieldFormatException;
import org.nuclos.common2.StringUtils;

/**
 * <code>CollectableComponent</code> to display/enter a hyperlink.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:maik.stueker@nuclos.de">Maik Stueker</a>
 * @version	01.00.00
 */
public class CollectableHyperlink extends CollectableTextComponent {

	private static final Logger LOG = Logger.getLogger(CollectableHyperlink.class);

	/**
	 * @param clctef
	 * @postcondition this.isDetailsComponent()
	 */
	public CollectableHyperlink(CollectableEntityField clctef) {
		this(clctef, false);
		this.overrideActionMap();

		assert this.isDetailsComponent();
	}

	/**
	 * @param clctef
	 * @param bSearchable
	 */
	public CollectableHyperlink(CollectableEntityField clctef, boolean bSearchable) {
		this(clctef, bSearchable,
				new LabeledHyperlink(new LabeledComponentSupport(),
						clctef.isNullable(), bSearchable));
	}

	protected CollectableHyperlink(CollectableEntityField clctef, boolean bSearchable, LabeledHyperlink labHyperlink) {
		super(clctef, labHyperlink, bSearchable);
	}


	// Override the tab key
		protected void overrideActionMap() {
			JTextComponent component = getJTextComponent();

			// The actions
			Action nextFocusAction = new AbstractAction("insert-tab") {

				@Override
	            public void actionPerformed(ActionEvent evt) {
					((Component) evt.getSource()).transferFocus();
				}
			};
			Action prevFocusAction = new AbstractAction("Move Focus Backwards") {

				@Override
	            public void actionPerformed(ActionEvent evt) {
					((Component) evt.getSource()).transferFocusBackward();
				}
			};
			// Add actions
			component.getActionMap().put(nextFocusAction.getValue(Action.NAME), nextFocusAction);
			component.getKeymap().addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, KeyEvent.SHIFT_MASK), prevFocusAction);
		}

	public HyperlinkTextFieldWithButton getHyperlink() {
		return ((LabeledHyperlink) getJComponent()).getHyperlink();
	}

	@Override
	public JComponent getFocusableComponent() {
		return getHyperlink();
	}

	@Override
	public void setColumns(int iColumns) {
		this.getHyperlink().setColumns(iColumns);
	}

	@Override
	public void setComparisonOperator(ComparisonOperator compop) {
		super.setComparisonOperator(compop);
	}

	@Override
	public CollectableField getFieldFromView() throws CollectableFieldFormatException {
		return CollectableUtils.newCollectableFieldForValue(this.getEntityField(), this.getHyperlink().getText());
	}

	@Override
	protected void updateView(CollectableField clctfValue) {
		this.getHyperlink().setText((String) clctfValue.getValue());

		this.adjustAppearance();
	}

	@Override
	protected void adjustBackground() {
//		this.getDateChooser().getJTextField().setBackground(this.getBackgroundColor());
	}

	@Override
	protected void setEnabledState(boolean flag) {
		this.getHyperlink().setEditable(flag);
		this.getHyperlink().setButtonEnabled(flag);
	}

	@Override
	public TableCellRenderer getTableCellRenderer(boolean subform) {
		final TableCellRenderer parentRenderer = super.getTableCellRenderer(subform);
		if (subform) {
			return new HyperlinkCellRenderer(parentRenderer);
		} else {
			return parentRenderer;
		}
	}

	private class HyperlinkCellRenderer implements TableCellRenderer, TableCellCursor {

		private final Cursor curHand = new Cursor(Cursor.HAND_CURSOR);

		private final TableCellRenderer parentRenderer;

		private final TableCellRendererPanel cellPanel = new TableCellRendererPanel();

		public HyperlinkCellRenderer(TableCellRenderer parentRenderer) {
			this.parentRenderer = parentRenderer;
		}

		@Override
		public Component getTableCellRendererComponent(JTable tbl, Object oValue, boolean bSelected, boolean bHasFocus, int iRow, int iColumn) {
			Component comp = parentRenderer.getTableCellRendererComponent(tbl, oValue, bSelected, bHasFocus, iRow, iColumn);
			if (comp instanceof JLabel) {
				final JLabel lb = (JLabel) comp;
				lb.setFont(getHyperlink().getFont());
				final CollectableField cf = (CollectableField) oValue;
				if (cf == null) {
					lb.setText("");
				} else {
					lb.setText((String) cf.getValue());
				}
			}
			cellPanel.removeAll();
			cellPanel.add(comp, BorderLayout.CENTER);
			return cellPanel;
		}

		@Override
		public Cursor getCursor(Object cellValue, int cellWidth, int x) {
			if (cellValue != null) {
				if (cellValue instanceof CollectableField) {
					final Object cellValueObject = ((CollectableField)cellValue).getValue();
					if (cellValueObject != null && cellValueObject instanceof String) {
						if (!StringUtils.looksEmpty((String) cellValueObject)) {
							final Rectangle r = HyperlinkTextFieldWithButton.getIconRectangle(new Dimension(cellWidth, getHyperlink().getButtonIcon().getIconHeight()));
							if (!r.contains(x, getHyperlink().getButtonIcon().getIconHeight()/2)) {
								return curHand;
							}
						}
					}
				}
			}
			return null;
		}

	}

	private class TableCellRendererPanel extends JPanel {

		private Icon iconButton;

		public TableCellRendererPanel() {
			super(new BorderLayout());
			iconButton = getHyperlink().getButtonIcon();
		}

		@Override
		public void paint(Graphics g) {
			super.paint(g);

			Graphics2D g2d = (Graphics2D) g;

			final Rectangle r = HyperlinkTextFieldWithButton.getIconRectangle(this.getSize());
			r.y = 0;

			int w = iconButton.getIconWidth();
			int h = iconButton.getIconHeight();
			BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
			Graphics imgg = bi.getGraphics();
			iconButton.paintIcon(this, imgg, 0, 0);
			imgg.dispose();

			float[] scales = { 1f, 1f, 1f, 0.5f };
			float[] offsets = new float[4];
			RescaleOp rop = new RescaleOp(scales, offsets, null);
			g2d.drawImage(bi, rop, r.x, r.y);
		}

	}

}  // class CollectableHyperlink
