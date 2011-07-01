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
package org.nuclos.client.layout.wysiwyg.editor.util.valueobjects;

import java.io.Serializable;

import org.nuclos.client.layout.wysiwyg.component.TranslationMap;
import org.nuclos.common2.layoutml.LayoutMLConstants;

/**
 * This Class stores the Values needed for {@link LayoutMLConstants#ELEMENT_OPTION}.<br>
 * They are stored in {@link WYSIWYGOptions}.<br>
 * 
 * <br>
 * Created by Novabit Informationssysteme GmbH <br>
 * Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 * 
 * @author <a href="mailto:hartmut.beckschulze@novabit.de">hartmut.beckschulze</a>
 * @version 01.00.00
 */
@SuppressWarnings("serial")
public class WYSIWYGOption implements Cloneable, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String name = "";
	private String value = "";
	private String label = "";
	private String mnemonic = "";
	private TranslationMap translations;

	/**
	 * 
	 * @param name the name for this Option
	 * @param value the value for this Option
	 * @param label the Label to display for this Option
	 * @param mnemonic the Shortcut Key for this Option
	 */
	public WYSIWYGOption(String name, String value, String label, String mnemonic) {
		setName(name);
		setValue(value);
		setLabel(label);
		setMnemonic(mnemonic);
	}

	/**
	 * Empty Constructor
	 */
	public WYSIWYGOption() {
	}

	/**
	 * @param name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @param value to set
	 */
	public void setValue(String value) {
		this.value = value;
	}

	/**
	 * @param label to set
	 */
	public void setLabel(String label) {
		this.label = label;
	}

	/**
	 * @param mnemonic shortcut key to set
	 */
	public void setMnemonic(String mnemonic) {
		this.mnemonic = mnemonic;
	}

	/**
	 * @return the Name of this Option
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the Value for this Option
	 */
	public String getValue() {
		return value;
	}

	/**
	 * @return the Label for this Option, is never a <b>null</b> value
	 */
	public String getLabel() {
		if (label == null)
			return "";
		else
			return label;
	}

	/**
	 * @return returning the Shortcut Key for this Option, is never a <b>null</b> value
	 */
	public String getMnemonic() {
		if (mnemonic == null)
			return "";
		else
			return mnemonic;
	}
	
	public TranslationMap getTranslations() {
		return translations;
	}

	public void setTranslations(TranslationMap translations) {
		this.translations = translations;
	}

	/**
	 * Overwritten clone Method, creating a new Instance of this Object<br>
	 * called by {@link WYSIWYGOptions#clone()}
	 */
	@Override
	public Object clone() throws CloneNotSupportedException {
		WYSIWYGOption clonedOption = new WYSIWYGOption();
		clonedOption.setName(new String(name));
		clonedOption.setValue(new String(value));
		clonedOption.setLabel(new String(label));
		if (mnemonic == null)
			clonedOption.setMnemonic("");
		else
			clonedOption.setMnemonic(new String(mnemonic));
		if (translations != null)
			clonedOption.setTranslations(new TranslationMap(translations));

		return clonedOption;
	}
}
