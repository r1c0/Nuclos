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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;
import org.nuclos.client.common.EntityCollectController;
import org.nuclos.client.common.MetaDataClientProvider;
import org.nuclos.client.common.NuclosCollectControllerFactory;
import org.nuclos.client.common.security.SecurityCache;
import org.nuclos.client.dal.DalSupportForGO;
import org.nuclos.client.entityobject.EntityObjectDelegate;
import org.nuclos.client.main.Main;
import org.nuclos.client.main.mainframe.MainFrame;
import org.nuclos.client.main.mainframe.MainFrameTab;
import org.nuclos.client.masterdata.CollectableMasterDataWithDependants;
import org.nuclos.client.masterdata.MasterDataCache;
import org.nuclos.client.masterdata.MasterDataCollectController;
import org.nuclos.client.masterdata.MasterDataDelegate;
import org.nuclos.client.ui.CommonClientWorkerAdapter;
import org.nuclos.client.ui.CommonMultiThreader;
import org.nuclos.client.ui.Errors;
import org.nuclos.client.ui.UIUtils;
import org.nuclos.client.ui.collect.CollectController;
import org.nuclos.client.ui.collect.CollectState;
import org.nuclos.client.ui.collect.component.EntityListOfValues;
import org.nuclos.client.ui.collect.component.ICollectableListOfValues;
import org.nuclos.client.ui.collect.component.LookupEvent;
import org.nuclos.client.ui.collect.component.LookupListener;
import org.nuclos.client.ui.multiaction.MultiActionProgressLine;
import org.nuclos.client.ui.multiaction.MultiActionProgressPanel;
import org.nuclos.client.ui.multiaction.MultiActionProgressResultHandler;
import org.nuclos.client.ui.multiaction.MultiCollectablesActionController;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.PointerCollection;
import org.nuclos.common.UsageCriteria;
import org.nuclos.common.collect.collectable.Collectable;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.Pair;
import org.nuclos.common.collection.Transformer;
import org.nuclos.common.dal.DalSupportForMD;
import org.nuclos.common.dal.vo.EntityFieldMetaDataVO;
import org.nuclos.common.dal.vo.EntityMetaDataVO;
import org.nuclos.common.dal.vo.EntityObjectVO;
import org.nuclos.common.entityobject.CollectableEOEntity;
import org.nuclos.common2.CommonLocaleDelegate;
import org.nuclos.common2.CommonRunnable;
import org.nuclos.common2.IdUtils;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.server.genericobject.ejb3.GenerationResult;
import org.nuclos.server.genericobject.valueobject.GeneratorActionVO;

public class GenerationController {

	private static final Logger LOG = Logger.getLogger(GenerationController.class);

	private final Map<Long, UsageCriteria> sources;
	private final GeneratorActionVO action;

	private final EntityCollectController<?> parentController;
	private final MainFrameTab parent;
	private final JTabbedPane pane;

	private final List<GenerationListener> listeners = new ArrayList<GenerationController.GenerationListener>();

	private boolean confirmationEnabled = true;
	private boolean headless = false;

	public GenerationController(Map<Long, UsageCriteria> sources, GeneratorActionVO action, EntityCollectController<?> parentController, MainFrameTab parent) {
		this(sources, action, parentController, parent, parent.getTabbedPane());
	}

	public GenerationController(Map<Long, UsageCriteria> sources, GeneratorActionVO action, EntityCollectController<?> parentController, MainFrameTab parent, JTabbedPane pane) {
		super();
		this.sources = sources;
		this.action = action;
		this.parentController = parentController;
		this.parent = parent;
		this.pane = pane;
	}

	public boolean isConfirmationEnabled() {
		return confirmationEnabled;
	}

	public void setConfirmationEnabled(boolean confirmationEnabled) {
		this.confirmationEnabled = confirmationEnabled;
	}

	public boolean isHeadless() {
		return headless;
	}

	public void setHeadless(boolean headless) {
		this.headless = headless;
	}

	public void addGenerationListener(GenerationListener l) {
		listeners.add(l);
	}

	public void removeGenerationListener(GenerationListener l) {
		listeners.remove(l);
	}

