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
package org.nuclos.client.ui;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;
import info.clearthought.layout.TableLayoutConstraints;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;

import javax.swing.ButtonGroup;
import javax.swing.InputVerifier;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;

import org.nuclos.common2.SeriesUtils;
import org.nuclos.common2.SeriesUtils.SeriesListItem;

/**
 * JComponent for selecting a date time series. 
 * The selection is represented in string format <code>getText()</code>
 * and has following code:
 * DAILY_MODE = d|[time hours]|[time minutes]|[day (0 meens every working day)]
 * WEEKLY_MODE = w|[time hours]|[time minutes]|[weeks]|[monday]|[tuesday]|[wednesday]|[thursday]|[friday]|[saturday]|[sunday]
 * MONTHLY_MODE 1 = m|[time hours]|[time minutes]|m1||[day]|[month]
 * MONTHLY_MODE 2 = m|[time hours]|[time minutes]|m2||[number]|[weekday]|[month]
 * YEARLY MODE 1 = m|[time hours]|[time minutes]|y1||[day]|[month]
 * YEARLY MODE 2 = m|[time hours]|[time minutes]|y2||[number]|[weekday]|[month]
 * DEACTIVED = n
 * 
 * See <code>SeriesUtils</code> for calculating new dates with this codes!
 *
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Maik.Stueker@novabit.de">Maik Stueker</a>
 * @version 01.00.00
 *
 * @beaninfo
 *   attribute: isContainer false
 *   description: 
 */
public class DateTimeSeries extends JComponent implements Serializable, ActionListener {
	
	public static final int NO_MODE = -1;
	public static final int DAILY_MODE = 0;
	public static final int WEEKLY_MODE = 1;
	public static final int MONTHLY_MODE = 2;
	public static final int YEARLY_MODE = 3;
	
	private boolean isNoModeAvaiable;
	private int iCurrentMode;
	
	private boolean isUpdatingGUI;
	private boolean isUpdatingField;

	/**
	 * This DateTimeSeries has no
	 * "no series"-option for selection
	 */
	public DateTimeSeries() {
		this(false);
	}
	
	/**
	 * 
	 * @param isNoModeAvaiable
	 * 			if <code>true</code> a "no series"-option 
	 * 			would be added
	 */
	public DateTimeSeries(boolean isNoModeAvaiable){
		this.isNoModeAvaiable = isNoModeAvaiable;
		setupControl();
		setMode(NO_MODE);
	}
	
	/**
	 * Components for the control itself
	 */
	private JPanel pnlTime;
	private TableLayoutConstraints constraintsPnlTime;
	private JPanel pnlDailyMode;
	private JPanel pnlWeeklyMode;
	private JPanel pnlMonthlyMode;
	private JPanel pnlYearlyMode;
	private JPanel pnlEmpty;
	
	// the textfield that holds the series
	private final DateTimeSeriesCommonJTextField tfSeries = new DateTimeSeriesCommonJTextField();
	
	private final JTextField tfTimeH = new JTextField();
	private final JTextField tfTimeM = new JTextField();
	private final ButtonGroup bgMainModus = new ButtonGroup();
	private final JRadioButton radioDaily = new JRadioButton("T\u00e4glich");
	private final JRadioButton radioWeekly = new JRadioButton("W\u00f6chentlich");
	private final JRadioButton radioMonthly = new JRadioButton("Monatlich");
	private final JRadioButton radioYearly = new JRadioButton("J\u00e4hrlich");
	private final JRadioButton radioNoMode = new JRadioButton("kein Muster");

	/**
	 * DAILY MODE
	 */
	private final JRadioButton radioDailyAll = new JRadioButton("Alle");
	private final JRadioButton radioDailyWorking = new JRadioButton("Jeden Arbeitstag");
	private final JTextField tfDailyDays = new JTextField();
	
	/**
	 * WEEKLY MODE
	 */
	private final JTextField tfWeeklyWeeks = new JTextField();
	private final JCheckBox checkMonday = new JCheckBox("Montag");
	private final JCheckBox checkTuesday = new JCheckBox("Dienstag");
	private final JCheckBox checkWednesday = new JCheckBox("Mittwoch");
	private final JCheckBox checkThursday = new JCheckBox("Donnerstag");
	private final JCheckBox checkFriday = new JCheckBox("Freitag");
	private final JCheckBox checkSaturday = new JCheckBox("Samstag");
	private final JCheckBox checkSunday = new JCheckBox("Sonntag");
	
