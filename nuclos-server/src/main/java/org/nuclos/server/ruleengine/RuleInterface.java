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
package org.nuclos.server.ruleengine;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.mail.Address;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperPrint;

import org.apache.commons.lang.NullArgumentException;
import org.nuclos.common.NuclosAttributeNotFoundException;
import org.nuclos.common.NuclosBusinessException;
import org.nuclos.common.NuclosEOField;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.NuclosFile;
import org.nuclos.common.ParameterProvider;
import org.nuclos.common.Priority;
import org.nuclos.common.PropertiesMap;
import org.nuclos.common.RuleNotification;
import org.nuclos.common.SearchConditionUtils;
import org.nuclos.common.attribute.DynamicAttributeVO;
import org.nuclos.common.collect.collectable.searchcondition.CollectableComparison;
import org.nuclos.common.collect.collectable.searchcondition.CollectableSearchCondition;
import org.nuclos.common.collect.collectable.searchcondition.ComparisonOperator;
import org.nuclos.common.collection.Pair;
import org.nuclos.common.dal.vo.EntityObjectVO;
import org.nuclos.common.fileimport.NuclosFileImport;
import org.nuclos.common.fileimport.NuclosFileImportResult;
import org.nuclos.common.fileimport.NuclosFileImportStructureUsage;
import org.nuclos.common.mail.NuclosMail;
import org.nuclos.common2.CommonLocaleDelegate;
import org.nuclos.common2.DateUtils;
import org.nuclos.common2.LangUtils;
import org.nuclos.common2.LocaleInfo;
import org.nuclos.common2.ServiceLocator;
import org.nuclos.common2.StringUtils;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.exception.CommonCreateException;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.common2.exception.CommonPermissionException;
import org.nuclos.common2.exception.CommonRemoveException;
import org.nuclos.common2.exception.CommonValidationException;
import org.nuclos.common2.fileimport.NuclosFileImportException;
import org.nuclos.server.attribute.valueobject.AttributeCVO;
import org.nuclos.server.attribute.valueobject.AttributeValueVO;
import org.nuclos.server.common.AttributeCache;
import org.nuclos.server.common.MasterDataMetaCache;
import org.nuclos.server.common.MetaDataServerProvider;
import org.nuclos.server.common.ModuleConstants;
import org.nuclos.server.common.ServerParameterProvider;
import org.nuclos.server.common.calendar.CommonDate;
import org.nuclos.server.common.ejb3.LocaleFacadeLocal;
import org.nuclos.server.common.ejb3.SecurityFacadeLocal;
import org.nuclos.server.common.ejb3.TaskFacadeLocal;
import org.nuclos.server.common.ejb3.TimelimitTaskFacadeLocal;
import org.nuclos.server.common.mail.NuclosMailSender;
import org.nuclos.server.common.valueobject.TaskObjectVO;
import org.nuclos.server.common.valueobject.TaskVO;
import org.nuclos.server.common.valueobject.TimelimitTaskVO;
import org.nuclos.server.customcode.CustomCodeInterface;
import org.nuclos.server.dal.provider.NucletDalProvider;
import org.nuclos.server.database.DataBaseHelper;
import org.nuclos.server.fileimport.ImportContext;
import org.nuclos.server.fileimport.ejb3.ImportFacadeLocal;
import org.nuclos.server.genericobject.Modules;
import org.nuclos.server.genericobject.ejb3.GeneratorFacadeLocal;
import org.nuclos.server.genericobject.ejb3.GenericObjectFacadeLocal;
import org.nuclos.server.genericobject.searchcondition.CollectableSearchExpression;
import org.nuclos.server.genericobject.valueobject.GenericObjectDocumentFile;
import org.nuclos.server.genericobject.valueobject.GenericObjectVO;
import org.nuclos.server.genericobject.valueobject.GenericObjectWithDependantsVO;
import org.nuclos.server.masterdata.ejb3.MasterDataFacadeLocal;
import org.nuclos.server.masterdata.valueobject.DependantMasterDataMap;
import org.nuclos.server.masterdata.valueobject.MasterDataMetaVO;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;
import org.nuclos.server.masterdata.valueobject.MasterDataWithDependantsVO;
import org.nuclos.server.navigation.treenode.GenericObjectTreeNode;
import org.nuclos.server.navigation.treenode.GenericObjectTreeNode.RelationDirection;
import org.nuclos.server.report.ejb3.ReportFacadeRemote;
import org.nuclos.server.report.valueobject.ReportOutputVO;
import org.nuclos.server.ruleengine.valueobject.RuleObjectContainerCVO;
import org.nuclos.server.ruleengine.valueobject.RuleObjectContainerCVO.Event;
import org.nuclos.server.ruleengine.valueobject.RuleVO;
import org.nuclos.server.security.NuclosLocalServerSession;
import org.nuclos.server.statemodel.ejb3.StateFacadeLocal;

/**
 * Interface for rule developers. Delegates all calls to RuleInterfaceBean.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:ramin.goettlich@novabit.de">ramin.goettlich</a>
 * @version 00.01.000#
 */
public class RuleInterface extends CustomCodeInterface {

	private final List<RuleNotification> lstNotification = new ArrayList<RuleNotification>();

	/**
	 * the current object for event save and state changes - target object in rule generation
	 */
	private RuleObjectContainerCVO roccvo;

	/**
	 * the source object for rule generation, null in any other case
	 */
	private RuleObjectContainerCVO roccvoSource;

	/**
	 * the parameter object (object generation), or null
	 */
	private RuleObjectContainerCVO roccvoParameter;

	/**
	 * In this list additional actions can be marked down, which are to be executed later on (mainly in the object generation)
	 */
	private List<String> lstActions;

	/**
	 * a list of failure messages from all failed checks
	 */
	private final List<Object> lstCheckFailed = new ArrayList<Object>();

	/**
	 * the rule accessing this interface - used by logging (RuleAttributeChange)
	 */
	private RuleVO rulevo;

	private Object userObject;

	// Dynamical properties for multiple information, e.g. from object generation
	private PropertiesMap mpProperties = new PropertiesMap();

	private Integer iSessionId;

	// message levels
	public static final int ERROR_MESSAGES = -1;
	public static final int WARNING_MESSAGES = 0;
	public static final int INFO_MESSAGES = 1;

	/**
	 * Create a <code>RuleInterface</code> with the <code>RuleObjectContainerCVO</code> for which the rule is fired
	 * @param ruleVO
	 * @param roccvoCurrent RuleObjectContainerCVO (RuleObjectContainerCVO)roccvo in the Rule
	 * @param roccvoTargetObject if not null this one is the current object
	 */
	public RuleInterface(RuleVO ruleVO, RuleObjectContainerCVO roccvoCurrent, RuleObjectContainerCVO roccvoTargetObject) {
		this(ruleVO, roccvoCurrent, roccvoTargetObject, null, null);
	}

	/**
	 * Create a <code>RuleInterface</code> with the
	 * <code>GenericObjectContainerCVO</code> for which the rule is fired
	 *
	 * @param ruleVO
	 * @param roccvoCurrent GenericObjectContainerCVO (GenericObjectContainerCVO)roccvo in the Rule
	 * @param roccvoTargetObject if not null this one is the current object
	 */
	public RuleInterface(RuleVO ruleVO, RuleObjectContainerCVO roccvoCurrent, RuleObjectContainerCVO roccvoTargetObject, RuleObjectContainerCVO roccvoParameterObject, List<String> lstActions) {
		if (roccvoTargetObject != null) {
			this.roccvo = roccvoTargetObject;
			this.roccvoSource = roccvoCurrent;
		}
		else {
			this.roccvo = roccvoCurrent;
		}
		this.roccvoParameter = roccvoParameterObject;
		this.lstActions = lstActions;
		this.rulevo = ruleVO;
	}

	public void setUserObject(Object obj) {
		this.userObject = obj;
	}

	public Object getUserObject() {
		return this.userObject;
	}

	public void setProperty(String sKey, Serializable oValue) {
		mpProperties.put(sKey, oValue);
	}

	public void setProperties(Map<String, ? extends Serializable> mpProperties) {
		if(mpProperties != null) {
			this.mpProperties.putAll(mpProperties);
		}
	}

	public Serializable getProperty(String sKey) {
		return mpProperties.get(sKey);
	}

	/**
	 * Helper class used in the check and evaluateCheckResult function.
	 * <br>
	 * <br>Created by Novabit Informationssysteme GmbH
	 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
	 *
	 * @author	<a href="mailto:Lars.Rueckemann@novabit.de">Lars Rueckemann</a>
	 * @version 01.00.00
	 */
	private static class Check {
		private final DynamicAttributeVO attrvo;
		private final int iCheckType;
		private final DynamicAttributeVO attrvoCompare;

		Check(DynamicAttributeVO attrvo, int iCheckType, DynamicAttributeVO attrvoCompare) {
			this.attrvo = attrvo;
			this.iCheckType = iCheckType;
			this.attrvoCompare = attrvoCompare;
		}

		Check(DynamicAttributeVO attrvo, int iCheckType) {
			/** @todo P2 possible NPE for attrvoCompare! */
			this(attrvo, iCheckType, null);
		}

		@Override
		public String toString() {
			final AttributeCache attrcache = AttributeCache.getInstance();
			final String sAttributeLabel = CommonLocaleDelegate.getLabelFromAttributeCVO(attrcache.getAttribute(attrvo.getAttributeId()));

			final LocaleInfo userLocale = getLocaleFacade().getUserLocale();

			switch (iCheckType) {
				case RuleConstants.NULL:
					return MessageFormat.format(getLocaleFacade().getResourceById(userLocale, "ruleinterface.check.attribute.error.1"), sAttributeLabel);
					//"\"" + sAttributeLabel + "\" darf keinen Wert enthalten.";
				case RuleConstants.NOT_NULL:
					return MessageFormat.format(getLocaleFacade().getResourceById(userLocale, "ruleinterface.check.attribute.error.2"), sAttributeLabel, getComparedAttributeLabel(attrcache),getComparedAttributeValue(attrcache));
					//"\"" + sAttributeLabel + "\" darf nicht leer sein.";
				case RuleConstants.EQUAL:
					return MessageFormat.format(getLocaleFacade().getResourceById(userLocale, "ruleinterface.check.attribute.error.3"), sAttributeLabel, getComparedAttributeLabel(attrcache),getComparedAttributeValue(attrcache));
					//"\"" + sAttributeLabel + "\" muss den gleichen Wert enthalten wie \"" + getComparedAttributeLabel(attrcache) + "\" (" + getComparedAttributeValue(attrcache) + ").";
				case RuleConstants.NOT_EQUAL:
					return MessageFormat.format(getLocaleFacade().getResourceById(userLocale, "ruleinterface.check.attribute.error.4"), sAttributeLabel, getComparedAttributeLabel(attrcache),getComparedAttributeValue(attrcache));
					//"\"" + sAttributeLabel + "\" darf nicht den gleichen Wert enthalten wie \"" + getComparedAttributeLabel(attrcache) + "\" (" + getComparedAttributeValue(attrcache) + ").";
				case RuleConstants.GREATER:
					return MessageFormat.format(getLocaleFacade().getResourceById(userLocale, "ruleinterface.check.attribute.error.5"), sAttributeLabel, getComparedAttributeLabel(attrcache),getComparedAttributeValue(attrcache));
					//"\"" + sAttributeLabel + "\" muss einen gr\u00f6\u00dferen Wert enthalten als \"" + getComparedAttributeLabel(attrcache) + "\" (" + getComparedAttributeValue(attrcache) + ").";
				case RuleConstants.LESS:
					return MessageFormat.format(getLocaleFacade().getResourceById(userLocale, "ruleinterface.check.attribute.error.6"), sAttributeLabel, getComparedAttributeLabel(attrcache),getComparedAttributeValue(attrcache));
					//"\"" + sAttributeLabel + "\" muss einen kleineren Wert enthalten als  \"" + getComparedAttributeLabel(attrcache) + "\" (" + getComparedAttributeValue(attrcache) + ").";
				case RuleConstants.GREATER_OR_EQUAL:
					return MessageFormat.format(getLocaleFacade().getResourceById(userLocale, "ruleinterface.check.attribute.error.7"), sAttributeLabel, getComparedAttributeLabel(attrcache),getComparedAttributeValue(attrcache));
					//"\"" + sAttributeLabel + "\" muss einen Wert gr\u00f6\u00dfer oder gleich \"" + getComparedAttributeLabel(attrcache) + "\" (" + getComparedAttributeValue(attrcache) + ") enthalten.";
				case RuleConstants.LESS_OR_EQUAL:
					return MessageFormat.format(getLocaleFacade().getResourceById(userLocale, "ruleinterface.check.attribute.error.8"), sAttributeLabel, getComparedAttributeLabel(attrcache),getComparedAttributeValue(attrcache));
					//"\"" + sAttributeLabel + "\" muss einen Wert kleiner oder gleich \"" + getComparedAttributeLabel(attrcache) + "\" (" + getComparedAttributeValue(attrcache) + ") enthalten.";
				default :
					return sAttributeLabel;
			}
		}

		private String getComparedAttributeLabel(AttributeCache attrcache) {
			return CommonLocaleDelegate.getLabelFromAttributeCVO(attrcache.getAttribute(attrvoCompare.getAttributeId()));
		}

		private String getComparedAttributeValue(AttributeCache attrcache) {
			return this.attrvoCompare.getCanonicalValue(attrcache);
		}

		private LocaleFacadeLocal getLocaleFacade() {
			return ServiceLocator.getInstance().getFacade(LocaleFacadeLocal.class);
		}

	}	// inner class Check

	/**
	 * may be called by rules to get the current object.
	 * @return the current object.
	 * @throws NuclosFatalRuleException if there is no current object.
	 */
	public RuleObjectContainerCVO getRuleObjectContainerCVO() {
		if (this.roccvo == null) {
			throw new NuclosFatalRuleException("rule.interface.error.1");
				//"Der Zugriff auf ein aktuelles Objekt ist in dieser Regel nicht m\u00f6glich - es gibt kein aktuelles Objekt.");
		}
		return this.roccvo;
	}

