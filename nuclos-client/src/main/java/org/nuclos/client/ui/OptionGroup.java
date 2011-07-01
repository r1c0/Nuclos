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

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.SwingConstants;

import org.apache.commons.lang.NullArgumentException;

/**
 * A group of radio buttons.
 *
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Boris.Sander@novabit.de">Boris Sander</a>
 * @version	01.00.00
 * @todo review and refactor duplicated code
 */
public class OptionGroup extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final int ALIGN_HORIZONTAL = SwingConstants.HORIZONTAL;
	public static final int ALIGN_VERTICAL = SwingConstants.VERTICAL;

	private static final int VALUE_INDEX = 0;
	private static final int LABEL_INDEX = 1;
	private static final int MNEMONIC_INDEX = 2;

	private final GridLayout layout = new GridLayout();
	private final ButtonGroup bg = new ButtonGroup();

	private List<ActionListener> lstListener = new Vector<ActionListener>();

	private ActionListener actionListener = new OptionGroupActionListener();
	
	private class OptionGroupActionListener implements ActionListener, Serializable{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {
			OptionGroup.this.fireAction(e);
		}
	}

	public OptionGroup() {
		layout.setColumns(1);
		setLayout(layout);
	}

	/**
	 * @param lstOptions
	 * @precondition lstOptions != null
	 */
	public void setOptions(List<String[]> lstOptions) {
		if (lstOptions == null) {
			throw new NullArgumentException("lstOptions");
		}
		/** @todo old buttons must be removed! */

		for (Iterator<String[]> iter = lstOptions.iterator(); iter.hasNext();) {
			final String[] sOptions = iter.next();
			final String sLabel = sOptions[LABEL_INDEX];
			final String sMnemonic = sOptions[MNEMONIC_INDEX];
			final String sValue = sOptions[VALUE_INDEX];

			addRadioButton(sLabel, sMnemonic, sValue);
		}
		this.revalidate();
	}

	/**
	 * adds an option (aka radio button) with the given properties.
	 * @param sValue
	 * @param sLabel
	 * @param sMnemonic
	 */
	public void addOption(String sValue, String sLabel, String sMnemonic) {
		this.addRadioButton(sLabel, sMnemonic, sValue);
		this.revalidate();
	}

	private void addRadioButton(String sLabel, String sMnemonic, String sValue) {
		final JRadioButton radiobtn = new JRadioButton(sLabel);
		if (sMnemonic != null && sMnemonic.length() > 0) {
			radiobtn.setMnemonic(sMnemonic.charAt(0));
		}
		radiobtn.getModel().setActionCommand(sValue);
		radiobtn.getModel().setGroup(bg);
		radiobtn.addActionListener(actionListener);
		radiobtn.setEnabled(this.isEnabled());

		bg.add(radiobtn);
		this.add(radiobtn);
	}

	/**
	 * @return the action command of the selected button or <code>null</code>, if none is selected.
	 */
	public String getValue() {
		final ButtonModel buttonmodelSelected = bg.getSelection();
		return buttonmodelSelected == null ? null : buttonmodelSelected.getActionCommand();
	}

	/**
	 * @param sValue
	 */
	public void setValue(String sValue) {
		final Enumeration<AbstractButton> enumElements = bg.getElements();
		while (enumElements.hasMoreElements()) {
			final JRadioButton radiobtn = (JRadioButton) enumElements.nextElement();
			if (sValue == null) {
				// unselect all buttons:
				radiobtn.setSelected(false);
			}
			else {
				// select matching button, if any:
				if (radiobtn.getModel().getActionCommand().equals(sValue)) {
					radiobtn.setSelected(true);
					break;
				}
			}
		}
	}

	/**
	 * @todo What does this mean? - Compare to setValue()!
	 * @param sDefaultOption
	 */
	public void setDefaultOption(String sDefaultOption) {
		final Enumeration<AbstractButton> enumeration = bg.getElements();
		while (enumeration.hasMoreElements()) {
			final JRadioButton radiobtn = (JRadioButton) enumeration.nextElement();
			if (radiobtn.getModel().getActionCommand().equals(sDefaultOption)) {
				radiobtn.getModel().setSelected(true);
				break;
			}
		}
	}

	/**
	 *
	 * @param iOrientation
	 */
	public void setOrientation(int iOrientation) {
		switch (iOrientation) {
			case ALIGN_HORIZONTAL:
				layout.setRows(1);
				layout.setColumns(0);
				break;
			case ALIGN_VERTICAL:
				layout.setRows(0);
				layout.setColumns(1);
				break;
			default:
				throw new IllegalArgumentException("iOrientation");
		}
		this.revalidate();
	}

	/**
	 *
	 * @param actionListener
	 */
	public void addActionListener(ActionListener actionListener) {
		lstListener.add(actionListener);
	}

	/**
	 *
	 * @param actionListener
	 */
	public void removeActionListener(ActionListener actionListener) {
		lstListener.remove(actionListener);
	}

	/**
	 *
	 * @param ev
	 */
	public void fireAction(ActionEvent ev) {
		for (Iterator<ActionListener> iter = lstListener.iterator(); iter.hasNext();) {
			final ActionListener al = iter.next();
			al.actionPerformed(ev);
		}
	}

	/**
	 *
	 */
	public ActionListener getActionListener() {
		return new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ev) {
				final Enumeration<AbstractButton> enumeration = bg.getElements();
				while (enumeration.hasMoreElements()) {
					final JRadioButton radiobtn = (JRadioButton) enumeration.nextElement();
					if (radiobtn.getModel().getActionCommand().equals(ev.getActionCommand())) {
						radiobtn.setSelected(true);
						break;
					}
				}
			}
		};
	}

	/**
	 * enables or disables this option group, especially the contained radio buttons.
	 * @param bEnabled
	 */
	@Override
	public void setEnabled(boolean bEnabled) {
		super.setEnabled(bEnabled);
		final Enumeration<AbstractButton> enumeration = bg.getElements();
		while (enumeration.hasMoreElements()) {
			final JRadioButton radiobtn = (JRadioButton) enumeration.nextElement();
			radiobtn.setEnabled(bEnabled);
		}
	}

	public ButtonGroup getButtonGroup() {
		return bg;
	}

}  // class OptionGroup
