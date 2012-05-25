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

import static org.nuclos.common.collection.Factories.memoizingFactory;
import static org.nuclos.common.collection.Factories.synchronizingFactory;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.log4j.Logger;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.SpringApplicationContextHolder;
import org.nuclos.common.collection.Factory;
import org.nuclos.common2.IOUtils;
import org.nuclos.server.common.NuclosSystemParameters;
import org.nuclos.server.common.RuleCache;
import org.nuclos.server.customcode.NuclosRule;
import org.nuclos.server.customcode.NuclosTimelimitRule;
import org.nuclos.server.customcode.codegenerator.CodeGenerator.JavaSourceAsString;
import org.nuclos.server.customcode.valueobject.CodeVO;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;
import org.nuclos.server.ruleengine.NuclosCompileException;
import org.nuclos.server.ruleengine.ejb3.RuleEngineFacadeBean;
import org.nuclos.server.ruleengine.ejb3.TimelimitRuleFacadeBean;
import org.nuclos.server.ruleengine.valueobject.RuleVO;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Component
public class NuclosJavaCompilerComponent {

	private static final Logger LOG = Logger.getLogger(NuclosJavaCompilerComponent.class);
	
	//

	private static final String JAVAC_CLASSNAME = "com.sun.tools.javac.api.JavacTool";

	static final String JAVA_SRC_ENCODING = "UTF-8";

	public static final File JARFILE = new File(
			NuclosSystemParameters.getDirectory(NuclosSystemParameters.GENERATOR_OUTPUT_PATH), "Nuclet.jar");

	private static final File JARFILE_OLD = new File(
			NuclosSystemParameters.getDirectory(NuclosSystemParameters.GENERATOR_OUTPUT_PATH), "Nuclet.jar.old");

	private static long lastSrcWriteTime = System.currentTimeMillis();

	private static Attributes.Name NUCLOS_CODE_NUCLET = new Attributes.Name("Nuclos-Code-Nuclet");
	private static Attributes.Name NUCLOS_CODE_HASH = new Attributes.Name("Nuclos-Code-Hash");
	
	//
	
	NuclosJavaCompilerComponent() {
		
	}

	public static JavaCompiler getJavaCompilerTool() {
		JavaCompiler tool = ToolProvider.getSystemJavaCompiler();
		if (tool != null)
			return tool;

		// No system Java compiler found, try to locate Javac ourself
		// (maybe we found a "bundled" Javac class on our classpath).
		try {
			Class<?> clazz = Class.forName(JAVAC_CLASSNAME);
			try {
				return (JavaCompiler) clazz.newInstance();
			} catch (Exception ex) {
				LOG.error(ex);
			}
		} catch(ClassNotFoundException e) {
			LOG.warn("getJavaCompilerTool failed: " + e);
		}
		return null;
	}

	public long getLastSrcWriteTime() {
		return lastSrcWriteTime;
	}
	
	synchronized final void setLastSrcWriteTime(long time) {
		lastSrcWriteTime = time;
	}

	/** the output path where generated java and class files are stored */
	public final File getOutputPath() {
		File dir = NuclosSystemParameters.getDirectory(NuclosSystemParameters.GENERATOR_OUTPUT_PATH);
		if (!dir.exists())
			dir.mkdirs();
		return dir;
	}

	final File getSourceOutputPath() {
		File dir = new File(getOutputPath(), "src");
		if (!dir.exists())
			dir.mkdirs();
		return dir;
	}

	final File getWsdlDir() {
		File dir = new File(getOutputPath(), "wsdl");
		if (!dir.exists()) {
			dir.mkdir();
		}
		return dir;
	}

	public final File getBuildOutputPath() {
		File dir = new File(getOutputPath(), "build");
		if (!dir.exists())
			dir.mkdirs();
		return dir;
	}
	
	private synchronized boolean moveJarToOld() {
		boolean oldExists = false;
		if (JARFILE.exists()) {
			JARFILE_OLD.delete();
			oldExists = JARFILE.renameTo(JARFILE_OLD);
			if (JARFILE.exists()) {
				try {
					IOUtils.copyFile(JARFILE, JARFILE_OLD);
					oldExists = true;
				}
				catch (IOException ex) {
					throw new IllegalStateException(ex);
				}
			}
		}
		return oldExists;
	}

