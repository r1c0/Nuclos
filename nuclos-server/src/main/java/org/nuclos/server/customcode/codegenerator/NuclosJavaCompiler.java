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

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticListener;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;

import org.apache.log4j.Logger;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.server.common.NuclosSystemParameters;
import org.nuclos.server.customcode.codegenerator.CodeGenerator.JavaSourceAsString;
import org.nuclos.server.ruleengine.NuclosCompileException;
import org.nuclos.server.ruleengine.NuclosCompileException.ErrorMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

@Configurable
class NuclosJavaCompiler implements Closeable {

	private static final Logger LOG = Logger.getLogger(NuclosJavaCompiler.class);

	// Spring injection
	
	private NuclosJavaCompilerComponent nuclosJavaCompilerComponent;
	
	// End of Spring injection 

	private final Locale locale;

	private JavaCompiler javac;

	private CodeGeneratorDiagnosticListener diagnosticListener;

	private StandardJavaFileManager stdFileManager;

	NuclosJavaCompiler() {
		this(null);
	}
	
	NuclosJavaCompiler(Locale locale) {
		this.locale = locale;
	}

	@Autowired
	final void setNuclosJavaCompilerComponent(NuclosJavaCompilerComponent nuclosJavaCompilerComponent) {
		this.nuclosJavaCompilerComponent = nuclosJavaCompilerComponent;
	}

	@PostConstruct
	final void init() {
		// We use Java 6's compiler API...
		javac = nuclosJavaCompilerComponent.getJavaCompilerTool();
		if (javac == null) {
			throw new NuclosFatalException("No registered system Java compiler found");
		}
		this.diagnosticListener = new CodeGeneratorDiagnosticListener(locale);
		this.stdFileManager = javac.getStandardFileManager(diagnosticListener, locale, null);
		try {
			// add libs from tomcat
			String catalinaHome = (String) System.getProperties().get("catalina.home");			
			List<File> classpath = new ArrayList<File>();
			if(catalinaHome != null) {
				catalinaHome = catalinaHome + File.separator + "lib";
				classpath.addAll(nuclosJavaCompilerComponent.getLibs(new File(catalinaHome)));
			}
			classpath.addAll(nuclosJavaCompilerComponent.getExpandedSystemParameterClassPath());
			classpath.addAll(nuclosJavaCompilerComponent.getLibs(
					NuclosSystemParameters.getDirectory(NuclosSystemParameters.WSDL_GENERATOR_LIB_PATH)));
			classpath.add(NuclosJavaCompilerComponent.JARFILE);
			stdFileManager.setLocation(StandardLocation.CLASS_PATH, new ArrayList<File>(classpath));
			stdFileManager.setLocation(StandardLocation.SOURCE_OUTPUT, 
					Collections.singleton(nuclosJavaCompilerComponent.getSourceOutputPath()));
			stdFileManager.setLocation(StandardLocation.CLASS_OUTPUT, 
					Collections.singleton(nuclosJavaCompilerComponent.getBuildOutputPath()));
		} catch(IOException e) {
			throw new NuclosFatalException(e);
		}
	}

	synchronized Map<String, byte[]> javac(List<CodeGenerator> generators, boolean saveSrc) throws NuclosCompileException {
		LOG.debug("Compiler Classpath: " + stdFileManager.getLocation(StandardLocation.CLASS_PATH));
		final Set<JavaFileObject> sources = new HashSet<JavaFileObject>();
		for (CodeGenerator generator : generators) {
			if (generator.isRecompileNecessary()) {
				for (JavaFileObject jfo : generator.getSourceFiles()) {
					if (!sources.add(jfo)) {
						if (jfo instanceof JavaSourceAsString) {
							LOG.warn("Duplicate class: " + ((JavaSourceAsString) jfo).getFQName());
						}
						throw new NuclosCompileException("nuclos.compiler.duplicateclasses");
					}
				}
			}
		}
		LOG.info("Execute Java compiler for source files: " + sources);

		if (saveSrc) {
			try {
				saveSrc(generators);
			}
			catch(IOException e1) {
				// Source is saved to disk just for debugging purposes
				LOG.warn("javac failed: " + e1);
			}
		}

		if (sources.size() > 0) {
			List<String> options = Arrays.asList("-g");

			ByteArrayOutputFileManager byteFileManager = new ByteArrayOutputFileManager(stdFileManager);
			CompilationTask task = javac.getTask(null, byteFileManager, diagnosticListener, options, null, sources);

			task.setLocale(locale);

			boolean success = task.call();

			List<ErrorMessage> errors = diagnosticListener.clearErrors();
			if (!success) {
				LOG.info("Compile failed with " + errors.size() + " errors:");
				for (ErrorMessage em: errors) {
					LOG.info(em);
				}
				throw new NuclosCompileException(errors);
			}

			Map<String, byte[]> output;
			try {
				output = byteFileManager.getOutput();
				byteFileManager.flush();
			}
			catch(IOException e) {
				throw new NuclosCompileException(e);
			}
			return output;
		}
		else {
			return new HashMap<String, byte[]>();
		}
	}

