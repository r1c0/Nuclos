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
package org.nuclos.server.transfer.ejb3;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.apache.xml.security.utils.Base64;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.nuclos.common.ApplicationProperties;
import org.nuclos.common.NuclosEOField;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.attribute.DynamicAttributeVO;
import org.nuclos.common.collection.Pair;
import org.nuclos.common2.EntityAndFieldName;
import org.nuclos.common2.ServiceLocator;
import org.nuclos.common2.StringUtils;
import org.nuclos.common2.exception.CommonCreateException;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.common2.exception.CommonPermissionException;
import org.nuclos.server.attribute.ejb3.LayoutFacadeLocal;
import org.nuclos.server.attribute.valueobject.AttributeCVO;
import org.nuclos.server.common.AttributeCache;
import org.nuclos.server.common.MasterDataMetaCache;
import org.nuclos.server.common.NuclosSystemParameters;
import org.nuclos.server.common.ServerParameterProvider;
import org.nuclos.server.common.ejb3.LocaleFacadeLocal;
import org.nuclos.server.common.ejb3.NuclosFacadeBean;
import org.nuclos.server.genericobject.GenericObjectMetaDataCache;
import org.nuclos.server.genericobject.Modules;
import org.nuclos.server.genericobject.valueobject.GenericObjectVO;
import org.nuclos.server.masterdata.valueobject.MasterDataMetaFieldVO;
import org.nuclos.server.masterdata.valueobject.MasterDataMetaVO;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;
import org.nuclos.server.report.ByteArrayCarrier;
import org.nuclos.server.resource.valueobject.ResourceFile;
import org.nuclos.server.ruleengine.NuclosBusinessRuleException;
import org.nuclos.server.transfer.XmlExportImportHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * Facade bean for all XML Transfer functions. <br>
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 */
// @Stateless
// @Local(XmlExportFacadeLocal.class)
// @Remote(XmlExportFacadeRemote.class)
@Transactional
public class XmlExportFacadeBean extends NuclosFacadeBean implements XmlExportFacadeLocal, XmlExportFacadeRemote {

	private XmlExportImportProtocolFacadeLocal protocolFacade;
	
	private LocaleFacadeLocal locale = ServiceLocator.getInstance().getFacade(LocaleFacadeLocal.class);

	private Integer iProtocolId;

	private Modules modules;
	private GenericObjectMetaDataCache genericObjectMetaDataCache;
	private ServerParameterProvider serverParameterProvider;
	private List<Pair<String, Integer>> exportedIds;
	private Map<Integer, Map<EntityAndFieldName, String>> mpGoSubFormsWithForeignKeys;
	private Map<String, Map<EntityAndFieldName, String>> mpMdSubFormsWithForeignKeys;
	private Date today;
	private SimpleDateFormat dateFormat;

	private Integer iProcessedEntities = 0;

	private Integer iActionNumber = 1;

	@Autowired
	void setServerParameterProvider(ServerParameterProvider serverParameterProvider) {
		this.serverParameterProvider = serverParameterProvider;
	}
	
	@Autowired
	void setGenericObjectMetaDataCache(GenericObjectMetaDataCache genericObjectMetaDataCache) {
		this.genericObjectMetaDataCache = genericObjectMetaDataCache;
	}
	
	@Autowired
	void setModules(Modules modules) {
		this.modules = modules;
	}

	private XmlExportImportProtocolFacadeLocal getProtocolFacade() {
		if (protocolFacade == null)
			protocolFacade = ServiceLocator.getInstance().getFacade(XmlExportImportProtocolFacadeLocal.class);
		return protocolFacade;
	}
	
	@Override
	public org.nuclos.common2.File xmlExport(Map<Integer, String> exportEntities, boolean deepexport, String sFileName) 
			throws CommonFinderException, CommonPermissionException, IOException, CommonCreateException, NuclosBusinessRuleException {
		return xmlExport(exportEntities, deepexport, true, sFileName);
	}