	/**
	 * may be called by rules to get the source object for object generation rules
	 * @return the target object of rule generation
	 * @throws NuclosFatalRuleException if there is no target object.
	 */
	public RuleObjectContainerCVO getSourceObjectContainerCVO() {
		if (this.roccvoSource == null) {
			throw new NuclosFatalRuleException("rule.interface.error.2");
				//"Der Zugriff auf das Zielobjekt einer Objektgenerierung ist in dieser Regel nicht m\u00f6glich - es gibt kein Zielobjekt.");
		}
		return this.roccvoSource;
	}

	/**
	 * may be called by rules to get the source object for object generation rules
	 * @return the target object of rule generation
	 * @throws NuclosFatalRuleException if there is no target object.
	 */
	public RuleObjectContainerCVO getParameterObjectContainerCVO() {
		if (this.roccvoParameter == null) {
			throw new NuclosFatalRuleException("nuclos.ruleinterface.error.noparameterobject");
		}
		return this.roccvoParameter;
	}

	/**
	 * may be called by the BeanShellRunner only. Must be public but is not part of the "official" rule interface.
	 * @return the current object, if any.
	 */
	public RuleObjectContainerCVO getRuleObjectContainerCVOIfAny() {
		return this.roccvo;
	}

	/**
	 * set dependants for generic object
	 * @param iModuleId
	 * @param iGenericObjectId
	 * @param sEntity
	 * @param collmdvoDependants
	 */
	@SuppressWarnings("deprecation")
	private void setDependants(Integer iModuleId, Integer iGenericObjectId, String sEntity, Collection<MasterDataVO> collmdvoDependants) {
		final RuleObjectContainerCVO roccvo;
		try {
			roccvo = getGenericObjectFacade().getRuleObjectContainerCVO(Event.UNDEFINED, iGenericObjectId);
		}
		catch (CommonPermissionException ex) {
			throw new NuclosFatalRuleException(ex.getMessage(), ex);
		}
		catch (CommonFinderException ex) {
			throw new NuclosFatalRuleException(ex.getMessage(), ex);
		}

		roccvo.setDependants(sEntity, collmdvoDependants);
		try {
			getGenericObjectFacade().modify(iModuleId, new GenericObjectWithDependantsVO(roccvo));
		}
		catch (CommonBusinessException ex) {
			throw new NuclosFatalRuleException(ex.getMessage(), ex);
		}
	}

	/**
	 * may be called by rules to overwrite the set of specified subentity entries.
	 * @param sEntityName
	 * @param iObjectId
	 * @param sDependantEntityName
	 * @param collmdvoDependants
	 * @throws NuclosFatalRuleException
	 */
	public void setDependants(String sEntityName, Integer iObjectId, String sDependantEntityName, Collection<MasterDataVO> collmdvoDependants) {
		if (Modules.getInstance().isModuleEntity(sEntityName)) {
			this.setDependants(Modules.getInstance().getModuleIdByEntityName(sEntityName), iObjectId, sDependantEntityName, collmdvoDependants);
		}
		else {
			final RuleObjectContainerCVO roccvo;
			try {
				roccvo = getMasterDataFacade().getRuleObjectContainerCVO(Event.UNDEFINED, sEntityName, iObjectId);
			}
			catch (CommonPermissionException ex) {
				throw new NuclosFatalRuleException(ex.getMessage(), ex);
			}
			catch (CommonFinderException ex) {
				throw new NuclosFatalRuleException(ex.getMessage(), ex);
			}
			catch (NuclosBusinessException ex) {
				throw new NuclosFatalRuleException(ex.getMessage(), ex);
			}

			roccvo.setDependants(sDependantEntityName, collmdvoDependants);

			try {
				getMasterDataFacade().modify(sEntityName, roccvo.getMasterData(), roccvo.getDependants());
			}
			catch (CommonBusinessException ex) {
				throw new NuclosFatalRuleException(ex.getMessage(), ex);
			}
		}
	}

//	private void setForeignKeyField(String sEntityName, String sDependantEntityName, MasterDataVO mdvo, Object oId) {
//		MasterDataMetaVO metavo = MasterDataMetaCache.getInstance().getMetaData(sDependantEntityName);
//		for (String sFieldName : metavo.getFieldNames()) {
//			if (metavo.getField(sFieldName).getForeignEntity() != null && metavo.getField(sFieldName).getForeignEntity().equals(sEntityName)) {
//				mdvo.setField(sFieldName+"Id", oId);
//			}
//		}
//	}

	private GenericObjectFacadeLocal getGenericObjectFacade() {
		return ServiceLocator.getInstance().getFacade(GenericObjectFacadeLocal.class);
	}

	private MasterDataFacadeLocal getMasterDataFacade() {
		return ServiceLocator.getInstance().getFacade(MasterDataFacadeLocal.class);
	}

	private LocaleFacadeLocal getLocaleFacade() {
		return ServiceLocator.getInstance().getFacade(LocaleFacadeLocal.class);
	}

	/**
	 * @return the <code>GenericObjectVO</code> contained in roccvo.
	 */
	@Override
	public GenericObjectVO getGenericObject() {
		if (this.roccvo != null) {
			return this.getRuleObjectContainerCVO().getGenericObject();
		}
		return null;
	}

	/**
	 * @return the <code>MasterDataVO</code> contained in roccvo.
	 */
	@Override
	public MasterDataVO getMasterData() {
		if (this.roccvo != null) {
			return this.getRuleObjectContainerCVO().getMasterData();
		}
		return null;
	}

	/** get the GenericObjectVO with the given id
	 * @return <code>GenericObjectVO</code>
	 */
	public GenericObjectVO getGenericObject(Integer iGenericObjectId) throws CommonFinderException, CommonPermissionException {
		return this.getRuleInterface().getGenericObject(iGenericObjectId);
	}

	/**
	 * gets the dependant masterdata records belonging to the given entity, for the current generic object.
	 *
	 * Deleted records are flagged with <code>isRemoved()</code>.
	 *
	 * Changes to these masterdata records are not stored.
	 * The masterdata is merged, so that the result contains the data as if it was saved yet.
	 * This is the same as <code>this.getDependants(sEntityName, "genericObject")</code>.
	 * @param sEntityName name of the dependant entity
	 * @return Collection<MasterDataVO>
	 */
	public Collection<MasterDataVO> getDependantsWithDeleted(String sEntityName) {
		try {
			return this.getRuleObjectContainerCVO().getDependantsWithDeleted(sEntityName);
		}
		catch (CommonFatalException ex) {
			throw new NuclosFatalRuleException(ex);
		}
	}

	/**
	 * gets the dependant masterdata records belonging to the given entity, for the current generic object.
	 * Changes to these masterdata records are not stored.
	 * The masterdata is merged, so that the result contains the data as if it was saved yet.
	 * This is the same as <code>this.getDependants(sEntityName, "genericObject")</code>.
	 * @param sEntityName name of the dependant entity
	 * @return Collection<MasterDataVO>
	 */
	public Collection<MasterDataVO> getDependants(String sEntityName) {
		if (this.getGenericObject() != null) {
			return this.getDependants(sEntityName, ModuleConstants.DEFAULT_FOREIGNKEYFIELDNAME);
		}
		else {
			throw new NuclosFatalRuleException("rule.interface.error.3");//"Bitte geben Sie das Fremdschl\u00fcssel an.");
		}

	}

	/**
	 * gets the dependant masterdata records belonging to the given entity, for the current generic object,
	 * using the given foreign key field to the current generic object.
	 * Changes to these masterdata records are not stored.
	 * The masterdata is merged, so that the result contains the data as if it was saved yet.
	 * @param sEntityName name of the dependant entity
	 * @param sForeignKeyFieldName name of the foreign key field to the entity of the current generic object.
	 * @return Collection<MasterDataVO>
	 */
	public Collection<MasterDataVO> getDependants(String sEntityName, String sForeignKeyFieldName) {
		try {
//			if (this.getGenericObject() != null) {
			    return this.getRuleObjectContainerCVO().getDependants(sEntityName, sForeignKeyFieldName);
//			}
//			else {
//				return getDependants(this.getMasterData().getIntId(), sEntityName, sForeignKeyFieldName);
//			}

		}
		catch (CommonFatalException ex) {
			throw new NuclosFatalRuleException(ex);
		}
	}

	/**
	 * set the generic objects dependants of the given entity.
	 * @param sEntityName name of the dependant entity
	 * @param coll Collection<MasterDataVO>
	 */
	public void setDependants(String sEntityName, Collection<MasterDataVO> coll) {
		try {
			this.getRuleObjectContainerCVO().setDependants(sEntityName, coll);
		}
		catch (CommonFatalException ex) {
			throw new NuclosFatalRuleException(ex);
		}
	}

	/**
	 * Create and return a new MasterDataVO for a certain entity.
	 * @param sEntity the entity name.
	 * @return a fresh and shiny new MasterDataVO.
	 */
	public MasterDataVO getNewMasterDataCVO(String sEntity) {
		final MasterDataMetaVO mdmetavo = MasterDataMetaCache.getInstance().getMetaData(sEntity);

		return new MasterDataVO(mdmetavo, true);
	}

	/**
	 * Compare the value of an attribute from a generic object stored in the database with the given value.
	 * Use <code>isAttributeEqual(String sAttributeName, String oValue) </code>
	 * if you want to compare an attribute of the current generic object.
	 * @param iGenericObjectId generic object id
	 * @param sAttributeName name of attribute to compare
	 * @param oValue attribute value to compare to
	 * @return
	 * @precondition iGenericObjectId != null
	 * @precondition sAttributeName != null
	 * @deprecated
	 */
	@Deprecated
	public boolean isAttributeValueEqual(Integer iGenericObjectId, String sAttributeName, Object oValue) {
		return this.isFieldValueEqual(Modules.getInstance().getEntityNameByModuleId(this.getModuleId(iGenericObjectId)),
				iGenericObjectId, sAttributeName, oValue);
	}

	/**
	 * Compare the value of an attribute from a generic object stored in the database with the given value.
	 * Use <code>isAttributeEqual(String sAttributeName, String oValue) </code>
	 * if you want to compare an attribute of the current generic object.
	 * @param iGenericObjectId generic object id
	 * @param sAttributeName name of attribute to compare
	 * @param oValue attribute value to compare to
	 * @return
	 * @precondition iGenericObjectId != null
	 * @precondition sAttributeName != null
	 */
	public boolean isFieldValueEqual(String sEntityName, Integer iObjectId, String sFieldName, Object oValue) {
		/** @todo refactor */
		if (iObjectId == null) {
			throw new NullArgumentException("iObjectId");
		}
		if (sFieldName == null) {
			throw new NullArgumentException("sFieldName");
		}
		if (Modules.getInstance().isModuleEntity(sEntityName)) {
			return getRuleInterface().isAttributeEqual(iObjectId, sFieldName, oValue);
		}
		else {
			return this.getMasterData(sEntityName, iObjectId).getField(sFieldName).equals(oValue);
		}

	}

	/**
	 * Compare the value of an attribute from the current generic object with the given value.
	 * Use <code>isAttributeEqual(Integer iGenericObjectId, String sAttributeName, String oValue) </code>
	 * if you want to compare an attribute from any other generic object.
	 * @param sAttributeName name of attribute to compare
	 * @param oValue attribute value to compare to
	 * @return
	 * @precondition sAttributeName != null
	 * @deprecated
	 */
	@Deprecated
	public boolean isAttributeValueEqual(String sAttributeName, Object oValue) {
		return this.isFieldValueEqual(sAttributeName, oValue);
	}

	/**
	 * Compare the value of a field from the current object with the given value.
	 * Use <code>isFieldEqual(Integer iObjectId, String sFieldName, String oValue) </code>
	 * if you want to compare a field from any other object.
	 * @param sFieldName name of field to compare
	 * @param oValue field value to compare to
	 * @return
	 * @precondition sFieldeName != null
	 */
	public boolean isFieldValueEqual(String sFieldName, Object oValue) {
		/** @todo refactor */
		if (sFieldName == null) {
			throw new NullArgumentException("sFieldName");
		}
		if (this.getGenericObject() != null) {
			final DynamicAttributeVO attrvo = this.getGenericObject().getAttribute(sFieldName, AttributeCache.getInstance());
			/** @todo what if attrvo.isRemoved()? */
			return LangUtils.equals(getValueOrNull(attrvo), oValue);
		}
		else {
			return LangUtils.equals(this.getMasterData().getField(sFieldName), oValue);
		}

	}

	/**
	 * check if an attribute value of generic object with intid <code>iGenericObjectId</code> is null.
	 * Do not use this function if you want to compare an attribute of the current generic object!
	 * @param iGenericObjectId generic object id
	 * @param sAttributeName name of attribute to compare
	 * @return
	 * @precondition iGenericObjectId != null
	 * @precondition sAttributeName != null
	 * @deprecated
	 */
	@Deprecated
	public boolean isAttributeValueNull(Integer iGenericObjectId, String sAttributeName) {
		String sEntityName = Modules.getInstance().getEntityNameByModuleId(this.getModuleId(iGenericObjectId));
		return this.isFieldValueNull(sEntityName, iGenericObjectId, sAttributeName);
	}

	/**
	 * check if a field value of object with intid <code>iObjectId</code> is null.
	 * @param iObjectId object (generic or masterdata) id
	 * @param sFieldName name of field to compare
	 * @return
	 * @precondition iObjectId != null
	 * @precondition sFieldName != null
	 */
	public boolean isFieldValueNull (String sEntityName, Integer iObjectId, String sFieldName) {
		if (iObjectId == null) {
			throw new NullArgumentException("iObjectId");
		}
		if (sFieldName == null) {
			throw new NullArgumentException("sFieldName");
		}
		if (Modules.getInstance().isModuleEntity(sEntityName)) {
			return getRuleInterface().isAttributeNull(iObjectId, sFieldName);
		}
		else {
			return this.getMasterData(sEntityName, iObjectId).getField(sFieldName) == null;
		}
	}

	/**
	 * @param sAttributeName
	 * @return Is the attribute value with the given name of the current generic object <code>null</code>?
	 * @precondition sAttributeName != null
	 * @deprecated
	 */
	@Deprecated
	public boolean isAttributeValueNull(String sAttributeName) {
		return this.isFieldValueNull(sAttributeName);
	}

