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
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import org.apache.log4j.Logger;
import org.jdesktop.jxlayer.JXLayer;
import org.nuclos.client.ui.labeled.LabeledEditorPane;
import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common.collect.collectable.searchcondition.ComparisonOperator;
import org.nuclos.common2.SpringLocaleDelegate;

/**
 * A <code>CollectableComponent</code> that presents a value in a <code>JEditorPane</code>.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:maik.stueker@nuclos.de">Maik.Stueker</a>
 * @version	01.00.00
 */
public class CollectableEditorPane extends CollectableTextComponent {

	private static final Logger LOG = Logger.getLogger(CollectableEditorPane.class);
	
	/**
	 * @param clctef
	 * @postcondition this.isDetailsComponent()
	 */
	public CollectableEditorPane(CollectableEntityField clctef) {
		this(clctef, false);

		assert this.isDetailsComponent();
	}

	public CollectableEditorPane(CollectableEntityField clctef, boolean bSearchable) {
		super(clctef, new LabeledEditorPane(clctef.isNullable(), clctef.getJavaClass(), clctef.getFormatInput(), bSearchable), bSearchable);
		this.setFillControlHorizontally(true);
	}

	public JEditorPane getJEditorPane() {
		return (JEditorPane) this.getJTextComponent();
	}

	@Override
	public JComponent getFocusableComponent() {
		return this.getJEditorPane();
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
						LOG.error("CollectableEditorPane.setComparisionOperator: " + e, e);
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
	public TableCellRenderer getTableCellRenderer() {
		return new TableCellRenderer() {
			@Override
            public Component getTableCellRendererComponent(JTable tbl, Object oValue, boolean bSelected, boolean bHasFocus,
					int iRow, int iColumn) {
				
				Component comp = CollectableEditorPane.super.getTableCellRenderer().getTableCellRendererComponent(tbl, oValue, bSelected, bHasFocus, iRow, iColumn);
				if (comp instanceof JXLayer<?>) {
					return comp;
				}				
				
				CollectableEditorPane.this.setObjectValue(oValue);

				final JEditorPane ep = CollectableEditorPane.this.getJEditorPane();
				ep.setBackground(bSelected ? tbl.getSelectionBackground() : tbl.getBackground());
				ep.setForeground(bSelected ? tbl.getSelectionForeground() : tbl.getForeground());
				ep.setCaretPosition(0);

				return CollectableEditorPane.this.getControlComponent();
			}
		};
	}
	
	@Override
	protected boolean selectAllOnGainFocus() {
		return false;
	}

	@Override
	public JPopupMenu newJPopupMenu() {
		JPopupMenu res = super.newJPopupMenu();
		
		if (res == null) {
			res = new JPopupMenu();
		} else if (res.getComponentCount() != 0) {
			res.addSeparator();
		}
		
		JRadioButtonMenuItem miHTML = new JRadioButtonMenuItem(new AbstractAction(SpringLocaleDelegate.getInstance().getMessage(
				"CollectableEditorPane.view", "Ansicht")) {
			@Override
			public void actionPerformed(ActionEvent e) {
				String text = getJEditorPane().getText();
				getJEditorPane().setContentType("text/html");
				getJEditorPane().setText(text);
				getJEditorPane().getDocument().addDocumentListener(newDocumentListenerForTextComponentWithComparisonOperator());
				((LabeledEditorPane) getLabeledComponent()).setToolbarEnabled(true);
			}
		});
		miHTML.setSelected("text/html".equals(getJEditorPane().getContentType()));
		res.add(miHTML);
		JRadioButtonMenuItem miTXT = new JRadioButtonMenuItem(new AbstractAction("HTML") {
			@Override
			public void actionPerformed(ActionEvent e) {
				String text = getJEditorPane().getText();
				getJEditorPane().setContentType("text/plain");
				getJEditorPane().setText(text);
				getJEditorPane().getDocument().addDocumentListener(newDocumentListenerForTextComponentWithComparisonOperator());
				((LabeledEditorPane) getLabeledComponent()).setToolbarEnabled(false);
			}
		});
		miTXT.setSelected("text/plain".equals(getJEditorPane().getContentType()));
		res.add(miTXT);
		
		return res;
	}

}  // class CollectableEditorPane
