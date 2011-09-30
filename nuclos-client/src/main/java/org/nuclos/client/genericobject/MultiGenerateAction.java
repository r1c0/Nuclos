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

import java.util.Collection;

import org.nuclos.client.common.MetaDataClientProvider;
import org.nuclos.client.ui.multiaction.MultiCollectablesActionController.Action;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common2.CommonLocaleDelegate;
import org.nuclos.common2.StringUtils;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.server.genericobject.ejb3.GenerationResult;
import org.nuclos.server.genericobject.valueobject.GeneratorActionVO;
import org.nuclos.server.genericobject.valueobject.GenericObjectVO;

/**
 * Action used by the controller for creating multiple generic objects
 */
class MultiGenerateAction implements Action<Collection<GenericObjectVO>, GenerationResult> {

	private final Integer parameterObjectId;
	private final GeneratorActionVO generatoractionvo;

	MultiGenerateAction(Integer parameterObjectId, GeneratorActionVO generatoractionvo) {
		this.parameterObjectId = parameterObjectId;
		this.generatoractionvo = generatoractionvo;
	}

	/**
	 * performs the action on the given object.
	 *
	 * @param clct
	 * @throws CommonBusinessException
	 */
	@Override
	public GenerationResult perform(final Collection<GenericObjectVO> sources) throws CommonBusinessException {
		if (generatoractionvo.isGroupAttributes()) {
			return GeneratorDelegate.getInstance().generateGenericObject(sources, parameterObjectId, generatoractionvo);
		}
		else {
			if (sources.size() > 1) {
				throw new NuclosFatalException();
			}
			return GeneratorDelegate.getInstance().generateGenericObject(sources.iterator().next().getId(), parameterObjectId, generatoractionvo);
		}
	}

	/**
	 * @param clct
	 * @return the text to display for the action on the given object.
	 */
	@Override
	public String getText(Collection<GenericObjectVO> sources) {
		if (sources.size() == 1) {
			String entity = MetaDataClientProvider.getInstance().getEntity(generatoractionvo.getTargetModuleId().longValue()).getEntity();
			return CommonLocaleDelegate.getTreeViewLabel(sources.iterator().next(), entity, MetaDataClientProvider.getInstance());
		}
		else {
			return CommonLocaleDelegate.getMessage("generation.multiple", "Objektgenerierung f\u00fcr {0} Objekte ...", sources.size());
		}
	}

	/**
	 * @param clct
	 * @return the text to display after successful execution of the action for
	 *         the given object.
	 */
	@Override
	public String getSuccessfulMessage(Collection<GenericObjectVO> sources, GenerationResult rResult) {
		if (!StringUtils.isNullOrEmpty(rResult.getError())) {
			return CommonLocaleDelegate.getMessage("generation.unsaved",
					"Generated obect could not be saved: \\n{0} \\nPlease edit object in details view (see context menu).",
					CommonLocaleDelegate.getMessageFromResource(rResult.getError()));
		}
		else {
			String entity = MetaDataClientProvider.getInstance().getEntity(generatoractionvo.getTargetModuleId().longValue()).getEntity();
			return CommonLocaleDelegate.getMessage("R00022880",
					"Object \"{0}\" successfully generated.",
					CommonLocaleDelegate.getTreeViewLabel(rResult.getGeneratedObject(), entity, MetaDataClientProvider.getInstance()));
		}
	}

	@Override
	public String getConfirmStopMessage() {
		return CommonLocaleDelegate.getMessage("R00022886", "Wollen Sie die Objektgenerierung an dieser Stelle beenden?\n(Die bisher generierten Objekte bleiben in jedem Fall erhalten.)");
	}

	@Override
	public String getExceptionMessage(Collection<GenericObjectVO> sources, Exception ex) {
		return CommonLocaleDelegate.getMessage("R00022883", "Objektgenerierung fehlgeschlagen. \\n{1}", ex.getMessage());
	}

	@Override
	public void executeFinalAction() throws CommonBusinessException {
		// do nothing
	}
}
