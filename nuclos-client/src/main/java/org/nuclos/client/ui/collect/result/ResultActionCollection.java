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
package org.nuclos.client.ui.collect.result;

import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;

public class ResultActionCollection {

	private final String label;
	
	private final List<Action> actions;

	public ResultActionCollection(String label) {
		this(label, null);
	}
	
	public ResultActionCollection(String label, List<Action> actions) {
		super();
		this.label = label;
		this.actions = new ArrayList<Action>();
		if (actions != null) {
			addAllActions(actions);
		}
	}
	
	public void addAction(Action action) {
		this.actions.add(action);
	}
	
	public void addAllActions(List<Action> actions) {
		this.actions.addAll(actions);
	}
	
	public List<Action> getActions() {
		return actions;
	}

	public String getLabel() {
		return label;
	}
}
