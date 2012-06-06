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
package org.nuclos.client.ui.labeled;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstraints;

import java.awt.Color;
import java.awt.event.MouseEvent;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.ToolTipManager;
import javax.swing.border.EmptyBorder;

import org.nuclos.client.ui.Icons;
import org.nuclos.client.ui.TextFieldWithButton;
import org.nuclos.client.ui.ToolTipTextProvider;
import org.nuclos.client.ui.collect.component.CollectableScriptComponent;
import org.nuclos.common.NuclosScript;
import org.nuclos.common2.SpringLocaleDelegate;

/**
 * GUI component for {@link CollectableScriptComponent}.
 * @author thomas.schiffmann
 */
public class ScriptComponent extends JPanel {

	private final JLabel icon = new JLabel(Icons.getInstance().getIconTextFieldButtonScript());

	private final LabeledComponentSupport support = new LabeledComponentSupport();

	private final TextFieldWithButton scriptComponent = new TextFieldWithButton(
			Icons.getInstance().getIconTextFieldButtonFile(), support) {

		@Override
		public String getToolTipText(MouseEvent ev) {
			final ToolTipTextProvider provider = tooltiptextprovider;
			return (provider != null) ? provider.getDynamicToolTipText() : super.getToolTipText(ev);
		}

		@Override
		public boolean isButtonEnabled() {
			return btnEdit.isEnabled();
		}

		@Override
		public void buttonClicked(MouseEvent me) {
			btnEdit.doClick();
		}

		@Override
		protected boolean fadeLeft() {
			return false;
		}
	};

	private final JButton btnEdit = new JButton("...");

	private ToolTipTextProvider tooltiptextprovider;

	private static final double[] COLS = new double[] {TableLayout.PREFERRED, TableLayout.FILL};
	private static final double[] ROWS = new double[] {TableLayout.PREFERRED};
	private static final TableLayoutConstraints TLC_ICON = new TableLayoutConstraints(0, 0);
	private static final TableLayoutConstraints TLC_NAME = new TableLayoutConstraints(1, 0);

	public ScriptComponent() {
		super(new TableLayout(COLS, ROWS));
		this.setOpaque(true);
		this.init();
	}

	private void init() {
		this.add(icon, TLC_ICON);
		this.add(scriptComponent, TLC_NAME);
		this.btnEdit.setToolTipText(SpringLocaleDelegate.getInstance().getMessage(
				"file.chooser.component.tooltip", "Datei ausw\u00e4hlen"));

		this.icon.setOpaque(false);

		this.scriptComponent.setBorder(new EmptyBorder(0,2,0,2));
		this.scriptComponent.setEditable(false);
	}

	public void setPreview(NuclosScript script) {
		if (script != null) {
			this.scriptComponent.setText(script.getSource());
		}
		else {
			this.scriptComponent.setText("");
		}
	}

	@Override
	public void setToolTipText(String sToolTip) {
		this.scriptComponent.setToolTipText(sToolTip);
	}

	public void setToolTipTextProviderForLabel(ToolTipTextProvider tooltiptextprovider) {
		this.tooltiptextprovider = tooltiptextprovider;
		if(tooltiptextprovider != null) {
			ToolTipManager.sharedInstance().registerComponent(this.scriptComponent);
		}
	}

	public JComponent getScriptComponent() {
		return this.scriptComponent;
	}

	public JButton getEditButton() {
		return this.btnEdit;
	}

	@Override
	public boolean requestFocusInWindow() {
		return scriptComponent.requestFocusInWindow();
	}

	@Override
	public void setBackground(Color bg) {
		super.setBackground(bg);
		if(icon != null) {
			icon.setBackground(bg);
			scriptComponent.setBackground(bg);
			btnEdit.setBackground(bg);
		}
	}

	@Override
	public void setForeground(Color fg) {
		super.setForeground(fg);
		if(icon != null) {
			icon.setForeground(fg);
			scriptComponent.setForeground(fg);
			btnEdit.setForeground(fg);
		}
	}
}
