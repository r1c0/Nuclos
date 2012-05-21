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
package org.nuclos.installer.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import org.nuclos.installer.mode.Installer;

public class FileUtils {

	public static List<String> unpack(InputStream resourcefile, File targetDir, Installer i) throws IOException {
		final List<String> result = new ArrayList<String>();
		final ZipInputStream zis = new ZipInputStream(new BufferedInputStream(resourcefile));
		try {
			ZipEntry entry = zis.getNextEntry();
			while (entry != null) {
				String targetname = entry.getName();
				File target = new File(targetDir, targetname);
				result.add(target.getAbsolutePath());
				if (entry.isDirectory()) {
					i.info("info.create.dir", target.getAbsolutePath());
					forceMkdir(target);
				}
				else {
					i.info("info.create.file", target.getAbsolutePath());
					target.getParentFile().mkdirs();
					final OutputStream fos = new BufferedOutputStream(new FileOutputStream(target));
					try {
						int n;
						byte[] buffer = new byte[1024];
			            while ((n = zis.read(buffer, 0, 1024)) > -1) {
			            	fos.write(buffer, 0, n);
			            }
					}
					finally {
						fos.close();
					}
	                zis.closeEntry();
				}
				entry = zis.getNextEntry();
			}
		}
		finally {
			zis.close();
		}
		return result;
	}

	public static void unpackFile(InputStream resourcefile, String name, File targetfile, Installer i) throws IOException {
		ZipInputStream zis = new ZipInputStream(new BufferedInputStream(resourcefile));
		ZipEntry entry = zis.getNextEntry();
		while (entry != null) {
			if (entry.getName().equals(name) && !entry.isDirectory()) {
				i.info("info.create.file", targetfile.getAbsolutePath());
				final OutputStream fos = new BufferedOutputStream(new FileOutputStream(targetfile));

				int n;
				byte[] buffer = new byte[1024];
	            while ((n = zis.read(buffer, 0, 1024)) > -1) {
	            	fos.write(buffer, 0, n);
	            }
                fos.close();
                zis.closeEntry();
                break;
			}
			entry = zis.getNextEntry();
		}
		zis.close();
	}

	public static boolean isEmptyDir(File dir, boolean allowEmptySubdirs) throws IOException {
		if (!dir.isDirectory())
			return false;
		for (File file : dir.listFiles()) {
			if (allowEmptySubdirs && isEmptyDir(file, true))
				continue;
			return false;
		}
		return true;
	}

	public static boolean isEmptyDir(File dir, String...excludes) throws IOException {
		if (!dir.isDirectory())
			return false;
		for (File file : dir.listFiles()) {
			boolean excluded = false;
			if (excludes != null) {
				for (String exclude : excludes) {
					if (file.getName().equals(exclude)) {
						excluded = true;
						break;
					}
				}
			}
			if (!excluded) {
				return false;
			}
		}
		return true;
	}

	public static void renameToBackupName(File file) throws IOException {
		File backupFile = new File(file.getParentFile(), file.getName() + "~");
		if (backupFile.exists()) {
			boolean deleted = backupFile.delete();
			if (!deleted) {
				throw new IOException("Cannot delete backup file " + backupFile);
			}
		}
		boolean renamed = file.renameTo(backupFile);
		if (!renamed) {
			throw new IOException("Cannot create backup file for " + file);
		}
	}

	public static void touch(File file) throws IOException {
		touch(file, false);
	}

	public static void touch(File file, boolean recursive) throws IOException {
		boolean success = false;
		if (file.exists()) {
			success = file.setLastModified(System.currentTimeMillis());
		}
		checkSuccess(success, "Cannot touch file " + file);
	}

	public static void forceMkdir(File dir) throws IOException {
		boolean success = dir.isDirectory();
		if (!success) {
			success = dir.mkdirs();
		}
		checkSuccess(success, "Cannot create directory " + dir);
	}

	public static void copy(InputStream is, OutputStream os) throws IOException {
		try {
			byte[] buf = new byte[10240];
			int len;
			while ((len = is.read(buf)) != -1) {
				os.write(buf, 0, len);
			}
		} finally {
			os.close();
			is.close();
		}
	}

