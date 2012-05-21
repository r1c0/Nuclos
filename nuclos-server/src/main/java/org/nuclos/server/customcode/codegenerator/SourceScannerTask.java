//Copyright (C) 2012  Novabit Informationssysteme GmbH
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
package org.nuclos.server.customcode.codegenerator;

import java.io.BufferedReader;
import java.io.CharArrayWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common2.exception.CommonCreateException;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.common2.exception.CommonPermissionException;
import org.nuclos.common2.exception.CommonRemoveException;
import org.nuclos.common2.exception.CommonStaleVersionException;
import org.nuclos.common2.exception.CommonValidationException;
import org.nuclos.server.common.RuleCache;
import org.nuclos.server.customcode.ejb3.CodeFacadeLocal;
import org.nuclos.server.genericobject.valueobject.GenericObjectDocumentFile;
import org.nuclos.server.masterdata.ejb3.MasterDataFacadeLocal;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;
import org.nuclos.server.ruleengine.NuclosBusinessRuleException;
import org.nuclos.server.ruleengine.NuclosCompileException;
import org.nuclos.server.security.NuclosLocalServerSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

/**
 * @author Thomas Pasch
 */
@Configurable
class SourceScannerTask extends TimerTask {
	
	private static final Logger LOG = Logger.getLogger(SourceScannerTask.class);
	
	private static final Pattern PROP_PAT = Pattern.compile("^//\\s*(\\p{Alnum}+)=(.*)$");
	
	//
	
	private CodeFacadeLocal codeFacadeLocal;
	
	private MasterDataFacadeLocal masterDataFacade;
	
	private RuleCache ruleCache;
	
	private NuclosLocalServerSession nuclosLocalServerSession;
	
	SourceScannerTask() {
		LOG.info("Created scanner");
	}
	
	@Autowired
	final void setCodeFacadeLocal(CodeFacadeLocal codeFacadeLocal) {
		this.codeFacadeLocal = codeFacadeLocal;
	}

	@Autowired
	final void setMasterDataFacade(MasterDataFacadeLocal masterDataFacade) {
		this.masterDataFacade = masterDataFacade;
	}
	
	@Autowired
	final void setRuleCache(RuleCache ruleCache) {
		this.ruleCache = ruleCache;
	}
	
	@Autowired
	final void setNuclosLocalServerSession(NuclosLocalServerSession nuclosLocalServerSession) {
		this.nuclosLocalServerSession = nuclosLocalServerSession;
	}
	
	@Override
	public void run() {
		try {
			_run();
		}
		catch (Exception e) {
			LOG.warn("scanner failed: " + e.toString(), e);
		}
	}
	
	private void _run() throws NuclosCompileException {
		final long modified = NuclosJavaCompiler.getLastSrcWriteTime();
		final File srcDir = NuclosJavaCompiler.getSourceOutputPath();
		final File wsdlDir = NuclosJavaCompiler.getWsdlDir();
		
		if (!NuclosJavaCompiler.JARFILE.exists()) {
			NuclosJavaCompiler.compile();
		}
		
		// Find modified files on disk
		final List<File> javaSrc = new ArrayList<File>();
		scanDir(javaSrc, srcDir, modified, ".java");
		final List<File> wsdlSrc = new ArrayList<File>();
		scanDir(wsdlSrc, wsdlDir, modified, ".wsdl");
		
		if (javaSrc.isEmpty() && wsdlSrc.isEmpty()) {
			LOG.debug("No changes on disk");
			return;
		}
		// Try to compile and only proceed if there are no errors
		try {
			NuclosJavaCompiler.check();
		}
		catch (NuclosCompileException e) {
			LOG.info("Changes on disk but compile errors: " + e.toString());
			return;
		}
		
		// Parse files on disk (to get type and id)
		final List<GeneratedFile> java = new ArrayList<GeneratedFile>();
		for (File f: javaSrc) {
			try {
				java.add(parseFile(f));
			}
			catch (IOException e) {
				LOG.warn("Can't parse file " + f + ": " + e.toString(), e);
			}
		}
		final List<GeneratedFile> wsdl = new ArrayList<GeneratedFile>();
		for (File f: wsdlSrc) {
			try {
				wsdl.add(parseFile(f));
			}
			catch (IOException e) {
				LOG.warn("Can't parse file " + f + ": " + e.toString(), e);
			}
		}
		if (java.isEmpty() && wsdl.isEmpty()) {
			LOG.warn("Changes on disk, but unparsable");
			return;
		}
		
		// Change data in DB based on file changes on disk
		nuclosLocalServerSession.loginAsSuperUser();
		try {
			for (GeneratedFile gf: java) {
				try {
					if ("org.nuclos.server.ruleengine.valueobject.RuleVO".equals(gf.getType())) {
						updateRule(gf);
					}
					else if ("org.nuclos.server.customcode.valueobject.CodeVO".equals(gf.getType())) {
						updatePlainCode(gf);
					}
					else {
						LOG.warn("Don't know how to write object type " + gf.getType() + " to DB");
					}
				}
				catch (Exception e) {
					LOG.warn("Can't save modified file " + gf.getFile() + " to DB: " + e.toString(), e);
				}
			}
			for (GeneratedFile gf: wsdl) {
				LOG.warn("Modified file " + gf.getFile() + " from WSDL, but support for change is not implemented");
			}
		}
		finally {
			nuclosLocalServerSession.logout();
			// This is necessary to get fresh generators.
			ruleCache.invalidate();
			// Compile and write (anew) to disk
			NuclosJavaCompiler.compile();
		}
	}
	
