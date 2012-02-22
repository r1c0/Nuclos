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

package org.nuclos.client.customcomp.resplan;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;

import org.apache.log4j.Logger;
import org.nuclos.client.common.security.SecurityCache;
import org.nuclos.client.customcomp.CustomComponentController;
import org.nuclos.client.main.Main;
import org.nuclos.client.main.mainframe.MainFrame;
import org.nuclos.client.resource.NuclosResourceCache;
import org.nuclos.client.ui.Errors;
import org.nuclos.client.ui.UIUtils;
import org.nuclos.common2.SpringLocaleDelegate;
import org.nuclos.server.customcomp.valueobject.CustomComponentVO;

public class ResPlanAction extends AbstractAction {
	
	private static final Logger LOG = Logger.getLogger(ResPlanAction.class);

	public ResPlanAction(CustomComponentVO componentVO) {
		putValue(Action.NAME, SpringLocaleDelegate.getInstance().getTextFallback(
				componentVO.getLabelResourceId(), componentVO.getLabelResourceId()) + "...");
		putValue(Action.SMALL_ICON, MainFrame.resizeAndCacheTabIcon(NuclosResourceCache.getNuclosResourceIcon("org.nuclos.client.resource.icon.glyphish.83-calendar.png")));
		putValue(Action.ACTION_COMMAND_KEY, componentVO.getInternalName());
		boolean enabled = false;
		// TODO: move this into the controller?
		try {
			ResPlanConfigVO configVO = ResPlanConfigVO.fromBytes(componentVO.getData());
			enabled = SecurityCache.getInstance().isReadAllowedForEntity(configVO.getResourceEntity())
				&& SecurityCache.getInstance().isReadAllowedForEntity(configVO.getEntryEntity());
		} catch (Exception e) {
			LOG.warn("ResPlanAction failed: " + e, e);
		}
		setEnabled(enabled);
	}

	@Override
	public void actionPerformed(ActionEvent evt) {
		final String internalName = evt.getActionCommand();
		UIUtils.runCommand(MainFrame.getPredefinedEntityOpenLocation(internalName), new Runnable() {
			@Override
			public void run() {
				try {
					CustomComponentController controller = CustomComponentController.newController(internalName);
					controller.run();
				} catch (Exception ex) {
					Errors.getInstance().showExceptionDialog(Main.getInstance().getMainFrame(), ex);
				}
			}
		});
	}
}
