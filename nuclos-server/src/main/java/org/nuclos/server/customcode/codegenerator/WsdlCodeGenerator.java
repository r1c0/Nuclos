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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.log4j.Logger;
import org.nuclos.common.CryptUtil;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common2.LangUtils;
import org.nuclos.common2.StringUtils;
import org.nuclos.server.common.NuclosSystemParameters;
import org.nuclos.server.genericobject.valueobject.GenericObjectDocumentFile;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

/**
 * Generate java code from wsdl files using Apache Axis2.
 * <br>
 * Created by Novabit Informationssysteme GmbH <br>
 * Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 */
@Configurable
public class WsdlCodeGenerator implements CodeGenerator {

	private static final Logger LOG = Logger.getLogger(WsdlCodeGenerator.class);

	public static final String DEFAULT_PACKAGE_WEBSERVICES = "org.nuclos.webservices";
	
	// Spring injection

	private NuclosJavaCompilerComponent nuclosJavaCompilerComponent;
	
	// End of Spring injection
	
	private final MasterDataVO webservice;

	private List<JavaSourceAsString> sourcefiles;
	
	private boolean wsdlChecked = false;
	
	private boolean recompileIsNecessary = true;
	
	private File wsdl;

	private String packageName;
	
	private File generatedSourceFolder;
	
	public WsdlCodeGenerator(MasterDataVO webservice) {
		this.webservice = webservice;
	}
	
	@Autowired
	final void setNuclosJavaCompilerComponent(NuclosJavaCompilerComponent nuclosJavaCompilerComponent) {
		this.nuclosJavaCompilerComponent = nuclosJavaCompilerComponent;
	}
	
	private void checkWsdl() throws IOException {
		if (!wsdlChecked) {
			GenericObjectDocumentFile gofile = webservice.getField("wsdl", GenericObjectDocumentFile.class);
			wsdl = new File(nuclosJavaCompilerComponent.getWsdlDir(), gofile.getFilename());
			final String newDigest = CryptUtil.digestStringOf(gofile.getContents());
			
			final File wsdlDigest = new File(wsdl.getParent(), wsdl.getName() + ".sha1");
			if (wsdlDigest.canRead()) {
				final BufferedReader reader = new BufferedReader(new InputStreamReader(
						new FileInputStream(wsdlDigest), "UTF-8"));
				try {
					final String oldDigest = reader.readLine();
					if (oldDigest != null) {
						LOG.debug("WSDL digest: old: " + oldDigest + " new: " + newDigest);
						recompileIsNecessary = !oldDigest.equals(newDigest);
						// force recompile for testing
						// recompileIsNecessary = true;
					}
				}
				finally {
					reader.close();
				}
			}
			LOG.info("recompileIsNecessary: " + recompileIsNecessary);

			packageName = DEFAULT_PACKAGE_WEBSERVICES + "." +  WsdlCodeGenerator.getServiceName(webservice.getField("name", String.class));
			generatedSourceFolder =  new File(nuclosJavaCompilerComponent.getOutputPath(), "src/" + packageName.replaceAll("\\.", "/"));
			
			if (recompileIsNecessary) {
				deleteAndNew(wsdl);
				deleteAndNew(wsdlDigest);
				
				final OutputStream wsdlOut = new BufferedOutputStream(new FileOutputStream(wsdl));
				final BufferedWriter wsdlDigestOut = new BufferedWriter(new OutputStreamWriter(
						new FileOutputStream(wsdlDigest), "UTF-8"));
				try {
					wsdlOut.write(gofile.getContents());
					wsdlDigestOut.write(newDigest);
				}
				finally {
					wsdlOut.close();
					wsdlDigestOut.close();
				}
				
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
			}
			
			wsdlChecked = true;
		}
	}
	
	private void deleteAndNew(File file) throws IOException {
		if (file.exists()) {
			file.delete();
		}
		file.getParentFile().mkdirs();
		if (!file.createNewFile()) {
			throw new IOException("can delete and re-create " + file);
		}
	}

