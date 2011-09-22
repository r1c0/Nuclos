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
package org.nuclos.client.explorer;

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import org.nuclos.common2.ClientPreferences;
import org.nuclos.common2.KeyEnum;
import org.nuclos.common2.KeyEnum.Utils;
import org.nuclos.common2.exception.PreferencesException;
import org.nuclos.common2.Localizable;
import org.nuclos.common2.PreferencesUtils;

/**
 * Class to save and load preferences for explorer tree.
 *
 * @author thomas.schiffmann
 */
public class ExplorerSettings {

	private ObjectNodeAction objectNodeAction;
	private FolderNodeAction folderNodeAction;

	private ExplorerSettings(ObjectNodeAction objectNodeAction, FolderNodeAction folderNodeAction) {
		this.objectNodeAction = objectNodeAction;
		this.folderNodeAction = folderNodeAction;
	}

	public ObjectNodeAction getObjectNodeAction() {
		return objectNodeAction;
	}

	public void setObjectNodeAction(ObjectNodeAction objectNodeAction) {
		this.objectNodeAction = objectNodeAction;
	}

	public FolderNodeAction getFolderNodeAction() {
		return folderNodeAction;
	}

	public void setFolderNodeAction(FolderNodeAction folderNodeAction) {
		this.folderNodeAction = folderNodeAction;
	}

	public void save() throws PreferencesException {
		Preferences prefs = ClientPreferences.getUserPreferences().node("explorer/actions/default");
		if (this.objectNodeAction != null) {
			prefs.put("objectNodeAction", this.objectNodeAction.value);
		}
		else {
			prefs.remove("objectNodeAction");
		}
		if (this.objectNodeAction != null) {
			prefs.put("folderNodeAction", this.folderNodeAction.value);
		}
		else {
			prefs.remove("folderNodeAction");
		}
		try {
			prefs.flush();
		} catch (BackingStoreException e) {
			throw new PreferencesException(e);
		}
	}

	public static ExplorerSettings getInstance() {
		// default actions
		Preferences prefs = ClientPreferences.getUserPreferences().node("explorer/actions/default");
		ObjectNodeAction objectNodeAction = Utils.findEnum(ObjectNodeAction.class, prefs.get("objectNodeAction", ObjectNodeAction.SHOW_DETAILS.value));
		FolderNodeAction folderNodeAction = Utils.findEnum(FolderNodeAction.class, prefs.get("folderNodeAction", FolderNodeAction.EXPAND_SUBNODES.value));
		return new ExplorerSettings(objectNodeAction, folderNodeAction);
	}

	/**
	 * Possible command options for double click on object node.
	 * Do NOT change the values, because they are persisted in the user preferences.
	 *
	 * @author thomas.schiffmann
	 */
	public enum ObjectNodeAction implements KeyEnum<String>, Localizable {

		SHOW_DETAILS("ShowDetails", "ExplorerSettings.ObjectNodeAction.ShowDetails"),
		SHOW_LIST("ShowList", "ExplorerSettings.ObjectNodeAction.ShowList");

		private String value;
		private String resourceId;

		private ObjectNodeAction(String value, String resourceId) {
			this.value = value;
			this.resourceId = resourceId;
		}

		@Override
		public String getValue() {
			return this.value;
		}

		@Override
		public String getResourceId() {
			return this.resourceId;
		}
	}

	/**
	 * Possible command options for double click on folder node.
	 * Do NOT change the values, because they are persisted in the user preferences.
	 *
	 * @author thomas.schiffmann
	 */
	public enum FolderNodeAction implements KeyEnum<String>, Localizable {

		EXPAND_SUBNODES("ExpandSubnodes", "ExplorerSettings.FolderNodeAction.ExpandSubnodes"),
		SHOW_LIST("ShowList", "ExplorerSettings.FolderNodeAction.ShowList");

		private String value;
		private String resourceId;

		private FolderNodeAction(String value, String resourceId) {
			this.value = value;
			this.resourceId = resourceId;
		}

		@Override
		public String getValue() {
			return this.value;
		}

		@Override
		public String getResourceId() {
			return this.resourceId;
		}
	}
}
