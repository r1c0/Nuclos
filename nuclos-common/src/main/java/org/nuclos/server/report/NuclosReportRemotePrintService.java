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
package org.nuclos.server.report;

import java.io.Serializable;

import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.ServiceUIFactory;
import javax.print.attribute.Attribute;
import javax.print.attribute.AttributeSet;
import javax.print.attribute.HashAttributeSet;
import javax.print.attribute.HashPrintServiceAttributeSet;
import javax.print.attribute.PrintServiceAttribute;
import javax.print.attribute.PrintServiceAttributeSet;
import javax.print.event.PrintServiceAttributeListener;

import org.nuclos.common2.LangUtils;

/**
 *
 */
public class NuclosReportRemotePrintService implements PrintService, Serializable {

    private static final DocFlavor[] SUPPORTED_FLAVORS = new DocFlavor[]{DocFlavor.BYTE_ARRAY.PNG,
        DocFlavor.BYTE_ARRAY.GIF,
        DocFlavor.BYTE_ARRAY.JPEG,
        DocFlavor.SERVICE_FORMATTED.PAGEABLE,
        DocFlavor.SERVICE_FORMATTED.PRINTABLE,
        DocFlavor.SERVICE_FORMATTED.RENDERABLE_IMAGE,
        DocFlavor.INPUT_STREAM.PNG,
        DocFlavor.INPUT_STREAM.GIF,
        DocFlavor.INPUT_STREAM.JPEG,};
    
    private final String name;
    private final HashPrintServiceAttributeSet attributeSet = new HashPrintServiceAttributeSet();
    private DocFlavor[] supportedDocFlavors =SUPPORTED_FLAVORS;
    private Class<?>[] supportedAttributeCategories = new Class<?>[0];
 
    private final transient PrintService ps;
    public NuclosReportRemotePrintService(PrintService ps) {
    	name = ps.getName();
    	attributeSet.addAll(ps.getAttributes());
    	this.ps= ps;
    	supportedDocFlavors = ps.getSupportedDocFlavors();
    	supportedAttributeCategories = ps.getSupportedAttributeCategories();
    }

    public String getName() {
        return name;
    }
    
    @Override
    public boolean equals(Object obj) {
    	if (!(obj instanceof NuclosReportRemotePrintService))
    		return false;
    	return LangUtils.equals(getName(), ((NuclosReportRemotePrintService)obj).getName());
    }

    @Override
    public int hashCode() {
    	return getName().hashCode();
    }
    public DocPrintJob createPrintJob() {
    	if (ps != null)
    		return ps.createPrintJob();
    	else {
    		for (int i = 0; i < getSupportedDocFlavors().length; i++) {
        		PrintService[] printServices = lookupPrintServices(getSupportedDocFlavors()[i], getAttributes());
        		for (int j = 0; j < printServices.length; j++) {
    				if (printServices[j].getName().equals(getName()))
    					return printServices[j].createPrintJob();
    			}
			}
    	}
    	return null;
    }
    
	private PrintService[] lookupPrintServices(DocFlavor flavor, AttributeSet as) {
		PrintService prservDflt = PrintServiceLookup.lookupDefaultPrintService();
		PrintService[] prservices = PrintServiceLookup.lookupPrintServices(flavor, as);
		if (null == prservices || 0 >= prservices.length) {
			if (null != prservDflt) {
				prservices = new PrintService[] { prservDflt };
			}
		}
		return prservices;
	}


    public void addPrintServiceAttributeListener(PrintServiceAttributeListener pl) {
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    public void removePrintServiceAttributeListener(PrintServiceAttributeListener pl) {
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    public PrintServiceAttributeSet getAttributes() {
        return attributeSet;
    }

    public <T extends PrintServiceAttribute> T getAttribute(Class<T> type) {
        return null;
    }

    public DocFlavor[] getSupportedDocFlavors() {
        return supportedDocFlavors;
    }

    public boolean isDocFlavorSupported(DocFlavor df) {
        for (DocFlavor current : getSupportedDocFlavors()) {
            if (current.equals(df)) {
                return true;
            }
        }
        return false;
    }

    public Class<?>[] getSupportedAttributeCategories() {
        return supportedAttributeCategories;
    }

    public boolean isAttributeCategorySupported(Class<? extends Attribute> type) {
        return false;
    }

    public Object getDefaultAttributeValue(Class<? extends Attribute> type) {
        return null;
    }

    public Object getSupportedAttributeValues(Class<? extends Attribute> type, DocFlavor df, AttributeSet as) {
        return null;
    }

    public boolean isAttributeValueSupported(Attribute atrbt, DocFlavor df, AttributeSet as) {
        return false;
    }

    public AttributeSet getUnsupportedAttributes(DocFlavor df, AttributeSet as) {
        return new HashAttributeSet();
    }

    public ServiceUIFactory getServiceUIFactory() {
        return null;
    }

    @Override
    public String toString() {
        return getName();
    }
}