	/**
	 *
	 * @param exportEntities
	 *            map of ids and entitynames to export
	 * @return Zip File with Xml output and Document files
	 * @throws IOException
	 * @throws CommonPermissionException
	 * @throws CommonFinderException
	 * @throws CreateException
	 * @throws CommonCreateException
	 * @throws NuclosBusinessRuleException
	 * @throws Exception
	 * @jboss.method-attributes read-only = "true"
	 */
	@Override
	public org.nuclos.common2.File xmlExport(Map<Integer, String> exportEntities, boolean deepexport, boolean withDependants, String sFileName) 
			throws CommonFinderException, CommonPermissionException, IOException, CommonCreateException, NuclosBusinessRuleException {
		today = new Date();
		dateFormat = new SimpleDateFormat("dd.MM.yyy HH:mm:ss");
		iActionNumber = 1;

		// List of exported Ids
		exportedIds = new ArrayList<Pair<String, Integer>>();

		// map of subforms with their foreign keys of the entities
		mpGoSubFormsWithForeignKeys = new HashMap<Integer, Map<EntityAndFieldName, String>>();
		mpMdSubFormsWithForeignKeys = new HashMap<String, Map<EntityAndFieldName, String>>();

		// create and clear export dirs
		File expimpDir = NuclosSystemParameters.getDirectory(NuclosSystemParameters.EXPORT_IMPORT_PATH);
		if (!expimpDir.exists()) {
			expimpDir.mkdir();
		}
		File expimpTimestampDir = new File(expimpDir, "" + today.getTime());
		expimpTimestampDir.mkdir();
		try {
			File expimpTsResourceDir = new File(expimpTimestampDir, "ressource");
			expimpTsResourceDir.mkdir();

			Set<Integer> stExportId = exportEntities.keySet();

			// create XML Document and Header
			Document document = DocumentHelper.createDocument();
			Element header = document.addElement("nucleusXmlExport");
			header.addAttribute("version", ApplicationProperties.getInstance().getNuclosVersion().getVersionNumber());
			header.addAttribute("date", dateFormat.format(today));
			String sRootEntity = "";
			if (!stExportId.isEmpty()) {
				sRootEntity = exportEntities.get(stExportId.toArray()[0]);
			}
			header.addAttribute("rootentity", sRootEntity);
			header.addAttribute("deepexport", Boolean.toString(deepexport));

			info("Begin Export: user ["+getCurrentUserName()+"] date ["+today+"] entity ["+sRootEntity+"]");
			iProtocolId = getProtocolFacade().writeExportImportLogHeader(XmlExportImportHelper.EXPIMP_TYPE_EXPORT, getCurrentUserName(), today, sRootEntity, sFileName);

//			// special handling for elisa, that means the following:
//			// deep export = main data + subform data will be exported
//			// no deep export = only main data will be exported
//			if (deepexport) {
//				bElisaDeepExport = true;
//				deepexport = false;
//			}
//			else {
//				bElisaDeepExport = false;
//			}

			for (Integer iExportId : stExportId) {
				// call recursive XML Helper Method
				xmlExportHelper(exportEntities.get(iExportId), iExportId, header, deepexport, withDependants, true);
			}

			try {
				// write XML File
				File fExport = new File(expimpTimestampDir.getPath() + "/export.xml");
				FileOutputStream os = new FileOutputStream(fExport);
				OutputStreamWriter osr = new OutputStreamWriter(os, "UTF-8");
				OutputFormat format = OutputFormat.createPrettyPrint();
				format.setTrimText(false);
				format.setEncoding("UTF-8");
				XMLWriter writer = new XMLWriter(osr, format);
				writer.write(document);
				writer.close();

				// activate the following code, if you want to save the exported file in the database
				// NOTE: it is recommended to compress the stored data
				//xmlExportImportProtocol.addXmlFile(iProtocolId, IOUtils.readFromTextFile(fExport, "UTF-8"));
			}
			catch (IOException e) {
				throw new IOException("xmlexport.error.creating.file", e);
					//"Fehler beim erstellen der XML-Export-Datei aufgetreten: "+e);
			}

			try {
				// Zip Export Files
				 XmlExportImportHelper.zipFolder(expimpTimestampDir, "nucleus_export.zip");
			}
			catch (Exception e) {
				throw new NuclosFatalException("xmlexport.error.creating.zipfile", e);
					//"Fehler beim erstellen der ZIP-Export-Datei aufgetreten: "+e);
			}

			// create serializeable File from Zip
			org.nuclos.common2.File transfile = XmlExportImportHelper.createFile(expimpTimestampDir.getAbsolutePath(), "nucleus_export.zip");

			getProtocolFacade().addFile(iProtocolId, transfile.getContents());

			info("END Export");

			return transfile;
		}
		finally {
			// delete export dir
			XmlExportImportHelper.deleteDir(expimpTimestampDir);
		}
	}

