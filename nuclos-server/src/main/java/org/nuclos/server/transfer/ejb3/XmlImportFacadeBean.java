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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.annotation.PostConstruct;
import javax.ejb.CreateException;
import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;

import org.apache.xml.security.exceptions.Base64DecodingException;
import org.apache.xml.security.utils.Base64;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.nuclos.common.ApplicationProperties;
import org.nuclos.common.NuclosBusinessException;
import org.nuclos.common.NuclosEOField;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.UsageCriteria;
import org.nuclos.common.attribute.DynamicAttributeVO;
import org.nuclos.common.collect.collectable.CollectableEntity;
import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common.collect.collectable.CollectableField;
import org.nuclos.common.collect.collectable.CollectableValueField;
import org.nuclos.common.collect.collectable.CollectableValueIdField;
import org.nuclos.common.collect.collectable.searchcondition.CollectableComparison;
import org.nuclos.common.collect.collectable.searchcondition.CollectableIsNullCondition;
import org.nuclos.common.collect.collectable.searchcondition.CollectableSearchCondition;
import org.nuclos.common.collect.collectable.searchcondition.ComparisonOperator;
import org.nuclos.common.collect.collectable.searchcondition.CompositeCollectableSearchCondition;
import org.nuclos.common.collect.collectable.searchcondition.LogicalOperator;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.MasterDataToEntityObjectTransformer;
import org.nuclos.common.collection.Pair;
import org.nuclos.common.masterdata.CollectableMasterDataEntity;
import org.nuclos.common2.DateUtils;
import org.nuclos.common2.EntityAndFieldName;
import org.nuclos.common2.IOUtils;
import org.nuclos.common2.ServiceLocator;
import org.nuclos.common2.StringUtils;
import org.nuclos.common2.TruncatableCollection;
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
import org.nuclos.server.common.valueobject.NuclosValueObject;
import org.nuclos.server.dal.processor.ProcessorFactorySingleton;
import org.nuclos.server.database.DataBaseHelper;
import org.nuclos.server.dblayer.DbException;
import org.nuclos.server.dblayer.DbTuple;
import org.nuclos.server.dblayer.query.DbFrom;
import org.nuclos.server.dblayer.query.DbQuery;
import org.nuclos.server.dblayer.query.DbQueryBuilder;
import org.nuclos.server.genericobject.GenericObjectMetaDataCache;
import org.nuclos.server.genericobject.Modules;
import org.nuclos.server.genericobject.valueobject.CanonicalAttributeFormat;
import org.nuclos.server.genericobject.valueobject.GenericObjectDocumentFile;
import org.nuclos.server.genericobject.valueobject.GenericObjectVO;
import org.nuclos.server.genericobject.valueobject.GenericObjectWithDependantsVO;
import org.nuclos.server.masterdata.valueobject.DependantMasterDataMap;
import org.nuclos.server.masterdata.valueobject.MasterDataMetaFieldVO;
import org.nuclos.server.masterdata.valueobject.MasterDataMetaVO;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;
import org.nuclos.server.report.ByteArrayCarrier;
import org.nuclos.server.resource.valueobject.ResourceFile;
import org.nuclos.server.ruleengine.NuclosBusinessRuleException;
import org.nuclos.server.statemodel.ejb3.StateFacadeLocal;
import org.nuclos.server.statemodel.valueobject.StateModelUsagesCache;
import org.nuclos.server.statemodel.valueobject.StateVO;
import org.nuclos.server.transfer.XmlExportImportHelper;
import org.springframework.transaction.annotation.Transactional;

/**
 * Facade bean for XML Import functions. <br>
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 */
@Stateless
@Local(XmlImportFacadeLocal.class)
@Remote(XmlImportFacadeRemote.class)
@Transactional
public class XmlImportFacadeBean extends NuclosFacadeBean implements XmlImportFacadeLocal, XmlImportFacadeRemote {

	private XmlExportImportProtocolFacadeLocal protocolFacade;

	private Integer iProtocolId;

	private Modules modules = Modules.getInstance();
	private GenericObjectMetaDataCache genericObjectMetaDataCache = GenericObjectMetaDataCache.getInstance();
	private AttributeCache attributeCache = AttributeCache.getInstance();
	private ServerParameterProvider serverParameterProvider = ServerParameterProvider.getInstance();
	
	private LocaleFacadeLocal locale = ServiceLocator.getInstance().getFacade(LocaleFacadeLocal.class);

	private Date today;

	private SimpleDateFormat dateFormat;

	private Map<Pair<String, String>, String> idMap;

	private List<Pair<String, Integer>> lstAllReadEntityIds;
	private List<Pair<String, Integer>> lstAllImportedEntityIds;

	private Map<Integer, List<Map<EntityAndFieldName, String>>> mpGoSubFormsWithForeignKeys;
	private Map<String, Map<EntityAndFieldName, String>> mpMdSubFormsWithForeignKeys;

	private Integer iActionNumber = 1;

	@PostConstruct
	@Override
	public void postConstruct() {
      super.postConstruct();

      modules = Modules.getInstance();
   	genericObjectMetaDataCache = GenericObjectMetaDataCache.getInstance();
   	attributeCache = AttributeCache.getInstance();
   	serverParameterProvider = ServerParameterProvider.getInstance();
	}

	private XmlExportImportProtocolFacadeLocal getProtocolFacade() {
		if (protocolFacade == null)
			protocolFacade = ServiceLocator.getInstance().getFacade(XmlExportImportProtocolFacadeLocal.class);
		return protocolFacade;
	}

	/**
	 * Import Method
	 *
	 * @param importFile
	 *            zipfile with content to import
	 * @throws IOException
	 * @throws CommonPermissionException
	 * @throws CommonCreateException
	 * @throws CreateException
	 * @throws CommonFinderException
	 * @throws ElisaBusinessException
	 * @jboss.method-attributes read-only = "true"
	 */
	@Override
	@SuppressWarnings("unchecked")
	public void xmlImport(String sEntityName, org.nuclos.common2.File importFile) throws IOException, DocumentException, CommonCreateException, CommonPermissionException, CreateException, NuclosBusinessException, CommonFinderException {
		today = new Date();
		dateFormat = new SimpleDateFormat("dd.MM.yy HH:mm:ss");
		iActionNumber = 1;

		idMap = new HashMap<Pair<String, String>, String>();
		lstAllReadEntityIds = new ArrayList<Pair<String, Integer>>();
		lstAllImportedEntityIds = new ArrayList<Pair<String, Integer>>();

		// map of subforms with their foreign keys of the entities
		mpGoSubFormsWithForeignKeys = new HashMap<Integer, List<Map<EntityAndFieldName, String>>>();
		mpMdSubFormsWithForeignKeys = new HashMap<String, Map<EntityAndFieldName, String>>();

		info("Begin Import: user ["+getCurrentUserName()+"] date ["+today+"] entity ["+sEntityName+"] file ["+importFile.getFilename()+"]");
		iProtocolId = getProtocolFacade().writeExportImportLogHeader(XmlExportImportHelper.EXPIMP_TYPE_IMPORT, getCurrentUserName(), today, sEntityName, importFile.getFilename());
		getProtocolFacade().addFile(iProtocolId, importFile.getContents());

		// create and clear import dirs
		File expimpDir = NuclosSystemParameters.getDirectory(NuclosSystemParameters.EXPORT_IMPORT_PATH);
		if (!expimpDir.exists()) {
			expimpDir.mkdir();
		}
		File expimpTimestampDir = new File(expimpDir, "" + today.getTime());
		expimpTimestampDir.mkdir();

		try {
			// write zip File to import dir
			File zipFile = new File(expimpTimestampDir, "import.zip");
			FileOutputStream fos = new FileOutputStream(zipFile);
			fos.write(importFile.getContents());
			fos.flush();
			fos.close();

			// extract zip file
			XmlExportImportHelper.extractZipArchive(zipFile, expimpTimestampDir);

			// read xml file
			SAXReader reader = new SAXReader();
			//reader.setStripWhitespaceText(false);

			File fImport = new File(expimpTimestampDir, "export.xml");

			FileInputStream is = new FileInputStream(fImport);
			InputStreamReader isr = new InputStreamReader(is, "UTF-8");
			Document document = reader.read(isr);

			Element root = document.getRootElement();

			String sVersionExport = root.attribute("version").getValue();
			String sVersionImport = ApplicationProperties.getInstance().getNuclosVersion().getVersionNumber();

			if (!sVersionImport.equals(sVersionExport)) {
				String sMessage = MessageFormat.format(locale.getResourceById(locale.getUserLocale(), "xmlimport.error.incompatible.app.version"), sVersionExport, sVersionImport);
					//"Die zu importierenden Daten stammen aus einer Applikation mit der Versionsnummer ["+sVersionExport+"]\n" +
				//"Diese Versionsnummer stimmt nicht mit der Version ["+sVersionImport+"] der aktuellen Applikation \u00fcberein.";

				getProtocolFacade().writeExportImportLogEntry(iProtocolId, XmlExportImportHelper.EXPIMP_MESSAGE_LEVEL_ERROR,
						XmlExportImportHelper.EXPIMP_ACTION_INSERT, sMessage, iActionNumber++);

				throw new NuclosBusinessException(StringUtils.getParameterizedExceptionMessage("xmlimport.error.incompatible.app.version",
					sVersionExport, sVersionImport));
			}

			String sEntityNameExport = root.attribute("rootentity").getValue();

			if (!sEntityNameExport.equals(sEntityName)) {
				String sMessage = MessageFormat.format(locale.getResourceById(locale.getUserLocale(), "xmlimport.error.incompatible.entity"), sEntityNameExport, sEntityName);
					//"Die zu importierenden Daten stammen aus der Entit\u00e4t ["+sEntityNameExport+"]\n" +
				//"Diese Entit\u00e4t stimmt nicht mit der aktuell ausgew\u00e4hlten Entit\u00e4t ["+sEntityName+"] \u00fcberein.";

				getProtocolFacade().writeExportImportLogEntry(iProtocolId, XmlExportImportHelper.EXPIMP_MESSAGE_LEVEL_ERROR,
						XmlExportImportHelper.EXPIMP_ACTION_INSERT, sMessage, iActionNumber++);

				throw new NuclosBusinessException(StringUtils.getParameterizedExceptionMessage("xmlimport.error.incompatible.entity", sEntityNameExport, sEntityName));
			}

//			// special handling for elisa (see export facade)
//			if (bDeepexport) {
//				bElisaDeepExport = true;
//				bDeepexport = false;
//			}
//			else {
//				bElisaDeepExport = false;
//			}

			// iterate through child elements of root
			for (Iterator<Element> i = root.elementIterator(); i.hasNext();) {
				Element element = i.next();
				if (element.attribute("type").getValue().equals("masterdata")) {
					importMDEntity(element);
				}
				else if (element.attribute("type").getValue().equals("genericObject")) {
					importGOEntity(element);
				}
			}

			// delete not used data
			removeNotExportedData();

			info("END Import");
		}
		finally {
			// delete export dir
			XmlExportImportHelper.deleteDir(expimpTimestampDir);
		}
	}

