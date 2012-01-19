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
package org.nuclos.client.ui.collect;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JToolBar;

import org.nuclos.client.ui.CenteringPanel;
import org.nuclos.client.ui.PopupButton;
import org.nuclos.client.ui.StatusBarTextField;
import org.nuclos.client.ui.UIUtils;
import org.nuclos.client.ui.collect.component.model.EditModel;
import org.nuclos.common2.CommonLocaleDelegate;
import org.nuclos.common2.StringUtils;

/**
 * The common base for <code>SearchPanel</code> and <code>DetailsPanel</code>.
 * Contains an <code>EditView</code>.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:christoph.radig@novabit.de">christoph.radig</a>
 * @version 01.00.00
 * @todo extends CollectPanel.TabComponent?
 */
public abstract class SearchOrDetailsPanel extends JPanel {

	/**
	 * the toolbar.
	 */
	private final JToolBar toolBar = UIUtils.createNonFloatableToolBar();

	private int popbtnExtraIndex = -1;

	private final PopupButton popbtnExtra = new PopupButton(CommonLocaleDelegate.getMessage("PopupButton.Extras","Extras"));

	/**
	 * the center panel (between toolbar and status bar) containing the "edit component".
	 */
	private final CenteringPanel pnlCenter = new CenteringPanel(true);

	/**
	 * @see EditView
	 * @invariant editview != null
	 */
	private EditView editview;

	/**
	 * The textfield in the status bar containing a message.
	 * @todo encapsulate
	 */
	public final StatusBarTextField tfStatusBar = new StatusBarTextField(" ");

	protected SearchOrDetailsPanel(boolean bForSearch) {
		super(new BorderLayout());

		this.editview = new DefaultEditView(null, new DefaultCollectableComponentsProvider(), bForSearch, null);

		if (!bForSearch)
			showToolbar(false);
	}

	public final CenteringPanel getCenteringPanel() {
		return pnlCenter;
	}

	/**
	 * init after construct...
	 */
	protected void init() {
		setupDefaultToolBarActions(toolBar);
		setNorthComponent(toolBar);
		popbtnExtraIndex = getToolBarNextIndex();
		if (popbtnExtra.getItemCount() > 0)
			toolBar.add(popbtnExtra);
	}

	public void updatePopupExtraVisibility() {
		if (popbtnExtraIndex != -1 && toolBar.getComponentIndex(popbtnExtra) < 0) {
			toolBar.add(popbtnExtra, popbtnExtraIndex);
		}
	}

	private void setNorthComponent(JComponent comp) {
		add(comp, BorderLayout.NORTH);
	}

	protected void setCenterComponent(JComponent comp) {
		add(comp, BorderLayout.CENTER);
	}

	protected void setSouthComponent(JComponent comp) {
		add(comp, BorderLayout.SOUTH);
	}

	protected abstract void setupDefaultToolBarActions( JToolBar toolBar);

	public void showToolbar(boolean show) {
		toolBar.setVisible(show);
	}

	public void addPopupExtraSeparator() {
		updatePopupExtraVisibility();
		popbtnExtra.addSeparator();
	}

	public Component addPopupExtraComponent(Component comp) {
		updatePopupExtraVisibility();
		return popbtnExtra.add(comp);
	}

	public void removePopupExtraComponent(Component comp) {
		popbtnExtra.remove(comp);
	}

	public JMenuItem addPopupExtraMenuItem(JMenuItem mi) {
		updatePopupExtraVisibility();
		return popbtnExtra.add(mi);
	}

	public void removePopupExtrasMenuItem(JMenuItem mi) {
		popbtnExtra.remove(mi);
	}

	/**
	 *
	 * @param comp
	 * @return index of comp in toolbar
	 */
	public int addToolBarComponent(Component comp) {
		toolBar.add(comp);
		toolBar.validate();
		return toolBar.getComponentIndex(comp);
	}

	/**
	 *
	 * @param comps
	 * @return index of comp in toolbar
	 */
	public int addToolBarComponents(List<Component> comps) {
		if (comps.size() == 0)
			return -1;

		for (Component comp : comps)
			toolBar.add(comp);
		toolBar.validate();
		return toolBar.getComponentIndex(comps.get(0));
	}

	public void addToolBarComponents(List<Component> comps, int index) {
		if (comps.size() == 0)
			return;

		// add last list entry first to toolbar
		List<Component> reversedComps = new ArrayList<Component>(comps);
		Collections.reverse(reversedComps);
		for (Component comp : reversedComps)
			toolBar.add(comp, index);
		toolBar.validate();
	}

	public int getToolBarNextIndex() {
		return toolBar.getComponentCount();
	}

	public int addToolBarSeparator() {
		toolBar.addSeparator();
		return toolBar.getComponentCount()-1;
	}

	public void addToolBarComponent(Component comp, int index) {
		toolBar.add(comp, index);
		toolBar.validate();
	}

	public void addToolBarHorizontalStruct(int width) {
		toolBar.add(Box.createHorizontalStrut(width));
	}

	public void removeToolBarComponent(Component comp) {
		toolBar.remove(comp);
		toolBar.revalidate();
	}

	public void removeToolBarComponents(List<Component> comps) {
		for (Component comp : comps)
			toolBar.remove(comp);
		toolBar.revalidate();
	}

	/**
	 * @return the "edit component" containing all there is to edit (esp. the <code>CollectableComponent</code>s).
	 * @todo make private
	 */
	public JComponent getEditComponent() {
		return this.getEditView().getJComponent();
	}

	/**
	 * @postcondition result != null
	 */
	public EditView getEditView() {
		return this.editview;
	}

	/**
	 * sets the edit view. This can be done dynamically.
	 * @param editview
	 * @precondition editview != null
	 * @postcondition this.getEditView() == editview
	 */
	public void setEditView(EditView editview) {
		this.setEditComponent(editview.getJComponent());

		this.editview = editview;

		assert this.getEditView() == editview;
	}

	/**
	 * @return the model of the edit view.
	 * @see #getEditView()
	 */
	public abstract EditModel getEditModel();

	/**
	 * sets the "edit component". This can be done dynamically.
	 * @param compEdit
	 * @see #getEditComponent()
	 * @precondition compEdit != null
	 */
	private void setEditComponent(JComponent compEdit) {
		this.pnlCenter.setCenteredComponent(compEdit);
	}

	public void setStatusBarText(String sText) {
		this.tfStatusBar.setText(sText);
		this.tfStatusBar.setCaretPosition(0);
		final String sStatusMultiLine = StringUtils.splitIntoSeparateLines(sText, 100);
		final String sStatusToolTip = "<html>" + sStatusMultiLine.replaceAll("\n", "<br>") + "</html>";
		this.tfStatusBar.setToolTipText(sStatusToolTip);
	}

	public JToolBar getToolBar() {
		return toolBar;
	}
}	// class SearchOrDetailsPanel