	/**
	 * MONTHLY MODE
	 */
	final JRadioButton radioMonthly1 = new JRadioButton("Am ");
	final JRadioButton radioMonthly2 = new JRadioButton("Am ");
	private final JTextField tfMonthly1Day = new JTextField();
	private final JTextField tfMonthly1Month = new JTextField();
	private final JComboBox comboMonthly2Number = new JComboBox();
	private final JComboBox comboMonthly2Weekday = new JComboBox();
	private final JTextField tfMonthly2Month = new JTextField();
	
	/**
	 * YEARLY MODE
	 */
	private final JRadioButton radioYearly1 = new JRadioButton("Jeden ");
	private final JRadioButton radioYearly2 = new JRadioButton("Am ");
	private final JTextField tfYearly1Day = new JTextField();
	private final JComboBox comboYearly1Month = new JComboBox();
	private final JComboBox comboYearly2Number = new JComboBox();
	private final JComboBox comboYearly2Weekday = new JComboBox();
	private final JComboBox comboYearly2Month = new JComboBox();
	
	/**
	 * Use with caution. Only use for reading or adding listeners. Don't write to this text field.
	 * @return JtextField
	 */
	public JTextField getJTextField() {
		return this.tfSeries;
	}
	
	/**
	 * 
	 * @param the GUI selection in String
	 */
	public void setText(String sText) {
		this.tfSeries.setText(sText);
	}
	
	/**
	 * 
	 * @return the GUI selection in String
	 */
	public String getText() {
		return this.tfSeries.getText();
	}
	
	/**
	 * reset all input fields to default
	 */
	private void resetAllInputFieldsToDefault(){
		
		radioDailyAll.setSelected(true);
		tfDailyDays.setText("1");
		tfDailyDays.setEnabled(true);
		
		tfMonthly1Day.setEnabled(false);
		tfMonthly1Month.setEnabled(false);
		comboMonthly2Number.setEnabled(false);
		comboMonthly2Weekday.setEnabled(false);
		tfMonthly2Month.setEnabled(false);
		
		tfWeeklyWeeks.setText("1");
		checkMonday.setSelected(true);
		checkTuesday.setSelected(false);
		checkWednesday.setSelected(false);
		checkThursday.setSelected(false);
		checkFriday.setSelected(false);
		checkSaturday.setSelected(false);
		checkSunday.setSelected(false);
		
		radioMonthly1.setSelected(true);
		tfMonthly1Day.setText("1");
		tfMonthly1Day.setEnabled(true);
		tfMonthly1Month.setText("1");
		tfMonthly1Month.setEnabled(true);
		tfMonthly2Month.setText("1");
		tfMonthly2Month.setEnabled(false);
		comboMonthly2Number.setEnabled(false);
		if (comboMonthly2Number.getItemCount()>0)
			comboMonthly2Number.setSelectedIndex(0);
		comboMonthly2Weekday.setEnabled(false);
		if (comboMonthly2Weekday.getItemCount()>0)
			comboMonthly2Weekday.setSelectedIndex(0);
		
		radioYearly1.setSelected(true);
		tfYearly1Day.setText("1");
		tfYearly1Day.setEnabled(true);
		comboYearly1Month.setEnabled(true);
		if (comboYearly1Month.getItemCount()>0)
			comboYearly1Month.setSelectedIndex(0);
		comboYearly2Number.setEnabled(false);
		if (comboYearly2Number.getItemCount()>0)
			comboYearly2Number.setSelectedIndex(0);
		comboYearly2Weekday.setEnabled(false);
		if (comboYearly2Weekday.getItemCount()>0)
			comboYearly2Weekday.setSelectedIndex(0);
		comboYearly2Month.setEnabled(false);
		if (comboYearly2Month.getItemCount()>0)
			comboYearly2Month.setSelectedIndex(0);
	}
	
