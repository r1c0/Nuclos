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
package org.nuclos.client.application.assistant;

import java.util.HashMap;
import java.util.Map;



public class ApplicationObserver {
	
	public static final String NEWENTITY = "newEntity";
	public static final String STARTUPPANEL = "startupPanel";
	
	static ApplicationObserver observer;
	
	Map<String,ApplicationAssistantListener> mp;
	
	
	protected ApplicationObserver() {
		mp = new HashMap<String,ApplicationAssistantListener>();
	}
	
	public void addApplicationAssistantListener(String name, ApplicationAssistantListener listener) {
		mp.put(name, listener);
	}
	
	public void fireApplicationChangedEvent(ApplicationChangedEvent event) {
		try {
			for(ApplicationAssistantListener l : mp.values()) {
				l.applicationChanged(event);
			}
		}
		catch(Exception e) {
			// do noting here
		}
	}
	
	public void removeApplicationAssistantListener(String name) {
		mp.remove(name);
	}
	
	public static synchronized ApplicationObserver getInstance()  {
		if(observer == null) {
			observer = new ApplicationObserver();
		}
		return observer;
	}

}