	/**
	 * @param sFieldName
	 * @return Is the field value with the given name of the current object <code>null</code>?
	 * @precondition sFieldName != null
	 */
	public boolean isFieldValueNull(String sFieldName) {
		/** @todo refactor */
		if (sFieldName == null) {
			throw new NullArgumentException("sFieldName");
		}
		if (this.getGenericObject() != null) {
			return isAttributeNull(this.getGenericObject().getAttribute(sFieldName, AttributeCache.getInstance()));
		}
		else {
			return (this.getMasterData().getField(sFieldName) == null);
		}

	}

	/**
	 * @param attrvo
	 * @return Is <code>attrvo</code> or it's value <code>null</code> or removed
	 */
	public boolean isAttributeNull(DynamicAttributeVO attrvo) {
		return attrvo == null || attrvo.isRemoved() || attrvo.getValue() == null;
	}

	/**
	 * @param sEntityName
	 * @param mdvo
	 * @param sFieldName
	 * @return is the <code>MasterDataMetaFieldVO</code> or it's value <code>null</code> or removed
	 */
	public boolean isFieldNull(String sEntityName, MasterDataVO mdvo, String sFieldName) {
		return MasterDataMetaCache.getInstance().getMetaData(sEntityName).getField(sFieldName) == null ||
			MasterDataMetaCache.getInstance().getMetaData(sEntityName).getField(sFieldName).isRemoved() || mdvo.getField(sFieldName) == null;
	}

	/**
	 * sets an attribute of generic object with intid <code>iGenericObjectId</code>
	 * @param iGenericObjectId
	 * @param sAttributeName
	 * @param attrvo
	 * @throws NuclosBusinessRuleException
	 * @precondition iGenericObjectId != null
	 * @precondition sAttributeName != null
	 */
	public void setAttribute(Integer iGenericObjectId, String sAttributeName, DynamicAttributeVO attrvo) throws NuclosBusinessRuleException {
		/** @todo refactor */
		if (iGenericObjectId == null) {
			throw new NullArgumentException("iGenericObjectId");
		}
		if (sAttributeName == null) {
			throw new NullArgumentException("sAttributeName");
		}
		this.setAttribute(iGenericObjectId, sAttributeName, getValueIdOrNull(attrvo), getValueOrNull(attrvo));
	}

	/**
	 * @param attrvo
	 * @return the value of attrvo or <code>null</code>, if attrvo itself is <code>null</code>.
	 * @todo generally consider isRemoved() here?
	 * @todo move to DynamicAttributeVO?
	 */
	public static Object getValueOrNull(DynamicAttributeVO attrvo) {
		return (attrvo == null) ? null : attrvo.getValue();
	}

	/**
	 * @param attrvo
	 * @return the value id of attrvo or <code>null</code>, if <code>attrvo</code> itself is <code>null</code>.
	 * @todo generally consider isRemoved() here?
	 * @todo move to DynamicAttributeVO?
	 */
	public static Integer getValueIdOrNull(DynamicAttributeVO attrvo) {
		return (attrvo == null) ? null : attrvo.getValueId();
	}

	/**
	 * set an attribute of the current generic object.
	 * @param sAttributeName
	 * @param attrvo
	 * @precondition sAttributeName != null
	 */
	public void setAttribute(String sAttributeName, DynamicAttributeVO attrvo) {
		/** @todo refactor */
		if (sAttributeName == null) {
			throw new NullArgumentException("sAttributeName");
		}
		this.setAttribute(sAttributeName, getValueIdOrNull(attrvo), getValueOrNull(attrvo));
	}

	/**
	 * sets an attribute of generic object with intid <code>iGenericObjectId</code>
	 * @param iGenericObjectId generic object id
	 * @param sAttributeName attribute to change value for
	 * @param iValueId new value id
	 * @param oValue new value
	 * @precondition iGenericObjectId != null
	 * @precondition sAttributeName != null
	 * @deprecated
	 */
	@Deprecated
	public void setAttribute(Integer iGenericObjectId, String sAttributeName, Integer iValueId, Object oValue) throws NuclosBusinessRuleException {
		this.setField(Modules.getInstance().getEntityNameByModuleId(this.getModuleId(iGenericObjectId)), iGenericObjectId, sAttributeName, iValueId, oValue);
	}

	/**
	 * sets an field of (generic or masterdata) object with intid <code>iObjectId</code>
	 * @param iObjectId  object id
	 * @param sFieldName field to change value for
	 * @param iValueId new value id
	 * @param oValue new value
	 * @precondition iObjectId != null
	 * @precondition sFieldName != null
	 */
	public void setField(String sEntityName, Integer iObjectId, String sFieldName, Integer iValueId, Object oValue) throws NuclosBusinessRuleException{
		if (iObjectId == null) {
			throw new NullArgumentException("iObjectId");
		}
		if (sFieldName == null) {
			throw new NullArgumentException("sFieldName");
		}
		if (iObjectId.equals(getObjectId())) {
			setField(sEntityName, sFieldName, iValueId, oValue);
		}
		else {
			if (Modules.getInstance().isModuleEntity(sEntityName)) {
				try {
					getRuleInterface().setAttribute(this.rulevo, iObjectId, sFieldName, iValueId, oValue);
				}
				catch (NuclosBusinessException ex) {
					throw new NuclosBusinessRuleException(ex);
				}
			}
			else {
				getRuleInterface().setMasterDataField(sEntityName, iObjectId, sFieldName, iValueId, oValue);
			}
		}
	}

	/**
	 * sets an attribute of the current generic object.
	 * @param sAttributeName attribute to change value for
	 * @param iValueId new value id
	 * @param oValue new value
	 * @precondition sAttributeName != null
	 * @deprecated
	 */
	@Deprecated
	public void setAttribute(String sAttributeName, Integer iValueId, Object oValue) {
		this.setField(Modules.getInstance().getEntityNameByModuleId(this.getModuleId()), sAttributeName, iValueId, oValue);
	}

	/**
	 * sets a field of the current object.
	 * @param sFieldName field to change value for
	 * @param iValueId new value id
	 * @param oValue new value
	 * @precondition sFieldName != null
	 */
	public void setField(String sEntityName, String sFieldName, Integer iValueId, Object oValue) {
		if (sFieldName == null) {
			throw new NullArgumentException("sFieldName");
		}
		if (Modules.getInstance().isModuleEntity(sEntityName)) {
			getRuleInterface().setAttribute(this.rulevo, this.getGenericObject(), sFieldName, iValueId, oValue);
		}
		else {
			getRuleInterface().setMasterDataField(sEntityName, this.getMasterData(), sFieldName, iValueId, oValue);
		}
	}

	/**
	 * sets the attribute <code>sAttributeName</code> to the value <code>oValue</code> for the current generic object.
	 * the data type of the attribute and oValue has to be the same
	 * @param sAttributeName attribute to change value for
	 * @param oValue new value
	 * @precondition sAttributeName != null
	 * @deprecated
	 */
	@Deprecated
	public void setAttributeValue(String sAttributeName, Object oValue) {
		if (sAttributeName == null) {
			throw new NullArgumentException("sAttributeName");
		}
		this.setAttribute(sAttributeName, null, oValue);
	}

	/**
	 * sets the field <code>sFieldName</code> to the value <code>oValue</code> for the current object.
	 * the data type of the field and oValue has to be the same
	 * @param sFieldName field to change value for
	 * @param oValue new value
	 * @precondition sFieldName != null
	 */
	public void setFieldValue(String sEntityName, String sFieldName, Object oValue) {
		if (sFieldName == null) {
			throw new NullArgumentException("sFieldName");
		}

		this.setField(sEntityName, sFieldName, null, oValue);
	}

	/**
	 * sets the attribute <code>sAttributeName</code> to the value <code>oValue</code> for the generic object with intid <code>iGenericObjectId</code>.
	 * the data type of the attribute and oValue has to be the same
	 * @param sAttributeName attribute to change value for
	 * @param oValue new value
	 * @precondition sAttributeName != null
	 * @deprecated
	 */
	@Deprecated
	public void setAttributeValue(Integer iGenericObjectId, String sAttributeName, Object oValue) throws NuclosBusinessRuleException {
		if (sAttributeName == null) {
			throw new NullArgumentException("sAttributeName");
		}
		this.setAttribute(iGenericObjectId, sAttributeName, null, oValue);
	}

	/**
	 * sets the field <code>sFieldName</code> to the value <code>oValue</code> for the object with intid <code>iObjectId</code>.
	 * the data type of the field and oValue has to be the same
	 * @param sEntityName
	 * @param sFieldName field to change value for
	 * @param oValue new value
	 * @precondition sFieldName != null
	 */
	public void setFieldValue(String sEntityName, Integer iObjectId, String sFieldName, Object oValue) throws NuclosBusinessRuleException {
		if (sFieldName == null) {
			throw new NullArgumentException("sFieldName");
		}
		this.setField(sEntityName, iObjectId, sFieldName, null, oValue);
	}


	/**
	 * Gets an attribute value from a source object in an object generation.
	 * @param sAttributeName name of the attribute to read.
	 * @return the value of the attribute.
	 */
	public Object getSourceAttributeValue(String sAttributeName) {
		if (this.roccvoSource != null && this.roccvoSource.getMasterData() != null) {
			throw new NuclosFatalException("rule.interface.error.4");//"Die Objektgenerierung bei Stammdaten ist nicht m\u00f6glich.");
		}
		Object result = null;
		if (this.roccvoSource != null) {
			final DynamicAttributeVO attrvo = this.roccvoSource.getGenericObject().getAttribute(sAttributeName, AttributeCache.getInstance());
			/** @todo what if attrvo.isRemoved()? */
			result = getValueOrNull(attrvo);
		}
		return result;
	}

	/**
	 * Iterates the value list of the attribute with the given name, trying to find an entry with the given value.
	 * @param sAttributeName
	 * @param oValue
	 * @return the value id corresponding to the given value, if any.
	 * @precondition sAttributeName != null
	 */
	public Integer getValueIdFromValue(String sAttributeName, Object oValue) {
		if (sAttributeName == null) {
			throw new NullArgumentException("sAttributeName");
		}
		Integer result = null;
		if (oValue != null) {
			final AttributeCVO attrcvo;
			try {
				attrcvo = AttributeCache.getInstance().getAttribute(this.getModuleId(), sAttributeName);
			}
			catch (NuclosAttributeNotFoundException ex) {
				throw new NuclosFatalRuleException(ex.getMessage(), ex);
			}
			final Collection<AttributeValueVO> collValues = attrcvo.getValues();
			if (collValues != null) {
				for (AttributeValueVO attrvaluevo : collValues) {
					if (oValue.equals(attrvaluevo.getValue())) {
						result = attrvaluevo.getId();
						break;
					}
				}
			}
		}
		return result;
	}

	/**
	 * sets the value of an enumerated attribute (that is an attribute containing a value list). Tries to find an entry
	 * in the value list containing the given value. If none is found, null is written as value id.
	 * @param sAttributeName
	 * @param oValue
	 * @throws NuclosBusinessRuleException
	 * @precondition sAttributeName != null
	 */
	public void setEnumeratedAttributeByValue(String sAttributeName, Object oValue) throws NuclosBusinessRuleException {
		if (sAttributeName == null) {
			throw new NullArgumentException("sAttributeName");
		}
		this.setAttribute(sAttributeName, this.getValueIdFromValue(sAttributeName, oValue), oValue);
	}

	/**
	 * Sends a message with specified content and subject to specified recipients
	 * @param asRecipients adresses of recipients of message
	 * @param sSubject subject of content of message
	 * @param sContent content of e-mail to message
	 */
	public void sendMessage(String[] asRecipients, String sSubject, String sContent) throws NuclosBusinessRuleException {
		if (ServerParameterProvider.getInstance().getValue(ParameterProvider.KEY_NOTIFICATION_SENDER).equals("Enabled")) {
			info("Sending notification e-mail");
			getRuleInterface().sendMessage(asRecipients, sSubject, sContent);
		}
	}


	/**
	 * Create a new object based on the current generic object.
	 *
	 * If the object-generation fails, this method will not throw an error, but
	 * return null instead of the new object id
	 *
	 * @param sGeneratorName name of object generator
	 * @return the new object's id or null
	 */
	public Integer createObject(String sGeneratorName) throws NuclosBusinessRuleException {
		if (this.getMasterData() != null) {
			throw new NuclosBusinessRuleException("rule.interface.error.5");//"Objektgenerierung bei den Stammdaten ist unzul\u00e4ssig");
		}
		if (this.getObjectId() != null) {
			return getRuleInterface().createObject(this.getObjectId(), sGeneratorName);
		}
		else {
			return getRuleInterface().createObject(this.getRuleObjectContainerCVO(), sGeneratorName);
		}
	}


	/**
	 * create a new object based on the given RuleObjectContainerCVO
	 *
	 * If the object-generation fails, this method will not throw an error, but
	 * return null instead of the new object id
	 *
	 * @param sGeneratorName name of object generator
	 * @return the new object's id or null
	 */
	public Integer createObject(RuleObjectContainerCVO roccvo, String sGeneratorName) throws NuclosBusinessRuleException {
		if (roccvo.getMasterData() != null) {
			throw new NuclosBusinessRuleException("rule.interface.error.5");//"Objektgenerierung bei den Stammdaten ist unzul\u00e4ssig.");
		}
		return getRuleInterface().createObject(roccvo, sGeneratorName);
	}

	/**
	 * gets collection of all originating generic objects ids in specified module for generic object with intid <code>iGenericObjectId</code>
	 * @param iGenericObjectId generic object id
	 * @param iModuleId id of module to get related objects for
	 * @return Collection<Integer> collection of related generic object ids
	 * @postcondition result != null
	 * @postcondition (iModuleId == null) --> result.isEmpty()
	 * @postcondition (iGenericObjectId == null) --> result.isEmpty()
	 */
	public Collection<Integer> getGenericObjectsOriginating(Integer iGenericObjectId, Integer iModuleId) throws NuclosBusinessRuleException{
		final Collection<Integer> result = this.getRelatedGenericObjectIds(iModuleId, iGenericObjectId,
				RelationDirection.REVERSE, GenericObjectTreeNode.SystemRelationType.PREDECESSOR_OF.getValue());

		assert result != null;
		assert !(iModuleId == null) || result.isEmpty();
		assert !(iGenericObjectId == null) || result.isEmpty();

		return result;
	}

