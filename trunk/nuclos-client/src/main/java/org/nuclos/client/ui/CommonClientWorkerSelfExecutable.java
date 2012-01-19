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
/*
 * Created on 08.11.2005
 *
 */
package org.nuclos.client.ui;

import org.nuclos.common2.exception.CommonBusinessException;

/**
 * Interface for separating work and result (eg server communication vs. building up resultUI)
 *
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:florian.speidel@novabit.de">florian.speidel</a>
 */
public interface CommonClientWorkerSelfExecutable extends CommonClientWorker {

	/**
	 * 
	 * @throws CommonBusinessException
	 */
	void runInCallerThread() throws CommonBusinessException;
}
