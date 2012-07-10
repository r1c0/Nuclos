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
package org.nuclos.client.ui.multiaction;


import info.clearthought.layout.TableLayout;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;

import org.nuclos.client.synthetica.NuclosThemeSettings;
import org.nuclos.client.ui.IOverlayCenterComponent;
import org.nuclos.client.ui.IOverlayFrameChangeListener;
import org.nuclos.client.ui.Icons;
import org.nuclos.client.ui.ResultListener;
import org.nuclos.client.ui.StatusBarTextField;
import org.nuclos.client.ui.UIUtils;
import org.nuclos.client.ui.UpDownButton;
import org.nuclos.client.ui.popupmenu.JPopupMenuFactory;
import org.nuclos.client.ui.popupmenu.JTableJPopupMenuListener;
import org.nuclos.client.ui.table.CommonJTable;
import org.nuclos.client.ui.util.TableLayoutBuilder;
import org.nuclos.common2.SpringLocaleDelegate;

/**
 * Panel for displaying the progress of "multi actions".
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version	01.00.00
 * @todo provide a custom table model that contains a traffic light and holds the whole exception, if any, so that the user
 * can double click to get the detailed message. Double click on the specific object should be possible also.
 */
public class MultiActionProgressPanel extends JPanel implements IOverlayCenterComponent {

	public final UpDownButton btnProtocol = new UpDownButton(false);
	public final JToggleButton btnPause = new JToggleButton(Icons.getInstance().getIconPause16());
	public final JButton btnStop = new JButton(Icons.getInstance().getIconStop16());
	public final JButton btnClose = new JButton(
			SpringLocaleDelegate.getInstance().getMessage(
					"MultiActionProgressPanel.4","Schlie\u00dfen"));
	public final JButton btnSaveResult = new JButton(Icons.getInstance().getIconSave16());

	private JProgressBar progressbar;
	private MultiActionProgressTableModel tblmdl;
	protected final JTable tblResult = new CommonJTable() {

		@Override
		public boolean isCellEditable(int row, int column) {
			return false;
		}
	};
	private final JLabel labAction = new JLabel(" ");
	private final JScrollPane scrlpn = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
	private final JLabel labStatus = new JLabel(SpringLocaleDelegate.getInstance().getMessage("MultiActionProgressPanel.5","Bereit"));

	private JTextArea txtAreaDetail;
	private JScrollPane scrlpnDetail;
	
	private JSplitPane splitPanel;
	
	private IMultiActionProgressResultHandler resultHandler;
	
	private final Collection<IOverlayFrameChangeListener> overlayFrameChangeListeners = new ArrayList<IOverlayFrameChangeListener>();
	
	private CloseHandler closeHandler;

	public MultiActionProgressPanel(int iCount) {
		super(new BorderLayout());

		this.tblmdl = new MultiActionProgressTableModel(new ArrayList<MultiActionProgressLine>(), null);
		this.tblResult.setModel(this.tblmdl);
		this.tblResult.setRowSorter(new TableRowSorter<MultiActionProgressTableModel>(this.tblmdl));

		this.init(iCount);
		setPauseStopButtons();
		initiallySetColumnWidths();
		this.tblResult.getColumnModel().removeColumn(tblResult.getColumnModel().getColumn(MultiActionProgressTableModel.COLUMN_ID));
		this.tblResult.setDefaultRenderer(java.lang.Object.class, new ColorRenderer());
	}

	private void setPauseStopButtons() {
		btnPause.setEnabled(true);
		btnStop.setEnabled(true);
		btnSaveResult.setEnabled(false);
	}

	/**
	 * removes the pause/stop buttons in favor of a close button.
	 */
	public void setCloseButton() {
		progressbar.setValue(progressbar.getMaximum());
		progressbar.setEnabled(false);
		
		btnPause.setEnabled(false);
		btnStop.setEnabled(false);
		btnSaveResult.setEnabled(true);
	}