	/**
	 * gets collection of all originating generic object ids in specified module for the current generic object
	 * @param iModuleId id of module to get related objects for
	 * @return collection of related generic object ids
	 * @postcondition result != null
	 * @postcondition (iModuleId == null) --> result.isEmpty()
	 */
	public Collection<Integer> getGenericObjectsOriginating(Integer iModuleId) throws NuclosBusinessRuleException{
		if (this.getMasterData() != null) {
			throw new NuclosBusinessRuleException("rule.interface.error.5");//"Objektgenerierung bei den Stammdaten ist unzul\u00e4ssig.");
		}
		final Collection<Integer> result = this.getGenericObjectsOriginating(this.getGenericObject().getId(), iModuleId);
		assert result != null;
		assert !(iModuleId == null) || result.isEmpty();

		return result;
	}

	/**
	 * gets collection of all resulting generic object ids in specified module for generic object with intid <code>iGenericObjectId</code>
	 * @param iGenericObjectId generic object id
	 * @param iModuleId id of module to get related objects for
	 * @return collection of related generic object ids
	 * @postcondition result != null
	 * @postcondition (iModuleId == null) --> result.isEmpty()
	 * @postcondition (iGenericObjectId == null) --> result.isEmpty()
	 */
	public Collection<Integer> getGenericObjectsResulting(Integer iGenericObjectId, Integer iModuleId) {
		final Collection<Integer> result = this.getRelatedGenericObjectIds(iModuleId, iGenericObjectId, RelationDirection.FORWARD,
			GenericObjectTreeNode.SystemRelationType.PREDECESSOR_OF.getValue());

		assert result != null;
		assert !(iModuleId == null) || result.isEmpty();
		assert !(iGenericObjectId == null) || result.isEmpty();
		return result;
	}

	/**
	 * gets collection of all resulting generic object ids in specified module for the current generic object
	 * @param iModuleId id of module to get related objects for
	 * @return collection of related generic object ids
	 * @postcondition result != null
	 * @postcondition (iModuleId == null) --> result.isEmpty()
	 */
	public Collection<Integer> getGenericObjectsResulting(Integer iModuleId) {
		final Collection<Integer> result = this.getGenericObjectsResulting(this.getGenericObject().getId(), iModuleId);
		assert result != null;
		assert !(iModuleId == null) || result.isEmpty();
		return result;
	}

	/**
	 * get a collection of all contained generic object ids in specified module for generic object with intid <code>iGenericObjectId</code>
	 * @param iGenericObjectId generic object id
	 * @param iModuleId id of module to get related objects for
	 * @return collection of related generic object ids
	 * @postcondition result != null
	 * @postcondition (iModuleId == null) --> result.isEmpty()
	 * @postcondition (iGenericObjectId == null) --> result.isEmpty()
	 */
	public Collection<Integer> getGenericObjectParts(Integer iGenericObjectId, Integer iModuleId) {
		final Collection<Integer> result = this.getRelatedGenericObjectIds(iModuleId, iGenericObjectId, RelationDirection.REVERSE,
			GenericObjectTreeNode.SystemRelationType.PART_OF.getValue());
		assert result != null;
		assert !(iModuleId == null) || result.isEmpty();
		assert !(iGenericObjectId == null) || result.isEmpty();
		return result;
	}

	/**
	 * get a collection of all contained generic object ids in specified module for the current generic object
	 * @param iModuleId id of module to get related objects for
	 * @return collection of related generic object ids
	 * @postcondition result != null
	 * @postcondition (iModuleId == null) --> result.isEmpty()
	 */
	public Collection<Integer> getGenericObjectParts(Integer iModuleId) {
		final Collection<Integer> result = this.getGenericObjectParts(this.getGenericObject().getId(), iModuleId);
		assert result != null;
		assert !(iModuleId == null) || result.isEmpty();
		return result;
	}

	/**
	 * Find all generic objects containing the specified generic object
	 * @return Collection<Integer> collection of generic object ids
	 * @postcondition result != null
	 * @postcondition (iModuleId == null) --> result.isEmpty()
	 * @postcondition (iGenericObjectId == null) --> result.isEmpty()
	 */
	public Collection<Integer> getGenericObjectContaining(Integer iGenericObjectId, Integer iModuleId) {
		final Collection<Integer> result = this.getRelatedGenericObjectIds(iModuleId, iGenericObjectId,
				GenericObjectTreeNode.RelationDirection.FORWARD, GenericObjectTreeNode.SystemRelationType.PART_OF.getValue());
		assert result != null;
		assert !(iModuleId == null) || result.isEmpty();
		assert !(iGenericObjectId == null) || result.isEmpty();
		return result;
	}

	/**
	 * Find all generic objects containing the specified generic object
	 * @return Collection<Integer> collection of generic object ids
	 * @postcondition result != null
	 * @postcondition (iModuleId == null) --> result.isEmpty()
	 * @postcondition (iGenericObjectId == null) --> result.isEmpty()
	 */
	public Collection<Integer> getGenericObjectContaining(Integer iModuleId) {
		final Collection<Integer> result = this.getGenericObjectContaining(this.getGenericObject().getId(), iModuleId);
		assert result != null;
		assert !(iModuleId == null) || result.isEmpty();
		return result;
	}

	/**
	 * @param iModuleId
	 * @param iGenericObjectId
	 * @param bForward true: forward - false: reverse
	 * @param relationType
	 * @return ids of the generic objects of the given module related to the given generic object in the specified way.
	 * @postcondition result != null
	 * @postcondition (iModuleId == null) --> result.isEmpty()
	 * @postcondition (iGenericObjectId == null) --> result.isEmpty()
	 */
	public Collection<Integer> getRelatedGenericObjectIds(Integer iModuleId, Integer iGenericObjectId, boolean bForward, String relationType) {
		return this.getRelatedGenericObjectIds(iModuleId, iGenericObjectId, bForward ? RelationDirection.FORWARD : RelationDirection.REVERSE, relationType);
	}

	/**
	 * @param iModuleId
	 * @param iGenericObjectId
	 * @param direction
	 * @param relationType
	 * @return ids of the generic objects of the given module related to the given generic object in the specified way.
	 * @postcondition result != null
	 * @postcondition (iModuleId == null) --> result.isEmpty()
	 * @postcondition (iGenericObjectId == null) --> result.isEmpty()
	 */
	private Collection<Integer> getRelatedGenericObjectIds(Integer iModuleId, Integer iGenericObjectId, RelationDirection direction, String relationType) {
		return this.getRuleInterface().getRelatedGenericObjectIds(iModuleId, iGenericObjectId, direction, relationType);
	}

	/**
	 * schedules a test job once for ten seconds later
	 */
	public void scheduleTestJob() {
		this.getRuleInterface().scheduleTestJob();
	}

	/**
	 * @return the initial state for the current generic object
	 */
	public Integer getInitialStateNumeral() {
		if (this.getMasterData() != null) {
			throw new NuclosFatalException("code.interface.exception.3");//"Die Stammdaten haben kein Statusmodell");
		}

		final StateFacadeLocal stateFacadeLocal = ServiceLocator.getInstance().getFacade(StateFacadeLocal.class);
		return stateFacadeLocal.getInitialState(this.getGenericObject().getUsageCriteria(AttributeCache.getInstance())).getNumeral();
	}

	/**
	 * @param iGenericObjectId
	 * @return the initial state for the generic object with the given intid.
	 */
	public Integer getInitialStateNumeral(Integer iGenericObjectId) {
		try {
			this.getGenericObject(iGenericObjectId);
			final StateFacadeLocal stateFacadeLocal = ServiceLocator.getInstance().getFacade(StateFacadeLocal.class);
			return stateFacadeLocal.getInitialState(iGenericObjectId).getNumeral();
		}
		catch (CommonFinderException ex) {
			throw new NuclosFatalRuleException("code.interface.exception.3", ex);//"Die Stammdaten haben kein Statusmodell", ex);
		}
		catch (CommonPermissionException ex) {
			throw new NuclosFatalRuleException(ex);
		}
	}

	/**
	 * @param sEntityName the entity of the desired masterdata object
	 * @param iId the id of the desired masterdata object
	 * @return the master data value object of the given entity, with the given id (as primary key)
	 */
	public MasterDataVO getMasterData(String sEntityName, Integer iId) {
		try {
			final MasterDataFacadeLocal mdFacade = ServiceLocator.getInstance().getFacade(MasterDataFacadeLocal.class);
			return mdFacade.get(sEntityName, iId);
		}
		catch (Exception ex) {
			throw new NuclosFatalRuleException(ex);
		}
	}

	/**
	 * @param sEntityName the entity of the desired masterdata object
	 * @param iId the id of the desired masterdata object
	 * @return the {@link MasterDataWithDependantsVO} of the given entity, with the given id (as primary key)
	 * NUCLEUSINT-1160
	 */
	public MasterDataWithDependantsVO getMasterDataWithDependants(String sEntityName, Integer iId){
		try {
			final MasterDataFacadeLocal mdFacade = ServiceLocator.getInstance().getFacade(MasterDataFacadeLocal.class);
			return mdFacade.getWithDependants(sEntityName, iId);
		}
		catch (Exception ex) {
			throw new NuclosFatalRuleException(ex);
		}
	}

	/**
	 * @param sEntityName the entity of the desired masterdata object
	 * @return all intids of the masterdata objects for the given entity
	 */
	public Collection<Object> getMasterDataIds(String sEntityName) {
		try {
			final MasterDataFacadeLocal mdFacade = ServiceLocator.getInstance().getFacade(MasterDataFacadeLocal.class);
			return mdFacade.getMasterDataIds(sEntityName);
		}
		catch (Exception ex) {
			throw new NuclosFatalRuleException(ex);
		}
	}

	/**
	 * get todays date with hour, minute, second and milisecond set to 0.
	 * @return today's date with day precision
	 */
	public Date today() {
		return DateUtils.today();
	}

	/**
	 * get todays date with hour, minute, second and milisecond set to the current time.
	 * @return today's date and time with milisecond precision
	 */
	public Date now() {
		return new Date();
	}

	/**
	 * @return the module id of the current generic object
	 */
	public Integer getModuleId() {
		return this.getGenericObject().getModuleId();
	}

	/**
	 * @return the module id of the generic object with the given id.
	 * @precondition iGenericObjectId != null
	 * @todo add postcondition result != null
	 */
	public Integer getModuleId(Integer iGenericObjectId) {
		return this.getRuleInterface().getModuleId(iGenericObjectId);
	}

	/**
	 * changes the state for the current generic object
	 * @param iNumeral requested subsequent state
	 */
	@SuppressWarnings("deprecation")
	public void changeState(int iNumeral) throws NuclosBusinessRuleException, CommonFinderException {
		if (this.getMasterData() != null) {
			throw new NuclosFatalException("rule.interface.error.6");
				//"Statuswecksel innerhalb der Stammdaten ist unzul\u00e4ssig. Die Stammdaten haben kein Statusmodell.");
		}

		/* sleep for 1 second befor changing the state, because some problems could occure while
		 * analyzing the state history when changing the state within a rule that is executed within a manuel state change
		 */
		//this.sleep(1000);
		final GenericObjectVO govo = getRuleInterface().changeState(this.getGenericObject(), this.getObjectId(), iNumeral);
		this.getRuleObjectContainerCVO().setGenericObject(govo);

		/* reload the dependant data - this is necessary, because the version id
		 * of the dependant data was changed while changing the status
		 */
		DependantMasterDataMap mpDependants = this.getRuleObjectContainerCVO().getDependants();
		getGenericObjectFacade().reloadDependants(new GenericObjectWithDependantsVO(this.getRuleObjectContainerCVO()), mpDependants, false);
		for(String sEntity : mpDependants.getEntityNames()) {
			this.getRuleObjectContainerCVO().setDependants(sEntity, mpDependants.getValues(sEntity));
		}
	}

	/**
	 * changes the state for generic object with intid <code>iGenericObjectId</code>
	 * @param iGenericObjectId generic object value object
	 * @param iNumeral requested subsequent state
	 */
	@SuppressWarnings("deprecation")
	public void changeState(Integer iGenericObjectId, int iNumeral) throws NuclosBusinessRuleException, CommonFinderException {
		try {
			this.getGenericObject(iGenericObjectId);
		}
		catch(Exception ex) {
			throw new NuclosFatalException("rule.interface.error.6");
				//"Statuswecksel innerhalb der Stammdaten ist unzul\u00e4ssig. Die Stammdaten haben kein Statusmodell.");
		}

		/* sleep for 1 second befor changing the state, because some problems could occure while
		 * analyzing the state history when changing the state within a rule that is executed within a manuel state change
		 */
		//this.sleep(1000);
		if (this.getRuleObjectContainerCVOIfAny() == null) {
			this.getRuleInterface().changeState(iGenericObjectId, iNumeral);
		}
		else {
			final GenericObjectVO govo = this.getRuleInterface().changeState(this.getGenericObject(), iGenericObjectId, iNumeral);
			this.getRuleObjectContainerCVO().setGenericObject(govo);
			if (LangUtils.equals(iGenericObjectId, this.getGenericObject().getId())) {
				/*
				 * reload the dependant data - this is necessary, because the version id
				 * of the dependant data was changed while changing the status
				 * (but only if the affected object is the containers object)
				 */
				DependantMasterDataMap mpDependants = this.getRuleObjectContainerCVO().getDependants();
				getGenericObjectFacade().reloadDependants(new GenericObjectWithDependantsVO(this.getRuleObjectContainerCVO()), mpDependants, false);
				for(String sEntity : mpDependants.getEntityNames()) {
					this.getRuleObjectContainerCVO().setDependants(sEntity, mpDependants.getValues(sEntity));
				}
			}
		}
	}

