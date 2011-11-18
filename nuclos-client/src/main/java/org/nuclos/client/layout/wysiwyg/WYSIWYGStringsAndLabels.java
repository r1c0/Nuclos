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
package org.nuclos.client.layout.wysiwyg;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.nuclos.client.layout.wysiwyg.editor.util.InterfaceGuidelines;


/**
 * This class contains all used Strings and Messages shown in the WYSIWYGEditor.
 * They are sorted by Class and Category for structuring all the Messages.
 * 
 * <br>
 * Created by Novabit Informationssysteme GmbH <br>
 * Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 * 
 * @author <a href="mailto:hartmut.beckschulze@novabit.de">hartmut.beckschulze</a>
 * @version 01.00.00
 * @deprecated Should be done with the help of CommonLocaleDelegate and localeresource.json.
 */
public class WYSIWYGStringsAndLabels {

	/** this marker is used by partedString(String message, String... additionalInfo) for concating Strings in Messages/ Dialogs. */
	private static final String MARKER = "@EXTRATEXT@";
	
	/** the Modes the WYSIWYG Editor can be run in */
	public static final String STANDARD_MODE = "Standardmodus";
	public static final String EXPERT_MODE = "Expertenmodus";

	/**
	 * @deprecated Should be done with the help of CommonLocaleDelegate and localeresource.json.
	 */
	public static class ERROR_MESSAGES {
		public static final String PARENT_NO_WYSIWYG = "Elternelement ist kein WYSIWYGLayoutEditorPanel!";
		public static final String CLONE_NOT_SUPPORTED = "Operation .clone wird von " + MARKER + " nicht unterst\u00fctzt!";
		//NUCLEUSINT-398
		public static final String INVALID_LAYOUTML = "Die LayoutML ist fehlerhaft und hat Strukturfehler!\n";
		//NUCLEUSINT-398
		public static final String XML_MISSING_CLOSETAG = INVALID_LAYOUTML + "\nEs fehlt ein schlie\u00dfender Tag im XML."; 
		//NUCLEUSINT-261
		public static final String MISSING_ASSIGNMENT_GO = "Es muss eine Verwendung angegeben werden damit die Metainformationen f\u00fcr die Komponenten passend geladen werden k\u00f6nnen.";
		public static final String MISSING_ASSIGNMENT_MD = "Es muss eine Verwendung angegeben werden damit die zugeh\u00f6rigen Entit\u00e4ten geladen werden k\u00f6nnen.";
		
		public static final String ERROR_VALIDATING_LAYOUTMLRULES = "Die eingegebenen Regeln sind so nicht valide.\nBitte beheben sie die Fehler (siehe unten) und speichern sie erneut.\n";
		public static final String TEXT_MISSING = "ist leer";
		//NUCLEUSINT-1137
		public static final String ENTITY_USED_IN_LAYOUT_MISSING = "Entity \"" + MARKER + "\" ist nicht mehr vorhanden!";
	}

	/**
	 * @deprecated Should be done with the help of CommonLocaleDelegate and localeresource.json.
	 */
	public static class COLLECTABLE_COMPONENT implements PaletteItemElement {
		public static final String PROPERTY_LABEL = "Eigenschaften";
		public static final String STRING_WAITINGFORMETA = "Bitte w\u00e4hlen Sie ein Feld aus.";
		public static final String PALETTE_ELEMENTNAME = "Universelle Komponente";
		public static final String PALETTE_ELEMENTTOOLTIP = "Universelle Komponente (Art des Feldes wird unter anderem durch Metadaten bestimmt)";
		public static final String PALETTE_ELEMENTICON = "org/nuclos/client/layout/wysiwyg/palette/images/JComponentColor16.gif";
		
		// NUCLEUSINT-269
		public static final String PROPERTY_VALIDATION_MESSAGE_CONTROLTYPECLASS_NOT_FOUND = MARKER + " konnte nicht gesetzt werden weil die Klasse nicht exisitiert.";
		public static final String PROPERTY_VALIDATION_MESSAGE_CONTROLTYPE_NOT_VALID = MARKER + " ist kein valider Control Typ f\u00fcr diese Entit\u00e4t";

		@Override
		public Icon getIcon() {
			if ("".equals(PALETTE_ELEMENTICON))
				return null;
			return new ImageIcon(this.getClass().getClassLoader().getResource(PALETTE_ELEMENTICON));
		}
		
		@Override
		public String getLabel() {
			return PALETTE_ELEMENTNAME;
		}
		
		@Override
		public String getToolTip() {
			return PALETTE_ELEMENTTOOLTIP;
		}
		
		@Override
		public boolean displayLabelAndIcon() {
			return true;
		}

		@Override
		public boolean isLabeledComponent() {
			return false;
		}
	}
	
	/**
	 * @deprecated Should be done with the help of CommonLocaleDelegate and localeresource.json.
	 */
	public static class COLLECTABLE_CHECKBOX implements PaletteItemElement {
		public static final String PROPERTY_LABEL = "Eigenschaften";

		public static final String PALETTE_ELEMENTNAME = "Checkbox";
		public static final String PALETTE_ELEMENTTOOLTIP = "Feld f\u00fcr Ja/Nein Werte";
		public static final String PALETTE_ELEMENTICON = "org/nuclos/client/layout/wysiwyg/palette/images/JCheckBoxColor16.gif";

		@Override
		public Icon getIcon() {
			if ("".equals(PALETTE_ELEMENTICON))
				return null;
			return new ImageIcon(this.getClass().getClassLoader().getResource(PALETTE_ELEMENTICON));
		}
		@Override
		public String getLabel() {
			return PALETTE_ELEMENTNAME;
		}
		@Override
		public String getToolTip() {
			return PALETTE_ELEMENTTOOLTIP;
		}
		@Override
		public boolean displayLabelAndIcon() {
			return true;
		}
		@Override
		public boolean isLabeledComponent() {
			return false;
		}
	}
	
	/**
	 * @deprecated Should be done with the help of CommonLocaleDelegate and localeresource.json.
	 */
	// NUCLEUSINT-496
	public static class COLLECTABLE_LABELED_CHECKBOX implements PaletteItemElement {
		public static final String PALETTE_ELEMENTNAME = "Checkbox mit Label";
		public static final String PALETTE_ELEMENTTOOLTIP = "Feld f\u00fcr Ja/Nein Werte";
		public static final String PALETTE_ELEMENTICON = "org/nuclos/client/layout/wysiwyg/palette/images/JCheckBoxColor16.gif";

		@Override
		public Icon getIcon() {
			if ("".equals(PALETTE_ELEMENTICON))
				return null;
			return new ImageIcon(this.getClass().getClassLoader().getResource(PALETTE_ELEMENTICON));
		}
		@Override
		public String getLabel() {
			return PALETTE_ELEMENTNAME;
		}
		@Override
		public String getToolTip() {
			return PALETTE_ELEMENTTOOLTIP;
		}
		@Override
		public boolean displayLabelAndIcon() {
			return true;
		}
		@Override
		public boolean isLabeledComponent() {
			return true;
		}
	}

	/**
	 * @deprecated Should be done with the help of CommonLocaleDelegate and localeresource.json.
	 */
	public static class COLLECTABLE_COMBOBOX implements PaletteItemElement {
		public static final String PROPERTY_LABEL = "Eigenschaften";

		public static final String PALETTE_ELEMENTNAME = "Combobox";
		public static final String PALETTE_ELEMENTICON = "org/nuclos/client/layout/wysiwyg/palette/images/JComboBoxColor16.gif";
		public static final String PALETTE_ELEMENTTOOLTIP = "Auswahlfeld";

		@Override
		public Icon getIcon() {
			if ("".equals(PALETTE_ELEMENTICON))
				return null;
			return new ImageIcon(this.getClass().getClassLoader().getResource(PALETTE_ELEMENTICON));
		}
		@Override
		public String getLabel() {
			return PALETTE_ELEMENTNAME;
		}
		@Override
		public String getToolTip() {
			return PALETTE_ELEMENTTOOLTIP;
		}
		@Override
		public boolean displayLabelAndIcon() {
			return false;
		}
		@Override
		public boolean isLabeledComponent() {
			return false;
		}
	}
	
	/**
	 * @deprecated Should be done with the help of CommonLocaleDelegate and localeresource.json.
	 */
	// NUCLEUSINT-496
	public static class COLLECTABLE_LABELED_COMBOBOX implements PaletteItemElement {
		public static final String PALETTE_ELEMENTNAME = "Combobox mit Label";
		public static final String PALETTE_ELEMENTICON = "org/nuclos/client/layout/wysiwyg/palette/images/JComboBoxColor16.gif";
		public static final String PALETTE_ELEMENTTOOLTIP = "Auswahlfeld";

		@Override
		public Icon getIcon() {
			if ("".equals(PALETTE_ELEMENTICON))
				return null;
			return new ImageIcon(this.getClass().getClassLoader().getResource(PALETTE_ELEMENTICON));
		}
		@Override
		public String getLabel() {
			return PALETTE_ELEMENTNAME;
		}
		@Override
		public String getToolTip() {
			return PALETTE_ELEMENTTOOLTIP;
		}
		@Override
		public boolean displayLabelAndIcon() {
			return false;
		}
		@Override
		public boolean isLabeledComponent() {
			return true;
		}
	}

	/**
	 * @deprecated Should be done with the help of CommonLocaleDelegate and localeresource.json.
	 */
	public static class COLLECTABLE_DATECHOOSER implements PaletteItemElement {
		public static final String PROPERTY_LABEL = "Eigenschaften";

		public static final String PALETTE_ELEMENTNAME = "Datechooser";
		public static final String PALETTE_ELEMENTICON = "org/nuclos/client/layout/wysiwyg/palette/images/JTextFieldColor16.gif";
		public static final String PALETTE_ELEMENTTOOLTIP = "Komponente um Datumswerte auszuw\u00e4hlen";

		@Override
		public Icon getIcon() {
			if ("".equals(PALETTE_ELEMENTICON))
				return null;
			return new ImageIcon(this.getClass().getClassLoader().getResource(PALETTE_ELEMENTICON));
		}
		@Override
		public String getLabel() {
			return PALETTE_ELEMENTNAME;
		}
		@Override
		public String getToolTip() {
			return PALETTE_ELEMENTTOOLTIP;
		}
		@Override
		public boolean displayLabelAndIcon() {
			return false;
		}
		@Override
		public boolean isLabeledComponent() {
			return false;
		}
	}
	
