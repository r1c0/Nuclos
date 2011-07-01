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
package org.nuclos.client.main.mainframe;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.HeadlessException;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.nuclos.client.NuclosIcons;
import org.nuclos.client.main.Main;
import org.nuclos.client.main.mainframe.workspace.WorkspaceFrame;
import org.nuclos.client.synthetica.NuclosSyntheticaConstants;
import org.nuclos.client.ui.CommonJFrame;
import org.nuclos.common2.CommonLocaleDelegate;

public class ExternalFrame extends CommonJFrame implements WorkspaceFrame{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final int number;
	
	private final JPanel pnlDesktop = new JPanel(new BorderLayout());
	
	/**
	 * 
	 * @throws HeadlessException
	 */
	public ExternalFrame(int number) throws HeadlessException {
		super();
		this.number = number;
		addWindowFocusListener(new MainFrame.ZOrderUpdater(ExternalFrame.this));
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		JPanel contentpane = (JPanel) getContentPane();
		contentpane.setLayout(new BorderLayout());
		contentpane.setBackground(NuclosSyntheticaConstants.BACKGROUND_DARKER);
		
		pnlDesktop.setOpaque(false);
		contentpane.add(pnlDesktop, BorderLayout.CENTER);
		
		setIconImage(NuclosIcons.getInstance().getFrameIcon().getImage());
		setTitle("Nuclos " + CommonLocaleDelegate.getMessage("ExternalFrame.Title","Erweiterungsfenster {0}",number));
		MainFrame.setupLiveSearchKey(this);
	}

	/**
	 * 
	 */
	@Override
	public void dispose() {
		for (MainFrameTabbedPane tabbedPane : new ArrayList<MainFrameTabbedPane>(MainFrame.getTabbedPanes(ExternalFrame.this))) {
			MainFrame.removeTabbedPane(tabbedPane, true);
		}
		
		MainFrame.removeFrameFromContent(ExternalFrame.this);
		Main.getMainController().refreshMenus();
		
		super.dispose();
	}
	
	/**
	 * 
	 */
	@Override
	public Component getFrameContent() {
		return pnlDesktop.getComponent(0);
	}
	
	/**
	 * 
	 */
	public void clearFrameContent() {
		pnlDesktop.removeAll();
	}
	
	/**
	 * 
	 */
	@Override
	public void setFrameContent(Component comp) {
		clearFrameContent();
		pnlDesktop.add(comp, BorderLayout.CENTER);
	}
	
	/**
	 * 
	 */
	@Override
	public CommonJFrame getFrame() {
		return this;
	}

	/**
	 * 
	 * @return
	 */
	@Override
	public int getNumber() {
		return number;
	}
	
}