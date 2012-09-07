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
package org.nuclos.client.relation;

import info.clearthought.layout.TableLayout;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;

import org.nuclos.client.common.LafParameterProvider;
import org.nuclos.client.common.MetaDataClientProvider;
import org.nuclos.client.common.NuclosCollectController;
import org.nuclos.client.common.NuclosResultPanel;
import org.nuclos.client.main.Main;
import org.nuclos.client.main.mainframe.IconResolver;
import org.nuclos.client.main.mainframe.IconResolverConstants;
import org.nuclos.client.main.mainframe.MainFrame;
import org.nuclos.client.main.mainframe.MainFrameTab;
import org.nuclos.client.masterdata.CollectableMasterData;
import org.nuclos.client.masterdata.MasterDataDelegate;
import org.nuclos.client.masterdata.MetaDataDelegate;
import org.nuclos.client.ui.collect.CollectPanel;
import org.nuclos.client.ui.collect.DefaultEditView;
import org.nuclos.client.ui.collect.component.CollectableTextField;
import org.nuclos.client.ui.collect.result.ResultPanel;
import org.nuclos.common.LafParameter;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.collect.collectable.CollectableValueField;
import org.nuclos.common.collection.Pair;
import org.nuclos.common.dal.vo.EntityMetaDataVO;
import org.nuclos.common.masterdata.CollectableMasterDataEntity;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.server.masterdata.valueobject.MasterDataMetaVO;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;
import org.w3c.dom.Document;

import com.mxgraph.io.mxCodec;
import com.mxgraph.util.mxUtils;
import com.mxgraph.util.mxXmlUtils;

/**
 * Controller for collecting state models.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */
public class EntityRelationShipCollectController extends NuclosCollectController<EntityRelationshipModel> {
	
	final CollectableTextField clcttfName = new CollectableTextField(
		EntityRelationshipModel.clcte.getEntityField("name"));

	final CollectableTextField clcttfDescription = new CollectableTextField(
		EntityRelationshipModel.clcte.getEntityField("description"));

	private final CollectPanel<EntityRelationshipModel> pnlCollect = new EntityRelationshipCollectPanel(MetaDataClientProvider.getInstance().getEntity(EntityRelationshipModel.clcte.getName()).getId(), false);
	private final EntityRelationshipModelEditPanel pnlEdit;
	
