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
/**
 * 
 */
package org.nuclos.client.processmonitor;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;

import org.apache.log4j.Logger;

import org.nuclos.client.statemodel.StateDelegate;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.server.processmonitor.valueobject.ProcessStateRuntimeFormatVO;
import org.nuclos.server.processmonitor.valueobject.SubProcessUsageCriteriaVO;
import org.nuclos.server.statemodel.valueobject.StateModelVO;

/**
 * @author Marc.Finke
 * the model holds the attributes for a Subprocess
 *
 */
public class SubProcessPanelModel implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	// one document for every subprocess attribute
	// below the methods to get/set the content of the model's
	public Document docSubProcessStateModel = new PlainDocument();
	public Document docName = new PlainDocument();
	public Document docSubProcessUsageCriteria = new PlainDocument();
	public Document docGuarantor = new PlainDocument();
	public Document docSecondGuarantor = new PlainDocument();
	public Document docDescription = new PlainDocument();
	public Document docSupervisor = new PlainDocument(); 
	public Document docOriginalSystem = new PlainDocument();
	public Document docPlanStartSeries = new PlainDocument();
	public Document docPlanEndSeries = new PlainDocument();
	public Document docRuntime = new PlainDocument();
	public Document docRuntimeFormat = new PlainDocument();
	
	// a comboboxmodel for the statemodel which represents a subprocess
	public final ComboBoxModel modelSubProcessStateModel = new DefaultComboBoxModel();
	public final ComboBoxModel modelSubProcessUsageCriteria = new DefaultComboBoxModel();
	
	public final ComboBoxModel modelRuntimeFormat = new DefaultComboBoxModel();
	
	protected static final Logger log = Logger.getLogger(SubProcessPanelModel.class);

	/**
	 * 
	 */
	public SubProcessPanelModel() {
		// TODO Auto-generated constructor stub
		getStateModelForComboBox();		
		getProcessStateRuntimeFormatForComboBox();
	}
	
	/*
	 * reinit the model
	 */
	public void clear() {
		try {
			docSubProcessStateModel.remove(0, docSubProcessStateModel.getLength());
			docName.remove(0, docName.getLength());
			docSubProcessUsageCriteria.remove(0, docSubProcessUsageCriteria.getLength());
			docDescription.remove(0, docDescription.getLength());
			docGuarantor.remove(0, docGuarantor.getLength());
			docSecondGuarantor.remove(0, docSecondGuarantor.getLength());
			docSupervisor.remove(0, docSupervisor.getLength());
			docPlanStartSeries.remove(0, docPlanStartSeries.getLength());
			docPlanEndSeries.remove(0, docPlanEndSeries.getLength());
			docRuntime.remove(0, docRuntime.getLength());
			docRuntimeFormat.remove(0, docRuntimeFormat.getLength());
		}
		catch (BadLocationException ex) {
			throw new NuclosFatalException(ex);
		}
	}
	
	/*
	 * @return the value object of the selected StateModel in the combobox
	 */
	public StateModelVO getStateModelVO() {
		Object o = modelSubProcessStateModel.getSelectedItem();
		if(o instanceof SubProcess) {
			SubProcess sub = (SubProcess)o;
			return sub.getStateModelVO();
		}
		else {
			return null;
		}		
	}
	
	/*
	 * fills the statemodel combobox with the available state models
	 */
	protected void getStateModelForComboBox() {
		((DefaultComboBoxModel)modelSubProcessStateModel).addElement("");
		Collection<StateModelVO> colStateModels = StateDelegate.getInstance().getAllStateModels();
		for(Iterator<StateModelVO> it = colStateModels.iterator(); it.hasNext(); ) {
			StateModelVO vo = it.next();
			SubProcess sub = new SubProcess(vo);
			((DefaultComboBoxModel)modelSubProcessStateModel).addElement(sub);
		}
	}
	
	/*
	 * selects the statemodel in the combox 
	 */
	protected void setStateModelComboBoxItem(Integer subId) {
		if (subId == null){
			modelSubProcessStateModel.setSelectedItem("");
			((DefaultComboBoxModel) modelSubProcessUsageCriteria).removeAllElements();
		} else {
			for(int i = 0; i < modelSubProcessStateModel.getSize(); i++) {
				Object obj = modelSubProcessStateModel.getElementAt(i);
				if(obj instanceof SubProcess) {
					SubProcess sub = (SubProcess)obj;
					if(sub.getStateModelVO().getId().equals(subId)) {
						modelSubProcessStateModel.setSelectedItem(sub);
						break;
					}
				}
			}
		}
	}
	
	/*
	 * @return the value object of the selected runtime format in the combobox
	 */
	public ProcessStateRuntimeFormatVO getProcessStateRuntimeFormatVO() {
		Object o = modelRuntimeFormat.getSelectedItem();
		if(o instanceof ProcessStateRuntimeFormatVO) {
			ProcessStateRuntimeFormatVO formatVO = (ProcessStateRuntimeFormatVO)o;
			return formatVO;
		}
		else {
			return null;
		}		
	}
	
	/*
	 * fills the runtime format combobox with possible values
	 */
	protected void getProcessStateRuntimeFormatForComboBox() {
		for (ProcessStateRuntimeFormatVO formatVO : ProcessMonitorDelegate.getInstance().getPossibleRuntimeFormats()){
			((DefaultComboBoxModel)modelRuntimeFormat).addElement(formatVO);
		}
	}
	
	/*
	 * selects the runtime format in the combox 
	 * not really used yet
	 */
	protected void setProcessStateRuntimeFormatComboBoxItem(Integer iValue) {
		for(int i = 0; i < modelRuntimeFormat.getSize(); i++) {
			Object obj = modelRuntimeFormat.getElementAt(i);
			if(obj instanceof ProcessStateRuntimeFormatVO) {
				ProcessStateRuntimeFormatVO formatVO = (ProcessStateRuntimeFormatVO)obj;
				if(formatVO.getValue().equals(iValue)) {
					modelRuntimeFormat.setSelectedItem(obj);
					break;
				}
			}
		}
	}
	
	/*
	 * @return the value object of the selected StateModelUsage in the combobox
	 */
	public SubProcessUsageCriteriaVO getSubProcessUsageCriteriaVO() {
		Object o = modelSubProcessUsageCriteria.getSelectedItem();
		if(o instanceof SubProcessUsageCriteriaVO) {
			return (SubProcessUsageCriteriaVO) o;
		}
		else {
			return null;
		}		
	}
	
	/*
	 * fills the statemodelusage combobox with the available state models usages
	 */
	protected void getSubProcessUsageCriteriaForComboBox() {
		((DefaultComboBoxModel)modelSubProcessUsageCriteria).removeAllElements();
		if (getStateModelVO() != null){
			for (SubProcessUsageCriteriaVO uc : ProcessMonitorDelegate.getInstance().getSubProcessUsageCriterias(getStateModelVO().getId())){
				((DefaultComboBoxModel)modelSubProcessUsageCriteria).addElement(uc);
			}
		}
	}
	
	/*
	 * selects the statemodelusage in the combox 
	 */
	protected void setSubProcessUsageCriteriaComboBoxItem(Integer ucId) {
		for(int i = 0; i < modelSubProcessUsageCriteria.getSize(); i++) {
			Object obj = modelSubProcessUsageCriteria.getElementAt(i);
			if(obj instanceof SubProcessUsageCriteriaVO) {
				SubProcessUsageCriteriaVO uc = (SubProcessUsageCriteriaVO)obj;
				if(uc.getId().equals(ucId)) {
					modelSubProcessUsageCriteria.setSelectedItem(uc);
					break;
				}
			}
		}
	}
	
	public Integer getSubProcessStateModel(){
		Integer i = new Integer(0);
		
		String strValue = new String();
		
		try {
			strValue = docSubProcessStateModel.getText(0, docSubProcessStateModel.getLength());			
			i = new Integer(strValue);
		}
		catch (BadLocationException e) {
			// this should never happens
		} catch (NumberFormatException e) {
			log.warn("Teil-Prozess -> unterliegende Statusmodell ID ["+strValue+"] ist kein numerischer Wert.", e);
		}
		
		return i;
	}
	
	public void setSubProcessStateModel(Integer subId){
		try {
			this.docSubProcessStateModel.remove(0, docSubProcessStateModel.getLength());
			this.docSubProcessStateModel.insertString(0, subId==null?null:subId.toString(), null);
			setStateModelComboBoxItem(subId);
		}
		catch (BadLocationException ex) {
			throw new NuclosFatalException(ex);
		}
	}
	
	public void setName(String strName) {
		try {
			docName.remove(0, docName.getLength());
			docName.insertString(0, strName, null);
		}
		catch (BadLocationException ex) {
			throw new NuclosFatalException(ex);
		}
	}
	
	public String getName() {
		String str = new String();		
		try {
			str = docName.getText(0, docName.getLength());
		} catch (BadLocationException e) {
			// this can't happend		
		} 
		return str;
	}
	
	public void setDescription(String strDesc) {
		try {
			docDescription.remove(0, docDescription.getLength());
			docDescription.insertString(0, strDesc, null);
		}
		catch (BadLocationException ex) {
			throw new NuclosFatalException(ex);
		}
	}
	
	public String getDescription() {
		String strValue = new String();
		try {
			strValue = docDescription.getText(0, docDescription.getLength());
		} catch (BadLocationException e) {
			// this should never happens		
		} 		
		return strValue;
	}
	
	public String getGuarantor() {
		String strValue = new String();
		try {
			strValue = docGuarantor.getText(0, docGuarantor.getLength());
		} catch (BadLocationException e) {
			// this should never happens		
		} 		
		return strValue;
	}
	
	public void setGuarantor(String strGuarantor) {
		try {
			docGuarantor.remove(0, docGuarantor.getLength());
			docGuarantor.insertString(0, strGuarantor, null);
		}
		catch (BadLocationException ex) {
			throw new NuclosFatalException(ex);
		}
	}
	
	public String getSecondGuarantor() {
		String strValue = new String();
		try {
			strValue = docSecondGuarantor.getText(0, docSecondGuarantor.getLength());
		} catch (BadLocationException e) {
			// this should never happens		
		} 		
		return strValue;
	}
	
	public void setSecondGuarantor(String strSecondGuarantor) {
		try {
			docSecondGuarantor.remove(0, docSecondGuarantor.getLength());
			docSecondGuarantor.insertString(0, strSecondGuarantor, null);
		}
		catch (BadLocationException ex) {
			throw new NuclosFatalException(ex);
		}
	}
	
	public String getSupervisor() {
		String strValue = new String();
		try {
			strValue = docSupervisor.getText(0, docSupervisor.getLength());
		} catch (BadLocationException e) {
			// this should never happens		
		} 		
		return strValue;
	}
	
	public void setSupervisor(String strSupervisor) {
		try {
			docSupervisor.remove(0, docSupervisor.getLength());
			docSupervisor.insertString(0, strSupervisor, null);
		}
		catch (BadLocationException ex) {
			throw new NuclosFatalException(ex);
		}
	}
	
	public String getOriginalSystem() {
		String strValue = new String();
		try {
			strValue = docOriginalSystem.getText(0, docOriginalSystem.getLength());
		} catch (BadLocationException e) {
			// this should never happens		
		} 		
		return strValue;
	}
	
	public void setOriginalSystem(String strOriginalSystem) {
		try {
			docOriginalSystem.remove(0, docOriginalSystem.getLength());
			docOriginalSystem.insertString(0, strOriginalSystem, null);
		}
		catch (BadLocationException ex) {
			throw new NuclosFatalException(ex);
		}
	}
	
	public void setRuntime(Integer iRuntime) {
		try {
			this.docRuntime.remove(0, docRuntime.getLength());
			if (iRuntime != null) {
				this.docRuntime.insertString(0, iRuntime.toString(), null);
			}
		}
		catch (BadLocationException ex) {
			throw new NuclosFatalException(ex);
		}
	}
	
	public Integer getRuntime() {
		Integer i = new Integer(0);
		
		String strValue = new String();
		
		try {
			strValue = docRuntime.getText(0, docRuntime.getLength());			
			i = new Integer(strValue);
		}
		catch (BadLocationException e) {
			// this should never happens
		} catch (NumberFormatException e) {
			log.warn("Die Durchlaufzeit ["+strValue+"] ist kein numerischer Wert.", e);
		}
		
		return i;
	}
	
	public void setRuntimeFormat(Integer iFormat) {
		try {
			this.docRuntimeFormat.remove(0, docRuntimeFormat.getLength());
			if (iFormat != null) {
				this.docRuntimeFormat.insertString(0, iFormat.toString(), null);
				setProcessStateRuntimeFormatComboBoxItem(iFormat);
			}
		}
		catch (BadLocationException ex) {
			throw new NuclosFatalException(ex);
		}
	}
	
	public Integer getRuntimeFormat() {
		Integer i = new Integer(0);
		
		String strValue = new String();
		
		try {
			strValue = docRuntimeFormat.getText(0, docRuntimeFormat.getLength());			
			i = new Integer(strValue);
		}
		catch (BadLocationException e) {
			// this should never happens
		} catch (NumberFormatException e) {
			log.warn("Das Durchlaufformat["+strValue+"] ist kein numerischer Wert.", e);
		}
		
		return i;
	}
	
	public Integer getSubProcessUsageCriteria(){
		Integer i = new Integer(0);
		
		String strValue = new String();
		
		try {
			strValue = docSubProcessUsageCriteria.getText(0, docSubProcessUsageCriteria.getLength());			
			i = new Integer(strValue);
		}
		catch (BadLocationException e) {
			// this should never happens
		} catch (NumberFormatException e) {
//			log.warn("Die Teil-Prozess Verwendung ["+strValue+"] ist kein numerischer Wert.", e);
		}
		
		return i;
	}
	
	public void setSubProcessUsageCriteria(Integer ucId){
		try {
			this.docSubProcessUsageCriteria.remove(0, docSubProcessUsageCriteria.getLength());
			if (ucId != null){
				this.docSubProcessUsageCriteria.insertString(0, ucId.toString(), null);
				setSubProcessUsageCriteriaComboBoxItem(ucId);
			}
		}
		catch (BadLocationException ex) {
			throw new NuclosFatalException(ex);
		}
	}

	public void setPlanStartSeries(String strSeries) {
		try {
			docPlanStartSeries.remove(0, docPlanStartSeries.getLength());
			docPlanStartSeries.insertString(0, strSeries, null);
		}
		catch (BadLocationException ex) {
			throw new NuclosFatalException(ex);
		}
	}
	
	public String getPlanStartSeries() {
		String strValue = new String();
		try {
			strValue = docPlanStartSeries.getText(0, docPlanStartSeries.getLength());
		} catch (BadLocationException e) {
			// this should never happens		
		} 		
		return strValue;
	}
	
	public void setPlanEndSeries(String strSeries) {
		try {
			docPlanEndSeries.remove(0, docPlanEndSeries.getLength());
			docPlanEndSeries.insertString(0, strSeries, null);
		}
		catch (BadLocationException ex) {
			throw new NuclosFatalException(ex);
		}
	}
	
	public String getPlanEndSeries() {
		String strValue = new String();
		try {
			strValue = docPlanEndSeries.getText(0, docPlanEndSeries.getLength());
		} catch (BadLocationException e) {
			// this should never happens		
		} 		
		return strValue;
	}

}
