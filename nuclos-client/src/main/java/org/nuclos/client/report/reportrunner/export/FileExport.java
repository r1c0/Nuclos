package org.nuclos.client.report.reportrunner.export;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.nuclos.client.report.reportrunner.AbstractReportExporter;
import org.nuclos.common.NuclosFile;
import org.nuclos.server.report.NuclosReportException;
import org.nuclos.server.report.valueobject.ResultVO;

public class FileExport extends AbstractReportExporter {

	private final NuclosFile file;

	public FileExport(NuclosFile file) {
		this.file = file;
	}

	public void export(String sReportName, String parameter, boolean bOpenFile) throws NuclosReportException {
		final String sDir = createExportDir(parameter);
		final String sFileName = getFileName(sDir, sReportName, file.getFileName().substring(file.getFileName().lastIndexOf('.')));

		File file = new File(sFileName);
		FileOutputStream fos = null;

		try {
			fos = new FileOutputStream(file);
			fos.write(this.file.getFileContents());
			openFile(sFileName, bOpenFile);
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
	public void export(String sReportName, ResultVO resultVO, String sourceFile, String parameter, boolean bOpenFile) throws NuclosReportException {
		/** @todo adjust design */
		throw new UnsupportedOperationException();
	}
}
