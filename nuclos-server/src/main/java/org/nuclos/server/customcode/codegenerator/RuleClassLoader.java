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
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.jar.JarFile;

import org.apache.log4j.Logger;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.server.common.NuclosSystemParameters;
import org.nuclos.server.ruleengine.NuclosCompileException;

/**
 * ClassLoader for loading business and timelimit rules.
 * Common generated code like common code artifacts and webservice proxies are loaded if present (and active).
 * Each compilation unit is validated by comparing the actual manifest with the artifacts's generated one.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 */
public class RuleClassLoader extends URLClassLoader {

	private static final Logger log = Logger.getLogger(RuleClassLoader.class);


	public static ClassLoader getInstance() throws NuclosCompileException {
		return getInstance(RuleClassLoader.class.getClassLoader());
	}

	/**
	 * Obtain an instance of the classloader for a given rule artifact.
	 *
	 * @param parent the parent classloader for classloader delegation (usually the application classloader)
	 * @param rulegenerator a generator for the loaded artifact
	 * @return classloader
	 * @throws NuclosCompileException
	 */
	@SuppressWarnings("deprecation")
	public static ClassLoader getInstance(ClassLoader parent) throws NuclosCompileException {
		RuleClassLoader classLoader = new RuleClassLoader(parent);

		JarFile jar = null;
		try {
			NuclosJavaCompiler.validate();
			jar = new JarFile(NuclosJavaCompiler.JARFILE);
			classLoader.addJarsToClassPath(NuclosSystemParameters.getDirectory(NuclosSystemParameters.WSDL_GENERATOR_LIB_PATH));
			classLoader.addURL(NuclosJavaCompiler.JARFILE.toURL());
		}
		catch (IOException ex) {
			throw new NuclosFatalException(ex);
		}
		finally {
			try {
				if (jar != null) {
					jar.close();
				}
			}
			catch(IOException e) {
				log.warn(e);
			}
		}

		return classLoader;
	}

	private RuleClassLoader(ClassLoader parent) {
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
					log.debug(jarFiles[i].toString());
					addURL(new URL(jarFiles[i].toURI().toString()));
				}
			}
		}
		catch(Exception e) {
			log.warn(e.getMessage(), e);
		}
	}
}
