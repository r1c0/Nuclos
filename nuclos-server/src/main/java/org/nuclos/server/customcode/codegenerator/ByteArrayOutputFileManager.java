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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;

class ByteArrayOutputFileManager extends ForwardingJavaFileManager<StandardJavaFileManager> {
	
	private final Map<String, ByteArrayOutputJavaFileObject> outputFiles;
	
	public ByteArrayOutputFileManager(StandardJavaFileManager fileManager) {
		super(fileManager);
		this.outputFiles = new LinkedHashMap<String, ByteArrayOutputJavaFileObject>();
	}

	public Map<String, byte[]> getOutput() throws IOException {
		this.flush();
		Map<String, byte[]> output = new LinkedHashMap<String, byte[]>();
		for (Map.Entry<String, ByteArrayOutputJavaFileObject> e : outputFiles.entrySet()) {
			output.put(e.getKey(), e.getValue().getBytes());
		}
		return output;
	}
	
	@Override
	public JavaFileObject getJavaFileForOutput(Location location, String className, Kind kind, FileObject sibling) throws IOException {
		if (kind == Kind.CLASS && location == StandardLocation.CLASS_OUTPUT) {
			String name = className.replace('.', '/') + kind.extension;
			URI uri;
			try {
				uri = new URI("byte://" + name);
			} catch(URISyntaxException ex) {
				throw new IOException(ex);
			}
			ByteArrayOutputJavaFileObject fo = new ByteArrayOutputJavaFileObject(uri, name, kind);
			outputFiles.put(name, fo);
			return fo;
		} else {
			return super.getJavaFileForOutput(location, className, kind, sibling);
		}
	}
	
	private static class ByteArrayOutputJavaFileObject extends SimpleJavaFileObject {

		private final String name;
		volatile ByteArrayOutputStream baos;
		
		public ByteArrayOutputJavaFileObject(URI uri, String name, Kind kind) {
			super(uri, kind);
			this.name = name;
		}
		
		@Override
		public String getName() {
			return name;
		}
		
		@Override
		public OutputStream openOutputStream() throws IOException {
			baos = new ByteArrayOutputStream();
			return baos;
		}
		
		public byte[] getBytes() {
			return baos.toByteArray();
		}
	}
}
