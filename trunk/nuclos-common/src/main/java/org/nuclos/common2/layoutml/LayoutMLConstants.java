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
package org.nuclos.common2.layoutml;

import java.awt.Dimension;

/**
 * Constants for the LayoutML.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */

public interface LayoutMLConstants {
	/**
	 * the system identifier (URI) for the LayoutML DTD.
	 */
	public static final String LAYOUTML_DTD_SYSTEMIDENTIFIER = "http://www.novabit.de/technologies/layoutml/layoutml.dtd";

	/**
	 * the resource path of the bundled LayoutML DTD
	 */
	public static final String LAYOUTML_DTD_RESSOURCEPATH = "org/nuclos/common2/layoutml/layoutml.dtd";

	// constants for XML elements used in the LayoutML:
	public static final String ELEMENT_PANEL = "panel";
	public static final String ELEMENT_EMPTYPANEL = "empty-panel";
	public static final String ELEMENT_LABEL = "label";
	public static final String ELEMENT_TEXTFIELD = "textfield";
	//NUCLEUSINT-1142
	public static final String ELEMENT_PASSWORD = "password";
	public static final String ELEMENT_TEXTAREA = "textarea";
	public static final String ELEMENT_COMBOBOX = "combobox";
	public static final String ELEMENT_CHECKBOX = "checkbox";
	public static final String ELEMENT_BUTTON = "button";
	public static final String ELEMENT_COLLECTABLECOMPONENT = "collectable-component";
	public static final String ELEMENT_TABBEDPANE = "tabbedpane";
	public static final String ELEMENT_SCROLLPANE = "scrollpane";
	public static final String ELEMENT_SPLITPANE = "splitpane";
	public static final String ELEMENT_SUBFORM = "subform";
	public static final String ELEMENT_SUBFORMCOLUMN = "subform-column";
	public static final String ELEMENT_DESCRIPTION = "description";
	public static final String ELEMENT_BORDERLAYOUT = "borderlayout";
	public static final String ELEMENT_FLOWLAYOUT = "flowlayout";
	public static final String ELEMENT_GRIDLAYOUT = "gridlayout";
	public static final String ELEMENT_GRIDBAGLAYOUT = "gridbaglayout";
	public static final String ELEMENT_TABLELAYOUT = "tablelayout";
	public static final String ELEMENT_BOXLAYOUT = "boxlayout";
	public static final String ELEMENT_ROWLAYOUT = "rowlayout";
	public static final String ELEMENT_COLUMNLAYOUT = "columnlayout";
	public static final String ELEMENT_BORDERLAYOUTCONSTRAINTS = "borderlayout-constraints";
	public static final String ELEMENT_GRIDBAGCONSTRAINTS = "gridbag-constraints";
	public static final String ELEMENT_TABLELAYOUTCONSTRAINTS = "tablelayout-constraints";
	public static final String ELEMENT_TABBEDPANECONSTRAINTS = "tabbedpane-constraints";
	public static final String ELEMENT_SPLITPANECONSTRAINTS = "splitpane-constraints";
	public static final String ELEMENT_BACKGROUND = "background";
	public static final String ELEMENT_MINIMUMSIZE = "minimum-size";
	public static final String ELEMENT_PREFERREDSIZE = "preferred-size";
	public static final String ELEMENT_CLEARBORDER = "clear-border";
	public static final String ELEMENT_EMPTYBORDER = "empty-border";
	public static final String ELEMENT_ETCHEDBORDER = "etched-border";
	public static final String ELEMENT_TITLEDBORDER = "titled-border";
	public static final String ELEMENT_BEVELBORDER = "bevel-border";
	public static final String ELEMENT_LINEBORDER = "line-border";
	public static final String ELEMENT_SEPARATOR = "separator";
	public static final String ELEMENT_TITLEDSEPARATOR = "titled-separator";
	public static final String ELEMENT_IMAGE = "image";
	public static final String ELEMENT_OPTIONS = "options";
	public static final String ELEMENT_OPTION = "option";
	public static final String ELEMENT_OPTIONGROUP = "optiongroup";
	public static final String ELEMENT_RULES = "rules";
	public static final String ELEMENT_RULE = "rule";
	public static final String ELEMENT_EVENT = "event";
	public static final String ELEMENT_CONDITION = "condition";
	public static final String ELEMENT_ACTIONS = "actions";
	public static final String ELEMENT_TRANSFERLOOKEDUPVALUE = "transfer-lookedup-value";
	public static final String ELEMENT_CLEAR = "clear";
	public static final String ELEMENT_ENABLE = "enable";
	public static final String ELEMENT_REFRESHVALUELIST = "refresh-valuelist";
	public static final String ELEMENT_FONT = "font";
	public static final String ELEMENT_COLLECTABLEFIELD = "collectable-field";
	public static final String ELEMENT_DEPENDENCY = "dependency";
	public static final String ELEMENT_DEPENDENCIES = "dependencies";
	public static final String ELEMENT_VALUELISTPROVIDER = "valuelist-provider";
	public static final String ELEMENT_PARAMETER = "parameter";
	public static final String ELEMENT_PROPERTY = "property";
	public static final String ELEMENT_INITIALFOCUSCOMPONENT = "initial-focus-component";
	public static final String ELEMENT_INITIALSORTINGORDER = "initial-sorting-order";
	public static final String ELEMENT_TRANSLATIONS = "translations";
	public static final String ELEMENT_TRANSLATION = "translation";