	private synchronized void saveSrc(List<CodeGenerator> generators) throws IOException {
		for (CodeGenerator generator : generators) {
			saveSrc(generator, false);
		}
		nuclosJavaCompilerComponent.setLastSrcWriteTime(System.currentTimeMillis());
	}

	void saveSrc(CodeGenerator generator, boolean remove) throws IOException {
		if (generator.isRecompileNecessary()) {
			for (JavaSourceAsString srcobject : generator.getSourceFiles()) {
				final File f = generator.getJavaSrcFile(srcobject);
				if (remove) {
					final boolean success = f.delete();
					if (!success) {
						LOG.warn("Unable to delete " + f);
					}
				}
				else {
					if (!f.exists()) {
						f.getParentFile().mkdirs();
						f.createNewFile();
					}
					final BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f),
							NuclosJavaCompilerComponent.JAVA_SRC_ENCODING));
					try {
						generator.writeSource(out, srcobject);
					}
					finally {
						out.close();
					}
				}
			}
		}		
	}
	
	@Override
	public void close() throws IOException {
		if (stdFileManager != null) {
			stdFileManager.close();
		}
	}

	/**
	 * A diagnostic listener which collects the error messages.  It recognizes {@link GeneratedJavaFileObject}s
	 * and automatically adjust the line and position offsets.
	 */
	static class CodeGeneratorDiagnosticListener implements DiagnosticListener<JavaFileObject> {

		private final Locale locale;
		private final List<NuclosCompileException.ErrorMessage> errors;

		CodeGeneratorDiagnosticListener(Locale locale) {
			this.locale = locale;
			this.errors = new ArrayList<NuclosCompileException.ErrorMessage>();
		}

		@Override
		public synchronized void report(Diagnostic<? extends JavaFileObject> diag) {
			if (diag.getKind() == Diagnostic.Kind.ERROR) {
				JavaFileObject source = diag.getSource();
				String message = getMessageWithoutPath(diag);
				if (message == null || message.isEmpty())
					message = "Unknown error";
				long dl = 0, dp = 0; // line and position deltas


				if (source instanceof JavaSourceAsString) {
					final JavaSourceAsString t = (JavaSourceAsString) source;
					long line = diag.getLineNumber();
					if (line != Diagnostic.NOPOS && source.getKind() == JavaFileObject.Kind.SOURCE) {
						if (message.startsWith(line + ":"))
							message = message.substring((line + ":").length());
						line = shift(line, t.getLineDelta());
					}
					dl = t.getLineDelta();
					dp = t.getPositionDelta();
				}

				String sourcename = source.getName(); // physical or symbolic source name
				String entityname = null;
				Long id = -1L;
				if (source instanceof JavaSourceAsString) {
					JavaSourceAsString jas = (JavaSourceAsString) source;
					sourcename = jas.getLabel();
					entityname = jas.getEntityname();
					id = jas.getId();
				}
				// NPE in errors.add(new ErrorMessage(...
				if (id == null) {
					id = -1L;
				}

				errors.add(new ErrorMessage(diag.getKind(), sourcename, message, entityname, id,
					shift(diag.getLineNumber(), dl), diag.getColumnNumber(),
					shift(diag.getPosition(), dp),
					shift(diag.getStartPosition(), dp),
					shift(diag.getEndPosition(), dp)));
			}
		}

		public synchronized List<NuclosCompileException.ErrorMessage> clearErrors() {
			List<NuclosCompileException.ErrorMessage> result = new ArrayList<NuclosCompileException.ErrorMessage>(errors);
			errors.clear();
			return result;
		}

		private String getMessageWithoutPath(Diagnostic<? extends JavaFileObject> diag) {
			String message = diag.getMessage(locale);
			JavaFileObject source = diag.getSource();
			if (source != null && message != null) {
				String path = source.toUri().getPath();
				if (path != null &&  message.startsWith(path + ":"))
					message = message.substring(path.length() + 1);
				String lineNumber = "" + diag.getLineNumber();
				if (message.startsWith(diag.getLineNumber() + ":"))
					message = message.substring(lineNumber.length() + 1);
				message = message.trim();
			}
			return message;
		}

		private static long shift(long pos, long delta) {
			if (pos != Diagnostic.NOPOS) {
				pos -= delta;
				if (pos < 0)
					pos = 0;
			}
			return pos;
		}
	}

}