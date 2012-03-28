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

import java.awt.Component;
import java.awt.Dimension;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.nuclos.client.ui.Controller;
import org.nuclos.client.ui.DateChooser;
import org.nuclos.client.ui.labeled.LabeledComponentSupport;
import org.nuclos.common2.DateTime;
import org.nuclos.common2.SeriesUtils;
import org.nuclos.common2.exception.CommonValidationException;

/**
 * Controller for selecting Plan Start.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:maik.stueker@novabit.de">Maik Stueker</a>
 * @version	01.00.00
 */
public class InstanceSelectPlanStartController extends Controller {
	
	private JPanel jpnResult = new JPanel();
	
	private final LabeledComponentSupport support = new LabeledComponentSupport();
	private final DateChooser dateFrom = new DateChooser(support);
	private DateChooser dateUntil = new DateChooser(support);
	
	private JComboBox cmbPlanStart = new JComboBox();
	
	private String series = null;
	

	public InstanceSelectPlanStartController(Component parent) {
		super(parent);
		setupControls();
	}
	
	private void setupControls(){
		final double[] columns = new double[]{
				2.0, TableLayout.PREFERRED, TableLayout.PREFERRED, 2.0};
		final double[] rows = new double[]{
				4.0,
				24.0,
				24.0,
				10.0,
				24.0,
				4.0};
		
		jpnResult.setLayout(new TableLayout(columns, rows));
		
		jpnResult.add(new JLabel("Bereich Start  "), getConstraints(1, 1, 1, 1));
		jpnResult.add(dateFrom, getConstraints(2, 2, 1, 1));
		dateFrom.getJTextField().getDocument().addDocumentListener(new DocumentListener(){
			@Override
			public void changedUpdate(DocumentEvent e) {
				calculatePlanStarts();
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				calculatePlanStarts();
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				calculatePlanStarts();
			}	
		});
		
		jpnResult.add(new JLabel("Bereich Ende  "), getConstraints(1, 1, 2, 2));
		jpnResult.add(dateUntil, getConstraints(2, 2, 2, 2));
		dateUntil.getJTextField().getDocument().addDocumentListener(new DocumentListener(){
			@Override
			public void changedUpdate(DocumentEvent e) {
				calculatePlanStarts();
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				calculatePlanStarts();
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				calculatePlanStarts();
			}	
		});
		
		jpnResult.add(new JLabel("M\u00f6gliche Starttermine  "), getConstraints(1, 1, 4, 4));
		jpnResult.add(cmbPlanStart, getConstraints(2, 2, 4, 4, TableLayoutConstants.LEFT, TableLayoutConstants.CENTER));
		cmbPlanStart.setPreferredSize(new Dimension(140, dateFrom.getPreferredSize().height));
		
		
	}
	
	/**
	 * 
	 */
	private void calculatePlanStarts(){
		try {
			cmbPlanStart.removeAllItems();
			if(dateFrom.getDate() != null &&
					dateUntil.getDate() != null){
				Date dFrom = dateFrom.getDate();
				Date dUntil = dateUntil.getDate();
				
				List<DateTime> lstPossibleDates = SeriesUtils.getPossibleDates(series, new DateTime(dFrom), new DateTime(dUntil));
				for (DateTime dateTime : lstPossibleDates){
					cmbPlanStart.addItem(new DateItem(dateTime.getDate()));
				}
			}
		} catch (CommonValidationException e) {
			cmbPlanStart.removeAllItems();
		}
	}
	
	/**
	 * @return Did the user press OK?
	 */
	public boolean run(String series) {
		// model -> dialog
		this.series = series;
		
		final JOptionPane optpn = new JOptionPane(jpnResult, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION);

		// perform the dialog:
		final JDialog dlg = optpn.createDialog(this.getParent(), "Plan Start ausw\u00e4hlen...");
		dlg.setModal(true);
		dlg.setResizable(true);
		dlg.pack();
		dlg.setLocationRelativeTo(this.getParent());
		dlg.setVisible(true);

		final Integer iBtn = (Integer) optpn.getValue();

		return (iBtn != null && iBtn.intValue() == JOptionPane.OK_OPTION);
	}
	
	/**
	 * 
	 * @return selected PlanStart if any (could be null!)
	 */
	public DateTime getPlanStart(){
		DateItem di = (DateItem) cmbPlanStart.getSelectedItem();
		if (di == null){
			return null;
		} else {
			return new DateTime(di.getValue());
		}
	}
	
	/**
	 * 
	 */
	private class DateItem{
		private Date value;
		
		public DateItem(Date value){
			this.value = value;
		}
		
		public Date getValue() {
			return value;
		}

		@Override
		public String toString(){
			if (value != null){
				final SimpleDateFormat sdf = new SimpleDateFormat("EE, "+DateTime.DATE_FORMAT_STRING);
				return sdf.format(value);
			} else {
				return "";
			}
		}
	}
	
	/**
	 * hAlign = LEFT
	 * vAlign = FULL
	 * 
	 * @param col1
	 * @param col2
	 * @param row1
	 * @param row2
	 * @return
	 */
	private TableLayoutConstraints getConstraints(int col1, int col2, int row1, int row2){
		return this.getConstraints(col1, col2, row1, row2, TableLayoutConstants.LEFT, TableLayoutConstants.FULL);
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

}
