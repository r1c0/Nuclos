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
import java.awt.event.ActionListener;

import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import org.nuclos.client.scripting.ScriptEditor;
import org.nuclos.client.ui.labeled.ScriptComponent;
import org.nuclos.client.ui.popupmenu.JPopupMenuListener;
import org.nuclos.common.NuclosScript;
import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common.collect.collectable.CollectableField;
import org.nuclos.common.collect.collectable.CollectableValueField;
import org.nuclos.common.collect.exception.CollectableFieldFormatException;
import org.nuclos.common2.SpringLocaleDelegate;

/**
 * {@link CollectableComponent} implementation for type NuclosScript.
 * @author thomas.schiffmann
 */
public class CollectableScriptComponent extends AbstractCollectableComponent {

	private final ScriptEditor se = new ScriptEditor();

	public CollectableScriptComponent(CollectableEntityField clctef, boolean bSearchable) {
		super(clctef, new ScriptComponent(), bSearchable);

		this.getScriptComponent().setToolTipTextProviderForLabel(this);

		this.getScriptComponent().getEditButton().addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ev) {
				cmdEdit();
			}
		});
	}

	protected ScriptComponent getScriptComponent() {
		return (ScriptComponent) this.getJComponent();
	}

	private void cmdEdit() {
		if (getModel().getField() != null && getModel().getField().getValue() instanceof NuclosScript) {
			NuclosScript script = (NuclosScript) getModel().getField().getValue();
			if (script != null) {
				se.setScript(new NuclosScript(script.getLanguage(), script.getSource()));
			}
			else {
				se.setScript(null);
			}

		}
		se.run();
		NuclosScript script = se.getScript();
		if (org.nuclos.common2.StringUtils.isNullOrEmpty(script.getSource())) {
			script = null;
		}
		else {
			script = new NuclosScript(script.getLanguage(), script.getSource());
		}
		setField(new CollectableValueField(script));
	}

	@Override
	public JComponent getFocusableComponent() {
		return getScriptComponent().getScriptComponent();
	}

	@Override
	public CollectableField getFieldFromView() throws CollectableFieldFormatException {
		NuclosScript script = se.getScript();
		if (org.nuclos.common2.StringUtils.isNullOrEmpty(script.getSource())) {
			script = null;
		}
		return new CollectableValueField(script);
	}

	@Override
	protected void updateView(CollectableField clctfValue) {
		NuclosScript script = (NuclosScript) clctfValue.getValue();
		se.setScript(script);
		getScriptComponent().setPreview(script);
	}

	@Override
	public void setInsertable(boolean bInsertable) {
		// do nothing
	}

	@Override
	protected void setupJPopupMenuListener(JPopupMenuListener popupmenulistener) {
		this.getScriptComponent().getScriptComponent().addMouseListener(popupmenulistener);
		this.getScriptComponent().getEditButton().addMouseListener(popupmenulistener);
	}

	/**
	 * adds a "Clear" entry to the popup menu.
	 */
	@Override
	public JPopupMenu newJPopupMenu() {
		final JPopupMenu result = new JPopupMenu();

		final JMenuItem miClear = new JMenuItem(
				SpringLocaleDelegate.getInstance().getMessage("CollectableFileNameChooserBase.1","Zur\u00fccksetzen"));
		boolean bClearEnabled;
		try {
			bClearEnabled = this.getField().getValue() != null;
		}
		catch (CollectableFieldFormatException ex) {
			bClearEnabled = false;
		}
		miClear.setEnabled(bClearEnabled);

		miClear.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ev) {
				clear();
			}
		});
		result.add(miClear);

		return result;
	}

	@Override
	public TableCellRenderer getTableCellRenderer(boolean subform) {
		return new CollectableComponentDefaultTableCellRenderer() {
			@Override
			public Component getTableCellRendererComponent(JTable tbl, Object oValue, boolean bSelected, boolean bHasFocus, int iRow, int iColumn) {
				Component c = super.getTableCellRendererComponent(tbl, oValue, bSelected, bHasFocus, iRow, iColumn);
				return c;
			}
		};
	}
}
