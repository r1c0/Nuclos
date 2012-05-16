package org.nuclos.client.report.reportrunner.export;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.nuclos.client.report.reportrunner.AbstractReportExporter;
import org.nuclos.common.NuclosFile;
import org.nuclos.server.report.NuclosReportException;
import org.nuclos.server.report.NuclosReportPrintJob;
import org.nuclos.server.report.print.FilePrintJob;
import org.nuclos.server.report.valueobject.ReportOutputVO;
import org.nuclos.server.report.valueobject.ResultVO;

public class FileExport extends AbstractReportExporter {

	private final NuclosFile file;

	public FileExport(NuclosFile file) {
		this.file = file;
	}

	public void export(String sReportName, String parameter, ReportOutputVO.Destination destination) throws NuclosReportException {
		final String sDir = createExportDir(parameter);
		final String sFileName = getFileName(sDir, sReportName, file.getFileName().substring(file.getFileName().lastIndexOf('.')));

		File file = new File(sFileName);
		OutputStream fos = null;

		try {
			fos = new BufferedOutputStream(new FileOutputStream(file));
			fos.write(this.file.getFileContents());

			switch (destination) {
			case FILE:
				openFile(sFileName, false);
				break;
			case PRINTER_CLIENT:
				openPrintDialog(sFileName, true, false);
				break;
			case PRINTER_SERVER:
				openPrintDialog(sFileName, false, false);
				break;
			case DEFAULT_PRINTER_CLIENT:
				openPrintDialog(sFileName, true, true);
				break;
			case DEFAULT_PRINTER_SERVER:
				openPrintDialog(sFileName, false, true);
				break;
			default:
				// TYPE SCREEN
				openFile(sFileName, true);
				break;
			}			
		}
		catch (Exception ex) {
			throw new NuclosReportException(ex);
		}
		finally {
			try {
				if (fos != null) {
					fos.close();
				}
			} catch (IOException e) { }
		}
	}

	@Override
	public void export(String sReportName, ResultVO resultVO, String sourceFile, String parameter, ReportOutputVO.Destination destination) throws NuclosReportException {
		/** @todo adjust design */
		throw new UnsupportedOperationException();
	}
	
	@Override
	protected NuclosReportPrintJob getNuclosReportPrintJob() {
		return new FilePrintJob();
	}
}
