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
package org.nuclos.client.timelimit;

import org.nuclos.client.ui.collect.model.SortableCollectableTableModelImpl;
import org.nuclos.server.common.valueobject.TimelimitTaskVO;

/**
 * <code>TableModel</code> for time limit task view.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Uwe.Allner@novabit.de">Uwe.Allner</a>
 * @version 01.00.00
 */

public class TimelimitTaskTableModel extends SortableCollectableTableModelImpl<CollectableTimelimitTask> {

	public TimelimitTaskTableModel() {
		super(null);
	}
	
	public TimelimitTaskVO getTimelimitTask(int iSelectedRow) {
		return this.getCollectable(iSelectedRow).getTimelimitTaskVO();
	}

}	// class PersonalTaskTableModel