	/**
	 * reads values from <code>getJTextField()</code>
	 * and present it in GUI
	 * 
	 * by modifying the code structure dont forget to adapt 
	 * <code>SeriesUtils</code>
	 */
	private synchronized void handleUpdateGUI(){
		if (isUpdatingField){
			return;
		}
		this.isUpdatingGUI = true;
		
		resetAllInputFieldsToDefault();
		
		String data = this.tfSeries.getText();
		
		// if empty field
		if ("".equals(data)){
			data = "n";
		}
		
		String[] split = org.apache.commons.lang.StringUtils.split(data, '|');
		
		if (split.length > 0){
			String modus = split[0];
			
			if ("d".equals(modus)){
				this.setMode(DAILY_MODE);
				
				tfTimeH.setText(split[1]);
				tfTimeM.setText(split[2]);
				
				int days = Integer.parseInt(split[3]);
				if (days == 0){
					radioDailyWorking.setSelected(true);
					tfDailyDays.setEnabled(false);
				} else {
					radioDailyAll.setSelected(true);
					tfDailyDays.setEnabled(true);
					tfDailyDays.setText(String.valueOf(days));
				}
				
			} else if ("w".equals(modus)){
				this.setMode(WEEKLY_MODE);
				
				tfTimeH.setText(split[1]);
				tfTimeM.setText(split[2]);
				tfWeeklyWeeks.setText(split[3]);
				
				checkMonday.setSelected("1".equals(split[4])?true:false);
				checkTuesday.setSelected("1".equals(split[5])?true:false);
				checkWednesday.setSelected("1".equals(split[6])?true:false);
				checkThursday.setSelected("1".equals(split[7])?true:false);
				checkFriday.setSelected("1".equals(split[8])?true:false);
				checkSaturday.setSelected("1".equals(split[9])?true:false);
				checkSunday.setSelected("1".equals(split[10])?true:false);
				
			} else if ("m".equals(modus)){
				this.setMode(MONTHLY_MODE);
				
				tfTimeH.setText(split[1]);
				tfTimeM.setText(split[2]);
				
				if ("m1".equals(split[3])){
					radioMonthly1.setSelected(true);
					radioMonthly2.setSelected(false);
					
					tfMonthly1Day.setEnabled(true);
					tfMonthly1Month.setEnabled(true);
					tfMonthly2Month.setEnabled(false);
					comboMonthly2Number.setEnabled(false);
					comboMonthly2Weekday.setEnabled(false);
					
					tfMonthly1Day.setText(split[4]);
					tfMonthly1Month.setText(split[5]);
				} else {
					radioMonthly1.setSelected(false);
					radioMonthly2.setSelected(true);
					
					tfMonthly1Day.setEnabled(false);
					tfMonthly1Month.setEnabled(false);
					tfMonthly2Month.setEnabled(true);
					comboMonthly2Number.setEnabled(true);
					comboMonthly2Weekday.setEnabled(true);
					
					comboMonthly2Number.setSelectedItem(SeriesUtils.getNumberItemById(Integer.parseInt(split[4])));
					comboMonthly2Weekday.setSelectedItem(SeriesUtils.getWeekdayItemById(Integer.parseInt(split[5])));
					tfMonthly2Month.setText(split[6]);
				}
				
			} else if ("y".equals(modus)){
				this.setMode(YEARLY_MODE);
				
				tfTimeH.setText(split[1]);
				tfTimeM.setText(split[2]);
				
				if ("y1".equals(split[3])){
					radioYearly1.setSelected(true);
					radioYearly2.setSelected(false);
					
					tfYearly1Day.setEnabled(true);
					comboYearly1Month.setEnabled(true);
					
					comboYearly2Number.setEnabled(false);
					comboYearly2Weekday.setEnabled(false);
					comboYearly2Month.setEnabled(false);
					
					tfYearly1Day.setText(split[4]);
					comboYearly1Month.setSelectedItem(SeriesUtils.getMonthItemById(Integer.parseInt(split[5])));
				} else {
					radioYearly1.setSelected(false);
					radioYearly2.setSelected(true);
					
					tfYearly1Day.setEnabled(false);
					comboYearly1Month.setEnabled(false);
					
					comboYearly2Number.setEnabled(true);
					comboYearly2Weekday.setEnabled(true);
					comboYearly2Month.setEnabled(true);
					
					comboYearly2Number.setSelectedItem(SeriesUtils.getNumberItemById(Integer.parseInt(split[4])));
					comboYearly2Weekday.setSelectedItem(SeriesUtils.getWeekdayItemById(Integer.parseInt(split[5])));
					comboYearly2Month.setSelectedItem(SeriesUtils.getMonthItemById(Integer.parseInt(split[6])));
				}
				
			} else if ("n".equals(modus)){
				this.setMode(NO_MODE);
			}
		}
		this.isUpdatingGUI = false;
	}
	