	@Override
	public boolean isRecompileNecessary() {
		try {
			checkWsdl();
		}
		catch (IOException e) {
			throw new IllegalStateException(e.toString(), e);
		}
		return recompileIsNecessary || !NuclosJavaCompilerComponent.JARFILE.exists();
	}

	@Override
	public String getPrefix() {
		final StringBuilder writer = new StringBuilder();
		writer.append("// DO NOT REMOVE THIS COMMENT (UP TO PACKAGE DECLARATION)");
		writer.append("\n// class=org.nuclos.server.customcode.codegenerator.WsdlCodeGenerator");
		writer.append("\n// type=org.nuclos.server.masterdata.valueobject.MasterDataVO");
		writer.append("\n// name=");
		writer.append(wsdl.getName());
		writer.append("\n// id=");
		if (webservice.getId() != null) {
			writer.append(webservice.getId().toString());
		}
		writer.append("\n// version=");
		writer.append(Integer.toString(webservice.getVersion()));
		writer.append("\n// modified=");
		final Date changed = webservice.getChangedAt();
		if (changed != null) {
			writer.append(Long.toString(changed.getTime()));
		}
		writer.append("\n// date=");
		if (changed != null) {
			writer.append(changed.toString());
		}
		writer.append("\n// END\n");
		return writer.toString();
	}

	@Override
	public int getPrefixAndHeaderLineCount() {
		return StringUtils.countLines(getPrefix());
	}

	@Override
	public int getPrefixAndHeaderOffset() {
		return getPrefix().length();
	}

	@Override
	public void writeSource(Writer writer, JavaSourceAsString src) throws IOException {
		writer.write(src.getPrefix());
		writer.write(src.getSource());
	}

	@Override
	public synchronized Iterable<? extends JavaSourceAsString> getSourceFiles() {
		if (sourcefiles == null) {
			try {
				checkWsdl();
				ClassLoader classloader = new AxisCodeGenerationClassLoader(WsdlCodeGenerator.class.getClassLoader());
				Class<?> clzzOptionParser = classloader.loadClass("org.apache.axis2.util.CommandLineOptionParser");
				Class<?> clzzEngine = classloader.loadClass("org.apache.axis2.wsdl.codegen.CodeGenerationEngine");
				String[] args = new String[]{"-uri", wsdl.getAbsolutePath(),
					"-o", nuclosJavaCompilerComponent.getOutputPath().getAbsolutePath(),
					"-p", packageName,
					};

				Object optionParser = clzzOptionParser.getConstructor(new Class[]{String[].class}).newInstance(new Object[]{args});
				Object engine = clzzEngine.getConstructor(clzzOptionParser).newInstance(optionParser);
				clzzEngine.getMethod("generate").invoke(engine);

				final File[] sourceFiles = generatedSourceFolder.listFiles(new FileFilter() {
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
					String name = packageName + "." + sourcefile.getName().substring(0, sourcefile.getName().lastIndexOf('.'));
					result.add(new JavaSourceAsString(name, getPrefix(), readFile(sourcefile), NuclosEntity.WEBSERVICE.getEntityName(), 
							webservice.getId() == null ? null : ((Integer)webservice.getId()).longValue(),
							getPrefixAndHeaderLineCount(), getPrefixAndHeaderOffset()));
				}
				sourcefiles = result;
			}
			catch (Exception ex) {
				throw new NuclosFatalException(ex);
			}
		}
		return sourcefiles;
	}

	private String readFile(File f) throws IOException {
		final StringBuilder text = new StringBuilder();
		final String newline = System.getProperty("line.separator");
		final Scanner scanner = new Scanner(new BufferedInputStream(new FileInputStream(f)),
				NuclosJavaCompilerComponent.JAVA_SRC_ENCODING);
		try {
			while (scanner.hasNextLine()) {
				text.append(scanner.nextLine() + newline);
			}
		}
		finally {
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
	public String toString() {
		final StringBuilder result = new StringBuilder("WsdlCG[file=");
		result.append(wsdl.getPath());
		if (sourcefiles != null) {
			result.append(",src=").append(sourcefiles);
		}
		result.append("]");
		return result.toString();
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

	private static class AxisCodeGenerationClassLoader extends URLClassLoader {

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