	/**
	 * generates one or more object(s) from current.
	 * @param generatoractionvo generator action vo to be used for generation
	 */
	public void generateGenericObject() {
		try {
			final String sTargetModuleName = getModuleLabel(action.getTargetModuleId());
			final String sSourceModuleName = getModuleLabel(action.getSourceModuleId());

			final boolean bMulti = sources.size() > 1;

			final int iBtn = isConfirmationEnabled() ? confirmGenerationType(bMulti, sSourceModuleName, sTargetModuleName, action) : JOptionPane.OK_OPTION;

			if (iBtn != JOptionPane.CANCEL_OPTION && iBtn != JOptionPane.CLOSED_OPTION) {
				final AtomicReference<List<Long>> parameterObjectIdRef = new AtomicReference<List<Long>>();
				final CommonRunnable generateRunnable = new CommonRunnable() {
					@Override
					public void run() throws CommonBusinessException {
						List<Long> parameterObjectId = parameterObjectIdRef.get();
						generate(parameterObjectId);
					}
				};

				if (action.getParameterEntityId() != null) {
					UIUtils.runShortCommand(parent, new CommonRunnable() {
						@Override
						public void run() throws CommonBusinessException {
							final EntityMetaDataVO parameterEntity =
								MetaDataClientProvider.getInstance().getEntity(action.getParameterEntityId().longValue());
							final String pEntityStr = parameterEntity.getEntity();

							final ICollectableListOfValues lov = new EntityListOfValues(parent);
							final CollectController<?> ctl = NuclosCollectControllerFactory.getInstance().newCollectController(parent, pEntityStr, null);
							lov.addLookupListener(new LookupListener() {
								@Override
								public void lookupSuccessful(LookupEvent ev) {
									if (ev.getAdditionalCollectables() != null && ev.getAdditionalCollectables().size() > 0) {
										List<Long> parameterIds = CollectionUtils.transform(ev.getAdditionalCollectables(), new Transformer<Collectable, Long>() {
											@Override
											public Long transform(Collectable i) {
												return IdUtils.toLongId(i.getId());
											}
										});
										parameterIds.add(IdUtils.toLongId(ev.getSelectedCollectable().getId()));
										parameterObjectIdRef.set(parameterIds);
									}
									else {
										Collectable clct = ev.getSelectedCollectable();
										if (clct != null) {
											parameterObjectIdRef.set(Collections.singletonList(IdUtils.toLongId((Integer) clct.getId())));
										}
									}
									UIUtils.runShortCommand(parent, generateRunnable);
								}
								@Override
								public int getPriority() {
									return 1;
								}
							});
							ctl.runLookupCollectable(lov);
						}
					});
				}
				else {
					UIUtils.runShortCommand(parent, generateRunnable);
				}
			}
		}
		catch (Exception ex) {
			Errors.getInstance().showExceptionDialog(parent, ex);
		}
	}

	public static String getModuleLabel(Integer id) {
		EntityMetaDataVO meta = MetaDataClientProvider.getInstance().getEntity(IdUtils.toLongId(id));
		return CommonLocaleDelegate.getLabelFromMetaDataVO(meta);
	}

	/**
	 * Ask the user for confirmation of object generation.
	 * @param bMulti
	 * @param sSourceModuleName
	 * @param sTargetModuleName
	 * @param generatoractionvo
	 * @return selected option
	 */
	private int confirmGenerationType(boolean bMulti, String sSourceModuleName, String sTargetModuleName, GeneratorActionVO generatoractionvo) {
		final int iBtn;
		if (generatoractionvo.getTargetProcessId() != null) {
			try {
				sTargetModuleName = MessageFormat.format("{0} ({1})", sTargetModuleName, MasterDataCache.getInstance().get(NuclosEntity.PROCESS.getEntityName(), generatoractionvo.getTargetProcessId()).getField("name", String.class));
			} catch (CommonFinderException e) {
				LOG.error("Unable to determine target process name.", e);
			}
		}
		if (bMulti) {
			String message;
			if(generatoractionvo.isGroupAttributes()) {
				message = CommonLocaleDelegate.getMessage("GenericObjectCollectController.71a","Create one or more grouped objects of type \"{1}\" from the selected objects of type \"{0}\"?", sSourceModuleName, sTargetModuleName);
			}
			else {
				message = CommonLocaleDelegate.getMessage("GenericObjectCollectController.72","Soll aus den markierten Objekten vom Typ \"{0}\" jeweils ein Objekt vom Typ \"{1}\" erzeugt werden?", sSourceModuleName, sTargetModuleName);
			}
			iBtn = JOptionPane.showConfirmDialog(this.pane, message, CommonLocaleDelegate.getMessage("GenericObjectCollectController.5","{0} erzeugen", sTargetModuleName), JOptionPane.OK_CANCEL_OPTION);
		}
		else {
			UsageCriteria uc = sources.values().iterator().next();
			if (uc != null && uc.getProcessId() != null) {
				Integer processId = sources.values().iterator().next().getProcessId();
				String process;
				try {
					process = MasterDataDelegate.getInstance().get(NuclosEntity.PROCESS.getEntityName(), processId).getField("name", String.class);
					sSourceModuleName = MessageFormat.format("{0} ({1})", sSourceModuleName, process);
				} catch (CommonBusinessException e) {
					LOG.error(e);
				}
			}
			final String sMessage = CommonLocaleDelegate.getMessage("GenericObjectCollectController.71","Soll aus dem/der aktuellen {0} ein(e) {1} erzeugt werden?", sSourceModuleName, sTargetModuleName);
			iBtn = JOptionPane.showConfirmDialog(this.pane, sMessage, CommonLocaleDelegate.getMessage("GenericObjectCollectController.5","{0} erzeugen", sTargetModuleName), JOptionPane.OK_CANCEL_OPTION);
		}
		return iBtn;
	}