	public static byte[] readAll(File file) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		copy(new BufferedInputStream(new FileInputStream(file)), baos);
		return baos.toByteArray();
	}

	public static void copyInputStreamToFile(InputStream is, File targetFile, boolean backup) throws IOException {
		if (targetFile.exists() && backup) {
			renameToBackupName(targetFile);
		} else if (!targetFile.getParentFile().exists()) {
			forceMkdir(targetFile.getParentFile());
		}
		copy(is, new BufferedOutputStream(new FileOutputStream(targetFile)));
	}

	public static String copyFile(File sourceFile, File targetFile, boolean backup, Installer i) throws IOException {
		i.info("info.create.file", targetFile.getAbsolutePath());
		InputStream is = new BufferedInputStream(new FileInputStream(sourceFile));
		try {
			copyInputStreamToFile(is, targetFile, backup);
			targetFile.setLastModified(sourceFile.lastModified());
		} finally {
			is.close();
		}
		return targetFile.getAbsolutePath();
	}

	public static String copyFile(InputStream is, File targetFile, Installer i) throws IOException {
		i.info("info.create.file", targetFile.getAbsolutePath());
		try {
			copyInputStreamToFile(is, targetFile, false);
		} finally {
			is.close();
		}
		return targetFile.getAbsolutePath();
	}

	public static Map<String, byte[]> getChecksums(File file, Collection<String> algorithms) throws IOException {
		int digestCount = 0;
		MessageDigest[] digests = new MessageDigest[algorithms.size()];
		for (String algorithm : algorithms) {
			try {
				digests[digestCount++] = MessageDigest.getInstance(algorithm);
			} catch (NoSuchAlgorithmException e) {
				// ignore (no entry in result map)
			}
		}
		InputStream is = new BufferedInputStream(new FileInputStream(file));
		try {
			byte[] buf = new byte[10240];
			int len;
			while ((len = is.read(buf)) != -1) {
				for (int i = 0; i < digestCount; i++) {
					digests[i].update(buf, 0, len);
				}
			}
			Map<String, byte[]> result = new HashMap<String, byte[]>();
			for (int i = 0; i < digestCount; i++) {
				result.put(digests[i].getAlgorithm(), digests[i].digest());
			}
			return result;
		} finally {
			is.close();
		}
	}

	public static List<String> copyDirectory(File sourceDir, File targetDir, Installer i) throws IOException {
		List<String> result = new ArrayList<String>();
		i.info("info.create.dir", targetDir.getAbsolutePath());
		File[] files = sourceDir.listFiles();
		checkSuccess(files != null, "Source directory " + sourceDir + " not accessible");
		forceMkdir(targetDir);

		for (File file : files) {
			if (file.isDirectory()) {
				result.addAll(copyDirectory(file, new File(targetDir, file.getName()), i));
			} else if (file.isFile()) {
				result.add(copyFile(file, new File(targetDir, file.getName()), false, i));
			}
		}
		return result;
	}

//	public static void move(File source, File target) throws IOException {
//		if (target.exists()) {
//			throw new IOException("Target directory " + target.getAbsolutePath() + " already exist");
//		}
//		if (source.isFile()) {
//			boolean moved = source.renameTo(target);
//			if (!moved) {
//				copyFile(source, target, false);
//				delete(source, false);
//			}
//		} else if (source.isDirectory()) {
//			forceMkdir(target);
//			for (File child : source.listFiles()) {
//				move(child, new File(target, child.getName()));
//			}
//		}
//	}

	public static void delete(File file, boolean recursive) throws IOException {
		if (file.exists()) {
			if (file.isDirectory() && recursive) {
				for (File childFile : file.listFiles()) {
					delete(childFile, recursive);
				}
			}
			if (!file.delete()) {
				throw new IOException("Cannot delete file " + file);
			}
		}
	}

	public static void unzip(File archiveFile, File targetDir, boolean skipRoot) throws IOException {
		forceMkdir(targetDir);
		ZipFile zipFile = new ZipFile(archiveFile);
		Enumeration<? extends ZipEntry> entries = zipFile.entries();
		while (entries.hasMoreElements()) {
			ZipEntry entry = entries.nextElement();
			File entryTarget;
			if(skipRoot) {
				String name = entry.getName();
				name = name.substring(name.indexOf('/') + 1);
				if(name.length() == 0)
					entryTarget = targetDir;
				else
					entryTarget = new File(targetDir, name);
			} else {
				entryTarget = new File(targetDir, entry.getName());
			}
			unzipEntry(zipFile, entry, entryTarget);
		}
		zipFile.close();
	}

	public static void unzipEntry(ZipFile zipFile, ZipEntry entry, File entryTarget) throws IOException {
		if (entry.isDirectory()) {
			forceMkdir(entryTarget);
		} else {
			InputStream is = zipFile.getInputStream(entry);
			copyInputStreamToFile(is, entryTarget, false);
		}
	}

	public static void setExecutable(File f, final String pattern) {
		if (f.isDirectory()) {
			File[] files = f.listFiles(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					return Pattern.matches(pattern, name);
				}
			});
			for (File bin : files) {
				bin.setExecutable(true);
			}
		}
		else {
			f.setExecutable(true);
		}
	}

	public static Manifest extractManifest(File jarFile) throws IOException {
		JarFile jar = new JarFile(jarFile);
		try {
			return jar.getManifest();
		} finally {
			jar.close();
		}
	}

	private static void checkSuccess(boolean success, String message) throws IOException {
		if (!success) {
			throw new IOException(message);
		}
	}

	public static List<String> getFiles(File dir, File...excludes) {
		List<String> result = new ArrayList<String>();
		File[] aFiles = dir.listFiles();
		List<File> files = Arrays.asList(aFiles);
		for (File file : files) {
			boolean exclude = false;
			for (File f : excludes) {
				if (file.getAbsolutePath().startsWith(f.getAbsolutePath())) {
					exclude = true;
					break;
				}
			}
			if (!exclude) {
				result.add(file.getAbsolutePath());
				if (!file.isFile()) {
					List<String> deeperList = getFiles(file, excludes);
					result.addAll(deeperList);
				}
			}
		}
		return result;
	}
}
