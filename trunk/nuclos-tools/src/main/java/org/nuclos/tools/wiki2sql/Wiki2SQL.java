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
package org.nuclos.tools.wiki2sql;

import java.io.*;
import java.util.*;

/**
 * Converts Wiki table description entries to (Oracle) SQL comment statements.
 * Wiki files must be in one directory and must follow this structure:
 * <code>
 * !!!table name
 * table comment; free text
 * || some table header entries
 * | column name | data type | not null | description [ | internal comment [ |... ]]
 *
 * Only allowed format tags are '[', ']', ' \\'
 *
 * Output is generated into one sql file.
 *
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 * @author	<a href="mailto:uwe.allner@novabit.de">uwe.allner</a>
 * @version 01.00.00
 */
public class Wiki2SQL {
	private static Set<String> setTablesWithoutStandardFields;
	private static Set<String> setTablesWithoutIntid;

	private static void init() {
		setTablesWithoutStandardFields = new HashSet<String>(10);
		setTablesWithoutStandardFields.add("T_UD_GO_ATTRIBUTE");
		setTablesWithoutStandardFields.add("T_UD_DATASOURCEUSAGE");
		setTablesWithoutStandardFields.add("T_AD_MODULE_SEQUENTIALNUMBER");

		setTablesWithoutIntid = new HashSet<String>(5);
		setTablesWithoutIntid.add("T_AD_MODULE_SEQUENTIALNUMBER");
	}

	public static void main(String[] args) {
		if (args.length < 2) {
			System.out.println(getUsage());
			System.exit(-1);
		}
		else {
			String sInputDir = args[0];
			String sOutputFile = args[1];
			FileWriter fwOut = null;
			try {
				final File fileInputDir = testForNonEmptyDirectory(sInputDir);
				Collection<String> collFiles = gatherFiles(fileInputDir);
				final File fileOutput = new File(sOutputFile);
				fwOut = new FileWriter(fileOutput);
				fwOut.write("-- The contents of this file are created automatically\n-- Do not change them here!");
				init();
				parseFiles(collFiles, fwOut);
			}
			catch (ToolException ex) {
				System.out.println(ex.getStackTrace());
			}
			catch (IOException ex) {
				System.out.println(ex.getStackTrace());
			}
			finally {
				if (fwOut != null) {
					try {
						fwOut.close();
					}
					catch (IOException ex) {
						System.out.println(ex.getStackTrace());
					}
				}
			}
		}
	}

	private static void parseFiles(Collection<String> collFiles, FileWriter fwOut) throws IOException {
		for (Iterator<String> iterator = collFiles.iterator(); iterator.hasNext();) {
			String sTable = iterator.next();
			String sTableName = "";
			String sColumnName = "";
			String sComment = "";
			StringTokenizer stLines = new StringTokenizer(sTable, "\n");

			boolean bBreakDueToInvalidDescription = false;

			while (stLines.hasMoreElements()) {
				String sLine = stLines.nextToken().trim();

				// Found a page which has to be deleted in the Wiki
				if (sLine.startsWith("[DELETE")) {
					bBreakDueToInvalidDescription = true;
				}

				if (sLine.startsWith("!!!")) {
					sTableName = sLine.substring(3);

					if (bBreakDueToInvalidDescription) {
						fwOut.write("-- ACHTUNG! Kommentar zu nicht mehr vorhandener Tabelle gefunden!: " + sTableName);
						break;
					}
				}
				else if (sLine.startsWith("||")) {
					// ignore header line; only write table comment
					writeComment(sTableName, null, sComment, fwOut);
					sComment = "";
				}
				else if (sLine.startsWith("|")) {
					StringTokenizer stCol = new StringTokenizer(sLine, "|");
					sColumnName = stCol.nextToken().trim();
					stCol.nextToken();	// ignore data type for now
					stCol.nextToken();	// ignore not null for now
					if (stCol.hasMoreElements()) {
						sComment = stCol.nextToken().trim();
					}

					writeComment(sTableName, sColumnName, sComment, fwOut);

					sComment = "";
				}
				else {
					// Should be a table comment line
					sComment += sLine;
				}
			}

		}
	}