	/**
	 * determines whether an entity with the given id was already imported.
	 * if entity with given id was imported, then return the corresponding id
	 * of the target system, otherwise null
	 * @param pairToDetermine
	 * @return String
	 */
	private String getMappedId(Pair<String, String> pairToDetermine) {
		for (Pair<String, String> pairExists : idMap.keySet()) {
			if (pairExists.getX().equals(pairToDetermine.getX()) &&
					pairExists.getY().equals(pairToDetermine.getY())) {
				return idMap.get(pairExists);
			}
		}
		return null;
	}

	/**
	 * determines whether an entity with the given id was already imported.
	 * if entity with given id was imported, then return the corresponding id
	 * of the target system, otherwise null
	 * Caution: this works only fine, if the object id is unique, but normally the id should be unique,
	 * nevertheless this method should only be used in the context of genericobjectrelation
	 * @param sOldId
	 * @return String
	 */
	private String getMappedId(String sOldId) {
		for (Pair<String, String> pairExists : idMap.keySet()) {
			if (pairExists.getY().equals(sOldId)) {
				return idMap.get(pairExists);
			}
		}
		return null;
	}

	/**
	 * imports a Generic Object Entity
	 *
	 * @param element
	 * @throws CreateException
	 * @throws CommonPermissionException
	 * @throws CommonCreateException
	 * @throws NuclosBusinessRuleException
	 */
	@SuppressWarnings("unchecked")
	private void importGOEntity(Element element) throws CommonCreateException, CommonPermissionException, CreateException, NuclosBusinessRuleException {
		// Map for Attributes
		Map<Integer, DynamicAttributeVO> attributesMap = new HashMap<Integer, DynamicAttributeVO>();
		// Map for System Attributes
		Map<String, String> systemAttributes = new HashMap<String, String>();
		// Map for loaded Attribute Ids
		Set<Integer> loadattr = new HashSet<Integer>();

		Boolean bImportData = new Boolean(element.attributeValue("import"));

		// get id for the exported Module on the target system
		Integer iModuleId = modules.getModuleIdByEntityName(element.attributeValue("name"));

		String sProcessId = element.attributeValue("process");
		Integer iProcessId = (sProcessId == null || sProcessId.equals("")) ? null : new Integer(sProcessId);

		info("Import GenericObject Entity: "+element.attributeValue("name")+" ModuleId: "+iModuleId+" Export-Id: "+element.attribute("id").getValue());

		// Attribute Iterator
		Iterator<Element> elmit = element.elementIterator();

		try {
			// setup usage criteria
			AttributeCVO attrcvo = attributeCache.getAttribute(iModuleId, NuclosEOField.PROCESS.getMetaData().getField());
			DynamicAttributeVO davo = new DynamicAttributeVO(null, attrcvo.getId(), null, sProcessId);
			// put new attribute into the Map
			attributesMap.put(davo.getAttributeId(), davo);
			// put atributeId to loaded Map
			loadattr.add(attrcvo.getId());

			davo = null;
			// Iterate Attributes of the XML File
			while (elmit.hasNext()) {
				Element elem = elmit.next();
				attrcvo = attributeCache.getAttribute(iModuleId, elem.attribute("name").getText());

				debug("Import GenericObject Attribute: "+elem.attribute("name").getText());

				// fill system attribute map
				if (elem.attribute("name").getText().startsWith("[")) {
					systemAttributes.put(elem.attribute("name").getText(), elem.element("value").getTextTrim());
				}

				// attribute has references
				if (Boolean.valueOf(elem.attribute("hasReference").getText())) {
					// if attribute nuclosState set the right status model
					// reference
					if (elem.attribute("name").getText().equals(NuclosEOField.STATE.getMetaData().getField())) {
						String sStatus = elem.element("value").getTextTrim();

						// get new StatusId
						Integer newStatusId = getReferenecedStatusId(iModuleId, iProcessId, sStatus);

						// create Attribute
						davo = new DynamicAttributeVO(null, attrcvo.getId(), newStatusId, sStatus);
					}
					// if attribute nuclosProcess
					else if (elem.attribute("name").getText().equals(NuclosEOField.PROCESS.getMetaData().getField())) {
						davo = new DynamicAttributeVO(null, attrcvo.getId(),
								this.getReferencedProcessId(iModuleId, elem.element("value").getTextTrim()), elem.element("value").getTextTrim());

					// common Atributes with references
					} else {
						Boolean bIsForgeinEntityImportExportable = true;
						String sExternalEntity = AttributeCache.getInstance().getAttribute(iModuleId, elem.attribute("name").getText()).getExternalEntity();
						// id aus map ersetzen
						String oldId = elem.element("referenceId").getTextTrim();
						String newId = getMappedId(new Pair<String, String>(sExternalEntity, oldId));

						if (Modules.getInstance().isModuleEntity(sExternalEntity)) {
							bIsForgeinEntityImportExportable = Modules.getInstance().isImportExportable(sExternalEntity);
						}
						else {
							MasterDataMetaVO mdmvo = MasterDataMetaCache.getInstance().getMetaData(sExternalEntity);
							bIsForgeinEntityImportExportable = (mdmvo == null) ? false : mdmvo.getIsImportExport();
						}


						if (newId == null && bIsForgeinEntityImportExportable) {
							String sMessage = StringUtils.getParameterizedExceptionMessage("xmlimport.error.missing.reference.1", elem.attribute("name").getText(), element.attributeValue("name"), element.attribute("id").getValue());
//								"Fehler beim Ermitteln der Referenz-Id f\u00fcr das Attribut ["+elem.attribute("name").getText()+"]"+
//							" der Entit\u00e4t "+element.attributeValue("name")+" mit der Id ["+element.attribute("id").getValue()+"]";
							getProtocolFacade().writeExportImportLogEntry(iProtocolId, XmlExportImportHelper.EXPIMP_MESSAGE_LEVEL_ERROR,
								XmlExportImportHelper.EXPIMP_ACTION_INSERT, 
								MessageFormat.format(locale.getResourceById(locale.getUserLocale(), "xmlimport.error.missing.reference.1"), elem.attribute("name").getText(), element.attributeValue("name"), element.attribute("id").getValue()), iActionNumber++);

							if (NuclosEntity.REPORT.checkEntityName(sExternalEntity)) {
								sMessage = StringUtils.getParameterizedExceptionMessage("xmlimport.error.missing.reference.2", elem.attribute("name").getText(), element.attributeValue("name"), element.attribute("id").getValue());
									//" Sie besitzen kein Recht auf den referenzierten Report bzw. Formular.";
								getProtocolFacade().writeExportImportLogEntry(iProtocolId, XmlExportImportHelper.EXPIMP_MESSAGE_LEVEL_ERROR,
									XmlExportImportHelper.EXPIMP_ACTION_INSERT, 
									MessageFormat.format(locale.getResourceById(locale.getUserLocale(), "xmlimport.error.missing.reference.2"), elem.attribute("name").getText(), element.attributeValue("name"), element.attribute("id").getValue()), iActionNumber++);
							}
							else {
								sMessage = StringUtils.getParameterizedExceptionMessage("xmlimport.error.missing.reference.3", elem.attribute("name").getText(), element.attributeValue("name"), element.attribute("id").getValue());
									//" Evtl. wurde die Referenzierte Entit\u00e4t zuvor nicht exportiert und konnte deshalb nicht gefunden werden.";
								getProtocolFacade().writeExportImportLogEntry(iProtocolId, XmlExportImportHelper.EXPIMP_MESSAGE_LEVEL_ERROR,
									XmlExportImportHelper.EXPIMP_ACTION_INSERT, 
									MessageFormat.format(locale.getResourceById(locale.getUserLocale(), "xmlimport.error.missing.reference.3"), elem.attribute("name").getText(), element.attributeValue("name"), element.attribute("id").getValue()), iActionNumber++);
							}
							
							
							throw new NuclosFatalException(sMessage);
						}
						else {
							if (!bIsForgeinEntityImportExportable) {
								newId = oldId;
							}
						}

						davo = new DynamicAttributeVO(null, attrcvo.getId(), new Integer(newId), elem.element("value").getTextTrim());
					}
				}
				// attributes without references
				else {
					davo = new DynamicAttributeVO(null, attrcvo.getId(), null, elem.element("value").getTextTrim());

					// check whether attribute has a value list
					if (!attrcvo.getValues().isEmpty()) {
						Element eValueId = elem.element("valueId");
						String sValueId = (eValueId == null) ? null : eValueId.getTextTrim();
						if (sValueId != null && !sValueId.equals("")) {
							davo.setValueId(new Integer(sValueId));
						}
					}
				}
				// put new attribute into the Map
				attributesMap.put(davo.getAttributeId(), davo);
				// put atributeId to loaded Map
				loadattr.add(attrcvo.getId());
			}

			// TODO nicht generisch, complete missing system attributes
			if (!systemAttributes.containsKey(NuclosEOField.PROCESS.getMetaData().getField())) {
				attributesMap.put(attributeCache.getAttribute(iModuleId, NuclosEOField.PROCESS.getMetaData().getField()).getId(),
						new DynamicAttributeVO(null, attributeCache.getAttribute(iModuleId, NuclosEOField.PROCESS.getMetaData().getField()).getId(), null, ""));
				loadattr.add(attributeCache.getAttribute(iModuleId, NuclosEOField.PROCESS.getMetaData().getField()).getId());
			}
			if (!systemAttributes.containsKey(NuclosEOField.ORIGIN.getMetaData().getField())) {
				attributesMap.put(attributeCache.getAttribute(iModuleId, NuclosEOField.ORIGIN.getMetaData().getField()).getId(),
						new DynamicAttributeVO(null, attributeCache.getAttribute(iModuleId, NuclosEOField.ORIGIN.getMetaData().getField()).getId(), null, ""));
				loadattr.add(attributeCache.getAttribute(iModuleId, NuclosEOField.ORIGIN.getMetaData().getField()).getId());
			}

			// read parentId and deleted flag
			Integer parentId = null;
			if (!element.attributeValue("parentId").equals("null")) {
				parentId = Integer.valueOf(element.attributeValue("parentId"));
			}
			Boolean deleted = Boolean.valueOf(element.attributeValue("deleted"));

			// 1. step create NuclosValueObject with MetaData
			NuclosValueObject nvo = new NuclosValueObject(null,
					(Date)CanonicalAttributeFormat.getInstance(Date.class).parse(attributesMap.get(AttributeCache.getInstance().getAttribute(iModuleId, NuclosEOField.CREATEDAT.getMetaData().getField()).getId()).getCanonicalValue(AttributeCache.getInstance())),
					systemAttributes.get(NuclosEOField.CREATEDBY.getMetaData().getField()),
					(Date)CanonicalAttributeFormat.getInstance(Date.class).parse(attributesMap.get(AttributeCache.getInstance().getAttribute(iModuleId, NuclosEOField.CHANGEDAT.getMetaData().getField()).getId()).getCanonicalValue(AttributeCache.getInstance())),
					systemAttributes.get(NuclosEOField.CHANGEDBY.getMetaData().getField()), 1);

			// 2. step create GenericObjectVO
			GenericObjectVO govo = new GenericObjectVO(nvo, iModuleId, parentId,	null, loadattr, deleted);
			// set attributes from attribute Map
			govo.setAttributes(attributesMap.values());

			// check if there is an existing GO with same Values in the unique
			// attributes
			GenericObjectVO existingGO = getExistingGOEntity(govo, iModuleId);

			// Entity already exists
			if (existingGO != null) {
				// modify Version and id
				nvo = new NuclosValueObject(existingGO.getId(),
						(Date)CanonicalAttributeFormat.getInstance(Date.class).parse(attributesMap.get(AttributeCache.getInstance().getAttribute(iModuleId, NuclosEOField.CREATEDAT.getMetaData().getField()).getId()).getCanonicalValue(AttributeCache.getInstance())),
						//dateFormat.parse(metaAttributes.get(NuclosEOField.CREATEDAT.getMetaData().getField())),
						systemAttributes.get(NuclosEOField.CREATEDBY.getMetaData().getField()),
						(Date)CanonicalAttributeFormat.getInstance(Date.class).parse(attributesMap.get(AttributeCache.getInstance().getAttribute(iModuleId, NuclosEOField.CHANGEDAT.getMetaData().getField()).getId()).getCanonicalValue(AttributeCache.getInstance())),
						//dateFormat.parse(metaAttributes.get(NuclosEOField.CHANGEDAT.getMetaData().getField())),
						systemAttributes.get(NuclosEOField.CHANGEDBY.getMetaData().getField()),
						existingGO.getVersion());

				Collection<DynamicAttributeVO> exattr = existingGO.getAttributes();

				// replace attribute values
				for (DynamicAttributeVO dynattr : exattr) {
					if (attributesMap.containsKey(dynattr.getAttributeId())) {
						DynamicAttributeVO mapvalue = attributesMap.get(dynattr.getAttributeId());
						dynattr.setCanonicalValue(mapvalue.getCanonicalValue(attributeCache), attributeCache);
						if (mapvalue.getValueId() != null) {
							dynattr.setValueId(mapvalue.getValueId());
						}
						attributesMap.remove(dynattr.getAttributeId());
					}
					else {
						dynattr.remove();
					}
				}

				govo = new GenericObjectVO(nvo, iModuleId, parentId, null, loadattr, deleted);

				// set existing attributes with new values
				govo.setAttributes(exattr);
				// set new attributes
				for (DynamicAttributeVO dynattr : attributesMap.values()) {
					govo.setAttribute(dynattr);
				}

				// read all subforms of all layouts
				if (!mpGoSubFormsWithForeignKeys.containsKey(govo.getModuleId())) {
					List<Map<EntityAndFieldName, String>> lseafn = new ArrayList<Map<EntityAndFieldName, String>>();
					for (Integer iLayoutId : genericObjectMetaDataCache.getLayoutIdsByModuleId(govo.getModuleId(), false)) {
						LayoutFacadeLocal layoutFacade = ServiceLocator.getInstance().getFacade(LayoutFacadeLocal.class);
						Map<EntityAndFieldName, String> subformtree = layoutFacade.getSubFormEntityAndParentSubFormEntityNamesByLayoutId(iLayoutId);
						Map<EntityAndFieldName, String> subformtree_with_fkeys = new HashMap<EntityAndFieldName, String>();
						for (EntityAndFieldName eafn : subformtree.keySet()) {
							String sParentEntity = element.attributeValue("name");
							if (subformtree.get(eafn) != null) {
								sParentEntity = subformtree.get(eafn);
							}

							String s1 = eafn.getEntityName();
							String sForeignKeyFieldName = XmlExportImportHelper.getForeignKeyFieldName(sParentEntity, eafn.getFieldName(), eafn.getEntityName());
							subformtree_with_fkeys.put(new EntityAndFieldName(s1, sForeignKeyFieldName), subformtree.get(eafn));
						}
						lseafn.add(subformtree_with_fkeys);
					}
					mpGoSubFormsWithForeignKeys.put(govo.getModuleId(), lseafn);
				}

				// if data was "imported" remove all dependant data, because the dependant data were exported as well
				if (bImportData) {
					for(Map<EntityAndFieldName, String> mpSubFormsWithForeignKeys : mpGoSubFormsWithForeignKeys.get(govo.getModuleId())) {
						readDependants(mpSubFormsWithForeignKeys, null, existingGO.getId(), new DependantMasterDataMap());
					}
				}

				// call modify Method
				String sMessage;// = StringUtils.getParameterizedExceptionMessage("xmlimport.error.module.entity.1", element.attribute("name").getValue(), existingGO.getId());
					//"Modul-Entit\u00e4t ["+element.attribute("name").getValue()+"] mit der ID ["+existingGO.getId()+"] ";
				String sAction = XmlExportImportHelper.EXPIMP_ACTION_UPDATE;
				if (bImportData) {
					GenericObjectWithDependantsVO modifiedGO = getGenericObjectFacade().modify(iModuleId, new GenericObjectWithDependantsVO(govo, null));

					if ((modifiedGO.getId()).compareTo(existingGO.getId()) != 0) {
						sMessage = StringUtils.getParameterizedExceptionMessage("xmlimport.error.module.entity.2", element.attribute("name").getValue(), existingGO.getId());
							//"konnte nicht im Zielsystem ermittelt werden.";
						getProtocolFacade().writeExportImportLogEntry(iProtocolId, XmlExportImportHelper.EXPIMP_MESSAGE_LEVEL_ERROR,
								sAction, MessageFormat.format(locale.getResourceById(locale.getUserLocale(), "xmlimport.error.module.entity.2"), element.attribute("name").getValue(), existingGO.getId()), iActionNumber++);
						throw new NuclosFatalException(sMessage);
					}
					else {
						sMessage = MessageFormat.format(locale.getResourceById(locale.getUserLocale(), "xmlimport.error.module.entity.3"), element.attribute("name").getValue(), existingGO.getId()); 
							//"erfolgreich ge\u00e4ndert.";
						info(sMessage);
						getProtocolFacade().writeExportImportLogEntry(iProtocolId, XmlExportImportHelper.EXPIMP_MESSAGE_LEVEL_INFO,
								sAction, sMessage, iActionNumber++);
					}
				}
				else {
					sAction = XmlExportImportHelper.EXPIMP_ACTION_READ;
					sMessage = MessageFormat.format(locale.getResourceById(locale.getUserLocale(), "xmlimport.error.module.entity.4"), element.attribute("name").getValue(), existingGO.getId()); 
						//"erfolgreich im Zielsystem ermittelt.";
					info(sMessage);
					getProtocolFacade().writeExportImportLogEntry(iProtocolId, XmlExportImportHelper.EXPIMP_MESSAGE_LEVEL_INFO, sAction, sMessage, iActionNumber++);
				}

				// create old/new Id matching
				idMap.put(new Pair<String, String>(element.attribute("name").getValue(),element.attribute("id").getValue()), String.valueOf(existingGO.getId()));
				// remember all imported genericobjects
				lstAllImportedEntityIds.add(new Pair<String, Integer>(element.attributeValue("name"), existingGO.getId()));

//				info(sMessage);
//				getProtocolFacade().writeExportImportLogEntry(iProtocolId, XmlExportImportHelper.EXPIMP_MESSAGE_LEVEL_INFO,
//						sAction, sMessage, iActionNumber++);
			}
			// Entity not exists
			else {
				// create new GO
				if (bImportData) {
					GenericObjectVO newGoVO = getGenericObjectFacade().create(new GenericObjectWithDependantsVO(govo, new DependantMasterDataMap()));

					// create old/new Id matching
					idMap.put(new Pair<String, String>(element.attributeValue("name"),element.attribute("id").getValue()), String.valueOf(newGoVO.getId()));
					// remember all imported genericobjects
					lstAllImportedEntityIds.add(new Pair<String, Integer>(element.attributeValue("name"), newGoVO.getId()));

					String sMessage = MessageFormat.format(locale.getResourceById(locale.getUserLocale(), "xmlimport.error.module.entity.1"), element.attribute("name").getValue(), newGoVO.getId());
						//"Modul-Entit\u00e4t ["+element.attribute("name").getValue()+"] mit der ID ["+newGoVO.getId()+"] erfolgreich angelegt.";
					info(sMessage);
					getProtocolFacade().writeExportImportLogEntry(iProtocolId, XmlExportImportHelper.EXPIMP_MESSAGE_LEVEL_INFO,
							XmlExportImportHelper.EXPIMP_ACTION_INSERT, sMessage, iActionNumber++);
				}
			}
		}
		catch (Exception e) {
			String sMessage = StringUtils.getParameterizedExceptionMessage("xmlimport.error.module.entity.5", element.attribute("name").getValue(), element.attribute("id").getValue(), e);  
				//"Modul-Entit\u00e4t ["+element.attribute("name").getValue()+"] mit der Export-ID ["+element.attribute("id").getValue()+"] konnte nicht angelegt werden. "+e;
			getProtocolFacade().writeExportImportLogEntry(iProtocolId, XmlExportImportHelper.EXPIMP_MESSAGE_LEVEL_ERROR,
					XmlExportImportHelper.EXPIMP_ACTION_INSERT, 
					MessageFormat.format(locale.getResourceById(locale.getUserLocale(), "xmlimport.error.module.entity.5"), element.attribute("name").getValue(), element.attribute("id").getValue(), e), iActionNumber++);
			throw new NuclosFatalException(sMessage);
		}
	}

