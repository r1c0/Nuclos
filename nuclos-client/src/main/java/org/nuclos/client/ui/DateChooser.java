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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.Timer;
import javax.swing.ToolTipManager;
import javax.swing.text.Document;

import org.apache.log4j.Logger;
import org.nuclos.common2.SpringLocaleDelegate;
import org.nuclos.common2.DateUtils;
import org.nuclos.common2.RelativeDate;
import org.nuclos.common2.StringUtils;
import org.nuclos.common2.exception.CommonValidationException;

/**
 * A component that combines a text field and a popup calendar.
 * The user can select a date from the popup calendar, which appears at the
 * user's request. If you make the date chooser editable, then the date chooser
 * includes an editable field into which the user can type a value.
 *
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Boris.Sander@novabit.de">Boris Sander</a>
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph Radig</a>
 * @version 01.00.00
 *
 * @beaninfo
 *   attribute: isContainer false
 *   description: A combination of a text field and a popup calendar.
 */
public class DateChooser extends JComponent implements Serializable {

	private static final Logger log = Logger.getLogger(DateChooser.class);

	/**
	 * default number of columns
	 */
	public static final int DEFAULT_COLUMNCOUNT = "01.01.2000+bu".length();

	/**
	 * Flag which determines wether or not the text field is editable
	 *
	 * @see #isEditable
	 * @see #setEditable
	 */
	private boolean bEditable = true;
	private boolean bPopupOpen = false;

	/**
	 * The rate at which the user scrolls through the months on the popup
	 * calendar when the arrow button at the top of the popup calendar are
	 * held down.  Units are in months per second.
	 */
	private int iMonthScrollRate = 5;

	/**
	 * The date which the user has selected.  Return null of the user chose
	 * "NONE".
	 *
	 * @see #getDate
	 * @see #setDate
	 */
	private Date dateSelected;

	/**
	 * The format in which the selected date will appear in the text field.
	 */
	private DateFormat dateformatTextField = SpringLocaleDelegate.getInstance().getDateFormat(); 
		// DateFormat.getDateInstance(DateFormat.DEFAULT, SpringLocaleDelegate.getLocale());
		// SpringLocaleDelegate.getDateFormat(); 
		// (SimpleDateFormat) DateFormat.getDateInstance();

	/**
	 * Components for the control itself
	 */
	// the textfield that holds the date
	private TextFieldWithButton tfDate;
	// the button for popping up (or dropping down) the calendar.
	private JButton btnDropDown;

	/**
	 * configures the popup calendar to show the historical view (disabled today button, none button reads "Aktuell"
	 */
	private boolean bHistoricalState = false;

	/**
	 * the dialog for the calendar
	 */
	private PopupCalendar dlgCalendar = null;

	private GregorianCalendar selectedCalendar;
	private SimpleDateFormat dateformatMonthYear = new SimpleDateFormat("MMMM yyyy");

	private String sPattern;

	private JButton btnToday;
	private JButton btnNone;
	private javax.swing.Timer timerShiftTimePeriod;

	private ToolTipTextProvider tooltiptextproviderForTextField;

	private final boolean bTodayIsRelative;

	private String actionCommand;

	/**
	 * Creates a <code>DateChooser</code> with no initial date and with the default number of columns.
	 * @postcondition !isTodayRelative()
	 */
	public DateChooser() {
		this(false);

		assert !isTodayRelative();
	}

	/**
	 * Creates a <code>DateChooser</code> with the given initial date.
	 */
	public DateChooser(Date date) {
		this(date, DEFAULT_COLUMNCOUNT, false);
	}

	/**
	 * Creates a <code>DateChooser</code> with no initial date and with the default number of columns.
	 * @param bTodayIsRelative
	 * @postcondition isTodayRelative() == bTodayIsRelative
	 */
	public DateChooser(boolean bTodayIsRelative) {
		this(null, DEFAULT_COLUMNCOUNT, bTodayIsRelative);

		assert isTodayRelative() == bTodayIsRelative;
	}

	/**
	 * Creates a <code>DateChooser</code> with a specified initially
	 * selected date and a text field containing the specified number
	 * of columns.
	 *
	 * @param date the date which is initially selected and appears
	 * in the text field. May be null.
	 * @param iColumns the number of columns in the text field.
	 * @param bTodayIsRelative
	 * @postcondition isTodayRelative() == bTodayIsRelative
	 */
	public DateChooser(Date date, int iColumns, boolean bTodayIsRelative) {
		this.bTodayIsRelative = bTodayIsRelative;

		this.setupControl(iColumns);

		if (date != null) {
			this.initializeDate(date);
		}

		if (iColumns != 0) {
			this.setColumns(iColumns);
		}

		super.addFocusListener(new DateChooserFocusListener());

		assert isTodayRelative() == bTodayIsRelative;
	}