	private synchronized void jar(Map<String, byte[]> javacresult, List<CodeGenerator> generators) {
		try {
			final boolean oldExists = moveJarToOld();
			if (javacresult.size() > 0) {
				final Set<String> entries = new HashSet<String>();
				final JarOutputStream jos = new JarOutputStream(
						new BufferedOutputStream(new FileOutputStream(JARFILE)), getManifest());

				try {
					for(final String key : javacresult.keySet()) {
						entries.add(key);
						byte[] bytecode = javacresult.get(key);

						// call postCompile() (weaving) on compiled sources
						for (CodeGenerator generator : generators) {
							if (!oldExists || generator.isRecompileNecessary()) {
								for(JavaSourceAsString src : generator.getSourceFiles()) {
									final String name = src.getFQName();
									if (key.startsWith(name.replaceAll("\\.", "/"))) {
										LOG.debug("postCompile (weaving) " + key);
										bytecode = generator.postCompile(key, bytecode);
										// Can we break here???
										// break outer;
									}
								}
							}
						}
						jos.putNextEntry(new ZipEntry(key));
						LOG.debug("writing to " + key + " to jar " + JARFILE);
						jos.write(bytecode);
						jos.closeEntry();
					}

					if (oldExists) {
						final JarInputStream in = new JarInputStream(
								new BufferedInputStream(new FileInputStream(JARFILE_OLD)));
		                final byte[] buffer = new byte[2048];
						try {
			                int size;
							JarEntry entry;
							while ((entry = in.getNextJarEntry()) != null) {
								if (!entries.contains(entry.getName())) {
									jos.putNextEntry(entry);
									LOG.debug("copying " + entry.getName() + " from old jar " + JARFILE_OLD);
									while ((size = in.read(buffer, 0, buffer.length)) != -1) {
										jos.write(buffer, 0, size);
									}
									jos.closeEntry();
								}
								in.closeEntry();
							}
						}
						finally {
							in.close();
						}
					}
				}
				finally {
					jos.close();
				}
			}
		}
		catch(IOException ex) {
			throw new NuclosFatalException(ex);
		}
	}

	public void compile() throws NuclosCompileException {
		final List<CodeGenerator> generators = getAllCurrentGenerators();
		compile(generators);
	}
	
	private synchronized NuclosJavaCompiler compile(List<CodeGenerator> generators) throws NuclosCompileException {
		final NuclosJavaCompiler c = new NuclosJavaCompiler();
		try {
			jar(c.javac(generators, true), generators);
		}
		finally {
			try {
				c.close();
			}
			catch(IOException e) {
				LOG.warn("getFile failed: " + e, e);
			}
		}
		return c;
	}

	public synchronized void check(CodeGenerator modified, boolean remove) throws NuclosCompileException {
		final List<CodeGenerator> generators = getAllCurrentGenerators();
		int index = generators.indexOf(modified);
		if (index > -1) {
			if (remove) {
				generators.remove(index);
			}
			else {
				generators.set(index, modified);
			}
		}
		else {
			generators.add(modified);
		}
		final NuclosJavaCompiler c = check(generators);
		
		// If check was successful, update source on disk
		try {
			c.saveSrc(modified, remove);
		}
		catch (IOException e) {
			LOG.warn("Update source on disk failed: " + e.toString(), e);
		}
	}
		
	private synchronized NuclosJavaCompiler check(List<CodeGenerator> generators) throws NuclosCompileException {
		final NuclosJavaCompiler c;
		if (JARFILE.exists()) {
			c = new NuclosJavaCompiler();
			try {
				c.javac(generators, false);
			}
			finally {
				try {
					c.close();
				}
				catch(IOException e) {
					LOG.warn("check failed: " + e, e);
				}
			}
		}
		else {
			c = compile(generators);
		}
		return c;
	}

