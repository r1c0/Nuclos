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
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import org.nuclos.client.ui.labeled.ILabeledComponentSupport;
import org.nuclos.client.ui.labeled.LabeledComponent;
import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common.collect.collectable.CollectableField;
import org.nuclos.common.collect.collectable.CollectableFieldFormat;

/**
 * Default (abstract) implementation of a <code>CollectableComponent</code>,
 * consisting of a label and a second ("control") component.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version	01.00.00
 */

public abstract class LabeledCollectableComponent extends AbstractCollectableComponent {

	protected LabeledCollectableComponent(CollectableEntityField clctef, LabeledComponent labcomp, boolean bSearchable) {
		super(clctef, labcomp, bSearchable);
		final LabeledComponent lc = getLabeledComponent();
		final ILabeledComponentSupport support = lc.getLabeledComponentSupport();
		
		support.setToolTipTextProvider(this);
		support.setColorProvider(new BackgroundColorProvider());
	}

	public LabeledComponent getLabeledComponent() {
		return (LabeledComponent) getJComponent();
	}

	@Override
	public void setInsertable(boolean bInsertable) {
	}

	@Override
	public void setLabelText(String sLabel) {
		getLabeledComponent().setLabelText(sLabel);
	}

	@Override
	public void setMnemonic(char cMnemonic) {
		getLabeledComponent().setMnemonic(cMnemonic);
	}

	public JLabel getJLabel() {
		return getLabeledComponent().getJLabel();
	}

	@Override
	public JComponent getControlComponent() {
		return getLabeledComponent().getControlComponent();
	}

	@Override
	public void setFillControlHorizontally(boolean bFill) {
		getLabeledComponent().setFillControlHorizontally(bFill);
	}

	@Override
	public TableCellRenderer getTableCellRenderer() {
		final TableCellRenderer parentRenderer = super.getTableCellRenderer();
		return new TableCellRenderer() {
			@Override
			public Component getTableCellRendererComponent(JTable tbl, Object oValue, boolean bSelected, boolean bHasFocus, int iRow, int iColumn) {
				Component comp = parentRenderer.getTableCellRendererComponent(tbl, oValue, bSelected, bHasFocus, iRow, iColumn);
				if (comp instanceof JLabel) {
					JLabel lb = (JLabel) comp;
					CollectableFieldFormat format = CollectableFieldFormat.getInstance(getEntityField().getJavaClass());

					final CollectableField cf = (CollectableField) oValue;
					if (cf != null && cf.getValue() != null) {
						if (cf.getValue() instanceof List) {
							List<Object> values = (List<Object>) cf.getValue();
							StringBuilder sb = new StringBuilder();
							for (Object o : values) {
								if (o != null) {
									if (sb.length() > 0) {
										sb.append(", ");
									}
									sb.append(format.format(getEntityField().getFormatOutput(), o));
								}
							}
							lb.setText(sb.toString());
						} else {
							lb.setText(format.format(getEntityField().getFormatOutput(), cf.getValue()));
						}
					}
					else {
						lb.setText("");
					}
				}
				return comp;
			}
		};
	}
}  // class LabeledCollectableComponent
