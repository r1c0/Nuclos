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
package org.nuclos.client.ui.collect;

import java.util.Collection;

import javax.swing.JComponent;

import org.nuclos.client.ui.collect.component.CollectableComponent;
import org.nuclos.client.ui.collect.component.model.DefaultDetailsEditModel;
import org.nuclos.client.ui.collect.component.model.DefaultSearchEditModel;
import org.nuclos.client.ui.collect.component.model.EditModel;
import org.nuclos.common.collect.exception.CollectableFieldFormatException;
import org.nuclos.common2.EntityAndFieldName;

/**
 * Default implementation of <code>EditView</code>.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:christoph.radig@novabit.de">christoph.radig</a>
 * @version 01.00.00
 */
public class DefaultEditView implements EditView {

	private final JComponent compRoot;

	private final CollectableComponentsProvider clctcompprovider;

	private final EditModel model;
	
	private final EntityAndFieldName initialFocusField;

	public static DefaultEditView newSearchEditView(JComponent compRoot, CollectableComponentsProvider clctcompprovider, EntityAndFieldName initialFocusField) {
		return new DefaultEditView(compRoot, clctcompprovider, true, initialFocusField);
	}

	public static DefaultEditView newDetailsEditView(JComponent compRoot, CollectableComponentsProvider clctcompprovider) {
		return new DefaultEditView(compRoot, clctcompprovider, false, null);
	}

	public static DefaultEditView newDetailsEditView(JComponent compRoot, CollectableComponentsProvider clctcompprovider, EntityAndFieldName initialFocusField) {
		return new DefaultEditView(compRoot, clctcompprovider, false, initialFocusField);
	}

	protected DefaultEditView(JComponent compRoot, CollectableComponentsProvider clctcompprovider, boolean bForSearch, EntityAndFieldName initialFocusField) {
		this(compRoot, clctcompprovider, newEditModel(clctcompprovider, bForSearch), initialFocusField);
	}

	protected DefaultEditView(JComponent compRoot, CollectableComponentsProvider clctcompprovider, EditModel model, EntityAndFieldName initialFocusField) {
		this.compRoot = compRoot;
		this.clctcompprovider = clctcompprovider;
		this.model = model;
		this.initialFocusField = initialFocusField;
	}
	
	@Override
	public JComponent getJComponent() {
		return this.compRoot;
	}

	@Override
	public EditModel getModel() {
		return this.model;
	}
	
	public EntityAndFieldName getInitialFocusField() {
		return initialFocusField;
	}

	@Override
	public Collection<CollectableComponent> getCollectableComponents() {
		return this.clctcompprovider.getCollectableComponents();
	}
	
	//NUCLEUSINT-442
	@Override
	public Collection<CollectableComponent> getCollectableLabels() {
		return this.clctcompprovider.getCollectableLabels();
	}

	@Override
	public Collection<CollectableComponent> getCollectableComponentsFor(String sFieldName) {
		return this.clctcompprovider.getCollectableComponentsFor(sFieldName);
	}

	// @todo make public and move to Utils class?
	private static EditModel newEditModel(CollectableComponentsProvider clctcompprovider, boolean bForSearch) {
		final Collection<CollectableComponent> clctcomp = clctcompprovider.getCollectableComponents();
		return bForSearch ? new DefaultSearchEditModel(clctcomp) : new DefaultDetailsEditModel(clctcomp);
	}

	@Override
	public void makeConsistent(String sFieldName) throws CollectableFieldFormatException {
		// make the model consistent with the view:
		for (CollectableComponent clctcomp : this.getCollectableComponentsFor(sFieldName)) {
			clctcomp.makeConsistent();
		}
	}

	@Override
	public void makeConsistent() throws CollectableFieldFormatException {
		// make the model consistent with the view:
		for (CollectableComponent clctcomp : this.getCollectableComponents()) {
			clctcomp.makeConsistent();
		}
	}

	@Override
	public void setComponentsEnabled(boolean bEnabled) {
		for (CollectableComponent clctcomp : this.getCollectableComponents()) {
			clctcomp.setEnabled(bEnabled);
		}
	}

}	// class DefaultEditView
