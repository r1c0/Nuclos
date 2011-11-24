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
package org.nuclos.client.report.reportrunner.export;

import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.ServiceUI;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperPrint;

import org.apache.log4j.Logger;
import org.nuclos.client.report.ReportDelegate;
import org.nuclos.client.report.reportrunner.AbstractReportExporter;
import org.nuclos.common2.CommonLocaleDelegate;
import org.nuclos.server.report.NuclosReportException;
import org.nuclos.server.report.NuclosReportPrintJob;
import org.nuclos.server.report.NuclosReportRemotePrintService;
import org.nuclos.server.report.print.PDFPrintJob;
import org.nuclos.server.report.valueobject.ReportOutputVO;
import org.nuclos.server.report.valueobject.ResultVO;
/**
 * Exporter which creates Adobe PDF-files. <br>
 * <br>
 * Created by Novabit Informationssysteme GmbH <br>
 * Please visit <a href="http://www.novabit.de">www.novabit.de </a>
 *
 * @author <a href="mailto:uwe.allner@novabit.de">Uwe.Allner</a>
 * @version 01.00.00
 * @todo don't extend AbstractReportExporter
 */
public class PDFExport extends AbstractReportExporter {

	private static final Logger LOG = Logger.getLogger(PDFExport.class);

	/**
	 * @param sReportName
	 * @param printObj
	 * @param parameter
	 * @throws NuclosReportException
	 */
	public void export(String sReportName, JasperPrint printObj,
			String parameter, ReportOutputVO.Destination destination) throws NuclosReportException {

		final String sDir = createExportDir(parameter);
		final String sFileName = getFileName(sDir, sReportName, ".pdf");
		LOG.debug("export to PDF as " + sFileName);
		try {
			JasperExportManager.exportReportToPdfFile(printObj, sFileName);

			switch (destination) {
			case FILE:
				openFile(sFileName, false);
				break;
			case PRINTER_CLIENT:
				openPrintDialog(sFileName, printObj, true, false);
				break;
			case PRINTER_SERVER:
				openPrintDialog(sFileName, printObj, false, false);
				break;
			case DEFAULT_PRINTER_CLIENT:
				openPrintDialog(sFileName, printObj, true, true);
				break;
			case DEFAULT_PRINTER_SERVER:
				openPrintDialog(sFileName, printObj, false, true);
				break;
			default:
				// TYPE SCREEN
				openFile(sFileName, true);
				break;
			}
		}
		catch (JRException ex) {
			throw new NuclosReportException(ex);
		}
	}public void openPrintDialog(String sFileName, JasperPrint printObj, boolean bIsClient, boolean bIsDefault) throws NuclosReportException {
		try {
	        PrintService   prservDflt;
	        if (bIsClient)
	        	prservDflt = PrintServiceLookup.lookupDefaultPrintService();
	        else
	        	prservDflt = ReportDelegate.getInstance().lookupDefaultPrintService();

	        PrintService[] prservices;
	        if (bIsClient)
	        	prservices = PrintServiceLookup.lookupPrintServices(null, null);
	        else
	        	prservices = ReportDelegate.getInstance().lookupPrintServices(null, null);

	        if(null == prservices || 0 >= prservices.length) {
	          if(null != prservDflt) {
	        	  prservices = new PrintService[] {prservDflt};
	          } else {
	        	  throw new NuclosReportException(CommonLocaleDelegate.getMessage("AbstractReportExporter.5", "Es ist kein passender Print-Service installiert."));
	          }
	        }

	        PrintService prserv = null;
	        PrintRequestAttributeSet aset = new HashPrintRequestAttributeSet();
	        if (prservDflt == null || !bIsDefault) {
	        	Rectangle gcBounds = GraphicsEnvironment.
	        		    getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().getBounds();
	        	prserv = ServiceUI.printDialog(null, (gcBounds.width / 2) - 200, (gcBounds.height / 2) - 200, prservices, prservDflt, null, aset);
	        } else
	        	prserv = prservDflt;

	        if( null != prserv ) {
	        	File file = getFileFromBytes(printObj);
	        	if (bIsClient)
	        		getNuclosReportPrintJob().print(prserv, file.getAbsolutePath(), aset);
	        	else {
	        		ReportDelegate.getInstance().printViaPrintService((NuclosReportRemotePrintService)prserv, getNuclosReportPrintJob(), aset, getBytesFromFile(file));
	        	}
	        }
		} catch (Exception e) {
			throw new NuclosReportException(e);
		}
	}

	private static File getFileFromBytes(Object data) throws IOException {
		File file = File.createTempFile("report_", ".tmp");
		file.deleteOnExit();

		OutputStream os = new FileOutputStream(file);

		ByteArrayOutputStream bos = new ByteArrayOutputStream();
	    ObjectOutputStream oos = new ObjectOutputStream(bos);
	    oos.writeObject(data);
	    oos.flush();
	    oos.close();
	    bos.close();

	    os.write(bos.toByteArray());
		os.close();

		return file;
	}

	/**
	 * @param sReportName
	 * @param resultVO
	 * @param destination
	 * @param parameter
	 * @param bOpenFile
	 * @throws NuclosReportException
	 */
	@Override
	public void export(String sReportName, ResultVO resultVO,
			String sourceFile, String parameter, ReportOutputVO.Destination destination) throws NuclosReportException {
		/** @todo adjust design */
		throw new UnsupportedOperationException();
	}

	@Override
	protected NuclosReportPrintJob getNuclosReportPrintJob() {
		return new PDFPrintJob();
	}

}	// class PDFExport
