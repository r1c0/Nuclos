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

import org.apache.log4j.Logger;
import org.nuclos.server.customcode.codegenerator.NuclosJavaCompiler;
import org.nuclos.server.customcode.codegenerator.RuleClassLoader;
import org.nuclos.server.customcode.codegenerator.RuleCodeGenerator;
import org.nuclos.server.ruleengine.NuclosCompileException;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class CustomCodeManager implements ApplicationContextAware {

	private static final Logger log = Logger.getLogger(CustomCodeManager.class);

	//private ApplicationContext context;
	private ClassLoader cl;

	@Override
	public void setApplicationContext(ApplicationContext context) throws BeansException {
		//this.context = context;
	}

	@SuppressWarnings("unchecked")
	public <T> T getInstance(RuleCodeGenerator<T> generator) throws NuclosCompileException {
		try {
			if (NuclosJavaCompiler.validate()) {
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
				this.cl = RuleClassLoader.getInstance(CustomCodeManager.class.getClassLoader());
			}
			catch(NuclosCompileException ex) {
				log.error("Compilation failed.", ex);
			}
		}
		return cl;
	}
}