	/**
	 * @deprecated Should be done with the help of CommonLocaleDelegate and localeresource.json.
	 */
	// NUCLEUSINT-496
	public static class COLLECTABLE_LABELED_DATECHOOSER implements PaletteItemElement {
		public static final String PALETTE_ELEMENTNAME = "Datechooser mit Label";
		public static final String PALETTE_ELEMENTICON = "org/nuclos/client/layout/wysiwyg/palette/images/JTextFieldColor16.gif";
		public static final String PALETTE_ELEMENTTOOLTIP = "Komponente um Datumswerte auszuw\u00e4hlen";

		@Override
		public Icon getIcon() {
			if ("".equals(PALETTE_ELEMENTICON))
				return null;
			return new ImageIcon(this.getClass().getClassLoader().getResource(PALETTE_ELEMENTICON));
		}
		@Override
		public String getLabel() {
			return PALETTE_ELEMENTNAME;
		}
		@Override
		public String getToolTip() {
			return PALETTE_ELEMENTTOOLTIP;
		}
		@Override
		public boolean displayLabelAndIcon() {
			return false;
		}
		@Override
		public boolean isLabeledComponent() {
			return true;
		}
	}

	/**
	 * @deprecated Should be done with the help of CommonLocaleDelegate and localeresource.json.
	 */
	public static class COLLECTABLE_LABEL implements PaletteItemElement {
		public static final String PROPERTY_LABEL = "Eigenschaften";

		public static final String PALETTE_ELEMENTNAME = "Label";
		public static final String PALETTE_ELEMENTICON = "org/nuclos/client/layout/wysiwyg/palette/images/JLabelColor16.gif";
		public static final String PALETTE_ELEMENTTOOLTIP = "Beschriftungsfeld";

		@Override
		public Icon getIcon() {
			if ("".equals(PALETTE_ELEMENTICON))
				return null;
			return new ImageIcon(this.getClass().getClassLoader().getResource(PALETTE_ELEMENTICON));
		}
		@Override
		public String getLabel() {
			return PALETTE_ELEMENTNAME;
		}
		@Override
		public String getToolTip() {
			return PALETTE_ELEMENTTOOLTIP;
		}
		@Override
		public boolean displayLabelAndIcon() {
			return false;
		}
		@Override
		public boolean isLabeledComponent() {
			return false;
		}
	}

	/**
	 * @deprecated Should be done with the help of CommonLocaleDelegate and localeresource.json.
	 */
	public static class COLLECTABLE_LISTOFVALUES implements PaletteItemElement {
		public static final String PROPERTY_LABEL = "Eigenschaften";

		public static final String PALETTE_ELEMENTNAME = "List of Values";
		public static final String PALETTE_ELEMENTICON = "org/nuclos/client/layout/wysiwyg/palette/images/JTextFieldColor16.gif";
		public static final String PALETTE_ELEMENTTOOLTIP = "Komponente zum Heraussuchen von Werten";

		@Override
		public Icon getIcon() {
			if ("".equals(PALETTE_ELEMENTICON))
				return null;
			return new ImageIcon(this.getClass().getClassLoader().getResource(PALETTE_ELEMENTICON));
		}
		@Override
		public String getLabel() {
			return PALETTE_ELEMENTNAME;
		}
		@Override
		public String getToolTip() {
			return PALETTE_ELEMENTTOOLTIP;
		}
		@Override
		public boolean displayLabelAndIcon() {
			return false;
		}
		@Override
		public boolean isLabeledComponent() {
			return false;
		}
	}
	
	/**
	 * @deprecated Should be done with the help of CommonLocaleDelegate and localeresource.json.
	 */
	// NUCLEUSINT-496
	public static class COLLECTABLE_LABELED_LISTOFVALUES implements PaletteItemElement {
		public static final String PALETTE_ELEMENTNAME = "List of Values mit Label";
		public static final String PALETTE_ELEMENTICON = "org/nuclos/client/layout/wysiwyg/palette/images/JTextFieldColor16.gif";
		public static final String PALETTE_ELEMENTTOOLTIP = "Komponente zum Heraussuchen von Werten";

		@Override
		public Icon getIcon() {
			if ("".equals(PALETTE_ELEMENTICON))
				return null;
			return new ImageIcon(this.getClass().getClassLoader().getResource(PALETTE_ELEMENTICON));
		}
		@Override
		public String getLabel() {
			return PALETTE_ELEMENTNAME;
		}
		@Override
		public String getToolTip() {
			return PALETTE_ELEMENTTOOLTIP;
		}
		@Override
		public boolean displayLabelAndIcon() {
			return false;
		}
		@Override
		public boolean isLabeledComponent() {
			return true;
		}
	}

	/**
	 * @deprecated Should be done with the help of CommonLocaleDelegate and localeresource.json.
	 */
	public static class COLLECTABLE_OPTIONGROUP implements PaletteItemElement {
		public static final String PROPERTY_LABEL = "Eigenschaften";

		public static final String PALETTE_ELEMENTNAME = "Optiongroup";
		public static final String PALETTE_ELEMENTICON = "org/nuclos/client/layout/wysiwyg/palette/images/JRadioButtonColor16.gif";
		public static final String PALETTE_ELEMENTTOOLTIP = "Feld zur Auswahl eines Werts";

		@Override
		public Icon getIcon() {
			if ("".equals(PALETTE_ELEMENTICON))
				return null;
			return new ImageIcon(this.getClass().getClassLoader().getResource(PALETTE_ELEMENTICON));
		}
		@Override
		public String getLabel() {
			return PALETTE_ELEMENTNAME;
		}
		@Override
		public String getToolTip() {
			return PALETTE_ELEMENTTOOLTIP;
		}
		@Override
		public boolean displayLabelAndIcon() {
			return true;
		}
		@Override
		public boolean isLabeledComponent() {
			return false;
		}
	}
	
	/**
	 * @deprecated Should be done with the help of CommonLocaleDelegate and localeresource.json.
	 */
	// NUCLEUSINT-496
	public static class COLLECTABLE_LABELED_OPTIONGROUP implements PaletteItemElement {
		public static final String PALETTE_ELEMENTNAME = "Optiongroup mit Label";
		public static final String PALETTE_ELEMENTICON = "org/nuclos/client/layout/wysiwyg/palette/images/JRadioButtonColor16.gif";
		public static final String PALETTE_ELEMENTTOOLTIP = "Feld zur Auswahl eines Werts";

		@Override
		public Icon getIcon() {
			if ("".equals(PALETTE_ELEMENTICON))
				return null;
			return new ImageIcon(this.getClass().getClassLoader().getResource(PALETTE_ELEMENTICON));
		}
		@Override
		public String getLabel() {
			return PALETTE_ELEMENTNAME;
		}
		@Override
		public String getToolTip() {
			return PALETTE_ELEMENTTOOLTIP;
		}
		@Override
		public boolean displayLabelAndIcon() {
			return true;
		}
		@Override
		public boolean isLabeledComponent() {
			return true;
		}
	}
	
	/**
	 * @deprecated Should be done with the help of CommonLocaleDelegate and localeresource.json.
	 */
	public static class COLLECTABLE_TEXTAREA implements PaletteItemElement {
		public static final String PROPERTY_LABEL = "Eigenschaften";

		public static final String PALETTE_ELEMENTNAME = "Textarea";
		public static final String PALETTE_ELEMENTICON = "org/nuclos/client/layout/wysiwyg/palette/images/JTextAreaColor16.gif";
		public static final String PALETTE_ELEMENTTOOLTIP = "Komponente f\u00fcr mehrzeilige Texteingaben";

		@Override
		public Icon getIcon() {
			if ("".equals(PALETTE_ELEMENTICON))
				return null;
			return new ImageIcon(this.getClass().getClassLoader().getResource(PALETTE_ELEMENTICON));
		}
		@Override
		public String getLabel() {
			return PALETTE_ELEMENTNAME;
		}
		@Override
		public String getToolTip() {
			return PALETTE_ELEMENTTOOLTIP;
		}
		@Override
		public boolean displayLabelAndIcon() {
			return false;
		}
		@Override
		public boolean isLabeledComponent() {
			return false;
		}
	}
	
	/**
	 * @deprecated Should be done with the help of CommonLocaleDelegate and localeresource.json.
	 */
	// NUCLEUSINT-496
	public static class COLLECTABLE_LABELED_TEXTAREA implements PaletteItemElement {
		public static final String PALETTE_ELEMENTNAME = "Textarea mit Label";
		public static final String PALETTE_ELEMENTICON = "org/nuclos/client/layout/wysiwyg/palette/images/JTextAreaColor16.gif";
		public static final String PALETTE_ELEMENTTOOLTIP = "Komponente f\u00fcr mehrzeilige Texteingaben";

		@Override
		public Icon getIcon() {
			if ("".equals(PALETTE_ELEMENTICON))
				return null;
			return new ImageIcon(this.getClass().getClassLoader().getResource(PALETTE_ELEMENTICON));
		}
		@Override
		public String getLabel() {
			return PALETTE_ELEMENTNAME;
		}
		@Override
		public String getToolTip() {
			return PALETTE_ELEMENTTOOLTIP;
		}
		@Override
		public boolean displayLabelAndIcon() {
			return false;
		}
		@Override
		public boolean isLabeledComponent() {
			return true;
		}
	}

	/**
	 * @deprecated Should be done with the help of CommonLocaleDelegate and localeresource.json.
	 */
	public static class COLLECTABLE_TEXTFIELD implements PaletteItemElement {
		public static final String PROPERTY_LABEL = "Eigenschaften";

		public static final String PALETTE_ELEMENTNAME = "Textfield";
		public static final String PALETTE_ELEMENTICON = "org/nuclos/client/layout/wysiwyg/palette/images/JTextFieldColor16.gif";
		public static final String PALETTE_ELEMENTTOOLTIP = "Komponente f\u00fcr einzeilige Texteingaben";

