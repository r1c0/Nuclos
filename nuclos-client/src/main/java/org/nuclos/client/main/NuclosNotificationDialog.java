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
package org.nuclos.client.main;



import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.table.DefaultTableModel;

import org.nuclos.client.common.ClientParameterProvider;
import org.nuclos.client.common.NuclosCollectControllerFactory;
import org.nuclos.client.genericobject.CollectableGenericObjectWithDependants;
import org.nuclos.client.genericobject.GenericObjectCollectController;
import org.nuclos.client.genericobject.GenericObjectDelegate;
import org.nuclos.client.ui.popupmenu.DefaultJPopupMenuListener;
import org.nuclos.common.ParameterProvider;
import org.nuclos.common.RuleNotification;
import org.nuclos.common2.SpringLocaleDelegate;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.server.genericobject.valueobject.GenericObjectVO;

/**
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:florian.speidel@novabit.de">Florian.Speidel</a>
 * @author	<a href="mailto:uwe.allner@novabit.de">Uwe.Allner</a>
 * @version 01.00.00
 */
public class NuclosNotificationDialog extends JDialog {

	private static final String COL_PRIORITY = SpringLocaleDelegate.getInstance().getMessage("NuclosNotificationDialog.6","Priorit\u00e4t");
	private static final String COL_MSGTEXT = SpringLocaleDelegate.getInstance().getMessage("NuclosNotificationDialog.2","Meldung");
	private static final String COL_OWNER = SpringLocaleDelegate.getInstance().getMessage("NuclosNotificationDialog.9","Versendet von");
	private static final String COL_TIMESTAMP = SpringLocaleDelegate.getInstance().getMessage("NuclosNotificationDialog.1","Datum / Uhrzeit");
	private static final String COL_OBJSOURCE = SpringLocaleDelegate.getInstance().getMessage("NuclosNotificationDialog.5","Objektbezeichner");
	private static final String COL_OBJDEST = SpringLocaleDelegate.getInstance().getMessage("NuclosNotificationDialog.12","Zielobjektbezeichner");
	private static final String COL_SOURCEID = "sourceID";
	private static final String COL_TARGETID = "targetID";

	private int sourceIDColumn;
	private int targetIDColumn;

	private final JPanel pnlDetails;
	private final JTextField tfPriority = new JTextField();
	private final JTextField tfMessageSource = new JTextField();
	private final JTextField tfObjectIdentifier = new JTextField();
	private final JTextField tfDestinationObject = new JTextField();
	private final JTextField tfTimestamp = new JTextField();
	private final JTextArea taMessageText = new JTextArea();

	private final JTable tbl;

	private final String[] asHeaders = new String[] {
			COL_PRIORITY, COL_MSGTEXT, COL_OWNER, COL_TIMESTAMP, COL_OBJSOURCE, COL_OBJDEST, COL_SOURCEID, COL_TARGETID
	};

	private final DefaultTableModel tblmodel = new DefaultTableModel(asHeaders, 0) {

		@Override
		public boolean isCellEditable(int iRow, int iColumn) {
			return false;
		}
	};

