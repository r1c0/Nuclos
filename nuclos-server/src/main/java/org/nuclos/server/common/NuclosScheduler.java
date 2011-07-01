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
package org.nuclos.server.common;

import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.SpringApplicationContextHolder;
import org.quartz.Scheduler;

/**
 * Singleton class for holding the instance of a Quartz Scheduler.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:ramin.goettlich@novabit.de">ramin.goettlich</a>
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph Radig</a>
 * @version 00.01.000
 */
public class NuclosScheduler {

	public static final String JOBGROUP_IMPORT = "import";

	private static NuclosScheduler singleton;

	private final Scheduler scheduler;

	public static synchronized NuclosScheduler getInstance() {
		if (singleton == null) {
			singleton = new NuclosScheduler();
		}
		return singleton;
	}

	private NuclosScheduler() {
		try {
			scheduler = (Scheduler)SpringApplicationContextHolder.getBean("nuclosScheduler");
		}
		catch (Exception ex) {
			throw new NuclosFatalException(ex);
		}
	}

	public Scheduler getScheduler() {
		return this.scheduler;
	}

}	// class NuclosScheduler
