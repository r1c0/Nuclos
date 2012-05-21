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
package org.nuclos.server.transfer;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.nuclos.common2.IOUtils;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.server.common.MasterDataMetaCache;

/**
 * Helper class for XML Export and Import.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:fabian.kastlunger@novabit.de">fabian.kastlunger</a>
 * @version 01.00.00
 */
public class XmlExportImportHelper {

	public static final String EXPIMP_TYPE_EXPORT = "Export";
	public static final String EXPIMP_TYPE_IMPORT = "Import";
	
	public static final String EXPIMP_MESSAGE_LEVEL_INFO = "INFO";
	public static final String EXPIMP_MESSAGE_LEVEL_WARNING = "WARNING";
	public static final String EXPIMP_MESSAGE_LEVEL_ERROR = "ERROR";
	
	public static final String EXPIMP_ACTION_INSERT = "INSERT";
	public static final String EXPIMP_ACTION_UPDATE = "UPDATE";
	public static final String EXPIMP_ACTION_DELETE = "DELETE";
	public static final String EXPIMP_ACTION_READ = "READ";
	
	/**
 * deletes all Files and Subfolders in a directory
 * @param dir target directory
 * @return
 */
	public static boolean deleteDir(File dir) {
		if (dir.isDirectory()) {
			String[] entries = dir.list();
			for (int x = 0; x < entries.length; x++) {
				File aktFile = new File(dir.getPath(), entries[x]);
				deleteDir(aktFile);
			}
			if (dir.delete()) {
				return true;
			}
			else {
				return false;
			}
		}
		else {
			if (dir.delete()) {
				return true;
			}
			else {
				return false;
			}
		}
	}
	
	/**
	 * Copys a document file from one directory to another
	 * @param documentPath
	 * @param destPath
	 * @param filename
	 * @param entityId src file Entity ID
	 * @param newEntityId dest file Entity ID
	 * @throws IOException
	 */
	public static void copyDocumentFile(File documentDir, File destDir, String filename, Object entityId, Object newEntityId)
	throws IOException {
		File src = new File(documentDir, filename);
		File dest = new File(destDir, filename);
		if (dest.exists()) dest.delete();
		
	    byte[] buffer = new byte[4096];
	    int read = 0;
	    InputStream in = null;
	    OutputStream out = null;
	    try {
	        in = new BufferedInputStream(new FileInputStream(src));
	        out = new BufferedOutputStream(new FileOutputStream(dest));
	        while(true) {
	            read = in.read(buffer);
	            if (read == -1) {
	                // -1 bedeutet EOF
	                break;
	            }
	            out.write(buffer, 0, read);
	        }
	    } finally {
	        if (in != null) {
	            try {
	                in.close();
	            }
	            finally {
	                if (out != null) {
	                    out.close();
	                }
	            }
	        }
	    }
	}
	
	public static void writeObjectToFile(Object oObject, File destDir, String filename) throws IOException {
		File dest = new File(destDir, filename);
		if (dest.exists()) dest.delete();

		OutputStream outStream = null;
		try {
			outStream = new BufferedOutputStream(new FileOutputStream(dest)); 
			outStream.write((byte[])oObject);
		}
		finally {
			if (outStream != null) {
				outStream.close();
			}
		}
	}

	public static Object readObjectFromFile(File destDir, String filename) throws IOException, ClassNotFoundException {
		File src = new File(destDir, filename);
		return IOUtils.readFromBinaryFile(src);
	}
	
