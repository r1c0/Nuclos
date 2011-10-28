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
package org.nuclos.client.processmonitor;

import java.awt.CardLayout;

import javax.swing.JPanel;

import org.nuclos.client.statemodel.panels.NotePropertiesPanel;

/**
 * Panel containing the properties for the various instance view elements.
 * 
 * at the moment:
 * Properties Panel for Subprocess and Object
 * Empty Panel for nothing is selected
 *
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:maik.stueker@novabit.de">Maik Stueker</a>
 * @version 01.00.00
 */
public class InstanceViewerPropertiesPanel extends JPanel {
	
	private final CardLayout cardLayout = new CardLayout();
	
	private final JPanel pnlEmpty = new JPanel();
	private final NotePropertiesPanel pnlNote = new NotePropertiesPanel();
	private final InstanceViewObjectPropertiesPanel pnlSubProcessObject = new InstanceViewObjectPropertiesPanel();
	
	public InstanceViewerPropertiesPanel() {
		super();
		init();
	}
	
	private void init() {
		this.setLayout(cardLayout);
		
		this.add(pnlNote, "Note");
		this.add(pnlEmpty, "None");
		this.add(pnlSubProcessObject, "SubProcessObject");
	}
	
	/*
	 * bring the given panel to front  
	 */
	public void setPanel(String sName) {
		cardLayout.show(this, sName);
	}
	
	public NotePropertiesPanel getNotePanel() {
		return pnlNote;
	}
	
	public InstanceViewObjectPropertiesPanel getSubProcessObjectPanel() {
		return pnlSubProcessObject;
	}
}
