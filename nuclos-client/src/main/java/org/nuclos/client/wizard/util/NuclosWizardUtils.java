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
package org.nuclos.client.wizard.util;

import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.text.JTextComponent;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.nuclos.client.common.LocaleDelegate;
import org.nuclos.client.common.MetaDataClientProvider;
import org.nuclos.client.masterdata.CollectableMasterData;
import org.nuclos.client.masterdata.MasterDataDelegate;
import org.nuclos.client.masterdata.MetaDataCache;
import org.nuclos.client.masterdata.MetaDataDelegate;
import org.nuclos.client.wizard.NuclosEntityWizardStaticModel;
import org.nuclos.client.wizard.model.DataTyp;
import org.nuclos.common.NuclosEOField;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.SearchConditionUtils;
import org.nuclos.common.collect.collectable.searchcondition.CollectableComparison;
import org.nuclos.common.collect.collectable.searchcondition.ComparisonOperator;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.Predicate;
import org.nuclos.common.collection.Transformer;
import org.nuclos.common.dal.vo.EntityFieldMetaDataVO;
import org.nuclos.common.dal.vo.EntityMetaDataVO;
import org.nuclos.common.masterdata.CollectableMasterDataEntity;
import org.nuclos.common2.CommonLocaleDelegate;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.common2.exception.CommonPermissionException;
import org.nuclos.common2.layoutml.LayoutMLParser;
import org.nuclos.common2.layoutml.exception.LayoutMLException;
import org.nuclos.server.masterdata.valueobject.MasterDataMetaVO;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;
import org.xml.sax.InputSource;


/**
* <br>
* Created by Novabit Informationssysteme GmbH <br>
* Please visit <a href="http://www.novabit.de">www.novabit.de</a>
*
* @author <a href="mailto:marc.finke@novabit.de">Marc Finke</a>
* @version 01.00.00
*/

public class NuclosWizardUtils {

	public static String COLUMN_PREFFIX = "c_";
	public static String COLUMN_STRING_PREFFIX = "STR";
	public static String COLUMN_INTEGER_PREFFIX = "INT";
	public static String COLUMN_DATE_PREFFIX = "DAT";
	public static String COLUMN_BOOLEAN_PREFFIX = "BLN";
	public static String COLUMN_DOUBLE_PREFFIX = "DBL";
	public static String COLUMN_OBJECT_PREFFIX = "OBJ";

	public static DataTyp getDataTyp(String javaType, Integer scale, Integer precision, String inputFormat, String outputFormat) throws CommonFinderException, CommonPermissionException{
		DataTyp typ = null;
		Collection<MasterDataVO> colMasterData = MasterDataDelegate.getInstance().getMasterData(NuclosEntity.DATATYP.getEntityName());

		List<MasterDataVO> lstVO = new ArrayList<MasterDataVO>(colMasterData);
		Collections.sort(lstVO, new Comparator<MasterDataVO>() {

			@Override
			public int compare(MasterDataVO o1, MasterDataVO o2) {
				return ((String)o1.getField("name")).compareTo((String)o2.getField("name"));
			}

		});

		for(MasterDataVO vo : lstVO) {
			String strJavaTyp = (String)vo.getField("javatyp");
			String strOutputFormat = (String)vo.getField("outputformat");
			String strInputFormat = (String)vo.getField("inputformat");
			Integer iScale = (Integer)vo.getField("scale");
			if(iScale != null && iScale.intValue() == 0)
				iScale = null;
			Integer iPrecision = (Integer)vo.getField("precision");
			if(iPrecision != null && iPrecision.intValue() == 0)
				iPrecision = null;

			String strDatabaseTyp = (String)vo.getField("databasetyp");
			String strName = (String)vo.getField("name");
			if(strName.equals("Referenz Feld"))
				continue;

			if(StringUtils.equals(javaType, strJavaTyp) && StringUtils.equals(outputFormat, strOutputFormat) &&
				/*StringUtils.equals(inputFormat, strInputFormat) &&*/ ObjectUtils.equals(scale, iScale) &&
				ObjectUtils.equals(precision, iPrecision)) {
				typ = new DataTyp(strName, strInputFormat, strOutputFormat, strDatabaseTyp, iScale, iPrecision, strJavaTyp);
				break;
			}
		}
		if(typ == null) {
			 typ = new DataTyp("Unknown", inputFormat, outputFormat, null, scale, precision, javaType);
		}
		return typ;
	}

	public static Set<String> searchParentEntity(String sEntity) {
		Set<String> setParents = new HashSet<String>();

		for(MasterDataVO vo : MasterDataDelegate.getInstance().getMasterData(NuclosEntity.LAYOUT.getEntityName())) {
			LayoutMLParser parser = new LayoutMLParser();
			try {
	            String sLayout = (String)vo.getField("layoutML");
	            if(sLayout == null)
	            	continue;
				Set<String> setSubforms = parser.getSubFormEntityNames(new InputSource(new StringReader(sLayout)));
	            if(setSubforms.contains(sEntity)) {
					CollectableComparison compare = SearchConditionUtils.newComparison(NuclosEntity.LAYOUTUSAGE.getEntityName(), "layout", ComparisonOperator.EQUAL, vo.getField("name"));
					for(MasterDataVO voUsage : MasterDataDelegate.getInstance().getMasterData(NuclosEntity.LAYOUTUSAGE.getEntityName(), compare)) {
						setParents.add((String)voUsage.getField("entity"));
					}
	            }
            }
            catch(LayoutMLException e) {
	            // do nothing here
            }

		}


		return setParents;
	}