	/**
	 * coverts a File into novabit container file (serializeable)
	 * @param path
	 * @param filename
	 * @return
	 * @throws IOException
	 */
	public static org.nuclos.common2.File createFile (String path, String filename)
	throws IOException{
		File file = new File(path+"/"+filename); 
		// File length
		int size = (int)file.length();
		
		byte[] bytes = new byte[size]; 
		DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(file))); 
		int read = 0;
		int numRead = 0;
		
		while (read < bytes.length && (numRead=dis.read(bytes, read,
				bytes.length-read)) >= 0) {
			read = read + numRead;
		}
		dis.close();
		return( new org.nuclos.common2.File(filename,bytes));
	}

	/**
	 * creates azip file from an directory with all subfolders
	 * @param dir
	 * @param zipFileName
	 * @throws IOException
	 */
	public static void createZipFile(String dir, String zipFileName)
			throws IOException {
		String dirFile = dir +"/"+ zipFileName;
		ZipOutputStream zipOut = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(
				dirFile)));
		try {
			zipDir(dir, zipOut, zipFileName);
		}
		finally {
			zipOut.close();
		}
	}
	
	/**
	 * zips a directory
	 * @param dir2zip
	 * @param zipOut
	 * @param zipFileName
	 * @throws IOException
	 */
	private static void zipDir(String dir2zip, ZipOutputStream zipOut, String zipFileName)
	throws IOException {
		File zipDir = new File(dir2zip); 
		// get a listing of the directory content
		String[] dirList = zipDir.list(); 
		byte[] readBuffer = new byte[2156]; 
		int bytesIn = 0; 
		// loop through dirList, and zip the files
		for(int i=0; i<dirList.length; i++) { 
			File f = new File(zipDir, dirList[i]); 
			if(f.isDirectory()) { 
				// if the File object is a directory, call this
				// function again to add its content recursively
				String filePath = f.getPath(); 
				zipDir(filePath, zipOut,zipFileName); 
				// loop again
				continue; 
			}
			
			if(f.getName().equals(zipFileName)){
				continue;
			}
			// if we reached here, the File object f was not a directory
			// create a FileInputStream on top of f
			final InputStream fis = new BufferedInputStream(new FileInputStream(f));
			try {
				ZipEntry anEntry = new ZipEntry(f.getPath().substring(dir2zip.length()+1)); 
				// place the zip entry in the ZipOutputStream object
				zipOut.putNextEntry(anEntry); 
				// now write the content of the file to the ZipOutputStream
				while((bytesIn = fis.read(readBuffer)) != -1) { 
					zipOut.write(readBuffer, 0, bytesIn); 
				}
			}
			finally {
				// close the Stream
				fis.close(); 
			}
		} 
	}
	
	/**
	 * extracts a zip file to the given dir
	 * @param archive
	 * @param destDir
	 * @throws IOException 
	 * @throws ZipException 
	 * @throws Exception
	 */
	public static void extractZipArchive(File archive, File destDir)
	throws ZipException, IOException {
		if (!destDir.exists()) {
			destDir.mkdir();
		}

		ZipFile zipFile = new ZipFile(archive);
		Enumeration<? extends ZipEntry> entries = zipFile.entries();

		byte[] buffer = new byte[16384];
		int len;
		while (entries.hasMoreElements()) {
			ZipEntry entry = entries.nextElement();

			String entryFileName = entry.getName();

			File dir = buildDirectoryHierarchyFor(entryFileName, destDir);
			if (!dir.exists()) {
				dir.mkdirs();
			}

			if (!entry.isDirectory()) {
				BufferedOutputStream bos = new BufferedOutputStream(
						new FileOutputStream(new File(destDir, entryFileName)));

				BufferedInputStream bis = new BufferedInputStream(zipFile
						.getInputStream(entry));

				while ((len = bis.read(buffer)) > 0) {
					bos.write(buffer, 0, len);
				}

				bos.flush();
				bos.close();
				bis.close();
			}
		}
	}
	
	private static File buildDirectoryHierarchyFor(String entryName, File destDir) {
		int lastIndex = entryName.lastIndexOf('/');
		entryName.substring(lastIndex + 1);
		String internalPathToEntry = entryName.substring(0, lastIndex + 1);
		return new File(destDir, internalPathToEntry);
	}
	
	static public File zipFolder(File srcFolder, String destZipFile)
	throws Exception {
	    final File file = new File(srcFolder, destZipFile);
	    final ZipOutputStream zip = new ZipOutputStream(
	    		new BufferedOutputStream(new FileOutputStream(file)));
	    try {
	    	addFolderToZip("", srcFolder, zip, destZipFile);
	    }
	    finally {
	    	zip.close();
	    }
	    return file;
	  }

	  static private void addFileToZip(String path, File srcFile, ZipOutputStream zip,String destZipFile)
	  throws Exception {
		  if (srcFile.isDirectory()) {
			  addFolderToZip(path, srcFile, zip, destZipFile);
		  }
		  else if (!srcFile.getName().equals(destZipFile)){
			  byte[] buf = new byte[1024];
			  int len;
			  final InputStream in = new BufferedInputStream(new FileInputStream(srcFile));
			  try {
				  if (path.equals("/")) {
					  zip.putNextEntry(new ZipEntry(srcFile.getName()));
				  }
				  else {
					  zip.putNextEntry(new ZipEntry(path + "/" + srcFile.getName()));
				  }
				  while ((len = in.read(buf)) > 0) {
					  zip.write(buf, 0, len);
				  }
			  }
			  finally {
				  in.close();
			  }
		  }
	  }

	  static private void addFolderToZip(String path, File srcFolder, ZipOutputStream zip,String destZipFile)
	  throws Exception {
	    for (String fileName : srcFolder.list()) {
	      if (path.equals("")) {
	        addFileToZip("/", new File(srcFolder, fileName), zip,destZipFile);
	      }
	      else {
	        addFileToZip(srcFolder.getName(), new File(srcFolder, fileName), zip,destZipFile);
	      }
	    }
	  }
	  
	  /**
	   * @return the name of the foreign key field referencing the parent entity
	   * @postcondition result != null
	   */
	  public static final String getForeignKeyFieldName(String sParentEntityName, String sForeignKeyFieldToParent, String sChildEntityName) {
		  // Use the field referencing the parent entity from the subform, if any:
		  String result = sForeignKeyFieldToParent;
		  
		  if (result == null) {
			  // Default: Find the field referencing the parent entity from the meta data.
			  // If more than one field applies, throw an exception:
			  Set<String> stFieldNames = MasterDataMetaCache.getInstance().getMetaData(sChildEntityName).getFieldNames();
			  
			  for (String sFieldName : stFieldNames) {
				  if (sParentEntityName.equals(MasterDataMetaCache.getInstance().getMetaData(sChildEntityName).getField(sFieldName).getForeignEntity())) {
					  if (result == null) {
						  // this is the foreign key field:
						  result = sFieldName;
					  }
					  else {
						  final String sMessage = "Das Unterformular f\u00fcr die Entit\u00e4t \"" + sChildEntityName +
						  "\" enth\u00e4lt mehr als ein Fremdschl\u00fcsselfeld, das die \u00fcbergeordnete Entit\u00e4t \"" + sParentEntityName + "\" referenziert:\n" +
						  "\t" + result + "\n" +
						  "\t" + sFieldName + "\n" +
						  "Bitte geben Sie das Feld im Layout explizit an.";
						  throw new CommonFatalException(sMessage);
					  }
				  }
			  }
		  }
		  
		  if (result == null) {
			  throw new CommonFatalException("Das Unterformular f\u00fcr die Entit\u00e4t \"" + sChildEntityName +
					  "\" enth\u00e4lt kein Fremdschl\u00fcsselfeld, das die \u00fcbergeordnete Entit\u00e4t \"" + sParentEntityName + "\" referenziert." + 
			  "\nBitte geben Sie das Feld im Layout explizit an.");
		  }
		  assert result != null;
		  return result;
	  }
}
