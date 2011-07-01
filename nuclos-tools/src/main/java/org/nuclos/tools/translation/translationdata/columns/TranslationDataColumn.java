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
package org.nuclos.tools.translation.translationdata.columns;

import org.nuclos.tools.translation.translationdata.TranslationData;


public abstract class TranslationDataColumn {

   public static final int COLUMN_INDEX_EXECUTE = 0;
   public static final int COLUMN_INDEX_ACTION  = 1;
   public static final int COLUMN_INDEX_RESS_ID = 2;
   public static final int COLUMN_INDEX_FILE    = 3;
   public static final int COLUMN_INDEX_LINE    = 4;
   public static final int COLUMN_INDEX_TEXT    = 5;
   
   public static final int NUMBER_OF_COLUMNS = 6;
   
   public abstract String getColumnName();
   public abstract Class<?> getColumnClass();
   public abstract int getPreferredColumnWidth();
   public abstract Object getValueAt(TranslationData td);
   public abstract void setValueAt(Object value, TranslationData td);
   public abstract boolean isCellEditable(TranslationData td);
   
   public static TranslationDataColumn makeTranslationDataColumn(int index) {
      switch (index) {
         case COLUMN_INDEX_EXECUTE:  return new TranslationDataExecuteColumn();
         case COLUMN_INDEX_ACTION:   return new TranslationDataActionColumn();
         case COLUMN_INDEX_RESS_ID:  return new TranslationDataRessIdColumn();
         case COLUMN_INDEX_FILE:     return new TranslationDataFileColumn();
         case COLUMN_INDEX_LINE:     return new TranslationDataLineNumberColumn();
         case COLUMN_INDEX_TEXT:     return new TranslationDataTextColumn();
      }
      return new TranslationDataDummyColumn();
   }
}