	/**
	 * imports a MasterData Entity
	 *
	 * @param element
	 *            node of the exportet Entity in the xml file
	 * @throws CreateException
	 * @throws CommonPermissionException
	 * @throws CommonCreateException
	 * @throws CommonFinderException
	 * @throws ElisaBusinessException
	 */
	@SuppressWarnings("unchecked")
	private void importMDEntity(Element element) throws CommonCreateException, CommonPermissionException, CreateException, NuclosBusinessException, CommonFinderException {
		info("Import MasterData Entity: " + element.attributeValue("name")+" Export-Id: "+element.attribute("id").getValue());

		Boolean bImportData = new Boolean(element.attributeValue("import"));

		// fetch metadata
		MasterDataMetaVO mdm = getMasterDataFacade().getMetaData(element.attribute("name").getValue());
		List<MasterDataMetaFieldVO> metafields = mdm.getFields();

		// Map for MD values
		Map<String, Object> mpFields = new HashMap<String, Object>();
		// Map for additional system Attributes like created_by, changed_at, version ...
		Map<String, String> metaAttributes = new HashMap<String, String>();

		// iterates the MD Meta fields and fills the Master Data Value Map
		Iterator<MasterDataMetaFieldVO> fieldIt = metafields.iterator();
		MasterDataMetaFieldVO mfield;
		while (fieldIt.hasNext()) {
			mfield = fieldIt.next();

			debug("Import MasterDataField: "+mfield.getFieldName());

			try {
				// if Field with Reference
				if (mfield.getForeignEntity() != null) {
					Element elm = element.element(mfield.getFieldName() + "Id");
					if (elm != null) {
						String oldId = elm.getTextTrim();

						if (oldId != null && !oldId.equals("")) {
							Boolean bIsForgeinEntityImportExportable = true;
							String sForeignEntity = mfield.getForeignEntity();

							assert !StringUtils.isNullOrEmpty(sForeignEntity);

							if (Modules.getInstance().isModuleEntity(sForeignEntity)) {
								bIsForgeinEntityImportExportable = Modules.getInstance().isImportExportable(sForeignEntity);
							}
							else {
								MasterDataMetaVO mdmvo = MasterDataMetaCache.getInstance().getMetaData(sForeignEntity);
								bIsForgeinEntityImportExportable = (mdmvo == null) ? false : mdmvo.getIsImportExport();
							}

							// match old to new Id
							Object newId = null;
							// special handling for entity genericobjectrelation, because the foreignkey entity is set to generalsearch
							// for the fields source and destination and therefore it's not possible to determine the corresponding object ids
//							if ("genericobjectrelation".equals(element.attributeValue("name")) &&
//									(mfield.getFieldName().equals("source") || mfield.getFieldName().equals("destination"))) {
							if ("generalSearch".equalsIgnoreCase(sForeignEntity)) {
								bIsForgeinEntityImportExportable = true;
								newId = getMappedId(oldId);
							}
							else {
								newId = getMappedId(new Pair<String, String>(sForeignEntity, oldId));
							}

							if (newId == null && bIsForgeinEntityImportExportable) {
								String sMessage; 
//									"Fehler beim Ermitteln der Referenz-Id f\u00fcr das Feld ["+mfield.getFieldName()+"]"+
//								" der Entit\u00e4t "+element.attributeValue("name")+" mit der Id ["+element.attribute("id").getValue()+"]";

								if (NuclosEntity.REPORT.checkEntityName(sForeignEntity)) {
									sMessage = StringUtils.getParameterizedExceptionMessage("xmlimport.error.missing.mdreference.2", mfield.getFieldName(), element.attributeValue("name"), element.attribute("id").getValue());
										//" Sie besitzen kein Recht auf den referenzierten Report bzw. Formular.";
									getProtocolFacade().writeExportImportLogEntry(iProtocolId, XmlExportImportHelper.EXPIMP_MESSAGE_LEVEL_ERROR,
										XmlExportImportHelper.EXPIMP_ACTION_INSERT, 
										MessageFormat.format(locale.getResourceById(locale.getUserLocale(), "xmlimport.error.missing.mdreference.2"), mfield.getFieldName(), element.attributeValue("name"), element.attribute("id").getValue()), iActionNumber++);
								}
								else {
									sMessage = StringUtils.getParameterizedExceptionMessage("xmlimport.error.missing.mdreference.3", mfield.getFieldName(), element.attributeValue("name"), element.attribute("id").getValue());
										//" Evtl. wurde die Referenzierte Entit\u00e4t zuvor nicht exportiert und konnte deshalb nicht gefunden werden.";
									getProtocolFacade().writeExportImportLogEntry(iProtocolId, XmlExportImportHelper.EXPIMP_MESSAGE_LEVEL_ERROR,
										XmlExportImportHelper.EXPIMP_ACTION_INSERT, 
										MessageFormat.format(locale.getResourceById(locale.getUserLocale(), "xmlimport.error.missing.mdreference.3"), mfield.getFieldName(), element.attributeValue("name"), element.attribute("id").getValue()), iActionNumber++);
								}
//								getProtocolFacade().writeExportImportLogEntry(iProtocolId, XmlExportImportHelper.EXPIMP_MESSAGE_LEVEL_ERROR,
//										XmlExportImportHelper.EXPIMP_ACTION_INSERT, sMessage, iActionNumber++);
								throw new NuclosFatalException(sMessage);
							}
							else {
								if (!bIsForgeinEntityImportExportable) {
									newId = oldId;
								}
							}
							mpFields.put(mfield.getFieldName() + "Id", Integer.parseInt(newId.toString()));
						}
					}
				}
				// if field without a Reference
				else {
					Element elm = element.element(mfield.getFieldName());
					if (elm != null) {
						String selmText = elm.getText();
						if (!"".equals(selmText)) {
							putFieldToMap(mpFields, mfield, selmText, element);
						}
					}
				}
			}
			catch (ParseException e) {
				String sMessage = StringUtils.getParameterizedExceptionMessage("xmlimport.error.parse.value", element.attribute("name").getValue(), element.attribute("id").getValue(), e);  
//					"Fehler beim Parsen eines Wertes der zu importierenden Datei bei der Stammdaten-Entit\u00e4t ["+element.attribute("name").getValue()+"] " +
//				"mit der Export-ID ["+element.attribute("id").getValue()+"]. "+e;
				getProtocolFacade().writeExportImportLogEntry(iProtocolId, XmlExportImportHelper.EXPIMP_MESSAGE_LEVEL_ERROR,
						XmlExportImportHelper.EXPIMP_ACTION_INSERT, 
						MessageFormat.format(locale.getResourceById(locale.getUserLocale(), "xmlimport.error.parse.value"), element.attribute("name").getValue(), element.attribute("id").getValue(), e), iActionNumber++);
				throw new NuclosFatalException(sMessage);
			}
			catch (IOException e) {
				String sMessage = StringUtils.getParameterizedExceptionMessage("xmlimport.error.read.file", element.attribute("name").getValue(), element.attribute("id").getValue(), e);  
//					"Fehler beim lesen einer zu importierenden Datei bei der Stammdaten-Entit\u00e4t ["+element.attribute("name").getValue()+"] " +
//				"mit der Export-ID ["+element.attribute("id").getValue()+"]. "+e;
				getProtocolFacade().writeExportImportLogEntry(iProtocolId, XmlExportImportHelper.EXPIMP_MESSAGE_LEVEL_ERROR,
						XmlExportImportHelper.EXPIMP_ACTION_INSERT, 
						MessageFormat.format(locale.getResourceById(locale.getUserLocale(), "xmlimport.error.read.file"), element.attribute("name").getValue(), element.attribute("id").getValue(), e), iActionNumber++);
				throw new NuclosFatalException(sMessage);
			}
			catch (ClassNotFoundException e) {
				String sMessage = StringUtils.getParameterizedExceptionMessage("xmlimport.error.read.object", element.attribute("name").getValue(), element.attribute("id").getValue(), e); 
//					"Fehler beim lesen eines zu importierenden Objekts der Stammdaten-Entit\u00e4t ["+element.attribute("name").getValue()+"] " +
//				"mit der Export-ID ["+element.attribute("id").getValue()+"]. "+e;
				getProtocolFacade().writeExportImportLogEntry(iProtocolId, XmlExportImportHelper.EXPIMP_MESSAGE_LEVEL_ERROR,
						XmlExportImportHelper.EXPIMP_ACTION_INSERT, 
						MessageFormat.format(locale.getResourceById(locale.getUserLocale(), "xmlimport.error.read.object"), element.attribute("name").getValue(), element.attribute("id").getValue(), e), iActionNumber++);
				throw new NuclosFatalException(sMessage);
			}
			catch (Exception e) {
				throw new NuclosFatalException(e);
			}
		}

		// fill MD additional System attributes like created_by, changed_at
		Iterator<Element> i2 = element.elementIterator();
		while (i2.hasNext()) {
			Element field = i2.next();
			if (field.getName().equals("attribute")) {
				metaAttributes.put(field.attributeValue("name"), field.getTextTrim());
			}
		}

		// prepare new MD Record
		MasterDataVO prepVO;
		try {
			String sCreatedAt = metaAttributes.get(NuclosEOField.CREATEDAT.getMetaData().getField());
			String sCreatedBy = metaAttributes.get(NuclosEOField.CREATEDBY.getMetaData().getField());
			String sChangedAt = metaAttributes.get(NuclosEOField.CHANGEDAT.getMetaData().getField());
			String sChangedBy = metaAttributes.get(NuclosEOField.CHANGEDBY.getMetaData().getField());

			String sNow = DateUtils.toString(DateUtils.now()) + " 00:00:00";

			prepVO = new MasterDataVO(null,
					dateFormat.parse((sCreatedAt == null) ? sNow : sCreatedAt),
					(sCreatedBy == null) ? "INITIAL" : sCreatedBy,
					dateFormat.parse((sChangedAt == null) ? sNow : sChangedAt),
					(sChangedBy == null) ? "INITIAL" : sChangedBy, 1, mpFields);
		}
		catch (ParseException e) {
			String sMessage = StringUtils.getParameterizedExceptionMessage("xmlimport.error.parse.date", element.attribute("name").getValue(), element.attribute("id").getValue(), e);   
//				"Fehler beim Parsen des Datums bei der Stammdaten-Entit\u00e4t ["+element.attribute("name").getValue()+"] " +
//				"mit der Export-ID ["+element.attribute("id").getValue()+"]. "+e;
			getProtocolFacade().writeExportImportLogEntry(iProtocolId, XmlExportImportHelper.EXPIMP_MESSAGE_LEVEL_ERROR,
					XmlExportImportHelper.EXPIMP_ACTION_INSERT, 
					MessageFormat.format(locale.getResourceById(locale.getUserLocale(), "xmlimport.error.parse.date"), element.attribute("name").getValue(), element.attribute("id").getValue(), e), iActionNumber++);
			throw new NuclosFatalException(sMessage);
		}

		// check if Entity already exists
		MasterDataVO existingEntity = getExistingMDEntity(prepVO, element.attribute("name").getValue());

		if (existingEntity != null) {
			// entity exists
			try {
				// set old id and old version
				prepVO = new MasterDataVO(existingEntity.getId(),
						prepVO.getCreatedAt(), prepVO.getCreatedBy(),
						prepVO.getChangedAt(), prepVO.getChangedBy(),
						existingEntity.getVersion(), prepVO.getFields());

				// read all dependant data
				DependantMasterDataMap dmdm = null;
				String sEntityName = element.attribute("name").getValue();

				if (!mpMdSubFormsWithForeignKeys.containsKey(sEntityName)) {
					LayoutFacadeLocal layoutFacade = ServiceLocator.getInstance().getFacade(LayoutFacadeLocal.class);
					mpMdSubFormsWithForeignKeys.put(sEntityName, layoutFacade.getSubFormEntityAndParentSubFormEntityNames(sEntityName,prepVO.getIntId(),true));
				}

				// if data was "imported" remove all dependant data, because the dependant data were exported as well
				if (bImportData) {
					readDependants(mpMdSubFormsWithForeignKeys.get(sEntityName), null, existingEntity.getIntId(), new DependantMasterDataMap());
				}

				// call modify Method
				String sMessage;// = "Stammdaten-Entit\u00e4t ["+element.attribute("name").getValue()+"] mit der ID ["+existingEntity.getId()+"] ";
				String sAction = XmlExportImportHelper.EXPIMP_ACTION_UPDATE;
				if (bImportData) {
					Object modid = getMasterDataFacade().modify(element.attribute("name").getValue(), prepVO, dmdm);

					if (((Integer)modid).compareTo((Integer)existingEntity.getId()) != 0) {
						sMessage = StringUtils.getParameterizedExceptionMessage("xmlimport.error.masterdata.entity.2", element.attribute("name").getValue(), existingEntity.getId());
							//"konnte nicht im Zielsystem ermittelt werden.";
						getProtocolFacade().writeExportImportLogEntry(iProtocolId, XmlExportImportHelper.EXPIMP_MESSAGE_LEVEL_ERROR, sAction, 
							MessageFormat.format(locale.getResourceById(locale.getUserLocale(), "xmlimport.error.masterdata.entity.2"), element.attribute("name").getValue(), existingEntity.getId()), iActionNumber++);
						throw new NuclosFatalException(sMessage);
					}
					else {
						sMessage = MessageFormat.format(locale.getResourceById(locale.getUserLocale(), "xmlimport.error.masterdata.entity.3"), element.attribute("name").getValue(), existingEntity.getId()); 
							//"erfolgreich ge\u00e4ndert.";
						info(sMessage);
						getProtocolFacade().writeExportImportLogEntry(iProtocolId, XmlExportImportHelper.EXPIMP_MESSAGE_LEVEL_INFO,
								sAction, sMessage, iActionNumber++);
					}
				}
				else {
					sAction = XmlExportImportHelper.EXPIMP_ACTION_READ;
					sMessage = MessageFormat.format(locale.getResourceById(locale.getUserLocale(), "xmlimport.error.masterdata.entity.4"), element.attribute("name").getValue(), existingEntity.getId()); 
						//"erfolgreich im Zielsystem ermittelt.";
					info(sMessage);
					getProtocolFacade().writeExportImportLogEntry(iProtocolId, XmlExportImportHelper.EXPIMP_MESSAGE_LEVEL_INFO,
							sAction, sMessage, iActionNumber++);
				}

				// create old/new Id matching
				idMap.put(new Pair<String, String>(element.attribute("name").getValue(), element.attribute("id").getValue()), String.valueOf(existingEntity.getId()));
				// remember all imported masterdata
				lstAllImportedEntityIds.add(new Pair<String, Integer>(element.attribute("name").getValue() ,(Integer)existingEntity.getId()));

//				info(sMessage);
//				getProtocolFacade().writeExportImportLogEntry(iProtocolId, XmlExportImportHelper.EXPIMP_MESSAGE_LEVEL_INFO,
//						sAction, sMessage, iActionNumber++);
			}
			catch (Exception e) {
				String sMessage = StringUtils.getParameterizedExceptionMessage("xmlimport.error.masterdata.entity.5", element.attribute("name").getValue(), element.attribute("id"), e);  
					//"Stammdaten-Entit\u00e4t ["+element.attribute("name").getValue()+"] mit der Export-ID ["+element.attribute("id")+"] konnte nicht modifiziert werden. "+e;
				getProtocolFacade().writeExportImportLogEntry(iProtocolId, XmlExportImportHelper.EXPIMP_MESSAGE_LEVEL_ERROR,
						XmlExportImportHelper.EXPIMP_ACTION_UPDATE, 
						MessageFormat.format(locale.getResourceById(locale.getUserLocale(), "xmlimport.error.masterdata.entity.5"), element.attribute("name").getValue(), element.attribute("id"), e), iActionNumber++);
				throw new NuclosFatalException(sMessage);
			}
		}
		else {
			// entity not exists
			try {
				if (bImportData) {
					// call create Method
					MasterDataVO newVO = getMasterDataFacade().create(element.attribute("name").getValue(), prepVO, null);

					// create old/new Id matching
					idMap.put(new Pair<String, String>(element.attribute("name").getValue(), element.attribute("id").getValue()), String.valueOf(newVO.getId()));
					// remember all imported masterdata
					lstAllImportedEntityIds.add(new Pair<String, Integer>(element.attribute("name").getValue() ,newVO.getIntId()));

					String sMessage = MessageFormat.format(locale.getResourceById(locale.getUserLocale(), "xmlimport.error.masterdata.entity.6"), element.attribute("name").getValue(), newVO.getIntId()); 
						//"Stammdaten-Entit\u00e4t ["+element.attribute("name").getValue()+"] mit der ID ["+newVO.getIntId()+"] erfolgreich angelegt.";
					info(sMessage);
					getProtocolFacade().writeExportImportLogEntry(iProtocolId, XmlExportImportHelper.EXPIMP_MESSAGE_LEVEL_INFO,
							XmlExportImportHelper.EXPIMP_ACTION_INSERT, sMessage, iActionNumber++);
				}
			}
			catch (Exception e) {
				String sMessage = StringUtils.getParameterizedExceptionMessage("xmlimport.error.masterdata.entity.7", element.attribute("name").getValue(), element.attribute("id"), e);  
					//"Stammdaten-Entit\u00e4t ["+element.attribute("name").getValue()+"] mit der Export-ID ["+element.attribute("id")+"] konnte nicht angelegt werden. "+e;
				getProtocolFacade().writeExportImportLogEntry(iProtocolId, XmlExportImportHelper.EXPIMP_MESSAGE_LEVEL_ERROR,
						XmlExportImportHelper.EXPIMP_ACTION_INSERT, 
						MessageFormat.format(locale.getResourceById(locale.getUserLocale(), "xmlimport.error.masterdata.entity.7"), element.attribute("name").getValue(), element.attribute("id"), e), iActionNumber++);
				throw new NuclosFatalException(sMessage);
			}
		}
	}