	/**
	 * Causes the currently executing rule to sleep for the specified number of milliseconds.
	 * @param ims milliseconds
	 */
	public void sleep(Integer ims) {
		try {
			Thread.sleep(ims);
		} catch (InterruptedException e) {
			logger.error(e);
		}
	}

	/**
	 * performs a state change for the current generic object at the given point in time. If an old job exists already, it
	 * is always removed.
	 * @param iNewState the new state for the object.
	 * @param dateToSchedule the date for the state change to happen. If <code>null</code>, only a possibly existing job is removed.
	 * If <code>dateToSchedule</code> is in the future, a new job is scheduled for the given date. If <code>dateToSchedule</code> is in the past,
	 * the state change is executed immediately (synchronously).
	 * @precondition this.getGenericObjectId() != null
	 * @throws NuclosBusinessRuleException if the transition from the current state to the new state is not possible for the given object.
	 */
	public void scheduleStateChange(int iNewState, Date dateToSchedule) throws NuclosBusinessRuleException {
		if (this.getMasterData() != null) {
			throw new NuclosFatalException("rule.interface.error.6");
				//"Statuswecksel innerhalb der Stammdaten ist unzul\u00e4ssig. Die Stammdaten haben kein Statusmodell.");
		}
		final Integer iGenericObjectId = this.getObjectId();
		if (iGenericObjectId == null) {
			throw new NuclosFatalRuleException("rule.interface.error.7");
				//"scheduleStateChangeJob(int iNewState, Date dateToSchedule) ben\u00f6tigt ein existierendes aktuelles Objekt. getGenericObjectId() darf nicht null sein.");
		}
		// Note that it is ok to use the contents of the current object in the database instead of the current govo,
		// as it will be synced:
		this.scheduleStateChange(iGenericObjectId, iNewState, dateToSchedule);
	}

	/**
	 * performs a state change for the generic object with the given id at the given point in time. If an old job exists already, it
	 * is always removed.
	 * @param iGenericObjectId
	 * @param iNewState the new state for the object.
	 * @param dateToSchedule the date for the state change to happen. If <code>null</code> only a possibly existing job is removed.
	 * If <code>dateToSchedule</code> is in the future, a new job is scheduled for the given date. If <code>dateToSchedule</code> is in the past,
	 * the state change is executed immediately (synchronously).
	 * @precondition iGenericObjectId != null
	 * @throws NuclosBusinessRuleException if the transition from the current state to the new state is not possible for the given object.
	 */
	public void scheduleStateChange(Integer iGenericObjectId, int iNewState, Date dateToSchedule) throws NuclosBusinessRuleException {
		try {
			this.getGenericObject(iGenericObjectId);
		}
		catch(Exception ex) {
			throw new NuclosFatalException("rule.interface.error.6");
				//"Statuswecksel innerhalb der Stammdaten ist unzul\u00e4ssig. Die Stammdaten haben kein Statusmodell.");
		}

		final GenericObjectVO govo;
		try {
			govo = this.getRuleInterface().scheduleStateChange(this.getGenericObject(), iGenericObjectId, iNewState, dateToSchedule);
		}
		catch (CommonFinderException ex) {
			throw new NuclosBusinessRuleException(ex);
		}
		// accept the (possibly changed) govo:
		this.getRuleObjectContainerCVO().setGenericObject(govo);
	}

	/**
	 * check if a state change to state is possible for generic object with id iGenericObjectId
	 * @param iGenericObjectId
	 * @param state
	 * @return
	 * @precondition iGenericObjectId != null
	 */
	public boolean isStateChangePossible(Integer iGenericObjectId, int state) throws CommonFinderException {
		try {
			this.getGenericObject(iGenericObjectId);
		}
		catch(Exception ex) {
			throw new NuclosFatalException("rule.interface.error.6");
				//"Statuswecksel innerhalb der Stammdaten ist unzul\u00e4ssig. Die Stammdaten haben kein Statusmodell.");
		}

		return this.getRuleInterface().isStateChangePossible(iGenericObjectId, state);
	}

	/**
	 * transfers data from iGenericObjectSource to iGenericObjectTarget
	 * @param iGenericObjectSource
	 * @param iGenericObjectTarget
	 * @param asAttributes Array of attribute names to specify transferred data
	 * @precondition asAttributes != null
	 */
	public void transferGenericObjectData(GenericObjectVO govoSource, Integer iGenericObjectTarget, String[][] asAttributes) {
		if (asAttributes == null) {
			throw new NullArgumentException("asAttributes");
		}
		final GeneratorFacadeLocal generatorFacade = ServiceLocator.getInstance().getFacade(GeneratorFacadeLocal.class);
		generatorFacade.transferGenericObjectData(govoSource, iGenericObjectTarget, asAttributes);
	}

	/**
	 * allows loggin in rules, debug level
	 * @param message
	 */
	public void debug(Object message) {
		logger.debug(message);
	}

	/**
	 * add a new entry in the timelimit task list
	 * @param iGenericObjectId
	 * @param dateExpired
	 * @param sDescription
	 * @param dateCompleted
	 */
	public void addTimelimitTask(Integer iGenericObjectId, Date dateExpired, String sDescription, Date dateCompleted) {
		try {
			this.getGenericObject(iGenericObjectId);
		}
		catch(Exception ex) {
			throw new NuclosFatalException("rule.interface.error.8");
				//"Fristen k\u00f6nnen nicht f\u00fcr Stammdaten verwendet werden.");
		}
		final TimelimitTaskFacadeLocal timelimitfacade = ServiceLocator.getInstance().getFacade(TimelimitTaskFacadeLocal.class);
		try {
			timelimitfacade.create(new TimelimitTaskVO(sDescription, dateExpired, dateCompleted, iGenericObjectId));
		}
		catch (CommonValidationException ex) {
			throw new NuclosFatalRuleException(ex);
		}
		catch (CommonBusinessException ex) {
			throw new NuclosFatalRuleException(ex);
		}
	}

	/**
	 * add a new entry in the task list
	 * @param sTask
	 * @param sOwner
	 * @param sDelegator
	 * @param dScheduled
	 * @param dCompleted
	 * @param collTaskObjects
	 * @deprecated use addTask(String sTask, String sOwner, String sDelegator, java.util.Date dScheduled, java.util.Date dCompleted, Integer iPriority, Integer iVisibility, String description, Integer taskstatusId, String taskstatus, Collection<Pair<String, Long>> collTaskObjects)
	 */
	@Deprecated
	public void addTask(String sTask, String sOwner, String sDelegator, java.util.Date dScheduled, java.util.Date dCompleted, Integer iPriority, Collection<Integer> collTaskObjects) {
		addTask(sTask, sOwner, sDelegator, dScheduled, dCompleted, iPriority, TaskVO.TaskVisibility.PRIVATE.getValue(), collTaskObjects, null, null, null);
	}

	/**
	 * add a new entry in the task list
	 * @param sTask
	 * @param sOwner
	 * @param sDelegator
	 * @param dScheduled
	 * @param dCompleted
	 * @param collTaskObjects
	 * @deprecated use addTask(String sTask, String sOwner, String sDelegator, java.util.Date dScheduled, java.util.Date dCompleted, Integer iPriority, Integer iVisibility, String description, Integer taskstatusId, String taskstatus, Collection<Pair<String, Long>> collTaskObjects)
	 */
	@Deprecated
	public void addTask(String sTask, String sOwner, String sDelegator, java.util.Date dScheduled, java.util.Date dCompleted, Integer iPriority, Integer iVisibility, Collection<Integer> collTaskObjects, String description, Integer taskstatusId, String taskstatus) {
		ArrayList<Pair<String, Long>> objects = new ArrayList<Pair<String,Long>>();
		for (Integer iGenericObject : collTaskObjects) {
			try {
				int entityId = getGenericObjectFacade().getModuleContainingGenericObject(iGenericObject);
				objects.add(new Pair<String, Long>(Modules.getInstance().getEntityNameByModuleId(entityId), iGenericObject.longValue()));
			}
			catch (CommonFinderException ex) {
				throw new NuclosFatalRuleException(ex);
			}
		}
		addTask(sTask, sOwner, sDelegator, dScheduled, dCompleted, iPriority, iVisibility, description, taskstatusId, taskstatus, objects);
	}

	public void addTask(String sTask, String sOwner, String sDelegator, java.util.Date dScheduled, java.util.Date dCompleted, Integer iPriority, Integer iVisibility, String description, Integer taskstatusId, String taskstatus, Collection<Pair<String, Long>> collTaskObjects) {
		try {
			final TaskFacadeLocal taskfacadelocal = ServiceLocator.getInstance().getFacade(TaskFacadeLocal.class);
			Set<Long> stOwnerId = new HashSet<Long>();
			Long userId = taskfacadelocal.getUserId(sOwner);
			if(userId == null){
				throw new NuclosFatalRuleException("Owner/User with name " + sOwner + " is not available.");
			}
			stOwnerId.add(userId);
			MasterDataVO delegator = getUserVO(sDelegator);
			if(delegator == null){
				throw new NuclosBusinessRuleException("no valid user found: "+sDelegator);
			}
			TaskVO task = taskfacadelocal.create(new TaskVO(sTask, iVisibility, iPriority, dScheduled, dCompleted, delegator.getIntId(), sDelegator, taskstatusId, taskstatus, description, null, Collections.<TaskObjectVO>emptyList()), stOwnerId);
			for (Pair<String, Long> taskobject : collTaskObjects) {
				task.addRelatedObject(taskobject.getY(), taskobject.getX());
			}
			taskfacadelocal.modify(task, stOwnerId);
		}
		catch (CommonBusinessException ex) {
			throw new NuclosFatalRuleException(ex);
		}
	}

	private MasterDataVO getUserVO(String sUserName) {
		String result = null;

		final MasterDataFacadeLocal mdFacade = ServiceLocator.getInstance().getFacade(MasterDataFacadeLocal.class);

		final CollectableComparison cond = SearchConditionUtils.newMDComparison(MasterDataMetaCache.getInstance().getMetaData(NuclosEntity.USER), "name", ComparisonOperator.EQUAL, sUserName);
		final Collection<MasterDataVO> collmdvo = mdFacade.getMasterData(NuclosEntity.USER.getEntityName(), cond, false);

		return collmdvo != null && !collmdvo.isEmpty() ? collmdvo.iterator().next() : null;
	}

	/**
	 * @param sUserName
	 * @return email address of the given user or <code>null</code> if user is not found.
	 */
	public String getUserMailAddress(String sUserName) {
		String result = null;

		final MasterDataFacadeLocal mdFacade = ServiceLocator.getInstance().getFacade(MasterDataFacadeLocal.class);

		final CollectableComparison cond = SearchConditionUtils.newMDComparison(MasterDataMetaCache.getInstance().getMetaData(NuclosEntity.USER), "name", ComparisonOperator.EQUAL, sUserName);
		final Collection<MasterDataVO> collmdvo = mdFacade.getMasterData(NuclosEntity.USER.getEntityName(), cond, false);
		final Iterator<MasterDataVO> iter = collmdvo.iterator();
		if (iter.hasNext()) {
			result = (String) iter.next().getField("email");
		}

		return result;
	}

	public boolean isPeriodOverlapping(Date datePeriod1From, Date datePeriod1Until, Date datePeriod2From, Date datePeriod2Until) throws NuclosBusinessRuleException {
		if (isPeriodValid(datePeriod1From, datePeriod1Until) && isPeriodValid(datePeriod2From, datePeriod2Until)) {
			if (datePeriod1From != null && datePeriod1From.compareTo(datePeriod2From) < 0) {
				// period 1 is first one
				return !(datePeriod1Until.compareTo(datePeriod2From) < 0);
			}
			else if (datePeriod2From != null && datePeriod2From.compareTo(datePeriod1From) < 0) {
				// period 2 is first one
				return !(datePeriod2Until.compareTo(datePeriod1From) < 0);
			}
			else {
				return true;
			}
		}
		else {
			throw new NuclosBusinessRuleException("rule.interface.error.9");
				//"Eine der Perioden ist nicht g\u00fcltig. G\u00fcltig Von muss vor G\u00fcltig bis liegen.");
		}
	}

	/**
	 * checks if dateFrom is before dateUntil or if one of them is null
	 * @param dateFrom
	 * @param dateUntil
	 * @return true if dateFrom is before dateUntil or one of them is null
	 */
	public boolean isPeriodValid(Date dateFrom, Date dateUntil) {
		return dateFrom == null || dateUntil == null || dateFrom.compareTo(dateUntil) <= 0;
	}

	public CommonDate calculateTimelimit(MasterDataVO mdvoTimelimit) throws NuclosBusinessRuleException {
		return calculateTimelimit(mdvoTimelimit, new CommonDate());
	}

	/**
	 * calculate a timelimit from a masterdata record of entity timelimit
	 * @param mdvoTimelimt MasterDataVO from entity timelimit
	 * @return
	 * @throws NuclosBusinessRuleException
	 * @todo refactor: timelimit calculation does not belong here
	 */
	public CommonDate calculateTimelimit(MasterDataVO mdvoTimelimt, CommonDate ndStartDate) throws NuclosBusinessRuleException {
		final String sUnit = (String) mdvoTimelimt.getField("timelimitunit");
		final Integer iTimelimitCount = (Integer) mdvoTimelimt.getField("number");
		if (sUnit != null && iTimelimitCount != null) {
			int iCount = iTimelimitCount;
			if (sUnit.equals("Arbeitstag(e)")) {
				/* "Arbeitstag(e)" -> Montag bis Freitag ohne Feiertage */
				ndStartDate.addWorkingDays(iCount);
				return ndStartDate;
			}
			else if (sUnit.equals("Kalendertag(e)")) {
				ndStartDate.addDays(iCount);
				return ndStartDate;
			}
			else if (sUnit.equals("Jahr(e)")) {
				ndStartDate.set(ndStartDate.getYYYY() + iCount, ndStartDate.getMM(), ndStartDate.getDD());
				return ndStartDate;
			}
			else if (sUnit.equals("Monat(e)")) {
				ndStartDate.addMonth(iCount);
				return ndStartDate;
			}
			else if (sUnit.equals("Monat(e) zum Quartalsende")) {
				ndStartDate.addMonth(iCount);
				final CommonDate dQuarter;

				if (ndStartDate.getMM() < 4) {
					dQuarter = new CommonDate(ndStartDate.getYYYY(), 3, 31);
				}
				else if (ndStartDate.getMM() < 7) {
					dQuarter = new CommonDate(ndStartDate.getYYYY(), 6, 30);
				}
				else if (ndStartDate.getMM() < 10) {
					dQuarter = new CommonDate(ndStartDate.getYYYY(), 9, 30);
				}
				else {
					dQuarter = new CommonDate(ndStartDate.getYYYY(), 12, 31);
				}
				return dQuarter;
			}
			else if (sUnit.equals("Stunde(n)")) {
				throw new NuclosBusinessRuleException("rule.interface.error.10");
					//"Fristen k\u00f6nnen nicht stundengenau berechnet werden.");
			}
			else if (sUnit.equals("Werktag(e)")) {
				/* "Werktage(e)" -> Montag bis Samstag ohne Feiertage */
				ndStartDate.addBusinessDays(iCount);
				return ndStartDate;
			}
			else if (sUnit.equals("Woche(n)")) {
				ndStartDate.addDays(7 * iCount);
				return ndStartDate;
			}
			else if (sUnit.equals("Termin")) {
				return ndStartDate;
			}
			else {
				throw new NuclosBusinessRuleException(StringUtils.getParameterizedExceptionMessage("rule.interface.error.11", sUnit));
					//"Die Frist " + sUnit + " wird nicht unterst\u00fctzt");
			}
		}
		return null;
	}

