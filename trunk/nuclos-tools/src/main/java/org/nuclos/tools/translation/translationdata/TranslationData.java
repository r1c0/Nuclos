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
package org.nuclos.tools.translation.translationdata;


public class TranslationData {

   public enum TranslationAction {
      NEW_ID("neue ID vergeben (DB + Code)"),
      SAVE_ID("ID in DB speichern"),
      UPDATE_DB("Text in DB aktualisieren"),
      DELETE_ID("ungenutzte ID in DB l\u00f6schen");
      //MARK_UNINTERESTING("String nicht mehr beachten");

      public final String name;

      private TranslationAction(String name) {
         this.name = name;
      }

      public String getName() {
         return name;
      }

      public static TranslationAction getByName(String name) {
         for (TranslationAction a : TranslationAction.values()) {
            if (a.name == name) {
               return a;
            }
         }
         return null;
      }
   }

   private boolean perform;
   private TranslationAction action;
   private String resourceId;
   private String text;
   private String filePath;
   private Integer lineNumber;
   private Integer filePos;
   private boolean visible;


   public TranslationData() {
      perform = false;
      visible = true;
   }

   public boolean getPerform() {
      return perform;
   }

   public void setPerform(boolean perform) {
      this.perform = perform;
   }

   public TranslationAction getAction() {
      return action;
   }

   public void setAction(TranslationAction action) {
      this.action = action;
   }

   public String getResourceId() {
      return resourceId;
   }

   public void setResourceId(String resourceId) {
      this.resourceId = resourceId;
   }

   public String getText() {
      return text;
   }

   public void setText(String text) {
      this.text = text;
   }

   public String getFileName() {
      return filePath.substring(filePath.lastIndexOf(System.getProperty("file.separator"))+1,filePath.length());
   }

   public String getFilePath() {
      return filePath;
   }

   public void setFilePath(String filePath) {
      this.filePath = filePath;
   }

   public Integer getLineNumber() {
      return lineNumber;
   }

   public void setLineNumber(Integer lineNumber) {
      this.lineNumber = lineNumber;
   }

   public Integer getFilePos() {
      return filePos;
   }

   public void setFilePos(Integer filePos) {
      this.filePos = filePos;
   }

   public boolean isVisible() {
      return visible;
   }

   public void setVisible(boolean visible) {
      this.visible = visible;
   }

   public TranslationData copy() {
   	TranslationData td = new TranslationData();
   	td.setAction(this.action);
   	td.setFilePath(this.filePath);
   	td.setFilePos(filePos);
   	td.setLineNumber(lineNumber);
   	td.setPerform(perform);
   	td.setResourceId(resourceId);
   	td.setText(text);
   	td.setVisible(visible);

   	return td;
   }
}