	private void putFieldToMap(Map<String, Object> mpFields, MasterDataMetaFieldVO mfield, String selmText, Element element) throws IOException, ClassNotFoundException, ParseException, CommonCreateException, CommonPermissionException, CreateException, NuclosBusinessRuleException, NumberFormatException, Base64DecodingException {
		// Import documentfiles
		if (mfield.getJavaClass().getName().equals("org.nuclos.server.genericobject.valueobject.GenericObjectDocumentFile")){
			try {
				if (selmText != null && !selmText.equals("")) {
					File expimpDir = NuclosSystemParameters.getDirectory(NuclosSystemParameters.EXPORT_IMPORT_PATH);
					File expimpResourceDir = new File(expimpDir, "" + today.getTime() + "/ressource");
					File df = new File(expimpResourceDir, element.attributeValue("id")+"."+selmText);
					GenericObjectDocumentFile godf = new GenericObjectDocumentFile(selmText,IOUtils.readFromBinaryFile(df));
					mpFields.put(mfield.getFieldName(), godf);
					df = null;
				}
			}
			catch (Exception e) {
				//String sMessage = StringUtils.getParameterizedExceptionMessage("xmlimport.error.masterdata.entity.8", selmText, element.attribute("name").getValue(), element.attribute("id"), e); 
					//"Das Dokument ["+selmText+"] f\u00fcr die Stammdaten-Entit\u00e4t ["+element.attribute("name").getValue()+"] mit der Export-ID ["+element.attribute("id")+"] konnte nicht angelegt werden. "+e;
				getProtocolFacade().writeExportImportLogEntry(iProtocolId, XmlExportImportHelper.EXPIMP_MESSAGE_LEVEL_ERROR,
						XmlExportImportHelper.EXPIMP_ACTION_INSERT, 
						MessageFormat.format(locale.getResourceById(locale.getUserLocale(), "xmlimport.error.masterdata.entity.8"), selmText, element.attribute("name").getValue(), element.attribute("id"), e), iActionNumber++);
			}
		}
		else if (mfield.getJavaClass().getName().equals("java.lang.Object")){
			if (selmText != null && !selmText.equals("")) {
				File expimpDir = NuclosSystemParameters.getDirectory(NuclosSystemParameters.EXPORT_IMPORT_PATH);
				File expimpResourceDir = new File(expimpDir, "" + today.getTime() + "/ressource");
				Object oData = XmlExportImportHelper.readObjectFromFile(expimpResourceDir, selmText);
				mpFields.put(mfield.getFieldName(), oData);
			}
		}
		else if (mfield.getJavaClass().getName().equals("[B")) {
			if (selmText != null && !selmText.equals("")) {
				mpFields.put(mfield.getFieldName(), Base64.decode(selmText));
			}
		}
		else if (mfield.getJavaClass().getName().equals("org.nuclos.server.report.ByteArrayCarrier")) {
			if (selmText != null && !selmText.equals("")) {
				mpFields.put(mfield.getFieldName(), new ByteArrayCarrier(Base64.decode(selmText)));
			}
		}
		else if (mfield.getJavaClass().getName().equals("org.nuclos.server.resource.valueobject.ResourceFile")) {
			if (selmText != null && !selmText.equals("")) {
				mpFields.put(mfield.getFieldName(), new ResourceFile(element.attributeValue("filename"), element.attributeValue("documentFileId")==null?null:Integer.parseInt(element.attributeValue("documentFileId")), Base64.decode(selmText)));
			}
		}
		else if (mfield.getJavaClass().getName().equals("java.util.Date")) {
			if (!selmText.equals("")) {
				mpFields.put(mfield.getFieldName(), dateFormat.parse(selmText));
			}
		}
		else if (mfield.getJavaClass().getName().equals("java.lang.String")) {
			mpFields.put(mfield.getFieldName(), selmText);
		}
		else if (mfield.getJavaClass().getName().equals("java.lang.Integer")) {
			if (!selmText.equals("")) {
				mpFields.put(mfield.getFieldName(), Integer.valueOf(selmText));
			}
		}
		else if (mfield.getJavaClass().getName().equals("java.lang.Boolean")) {
			if (!selmText.equals("")) {
				mpFields.put(mfield.getFieldName(), Boolean.valueOf(selmText));
			}
		}
		else if (mfield.getJavaClass().getName().equals("java.lang.Double")) {
			if (!selmText.equals("")) {
				mpFields.put(mfield.getFieldName(), Double.valueOf(selmText));
			}
		}
	}