	/**
	 * simple version of check only for NULL and NOT_NULL checks
	 * @param attrvo
	 * @param iComparator
	 * @return
	 * @throws NuclosBusinessRuleException
	 */
	public boolean check(DynamicAttributeVO attrvo, int iComparator) throws NuclosBusinessRuleException {
		return check(attrvo, iComparator, null);
	}

	/**
	 * validate if the condition defined in iComparator is true between atrrVO and attrvoCompare
	 * if the condition is false the attribute is added to liAttributeCheckFailed
	 * @param attrvo
	 * @param iComparator
	 * @param attrvoCompare
	 * @return
	 * @throws NuclosBusinessRuleException
	 */
	public boolean check(DynamicAttributeVO attrvo, int iComparator, DynamicAttributeVO attrvoCompare) throws NuclosBusinessRuleException {
		if (iComparator > 1 && attrvoCompare == null) {
			throw new NuclosBusinessRuleException("rule.interface.error.12");
				//"Die Regel ist fehlerhaft. Bitte geben Sie als dritten Parameter der Funktion check einen Vergleichswert an!");
		}
		boolean result = true;
		switch (iComparator) {
			case RuleConstants.NULL:
				if (!this.isAttributeValueNull(AttributeCache.getInstance().getAttribute(attrvo.getAttributeId()).getName())) {
					addFailedCheck(attrvo, iComparator);
					result = false;
				}
				break;

			case RuleConstants.NOT_NULL:
				if ((attrvo == null || attrvo.isRemoved() || attrvo.getValue() == null)) {
					addFailedCheck(attrvo, iComparator);
					result = false;
				}
				break;

			case RuleConstants.EQUAL:
				/** @todo replace with LangUtils.compare(getValueOrNull(attrvo), getValueOrNull(attrvoCompare)) */
				if (!((attrvo == null) ? (attrvoCompare == null) : LangUtils.compare(attrvo.getValue(), attrvoCompare.getValue()) == 0))
				{
					addFailedCheck(attrvo, iComparator, attrvoCompare);
					result = false;
				}
				break;

			case RuleConstants.NOT_EQUAL:
				/** @todo replace with LangUtils.compare(getValueOrNull(attrvo), getValueOrNull(attrvoCompare)) */
				if ((attrvo == null) ? (attrvoCompare == null) : LangUtils.compare(attrvo.getValue(), attrvoCompare.getValue()) == 0)
				{
					addFailedCheck(attrvo, iComparator, attrvoCompare);
					result = false;
				}
				break;

			case RuleConstants.GREATER:
				if (LangUtils.compare(attrvo.getValue(), attrvoCompare.getValue()) <= 0 || attrvo.isRemoved()) {
					addFailedCheck(attrvo, iComparator, attrvoCompare);
					result = false;
				}
				break;

			case RuleConstants.LESS:
				if (LangUtils.compare(attrvo.getValue(), attrvoCompare.getValue()) >= 0) {
					addFailedCheck(attrvo, iComparator, attrvoCompare);
					result = false;
				}
				break;

			case RuleConstants.GREATER_OR_EQUAL:
				if (!attrvo.isRemoved() && !attrvoCompare.isRemoved()) {
					if (LangUtils.compare(attrvo.getValue(), attrvoCompare.getValue()) < 0) {
						addFailedCheck(attrvo, iComparator, attrvoCompare);
						result = false;
					}
				}
				else {
					if (attrvo.isRemoved() && !attrvoCompare.isRemoved()) {
						addFailedCheck(attrvo, iComparator, attrvoCompare);
						result = false;
					}
				}
				break;

			case RuleConstants.LESS_OR_EQUAL:
				if (!attrvo.isRemoved() && !attrvoCompare.isRemoved()) {
					if (LangUtils.compare(attrvoCompare.getValue(), attrvo.getValue()) < 0) {
						addFailedCheck(attrvo, iComparator, attrvoCompare);
						result = false;
					}
				}
				else {
					if (attrvoCompare.isRemoved() && !attrvo.isRemoved()) {
						addFailedCheck(attrvo, iComparator, attrvoCompare);
						result = false;
					}
				}
				break;

			default:
				/** @todo ??? */
		}
		return result;
	}

	/**
	 * evaluates the result of all prior calls to the check(..) function
	 * if liAttributeCheckFailed is not empty an error String is created
	 * @throws NuclosBusinessRuleException
	 */
	public void evaluateCheckResult() throws NuclosBusinessRuleException {
		if (!lstCheckFailed.isEmpty()) {
			LocaleInfo localeUser = getLocaleFacade().getUserLocale();
			final StringBuffer sbMessage = new StringBuffer(getLocaleFacade().getResourceById(localeUser, "ruleinterface.error.attribute.validation"));//"Validierung fehlgeschlagen. ");
			sbMessage.append((lstCheckFailed.size() == 1 ?
				getLocaleFacade().getResourceById(localeUser, "ruleinterface.message.attribute.validation.1") :
					getLocaleFacade().getResourceById(localeUser, "ruleinterface.message.attribute.validation.2")));
			//Das folgende Feld ist " : "Die folgenden Felder sind "));
			sbMessage.append(getLocaleFacade().getResourceById(localeUser, "ruleinterface.message.attribute.validation.3"));//"nicht korrekt gef\u00fcllt: \n");
			for (Object oMessage : lstCheckFailed) {
				sbMessage.append(oMessage).append("\n");
			}
			throw new NuclosBusinessRuleException(sbMessage.toString());
		}
	}

	/**
	 * add an attribute to the list of failed checks
	 * @param attrvo
	 * @param iComparator
	 * @param attrvoCompare
	 */
	private void addFailedCheck(DynamicAttributeVO attrvo, int iComparator, DynamicAttributeVO attrvoCompare) {
		lstCheckFailed.add(new Check(attrvo, iComparator, attrvoCompare));
	}

	/**
	 * add an attribute to the list of failed checks
	 * @param attrvo
	 * @param iComparator
	 */
	private void addFailedCheck(DynamicAttributeVO attrvo, int iComparator) {
		lstCheckFailed.add(new Check(attrvo, iComparator));
	}

	/**
	 * add a message to the list of failed checks
	 * @param sErrorMessage
	 */
	public void addErrorMessage(String sErrorMessage) {
		lstCheckFailed.add(sErrorMessage);
	}

	/**
	 * creates a relation between the given generic objects, of the given type.
	 * @param iGenericObjectIdFrom
	 * @param iGenericObjectIdTo
	 * @param relationType
	 */
	public void relate(Integer iGenericObjectIdFrom, Integer iGenericObjectIdTo, String relationType)
			throws NuclosBusinessRuleException {
		this.relate(iGenericObjectIdFrom, iGenericObjectIdTo, relationType, null, null, null);
	}

	/**
	 * creates a relation between the given generic objects, of the given type.
	 * @param iGenericObjectIdFrom
	 * @param iGenericObjectIdTo
	 * @param relationType
	 * @param bDeferred if true relating is executed later on, because the object to relate is not yet created
	 */
	public void relate(Integer iGenericObjectIdFrom, Integer iGenericObjectIdTo, String relationType, boolean bDeferred)
			throws NuclosBusinessRuleException {
		if (bDeferred) {
			if (this.lstActions == null) {
				throw new NuclosBusinessRuleException("rule.interface.error.13");
					//"Verz\u00f6gerte Ausf\u00fchrung ist nur in Objektgenerierungsregeln erlaubt.");
			}
			final StringBuilder sb = new StringBuilder("relate:");
			sb.append(iGenericObjectIdFrom != null ? iGenericObjectIdFrom : "this");
			sb.append(":");
			sb.append(iGenericObjectIdTo != null ? iGenericObjectIdTo : "this");
			sb.append(":");
			sb.append(relationType);
			// TODO_AUTOSYNC: what happens with this list?
			lstActions.add(sb.toString());
		}
		else {
			this.relate(iGenericObjectIdFrom, iGenericObjectIdTo, relationType, null, null, null);
		}
	}

	/**
	 * creates a relation between the given generic objects, of the given type.
	 * @param iGenericObjectIdFrom
	 * @param iGenericObjectIdTo
	 * @param relationType
	 * @param dateValidFrom
	 * @param dateValidUntil
	 * @param sDescription
	 */
	public void relate(Integer iGenericObjectIdFrom, Integer iGenericObjectIdTo, String relationType,
			Date dateValidFrom, Date dateValidUntil, String sDescription) throws NuclosBusinessRuleException {
		try {
			this.getRuleInterface().relate(iGenericObjectIdFrom, iGenericObjectIdTo, relationType, dateValidFrom, dateValidUntil, sDescription);
		}
		catch (CommonCreateException ex) {
			throw new NuclosBusinessRuleException(ex);
		}
		catch (CommonFinderException ex) {
			throw new NuclosBusinessRuleException(ex);
		}
		catch (CommonPermissionException ex) {
			throw new NuclosBusinessRuleException(ex);
		}
	}

	/**
	 * removes the relations between the given generic objects of the given type.
	 * @param iGenericObjectIdFrom
	 * @param iGenericObjectIdTo
	 * @param relationType
	 */
	public void unrelate(Integer iGenericObjectIdFrom, Integer iGenericObjectIdTo, String relationType)
			throws NuclosBusinessRuleException {
		try {
			this.getRuleInterface().unrelate(iGenericObjectIdFrom, iGenericObjectIdTo, relationType);
		}
		catch (CommonFinderException ex) {
			throw new NuclosBusinessRuleException(ex);
		}
		catch (CommonPermissionException ex) {
			throw new NuclosBusinessRuleException(ex);
		}
		catch (CommonRemoveException ex) {
			throw new NuclosBusinessRuleException(ex);
		}
	}

	/**
	 * invalidates the relation between the given generic objects, of the given type, by setting the "validUntil" field to the current date, if necessary.
	 * @todo rename to invalidateRelations
	 * @param iGenericObjectIdFrom
	 * @param iGenericObjectIdTo
	 * @param relationType
	 */
	public void invalidateRelation(Integer iGenericObjectIdFrom, Integer iGenericObjectIdTo, String relationType) throws NuclosBusinessRuleException {
		try {
			this.getRuleInterface().invalidateRelations(iGenericObjectIdFrom, iGenericObjectIdTo, relationType);
		}
		catch (CommonFinderException ex) {
			throw new NuclosBusinessRuleException(ex);
		}
		catch (CommonBusinessException ex) {
			throw new NuclosBusinessRuleException(ex);
		}
	}

	/**
	 * add a notification message which will be shown after the transaction was successful
	 * @param sMessage
	 */
	public void addNotificationMessage(String sMessage, Priority priority) {
		final RuleNotification message = new RuleNotification(priority, sMessage, rulevo.getName());

		final String sAttributeNameSystemIdentifier = NuclosEOField.SYSTEMIDENTIFIER.getMetaData().getField();

		final AttributeCache attrprovider = AttributeCache.getInstance();

		if (roccvoSource != null) {
			message.setSourceIdentifier((String) getValueOrNull(roccvoSource.getGenericObject().getAttribute(sAttributeNameSystemIdentifier, attrprovider)));
			message.setSourceId(roccvoSource.getGenericObject().getId());

			message.setTargetIdentifier((String) getValueOrNull(roccvo.getGenericObject().getAttribute(sAttributeNameSystemIdentifier, attrprovider)));
			message.setTargetId(roccvo.getGenericObject().getId());
		}
		else {
			String sSourceIdentifier;
			Integer iSourceId;
			if (roccvo.getGenericObject() != null) {
				final DynamicAttributeVO attrvo = roccvo.getGenericObject().getAttribute(sAttributeNameSystemIdentifier, attrprovider);
				sSourceIdentifier = (String) getValueOrNull(attrvo);
				iSourceId = roccvo.getGenericObject().getId();
			}
			else {
				sSourceIdentifier = (String)roccvo.getMasterData().getField("name");
				iSourceId = roccvo.getMasterData().getIntId();
			}
			message.setSourceIdentifier(sSourceIdentifier);
			message.setSourceId(iSourceId);
		}

		lstNotification.add(message);
	}

	/** get the notifications added by a single rule */
	public List<RuleNotification> getRuleNotification() {
		return this.lstNotification;
	}

	/**
	 * @deprecated
	 */
	@Deprecated
	public boolean isGenericObjectNew() {
		return this.isObjectNew();
	}

	public boolean isObjectNew() {
		if (roccvo.getGenericObject() != null)
			return roccvo.getGenericObject().getId() == null;
		else
			return roccvo.getMasterData().getId() == null;
	}

	/**
	 * @param sAttributeName
	 * @return Has one of the given attributes changed in the current object?
	 * @deprecated
	 */
	@Deprecated
	public boolean hasAttributeChanged(String... sAttributeName) {
		return this.hasFieldChanged(Modules.getInstance().getEntityNameByModuleId(this.getModuleId()), sAttributeName);
	}

