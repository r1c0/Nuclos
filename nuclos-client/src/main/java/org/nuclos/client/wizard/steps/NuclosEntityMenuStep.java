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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.prefs.Preferences;

import javax.annotation.PostConstruct;
import javax.swing.Icon;
import javax.swing.JOptionPane;
import javax.swing.JToolBar;

import org.nuclos.client.common.LocaleDelegate;
import org.nuclos.client.entityobject.CollectableEOEntityClientProvider;
import org.nuclos.client.entityobject.CollectableEntityObject;
import org.nuclos.client.main.mainframe.MainFrameTab;
import org.nuclos.client.masterdata.MasterDataSubFormController;
import org.nuclos.client.ui.collect.SubForm;
import org.nuclos.client.ui.collect.component.CollectableComponentType;
import org.nuclos.client.ui.collect.component.model.CollectableComponentModel;
import org.nuclos.client.ui.collect.component.model.CollectableComponentModelProvider;
import org.nuclos.client.ui.collect.component.model.DetailsComponentModel;
import org.nuclos.common.NuclosBusinessException;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.TranslationVO;
import org.nuclos.common.collect.collectable.AbstractCollectableEntity;
import org.nuclos.common.collect.collectable.CollectableComponentTypes;
import org.nuclos.common.collect.collectable.CollectableEntity;
import org.nuclos.common.collect.collectable.CollectableField;
import org.nuclos.common.collect.collectable.CollectableFieldsProvider;
import org.nuclos.common.collect.collectable.CollectableValueIdField;
import org.nuclos.common.collect.collectable.DefaultCollectableEntityField;
import org.nuclos.common.collect.collectable.DefaultCollectableEntityProvider;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.Transformer;
import org.nuclos.common.dal.vo.EntityObjectVO;
import org.nuclos.common2.LocaleInfo;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.exception.CommonValidationException;
import org.pietschy.wizard.InvalidStateException;
import org.springframework.beans.factory.annotation.Configurable;

/**
* <br>
* Created by Novabit Informationssysteme GmbH <br>
* Please visit <a href="http://www.novabit.de">www.novabit.de</a>
*/
@Configurable
public class NuclosEntityMenuStep extends NuclosEntityAbstractStep {

	private static final long serialVersionUID = 2900241917334839766L;

	private static final String ENTITYNAME_MENU = NuclosEntity.ENTITYMENU.getEntityName();

	public static final String[] labels = TranslationVO.labelsEntity;
	
	//

	private SubForm subform;
	private MasterDataSubFormController subFormController;

	public NuclosEntityMenuStep() {
		// initComponents();
	}

	public NuclosEntityMenuStep(String name, String summary) {
		super(name, summary);
		// initComponents();
	}

	public NuclosEntityMenuStep(String name, String summary, Icon icon) {
		super(name, summary, icon);
		// initComponents();
	}

	@PostConstruct
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

		subform = new SubForm(ENTITYNAME_MENU, JToolBar.VERTICAL, "entity");

		CollectableEntity clcte = new EntityMenuCollectableEntity(CollectableEOEntityClientProvider.getInstance().getCollectableEntity(NuclosEntity.ENTITYMENU.getEntityName()));

		// do not show column for resource id
		SubForm.Column column = new SubForm.Column("menupath", "Menupath", new CollectableComponentType(CollectableComponentTypes.TYPE_TEXTFIELD, null), false, false, false, 0, 0);
		subform.addColumn(column);

		SubForm.Column column2 = new SubForm.Column("process", clcte.getEntityField("process").getLabel(), 
				new CollectableComponentType(CollectableComponentTypes.TYPE_COMBOBOX, null), true, model.isStateModel(), false, 0, 0);
		column2.setValueListProvider(new ProcessCollectableFieldsProvider());
		subform.addColumn(column2);

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

		Preferences prefs = java.util.prefs.Preferences.userRoot().node("org/nuclos/client/entitywizard/steps/menu");

		subFormController = new MasterDataSubFormController(clcte, tab, provider, ENTITYNAME_MENU, subform, prefs, 
				getEntityPreferences(), null);
		Collection<EntityObjectVO> data = model.getEntityMenus();

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
		// close SubForm support
		if (subFormController != null) {
			subFormController.close();
		}
		subFormController = null;
		if (subform !=  null) {
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
			JOptionPane.showMessageDialog(this, localeDelegate.getMessageFromResource(e1.getMessage()),
	    			localeDelegate.getMessage("wizard.step.menu.error.title", "Achtung!"), 
	    			JOptionPane.OK_OPTION);
 	        throw new InvalidStateException();
		}
		Collection<EntityObjectVO> entityMenus = CollectionUtils.transform(subformdata, new Transformer<CollectableEntityObject, EntityObjectVO>() {
			@Override
			public EntityObjectVO transform(CollectableEntityObject i) {
				return i.getEntityObjectVO();
			}
		});
		model.setEntityMenus(entityMenus);
		
		super.applyState();
	}

	public static class EntityMenuCollectableEntity extends AbstractCollectableEntity {

		public EntityMenuCollectableEntity(CollectableEntity clcte) {
			super(NuclosEntity.ENTITYMENU.getEntityName(), clcte.getLabel());

			for (String field : clcte.getFieldNames()) {
				this.addCollectableEntityField(clcte.getEntityField(field));
			}

			for (LocaleInfo li : LocaleDelegate.getInstance().getAllLocales(false)) {
				String fieldname = "menupath_" + li.getTag();
				String label = getSpringLocaleDelegate().getMessage("EntityMenuCollectableEntity.translationfield.label", "Menu path ({0})", li.title);
				String description = getSpringLocaleDelegate().getMessage("EntityMenuCollectableEntity.translationfield.description", "Menu path ({0}). Use \\ to create submenus.", li.title);
				DefaultCollectableEntityField field = new DefaultCollectableEntityField(fieldname, String.class, label,
						description, null, null, false, CollectableField.TYPE_VALUEFIELD, null, null, NuclosEntity.ENTITYMENU.getEntityName(), null);
				field.setCollectableEntity(this);
				this.addCollectableEntityField(field);
			}
		}
	}

	private class ProcessCollectableFieldsProvider implements CollectableFieldsProvider {

		@Override
		public void setParameter(String sName, Object oValue) { }

		@Override
		public List<CollectableField> getCollectableFields() throws CommonBusinessException {
			if (model.getProcesses() != null) {
				return CollectionUtils.transform(model.getProcesses(), new Transformer<EntityObjectVO, CollectableField>() {
					@Override
					public CollectableField transform(EntityObjectVO i) {
						return new CollectableValueIdField(i.getId(), i.getField("name"));
					}
				});
			}
			else {
				return new ArrayList<CollectableField>();
			}
		}
	}
}
