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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.log4j.Logger;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common2.LangUtils;
import org.nuclos.common2.StringUtils;
import org.nuclos.server.common.NuclosSystemParameters;
import org.nuclos.server.genericobject.valueobject.GenericObjectDocumentFile;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;

/**
 * Generate java code from wsdl files using Apache Axis2.
 * <br>
 * Created by Novabit Informationssysteme GmbH <br>
 * Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 */
public class WsdlCodeGenerator implements CodeGenerator {

	private static final Logger LOG = Logger.getLogger(WsdlCodeGenerator.class);

	public static final String DEFAULT_PACKAGE_WEBSERVICES = "org.nuclos.webservices";

	private final MasterDataVO webservice;

	private List<JavaSourceAsString> sourcefiles;

	public WsdlCodeGenerator(MasterDataVO webservice) {
		this.webservice = webservice;
	}

	@Override
	public synchronized Iterable<? extends JavaSourceAsString> getSourceFiles() {
		if (sourcefiles == null) {
			try {
				ClassLoader classloader = new AxisCodeGenerationClassLoader(WsdlCodeGenerator.class.getClassLoader());

				GenericObjectDocumentFile gofile = this.webservice.getField("wsdl", GenericObjectDocumentFile.class);
				File wsdl = new File(getWsdlDir(), gofile.getFilename());
				if (wsdl.exists()) {
					wsdl.delete();
				}
				wsdl.createNewFile();
				FileOutputStream outstream = new FileOutputStream(wsdl);
				outstream.write(gofile.getContents());
				outstream.close();

				String packagename = DEFAULT_PACKAGE_WEBSERVICES + "." +  WsdlCodeGenerator.getServiceName(webservice.getField("name", String.class));

				File generatedSourceFolder =  new File(NuclosJavaCompiler.getOutputPath(), "src/" + packagename.replaceAll("\\.", "/"));
				// cleanup
				if (generatedSourceFolder.exists()) {
					File[] generatedSourcefiles = generatedSourceFolder.listFiles(new FileFilter() {
						@Override
						public boolean accept(File pathname) {
							if(pathname.toString().lastIndexOf(".java") != -1)
								return true;
							else
								return false;
						}
					});
					for (File generatedSourcefile : generatedSourcefiles)
						generatedSourcefile.delete();
				}

				Class<?> clzzOptionParser = classloader.loadClass("org.apache.axis2.util.CommandLineOptionParser");
				Class<?> clzzEngine = classloader.loadClass("org.apache.axis2.wsdl.codegen.CodeGenerationEngine");
				String[] args = new String[]{"-uri", wsdl.getAbsolutePath(),
					"-o", NuclosJavaCompiler.getOutputPath().getAbsolutePath(),
					"-p", packagename,
					};

				Object optionParser = clzzOptionParser.getConstructor(new Class[]{String[].class}).newInstance(new Object[]{args});
				Object engine = clzzEngine.getConstructor(clzzOptionParser).newInstance(optionParser);
				clzzEngine.getMethod("generate").invoke(engine);

				File[] sourceFiles = generatedSourceFolder.listFiles(new FileFilter() {
					@Override
					public boolean accept(File arg0) {
						if(arg0.toString().lastIndexOf(".java") != -1)
							return true;
						else
							return false;
					}
				});

				List<JavaSourceAsString> result = new ArrayList<JavaSourceAsString>();
				for (File sourcefile : sourceFiles) {
					String name = packagename + "." + sourcefile.getName().substring(0, sourcefile.getName().lastIndexOf('.'));
					result.add(new JavaSourceAsString(name, readFile(sourcefile), NuclosEntity.WEBSERVICE.getEntityName(), webservice.getId() == null ? null : ((Integer)webservice.getId()).longValue()));
				}
				sourcefiles = result;
			}
			catch (Exception ex) {
				throw new NuclosFatalException(ex);
			}
		}
		return sourcefiles;
	}

	private File getWsdlDir() {
		File dir = new File(NuclosJavaCompiler.getOutputPath(), "wsdl");
		if (!dir.exists()) {
			dir.mkdir();
		}
		return dir;
	}

	private String readFile(File f) throws IOException {
		StringBuilder text = new StringBuilder();
	    String newline = System.getProperty("line.separator");
	    Scanner scanner = new Scanner(new FileInputStream(f), NuclosJavaCompiler.ENCODING);
	    try {
	      while (scanner.hasNextLine()){
	        text.append(scanner.nextLine() + newline);
	      }
	    }
	    finally{
	      scanner.close();
	    }
	    return text.toString();
	}

	@Override
	public int hashCode() {
		HashCodeBuilder builder = new HashCodeBuilder(17, 37);
		builder.append(webservice.getId());
		builder.append(webservice.getVersion());
		return builder.toHashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof WsdlCodeGenerator)) {
			return false;
		}
		else {
			WsdlCodeGenerator other = (WsdlCodeGenerator) obj;
			if (LangUtils.compare(this.webservice.getId(), other.webservice.getId()) == 0) {
				return LangUtils.compare(this.webservice.getVersion(), other.webservice.getVersion()) == 0;
			}
			else {
				return false;
			}
		}
	}

	@Override
	public byte[] postCompile(String name, byte[] bytecode) {
		return bytecode;
	}

	public static String getServiceName(String serviceName) {
		serviceName = serviceName.replaceAll(" ", "");
		serviceName = serviceName.toLowerCase();
		serviceName = serviceName.replaceAll("[^a-z,0-9]", "");

		return serviceName;
	}

	private class AxisCodeGenerationClassLoader extends URLClassLoader {

		AxisCodeGenerationClassLoader(ClassLoader parent) {
			super(new URL[]{}, parent);
			File libfolder = NuclosSystemParameters.getDirectory(NuclosSystemParameters.WSDL_GENERATOR_LIB_PATH);
			if (!libfolder.exists()) {
				final String msg = StringUtils.getParameterizedExceptionMessage("nuclos.compileexception.wsdllibpath", libfolder.getAbsolutePath());
				LOG.error("Missing axislibs folder: " + msg);
				throw new NuclosFatalException(msg);
			}

			File[] jarFiles = libfolder.listFiles(new FileFilter() {
				@Override
				public boolean accept(File f) {
					return (f.toString().lastIndexOf(".jar") != -1);
				}
			});
			if (jarFiles != null) {
				for (File jarFile : jarFiles) {
					try {
						addURL(new URL(jarFile.toURI().toString()));
					}
					catch(MalformedURLException e) {
						// Ok! (tp)
						e.printStackTrace();
						LOG.warn("AxisCodeGenerationClassLoader failed: " + e);
					}
				}
			}
		}
	}
}
