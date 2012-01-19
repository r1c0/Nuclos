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
package org.nuclos.client.report.reportrunner;

import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.ServiceUI;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;

import org.apache.log4j.Logger;
import org.nuclos.client.report.ReportDelegate;
import org.nuclos.common2.CommonLocaleDelegate;
import org.nuclos.common2.IOUtils;
import org.nuclos.common2.StringUtils;
import org.nuclos.common2.SystemUtils;
import org.nuclos.server.report.NuclosReportException;
import org.nuclos.server.report.NuclosReportPrintJob;
import org.nuclos.server.report.NuclosReportRemotePrintService;
import org.nuclos.server.report.valueobject.ReportOutputVO;
import org.nuclos.server.report.valueobject.ReportVO;
import org.nuclos.server.report.valueobject.ResultVO;

/**
 * Base Class for all export types in the package "export"
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:uwe.allner@novabit.de">uwe.allner</a>
 * @version 02.01.00
 */
public abstract class AbstractReportExporter implements ReportExporter {

	private final static Logger log = Logger.getLogger(AbstractReportExporter.class);

	/** @todo try to eliminate these fields */
	protected ReportVO reportVO;
	protected ReportOutputVO reportOutputVO;

	private String sGenericObjectIdentifier;

	private String sReportFileName;

	@Override
	public void export(ResultVO resultvo, ReportVO reportvo, ReportOutputVO outputvo)
			throws NuclosReportException {

		this.reportVO = reportvo;
		this.reportOutputVO = outputvo;

		final String sReportName = reportvo.getName();
		final String sSourceFileName = outputvo.getSourceFile();
		final String sParameter = outputvo.getParameter();

		export(outputvo.getDescription() != null ? outputvo.getDescription() : sReportName , resultvo, sSourceFileName, sParameter, outputvo.getDestination());
	}

	@Override
	public void setReportFileName(String filename) {
		this.sReportFileName = filename;
	}

	/**
	 * @param sReportName
	 * @param resultVO
	 * @param parameter
	 * @throws NuclosReportException
	 */
	public abstract void export(String sReportName, ResultVO resultVO, String sourceFile, String parameter,
			 ReportOutputVO.Destination destination) throws NuclosReportException;

	/**
	 * Generate the output file name
	 * @param sExportPath
	 * @param sReportName
	 * @param sExtension
	 * @return file name of the report
	 */
	protected final String getFileName(String sExportPath, String sReportName, String sExtension) {
		String result;
		final File file = new File(sExportPath);
		if (file.isDirectory()) {
			final DateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss", Locale.getDefault());
			//final DateFormat dateformat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM, Locale.getDefault());
			if(sReportFileName != null && sReportFileName.length() > 0) {
				result = sReportFileName + "_" + dateformat.format(Calendar.getInstance(Locale.getDefault()).getTime());
			}
			else {
				result = sReportName + getGenericObjectSystemIdentifier() + "_" + dateformat.format(Calendar.getInstance(Locale.getDefault()).getTime());
			}
			result = result.replaceAll("[/+*%?#!.:]", "-");
			result = sExportPath + ((sExportPath.endsWith(File.separator)) ? "" : File.separator) + result + sExtension; // sExtension comes with dot!
		}
		else {
			final String sExt = sExportPath.substring(sExportPath.lastIndexOf("."));
			result = sExportPath.substring(0, sExportPath.lastIndexOf("."));	// sExportPath without extension and dot
			result = result + getGenericObjectSystemIdentifier() + sExt;			// will be like before, if sGenericObjectIdentifier ist empty
		}
		// set the name of the generated report in the thread for further use
		if (Thread.currentThread().getClass().equals(ReportThread.class)) {
			/** @todo refactor! */
			((ReportThread) Thread.currentThread()).setDocumentName(result);
		}

		return result;
	}


	private String getGenericObjectSystemIdentifier() {
		String result = "";
		if (!StringUtils.isNullOrEmpty(this.sGenericObjectIdentifier)) {
			result = this.sGenericObjectIdentifier;
			result = result.replaceAll("[/+*%?#!.:]", "-");
			result = "_" + result;
		}

		return result;
	}

