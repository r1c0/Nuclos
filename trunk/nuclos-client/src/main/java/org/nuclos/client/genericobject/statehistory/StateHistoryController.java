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
package org.nuclos.client.genericobject.statehistory;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.KeyStroke;

import org.apache.commons.lang.NullArgumentException;
import org.nuclos.client.common.NuclosCollectControllerFactory;
import org.nuclos.client.genericobject.CollectableGenericObjectWithDependants;
import org.nuclos.client.genericobject.GenericObjectDelegate;
import org.nuclos.client.genericobject.Modules;
import org.nuclos.client.main.MainController;
import org.nuclos.client.main.mainframe.MainFrameTab;
import org.nuclos.client.statemodel.StateDelegate;
import org.nuclos.client.ui.Controller;
import org.nuclos.client.ui.Errors;
import org.nuclos.client.ui.UIUtils;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common2.CommonLocaleDelegate;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.server.genericobject.valueobject.GenericObjectWithDependantsVO;
import org.nuclos.server.statemodel.valueobject.StateHistoryVO;

/**
 * Controller for loading and displaying the state history.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */

public class StateHistoryController extends Controller {
	private JComponent parent;

	private int iModuleId;
	private int iGenericObjectId;

	/**
	 * @param parent Typically, the <code>JDesktopPane</code> to add the internal frame
	 */
	public StateHistoryController(JComponent parent) {
		super(parent);
		this.parent = parent;
	}

	/**
	 * runs the controller. Gets and displays the state history in a new internal frame.
	 * @param iGenericObjectId
	 * @param sGenericObjectIdentifier
	 * @throws NuclosFatalException
	 */
	public void run(int iModuleId, int iGenericObjectId, String sGenericObjectIdentifier) throws CommonBusinessException {
		this.iModuleId = iModuleId;
		this.iGenericObjectId = iGenericObjectId;
		final List<StateHistoryVO> lstHistory = StateDelegate.getInstance().getStateHistory(iModuleId, iGenericObjectId);

		final StateHistoryPanel pnlHistory = new StateHistoryPanel(lstHistory);

		final String sTitle = getTitle(sGenericObjectIdentifier, iGenericObjectId, iModuleId);

		final MainFrameTab ifrm = MainController.newMainFrameTab(null, sTitle);
		//ifrm.setContentPane(pnlHistory);
		ifrm.setLayeredComponent(pnlHistory);
		parent.add(ifrm);

		setupEscapeKey(ifrm, pnlHistory);

		setupDoubleClickListener(pnlHistory);

//		ifrm.pack();
		ifrm.setVisible(true);
	}

	private void setupDoubleClickListener(final StateHistoryPanel pnlStateHistory) {
		pnlStateHistory.getTable().addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent ev) {
				if (ev.getClickCount() == 2) {
					final int iSelectedRow = pnlStateHistory.getTable().getSelectedRow();
					if (iSelectedRow >= 0) {
						final StateHistoryVO stateHistory = ((StateHistoryPanel.TableModel) pnlStateHistory.getTable().getModel()).getRow(iSelectedRow);

						// Workaround for retrieval problems with initial state
						Calendar cal = Calendar.getInstance();
						cal.setTime(stateHistory.getCreatedAt());
						cal.add(Calendar.MINUTE, 1);

						cmdShowHistoricalGenericObject(parent, cal.getTime());
					}
				}
			}
		});
	}

	private void cmdShowHistoricalGenericObject(final JComponent parent, final Date dateHistorical) {
		if (dateHistorical == null) {
			throw new NullArgumentException("dateHistorical");
		}
		UIUtils.runCommand(parent, new Runnable() {
			@Override
			public void run() {
				try {
					final GenericObjectWithDependantsVO lowdcvo = GenericObjectDelegate.getInstance().getHistorical(iGenericObjectId,
							dateHistorical);

					final CollectableGenericObjectWithDependants clct = new CollectableGenericObjectWithDependants(lowdcvo);
					NuclosCollectControllerFactory.getInstance().newGenericObjectCollectController(parent, iModuleId, null).runViewSingleHistoricalCollectable(clct, dateHistorical);
				}
				catch (/* CommonBusiness */ Exception ex) {
					Errors.getInstance().showExceptionDialog(parent, ex);
				}
			}
		});
	}

	private static String getTitle(String sGenericObjectIdentifier, int iGenericObjectId, int iModuleId) {
		final StringBuffer sbTitle = new StringBuffer(CommonLocaleDelegate.getMessage("StateHistoryController.1", "Statushistorie f\u00fcr") + " ");

		if (sGenericObjectIdentifier == null) {
			sbTitle.append(CommonLocaleDelegate.getMessage("LogbookController.13", "das Objekt mit der Id") + " ");
			sbTitle.append(Integer.toString(iGenericObjectId));
		}
		else {
			sbTitle.append(Modules.getInstance().getEntityLabelByModuleId(new Integer(iModuleId)) + " ");
			sbTitle.append("\"" + sGenericObjectIdentifier + "\"");
		}

		return sbTitle.toString();
	}

	private static void setupEscapeKey(final MainFrameTab ifrm, final StateHistoryPanel pnlHistory) {
		// Escape key is to close the window:
		final Action actClose = new AbstractAction() {

			@Override
			public void actionPerformed(ActionEvent ev) {
				ifrm.dispose();
			}
		};

		final String KEY_CLOSE = "Close";
		ifrm.getRootPane().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(
				KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), KEY_CLOSE);
		ifrm.getRootPane().getActionMap().put(KEY_CLOSE, actClose);

		pnlHistory.getTable().getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), KEY_CLOSE);
		pnlHistory.getTable().getActionMap().put(KEY_CLOSE, actClose);
	}

}	// class StateHistoryController