	private void init(int iCount) {
		btnProtocol.setFocusable(false);
		btnProtocol.setToolTipText(SpringLocaleDelegate.getInstance().getMessage("MultiActionProgressPanel.10","Protokoll anzeigen"));
		btnProtocol.setGradientUp(true);
		btnProtocol.setRootPaneBG(true);
		btnPause.setFocusable(false);
		btnPause.setToolTipText(SpringLocaleDelegate.getInstance().getMessage("MultiActionProgressPanel.2","Pause"));
		btnPause.setPreferredSize(new Dimension(24,24));
		btnPause.setBorderPainted(false);
		btnStop.setFocusable(false);
		btnStop.setToolTipText(SpringLocaleDelegate.getInstance().getMessage("MultiActionProgressPanel.3","Stop"));
		btnStop.setPreferredSize(new Dimension(24,24));
		btnStop.setBorderPainted(false);
		btnSaveResult.setFocusable(false);
		btnSaveResult.setToolTipText(SpringLocaleDelegate.getInstance().getMessage("MultiActionProgressPanel.9","Protokoll speichern"));
		btnClose.setFocusable(false);
		labAction.setForeground(Color.WHITE);
		Dimension actionSize = new Dimension(300, 12);
		labStatus.setMinimumSize(actionSize);
		labStatus.setMaximumSize(actionSize);
		labStatus.setPreferredSize(actionSize);
		labStatus.setSize(actionSize);
		labStatus.setForeground(Color.WHITE);
		labStatus.setBorder(BorderFactory.createEmptyBorder(0, 17, 0, 0));
		
		final JPanel pnlCenter = new JPanel(new BorderLayout());
		final JPanel pnlDetails = new JPanel(new BorderLayout());

		final JPanel splitTopPanel = new JPanel(new BorderLayout());
		splitTopPanel.setBackground(NuclosThemeSettings.BACKGROUND_ROOTPANE);

		splitPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		splitPanel.setVisible(false);
		this.add(splitTopPanel, BorderLayout.NORTH);
		this.add(splitPanel, BorderLayout.CENTER);
		
		final JPanel pnlHeader = new JPanel();
		pnlHeader.setOpaque(false);
		pnlHeader.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
		TableLayoutBuilder tbllayHeader = new TableLayoutBuilder(pnlHeader).columns(
				TableLayout.FILL, 
				TableLayout.PREFERRED, 
				10d,
				TableLayout.PREFERRED,
				0d,
				TableLayout.PREFERRED, 
				TableLayout.FILL);
		
		tbllayHeader.newRow().skip().add(labAction, 6, TableLayout.LEFT, TableLayout.CENTER);
		progressbar = new JProgressBar(0, iCount);
		progressbar.setName("ProgressBarForDarkBG");
		progressbar.setPreferredSize(new Dimension(300, 12));
		tbllayHeader.newRow().skip();
		tbllayHeader.add(progressbar, 1, TableLayout.CENTER, TableLayout.CENTER).skip();
		tbllayHeader.add(btnPause, 1, TableLayout.CENTER, TableLayout.CENTER).skip();
		tbllayHeader.add(btnStop, 1, TableLayout.CENTER, TableLayout.CENTER);
		
		splitTopPanel.add(pnlHeader, BorderLayout.NORTH);
		
		final JPanel pnlSubHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		pnlSubHeader.setOpaque(false);
		pnlSubHeader.add(btnProtocol);
		pnlSubHeader.add(labStatus);
		
		splitTopPanel.add(pnlSubHeader, BorderLayout.SOUTH);

		JToolBar toolBar = UIUtils.createNonFloatableToolBar();
		toolBar.add(btnSaveResult);
		pnlCenter.add(toolBar, BorderLayout.NORTH);
		pnlCenter.add(scrlpn, BorderLayout.CENTER);

		scrlpn.setViewportView(tblResult);
		tblResult.setAutoscrolls(true);

		txtAreaDetail = new JTextArea();
		txtAreaDetail.setLineWrap(true);
		txtAreaDetail.setWrapStyleWord(true);
		txtAreaDetail.setAutoscrolls(true);
		txtAreaDetail.setEditable(false);

		scrlpnDetail = new JScrollPane(txtAreaDetail);
		scrlpnDetail.setBorder(BorderFactory.createEmptyBorder());

		pnlDetails.add(scrlpnDetail, BorderLayout.CENTER);

		splitPanel.setDividerLocation(300);
		splitPanel.setTopComponent(pnlCenter);
		splitPanel.setBottomComponent(scrlpnDetail);

		ListSelectionModel rowSM = tblResult.getSelectionModel();
        rowSM.addListSelectionListener(new ListSelectionListener() {
            @Override
			public void valueChanged(ListSelectionEvent e) {
                ListSelectionModel lsm = (ListSelectionModel)e.getSource();
                //int colNummer = tblmdl.findColumn("Ergebnis");
                String sDetail = "";
                if(lsm.getMinSelectionIndex() >= 0 && lsm.getMinSelectionIndex() == lsm.getMaxSelectionIndex()){
                	int modelIndex = tblResult.convertRowIndexToModel(lsm.getMinSelectionIndex());
                	sDetail = (String)tblResult.getModel().getValueAt(modelIndex, 1);
                }
                txtAreaDetail.setText(sDetail);
            }
        });
	}