		@Override
		public Icon getIcon() {
			if ("".equals(PALETTE_ELEMENTICON))
				return null;
			return new ImageIcon(this.getClass().getClassLoader().getResource(PALETTE_ELEMENTICON));
		}
		@Override
		public String getLabel() {
			return PALETTE_ELEMENTNAME;
		}
		@Override
		public String getToolTip() {
			return PALETTE_ELEMENTTOOLTIP;
		}
		@Override
		public boolean displayLabelAndIcon() {
			return false;
		}
		@Override
		public boolean isLabeledComponent() {
			return false;
		}
	}
	
	/**
	 * @deprecated Should be done with the help of CommonLocaleDelegate and localeresource.json.
	 */
	//NUCLEUSINT-1142
	public static class COLLECTABLE_PASSWORDFIELD implements PaletteItemElement {
		public static final String PROPERTY_LABEL = "Eigenschaften";

		public static final String PALETTE_ELEMENTNAME = "Passwortfeld";
		public static final String PALETTE_ELEMENTICON = "org/nuclos/client/layout/wysiwyg/palette/images/PasswordField.gif";
		public static final String PALETTE_ELEMENTTOOLTIP = "Komponente f\u00fcr Passw\u00f6rter";

		@Override
		public Icon getIcon() {
			if ("".equals(PALETTE_ELEMENTICON))
				return null;
			return new ImageIcon(this.getClass().getClassLoader().getResource(PALETTE_ELEMENTICON));
		}
		@Override
		public String getLabel() {
			return PALETTE_ELEMENTNAME;
		}
		@Override
		public String getToolTip() {
			return PALETTE_ELEMENTTOOLTIP;
		}
		@Override
		public boolean displayLabelAndIcon() {
			return false;
		}
		@Override
		public boolean isLabeledComponent() {
			return false;
		}
	}
	
	/**
	 * @deprecated Should be done with the help of CommonLocaleDelegate and localeresource.json.
	 */
	// NUCLEUSINT-496
	public static class COLLECTABLE_LABELED_TEXTFIELD implements PaletteItemElement {
		public static final String PALETTE_ELEMENTNAME = "Textfield mit Label";
		public static final String PALETTE_ELEMENTICON = "org/nuclos/client/layout/wysiwyg/palette/images/JTextFieldColor16.gif";
		public static final String PALETTE_ELEMENTTOOLTIP = "Komponente f\u00fcr einzeilige Texteingaben";

		@Override
		public Icon getIcon() {
			if ("".equals(PALETTE_ELEMENTICON))
				return null;
			return new ImageIcon(this.getClass().getClassLoader().getResource(PALETTE_ELEMENTICON));
		}
		@Override
		public String getLabel() {
			return PALETTE_ELEMENTNAME;
		}
		@Override
		public String getToolTip() {
			return PALETTE_ELEMENTTOOLTIP;
		}
		@Override
		public boolean displayLabelAndIcon() {
			return false;
		}
		@Override
		public boolean isLabeledComponent() {
			return true;
		}
	}
	
	/**
	 * @deprecated Should be done with the help of CommonLocaleDelegate and localeresource.json.
	 */
	//NUCLEUSINT-1142
	public static class COLLECTABLE_LABELED_PASSWORDFIELD implements PaletteItemElement {
		public static final String PALETTE_ELEMENTNAME = "Passwortfeld mit Label";
		public static final String PALETTE_ELEMENTICON = "org/nuclos/client/layout/wysiwyg/palette/images/PasswordField.gif";
		public static final String PALETTE_ELEMENTTOOLTIP = "Komponente f\u00fcr die Darstellung von Passw\u00f6rtern";

		@Override
		public Icon getIcon() {
			if ("".equals(PALETTE_ELEMENTICON))
				return null;
			return new ImageIcon(this.getClass().getClassLoader().getResource(PALETTE_ELEMENTICON));
		}
		@Override
		public String getLabel() {
			return PALETTE_ELEMENTNAME;
		}
		@Override
		public String getToolTip() {
			return PALETTE_ELEMENTTOOLTIP;
		}
		@Override
		public boolean displayLabelAndIcon() {
			return false;
		}
		@Override
		public boolean isLabeledComponent() {
			return true;
		}
	}
	
	/**
	 * @deprecated Should be done with the help of CommonLocaleDelegate and localeresource.json.
	 */
	public static class COLLECTABLE_LABELED_IMAGE implements PaletteItemElement {
		public static final String PALETTE_ELEMENTNAME = "Image";
		public static final String PALETTE_ELEMENTICON = "org/nuclos/client/layout/wysiwyg/palette/images/JButtonColor16.gif";
		public static final String PALETTE_ELEMENTTOOLTIP = "Komponente f\u00fcr Bildanzeige";

		@Override
		public Icon getIcon() {
			if ("".equals(PALETTE_ELEMENTICON))
				return null;
			return new ImageIcon(this.getClass().getClassLoader().getResource(PALETTE_ELEMENTICON));
		}
		@Override
		public String getLabel() {
			return PALETTE_ELEMENTNAME;
		}
		@Override
		public String getToolTip() {
			return PALETTE_ELEMENTTOOLTIP;
		}
		@Override
		public boolean displayLabelAndIcon() {
			return false;
		}
		@Override
		public boolean isLabeledComponent() {
			return false;
		}
	}

	/**
	 * @deprecated Should be done with the help of CommonLocaleDelegate and localeresource.json.
	 */
	public static class COLLECTABLE_SUBFORM implements PaletteItemElement {
		public static final String LABEL_NO_ENTITY_ASSIGNED = "Bitte geben Sie eine Entit\u00e4t an.";
		public static final String PROPERTY_LABEL = "Eigenschaften";

		public static final String PALETTE_ELEMENTNAME = "Subform";
		public static final String PALETTE_ELEMENTICON = "org/nuclos/client/layout/wysiwyg/palette/images/JTableColor16.gif";
		public static final String PALETTE_ELEMENTTOOLTIP = "Unterformular";

		@Override
		public Icon getIcon() {
			if ("".equals(PALETTE_ELEMENTICON))
				return null;
			return new ImageIcon(this.getClass().getClassLoader().getResource(PALETTE_ELEMENTICON));
		}
		@Override
		public String getLabel() {
			return PALETTE_ELEMENTNAME;
		}
		@Override
		public String getToolTip() {
			return PALETTE_ELEMENTTOOLTIP;
		}
		@Override
		public boolean displayLabelAndIcon() {
			return false;
		}
		@Override
		public boolean isLabeledComponent() {
			return false;
		}
	}

	/**
	 * @deprecated Should be done with the help of CommonLocaleDelegate and localeresource.json.
	 */
	public static class COLLECTABLE_SUBFORM_COLUMN {
		public static final String PROPERTY_LABEL = "Spalteneigenschaften";
	}

	/**
	 * @deprecated Should be done with the help of CommonLocaleDelegate and localeresource.json.
	 */
	public static class PROPERTY_LABELS {
		public static final String NAME = "Name der Komponente";
		public static final String PREFFEREDSIZE = "Bevorzugte Gr\u00f6\u00dfe";
		public static final String SHOWONLY = "Komponentendarstellung";
		public static final String ENABLED = "Aktiv";
		public static final String VISIBLE = "Sichtbar";
		public static final String OPAQUE = "Deckend";
		public static final String INSERTABLE = "Werteliste editierbar";
		public static final String MNEMONIC = "Tastenk\u00fcrzel";
		public static final String COLLECTABLECOMPONENTPROPERTY = "Erweiterte Eigenschaften";
		/** tablelayoutconstraints */
		public static final String CONSTRAINT_COL1 = "Beginnt in Spalte";
		public static final String CONSTRAINT_COL2 = "Endet in Spalte";
		public static final String CONSTRAINT_ROW1 = "Startet in Zeile";
		public static final String CONSTRAINT_ROW2 = "Endet in Zeile";
		public static final String CONSTRAINT_HALIGN = "Horizontale Ausrichtung";
		public static final String CONSTRAINT_VALIGN = "Vertikale Ausrichtung";
		public static final String VALUELISTPROVIDER = "Valuelist-Provider";
		public static final String BACKGROUNDCOLOR = "Hintergrundfarbe";
		public static final String BORDER = "Rahmen";
		public static final String FONT = "Schriftgr\u00f6\u00dfe";
		public static final String DESCRIPTION = "Alternativer Tooltip";
		public static final String ROWS = "Zeilen";
		public static final String COLUMNS = "Spalten";
		public static final String HORIZONTALSCROLLBAR = "Horizontale Scrollbar";
		public static final String VERTICALSCROLLBAR = "Vertikale Scrollbar";
		public static final String ORIENTATION = "Ausrichtung";
		/** splitpane */
		public static final String DIVIDERSIZE = "Trennstrichbreite";
		public static final String RESIZEWEIGHT = "Resizegewichtung";
		public static final String CONTINUOUSLAYOUT = "Durchgehendes Layout";
		public static final String EXPANDABLE = "Ein-/Ausklappbar";
		/** subform */
		public static final String ENTITY = "Entit\u00e4t";
		public static final String FOREIGNKEY = "Fremdschl\u00fcssel";
		public static final String TOOLBARORIENTATION = "Toolbarausrichtung";
		public static final String UNIQUEMASTERCOLUMN = "Unique Mastercolumn";
		public static final String CONTROLLERTYPE = "Controllertyp";
		public static final String INITIALSORTINGORDER = "Standardsortierung";
		/** subform column */
		public static final String DEFAULTVALUES = "defaultvalues";
		public static final String CONTROLTYPE = "Komponenten Typ";
		public static final String CONTROLTYPECLASS = "Typklasse";
		//NUCLEUSINT-390
		public static final String PARENT_SUBFORM = "\u00dcbergeordnetes Unterformular";
		/** tabbedpane */
		public static final String TABLAYOUTPOLICY = "Layoutverhalten";
		public static final String TABPLACEMENT = "Reiterposition";
		/** static button */
		public static final String ACTIONCOMMAND = "Befehlstyp";
		public static final String LABEL = "Beschriftung";
		public static final String TOOLTIP = "Tooltip";
		//NUCLOSINT-743
		public static final String RULE = "Auszuf\u00fchrende Regel";
		/** static combobox */
		public static final String EDITABLE = "Editierbar";
		/** static label */
		public static final String TEXT = "Text";
		/** translations */
		public static final String TRANSLATIONS = "\u00dcbersetzungen";
		/** */
		public static final String NEXTFOCUSCOMPONENT = "N\u00e4chstes Feld (Tabulator-Taste)";