	/**
	 * @param sFieldName
	 * @return Has one of the given fields changed in the current object?
	 */
	public boolean hasFieldChanged(String sEntityName, String...sFieldName) {
		if (Modules.getInstance().isModuleEntity(sEntityName)) {
			return this.hasFieldChanged(sEntityName, this.roccvo.getGenericObject().getId(), sFieldName);
		}
		else {
			return this.hasFieldChanged(sEntityName, this.roccvo.getMasterData().getIntId(), sFieldName);
		}

	}

	/**
	 * @param iGenericObjectToCompareWith the id if the object to compare attributes with
	 * @param asAttributeNames names of the attributes to compare
	 * @return Does one of the given attributes have a different value in the current object
	 * than in the object with the given id?
	 * @deprecated
	 */
	@Deprecated
	public boolean hasAttributeChanged(Integer iGenericObjectToCompareWith, String... asAttributeNames) {
		return this.hasFieldChanged(Modules.getInstance().getEntityNameByModuleId(this.getModuleId(iGenericObjectToCompareWith)), iGenericObjectToCompareWith, asAttributeNames);
	}

	/**
	 * @param iObjectToCompareWith the id if the object to compare fields with
	 * @param asFieldNames names of the fields to compare
	 * @return Does one of the given fields have a different value in the current object
	 * than in the object with the given id?
	 */
	public boolean hasFieldChanged(String sEntityName, Integer iObjectToCompareWith, String... asFieldNames) {
		boolean result = false;
		if (Modules.getInstance().isModuleEntity(sEntityName)) {
			for (String sAttributeName : asFieldNames) {
				final Object oValueOld = getValueOrNull(this.getRuleInterface().getAttribute(iObjectToCompareWith, sAttributeName));
				final Object oValueNew = this.getFieldValue(sEntityName,sAttributeName);
				result = !LangUtils.equals(oValueOld, oValueNew);
				if (result) {
					break;
				}
			}
		}
		else {
			for (String sFieldName : asFieldNames) {
				final Object oValueOld = this.getMasterData(sEntityName, iObjectToCompareWith).getField(sFieldName);
				final Object oValueNew = this.getMasterData().getField(sFieldName);
				result = !LangUtils.equals(oValueOld, oValueNew);
				if (result) {
					break;
				}
			}
		}
			return result;
	}


	/**
	 * @return next unique system id using default sequence "IDFACTORY"
	 */
	public Integer getNextIntid() {
		return DataBaseHelper.getNextIdAsInteger(DataBaseHelper.DEFAULT_SEQUENCE);
	}

