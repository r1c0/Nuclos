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

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;

import org.apache.log4j.Logger;
import org.nuclos.client.common.NuclosCollectControllerFactory;
import org.nuclos.common.NuclosBusinessException;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.common2.exception.CommonPermissionException;
import org.nuclos.server.statemodel.valueobject.StateVO;

/**
 * @author Marc.Finke
 * the model holds the attributes for a Subprocess
 *
 */
public class SubProcessTransitionPanelModel implements Serializable {
	
	public static final int GENERATIONSTATEID_NOT_SET = -1;
	public static final int SUBPROCESSTRANSITIONID_NOT_SET = -2;
	public static final int GENERATIONID_NOT_SET = -3;
	public static final int RESULT_OKAY = 1;
	
	// one document for every subprocess attribute
	// below the methods to get/set the content of the model's
	public Document docName = new PlainDocument();
	public Document docStateModel = new PlainDocument();
	
	public Document docGeneratorState = new PlainDocument();
	
	private boolean loadingStates = false;
	
	// a comboboxmodel for the statemodel which represents a subprocess
	public final ComboBoxModel modelStates = new DefaultComboBoxModel();
	
	private Integer iSubProcessTransitionId;
	
	protected static final Logger log = Logger.getLogger(SubProcessTransitionPanelModel.class);

	public SubProcessTransitionPanelModel() {
		// TODO Auto-generated constructor stub		
	}
	
	private synchronized void loadStates(Integer stateModelId) {
		loadingStates = true;
		DefaultComboBoxModel defaultModel = (DefaultComboBoxModel)modelStates;
		defaultModel.removeAllElements();
		defaultModel.addElement("");
		Collection<StateVO> colStates = ProcessMonitorDelegate.getInstance().getStateByModelId(stateModelId);
		for (StateVO stateVO : colStates) {			
			TransitionSubProcess tsp = new TransitionSubProcess();
			tsp.setStateModelVO(stateVO);
			defaultModel.addElement(tsp);
		}
		loadingStates = false;
	}
	
	/*
	 * reinit the model
	 */
	public void clear() {
		try {
			docName.remove(0, docName.getLength());
			DefaultComboBoxModel defaultModel = (DefaultComboBoxModel)modelStates;
			defaultModel.removeAllElements();
			iSubProcessTransitionId = null;
		}
		catch (BadLocationException ex) {
			throw new NuclosFatalException(ex);
		}
	}
	
	public void setStateModelId(Integer iStateModelId) {
		try {
			this.docStateModel.remove(0, docStateModel.getLength());
			if (iStateModelId != null) {
				this.docStateModel.insertString(0, iStateModelId.toString(), null);
			}
			loadStates(iStateModelId);
		}
		catch (BadLocationException ex) {
			throw new NuclosFatalException(ex);
		}
	}
	
	public Integer getStateModelId() {
		Integer i = new Integer(0);
		
		String strValue = new String();
		
		try {
			strValue = docStateModel.getText(0, docStateModel.getLength());			
			i = new Integer(strValue);
		}
		catch (BadLocationException e) {
			// this should never happens
		} catch (NumberFormatException e) {
			log.warn("Die Durchlaufzeit ["+strValue+"] ist kein numerischer Wert.", e);
		}
		
		return i;
	}
		
	public void setGeneratorStateId(Integer iGeneratorStateId) {
		try {
			this.docGeneratorState.remove(0, docGeneratorState.getLength());
			if (iGeneratorStateId != null) {
				this.docGeneratorState.insertString(0, iGeneratorStateId.toString(), null);
				for (int i = 0; i < this.modelStates.getSize(); i++){
					Object obj = this.modelStates.getElementAt(i);
					if (obj instanceof TransitionSubProcess){
						TransitionSubProcess tsp = (TransitionSubProcess) obj;
						if (tsp.getStateModelVO().getId().equals(iGeneratorStateId)){
							this.modelStates.setSelectedItem(tsp);
						}
					}
				}
			} else {
				this.modelStates.setSelectedItem("");
			}
		}
		catch (BadLocationException ex) {
			throw new NuclosFatalException(ex);
		}
	}
	
	public Integer getGeneratorStateId() {
		Integer i = new Integer(0);
		
		String strValue = new String();
		
		try {
			strValue = docGeneratorState.getText(0, docGeneratorState.getLength());			
			i = new Integer(strValue);
		}
		catch (BadLocationException e) {
			// this should never happens
		} catch (NumberFormatException e) {
			log.warn("Der Status an dem ein Generator installiert werden soll ["+strValue+"] ist kein numerischer Wert.", e);
		}
		
		return i;
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

	public boolean isLoadingStates() {
		return loadingStates;
	}

	/**
	 * 
	 * @return RESULT_OKAY, GENERATIONSTATEID_NOT_SET, SUBPROCESSTRANSITIONID_NOT_SET
	 * @throws CommonBusinessException 
	 * @throws CommonFatalException 
	 * @throws CommonPermissionException 
	 * @throws NuclosBusinessException 
	 * 			
	 */
	public int openObjectGeneration() throws NuclosBusinessException, CommonPermissionException, CommonFatalException, CommonBusinessException {
		Integer generationStateId = getGeneratorStateId();
		if (generationStateId == null){
			return GENERATIONSTATEID_NOT_SET;
		}
		if (this.iSubProcessTransitionId == null){
			return SUBPROCESSTRANSITIONID_NOT_SET;
		}
		Integer iGenerationId = ProcessMonitorDelegate.getInstance().getGenerationIdFromSubProcessTransition(iSubProcessTransitionId);
		if (iGenerationId == null){
			return GENERATIONID_NOT_SET;
		}
		
		NuclosCollectControllerFactory.getInstance().newCollectController(NuclosEntity.GENERATION.getEntityName(), null)
				.runViewSingleCollectableWithId(iGenerationId);
		
		return RESULT_OKAY;
	}

	/**
	 * 
	 * @param id
	 */
	public void setSubProcessTransitionId(Integer id) {
		this.iSubProcessTransitionId = id;
	}
	

}
