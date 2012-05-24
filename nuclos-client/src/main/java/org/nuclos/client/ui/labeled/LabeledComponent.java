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

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseListener;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.ToolTipManager;

import org.nuclos.client.ui.ToolTipTextProvider;
import org.nuclos.common2.LangUtils;
import org.nuclos.common2.StringUtils;

/**
 * A component that consists of a label and a second ("control") component.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version	01.00.00
 */

public abstract class LabeledComponent extends JPanel {

	/**
	 * margin above and below the control component.
	 * @todo move this margin 2 to CollectableComponent. Should be 0 here.
	 */
	private static final int VERTICAL_MARGIN = 2;

	private static final GridBagConstraints gbcControlFill = new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0,
			GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, new Insets(VERTICAL_MARGIN, 0, VERTICAL_MARGIN, 0), 0, 0);

	private static final GridBagConstraints gbcControlNoFill = new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0,
			GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(VERTICAL_MARGIN, 0, VERTICAL_MARGIN, 0), 0, 0);

	private static final GridBagConstraints gbcLabel = new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
			new Insets(VERTICAL_MARGIN, 0, VERTICAL_MARGIN, 5), 0, 0);

	private final JLabel lab = new JLabel();

	private final JPanel pnlControl = new JPanel(new BorderLayout());
	
	protected final LabeledComponentSupport support;

	protected LabeledComponent() {
		this(new LabeledComponentSupport());
	}
	
	protected LabeledComponent(LabeledComponentSupport support) {
		super(new GridBagLayout());
		if (support == null) {
			throw new NullPointerException();
		}
		
		this.support = support;
		this.pnlControl.setOpaque(false);
		this.setOpaque(false);		
	}
	
	public ILabeledComponentSupport getLabeledComponentSupport() {
		return support;
	}

	public JLabel getJLabel() {
		return this.lab;
	}
	
	@Override
	public void requestFocus() {
		this.getControlComponent().requestFocus();
	}
	
	public void addMouseListenerToHiddenComponents(MouseListener l) {
	}
	
	public void removeMouseListenerFromHiddenComponents(MouseListener l) {
	}

	protected final void addControl(JComponent comp) {
		this.pnlControl.add(comp, BorderLayout.CENTER);
		this.add(pnlControl, this.getGridBagConstraintsForControl(false));
		this.getJLabel().setLabelFor(comp);
	}
	
	protected final void replaceControl(JComponent oldComp, JComponent newComp) {
		this.pnlControl.remove(oldComp);
		this.pnlControl.add(newComp, BorderLayout.CENTER);
	}

	/**
	 * sets the label's text
	 * @param sText
	 */
	public void setLabelText(String sText) {
		this.lab.setText(sText);
		this.remove(lab);
		if (!StringUtils.isNullOrEmpty(sText)) {
			this.add(this.lab, getGridBagConstraintsForLabel());
		}
	}

	/**
	 * enables/disables the control component, as in <code>getControlComponent()</code>
	 * @param bEnabled
	 */
	protected void setControlsEnabled(boolean bEnabled) {
		this.getControlComponent().setEnabled(bEnabled);
	}

	/**
	 * makes editable or not the controls contained in this component
	 * @param bEditable
	 */
	protected void setControlsEditable(boolean bEditable) {
		// do nothing here
	}

	/**
	 * @return the control component, that is the component that displays and lets the user edit
	 * the value
	 */
	public abstract JComponent getControlComponent();

	/**
	 * enables this component, that is makes it accessible (or not)
	 * @param bEnabled
	 */
	@Override
	public void setEnabled(boolean bEnabled) {
		super.setEnabled(bEnabled);
		this.setControlsEnabled(bEnabled);
	}

	/**
	 * makes the component editable (or not). Text fields are editable by default. Comboboxes may be
	 * or not be editable. Note that "editable" has slightly different semantics in text components
	 * and comboboxes.
	 * @param bEditable
	 */
	public final void setEditable(boolean bEditable) {
		this.setControlsEditable(bEditable);
	}

	/**
	 * sets the mnemonic that will be displayed in the label and will be associated
	 * with the control component.
	 * @param c
	 */
	public void setMnemonic(char c) {
		this.lab.setDisplayedMnemonic(c);
	}

	/**
	 * sets the static tooltip text for the label and the control component (not for the panel itself).
	 * The static tooltip is shown in the control component only if no tooltiptextprovider was set for the control.
	 * @param sToolTipText
	 * @postcondition LangUtils.equals(this.getToolTipText(), sToolTipText)
	 * @see #setToolTipTextProviderForControl(ToolTipTextProvider)
	 */
	@Override
	public void setToolTipText(String sToolTipText) {
		this.getJLabel().setToolTipText(sToolTipText);
		this.getControlComponent().setToolTipText(sToolTipText);

		assert LangUtils.equals(this.getToolTipText(), sToolTipText);
	}

	/**
	 * @return the static tooltip text of this component (which is the tooltip text of the label).
	 */
	@Override
	public String getToolTipText() {
		return this.getJLabel().getToolTipText();
	}

	/**
	 * sets the tooltip text provider for the control's (dynamic) tooltip and registers the control component with the
	 * tooltip manager to enable tooltips.
	 * @param tooltiptextprovider May be <code>null</code> to enable the static tooltip text.
	 */
	protected void setToolTipTextProviderForControl(ToolTipTextProvider tooltiptextprovider) {
		support.setToolTipTextProvider(tooltiptextprovider);
		if (tooltiptextprovider != null) {
			ToolTipManager.sharedInstance().registerComponent(this.getControlComponent());
		}
	}

	/**
	 * sets the number of columns for this component, if applicable.
	 * @param iColumns
	 */
	public void setColumns(int iColumns) {
		// do nothing here
	}

	/**
	 * sets the number of rows for this component, if reasonable.
	 * Note that this doesn't apply to comboboxes.
	 * @param iRows
	 */
	public void setRows(int iRows) {
		// do nothing here
	}

	protected GridBagConstraints getGridBagConstraintsForControl(boolean bFill) {
		return bFill ? gbcControlFill : gbcControlNoFill;
	}

	protected GridBagConstraints getGridBagConstraintsForLabel() {
		return gbcLabel;
	}

	public void setFillControlHorizontally(boolean bFill) {
		this.remove(pnlControl);
		this.add(pnlControl, this.getGridBagConstraintsForControl(bFill));
	}
	
	@Override
	public boolean isOpaque() {
		// always false
		return false;
	}

	@Override
	public void setOpaque(boolean isOpaque) {
		// ignore
	}

}  // class LabeledComponent
