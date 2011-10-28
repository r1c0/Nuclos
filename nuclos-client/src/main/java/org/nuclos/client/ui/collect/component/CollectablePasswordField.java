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

import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import org.nuclos.client.ui.CommonJPasswordField;
import org.nuclos.client.ui.labeled.LabeledPasswordField;
import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common.collect.collectable.searchcondition.ComparisonOperator;

/**
 * NUCLEUSINT-1142
 * @author hartmut.beckschulze
 * @version 01.00.00
 */
public class CollectablePasswordField extends CollectableTextComponent {
	
	// @SuppressWarnings("unused")
	private CollectableEntityField clctef;
	
	/**
	 * @param clctef
	 * @postcondition this.isDetailsComponent()
	 */
	public CollectablePasswordField(CollectableEntityField clctef) {
		this(clctef, false);
		assert this.isDetailsComponent();
	}

	public CollectablePasswordField(CollectableEntityField clctef, boolean bSearchable) {
		super(clctef, new LabeledPasswordField(clctef.isNullable(), clctef.getJavaClass(), clctef.getFormatInput(), bSearchable), bSearchable);
		this.clctef = clctef;
	}

	// @todo return JTextField
	public CommonJPasswordField getJTextField() {
		return (CommonJPasswordField) this.getJTextComponent();
	}

	@Override
	public void setColumns(int iColumns) {
		this.getJTextField().setColumns(iColumns);
	}

	@Override
	protected ComparisonOperator[] getSupportedComparisonOperators() {
		return null;
	}


	@Override
	public void setComparisonOperator(ComparisonOperator compop) {
		super.setComparisonOperator(compop);

		if (compop.getOperandCount() < 2) {
			this.runLocked(new Runnable() {
				@Override
				public void run() {
					getJTextComponent().setText(null);
				}
			});
		}
	}

	@Override
	public TableCellRenderer getTableCellRenderer() {
		final TableCellRenderer parentRenderer = CollectablePasswordField.super.getTableCellRenderer();
		return new TableCellRenderer() {
			@Override
			public Component getTableCellRendererComponent(JTable tbl, Object oValue, boolean bSelected, boolean bHasFocus, int iRow, int iColumn) {
				Component comp = parentRenderer.getTableCellRendererComponent(tbl, oValue, bSelected, bHasFocus, iRow, iColumn);
				return comp;
			}
		};
	}

}	// class CollectableTextField
