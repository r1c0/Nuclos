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

import java.awt.Point;

import javax.swing.DefaultBoundedRangeModel;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.SwingWorker;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.log4j.Logger;

import org.nuclos.client.main.mainframe.MainFrameTab;
import org.nuclos.client.ui.Errors;
import org.nuclos.client.ui.UIUtils;
import org.nuclos.client.ui.collect.result.ResultPanel;
import org.nuclos.client.ui.table.TableUtils;
import org.nuclos.common2.CommonLocaleDelegate;
import org.nuclos.server.genericobject.ProxyList;

/**
 * <code>ChangeListener</code> for the result table's vertical scrollbar.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */
public class ChangeListenerForResultTableVerticalScrollBar implements ChangeListener {
	
	private static final Logger LOG = Logger.getLogger(ChangeListenerForResultTableVerticalScrollBar.class);

	private final DefaultBoundedRangeModel model;
	private final JViewport viewport;
	private final ProxyList<?> proxylst;
	private final ResultPanel<?> resultpanel;
	private final MainFrameTab ifrm;

	/**
	 * lock to avoid recursion that might otherwise occur when there are more key down events in the event queue.
	 */
	private boolean bLock;

	public ChangeListenerForResultTableVerticalScrollBar(MainFrameTab aFrame, ResultPanel<?> aResultPanel, DefaultBoundedRangeModel model,
			JViewport vp, ProxyList<?> proxylst) {
		this.ifrm = aFrame;
		this.resultpanel = aResultPanel;
		this.model = model;
		this.viewport = vp;
		this.proxylst = proxylst;
	}

	@Override
	public synchronized void stateChanged(ChangeEvent ev) {
		if (bLock) {
			LOG.debug("Ignoring stateChanged event because lock is already set.");
		}
		else if (model.getValueIsAdjusting()) {
			LOG.debug("Ignoring stateChanged event because valueIsAdjusting.");
		}
		else {
			try {
				// set lock to avoid unwanted recursion:
				bLock = true;

				LOG.debug("Knob released.");
				LOG.debug("model.getValue() = " + model.getValue());
				final JTable tbl = resultpanel.getResultTable();
				final int iMaxYBeforeMove = UIUtils.getMaxVisibleY(tbl);
				LOG.debug("iMaxYBeforeMove = " + iMaxYBeforeMove);
				final int iLastRowBeforeMove = TableUtils.getLastVisibleRow(tbl);
				LOG.debug("iLastRowBeforeMove = " + iLastRowBeforeMove);

				final Point p = viewport.getViewPosition();
				final int iShiftY = model.getValue() - p.y;
				final int iMaxYAfterMove = iMaxYBeforeMove + iShiftY;
				LOG.debug("iMaxYAfterMove = " + iMaxYAfterMove);
				final int iLastRowAfterMove = TableUtils.getLastVisibleRow(tbl, iMaxYAfterMove);
				LOG.debug("iLastRowAfterMove = " + iLastRowAfterMove);

				final int iLastRow = iLastRowAfterMove;
				LOG.debug("getLastIndexRead() = " + proxylst.getLastIndexRead());
				if (iLastRow != -1  && !proxylst.hasObjectBeenReadForIndex(iLastRow)) {
					LOG.debug("*** NEED TO GET DATA!");
					
					class FetchSearchResultSwingWorker extends SwingWorker<Integer, Integer> {
						@Override
						public Integer doInBackground() {
							LOG.debug("START FetchSearchResultSwingWorker");
							// since fetchDataIfNecessary never publishes intermediate results (progress)
							// to its ChangeListener, there is no need to install one
							proxylst.fetchDataIfNecessary(iLastRow, null);
							LOG.debug("FINISHED FetchSearchResultSwingWorker");
							return proxylst.getLastIndexRead();
						}
						
						@Override
						public void done() {
							ifrm.unlockLayer();
							ChangeListenerForResultTableVerticalScrollBar.this.setResultTableEnabled(true);							
							resultpanel.getResultTable().requestFocusInWindow();
							
							try {
								int iLastIndexRead = this.get();
								if (iLastIndexRead >= iLastRow) {
									// move the table:
									p.y = model.getValue();
									viewport.setViewPosition(p);
									LOG.debug("getLastVisibleRow(tbl) = " + TableUtils.getLastVisibleRow(tbl));
								}
								else {
									// move to the last read row:
									LOG.debug("*** MOVE TO THE LAST READ ROW: " + iLastIndexRead);
									model.setValue(getModelValue(tbl, iLastIndexRead));
								}
							} 
							catch(Exception e) {
								// an exception or error occured:
								// reset view position:
								LOG.info("Resetting vertical scrollbar because of " + e);
								model.setValue(0);

								final String sMessage = CommonLocaleDelegate.getInstance().getMessage(
										"ChangeListenerForResultTableVerticalScrollBar.1", "Beim Nachladen von Datens\u00e4tzen ist ein Fehler ist aufgetreten. Die Datens\u00e4tze k\u00f6nnen nicht angezeigt werden.");
								Errors.getInstance().showExceptionDialog(ifrm, sMessage, e);
							}							
						}
					}

					final FetchSearchResultSwingWorker worker = new FetchSearchResultSwingWorker();
					this.setResultTableEnabled(false);
					this.ifrm.lockLayerBusy();
					worker.execute();
				}
				else {
					// move the table:
					p.y = model.getValue();
					viewport.setViewPosition(p);
					LOG.debug("getLastVisibleRow(tbl) = " + TableUtils.getLastVisibleRow(tbl));
				}
			}
			finally {
				bLock = false;
			}
		}
	}

	private static int getModelValue(final JTable tbl, final int iLastIndexRead) {
		final int iNewLastRow = iLastIndexRead;
		final int iNewMaxY = (iNewLastRow + 1) * tbl.getRowHeight() - 1;
		final int result = iNewMaxY - tbl.getVisibleRect().height;
		return result;
	}

	/**
	 * The dialog is not really modal. We have to disable the table and the vertical scrollbar to avoid
	 * recursion.
	 * @param bEnabled
	 */
	private void setResultTableEnabled(boolean bEnabled) {
		resultpanel.getResultTable().setEnabled(bEnabled);
		resultpanel.getResultTableScrollPane().getVerticalScrollBar().setEnabled(bEnabled);
	}

}	// class ChangeListenerForResultTableVerticalScrollBar
