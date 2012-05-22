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
package org.nuclos.server.customcode;

import java.io.IOException;
import java.util.jar.JarFile;

import org.apache.log4j.Logger;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.server.common.NuclosSystemParameters;
import org.nuclos.server.customcode.codegenerator.NuclosJavaCompilerComponent;
import org.nuclos.server.customcode.codegenerator.RuleClassLoader;
import org.nuclos.server.customcode.codegenerator.RuleCodeGenerator;
import org.nuclos.server.ruleengine.NuclosCompileException;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * Provides the classloader for dynamically loaded code (Rules, Wsdl).
 */
@Component
public class CustomCodeManager {

	private static final Logger log = Logger.getLogger(CustomCodeManager.class);
	
	// Spring injection
	
	private NuclosJavaCompilerComponent nuclosJavaCompilerComponent;
	
	// End of Spring injection

	private ClassLoader cl;

	CustomCodeManager() {
	}
	
	@Autowired
	final void setNuclosJavaCompilerComponent(NuclosJavaCompilerComponent nuclosJavaCompilerComponent) {
		this.nuclosJavaCompilerComponent = nuclosJavaCompilerComponent;
	}
	
	public <T> T getInstance(RuleCodeGenerator<T> generator) throws NuclosCompileException {
		try {
			if (nuclosJavaCompilerComponent.validate()) {
				this.cl = null;
			}
			return (T) getClassLoader().loadClass(generator.getClassName()).newInstance();
		}
		catch(InstantiationException e) {
			throw new NuclosCompileException(e);
		}
		catch(IllegalAccessException e) {
			throw new NuclosCompileException(e);
		}
		catch(ClassNotFoundException e) {
			throw new NuclosCompileException(e);
		}
	}

	private synchronized ClassLoader getClassLoader() {
		if (cl == null) {
			try {
				this.cl = getInstance(CustomCodeManager.class.getClassLoader());
			}
			catch(NuclosCompileException ex) {
				log.error("Compilation failed.", ex);
			}
		}
		return cl;
	}
	
	/**
	 * Obtain an instance of the classloader for a given rule artifact.
	 *
	 * @param parent the parent classloader for classloader delegation (usually the application classloader)
	 * @param rulegenerator a generator for the loaded artifact
	 * @return classloader
	 * @throws NuclosCompileException
	 */
	public ClassLoader getInstance(ClassLoader parent) throws NuclosCompileException {
		RuleClassLoader classLoader = new RuleClassLoader(parent);

		JarFile jar = null;
		try {
			nuclosJavaCompilerComponent.validate();
			jar = new JarFile(NuclosJavaCompilerComponent.JARFILE);
			classLoader.addJarsToClassPath(NuclosSystemParameters.getDirectory(NuclosSystemParameters.WSDL_GENERATOR_LIB_PATH));
			classLoader.addURL(NuclosJavaCompilerComponent.JARFILE.toURL());
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
				log.warn("getInstance: " + e);
			}
		}
		return classLoader;
	}
	
	public ClassLoader getInstance() throws NuclosCompileException {
		return getInstance(RuleClassLoader.class.getClassLoader());
	}

}
