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
package org.nuclos.server.restservice;

import org.nuclos.server.customcode.CustomCodeManager;
import org.nuclos.server.ruleengine.NuclosCompileException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class NuclosRestWorkerFactory {
	
	CustomCodeManager customCodeManager;	
	
	public NuclosRestWorker<?> getRestfulWorker(String sClazz) {
		NuclosRestWorker<?> worker = null;
		try {
			worker = (NuclosRestWorker<?>) Class.forName(sClazz).newInstance();
								
			return worker;
		}
		catch(ClassNotFoundException e) {
			try {
				worker = (NuclosRestWorker<?>)customCodeManager.getInstance(sClazz);
				return worker;
			} catch (NuclosCompileException ex) {
				throw new NuclosRestServiceException(ex);
			}
		}
		catch(ClassCastException e) {
			try {
				worker = (NuclosRestWorker<?>)customCodeManager.getInstance(sClazz);
				return worker;
			} catch (NuclosCompileException ex) {
				throw new NuclosRestServiceException(ex);
			}
		}
		catch(Exception ex) {
			throw new NuclosRestServiceException(ex);
		}		
	}
	
	@Autowired
	void setCustomCodeManager(CustomCodeManager manager) {
		this.customCodeManager = manager;
	}
		
}