	// constants for XML attributes used in the LayoutML:
	public static final String ATTRIBUTE_NAME = "name";
	public static final String ATTRIBUTE_TEXT = "text";
	public static final String ATTRIBUTE_ROWS = "rows";
	public static final String ATTRIBUTE_COLUMNS = "columns";
	public static final String ATTRIBUTE_TITLE = "title";
	public static final String ATTRIBUTE_POSITION = "position";
	public static final String ATTRIBUTE_RED = "red";
	public static final String ATTRIBUTE_GREEN = "green";
	public static final String ATTRIBUTE_BLUE = "blue";
	public static final String ATTRIBUTE_ALIGN = "align";
	public static final String ATTRIBUTE_WIDTH = "width";
	public static final String ATTRIBUTE_HEIGHT = "height";
	public static final String ATTRIBUTE_TOP = "top";
	public static final String ATTRIBUTE_LEFT = "left";
	public static final String ATTRIBUTE_BOTTOM = "bottom";
	public static final String ATTRIBUTE_RIGHT = "right";
	public static final String ATTRIBUTE_TYPE = "type";
	public static final String ATTRIBUTE_HGAP = "hgap";
	public static final String ATTRIBUTE_VGAP = "vgap";
	public static final String ATTRIBUTE_GAP = "gap";
	public static final String ATTRIBUTE_ENABLED = "enabled";
	public static final String ATTRIBUTE_INVERTABLE = "invertable";
	public static final String ATTRIBUTE_EDITABLE = "editable";
	public static final String ATTRIBUTE_INSERTABLE = "insertable";
	public static final String ATTRIBUTE_SCALABLE = "scalable";
	public static final String ATTRIBUTE_ASPECTRATIO = "aspectratio";
	public static final String ATTRIBUTE_TABLAYOUTPOLICY = "tablayoutpolicy";
	public static final String ATTRIBUTE_TABPLACEMENT = "tabplacement";
	public static final String ATTRIBUTE_GRIDX = "gridx";
	public static final String ATTRIBUTE_GRIDY = "gridy";
	public static final String ATTRIBUTE_GRIDWIDTH = "gridwidth";
	public static final String ATTRIBUTE_GRIDHEIGHT = "gridheight";
	public static final String ATTRIBUTE_WEIGHTX = "weightx";
	public static final String ATTRIBUTE_WEIGHTY = "weighty";
	public static final String ATTRIBUTE_ANCHOR = "anchor";
	public static final String ATTRIBUTE_FILL = "fill";
	public static final String ATTRIBUTE_FILLHORIZONTALLY = "fill-horizontally";
	public static final String ATTRIBUTE_FILLVERTICALLY = "fill-vertically";
	public static final String ATTRIBUTE_PADX = "padx";
	public static final String ATTRIBUTE_PADY = "pady";
	public static final String ATTRIBUTE_INSETTOP = "insettop";
	public static final String ATTRIBUTE_INSETLEFT = "insetleft";
	public static final String ATTRIBUTE_INSETBOTTOM = "insetbottom";
	public static final String ATTRIBUTE_INSETRIGHT = "insetright";
	public static final String ATTRIBUTE_LABEL = "label";
	public static final String ATTRIBUTE_TOOLTIP = "tooltip";
	public static final String ATTRIBUTE_HORIZONTALSCROLLBARPOLICY = "horizontalscrollbar";
	public static final String ATTRIBUTE_VERTICALSCROLLBARPOLICY = "verticalscrollbar";
	public static final String ATTRIBUTE_CONTROLTYPE = "controltype";
	public static final String ATTRIBUTE_CONTROLTYPECLASS = "controltypeclass";
	public static final String ATTRIBUTE_MNEMONIC = "mnemonic";
	public static final String ATTRIBUTE_ORIENTATION = "orientation";
	public static final String ATTRIBUTE_DIVIDERSIZE = "dividersize";
	public static final String ATTRIBUTE_EXPANDABLE = "expandable";
	public static final String ATTRIBUTE_CONTINUOUSLAYOUT = "continuous-layout";
	public static final String ATTRIBUTE_RESIZEWEIGHT = "resizeweight";
	public static final String ATTRIBUTE_AXIS = "axis";
	public static final String ATTRIBUTE_ID = "id";
	public static final String ATTRIBUTE_OPAQUE = "opaque";
	public static final String ATTRIBUTE_CONSTRAINTS = "constraints";
	public static final String ATTRIBUTE_ENTITY = "entity";
	public static final String ATTRIBUTE_ACTIONCOMMAND = "actioncommand";
	public static final String ATTRIBUTE_VALUE = "value";
	public static final String ATTRIBUTE_DEFAULT = "default";
	public static final String ATTRIBUTE_SHOWONLY = "show-only";
	public static final String ATTRIBUTE_TOOLBARORIENTATION = "toolbarorientation";
	public static final String ATTRIBUTE_THICKNESS = "thickness";
	public static final String ATTRIBUTE_SOURCEFIELD = "sourcefield";
	public static final String ATTRIBUTE_SOURCECOMPONENT = "sourcecomponent";
	public static final String ATTRIBUTE_TARGETCOMPONENT = "targetcomponent";
	public static final String ATTRIBUTE_PARENTCOMPONENT = "parentcomponent";
	public static final String ATTRIBUTE_VISIBLE = "visible";
	public static final String ATTRIBUTE_FILLCONTROLHORIZONTALLY = "fill-control-horizontally";
	public static final String ATTRIBUTE_SIZE = "size";
	public static final String ATTRIBUTE_DEPENDANTFIELD = "dependant-field";
	public static final String ATTRIBUTE_DEPENDSONFIELD = "depends-on";
	public static final String ATTRIBUTE_PARAMETER_FOR_SOURCECOMPONENT = "parameter-for-sourcecomponent";
	public static final String ATTRIBUTE_UNIQUEMASTERCOLUMN = "unique-mastercolumn";
	public static final String ATTRIBUTE_CONTROLLERTYPE = "controllertype";
	public static final String ATTRIBUTE_FOREIGNKEYFIELDTOPARENT = "foreignkeyfield-to-parent";
	public static final String ATTRIBUTE_SORTINGORDER = "sorting-order";
	public static final String ATTRIBUTE_PARENTSUBFORM = "parent-subform";
	public static final String ATTRIBUTE_COL1 = "col1";
	public static final String ATTRIBUTE_COL2 = "col2";
	public static final String ATTRIBUTE_ROW1 = "row1";
	public static final String ATTRIBUTE_ROW2 = "row2";
	public static final String ATTRIBUTE_HALIGN = "hAlign";
	public static final String ATTRIBUTE_VALIGN = "vAlign";
	@Deprecated /** @deprecated resourceId is replaced with nested translation elements; use this attribute only for migration. */
	public static final String ATTRIBUTE_LOCALERESOURCEID = "resourceId";
	public static final String ATTRIBUTE_LANG = "lang";
	public static final String ATTRIBUTE_INTERNALNAME = "internalname";
	public static final String ATTRIBUTE_COLUMNWIDTH = "width";
	public static final String ATTRIBUTE_NEXTFOCUSCOMPONENT = "nextfocuscomponent";
	public static final String ATTRIBUTE_ICON = "icon";


