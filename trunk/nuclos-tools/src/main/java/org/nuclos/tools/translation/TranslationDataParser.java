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
package org.nuclos.tools.translation;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.nuclos.tools.translation.translationdata.TranslationData;
import org.nuclos.tools.translation.translationdata.TranslationData.TranslationAction;


public class TranslationDataParser {

	private Map<String,FileData> fileDataMap;
   private List<TranslationData> dataList;
   private String sourcePath = null;
   private String backupPath = null;
   private TranslationData lastTranslationData = null;
   private StringBuffer buf = null;
   private int bufSize = 1048576;
   private boolean hasDelegateImport = false;
   private boolean translationImportDeleted = false;
   private int offset = 0;

   public TranslationDataParser(List<TranslationData> dataList, Map<String,FileData> fileDataMap, String sourcePath, String backupPath) {
      this.dataList = dataList;
      this.sourcePath = sourcePath;
      this.backupPath = backupPath;
      this.fileDataMap = fileDataMap;
   }

   private void sortDataList() {
      Collections.sort(dataList, new Comparator<TranslationData>() {
         @Override
		public int compare(TranslationData data1, TranslationData data2) {
            if (!data1.getFilePath().equals(data2.getFilePath()))
               return data1.getFilePath().compareTo(data2.getFilePath());
            return data1.getLineNumber().compareTo(data2.getLineNumber());
         }});

   }

   public void startParsing() {
      sortDataList();

      try {
         TranslationDAO.saveTransactionData(dataList);
      } catch (SQLException e) {
         System.err.println(e.getMessage());
         e.printStackTrace();
      }

      for (TranslationData td : dataList) {
         if (!td.getPerform())
            continue;

         if (td.getAction().equals(TranslationAction.NEW_ID))
            updateFile(td);
      }

      if (lastTranslationData != null)
         saveCurrentFile();
   }

   public void startRollback() {
      sortDataList();

      for (TranslationData td : dataList) {
         if (td.getPerform()) {
         	lastTranslationData = td;
            doRollback();
         }
      }
   }

   private void updateFile(TranslationData td) {

      String replaceText = null;
      int startIndex = 0;
      int endIndex = 0;

      FileData filedata = fileDataMap.get(td.getFilePath());

      if (lastTranslationData == null || !lastTranslationData.getFilePath().equals(td.getFilePath())) {
         System.out.println("----- updating file "+td.getFilePath()+" -----");
         readFile(td);
         hasDelegateImport = false;
      }

      String text = td.getText();

      if (text.startsWith(TranslationHelper.TARGET_METHOD)) {
      	text = TranslationUtil.removeMethodFrame(text);
      	filedata.setTranslationCalls(filedata.getTranslationCalls()-1);
      }
      else if (!text.endsWith("'"))
      	text = text+")";

      if (td.getAction().equals(TranslationAction.NEW_ID)) {

         replaceText = "CommonLocaleDelegate."+TranslationHelper.REPLACING_METHOD+"(\""+td.getResourceId()+"\","+text;
         startIndex = offset + td.getFilePos();
         endIndex = offset + td.getFilePos()+td.getText().length();
      }

      System.out.println(String.format("replacing %s at line %s with %s",
            buf.substring(startIndex,endIndex),td.getLineNumber(),replaceText));

      buf.replace(startIndex,endIndex,replaceText);
      offset = offset + replaceText.length() - td.getText().length();
   }

   private void readFile(TranslationData td) {
      if (lastTranslationData != null)
         saveCurrentFile();

      lastTranslationData = td;

      try {
         FileReader reader = new FileReader(lastTranslationData.getFilePath());

         try {
            int len = 0;
            int fileSize = 0;
            hasDelegateImport = false;
            translationImportDeleted = false;
            offset = 0;
            char buffer[] = new char[bufSize];

            while ((len = reader.read(buffer)) > 0)
               fileSize += len;

            buf = new StringBuffer();
            buf.append(buffer,0,fileSize);
         } finally {
            reader.close();
         }

         doBackup();
      } catch (FileNotFoundException e) {
         System.err.println(e.getMessage());
         e.printStackTrace();
      } catch (IOException e) {
         System.err.println(e.getMessage());
         e.printStackTrace();
      }
   }

   private void checkImports(TranslationData td) {
   	offset = 0;

   	FileData filedata = fileDataMap.get(td.getFilePath());

   	if (!translationImportDeleted && filedata.getTranslationCalls() == 0 && filedata.getTranslationImportStartIndex() >= 0) {
      	int start = filedata.getTranslationImportStartIndex();
      	int end = filedata.getTranslationImportEndIndex()+1;

      	System.out.println(String.format("no getTranslation-calls left, deleting import for getTranslation at startIndex %d with endIndex %d",start,end));

      	buf.delete(start,end);
      	translationImportDeleted = true;
      	offset = offset + start - end;
      }
      if (!hasDelegateImport && !filedata.hasDelegateImport()) {
      	int start = filedata.getFirstImportStartIndex();

      	if (filedata.getFirstImportStartIndex() > filedata.getTranslationImportStartIndex())
      		start += offset;

      	int end = start + (TranslationHelper.GETMESSAGE_IMPORT+"\n").length();
      	System.out.println(String.format("import for getMessage needed, import inserted at startIndex %d with endIndex %d",start,end));

      	buf.insert(start, TranslationHelper.GETMESSAGE_IMPORT+"\n");
      	hasDelegateImport = true;
      }
   }

   private void doBackup() {
      copyFile(new File(lastTranslationData.getFilePath()),new File(backupPath+lastTranslationData.getFilePath().substring(sourcePath.length())));
   }

   private void doRollback() {
      copyFile(new File(backupPath+lastTranslationData.getFilePath().substring(sourcePath.length())),new File(lastTranslationData.getFilePath()));
   }

   private void copyFile(File src, File dest) {

      dest.getParentFile().mkdirs();

      if (dest.exists()) {
         dest.delete();
      }

      try {
         FileInputStream fis = new FileInputStream(src);
         try {
            byte buffer[] = new byte[bufSize];

            FileOutputStream fos = new FileOutputStream(dest);
            try {
               int ln;
               while ((ln = fis.read(buffer)) > 0) {
                  fos.write(buffer, 0, ln);
               }
            } finally {
               fos.close();
            }
         } finally {
            fis.close();
         }
      } catch (IOException e) {
         System.err.println(e.getMessage());
         e.printStackTrace();
      }
   }

   private void saveCurrentFile() {
   	checkImports(lastTranslationData);

      try {
         FileWriter writer = new FileWriter(lastTranslationData.getFilePath());

         try {
            char buffer[] = new char[bufSize];
            buf.getChars(0, buf.length()-1, buffer, 0);
            writer.write(buffer,0,buf.length()-1);
         } finally {
            writer.close();
         }
      } catch (IOException e) {
         System.err.println(e.getMessage());
         e.printStackTrace();
      }
   }
}
