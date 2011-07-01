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
package org.nuclos.server.dbtransfer;

import org.nuclos.server.common.LockedTabProgressNotifier;

public class TransferNotifierHelper {
	
	private final LockedTabProgressNotifier jmsNotifier;
	private final String text;
	private final int start;
	private final int end;
	
	private double progressPerStep;
	private double progress;
	
	public TransferNotifierHelper(LockedTabProgressNotifier jmsNotifier, String text, int start, int end) {
		super();
		this.jmsNotifier = jmsNotifier;
		this.text = text;
		this.start = start;
		this.end = end;
		
		progressPerStep = (end-start);
		progress = Integer.valueOf(start).doubleValue();
	}
	
	public void setSteps(int steps) {
		progressPerStep = (Double.valueOf(end-start))/Double.valueOf(steps);
	}
	
	public void notifyNextStep() {
		jmsNotifier.notify(text, Double.valueOf(progress).intValue());
		progress += progressPerStep;
	}

}
