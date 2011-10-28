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
package org.nuclos.client.processmonitor;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;
import info.clearthought.layout.TableLayoutConstraints;

import java.awt.BorderLayout;

import javax.swing.JPanel;

import org.nuclos.client.common.NuclosCollectableComboBox;
import org.nuclos.client.masterdata.MasterDataDelegate;
import org.nuclos.client.ui.collect.CollectableComponentsProvider;
import org.nuclos.client.ui.collect.DefaultCollectableComponentsProvider;
import org.nuclos.client.ui.collect.component.CollectableComboBox;
import org.nuclos.client.ui.collect.component.CollectableTextField;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.masterdata.MakeMasterDataValueIdField;

/**
 * Header for the edit panels.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:maik.stueker@novabit.de">Maik Stueker</a>
 * @version 01.00.00
 */
public class InstanceViewHeaderPanel extends JPanel {

	private final JPanel pnlTextFields = new JPanel();

	private final CollectableTextField clcttfName = new CollectableTextField(
			CollectableInstanceModel.clcte.getEntityField(CollectableInstanceModel.FIELDNAME_NAME));

	private final CollectableComboBox clctcomboProcessmodel = newProcessmodelCombobox();

	private final CollectableTextField clcttfPlanstart = new CollectableTextField(
			CollectableInstanceModel.clcte.getEntityField(CollectableInstanceModel.FIELDNAME_PLANSTART));

	private final CollectableTextField clcttfPlanend = new CollectableTextField(
			CollectableInstanceModel.clcte.getEntityField(CollectableInstanceModel.FIELDNAME_PLANEND));

	private final CollectableTextField clcttfRealstart = new CollectableTextField(
			CollectableInstanceModel.clcte.getEntityField(CollectableInstanceModel.FIELDNAME_REALSTART));

	private final CollectableTextField clcttfRealend = new CollectableTextField(
			CollectableInstanceModel.clcte.getEntityField(CollectableInstanceModel.FIELDNAME_REALEND));

