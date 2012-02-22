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
package org.nuclos.client.explorer.node.rule;

import org.nuclos.common2.SpringLocaleDelegate;


public class RuleTreeModel {

	public static final String SAVE_EVENT_NAME = "Save";
	public static final String USER_EVENT_NAME = "User";
	public static final String DELETE_EVENT_NAME = "Delete";
	public static final String FRIST_EVENT_NAME = SpringLocaleDelegate.getInstance().getMessage("RuleTreeModel.3","Frist");
	public static final String ALL_RULES_NODE_LABEL = SpringLocaleDelegate.getInstance().getMessage("RuleTreeModel.1","Alle Regeln");
	public static final String FRIST_NODE_LABEL = SpringLocaleDelegate.getInstance().getMessage("RuleTreeModel.4","Fristen");
	public static final String LIBRARY_LABEL = SpringLocaleDelegate.getInstance().getText("treenode.rules.library.label");

	public RuleTreeModel() {
		super();
	}
}
