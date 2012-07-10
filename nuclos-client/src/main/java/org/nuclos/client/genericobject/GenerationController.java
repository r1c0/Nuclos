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
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;
import org.nuclos.client.common.EntityCollectController;
import org.nuclos.client.common.MetaDataClientProvider;
import org.nuclos.client.common.NuclosCollectControllerFactory;
import org.nuclos.client.common.security.SecurityCache;
import org.nuclos.client.dal.DalSupportForGO;
import org.nuclos.client.datasource.DatasourceDelegate;
import org.nuclos.client.entityobject.EntityObjectDelegate;
import org.nuclos.client.main.Main;
import org.nuclos.client.main.mainframe.MainFrame;
import org.nuclos.client.main.mainframe.MainFrameTab;
import org.nuclos.client.main.mainframe.MainFrameTabbedPane;
import org.nuclos.client.masterdata.CollectableMasterDataWithDependants;
import org.nuclos.client.masterdata.MasterDataCache;
import org.nuclos.client.masterdata.MasterDataCollectController;
import org.nuclos.client.masterdata.MasterDataDelegate;
import org.nuclos.client.resource.ResourceCache;
import org.nuclos.client.statemodel.StateDelegate;
import org.nuclos.client.ui.CommonClientWorkerAdapter;
import org.nuclos.client.ui.CommonMultiThreader;
import org.nuclos.client.ui.Errors;
import org.nuclos.client.ui.MainFrameTabAdapter;
import org.nuclos.client.ui.MainFrameTabListener;
import org.nuclos.client.ui.OvOpAdapter;
import org.nuclos.client.ui.OvOpListener;
import org.nuclos.client.ui.OverlayOptionPane;
import org.nuclos.client.ui.UIUtils;
import org.nuclos.client.ui.collect.CollectController;
import org.nuclos.client.ui.collect.CollectState;
import org.nuclos.client.ui.collect.InvokeWithInputRequiredSupport;
import org.nuclos.client.ui.collect.component.EntityListOfValues;
import org.nuclos.client.ui.collect.component.ICollectableListOfValues;
import org.nuclos.client.ui.collect.component.LookupEvent;
import org.nuclos.client.ui.collect.component.LookupListener;
import org.nuclos.client.ui.multiaction.MultiActionProgressLine;
import org.nuclos.client.ui.multiaction.MultiActionProgressPanel;
import org.nuclos.client.ui.multiaction.MultiActionProgressResultHandler;
import org.nuclos.client.ui.multiaction.MultiCollectablesActionController;
import org.nuclos.common.NuclosEOField;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.PointerCollection;
import org.nuclos.common.PointerException;
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
import org.nuclos.common2.CommonRunnable;
import org.nuclos.common2.IdUtils;
import org.nuclos.common2.SpringLocaleDelegate;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.server.genericobject.GeneratorFailedException;
import org.nuclos.server.genericobject.ejb3.GenerationResult;
import org.nuclos.server.genericobject.valueobject.GeneratorActionVO;
import org.nuclos.server.masterdata.valueobject.DependantMasterDataMap;
import org.nuclos.server.report.valueobject.DatasourceParameterVO;
import org.nuclos.server.report.valueobject.ValuelistProviderVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

@Configurable
public class GenerationController {

	private static final Logger LOG = Logger.getLogger(GenerationController.class);
	
	//

	private final Map<Long, UsageCriteria> sources;
	private final GeneratorActionVO action;

	private final EntityCollectController<?> parentController;
	private final MainFrameTab parent;

	private MainFrameTab parentForLookup;

	private final List<GenerationListener> listeners = new ArrayList<GenerationController.GenerationListener>();

	private boolean confirmationEnabled = true;
	private boolean headless = false;
	
	private InvokeWithInputRequiredSupport invokeWithInputRequiredSupport;

