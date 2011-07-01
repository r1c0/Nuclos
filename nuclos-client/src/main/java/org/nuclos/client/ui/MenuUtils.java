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
package org.nuclos.client.ui;

import java.awt.Component;
import java.text.Collator;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.nuclos.common2.LangUtils;
import org.nuclos.common2.exception.CommonFatalException;

/**
 * Utility methods for menus.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version	01.00.00
 */

public class MenuUtils {

	protected MenuUtils() {
	}

   /**
    * @param menu
    * @param sActionCommand
    * @return the menu item (if any) with the given text as child of the given menu.
    */
   public static JMenuItem getJMenuItemByActionCommand(JMenu menu, String sActionCommand) {
      JMenuItem result = null;
      for (int i = 0; i < menu.getMenuComponentCount(); i++) {
         final JMenuItem mi = (JMenuItem) menu.getMenuComponent(i);
         if (LangUtils.equals(mi.getActionCommand(), sActionCommand)) {
            result = mi;
            break;
         }
      }
      return result;
   }

   /**
    * @param popupmenu
    * @param sActionCommand
    * @return the menu item (if any) with the given action command as child of the given popupmenu.
    */
   public static JMenuItem getJMenuItemByActionCommand(JPopupMenu popupmenu, String sActionCommand) {
      JMenuItem result = null;
      for (int i = 0; i < popupmenu.getComponentCount(); i++) {
         final Component comp = popupmenu.getComponent(i);
         if (comp instanceof JMenuItem) {
            final JMenuItem mi = (JMenuItem) comp;
            if (LangUtils.equals(mi.getActionCommand(), sActionCommand)) {
               result = mi;
               break;
            }
         }
      }
      return result;
   }

   /**
    * @param menu
    * @param sText
    * @return the menu item (if any) with the given text as child of the given menu.
    */
   public static JMenuItem getJMenuItemByText(JMenu menu, String sText) {
      JMenuItem result = null;
      for (int i = 0; i < menu.getMenuComponentCount(); i++) {
    	  final Component comp = menu.getMenuComponent(i);
    	  if(comp instanceof JMenuItem){
    		  final JMenuItem mi = (JMenuItem) menu.getMenuComponent(i);
    	         if (LangUtils.equals(mi.getText(), sText)) {
    	            result = mi;
    	            break;
    	         }
    	  }        
      }
      return result;
   }

   /**
    * @param popupmenu
    * @param sText
    * @return the menu item (if any) with the given text as child of the given popupmenu.
    */
   public static JMenuItem getJMenuItemByText(JPopupMenu popupmenu, String sText) {
      JMenuItem result = null;
      for (int i = 0; i < popupmenu.getComponentCount(); i++) {
         final Component comp = popupmenu.getComponent(i);
         if (comp instanceof JMenuItem) {
            final JMenuItem mi = (JMenuItem) comp;
            if (LangUtils.equals(mi.getText(), sText)) {
               result = mi;
               break;
            }
         }
      }
      return result;
   }
   /**
    * get the text of menus from a given list of menus
    * @param lstMenus
    * @return
    */
   public static Map<String,Integer> getTextByJMenu(List<JMenu> lstMenus){
	   Map<String, Integer> mpMenus = new HashMap<String, Integer>();
	   for(int i=0;i<lstMenus.size();i++)						
		   mpMenus.put(lstMenus.get(i).getText(),i);
			
	   return mpMenus;	
   }

	/**
	 * sorts the given menu (and recursively, its submenus) according to the labels of the menu items,
	 * using the default Collator.
	 * @param menu
	 */
	public static void sortMenu(JMenu menu) {
		sortMenu(menu, null);
	}

   /**
    * sorts the given menu (and recursively, its submenus) according to the labels of the menu items,
		* using the given Collator.
    * @param menu
		* @param collator the collator to use or <code>null<> for the default Collator.
    */
   public static void sortMenu(JMenu menu, final Collator collator) {
      final Component[] acomp = menu.getMenuComponents();

      // sort the menu items by their labels. Note that JMenus are JMenuItems too.
      Arrays.sort(acomp, new Comparator<Object>() {
         @Override
		public int compare(Object o1, Object o2) {
					 final String s1 = getText(o1);
					 final String s2 = getText(o2);
					 return collator == null ? LangUtils.compare(s1, s2) : LangUtils.compare(s1, s2, collator);
         }

         private String getText(Object o) {
            if (o instanceof JMenuItem) {
               return ((JMenuItem) o).getText();
            }
            throw new CommonFatalException("Don't know how to sort " + o);
         }
      });

      menu.removeAll();
       for (Component comp : acomp) {
           // sort submenus:
           if (comp instanceof JMenu) {
               sortMenu((JMenu) comp, collator);
           }
           menu.add(comp);
       }
   }

	/**
	 * disables (recursively) the given menu if all menu items in the menu (all direct children)
	 * are disabled.
	 * @param menu
	 * @param bRemoveEmptySubMenus Are submenus without enabled menu items to be removed?
	 */
	public static void disableMenuIfAllMenuItemsAreDisabled(JMenu menu, boolean bRemoveEmptySubMenus) {
		int iEnabledMenuItems = 0;
        for (Component comp : menu.getMenuComponents()) {
            if (comp instanceof JMenuItem) {
                final JMenuItem mi = (JMenuItem) comp;
                if (mi instanceof JMenu) {
                    final JMenu menuChild = (JMenu) mi;
                    disableMenuIfAllMenuItemsAreDisabled(menuChild, bRemoveEmptySubMenus);
                    if (bRemoveEmptySubMenus && !mi.isEnabled()) {
                        menu.remove(mi);
                    }
                }
                if (mi.isEnabled()) {
                    ++iEnabledMenuItems;
                }
            }
        }
		if (iEnabledMenuItems == 0) {
			menu.setEnabled(false);
		}
	}

}  // class MenuUtils
