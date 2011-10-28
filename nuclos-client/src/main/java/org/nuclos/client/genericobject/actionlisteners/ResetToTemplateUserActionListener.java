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
/**
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:hartmut.beckschulze@novabit.de">Hartmut Beckschulze</a>
 * @version 01.00.00
 */
package org.nuclos.client.genericobject.actionlisteners;

import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JOptionPane;

import org.apache.log4j.Logger;
import org.nuclos.common.collect.collectable.searchcondition.CollectableSearchCondition;
import org.nuclos.common2.PreferencesUtils;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.common2.exception.CommonPermissionException;
import org.nuclos.client.common.NuclosCollectController;
import org.nuclos.client.common.NuclosCollectControllerFactory;
import org.nuclos.client.genericobject.GenericObjectCollectController;
import org.nuclos.client.main.Main;
import org.nuclos.client.ui.Errors;
import org.nuclos.common.NuclosBusinessException;

/**
 * 
 * @author hartmut.beckschulze
 *
 */
public class ResetToTemplateUserActionListener implements ActionListener{
	
	private static final Logger LOG = Logger.getLogger(ResetToTemplateUserActionListener.class);
	
	private final GenericObjectCollectController goCon;
	
	public ResetToTemplateUserActionListener(GenericObjectCollectController goCon) {
		this.goCon = goCon;
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		int choice = JOptionPane.showOptionDialog(goCon.getCollectPanel(), "Wollen sie die Standardergebnisansicht wiederherstellen?\nIhre pers\u00f6nlichen Einstellungen werden dabei \u00fcberschrieben.", "Standardansicht wiederherstellen", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, JOptionPane.YES_OPTION);
		
		if (choice == JOptionPane.YES_OPTION) {
			try {
				PreferencesUtils.resetToTemplateUser(goCon.getEntityName());
				CollectableSearchCondition searchCondition = goCon.getSearchStrategy().getCollectableSearchCondition();
				NuclosCollectController<?> goConNew = NuclosCollectControllerFactory.getInstance().newCollectController(Main.getMainFrame().getHomePane(), goCon.getEntityName(), null);
				goConNew.runViewResults(searchCondition);
				Rectangle goConBounds = goCon.getFrame().getBounds();
				goConNew.getFrame().setBounds(goConBounds);
				goCon.getFrame().dispose();
			} catch(NuclosBusinessException e1) {
				Errors.getInstance().showExceptionDialog(goCon.getCollectPanel(), e1);
			} catch(CommonPermissionException e1) {
				Errors.getInstance().showExceptionDialog(goCon.getCollectPanel(), e1);
			} catch(CommonFatalException e1) {
				Errors.getInstance().showExceptionDialog(goCon.getCollectPanel(), e1);
			} catch(CommonBusinessException e1) {
				Errors.getInstance().showExceptionDialog(goCon.getCollectPanel(), e1);			
			} 
		} 
	}
}
