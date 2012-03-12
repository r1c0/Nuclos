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

package org.nuclos.client.tasklist;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;

import org.apache.log4j.Logger;
import org.nuclos.client.main.Main;
import org.nuclos.client.main.mainframe.MainFrame;
import org.nuclos.client.resource.NuclosResourceCache;
import org.nuclos.client.ui.Errors;
import org.nuclos.client.ui.UIUtils;
import org.nuclos.common.tasklist.TasklistDefinition;
import org.nuclos.common2.SpringLocaleDelegate;

public class TasklistAction extends AbstractAction {
	
	private static final Logger LOG = Logger.getLogger(TasklistAction.class);
	
	private final TasklistDefinition def;

	public TasklistAction(TasklistDefinition def) {
		this.def = def;
		putValue(Action.NAME, SpringLocaleDelegate.getInstance().getTextFallback(
				def.getLabelResourceId(), def.getLabelResourceId()) + "...");
		putValue(Action.SMALL_ICON, MainFrame.resizeAndCacheTabIcon(NuclosResourceCache.getNuclosResourceIcon("org.nuclos.client.resource.icon.glyphish-blue.06-magnify.png")));
		putValue(Action.ACTION_COMMAND_KEY, def.getName());
	}

	@Override
	public void actionPerformed(ActionEvent evt) {
		UIUtils.runCommand(Main.getInstance().getMainFrame(), new Runnable() {
			@Override
			public void run() {
				try {
					Main.getInstance().getMainController().getTaskController().cmdShowTasklist(def);
				} catch (Exception ex) {
					Errors.getInstance().showExceptionDialog(Main.getInstance().getMainFrame(), ex);
				}
			}
		});
	}
}