	/**
	 * read all dependant masterdata
	 * this is necessary to delete data in the traget system which have already been deleted in the source system
	 * Note: only used in the case, that the imported data was exported in 'deepexport' mode
	 * @param subformtree
	 * @param sEntityName
	 * @param entityId
	 * @param dmdm
	 */
	private DependantMasterDataMap readDependants(final Map<EntityAndFieldName, String> subformtree, String sEntityName, Object entityId, DependantMasterDataMap dmdm) {
		LayoutFacadeLocal layoutFacade = ServiceLocator.getInstance().getFacade(LayoutFacadeLocal.class);

		List<EntityAndFieldName> entitylist = new ArrayList<EntityAndFieldName>();
		for (Entry<EntityAndFieldName, String> entry : subformtree.entrySet()) {
			if (sEntityName == null) {
				if (entry.getValue() == null) {
					entitylist.add(entry.getKey());
				}
			} else if (sEntityName.equals(entry.getValue())) {
				entitylist.add(entry.getKey());
			}
		}

		for (EntityAndFieldName eafn : entitylist) {
			Collection<MasterDataVO> mdList = getMasterDataFacade().getDependantMasterData(eafn.getEntityName(), eafn.getFieldName(), entityId);

			for (MasterDataVO mdvo : mdList) {
				DependantMasterDataMap dmdmOfChildren = readDependants(subformtree, eafn.getEntityName(), mdvo.getIntId(), new DependantMasterDataMap());
				mdvo.setDependants(dmdmOfChildren);

				// check subforms in other layouts
				if (eafn.getEntityName() != null) {
					if (Modules.getInstance().isModuleEntity(eafn.getEntityName())) {
						// this should never happen
					}
					else if (MasterDataMetaCache.getInstance().getMetaData(eafn.getEntityName()).getIsImportExport()){
						if (!mpMdSubFormsWithForeignKeys.containsKey(eafn.getEntityName())) {
							if (layoutFacade.isMasterDataLayoutAvailable(eafn.getEntityName())) {
								mpMdSubFormsWithForeignKeys.put(eafn.getEntityName(), layoutFacade.getSubFormEntityAndParentSubFormEntityNames(eafn.getEntityName(),mdvo.getIntId(),true));
							}
						}

						if (mpMdSubFormsWithForeignKeys.get(eafn.getEntityName()) != null) {
							readDependants(mpMdSubFormsWithForeignKeys.get(eafn.getEntityName()), null, mdvo.getIntId(), new DependantMasterDataMap());
						}
						//todo check this
						if (!MasterDataMetaCache.getInstance().getMetaData(eafn.getEntityName()).getUniqueFieldNames().isEmpty()) {
							Pair<String, Integer> pair = new Pair<String, Integer>(eafn.getEntityName(), mdvo.getIntId());
							if (!isEntityIdAlreadyRead(pair)){
								lstAllReadEntityIds.add(pair);
							}
						}
					}
				}

				//lstAllReadEntityIds.add(new Pair<String, MasterDataVO>(eafn.getEntityName(), mdvo));
			}
			dmdm.addAllData(eafn.getEntityName(), CollectionUtils.transform(mdList, new MasterDataToEntityObjectTransformer()));
		}
		return dmdm;
	}