		/** titled separator */
		public static final String SEPERATOR_TITLE = "Titel";
		/** collectable component */
		public static final String FILL_HORIZONTALLY = "Horizontal strecken";
		
		public static final String COLUMNWIDTH = "Spaltenbreite";
	}

	/**
	 * @deprecated Should be done with the help of CommonLocaleDelegate and localeresource.json.
	 */
	public static class BORDER_EDITOR {
		public static final String TITLE_BORDER_EDITOR = "Rahmeneditor";

		public static final String BORDER_PANEL_TITLE = MARKER + ". Rahmen";
		public static final String LABEL_CLEAR_BORDER_CHECKBOX = "Defaultrahmen entfernen?";
		public static final String LABEL_REMOVE_ALL_BORDERS = "Alle Rahmen entfernen";

		public static final String NAME_EMPTYBORDER = "Leerer Rahmen";
		public static final String NAME_ETCHEDBORDER = "Gravierter Rahmen";
		public static final String NAME_BEVELBORDER = "Reliefrahmen";
		public static final String NAME_LINEBORDER = "Linienrahmen";
		public static final String NAME_TITLEDBORDER = "Rahmen mit Titel";

		public static final String DEFAULT_TITLE_FOR_NEW_TITLED_BORDER = "Titel";

		public static final String LABEL_BORDERTYPE = "Typ";
		public static final String LABEL_BORDERPROPERTIES = "Eigenschaften";

		public static final String TOP_BORDERLOCATION = "Oben";
		public static final String BOTTOM_BORDERLOCATION = "Unten";
		public static final String LEFT_BORDERLOCATION = "Links";
		public static final String RIGHT_BORDERLOCATION = "Rechts";

		public static final String LABEL_ETCHEDBORDER_TYPE = "Rahmenstil";
		public static final String LABEL_BEVELBORDER_TYPE = "Rahmenstil";
		public static final String LOWERED_PROPERTY_BEVEL_BORDER = "abgesenkt";
		public static final String RAISED_PROPERTY_BEVEL_BORDER = "erh\u00f6ht";
		public static final String LABEL_THICKNESS_PROPERTY = "Linienst\u00e4rke";
		public static final String LABEL_COLOR_PROPERTY = "Farbe";
		public static final String LABEL_TITLE_PROPERTY = "Titel";
		public static final String LABEL_TRANSLATIONS_PROPERTY = "\u00dcbersetzungen";

		public static final String COLORPICKER_LINECOLOR_TITLE = "Farbe ausw\u00e4hlen";
		
		public static final String BORDERS_DEFINED = MARKER + " Rahmen definiert";
		public static final String NO_BORDERS_DEFINED = "Kein Rahmen definiert";
	}

	/**
	 * @deprecated Should be done with the help of CommonLocaleDelegate and localeresource.json.
	 */
	public static class LAYOUTML_RULE_EDITOR {
		public static final String TITLE_LAYOUTML_RULE_EDITOR = "Regeleditor";

		public static final String LABEL_ACTION_SELECTOR = "Aktionstyp:";
		public static final String LABEL_TARGET_COMPONENT = "Zielkomponente:";
		public static final String LABEL_SOURCE_COMPONENT = "Quellkomponente:";
		public static final String LABEL_ENTITY = "Entit\u00e4t:";

		public static final String LABEL_INVERTABLE = "Invertierbar:";
		public static final String LABEL_PARAMETER_FOR_SOURCECOMPONENT = "Parameter:";

		public static final String NAME_FOR_EVENT_LOOKUP = "Wert nachschlagen";
		public static final String NAME_FOR_EVENT_VALUE_CHANGED = "Wenn der Wert sich \u00e4ndert";

		public static final String NAME_FOR_ACTION_TRANSFER_LOOKEDUP_VALUE = "Nachgeschlagenen Wert \u00fcbertragen";
		public static final String NAME_FOR_ACTION_CLEAR = "Feld zur\u00fccksetzen";
		public static final String NAME_FOR_ACTION_ENABLE = "Feld aktivieren";
		public static final String NAME_FOR_ACTION_REFRESH_VALUELIST = "Werteliste aktualisieren";

		public static final String LABEL_RULE_SOURCECOMPONENT = "Komponente:";

		public static final String BUTTON_REMOVE_ALL_RULES_FOR_COMPONENT = "Alle Regeln entfernen";
		public static final String MESSAGE_DIALOG_SURE_TO_DELETE_ALL_RULES = "M\u00f6chten sie wirklich alle Regeln entfernen?";
		public static final String TITLE_DIALOG_SURE_TO_DELETE_ALL_RULES = "Entfernen der Regeln best\u00e4tigen";

		public static final String LABEL_TITLE_ACTION_PANEL = "Aktionen";
		public static final String LABEL_TITLE_EVENT_PANEL = "Ereignis";

		public static final String LABEL_EVENT_TRIGGERING_RULE = "Ereignis:";

		public static final String INPUT_DIALOG_TITLE_CHANGE_RULENAME = "Name der Regel:";
	}

	/**
	 * @deprecated Should be done with the help of CommonLocaleDelegate and localeresource.json.
	 */
	public static class OPTIONS_EDITOR {
		public static final String LABEL_OPTIONGROUP_NAME = "Name der Werteliste:";
		public static final String LABEL_OPTIONGROUP_DEFAULT_VALUE = "Standardwert:";
		public static final String LABEL_OPTIONGROUP_ORIENTATION = "Ausrichtung:";

		public static final String LABEL_ORIENTATION_HORIZONTAL = "horizontal";
		public static final String LABEL_ORIENTATION_VERTICAL = "vertikal";

		public static final String BUTTON_ADD_OPTION = "Wert hinzuf\u00fcgen";

		public static final String BUTTON_DELETE_ALL_OPTIONS = "Alle Optionen entfernen";
		
		public static final String LABEL_DIMENSION_WIDTH = "Breite:";
		public static final String LABEL_DIMENSION_HEIGHT = "H\u00f6he:";

		public static final String LABEL_OPTION_NAME = "Name:";
		public static final String LABEL_OPTION_VALUE = "Wert:";
		public static final String LABEL_OPTION_LABEL = "Beschriftung:";
		public static final String LABEL_OPTION_MNEMONIC = "Tastenk\u00fcrzel:";
		
		public static final String ERROR_INCOMPLETE_OPTION = "Bei Optionen muss der Name und der Wert angegeben werden.";
		public static final String ERROR_DEFAULTVALUE_OPTIONS = "Bitte geben Sie einen Standardwert an.";
	}

	/**
	 * @deprecated Should be done with the help of CommonLocaleDelegate and localeresource.json.
	 */
	public static class VALUELIST_PROVIDER_EDITOR {
		//NUCLEUSINT-312
		public static final String TITLE_VALUELIST_PROVIDER_EDITOR = "Valuelistprovider bearbeiten";
		public static final String LABEL_VALUELIST_PROVIDER_NAME = "Valuelistprovider Name:";
		public static final String BUTTON_ADD_PARAMETER = "Parameter hinzuf\u00fcgen";
		public static final String BUTTON_DELETE_VALUELIST_PROVIDER = "Valuelistprovider l\u00f6schen";
		public static final String NO_VALUELISTPROVIDER_DEFINED = "Kein ValueListprovider definiert";
		public static final String LABEL_PARAMETER_NAME = "Anzeigename";
		public static final String LABEL_PARAMETER_ID = "ID Feld";
		public static final String SEPARATOR_DATASOURCES = "--------- Datenquellen ---------";
		public static final String SEPARATOR_SYSTEM = "--------- System ---------";
		//NUCLEUSINT-811
		public static final String VALIDATIONEXCEPTION_PARAMETERS_1 = "Der Valuelistprovider " + MARKER + " ist nicht valide.\n" +
		 "Das Attribut hat als Datentyp \"" + MARKER + "\" und der Valuelistprovider liefert die Werte als \"" + MARKER + "\"\n" +
		 "Um zu Speichern \u00e4ndern sie bitte den Wert von showClass auf \"" + MARKER + "\"";
		public static final String VALIDATIONEXCEPTION_PARAMETERS_2 = "Der Valuelistprovider " + MARKER + " ist nicht valide.\n" +
		 "Das Attribut hat als Datentyp \"" + MARKER + "\" und der Valuelistprovider liefert die Werte als \"java.lang.String\"\n" +
		 "Um zu Speichern \u00e4ndern sie bitte den Wert von showClass auf \"" + MARKER + "\"";
	}

	/**
	 * @deprecated Should be done with the help of CommonLocaleDelegate and localeresource.json.
	 */
	public static class COLLECTABLE_COMPONENT_PROPERTY_EDITOR {
		//NUCLEUSINT-283
		public static final String TITLE_PROPERTY_EDITOR = "Eigenschaften bearbeiten";
		
		public static final String BUTTON_ADD_PROPERTY = "Eigenschaft hinzuf\u00fcgen";
		public static final String BUTTON_DELETE_PROPERTY = "Eigenschaft entfernen";

		public static final String LABEL_PROPERTY_NAME = "Eigenschaftsname:";
		public static final String LABEL_PROPERTY_VALUE = "Eigenschaftswert:";
		
		public static final String PROPERTIES_DEFINED = MARKER + " Eigenschaften definiert";
		public static final String NO_PROPERTIES_DEFINED = "Keine Eigenschaften definiert";
	}

	/**
	 * @deprecated Should be done with the help of CommonLocaleDelegate and localeresource.json.
	 */
	public static class INITIAL_FOCUS_EDITOR {
		public static final String TOOLBAR_TOOLTIP = "Initialen Focus f\u00fcr das Layout setzen";
		public static final String TITLE_INITIAL_FOCUS_EDITOR = "Initialen Focus setzen";
		public static final String LABEL_BUTTON_REMOVE_INITIAL_FOCUS = "Initialen Fokus entfernen";
		public static final String LABEL_ENTITY = "Enit\u00e4t: ";
		public static final String LABEL_ATTRIBUTE = "Attribut: ";
	}

