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
package org.nuclos.client.report.reportrunner;

import java.awt.Dimension;
import java.awt.Frame;

import javax.swing.JDialog;

import org.nuclos.common2.SpringLocaleDelegate;

/**
 * Dialog which shows the status of background processes.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Boris.Sander@novabit.de">Boris Sander</a>
 * @version 01.00.00
 */
public class BackgroundProcessStatusDialog extends JDialog {

	private BackgroundProcessStatusPanel pnlStatus = new BackgroundProcessStatusPanel();

	public BackgroundProcessStatusDialog(Frame owner) {
		super(owner, SpringLocaleDelegate.getInstance().getMessage(
				"BackgroundProcessStatusDialog.1","Hintergrundprozesse"), false);
		setSize(400, 200);
		//setLocation(100, 100);
		init();
		
		this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		//new Thread(new TableUpdater(this)).start();
		this.setLocationRelativeTo(owner); //center on screen for first use in this session
	}

	private void init() {
		getContentPane().add(pnlStatus);
		pack();
	}

	@Override
	public Dimension getMinimumSize() {
		return new Dimension(300, 120);
	}

	public BackgroundProcessStatusPanel getStatusPanel() {
		return pnlStatus;
	}
}
