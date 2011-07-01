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

import java.util.concurrent.Callable;

import javax.swing.JOptionPane;

import org.nuclos.client.ui.UIUtils;
import org.nuclos.client.ui.multiaction.MultiCollectablesActionController.Action;
import org.nuclos.common2.CommonLocaleDelegate;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common.NuclosBusinessException;
import org.nuclos.server.genericobject.valueobject.GeneratorActionVO;
import org.nuclos.server.genericobject.valueobject.GenericObjectVO;

/**
 * Action used by the controller for creating multiple generic objects
 */
class MultiGenerateAction implements Action<CollectableGenericObjectWithDependants, GenericObjectVO> {

	private final GenericObjectCollectController parent;
	private final Integer parameterObjectId;
	private final GeneratorActionVO generatoractionvo;
	private volatile Boolean showIncompleteObjects; 

	MultiGenerateAction(GenericObjectCollectController parent, Integer parameterObjectId, GeneratorActionVO generatoractionvo) {
		this.parent = parent;
		this.parameterObjectId = parameterObjectId;
		this.generatoractionvo = generatoractionvo;
	}

	/**
	 * performs the action on the given object.
	 * @param clct
	 * @throws CommonBusinessException
	 */
	@Override
	public GenericObjectVO perform(final CollectableGenericObjectWithDependants clct) throws CommonBusinessException {
		final Integer sourceId = clct.getId();
		final GenericObjectVO generatedGo = GeneratorDelegate.getInstance().generateGenericObject(sourceId, parameterObjectId, generatoractionvo);
		
		if (generatedGo.getId() == null) {
			// NUCLEUSINT-733: asking the user if the generated object is incomplete 				
			UIUtils.invokeOnDispatchThread(new Callable<Void>() {
				@Override
				public Void call() throws Exception {
					if (showIncompleteObjects == null) {
						int opt = JOptionPane.showConfirmDialog(
							parent.getFrame(),
							CommonLocaleDelegate.getMessage("R00022871",
								"Der Datensatz konnte nicht erstellt werden, da nicht alle Pflichtfelder gef\u00fcllt werden konnten.\n\nM\u00f6chten Sie die unvollst\u00e4ndigen Datens\u00e4tze manuell nachbearbeiten?"),
							getText(clct),
							JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
						showIncompleteObjects = (opt == JOptionPane.YES_OPTION);
					}
					if (showIncompleteObjects) {
						parent.showIncompleteGenericObject(sourceId, generatedGo);
					}
					return null;
				}
			});

			// this is a little bit hackish but guarantees that the generated object
			// is counted as failure
			throw new NuclosBusinessException(CommonLocaleDelegate.getMessage("R00022874", "Nicht alle Pflichtfelder konnten gef\u00fcllt werden."));
		}
		
		return generatedGo;
	}

	/**
	 * @param clct
	 * @return the text to display for the action on the given object.
	 */
	@Override
	public String getText(CollectableGenericObjectWithDependants clct) {
		return CommonLocaleDelegate.getMessage("R00022877", "Objektgenerierung f\u00fcr {0} ...", clct.getIdentifierLabel());
	}

	/**
	 * @param clct
	 * @return the text to display after successful execution of the action for the given object.
	 */
	@Override
	//public String getSuccessfulMessage(CollectableGenericObjectWithDependants clct, Integer iResult) {
	public String getSuccessfulMessage(CollectableGenericObjectWithDependants clct, GenericObjectVO rResult) {
		return CommonLocaleDelegate.getMessage("R00022880", "Objektgenerierung f\u00fcr {0} erfolgreich", clct.getIdentifierLabel());
	}

	@Override
	public String getConfirmStopMessage() {
		return CommonLocaleDelegate.getMessage("R00022886", "Wollen Sie die Objektgenerierung an dieser Stelle beenden?\n(Die bisher generierten Objekte bleiben in jedem Fall erhalten.)");
	}

	@Override
	public String getExceptionMessage(CollectableGenericObjectWithDependants clct, Exception ex) {
		return CommonLocaleDelegate.getMessage("R00022883", "Objektgenerierung f\u00fcr {0} fehlgeschlagen.\n{1}",
			clct.getIdentifierLabel(), ex.getMessage());
	}

	@Override
	public void executeFinalAction() throws CommonBusinessException {
		// do nothing
	}
}
