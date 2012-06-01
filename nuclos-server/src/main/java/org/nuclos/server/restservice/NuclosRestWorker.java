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

import org.springframework.web.servlet.ModelAndView;

/**
 * 
 * @author marc.finke
 *
 * @param <T>
 */
public interface NuclosRestWorker<T> {
	
	/**
	 * set a parameter to work for
	 * @param parameter
	 */
	public void setParameter(T parameter);
	
	/**
	 * lets do some work in this method
	 * get some data or do some stuff
	 */
	public void doWork();
	
	/**
	 * return a ModelAndView Object
	 * ModelAndView contains a View
	 * a View can be a Web-Site or PDF Dokument or something similar
	 * @return
	 */
	public ModelAndView getModelAndView();

}