	public void setOutputFormat(String sPattern) {
		this.dateformatTextField = new SimpleDateFormat(sPattern);
		this.sPattern = sPattern;
	}

	public String getPattern() {
		return this.sPattern;
	}

	public void addActionListener(ActionListener listener) {
		listenerList.add(ActionListener.class, listener);
	}

	public void removeActionListener(ActionListener listener) {
		listenerList.remove(ActionListener.class, listener);
	}

	public ActionListener[] getActionListeners() {
		return listenerList.getListeners(ActionListener.class);
	}

	/**
	 * @return
	 * 	true: today button means "today" as a relative term (useful in search conditions)
	 * 	false: today button means the respective current date as an absolute term (used when storing a date)
	 */
	public boolean isTodayRelative() {
		return this.bTodayIsRelative;
	}

	public boolean isHistoricalState() {
		return this.bHistoricalState;
	}

	public void setHistoricalState(boolean bHistoricalState) {
		this.bHistoricalState = bHistoricalState;
	}

	@Override
	public boolean hasFocus() {
		return this.tfDate.hasFocus();
	}

	private void showCalendar(Point pOrigin, Dimension dimButtonSize, Dimension dimScreenSize, String sDate) {
		assert(dlgCalendar == null);

		bPopupOpen = true;

		/** @todo write factory method for JDialog */
		final Container containerTopLevelAncestor = this.getTopLevelAncestor();
		if (containerTopLevelAncestor instanceof Dialog) {
			dlgCalendar = new PopupCalendar((Dialog) containerTopLevelAncestor, pOrigin, dimButtonSize, dimScreenSize, sDate);
		}
		else {
			dlgCalendar = new PopupCalendar((Frame) containerTopLevelAncestor, pOrigin, dimButtonSize, dimScreenSize, sDate);
		}
		dlgCalendar.updateCalendarDisplay();
		dlgCalendar.setVisible(true);
	}

	private void disposeCalendar() {
		if (dlgCalendar != null) dlgCalendar.dispose();
	}

	public JButton getBrowseButton() {
		return btnDropDown;
	}

	private JButton newArrowButton() {
		JButton result = new JButton(new ImageIcon(this.getClass().getResource("/org/nuclos/client/lookandfeel/images/arrowDown.png")));
		result.setPreferredSize(new Dimension(20, 21));
		return result;
	}

