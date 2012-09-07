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
package org.nuclos.client.wizard.steps;

import info.clearthought.layout.TableLayout;

import java.util.Collection;

import javax.swing.DefaultCellEditor;
import javax.swing.Icon;
import javax.swing.JComboBox;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableColumn;

import org.apache.log4j.Logger;
import org.nuclos.client.masterdata.MasterDataDelegate;
import org.nuclos.client.wizard.model.EntityRightsSelectTableModel;
import org.nuclos.client.wizard.util.NuclosWizardUtils;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.collect.collectable.CollectableField;
import org.nuclos.common.collect.collectable.LocalizedCollectableValueField;
import org.nuclos.common2.KeyEnum;
import org.nuclos.common2.Localizable;
import org.nuclos.common2.SpringLocaleDelegate;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.server.common.MasterDataPermission;
import org.nuclos.server.common.ModulePermission;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;
import org.pietschy.wizard.InvalidStateException;

/**
* <br>
* Created by Novabit Informationssysteme GmbH <br>
* Please visit <a href="http://www.novabit.de">www.novabit.de</a>
* 
* @author <a href="mailto:marc.finke@novabit.de">Marc Finke</a>
* @version 01.00.00
*/
public class NuclosUserGroupRightsStep extends NuclosEntityAbstractStep {

	private static final Logger LOG = Logger.getLogger(NuclosUserGroupRightsStep.class);

	private JTable table;
	private JScrollPane scroll;
	private EntityRightsSelectTableModel userRightsModel;
	private TableColumn col;
	private JComboBox cmbUserRights;
	
	
	public NuclosUserGroupRightsStep() {	
		initComponents();		
	}

	public NuclosUserGroupRightsStep(String name, String summary) {
		super(name, summary);
		initComponents();
	}

	public NuclosUserGroupRightsStep(String name, String summary, Icon icon) {
		super(name, summary, icon);
		initComponents();
	}
	
	@Override
	protected void initComponents() {
		
		double size [][] = {{ TableLayout.FILL, 10}, {TableLayout.FILL, 10}};
		
		TableLayout layout = new TableLayout(size);
		layout.setVGap(3);
		layout.setHGap(5);
		this.setLayout(layout);
		
		userRightsModel = new EntityRightsSelectTableModel();
		
		table = new JTable(userRightsModel);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);
		
		col = table.getColumnModel().getColumn(1);
		cmbUserRights = new JComboBox();
		
		col.setCellEditor(new DefaultCellEditor(cmbUserRights));
		
		scroll = new JScrollPane();
		scroll.getViewport().add(table);
		
		add(scroll, "0,0");
		
	}

	@Override
	public void close() {
		table = null;
		scroll = null;
		userRightsModel = null;
		col = null;
		cmbUserRights = null;
		
		super.close();
	}

	@Override
	public void applyState() throws InvalidStateException {
		this.model.setUserRights(this.userRightsModel.getUserRights());
		
		super.applyState();		
	}

	@Override
	public void prepare() {
		super.prepare();
		cmbUserRights.removeAllItems();
		cmbUserRights.addItem("");
		try {
			Class<? extends Enum<?>> clazz = (Class<? extends Enum<?>>) 
					Class.forName("org.nuclos.server.common.MasterDataPermission").asSubclass(Enum.class);
			for (Enum<?> e : clazz.getEnumConstants()) {
				// Don't add NO permission
				if (MasterDataPermission.NO.equals(e)) {
					continue;
				}
				Object value = (e instanceof KeyEnum) ? ((KeyEnum<?>) e).getValue() : e.name();
				String text = (e instanceof Localizable) ? 
						SpringLocaleDelegate.getInstance().getText((Localizable) e) : e.toString();
				CollectableField cf = new LocalizedCollectableValueField(value, text);
				cmbUserRights.addItem(cf);
			}
		}
		catch(ClassCastException e) {
			throw new CommonFatalException(e);
		}
        catch(ClassNotFoundException e) {
        	throw new CommonFatalException(e);
        }
        
		if(this.model.isStateModel()) {
			userRightsModel.setType(EntityRightsSelectTableModel.TYPE_STATEMODEL);
			CollectableField cf = new LocalizedCollectableValueField(ModulePermission.DELETE_PHYSICALLY.getValue(), "Lesen/Schreiben/Physikalisch L\u00f6schen");
			cmbUserRights.addItem(cf);			
		}
		else {
			userRightsModel.setType(EntityRightsSelectTableModel.TYPE_MASTERDATA);
		}
		if(this.model.getUserRights().size() > 0) {
			Collection<MasterDataVO> colVO = MasterDataDelegate.getInstance().getMasterData(NuclosEntity.ROLE.getEntityName());
			userRightsModel.clear();
			for(MasterDataVO ur : this.model.getUserRights()) {				
				this.userRightsModel.addRole(ur);
			}
			
			for(MasterDataVO voRole : colVO) {
				boolean add = true;
				for(MasterDataVO voEntityRole : userRightsModel.getUserRights()) {
					String role = (String)voRole.getField("name");
					if(role.equals(voEntityRole.getField("role"))) {
						add = false;
						break;
					}
				}
				if(add) {
					MasterDataVO voAdd = null;
					voAdd = NuclosWizardUtils.setFieldsForUserRight(voRole, (String)voRole.getField("name"), voAdd, this.model);
					this.userRightsModel.addRole(voAdd);
				}
			}		
		}		
		
	}

}
