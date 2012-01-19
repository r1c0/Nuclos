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
package org.nuclos.common.job;

import org.nuclos.common2.KeyEnum;
import org.nuclos.common2.Localizable;

public enum IntervalUnit implements KeyEnum<String>, Localizable {

	MONTH("Monat", "job.intervalunit.month"),

	DAY("Tag", "job.intervalunit.day"),

	HOUR("Stunde", "job.intervalunit.hour"),

	MINUTE("Minute", "job.intervalunit.minute");

	private final String value;
	private final String resourceId;

	private IntervalUnit(String value, String resourceId) {
		this.value = value;
		this.resourceId = resourceId;
	}

	@Override
	public String getValue() {
		return this.value;
	}

	@Override
	public String getResourceId() {
		return resourceId;
	}
}	// enum ImportMode