	/**
	 * @deprecated Should be done with the help of CommonLocaleDelegate and localeresource.json.
	 */
	public static class WYSIWYGLAYOUT_EDITOR_PANEL {
		public static final String PROPERTY_LABEL = "Eigenschaften";

		public static final String ERRORMESSAGE_INTERNAL_PANEL_CELL_TOO_SMALL = "Die Zelle ist nicht gro\u00df genug";
		public static final String ERRORMESSAGE_INTERNAL_PANEL_CELL_NOT_EMPTY = "Die Zelle ist nicht leer";
		public static final String ERRORMESSAGE_PANEL_HAS_NO_LAYOUT = "Das Panel geh\u00f6rt zu keinem Layout";
		
		public static final String WAITING_FOR_METAINFORMATION = "Es sind noch keine Metainformationen zugewiesen worden!";

	}

	/**
	 * @deprecated Should be done with the help of CommonLocaleDelegate and localeresource.json.
	 */
	public static class STATIC_TITLED_SEPARATOR implements PaletteItemElement {
		public static final String PROPERTY_LABEL = "Eigenschaften";

		public static final String PALETTE_ELEMENTNAME = "Titled Separator";
		public static final String PALETTE_ELEMENTICON = "org/nuclos/client/layout/wysiwyg/palette/images/JSeparatorColor16.gif";
		public static final String PALETTE_ELEMENTTOOLTIP = "Trennelement mit Titel";

		@Override
		public Icon getIcon() {
			if ("".equals(PALETTE_ELEMENTICON))
				return null;
			return new ImageIcon(this.getClass().getClassLoader().getResource(PALETTE_ELEMENTICON));
		}
		@Override
		public String getLabel() {
			return PALETTE_ELEMENTNAME;
		}
		@Override
		public String getToolTip() {
			return PALETTE_ELEMENTTOOLTIP;
		}
		@Override
		public boolean displayLabelAndIcon() {
			return false;
		}
		@Override
		public boolean isLabeledComponent() {
			return false;
		}
	}

	/**
	 * @deprecated Should be done with the help of CommonLocaleDelegate and localeresource.json.
	 */
	public static class STATIC_TEXTFIELD implements PaletteItemElement {
		public static final String PROPERTY_LABEL = "Eigenschaften";

		public static final String PALETTE_ELEMENTNAME = "Textfield";
		public static final String PALETTE_ELEMENTICON = "org/nuclos/client/layout/wysiwyg/palette/images/JTextFieldColor16.gif";
		public static final String PALETTE_ELEMENTTOOLTIP = COLLECTABLE_TEXTFIELD.PALETTE_ELEMENTTOOLTIP;

		@Override
		public Icon getIcon() {
			if ("".equals(PALETTE_ELEMENTICON))
				return null;
			return new ImageIcon(this.getClass().getClassLoader().getResource(PALETTE_ELEMENTICON));
		}
		@Override
		public String getLabel() {
			return PALETTE_ELEMENTNAME;
		}
		@Override
		public String getToolTip() {
			return PALETTE_ELEMENTTOOLTIP;
		}
		@Override
		public boolean displayLabelAndIcon() {
			return false;
		}
		@Override
		public boolean isLabeledComponent() {
			return false;
		}
	}

	/**
	 * @deprecated Should be done with the help of CommonLocaleDelegate and localeresource.json.
	 */
	public static class STATIC_TEXTAREA implements PaletteItemElement {
		public static final String PROPERTY_LABEL = "Eigenschaften";

		public static final String PALETTE_ELEMENTNAME = "Textarea";
		public static final String PALETTE_ELEMENTICON = "org/nuclos/client/layout/wysiwyg/palette/images/JTextAreaColor16.gif";
		public static final String PALETTE_ELEMENTTOOLTIP = COLLECTABLE_TEXTAREA.PALETTE_ELEMENTTOOLTIP;

		@Override
		public Icon getIcon() {
			if ("".equals(PALETTE_ELEMENTICON))
				return null;
			return new ImageIcon(this.getClass().getClassLoader().getResource(PALETTE_ELEMENTICON));
		}
		@Override
		public String getLabel() {
			return PALETTE_ELEMENTNAME;
		}
		@Override
		public String getToolTip() {
			return PALETTE_ELEMENTTOOLTIP;
		}
		@Override
		public boolean displayLabelAndIcon() {
			return false;
		}
		@Override
		public boolean isLabeledComponent() {
			return false;
		}
	}

	/**
	 * @deprecated Should be done with the help of CommonLocaleDelegate and localeresource.json.
	 */
	public static class STATIC_SEPERATOR implements PaletteItemElement {
		public static final String PROPERTY_LABEL = "Eigenschaften";

		public static final String PALETTE_ELEMENTNAME = "Separator";
		public static final String PALETTE_ELEMENTICON = "org/nuclos/client/layout/wysiwyg/palette/images/JSeparatorColor16.gif";
		public static final String PALETTE_ELEMENTTOOLTIP = "Einfaches Trennelement";

		@Override
		public Icon getIcon() {
			if ("".equals(PALETTE_ELEMENTICON))
				return null;
			return new ImageIcon(this.getClass().getClassLoader().getResource(PALETTE_ELEMENTICON));
		}
		@Override
		public String getLabel() {
			return PALETTE_ELEMENTNAME;
		}
		@Override
		public String getToolTip() {
			return PALETTE_ELEMENTTOOLTIP;
		}
		@Override
		public boolean displayLabelAndIcon() {
			return false;
		}
		@Override
		public boolean isLabeledComponent() {
			return false;
		}
	}

	/**
	 * @deprecated Should be done with the help of CommonLocaleDelegate and localeresource.json.
	 */
	public static class STATIC_LABEL implements PaletteItemElement {
		public static final String PROPERTY_LABEL = "Eigenschaften";

		public static final String PALETTE_ELEMENTNAME = "Label";
		public static final String PALETTE_ELEMENTICON = "org/nuclos/client/layout/wysiwyg/palette/images/JLabelColor16.gif";
		public static final String PALETTE_ELEMENTTOOLTIP = COLLECTABLE_LABEL.PALETTE_ELEMENTTOOLTIP;

		@Override
		public Icon getIcon() {
			if ("".equals(PALETTE_ELEMENTICON))
				return null;
			return new ImageIcon(this.getClass().getClassLoader().getResource(PALETTE_ELEMENTICON));
		}
		@Override
		public String getLabel() {
			return PALETTE_ELEMENTNAME;
		}
		@Override
		public String getToolTip() {
			return PALETTE_ELEMENTTOOLTIP;
		}
		@Override
		public boolean displayLabelAndIcon() {
			return false;
		}
		@Override
		public boolean isLabeledComponent() {
			return false;
		}
	}
	
	/**
	 * @deprecated Should be done with the help of CommonLocaleDelegate and localeresource.json.
	 */
	public static class STATIC_IMAGE implements PaletteItemElement {
		public static final String PALETTE_ELEMENTNAME = "Image";
		public static final String PALETTE_ELEMENTICON = "org/nuclos/client/layout/wysiwyg/palette/images/JButtonColor16.gif";
		public static final String PALETTE_ELEMENTTOOLTIP = "Komponente f\u00fcr Bildanzeige";

		@Override
		public Icon getIcon() {
			if ("".equals(PALETTE_ELEMENTICON))
				return null;
			return new ImageIcon(this.getClass().getClassLoader().getResource(PALETTE_ELEMENTICON));
		}
		@Override
		public String getLabel() {
			return PALETTE_ELEMENTNAME;
		}
		@Override
		public String getToolTip() {
			return PALETTE_ELEMENTTOOLTIP;
		}
		@Override
		public boolean displayLabelAndIcon() {
			return false;
		}
		@Override
		public boolean isLabeledComponent() {
			return false;
		}
	}

	/**
	 * @deprecated Should be done with the help of CommonLocaleDelegate and localeresource.json.
	 */
	public static class STATIC_COMBOBOX implements PaletteItemElement {
		public static final String PROPERTY_LABEL = "Eigenschaften";

		public static final String PALETTE_ELEMENTNAME = "Combobox";
		public static final String PALETTE_ELEMENTICON = "org/nuclos/client/layout/wysiwyg/palette/images/JComboBoxColor16.gif";
		public static final String PALETTE_ELEMENTTOOLTIP = COLLECTABLE_COMBOBOX.PALETTE_ELEMENTTOOLTIP;

		@Override
		public Icon getIcon() {
			if ("".equals(PALETTE_ELEMENTICON))
				return null;
			return new ImageIcon(this.getClass().getClassLoader().getResource(PALETTE_ELEMENTICON));
		}
		@Override
		public String getLabel() {
			return PALETTE_ELEMENTNAME;
		}
		@Override
		public String getToolTip() {
			return PALETTE_ELEMENTTOOLTIP;
		}
		@Override
		public boolean displayLabelAndIcon() {
			return false;
		}
		@Override
		public boolean isLabeledComponent() {
			return false;
		}
	}

	/**
	 * @deprecated Should be done with the help of CommonLocaleDelegate and localeresource.json.
	 */
	public static class STATIC_BUTTON implements PaletteItemElement {
		public static final String PROPERTY_LABEL = "Eigenschaften";

		public static final String PALETTE_ELEMENTNAME = "Button";
		public static final String PALETTE_ELEMENTICON = "org/nuclos/client/layout/wysiwyg/palette/images/JButtonColor16.gif";
		public static final String PALETTE_ELEMENTTOOLTIP = "Button der eine Aktion ausf\u00fchrt";
		
