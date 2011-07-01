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
package org.nuclos.client.layout.wysiwyg.editor.util;

/**
 * This class contains some static values.<br>
 * Here are the margins defined, minimum and maximum sizes for Layouts etc.
 * 
 * <br>
 * Created by Novabit Informationssysteme GmbH <br>
 * Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 * 
 * @author <a href="mailto:hartmut.beckschulze@novabit.de">hartmut.beckschulze</a>
 * @version 01.00.00
 */
public class InterfaceGuidelines {
    public static final int MINIMUM_SIZE = 5;
    
    public static final int SENSITIVITY = 5;
    public static final int DISTANCE_TO_OTHER_OBJECTS = 12;
    public static final int MARGIN_BETWEEN = 5;
    
    /** default margins */
    public static final int MARGIN_LEFT = 12;
    public static final int MARGIN_TOP = 12;
    public static final int MARGIN_BOTTOM = 12;
    public static final int MARGIN_RIGHT = 12;
    
    public static final int CELL_MARGIN_LEFT = 3;
    public static final int CELL_MARGIN_TOP = 4;
    public static final int CELL_MARGIN_BOTTOM = 12;
    public static final int CELL_MARGIN_RIGHT = 12;
    
    public static final int CELL_BUTTON_MAXHEIGHT = 17;
    /** how much space is needed for adding a new LayoutPanel to a Cell */
    public static final int MINIMUM_WIDTH_FOR_EMBEDDED_LAYOUT = 100;
    public static final int MINIMUM_HEIGHT_FOR_EMBEDDED_LAYOUT = 60;
    
    /** how much can a cell be greater to fit a component */
    public static final int TOLERANCE_HEIGHT_EXISTING_CELL = 150;
    public static final int TOLERANCE_WIDTH_EXISTING_CELL = 150;
    
    /** splitpane dividersize min and max value */
    public static final int SPLITPANE_DIVIDERSIZE_MIN = 5;
    public static final int SPLITPANE_DIVIDERSIZE_MAX = 20;
    
    public static final int SEPARATOR_MIN_WIDTH = 3;
    public static final int SEPARATOR_MIN_LENGTH = 20;
    
    /** default size for added columns/ rows */
   //NUCLEUSINT-966
    public static final int DEFAULT_COLUMN_WIDTH = 50;
    public static final int DEFAULT_ROW_HEIGHT = 22;
    
    public static final int FONT_MAXIMUM_SIZE = 50;
    public static final int FONT_MINIMUM_SIZE = -5;
}