	/**
	 * Execute object generation once for every single selected leased object.
	 * Performed in an own thread.
	 * @param generatoractionvo
	 */
	private void generate(final List<Long> parameterObjectIds) throws CommonBusinessException {
		if (parentController != null) {
			CommonMultiThreader.getInstance().execute(new GenerationClientWorker(parentController, parameterObjectIds));
		}
		else  {
			generateImpl(parameterObjectIds);
		}
	}

	private void generateImpl(final List<Long> parameterObjectIds) throws CommonBusinessException {
		final Collection<Collection<EntityObjectVO>> sources;

		if (action.isGroupAttributes()) {
			sources = GeneratorDelegate.getInstance().groupObjects(this.sources.keySet(), action).values();
		}
		else {
			String sourceEntity = MetaDataClientProvider.getInstance().getEntity(IdUtils.toLongId(action.getSourceModuleId())).getEntity();
			sources = new ArrayList<Collection<EntityObjectVO>>();
			for (Long i : this.sources.keySet()) {
				sources.add(Collections.singleton(EntityObjectDelegate.getInstance().get(sourceEntity, i)));
			}
		}

		final Collection<Pair<Collection<EntityObjectVO>, Long>> sourceWithParameters = new ArrayList<Pair<Collection<EntityObjectVO>,Long>>();

		for (Collection<EntityObjectVO> sourceGroup : sources) {
			if (parameterObjectIds != null && parameterObjectIds.size() > 0) {
				for (Long parameterObjectId : parameterObjectIds) {
					sourceWithParameters.add(new Pair<Collection<EntityObjectVO>, Long>(sourceGroup, parameterObjectId));
				}
			}
			else {
				sourceWithParameters.add(new Pair<Collection<EntityObjectVO>, Long>(sourceGroup, null));
			}
		}

		if (sourceWithParameters.size() == 1 || isHeadless()) {
			UIUtils.runCommandLater(parent, new Runnable() {

				@Override
				public void run() {
					for (Pair<Collection<EntityObjectVO>, Long> pair : sourceWithParameters) {
						GenerationResult result;
						try {
							result = GeneratorDelegate.getInstance().generateGenericObject(pair.x, pair.y, action);
							fireGenerationEvent(result);
							if (!isHeadless()) {
								showResult(result);
							}
						} catch (CommonBusinessException e) {
							Errors.getInstance().showExceptionDialog(parent, e);
						}
					}
				}
			});
		}
		else {
			MultiActionProgressPanel panel = new MultiActionProgressPanel(sourceWithParameters.size());
			panel.setResultHandler(new MultiActionProgressResultHandler(null) {
				@Override
				public void handleMultiSelection(Collection<MultiActionProgressLine> selection) {
					try {
						for (MultiActionProgressLine o : selection) {
							if (o.getResultObject() instanceof GenerationResult) {
								GenerationResult result = (GenerationResult) o.getResultObject();
								if (result.getGeneratedObject().getId() != null) {
									showGenericObject(result.getGeneratedObject(), action.getTargetModuleId());
								}
								else {
									showIncompleteGenericObject(null, result.getGeneratedObject(), result.getError());
								}
							}
						}

					} catch (CommonBusinessException e) {
						Errors.getInstance().showExceptionDialog(pane, e);
					}
				}
			});
			new MultiCollectablesActionController<Pair<Collection<EntityObjectVO>, Long>, GenerationResult>(
				parent, sourceWithParameters, CommonLocaleDelegate.getMessage("R00022892", "Objektgenerierung"), parent.getTabIcon(),
				new MultiGenerateAction(action)
			).run(panel);
		}
	}

	private void fireGenerationEvent(GenerationResult result) {
		for (GenerationListener l : listeners) {
			l.generated(result);
		}
	}

