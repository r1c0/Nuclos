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
package org.nuclos.client.wizard;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.nuclos.client.main.mainframe.MainFrameTab;
import org.nuclos.client.main.mainframe.MainFrameTabbedPane;
import org.nuclos.client.masterdata.CollectableMasterDataWithDependants;
import org.nuclos.client.masterdata.MasterDataCollectController;
import org.nuclos.client.ui.collect.CollectState;
import org.nuclos.client.ui.collect.CollectStateAdapter;
import org.nuclos.client.ui.collect.CollectStateEvent;
import org.nuclos.client.ui.collect.component.CollectableComponent;
import org.nuclos.client.ui.collect.component.model.CollectableComponentModelEvent;
import org.nuclos.client.ui.collect.component.model.CollectableComponentModelListener;
import org.nuclos.client.ui.collect.component.model.DetailsComponentModelEvent;
import org.nuclos.client.ui.collect.component.model.SearchComponentModelEvent;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.collect.collectable.CollectableValueField;
import org.nuclos.common2.SpringLocaleDelegate;
import org.nuclos.common2.StringUtils;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.exception.CommonValidationException;

/**
 * Controller for <code>Wiki</code>
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:corina.mandoki@novabit.de">Corina Mandoki</a>
 * @version 01.00.00
 */

public class DataTypeCollectController extends MasterDataCollectController{

	private List<ChangeListener> lstChangeListener;

	protected class DataTypeCollectStateListener extends CollectStateAdapter {

		@Override
		public void detailsModeEntered(CollectStateEvent ev)
			throws CommonBusinessException {
			if(ev.getNewCollectState().isDetailsModeNew()) {
				for(CollectableComponent comp : getDetailsEditView().getCollectableComponents()) {
					comp.setEnabled(true);
				}
			}
			else if(ev.getNewCollectState().getInnerState() == CollectState.DETAILSMODE_NEW_CHANGED) {
				for(CollectableComponent comp : getDetailsEditView().getCollectableComponentsFor("javatyp")) {
					comp.setEnabled(true);
				}
			}
			else {
				for(CollectableComponent comp : getDetailsEditView().getCollectableComponentsFor("javatyp")) {
					comp.setEnabled(false);
				}
			}
		}
	}

	/**
	 * You should use {@link org.nuclos.client.ui.collect.CollectControllerFactorySingleton}
	 * to get an instance.
	 *
	 * @deprecated You should normally do sth. like this:<code><pre>
	 * ResultController<~> rc = new ResultController<~>();
	 * *CollectController<~> cc = new *CollectController<~>(.., rc);
	 * </code></pre>
	 */
	public DataTypeCollectController(MainFrameTab tabIfAny) {
		super(NuclosEntity.DATATYP, tabIfAny);
		// this.init();
		this.getCollectStateModel().addCollectStateListener(new DataTypeCollectStateListener());
		lstChangeListener = new ArrayList<ChangeListener>();
	}

	public void addChangeListener(ChangeListener listener) {
		lstChangeListener.add(listener);
	}


	@Override
	public void save() throws CommonBusinessException {
		super.save();

		for(ChangeListener cl : lstChangeListener) {
			cl.stateChanged(new ChangeEvent(this.getCompleteSelectedCollectable()));
		}

	}

	@Override
	protected void cmdCloneSelectedCollectable() {
		super.cmdCloneSelectedCollectable();
		resetFields();

	}




	@Override
	protected void validate(CollectableMasterDataWithDependants clct)
		throws CommonBusinessException {
		super.validate(clct);
		String sJavaType = (String)clct.getField("javatyp").getValue();
		if(sJavaType == null) {
			throw new CommonValidationException(
					SpringLocaleDelegate.getInstance().getMessage(
							"DataTypeCollectController.1","Kein Java Datentyp gesetzt"));
		}

		if(sJavaType.equals("java.lang.Double") || sJavaType.equals("java.lang.Integer")) {
			Integer iScale = (Integer)clct.getField("scale").getValue();
			if(iScale == null) {
				throw new CommonValidationException(
						SpringLocaleDelegate.getInstance().getMessage("DataTypeCollectController.2","Keine Vorkommastellen gesetzt"));
			}
		}

		String sOutputFormat = (String)clct.getField("outputformat").getValue();
		try {
			if (!StringUtils.isNullOrEmpty(sOutputFormat)) {
				if (Integer.class.getName().equals(sJavaType) || Double.class.getName().equals(sJavaType)) {
					new DecimalFormat(sOutputFormat);
				}
				else if (Date.class.getName().equals(sJavaType)) {
					new SimpleDateFormat(sOutputFormat);
				}
			}
		}
		catch (IllegalArgumentException ex) {
			throw new CommonBusinessException(StringUtils.getParameterizedExceptionMessage("wizard.step.attributeproperties.validation.outputformat", sOutputFormat, sJavaType));
		}
	}