	/**
	 * determines whether an entity with the given id was already exported.
	 * if entity with given id was imported, then return true, otherwise false
	 * @param pairToDetermine
	 * @return Boolean
	 */
	private Boolean wasExported(Pair<String, Integer> pairToDetermine) {
		for (Pair<String, Integer> pairExists : exportedIds) {
			if (pairExists.getX().equals(pairToDetermine.getX()) &&
					pairExists.getY().compareTo(pairToDetermine.getY()) == 0) {
				return true;
			}
		}
		return false;
	}

	/**
	 * XMlXportHelper decides wheter the exorting Entity is a MD oder GO and
	 * calls the right recursive Method
	 *
	 * @param sEntityName
	 * @param entityId
	 * @param parent
	 * @throws CommonPermissionException
	 * @throws CommonFinderException
	 * @throws CommonFinderException
	 * @throws CommonPermissionException
	 * @throws IOException
	 * @throws IOException
	 * @throws CreateException
	 * @throws CommonCreateException
	 * @throws NuclosBusinessRuleException
	 */
	private void xmlExportHelper(String sEntityName, Object entityId, Element parent, Boolean deepexport, Boolean bExportDependants, Boolean bImportData) 
			throws CommonFinderException, CommonPermissionException, IOException, CommonCreateException, NuclosBusinessRuleException {
		if (!wasExported(new Pair<String, Integer>(sEntityName,(Integer)entityId))) {
			// genericObject Entity
			if (modules.isModuleEntity(sEntityName) && modules.isImportExportable(sEntityName)) {
				exportGOEntity(sEntityName, entityId, parent, deepexport, bExportDependants, bImportData);
				// Master Data Entity
			} else {
				MasterDataMetaVO mdmvo = MasterDataMetaCache.getInstance().getMetaData(sEntityName);
				if (mdmvo != null && mdmvo.getIsImportExport()){
					exportMDEntity(sEntityName, entityId, parent, deepexport, bExportDependants, bImportData);
				}
			}
		}
	}

