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
package org.nuclos.client.wizard.steps;

import info.clearthought.layout.TableLayout;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.prefs.Preferences;

import javax.swing.Icon;
import javax.swing.JOptionPane;
import javax.swing.JToolBar;

import org.nuclos.client.entityobject.CollectableEntityObject;
import org.nuclos.client.main.mainframe.MainFrameTab;
import org.nuclos.client.masterdata.MasterDataSubFormController;
import org.nuclos.client.masterdata.MetaDataDelegate;
import org.nuclos.client.ui.collect.SubForm;
import org.nuclos.client.ui.collect.component.CollectableComponentType;
import org.nuclos.client.ui.collect.component.model.CollectableComponentModel;
import org.nuclos.client.ui.collect.component.model.CollectableComponentModelProvider;
import org.nuclos.client.ui.collect.component.model.DetailsComponentModel;
import org.nuclos.client.valuelistprovider.cache.CollectableFieldsProviderCache;
import org.nuclos.common.NuclosBusinessException;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.TranslationVO;
import org.nuclos.common.WorkspaceDescription.EntityPreferences;
import org.nuclos.common.collect.collectable.CollectableUtils;
import org.nuclos.common.collect.collectable.DefaultCollectableEntityProvider;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.Transformer;
import org.nuclos.common.dal.vo.EntityObjectVO;
import org.nuclos.common2.SpringLocaleDelegate;
import org.nuclos.common2.StringUtils;
import org.nuclos.common2.exception.CommonValidationException;
import org.pietschy.wizard.InvalidStateException;

/**
* <br>
* Created by Novabit Informationssysteme GmbH <br>
* Please visit <a href="http://www.novabit.de">www.novabit.de</a>
*/
public class NuclosEntityProcessStep extends NuclosEntityAbstractStep {

	private static final long serialVersionUID = 2900241917334839766L;

	private static final String ENTITYNAME_PROCESS = NuclosEntity.PROCESS.getEntityName();

	public static final String[] labels = TranslationVO.labelsEntity;
	
	private SubForm subform;
	private MasterDataSubFormController subFormController;

	public NuclosEntityProcessStep() {
		initComponents();
	}

	public NuclosEntityProcessStep(String name, String summary) {
		super(name, summary);
		initComponents();
	}

	public NuclosEntityProcessStep(String name, String summary, Icon icon) {
		super(name, summary, icon);
		initComponents();
	}

	@Override
	protected void initComponents() {
		double size [][] = {{TableLayout.FILL}, {TableLayout.FILL}};
		TableLayout layout = new TableLayout(size);
		layout.setVGap(3);
		layout.setHGap(5);
		setLayout(layout);
	}

	@Override
	public void prepare() {
		super.prepare();
		this.removeAll();

		subform = new SubForm(ENTITYNAME_PROCESS, JToolBar.VERTICAL, "module");

		SubForm.Column column = new SubForm.Column("nuclet", "Nuclet", new CollectableComponentType(CollectableUtils.getCollectableComponentTypeForClass(String.class), null), false, false, false, 0, 0);
		subform.addColumn(column);
		this.add(subform, "0,0");

		CollectableComponentModelProvider provider = new CollectableComponentModelProvider() {

			private CollectableComponentModel entityModel = new DetailsComponentModel(DefaultCollectableEntityProvider.getInstance().getCollectableEntity(NuclosEntity.ENTITY.getEntityName()).getEntityField("entity"));

			@Override
			public Collection<String> getFieldNames() {
				return Collections.singleton("entity");
			}

			@Override
			public Collection<? extends CollectableComponentModel> getCollectableComponentModels() {
				return Collections.singleton(entityModel);
			}

			@Override
			public CollectableComponentModel getCollectableComponentModelFor(String sFieldName) {
				if ("entity".equals(sFieldName)) {
					return entityModel;
				}
				return null;
			}
		};
		MainFrameTab tab = getModel().getParentFrame();

		Preferences prefs = java.util.prefs.Preferences.userRoot().node("org/nuclos/client/entitywizard/steps/process");

		subFormController = new ProcessSubformController(tab, provider,ENTITYNAME_PROCESS, subform, prefs, 
				getEntityPreferences(), null);
		Collection<EntityObjectVO> data = model.getProcesses();

		if (data != null) {
			try {
				subFormController.clear();
				subFormController.fillSubForm(null, data);
			} catch (NuclosBusinessException e) {
				throw new NuclosFatalException(e);
			}
		}

		setComplete(true);
	}