	private final MainFrameTabListener tabListener = new MainFrameTabAdapter() {
		@Override
		public void tabClosed(MainFrameTab tab) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					parent.unlockLayer();
				}
			});
		}
	};

	public GenerationController(Map<Long, UsageCriteria> sources, GeneratorActionVO action, EntityCollectController<?> parentController, MainFrameTab parent) {
		super();
		this.sources = sources;
		this.action = action;
		this.parentController = parentController;
		this.parent = parent;
	}
	
	@Autowired
	void setInvokeWithInputRequiredSupport(InvokeWithInputRequiredSupport invokeWithInputRequiredSupport) {
		this.invokeWithInputRequiredSupport = invokeWithInputRequiredSupport;
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

	public MainFrameTab getParentForLookup() {
		return parentForLookup;
	}

	public void setParentForLookup(MainFrameTab parentForLookup) {
		this.parentForLookup = parentForLookup;
	}

	/**
	 * generates one or more object(s) from current.
	 * @param generatoractionvo generator action vo to be used for generation
	 */
	public void generateGenericObject() {
		try {
//			SwingUtilities.invokeLater(new Runnable() {
//				@Override
//				public void run() {
//					parent.lockLayer();
//				}
//			});

			final String sTargetModuleName = getModuleLabel(action.getTargetModuleId());
			final String sSourceModuleName = getModuleLabel(action.getSourceModuleId());

			final boolean bMulti = sources.size() > 1;

			final ValuelistProviderVO vlp;
			final Map<String, Object> params = new HashMap<String, Object>();
			if (action.getValuelistProviderId() != null) {
				vlp = DatasourceDelegate.getInstance().getValuelistProvider(action.getValuelistProviderId());
				List<DatasourceParameterVO> parameters = DatasourceDelegate.getInstance().getParametersFromXML(vlp.getSource());
				if (parameters != null && parameters.size() > 0) {
					if (parameters.size() == 1) {
						DatasourceParameterVO parameter = parameters.get(0);
						if (parameter.getParameter().toUpperCase().equals("INTID")) {
							if (!bMulti) {
								params.put(parameter.getParameter(), IdUtils.unsafeToId(sources.keySet().iterator().next()));
							}
							else {
								throw new CommonBusinessException("GenerationController.vlp.intid.singlesource");
							}
						}
						else {
							throw new CommonBusinessException("GenerationController.vlp.wrong.parameter");
						}
					}
					else {
						throw new CommonBusinessException("GenerationController.vlp.wrong.parameter");
					}
				}
			}
			else {
				vlp = null;
			}

			if (isConfirmationEnabled()) {
				confirmGenerationType(bMulti, sSourceModuleName, sTargetModuleName, action, new OvOpAdapter() {
					@Override
					public void done(int result) {
						if (result == OverlayOptionPane.OK_OPTION) {
							run(vlp, params);
						} else {
//							unlock(parent);
						}
					}
				});
			} else {
				run(vlp, params);
			}
		}
		catch (Exception ex) {
			Errors.getInstance().showExceptionDialog(parent, ex);
//			unlock(parent);
		}
	}
	
	private void run(final ValuelistProviderVO vlp, final Map<String, Object> params) {
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

					final MainFrameTab lookupParent = getParentForLookup() != null ? getParentForLookup() : parent;
					final ICollectableListOfValues lov = new EntityListOfValues(lookupParent);
					final CollectController<?> ctl = NuclosCollectControllerFactory.getInstance().newCollectController(pEntityStr, null);
					if (vlp != null) {
						ctl.getSearchStrategy().setValueListProviderDatasource(vlp);
						ctl.getSearchStrategy().setValueListProviderDatasourceParameter(params);
					}
					ctl.getTab().addMainFrameTabListener(tabListener);

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
	
	private void unlock(final MainFrameTab tab) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				tab.unlockLayer();
			}
		});
	}

	public static String getModuleLabel(Integer id) {
		EntityMetaDataVO meta = MetaDataClientProvider.getInstance().getEntity(IdUtils.toLongId(id));
		return SpringLocaleDelegate.getInstance().getLabelFromMetaDataVO(meta);
	}

	/**
	 * Ask the user for confirmation of object generation.
	 * @param bMulti
	 * @param sSourceModuleName
	 * @param sTargetModuleName
	 * @param generatoractionvo
	 * @return selected option
	 */
	private void confirmGenerationType(boolean bMulti, String sSourceModuleName, String sTargetModuleName, GeneratorActionVO generatoractionvo, OvOpListener listener) {
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
				message = SpringLocaleDelegate.getInstance().getMessage(
						"GenericObjectCollectController.71a","Create one or more grouped objects of type \"{1}\" from the selected objects of type \"{0}\"?", sSourceModuleName, sTargetModuleName);
			}
			else {
				message = SpringLocaleDelegate.getInstance().getMessage(
						"GenericObjectCollectController.72","Soll aus den markierten Objekten vom Typ \"{0}\" jeweils ein Objekt vom Typ \"{1}\" erzeugt werden?", sSourceModuleName, sTargetModuleName);
			}
			OverlayOptionPane.showConfirmDialog(parent, message, 
					SpringLocaleDelegate.getInstance().getMessage(
							"GenericObjectCollectController.5","{0} erzeugen", sTargetModuleName), JOptionPane.OK_CANCEL_OPTION, generatoractionvo.getButtonIcon()==null?null:ResourceCache.getInstance().getIconResource(generatoractionvo.getButtonIcon().getId()), listener);
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
			final String sMessage = SpringLocaleDelegate.getInstance().getMessage(
					"GenericObjectCollectController.71","Soll aus dem/der aktuellen {0} ein(e) {1} erzeugt werden?", sSourceModuleName, sTargetModuleName);
			OverlayOptionPane.showConfirmDialog(parent, sMessage, 
					SpringLocaleDelegate.getInstance().getMessage(
							"GenericObjectCollectController.5","{0} erzeugen", sTargetModuleName), JOptionPane.OK_CANCEL_OPTION, generatoractionvo.getButtonIcon()==null?null:ResourceCache.getInstance().getIconResource(generatoractionvo.getButtonIcon().getId()), listener);
		}
		
	}

	/**
	 * Execute object generation once for every single selected leased object.
	 * Performed in an own thread.
	 * @param generatoractionvo
	 */
	public void generate(final List<Long> parameterObjectIds) throws CommonBusinessException {
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
					for (final Pair<Collection<EntityObjectVO>, Long> pair : sourceWithParameters) {
						final HashMap<String, Serializable> context = new HashMap<String, Serializable>();
						final AtomicReference<GenerationResult> result = new AtomicReference<GenerationResult>();
						try {
							invokeWithInputRequiredSupport.invoke(new CommonRunnable() {
								@Override
								public void run() throws CommonBusinessException {
									result.set(GeneratorDelegate.getInstance().generateGenericObject(pair.x, pair.y, action));
								}
							}, context, parent);
							fireGenerationSucessfulEvent(result.get());
							if (!isHeadless()) {
								showGenerationSucessfulResult(result.get());
								if (action.isRefreshSrcObject())
									parentController.refreshCurrentCollectable();
							}
						}
						catch (GeneratorFailedException e) {
							fireGenerationWithExceptionEvent(e);
							if (!isHeadless()) {
								showGenerationWithExceptionResult(e);
								if (action.isRefreshSrcObject()) {
									try {
										parentController.refreshCurrentCollectable();
									}
									catch (CommonBusinessException e2) {
										Errors.getInstance().showExceptionDialog(parent, e2);
									}
								}
							}
						}
						catch (CommonBusinessException e) {
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
									// dead code
									assert false;
									showIncompleteGenericObject(null, result.getGeneratedObject(), action, result.getError(), null);
								}
							}
						}	
					} 
					catch (GeneratorFailedException e) {
						final GenerationResult result = e.getGenerationResult();
						try {
							showIncompleteGenericObject(null, result.getGeneratedObject(), action, result.getError(), null);
						}
						catch (CommonBusinessException e2) {
							Errors.getInstance().showExceptionDialog(parent, e2);
						}
					}
					catch (CommonBusinessException e) {
						Errors.getInstance().showExceptionDialog(parent, e);
					}
				}
			});
			new MultiCollectablesActionController<Pair<Collection<EntityObjectVO>, Long>, GenerationResult>(
				parent, sourceWithParameters, 
				SpringLocaleDelegate.getInstance().getMessage("R00022892", "Objektgenerierung"), parent.getTabIcon(),
				new MultiGenerateAction(parent, action)
			).run(panel);
		}
	}

	private void fireGenerationSucessfulEvent(GenerationResult result) {
		for (GenerationListener l : listeners) {
			l.generatedSucessful(result);
		}
	}

	private void fireGenerationWithExceptionEvent(GeneratorFailedException result) {
		for (GenerationListener l : listeners) {
			l.generatedWithException(result);
		}
	}

	private void showGenerationSucessfulResult(GenerationResult result) {
		try {
			Integer generatedGoId = IdUtils.unsafeToId(result.getGeneratedObject().getId());
			EntityMetaDataVO meta = MetaDataClientProvider.getInstance().getEntity(IdUtils.toLongId(action.getTargetModuleId()));
			if ((meta.isStateModel() && SecurityCache.getInstance().isWriteAllowedForModule(
					Modules.getInstance().getEntityNameByModuleId(action.getTargetModuleId()), generatedGoId))
					|| (!meta.isStateModel() && SecurityCache.getInstance().isWriteAllowedForMasterData(meta.getEntity())) ) {
				
				if (generatedGoId != null) {
					if (action.isShowObject())
						showGenericObject(result.getGeneratedObject(), action.getTargetModuleId());
				}
				else {
					// dead code
					assert false;
					showIncompleteGenericObject(result.getSourceIds(), result.getGeneratedObject(), action, result.getError(), null);
				}
			}
		}
		catch (CommonBusinessException ex) {
			Errors.getInstance().showExceptionDialog(parent, ex);
		}
	}

	private void showGenerationWithExceptionResult(GeneratorFailedException ex) {
		final GenerationResult result = ex.getGenerationResult();
		try {
			EntityMetaDataVO meta = MetaDataClientProvider.getInstance().getEntity(IdUtils.toLongId(action.getTargetModuleId()));
			if ((meta.isStateModel() && SecurityCache.getInstance().isNewAllowedForModule(meta.getEntity()))
					|| (!meta.isStateModel() && SecurityCache.getInstance().isWriteAllowedForMasterData(meta.getEntity())) ) {
				showIncompleteGenericObject(result.getSourceIds(), result.getGeneratedObject(), action, result.getError(), ex.getCause());
			}
		}
		catch (CommonBusinessException e) {
			Errors.getInstance().showExceptionDialog(parent, e);
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
		Main.getInstance().getMainController().showDetails(entity, iGeneratedObjectId);
	}

	/**
	 * Open an incomplete generated object in its own controller.
	 * @param cause 
	 * @throws CommonBusinessException
	 */
	private void showIncompleteGenericObject(Collection<Long> sourceIds, EntityObjectVO result, GeneratorActionVO action, final String message, final Throwable cause) throws CommonBusinessException {
		String entity = result.getEntity();
		MainFrameTabbedPane pane;
		if (MainFrame.isPredefinedEntityOpenLocationSet(entity)) {
			pane = MainFrame.getPredefinedEntityOpenLocation(entity);
		}
		else {
			pane = MainFrame.getTabbedPane(parent);
		}
		EntityMetaDataVO metaVO = MetaDataClientProvider.getInstance().getEntity(entity);
		Map<String, EntityFieldMetaDataVO> mpFields = MetaDataClientProvider.getInstance().getAllEntityFieldsByEntity(entity);

		if (metaVO.isStateModel()) {
			final GenericObjectCollectController goclct = NuclosCollectControllerFactory.getInstance().newGenericObjectCollectController(IdUtils.unsafeToId(metaVO.getId()), null);
			goclct.setCollectState(CollectState.OUTERSTATE_DETAILS, CollectState.DETAILSMODE_NEW_CHANGED);
			goclct.setGenerationSource(sourceIds, action);
			
			CollectableEOEntity meta = new CollectableEOEntity(metaVO, mpFields);
			
			if (result.getFieldId(NuclosEOField.STATE.getName()) == null) {
				UsageCriteria usagecriteria = new UsageCriteria(
						IdUtils.unsafeToId(metaVO.getId()), 
						IdUtils.unsafeToId(result.getFieldId(NuclosEOField.PROCESS.getName())), 
						null);
				result.getFieldIds().put(NuclosEOField.STATE.getName(), 
						IdUtils.toLongId(StateDelegate.getInstance().getStatemodel(usagecriteria).getInitialStateId()));
			}
			
			goclct.unsafeFillDetailsPanel(new CollectableGenericObjectWithDependants(DalSupportForGO.getGenericObjectWithDependantsVO(result, meta)), 
					/*getUsageCriteriaFromClctOnly=*/true);
			goclct.selectTab();
			MainFrame.setSelectedTab(goclct.getTab());

			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					if (cause != null && cause instanceof Exception && PointerException.extractPointerExceptionIfAny((Exception) cause) != null) {
						goclct.setPointerInformation(PointerException.extractPointerExceptionIfAny((Exception) cause).getPointerCollection(), null);
					} else {
						goclct.setPointerInformation(new PointerCollection(
							SpringLocaleDelegate.getInstance().getMessageFromResource(message)), null);
					}
				}
			});
		}
		else {
			final MasterDataCollectController mdclct = NuclosCollectControllerFactory.getInstance().newMasterDataCollectController(metaVO.getEntity(), null);
			CollectableEOEntity meta = new CollectableEOEntity(metaVO, mpFields);
			CollectableMasterDataWithDependants clctmdwd = new CollectableMasterDataWithDependants(meta, DalSupportForMD.getMasterDataWithDependantsVO(result));
			final DependantMasterDataMap deps = result.getDependants();
			clctmdwd.setDependantMasterDataMap(deps);
			mdclct.runNewWith(clctmdwd);
			mdclct.setCollectState(CollectState.OUTERSTATE_DETAILS, CollectState.DETAILSMODE_NEW_CHANGED);
			MainFrame.setSelectedTab(mdclct.getTab());
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					if (cause != null && cause instanceof Exception && PointerException.extractPointerExceptionIfAny((Exception) cause) != null) {
						mdclct.setPointerInformation(PointerException.extractPointerExceptionIfAny((Exception) cause).getPointerCollection(), null);
					} else {
						mdclct.setPointerInformation(new PointerCollection(
							SpringLocaleDelegate.getInstance().getMessageFromResource(message)), null);
					}
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
		
		void generatedSucessful(GenerationResult result);
		
		void generatedWithException(GeneratorFailedException result);
		
	}
}
