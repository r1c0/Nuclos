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
package org.nuclos.server.customcode.ejb3;

import japa.parser.JavaParser;
import japa.parser.ParseException;
import japa.parser.ast.CompilationUnit;

import java.io.ByteArrayInputStream;

import javax.annotation.security.RolesAllowed;

import org.nuclos.common.NuclosEntity;
import org.nuclos.common2.StringUtils;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.exception.CommonValidationException;
import org.nuclos.server.common.RuleCache;
import org.nuclos.server.common.ejb3.NuclosFacadeBean;
import org.nuclos.server.customcode.codegenerator.NuclosJavaCompiler;
import org.nuclos.server.customcode.codegenerator.PlainCodeGenerator;
import org.nuclos.server.customcode.valueobject.CodeVO;
import org.nuclos.server.masterdata.MasterDataWrapper;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;
import org.nuclos.server.ruleengine.NuclosCompileException;

@RolesAllowed("Login")
public class CodeFacadeBean extends NuclosFacadeBean implements CodeFacadeRemote {

	@Override
	public MasterDataVO create(MasterDataVO vo) throws CommonBusinessException {
		checkWriteAllowed(NuclosEntity.CODE);

		CodeVO codevo = MasterDataWrapper.getCodeVO(vo);
		check(vo, codevo.isActive());

		getNameFromSource(codevo);
		codevo.validate();

		MasterDataVO mdvo = getMasterDataFacade().create(NuclosEntity.CODE.getEntityName(), MasterDataWrapper.wrapCodeVO(codevo), null);
		RuleCache.getInstance().invalidate();
		return mdvo;
	}

	@Override
	public MasterDataVO modify(MasterDataVO vo) throws CommonBusinessException {
		checkWriteAllowed(NuclosEntity.CODE);

		CodeVO codevo = MasterDataWrapper.getCodeVO(vo);
		check(vo, codevo.isActive());

		getNameFromSource(codevo);
		codevo.validate();

		Integer id = (Integer)getMasterDataFacade().modify(NuclosEntity.CODE.getEntityName(), MasterDataWrapper.wrapCodeVO(codevo), null);
		MasterDataVO mdVO = getMasterDataFacade().get(NuclosEntity.CODE.getEntityName(), id);
		RuleCache.getInstance().invalidate();
		return mdVO;
	}

	@Override
	public void remove(MasterDataVO vo) throws CommonBusinessException {
		checkDeleteAllowed(NuclosEntity.CODE);

		CodeVO cvo = MasterDataWrapper.getCodeVO(vo);
		if (cvo.isActive()) {
			NuclosJavaCompiler.check(new PlainCodeGenerator(cvo), true);
		}

		getMasterDataFacade().remove(NuclosEntity.CODE.getEntityName(), vo, false);
		RuleCache.getInstance().invalidate();
	}

	@Override
	public void check(MasterDataVO vo) throws CommonBusinessException {
		checkWriteAllowed(NuclosEntity.CODE);
		check(vo, false);
	}

	private void check(MasterDataVO vo, boolean active) throws CommonBusinessException {
		CodeVO codevo = MasterDataWrapper.getCodeVO(vo);
		getNameFromSource(codevo);
		NuclosJavaCompiler.check(new PlainCodeGenerator(codevo), !active);
	}

	private void getNameFromSource(CodeVO vo) throws CommonValidationException, NuclosCompileException {
		String fullName = "";
		CompilationUnit cu;
		try {
			cu = JavaParser.parse(new ByteArrayInputStream(vo.getSource().getBytes()));
			if (cu.getPackage() != null && cu.getPackage().getName() != null) {
				fullName = cu.getPackage().getName().toString() + ".";
			}
			if (cu.getTypes() != null && cu.getTypes().size() > 0) {
				fullName += cu.getTypes().get(0).getName();
			}
			else {
				if (StringUtils.isNullOrEmpty(fullName)) {
					throw new CommonValidationException("CodeFacadeBean.exception.extractname");
				}
				else {
					fullName += "package-info";
				}
			}
			vo.setName(fullName);
		}
		catch(ParseException e) {
			vo.setName("temp");

			// throw exception from compiler with more details
			NuclosJavaCompiler.compile();

			// if compiler does not throw an exception:
			throw new CommonValidationException("CodeFacadeBean.exception.extractname");
		}
	}
}
