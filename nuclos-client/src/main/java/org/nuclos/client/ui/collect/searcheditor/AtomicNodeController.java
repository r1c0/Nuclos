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
package org.nuclos.client.ui.collect.searcheditor;

import java.awt.CardLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JRootPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;

import org.apache.commons.lang.NullArgumentException;
import org.apache.log4j.Logger;
import org.jdesktop.swingx.renderer.DefaultListRenderer;
import org.nuclos.client.ui.CommonJTextField;
import org.nuclos.client.ui.Controller;
import org.nuclos.client.ui.Errors;
import org.nuclos.client.ui.LineLayout;
import org.nuclos.client.ui.ResourceIdMapper;
import org.nuclos.client.ui.UIUtils;
import org.nuclos.client.ui.ValidatingJOptionPane;
import org.nuclos.client.ui.collect.component.CollectableComboBox;
import org.nuclos.client.ui.collect.component.CollectableComponent;
import org.nuclos.client.ui.collect.component.CollectableComponentFactory;
import org.nuclos.client.ui.collect.component.CollectableTextComponent;
import org.nuclos.client.ui.collect.component.CollectableTextComponentHelper;
import org.nuclos.common.collect.collectable.CollectableEntity;
import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common.collect.collectable.CollectableFieldsProviderFactory;
import org.nuclos.common.collect.collectable.CollectableUtils;
import org.nuclos.common.collect.collectable.searchcondition.AtomicCollectableSearchCondition;
import org.nuclos.common.collect.collectable.searchcondition.CollectableComparison;
import org.nuclos.common.collect.collectable.searchcondition.CollectableComparisonWithOtherField;
import org.nuclos.common.collect.collectable.searchcondition.CollectableComparisonWithParameter;
import org.nuclos.common.collect.collectable.searchcondition.CollectableComparisonWithParameter.ComparisonParameter;
import org.nuclos.common.collect.collectable.searchcondition.CollectableIsNullCondition;
import org.nuclos.common.collect.collectable.searchcondition.CollectableLikeCondition;
import org.nuclos.common.collect.collectable.searchcondition.ComparisonOperator;
import org.nuclos.common.collect.collectable.searchcondition.visit.AtomicVisitor;
import org.nuclos.common.collect.exception.CollectableFieldFormatException;
import org.nuclos.common2.CommonRunnable;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.exception.CommonValidationException;

/**
 * Controller for creating or editing an <code>AtomicSearchConditionTreeNode</code>.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */
public class AtomicNodeController extends Controller {

	private static final Logger log = Logger.getLogger(AtomicNodeController.class);

	private final JTree tree;
	private final CollectableEntity clcte;
	private final CollectableFieldsProviderFactory clctfproviderfactory;
	private final Collection<CollectableEntityField> additionalFields;


	public AtomicNodeController(Component parent, JTree tree, CollectableEntity clcte,
			CollectableFieldsProviderFactory clctfproviderfactory, Collection<CollectableEntityField> additionalFields) {
		super(parent);
		this.tree = tree;
		this.clcte = clcte;
		this.clctfproviderfactory = clctfproviderfactory;
		this.additionalFields = additionalFields;
	}

	/**
	 * lets the user add a new node as a new child of the given parent.
	 * @param nodeParent
	 */
	public void runAdd(SearchConditionTreeNode nodeParent) {
		final AtomicNodePanel pnl = new AtomicNodePanel();
		final int iBtn = this.newOptionPane(getCommonLocaleDelegate().getMessage(
				"AtomicNodeController.1","Einfache Bedingung hinzuf\u00fcgen"), pnl).showDialog();

		if (iBtn == JOptionPane.OK_OPTION) {
			try {
				final AtomicCollectableSearchCondition cond = pnl.getSearchCondition();
				if (cond != null) {
					nodeParent.add(new AtomicSearchConditionTreeNode(cond));
					nodeParent.refresh(tree);
				}
			}
			catch (CommonBusinessException ex) {
				Errors.getInstance().showExceptionDialog(getParent(), ex);
			}
		}
	}