	/**
	 * checks whether the value of the given masterdatafield has changed
	 * @param sEntityName
	 * @param iId
	 * @param sFieldName
	 * @return true, if value of masterdatafield has changed or is new, otherwise false
	 * @throws NuclosFatalRuleException
	 */
	public boolean hasMasterDataFieldChanged(String sEntityName, Integer iId, String sFieldName) throws NuclosFatalRuleException {
		if (!MasterDataMetaCache.getInstance().exist(sEntityName)) {
			throw new NuclosFatalRuleException(StringUtils.getParameterizedExceptionMessage("rule.interface.error.14", sEntityName));
				//"Die Stammdatenentit\u00e4t '"+sEntityName+"' existiert nicht.");
		}

		if (MasterDataMetaCache.getInstance().getMetaData(sEntityName).getField(sFieldName) == null) {
			throw new NuclosFatalRuleException(StringUtils.getParameterizedExceptionMessage("rule.interface.error.15", sFieldName, sEntityName));
				//"Das Feld '"+sFieldName+"' der Stammdatenentit\u00e4t '"+sEntityName+"' existiert nicht.");
		}

		MasterDataVO mdvo_old;

		try {
			mdvo_old = getMasterData(sEntityName, iId);
		}
		catch (NuclosFatalRuleException e) {
			// record is new
			return true;
		}

		Collection<MasterDataVO> collmdvo_new = getDependants(sEntityName);

		for (MasterDataVO mdvo_new : collmdvo_new) {
			if (mdvo_new.getIntId() != null && mdvo_new.getIntId().compareTo(iId) == 0) {
				Object oNewValue = mdvo_new.getField(sFieldName);
				Object oOldValue = mdvo_old.getField(sFieldName);

				if ((oNewValue != null && oOldValue == null) ||
						(oNewValue == null && oOldValue != null)) {
					return true;
				}
				else if (oNewValue == null && oOldValue == null) {
					return false;
				}
				else if (oNewValue.equals(oOldValue)) {
					return false;
				}
				else {
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * Foreign key field must be set explicitly
	 * @param sEntity
	 * @param mapFields
	 */
	public void addSubformEntry(String sEntity, Map<String, Object> mapFields) {
	    MasterDataVO entry = new MasterDataVO(null, null, null, null, null, null, mapFields);
	    getRuleObjectContainerCVO().addDependant(sEntity, entry);
	}

	/**
	 *
	 * @param mail
	 * @throws NuclosFatalRuleException
	 */
	public void sendMail(NuclosMail mail) throws NuclosFatalRuleException{
		try{
			NuclosMailSender.sendMail(mail);
		} catch (CommonBusinessException e) {
			throw new NuclosFatalRuleException(e.getMessage());
		}
	}

	/**
	 *
	 * @param smtpHost
	 * @param mail
	 * @throws NuclosFatalRuleException
	 */
	public void sendMail(String smtpHost, NuclosMail mail) throws NuclosFatalRuleException{
		try{
			NuclosMailSender.sendMail(smtpHost, mail);
		} catch (CommonBusinessException e) {
			throw new NuclosFatalRuleException(e.getMessage());
		}
	}

	/**
	 *
	 * @param smtpHost
	 * @param smtpPort
	 * @param mail
	 * @throws NuclosFatalRuleException
	 */
	public void sendMail(String smtpHost, Integer smtpPort, NuclosMail mail) throws NuclosFatalRuleException{
		try{
			NuclosMailSender.sendMail(smtpHost, smtpPort, mail);
		} catch (CommonBusinessException e) {
			throw new NuclosFatalRuleException(e.getMessage());
		}
	}

	/**
	 *
	 * @param smtpHost
	 * @param smtpPort
	 * @param login
	 * @param password
	 * @param mail
	 * @throws NuclosFatalRuleException
	 */
	public void sendMail(String smtpHost, Integer smtpPort, String login, String password, NuclosMail mail) throws NuclosFatalRuleException{
		try{
			NuclosMailSender.sendMail(smtpHost, smtpPort, login, password, mail);
		} catch (CommonBusinessException e) {
			throw new NuclosFatalRuleException(e.getMessage());
		}
	}

	/**
	 * Get mails from POP3 by system parameters
	 *
	 * @param remove
	 * @return
	 * @throws NuclosFatalException
	 */
	public List<NuclosMail> getMails(boolean remove) throws NuclosFatalException {
		String pop3Host = ServerParameterProvider.getInstance().getValue(ParameterProvider.KEY_POP3_SERVER);
		String pop3Port = ServerParameterProvider.getInstance().getValue(ParameterProvider.KEY_POP3_PORT);
		String pop3User = ServerParameterProvider.getInstance().getValue(ParameterProvider.KEY_POP3_USERNAME);
		String pop3Password = ServerParameterProvider.getInstance().getValue(ParameterProvider.KEY_POP3_PASSWORD);

		return getMails(pop3Host, pop3Port, pop3User, pop3Password, remove);
	}

	/**
	 *
	 * @param pop3Host
	 * @param pop3Port
	 * @param pop3User
	 * @param pop3Password
	 * @param remove
	 * @return
	 * @throws NuclosFatalRuleException
	 */
	public List<NuclosMail> getMails(String pop3Host, String pop3Port, final String pop3User, final String pop3Password, boolean remove) throws NuclosFatalRuleException{
		try {
			Properties properties = new Properties();
			properties.setProperty("mail.pop3.host", pop3Host);
			properties.setProperty("mail.pop3.port", pop3Port);
			properties.setProperty("mail.pop3.auth", "true");
			properties.setProperty( "mail.pop3.socketFactory.class", "javax.net.DefaultSocketFactory");

			Session session = Session.getInstance(properties,
				new javax.mail.Authenticator() {
					@Override
					protected PasswordAuthentication getPasswordAuthentication() {
						return new PasswordAuthentication(pop3User, pop3Password);
					}
				});

			session.setDebug(true);
		   Store store = session.getStore("pop3");
		   store.connect();

		   Folder folder = store.getFolder("INBOX");
		   if (remove){
		   	folder.open(Folder.READ_WRITE);
		   } else {
		   	folder.open(Folder.READ_ONLY);
		   }

		   List<NuclosMail> result = new ArrayList<NuclosMail>();

		   Message message[] = folder.getMessages();
		   for ( int i = 0; i < message.length; i++ )
		   {
				Message m = message[i];
				NuclosMail mail = new NuclosMail();
				logger.debug("Received mail: From: " + Arrays.toString(m.getFrom()) + "; To: " + Arrays.toString(m.getAllRecipients()) + "; ContentType: " + m.getContentType() + "; Subject: " + m.getSubject() + "; Sent: " + m.getSentDate());

				Address[] senders = m.getFrom();
				if (senders.length == 1 && senders[0] instanceof InternetAddress) {
					mail.setFrom(((InternetAddress)senders[0]).getAddress());
				}
				else {
					mail.setFrom(Arrays.toString(m.getFrom()));
				}
				mail.setTo(Arrays.toString(m.getRecipients(RecipientType.TO)));
				mail.setSubject(m.getSubject());

				if (m.isMimeType("text/plain")) {
					mail.setMessage((String) m.getContent());
				}
				else {
					Multipart mp = (Multipart) m.getContent();
					for(int j = 0; j < mp.getCount(); j++) {
						Part part = mp.getBodyPart(j);
						String disposition = part.getDisposition();
						MimeBodyPart mimePart = (MimeBodyPart) part;
						logger.debug("Disposition: " + disposition + "; Part ContentType: " + mimePart.getContentType());

						if(disposition == null && (mimePart.isMimeType("text/plain") || mimePart.isMimeType("text/html"))) {
							mail.setMessage((String) mimePart.getDataHandler().getContent());
						}
					}
					getAttachments(mp, mail);
				}

				result.add(mail);

				if (remove) {
					m.setFlag(Flags.Flag.DELETED, true);
				}
			}

		   if (remove) {
		   	folder.close(true);
		   } else {
		   	folder.close(false);
		   }

			store.close();

			return result;
		} catch (Exception e) {
			throw new NuclosFatalRuleException(e);
		}
	}

	private void getAttachments(Multipart multipart, NuclosMail mail) throws MessagingException, IOException {
		for (int i = 0; i < multipart.getCount(); i++) {
			Part part = multipart.getBodyPart(i);
			String disposition = part.getDisposition();
			MimeBodyPart mimePart = (MimeBodyPart) part;
			logger.debug("Disposition: " + disposition + "; Part ContentType: " + mimePart.getContentType());

			if (part.getContent() instanceof Multipart) {
				logger.debug("Start child Multipart.");
				Multipart childmultipart = (Multipart) part.getContent();
				getAttachments(childmultipart, mail);
				logger.debug("Finished child Multipart.");
			}
			else if (Part.ATTACHMENT.equalsIgnoreCase(disposition)) {
				logger.debug("Attachment: " + mimePart.getFileName());
				InputStream in = mimePart.getInputStream();
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				byte[] buf = new byte[8192];
				int len;
				while ((len = in.read(buf)) > 0) {
					out.write(buf, 0, len);
				}
				mail.addAttachment(new NuclosFile(mimePart.getFileName(), out.toByteArray()));
			}
		}
	}

	public RuleVO getCurrentRule() {
		return this.rulevo;
	}

	/**
	 * set job run id
	 * @param iId
	 */
	public void setSessionId(Integer iId) {
		this.iSessionId = iId;
	}

	private Integer getSessionId() {
		return this.iSessionId;
	}

	/**
	 * @throws NuclosBusinessRuleException
	 */
	public MasterDataVO getCurrentUser() throws NuclosBusinessRuleException {
		MasterDataVO returnValue = null;

		SecurityFacadeLocal secFacadeHome = ServiceLocator.getInstance().getFacade(SecurityFacadeLocal.class);;
		String sUserName = secFacadeHome.getUserName();


		final MasterDataFacadeLocal mdfacadehome = ServiceLocator.getInstance().getFacade(MasterDataFacadeLocal.class);
		final CollectableComparison cond = SearchConditionUtils.newMDComparison(MasterDataMetaCache.getInstance().getMetaData(NuclosEntity.USER), "name", ComparisonOperator.EQUAL, sUserName);
		final Collection<MasterDataVO> collmdvo = mdfacadehome.getMasterData(NuclosEntity.USER.getEntityName(), cond, false);
		if (collmdvo.size() > 1) {
			throw new NuclosBusinessRuleException(StringUtils.getParameterizedExceptionMessage("rule.interface.error.16", sUserName));
				//"Der User \""+sUserName+"\" konnte nicht eindeutig identifiziert werden (Eventuell gibt es mehrere User mit dem gleichen Namen)");
		} else if (collmdvo.size() < 1) {
			throw new NuclosBusinessRuleException(StringUtils.getParameterizedExceptionMessage("rule.interface.error.17", sUserName));
				//"Der User \""+sUserName+"\" konnte nicht gefunden werden");
		}

		returnValue = collmdvo.iterator().next();
		return returnValue;
	}

	/**
	 * Store attachment to server object in GeneralSearchDocument entity
	 * @param attachment
	 */
	public void storeAttachment(final NuclosFile attachment) {
		this.storeAttachment(null, null, attachment);
	}

	public void storeAttachments(final Collection<NuclosFile> attachments) {
		for (NuclosFile attachment : attachments) {
			this.storeAttachment(attachment);
		}
	}

	/**
	 * Store attachment to server object in GeneralSearchDocument entity
	 * @param attachment
	 * @param comment
	 */
	public void storeAttachment(final NuclosFile attachment, final String comment) {
		this.storeAttachment(null, null, attachment, comment);
	}

	public void storeAttachments(final Collection<NuclosFile> attachments, final String comment) {
		for (NuclosFile attachment : attachments) {
			this.storeAttachment(attachment, comment);
		}
	}

	/**
	 * Store attachment to object of given entity in GeneralSearchDocument entity
	 * @param entity
	 * @param iObjectId
	 * @param attachment
	 */
	public void storeAttachment(final String entity, final Integer iObjectId, final NuclosFile attachment) {
		this.storeAttachment(entity, iObjectId, NuclosEntity.GENERALSEARCHDOCUMENT.getEntityName(), "genericObject", "file", attachment);
	}

	public void storeAttachments(final String entity, final Integer iObjectId, final Collection<NuclosFile> attachments) {
		for (NuclosFile attachment : attachments) {
			this.storeAttachment(entity, iObjectId, attachment);
		}
	}

	/**
	 * Store attachment to object of given entity in GeneralSearchDocument entity
	 * @param entity
	 * @param iObjectId
	 * @param attachment
	 * @param comment
	 */
	@SuppressWarnings("unchecked")
	public void storeAttachment(final String entity, final Integer iObjectId, final NuclosFile attachment, final String comment) {
		this.storeAttachment(entity, iObjectId, NuclosEntity.GENERALSEARCHDOCUMENT.getEntityName(), "genericObject", "file", attachment, new Pair<String, Object>("comment", comment));
	}

	public void storeAttachments(final String entity, final Integer iObjectId, final Collection<NuclosFile> attachments, String comment) {
		for (NuclosFile attachment : attachments) {
			this.storeAttachment(entity, iObjectId, attachment, comment);
		}
	}

	/**
	 * Store attachment to server object in specified attachment entity
	 * @param entity (entity of object id)
	 * @param iObjectId
	 * @param attachmentEntity (dependant entity for storing attachment)
	 * @param foreignField (referencing field between entity and attachmentEntity)
	 * @param attachmentField
	 * @param attachment (NuclosFile Object)
	 * @param additionalFields (may be like comments or something like this)
	 */
	public void storeAttachment(final String attachmentEntity, final String foreignField, final String attachmentField, final NuclosFile attachment, final Pair<String, Object>...additionalFields) {
		this.storeAttachment(null, null, attachmentEntity, foreignField, attachmentField, attachment, additionalFields);
	}

	public void storeAttachments(final String attachmentEntity, final String foreignField, final String attachmentField, final Collection<NuclosFile> attachments, final Pair<String, Object>...additionalFields) {
		for (NuclosFile attachment : attachments) {
			this.storeAttachment(attachmentEntity, foreignField, attachmentField, attachment, additionalFields);
		}
	}

	/**
	 * Store attachment to object of given entity in specified attachment entity
	 * @param entity (entity of object id)
	 * @param iObjectId
	 * @param attachmentEntity (dependant entity for storing attachment)
	 * @param foreignField (referencing field between entity and attachmentEntity)
	 * @param attachmentField
	 * @param attachment (NuclosFile Object)
	 * @param additionalFields (may be like comments or something like this)
	 */
	public void storeAttachment(final String entity, final Integer iObjectId, final String attachmentEntity, final String foreignField, final String attachmentField, final NuclosFile attachment, final Pair<String, Object>...additionalFields) {
		if (attachmentEntity == null) {
			throw new NuclosFatalRuleException("attachmentEntity could not be null");
		}
		if (foreignField == null) {
			throw new NuclosFatalRuleException("foreignField could not be null");
		}
		if (attachmentField == null) {
			throw new NuclosFatalRuleException("attachmentField could not be null");
		}
		if (attachment == null) {
			throw new NuclosFatalRuleException("attachment could not be null");
		}
		if (iObjectId == null) {
			// current server object
			Collection<MasterDataVO> attachments = this.getDependants(attachmentEntity, foreignField);
			attachments = this.addAttachmentToCollection(attachments, attachmentEntity, attachmentField, attachment, additionalFields);
			this.setDependants(attachmentEntity, attachments);
		} else {
			if (entity == null) {
				throw new NuclosFatalRuleException("entity could not be null");
			}
			Collection<MasterDataVO> attachments = this.getDependants(iObjectId, attachmentEntity, foreignField);
			attachments = this.addAttachmentToCollection(attachments, attachmentEntity, attachmentField, attachment, additionalFields);
			this.setDependants(entity, iObjectId, attachmentEntity, attachments);
		}
	}

	public void storeAttachments(final String entity, final Integer iObjectId, final String attachmentEntity, final String foreignField, final String attachmentField, final Collection<NuclosFile> attachments, final Pair<String, Object>...additionalFields) {
		for (NuclosFile attachment : attachments) {
			this.storeAttachment(entity, iObjectId, attachmentEntity, foreignField, attachmentField, attachment, additionalFields);
		}
	}

	private Collection<MasterDataVO> addAttachmentToCollection(Collection<MasterDataVO> attachments, final String attachmentEntity, final String attachmentField, final NuclosFile attachment, final Pair<String, Object>...additionalFields) {
		Collection<MasterDataVO> result = new ArrayList<MasterDataVO>();
		result.addAll(attachments);

		MasterDataVO newAttachment = new MasterDataVO(MasterDataMetaCache.getInstance().getMetaData(attachmentEntity), false);
		newAttachment.setField(attachmentField, new GenericObjectDocumentFile(attachment));
		for (Pair<String, Object> additionalField : additionalFields) {
			newAttachment.setField(additionalField.x, additionalField.y);
		}
		result.add(newAttachment);
		return result;
	}

	/**
	 * run report on server object
	 * @param reportName
	 * 	report name would be like "reportName_2010-06-01 12-05-00.pdf"
	 */
	public Collection<NuclosFile> runPDFReport(String reportName) {
		return runPDFReport(reportName, null, true);
	}

	/**
	 * run report on server object
	 * @param reportName
	 * @param objectFieldForReportName (if set report name would be like "reportName_VALUE-OF-THIS-FIELD.pdf" otherwise "reportName.pdf")
	 * @param nameWithTimestamp (is set report name would be like "reportName_2010-06-01 12-05-00.pdf" otherwise "reportName.pdf"
	 * 	both, objectFieldForReportName and nameWithTimestamp could be combined!
	 */
	public Collection<NuclosFile> runPDFReport(String reportName, String objectFieldForReportName, boolean nameWithTimestamp) {
		return this.runPDFReport(reportName,
			MasterDataMetaCache.getInstance().getMasterDataMetaById(this.getGenericObject().getModuleId()).getEntityName(),
			this.getGenericObjectId(),
			objectFieldForReportName,
			nameWithTimestamp);
	}

	/**
	 *
	 * @param reportName
	 * @param entity
	 * @param iObjectId
	 * @param objectFieldForReportName (if set report name would be like "reportName_VALUE-OF-THIS-FIELD.pdf" otherwise "reportName.pdf")
	 * @param nameWithTimestamp (is set report name would be like "reportName_2010-06-01 12-05-00.pdf" otherwise "reportName.pdf"
	 * 	both, objectFieldForReportName and nameWithTimestamp could be combined!
	 */
	public Collection<NuclosFile> runPDFReport(String reportName, String entity, Integer iObjectId, String objectFieldForReportName, boolean nameWithTimestamp) {
		if (reportName == null) {
			throw new NuclosFatalRuleException("reportName could not be null");
		}
		if (entity == null) {
			throw new NuclosFatalRuleException("entity could not be null");
		}
		if (iObjectId == null) {
			throw new NuclosFatalRuleException("iObjectId could not be null");
		}
		final ReportFacadeRemote reportFacade = ServiceLocator.getInstance().getFacade(ReportFacadeRemote.class);
		final CollectableSearchCondition clctcond = SearchConditionUtils.newEOComparison(NuclosEntity.REPORT.getEntityName(), "name", ComparisonOperator.EQUAL, reportName, MetaDataServerProvider.getInstance());
		final List<EntityObjectVO> lstReport = NucletDalProvider.getInstance().getEntityObjectProcessor(NuclosEntity.REPORT.getEntityName()).getBySearchExpression(new CollectableSearchExpression(clctcond));
		String fileName = reportName;
		if (objectFieldForReportName != null) {
			String fieldValue = (String) this.getRuleInterface().getEntityObject(entity, iObjectId.longValue()).getFields().get(objectFieldForReportName);
			fileName += fieldValue != null? ("_"+fieldValue) : "";
		}
		if (nameWithTimestamp) {
			final DateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss", Locale.getDefault());
			fileName += "_" + dateformat.format(Calendar.getInstance(Locale.getDefault()).getTime());
		}
		final Map<String, Object> mpParams = new HashMap<String, Object>();
		mpParams.put("intid", iObjectId);
		mpParams.put("iGenericObjectId", iObjectId);

		final Set<ReportOutputVO> reportOutputs = new HashSet<ReportOutputVO>();
		for (EntityObjectVO reportVO : lstReport) {
			for (ReportOutputVO outputVO : reportFacade.getReportOutputs(reportVO.getId().intValue())) {
				if (outputVO.getFormat().equals(ReportOutputVO.Format.PDF)) {
					reportOutputs.add(outputVO);
				}
			}
		}

		if (reportOutputs.isEmpty()) {
			throw new NuclosFatalRuleException(StringUtils.getParameterizedExceptionMessage("rule.interface.error.18", reportName));
		}

		final Collection<NuclosFile> result = new ArrayList<NuclosFile>();
		int countOutputs = 1;
		for (final ReportOutputVO outputVO : reportOutputs) {
			final String name = fileName;
			final int index = countOutputs;
			NuclosLocalServerSession.runUnrestricted(new Runnable() {
				@Override
				public void run() {
					try {
						final JasperPrint jasperPrint = reportFacade.prepareReport(outputVO.getId(), mpParams, null);
						result.add(new NuclosFile(name + (reportOutputs.size() > 1 ? ("_" + index) : "") + ".pdf", JasperExportManager.exportReportToPdf(jasperPrint)));
					}
					catch (CommonBusinessException e) {
						throw new NuclosFatalRuleException(e);
					}
					catch (JRException e) {
						throw new NuclosFatalRuleException(e);
					}
				}
			});
			countOutputs++;
		}

		return result;
	}

	/**
	 * define and execute a file import (csv)
	 *
	 * @param fileimport
	 * @return
	 * @throws NuclosBusinessRuleException
	 */
	public NuclosFileImportResult runImport(NuclosFileImport fileimport) throws NuclosBusinessRuleException {
		Map<String, Object> fields = new HashMap<String, Object>();
		fields.put("name", new GenericObjectDocumentFile(fileimport.getFile()));
		fields.put("mode", fileimport.getMode().getValue());
		fields.put("atomic", fileimport.getAtomic());
		fields.put("description", fileimport.getDescription());
		MasterDataVO importfile = new MasterDataVO(null, null, null, null, null, null, fields);

		List<EntityObjectVO> usages = new ArrayList<EntityObjectVO>();
		for (NuclosFileImportStructureUsage usage : fileimport.getStructures()) {
			String structure = usage.getStructure();
			CollectableSearchCondition condition = SearchConditionUtils.newMDComparison(MasterDataMetaCache.getInstance().getMetaData(NuclosEntity.IMPORT), "name", ComparisonOperator.EQUAL, structure);
			Collection<Object> structures = getMasterDataIds(NuclosEntity.IMPORT.getEntityName(), new CollectableSearchExpression(condition));
			if (structures != null && structures.size() > 0) {
				Map<String, Object> usagefields = new HashMap<String, Object>();
				usagefields.put("importId", structures.iterator().next());
				usagefields.put("import", structure);
				usagefields.put("order", usage.getOrder());
				usages.add(new EntityObjectVO());
			}
		}

		DependantMasterDataMap dependants = new DependantMasterDataMap(NuclosEntity.IMPORTUSAGE.getEntityName(), usages);
		MasterDataVO importfilevo;
        try {
	        importfilevo = ServiceLocator.getInstance().getFacade(ImportFacadeLocal.class).createFileImport(new MasterDataWithDependantsVO(importfile, dependants));
        }
        catch(CommonBusinessException e) {
        	throw new NuclosBusinessRuleException(e);
        }

        try {
        	Integer localeId = getLocaleFacade().getUserLocale().localeId;
        	String username = (String)getCurrentUser().getField("name");
	        getImportExecutionFacade().doImport(new ImportContext(importfilevo.getIntId(), null, localeId, username));
        }
        catch(NuclosFileImportException e) {
	        throw new NuclosBusinessRuleException(e);
        }

		return new NuclosFileImportResult();
	}

	private ImportFacadeLocal getImportExecutionFacade() {
		return ServiceLocator.getInstance().getFacade(ImportFacadeLocal.class);
	}

	/**
	 * write info message into protocol table (scheduled timelimit rules)
	 * @param sMessage
	 * @throws NuclosBusinessRuleException
	 * logging should work in business rules also
	 */
	public void logInfo(String sMessage) throws NuclosBusinessRuleException {
		this.getRuleInterface().logInfo(getSessionId(), sMessage, this.getCurrentRule().getName());
		logger.info(getSessionId() + " - " + sMessage + " - " + this.getCurrentRule().getName());
	}

	/**
	 * write warning message into protocol table (scheduled timelimit rules)
	 * @param sMessage
	 * @throws NuclosBusinessRuleException
	 * logging should work in business rules also
	 */
	public void logWarning(String sMessage) throws NuclosBusinessRuleException {
		this.getRuleInterface().logWarning(getSessionId(), sMessage, this.getCurrentRule().getName());
		logger.warn(getSessionId() + " - " + sMessage + " - " + this.getCurrentRule().getName());
	}

	/**
	 * write error message into protocol table (scheduled timelimit rules)
	 * @param sMessage
	 * @throws NuclosBusinessRuleException
	 * logging should work in business rules also
	 */
	public void logError(String sMessage) throws NuclosBusinessRuleException {
		this.getRuleInterface().logError(getSessionId(), sMessage, this.getCurrentRule().getName());
		logger.error(getSessionId() + " - " + sMessage + " - " + this.getCurrentRule().getName());
	}
} // class RuleInterface
