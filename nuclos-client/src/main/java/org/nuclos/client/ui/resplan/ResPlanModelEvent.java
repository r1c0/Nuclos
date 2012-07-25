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

package org.nuclos.client.ui.resplan;

import java.util.EventObject;

public class ResPlanModelEvent extends EventObject {

	private Object resource;
	private Object entry;
	private Interval<?> interval;
	private Object relation;
	
	public <R, T extends Comparable<? super T>, E, L> ResPlanModelEvent(ResPlanModel<R, T, E, L> source, R resource, E entry, Interval<T> interval, L relation) {
		super(source);
		this.resource = resource;
		this.entry = entry;
		this.interval = interval;
	}

	public Object getResource() {
		return resource;
	}
	
	public Object getEntry() {
		return entry;
	}
	
	public Object getInterval() {
		return interval;
	}
}
