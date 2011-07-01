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
package org.nuclos.client.common;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JComponent;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import org.apache.log4j.Logger;
import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;
import org.nuclos.client.ui.collect.component.CollectableComboBox;
import org.nuclos.common.ParameterProvider;
import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common.collect.collectable.CollectableFieldsProvider;

/**
 * Custom <code>CollectableComboBox</code> for Nucleus. The referencing listener is installed right here.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */
public class NuclosCollectableComboBox extends CollectableComboBox {
	private static final Logger log = Logger.getLogger(NuclosCollectableComboBox.class);
	private Integer iColumns;
	private boolean autoCompleteDecorated;
	private Color focusColor = Utils.translateColorFromParameter(ParameterProvider.KEY_FOCUSSED_ITEM_BACKGROUND_COLOR);

	/**
	 *
	 * @param clctef
	 * @param bSearchable
	 * @postcondition this.getReferencingListener() != null
	 */
	public NuclosCollectableComboBox(CollectableEntityField clctef, Boolean bSearchable) {
		super(clctef, bSearchable);
		this.setReferencingListener(NuclosLOVListener.getInstance());
		this.getJComboBox().setMaximumRowCount(20);
		// Delay the auto-complete decoration so that "editable" state will not become
		// polluted by the decorator
		this.getJComboBox().addAncestorListener(new AncestorListener() {
			@Override
			public void ancestorAdded(AncestorEvent event) {
				if (!autoCompleteDecorated) {
					NuclosCollectableComboBox.this.removeDocumentListenerForEditor();
					AutoCompleteDecorator.decorate(NuclosCollectableComboBox.this.getJComboBox());
					if(NuclosCollectableComboBox.this.hasDocumentListenerForEditor())
						NuclosCollectableComboBox.this.addDocumentListenerForEditor();
					autoCompleteDecorated = true;
				}
			}
			@Override
			public void ancestorMoved(AncestorEvent event) {
			}
			@Override
			public void ancestorRemoved(AncestorEvent event) {
			}
		});
	}
	
	@Override
	public void setInsertable(boolean bInsertable) {
		if (!autoCompleteDecorated) {
			super.setInsertable(bInsertable);
		} else if (bInsertable != isInsertable()) {
			log.warn("Ignore call to setInsertable() because combobox is already decorated with auto-completion");
		}
	}

	/**
	 * Other than the default <code>CollectableComboBox</code>, this implementation respects setColumns().
	 * @param iColumns
	 */
	@Override
	public void setColumns(int iColumns) {
		this.iColumns = iColumns;
	}

	@Override
	protected Integer getColumns() {
		return this.iColumns;
	}

	@Override
	public JComponent getFocusableComponent() {
		final Component ce = getJComboBox().getEditor().getEditorComponent();
		return (ce instanceof JComponent) ? (JComponent) ce : getJComboBox();
	}

	/**
	 * In addition to the default <code>CollectableComboBox</code>, this implementation distinguishes the contents of a combo box whether it is in search or details mask.
	 * @param clctfsprovider
	 */
	@Override
	public void setValueListProvider(CollectableFieldsProvider clctfsprovider) {
		super.setValueListProvider(clctfsprovider);

		if (clctfsprovider != null && isSearchComponent()) {
			clctfsprovider.setParameter("_searchmode", Boolean.TRUE);
		}
	}

	@Override
    protected Color getBackgroundColor() {
	    Color bg = super.getBackgroundColor();
	    if(bg == null && getJComboBox().getEditor() != null && getJComboBox().getEditor().getEditorComponent().hasFocus()) 
			bg = focusColor;
	    return bg;
    }
}	// class NuclosCollectableComboBox
