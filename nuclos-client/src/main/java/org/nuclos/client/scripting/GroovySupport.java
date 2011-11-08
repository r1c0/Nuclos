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

package org.nuclos.client.scripting;

import groovy.lang.GroovyClassLoader;
import groovy.lang.MetaClass;
import groovy.lang.MetaMethod;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.nuclos.client.main.Main;
import org.nuclos.client.ui.Errors;
import org.nuclos.common.NuclosScript;
import org.nuclos.common.collect.collectable.Collectable;
import org.nuclos.common.collect.collectable.CollectableField;
import org.nuclos.common.collection.Transformer;
import org.nuclos.common2.StringUtils;

public class GroovySupport {

	Class<?> groovyClass;
	Object instance;

	static final ScriptEngine engine = new ScriptEngineManager().getEngineByName("groovy");

	public GroovySupport() {
	}

	public void compile(String text) {
		groovyClass = null;
		instance = null;

		ClassLoader parent = getClass().getClassLoader();
		GroovyClassLoader loader = new GroovyClassLoader(parent);

		DefaultGroovyMethods.mixin(Collectable.class, CollectableMixin.class);
		groovyClass = loader.parseClass(text);
	}

	public Class<?> getGroovyClass() {
		return groovyClass;
	}

	public boolean isCompiled() {
		return groovyClass != null;
	}

	public void prepare() throws InstantiationException, IllegalAccessException {
		getInstance();
	}

	public Object getInstance() throws InstantiationException, IllegalAccessException {
		if (instance != null) {
			return instance;
		}
		instance = groovyClass.newInstance();
		if (instance instanceof Runnable) {
			((Runnable) instance).run();
		}
		return instance;
	}

	public MetaMethod getMethod(String name, Class<?>... argumentTypes) {
		if (groovyClass == null || name == null)
			return null;
		MetaClass metaClass = InvokerHelper.getMetaClass(groovyClass);
		return metaClass.pickMethod(name, argumentTypes);
	}

	public List<String> findMethodNames(Class<?>... argumentTypes) {
		Set<String> methods = new TreeSet<String>();
		if (groovyClass != null) {
			MetaClass metaClass = InvokerHelper.getMetaClass(groovyClass);
			for (MetaMethod method : metaClass.getMetaMethods()) {
				if (method.isValidMethod(argumentTypes)) {
					methods.add(method.getName());
				}
			}
		}
		return new ArrayList<String>(methods);
	}

	public boolean methodExists(String name) {
		if (groovyClass != null) {
			MetaClass metaClass = InvokerHelper.getMetaClass(groovyClass);
			for (MetaMethod method : metaClass.getMetaMethods()) {
				if (name.equals(method.getName())) {
					return true;
				}
			}
		}
		return false;
	}

	public InvocableMethod getInvocable(String name, Class<?>... signature) throws InstantiationException, IllegalAccessException {
		if (name != null) {
			MetaMethod method = getMethod(name, signature);
			return new InvocableMethod(getInstance(), method);
		}
		return null;
	}

	public static Object eval(NuclosScript script, final Collectable c, Object defaultValue) {
		final Bindings b = engine.createBindings();
        String source = StringUtils.replaceParameters(script.getSource(), new Transformer<String, String>() {
			@Override
			public String transform(String i) {
				String variable = "__var_" + i;
				b.put(variable, c.getValue(i));
				return variable;
			}
		});

        try {
			return engine.eval(source, b);
		} catch (ScriptException e) {
			return defaultValue;
		}
	}

	public static class InvocableMethod {

		private final Object delegate;
		private final MetaMethod groovyMethod;
		private boolean hasErrors;

		private InvocableMethod(Object delegate, MetaMethod method) {
			this.delegate = delegate;
			this.groovyMethod = method;
		}

		public Object invoke(Object... args) {
			if (delegate != null && groovyMethod != null && !hasErrors) {
				try {
					return groovyMethod.invoke(delegate, args);
				} catch (Exception ex) {
					hasErrors = true;
					Errors.getInstance().showExceptionDialog(Main.getMainFrame(), "Fehler in Skriptmethode " + groovyMethod.getName(), ex);
				}
			}
			return null;
		}

		public boolean hasErrors() {
			return hasErrors;
		}
	}

	public static class CollectableMixin {

		public static Object getAt(Collectable clct, String name) {
			return clct.getValue(name);
		}

		public static CollectableField propertyMissing(Collectable clct, String name) {
			return clct.getField(name);
		}
	}

}
