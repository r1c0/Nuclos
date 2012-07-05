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
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;

import javax.swing.JCheckBox;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.ToolTipManager;
import javax.swing.table.TableCellRenderer;

import org.apache.log4j.Logger;
import org.jdesktop.jxlayer.JXLayer;
import org.nuclos.client.ui.ToolTipTextProvider;
import org.nuclos.client.ui.TriStateCheckBox;
import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common.collect.collectable.CollectableField;
import org.nuclos.common.collect.collectable.CollectableValueField;
import org.nuclos.common.collect.exception.CollectableFieldFormatException;

/**
 * A <code>CollectableComponent</code> that presents a value in a <code>JCheckBox</code>.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version	01.00.00
 */
public class CollectableCheckBox extends AbstractCollectableComponent {

	private static final Logger LOG = Logger.getLogger(CollectableCheckBox.class);

	/**
	 * TriStateCheckBox with support for dynamic tooltips.
	 */
	private static class CheckBox extends TriStateCheckBox {

		private ToolTipTextProvider provider;

		private void setToolTipTextProvider(ToolTipTextProvider provider) {
			this.provider = provider;
		}

		@Override
		public String getToolTipText(MouseEvent ev) {
			return (provider != null) ? provider.getDynamicToolTipText() : super.getToolTipText(ev);
		}
	}

	private final ItemListener itemlistener = new ItemListener() {
		@Override
        public void itemStateChanged(ItemEvent ev) {
			try {
				CollectableCheckBox.this.viewToModel();
			}
			catch (CollectableFieldFormatException ex) {
				// this must never happen for a CollectableCheckBox
				assert false;
			}
		}
	};

	/**
	 * @param clctef
	 * @postcondition this.isDetailsComponent()
	 */
	public CollectableCheckBox(CollectableEntityField clctef) {
		this(clctef, false);

		assert this.isDetailsComponent();
	}

	public CollectableCheckBox(CollectableEntityField clctef, boolean bSearchable) {
		super(clctef, new CheckBox(), bSearchable);

		final CheckBox chkbx = this.getTriStateCheckBox();

		chkbx.setToolTipTextProvider(this);
		ToolTipManager.sharedInstance().registerComponent(chkbx);

		chkbx.addItemListener(this.itemlistener);
	}

	@Override
	public JCheckBox getJComponent() {
		return (JCheckBox) super.getJComponent();
	}

	public JCheckBox getJCheckBox() {
		return this.getJComponent();
	}

	private CheckBox getTriStateCheckBox() {
		return (CheckBox) super.getJComponent();
	}

	@Override
	protected void updateView(CollectableField clctfValue) {
		final Boolean bValue = (Boolean) clctfValue.getValue();
		final TriStateCheckBox chkbx = this.getTriStateCheckBox();
		if (bValue == null) {
			chkbx.setUndefined();
		}
		else {
			chkbx.setSelected(bValue.booleanValue());
		}
	}

	@Override
    public CollectableField getFieldFromView() {
		final TriStateCheckBox chkbx = this.getTriStateCheckBox();
		return chkbx.isUndefined() ? CollectableValueField.NULL : new CollectableValueField(chkbx.isSelected());
	}

	@Override
	public void setLabelText(String sLabel) {
		this.getTriStateCheckBox().setText(sLabel);
	}

	@Override
	public void setInsertable(boolean bInsertable) {
		// do nothing
	}

	@Override
	public JPopupMenu newJPopupMenu() {
		// Note that the default entries (as specified in AbstractCollectableComponent) are ignored.
		final JPopupMenu result = new JPopupMenu();

		if (CollectableCheckBox.this.isMultiEditable()) {
			result.add(CollectableCheckBox.this.newNoChangeEntry());
		}
		final JMenuItem miClear = CollectableCheckBox.this.newClearEntry();
		if (CollectableCheckBox.this.isSearchComponent()) {
			miClear.setText(getSpringLocaleDelegate().getMessage("RootNode.2", "<Alle>"));
		}
		result.add(miClear);
		return result;
	}

	@Override
	public TableCellRenderer getTableCellRenderer(boolean subform) {
		return new CheckBoxTableCellRenderer();
	}

	/** 
	 * TODO: This REALLY should be static - but how to archive this? (tp)
	 */
	protected class CheckBoxTableCellRenderer extends CollectableComponentDefaultTableCellRenderer {
		@Override
        public Component getTableCellRendererComponent(JTable tbl, Object oValue, boolean bSelected, boolean bHasFocus,
				int iRow, int iColumn) {

			Component comp = super.getTableCellRendererComponent(tbl, oValue, bSelected, bHasFocus, iRow, iColumn);
			if (comp instanceof JXLayer<?>) {
				return comp;
			}
			else if (comp instanceof TriStateCheckBox) {
				TriStateCheckBox check = (TriStateCheckBox) comp;
				check.setOpaque(true);
				check.setVerticalAlignment(SwingConstants.TOP);
				check.setHorizontalAlignment(JCheckBox.CENTER);
			}

			setBackgroundColor(comp, tbl, oValue, bSelected, bHasFocus, iRow, iColumn);
			return comp;
		}
	}

}  // class CollectableCheckBox