	/**
	 * lets the user edit the given node.
	 * @param node
	 */
	public void runEdit(AtomicSearchConditionTreeNode node) {
		final AtomicNodePanel pnl = new AtomicNodePanel();
		pnl.setSearchCondition(node.getSearchCondition());
		final int iBtn = this.newOptionPane(getCommonLocaleDelegate().getMessage(
				"AtomicNodeController.2","Einfache Bedingung bearbeiten"), pnl).showDialog();

		if (iBtn == JOptionPane.OK_OPTION) {
			try {
				final AtomicCollectableSearchCondition cond = pnl.getSearchCondition();
				final SearchConditionTreeNode nodeParent = (SearchConditionTreeNode) node.getParent();
				final int iChildIndex = nodeParent.getIndex(node);
				nodeParent.remove(iChildIndex);
				if (cond != null) {
					nodeParent.insert(new AtomicSearchConditionTreeNode(cond), iChildIndex);
				}
				nodeParent.refresh(tree);
			}
			catch (CommonBusinessException ex) {
				Errors.getInstance().showExceptionDialog(getParent(), ex);
			}
		}
	}

	private ValidatingJOptionPane newOptionPane(String sTitle, final AtomicNodePanel pnl) {
		return new ValidatingJOptionPane(getParent(), sTitle, pnl, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION) {

			@Override
			protected void validateInput() throws ValidatingJOptionPane.ErrorInfo {
				try {
					pnl.getSearchCondition();
				}
				catch (CommonBusinessException ex) {
					throw new ErrorInfo(ex.getMessage(), null);
				}
			}
		};
	}

	private class AtomicNodePanel extends JPanel {

		private static final int MAX_COLUMNS = 20;

		private final JComboBox cmbbxEntityField = new JComboBox();
		private final JComboBox cmbbxOperator = new JComboBox(ComparisonOperator.getComparisonOperators());

		private final CardLayout cardlayoutOperand = new CardLayout();
		private final JPanel pnlOperand = new JPanel(cardlayoutOperand);

		private final JComboBox cmbbxOtherEntityField = new JComboBox();
		private final JComboBox cmbbxParameter = new JComboBox();
		private CollectableComponent clctcompValue;
		private final CommonJTextField tfLikeComparand = new CommonJTextField(MAX_COLUMNS);

		private final JLabel labCompareWith = new JLabel(getCommonLocaleDelegate().getMessage(
				"AtomicNodeController.3","Vergleich mit"));
		private final JPanel pnlValueOrOtherField = new JPanel(new LineLayout(LineLayout.HORIZONTAL, 5, true));
		private final JRadioButton radiobtnValue = new JRadioButton(getCommonLocaleDelegate().getMessage(
				"AtomicNodeController.4","Wert"));
		private final JRadioButton radiobtnOtherField = new JRadioButton(getCommonLocaleDelegate().getMessage(
				"AtomicNodeController.5","anderem Feld"));
		private final JRadioButton radiobtnParameter = new JRadioButton(getCommonLocaleDelegate().getMessage(
				"AtomicNodeController.9","Parameter"));
		private final ButtonGroup btngrpValueOrOtherField = new ButtonGroup();

		private static final String CARD_VALUE = "value";
		private static final String CARD_LIKE = "like";
		private static final String CARD_OTHER_FIELD = "other field";
		private static final String CARD_PARAMETER = "parameter";

		AtomicNodePanel() {
			super(new GridBagLayout());

			final JPanel pnl = new JPanel(new GridBagLayout());
			this.add(pnl, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.SOUTHWEST, GridBagConstraints.NONE, new Insets(0,0,0,0), 0, 0));