	@Override
	public void close() {
		if (subFormController != null) {
			subFormController.close();
		}
		subFormController = null;
		if (subform != null) {
			subform.close();
		}
		subform = null;

		super.close();
	}

	@Override
	public void applyState() throws InvalidStateException {
		List<CollectableEntityObject> subformdata;
		try {
			subformdata = subFormController.getCollectables(true, true, true);
		} catch (CommonValidationException e1) {
			JOptionPane.showMessageDialog(this, SpringLocaleDelegate.getInstance().getMessageFromResource(e1.getMessage()),
	    			SpringLocaleDelegate.getInstance().getMessage("wizard.step.processes.error.title", "Achtung!"), JOptionPane.OK_OPTION);
 	        throw new InvalidStateException();
		}
		Collection<EntityObjectVO> processes = CollectionUtils.transform(subformdata, new Transformer<CollectableEntityObject, EntityObjectVO>() {
			@Override
			public EntityObjectVO transform(CollectableEntityObject i) {
				return i.getEntityObjectVO();
			}
		});
		// validate processes
		Set<String> names = new HashSet<String>();
		for (EntityObjectVO process : processes) {
			String name = process.getField("name");
			if (StringUtils.isNullOrEmpty(name)) {
				JOptionPane.showMessageDialog(this, SpringLocaleDelegate.getInstance().getMessage(
						"wizard.step.processes.error.name.mandatory", "Bitte definieren Sie für jede Aktion einen Namen."),
		    			SpringLocaleDelegate.getInstance().getMessage("wizard.step.processes.error.title", "Achtung!"), JOptionPane.OK_OPTION);
	 	        throw new InvalidStateException();
			}
			if (!names.add(name)) {
				JOptionPane.showMessageDialog(this, SpringLocaleDelegate.getInstance().getMessage(
						"wizard.step.processes.error.name.unique", "Der Name einer Aktion muss eindeutig sein ({0}).", name),
		    			SpringLocaleDelegate.getInstance().getMessage("wizard.step.processes.error.title", "Achtung!"), JOptionPane.OK_OPTION);
	 	        throw new InvalidStateException();
			}
		}
		model.setProcesses(processes);
		subFormController.close();
		super.applyState();
		
		// close Subform support
		subform.close();
		subform = null;
		
		super.applyState();
	}

	private class ProcessSubformController extends MasterDataSubFormController {

		public ProcessSubformController(MainFrameTab tab, CollectableComponentModelProvider clctcompmodelproviderParent, String sParentEntityName, SubForm subform, Preferences prefsUserParent, 
				EntityPreferences entityPrefs, CollectableFieldsProviderCache valueListProviderCache) {
			super(tab, clctcompmodelproviderParent, sParentEntityName, subform, prefsUserParent, entityPrefs, valueListProviderCache);
		}

		@Override
		protected void removeSelectedRows() {
			for (CollectableEntityObject process : this.getSelectedCollectables()) {
				if (process.getId() != null) {
					try {
						MetaDataDelegate.getInstance().tryRemoveProcess(process.getEntityObjectVO());
					}
					catch (NuclosBusinessException e) {
						JOptionPane.showMessageDialog(NuclosEntityProcessStep.this, 
								getSpringLocaleDelegate().getMessage(
										"wizard.step.processes.error.removeprocess", "Aktion {0} ist bereits in Verwendung und kann nicht entfernt werden.", process.getField("name")),
								getSpringLocaleDelegate().getMessage(
										"wizard.step.processes.error.title", "Achtung!"), JOptionPane.OK_OPTION);
						return;
					}
				}
			}
			super.removeSelectedRows();
		}

		@Override
		public CollectableEntityObject newCollectable() {
			CollectableEntityObject result = super.newCollectable();
			long i = -1;
			for (CollectableEntityObject o : getCollectableTableModel().getCollectables()) {
				if (o.getEntityObjectVO().getId() <= i) {
					i = o.getEntityObjectVO().getId() - 1;
				}
			}
			result.getEntityObjectVO().setId(i);
			return result;
		}

	}
}
