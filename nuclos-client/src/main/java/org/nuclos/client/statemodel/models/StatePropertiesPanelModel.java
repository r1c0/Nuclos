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
package org.nuclos.client.statemodel.models;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;

import org.apache.log4j.Logger;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common2.StringUtils;

/**
 * Model for the panel displaying the state models.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Boris@novabit.de">Boris</a>
 * @version 01.00.00
 */
public class StatePropertiesPanelModel implements Serializable {
	public static final String PROPERTY_SHAPE_NAME = "ShapeName";
	public static final String PROPERTY_SHAPE_MNEMONIC = "ShapeMnemonic";
	public static final String PROPERTY_SHAPE_DESCRIPTION = "ShapeDescription";

	public Document docName = new PlainDocument();
	public Document docMnemonic = new PlainDocument();
	public Document docDescription = new PlainDocument();
	public ComboBoxModel modelTab = new DefaultComboBoxModel();

	protected static final Logger log = Logger.getLogger(StatePropertiesPanelModel.class);
	
	private Map<String, String> mpTabName = new HashMap<String, String>();
	
	public StatePropertiesPanelModel() {
	}

	public String getName() {
		String sResult = "";
		try {
			sResult = docName.getText(0, docName.getLength());
		}
		catch (BadLocationException e) {
			e.printStackTrace();	// this should never happens
		}
		return sResult;
	}

	public void setName(String sName) {
		try {
			this.docName.remove(0, docName.getLength());
			this.docName.insertString(0, sName, null);
		}
		catch (BadLocationException ex) {
			throw new NuclosFatalException(ex);
		}
	}

	public Integer getNumeral() {
		Integer iNumeral = null;
		String strValue = "";
		try {
			strValue = docMnemonic.getText(0, docMnemonic.getLength());
		    /** otherwise clearing the field is resulting every time in a annoying dialog, the validation for null value is done later and is safe */
		    if (!StringUtils.isNullOrEmpty(strValue))
			iNumeral = new Integer(strValue);
		}
		catch (BadLocationException e) {
			e.printStackTrace();	// this should never happens
		} catch (NumberFormatException e) {
			log.warn("Das Statusnumeral ["+strValue+"] ist kein numerischer Wert.", e);
		}
		return iNumeral;
	}

	public void setNumeral(Integer iNumeral) {
		try {
			this.docMnemonic.remove(0, docMnemonic.getLength());
			if (iNumeral != null) {
				this.docMnemonic.insertString(0, iNumeral.toString(), null);
			}
		}
		catch (BadLocationException ex) {
			throw new NuclosFatalException(ex);
		}
	}

	public String getDescription() {
		final String result;
		try {
			result = docDescription.getText(0, docDescription.getLength());
		}
		catch (BadLocationException ex) {
			throw new NuclosFatalException(ex);
		}
		return result;
	}

	public void setDescription(String sDescription) {
		try {
			this.docDescription.remove(0, docDescription.getLength());
			if (sDescription != null) {
				this.docDescription.insertString(0, sDescription, null);
			}
		}
		catch (BadLocationException ex) {
			throw new NuclosFatalException(ex);
		}
	}
	
	public String getTab() {
		String sReturn = "";
		for(String key : mpTabName.keySet()) {
			if(mpTabName.get(key).equals(modelTab.getSelectedItem())) {
				sReturn = key;
				break;
			}
		}
		 
		return sReturn;
	}
	
	public void setTab(String sTab) {		
		modelTab.setSelectedItem(mpTabName.get(sTab));
	}
	
	public void setTabModelList(Map<String, String> mp) {
		mpTabName = mp;
		if(modelTab instanceof DefaultComboBoxModel) {
			DefaultComboBoxModel model = (DefaultComboBoxModel)modelTab;
			model.removeAllElements();
			for(String sName : mp.values()) {
				model.addElement(sName);
			}
		}
		
	}
}