	private void setupControl(int iColumns) {
		this.setLayout(new BorderLayout(2, 0));
		this.tfDate = new TextFieldWithButton(Icons.getInstance().getIconTextFieldButtonCalendar()) {

			/**
			 * shows the dynamic tooltip text, if a tooltiptextprovider was set.
			 * Otherwise, shows the static tooltip text, if any.
			 * This method is needed for displaying the tooltip in a regular component or in a <code>TableCellEditor</code>.
			 * @param ev
			 * @return tooltip text
			 */
			@Override
			public String getToolTipText(MouseEvent ev) {
				final ToolTipTextProvider provider = DateChooser.this.getToolTipTextProvider();
				return (provider != null) ? provider.getDynamicToolTipText() : super.getToolTipText(ev);
			}

			@Override
			public boolean isButtonEnabled() {
				return DateChooser.this.btnDropDown.isEnabled();
			}

			@Override
			public void buttonClicked() {
				DateChooser.this.btnDropDown.doClick();
			}
		};
		this.tfDate.setColumns(iColumns);

		final JPanel pnlHolder = new JPanel(new BorderLayout(2, 0));
		this.tfDate.setColumnWidthChar('0');
		pnlHolder.add(this.tfDate, BorderLayout.CENTER);
		this.btnDropDown = newArrowButton();
		final AbstractAction actOpenCalendar = new AbstractAction() {

			@Override
            public void actionPerformed(ActionEvent ev) {
				btnDropDown.setSelected(bPopupOpen);
				if (bPopupOpen || dlgCalendar != null) {
					disposeCalendar();
				}
				else {
					final Point pOrigin = tfDate.getLocationOnScreen();
					final Dimension dimScreenSize = UIUtils.getFrameForComponent(tfDate).getSize();
					final Dimension dimButtonSize = tfDate.getSize();

					showCalendar(pOrigin, dimButtonSize, dimScreenSize, tfDate.getText());
				}
			}
		};
		this.btnDropDown.addActionListener(actOpenCalendar);
		this.btnDropDown.setFocusPainted(false);
		//pnlHolder.add(btnDropDown, BorderLayout.EAST);

		this.add(pnlHolder, BorderLayout.NORTH);

		final String sKeyAltDown = "AltDown";
		this.tfDate.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, KeyEvent.ALT_MASK), sKeyAltDown);
		this.tfDate.getActionMap().put(sKeyAltDown, actOpenCalendar);
		this.tfDate.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				fireDateChanged();
			}
		});

		this.tfDate.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				// TODO Auto-generated method stub
				super.focusLost(e);
			}
		});
	}

	/**
	 * @param tooltiptextprovider may be <code>null</code>, to enable static tooltip text.
	 */
	public void setToolTipTextProvider(ToolTipTextProvider tooltiptextprovider) {
		this.tooltiptextproviderForTextField = tooltiptextprovider;
		if (tooltiptextprovider != null) {
			ToolTipManager.sharedInstance().registerComponent(this.getJTextField());
		}
	}

	/**
	 * @return the <code>ToolTipTextProvider</code> for this component, if any.
	 */
	public ToolTipTextProvider getToolTipTextProvider() {
		return this.tooltiptextproviderForTextField;
	}

	/**
	 * sets the (static) tooltip text for this component. The tooltip text is shown in the text field.
	 * @param sText
	 */
	@Override
	public void setToolTipText(String sText) {
		// don't show a tooltip for the component itself, so don't call super.setToolTipText

		// show the tooltip in the textfield:
		this.getJTextField().setToolTipText(sText);

		// don't show a tooltip in the button
	}

	private void initializeDate(Date date) {
		dateSelected = date;

		if (selectedCalendar != null) {
			selectedCalendar.setTime(dateSelected);
		}
		setDateText(dateformatTextField.format(dateSelected), false);
	}

	private void setDateText(String text, boolean fireChange) {
		if (!StringUtils.emptyIfNull(text).equals(tfDate.getText())) {
			tfDate.setText(text);
			if (fireChange)
				fireDateChanged();
		}
	}

	public void setColumns(int iColumns) {
		tfDate.setColumns(iColumns);
	}

	public Date getDate() throws CommonValidationException {
		return getDateFromText();
	}

	/**
	 * get a date from the Text Field
	 */
	private Date getDateFromText() throws CommonValidationException {
		Date dateFromText = null;
		if (this.getText() != null && !this.getText().equals("")) {
			try {
				String date = this.getText();
				if (date.equalsIgnoreCase(SpringLocaleDelegate.getInstance().getMessage("datechooser.today.label", "Heute"))) {
					date = DateUtils.toString(DateUtils.today());
				}
				dateFromText = SpringLocaleDelegate.getInstance().parseDate(date);
			}
			catch (ParseException e) {
				throw new CommonValidationException(StringUtils.getParameterizedExceptionMessage("datechooser.exception", getText()), e);//"Ung\u00fcltiges Datumsformat: \"" + getText() + "\"", e);
			}
		}
		return dateFromText;
	}

	/**
	 * @return the text from the text field
	 */
	public String getText() {
		return tfDate.getText();
	}

	public Document getDocument() {
		return tfDate.getDocument();
	}

	/**
	 * Use with caution. Only use for reading or adding listeners. Don't write to this text field.
	 * @return JtextField
	 */
	public JTextField getJTextField() {
		return this.tfDate;
	}

	/**
	 * @return Is the text field editable? (true by default)
	 */
	public boolean isEditable() {
		return this.bEditable;
	}

	/**
	 * @param bEditable Is the text field to be editable?
	 */
	public void setEditable(boolean bEditable) {
		this.bEditable = bEditable;
		tfDate.setEditable(bEditable && this.isEnabled());
		btnDropDown.setEnabled(bEditable && this.isEnabled());
	}

	@Override
	public void setEnabled(boolean bEnabled) {
		super.setEnabled(bEnabled);

		// the text field is never disabled so the contents can be copied.
		tfDate.setEditable(bEnabled && this.isEditable());

		btnDropDown.setEnabled(bEnabled && this.isEditable());
	}

	public void setDate(Date date) {
		if (date == null) {
			dateSelected = null;
			setDateText(null, false);
		} else {
			String specialText = null;
			if (date == RelativeDate.today()) {
				if (isTodayRelative()) {
					/** @todo refactor with special DateFormat */
					specialText = SpringLocaleDelegate.getInstance().getMessage("datechooser.today.label", "Heute");
				} else {
					// since isTodayRelative()=false, "today" should be treated as static date value
					date = DateUtils.today();
				}
			}
			setDateInternal(date);
			setDateText(specialText != null ? specialText : dateformatTextField.format(date), false);
		}
	}

	private void setDateInternal(Date date) {
		dateSelected = date;
		if (selectedCalendar != null) {
			selectedCalendar.setTime(dateSelected);
		}
	}

	private void fireDateChanged() {
		ActionEvent event = null;
		for (ActionListener listener : getActionListeners()) {
			if (event == null)
				event = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, getActionCommand());
			listener.actionPerformed(event);
		}
	}