	private void updateRule(GeneratedFile gf) throws CommonFinderException, CommonPermissionException, 
		NuclosBusinessRuleException, CommonCreateException, CommonRemoveException, 
		CommonStaleVersionException, CommonValidationException {
		
		final String entity;
		if (gf.getFile().getName().startsWith("Rule_")) {
			entity = NuclosEntity.RULE.getEntityName();
		}
		else {
			entity = NuclosEntity.TIMELIMITRULE.getEntityName();
		}
		
		final MasterDataVO vo = getRuleAsMd(entity, gf.getId());
		// used in time limit rules
		if (vo.getField("name") == null) {
			vo.setField("name", gf.getName());
		}
		// used in 'normal' rules
		if (vo.getField("rule") == null) {
			vo.setField("rule", gf.getName());
		}
		
		if (!gf.getName().equals((String) vo.getField("rule"))) {
			throw new IllegalStateException();
		}
		
		vo.setField("version", Integer.valueOf(gf.getVersion()));
		vo.setField("source", new String(gf.getContent()));
		masterDataFacade.modify(entity, vo, null);
		
		LOG.info("Update rule in db: name=" + gf.getName() + " id=" + gf.getId() + " from " + gf.getFile());
	}
	
	private void updatePlainCode(GeneratedFile gf) throws CommonFinderException, CommonPermissionException, 
		NuclosBusinessRuleException, CommonCreateException, CommonRemoveException, 
		CommonStaleVersionException, CommonValidationException {
		
		final MasterDataVO vo = getCodeAsMd(gf.getId());
		if (!gf.getName().equals((String) vo.getField("name"))) {
			throw new IllegalStateException();
		}
		vo.setField("version", Integer.valueOf(gf.getVersion()));
		vo.setField("source", new String(gf.getContent()));
		masterDataFacade.modify(NuclosEntity.CODE.getEntityName(), vo, null);
		
		LOG.info("Update code in db: name=" + gf.getName() + " id=" + gf.getId() + " from " + gf.getFile());
	}

	private void updateWsdl(GeneratedFile gf) throws CommonFinderException, CommonPermissionException, 
		NuclosBusinessRuleException, CommonCreateException, CommonRemoveException, 
		CommonStaleVersionException, CommonValidationException {
	
		final MasterDataVO vo = getWsdlAsMd(gf.getId());
		if (!gf.getName().equals((String) vo.getField("name"))) {
			throw new IllegalStateException();
		}
		vo.setField("version", Integer.valueOf(gf.getVersion()));
		
		final GenericObjectDocumentFile oldDoc = vo.getField("wsdl", GenericObjectDocumentFile.class);
		final GenericObjectDocumentFile newDoc = new GenericObjectDocumentFile(
				oldDoc.getFilename(), oldDoc.getDocumentFileId(), new String(gf.getContent()).getBytes());
		
		vo.setField("source", new String(gf.getContent()));
		masterDataFacade.modify(NuclosEntity.CODE.getEntityName(), vo, null);
		
		LOG.info("Update wsdl generated java in db: name=" + gf.getName() + " id=" + gf.getId() + " from " + gf.getFile());
	}

