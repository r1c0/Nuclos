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
package org.nuclos.client.ui.collect.component;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import org.apache.log4j.Logger;
import org.nuclos.client.common.NuclosLOVListener;
import org.nuclos.client.resource.NuclosResourceCategory;
import org.nuclos.client.resource.ResourceCache;
import org.nuclos.client.ui.collect.CollectController.CollectableEventListener;
import org.nuclos.client.ui.collect.CollectController.MessageType;
import org.nuclos.client.ui.resource.ResourceIconChooser;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.collect.collectable.Collectable;
import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common.collect.collectable.CollectableField;
import org.nuclos.common.collect.collectable.CollectableValueField;
import org.nuclos.common.collect.collectable.CollectableValueIdField;
import org.nuclos.common.collect.exception.CollectableFieldFormatException;
import org.nuclos.server.resource.valueobject.ResourceVO;

/**
 * A <code>CollectableComponent</code> that presents a resource icon.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:maik.stueker@nuclos.de">Maik Stueker</a>
 * @version	01.00.00
 */
public class CollectableResourceIconChooserButton extends AbstractCollectableComponent implements CollectableEventListener {

	private static final Logger LOG = Logger.getLogger(CollectableResourceIconChooserButton.class);
	
	private NuclosResourceCategory resCategory;

	private final ItemListener itemlistener = new ItemListener() {
		@Override
        public void itemStateChanged(ItemEvent ev) {
			try {
				CollectableResourceIconChooserButton.this.viewToModel();
			}
			catch (CollectableFieldFormatException ex) {
				assert false;
			}
		}
	};

	/**
	 * @param clctef
	 * @postcondition this.isDetailsComponent()
	 */
	public CollectableResourceIconChooserButton(CollectableEntityField clctef) {
		this(clctef, false);

		assert this.isDetailsComponent();
	}

	public CollectableResourceIconChooserButton(CollectableEntityField clctef, boolean bSearchable) {
		super(clctef, new ResourceIconChooser.Button(clctef.getLabel(), null, null), bSearchable);
		getResourceIconChooserButton().addItemListener(this.itemlistener);
		if (isCustomResource()) {
			this.setReferencingListener(NuclosLOVListener.getInstance());
		}
	}

	@Override
	public ResourceIconChooser.Button getJComponent() {
		return (ResourceIconChooser.Button) super.getJComponent();
	}

	private ResourceIconChooser.Button getResourceIconChooserButton() {
		return (ResourceIconChooser.Button) super.getJComponent();
	}
	
	private boolean isCustomResource() {
		return NuclosEntity.RESOURCE.checkEntityName(getEntityField().getReferencedEntityName());
	}

	@Override
	protected void updateView(CollectableField clctfValue) {
		final String sRes = (String) clctfValue.getValue();
		getResourceIconChooserButton().setResource(sRes);
	}

	@Override
    public CollectableField getFieldFromView() {
		final ResourceIconChooser.Button btn = this.getResourceIconChooserButton();
		String sRes = btn.getResource();
		if (isCustomResource()) {
			Integer iRes = null;
			if (sRes != null) {
				ResourceVO res = ResourceCache.getInstance().getResourceByName(btn.getResource());
				if (res != null) {
					iRes = res.getId();
				} else {
					sRes = null;
				}
			}
			return new CollectableValueIdField(iRes, sRes);
		} else {
			return new CollectableValueField(sRes);
		}
	}

	@Override
	public void setLabelText(String sLabel) {
		this.getResourceIconChooserButton().setLabel(sLabel);
	}

	@Override
	public void setInsertable(boolean bInsertable) {
		// do nothing
	}

	@Override
	public void setProperty(String sName, Object oValue) {
		super.setProperty(sName, oValue);
		if (NuclosResourceCategory.PROPERTY_NAME.equals(sName)) {
			resCategory = NuclosResourceCategory.valueOf((String) oValue);
			getResourceIconChooserButton().setCategory(resCategory);
		}
	}
	
	@Override
	public void handleCollectableEvent(final Collectable collectable, final MessageType messageType) {
		switch (messageType) {
			case EDIT_DONE:
			case DELETE_DONE:
			case STATECHANGE_DONE:
			case NEW_DONE:
				if (collectable != null) {
					getResourceIconChooserButton().setResource((String) collectable.getValue("name"));
				} else {
					getResourceIconChooserButton().setResource(null);
				}
				try {
					viewToModel();
				} catch (CollectableFieldFormatException e) {
					assert false;
				}
				break;
		}
	}

}  // class CollectableCheckBox