		//FIX NUCLEUSINT-255
		public static final String EXCEPTION_AC_ISNULL = "Der ActionCommand darf nicht leer sein.";
		public static final String EXCEPTION_AC_NOTFOUND = "Die Klasse des ActionCommands kann nicht geladen werden.";
		public static final String EXCEPTION_AC_WRONG_TYPE = "Der ActionCommand stammt nicht von CollectActionAdapter ab.";
		//NUCLEUSINT-1159
		public static final String STATE_CHANGE_ACTION_LABEL = "Statuswechsel";
		public static final String STATE_CHANGE_ACTION = "org.nuclos.client.layout.wysiwyg.component.ChangeStateButtonAction";
		//NUCLOSINT-743
		public static final String EXECUTE_RULE_ACTION_LABEL = "Regelbutton";
		public static final String EXECUTE_RULE_ACTION = "org.nuclos.client.layout.wysiwyg.component.ExecuteRuleButtonAction";
		public static final String DUMMY_BUTTON_ACTION_LABEL = "Dummy Action";
		public static final String DUMMY_BUTTON_ACTION = "org.nuclos.client.layout.wysiwyg.component.DummyButtonAction";

		@Override
		public Icon getIcon() {
			if ("".equals(PALETTE_ELEMENTICON))
				return null;
			return new ImageIcon(this.getClass().getClassLoader().getResource(PALETTE_ELEMENTICON));
		}
		@Override
		public String getLabel() {
			return PALETTE_ELEMENTNAME;
		}
		@Override
		public String getToolTip() {
			return PALETTE_ELEMENTTOOLTIP;
		}
		@Override
		public boolean displayLabelAndIcon() {
			return false;
		}
		@Override
		public boolean isLabeledComponent() {
			return false;
		}
	}
	
	/**
	 * @deprecated Should be done with the help of CommonLocaleDelegate and localeresource.json.
	 */
	//NUCLEUSINT-650
	public static class EDITORPANEL implements PaletteItemElement {
		public static final String PALETTE_ELEMENTNAME = "Layoutpanel";
		public static final String PALETTE_ELEMENTICON = "org/nuclos/client/layout/wysiwyg/palette/images/JPanelColor16.gif";
		public static final String PALETTE_ELEMENTTOOLTIP = "Layoutpanel das Komponenten aufnehmen, und ein individuelles Layout haben kann";
		
		@Override
		public boolean displayLabelAndIcon() {
			return false;
		}
		@Override
		public Icon getIcon() {
			if ("".equals(PALETTE_ELEMENTICON))
				return null;
			return new ImageIcon(this.getClass().getClassLoader().getResource(PALETTE_ELEMENTICON));
		}
		@Override
		public String getLabel() {
			return PALETTE_ELEMENTNAME;
		}
		@Override
		public String getToolTip() {
			return PALETTE_ELEMENTTOOLTIP;
		}
		@Override
		public boolean isLabeledComponent() {
			return false;
		}
	}

	/**
	 * @deprecated Should be done with the help of CommonLocaleDelegate and localeresource.json.
	 */
	public static class JSCROLLPANE implements PaletteItemElement {
		public static final String PROPERTY_LABEL = "Eigenschaften";

		public static final String PALETTE_ELEMENTNAME = "ScrollPane";
		public static final String PALETTE_ELEMENTICON = "org/nuclos/client/layout/wysiwyg/palette/images/JScrollPaneColor16.gif";
		public static final String PALETTE_ELEMENTTOOLTIP = "Ein Panel das Scrollleisten hat";

		public static final String LABEL_ALWAYS = "immer";
		public static final String LABEL_NEVER = "niemals";
		public static final String LABEL_AS_NEEDED = "wenn n\u00f6tig";

		@Override
		public Icon getIcon() {
			if ("".equals(PALETTE_ELEMENTICON))
				return null;
			return new ImageIcon(this.getClass().getClassLoader().getResource(PALETTE_ELEMENTICON));
		}
		@Override
		public String getLabel() {
			return PALETTE_ELEMENTNAME;
		}
		@Override
		public String getToolTip() {
			return PALETTE_ELEMENTTOOLTIP;
		}
		@Override
		public boolean displayLabelAndIcon() {
			return false;
		}
		@Override
		public boolean isLabeledComponent() {
			return false;
		}
	}

	/**
	 * @deprecated Should be done with the help of CommonLocaleDelegate and localeresource.json.
	 */
	public static class JTABBEDPANE implements PaletteItemElement {
		public static final String PROPERTY_LABEL = "Eigenschaften";

		public static final String DEFAULT_TABNAME = "Reiter_";

		public static final String SCROLL_TAB_LAYOUT = "scrollen";
		public static final String WRAP_TAB_LAYOUT = "umbrechen";

		public static final String TOP_TABPLACEMENT = "oben";
		public static final String BOTTOM_TABPLACEMENT = "unten";
		public static final String LEFT_TABPLACEMENT = "links";
		public static final String RIGHT_TABPLACEMENT = "rechts";

		public static final String MENUITEM_ADD_TAB = "Reiter hinzuf\u00fcgen";
		public static final String MENUITEM_RENAME_TAB = "Reiter umbenennen";
		public static final String MENUITEM_SET_MNEMONIC = "Reiter Tastenkürzel";
		public static final String MENUITEM_REMOVE_TAB = "Reiter entfernen";
		public static final String MENUITEM_ORDER_TABS = "Reiter umsortieren";
		public static final String MENUITEM_ENABLE_OR_DISABLE_TABS = "Reiter (de)aktivieren";

		public static final String INPUTDIALOG_RENAME_ACTION_TITLE = "Neuen Namen eingeben:";
		public static final String INPUTDIALOG_SETMNEMONIC_ACTION = "Tastenkürzel eingeben:";
		public static final String INPUTDIALOG_SETMNEMONIC_ACTION_TEXT = "Schnelltaste für Tab-Aktivierung eingeben:";
		public static final String INPUTDIALOG_RENAME_ACTION_BUTTON = "Reiter umbenennen...";

		public static final String INPUTDIALOG_REMOVE_ACTION_TITLE = "W\u00e4hlen sie den Reiter zum Entfernen aus:";
		public static final String INPUTDIALOG_REMOVE_ACTION_BUTTON = "Reiter entfernen...";
		public static final String ERRORMESSAGE_VALIDATION_REMOVE = "Eingabe nicht korrent! Sie muss folgenderma\u00dfen aussehen: '1:NEW' (index;name)";

		public static final String INPUTDIALOG_MOVE_ACTION_TITLE = "Reiter ausw\u00e4hlen zum Bewegen:";
		public static final String INPUTDIALOG_MOVE_ACTION_BUTTON = "Reiter bewegen...";

		public static final String INPUTDIALOG_INSERT_MOVED_TAB_ACTION_TITLE = "Reiter ausw\u00e4hlen wovor '" + MARKER + "' eingef\u00fcgt werden soll:";
		public static final String INPUTDIALOG_INSERT_MOVED_TAB_ACTION_BUTTON = "Reiter an der Stelle einf\u00fcgen...";
		public static final String ERRORMESSAGE_VALIDATION_MOVE = "Eingabe nicht korrent! Sie muss folgenderma\u00dfen aussehen: '1:NEW' (index;name)";

		public static final String PALETTE_ELEMENTNAME = "TabbedPane";
		public static final String PALETTE_ELEMENTICON = "org/nuclos/client/layout/wysiwyg/palette/images/JTabbedPaneColor16.gif";
		public static final String PALETTE_ELEMENTTOOLTIP = "";

		@Override
		public Icon getIcon() {
			if ("".equals(PALETTE_ELEMENTICON))
				return null;
			return new ImageIcon(this.getClass().getClassLoader().getResource(PALETTE_ELEMENTICON));
		}
		@Override
		public String getLabel() {
			return PALETTE_ELEMENTNAME;
		}
		@Override
		public String getToolTip() {
			return PALETTE_ELEMENTTOOLTIP;
		}
		@Override
		public boolean displayLabelAndIcon() {
			return false;
		}
		@Override
		public boolean isLabeledComponent() {
			return false;
		}
	}

	/**
	 * @deprecated Should be done with the help of CommonLocaleDelegate and localeresource.json.
	 */
	public static class JSPLITPANE implements PaletteItemElement {
		public static final String PROPERTY_LABEL = "Eigenschaften";

		public static final String ERRORMESSAGE_VALIDATION_RESIZEWEIGHT = "Wert muss gr\u00f6\u00dfer als 0 sein und kleiner als 1";
		public static final String ERRORMESSAGE_VALIDATION_DIVIDERSIZE = "Wert darf nicht kleiner sein als " + InterfaceGuidelines.SPLITPANE_DIVIDERSIZE_MIN + " oder gr\u00f6\u00dfer als " + InterfaceGuidelines.SPLITPANE_DIVIDERSIZE_MAX;

		public static final String PALETTE_ELEMENTNAME = "Splitpane";
		public static final String PALETTE_ELEMENTICON = "org/nuclos/client/layout/wysiwyg/palette/images/JSplitPaneColor16.gif";
		public static final String PALETTE_ELEMENTTOOLTIP = "";

		@Override
		public Icon getIcon() {
			if ("".equals(PALETTE_ELEMENTICON))
				return null;
			return new ImageIcon(this.getClass().getClassLoader().getResource(PALETTE_ELEMENTICON));
		}
		@Override
		public String getLabel() {
			return PALETTE_ELEMENTNAME;
		}
		@Override
		public String getToolTip() {
			return PALETTE_ELEMENTTOOLTIP;
		}
		@Override
		public boolean displayLabelAndIcon() {
			return false;
		}
		@Override
		public boolean isLabeledComponent() {
			return false;
		}
	}

	/**
	 * @deprecated Should be done with the help of CommonLocaleDelegate and localeresource.json.
	 */
	public static class BUTTON_LABELS {
		//NUCLEUSINT-624
		public static final String LABEL_SAVE = "OK";
		public static final String LABEL_CANCEL = "Abbrechen";
		public static final String LABEL_EDIT = "Bearbeiten";
		//NUCLEUSINT-465
		public static final String LABEL_APPLY = "\u00dcbernehmen";
	}

	/**
	 * @deprecated Should be done with the help of CommonLocaleDelegate and localeresource.json.
	 */
	public static class PROPERTY_VALUE_COLOR {
		public static final String LABEL_OK = "OK";
		public static final String LABEL_REMOVE_BACKGROUND = "Hintergrund entfernen";
		public static final String COLOR_PICKER_TITLE = "Farbe ausw\u00e4hlen";
		
		public static final String NO_COLOR_DEFINED = "Keine Farbe definiert";

	}