	/**
	 * writes the selected values to
	 * <code>getJTextField()</code>
	 * 
	 * by modifying the code structure dont forget to adapt 
	 * <code>SeriesUtils</code>
	 */
	private synchronized void handleUpdateField(){
		if (isUpdatingGUI){
			return;
		}
		this.isUpdatingField = true;
		
		StringBuffer result = new StringBuffer();
		
		switch (iCurrentMode){
		case DAILY_MODE:
			result.append("d|"+getTime());
			if (radioDailyAll.isSelected()){
				String days = tfDailyDays.getText();
				if (days != null && !"".equals(days)){
					int iDays = Integer.parseInt(days);
					result.append("|"+iDays);
				}
			} else {
				result.append("|0");
			}
			break;
		case WEEKLY_MODE:
			result.append("w|"+getTime());
			result.append("|"+tfWeeklyWeeks.getText());
			result.append("|"+(checkMonday.isSelected()?"1":"0"));
			result.append("|"+(checkTuesday.isSelected()?"1":"0"));
			result.append("|"+(checkWednesday.isSelected()?"1":"0"));
			result.append("|"+(checkThursday.isSelected()?"1":"0"));
			result.append("|"+(checkFriday.isSelected()?"1":"0"));
			result.append("|"+(checkSaturday.isSelected()?"1":"0"));
			result.append("|"+(checkSunday.isSelected()?"1":"0"));
			break;
		case MONTHLY_MODE:
			result.append("m|"+getTime());
			if (radioMonthly1.isSelected()){
				result.append("|m1");
				result.append("|"+tfMonthly1Day.getText());
				result.append("|"+tfMonthly1Month.getText());
			} else {
				result.append("|m2");
				result.append("|"+((SeriesListItem)comboMonthly2Number.getSelectedItem()).getId());
				result.append("|"+((SeriesListItem)comboMonthly2Weekday.getSelectedItem()).getId());
				result.append("|"+tfMonthly2Month.getText());
			}
			break;
		case YEARLY_MODE:
			result.append("y|"+getTime());
			if(radioYearly1.isSelected()){
				result.append("|y1");
				result.append("|"+tfYearly1Day.getText());
				result.append("|"+((SeriesListItem)comboYearly1Month.getSelectedItem()).getId());
			} else {
				result.append("|y2");
				result.append("|"+((SeriesListItem)comboYearly2Number.getSelectedItem()).getId());
				result.append("|"+((SeriesListItem)comboYearly2Weekday.getSelectedItem()).getId());
				result.append("|"+((SeriesListItem)comboYearly2Month.getSelectedItem()).getId());
			}
			break;
		case NO_MODE:
			result.append("n");
			break;
		}
		
		if (!getJTextField().getText().equals(result.toString())){
			getJTextField().setText(result.toString());
		}
		this.isUpdatingField = false;
	}
	
	/**
	 * 
	 * @return [h]|[m]
	 */
	private String getTime(){
		final String h = tfTimeH.getText();
		final String m = tfTimeM.getText();
		return ((h==null||"".equals(h))?"0":h)+"|"+((m==null||"".equals(m))?"00":m);
	}
	
	/**
	 * switches the mode panel and calls 
	 * <code>handleUpdateField()</code>
	 * 
	 * @param iMode
	 * 			options are:
	 * 			<code>DateTimeSeries.DAILY_MODE</code>
	 * 			<code>DateTimeSeries.WEEKLY_MODE</code>
	 * 			<code>DateTimeSeries.MONTHLY_MODE</code>
	 * 			<code>DateTimeSeries.YEARLY_MODE</code>
	 * 			<code>DateTimeSeries.DEACTIVATED</code>
	 */
	private synchronized void setMode(int iMode){
		this.iCurrentMode = iMode;
		switch (iMode){
		case DAILY_MODE:
			clearMode();
			bgMainModus.setSelected(radioDaily.getModel(), true);
			this.pnlDailyMode.add(pnlTime, constraintsPnlTime);
			this.pnlDailyMode.setVisible(true);
			break;
		case WEEKLY_MODE:
			clearMode();
			bgMainModus.setSelected(radioWeekly.getModel(), true);
			this.pnlWeeklyMode.add(pnlTime, constraintsPnlTime);
			this.pnlWeeklyMode.setVisible(true);
			break;
		case MONTHLY_MODE:
			clearMode();
			bgMainModus.setSelected(radioMonthly.getModel(), true);
			this.pnlMonthlyMode.add(pnlTime, constraintsPnlTime);
			this.pnlMonthlyMode.setVisible(true);
			break;
		case YEARLY_MODE:
			clearMode();
			bgMainModus.setSelected(radioYearly.getModel(), true);
			this.pnlYearlyMode.add(pnlTime, constraintsPnlTime);
			this.pnlYearlyMode.setVisible(true);
			break;
		case NO_MODE:
			clearMode();
			bgMainModus.setSelected(radioNoMode.getModel(), true);
			this.pnlEmpty.setVisible(true);
			break;
		}
		handleUpdateField();
	}
	
	/**
	 * set all mode panels to unvisible
	 */
	private void clearMode(){
		this.pnlDailyMode.setVisible(false);
		this.pnlWeeklyMode.setVisible(false);
		this.pnlMonthlyMode.setVisible(false);
		this.pnlYearlyMode.setVisible(false);
		this.pnlEmpty.setVisible(false);
	}
	
