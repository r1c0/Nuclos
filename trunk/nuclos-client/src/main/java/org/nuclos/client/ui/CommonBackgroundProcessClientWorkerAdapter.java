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
package org.nuclos.client.ui;

import org.nuclos.common.collect.collectable.Collectable;
import org.nuclos.client.report.reportrunner.BackgroundProcessTableEntry;
import org.nuclos.client.ui.collect.CollectController;

/**
 * Extension of CommonClientWorkerAdapter interface in order to view an adapter as a background process
 *
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:rostislav.maksymovskyi@novabit.de">rostislav.maksymovskyi</a>
 * @version 01.00.00
 */
public abstract class CommonBackgroundProcessClientWorkerAdapter<T extends Collectable> extends CommonClientWorkerAdapter<T> {

	public CommonBackgroundProcessClientWorkerAdapter(CollectController<T> ctl) {
		super(ctl);
	}
	
	public abstract void setBackgroundProcessTableEntry(BackgroundProcessTableEntry backgroundProcessTableEntry);
	
	//public abstract void setBackgroundProcessFinishedStatus(final BackgroundProcessTableEntry entry, final BackgroundProcessInfo.Status status, final String statusMessage);	
}
