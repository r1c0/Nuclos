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
package org.nuclos.tools.licensemaker;

public class LicenseMakerMain {

	/**
	 * exit 0 - everything ok
	 * exit 1 - sourcefolder invalid
	 * exit 2 - license file invalid 
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		String licenseFile = "C:/_dev/_workspace/nucleus/nuclos/conf/nucloslicense.txt";
		String sourceFolder = "C:/_dev/_workspace/nucleus/nuclos/src/java";
		
		new LicenseMaker(licenseFile, sourceFolder);
	}
}