	/**
	 * TODO: Make this protected.
	 */
	public void init() {
		super.init();
		this.getDetailsEditView().getModel().getCollectableComponentModelFor("javatyp").addCollectableComponentModelListener(new CollectableComponentModelListener() {

			@Override
			public void valueToBeChanged(DetailsComponentModelEvent ev) {}

			@Override
			public void searchConditionChangedInModel(SearchComponentModelEvent ev) {}

			@Override
			public void collectableFieldChangedInModel(CollectableComponentModelEvent ev) {

				resetFields();

				String sValue = (String)ev.getNewValue().getValue();

				if("java.lang.Integer".equals(sValue)) {
					disablePrecision();
					for(CollectableComponent comp : getDetailsEditView().getCollectableComponentsFor("databasetyp")) {
						comp.setField(new CollectableValueField("number"));
					}
				}
				else if("java.lang.Double".equals(sValue)) {
					for(CollectableComponent comp : getDetailsEditView().getCollectableComponentsFor("databasetyp")) {
						comp.setField(new CollectableValueField("number"));
					}
				}
				else if("java.util.Date".equals(sValue)) {
					disablePrecision();
					disableScale();
					for(CollectableComponent comp : getDetailsEditView().getCollectableComponentsFor("databasetyp")) {
						comp.setField(new CollectableValueField("date"));
					}
				}
				else if("java.lang.Boolean".equals(sValue)) {
					disablePrecision();
					disableScale();
					//disableInputFormat();
					disableOutputFormat();

					for(CollectableComponent comp : getDetailsEditView().getCollectableComponentsFor("scale")) {
						comp.setField(new CollectableValueField(new Integer(1)));
					}
					for(CollectableComponent comp : getDetailsEditView().getCollectableComponentsFor("databasetyp")) {
						comp.setField(new CollectableValueField("number"));
					}
				}
				if("java.lang.String".equals(sValue)) {
					disablePrecision();
					for(CollectableComponent comp : getDetailsEditView().getCollectableComponentsFor("databasetyp")) {
						comp.setField(new CollectableValueField("varchar"));
					}
				}
				else {
					for(CollectableComponent comp : getDetailsEditView().getCollectableComponentsFor("databasetyp")) {
						comp.setField(new CollectableValueField("varchar"));
					}
				}
			}
		});

	}

	private void resetFields() {
		for(CollectableComponent comp : getDetailsEditView().getCollectableComponents()) {
			if(!comp.getEntityField().getName().equals("javatyp")) {
				comp.setEnabled(true);
			}
		}
	}

	private void disablePrecision() {
		Iterator<CollectableComponent> it = getDetailsEditView().getCollectableComponentsFor("precision").iterator();
		while(it.hasNext()) {
			CollectableComponent comp = it.next();
			comp.setEnabled(false);
			comp.setField(new CollectableValueField(null));
		}
	}

	private void disableInputFormat() {
		Iterator<CollectableComponent> it = getDetailsEditView().getCollectableComponentsFor("inputformat").iterator();
		while(it.hasNext()) {
			CollectableComponent comp = it.next();
			comp.setEnabled(false);
			comp.setField(new CollectableValueField(null));
		}
	}

	private void disableOutputFormat() {
		Iterator<CollectableComponent> it = getDetailsEditView().getCollectableComponentsFor("outputformat").iterator();
		while(it.hasNext()) {
			CollectableComponent comp = it.next();
			comp.setEnabled(false);
			comp.setField(new CollectableValueField(null));
		}
	}

	private void disableScale() {
		Iterator<CollectableComponent> it = getDetailsEditView().getCollectableComponentsFor("scale").iterator();
		while(it.hasNext()) {
			CollectableComponent comp = it.next();
			comp.setEnabled(false);
			comp.setField(new CollectableValueField(null));
		}
	}


}
