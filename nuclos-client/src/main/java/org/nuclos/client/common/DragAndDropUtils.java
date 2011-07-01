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
package org.nuclos.client.common;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jawin.DispatchPtr;
import org.jawin.win32.Ole32;
import org.nuclos.client.report.reportrunner.AbstractReportExporter;
import org.nuclos.common.PointerException;
import org.nuclos.common2.CommonLocaleDelegate;
import org.nuclos.common2.IOUtils;
import org.nuclos.common2.StringUtils;

public abstract class DragAndDropUtils {

	/**
	 * takes all selected mails from outlook over jawin interface
	 * save in tempory file
	 * @return List of java.io.File 
	 * @throws PointerException 
	 */
	public static List<File> mailHandling() throws PointerException {
		try {
			AbstractReportExporter.checkJawin();
		}
		catch(Exception e) {
			throw new PointerException(CommonLocaleDelegate.getMessage("details.subform.controller.2", "Diese Funktion wird nur unter Microsoft Windows unterstützt!"));
		}
		
		List<File> lstFiles = new ArrayList<File>();
		try {
			
			Ole32.CoInitialize();
			DispatchPtr application = new DispatchPtr("Outlook.Application");			
			DispatchPtr ae = application.getObject("ActiveExplorer");
			DispatchPtr selection = ae.getObject("Selection");
			Integer count = (Integer)selection.get("Count");
			
			for(int i = 0; i < count; i++) {
				DispatchPtr mailItem = selection.getObject("Item", String.valueOf(i+1));
				Object subject = mailItem.get("Subject");
				String tempDir = IOUtils.getDefaultTempDir().getAbsolutePath() + "\\";				
				String strFileName = subject + ".msg";				
				strFileName = StringUtils.trimInvalidCharactersInFilename(strFileName);
				String wholeFileName = tempDir + strFileName;
				
				Object dummy = mailItem.invoke("SaveAs", wholeFileName);				
				File file = new File(wholeFileName);
				file.deleteOnExit();
				lstFiles.add(file);
			}
			
			application.close();
			
		} catch (Exception e) {
			throw new PointerException(CommonLocaleDelegate.getMessage("details.subform.controller.2", "Diese Funktion wird nur unter Microsoft Windows unterstützt!"));
		}
		finally {			
			 try {
				 Ole32.CoUninitialize();
			} catch (Exception e2) {
				throw new PointerException(CommonLocaleDelegate.getMessage("details.subform.controller.2", "Diese Funktion wird nur unter Microsoft Windows unterstützt!"));
			}
		}
		return lstFiles;
	}
	
	public static int getIndexOfFileList(DataFlavor flavor[], Transferable trans) {
		int index = -1;
		if(flavor != null && flavor.length > 0) {
			try {
				for(int i = 0; i < flavor.length; i++) {
					if(trans.getTransferData(flavor[i]) instanceof List) {
						index = i;
						break;
					}
				}
			}
			catch(UnsupportedFlavorException e) {}
			catch(IOException e) {}
		}
		
		return index;
	}
	
}