	/**
	 * @param sExportPath destination
	 * @return directory for export or filename (if filename was given)
	 */
	protected static String createExportDir(String sExportPath) throws NuclosReportException {
		final String result;

		if (sExportPath == null) {
			result = System.getProperty("java.io.tmpdir");
		}
		else {
			result = sExportPath;
			final int lastDotPos = sExportPath.lastIndexOf(".");
			final int lastSlashPos = sExportPath.lastIndexOf(File.separator);
			if (lastSlashPos >= lastDotPos) {
				final File fileExportDir = new File(sExportPath);
				if (!fileExportDir.exists()) {
					if (!fileExportDir.mkdir()) {
						throw new NuclosReportException(CommonLocaleDelegate.getMessage("AbstractReportExporter.1", "Das Verzeichnis {0} konnte nicht angelegt werden.", sExportPath));
					}
				}
			}
		}
		return result;
	}

	protected static void copyTemplateFile(String sFileName, ReportOutputVO outputvo) throws NuclosReportException {
		try {
			final File fileDestination = new File(sFileName);
			if (fileDestination.exists()) {
				fileDestination.delete();
			}
			final String sSourceFile = outputvo.getSourceFile();
			if (sSourceFile != null) {
				final org.nuclos.server.report.ByteArrayCarrier bac = outputvo.getSourceFileContent();
				if (bac == null) {
					throw new NuclosReportException(CommonLocaleDelegate.getMessage("AbstractReportExporter.2", "Die Dokumentvorlage \"{0}\" konnte nicht gelesen werden.", sSourceFile));
				}
				IOUtils.writeToBinaryFile(fileDestination, bac.getData());
			}
		}
		catch (IOException ex) {
			throw new NuclosReportException(CommonLocaleDelegate.getMessage("AbstractReportExporter.3", "Die Datei \"{0}\" konnte nicht erstellt werden.", sFileName) + "\n", ex);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setGenericObjectIdentifier(String sGenericObjectIdentifier) {
		this.sGenericObjectIdentifier = sGenericObjectIdentifier;
	}

	/** @todo openFile should just "open the file" and not "open the file depending on parameter bOpenFile"
	 *  FIX ELISA-6498 public instead of protected, is used for preview of template files
	 * */
	public static void openFile(String sFileName, boolean bOpenFile) throws NuclosReportException {
		if (bOpenFile) {
			try {
				SystemUtils.open(sFileName);
			}
			catch (IOException ex) {
				throw new NuclosReportException(CommonLocaleDelegate.getMessage("AbstractReportExporter.4", "Die Datei {0} konnte nicht ge\u00f6ffnet werden.", sFileName), ex);
			}
		}
		else {
			log.debug("NOT opening " + sFileName);
		}
	}
	
	
	public void openPrintDialog(String sFileName, boolean bIsClient, boolean bIsDefault) throws NuclosReportException {
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
	        	if (bIsClient) 
	        		getNuclosReportPrintJob().print(prserv, sFileName, aset);
	        	else {
	        		ReportDelegate.getInstance().printViaPrintService((NuclosReportRemotePrintService)prserv, getNuclosReportPrintJob(), aset, getBytesFromFile(new File(sFileName)));
	        	}
	        }	
		} catch (Exception e) {
			throw new NuclosReportException(e);
		}
	}
	
	protected static byte[] getBytesFromFile(File file) throws IOException {
	    InputStream is = new FileInputStream(file);

	    // Get the size of the file
	    long length = file.length();

	    // You cannot create an array using a long type.
	    // It needs to be an int type.
	    // Before converting to an int type, check
	    // to ensure that file is not larger than Integer.MAX_VALUE.
	    if (length > Integer.MAX_VALUE) {
	        // File is too large
	    }

	    // Create the byte array to hold the data
	    byte[] bytes = new byte[(int)length];

	    // Read in the bytes
	    int offset = 0;
	    int numRead = 0;
	    while (offset < bytes.length
	           && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
	        offset += numRead;
	    }

	    // Ensure all the bytes have been read in
	    if (offset < bytes.length) {
	        throw new IOException("Could not completely read file "+file.getName());
	    }

	    // Close the input stream and return bytes
	    is.close();
	    return bytes;
	}
	
	public static synchronized void checkJawin() throws NuclosReportException {
		if (!jawinLoaded) {
			try {
				System.loadLibrary("jawin");
				jawinLoaded = true;
			} catch (SecurityException e) {
				log.error("Error loading jawin", e);
				throw new NuclosReportException("nuclos.jawin.linkerror", e);
			} catch (UnsatisfiedLinkError e) {
				log.error("Error loading jawin", e);
				throw new NuclosReportException("nuclos.jawin.linkerror", e);
			}
		}
	}

	private static volatile boolean jawinLoaded = false;
	
	protected abstract NuclosReportPrintJob getNuclosReportPrintJob();
}	// class AbstractReportExporter
