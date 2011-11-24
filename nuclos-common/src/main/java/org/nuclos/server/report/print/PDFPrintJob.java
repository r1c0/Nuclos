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

import java.awt.Graphics;
import java.awt.print.Book;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;

import javax.print.PrintException;
import javax.print.PrintService;
import javax.print.attribute.PrintRequestAttributeSet;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JRReport;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperPrintManager;
import net.sf.jasperreports.engine.export.JRGraphics2DExporter;
import net.sf.jasperreports.engine.export.JRGraphics2DExporterParameter;
import net.sf.jasperreports.engine.util.JRLoader;

import org.nuclos.server.report.NuclosReportException;
import org.nuclos.server.report.NuclosReportPrintJob;

/**
 * Exporter which prints Adobe PDF-files. <br>
 * <br>
 * Created by Novabit Informationssysteme GmbH <br>
 * Please visit <a href="http://www.novabit.de">www.novabit.de </a>
 *
 * @author <a href="mailto:uwe.allner@novabit.de">Uwe.Allner</a>
 * @version 01.00.00
 * @todo don't extend AbstractReportExporter
 */
public class PDFPrintJob extends NuclosReportPrintJob {
	
	@Override
	public void print(PrintService prserv, String sFilename,
			PrintRequestAttributeSet aset) throws PrintException {
		try {
			final JasperPrint jasperPrint = (JasperPrint)JRLoader.loadObject(sFilename);
		
			int firstPageIndex = 0;
			int lastPageIndex = jasperPrint.getPages().size() - 1;
			
			if (firstPageIndex < 0 ||
				firstPageIndex > lastPageIndex ||
				lastPageIndex >= jasperPrint.getPages().size())
			{
				throw new NuclosReportException("Invalid page index range : " + firstPageIndex + " - " + lastPageIndex + " of " + jasperPrint.getPages().size());
			}

			final int pageOffset = firstPageIndex;

			PrinterJob printJob = PrinterJob.getPrinterJob();

			// fix for bug ID 6255588 from Sun bug database
			try {
				printJob.setPrintService(prserv);
			} catch (PrinterException e) { 
				//... as in Jasper JRPrinterAWT
			}
			
			PageFormat pageFormat = printJob.defaultPage();
			Paper paper = pageFormat.getPaper();

			printJob.setJobName("JasperReports - " + jasperPrint.getName());
			
			switch (jasperPrint.getOrientation())
			{
				case JRReport.ORIENTATION_LANDSCAPE :
				{
					pageFormat.setOrientation(PageFormat.LANDSCAPE);
					paper.setSize(jasperPrint.getPageHeight(), jasperPrint.getPageWidth());
					paper.setImageableArea(0, 0, jasperPrint.getPageHeight(), jasperPrint.getPageWidth());
					break;
				}
				case JRReport.ORIENTATION_PORTRAIT :
				default :
				{
					pageFormat.setOrientation(PageFormat.PORTRAIT);
					paper.setSize(jasperPrint.getPageWidth(), jasperPrint.getPageHeight());
					paper.setImageableArea(0, 0, jasperPrint.getPageWidth(), jasperPrint.getPageHeight());
				}
			}

			pageFormat.setPaper(paper);

			Book book = new Book();
			book.append(new Printable() {
				
				@Override
				public int print(Graphics graphics, PageFormat pageFormat, int pageIndex)
						throws PrinterException {
					if (Thread.currentThread().isInterrupted()) {
						throw new PrinterException("Current thread interrupted.");
					}

					pageIndex += pageOffset;

					if (pageIndex < 0 || pageIndex >= jasperPrint.getPages().size()) {
						return Printable.NO_SUCH_PAGE;
					}

					try {
						JRGraphics2DExporter exporter = new JRGraphics2DExporter();
						exporter.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
						exporter.setParameter(JRGraphics2DExporterParameter.GRAPHICS_2D, graphics);
						exporter.setParameter(JRExporterParameter.PAGE_INDEX, new Integer(pageIndex));
						exporter.exportReport();
					} catch (JRException e) {
						throw new PrinterException(e.getMessage());
					}

					return Printable.PAGE_EXISTS;
				}
			}, pageFormat, lastPageIndex - firstPageIndex + 1);
			printJob.setPageable(book);
			
			printJob.print();
		} catch (Exception e) {
			throw new PrintException(e.getMessage());
		}	
	}
}	// class PDFExport