	private void initiallySetColumnWidths() {
		for (int iColumn = 0; iColumn < this.tblResult.getColumnCount(); iColumn++) {
			final TableColumn column = this.tblResult.getColumnModel().getColumn(iColumn);
			final int iPreferredCellWidth = MultiActionProgressTableModel.getPreferredColumnWidth(iColumn);
			column.setPreferredWidth(iPreferredCellWidth);
			column.setWidth(iPreferredCellWidth);
		}
		this.tblResult.revalidate();
	}

	private void addResultMouseListener() {
		tblResult.addMouseListener(new JTableJPopupMenuListener(tblResult, new JPopupMenuFactory() {
			@Override
			public JPopupMenu newJPopupMenu() {
				final JPopupMenu result = new JPopupMenu();
				final boolean bMultipleSelection = (tblResult.getSelectedRowCount() > 1);
				result.add(newMenuItemForHandleSelection(bMultipleSelection));
				return result;
			}
		}));
	}

	private JMenuItem newMenuItemForHandleSelection(boolean bMultipleSelection) {
		final String sText = bMultipleSelection ? getResultHandler().getMultiSelectionMenuLabel() : getResultHandler().getSingleSelectionMenuLabel();
		final JMenuItem result = new JMenuItem(sText);
		result.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ev) {
				getResultHandler().handleMultiSelection(null);
			}
		});
		return result;
	}
	
	public void showProtocol(boolean show) {
		btnProtocol.setUp(show);
		btnProtocol.setSelected(show);
		splitPanel.setVisible(show);
		revalidate();
		newSize(getPreferredSize());
	}

	public void setProgress(int iValue) {
		this.progressbar.setValue(iValue);
	}

	public void setActionText(String sText) {
		this.labAction.setText(sText);
	}

	public void addProtocolLine(Object id, Object oResult, String result, String state) {
		MultiActionProgressLine newLine = new MultiActionProgressLine(id, oResult, result, state);
		tblmdl.addRow(newLine);
		int viewIndex = tblResult.getRowCount() - 1;
		if (viewIndex != -1) {
			tblResult.setRowSelectionInterval(viewIndex, viewIndex);
			tblResult.scrollRectToVisible(tblResult.getCellRect(viewIndex, 0, true));
		}
	}

	public void setStatus(String sText) {
		labStatus.setText(sText);
	}

	public boolean hasResultHandler(){
		return getResultHandler() != null;
	}

	private IMultiActionProgressResultHandler getResultHandler() {
		return resultHandler;
	}

	public void setResultHandler(IMultiActionProgressResultHandler resultHandler) {
		this.resultHandler = resultHandler;
		addResultMouseListener();
		String[] columnNames = new String[3];
		columnNames[0] = SpringLocaleDelegate.getInstance().getMessage("MultiActionProgressPanel.7","ID");
		columnNames[1] = SpringLocaleDelegate.getInstance().getMessage("MultiActionProgressPanel.8","Ergebnis");
		columnNames[2] = resultHandler.getStateHeaderLabel();
		this.tblmdl.setColumnNames(columnNames);
		this.tblResult.revalidate();
	}

	public String getStateHeaderLabel(){
		String stateHeaderLabel = null;
		if(getResultHandler() != null){
			stateHeaderLabel = getResultHandler().getStateHeaderLabel();
		}
		return stateHeaderLabel;
	}

	public String getSuccessLabel(){
		String successLabel = null;
		if(getResultHandler() != null){
			successLabel = getResultHandler().getSuccessLabel();
		}
		return successLabel;
	}

	public String getExceptionLabel(){
		String exceptionLabel = null;
		if(getResultHandler() != null){
			exceptionLabel = getResultHandler().getExceptionLabel();
		}
		return exceptionLabel;
	}

	public String getSingleSelectionMenuLabel(){
		String singleSelectionMenuLabel = SpringLocaleDelegate.getInstance().getMessage(
				"RuleExplorerNode.1","Details anzeigen");
		if(getResultHandler() != null){
			singleSelectionMenuLabel = getResultHandler().getSingleSelectionMenuLabel();
		}
		return singleSelectionMenuLabel;
	}

	public String getMultiSelectionMenuLabel(){
		String multiSelectionMenuLabel = SpringLocaleDelegate.getInstance().getMessage(
				"RuleExplorerNode.1","Details anzeigen");
		if(getResultHandler() != null){
			multiSelectionMenuLabel = getResultHandler().getMultiSelectionMenuLabel();
		}
		return multiSelectionMenuLabel;
	}

	public void handleMultiSelection(Collection<MultiActionProgressLine> selection){
		if(getResultHandler() != null){
			getResultHandler().handleMultiSelection(selection);
		}
	}

	private class ColorRenderer extends DefaultTableCellRenderer {

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column){
			Component returnMe = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			if(value instanceof String){
				Color txtColor = Color.black;
				Object statusLabel = getStatusLabel(table, row, column);
				if((statusLabel instanceof String)  && isExceptionLabel((String)statusLabel)){
					txtColor = Color.red;
				}
				if(isSelected){
					txtColor = Color.white;
				}
				returnMe.setForeground(txtColor);
			}
			return returnMe;
		}

		private Object getStatusLabel(JTable table, int row, int column){
			int columnIndex = column;
			int lastColumnIndex = table.getColumnCount()-1;
			if(lastColumnIndex > columnIndex){
				columnIndex = lastColumnIndex;
			}
			return table.getValueAt(row, columnIndex);
		}

		private boolean isExceptionLabel(String valueTxt) {
			return (valueTxt != null) && valueTxt.equalsIgnoreCase(getExceptionLabel());
		}

	}

	public List<MultiActionProgressLine> getSelection() {
		List<MultiActionProgressLine> selection = new ArrayList<MultiActionProgressLine>();
		int[] selectedRows = tblResult.getSelectedRows();
		for(int i=0; i < selectedRows.length; i++){
			selection.add(this.tblmdl.getRow(selectedRows[i]));
		}
		return selection;
	}
	
	public interface CloseHandler {
		public boolean isClosable();
		public void closed();
	}
	
	public CloseHandler getCloseHandler() {
		return closeHandler;
	}

	public void setCloseHandler(CloseHandler closableHandler) {
		this.closeHandler = closableHandler;
	}

	@Override
	public void transferSize(Dimension size) {
	}

	@Override
	public void addOverlayFrameChangeListener(IOverlayFrameChangeListener listener) {
		overlayFrameChangeListeners.add(listener);
	}

	@Override
	public void removeOverlayFrameChangeListener(IOverlayFrameChangeListener listener) {
		overlayFrameChangeListeners.remove(listener);
	}

	@Override
	public boolean isClosable() {
		return closeHandler==null?true:closeHandler.isClosable();
	}

	@Override
	public void notifyClosing(ResultListener<Boolean> rl) {
		rl.done(true);
		if (closeHandler!=null) {
			closeHandler.closed();
		}
	}

	@Override
	public Dimension getCenterSize() {
		return getPreferredSize();
	}
	
	public void close() {
		for (IOverlayFrameChangeListener listener : new ArrayList<IOverlayFrameChangeListener>(overlayFrameChangeListeners)) {
			listener.closeOverlay();
		}
	}
	
	public void newSize(Dimension newSize) {
		for (IOverlayFrameChangeListener listener : new ArrayList<IOverlayFrameChangeListener>(overlayFrameChangeListeners)) {
			listener.sizeChanged(newSize);
		}
	}

}  // class MultiActionProgressPanel
