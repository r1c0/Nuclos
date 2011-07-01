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
package org.nuclos.tools.licensemaker;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class LicenseMaker {

	String licenseInformation;
	String firstLine = null;

	/**
	 * 
	 * @param licenseFile
	 * @param mainDir
	 */
	public LicenseMaker(String licenseFile, String mainDir) {
		File mainFolder = new File(mainDir);

		if (!mainFolder.exists())
			System.exit(1);

		try {
			licenseInformation = getLicenseInformation(licenseFile);
		} catch (IOException e) {
			e.printStackTrace();
		}

		getJavaFiles(mainFolder);
		System.out.println("License Information hinzugef\u00fcgt");
	}

	/**
	 * reads the file with the license text
	 * 
	 * @param licenseFile
	 *            the file to read
	 * @return the text of the file
	 * @throws IOException
	 *             if something went wrong during reading
	 */
	public String getLicenseInformation(String licenseFile) throws IOException {
		File license = new File(licenseFile);
		if (!license.exists())
			System.exit(2);

		BufferedReader originalFile = new BufferedReader(
				new FileReader(license));
		StringBuffer buffer = new StringBuffer();
		String s = "";
		int lineCount = 0;
		while ((s = originalFile.readLine()) != null) {
			if (firstLine == null)
				firstLine = new String(s);
			buffer.append(s + "\n");
			lineCount++;
		}
		originalFile.close();

		return buffer.toString();
	}

	/**
	 * 
	 * @param sourceFolder
	 */
	public void getJavaFiles(File sourceFolder) {
		File[] subfolders = sourceFolder.listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				if (pathname.isDirectory())
					return true;
				return false;
			}
		});
		File[] javafiles = sourceFolder.listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				if (pathname.getName().endsWith("java"))
					return true;
				return false;
			}
		});

		for (File subfolder : subfolders)
			getJavaFiles(subfolder);

		for (File sourceFile : javafiles) {
			try {
				writeLicenseInformationToFile(sourceFile);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 
	 * @param sourceFile
	 * @throws IOException
	 */
	public void writeLicenseInformationToFile(File sourceFile)
			throws IOException {
		String fileName = new String(sourceFile.getName());
		String path = new String(sourceFile.getParentFile().getPath());
		sourceFile.renameTo(new File(path + "/ORIGINAL_" + fileName));
		sourceFile = new File(path + "/ORIGINAL_" + fileName);
		BufferedReader originalFile = new BufferedReader(new FileReader(
				sourceFile));
		File modifiedFile = new File(path + "/" + fileName);
//		System.out.println(sourceFile.getAbsolutePath() + " -> "+ modifiedFile.getAbsolutePath());
		BufferedWriter out = new BufferedWriter(new FileWriter(modifiedFile));
		String s = "";
		out.write(licenseInformation);
		boolean hasLicenseText = false;
		while ((s = originalFile.readLine()) != null && !hasLicenseText) {
			if (firstLine.equals(s))
				hasLicenseText = true;
			out.write(s);
			out.newLine();
		}

		originalFile.close();
		out.close();

		if (hasLicenseText) {
			System.out.println("[AKTUELL] " + modifiedFile.getAbsolutePath());
			modifiedFile.delete();
			sourceFile.renameTo(new File(path + "/" + fileName));
		} else {
			System.out.println("[UPDATE] " + modifiedFile.getAbsolutePath());
			sourceFile.delete();
		}
	}

}