	/**
	 * @deprecated Should be done with the help of CommonLocaleDelegate and localeresource.json.
	 */
	public static class PROPERTY_VALUE_DESCRIPTION {
		public static final String LABEL_EDIT_DESCRIPTION = "Tooltip bearbeiten";
		public static final String TITLE_INPUT_DIALOG = LABEL_EDIT_DESCRIPTION;
	}

	/**
	 * @deprecated Should be done with the help of CommonLocaleDelegate and localeresource.json.
	 */
	public static class PROPERTY_VALUE_DIMENSION {
		public static final String ERRORMESSAGE_NUMBERFORMAT = "Ganze Zahl erwartet f\u00fcr Attribut \"" + MARKER + "\".";
		public static final String ERRORMESSAGE_PARSING_FAILED = "Fehler beim Parsen der Werte";
	}

	/**
	 * @deprecated Should be done with the help of CommonLocaleDelegate and localeresource.json.
	 */
	public static class PROPERTY_INITIAL_SORTING_ORDER {
		public static final String ASCENDING = "aufsteigend";
		public static final String DESCENDING = "absteigend";
	}

	/**
	 * @deprecated Should be done with the help of CommonLocaleDelegate and localeresource.json.
	 */
	public static class PROPERTY_VALUE_FONT {
		public static final String ERRORMESSAGE_VALUE_OUT_OF_RANGE = "Der Wert ist nicht im Bereich von " + MARKER + " und " + MARKER;
	}

	/**
	 * @deprecated Should be done with the help of CommonLocaleDelegate and localeresource.json.
	 */
	public static class PROPERTYUTILS {
		public static final String ERRORMESSAGE_PROPERTY_NOT_REGISTERED = "Eigenschaft " + MARKER + " ist f\u00fcr die Komponente " + MARKER + " nicht definiert";
		public static final String ERRORMESSAGE_NO_PROPERTYVALUE_FOR_PROPERTY = "Kein Wert f\u00fcr die Eigenschaft " + MARKER + " in der Komponente " + MARKER + " gesetzt";
	}

	/**
	 * @deprecated Should be done with the help of CommonLocaleDelegate and localeresource.json.
	 */
	public static class COMMON_LABELS {
		public static final String EMPTY = "";
		public static final String USE_DEFAULT = "Defaultwert verwenden";
		public static final String ERROR = "Fehler";
	}

	/**
	 * @deprecated Should be done with the help of CommonLocaleDelegate and localeresource.json.
	 */
	public static class TABLELAYOUT_CONSTRAINTS {
		public static final String LABEL_LEFT = "Links";
		public static final String LABEL_RIGHT = "Rechts";

		public static final String LABEL_TOP = "Oben";
		public static final String LABEL_BOTTOM = "Unten";

		public static final String LABEL_CENTER = "Zentriert";
		public static final String LABEL_FULL = "Strecken";

	}

	/**
	 * @deprecated Should be done with the help of CommonLocaleDelegate and localeresource.json.
	 */
	public static class PROPERTIES_DIALOG {
		public static final String DIALOG_TITLE = "Eigenschaften von \"" + MARKER + "\"";
		public static final String PROBLEM_WITH_PROPERTY = "Problem mit der Eigenschaft: " + MARKER + "\n" + MARKER;

		public static final String LABEL_NO_NAME_SPECIFIED = "<kein Name gesetzt>";

		public static final String LABEL_DEFAULT_VALUES = "Standardwerte verwenden";
		//NUCLEUSINT-274
		public static final String LABEL_RESET_TO_DEFAULT_VALUES = "Standardwerte setzen";
	}

	/**
	 * @deprecated Should be done with the help of CommonLocaleDelegate and localeresource.json.
	 */
	public static class ENABLE_DISABLE_SLICING {
		public static final String TOOLTIP_ENABLED_SLICING = "Das Schneiden von neuen Zellen ist aktiviert";
		public static final String TOOLTIP_DISABLED_SLICING = "Das Schneiden ist ausgeschaltet. Die vorhandenen Zellen werden verwendet";
	}

	/**
	 * @deprecated Should be done with the help of CommonLocaleDelegate and localeresource.json.
	 */
	public static class PALETTE_CONTROLLER {
		public static final String TITLE_PALETTE = "Palette";

		public static final String SECTION_COLLECTABLE_COMPONENTS = "Datenkomponenten";
		// NUCLEUSINT-496
		public static final String SECTION_COLLECTABLE_LABELED_COMPONENTS = "Datenkomponenten mit Label";
		public static final String SECTION_STATIC_FIELDS = "Statische Komponenten";
		public static final String SECTION_COMPONENTS = "Containerelemente";
		public static final String SECTION_SEPARATOR = "Trennelemente";
		public static final String SECTION_TEMP = "Ablage";
	}

	/**
	 * @deprecated Should be done with the help of CommonLocaleDelegate and localeresource.json.
	 */
	public static class TABLELAYOUT_UTIL {
		public static final String ERRORMESSAGE_SUBFORM_NOT_DELETABLE_TARGET_OF_INITIAL_FOCUS = "Der Initiale Fokus liegt auf dieser Subform.\nEr muss erst entfernt werden, sonst kann sie nicht gel\u00f6scht werden.";
		public static final String ERRORMESSAGE_COMPONENT_NOT_DELETABLE_TARGET_OF_INITIAL_FOCUS = "Der Initiale Fokus liegt auf dieser Kompoente.\nEr muss erst entfernt werden, sonst kann sie nicht gel\u00f6scht werden.";
		public static final String ERRORMESSAGE_SUBFORM_NOT_DELETEABLE_TARGET_OF_RULE = "Diese Subform ist Bestandteil einer Regel";
		public static final String ERRORMESSAGE_COMPONENT_NOT_DELETABLE_TARGET_OF_RULE = "Diese Komponente ist Bestandteil einer Regel";

		public static final String MESSAGE_INPUT_DIALOG_ENTER_HEIGHT = "H\u00f6he eingeben";
		public static final String MESSAGE_INPUT_DIALOG_ENTER_WIDTH = "Breite eingeben";
	}

	/**
	 * @deprecated Should be done with the help of CommonLocaleDelegate and localeresource.json.
	 */
	public static class UNDO_REDO {
		public static final String LABEL_TOOLTIP_UNDO = "Die letzte \u00c4nderung zur\u00fccknehmen";
		public static final String LABEL_TOOLTIP_REDO = "Die zur\u00fcckgenommene \u00c4nderung wiederholen";
	}

	/**
	 * @deprecated Should be done with the help of CommonLocaleDelegate and localeresource.json.
	 */
	public static class ALIGNMENT_DIALOG {
		public static final String TOOLTIP_00 = "linksb\u00fcndig oben";
		public static final String TOOLTIP_01 = "linksb\u00fcndig mitte";
		public static final String TOOLTIP_02 = "linksb\u00fcndig unten";
		public static final String TOOLTIP_03 = "rechtsb\u00fcndig oben";
		public static final String TOOLTIP_04 = "rechtsb\u00fcndig mitte";
		public static final String TOOLTIP_05 = "rechtsb\u00fcndig unten";
		public static final String TOOLTIP_06 = "mitte oben";
		public static final String TOOLTIP_07 = "zentriert";
		public static final String TOOLTIP_08 = "mitte unten";
		public static final String TOOLTIP_09 = "ausf\u00fcllen";
		public static final String TOOLTIP_10 = "oben ausf\u00fcllend";
		public static final String TOOLTIP_11 = "mitte ausf\u00fcllend";
		public static final String TOOLTIP_12 = "unten ausf\u00fcllend";
		public static final String TOOLTIP_13 = "linke seite ausf\u00fcllen";
		public static final String TOOLTIP_14 = "mitte ausf\u00fcllen";
		public static final String TOOLTIP_15 = "rechts ausf\u00fcllen";

		public static final String LABEL_CENTERED = "ZENTRIERT";
		public static final String LABEL_FULL = "STRECKEN";
	}

	/**
	 * @deprecated Should be done with the help of CommonLocaleDelegate and localeresource.json.
	 */
	public static class MEASUREMENT_DESCRIPTIONS {
		public static final String NAME_ABSOLUTE_SIZE = "Absolute Gr\u00f6\u00dfe setzen";
		public static final String NAME_PERCENTUAL_SIZE = "Prozentuale Gr\u00f6\u00dfe setzen";
		public static final String NAME_PREFERRED_SIZE = "Bevorzugte Gr\u00f6\u00dfe setzen";
		public static final String NAME_MINIMUM_SIZE = "Minimale Gr\u00f6\u00dfe setzen";
		public static final String NAME_FILL = "Restlichen Platz nutzen";

		public static final String MESSAGE_INPUT_DIALOG_PERCENTUAL = "Bitte Wert f\u00fcr prozentuale Gr\u00f6\u00dfe eingeben";
		public static final String MESSAGE_INPUT_DIALOG_ABSOLUTE = "Bitte Wert f\u00fcr absolute Gr\u00f6\u00dfe in Pixeln eingeben:";
	}

	/**
	 * @deprecated Should be done with the help of CommonLocaleDelegate and localeresource.json.
	 */
	public static class CHANGE_SIZE_COLUMN_POPUP {

		public static final String TITLE_MENUITEM = "Spalten";
		public static final String TITLE_SUBMENU_ADD_OR_DELETE = "Spalten";
		public static final String TITLE_ADD_COLUMN_RIGHT = "Neue Spalte rechts";
		//NUCLEUSINT-966
		public static final String TITLE_ADD_MULTIPLE_COLUMNS_RIGHT = "Mehrere Spalten rechts";
		public static final String TITLE_ADD_COLUMN_LEFT = "Neue Spalte links";
		//NUCLEUSINT-966
		public static final String TITLE_ADD_MULTIPLE_COLUMNS_LEFT = "Mehrere Spalten links";
		public static final String TITLE_ADD_DEFAULT_BORDER = "Standardrand erstellen";
		public static final String TITLE_DELETE_COLUMN = "Spalte l\u00f6schen";

		public static final String MESSAGE_INPUT_DIALOG_ENTER_WIDTH = "Breite f\u00fcr Spalte eingeben";
		//NUCLEUSINT-966
		public static final String MESSAGE_INPUT_DIALOG_AMOUNT_COLS = "Anzahl der Spalten angeben";
	}