	private MasterDataVO getCodeAsMd(long id) throws CommonFinderException, CommonPermissionException {
		return masterDataFacade.get(NuclosEntity.CODE.getEntityName(), Integer.valueOf((int) id));
	}
	
	private MasterDataVO getRuleAsMd(String entity, long id) throws CommonFinderException, CommonPermissionException {
		return masterDataFacade.get(entity, Integer.valueOf((int) id));
	}
	
	private MasterDataVO getWsdlAsMd(long id) throws CommonFinderException, CommonPermissionException {
		return masterDataFacade.get(NuclosEntity.WEBSERVICE.getEntityName(), Integer.valueOf((int) id));
	}
	
	private void scanDir(List<File> result, File dir, long modified, String ext) {
		final File[] files = dir.listFiles();
		for (File f: files) {
			if (f.isDirectory()) {
				scanDir(result, f, modified, ext);
			}
			else if (f.isFile()) {
				if (f.getName().endsWith(ext) && f.lastModified() > modified) {
					result.add(f);
				}
			}
		}
	}
	
	private GeneratedFile parseFile(File file) throws IOException {
		final GeneratedFile result = new GeneratedFile();
		result.setFile(file);
		final BufferedReader reader = new BufferedReader(new InputStreamReader(
				new FileInputStream(file), NuclosJavaCompiler.ENCODING));
		try {
			boolean prefixEnd = false;
			String line;
			while ((line = reader.readLine()) != null) {
				line = line.trim();
				if (line.equals("// END")) {
					prefixEnd = true;
					break;
				}
				final Matcher m = PROP_PAT.matcher(line);
				if (m.matches()) {
					final String key = m.group(1);
					final String value = m.group(2);
					if ("name".equals(key)) {
						result.setName(value);
					}
					else if ("type".equals(key)) {
						result.setType(value);
					}
					else if ("class".equals(key)) {
						result.setGeneratorClass(value);
					}
					else if ("id".equals(key)) {
						result.setId(Long.parseLong(value));
					}
					else if ("version".equals(key)) {
						result.setVersion(Integer.parseInt(value));
					}
					else if ("modified".equals(key)) {
						result.setModified(Long.parseLong(value));
					}
				}
			}
			if (!prefixEnd) {
				throw new IllegalStateException("Parse rule: Can't find prefix end in " + file);
			}
			final CharArrayWriter out = new CharArrayWriter();
			try {
				if ("org.nuclos.server.ruleengine.valueobject.RuleVO".equals(result.getType())) {
					copyRule(reader, out);
				}
				else {
					copy(reader, out);
				}
			}
			finally {
				out.close();
			}
			final char[] content = out.toCharArray();
			if (content.length <= 0) {
				throw new IllegalStateException();
			}
			result.setContent(content);
		}
		finally {
			reader.close();
		}
		return result;
	}
	
	private void copy(Reader r, Writer w) throws IOException {
		final char[] buffer = new char[4092];
		int size;
		while ((size = r.read(buffer)) >= 0) {
			w.write(buffer, 0, size);
		}
	}
	
	private void copyRule(BufferedReader r, Writer w) throws IOException {
		String line;
		boolean begin = false;
		// only copy the 'rule' part of the source
		while ((line = r.readLine()) != null) {
			if (line.trim().equals("// BEGIN RULE")) {
				begin = true;
				break;
			}
		}
		boolean end = false;
		while ((line = r.readLine()) != null) {
			if (line.trim().equals("// END RULE")) {
				end = true;
				break;
			}
			w.write(line, 0, line.length());
			w.write("\n");
		}
		if (!(begin && end)) {
			throw new IllegalStateException("Parse rule: Can't find begin and end");
		}		
	}
	
}
