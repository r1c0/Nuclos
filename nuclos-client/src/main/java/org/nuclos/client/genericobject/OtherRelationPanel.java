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
package org.nuclos.client.genericobject;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.nuclos.client.common.NuclosCollectableComboBox;
import org.nuclos.client.genericobject.RelateGenericObjectsPanel.SwappableRelationPanel;
import org.nuclos.client.genericobject.datatransfer.GenericObjectIdModuleProcess;
import org.nuclos.client.masterdata.MasterDataDelegate;
import org.nuclos.client.ui.DateChooser;
import org.nuclos.client.ui.ValidatingJOptionPane.ErrorInfo;
import org.nuclos.client.ui.collect.component.CollectableComboBox;
import org.nuclos.client.ui.collect.component.model.CollectableComponentModelAdapter;
import org.nuclos.client.ui.collect.component.model.CollectableComponentModelEvent;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.SearchConditionUtils;
import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common.collect.collectable.CollectableField;
import org.nuclos.common.collect.collectable.DefaultCollectableEntityField;
import org.nuclos.common.collect.collectable.searchcondition.CollectableComparison;
import org.nuclos.common.collect.collectable.searchcondition.ComparisonOperator;
import org.nuclos.common.collect.exception.CollectableFieldFormatException;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.masterdata.MakeMasterDataLocalizedValueIdField;
import org.nuclos.common2.CommonLocaleDelegate;
import org.nuclos.common2.LangUtils;
import org.nuclos.common2.StringUtils;

/**
 * Panel for editing a (directed or undirected) coupling.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */
class OtherRelationPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	final SwappableRelationPanel relationpanel;
	final CenterPanel pnlCenter;
	final CollectableComboBox clctcmbbxRelationType;
	final DateChooser datechooserValidFrom = new DateChooser();
	final DateChooser datechooserValidUntil = new DateChooser();
	final JTextArea taDescription = new JTextArea(3, 20);

	final Collection<GenericObjectIdModuleProcess> collgoimpSource;
	final GenericObjectIdModuleProcess goimpTarget;

	private static CollectableComboBox newComboBox() {
		final CollectableEntityField clctef = new DefaultCollectableEntityField(NuclosEntity.RELATIONTYPE.getEntityName(), String.class, CommonLocaleDelegate.getMessage("OtherRelationPanel.1", "Beziehungsart"),
			CommonLocaleDelegate.getMessage("OtherRelationPanel.1", "Beziehungsart"), null, null, false, CollectableEntityField.TYPE_VALUEIDFIELD, null, null);
		final CollectableComboBox result = new NuclosCollectableComboBox(clctef, false);
		// restrict value list to user-defined relation types:
		final CollectableComparison cond = SearchConditionUtils.newComparison(NuclosEntity.RELATIONTYPE.getEntityName(), "system", ComparisonOperator.EQUAL, false);
		result.setComboBoxModel(CollectionUtils.transform(MasterDataDelegate.getInstance().getMasterData(NuclosEntity.RELATIONTYPE.getEntityName(), cond), 
			new MakeMasterDataLocalizedValueIdField("name", "labelres")));
		return result;
	}

	/**
	 * @param collgoimpSource
	 * @param goimpTarget
	 * @precondition goimpTarget != null
	 */
	OtherRelationPanel(Collection<GenericObjectIdModuleProcess> collgoimpSource, GenericObjectIdModuleProcess goimpTarget) {
		super(new BorderLayout(0, 10));

		this.collgoimpSource = collgoimpSource;
		this.goimpTarget = goimpTarget;

		this.clctcmbbxRelationType = newComboBox();
		this.relationpanel = new SwappableRelationPanel(CommonLocaleDelegate.getMessage("OtherRelationPanel.2", "Quelle"), CommonLocaleDelegate.getMessage("OtherRelationPanel.3", "Ziel"));
		this.relationpanel.setup(collgoimpSource, goimpTarget);
		this.pnlCenter = new CenterPanel();

		this.init();
	}

	private void init() {
		final JPanel pnlNorth = new JPanel(new BorderLayout(5, 0));
		pnlNorth.add(new JLabel(CommonLocaleDelegate.getMessage("OtherRelationPanel.1", "Beziehungsart")), BorderLayout.WEST);
		pnlNorth.add(this.clctcmbbxRelationType.getJComboBox(), BorderLayout.CENTER);
		this.add(pnlNorth, BorderLayout.NORTH);

		this.add(this.pnlCenter, BorderLayout.CENTER);

		final JPanel pnlSouth = new JPanel(new GridBagLayout());
		this.add(pnlSouth, BorderLayout.SOUTH);

		final GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets.left = 5;
		gbc.insets.bottom = 5;

		pnlSouth.add(new JLabel(CommonLocaleDelegate.getMessage("OtherRelationPanel.4", "G\u00fcltig von")), gbc);
		pnlSouth.add(this.datechooserValidFrom, gbc);
		gbc.insets.left = 10;
		pnlSouth.add(new JLabel(CommonLocaleDelegate.getMessage("OtherRelationPanel.5", "bis")), gbc);
		gbc.insets.left = 5;
		pnlSouth.add(this.datechooserValidUntil, gbc);

		gbc.insets.top = 5;
		gbc.gridx = 0;
		pnlSouth.add(new JLabel(CommonLocaleDelegate.getMessage("OtherRelationPanel.6", "Bemerkung")), gbc);

		gbc.insets.top = 0;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		pnlSouth.add(new JScrollPane(this.taDescription), gbc);

		this.clctcmbbxRelationType.getModel().addCollectableComponentModelListener(new CollectableComponentModelAdapter() {
			@Override
			public void collectableFieldChangedInModel(CollectableComponentModelEvent ev) {
				pnlCenter.cardlayout.show(pnlCenter, StringUtils.emptyIfNull(LangUtils.toString(true)));
			}

		});
	}

	private class CenterPanel extends JPanel {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		final CardLayout cardlayout = new CardLayout();

		CenterPanel() {
			this.setLayout(this.cardlayout);
			this.add("", newEmptyPanel());
			this.add("true", OtherRelationPanel.this.relationpanel);
		}

		private JPanel newEmptyPanel() {
			final JPanel result = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 10));
			result.add(new JLabel(CommonLocaleDelegate.getMessage("OtherRelationPanel.7", "Bitte w\u00e4hlen Sie eine Beziehungsart aus.")));
			return result;
		}
	}

	/**
	 * @return all (source and target, if any) GOIMPs
	 */
	Collection<GenericObjectIdModuleProcess> getAllGoimps() {
		final Collection<GenericObjectIdModuleProcess> result = new ArrayList<GenericObjectIdModuleProcess>(this.collgoimpSource);
		if (this.goimpTarget != null) {
			result.add(this.goimpTarget);
		}
		return result;
	}

	void validateInput() throws ErrorInfo {
		final CollectableField clctf;
		try {
			clctf = this.clctcmbbxRelationType.getField();
		}
		catch (CollectableFieldFormatException ex) {
			// this must not happen as the combobox is not editable.
			throw new NuclosFatalException(ex);
		}
		if (clctf.isNull()) {
			final ErrorInfo ex = new ErrorInfo();
			ex.setErrorMessage(CommonLocaleDelegate.getMessage("OtherRelationPanel.7", "Bitte w\u00e4hlen Sie eine Beziehungsart aus."));
			ex.setComponent(this.clctcmbbxRelationType.getJComboBox());
			throw ex;
		}
	}

}	// class OtherRelationPanel