	/**
	 * You should use {@link org.nuclos.client.ui.collect.CollectControllerFactorySingleton} 
	 * to get an instance.
	 * 
	 * @deprecated You should normally do sth. like this:<code><pre>
	 * ResultController<~> rc = new ResultController<~>();
	 * *CollectController<~> cc = new *CollectController<~>(.., rc);
	 * </code></pre>
	 */
	public EntityRelationShipCollectController(MainFrame mf, MainFrameTab tabIfAny) {
		super(EntityRelationshipModel.clcte, tabIfAny);
		
		this.initialize(this.pnlCollect);
		
		//this.mf = mf;
		
		getTab().setLayeredComponent(pnlCollect);
		
		pnlEdit = new EntityRelationshipModelEditPanel(mf);
		
		this.getDetailsPanel().setEditView(DefaultEditView.newDetailsEditView(pnlEdit, pnlEdit.newCollectableComponentsProvider()));
		
		getTab().setTitle(getSpringLocaleDelegate().getMessage("nuclos.entityrelation.controller.2","Relationen Editor"));
		//final JPanel pnlCustomToolBarAreaDetails = new JPanel();		
		//pnlCustomToolBarAreaDetails.setLayout(new BorderLayout());
		//pnlCustomToolBarAreaDetails.setPreferredSize(new Dimension(25,25));
		JButton bt = new JButton(getSpringLocaleDelegate().getMessage(
				"nuclos.entityrelation.controller.3", "Entit\u00e4ten ausw\u00e4hlen"));
		//JToolBar bar = UIUtils.createNonFloatableToolBar();
		//bar.setOpaque(false);
		//bar.add(bt);
		//bar.add(Box.createHorizontalGlue());
		
		bt.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				showEntityDialog();				
			}
		});
		
		//pnlCustomToolBarAreaDetails.add(bar, BorderLayout.CENTER);		
	
		//this.getDetailsPanel().setCustomToolBarArea(pnlCustomToolBarAreaDetails);
		this.getDetailsPanel().addToolBarComponent(bt);
	}
	
	@Override
	protected void cmdEnterNewMode() {
		super.cmdEnterNewMode();
		pnlEdit.getGraphModel().clear();
		showEntityDialog();
	}
	
	private void showEntityDialog() {
		
		clcttfName.setField(new CollectableValueField(null));
		clcttfDescription.setField(new CollectableValueField(null));
		List<EntityMetaDataVO> lstInModel = pnlEdit.getEntitiesInModel();
		
		double cells [][] = {{5, TableLayout.PREFERRED, 5}, {5, TableLayout.PREFERRED,5}};	
		
		JDialog dia = new JDialog(Main.getInstance().getMainFrame(), getSpringLocaleDelegate().getMessage(
				"nuclos.entityrelation.controller.1", "Entit\u00e4ten f\u00fcr Datenmodell ausw\u00e4hlen"), true);
		EntityChoicePanel panel = new EntityChoicePanel(dia, lstInModel);
		dia.setLayout(new TableLayout(cells));
		dia.add(panel, "1,1");
		dia.pack();
		dia.setLocationRelativeTo(this.getTab());
		dia.setVisible(true);
	
		List<EntityMetaDataVO> lstEntites = panel.getSelectedEntites();
		pnlEdit.showDataModel(lstEntites, true);
	}

	/**
	 * @deprecated Move to DetailsController hierarchy and make protected again.
	 */
	@Override
	public void addAdditionalChangeListenersForDetails() {
		this.pnlEdit.addChangeListener(this.changelistenerDetailsChanged);		
	}

	/**
	 * @deprecated Move to DetailsController hierarchy and make protected again.
	 */
	@Override
	public void removeAdditionalChangeListenersForDetails() {
		this.pnlEdit.removeChangeListener(this.changelistenerDetailsChanged);
	}


	@Override
	protected void deleteCollectable(EntityRelationshipModel clct)	throws CommonBusinessException {
		
		MasterDataDelegate.getInstance().remove(NuclosEntity.ENTITYRELATION.getEntityName(), clct.getMasterDataVO(), getCustomUsage());
		clcttfName.setField(new CollectableValueField(null));
		clcttfDescription.setField(new CollectableValueField(null));
		pnlEdit.getGraphModel().clear();
		pnlEdit.clearModel();
	
	}

	@Override
	public EntityRelationshipModel findCollectableById(String sEntity, Object oId) throws CommonBusinessException {
		
		MasterDataVO vo = MasterDataDelegate.getInstance().get(NuclosEntity.ENTITYRELATION.getEntityName(), oId);
		
		EntityRelationshipModel model = new EntityRelationshipModel(MetaDataDelegate.getInstance().getEntityRelationshipModelVO(vo));
		
		byte[] b = (byte[])vo.getField("layout");
		String xml = new String(b);
		model.setXMLModel(xml);
		
		return model;
	}

	@Override
	protected EntityRelationshipModel findCollectableByIdWithoutDependants(String sEntity, Object oId) throws CommonBusinessException {
		return findCollectableById(sEntity, oId);
	}

	@Override
	protected String getEntityLabel() {
		return getSpringLocaleDelegate().getText("nuclos.entity.entityrelation.label", "Relationen Editor") ;
	}

	@Override
	protected EntityRelationshipModel insertCollectable(EntityRelationshipModel clctNew) throws CommonBusinessException {
				
		MasterDataMetaVO metaVO = MasterDataDelegate.getInstance().getMetaData(NuclosEntity.ENTITYRELATION.getEntityName());
		CollectableMasterDataEntity masterDataEntity = new CollectableMasterDataEntity(metaVO);
		CollectableMasterData masterData = new CollectableMasterData(masterDataEntity, new MasterDataVO(masterDataEntity.getMasterDataMetaCVO(), false));
		MasterDataVO mdvo = masterData.getMasterDataCVO();
		mdvo.setField("name", clctNew.getField("name").getValue());
		mdvo.setField("description", clctNew.getField("description").getValue());
		
		String xml = getXML();
		
		mdvo.setField("layout", xml.getBytes());		
		
		MasterDataVO vo = MasterDataDelegate.getInstance().create(NuclosEntity.ENTITYRELATION.getEntityName(), mdvo, null, getCustomUsage());
		
		pnlEdit.clearModel();
		
				
		EntityRelationshipModel model = new EntityRelationshipModel(MetaDataDelegate.getInstance().getEntityRelationshipModelVO(vo));
		model.setXMLModel(xml);
		
		return model;
	}
	
	

	@Override
	public EntityRelationshipModel newCollectable() {
		clcttfName.setField(new CollectableValueField(null));
		clcttfDescription.setField(new CollectableValueField(null));
		EntityRelationshipModel cltl = new EntityRelationshipModel();			
		return cltl;		
	}
	
	private String getXML() {
		mxCodec codec = new mxCodec();
		String xml = mxUtils.getXml(codec.encode(pnlEdit.getGraphComponent().getGraph().getModel()));
		return xml;
	}

	@Override
	protected EntityRelationshipModel updateCollectable(EntityRelationshipModel clct, Object oAdditionalData) throws CommonBusinessException {
		MasterDataVO mdvo = clct.getMasterDataVO();		
		String xml = getXML();
		mdvo.setField("layout", xml.getBytes());		
		Integer iId = (Integer)MasterDataDelegate.getInstance().update(NuclosEntity.ENTITYRELATION.getEntityName(), mdvo, null, getCustomUsage());
		pnlEdit.clearModel();
		EntityRelationshipModel model = findCollectableById(NuclosEntity.ENTITYRELATION.getEntityName(), iId);
		return model;
	}
	
	@Override
	protected void unsafeFillDetailsPanel(EntityRelationshipModel clct)	throws CommonBusinessException {
		super.unsafeFillDetailsPanel(clct);
		
		if(clct.getXMLModel() != null) {
			Document document = mxXmlUtils.parseXml(clct.getXMLModel());
	
			mxCodec codec = new mxCodec(document);
			codec.decode(document.getDocumentElement(), pnlEdit.getGraphComponent().getGraph().getModel());
			pnlEdit.fireChangeListenEvent();
			pnlEdit.loadReferenz();
			pnlEdit.removeNotExistentEntitiesFromModel();
		}
		
	}

	@Override
	protected EntityRelationshipModel newCollectableWithDefaultValues() {
		return new EntityRelationshipModel();
	}

	
	private class EntityRelationshipCollectPanel extends CollectPanel<EntityRelationshipModel> {

		EntityRelationshipCollectPanel(Long entityId, boolean bSearchPanelAvailable) {
			super(entityId, bSearchPanelAvailable, LafParameterProvider.getInstance().getValue(LafParameter.nuclos_LAF_Details_Overlay, entityId));
		}

		@Override
		public ResultPanel<EntityRelationshipModel> newResultPanel(Long entityId) {
			return new NuclosResultPanel<EntityRelationshipModel>(entityId);
		}
	}
	
	@Override
	protected boolean isDeleteAllowed(EntityRelationshipModel clct) {
		return true;
	}

	@Override
	protected boolean isDeleteSelectedCollectableAllowed() {
		return true;
	}

	@Override
	protected boolean isCloneAllowed() {
		return false;
	}

	@Override
	protected boolean isNewAllowed() {
		return true;
	}
	
	@Override
	public Pair<IconResolver, String> getIconAndResolver() {
		return new Pair<IconResolver, String>(IconResolverConstants.NUCLOS_RESOURCE_ICON_RESOLVER, "org.nuclos.client.resource.icon.glyphish-blue.55-network.png");
	}
	
}	// class StateModelCollectController