	/**
	 * Method checks if there is an existing MD Entity with same Data in the
	 * unique Fields
	 *
	 * @param mdvo
	 * @param entityName
	 * @return null if there is no equal Entity, the Entities MasterDataVO if
	 *         there is one
	 * @throws CommonFinderException
	 * @throws CreateException
	 * @throws CommonPermissionException
	 * @throws CommonCreateException
	 * @throws NuclosBusinessException
	 *             if ther are more thean one equal Entity
	 */
	private MasterDataVO getExistingMDEntity(MasterDataVO mdvo,	String entityName) throws NuclosBusinessException, CommonFinderException, CommonCreateException, CommonPermissionException, CreateException {
		MasterDataMetaVO metavo = getMasterDataFacade().getMetaData(entityName);
		Set<String> ufields = metavo.getUniqueFieldNames();

		// if no unique fields are set, return null and a new record will be
		// generated
		if (ufields.size() == 0) {
			return null;
		}

		CollectableEntity entity = new CollectableMasterDataEntity(metavo);

		ArrayList<CollectableSearchCondition> conditions = new ArrayList<CollectableSearchCondition>();

		for (String field : ufields) {
			MasterDataMetaFieldVO m = metavo.getField(field);
			int fieldtype = (m.getForeignEntity() == null) ? CollectableField.TYPE_VALUEFIELD : CollectableField.TYPE_VALUEIDFIELD;

			CollectableEntityField ef = entity.getEntityField(m.getFieldName());

			CollectableField value;
			if (fieldtype == CollectableField.TYPE_VALUEFIELD) {
				value = new CollectableValueField(mdvo.getField(field));

				if (value.isNull()) {
					continue;
				}
			}
			else if (fieldtype == CollectableField.TYPE_VALUEIDFIELD) {
				value = new CollectableValueIdField(mdvo.getField(m.getFieldName()+"Id"), mdvo.getField(field));
			}
			else {
				throw new NuclosFatalException();
			}

			if (value != null) {
				if ((value instanceof CollectableValueField && value.getValue() != null && !value.getValue().equals("")) ||
						(value instanceof CollectableValueIdField && value.getValueId() != null)) {

				//if (((value.getValue() != null && !value.getValue().equals("")) || value.getValueId() != null)) {
					conditions.add(new CollectableComparison(ef, ComparisonOperator.EQUAL, value));
				}
				else {
					conditions.add(new CollectableIsNullCondition(ef, ComparisonOperator.IS_NULL));
				}
			}
		}

		TruncatableCollection<MasterDataVO> result = getMasterDataFacade().getMasterData(entityName, new CompositeCollectableSearchCondition(LogicalOperator.AND, conditions), true);
		if (result.size() > 1) {
			String sMessage = locale.getResourceById(locale.getUserLocale(), "xmlimport.error.masterdata.entity.9");//"Mehr als eine eindeutige Entit\u00e4t gefunden, bitte manuell \u00fcberpr\u00fcfen";
			error(sMessage);
			getProtocolFacade().writeExportImportLogEntry(iProtocolId, XmlExportImportHelper.EXPIMP_MESSAGE_LEVEL_ERROR,
					XmlExportImportHelper.EXPIMP_ACTION_READ, sMessage, iActionNumber++);
			throw new NuclosBusinessException("xmlimport.error.masterdata.entity.10");//"Mehr als eine eindeutige Entit\u00e4t gefunden");
		}

		if (result.iterator().hasNext()) {
			return result.iterator().next();
		}

		return null;
	}

