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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public abstract class TranslationFilter {

   public enum FilterType {
      CLASS("classfilter.csv"),
      METHOD("methodfilter.csv");

      public final String filename;

      private FilterType(String filename) {
         this.filename = filename;
      }

      public String getFilename() {
         return filename;
      }

      public static FilterType getByFilename(String filename) {
         for (FilterType t : FilterType.values()) {
            if (t.filename == filename) {
               return t;
            }
         }
         return null;
      }
   }

   public static Set<String> getFilters(FilterType type) {

      Set<String> filters = new HashSet<String>();

      BufferedReader reader = null;

      try {
      	String userDir = System.getProperty("user.dir");
      	String filepath = userDir.contains("translation") ? userDir+"\\csv\\" : userDir+"\\src\\java\\de\\novabit\\nucleus\\tools\\translation\\csv\\";
         File f = new File(filepath+type.filename);
         String s = f.getAbsolutePath();
         reader = new BufferedReader(new FileReader(s));

         while (reader.ready())
            filters.add(reader.readLine());

      } catch (FileNotFoundException e) {
         System.err.println(e.getMessage());
         e.printStackTrace();
      } catch (IOException e) {
         System.err.println(e.getMessage());
         e.printStackTrace();
      } finally {
         try {
            if (reader != null)
               reader.close();
         } catch (IOException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
         }
      }

      return filters;
   }
}
