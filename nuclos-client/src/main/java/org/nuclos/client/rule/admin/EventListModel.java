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
package org.nuclos.client.rule.admin;

import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractListModel;

import org.nuclos.server.ruleengine.valueobject.RuleEventUsageVO;

/**
 * List model for rule events.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Boris.Sander@novabit.de">Boris Sander</a>
 * @version 01.00.00
 */
public class EventListModel extends AbstractListModel {

	private final List<RuleEventUsageVO> lst = new ArrayList<RuleEventUsageVO>();

	public void add(RuleEventUsageVO ruleeventusagevo) {
		lst.add(ruleeventusagevo);
	}

	@Override
	public int getSize() {
		return lst.size();
	}

	@Override
	public Object getElementAt(int index) {
		return lst.get(index).getEvent();
	}
}