	/**
	 * Exports a GO with all Depandents
	 *
	 * @param sEntityName
	 * @param entityId
	 * @param parent
	 * @throws CommonFinderException
	 * @throws CommonFinderException
	 * @throws CommonFinderException
	 * @throws CommonPermissionException
	 * @throws CommonPermissionException
	 * @throws IOException
	 * @throws IOException
	 * @throws CreateException
	 * @throws CommonCreateException
	 * @throws NuclosBusinessRuleException
	 */
	private void exportGOEntity(String sEntityName, Object entityId, Element parent, Boolean deepexport, Boolean bExportDependants, Boolean bImportData) 
			throws CommonFinderException, CommonPermissionException, IOException, CommonCreateException, NuclosBusinessRuleException {
		Element entity = DocumentHelper.createElement("entity");

		// get id for the exported Module on the source system
		Integer moduleId = modules.getModuleIdByEntityName(sEntityName);

		info("Export GenericObject Entity: "+sEntityName+" ModuleId: "+moduleId+" Entity-Id: "+entityId);

		GenericObjectVO govo;
		try {
			govo = getGenericObjectFacade().get((Integer) entityId);
		}
		catch (Exception e) {
			String sMessage = MessageFormat.format(locale.getResourceById(locale.getUserLocale(), "xmlexport.error.creating.entity"), sEntityName, entityId, e);
				//"Modul-Entit\u00e4t ["+sEntityName+"] mit der ID ["+entityId+"] konnte nicht exportiert werden. "+e;
			getProtocolFacade().writeExportImportLogEntry(iProtocolId, XmlExportImportHelper.EXPIMP_MESSAGE_LEVEL_ERROR,
					XmlExportImportHelper.EXPIMP_ACTION_READ, sMessage, iActionNumber++);
			throw new NuclosFatalException(StringUtils.getParameterizedExceptionMessage("xmlexport.error.creating.entity", sEntityName, entityId, e));
		}

		Collection<DynamicAttributeVO> attcol = govo.getAttributes();
		Iterator<DynamicAttributeVO> attrIt = attcol.iterator();

		entity.addAttribute("id", entityId.toString());
		entity.addAttribute("type", "genericObject");
		entity.addAttribute("name", sEntityName);

		DynamicAttributeVO dynamicAttributeVO = govo.getAttribute(NuclosEOField.PROCESS.getMetaData().getField(), AttributeCache.getInstance());
		String sProcess = (dynamicAttributeVO == null) ? "" : dynamicAttributeVO.getCanonicalValue(AttributeCache.getInstance());

		entity.addAttribute("process", (sProcess == null) ? "" : sProcess);

		entity.addAttribute("import", bImportData.toString());

		if (govo.getParentId() != null) {
			entity.addAttribute("parentId", govo.getParentId().toString());
		}
		else {
			entity.addAttribute("parentId", "null");
		}
		entity.addAttribute("deleted", String.valueOf(govo.isRemoved()));

		exportedIds.add(new Pair<String, Integer>(sEntityName, (Integer)entityId));

		while (attrIt.hasNext()) {
			DynamicAttributeVO davo = attrIt.next();
			AttributeCVO attrCVO = genericObjectMetaDataCache.getAttribute(davo.getAttributeId());
			Element attr2 = entity.addElement("attribute");
			attr2.addAttribute("id", (davo.getId() == null) ? "0" : davo.getId().toString());
			attr2.addAttribute("name", attrCVO.getName());
			if (attrCVO.getExternalEntity() != null) {
				attr2.addAttribute("hasReference", "true");
				Element attrval = attr2.addElement("value");
				if (attrCVO.getJavaClass().getName().equals("java.util.Date")) {
					attrval.setText(dateFormat.format((Date) davo.getValue()));
				}
				else {
					attrval.setText(davo.getValue().toString());
				}

				Element attrref = attr2.addElement("referenceId");
				attrref.setText(davo.getValueId().toString());
				if (deepexport) {
					xmlExportHelper(attrCVO.getExternalEntity(), davo.getValueId(), parent, deepexport, bExportDependants, bImportData);
				}
				else {
					xmlExportHelper(attrCVO.getExternalEntity(), davo.getValueId(), parent, deepexport, false, false);
				}
			} else {
				attr2.addAttribute("hasReference", "false");
				Element attrval = attr2.addElement("value");
				String sValue = davo.getCanonicalValue(AttributeCache.getInstance());
				attrval.setText((sValue == null) ? "" : sValue);

				// check whether attribute has a value list
				if (!attrCVO.getValues().isEmpty()) {
					Element attrvalId = attr2.addElement("valueId");
					Integer iValueId = davo.getValueId();
					attrvalId.setText((iValueId == null) ? "" : iValueId.toString());
				}
			}
		}
		parent.add(entity);

		String sMessage = MessageFormat.format(locale.getResourceById(locale.getUserLocale(), "xmlexport.message.successful"), sEntityName, entityId);  
			//"Module-entity ["+sEntityName+"] with the ID ["+entityId+"] successful exported.";
		info(sMessage);
		getProtocolFacade().writeExportImportLogEntry(iProtocolId, XmlExportImportHelper.EXPIMP_MESSAGE_LEVEL_INFO,
				XmlExportImportHelper.EXPIMP_ACTION_READ, sMessage, iActionNumber++);

		// subform
		Map<EntityAndFieldName, String> subformtree_with_fkeys = new HashMap<EntityAndFieldName, String>();
		if (bImportData) {
			if (!mpGoSubFormsWithForeignKeys.containsKey(govo.getModuleId())) {
				for (Integer iLayoutId : genericObjectMetaDataCache.getLayoutIdsByModuleId(govo.getModuleId(), false)) {
					LayoutFacadeLocal layoutFacade = ServiceLocator.getInstance().getFacade(LayoutFacadeLocal.class);
					Map<EntityAndFieldName, String> subformtree = layoutFacade.getSubFormEntityAndParentSubFormEntityNamesByLayoutId(iLayoutId);
					for (EntityAndFieldName eafn : subformtree.keySet()) {
						String sParentEntity = sEntityName;
						if (subformtree.get(eafn) != null) {
							sParentEntity = subformtree.get(eafn);
						}

						String s1 = eafn.getEntityName();
						String s2 = XmlExportImportHelper.getForeignKeyFieldName(sParentEntity, eafn.getFieldName(), eafn.getEntityName());
						subformtree_with_fkeys.put(new EntityAndFieldName(s1, s2), subformtree.get(eafn));
					}
				}
				mpGoSubFormsWithForeignKeys.put(govo.getModuleId(), subformtree_with_fkeys);
			}

			//special handling for relation entity
			subformtree_with_fkeys.put(new EntityAndFieldName(NuclosEntity.GENERICOBJECTRELATION, "source"), null);
			subformtree_with_fkeys.put(new EntityAndFieldName(NuclosEntity.GENERICOBJECTRELATION, "destination"), null);

			mpMdSubFormsWithForeignKeys.put(sEntityName, subformtree_with_fkeys);

			exportMDSubform(mpGoSubFormsWithForeignKeys.get(govo.getModuleId()), null, entityId, parent, deepexport);
		}
	}

