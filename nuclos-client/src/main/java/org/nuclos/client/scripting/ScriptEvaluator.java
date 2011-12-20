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

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.apache.log4j.Logger;
import org.nuclos.api.context.ScriptContext;
import org.nuclos.common.NuclosScript;

public class ScriptEvaluator {

	private static final String VARIABLE_CONTEXT = "context";
	private static final String VARIABLE_LOG = "log";

	private static final Logger LOG = Logger.getLogger(ScriptEvaluator.class);

	private static final ScriptEvaluator singleton = new ScriptEvaluator();

	private final ScriptEngine engine;

	private ScriptEvaluator() {
		engine = new ScriptEngineManager().getEngineByName("groovy");
	}

	public static final ScriptEvaluator getInstance() {
		return singleton;
	}

	public Object eval(NuclosScript script, ScriptContext context, Object defaultValue) {
		final Bindings b = engine.createBindings();
		b.put(VARIABLE_CONTEXT, context);
		b.put(VARIABLE_LOG, LOG);

        try {
			return engine.eval(script.getSource(), b);
		}
        catch (ScriptException e) {
			LOG.warn(e);
			return defaultValue;
		}
	}
}
