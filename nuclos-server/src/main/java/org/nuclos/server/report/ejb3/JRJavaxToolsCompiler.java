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

package org.nuclos.server.report.ejb3;

import java.io.ByteArrayOutputStream;
import java.io.File;

import javax.tools.JavaCompiler;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.design.JRAbstractMultiClassCompiler;

import org.apache.log4j.Logger;
import org.nuclos.server.customcode.codegenerator.NuclosJavaCompilerComponent;


/**
 * A JasperReports Compiler adapter which uses the Java 6's
 * javax.tools.JavaCompiler API (but only the simple variant
 * with run).
 * 
 * Based on JRJdk13Compiler.
 */
public class JRJavaxToolsCompiler extends JRAbstractMultiClassCompiler
{
	static final Logger LOG = Logger.getLogger(JRJavaxToolsCompiler.class);

	private static final int COMPILER_SUCCESS = 0;

	@Override
	public String compileClasses(File[] sourceFiles, String classpath) throws JRException
	{
		String[] source = new String[sourceFiles.length + 2];
		for (int i = 0; i < sourceFiles.length; i++)
		{
			source[i] = sourceFiles[i].getPath();
		}
		source[sourceFiles.length] = "-classpath";
		source[sourceFiles.length + 1] = classpath;
		
		String errors = null;

		JavaCompiler javac = NuclosJavaCompilerComponent.getJavaCompilerTool();
		if (javac == null) {
			throw new JRException("No registered system Java compiler found");
		}
		
		try 
		{
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			int result = javac.run(null, null, baos, source);
			
			if (result != COMPILER_SUCCESS)
			{
				errors = baos.toString();
			}
			else 
			{
				if (LOG.isInfoEnabled() && baos.size() > 0)
				{
					LOG.info(baos.toString());
				}
			}
		}
		catch (Exception e)
		{
			StringBuffer files = new StringBuffer();
			for (int i = 0; i < sourceFiles.length; ++i)
			{
				files.append(sourceFiles[i].getPath());
				files.append(' ');
			}
			throw new JRException("Error compiling report java source files : " + files, e);
		}
		
		return errors;
	}
}
