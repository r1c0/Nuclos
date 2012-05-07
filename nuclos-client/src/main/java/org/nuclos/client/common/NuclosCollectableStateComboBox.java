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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.swing.JList;
import javax.swing.plaf.basic.BasicComboBoxRenderer;

import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;
import org.nuclos.client.attribute.AttributeCache;
import org.nuclos.client.genericobject.Modules;
import org.nuclos.client.genericobject.valuelistprovider.StateCollectableFieldsProvider;
import org.nuclos.client.ui.collect.component.CollectableComboBox;
import org.nuclos.common.NuclosEOField;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common.collect.collectable.CollectableField;
import org.nuclos.common.collect.collectable.CollectableValueField;
import org.nuclos.common.collect.collectable.CollectableValueIdField;
import org.nuclos.common.collect.collectable.DefaultCollectableEntityField;
import org.nuclos.common.collect.collectable.searchcondition.CollectableComparison;
import org.nuclos.common.collect.collectable.searchcondition.CollectableLikeCondition;
import org.nuclos.common.collect.collectable.searchcondition.CollectableSearchCondition;
import org.nuclos.common.collect.collectable.searchcondition.ComparisonOperator;
import org.nuclos.common.collect.exception.CollectableFieldFormatException;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common2.SpringLocaleDelegate;
import org.nuclos.server.statemodel.valueobject.StateVO;

/**
 * @todo enter class description.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 * @author	<a href="mailto:uwe.allner@novabit.de">uwe.allner</a>
 * @version 01.00.00
 */
public class NuclosCollectableStateComboBox extends CollectableComboBox {


	private StateCollectableFieldsProvider valueListProvider;

	/** @todo use MultiListMap */
	private Map<Object, List<String>> mpToolTips;

	private int columnWidth = 0;
	private Integer iColumns;

	public NuclosCollectableStateComboBox(CollectableEntityField clctef, Boolean bSearchable) {
		super(clctef, bSearchable);

		getJComboBox().setEditable(true);
		getJComboBox().setRenderer(new MyComboBoxRenderer());
		valueListProvider = new StateCollectableFieldsProvider();
		AutoCompleteDecorator.decorate(getJComboBox()); 
	}

	private CollectableEntityField getStatus() {
		return new DefaultCollectableEntityField(NuclosEOField.STATE.getMetaData().getField(), String.class,
				SpringLocaleDelegate.getInstance().getLabelFromAttributeCVO(
						AttributeCache.getInstance().getAttribute(NuclosEOField.STATE.getMetaData().getId().intValue())),
				null, null, null, true, CollectableField.TYPE_VALUEIDFIELD, null, null, getEntityField().getEntityName(), null);
	}

	private CollectableEntityField getStatusNumeral() {
		return new DefaultCollectableEntityField(NuclosEOField.STATENUMBER.getMetaData().getField(), Integer.class,
				SpringLocaleDelegate.getInstance().getLabelFromAttributeCVO(
						AttributeCache.getInstance().getAttribute(NuclosEOField.STATENUMBER.getMetaData().getId().intValue())),
				null, null, null, true, CollectableField.TYPE_VALUEIDFIELD, null, null, getEntityField().getEntityName(), null);
	}

	@Override
	public void setProperty(String sName, Object oValue) {
		super.setProperty(sName, oValue);
		if ("entity".equals(sName)) {
			final Integer iModuleId = Modules.getInstance().getModuleIdByEntityName((String) oValue);
			if (isSearchComponent()) {
				valueListProvider.setParameter("module", iModuleId);
				setComboBoxModel(valueListProvider.getCollectableFields(), true);
			}
		} else if ("module".equals(sName)){
			if (isSearchComponent()) {
				valueListProvider.setParameter("module", oValue);
				setComboBoxModel(valueListProvider.getCollectableFields(), true);
			}
		} else if ("usagecriteria".equals(sName)){
			if (isSearchComponent()) {
				valueListProvider.setParameter("usagecriteria", oValue);
				setComboBoxModel(valueListProvider.getCollectableFields(), true);
			}
		}
	}

	/**
	 * uses a copy of the given Collection as model for this combobox. The selected value is not changed,
	 * thus no CollectableFieldEvents are fired.
	 * @param collEnumeratedFields Collection<CollectableField>
	 * @param bSort Sort the fields before adding them to the model?
	 */
	@Override
	public void setComboBoxModel(Collection<? extends CollectableField> collEnumeratedFields, boolean bSort) {
		final List<CollectableField> lstEntries = prepareModel(collEnumeratedFields);

		super.setComboBoxModel(lstEntries, bSort);
	}

