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

import java.text.SimpleDateFormat;

import org.apache.log4j.Logger;

/**
 * Nucleus Performance Logger (s. log4j Konfiguration: NovabitPerformanceLogger & PERFORMANCEFILE).
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:rostislav.maksymovskyi@novabit.de">Rostislav Maksymovskyi</a>
 * @version 01.00.00
 */

public class NuclosPerformanceLogger {	

	public static void performanceLog(
			final long startTime, 
			final long endateTime, 
			final String currentUserName,
			final Object currentObjId, 
			final Object currentEntityIdName,
			final String txtMsg, 
			final String addInfo1, 
			final String addInfo2) {

		performanceLog(startTime, 
				endateTime, 
				currentUserName,
				currentObjId, 
				currentEntityIdName,
				txtMsg, 
				addInfo1, 
				addInfo2,
				false);

	}
	public static void performanceLog(
			final long startTime, 
			final long endateTime, 
			final String currentUserName,
			final Object currentObjId, 
			final Object currentEntityIdName,
			final String txtMsg, 
			final String addInfo1, 
			final String addInfo2,
			final boolean logAddInfoSeparate) {
		SimpleDateFormat dFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss:SSS");
		String perfLogMsg = ";"+(endateTime-startTime); 		
		perfLogMsg += ";"+dFormat.format(startTime);			
		perfLogMsg += ";"+dFormat.format(endateTime);			
		perfLogMsg += ";"+currentUserName;						
		perfLogMsg += ";"+((currentObjId != null)? "Objekt: "+currentObjId.toString() : "");				
		perfLogMsg += ";"+((currentEntityIdName != null)? "Entity: "+currentEntityIdName.toString() : ""); 
		perfLogMsg += ";"+txtMsg;								
		perfLogMsg += ";"+((!logAddInfoSeparate && addInfo1 != null)? addInfo1 : ""); 	
		perfLogMsg += ";"+((!logAddInfoSeparate && addInfo2 != null)? addInfo2 : ""); 	
		Logger.getLogger("NovabitPerformanceLogger").info(perfLogMsg); //9 Spalten

		if(logAddInfoSeparate && addInfo1 != null && addInfo1.length() > 0){
			Logger.getLogger("NovabitPerformanceAddInfoLogger").info(addInfo1); 
		}
		if(logAddInfoSeparate && addInfo2 != null && addInfo2.length() > 0){
			Logger.getLogger("NovabitPerformanceAddInfoLogger").info(addInfo2); 
		}
	}

	} // class NuclosPerformanceLogger


