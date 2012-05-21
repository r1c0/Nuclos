//Copyright (C) 2012  Novabit Informationssysteme GmbH
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
package org.nuclos.server.customcode.codegenerator;

import java.util.Timer;

import javax.annotation.PostConstruct;

import org.nuclos.common.ApplicationProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Thomas Pasch
 */
@Component
public class SourceScannerComponent {
	
	private final long INTERVAL_MILLIS = 1000 * 20;
	
	//
	
	private ApplicationProperties applicationProperties;

	private Timer timer;
	
	SourceScannerComponent() {
	}
	
	@Autowired
	void setTimer(Timer timer) {
		this.timer = timer;
	}
	
	@Autowired
	void setApplicationProperties(ApplicationProperties applicationProperties) {
		this.applicationProperties = applicationProperties;
	}
	
	@PostConstruct
	final void init() {
		if (applicationProperties.isFunctionBlockDev()) {
			timer.schedule(new SourceScannerTask(), INTERVAL_MILLIS, INTERVAL_MILLIS);
		}
	}
	
}