	private List<CollectableField> prepareModel(Collection<? extends CollectableField> collEnumeratedFields) {
		final List<CollectableField> result = new ArrayList<CollectableField>(collEnumeratedFields.size());
		mpToolTips = CollectionUtils.newHashMap();

		for (CollectableField clctfIn : collEnumeratedFields) {
			final StateVO statevo = (StateVO) clctfIn.getValue();
			final Object oKey;
			final String sValue;
			final CollectableField clctfOut;
			if (getEntityField().getName().equals(NuclosEOField.STATE.getMetaData().getField())) {
				oKey = statevo.getStatename();
				sValue = statevo.getNumeral().toString();
				clctfOut = new CollectableValueField(oKey);
			}
			else if (getEntityField().getName().equals(NuclosEOField.STATENUMBER.getMetaData().getField())) {
				oKey = statevo.getNumeral();
				sValue = statevo.getStatename();
				clctfOut = new CollectableValueField(oKey);
			}
			else if (getEntityField().getName().equals("[status_num_plus_name]")) {
				oKey = statevo.getNumeral() + " " + statevo.getStatename();
				sValue = (String) oKey;
				clctfOut = new CollectableValueField(oKey);
			}
			else {
				throw new NuclosFatalException("NuclosCollectableStateComboBox for not supported entity \"" + getEntityField().getName() + " \" used.");
				//"NuclosCollectableStateComboBox f\u00fcr nicht unterst\u00fctzte Entit\u00e4t \"" + getEntityField().getName() + " \" verwendet.");
			}

			final List<String> lstValues;
			if (mpToolTips.containsKey(oKey)) {
				lstValues = mpToolTips.get(oKey);
			}
			else {
				lstValues = new ArrayList<String>();
				mpToolTips.put(oKey, lstValues);
				result.add(clctfOut);
			}

			if (!lstValues.contains(sValue)) {
				lstValues.add(sValue);
			}
		}

		return result;
	}

	@Override
	public void setColumns(int iColumns) {
		this.iColumns = iColumns;
		final Dimension dimSize = getJComboBox().getPreferredSize();
		if (iColumns != 0) {
			final Insets insets = getJComboBox().getInsets();
			// This is as BasicComboBoxUI implements it at this time
			// There seems to be no method to get this value regardless to UI; if you find one, feel free...
			final int comboButtonWidth = dimSize.height - insets.top - insets.bottom;
			final int textWidth = iColumns * getColumnWidth() +
			insets.left + insets.right + comboButtonWidth;
			dimSize.width = Math.min(textWidth, getJComboBox().getMinimumSize().width);
		}
		getJComboBox().setPreferredSize(dimSize);
		getJComboBox().setMinimumSize(dimSize);
	}

	@Override
	public Integer getColumns() {
		return iColumns;
	}

	private int getColumnWidth() {
		if (columnWidth == 0) {
			columnWidth = getJComboBox().getFontMetrics(getJComboBox().getFont()).charWidth('m');
		}
		return columnWidth;
	}

	@Override
	protected void setRenderer() {
		getJComboBox().setRenderer(new MyComboBoxRenderer());
	}

	class MyComboBoxRenderer extends BasicComboBoxRenderer {

		@Override
		public Component getListCellRendererComponent(JList list, Object oValue, int index, boolean bSelected, boolean bCellHasFocus) {
			if (bSelected) {
				setBackground(list.getSelectionBackground());
				setForeground(list.getSelectionForeground());

				final StringBuilder sbToolTip = new StringBuilder("");
				final Object oKey = ((CollectableField) oValue).getValue();

				final List<String> lstValues = mpToolTips.get(oKey);
				if (lstValues != null) {
					for (String sValue : lstValues) {
						sbToolTip.append(sValue);
						sbToolTip.append(",");
					}
				}
				// Remove last comma
				final String sToolTipText;
				if (sbToolTip.length() > 0) {
					sToolTipText = sbToolTip.substring(0, sbToolTip.length() - 1);
				}
				else {
					sToolTipText = "";
				}
				list.setToolTipText(sToolTipText);
			}
			else {
				setBackground(list.getBackground());
				setForeground(list.getForeground());
			}
			setFont(list.getFont());
			setText((oValue == null) ? "" : oValue.toString());

			return this;
		}
	}

