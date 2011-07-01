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

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.nuclos.common2.CommonLocaleDelegate;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.exception.CommonPermissionException;
import org.nuclos.client.datasource.DatasourceDelegate;
import org.nuclos.client.ui.Errors;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.collect.collectable.searchcondition.CollectableSearchCondition;
import org.nuclos.common.collect.collectable.searchcondition.PlainSubCondition;
import org.nuclos.common.querybuilder.DatasourceXMLTransformer;
import org.nuclos.common.querybuilder.NuclosDatasourceException;
import org.nuclos.common.valueobject.DatasourceVOValidator;
import org.nuclos.server.report.valueobject.DatasourceVO;

/**
 * Opens dialog for choosing a datasource, returns a CollectableSearchCondition (PlainSubCondition) by calling
 * getSearchCondition()
 *
 * Created by Novabit Informationssysteme GmbH <br>
 * Please visit <a href="http://www.novabit.de">www.novabit.de </a>
 *
 * @author <a href="mailto:rostislav.maksymovskyi@novabit.de">Rostislav.Maksymovskyi </a>
 * @version 01.00.00
 */
public class DataSourceSearchImporter implements ExtSourceSearchImporter {

	private JComponent parentComponent = null;
	private DatasourceVO datasource = null;
	private static final String intidAlias = "ds_intid";

	public DataSourceSearchImporter(Integer iModuleId, JComponent parentComponent) {
		this.parentComponent = parentComponent;
	}

	/**
	 *
	 * @return null if dialog was cancelled - generated CollectableSearchCondition otherwise
	 */	
	@Override
	public CollectableSearchCondition getSearchCondition() {
		showChooseDataSourceDialog();
		if (datasource != null) {
			return buildCollectableSearchConditionFromDatasource(this.datasource);
		}
		return null;
	}

	public static CollectableSearchCondition buildCollectableSearchConditionFromDatasource(DatasourceVO datasourceVO) {
		return buildPlainSubConditionFromDatasource(datasourceVO, null);
	}

	public static CollectableSearchCondition buildCollectableSearchConditionFromDatasource(DatasourceVO datasourceVO, Map<String,Object> dsParamsValues) {
		return buildPlainSubConditionFromDatasource(datasourceVO, dsParamsValues);
	}
	
	public static PlainSubCondition buildPlainSubConditionFromDatasource(DatasourceVO datasourceVO) {
		return buildPlainSubConditionFromDatasource(datasourceVO, null);
	}
	
	 public static PlainSubCondition buildPlainSubConditionFromDatasource(DatasourceVO datasourceVO, Map<String,Object> dsParams) {
		  PlainSubCondition resultPlainCondition = null;
		  try {
		   String columnAlias = "intid";
		   String dsXML = datasourceVO.getSource();
		   String modifiedXML = DatasourceXMLTransformer.replaceColumnAlias(datasourceVO.getSource(), "intid", intidAlias);
		   if(dsParams != null && !dsParams.isEmpty()){
		    modifiedXML = DatasourceXMLTransformer.setDatasourceParameters(modifiedXML, dsParams);
		   }
		   if(modifiedXML != null){
		    dsXML = modifiedXML;
		    columnAlias = intidAlias;
		   }
		   String plainSQL = DatasourceDelegate.getInstance().createSQL(dsXML);
		   resultPlainCondition = new PlainSubCondition("SELECT \""+columnAlias+"\" FROM ("+plainSQL+")",datasourceVO.getName());
		  }  catch (CommonBusinessException ex) {
		   final String sMessage = CommonLocaleDelegate.getMessage("DataSourceSearchImporter.1", "Beim Lesen der Datenquelle ist ein Fehler aufgetreten.");
		   Errors.getInstance().showExceptionDialog(new JPanel(), sMessage, ex);
		  } 
		  return resultPlainCondition;
		 }
	
