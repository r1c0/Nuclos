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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.filechooser.FileFilter;

import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;

import org.nuclos.client.ui.ValidatingJOptionPane;
import org.nuclos.common.SpringApplicationContextHolder;
import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common.collect.collectable.searchcondition.CollectableLikeCondition;
import org.nuclos.common.collect.collectable.searchcondition.CollectableSearchCondition;
import org.nuclos.common.collect.collectable.searchcondition.CompositeCollectableSearchCondition;
import org.nuclos.common.collect.collectable.searchcondition.LogicalOperator;
import org.nuclos.common2.SpringLocaleDelegate;

/**
 * Opens a filechooser - reads out headers of selected XLS file - opens dialog
 * for header/attribute relation returns a CollectableSearchCondition by calling
 * getSearchCondition()
 *
 * Created by Novabit Informationssysteme GmbH <br>
 * Please visit <a href="http://www.novabit.de">www.novabit.de </a>
 *
 * @author <a href="mailto:florian.speidel@novabit.de">Florian.Speidel </a>
 * @author <a href="mailto:uwe.allner@novabit.de">Uwe Allner</a>
 * @version 01.00.00
 */
public class XLSSearchImporter implements ExtSourceSearchImporter {
	
	private Integer iModuleId = null;
	private JComponent parentComponent = null;
	private String sSelectedAttribute = null;
	private int[] firstUpperLeftCell = null; //[column,row]
	private Workbook wb = null;
	private Collection<String> readValues = null;
	
	// former Spring injection
	
	private SpringLocaleDelegate localeDelegate;
	
	// end of former Spring injection

	public XLSSearchImporter(Integer iModuleId, JComponent parentComponent) {
		this.parentComponent = parentComponent;
		this.iModuleId = iModuleId;
		
		setSpringLocaleDelegate(SpringApplicationContextHolder.getBean(SpringLocaleDelegate.class));
	}
	
	final void setSpringLocaleDelegate(SpringLocaleDelegate cld) {
		this.localeDelegate = cld;
	}

	final SpringLocaleDelegate getSpringLocaleDelegate() {
		return localeDelegate;
	}

	@Override
	public String toString(){
		return getSpringLocaleDelegate().getMessage("XLSSearchImporter.1", "Excel Datei");
	}
	
	/**
	 *
	 * @return null if dialog was cancelled - generated CollectableSearchCondition otherwise
	 */
	@Override
	public CollectableSearchCondition getSearchCondition() {
		JFileChooser xlsFileChooser = new JFileChooser();
		xlsFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		xlsFileChooser.setMultiSelectionEnabled(false);
		xlsFileChooser.setFileFilter(new FileFilter() {
			@Override
			public boolean accept(File f) {
				if (f != null) {
					return f.isDirectory() || f.getName().toLowerCase().endsWith(".xls");
				}
				return false;
			}
			@Override
			public String getDescription() {
				return "*.xls";
			}
		});
		
		xlsFileChooser.showDialog(parentComponent, null);
		if (xlsFileChooser.getSelectedFile() == null)
			return null;

		try {
			wb = Workbook.getWorkbook(xlsFileChooser.getSelectedFile());
		} 
		catch (BiffException e) {
			JOptionPane.showMessageDialog(parentComponent,
					"<html>" + getSpringLocaleDelegate().getMessage("XLSSearchImporter.2", "Keine g\u00fcltige Excel Datei") 
					+ ":<br>" + e.getMessage()	+ "</html>");
			return getSearchCondition(); //try again

		} 
		catch (IOException e) {
			JOptionPane.showMessageDialog(parentComponent, "<html>" + getSpringLocaleDelegate().getMessage("XLSSearchImporter.3", "I/O Fehler") 
					+ ":<br>" + e.getMessage() + "</html>");
			return getSearchCondition(); //try again
		}

		chooseSheetAndColumn();
		if (readValues != null) {
			return buildSearchExpression();
		}
		return null;
	}

	private CollectableSearchCondition buildSearchExpression() {
		CollectableEntityField field = CollectableGenericObjectEntity.getByModuleId(iModuleId).getEntityField(this.sSelectedAttribute);
		ArrayList<CollectableLikeCondition> atoms = new ArrayList<CollectableLikeCondition>();
		for (String element : readValues) {
			CollectableLikeCondition atom = new CollectableLikeCondition(field, element);
			atoms.add(atom);
		}
		CompositeCollectableSearchCondition composited = new CompositeCollectableSearchCondition(LogicalOperator.OR, atoms);
		return composited;
	}

	/**
	 *
	 * Procedural method - shows dialog - sets necessary parameters
	 *
	 */
	private void chooseSheetAndColumn() {
		ValidateImportPanel pnlImport = new ValidateImportPanel();
		ValidateImportPane pane = new ValidateImportPane(pnlImport);
		int decision = pane.showDialog();

		// show dialog until all parameters are valid or user cancels this operation
		if (decision != JOptionPane.CANCEL_OPTION) {
			//everthing seems ok - start reading values
			Collection<String> values = new HashSet<String>();
			Sheet selectedSheet = wb.getSheet(pnlImport.cbSheetName.getSelectedIndex());
			int offsetRows = 0;
			if (pnlImport.chkHeaderAvailable.isSelected()) {
				offsetRows = 1;
			}
			for (int row = firstUpperLeftCell[1] + offsetRows; row < selectedSheet.getRows(); row++) {
				String content = selectedSheet.getCell(firstUpperLeftCell[0] + pnlImport.cbColumnName.getSelectedIndex(), row).getContents();
				if (content != null && !content.trim().equals("")) {
					values.add(content);
				}
			}
			this.readValues = values;
			this.sSelectedAttribute = pnlImport.cbAttributeName.getSelectedItem().toString();
			return;
		}
	}

