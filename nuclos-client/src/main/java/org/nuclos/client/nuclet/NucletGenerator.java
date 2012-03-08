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
package org.nuclos.client.nuclet;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.nuclos.client.ui.Errors;

public class NucletGenerator {
	
	public NucletGenerator() {
		
	}
	
	/**
	 * use File Chooser
	 */
	public void generateNucletFromXLSX() {
		final JFileChooser filechooser = new JFileChooser();
		final FileFilter filefilter = new FileFilter() {
			@Override
			public boolean accept(File file) {
				return file.isDirectory() || file.getName().toLowerCase().endsWith("xlsx");
			}
			@Override
			public String getDescription() {
				return "Microsoft Excel (xlsx)";
			}
		};
		filechooser.addChoosableFileFilter(filefilter);
		filechooser.setFileFilter(filefilter);
		
		final int iBtn = filechooser.showOpenDialog(null);

		if (iBtn == JFileChooser.APPROVE_OPTION) {
			final File file = filechooser.getSelectedFile();
			if (file != null) {
				try {
					generateNucletFromXLSX(new FileInputStream(file));
				} catch (FileNotFoundException e) {
					Errors.getInstance().showExceptionDialog(null, e);
				}
			}
		}
	}
	
	
	public void generateNucletFromXLSX(String xslxFile) {
		try {
			generateNucletFromXLSX(new FileInputStream(xslxFile));
		} catch (FileNotFoundException e) {
			Errors.getInstance().showExceptionDialog(null, e);
		}
	}

	
	public void generateNucletFromXLSX(InputStream xlsxFile) {

		try { 
			 
			XSSFWorkbook workbook = new XSSFWorkbook(xlsxFile);
						
		} catch (IOException e) {
			Errors.getInstance().showExceptionDialog(null, e);
		} 
		
		
	}
	
	
}