	// constants for XML attribute values used in the LayoutML:
	public static final String ATTRIBUTEVALUE_LOWERED = "lowered";
	public static final String ATTRIBUTEVALUE_RAISED = "raised";
	public static final String ATTRIBUTEVALUE_YES = "yes";
	public static final String ATTRIBUTEVALUE_NO = "no";
	public static final String ATTRIBUTEVALUE_TEXTFIELD = ELEMENT_TEXTFIELD;
	public static final String ATTRIBUTEVALUE_IDTEXTFIELD = "id-textfield";
	public static final String ATTRIBUTEVALUE_COMBOBOX = ELEMENT_COMBOBOX;
	public static final String ATTRIBUTEVALUE_CHECKBOX = ELEMENT_CHECKBOX;
	public static final String ATTRIBUTEVALUE_TEXTAREA = ELEMENT_TEXTAREA;
	public static final String ATTRIBUTEVALUE_LISTOFVALUES = "listofvalues";
	public static final String ATTRIBUTEVALUE_DATECHOOSER = "datechooser";
	public static final String ATTRIBUTEVALUE_OPTIONGROUP = ELEMENT_OPTIONGROUP;
	public static final String ATTRIBUTEVALUE_FILECHOOSER = "filechooser";
	public static final String ATTRIBUTEVALUE_IMAGE = "image";
	//NUCLEUSINT-1142
	public static final String ATTRIBUTEVALUE_PASSWORDFIELD = ELEMENT_PASSWORD;
	public static final String ATTRIBUTEVALUE_X = "x";
	public static final String ATTRIBUTEVALUE_Y = "y";
	public static final String ATTRIBUTEVALUE_LABEL = "label";
	public static final String ATTRIBUTEVALUE_CONTROL = "control";
	public static final String ATTRIBUTEVALUE_BROWSEBUTTON = "browsebutton";
	public static final String ATTRIBUTEVALUE_LOOKUP = "lookup";
	public static final String ATTRIBUTEVALUE_VALUECHANGED = "value-changed";
	public static final String ATTRIBUTEVALUE_DEFAULT = "default";
	public static final String ATTRIBUTEVALUE_DEPENDANT = "dependant";
	public static final String ATTRIBUTEVALUE_HORIZONTAL = "horizontal";
	public static final String ATTRIBUTEVALUE_VERTICAL = "vertical";
	public static final String ATTRIBUTEVALUE_LEFT = "left";
	public static final String ATTRIBUTEVALUE_RIGHT = "right";
	public static final String ATTRIBUTEVALUE_TOP = "top";
	public static final String ATTRIBUTEVALUE_BOTTOM = "bottom";
	public static final String ATTRIBUTEVALUE_SCROLL = "scroll";
	public static final String ATTRIBUTEVALUE_WRAP = "wrap";
	public static final String ATTRIBUTEVALUE_ASCENDING = "ascending";
	public static final String ATTRIBUTEVALUE_DESCENDING = "descending";
	public static final String ATTRIBUTEVALUE_ASNEEDED = "asneeded";
	public static final String ATTRIBUTEVALUE_ALWAYS = "always";
	public static final String aTTRIBUTEVALUE_NEVER = "never";
	public static final String ATTRIBUTEVALUE_HIDE = "hide";