	private int[] getFirstUpperLeftCell(Sheet sheet) {
		for (int column = 0; column < sheet.getColumns(); column++) {
			for (int row = 0; row < sheet.getRows(); row++) {
				String cellContent = sheet.getCell(column, row).getContents();
				if (cellContent != null && !cellContent.trim().equals(""))
					return new int[] { column, row };
			}
		}
		return null;
	}
	
	private class ValidateImportPane extends ValidatingJOptionPane {

		private ValidateImportPanel pnlContents;
		
		public ValidateImportPane(ValidateImportPanel contents) {
			super(parentComponent, getSpringLocaleDelegate().getMessage(
					"XLSSearchImporter.4", "Excel Import: Suche"), contents);
			pnlContents = contents;
		}
		
		@Override
		protected void validateInput() throws ValidatingJOptionPane.ErrorInfo {
			final SpringLocaleDelegate localeDelegate = getSpringLocaleDelegate();
			if (pnlContents.cbColumnName.getSelectedItem() == null) {
				throw new ErrorInfo(localeDelegate.getMessage(
						"XLSSearchImporter.5", "Bitte Spalte w\u00e4hlen"), pnlContents.cbColumnName);
			} 
			if (pnlContents.cbSheetName.getSelectedItem() == null) {
				throw new ErrorInfo(localeDelegate.getMessage(
						"XLSSearchImporter.6", "Bitte Arbeitsblatt w\u00e4hlen"), pnlContents.cbSheetName);
			} 
			if (firstUpperLeftCell == null) {
				throw new ErrorInfo(localeDelegate.getMessage(
						"XLSSearchImporter.7", "Gew\u00e4hltes Arbeitsblatt enth\u00e4lt keine Daten"), null);
			} 
		}
	} // inner class ValidateImportPane
	
	private class ValidateImportPanel extends JPanel {
		final SpringLocaleDelegate localeDelegate = getSpringLocaleDelegate();
		private final JComboBox cbSheetName = new JComboBox();
		private final JComboBox cbColumnName = new JComboBox();
		private final JComboBox cbAttributeName = new JComboBox();
		private final JCheckBox chkHeaderAvailable = new JCheckBox(localeDelegate.getMessage(
				"XLSSearchImporter.8", "Spalten haben \u00dcberschriften"));
		private final JLabel lblSheetName = new JLabel(localeDelegate.getMessage(
				"XLSSearchImporter.9", "Arbeitsblatt ausw\u00e4hlen"));
		private final JLabel lblColumnName = new JLabel(localeDelegate.getMessage(
				"XLSSearchImporter.10", "Spalte ausw\u00e4hlen"));
		private final JLabel lblAttributeName = new JLabel(localeDelegate.getMessage(
				"XLSSearchImporter.11", "Attribut ausw\u00e4hlen"));

		private final JLabel[] labels = new JLabel[] { lblSheetName, lblColumnName, lblAttributeName };
		private final JComboBox[] combos = new JComboBox[] { cbSheetName, cbColumnName, cbAttributeName };
		
		public ValidateImportPanel() {
			super(new GridBagLayout());
			
			chkHeaderAvailable.setSelected(true);

			for (int i = 0; i < wb.getSheets().length; i++) {
				String sheetName = wb.getSheet(i).getName();
				if (sheetName != null) {
					sheetName += " [#" + i + "]";
				}
				else {
					sheetName = " [#" + i + "]";
				}
				cbSheetName.addItem(sheetName);
			}
			cbSheetName.setSelectedItem(null);
			
			ActionListener sheetNameListener = new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					Sheet selectedSheet = wb.getSheet(cbSheetName.getSelectedIndex());
					int[] firstUpperLeftCellLocal = getFirstUpperLeftCell(selectedSheet);
					cbColumnName.removeAllItems();
					if (firstUpperLeftCellLocal == null) {
						firstUpperLeftCell = null;
						return;
					}
					firstUpperLeftCell = firstUpperLeftCellLocal;
					for (int i = firstUpperLeftCell[0]; i < selectedSheet.getColumns(); i++) {
						cbColumnName.addItem(selectedSheet.getCell(i,
								firstUpperLeftCell[1]).getContents()
								+ " [#" + i + "]");
					}
				}
			};

			cbSheetName.addActionListener(sheetNameListener);

			String[] fieldnames=CollectableGenericObjectEntity.getByModuleId(iModuleId).getFieldNames().toArray(new String[0]);
			Arrays.sort(fieldnames,Collator.getInstance());
			for (int i=0; i<fieldnames.length;i++) {
				cbAttributeName.addItem(fieldnames[i]);
			}

			GridBagConstraints constraints = new GridBagConstraints();
			
			chkHeaderAvailable.setPreferredSize(new Dimension(300, 25));
			constraints.gridx = 0;
			constraints.gridy = 0;
			constraints.gridwidth = 2;
			add(chkHeaderAvailable, constraints);
			constraints.gridwidth = 1;
			for (int i = 0; i < labels.length; i++) {
				labels[i].setPreferredSize(new Dimension(150, 25));
				combos[i].setPreferredSize(new Dimension(150, 25));
				constraints.gridx = 0;
				constraints.gridy = i + 1;
				add(labels[i], constraints);
				constraints.gridx = 1;
				add(combos[i], constraints);
			}
		}
	}
};