			pnl.add(new JLabel(getCommonLocaleDelegate().getMessage("AtomicNodeController.6","Feld")), 
					new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.SOUTHWEST, GridBagConstraints.NONE, 
					new Insets(0, 0, 2, 5), 0, 0));
			pnl.add(cmbbxEntityField, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 5), 0, 0));

			pnl.add(new JLabel(getCommonLocaleDelegate().getMessage("AtomicNodeController.7","Operator")), 
					new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.SOUTHWEST, GridBagConstraints.NONE, 
					new Insets(0, 0, 2, 5), 0, 0));
			pnl.add(cmbbxOperator, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 5), 0, 0));
			cmbbxOperator.setRenderer(new DefaultListCellRenderer() {

				@Override
				public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
					String label = null;
					if (value != null) {
						label = getCommonLocaleDelegate().getMessage(((ComparisonOperator) value).getResourceIdForLabel(), null);
					}
					return super.getListCellRendererComponent(list, label, index, isSelected, cellHasFocus);
				}
			});

			pnlValueOrOtherField.add(radiobtnValue);
			pnlValueOrOtherField.add(radiobtnOtherField);
			pnlValueOrOtherField.add(radiobtnParameter);
			btngrpValueOrOtherField.add(radiobtnValue);
			btngrpValueOrOtherField.add(radiobtnOtherField);
			btngrpValueOrOtherField.add(radiobtnParameter);

			pnlOperand.add(CARD_VALUE, new JPanel());
			pnlOperand.add(CARD_LIKE, tfLikeComparand);
			pnlOperand.add(CARD_OTHER_FIELD, cmbbxOtherEntityField);
			pnlOperand.add(CARD_PARAMETER, cmbbxParameter);

			pnl.add(labCompareWith, new GridBagConstraints(2, 0, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
			pnl.add(pnlValueOrOtherField, new GridBagConstraints(2, 1, 1, 1, 1.0, 0.0, GridBagConstraints.SOUTHWEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 2, 0), 0, 0));
			pnl.add(pnlOperand, new GridBagConstraints(2, 2, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));

			this.setOperandVisible(false);

			final List<CollectableEntityField> lstclctef = new ArrayList<CollectableEntityField>();
			lstclctef.addAll(CollectableUtils.getCollectableEntityFields(clcte));
			if (AtomicNodeController.this.additionalFields != null) {
				lstclctef.addAll(AtomicNodeController.this.additionalFields);
			}
			Collections.sort(lstclctef, new CollectableEntityField.LabelComparator());
			cmbbxEntityField.setModel(new DefaultComboBoxModel(lstclctef.toArray()));
			cmbbxOtherEntityField.setModel(new DefaultComboBoxModel(lstclctef.toArray()));

			ComparisonParameter[] parameters = ComparisonParameter.values();
			ResourceIdMapper<ComparisonParameter> parameterMapper = new ResourceIdMapper<ComparisonParameter>(parameters);
			Arrays.sort(parameters, parameterMapper);
			cmbbxParameter.setModel(new DefaultComboBoxModel(parameters));
			cmbbxParameter.setRenderer(new DefaultListRenderer(parameterMapper));
			
			this.setupListeners();

			// default: compare with value:
			radiobtnValue.setSelected(true);

			this.reinstallValueComponentIfNecessary();
		}

		private void showCard(String sCard) {
			cardlayoutOperand.show(pnlOperand, sCard);
		}

		private CollectableEntityField getCollectableEntityField() {
			return (CollectableEntityField) cmbbxEntityField.getSelectedItem();
		}

		private ComparisonOperator getComparisonOperator() {
			return (ComparisonOperator) cmbbxOperator.getSelectedItem();
		}

		private void setOperandVisible(boolean bVisible) {
			labCompareWith.setVisible(bVisible);
			pnlValueOrOtherField.setVisible(bVisible);
			pnlOperand.setVisible(bVisible);
		}

		/**
		 * @param atomiccond
		 * @precondition atomiccond != null
		 */
		private void setSearchCondition(AtomicCollectableSearchCondition atomiccond) {
			if (atomiccond == null) {
				throw new NullArgumentException("atomiccond");
			}

			cmbbxEntityField.setSelectedItem(atomiccond.getEntityField());
			final ComparisonOperator compop = atomiccond.getComparisonOperator();
			cmbbxOperator.setSelectedItem(compop);

			// set right operand (if any):
			atomiccond.accept(new AtomicVisitor<Void, RuntimeException>() {
				@Override
				public Void visitComparison(CollectableComparison comparison) throws RuntimeException {
					radiobtnValue.setSelected(true);
					reinstallValueComponentIfNecessary();
					clctcompValue.setField(comparison.getComparand());
					pnlOperand.add(CARD_VALUE, clctcompValue.getControlComponent());
					showCard(CARD_VALUE);
					return null;
				}

				@Override
				public Void visitComparisonWithParameter(CollectableComparisonWithParameter comparisonwp) {
					radiobtnParameter.setSelected(true);
					cmbbxParameter.setSelectedItem(comparisonwp.getParameter());
					showCard(CARD_PARAMETER);
					return null;
				}

				@Override
				public Void visitComparisonWithOtherField(CollectableComparisonWithOtherField comparisonwf) throws RuntimeException {
					radiobtnOtherField.setSelected(true);
					cmbbxOtherEntityField.setSelectedItem(comparisonwf.getOtherField());
					showCard(CARD_OTHER_FIELD);
					return null;
				}

				@Override
				public Void visitLikeCondition(CollectableLikeCondition likecond) throws RuntimeException {
					radiobtnValue.setSelected(true);
					tfLikeComparand.setText(likecond.getLikeComparand());
					showCard(CARD_LIKE);
					return null;
				}

				@Override
				public Void visitIsNullCondition(CollectableIsNullCondition isnullcond) throws RuntimeException {
					// nothing more to do
					return null;
				}
			});

			this.setOperandVisible(compop.getOperandCount() == 2);
		}

		/**
		 * @return the search condition in the panel, if any.
		 * @throws CollectableFieldFormatException
		 * @postcondition (this.getCollectableComparisonOperator() == ComparisonOperator.NONE) <--> (result == null)
		 */
		private AtomicCollectableSearchCondition getSearchCondition() throws CollectableFieldFormatException, CommonValidationException {
			final CollectableEntityField clctef = this.getCollectableEntityField();
			final ComparisonOperator compop = this.getComparisonOperator();

			final AtomicCollectableSearchCondition result;
			if (compop.getOperandCount() == 2 && radiobtnParameter.isSelected()) {
				// special case: comparison with parameter
				final ComparisonParameter parameter = (ComparisonParameter) cmbbxParameter.getSelectedItem();
				if (!parameter.isCompatible(clctef)) {
					throw new CommonValidationException(getCommonLocaleDelegate().getMessage("AtomicNodeController.10", null));
				}
				return new CollectableComparisonWithParameter(clctef, compop, parameter);
			}
			else if (compop.getOperandCount() == 2 && radiobtnOtherField.isSelected()) {
				// special case: comparison with other field
				final CollectableEntityField clctefOtherField = (CollectableEntityField) cmbbxOtherEntityField.getSelectedItem();

				// check that the fields match:
				if(clctef.getJavaClass() != clctefOtherField.getJavaClass()) {
					throw new CommonValidationException(getCommonLocaleDelegate().getMessage("AtomicNodeController.8","Die Datentypen der Felder stimmen nicht \u00fcberein."));
				}
				return new CollectableComparisonWithOtherField(clctef, compop, clctefOtherField);
			}
			else {
				result = CollectableTextComponentHelper.getAtomicSearchConditionFromView(clctef, compop,
						clctcompValue, tfLikeComparand.getText());
			}
			return result;
		}

		private void setupListeners() {
			cmbbxEntityField.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent ev) {
					if (ev.getStateChange() == ItemEvent.SELECTED) {
						UIUtils.runCommand(AtomicNodePanel.this, new CommonRunnable() {
							@Override
							public void run() {
								// is second operand visible:
								if (getComparisonOperator().getOperandCount() == 2) {
									// compare with value (or like):
									if (radiobtnValue.isSelected()) {
										if (CollectableLikeCondition.isValidOperator((ComparisonOperator) cmbbxOperator.getSelectedItem())) {
											tfLikeComparand.setText("");
											showCard(CARD_LIKE);
										}
										else {
											reinstallValueComponentIfNecessary();

											showCard(CARD_VALUE);
										}

									}
								}
							}
						});
					}
				}
			});

			cmbbxOperator.addItemListener(new ItemListener() {
				ComparisonOperator compopOld = ComparisonOperator.NONE;

				@Override
				public void itemStateChanged(final ItemEvent ev) {
					if (ev.getStateChange() == ItemEvent.SELECTED) {
						UIUtils.runCommand(AtomicNodePanel.this, new CommonRunnable() {
							@Override
							public void run() throws CollectableFieldFormatException {
								final ComparisonOperator compop = (ComparisonOperator) ev.getItem();
								setOperandVisible(compop.getOperandCount() == 2);
								if (compop.getOperandCount() == 2 && radiobtnValue.isSelected()) {
									if (CollectableLikeCondition.isValidOperator(compop))
									{
										// "value" --> "like"
										final String sOldValue = clctcompValue.getField().toString();

										tfLikeComparand.setText(sOldValue);
										showCard(CARD_LIKE);
									}
									else if (CollectableComparison.isValidOperator(compop)) {
										reinstallValueComponentIfNecessary();

										if (CollectableLikeCondition.isValidOperator(compopOld)) {
											// "like" --> "value"
											final String sOldValue = tfLikeComparand.getText();

											if (clctcompValue instanceof CollectableTextComponent) {
												try {
													clctcompValue.setField(CollectableTextComponentHelper.write(sOldValue, getCollectableEntityField()));
												}
												catch (CollectableFieldFormatException ex) {
													log.info("Vergleichswert konnte nicht \u00fcbertragen werden: " + sOldValue);
												}
											}
										}
										showCard(CARD_VALUE);
									}
								}
								adjustLayout();

								// remember last operator:
								compopOld = compop;
							}
						});
					}
				}
			});

			final ItemListener itemlistenerRadio = new ItemListener() {
				@Override
				public void itemStateChanged(final ItemEvent ev) {
					if (ev.getStateChange() == ItemEvent.SELECTED) {
						UIUtils.runCommand(AtomicNodePanel.this, new CommonRunnable() {
							@Override
							public void run() {
								if (ev.getSource() == radiobtnValue) {
									if (CollectableLikeCondition.isValidOperator(getComparisonOperator())) {
										showCard(CARD_LIKE);
									}
									else {
										reinstallValueComponentIfNecessary();
										showCard(CARD_VALUE);
									}
								}
								else if (ev.getSource() == radiobtnParameter) {
									showCard(CARD_PARAMETER);
								}
								else {
									assert ev.getSource() == radiobtnOtherField;
									showCard(CARD_OTHER_FIELD);
								}
							}
						});
					}
				}
			};
			radiobtnValue.addItemListener(itemlistenerRadio);
			radiobtnOtherField.addItemListener(itemlistenerRadio);
			radiobtnParameter.addItemListener(itemlistenerRadio);
		}

		private void reinstallValueComponentIfNecessary() {
			final CollectableEntityField clctef = getCollectableEntityField();
			// get component for new value:
			final CollectableComponent newclctcomp = CollectableComponentFactory.getInstance().newCollectableComponent(clctef, null, false);

			// if a new component is necessary
			if (clctcompValue == null || clctcompValue.getEntityField().getJavaClass() != clctef.getJavaClass()
					|| clctcompValue.getControlComponent() != newclctcomp.getControlComponent()) {

				clctcompValue = newclctcomp;

				if (clctcompValue instanceof CollectableComboBox) {
					final CollectableComboBox clctcmbbxValue = (CollectableComboBox) clctcompValue;
					clctcmbbxValue.setValueListProvider(clctfproviderfactory.newDefaultCollectableFieldsProvider(clcte.getName(), clctef.getName()));
					clctcmbbxValue.refreshValueList(false);

					clctcmbbxValue.getJComboBox().setSelectedIndex(0);
				}

				pnlOperand.add(CARD_VALUE, clctcompValue.getControlComponent());
			}
		}

		private void adjustLayout() {
			this.invalidate();
			final JRootPane rootpane = SwingUtilities.getRootPane(this);
			if (rootpane != null && rootpane.getParent() != null) {
				UIUtils.ensureMinimumSize(rootpane.getParent());
				rootpane.validate();
			}
			else {
				AtomicNodePanel.this.validate();
			}
		}

	}	// inner class AtomicNodePanel

}	// class AtomicNodeController