	/**
	 *
	 * Procedural method - shows dialog - sets necessary parameters
	 *
	 */
	private void showChooseDataSourceDialog() {

		JComboBox dataSourceCombo = new JComboBox();
		JLabel dataSourceLabel = new JLabel(CommonLocaleDelegate.getMessage("DataSourceSearchImporter.6", "Datenquelle"));
		JLabel[] labels = new JLabel[] { dataSourceLabel };
		JComboBox[] combos = new JComboBox[] { dataSourceCombo };

		int maxComboLength = 300;
		int comboLength = 200;
		Collection<DatasourceVO> allAllowedDatasources = null;
		try {
			allAllowedDatasources = getAllowedDatasources();
		} catch (CommonPermissionException e) {
			JOptionPane.showMessageDialog(parentComponent, CommonLocaleDelegate.getMessage("DataSourceSearchImporter.2", "Es ist keine Datenquelle gefunden worden f\u00fcr die Sie eine Berechtigung haben."));
			return;
		}
		if(allAllowedDatasources != null && allAllowedDatasources.size() == 0){
			JOptionPane.showMessageDialog(parentComponent, CommonLocaleDelegate.getMessage("DataSourceSearchImporter.3", "Keine passende Datenquelle vorhanden."));
			return;
		}
		for(DatasourceVO dataSourceVO : allAllowedDatasources){
			dataSourceCombo.addItem(dataSourceVO);
			if(dataSourceVO.toString() != null){
				int currentVOLength = dataSourceVO.toString().trim().length();
				if(currentVOLength > comboLength && currentVOLength < maxComboLength){
					comboLength = currentVOLength;
				}
			}
		}		

		GridBagLayout gbLayout = new GridBagLayout();
		GridBagConstraints constraints = new GridBagConstraints();
		JPanel dataSourcePanel = new JPanel(gbLayout);
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.gridwidth = 2;
		constraints.gridwidth = 1;
		for (int i = 0; i < labels.length; i++) {
			labels[i].setPreferredSize(new Dimension(100, 25));
			combos[i].setPreferredSize(new Dimension(comboLength+50, 25));
			constraints.gridx = 0;
			constraints.gridy = i + 1;
			dataSourcePanel.add(labels[i], constraints);
			constraints.gridx = 1;
			dataSourcePanel.add(combos[i], constraints);
		}

		int decision = showDialog(dataSourcePanel);

		// show dialog until user chooses valid selection or cancels this
		// operation
		while (decision != JOptionPane.CANCEL_OPTION) {
			//check selected column
			if (dataSourceCombo.getSelectedItem() == null) {
				JOptionPane.showMessageDialog(parentComponent,
					CommonLocaleDelegate.getMessage("DataSourceSearchImporter.4", "Bitte Datenquelle w\u00e4hlen"));
				decision = showDialog(dataSourcePanel);

			} else {
				this.datasource = (DatasourceVO)dataSourceCombo.getSelectedItem();
				return;
			}
		}
	}

	private Collection<DatasourceVO> getAllowedDatasources() throws CommonPermissionException {
		Collection<DatasourceVO> allAllowedDatasources = DatasourceDelegate.getInstance().getAllDatasources();
		List<DatasourceVO> validDatasourcesList = new ArrayList<DatasourceVO>();
		for(DatasourceVO dVO : allAllowedDatasources){
			if(isValidDatasource(dVO)){
				validDatasourcesList.add(dVO);
			}
		}
		//List<DatasourceVO> allAllowedDatasourcesList = new ArrayList<DatasourceVO>(allAllowedDatasources);
		Collections.sort(validDatasourcesList, new Comparator<DatasourceVO>() {
					@Override
					public int compare(DatasourceVO o1, DatasourceVO o2) {
						return Collator.getInstance().compare(o1.getName(),o2.getName());
					}
				});
		return validDatasourcesList;
	}
	
	private boolean isValidDatasource(DatasourceVO dvo) {
		try {
			return new DatasourceVOValidator(dvo).isValidIntIdSubSelect();
		} catch (NuclosFatalException e) {
			e.printStackTrace();
		} catch (NuclosDatasourceException e) {
			e.printStackTrace();
		}
		return false;
	}

	private int showDialog(JComponent contents) {
		return JOptionPane.showOptionDialog(parentComponent, contents,
			CommonLocaleDelegate.getMessage("DataSourceSearchImporter.5", "Datenquelle: Auswahl"), JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.DEFAULT_OPTION, null, null, contents);
	}
	
	@Override
	public String toString(){
		return CommonLocaleDelegate.getMessage("DataSourceSearchImporter.6", "Datenquelle");
	}	
}