	NuclosNotificationDialog(Frame frmParent) {
		super(frmParent, SpringLocaleDelegate.getInstance().getMessage("NuclosNotificationDialog.3","Meldungen"));

		this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

		tbl = new JTable(tblmodel);
		tbl.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tbl.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
            public void valueChanged(ListSelectionEvent e) {
				fillDetailsPanel();
			}
		});

		sourceIDColumn = tbl.getColumn(COL_SOURCEID).getModelIndex();
		targetIDColumn = tbl.getColumn(COL_TARGETID).getModelIndex();
		tbl.removeColumn(tbl.getColumn(COL_SOURCEID));
		tbl.removeColumn(tbl.getColumn(COL_TARGETID));
		tbl.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		setupPopupMenu();

		this.pnlDetails = createDetailsPanel();

		final JPanel pnlButtons = new JPanel();
		final JButton btnClear = new JButton(SpringLocaleDelegate.getInstance().getMessage("NuclosNotificationDialog.13","Zur\u00fccksetzen"));
		btnClear.setToolTipText(SpringLocaleDelegate.getInstance().getMessage("NuclosNotificationDialog.4","Meldungen aus der Liste entfernen"));
		btnClear.addActionListener(new ActionListener() {
			@Override
            public void actionPerformed(ActionEvent ev) {
				while (tblmodel.getRowCount() != 0) {
					tblmodel.removeRow(0);
				}
			}
		});
		pnlButtons.add(btnClear);

		final JPanel pnlMain = new JPanel(new BorderLayout());
		final JScrollPane scrlpn = new JScrollPane(tbl);
		scrlpn.getViewport().setBackground(tbl.getBackground());
		scrlpn.setPreferredSize(new Dimension(460, 200));

		pnlMain.add(pnlDetails, BorderLayout.NORTH);
		pnlMain.add(scrlpn, BorderLayout.CENTER);
		pnlMain.add(pnlButtons, BorderLayout.SOUTH);
		this.setContentPane(pnlMain);

		this.pack();
		this.setLocationRelativeTo(frmParent); //center on screen for first use in this session
	}

	private void fillDetailsPanel() {
		int iRow = tbl.getSelectedRow();
		if (iRow > -1) {
			tfDestinationObject.setText((String) tbl.getModel().getValueAt(iRow, tbl.getColumn(COL_OBJDEST).getModelIndex()));
			tfMessageSource.setText((String) tbl.getModel().getValueAt(iRow, tbl.getColumn(COL_OWNER).getModelIndex()));
			tfObjectIdentifier.setText((String) tbl.getModel().getValueAt(iRow, tbl.getColumn(COL_OBJSOURCE).getModelIndex()));
			tfPriority.setText((String) tbl.getModel().getValueAt(iRow, tbl.getColumn(COL_PRIORITY).getModelIndex()));
			tfTimestamp.setText((String) tbl.getModel().getValueAt(iRow, tbl.getColumn(COL_TIMESTAMP).getModelIndex()));
			taMessageText.setText((String) tbl.getModel().getValueAt(iRow, tbl.getColumn(COL_MSGTEXT).getModelIndex()));
		}
		else {
			tfDestinationObject.setText("");
			tfMessageSource.setText("");
			tfObjectIdentifier.setText("");
			tfPriority.setText("");
			tfTimestamp.setText("");
			taMessageText.setText("");
		}
	}

	private JPanel createDetailsPanel() {
		final JPanel pnlDetails = new JPanel(new GridBagLayout());
		final JLabel lbPriority = new JLabel(COL_PRIORITY + ":");
		final JLabel lbMessageSource = new JLabel(COL_OWNER + ":");
		final JLabel lbObjectIdentifier = new JLabel(COL_OBJSOURCE + ":");
		final JLabel lbDestinationObject = new JLabel(COL_OBJDEST + ":");
		final JLabel lbTimestamp = new JLabel(COL_TIMESTAMP + ":");
		final TitledBorder border = BorderFactory.createTitledBorder(COL_MSGTEXT);
		final Color bgColor = pnlDetails.getBackground();
		final Border noBorder = BorderFactory.createEmptyBorder();
		final GridBagConstraints constraints = new GridBagConstraints();

		tfPriority.setBackground(bgColor);
		tfPriority.setBorder(noBorder);
		tfPriority.setEditable(false);
		tfMessageSource.setBackground(bgColor);
		tfMessageSource.setBorder(noBorder);
		tfMessageSource.setEditable(false);
		tfObjectIdentifier.setBackground(bgColor);
		tfObjectIdentifier.setBorder(noBorder);
		tfObjectIdentifier.setEditable(false);
		tfObjectIdentifier.setToolTipText(SpringLocaleDelegate.getInstance().getMessage(
				"NuclosNotificationDialog.7","SystemID des Objekts von dem aus die Meldung geschickt wurde") + ".");
		tfDestinationObject.setBackground(bgColor);
		tfDestinationObject.setBorder(noBorder);
		tfDestinationObject.setEditable(false);
		tfDestinationObject.setToolTipText(SpringLocaleDelegate.getInstance().getMessage(
				"NuclosNotificationDialog.8","SystemID des Zielobjekts einer Objektgenerierung") + ".");
		tfTimestamp.setBackground(bgColor);
		tfTimestamp.setBorder(noBorder);
		tfTimestamp.setEditable(false);
		taMessageText.setRows(5);
		//taMessageText.setBorder(border);
		taMessageText.setBackground(pnlDetails.getBackground());
		taMessageText.setEditable(false);
		taMessageText.setFont(tfTimestamp.getFont());
		taMessageText.setLineWrap(true);

		constraints.insets = new Insets(1, 1, 1, 1);
		constraints.gridx = 0; //GridBagConstraints.RELATIVE;
		constraints.gridy = 0;
		constraints.weightx = 1.0;
		constraints.gridwidth = 1;
		constraints.fill = GridBagConstraints.HORIZONTAL;

		pnlDetails.add(lbPriority, constraints);
		constraints.gridx++;
		pnlDetails.add(tfPriority, constraints);
		constraints.gridx++;
		pnlDetails.add(lbTimestamp, constraints);
		constraints.gridx++;
		pnlDetails.add(tfTimestamp, constraints);

		constraints.gridx = 0;
		constraints.gridy++;
		pnlDetails.add(lbObjectIdentifier, constraints);
		constraints.gridx++;
		pnlDetails.add(tfObjectIdentifier, constraints);

		constraints.gridx++;
		pnlDetails.add(lbDestinationObject, constraints);
		constraints.gridx++;
		pnlDetails.add(tfDestinationObject, constraints);

		constraints.gridx = 0;
		constraints.gridy++;
		pnlDetails.add(lbMessageSource, constraints);
		constraints.gridx++;
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		pnlDetails.add(tfMessageSource, constraints);
		constraints.gridwidth = 1;

		constraints.gridx = 0;
		constraints.gridy++;
		constraints.gridwidth = GridBagConstraints.REMAINDER;

		JScrollPane sp = new JScrollPane(taMessageText);
		sp.setBorder(border);
		pnlDetails.add(sp, constraints);

		final JScrollPane scrlpn = new JScrollPane(tbl);
		scrlpn.getViewport().setBackground(tbl.getBackground());

		return pnlDetails;
	}

	private void setupPopupMenu() {
		final JPopupMenu popupmenu = new JPopupMenu();
		final JMenuItem miShowSource = new JMenuItem(SpringLocaleDelegate.getInstance().getMessage(
				"NuclosNotificationDialog.10","Zeige Objektdetails") + "...");
		popupmenu.add(miShowSource);
		miShowSource.addActionListener(new ActionListener() {
			@Override
            public void actionPerformed(ActionEvent ev) {
				cmdShowObjectDetails((Integer) tblmodel.getValueAt(tbl.getSelectedRow(), sourceIDColumn));
			}
		});
		final JMenuItem miShowTarget = new JMenuItem(SpringLocaleDelegate.getInstance().getMessage(
				"NuclosNotificationDialog.11","Zeige Zielobjektdetails") + "...");
		popupmenu.add(miShowTarget);
		miShowTarget.addActionListener(new ActionListener() {
			@Override
            public void actionPerformed(ActionEvent ev) {
				cmdShowObjectDetails((Integer) tblmodel.getValueAt(tbl.getSelectedRow(), targetIDColumn));
			}
		});
		tbl.addMouseListener(new DefaultJPopupMenuListener(popupmenu) {
			@Override
			protected void showPopupMenu(MouseEvent ev) {
				// first select/deselect the row:
				final int iRow = tbl.rowAtPoint(ev.getPoint());

				// Nur, wenn nicht selektiert, selektieren:
				if (!tbl.isRowSelected(iRow)) {
					tbl.setRowSelectionInterval(iRow, iRow);
				}	// if

				super.showPopupMenu(ev);
			}	// showPopupMenu
		});
		popupmenu.addPopupMenuListener(new PopupMenuListener() {
			@Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent ev) {
				miShowTarget.setEnabled(tblmodel.getValueAt(tbl.getSelectedRow(), targetIDColumn) != null);
				miShowSource.setEnabled(tblmodel.getValueAt(tbl.getSelectedRow(), sourceIDColumn) != null);
			}

			@Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent ev) {
			}

			@Override
            public void popupMenuCanceled(PopupMenuEvent ev) {
			}
		});
	}

	private void cmdShowObjectDetails(Integer iGenericObjectId) {
		try {
			final GenericObjectVO govo = GenericObjectDelegate.getInstance().get(iGenericObjectId);
			final GenericObjectCollectController ctlGenericObject =
					NuclosCollectControllerFactory.getInstance().newGenericObjectCollectController(govo.getModuleId(), null, ClientParameterProvider.getInstance().getValue(ParameterProvider.KEY_LAYOUT_CUSTOM_KEY));
			ctlGenericObject.runViewSingleCollectable(CollectableGenericObjectWithDependants.newCollectableGenericObject(govo));
		}
		catch (CommonBusinessException ex) {
			// What todo?
		}
	}

	/**
	 * add a message to the display.
	 * @param message
	 */
	public void addMessage(final RuleNotification message) {
		//normally we are here on the EventDispatching Thread ('onMessage()' in JMS)
		//to be sure we use SwingUtilities...
		SwingUtilities.invokeLater(new Runnable() {
			@Override
            public void run() {
				tblmodel.insertRow(0, new Object[] {
						message.getPriority().getName(),
						message.getMessage(),
						message.getRuleName(),
						SpringLocaleDelegate.getInstance().getDateTimeFormat().format(message.getTimestamp()),
						//DateFormat.getDateTimeInstance().format(message.getTimestamp()),
						message.getSourceIdentifier(),
						message.getTargetIdentifier(),
						message.getSourceId(),
						message.getTargetId()
				}
				);
				//select the new row
				tbl.setRowSelectionInterval(0, 0);
			}
		});
	}

}	// class NuclosNotificationDialog
