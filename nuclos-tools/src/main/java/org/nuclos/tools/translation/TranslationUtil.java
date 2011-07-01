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

public class TranslationUtil {

   public static String removeQuotes(String s) {
      if (s == null || s.indexOf('"') < 0 || s.lastIndexOf('"') == s.indexOf('"'))
         return s;

      return s.substring(s.indexOf('"')+1, s.lastIndexOf('"'));
   }

   public static String removeMethodFrame(String s) {
      if (s == null || s.indexOf('(') < 0 || s.lastIndexOf(')') < 0 || s.lastIndexOf(')') < s.length()-1) {
      	if (s != null && s.indexOf('(') > 0)
      		return s.substring(s.indexOf('(')+1,s.length());
      	else
      		return s;
      }

      return s.substring(s.indexOf('(')+1, s.lastIndexOf(')')+1);
   }

}