	// controltypes
	public static final String CONTROLTYPE_COMBOBOX = "combobox";
	public static final String CONTROLTYPE_CHECKBOX = "checkbox";
	public static final String CONTROLTYPE_TEXTFIELD = "textfield";
	//NUCLEUSINT-1142
	public static final String CONTROLTYPE_PASSWORDFIELD = "password";
	public static final String CONTROLTYPE_IMAGE = "image";
	public static final String CONTROLTYPE_DATECHOOSER = "datechooser";
	public static final String CONTROLTYPE_LISTOFVALUES = "listofvalues";
	public static final String CONTROLTYPE_TEXTAREA = "textarea";
	public static final String CONTROLTYPE_OPTIONGROUP = "optiongroup";
	public static final String CONTROLTYPE_LABEL = "label";

	// default element attribute values //NUCLEUSINT-485
	public static final Dimension DEFAULTVALUE_BUTTON_MINIMUMSIZE = new Dimension(15, 22);
	public static final Dimension DEFAULTVALUE_BUTTON_PREFERREDSIZE = new Dimension(50, 22);
	public static final Dimension DEFAULTVALUE_LABEL_MINIMUMSIZE = new Dimension(15, 22);
	public static final Dimension DEFAULTVALUE_LABEL_PREFERREDSIZE = new Dimension(50, 22);
	public static final Dimension DEFAULTVALUE_CHECKBOX_MINIMUMSIZE = new Dimension(19, 22);
	public static final Dimension DEFAULTVALUE_CHECKBOX_PREFERREDSIZE = new Dimension(20, 22);
	public static final Dimension DEFAULTVALUE_TEXTFIELD_MINIMUMSIZE = new Dimension(35, 22);
	public static final Dimension DEFAULTVALUE_TEXTFIELD_PREFERREDSIZE = new Dimension(70, 22);
	public static final Dimension DEFAULTVALUE_COMBOBOX_MINIMUMSIZE = new Dimension(35, 23);
	public static final Dimension DEFAULTVALUE_COMBOBOX_PREFERREDSIZE = new Dimension(70, 23);
	public static final Dimension DEFAULTVALUE_DATECHOOSER_MINIMUMSIZE = new Dimension(35, 22);
	public static final Dimension DEFAULTVALUE_DATECHOOSER_PREFERREDSIZE = new Dimension(90, 22);
	public static final Dimension DEFAULTVALUE_OPTIONGROUP_MINIMUMSIZE = new Dimension(35, 22);
	public static final Dimension DEFAULTVALUE_OPTIONGROUP_PREFERREDSIZE = new Dimension(70, 22);
	public static final Dimension DEFAULTVALUE_LISTOFVALUES_MINIMUMSIZE = new Dimension(35, 22);
	public static final Dimension DEFAULTVALUE_LISTOFVALUES_PREFERREDSIZE = new Dimension(70, 22);
	public static final Dimension DEFAULTVALUE_TEXTAREA_MINIMUMSIZE = new Dimension(35, 22);
	public static final Dimension DEFAULTVALUE_TEXTAREA_PREFERREDSIZE = new Dimension(70, 22);