	/**
	 * Method checks if there is an existing GO Entity with same Data in the
	 * unique Attributes
	 *
	 * @param govo
	 * @param moduleId
	 * @return
	 * @throws NuclosBusinessException
	 * @throws CommonPermissionException
	 * @throws CommonFinderException
	 * @throws CreateException
	 * @throws CommonCreateException
	 */
	private GenericObjectVO getExistingGOEntity(GenericObjectVO govo, Integer moduleId) throws NuclosBusinessException, CommonFinderException, CommonPermissionException, CommonCreateException, CreateException {

		ArrayList<CollectableSearchCondition> conditions = new ArrayList<CollectableSearchCondition>();

		List<Integer> result = getGenericObjectFacade().getGenericObjectIds(moduleId,
				new CompositeCollectableSearchCondition(LogicalOperator.AND, conditions));

		if (result.size() > 1) {
			String sMessage = locale.getResourceById(locale.getUserLocale(), "xmlimport.error.masterdata.entity.9");//"Mehr als eine eindeutige Entit\u00e4t gefunden, bitte manuell \u00fcberpr\u00fcfen";
			error(sMessage);
			getProtocolFacade().writeExportImportLogEntry(iProtocolId, XmlExportImportHelper.EXPIMP_MESSAGE_LEVEL_ERROR,
					XmlExportImportHelper.EXPIMP_ACTION_READ, sMessage, iActionNumber++);
			throw new NuclosBusinessException("xmlimport.error.masterdata.entity.10");//"Mehr als eine eindeutige Entit\u00e4t gefunden");
		}

		if (result.iterator().hasNext()) {
			return getGenericObjectFacade().get(result.iterator().next());
		}

		return null;
	}

	/**
	 * returns the matching id of the GO Process from the import system
	 *
	 * @param moduleId
	 * @param process
	 * @return
	 */
	private Integer getReferencedProcessId(Integer moduleId, String process) {
		DbQueryBuilder builder = DataBaseHelper.getDbAccess().getQueryBuilder();
		DbQuery<Integer> query = builder.createQuery(Integer.class);
		DbFrom t = query.from("T_MD_PROCESS").alias(ProcessorFactorySingleton.BASE_ALIAS);
		query.select(t.baseColumn("INTID", Integer.class));
		query.where(builder.and(
			builder.equal(t.baseColumn("INTID_T_MD_MODULE", Integer.class), moduleId),
			builder.equal(t.baseColumn("STRPROCESS", String.class), process)));
		return CollectionUtils.getFirst(DataBaseHelper.getDbAccess().executeQuery(query), 0);
	}

	/**
	 * returns the maching id of the GO status from the target system
	 *
	 * @param moduleId
	 *            Module ID
	 * @param status
	 *            State name
	 * @return Process name
	 */
	private Integer getReferenecedStatusId(Integer iModuleId, Integer iProcessId, String status) {
		UsageCriteria usagecriteria = new UsageCriteria(iModuleId, iProcessId);

		Integer stateModelId = StateModelUsagesCache.getInstance().getStateUsages().getStateModel(usagecriteria);
		Collection<StateVO> states = ServiceLocator.getInstance().getFacade(StateFacadeLocal.class).getStatesByModel(stateModelId);

		for (StateVO sta : states) {
			if (sta.getStatename().equals(status))
				return sta.getId();
		}
		return null;
	}

	/**
	 * check whether an entity with its id was already read
	 * @param pExportEntityId
	 * @return boolean
	 */
	private boolean isEntityIdAlreadyRead(Pair<String, Integer> pExportEntityId) {
		for (Pair<String, Integer> pAllEntityId : lstAllReadEntityIds ) {
			if (pAllEntityId.getX().equals(pExportEntityId.getX()) &&
					pAllEntityId.getY().compareTo(pExportEntityId.getY()) == 0) {
				return true;
			}
		}
		return false;
	}

