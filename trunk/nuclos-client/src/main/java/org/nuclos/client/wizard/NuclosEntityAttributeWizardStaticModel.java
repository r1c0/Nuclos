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
package org.nuclos.client.wizard;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;

import org.nuclos.client.wizard.model.Attribute;
import org.nuclos.client.wizard.model.DataTyp;
import org.nuclos.client.wizard.steps.NuclosEntityAttributeRelationShipStep;
import org.nuclos.client.wizard.steps.NuclosEntityAttributeTranslationStep;
import org.nuclos.common.TranslationVO;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.common2.exception.CommonPermissionException;
import org.pietschy.wizard.WizardStep;
import org.pietschy.wizard.models.StaticModel;

/**
* <br>
* Created by Novabit Informationssysteme GmbH <br>
* Please visit <a href="http://www.novabit.de">www.novabit.de</a>
* 
* @author <a href="mailto:marc.finke@novabit.de">Marc Finke</a>
* @version 01.00.00
*/

public class NuclosEntityAttributeWizardStaticModel extends StaticModel {
	
	String name;
	String desc;
	Attribute attribute;
	int iAttributeCount;
	boolean blnValueListTyp;
	boolean blnRefenzTyp;
	
	
	boolean blnEditMode;
	
	List<TranslationVO> lstTranslation;
	

	public NuclosEntityAttributeWizardStaticModel(int attributeCount) throws CommonFinderException, CommonPermissionException {
		attribute = new Attribute();
		attribute.setDatatyp(DataTyp.getDefaultDataTyp());
		this.iAttributeCount = attributeCount;
		lstTranslation = new ArrayList<TranslationVO>();
	}
	
	public void setEditMode(boolean editable) {
		this.blnEditMode = editable;
	}
	
	public boolean isEditMode() {
		return blnEditMode;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
		this.attribute.setLabel(name);
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
		attribute.setDescription(desc);
	}
	
	public void setAttribute(Attribute attr) {
		this.attribute = attr;
	}

	public Attribute getAttribute() {
		return attribute;
	}
	
	public int getAttributeCount () {
		return iAttributeCount;
	}
			
	public boolean isValueListTyp() {
		return blnValueListTyp;
	}

	public void setValueListTyp(boolean blnValueListTyp) {
		this.blnValueListTyp = blnValueListTyp;
	}	
	
	public boolean isRefernzTyp() {
		return blnRefenzTyp;
	}

	public void setReferenzTyp(boolean blnReferenceTyp) {
		this.blnRefenzTyp = blnReferenceTyp;
	}	
	
	
	@Override
    public JComponent getOverviewComponent() {
      return new NuclosEntityWizardStaticModelOverview(this);
    }
	
	@Override
	public void previousStep() {
		WizardStep step = this.getActiveStep();
		if(step instanceof NuclosEntityAttributeTranslationStep && !this.isValueListTyp() && !this.isRefernzTyp()) {
			super.previousStep();
			super.previousStep();
			super.previousStep();
		}
		else if(step instanceof NuclosEntityAttributeTranslationStep && this.isValueListTyp() && !this.isRefernzTyp()) {
			super.previousStep();
			super.previousStep();
		}
		else if(step instanceof NuclosEntityAttributeRelationShipStep){
			super.previousStep();
			super.previousStep();
		}
		else {
			super.previousStep();
		}
   }
	
	public void setTranslation(List<TranslationVO> translation) {
		this.lstTranslation = translation;
	}
	
	public List<TranslationVO> getTranslation() {
		return lstTranslation;
	}

}
