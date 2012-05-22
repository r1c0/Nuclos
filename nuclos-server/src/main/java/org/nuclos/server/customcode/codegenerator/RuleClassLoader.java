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

package org.nuclos.server.customcode.codegenerator;

import java.io.File;
import java.io.FileFilter;
import java.net.URL;
import java.net.URLClassLoader;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Configurable;

/**
 * ClassLoader for loading business and timelimit rules.
 * Common generated code like common code artifacts and webservice proxies are loaded if present (and active).
 * Each compilation unit is validated by comparing the actual manifest with the artifacts's generated one.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 */
@Configurable
public class RuleClassLoader extends URLClassLoader {

	private static final Logger LOG = Logger.getLogger(RuleClassLoader.class);
	
	//

	public RuleClassLoader(ClassLoader parent) {
		super(new URL[]{}, parent);
	}

	public void addJarsToClassPath(File folder) {
		try {
			if(folder.exists()) {
				File[] jarFiles = folder.listFiles(new FileFilter() {
					@Override
					public boolean accept(File arg0) {
						if(arg0.toString().lastIndexOf(".jar") != -1)
							return true;
						else
							return false;
					}
				});
				for(int i = 0; i < jarFiles.length; i++) {
					LOG.debug(jarFiles[i].toString());
					addURL(new URL(jarFiles[i].toURI().toString()));
				}
			}
		}
		catch(Exception e) {
			LOG.warn("addJarsToClassPath" + e, e);
		}
	}
	
	@Override
    public void addURL(URL url) {
    	super.addURL(url);
    }
	
}