	public static FocusAdapter createWizardFocusAdapter() {
		return new FocusAdapter() {

			@Override
			public void focusGained(FocusEvent e) {
				if(e.getSource() instanceof JTextComponent) {
					JTextComponent tf = (JTextComponent)e.getSource();
					tf.setSelectionStart(0);
					tf.setSelectionEnd(tf.getText().length());
				}
			}

		};
	}

	public static Integer getResourceId(String sResource) {
		Integer iResource = null;

		Collection<MasterDataVO> colVO = MasterDataDelegate.getInstance().getMasterData(NuclosEntity.RESOURCE.getEntityName());

		for(MasterDataVO vo : colVO) {
			if(vo.getField("name").equals(sResource)) {
				iResource = vo.getIntId();
				break;
			}
		}

		return iResource;
	}

	public static void flushCaches() {
		MetaDataDelegate.getInstance().invalidateServerMetadata();
		MetaDataCache.getInstance().invalidate();
		MasterDataDelegate.getInstance().invalidateCaches();
		MetaDataClientProvider.getInstance().revalidate();
		LocaleDelegate.getInstance().flush();
	}

	public static String replace(String str) {
		if(str == null)
			return str;
		str = StringUtils.replace(str, "\u00e4", "ae");
		str = StringUtils.replace(str, "\u00f6", "oe");
		str = StringUtils.replace(str, "\u00fc", "ue");
		str = StringUtils.replace(str, "\u00c4", "Ae");
		str = StringUtils.replace(str, "\u00d6", "Oe");
		str = StringUtils.replace(str, "\u00dc", "Ue");
		str = StringUtils.replace(str, "\u00df", "ss");
		str = str.replaceAll("[^\\w]", "");
		return str;
	}

	public static boolean isSystemField(String sField) {
		boolean blnSystemField = false;

		for(NuclosEOField field : NuclosEOField.values()) {
			if(field.getMetaData().getDbColumn().toUpperCase().equals(sField.toUpperCase()))
				return true;
		}
		if(sField.toUpperCase().equals("INTID") || sField.toUpperCase().equals("INTVERSION"))
			return true;

		return blnSystemField;
	}
	public static boolean isSystemField(EntityFieldMetaDataVO vo) {
		boolean blnSystemField = false;

		for(NuclosEOField field : NuclosEOField.values()) {
			if(field.getMetaData().getField().equals(vo.getField()))
				return true;
		}

		return blnSystemField;
	}

	public static String getStapledString(String str){
		str = org.nuclos.common2.StringUtils.emptyIfNull(str);
		return "${" + str + "}";
	}

	public static Collection<String> getExistingMenuPaths() {
		Collection<String> colMenuPaths = new ArrayList<String>();

		Collection<EntityMetaDataVO> colMetaData = MetaDataClientProvider.getInstance().getAllEntities();
		colMenuPaths = CollectionUtils.transform(colMetaData, new Transformer<EntityMetaDataVO, String>() {

			@Override
			public String transform(EntityMetaDataVO i) {
				String menu = CommonLocaleDelegate.getResource(i.getLocaleResourceIdForMenuPath(), "");
				return org.nuclos.common2.StringUtils.emptyIfNull(menu);
			}


		}, new Predicate<String>() {

			@Override
			public boolean evaluate(String t) {
				return !(t == null || t.length() == 0);
			}


		});

		Set<String> setMenupaths = new HashSet<String>(colMenuPaths);

		return CollectionUtils.sorted(setMenupaths);
	}

	public static MasterDataVO setFieldsForUserRight(MasterDataVO vo, String role, MasterDataVO voAdd, NuclosEntityWizardStaticModel model) {
	    if(voAdd == null && model.isStateModel()) {
	    	MasterDataMetaVO metaVO = MasterDataDelegate.getInstance().getMetaData(NuclosEntity.ROLEMODULE.getEntityName());
	    	CollectableMasterDataEntity masterDataEntity = new CollectableMasterDataEntity(metaVO);
	    	CollectableMasterData masterData = new CollectableMasterData(masterDataEntity, new MasterDataVO(masterDataEntity.getMasterDataMetaCVO(), false));
	    	voAdd = masterData.getMasterDataCVO();
	    	voAdd.setField("module", null);
	    	voAdd.setField("role", role);
	    	voAdd.setField("roleId", vo.getIntId());
	    	voAdd.setField("modulepermission", null);
	    }
	    else if(voAdd == null && !model.isStateModel()) {
	    	MasterDataMetaVO metaVO = MasterDataDelegate.getInstance().getMetaData(NuclosEntity.ROLEMASTERDATA.getEntityName());
	    	CollectableMasterDataEntity masterDataEntity = new CollectableMasterDataEntity(metaVO);
	    	CollectableMasterData masterData = new CollectableMasterData(masterDataEntity, new MasterDataVO(masterDataEntity.getMasterDataMetaCVO(), false));
	    	voAdd = masterData.getMasterDataCVO();
	    	voAdd.setField("entity", null);
	    	voAdd.setField("role", role);
	    	voAdd.setField("roleId", vo.getIntId());
	    	voAdd.setField("masterdatapermission", null);
	    }
	    return voAdd;
    }
}