	synchronized void checkSrcOnDisk(List<OnDiskCodeGenerator> modified) throws NuclosCompileException {	
		final List<CodeGenerator> generators = getAllCurrentGenerators();
		for (OnDiskCodeGenerator cg: modified) {
			int index = generators.indexOf(cg);
			if (index > -1) {
				LOG.info("Check/compile java source that changed on disk: " + cg);
				generators.set(index, cg);
			}
			else {
				LOG.warn("Unknown java source on disk: " + cg);
				generators.add(cg);
			}
		}
		check(generators);
	}
	
	/**
	 * Returns the expanded class path for system parameter {@code nuclos.codegenerator.class.path}.
	 * Note: WSDL libraries are not included.
	 */
	public synchronized List<File> getExpandedSystemParameterClassPath() {
		return expandedGeneratorClassPathFactory.create();
	}

	private final Factory<List<File>> expandedGeneratorClassPathFactory =
		synchronizingFactory(memoizingFactory(new Factory<List<File>>() {
			@Override
			public List<File> create() {
				List<File> classPath = new ArrayList<File>();
				Resource r = SpringApplicationContextHolder.getApplicationContext().getResource("WEB-INF/lib/");
				try {
					classPath.addAll(getLibs(r.getFile()));
				} catch (IOException e) {
					throw new NuclosFatalException(e);
				}
				return classPath;
			}
		}));

	List<File> getLibs(File folder) {
		List<File> files = new ArrayList<File>();
		if(!folder.isDirectory()) {
			// just return empty list, compiler will give notice if classes are missing
			return files;
		}

		for(File file : folder.listFiles()) {
			files.add(file);
		}

		return files;
	}

	private List<CodeGenerator> getAllCurrentGenerators() {
		final RuleCache ruleCache = RuleCache.getInstance();
		final List<CodeGenerator> result = new ArrayList<CodeGenerator>();

		if (ruleCache.getWebservices().size() > 0) {
			for (MasterDataVO ws : ruleCache.getWebservices()) {
				final CodeGenerator cg = new WsdlCodeGenerator(ws);
				if (cg.isRecompileNecessary()) {
					result.add(cg);
				}
			}
		}

		if (ruleCache.getCommonCode().size() > 0) {
			for (CodeVO code : ruleCache.getCommonCode()) {
				if (code.isActive()) {
					final CodeGenerator cg = new PlainCodeGenerator(code);
					if (cg.isRecompileNecessary()) {
						result.add(cg);
					}
				}
			}
		}

		if (ruleCache.getAllRules().size() > 0) {
			for (RuleVO rule : ruleCache.getAllRules()) {
				if (rule.isActive()) {
					final CodeGenerator cg = new RuleCodeGenerator<NuclosRule>(
							new RuleEngineFacadeBean.RuleTemplateType(), rule);
					if (cg.isRecompileNecessary()) {
						result.add(cg);
					}
				}
			}
		}

		if (ruleCache.getTimelimitRules().size() > 0) {
			for (RuleVO rule : ruleCache.getTimelimitRules()) {
				if (rule.isActive()) {
					final CodeGenerator cg = new RuleCodeGenerator<NuclosTimelimitRule>(
							new TimelimitRuleFacadeBean.TimelimitRuleCodeTemplate(), rule);
					if (cg.isRecompileNecessary()) {
						result.add(cg);
					}
				}
			}
		}

		return result;
	}

	private Manifest getManifest() {
		HashCodeBuilder builder = new HashCodeBuilder(11, 17);
		for (CodeGenerator gen : getAllCurrentGenerators()) {
			builder.append(gen.hashForManifest());
		}

		Manifest manifest = new Manifest();
		Attributes mainAttributes = manifest.getMainAttributes();
		mainAttributes.put(Attributes.Name.MANIFEST_VERSION, "1.0");
		mainAttributes.put(NUCLOS_CODE_NUCLET, "default");
		mainAttributes.put(NUCLOS_CODE_HASH, String.valueOf(builder.toHashCode()));
		return manifest;
	}

	public synchronized boolean validate() throws NuclosCompileException {
		if (JARFILE.exists()) {
			try {
				JarFile jar = new JarFile(JARFILE);
				if (!jar.getManifest().equals(getManifest())) {
					compile();
				}
				else {
					return false;
				}
			}
			catch(IOException e) {
				LOG.debug("validate: " + e);
				compile();
			}
		}
		else {
			compile();
		}
		return true;
	}

}