	/**
	 * Exports a MD Entity with all Dependants
	 *
	 * @param sEntityName
	 * @param entityId
	 * @param parent
	 * @throws CommonFinderException
	 * @throws CommonFinderException
	 * @throws CommonPermissionException
	 * @throws CommonPermissionException
	 * @throws IOException
	 * @throws IOException
	 * @throws CreateException
	 * @throws CommonCreateException
	 * @throws NuclosBusinessRuleException
	 */
	private void exportMDEntity(String sEntityName, Object entityId, Element parent, Boolean deepexport, Boolean bExportDependants, Boolean bImportData) 
			throws CommonFinderException, CommonPermissionException, IOException, CommonCreateException, NuclosBusinessRuleException {
		info("Export MasterData Entity: "+sEntityName+" Entity-Id: "+entityId);

		Element entity = DocumentHelper.createElement("entity");

		MasterDataVO md;
		try {
			md = getMasterDataFacade().get(sEntityName, entityId);
		}
		catch (Exception e) {
			String sMessage = MessageFormat.format(locale.getResourceById(locale.getUserLocale(), "xmlexport.error.creating.mdentity"), sEntityName, entityId, e);  
				//"Master data entity ["+sEntityName+"] with the ID ["+entityId+"] could not be exported. "+e;
			getProtocolFacade().writeExportImportLogEntry(iProtocolId, XmlExportImportHelper.EXPIMP_MESSAGE_LEVEL_ERROR,
					XmlExportImportHelper.EXPIMP_ACTION_READ, sMessage, iActionNumber++);
			throw new NuclosFatalException(StringUtils.getParameterizedExceptionMessage("xmlexport.error.creating.mdentity", sEntityName, entityId, e));
		}

		MasterDataMetaVO mdm = getMasterDataFacade().getMetaData(sEntityName);
		List<MasterDataMetaFieldVO> metafields = mdm.getFields();

		Iterator<MasterDataMetaFieldVO> fieldIt = metafields.iterator();
		MasterDataMetaFieldVO field;

		entity.addAttribute("id", entityId.toString());
		entity.addAttribute("type", "masterdata");
		entity.addAttribute("name", sEntityName);
		entity.addAttribute("import", bImportData.toString());
		Object objFieldValue;

		//exportedIds.add(new Pair<String, Integer>(sEntityName, (Integer)entityId));

		while (fieldIt.hasNext()) {
			field = fieldIt.next();

			// case 1:1 referenceces
			if (field.getForeignEntity() != null) {
				objFieldValue = md.getField(field.getFieldName() + "Id");
				if (objFieldValue != null) {
					Element attr = entity.addElement(field.getFieldName() + "Id");
					attr.setText(objFieldValue.toString());

					if (deepexport) {
						xmlExportHelper(field.getForeignEntity(), objFieldValue, parent, deepexport, bExportDependants, bImportData);
					}
					else {
						// special handling for statemodel - if the statemodel is exported, then set the import flag to true
						// for all state entities
						if (NuclosEntity.ROLESUBFORM.checkEntityName(sEntityName) && NuclosEntity.STATE.checkEntityName(field.getForeignEntity()) ||
								NuclosEntity.ROLEATTRIBUTEGROUP.checkEntityName(sEntityName) && NuclosEntity.STATE.checkEntityName(field.getForeignEntity()) ||
								NuclosEntity.STATETRANSITION.checkEntityName(sEntityName) && NuclosEntity.STATE.checkEntityName(field.getForeignEntity())) {
							xmlExportHelper(field.getForeignEntity(), objFieldValue, parent, deepexport, false, true);
						}
						// special handling for entity genericobjectrelation, because the foreignkey entity is set to generalsearch
						// for the fields source and destination and therefore it's not possible to determine the corresponding foreign entity
						else if (NuclosEntity.GENERICOBJECTRELATION.checkEntityName(sEntityName) &&
								(field.getFieldName().equals("source") || field.getFieldName().equals("destination"))) {
							Integer iModuleId = getGenericObjectFacade().get((Integer)objFieldValue).getModuleId();
							String sForeignEntityName = Modules.getInstance().getEntityNameByModuleId(iModuleId);
							xmlExportHelper(sForeignEntityName, objFieldValue, parent, deepexport, false, false);
						}
					}
				}
			}
			// case no references
			else {
				File expimpDir = NuclosSystemParameters.getDirectory(NuclosSystemParameters.EXPORT_IMPORT_PATH);
				File expimpResourceDir = new File(expimpDir, "" + today.getTime() + "/ressource");
				
				objFieldValue = md.getField(field.getFieldName());
				Element attr2 = entity.addElement(field.getFieldName());
				if (field.getJavaClass().getName().equals("java.util.Date")
						&& objFieldValue != null) {
					attr2.setText(dateFormat.format((Date) objFieldValue));
				}
				else if (field.getJavaClass().getName().equals("java.lang.Object")
						&& objFieldValue != null) {
					String sFileName = sEntityName+"_"+field.getFieldName()+"_"+entityId;

					XmlExportImportHelper.writeObjectToFile(objFieldValue,
						expimpResourceDir, sFileName);
					attr2.setText(sFileName);
				}
				else if (objFieldValue != null) {
					if (objFieldValue.getClass().getName().equals("org.nuclos.server.report.ByteArrayCarrier")) {
						ByteArrayCarrier bac = (ByteArrayCarrier) objFieldValue;
						attr2.setText(Base64.encode(bac.getData()));
					} else if (objFieldValue.getClass().getName().equals("org.nuclos.server.resource.valueobject.ResourceFile")) {
						ResourceFile rf = (ResourceFile) objFieldValue;
						attr2.addAttribute("filename", StringUtils.emptyIfNull(rf.getFilename()));
						attr2.addAttribute("documentFileId", rf.getDocumentFileId()==null?"":rf.getDocumentFileId().toString());
						attr2.setText(Base64.encode(rf.getContents()));
					} else if (objFieldValue instanceof byte[]) {
						attr2.setText(Base64.encode((byte[]) objFieldValue));
					} else
						attr2.setText(objFieldValue.toString());
				}
				else {
					attr2.setText("");
				}

				String sFileName = "";
				// copy Document Files if available
				if (objFieldValue != null && !objFieldValue.equals("") &&
						(field.getJavaClass().getName().equals("org.nuclos.server.genericobject.valueobject.GenericObjectDocumentFile"))) {
					try {
						File documentDir = NuclosSystemParameters.getDirectory(NuclosSystemParameters.DOCUMENT_PATH);

						if (field.getJavaClass().getName().equals("org.nuclos.server.genericobject.valueobject.GenericObjectDocumentFile")) {
							sFileName = md.getId()+"."+md.getField("file").toString();
						}

						XmlExportImportHelper.copyDocumentFile(documentDir, expimpResourceDir,
								sFileName, md.getId(), md.getId());
					}
					catch (IOException e) {
						String sMessage = MessageFormat.format(locale.getResourceById(locale.getUserLocale(), "xmlexport.error.copying.file"), md.getField("filename").toString(), sEntityName, entityId, e); 
							//"Error copying the file ["+md.getField("filename").toString()+"] of the master data entity ["+sEntityName+"] with the Id ["+entityId+"]";
						getProtocolFacade().writeExportImportLogEntry(iProtocolId, XmlExportImportHelper.EXPIMP_MESSAGE_LEVEL_ERROR,
								XmlExportImportHelper.EXPIMP_ACTION_READ, sMessage, iActionNumber++);
						throw new IOException(StringUtils.getParameterizedExceptionMessage("xmlexport.error.copying.file",  md.getField("filename").toString(), sEntityName, entityId, e));
							//"Fehler beim Kopieren der Datei ["+sFileName+"] der Stammdaten-Entit\u00e4t ["+sEntityName+"] mt der Id ["+entityId+"]: "+e);
					}
				}
			}
		}
		// additional meta Information
		if ((md.getCreatedBy() != null)) {
			Element attr3 = entity.addElement("attribute");
			attr3.addAttribute("name", NuclosEOField.CREATEDBY.getMetaData().getField());
			attr3.setText(md.getCreatedBy());
		}

		if ((md.getChangedBy() != null)) {
			Element attr4 = entity.addElement("attribute");
			attr4.addAttribute("name", NuclosEOField.CHANGEDBY.getMetaData().getField());
			attr4.setText(md.getChangedBy());
		}

		if ((md.getChangedAt() != null)) {
			Element attr5 = entity.addElement("attribute");
			attr5.addAttribute("name", NuclosEOField.CHANGEDAT.getMetaData().getField());
			attr5.setText(dateFormat.format(md.getChangedAt()));
		}

		if ((md.getCreatedAt() != null)) {
			Element attr6 = entity.addElement("attribute");
			attr6.addAttribute("name", NuclosEOField.CREATEDAT.getMetaData().getField());
			attr6.setText(dateFormat.format(md.getCreatedAt()));
		}

		if (md.getVersion() != 0) {
			Element attr7 = entity.addElement("attribute");
			attr7.addAttribute("name", "[version]");
			attr7.setText(String.valueOf(md.getVersion()));
		}


		if (wasExported(new Pair<String, Integer>(sEntityName, (Integer)entityId))) {
			return;
		}

		parent.add(entity);

		exportedIds.add(new Pair<String, Integer>(sEntityName, (Integer)entityId));

		String sMessage = MessageFormat.format(locale.getResourceById(locale.getUserLocale(), "xmlexport.message.successful.2"), sEntityName, entityId); 
			//"Master data entity ["+sEntityName+"] with the ID ["+entityId+"] successful exported.";
		info(sMessage);
		getProtocolFacade().writeExportImportLogEntry(iProtocolId, XmlExportImportHelper.EXPIMP_MESSAGE_LEVEL_INFO,
				XmlExportImportHelper.EXPIMP_ACTION_READ, sMessage, iActionNumber++);

		// subform 1:n references
		if (bExportDependants) {
			if (!mpMdSubFormsWithForeignKeys.containsKey(sEntityName)) {
				LayoutFacadeLocal layoutFacade = ServiceLocator.getInstance().getFacade(LayoutFacadeLocal.class);
				mpMdSubFormsWithForeignKeys.put(sEntityName, layoutFacade.getSubFormEntityAndParentSubFormEntityNames(sEntityName,(Integer)entityId,true));
			}

			exportMDSubform(mpMdSubFormsWithForeignKeys.get(sEntityName), null, entityId, parent, deepexport);
		}
	}