	private void showResult(GenerationResult result) {
		try {
			Integer generatedGoId = IdUtils.unsafeToId(result.getGeneratedObject().getId());
			EntityMetaDataVO meta = MetaDataClientProvider.getInstance().getEntity(IdUtils.toLongId(action.getTargetModuleId()));
			if ((meta.isStateModel() && SecurityCache.getInstance().isWriteAllowedForModule(Modules.getInstance().getEntityNameByModuleId(action.getTargetModuleId()), generatedGoId))
					|| (!meta.isStateModel() && SecurityCache.getInstance().isWriteAllowedForMasterData(meta.getEntity())) ) {
				if (generatedGoId != null) {
					showGenericObject(result.getGeneratedObject(), action.getTargetModuleId());
				}
				else {
					showIncompleteGenericObject(result.getSourceIds(), result.getGeneratedObject(), result.getError());
				}
			}
		}
		catch (CommonBusinessException ex) {
			Errors.getInstance().showExceptionDialog(pane, ex);
		}
	}

	/**
	 * Open the generated leased object in an own controller, if possible.
	 * @param iGeneratedObjectId
	 * @param iModuleId
	 * @throws CommonBusinessException
	 */
	private void showGenericObject(EntityObjectVO result, final Integer iModuleId) throws CommonBusinessException {
		Long iGeneratedObjectId = result.getId();

		String entity = MetaDataClientProvider.getInstance().getEntity(IdUtils.toLongId(action.getTargetModuleId())).getEntity();
		Main.getMainController().showDetails(entity, iGeneratedObjectId);
	}

	/**
	 * Open an incomplete generated object in its own controller.
	 * @throws CommonBusinessException
	 */
	private void showIncompleteGenericObject(Collection<Long> sourceIds, EntityObjectVO result, final String message) throws CommonBusinessException {
		String entity = result.getEntity();
		JTabbedPane pane;
		if (MainFrame.isPredefinedEntityOpenLocationSet(entity)) {
			pane = MainFrame.getPredefinedEntityOpenLocation(entity);
		}
		else {
			pane = this.pane;
		}
		EntityMetaDataVO metaVO = MetaDataClientProvider.getInstance().getEntity(entity);
		Map<String, EntityFieldMetaDataVO> mpFields = MetaDataClientProvider.getInstance().getAllEntityFieldsByEntity(entity);

		if (metaVO.isStateModel()) {
			final GenericObjectCollectController goclct = NuclosCollectControllerFactory.getInstance().newGenericObjectCollectController(pane, IdUtils.unsafeToId(metaVO.getId()), null);
			goclct.setCollectState(CollectState.OUTERSTATE_DETAILS, CollectState.DETAILSMODE_NEW_CHANGED);
			goclct.setGenerationSourceIds(sourceIds);
			CollectableEOEntity meta = new CollectableEOEntity(metaVO, mpFields);
			goclct.unsafeFillDetailsPanel(new CollectableGenericObjectWithDependants(DalSupportForGO.getGenericObjectWithDependantsVO(result, meta)));
			goclct.showFrame();
			MainFrame.setSelectedTab(goclct.getFrame());
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					goclct.setPointerInformation(new PointerCollection(CommonLocaleDelegate.getMessageFromResource(message)), null);
				}
			});
		}
		else {
			final MasterDataCollectController mdclct = NuclosCollectControllerFactory.getInstance().newMasterDataCollectController(pane, metaVO.getEntity(), null);
			CollectableEOEntity meta = new CollectableEOEntity(metaVO, mpFields);
			mdclct.runNewWith(new CollectableMasterDataWithDependants(meta, DalSupportForMD.getMasterDataWithDependantsVO(result)));
			mdclct.setCollectState(CollectState.OUTERSTATE_DETAILS, CollectState.DETAILSMODE_NEW_CHANGED);
			MainFrame.setSelectedTab(mdclct.getFrame());
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					mdclct.setPointerInformation(new PointerCollection(CommonLocaleDelegate.getMessageFromResource(message)), null);
				}
			});
		}
	}

	private class GenerationClientWorker<T extends Collectable> extends CommonClientWorkerAdapter<T> {

		private final List<Long> parameterObjectIds;

		public GenerationClientWorker(EntityCollectController<T> ctl, List<Long> parameterObjectIds) {
			super(ctl);
			this.parameterObjectIds = parameterObjectIds;
		}

		@Override
		public void init() throws CommonBusinessException {
			super.init();
		}

		@Override
		public void work() throws CommonBusinessException {
			generateImpl(parameterObjectIds);
		}
	}

	public interface GenerationListener {
		public void generated(GenerationResult result);
	}
}
