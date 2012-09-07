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
import org.nuclos.client.common.NuclosCollectableImage;
import org.nuclos.client.entityobject.CollectableEOEntityClientProvider;
import org.nuclos.client.resource.ResourceCache;
import org.nuclos.client.ui.collect.component.CollectableColorChooserButton;
import org.nuclos.client.ui.collect.component.CollectableResourceIconChooserButton;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.collect.collectable.AbstractCollectableEntityField;
import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common.collect.collectable.CollectableValueField;
import org.nuclos.common.collect.collectable.CollectableValueIdField;
import org.nuclos.common.collect.exception.CollectableFieldFormatException;
import org.nuclos.common2.StringUtils;
import org.nuclos.server.resource.valueobject.ResourceVO;

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
	
	private static final Logger LOG = Logger.getLogger(StatePropertiesPanelModel.class);
	
	// workaround - cause statemodel does not have an collectable metainformation yet.
	public class CollectableImageEntityField extends AbstractCollectableEntityField {

		@Override
		public int getFieldType() {
			return CollectableEntityField.TYPE_VALUEFIELD;
		}

		@Override
		public Class<?> getJavaClass() {
			return org.nuclos.common.NuclosImage.class;
		}

		@Override
		public String getName() {
			return null;
		}

		@Override
		public String getEntityName() {
			return null;
		}

		@Override
		public String getFormatInput() {
			return null;
		}

		@Override
		public String getFormatOutput() {
			return null;
		}

		@Override
		public String getLabel() {
			return null;
		}

		@Override
		public String getDescription() {
			return null;
		}

		@Override
		public Integer getMaxLength() {
			return null;
		}

		@Override
		public Integer getPrecision() {
			return null;
		}

		@Override
		public boolean isNullable() {
			return false;
		}

		@Override
		public boolean isReferencing() {
			return false;
		}

		@Override
		public String getReferencedEntityName() {
			return null;
		}

		@Override
		public String getDefaultComponentType() {
			return null;
		}	}
	
	public static final String PROPERTY_SHAPE_NAME = "ShapeName";
	public static final String PROPERTY_SHAPE_MNEMONIC = "ShapeMnemonic";
	public static final String PROPERTY_SHAPE_ICON = "ShapeIcon";
	public static final String PROPERTY_SHAPE_DESCRIPTION = "ShapeDescription";

	public Document docName = new PlainDocument();
	public Document docMnemonic = new PlainDocument();
	public Document docDescription = new PlainDocument();
	public ComboBoxModel modelTab = new DefaultComboBoxModel();
	public final NuclosCollectableImage clctImage;
	public Document docButtonLabel = new PlainDocument();
	public CollectableResourceIconChooserButton clctButtonIcon;
	public CollectableColorChooserButton clctColor;

	protected static final Logger log = Logger.getLogger(StatePropertiesPanelModel.class);
	
	private Map<String, String> mpTabName = new HashMap<String, String>();
	
	public StatePropertiesPanelModel() {
		this.clctImage = new NuclosCollectableImage(new CollectableImageEntityField());
		this.clctButtonIcon = new CollectableResourceIconChooserButton(
				CollectableEOEntityClientProvider.getInstance().getCollectableEntity(NuclosEntity.STATE.getEntityName()).getEntityField("buttonIcon"));
		this.clctColor = new CollectableColorChooserButton(
				CollectableEOEntityClientProvider.getInstance().getCollectableEntity(NuclosEntity.STATE.getEntityName()).getEntityField("color"));
	}

	public String getName() {
		String sResult = "";
		try {
			sResult = docName.getText(0, docName.getLength());
		}
		catch (BadLocationException e) {
			// this should never happens
			LOG.warn("getName failed: " + e, e);
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
	
	public String getButtonLabel() {
		String sResult = "";
		try {
			sResult = docButtonLabel.getText(0, docButtonLabel.getLength());
		}
		catch (BadLocationException e) {
			// this should never happens
			LOG.warn("getButtonLabel failed: " + e, e);
		}
		return sResult;
	}
	
	public void setButtonLabel(String sButtonLabel) {
		try {
			this.docButtonLabel.remove(0, docButtonLabel.getLength());
			this.docButtonLabel.insertString(0, sButtonLabel, null);
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
			// this should never happens
			LOG.warn("getNumeral failed: " + e, e);
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

	public void setIcon(org.nuclos.common.NuclosImage icon) {
		clctImage.setField(new CollectableValueField(icon));
	}

	public org.nuclos.common.NuclosImage getIcon() {
		try {
			return (org.nuclos.common.NuclosImage) clctImage.getField().getValue();
		}
		catch (CollectableFieldFormatException ex) {
			throw new NuclosFatalException(ex);
		}		
	}
	
	public void setButtonIcon(ResourceVO resButtonIcon) {
		clctButtonIcon.setField(new CollectableValueIdField(resButtonIcon==null?null:resButtonIcon.getId(), resButtonIcon==null?null:resButtonIcon.getName()));
	}
	
	public ResourceVO getButtonIcon() {
		try {
			CollectableValueIdField clctef = (CollectableValueIdField) clctButtonIcon.getField();
			return ResourceCache.getInstance().getResourceById((Integer) clctef.getValueId());
		}
		catch (CollectableFieldFormatException ex) {
			throw new NuclosFatalException(ex);
		}	
	}
	
	public void setColor(String color) {
		clctColor.setField(new CollectableValueField(color));
	}
	
	public String getColor() {
		try {
			return (String) clctColor.getField().getValue();
		}
		catch (CollectableFieldFormatException ex) {
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