	private static void writeComment(String sTableName, String sColumnName, String sComment, FileWriter fwOut) throws IOException {
		if (sComment.trim().length() == 0 || sComment.equals("...")) {
			// No comment; something is missing!
			fwOut.write("\n-- ACHTUNG! Kommentar fehlt bei " + sTableName + ((sColumnName != null) ? "." + sColumnName : "") + "\n");
			return;
		}

		sComment = sComment.replaceAll("\\[", "");
		sComment = sComment.replaceAll("\\]", "");
		sComment = sComment.replaceAll("\\\\", "");

		String sOutput = "";
		if (sColumnName == null) {
			// Generate table comment with standard fields
			if (!setTablesWithoutIntid.contains(sTableName)) {
				sOutput = "\n-- " + sTableName + "\nCOMMENT ON TABLE NUCLEUS." + sTableName + " IS '" + sComment + "';\n";
				sOutput += "COMMENT ON COLUMN NUCLEUS." + sTableName + ".INTID IS 'Interner Identifizierer; Primary key.';\n";
			}

			if (!setTablesWithoutStandardFields.contains(sTableName)) {
				sOutput +=
						"COMMENT ON COLUMN NUCLEUS." + sTableName + ".DATCREATED IS 'Datum der Erzeugung dieses Datensatzes; muss in Init-Skripten SYSDATE sein.';\n" +
								"COMMENT ON COLUMN NUCLEUS." + sTableName + ".STRCREATED IS 'Loginname des Erzeugers dieses Datensatzes; muss in Init-Skripten \u0092INITIAL\u0092 sein.';\n" +
								"COMMENT ON COLUMN NUCLEUS." + sTableName + ".DATCHANGED IS 'Datum der letzten \u00c4nderung dieses Datensatzes; muss in Init-Skripten SYSDATE sein.';\n" +
								"COMMENT ON COLUMN NUCLEUS." + sTableName + ".STRCHANGED IS 'Loginname des letzten \u00c4nderers dieses Datensatzes; muss in Init-Skripten \u0092INITIAL\u0092 sein.';\n" +
								"COMMENT ON COLUMN NUCLEUS." + sTableName + ".INTVERSION IS 'Versionsnummer f\u00fcr optimistisches Locking (Version Pattern); wird in Init-Skripten auf 1 gesetzt und bei jedem Speichern des Datensatzes um 1 erh\u00f6ht.';\n";
			}
		}
		else {
			sOutput = "COMMENT ON COLUMN NUCLEUS." + sTableName + "." + sColumnName + " IS '" + sComment + "';\n";
		}

		fwOut.write(sOutput);
	}

	private static File testForNonEmptyDirectory(String sOutputDir) throws ToolException {
		final File fileOutputDir = testForDirectory(sOutputDir, false);

		if (fileOutputDir.listFiles().length == 0) {
			throw new ToolException("The input directory is empty.");
		}
		return fileOutputDir;
	}

	private static File testForDirectory(String sOutputDir, boolean bCreate) throws ToolException {
		final File fileOutputDir = new File(sOutputDir);
		if (!fileOutputDir.exists() && bCreate) {
			fileOutputDir.mkdir();
		}

		if (!fileOutputDir.isDirectory()) {
			throw new ToolException("The specified path does not denote a directory.");
		}
		return fileOutputDir;
	}

	/**
	 * Gather all necessary files to process, and read their contents.
	 * @param fileInputDir
	 * @return Collection<String> of file contents
	 * @throws IOException
	 */
	private static Collection<String> gatherFiles(File fileInputDir) throws IOException {
		final Collection<String> result = new ArrayList<String>();

		final File[] afile = fileInputDir.listFiles(new FileFilter() {
			@Override
			public boolean accept(File file) {
				final String sFileName = file.getName();

				final int iDotPosition = sFileName.lastIndexOf('.');
				if (iDotPosition > 0) {
					if (".txt".equalsIgnoreCase(sFileName.substring(iDotPosition))) {
						if (sFileName.startsWith("T_") && iDotPosition > 4) {
							// All files ending on ".txt",
							// with a name beginning with "T_" longer than 4 characters (excluding category descriptions)
							return true;
						}
					}
				}

				return false;
			}
		});

		// Read the files from the filtered list
		for (File file : afile) {
			final String sFileSource = readFromTextFile(file);
			result.add(sFileSource);
		}
		return result;
	}

	/**
	 * reads the contents of a text file, using the default encoding.
	 *
	 * @param file a File that must have a size < 2GB.
	 * @return a String containing the contents of the file.
	 * @throws java.io.IOException
	 */
	public static String readFromTextFile(File file) throws IOException {
		final StringBuffer sb = new StringBuffer();
		final BufferedReader br = new BufferedReader(new FileReader(file));

		try {
			String sRead;
			while ((sRead = br.readLine()) != null) {
				if (sRead.length() > 0) {
					sb.append(sRead);
					sb.append('\n');
				}
			}
		}
		finally {
			br.close();
		}
		return sb.toString();
	}

	private static String getUsage() {
		final StringBuffer sb = new StringBuffer();

		sb.append("Usage: Wiki2SQL <inputDirectory> <outputFile>\n");

		return sb.toString();
	}
}

class ToolException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ToolException(String msg) {
		super(msg);
	}
}
