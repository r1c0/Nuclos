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
package org.nuclos.server.report.print;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintException;
import javax.print.PrintService;
import javax.print.SimpleDoc;
import javax.print.attribute.PrintRequestAttributeSet;

import org.nuclos.server.report.NuclosReportPrintJob;

/**
 * Exporter which prints MS-Word documents.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author <a href="mailto:uwe.allner@novabit.de">Uwe.Allner</a>
 * @version 01.00.00
 */
public class DOCPrintJob extends NuclosReportPrintJob {

	@Override
	public void print(PrintService prserv, String sFilename,
			PrintRequestAttributeSet aset) throws PrintException, IOException {
		
		InputStream fis = null;
		try {
			DocPrintJob pj = prserv.createPrintJob();
	        fis = new BufferedInputStream(new FileInputStream(sFilename));
	        pj.print(new SimpleDoc(fis, DocFlavor.INPUT_STREAM.AUTOSENSE, null), (PrintRequestAttributeSet) aset);
		} catch (Exception e) {
			throw new PrintException(e.getMessage());
		}
		finally {
			if (fis != null) {
				fis.close();
			}
		}
	}
}	// class DOCExport