	public static final Dimension DEFAULTVALUE_TABBEDPANE_MINIMUMSIZE = new Dimension(180, 40);
	public static final Dimension DEFAULTVALUE_TABBEDPANE_PREFERREDSIZE = new Dimension(300, 100);
	public static final Dimension DEFAULTVALUE_SUBFORM_MINIMUMSIZE = new Dimension(180, 40);
	public static final Dimension DEFAULTVALUE_SUBFORM_PREFERREDSIZE = new Dimension(300, 100);
	public static final Dimension DEFAULTVALUE_SCROLLPANE_MINIMUMSIZE = new Dimension(180, 40);
	public static final Dimension DEFAULTVALUE_SCROLLPANE_PREFERREDSIZE = new Dimension(300, 100);
	public static final Dimension DEFAULTVALUE_SPLITPANE_MINIMUMSIZE = new Dimension(180, 40);
	public static final Dimension DEFAULTVALUE_SPLITPANE_PREFERREDSIZE = new Dimension(300, 100);

	public static final int DEFAULTVALUE_TEXTAREA_COLUMNS = 30;
	public static final int DEFAULTVALUE_TEXTAREA_ROWS = 3;

	public static final int DEFAULTVALUE_TEXTFIELD_COLUMNS = 30;

	public static final int DEFAULTVALUE_SPLITPANE_DIVIDERSIZE = 5;

	public static final String DEFAULTVALUE_SEPARATOR_ORIENTATION= ATTRIBUTEVALUE_HORIZONTAL;
}	// interface LayoutMLConstants