	/**
	 * @deprecated Should be done with the help of CommonLocaleDelegate and localeresource.json.
	 */
	public static class CHANGE_SIZE_ROW_POPUP {

		public static final String TITLE_MENUITEM = "Zeilen";
		public static final String TITLE_SUBMENU_ADD_OR_DELETE = "Zeilen";
		public static final String TITLE_ADD_ROW_BELOW = "Neue Zeile unterhalb";
		//NUCLEUSINT-966
		public static final String TITLE_ADD_MULTIPLE_ROWS_BELOW = "Mehrere Zeilen unterhalb";
		public static final String TITLE_ADD_ROW_ON_TOP = "Neue Zeile oberhalb";
		//NUCLEUSINT-966
		public static final String TITLE_ADD_MULTIPLE_ROWS_ON_TOP = "Mehrere Zeilen oberhalb";
		public static final String TITLE_ADD_DEFAULT_BORDER = "Standardrand erstellen";
		public static final String TITLE_DELETE_ROW = "Zeile l\u00f6schen";

		public static final String MESSAGE_INPUT_DIALOG_ENTER_HEIGHT = "H\u00f6he f\u00fcr Zeile eingeben";
		//NUCLEUSINT-966
		public static final String MESSAGE_INPUT_DIALOG_AMOUNT_ROWS = "Anzahl der Zeilen angeben";
	}

	/**
	 * @deprecated Should be done with the help of CommonLocaleDelegate and localeresource.json.
	 */
	public static class MULTIPLE_SELECTION_ROWS_AND_COLUMNS {
		public static final String TITLE_MENUITEM = "Mehrfachauswahl";
		public static final String TITLE_DELETE_ALL = "Alle l\u00f6schen";

	}

	/**
	 * @deprecated Should be done with the help of CommonLocaleDelegate and localeresource.json.
	 */
	public static class COMPONENT_POPUP {
		public static final String LABEL_MOVE_COMPONENT = "Bewegen";
		public static final String LABEL_HIDE_STANDARD_BORDER = "Standardrand ausblenden";
		public static final String LABEL_SHOW_STANDARD_BORDER = "Standardrand einblenden";
		public static final String LABEL_COMPONENT_ALIGNMENT = "Ausrichtung bearbeiten";
		public static final String LABEL_EDIT_RULES_FOR_COMPONENT = "Regeln bearbeiten";
		public static final String LABEL_DELETE_COMPONENT = "L\u00f6schen";
	}

	/**
	 * @deprecated Should be done with the help of CommonLocaleDelegate and localeresource.json.
	 */
	public static class TWO_PARTED_ALIGNMENT_DIALOG {
		public static final String TOOLTIP_00 = "Links";
		public static final String TOOLTIP_01 = "Zentriert";
		public static final String TOOLTIP_02 = "Rechts";
		public static final String TOOLTIP_03 = "Vertikal ausf\u00fcllen";
		public static final String TOOLTIP_04 = "Oben";
		public static final String TOOLTIP_05 = "Zentriert";
		public static final String TOOLTIP_06 = "Unten";
		public static final String TOOLTIP_07 = "Horizontal ausf\u00fcllen";
	}

	/**
	 * @deprecated Should be done with the help of CommonLocaleDelegate and localeresource.json.
	 */
	public static class TABLELAYOUT_PANEL {
		public static final String TITLE_ADD_LAYOUT = "Panel einf\u00fcgen";
		public static final String SELECT_FIELD_FOR_METAINFORMATION = "Bitte Feld w\u00e4hlen";
		public static final String NO_ATTRIBUTE_FOR_THIS_COMPONENT_AVAILABLE = "F\u00fcr diese Verwendung existiert kein passendes Attribut.";
	}

	/**
	 * @deprecated Should be done with the help of CommonLocaleDelegate and localeresource.json.
	 */
	public static class TABLELAYOUT_PANEL_TOOLTIP {
		public static final String COLUMN = "Spalte";
		public static final String ROW = "Zeile";
		public static final String TOOLTIPHEADER = "<html><b>" + MARKER + " " + MARKER + "</b><hr>" + MARKER + "</html>";
		
		public static final String RELATIVE_SIZE = "Relative Gr\u00f6\u00dfe";
		public static final String ABSOLUTE_SIZE = "Absolute Gr\u00f6\u00dfe";
		public static final String PIXEL = "Pixel";
		
		public static final String PREFERRED = RELATIVE_SIZE + "<br>" + "Bevorzugte Gr\u00f6\u00dfe";
		public static final String MINIMUM = RELATIVE_SIZE + "<br>" + "Minimale Gr\u00f6\u00dfe";
		public static final String FILL = RELATIVE_SIZE + "<br>" + "Restlichen Platz nutzen";
		public static final String ABSOLUTE = ABSOLUTE_SIZE + "<br>" + MARKER + " " + PIXEL;
		public static final String MARGIN_BETWEEN = "Standardrand zwischen Komponenten";
		public static final String MARGIN = "Standardrand";
	}
	
	/**
	 * @deprecated Should be done with the help of CommonLocaleDelegate and localeresource.json.
	 */
	public static class COMPONENT_PROCESSOR {
		public static final String LABEL_DEFAULTNAME_COLLECTABLE_LABEL = "Label_";
		public static final String LABEL_DEFAULTNAME_COLLECTABLE_CHECKBOX = "Checkbox_";
		public static final String LABEL_DEFAULTNAME_COLLECTABLE_TEXTFIELD = "Textfield_";
		public static final String LABEL_DEFAULTNAME_COLLECTABLE_COMBOBOX = "Combobox_";
		public static final String LABEL_DEFAULTNAME_COLLECTABLE_DATECHOOSER = "Date_";
		public static final String LABEL_DEFAULTNAME_COLLECTABLE_OPTIONGROUP = "Optiongroup_";
		public static final String LABEL_DEFAULTNAME_COLLECTABLE_LISTBROWSER = "Listbrowser_";
		public static final String LABEL_DEFAULTNAME_COLLECTABLE_TEXTAREA = "Textarea_";
		public static final String LABEL_DEFAULTNAME_SPLITPANE = "SplitPane_";
		public static final String LABEL_DEFAULTNAME_SCROLLPANE = "ScrollPane_";
		public static final String LABEL_DEFAULTNAME_TABBEDPANE = "Tabbedpane_";
		public static final String LABEL_DEFAULTNAME_TAB1 = "Reiter_1";
		public static final String LABEL_DEFAULTNAME_TAB2 = "Reiter_2";
		public static final String LABEL_DEFAULTNAME_SUBFORM = "SubForm_";
		public static final String LABEL_DEFAULTNAME_STATIC_LABEL = "Label_";
		public static final String LABEL_DEFAULTNAME_STATIC_TEXTFIELD = "TextField_";
		public static final String LABEL_DEFAULTNAME_STATIC_TEXTAREA = "TextArea_";
		public static final String LABEL_DEFAULTNAME_STATIC_COMBOBOX = "Combobox_";
		public static final String LABEL_DEFAULTNAME_STATIC_IMAGE = "IMAGE_";
		public static final String LABEL_DEFAULTNAME_BUTTON = "Button_";
		//NUCLEUSINT-1159
		public static final String LABEL_DEFAULTACTIONCOMMAND_BUTTON = STATIC_BUTTON.DUMMY_BUTTON_ACTION_LABEL;
		public static final String LABEL_DEFAULTNAME_TITLED_SEPERATOR = "Titled Separator_";

		public static final String ERRORMESSAGE_NOT_SUPPORTED_CONTROLTYPE = "Nicht unterst\u00fctzter Controltype=" + MARKER + " f\u00fcr ein Collectable Component wurde verwendet.";
		public static final String ERRORMESSAGE_UNSUPPORTED_ELEMENT = "Nicht unterst\u00fctzte Componente!";
	}
	
	/**
	 * @deprecated Should be done with the help of CommonLocaleDelegate and localeresource.json.
	 */
	public static class VALIDATION_MESSAGES {
		public static final String PREFERREDSIZE_WIDTH = "Bevorzugte Gr\u00f6\u00dfe: Breite muss gr\u00f6\u00dfer als " + MARKER + " sein.";
		public static final String PREFERREDSIZE_HEIGHT = "Bevorzugte Gr\u00f6\u00dfe: H\u00f6he muss gr\u00f6\u00dfer als " + MARKER + " sein.";
		public static final String CONTROLTYPEANDCONTROLYPECLASS = "Die Eigenschaften " + PROPERTY_LABELS.CONTROLTYPE + " und " + PROPERTY_LABELS.CONTROLTYPECLASS + " k\u00f6nnen nicht gleichzeitig angegeben werden.";
	}
	
	/**
	 * @deprecated Should be done with the help of CommonLocaleDelegate and localeresource.json.
	 */
	public static class DUMMYBUTTONACTION {
		public static final String MESSAGE = "Der Button wurde angeklickt...";
	}
	
	/**
	 * @deprecated Should be done with the help of CommonLocaleDelegate and localeresource.json.
	 */
	//NUCLEUSINT-1159
	public static class CHANGESTATEACTIONLISTENER {
		public static final String NO_GENERICOBJECT = "Der Statuswechsel ist nur in Modulen verf\u00fcgbar.";
	}

	/**
	 * Small Helperclass as Structure for the Items shown in the Palette
	 * 
	 * @author hartmut.beckschulze
	 *
	 */
	public interface PaletteItemElement {
		public String getToolTip();
		public String getLabel();
		public Icon getIcon();
		public boolean displayLabelAndIcon();
		//NUCLEUSINT-496
		public boolean isLabeledComponent();

	}

	/**
	 * Method that should be used for concated Strings in Errormessages.
	 * The String contains the MARKER at the position where the passed Parameter should be inserted.
	 * 
	 * @param message The Message containing MARKERs
	 * @param additionalInfo variable List of Strings to insert instead of MARKER
	 * @return String with the concated (Error) Message
	 */
	public static String partedString(String message, String... additionalInfo) {
		String returnValue = new String(message);
		for (String string : additionalInfo) {
			returnValue = returnValue.replaceFirst(MARKER, string);
		}
		return returnValue;
	}
}
