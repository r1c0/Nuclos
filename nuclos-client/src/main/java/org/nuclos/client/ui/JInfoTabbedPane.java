// Copyright (C) 2011 Novabit Informationssysteme GmbH
//
// This file is part of Nuclos.
//
// Nuclos is free software: you can redistribute it and/or modify
// it under the terms of the GNU Affero General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// Nuclos is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU Affero General Public License for more details.
//
// You should have received a copy of the GNU Affero General Public License
// along with Nuclos. If not, see <http://www.gnu.org/licenses/>.
package org.nuclos.client.ui;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JTabbedPane;

import org.apache.commons.lang.ObjectUtils;

/**
 * JInfoTabbedPane is an extension of JTabbedPane that is able to display
 * additional (size) information on each tab.
 * <p>
 * In Nuclos it is used to display the element size of a (one and only) subform
 * embedded onto a tab (NUCLOSINT-63). LayoutMLParser now only uses
 * JInfoTabbedPane (instead of JTabbedPane).
 * </p>
 * 
 * @see SizeKnownListener
 * @author Thomas Pasch
 * @since Nuclos 3.1.00
 */
public class JInfoTabbedPane extends JTabbedPane {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Tab title text without the additional (size) information.
	 */
	private final List<String>	tabTitles		= new ArrayList<String>();

	/**
	 * The additional (size) information for each tab. If <code>null</code> and
	 * the corresponding displayTabInfo is <code>true</code> the 'size is
	 * loading' state is displayed.
	 */
	private final List<Integer>	tabInfo			= new ArrayList<Integer>();

	/**
	 * Flag indicating if the addional (size) information should be displayed
	 * for the tab. If <code>false</code> the tab is rendered like a normal
	 * JTabbedPane.
	 */
	private final List<Boolean>	displayTabInfo	= new ArrayList<Boolean>();

	public JInfoTabbedPane() {
		super();
	}

	@Override
	public void addTab(String title, Component component) {
		super.addTab(title, component);
		tabTitles.add(title);
		tabInfo.add(null);
		displayTabInfo.add(Boolean.FALSE);
	}

	@Override
	public void addTab(String title, Icon icon, Component component) {
		super.addTab(title, icon, component);
		tabTitles.add(title);
		tabInfo.add(null);
		displayTabInfo.add(Boolean.FALSE);
	}

	@Override
	public void addTab(String title, Icon icon, Component component, String tip) {
		super.addTab(title, icon, component, tip);
		tabTitles.add(title);
		tabInfo.add(null);
		displayTabInfo.add(Boolean.FALSE);
	}

	@Override
	public void setTabComponentAt(int tab, Component component) {
		if(!(component instanceof JLabel)) {
			throw new IllegalArgumentException(
				"JInfoTabbedPane.setTabComponentAt only accepts JLabel, not "
					+ component);
		}
		super.setTabComponentAt(tab, component);
		final JLabel label = (JLabel) component;
		tabTitles.set(tab, label.getText());
	}

	//

	/**
	 * Set the additional (size) information of the tab.
	 */
	public void setTabInfoAt(int tab, Integer info) {
		final Integer oldInfo = tabInfo.get(tab);
		if(!ObjectUtils.equals(info, oldInfo)) {
			tabInfo.set(tab, info);
			updateTab(tab);
		}
	}

	/**
	 * Set if the additional (size) information should be displayed on the tab.
	 */
	public void setDisplayTabInfoAt(int tab, boolean dti) {
		if(displayTabInfo.get(tab).booleanValue() != dti) {
			displayTabInfo.set(tab, dti);
			updateTabs();
		}
	}

	public boolean isDisplayTabInfoAt(int tab) {
		return displayTabInfo.get(tab);
	}

	private void updateTabs() {
		final int size = getTabCount();
		for(int i = 0; i < size; ++i) {
			updateTab(i);
		}
	}

	private void updateTab(int tab) {
		final JLabel label = (JLabel) getTabComponentAt(tab);
		final String title = tabTitles.get(tab);
		String text;
		if(displayTabInfo.get(tab)) {
			final Integer info = tabInfo.get(tab);
			if(info == null) {
				text = title + "  *";
			}
			else {
				int j = info.intValue();
				if(j < 0) {
					if(j > -10) {
						text = title + " " + j;
					}
					else {
						text = title + " -?";
					}
				}
				else if(j > 9) {
					text = title + " >9";
				}
				else {
					text = title + "  " + j;
				}
			}
		}
		else {
			text = title;
		}
		
		text = getMnemonicTextIfAny(text, tab);
		
		label.setText(text);
	}
	
	/**
	 * parse text for Mnemonic and underline it
	 * @param text
	 * @param tab
	 * @return
	 */
	private String getMnemonicTextIfAny(String text, int tab) {
		int keycode = this.getMnemonicAt(tab);
		try {
			if(keycode > 0) {
				keycode += 32;			
				byte b[] = {(byte)keycode};
				String sKey = new String(b);
				int index = text.indexOf(sKey);
				if(index >= 0) {
					String sBeforeMnemonic = text.substring(0, index);
					String sAfterMnemonic = text.substring(index+1);
					text = "<html>" + sBeforeMnemonic + "<u>" + sKey + "</u>" + sAfterMnemonic + "</html>";
				}
				else {
					text = "<html>" + text + "&nbsp;<u>" + sKey + "</u>" + "</html>";
				}
				
			}	
		}
		catch (Exception e) {
			// if any Exception return default text
		}
		return text;
	}

}
