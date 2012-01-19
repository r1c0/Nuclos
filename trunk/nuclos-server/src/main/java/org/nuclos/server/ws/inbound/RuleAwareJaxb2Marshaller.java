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
package org.nuclos.server.ws.inbound;

import javax.xml.bind.JAXBContext;

import org.nuclos.server.customcode.codegenerator.RuleClassLoader;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

public class RuleAwareJaxb2Marshaller extends Jaxb2Marshaller implements InitializingBean {

    private RuleClassLoader cl;

    @Override
    protected synchronized JAXBContext getJaxbContext() {
        super.setBeanClassLoader(cl);
        return super.getJaxbContext();
    }

    public void setRuleClassLoader(RuleClassLoader cl) {
        this.cl = cl;
    }

    public RuleClassLoader getRuleClassLoader() {
        return cl;
    }
}
