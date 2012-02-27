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

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.JComponent;

import org.nuclos.client.common.MetaDataClientProvider;
import org.nuclos.client.ui.collect.InvokeWithInputRequiredSupport;
import org.nuclos.client.ui.multiaction.MultiCollectablesActionController.Action;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.collection.Pair;
import org.nuclos.common.dal.vo.EntityObjectVO;
import org.nuclos.common2.SpringLocaleDelegate;
import org.nuclos.common2.CommonRunnable;
import org.nuclos.common2.StringUtils;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.server.genericobject.GeneratorFailedException;
import org.nuclos.server.genericobject.ejb3.GenerationResult;
import org.nuclos.server.genericobject.valueobject.GeneratorActionVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

/**
 * Action used by the controller for creating multiple generic objects
 */
@Configurable
class MultiGenerateAction implements Action<Pair<Collection<EntityObjectVO>, Long>, GenerationResult> {

	private final JComponent parent;
	private final GeneratorActionVO generatoractionvo;

	private InvokeWithInputRequiredSupport invokeWithInputRequiredSupport;

	MultiGenerateAction(JComponent parent, GeneratorActionVO generatoractionvo) {
		this.parent = parent;
		this.generatoractionvo = generatoractionvo;
	}

	@Autowired
	void setInvokeWithInputRequiredSupport(InvokeWithInputRequiredSupport invokeWithInputRequiredSupport) {
		this.invokeWithInputRequiredSupport = invokeWithInputRequiredSupport;
	}

	/**
	 * performs the action on the given object.
	 *
	 * @param clct
	 * @throws CommonBusinessException
	 */
	@Override
	public GenerationResult perform(final Pair<Collection<EntityObjectVO>, Long> sources) throws CommonBusinessException {
		final HashMap<String, Serializable> context = new HashMap<String, Serializable>();
		final AtomicReference<GenerationResult> result = new AtomicReference<GenerationResult>();
		invokeWithInputRequiredSupport.invoke(new CommonRunnable() {
			@Override
			public void run() throws CommonBusinessException {
				try {
					if (generatoractionvo.isGroupAttributes()) {
						result.set(GeneratorDelegate.getInstance().generateGenericObject(
								sources.x, sources.y, generatoractionvo));
					}
					else {
						if (sources.x.size() > 1) {
							throw new NuclosFatalException();
						}
						result.set(GeneratorDelegate.getInstance().generateGenericObject(
								sources.x.iterator().next().getId(), sources.y, generatoractionvo));
					}
				}
				catch (GeneratorFailedException e) {
					result.set(e.getGenerationResult());
				}
			}
		}, context, parent);
		return result.get();
	}

	/**
	 * @param clct
	 * @return the text to display for the action on the given object.
	 */
	@Override
	public String getText(Pair<Collection<EntityObjectVO>, Long> sources) {
		if (sources.x.size() == 1) {
			String entity = MetaDataClientProvider.getInstance().getEntity(generatoractionvo.getTargetModuleId().longValue()).getEntity();
			return SpringLocaleDelegate.getInstance().getTreeViewLabel(
					sources.x.iterator().next(), entity, MetaDataClientProvider.getInstance());
		}
		else {
			return SpringLocaleDelegate.getInstance().getMessage(
					"generation.multiple", "Objektgenerierung f\u00fcr {0} Objekte ...", sources.x.size());
		}
	}

	/**
	 * @param clct
	 * @return the text to display after successful execution of the action for
	 *         the given object.
	 */
	@Override
	public String getSuccessfulMessage(Pair<Collection<EntityObjectVO>, Long> sources, GenerationResult rResult) {
		if (!StringUtils.isNullOrEmpty(rResult.getError())) {
			return SpringLocaleDelegate.getInstance().getMessage(
					"generation.unsaved",
					"Generated obect could not be saved: \\n{0} \\nPlease edit object in details view (see context menu).",
					SpringLocaleDelegate.getInstance().getMessageFromResource(rResult.getError()));
		}
		else {
			// dead code
			assert false;
			String entity = MetaDataClientProvider.getInstance().getEntity(generatoractionvo.getTargetModuleId().longValue()).getEntity();
			return SpringLocaleDelegate.getInstance().getMessage("R00022880",
					"Object \"{0}\" successfully generated.",
					SpringLocaleDelegate.getInstance().getTreeViewLabel(
							rResult.getGeneratedObject(), entity, MetaDataClientProvider.getInstance()));
		}
	}

	@Override
	public String getConfirmStopMessage() {
		return SpringLocaleDelegate.getInstance().getMessage(
				"R00022886", "Wollen Sie die Objektgenerierung an dieser Stelle beenden?\n(Die bisher generierten Objekte bleiben in jedem Fall erhalten.)");
	}

	@Override
	public String getExceptionMessage(Pair<Collection<EntityObjectVO>, Long> sources, Exception ex) {
		return SpringLocaleDelegate.getInstance().getMessage(
				"R00022883", "Objektgenerierung fehlgeschlagen. \\n{1}", ex.getMessage());
	}

	@Override
	public void executeFinalAction() throws CommonBusinessException {
		// do nothing
	}
}