	public InstanceViewHeaderPanel() {
		super(new BorderLayout());
		this.add(pnlTextFields, BorderLayout.CENTER);

		final double[] columns = new double[]{
				4.0, TableLayout.PREFERRED, 4.0, TableLayout.PREFERRED, 10.0, TableLayout.PREFERRED, 4.0, TableLayout.PREFERRED, 10.0, TableLayout.PREFERRED, 4.0, TableLayout.PREFERRED, 4.0};
		final double[] rows = new double[]{
				2.0,
				20.0,
				2.0,
				20.0,
				2.0};

		pnlTextFields.setLayout(new TableLayout(columns, rows));

		clcttfName.setLabelText(CollectableInstanceModel.clcte.getEntityField(CollectableInstanceModel.FIELDNAME_NAME).getLabel());
		clcttfName.setToolTipText(CollectableInstanceModel.clcte.getEntityField(CollectableInstanceModel.FIELDNAME_NAME).getDescription());
		clcttfName.setColumns(20);
		pnlTextFields.add(this.clcttfName.getJLabel(), getConstraints(1, 1, 1, 1));
		pnlTextFields.add(this.clcttfName.getJTextField(), getConstraints(3, 3, 1, 1));

		clctcomboProcessmodel.setLabelText(CollectableInstanceModel.clcte.getEntityField(CollectableInstanceModel.FIELDNAME_PROCESSMODEL).getLabel());
		clctcomboProcessmodel.setToolTipText(CollectableInstanceModel.clcte.getEntityField(CollectableInstanceModel.FIELDNAME_PROCESSMODEL).getDescription());
		pnlTextFields.add(this.clctcomboProcessmodel.getJLabel(), getConstraints(1, 1, 3, 3));
		pnlTextFields.add(this.clctcomboProcessmodel.getJComboBox(), getConstraints(3, 3, 3, 3));

		clcttfPlanstart.setLabelText(CollectableInstanceModel.clcte.getEntityField(CollectableInstanceModel.FIELDNAME_PLANSTART).getLabel());
		clcttfPlanstart.setToolTipText(CollectableInstanceModel.clcte.getEntityField(CollectableInstanceModel.FIELDNAME_PLANSTART).getDescription());
		clcttfPlanstart.setColumns(11);
		pnlTextFields.add(this.clcttfPlanstart.getJLabel(), getConstraints(5, 5, 1, 1));
		pnlTextFields.add(this.clcttfPlanstart.getJTextField(), getConstraints(7, 7, 1, 1));

		clcttfPlanend.setLabelText(CollectableInstanceModel.clcte.getEntityField(CollectableInstanceModel.FIELDNAME_PLANEND).getLabel());
		clcttfPlanend.setToolTipText(CollectableInstanceModel.clcte.getEntityField(CollectableInstanceModel.FIELDNAME_PLANEND).getDescription());
		clcttfPlanend.setColumns(11);
		pnlTextFields.add(this.clcttfPlanend.getJLabel(), getConstraints(5, 5, 3, 3));
		pnlTextFields.add(this.clcttfPlanend.getJTextField(), getConstraints(7, 7, 3, 3));

		clcttfRealstart.setLabelText(CollectableInstanceModel.clcte.getEntityField(CollectableInstanceModel.FIELDNAME_REALSTART).getLabel());
		clcttfRealstart.setToolTipText(CollectableInstanceModel.clcte.getEntityField(CollectableInstanceModel.FIELDNAME_REALSTART).getDescription());
		clcttfRealstart.setColumns(11);
		pnlTextFields.add(this.clcttfRealstart.getJLabel(), getConstraints(9, 9, 1, 1));
		pnlTextFields.add(this.clcttfRealstart.getJTextField(), getConstraints(11, 11, 1, 1));

		clcttfRealend.setLabelText(CollectableInstanceModel.clcte.getEntityField(CollectableInstanceModel.FIELDNAME_REALEND).getLabel());
		clcttfRealend.setToolTipText(CollectableInstanceModel.clcte.getEntityField(CollectableInstanceModel.FIELDNAME_REALEND).getDescription());
		clcttfRealend.setColumns(11);
		pnlTextFields.add(this.clcttfRealend.getJLabel(), getConstraints(9, 9, 3, 3));
		pnlTextFields.add(this.clcttfRealend.getJTextField(), getConstraints(11, 11, 3, 3));
	}

	public CollectableComponentsProvider newCollectableComponentsProvider() {
		return new DefaultCollectableComponentsProvider(clcttfName, clctcomboProcessmodel, clcttfPlanstart, clcttfPlanend, clcttfRealstart, clcttfRealend);
	}

	private static CollectableComboBox newProcessmodelCombobox() {
		final CollectableComboBox result = new NuclosCollectableComboBox(CollectableInstanceModel.clcte.getEntityField(CollectableInstanceModel.FIELDNAME_PROCESSMODEL), false);
//		// restrict value list to user-defined relation types:
//		final CollectableComparison cond = SearchConditionUtils.newComparison("relationtype", "system", ComparisonOperator.EQUAL, false);
		String referencedentity = CollectableInstanceModel.clcte.getEntityField(CollectableInstanceModel.FIELDNAME_PROCESSMODEL).getReferencedEntityName();
		result.setComboBoxModel(CollectionUtils.transform(MasterDataDelegate.getInstance().getMasterData(referencedentity), new MakeMasterDataValueIdField(referencedentity)));
		return result;
	}

	/**
	 * hAlign = LEFT
	 * vAlign = CENTER
	 *
	 * @param col1
	 * @param col2
	 * @param row1
	 * @param row2
	 * @return
	 */
	private TableLayoutConstraints getConstraints(int col1, int col2, int row1, int row2){
		return this.getConstraints(col1, col2, row1, row2, TableLayoutConstants.LEFT, TableLayoutConstants.CENTER);
	}

	private TableLayoutConstraints getConstraints(int col1, int col2, int row1, int row2, int hAlign, int vAlign){
		TableLayoutConstraints constraints = new TableLayoutConstraints();
		constraints.col1 = col1;
		constraints.col2 = col2;
		constraints.row1 = row1;
		constraints.row2 = row2;
		constraints.hAlign = hAlign;
		constraints.vAlign = vAlign;

		return constraints;
	}

}	// class StateModelHeaderPanel