//	public void setDateFormat(SimpleDateFormat dateformat) {
//		dateformatTextField = dateformat;
//	}

//	public void setDateFormatPattern(String sPattern) {
//		dateformatTextField.applyPattern(sPattern);
//	}

	@Override
	public boolean requestFocusInWindow() {
		return this.tfDate.requestFocusInWindow();
	}

	@Override
	public void setName(String sName) {
		super.setName(sName);
		UIUtils.setCombinedName(this.tfDate, sName, "tfDate");
		UIUtils.setCombinedName(this.btnDropDown, sName, "btnDropDown");
	}

	public void setBackgroundColorProviderForTextField(ColorProvider colorproviderBackground) {
		this.tfDate.setBackgroundColorProviderForTextField(colorproviderBackground);
	}

	public String getActionCommand() {
		return actionCommand;
	}

	public void setActionCommand(String actionCommand) {
		this.actionCommand = actionCommand;
	}

	class DateChooserFocusListener implements FocusListener {
		@Override
		public void focusGained(FocusEvent e) {
			DateChooser.this.tfDate.requestFocus();
		}
		@Override
		public void focusLost(FocusEvent e) {
		}
	}

	/**
	 * Inner class representing the calendar popup window
	 */
	private class PopupCalendar extends JDialog {

		private JLabel[] alabDay = null;
		private GregorianCalendar todaysCalendar;
		private JLabel labMonth;

		private JButton btnPrevious;
		private JButton btnNext;

		private JLabel lblActive = null;
		private JLabel lblToday = null;

		private final MyMouseListener mouselistener = new MyMouseListener();

		private Map<JLabel, java.util.Date> mpDays = null;

		/**
		 * the main panel of the calendar
		 */
		private final JPanel pnlMain = new JPanel(new BorderLayout());

		private static final int DISPLAYED_WEEKS_PER_MONTH = 6;
		private static final int DAYS_PER_WEEK = 7;
		private static final int DISPLAYED_DAYS_PER_MONTH = DISPLAYED_WEEKS_PER_MONTH * DAYS_PER_WEEK;

		PopupCalendar(Dialog owner, Point pOrigin, Dimension dimButtonSize, Dimension dimScreenSize, String sDate) {
			super(owner);
			initPopupCalendar(pOrigin, dimButtonSize, dimScreenSize, sDate);
		}

		PopupCalendar(Frame owner, Point pOrigin, Dimension dimButtonSize, Dimension dimScreenSize, String sDate) {
			super(owner);
			initPopupCalendar(pOrigin, dimButtonSize, dimScreenSize, sDate);
		}

		/**
		 * Initialize the freshly popped-up calendar and create its components.
		 * @param pOrigin
		 * @param dimButtonSize
		 * @param dimScreenSize
		 * @param sDate
		 */
		private void initPopupCalendar(Point pOrigin, Dimension dimButtonSize, Dimension dimScreenSize, String sDate) {
			final SimpleDateFormat dateformat = new SimpleDateFormat("dd.MM.yy");

			todaysCalendar = new GregorianCalendar();
			todaysCalendar.setTime(new Date());

			dateSelected = null;
			selectedCalendar = new GregorianCalendar();

			if (sDate != null && !sDate.equals("")) {
				try {
					dateSelected = dateformat.parse(sDate);
					selectedCalendar.setTime(dateSelected);
				}
				catch (ParseException ex) {
					/** @todo !!! */
				}
			}
			else {
				dateSelected = todaysCalendar.getTime();
			}

			mpDays = new HashMap<JLabel, java.util.Date>();
			alabDay = new JLabel[DISPLAYED_DAYS_PER_MONTH];

			final JPanel pnlMonth = createMonthPanel();

			final JPanel pnlBottom = createBottomPanel(createButtonsPanel());

			pnlMain.add(pnlMonth, BorderLayout.NORTH);
			pnlMain.add(new CommonJSeparator(), BorderLayout.CENTER);
			pnlMain.add(pnlBottom, BorderLayout.SOUTH);
			pnlMain.setBorder(BorderFactory.createEtchedBorder());

			btnPrevious.setFocusable(false);
			btnNext.setFocusable(false);
			btnToday.setFocusable(false);
			btnNone.setFocusable(false);

			pnlMain.setFocusable(true);
			pnlMain.requestFocus();

			setupKeyboardActions();

			setUndecorated(true);
			setModal(false);
			getRootPane().setWindowDecorationStyle(JRootPane.FRAME);
			setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

			addWindowListener(new WindowAdapter() {
				@Override
				public void windowDeactivated(WindowEvent ev) {
					disposeCalendar();
				}

				@Override
				public void windowClosed(WindowEvent ev) {
					log.debug("DateChooser.windowClosed");
					dlgCalendar = null;
				}
			});

			getContentPane().add(pnlMain, BorderLayout.CENTER);
			pack();

			final Dimension dimMySize = getSize();
			pOrigin.translate(dimButtonSize.width - dimMySize.width, dimButtonSize.height);
			if (pOrigin.y + dimMySize.height > dimScreenSize.height) {
				pOrigin.translate(0, -(dimMySize.height + dimButtonSize.height));
			}
			setLocation(pOrigin);
		}

		private void setupKeyboardActions() {
			final String sKeyCTRLRight = "CTRLRight";
			pnlMain.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, KeyEvent.CTRL_MASK), sKeyCTRLRight);
			pnlMain.getActionMap().put(sKeyCTRLRight, new AbstractAction() {

				@Override
                public void actionPerformed(ActionEvent e) {
					shiftTimePeriod(Calendar.YEAR, 1);
				}
			});
			final String sKeyALTRight = "ALTRight";
			pnlMain.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, KeyEvent.ALT_MASK), sKeyALTRight);
			pnlMain.getActionMap().put(sKeyALTRight, new AbstractAction() {

				@Override
                public void actionPerformed(ActionEvent e) {
					shiftTimePeriod(Calendar.MONTH, 1);
				}
			});
			final String sKeyRight = "Right";
			pnlMain.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), sKeyRight);
			pnlMain.getActionMap().put(sKeyRight, new AbstractAction() {

				@Override
                public void actionPerformed(ActionEvent e) {
					shiftTimePeriod(Calendar.DAY_OF_MONTH, +1);
				}
			});
			final String sKeyCTRLLeft = "CTRLLeft";
			pnlMain.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, KeyEvent.CTRL_MASK), sKeyCTRLLeft);
			pnlMain.getActionMap().put(sKeyCTRLLeft, new AbstractAction() {

				@Override
                public void actionPerformed(ActionEvent e) {
					shiftTimePeriod(Calendar.YEAR, -1);
				}
			});
			final String sKeyALTLeft = "ALTLeft";
			pnlMain.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, KeyEvent.ALT_MASK), sKeyALTLeft);
			pnlMain.getActionMap().put(sKeyALTLeft, new AbstractAction() {

				@Override
                public void actionPerformed(ActionEvent e) {
					shiftTimePeriod(Calendar.MONTH, -1);
				}
			});
			final String sKeyLeft = "Left";
			pnlMain.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), sKeyLeft);
			pnlMain.getActionMap().put(sKeyLeft, new AbstractAction() {

				@Override
                public void actionPerformed(ActionEvent e) {
					shiftTimePeriod(Calendar.DAY_OF_MONTH, -1);
				}
			});
			final String sKeyUp = "Up";
			pnlMain.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), sKeyUp);
			pnlMain.getActionMap().put(sKeyUp, new AbstractAction() {

				@Override
                public void actionPerformed(ActionEvent e) {
					shiftTimePeriod(Calendar.DAY_OF_MONTH, -7);
				}
			});
			final String sKeyDown = "Down";
			pnlMain.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), sKeyDown);
			pnlMain.getActionMap().put(sKeyDown, new AbstractAction() {

				@Override
                public void actionPerformed(ActionEvent e) {
					shiftTimePeriod(Calendar.DAY_OF_MONTH, 7);
				}
			});
			final String sKeyEnter = "Enter";
			pnlMain.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), sKeyEnter);
			pnlMain.getActionMap().put(sKeyEnter, new AbstractAction() {

				@Override
                public void actionPerformed(ActionEvent e) {
					shiftTimePeriod(Calendar.DAY_OF_MONTH, 0); // prevent nullpointer...
					setDateText(dateformatTextField.format(dateSelected), true);
					disposeCalendar();
				}
			});
			final String sKeyEscape = "Escape";
			pnlMain.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), sKeyEscape);
			pnlMain.getActionMap().put(sKeyEscape, new AbstractAction() {

				@Override
                public void actionPerformed(ActionEvent e) {
					disposeCalendar();
				}
			});
		}

		private void shiftTimePeriod(int iDelta, int iAmount) {
			selectedCalendar.add(iDelta, iAmount);
			dateSelected = selectedCalendar.getTime();
			updateCalendarDisplay();
		}

		private JPanel createBottomPanel(final JPanel pnlButtons) {
			final JPanel pnlDays = createDaysPanel();

			final JPanel result = new JPanel(new GridBagLayout());
			result.setBackground(Color.white);
			result.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 15));
			final GridBagConstraints gbcBottomPanel = new GridBagConstraints();
			buildConstraints(gbcBottomPanel, 0, 0, 1, 1, 1, 0);
			gbcBottomPanel.fill = GridBagConstraints.HORIZONTAL;
			gbcBottomPanel.anchor = GridBagConstraints.CENTER;
			result.add(newDayHeaderPanel(), gbcBottomPanel);

			buildConstraints(gbcBottomPanel, 0, 1, 1, 1, 1, 0);
			gbcBottomPanel.fill = GridBagConstraints.HORIZONTAL;
			gbcBottomPanel.anchor = GridBagConstraints.CENTER;
			result.add(pnlDays, gbcBottomPanel);

			buildConstraints(gbcBottomPanel, 0, 2, 1, 1, 0, 0);
			gbcBottomPanel.fill = GridBagConstraints.BOTH;
			result.add(pnlButtons, gbcBottomPanel);

			return result;
		}

		private void buildConstraints(GridBagConstraints gbc, int gx, int gy, int gw, int gh, int wx, int wy) {
			gbc.gridx = gx;
			gbc.gridy = gy;
			gbc.gridwidth = gw;
			gbc.gridheight = gh;
			gbc.weightx = wx;
			gbc.weighty = wy;
		}

		private JPanel createButtonsPanel() {
			final JPanel pnlButtons = new JPanel(new GridLayout(1, 2, 6, 6));
			pnlButtons.setBackground(Color.white);
			pnlButtons.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
			final SpringLocaleDelegate localeDelegate = SpringLocaleDelegate.getInstance();
			btnToday = new JButton(localeDelegate.getMessage("datechooser.today.label", "Heute"));
			if (bHistoricalState) {
				btnToday.setEnabled(false);
			}
			btnToday.setMnemonic('H');
			btnToday.setMargin(new Insets(2, 2, 2, 2));
			btnToday.setFocusPainted(false);
			btnToday.addActionListener(new ActionListener() {
				@Override
                public void actionPerformed(ActionEvent ev) {
					if (DateChooser.this.isTodayRelative()) {
						dateSelected = RelativeDate.today();
						selectedCalendar.setTime(dateSelected);
						setDateText(localeDelegate.getMessage("datechooser.today.label", "Heute"), true);
					}
					else {
						dateSelected = todaysCalendar.getTime();
						selectedCalendar.setTime(dateSelected);
						setDateText(dateformatTextField.format(dateSelected), true);
					}
					disposeCalendar();
				}
			});
			pnlButtons.add(btnToday);

			btnNone = new JButton();
			if (bHistoricalState) {
				btnNone.setText(localeDelegate.getMessage("datechooser.actual.label", "Aktuell"));
				btnNone.setMnemonic('A');
			}
			else {
				btnNone.setText(localeDelegate.getMessage("datechooser.empty.label", "Leer"));
				btnNone.setMnemonic('L');
			}

			btnNone.setMargin(new Insets(2, 2, 2, 2));
			btnNone.setFocusPainted(false);
			btnNone.addActionListener(new ActionListener() {
				@Override
                public void actionPerformed(ActionEvent ev) {
					dateSelected = null;
					setDateText(null, true);
					disposeCalendar();
				}
			});
			pnlButtons.add(btnNone);
			return pnlButtons;
		}

		private JPanel createDaysPanel() {
			final JPanel result = new JPanel(new GridLayout(6, 7, 2, 2));
			result.setBackground(Color.white);
			result.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, Color.black));

			for (int i = 0; i < alabDay.length; i++) {
				alabDay[i] = new JLabel("00", JLabel.RIGHT);
				alabDay[i].setOpaque(true);
				alabDay[i].setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
				alabDay[i].setBackground(Color.white);
				alabDay[i].addMouseListener(mouselistener);
				result.add(alabDay[i]);
			}

			return result;
		}

		@Override
		public void dispose() {
			bPopupOpen = false;

			for (int i = 0; i < alabDay.length; i++) {
				alabDay[i].removeMouseListener(mouselistener);
			}
			btnNext.removeMouseListener(mouselistener);
			btnPrevious.removeMouseListener(mouselistener);
			mpDays.clear();

			super.dispose();
		}

		private JPanel newDayHeaderPanel() {
			final JPanel result = new JPanel(new GridLayout(1, 7, 2, 2));
			result.setBackground(Color.white);

			for (String sDayOfWeek : Arrays.asList("S", "M", "D", "M", "D", "F", "S")) {
				final JLabel labDayOfWeek = new JLabel(sDayOfWeek, JLabel.RIGHT);
				labDayOfWeek.setForeground(Color.black);
				labDayOfWeek.setBackground(Color.white);
				result.add(labDayOfWeek);
			}

			return result;
		}

		private JPanel createMonthPanel() {
			final JPanel result = new JPanel(new BorderLayout());

			final String sTooltipMonthScroll = SpringLocaleDelegate.getInstance().getMessage(
					"datechooser.tooltip", "Dr\u00fccken Sie STRG, um jahrweise zu bl\u00e4ttern.");

			final Icon iconLeft = Icons.getInstance().getIconPrevious16();
			btnPrevious = new JButton(iconLeft);
			btnPrevious.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
			btnPrevious.setPressedIcon(btnPrevious.getIcon());
			btnPrevious.setFocusPainted(false);
			btnPrevious.setToolTipText(sTooltipMonthScroll);
			btnPrevious.addMouseListener(mouselistener);
			result.add(btnPrevious, BorderLayout.WEST);

			labMonth = new JLabel("SEPTEMBER 8888", JLabel.CENTER);
			labMonth.setBorder(null);
			result.add(labMonth, BorderLayout.CENTER);

			final Icon iconNext = Icons.getInstance().getIconNext16();
			btnNext = new JButton(iconNext);
			btnNext.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
			btnNext.setPressedIcon(btnNext.getIcon());
			btnNext.setFocusPainted(false);
			btnNext.setToolTipText(sTooltipMonthScroll);
			btnNext.addMouseListener(mouselistener);
			result.add(btnNext, BorderLayout.EAST);

			return result;
		}

		private void updateCalendarDisplay() {
			if (dateSelected != null) {
				selectedCalendar.setTime(dateSelected);
			}

			final GregorianCalendar calendarDisplayed = new GregorianCalendar();
			calendarDisplayed.setTime(selectedCalendar.getTime());
			labMonth.setText(dateformatMonthYear.format(calendarDisplayed.getTime()));

			calendarDisplayed.set(Calendar.DAY_OF_MONTH, 1);
			final int firstWeekDayOfDisplayedMonth = calendarDisplayed.get(Calendar.DAY_OF_WEEK);
			final int daysInDisplayedMonth = calendarDisplayed.getActualMaximum(Calendar.DAY_OF_MONTH);

			calendarDisplayed.add(Calendar.MONTH, -1);
			final int daysInPrevDisplayedMonth = calendarDisplayed.getActualMaximum(Calendar.DAY_OF_MONTH);
			calendarDisplayed.set(Calendar.DAY_OF_MONTH, daysInPrevDisplayedMonth);

			final int iTodaysYear = todaysCalendar.get(Calendar.YEAR);
			final int iTodaysMonth = todaysCalendar.get(Calendar.MONTH);
			final int iTodaysDay = todaysCalendar.get(Calendar.DAY_OF_MONTH);

			final int iSelectedDay = selectedCalendar.get(Calendar.DAY_OF_MONTH);

			mpDays.clear();

			// remove eventually set "Today" frame
			if (lblToday != null) {
				lblToday.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
				lblToday = null;
			}

			// configure the labels of the days before the current month
			if (firstWeekDayOfDisplayedMonth > 1) {	// means: if the first of the current month is not a sunday...
				for (int i = (firstWeekDayOfDisplayedMonth - 2); i > (-1); i--) {
					mpDays.put(alabDay[i], calendarDisplayed.getTime());

					alabDay[i].setText(String.valueOf(calendarDisplayed.get(Calendar.DAY_OF_MONTH)));
					alabDay[i].setForeground(Color.lightGray);

					calendarDisplayed.add(Calendar.DAY_OF_MONTH, -1);
				}
			}

			calendarDisplayed.add(Calendar.MONTH, 1);
			calendarDisplayed.set(Calendar.DAY_OF_MONTH, 1);

			// configure the labels of the days of the current month
			final int thisYear = calendarDisplayed.get(Calendar.YEAR);
			final int thisMonth = calendarDisplayed.get(Calendar.MONTH);
			for (int i = firstWeekDayOfDisplayedMonth - 1; i < firstWeekDayOfDisplayedMonth + daysInDisplayedMonth - 1; i++) {
				mpDays.put(alabDay[i], calendarDisplayed.getTime());
				final int thisDay = calendarDisplayed.get(Calendar.DAY_OF_MONTH);

				alabDay[i].setText(String.valueOf(thisDay));
				alabDay[i].setForeground(Color.black);

				if (thisYear == iTodaysYear && thisMonth == iTodaysMonth && thisDay == iTodaysDay) {
					lblToday = alabDay[i];
				}
				calendarDisplayed.add(Calendar.DAY_OF_MONTH, 1);
			}

			// configure the labels of the days after the current month
			for (int i = (firstWeekDayOfDisplayedMonth + daysInDisplayedMonth - 1); i < alabDay.length; i++) {
				mpDays.put(alabDay[i], calendarDisplayed.getTime());

				alabDay[i].setText(String.valueOf(calendarDisplayed.get(Calendar.DAY_OF_MONTH)));
				alabDay[i].setForeground(Color.lightGray);

				calendarDisplayed.add(Calendar.DAY_OF_MONTH, 1);
			}

			// Highlight the selected day
			makeLabelActive(alabDay[firstWeekDayOfDisplayedMonth + iSelectedDay - 2]);

			// Create eventually "Today" frame
			if (lblToday != null) {
				lblToday.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.black));
			}
		}

		// Move the "Active" highlighting
		private void makeLabelActive(JLabel lbl) {
			if (lblActive != null) {
				lblActive.setBackground(Color.white);

			}
			lbl.setBackground(Color.lightGray);
			lblActive = lbl;
		}

		/**
		 * Special purpose mouse listener; contains logic for selection of days, months and years on the calendar popup
		 */
		private class MyMouseListener extends MouseAdapter {
			@Override
			public void mousePressed(MouseEvent ev) {
				final Object source = ev.getSource();
				if (mpDays.containsKey(source)) {
					makeLabelActive((JLabel) source);
				}
				else if (source == btnPrevious || source == btnNext) {
					final int iOffset = (source == btnPrevious) ? -1 : +1;
					final int iTimePeriod = ev.isControlDown() ? Calendar.YEAR : Calendar.MONTH;

					dlgCalendar.shiftTimePeriod(iTimePeriod, iOffset);

					timerShiftTimePeriod = new Timer(1000 / iMonthScrollRate, new ShiftTimePeriodActionListener(iTimePeriod, iOffset));
					timerShiftTimePeriod.setInitialDelay(1000);
					timerShiftTimePeriod.start();
				}
			}

			@Override
			public void mouseReleased(MouseEvent ev) {
				final Object source = ev.getSource();
				if (mpDays.containsKey(source)) {
					dateSelected = mpDays.get(source);
					selectedCalendar.setTime(dateSelected);
					setDateText(dateformatTextField.format(dateSelected), true);
					disposeCalendar();
				}
				destroyTimer(ev);
			}

			@Override
			public void mouseExited(MouseEvent ev) {
				destroyTimer(ev);
			}

			private void destroyTimer(MouseEvent ev) {
				final Object source = ev.getSource();
				if (source == btnPrevious || source == btnNext) {
					if (timerShiftTimePeriod != null) {
						timerShiftTimePeriod.stop();
						timerShiftTimePeriod = null;
					}
				}
			}

		}	// inner class MyMouseListener

		private class ShiftTimePeriodActionListener implements ActionListener {
			private final int iTimePeriod;
			private final int iOffset;

			/**
			 * @param iOffset the number of months or years to shift the calendar. Should be 1 or -1.
			 */
			ShiftTimePeriodActionListener(int iTimePeriod, int iOffset) {
				this.iTimePeriod = iTimePeriod;
				this.iOffset = iOffset;
			}

			@Override
            public void actionPerformed(ActionEvent ev) {
				dlgCalendar.shiftTimePeriod(iTimePeriod, iOffset);
			}
		} // inner class ShiftTimePeriodActionListener

	} // inner class PopupCalendar

}	// class DateChooser