	/**
	 * - creates the panels
	 * - set properties and actionlisteners to all controls
	 * - add all controls to the panels
	 */
	private synchronized void setupControl(){
		this.setLayout(new BorderLayout());
		
		final double[] columns = new double[]{
				2.0, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, 2.0};
		final double[] rows = new double[]{
				4.0,
				28.0,
				28.0,
				28.0,
				28.0,
				28.0,
				4.0};
		
		tfTimeH.setInputVerifier(new TextFieldNumberVerifier(0, 23));
		tfTimeH.setColumns(2);
		tfTimeM.setInputVerifier(new TextFieldNumberVerifier(0, 59));
		tfTimeM.setColumns(2);
		
		final JPanel pnlHolder = new JPanel();
		pnlHolder.setLayout(new TableLayout(columns, rows));
		pnlHolder.setBorder(tfSeries.getBorder());
		
		radioDaily.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {setMode(DAILY_MODE);}
		});
		radioWeekly.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {setMode(WEEKLY_MODE);}
		});
		radioMonthly.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {setMode(MONTHLY_MODE);}
		});
		radioYearly.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {setMode(YEARLY_MODE);}
		});
		radioNoMode.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {setMode(NO_MODE);}
		});
		
		bgMainModus.add(radioDaily);
		bgMainModus.add(radioWeekly);
		bgMainModus.add(radioMonthly);
		bgMainModus.add(radioYearly);
		bgMainModus.add(radioNoMode);
		
		pnlHolder.add(radioDaily, getConstraints(1, 1, 1, 1));
		pnlHolder.add(radioWeekly, getConstraints(1, 1, 2, 2));
		pnlHolder.add(radioMonthly, getConstraints(1, 1, 3, 3));
		pnlHolder.add(radioYearly, getConstraints(1, 1, 4, 4));
		if (isNoModeAvaiable){
			pnlHolder.add(radioNoMode, getConstraints(1, 1, 5, 5));
		}
		pnlHolder.add(new JSeparator(SwingConstants.VERTICAL), getConstraints(2, 2, 1, 5, TableLayoutConstants.CENTER, TableLayoutConstants.FULL));
		
		final JLabel labStartTime = new JLabel("Uhrzeit  ");
		
		constraintsPnlTime = getConstraints(1, 4, 1, 1);
		pnlTime = new JPanel(new TableLayout(
				new double[]{2.0, TableLayoutConstants.PREFERRED, TableLayoutConstants.PREFERRED, TableLayoutConstants.PREFERRED, TableLayoutConstants.PREFERRED, 2.0}, 
				new double[]{
						4.0,
						21.0,
						4.0}));
		pnlTime.add(labStartTime, getConstraints(1, 1, 1, 1));
		pnlTime.add(tfTimeH, getConstraints(2, 2, 1, 1));
		pnlTime.add(new JLabel(" : "), getConstraints(3, 3, 1, 1));
		pnlTime.add(tfTimeM, getConstraints(4, 4, 1, 1));
		
		final TableLayoutConstraints constraintsRight = 
		getConstraints(3, 3, 1, 5, TableLayoutConstants.FULL, TableLayoutConstants.FULL);
		
		pnlEmpty = new JPanel(new TableLayout());
		pnlHolder.add(pnlEmpty, constraintsRight);
		
		/**
		 * DAILY MODE
		 */
		pnlDailyMode = new JPanel(new TableLayout(
				new double[]{
						4.0, TableLayoutConstants.PREFERRED, TableLayoutConstants.PREFERRED, TableLayoutConstants.PREFERRED, 4.0},
				new double[]{
						4.0,
						TableLayoutConstants.PREFERRED, // for start time panel
						21.0,
						21.0,
						4.0}));
		
		tfDailyDays.setColumns(4);
		tfDailyDays.setInputVerifier(new TextFieldNumberVerifier(1, 366));
		
		radioDailyAll.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				tfDailyDays.setEnabled(true);
			}
		});
		radioDailyWorking.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				tfDailyDays.setEnabled(false);
			}
		});
		
		final ButtonGroup bgDailyMode = new ButtonGroup();
		bgDailyMode.add(this.radioDailyAll);
		bgDailyMode.add(this.radioDailyWorking);
		
		radioDailyAll.addActionListener(this);
		radioDailyWorking.addActionListener(this);
		
		pnlDailyMode.add(radioDailyAll, getConstraints(1, 1, 2, 2));
		pnlDailyMode.add(radioDailyWorking, getConstraints(1, 3, 3, 3));
		pnlDailyMode.add(tfDailyDays, getConstraints(2, 2, 2, 2));
		pnlDailyMode.add(new JLabel(" Tag(e)"), getConstraints(3, 3, 2, 2));
		
		pnlHolder.add(pnlDailyMode, constraintsRight);
		
		/**
		 * WEEKLY MODE
		 */
		pnlWeeklyMode = new JPanel(new TableLayout(
				new double[]{
						4.0, TableLayoutConstants.PREFERRED, TableLayoutConstants.PREFERRED, TableLayoutConstants.PREFERRED, 4.0},
				new double[]{
						4.0,
						TableLayoutConstants.PREFERRED, // for start time panel
						21.0,
						21.0,
						21.0,
						21.0,
						21.0,
						4.0}));
		
		tfWeeklyWeeks.setColumns(3);
		tfWeeklyWeeks.setInputVerifier(new TextFieldNumberVerifier(1, 53));
		
		pnlWeeklyMode.add(new JLabel("Jede/Alle  "), getConstraints(1, 1, 2, 2));
		pnlWeeklyMode.add(tfWeeklyWeeks, getConstraints(2, 2, 2, 2));
		pnlWeeklyMode.add(new JLabel(" Woche(n) am"), getConstraints(3, 3, 2, 2));
		pnlWeeklyMode.add(checkMonday, getConstraints(1, 2, 3, 3));
		pnlWeeklyMode.add(checkTuesday, getConstraints(1, 2, 4, 4));
		pnlWeeklyMode.add(checkWednesday, getConstraints(1, 2, 5, 5));
		pnlWeeklyMode.add(checkThursday, getConstraints(1, 2, 6, 6));
		pnlWeeklyMode.add(checkFriday, getConstraints(3, 3, 3, 3));
		pnlWeeklyMode.add(checkSaturday, getConstraints(3, 3, 4, 4));
		pnlWeeklyMode.add(checkSunday, getConstraints(3, 3, 5, 5));
		
		checkMonday.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (!isAtLeastOneWeekdaySelected())
					checkMonday.setSelected(true);
			}
		});
		checkMonday.addActionListener(this);
		checkTuesday.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (!isAtLeastOneWeekdaySelected())
					checkTuesday.setSelected(true);
			}
		});
		checkTuesday.addActionListener(this);
		checkWednesday.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (!isAtLeastOneWeekdaySelected())
					checkWednesday.setSelected(true);
			}
		});
		checkWednesday.addActionListener(this);
		checkThursday.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (!isAtLeastOneWeekdaySelected())
					checkThursday.setSelected(true);
			}
		});
		checkThursday.addActionListener(this);
		checkFriday.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (!isAtLeastOneWeekdaySelected())
					checkFriday.setSelected(true);
			}
		});
		checkFriday.addActionListener(this);
		checkSaturday.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (!isAtLeastOneWeekdaySelected())
					checkSaturday.setSelected(true);
			}
		});
		checkSaturday.addActionListener(this);
		checkSunday.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (!isAtLeastOneWeekdaySelected())
					checkSunday.setSelected(true);
			}
		});
		checkSunday.addActionListener(this);
		
		pnlHolder.add(pnlWeeklyMode, constraintsRight);
		
		
		/**
		 * MONTHLY MODE
		 */
		pnlMonthlyMode = new JPanel(new TableLayout(
				new double[]{
						4.0, TableLayoutConstants.PREFERRED, TableLayoutConstants.PREFERRED, TableLayoutConstants.PREFERRED, TableLayoutConstants.PREFERRED, TableLayoutConstants.PREFERRED, TableLayoutConstants.PREFERRED, 4.0},
				new double[]{
						4.0,
						TableLayoutConstants.PREFERRED, // for start time panel
						21.0,
						21.0,
						26.0,
						21.0,
						4.0}));
		
		final ButtonGroup bgMonthlyMode = new ButtonGroup();	
		radioMonthly1.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				tfMonthly1Day.setEnabled(true);
				tfMonthly1Month.setEnabled(true);
				comboMonthly2Number.setEnabled(false);
				comboMonthly2Weekday.setEnabled(false);
				tfMonthly2Month.setEnabled(false);
			}
			
		});
		radioMonthly1.addActionListener(this);
		radioMonthly2.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				tfMonthly1Day.setEnabled(false);
				tfMonthly1Month.setEnabled(false);
				comboMonthly2Number.setEnabled(true);
				comboMonthly2Weekday.setEnabled(true);
				tfMonthly2Month.setEnabled(true);
			}
			
		});
		radioMonthly2.addActionListener(this);
		
		bgMonthlyMode.add(radioMonthly1);
		bgMonthlyMode.add(radioMonthly2);
		
		tfMonthly1Day.setColumns(3);
		tfMonthly1Day.setInputVerifier(new TextFieldNumberVerifier(1, 31));
		tfMonthly1Month.setColumns(3);
		tfMonthly1Month.setInputVerifier(new TextFieldNumberVerifier(1, 12));
		tfMonthly2Month.setColumns(3);
		tfMonthly2Month.setInputVerifier(new TextFieldNumberVerifier(1, 12));
		
		comboMonthly2Number.addActionListener(this);
		for (SeriesListItem sli : SeriesUtils.getNumberItems()){
			comboMonthly2Number.addItem(sli);
		}
		
		comboMonthly2Weekday.addActionListener(this);
		for (SeriesListItem sli : SeriesUtils.getWeekdayItems()){
			comboMonthly2Weekday.addItem(sli);
		}
		
		pnlMonthlyMode.add(radioMonthly1, getConstraints(1, 1, 2, 2));
		pnlMonthlyMode.add(radioMonthly2, getConstraints(1, 1, 4, 4, TableLayoutConstants.LEFT, TableLayoutConstants.BOTTOM));
		
		pnlMonthlyMode.add(tfMonthly1Day, getConstraints(2, 2, 2, 2));
		pnlMonthlyMode.add(new JLabel(" . Tag"), getConstraints(3, 3, 2, 2));
		pnlMonthlyMode.add(new JLabel("jedes "), getConstraints(2, 2, 3, 3));
		pnlMonthlyMode.add(tfMonthly1Month, getConstraints(3, 3, 3, 3));
		pnlMonthlyMode.add(new JLabel(" . Monats"), getConstraints(4, 4, 3, 3));
		
		pnlMonthlyMode.add(comboMonthly2Number, getConstraints(2, 3, 4, 4, TableLayoutConstants.LEFT, TableLayoutConstants.BOTTOM));
		pnlMonthlyMode.add(comboMonthly2Weekday, getConstraints(4, 5, 4, 4, TableLayoutConstants.LEFT, TableLayoutConstants.BOTTOM));
		pnlMonthlyMode.add(new JLabel("jedes "), getConstraints(2, 2, 5, 5));
		pnlMonthlyMode.add(tfMonthly2Month, getConstraints(3, 3, 5, 5));
		pnlMonthlyMode.add(new JLabel(" . Monats"), getConstraints(4, 4, 5, 5));
		
		pnlHolder.add(pnlMonthlyMode, constraintsRight);
		
		/**
		 * YEARLY MODE
		 */
		pnlYearlyMode = new JPanel(new TableLayout(
				new double[]{
						4.0, TableLayoutConstants.PREFERRED, TableLayoutConstants.PREFERRED, TableLayoutConstants.PREFERRED, TableLayoutConstants.PREFERRED, TableLayoutConstants.PREFERRED, 4.0},
				new double[]{
						4.0,
						TableLayoutConstants.PREFERRED, // for start time panel
						21.0,
						26.0,
						21.0,
						4.0}));
		
		final ButtonGroup bgYearlyMode = new ButtonGroup();
		radioYearly1.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				tfYearly1Day.setEnabled(true);
				comboYearly1Month.setEnabled(true);
				comboYearly2Number.setEnabled(false);
				comboYearly2Weekday.setEnabled(false);
				comboYearly2Month.setEnabled(false);
			}
		});
		radioYearly1.addActionListener(this);
		radioYearly2.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				tfYearly1Day.setEnabled(false);
				comboYearly1Month.setEnabled(false);
				comboYearly2Number.setEnabled(true);
				comboYearly2Weekday.setEnabled(true);
				comboYearly2Month.setEnabled(true);
			}
		});
		radioYearly2.addActionListener(this);
		
		bgYearlyMode.add(radioYearly1);
		bgYearlyMode.add(radioYearly2);
		
		tfYearly1Day.setColumns(3);
		tfYearly1Day.setInputVerifier(new TextFieldNumberVerifier(1, 31));
		
		comboYearly1Month.addActionListener(this);
		for (SeriesListItem sli : SeriesUtils.getMonthItems()){
			comboYearly1Month.addItem(sli);
		}
		
		comboYearly2Number.addActionListener(this);
		for (SeriesListItem sli : SeriesUtils.getNumberItems()){
			comboYearly2Number.addItem(sli);
		}
		
		comboYearly2Weekday.addActionListener(this);
		for (SeriesListItem sli : SeriesUtils.getWeekdayItems()){
			comboYearly2Weekday.addItem(sli);
		}
		
		comboYearly2Month.addActionListener(this);
		for (SeriesListItem sli : SeriesUtils.getMonthItems()){
			comboYearly2Month.addItem(sli);
		}
		
		pnlYearlyMode.add(radioYearly1, getConstraints(1, 1, 2, 2));
		pnlYearlyMode.add(radioYearly2, getConstraints(1, 1, 3, 3, TableLayoutConstants.LEFT, TableLayoutConstants.BOTTOM));
		
		pnlYearlyMode.add(tfYearly1Day, getConstraints(2, 2, 2, 2));
		pnlYearlyMode.add(new JLabel(" . "), getConstraints(3, 3, 2, 2));
		pnlYearlyMode.add(comboYearly1Month, getConstraints(4, 5, 2, 2));
		
		pnlYearlyMode.add(comboYearly2Number, getConstraints(2, 3, 3, 3, TableLayoutConstants.LEFT, TableLayoutConstants.BOTTOM));
		pnlYearlyMode.add(comboYearly2Weekday, getConstraints(4, 5, 3, 3, TableLayoutConstants.LEFT, TableLayoutConstants.BOTTOM));
		pnlYearlyMode.add(new JLabel("im "), getConstraints(2, 2, 4, 4));
		pnlYearlyMode.add(comboYearly2Month, getConstraints(3, 5, 4, 4));
		
		pnlHolder.add(pnlYearlyMode, constraintsRight);
		
		resetAllInputFieldsToDefault();
		
		/*
		 * add "Main" Panel
		 */
		this.add(pnlHolder, BorderLayout.CENTER);
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

	/**
	 * set new value in field
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		handleUpdateField();
	}
	
	/**
	 * class for verifier input of an <code>JTextField</code>
	 * - input does not be empty
	 * - input has to be an int
	 * - in range of [from] and [until], both including (optional)
	 * 
	 * if input is okay <code>handleUpdateField()</code> is called 
	 * otherwise nothing happens and focus still leaves in <code>JTextField</code>
	 * 
	 */
	private class TextFieldNumberVerifier extends InputVerifier {
		
		private int iFrom = 0;
		private int iUntil = 0;
		
		boolean isCheckRange = false;
		
		/**
		 * Verifier with range
		 * @param iFrom
		 * @param iUntil
		 */
		public TextFieldNumberVerifier(int iFrom, int iUntil){
			this.isCheckRange = true;
			this.iFrom = iFrom;
			this.iUntil = iUntil;
		}
		
		@Override
		public boolean verify(JComponent input) {
			if (input instanceof JTextField) {
				JTextField tf = (JTextField)input;	
				String text = tf.getText();
				
				if ("".equals(text) || text == null){
					JOptionPane.showMessageDialog(DateTimeSeries.this, "Bitte geben Sie einen Wert ein.");
					return false;
				}

				// Check for numbers only!
				for (char c : text.toCharArray()){
					if (
							c == '0' ||
							c == '1' ||
							c == '2' ||
							c == '3' ||
							c == '4' ||
							c == '5' ||
							c == '6' ||
							c == '7' ||
							c == '8' ||
							c == '9'
							){
						// do nothing
					} else {
						JOptionPane.showMessageDialog(DateTimeSeries.this, "Hier d\u00fcrfen nur Zahlen eingetragen werden.");
						return false;
					}
				}
				
				if (isCheckRange) {
					int iText = Integer.parseInt(text);
					if (iText < iFrom ||
							iText > iUntil){
						JOptionPane.showMessageDialog(DateTimeSeries.this, "Der Wert muss zwischen "+iFrom+" und "+iUntil+" liegen.");
						return false;
					}
				}
				
				return true;
			}
			
			return false;
		}
		
		/**
		 * if result is okay field would be updated
		 */
		@Override
		public boolean shouldYieldFocus(JComponent input) {
			boolean result = verify(input);
			if (result) {
				handleUpdateField();
			}
			return result;
		}

	}
	
	/**
	 * class for adding a DocumentListener while adding a Document 
	 * to handle the GUI update when new input arrives.
	 * <code>handleUpdateGUI()</code>
	 *
	 */
	private class DateTimeSeriesCommonJTextField extends CommonJTextField{

		@Override
		public void setDocument(Document doc) {
			doc.addDocumentListener(new DocumentListener(){

				@Override
				public void changedUpdate(DocumentEvent e) {
					handleUpdateGUI();
				}

				@Override
				public void insertUpdate(DocumentEvent e) {
					handleUpdateGUI();
				}

				@Override
				public void removeUpdate(DocumentEvent e) {
					handleUpdateGUI();
				}
				
			});
			super.setDocument(doc);
		}		
	}
	
	/**
	 * 
	 * @return true if at least one weekday is selected
	 */
	private boolean isAtLeastOneWeekdaySelected(){
		boolean result = false;
		if (checkMonday.isSelected())
			result = true;
		else if (checkTuesday.isSelected())
			result = true;
		else if (checkWednesday.isSelected())
			result = true;
		else if (checkThursday.isSelected())
			result = true;
		else if (checkFriday.isSelected())
			result = true;
		else if (checkSaturday.isSelected())
			result = true;
		else if (checkSunday.isSelected())
			result = true;
		return result;
	}
}
