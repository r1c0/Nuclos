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

public class FilePrintJob extends NuclosReportPrintJob {	
	
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
}
