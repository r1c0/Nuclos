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

public class RessourceData {

   private String ressId;
   private String text;
   private boolean changed;
   
   public RessourceData() {}
   
   public RessourceData(String ressId, String text) {
      this.ressId = ressId;
      this.text = text;
      this.changed = false;
   }
   
   public RessourceData(String ressId, String text, boolean changed) {
      this.ressId = ressId;
      this.text = text;
      this.changed = changed;
   }
   
   public String getRessId() {
      return ressId;
   }

   public void setRessId(String ressId) {
      this.ressId = ressId;
   }

   public String getText() {
      return text;
   }
   
   public void setText(String text) {
      this.text = text;
   }

   public boolean hasChanged() {
      return changed;
   }

   public void setChanged(boolean changed) {
      this.changed = changed;
   }
   
}