	private void exportMDSubform(final Map<EntityAndFieldName, String> subformtree, String entityname, Object entityId, Element parent, boolean deepexport) 
			throws CommonFinderException, CommonPermissionException, IOException, CommonCreateException, NuclosBusinessRuleException {
		if (subformtree == null) {
			return;
		}

		List<EntityAndFieldName> entitylist = new ArrayList<EntityAndFieldName>();
		for (Entry<EntityAndFieldName, String> entry : subformtree.entrySet()) {
			if (entityname == null) {
				if (entry.getValue() == null) {
					entitylist.add(entry.getKey());
				}
			} else if (entityname.equals(entry.getValue())) {
				entitylist.add(entry.getKey());
			}
		}

		for (EntityAndFieldName eafn : entitylist) {
			Collection<MasterDataVO> mdList = getMasterDataFacade().getDependantMasterData(eafn.getEntityName(), eafn.getFieldName(), entityId);
			Iterator<MasterDataVO> mdfkIt = mdList.iterator();
			while (mdfkIt.hasNext()) {
				MasterDataVO m = mdfkIt.next();
				if (!wasExported(new Pair<String, Integer>(eafn.getEntityName(), (Integer)m.getId()))) {
					//check this again
					if (deepexport) {
						xmlExportHelper(eafn.getEntityName(), m.getId(), parent, deepexport, true, true);
					}
					else {
						xmlExportHelper(eafn.getEntityName(), m.getId(), parent, deepexport, false, true);
					}
					exportMDSubform(subformtree, eafn.getEntityName(), m.getId(), parent, deepexport);
				}
			}
		}
	}

	/**
	 * get the count of the processed exported entities
	 */
	@Override
	public Integer getProcessedEntities() {
		return this.iProcessedEntities;
	}
}
