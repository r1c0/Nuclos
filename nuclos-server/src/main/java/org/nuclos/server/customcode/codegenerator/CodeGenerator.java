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
import java.io.IOException;
import java.io.Writer;
import java.net.URI;

import javax.tools.SimpleJavaFileObject;

/**
 * Interface to be implemented for generating code from anything.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 */
public interface CodeGenerator {
	
	boolean isRecompileNecessary();

	Iterable<? extends JavaSourceAsString> getSourceFiles();

	byte[] postCompile(String name, byte[] bytecode);
	
	String getPrefix();
	
	int getPrefixAndHeaderLineCount();
	
	int getPrefixAndHeaderOffset();
	
	File getJavaSrcFile(JavaSourceAsString src);
	
	void writeSource(Writer writer, JavaSourceAsString src) throws IOException;
	
	/**
	 * hashForManifest() *must* return the same value for the same content of 
	 * source file(s) only. This is in contrast to hashCode()!!! (tp)
	 */
	int hashForManifest();
	
	/**
	 * hashCode() *must* return the same value for same (fully-qualified) class name to ensure
	 * that generator is changed when doing on disk source scanning. (tp)
	 */
	int hashCode();
	
	/**
	 * equals() *must* return true for same (fully-qualified) class name to ensure
	 * that generator is changed when doing on disk source scanning. (tp)
	 */
	boolean equals(Object other);

	public static class JavaSourceAsString extends SimpleJavaFileObject {

		private final String name;
		private final String prefix;
		private final String source;
		private final String entityname;
		private final Long id;

		private final long lineDelta;
		
		private final long posDelta;

		protected JavaSourceAsString(String name, String prefix, String source, 
				String entity, Long id, long lineDelta, long posDelta) {
			
			super(URI.create(name.replace('.', '/') + Kind.SOURCE.extension), Kind.SOURCE);
			this.name = name;
			this.prefix = prefix;
			this.source = source;
			this.entityname = entity;
			this.id = id;
			this.lineDelta = lineDelta;
			this.posDelta = posDelta;
		}
		
		public String getPrefix() {
			return prefix;
		}
		
		public String getSource() {
			return source;
		}

		public long getLineDelta() {
			return lineDelta;
		}

		public long getPositionDelta() {
			return posDelta;
		}

		@Override
		public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
			return prefix + source;
		}

		@Override
		public String getName() {
			String filename;
			if (name.contains(".")) {
				filename = name.substring(name.lastIndexOf('.') + 1);
			}
			else {
				filename = name;
			}
			return filename + Kind.SOURCE.extension;
		}

		public String getFQName() {
			return name;
		}

		// Note: toString() should be meaningless because it's not part of the public JSR199 Compiler API;
		// However, toString() is misused by Sun's javac for deriving the classfile's [SourceFile]
		// attribute (cf. JVM spec 4.8.10 and com.sun.tools.javac.jvm.ClassWriter#writeClassFile) which
		// in turn is used by debuggers for source code lookup.  Hence, a wrong value will confuse debuggers.
		// This misusing behaviour is prevalent in JDK6 (at least up to 6u21), but seems to be fixed in
		// (Open)JDK's head:
		// http://hg.openjdk.java.net/jdk6/jdk6/langtools/diff/5c2858bccb3f/src/share/classes/com/sun/tools/javac/jvm/ClassWriter.java
		@Override
		public String toString() {
			return getName();
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((name == null) ? 0 : name.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if(this == obj)
				return true;
			if(obj == null)
				return false;
			if(getClass() != obj.getClass())
				return false;
			JavaSourceAsString other = (JavaSourceAsString) obj;
			if(name == null) {
				if(other.name != null)
					return false;
			}
			else if(!name.equals(other.name))
				return false;
			return true;
		}

		public String getPath() {
			return name.replace('.', '/') + Kind.SOURCE.extension;
		}

		public String getLabel() {
			return getName();
		}

		public String getEntityname() {
			return this.entityname;
		}

		public Long getId() {
			return this.id;
		}
	}
}
