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

import java.io.File;
import java.util.Properties;

import org.nuclos.common2.StringUtils;


/**
 * Constants for Nuclos system-internal parameters.  These parameters cannot be overridden by
 * means of the T_AD_PARAMETER table.
 */
public class NuclosSystemParameters {

	// wurden sowieso statisch (=einmalig) im NuclosCodeGenerator verwendet
	public static final String GENERATOR_OUTPUT_PATH = "nuclos.codegenerator.output.path";
	public static final String WSDL_GENERATOR_LIB_PATH = "nuclos.wsdl.generator.lib.path";
	public static final String WSDL_GENERATOR_OUTPUT_PATH = "nuclos.wsdl.generator.output.path";

	// for JasperReports, we use the same name as the latter system property (see ReportFacadeBean)
	// @Deprecated public static final String JASPER_REPORTS_COMPILE_CLASS_PATH = "jasper.reports.compile.class.path";
	public static final String JASPER_REPORTS_COMPILE_KEEP_JAVA_FILE = "jasper.reports.compile.keep.java.file";
	public static final String JASPER_REPORTS_COMPILE_TMP = "jasper.reports.compile.temp";
	// @Deprecated /** @deprecated Nuclos comes with its own JR compiler adapter using javax.tools API */
	// public static final String JASPER_REPORTS_COMPILER_CLASS = "jasper.reports.compiler.class";

	public static final String DOCUMENT_PATH = "nuclos.data.documents.path";
	public static final String EXPORT_IMPORT_PATH = "nuclos.data.expimp.path";
	public static final String RESOURCE_PATH = "nuclos.data.resource.path";
	public static final String REPORT_PATH = "nuclos.data.reports.path";
	public static final String DATABASE_STRUCTURE_CHANGE_LOG_PATH = "nuclos.data.database-structure-changes.path";

	private static final Properties systemParameters;
	static {
		// For a list of predefined JBoss properties, see http://community.jboss.org/wiki/JBossProperties
		systemParameters= new Properties(System.getProperties());
		ServerProperties.loadProperties(systemParameters, ServerProperties.JNDI_SERVER_PROPERTIES, true);
	}

	private NuclosSystemParameters() {
	}

	/**
	 * Returns a parameter which denotes a server-side directory as {@link File} object.
	 */
	public static File getDirectory(String key) {
		return getFile(key); // internally is getDirectory the same as getFile()
	}

	/**
	 * Returns a parameter which denotes a server-side file as {@link File} object.
	 */
	public static File getFile(String key) {
		String value = getString(key);
		if (value != null) {
			return new File(value);
		}
		return null;
	}

	public static Integer getInteger(String key) {
		String value = getString(key);
		if (value != null) {
			return Integer.parseInt(value);
		}
		return null;
	}

	public static String getString(String key) {
		return getImpl(key);
	}

	private static String getImpl(String key) {
		String value = systemParameters.getProperty(key);
		if (value != null) {
			return StringUtils.replaceParametersRecursively(value, systemParameters);
		}
		return null;
	}
}