	/**
	 * detects the entity data which was read in the target system, but not exported by the
	 * source system and removes them
	 *
	 * @throws CommonCreateException
	 * @throws CommonPermissionException
	 * @throws CreateException
	 * @throws NuclosBusinessRuleException
	 */
	private void removeNotExportedData() throws CommonCreateException, CommonPermissionException, CreateException, NuclosBusinessRuleException {
		List<Pair<String, Integer>> lstRemoveEntityIds = new ArrayList<Pair<String, Integer>>();

		for (Pair<String, Integer> pAllEntityId : lstAllReadEntityIds) {
			boolean bfound = false;
			for (Pair<String, Integer> pImportedEntityId : lstAllImportedEntityIds) {
				if (pAllEntityId.getX().equals(pImportedEntityId.getX()) &&
						pAllEntityId.getY().compareTo(pImportedEntityId.getY()) == 0) {
					bfound = true;
					break;
				}
			}

			if (!bfound) {
				lstRemoveEntityIds.add(new Pair<String, Integer>(pAllEntityId.getX(), pAllEntityId.getY()));
			}
		}

		for (Pair<String, Integer> pRemove : lstRemoveEntityIds) {
			// entity data found in the target system which is not included in the export file
			// this means, that the data was deleted in the source system, because all entity data
			// and its dependencies should be included in the export file -> delete not exported entity data

			String sEntity = pRemove.getX();
			Integer iId = pRemove.getY();
			//MasterDataVO mdvo = pRemove.getY();

			try {
				MasterDataVO mdvo = getMasterDataFacade().get(sEntity, iId);

				// 1. remove references to current mdvo
				removeReferenceToEntity(sEntity, mdvo);

				// 2. finally remove current mdvo
				getMasterDataFacade().remove(sEntity, mdvo, false);

				//String sMessage = "Die Entit\u00e4t ["+sEntity+"] mit der ID ["+mdvo.getIntId()+"] wurde erfolgreich gel\u00f6scht.";
				getProtocolFacade().writeExportImportLogEntry(iProtocolId, XmlExportImportHelper.EXPIMP_MESSAGE_LEVEL_INFO,
						XmlExportImportHelper.EXPIMP_ACTION_DELETE, 
						MessageFormat.format(locale.getResourceById(locale.getUserLocale(), "xmlimport.error.masterdata.entity.11"), sEntity, mdvo.getIntId()), iActionNumber++);
			}
			catch (Exception e) {
				String sMessage = StringUtils.getParameterizedExceptionMessage("xmlimport.error.masterdata.entity.12", sEntity, iId, e); 
					//"Die Stammdaten-Entit\u00e4t ["+sEntity+"] mit der ID ["+iId+"] konnte nicht gel\u00f6scht werden. "+e;
				getProtocolFacade().writeExportImportLogEntry(iProtocolId, XmlExportImportHelper.EXPIMP_MESSAGE_LEVEL_ERROR,
						XmlExportImportHelper.EXPIMP_ACTION_DELETE, 
						MessageFormat.format(locale.getResourceById(locale.getUserLocale(), "xmlimport.error.masterdata.entity.12"), sEntity, iId, e), iActionNumber++);
				throw new NuclosFatalException(sMessage);
			}
		}
	}

	/**
	 * removes existing references to the given entity which will be deleted in the next step
	 *
	 * @param sEntity
	 * @param mdvo
	 */
	// @TODO GOREF
	private void removeReferenceToEntity(final String sEntity, final MasterDataVO mdvo) {
		DbQueryBuilder builder = DataBaseHelper.getDbAccess().getQueryBuilder();
		DbQuery<DbTuple> query = builder.createTupleQuery();
		DbFrom t = query.from("T_AD_MASTERDATA_FIELD").alias(ProcessorFactorySingleton.BASE_ALIAS);
		query.multiselect(
			t.baseColumn("INTID_T_AD_MASTERDATA", Integer.class),
			t.baseColumn("STRFIELD", String.class),
			t.baseColumn("STRDBFIELD", String.class));
		query.where(builder.equal(t.baseColumn("STRFOREIGNENTITY", String.class), sEntity));
		
		try {
			for (DbTuple tuple : DataBaseHelper.getDbAccess().executeQuery(query)) {
				final Integer iIntidOfForeignEntity = tuple.get(0, Integer.class);
				final String sField = tuple.get(1, String.class);
				String sDBField = tuple.get(2, String.class);
				sDBField = sDBField.toUpperCase().replaceFirst("STRVALUE_", "INTID_");
				final MasterDataMetaVO mdmvo = MasterDataMetaCache.getInstance().getMasterDataMetaById(iIntidOfForeignEntity);
	
				DbQuery<Integer> query2 = builder.createQuery(Integer.class);
				DbFrom t2 = query.from(mdmvo.getDBEntity()).alias("t2");
				query2.select(t2.baseColumn("INTID", Integer.class));
				query2.where(builder.equal(t2.baseColumn(sDBField, Integer.class), mdvo.getIntId()));
	
				try {
					for (Integer iIntid : DataBaseHelper.getDbAccess().executeQuery(query2)) {
						try {
							// if field is not nullable, then it's not possible to remove the reference
							if (!mdmvo.getField(sField).isNullable()) {
								String sMessage = StringUtils.getParameterizedExceptionMessage("xmlimport.error.masterdata.entity.13", sEntity, mdvo.getIntId(), sField, mdmvo.getEntityName());  
				//									"Die Refernz auf die Entit\u00e4t ["+sEntity+"] mit der ID ["+mdvo.getIntId()+"]" +
				//								"des Feldes ["+sField+"] der Entit\u00e4t ["+mdmvo.getEntityName()+"] " +
				//								"kann nicht entfernt werden, da das Feld nicht leer sein darf.";
								getProtocolFacade().writeExportImportLogEntry(iProtocolId, XmlExportImportHelper.EXPIMP_MESSAGE_LEVEL_ERROR,
										XmlExportImportHelper.EXPIMP_ACTION_DELETE, 
										MessageFormat.format(locale.getResourceById(locale.getUserLocale(), "xmlimport.error.masterdata.entity.13"), sEntity, mdvo.getIntId(), sField, mdmvo.getEntityName()), iActionNumber++);
								throw new NuclosFatalException(sMessage);
							}
				
							MasterDataVO mdvo_ref = null;
							try {
								mdvo_ref = getMasterDataFacade().get(mdmvo.getEntityName(), iIntid);
								mdvo_ref.setField(sField+"Id", null);
								getMasterDataFacade().modify(mdmvo.getEntityName(), mdvo_ref, null);
							}
							catch (Exception e) {
								String sMessage = StringUtils.getParameterizedExceptionMessage("xmlimport.error.masterdata.entity.14", sField, mdmvo.getEntityName(), mdvo_ref.getIntId(), e);   
				//									"Beim Versuch den Refernzeintrag des Feldes ["+sField+"] der Stammdaten-Entit\u00e4t ["+mdmvo.getEntityName()+"] " +
				//								"mit der ID ["+mdvo_ref.getIntId()+"] zur\u00fcckzusetzen, trat ein Fehler auf. "+e;
								getProtocolFacade().writeExportImportLogEntry(iProtocolId, XmlExportImportHelper.EXPIMP_MESSAGE_LEVEL_ERROR,
										XmlExportImportHelper.EXPIMP_ACTION_DELETE, 
										MessageFormat.format(locale.getResourceById(locale.getUserLocale(), "xmlimport.error.masterdata.entity.14"), sField, mdmvo.getEntityName(), mdvo_ref.getIntId(), e), iActionNumber++);
								throw new NuclosFatalException(sMessage);
							}
						}
						catch (Exception e) {
							throw new NuclosFatalException(e);
						}
					}
				}
				catch (DbException e) {
					String sMessage = StringUtils.getParameterizedExceptionMessage("xmlimport.error.masterdata.entity.15", sField, mdmvo.getEntityName(), iIntidOfForeignEntity, e);    
	//							"Beim Versuch die Refernzeintr\u00e4ge des Feldes ["+sField+"] der Stammdaten-Entit\u00e4t ["+mdmvo.getEntityName()+"] " +
	//						"mit der ID ["+iIntidOfForeignEntity+"] zur\u00fcckzusetzen, trat ein Fehler auf. "+e;
	
					try {
						getProtocolFacade().writeExportImportLogEntry(iProtocolId, XmlExportImportHelper.EXPIMP_MESSAGE_LEVEL_ERROR,
								XmlExportImportHelper.EXPIMP_ACTION_DELETE, 
								MessageFormat.format(locale.getResourceById(locale.getUserLocale(), "xmlimport.error.masterdata.entity.15"), sField, mdmvo.getEntityName(), iIntidOfForeignEntity, e), iActionNumber++);
					}
					catch (Exception e1) {
						throw new NuclosFatalException(e);
					}
					throw new NuclosFatalException(sMessage);
				}
			}
		}
		catch (DbException e) {
			String sMessage = StringUtils.getParameterizedExceptionMessage("xmlimport.error.masterdata.entity.16", sEntity, mdvo.getIntId(), e);     
//							"Beim Versuch die Refernzeintr\u00e4ge der Stammdaten-Entit\u00e4t ["+sEntity+"] " +
//						"mit der ID ["+mdvo.getIntId()+"] zur\u00fcckzusetzen, trat ein Fehler auf. "+e;

			try {
				getProtocolFacade().writeExportImportLogEntry(iProtocolId, XmlExportImportHelper.EXPIMP_MESSAGE_LEVEL_ERROR,
						XmlExportImportHelper.EXPIMP_ACTION_DELETE, 
						MessageFormat.format(locale.getResourceById(locale.getUserLocale(), "xmlimport.error.masterdata.entity.16"), sEntity, mdvo.getIntId(), e), iActionNumber++);
			}
			catch (Exception e1) {
				throw new NuclosFatalException(e1);
			}
			throw new NuclosFatalException(sMessage);
		}
	}
}
