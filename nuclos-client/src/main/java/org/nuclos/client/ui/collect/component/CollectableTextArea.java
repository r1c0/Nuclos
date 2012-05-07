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

import java.awt.Component;

import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.TableCellRenderer;

import org.apache.log4j.Logger;
import org.jdesktop.jxlayer.JXLayer;
import org.nuclos.client.ui.labeled.LabeledTextArea;
import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common.collect.collectable.searchcondition.ComparisonOperator;

/**
 * A <code>CollectableComponent</code> that presents a value in a <code>JTextArea</code>.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version	01.00.00
 */
public class CollectableTextArea extends CollectableTextComponent {

	private static final Logger LOG = Logger.getLogger(CollectableTextArea.class);
	
	/**
	 * @param clctef
	 * @postcondition this.isDetailsComponent()
	 */
	public CollectableTextArea(CollectableEntityField clctef) {
		this(clctef, false);

		assert this.isDetailsComponent();
	}

	public CollectableTextArea(CollectableEntityField clctef, boolean bSearchable) {
		super(clctef, new LabeledTextArea(clctef.isNullable(), clctef.getJavaClass(), clctef.getFormatInput(), bSearchable), bSearchable);

//		// always enable wrapping:
//		this.getJTextArea().setLineWrap(true);
//		this.getJTextArea().setWrapStyleWord(true);

		this.setFillControlHorizontally(true);
		
		 /*getJTextArea().addKeyListener(new KeyAdapter() {
			 @Override
			public void keyPressed(KeyEvent e) {
				 if (e.getKeyCode() == KeyEvent.VK_TAB)
					 e.consume();
			            
				super.keyPressed(e);
			}
		});*/
	}

	public JTextArea getJTextArea() {
		return (JTextArea) this.getJTextComponent();
	}

	@Override
	public JComponent getFocusableComponent() {
		return this.getJTextArea();
	}

	@Override
	public void setComparisonOperator(ComparisonOperator compop) {
		super.setComparisonOperator(compop);

		if (compop.getOperandCount() < 2) {
			this.runLocked(new Runnable() {
				@Override
                public void run() {
					try {
						getJTextComponent().setText(null);
					}
					catch (Exception e) {
						LOG.error("CollectableTextArea.setComparisionOperator: " + e, e);
					}						
				}
			});
		}
	}

	@Override
	public void setInsertable(boolean bInsertable) {
		// do nothing here
//		this.getJTextArea().setEditable(bInsertable);
	}

	@Override
	public void setRows(int iRows) {
		this.getJTextArea().setRows(iRows);
	}

	@Override
	public void setColumns(int iColumns) {
		this.getJTextArea().setColumns(iColumns);
	}

	@Override
	public TableCellRenderer getTableCellRenderer(final boolean subform) {
		return new TableCellRenderer() {
			@Override
            public Component getTableCellRendererComponent(JTable tbl, Object oValue, boolean bSelected, boolean bHasFocus,
					int iRow, int iColumn) {
				
				Component comp = CollectableTextArea.super.getTableCellRenderer(subform).getTableCellRendererComponent(tbl, oValue, bSelected, bHasFocus, iRow, iColumn);
				if (comp instanceof JXLayer<?>) {
					return comp;
				}				
				
				CollectableTextArea.this.setObjectValue(oValue);

				final JTextArea ta = CollectableTextArea.this.getJTextArea();
				ta.setBackground(bSelected ? tbl.getSelectionBackground() : tbl.getBackground());
				ta.setForeground(bSelected ? tbl.getSelectionForeground() : tbl.getForeground());
				ta.setCaretPosition(0);

				return CollectableTextArea.this.getControlComponent();
			}
		};
	}
	
	@Override
	protected boolean selectAllOnGainFocus() {
		return false;
	}

}  // class CollectableTextArea
