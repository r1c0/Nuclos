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
package org.nuclos.client.ui;

import java.awt.Component;
import java.awt.Dimension;

import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JToolBar;

import org.nuclos.common2.StringUtils;

import static org.nuclos.client.synthetica.NuclosThemeSettings.*;

public class NuclosToolBar extends JToolBar {

	public NuclosToolBar() {
		super();
		setLayout(new WrapLayout(WrapLayout.LEFT));
		setMinimumSize(new Dimension(4, 4));
	}
	
	public NuclosToolBar(int orientation) {
		super();
		setLayout(orientation==JToolBar.HORIZONTAL? new WrapLayout(WrapLayout.LEFT) : new BoxLayout(this, BoxLayout.Y_AXIS));
		setMinimumSize(new Dimension(4, 4));
	}

	@Override
	public void addSeparator() {
		super.addSeparator(ensureSeparatorMinimumSize(null));
	}
	
	@Override
	public void addSeparator(Dimension size) {
		super.addSeparator(ensureSeparatorMinimumSize(size));
	}

	@Override
	protected JButton createActionComponent(Action a) {
		JButton result = super.createActionComponent(a);
		ensureComponentDefaultSize(result);
		return result;
	}

	@Override
	public Component add(Component comp) {
		return super.add(ensureComponentDefaultSize(comp));
	}

	@Override
	public Component add(Component comp, int index) {
		return super.add(ensureComponentDefaultSize(comp), index);
	}

	@Override
	public void add(Component comp, Object constraints) {
		super.add(ensureComponentDefaultSize(comp), constraints);
	}

	@Override
	public void add(Component comp, Object constraints, int index) {
		super.add(ensureComponentDefaultSize(comp), constraints, index);
	}
	
	@Override
	public Dimension getMinimumSize() {
		return new Dimension(super.getMinimumSize().width, super.getPreferredSize().height);
	}

	/**
	 * Buttons are scaled to big during painting when there is no preferred size set.
	 * Maybe this is a bug in the UI.
	 *  
	 * @param c
	 * @return
	 */
	private Component ensureComponentDefaultSize(Component c) {
		if (c instanceof JButton) {
			JButton btn = (JButton) c;
			if (StringUtils.looksEmpty(btn.getText())) {
				// set only if it is not a text button
				btn.setPreferredSize(TOOLBAR_BUTTON_SIZE);
			}
		}
		return c;
	}

	/**
	 * Separators are scaled to small during painting if we don't set this minimum size.
	 * Maybe this is a bug in the UI.
	 * 
	 * @param size
	 * @return
	 */
	private Dimension ensureSeparatorMinimumSize(Dimension size) {
		if (getOrientation() == HORIZONTAL) {
			if (size == null)
				return TOOLBAR_SEPARATOR_H_MINIMUM;
			else {
				return new Dimension(size.width < TOOLBAR_SEPARATOR_H_MINIMUM.width   ? TOOLBAR_SEPARATOR_H_MINIMUM.width  : size.width,
									 size.height < TOOLBAR_SEPARATOR_H_MINIMUM.height ? TOOLBAR_SEPARATOR_H_MINIMUM.height : size.height);
			}
		} else {
			if (size == null)
				return TOOLBAR_SEPARATOR_V_MINIMUM;
			else {
				return new Dimension(size.width < TOOLBAR_SEPARATOR_V_MINIMUM.width   ? TOOLBAR_SEPARATOR_V_MINIMUM.width  : size.width,
					 				 size.height < TOOLBAR_SEPARATOR_V_MINIMUM.height ? TOOLBAR_SEPARATOR_V_MINIMUM.height : size.height);
			}
		}
	}

}