	@Override
	protected CollectableSearchCondition getSearchConditionFromView()
	throws CollectableFieldFormatException {
		if (getEntityField().getName().equals("[status_num_plus_name]")) {
			CollectableSearchCondition collectableSearchCondition = super.getSearchConditionFromView();

			if (collectableSearchCondition instanceof CollectableComparison) {
				CollectableComparison collectableComparison = (CollectableComparison) collectableSearchCondition;
				StateSearchConditionVO stateSearch = getStateSearchCondition(collectableComparison.getComparisonOperator(), 
					collectableComparison.getComparand()==null?"":collectableComparison.getComparand().toString());
				return new CollectableComparison(stateSearch.entityFieldDestination, 
					collectableComparison.getComparisonOperator(),
					new CollectableValueIdField(stateSearch.valueIdFieldComparand, stateSearch.valueFieldComparand));
			} else if (collectableSearchCondition instanceof CollectableLikeCondition) {
				CollectableLikeCondition collectableLikeCondition = (CollectableLikeCondition) collectableSearchCondition;
				StateSearchConditionVO stateSearch = getStateSearchCondition(collectableLikeCondition.getComparisonOperator(), 
					collectableLikeCondition.getLikeComparand()==null?"":collectableLikeCondition.getLikeComparand());
				return new CollectableLikeCondition(stateSearch.entityFieldDestination, 
					collectableLikeCondition.getComparisonOperator(), 
					(String) stateSearch.valueFieldComparand);
			}
			return null;
		} else {
			return super.getSearchConditionFromView();
		}
	}

	private class StateSearchConditionVO{
		public final CollectableEntityField entityFieldDestination;
		public final Object valueIdFieldComparand;
		public final Object valueFieldComparand;

		public StateSearchConditionVO(CollectableEntityField entityFieldDestination, Object id, Object value) {
			this.entityFieldDestination = entityFieldDestination;
			valueIdFieldComparand = id;
			valueFieldComparand = value;
		}
	}

	private StateSearchConditionVO getStateSearchCondition(ComparisonOperator compop, String sComparand){
		if (sComparand.matches("[0-9*%]+")) {
			return new StateSearchConditionVO(getStatusNumeral(), null, sComparand);
		} else {
			for (int i = 0; i < getJComboBox().getModel().getSize(); i++) {
				try {
					CollectableValueField cvif = (CollectableValueField) getJComboBox().getModel().getSelectedItem();
					if (getJComboBox().getModel().getElementAt(i) != null &&
						sComparand.equals(cvif.getValue())) {
						// Es wurde ein Eintrag aus der Liste gew\u00e4hlt

						if (compop.equals(ComparisonOperator.EQUAL) ||
							compop.equals(ComparisonOperator.NOT_EQUAL) ||
							compop.equals(ComparisonOperator.LIKE) ||
							compop.equals(ComparisonOperator.NOT_LIKE)) {
							// Suche \u00fcber Name
							final String sStatusName = sComparand.substring(sComparand.indexOf(" ") + 1);
							return new StateSearchConditionVO(getStatus(), null, sStatusName);
						} else {
							// Suche \u00fcber Numeral
							final String sStatusNumeral = sComparand.substring(0, sComparand.indexOf(" "));
							return new StateSearchConditionVO(getStatusNumeral(), null, sStatusNumeral);
						}

					}
				} catch (ClassCastException cce) {
					//first NULL Entry is by Default a ValueIdField... ignore
				}
			}
			return new StateSearchConditionVO(getStatus(), null, sComparand);
		}
	}

	@Override
	protected void updateView(CollectableField clctfValue) {
		if (getEntityField().getName().equals("[status_num_plus_name]")) {
			if (clctfValue == null || clctfValue.getValue() == null) {
				getJComboBox().setSelectedIndex(-1);
				return;
			}

			final String sValue = clctfValue.toString();
			final boolean isNumeral = sValue.matches("[0-9]+");
			String sStatus;
			String sStatusName;
			String sStatusNumeral;

			for (int i = 0; i < getJComboBox().getModel().getSize(); i++) {
				if (getJComboBox().getModel().getElementAt(i) != null){
					sStatus = (String) ((CollectableField) getJComboBox().getModel().getElementAt(i)).getValue();
					if (sStatus != null){
						sStatusName = sStatus.substring(sStatus.indexOf(" ") + 1);
						sStatusNumeral = sStatus.substring(0, sStatus.indexOf(" "));

						if (isNumeral) {
							if (sValue.equals(sStatusNumeral)){
								getJComboBox().getModel().setSelectedItem(getJComboBox().getModel().getElementAt(i));
								return;
							}
						} else {
							if (sValue.equals(sStatusName)){
								getJComboBox().getModel().setSelectedItem(getJComboBox().getModel().getElementAt(i));
								return;
							}
						}
					}
				}
			}
			getJComboBox().setSelectedItem(sValue);
		} else {
			super.updateView(clctfValue);
		}
	}
}	// class NuclosCollectableStateComboBox
