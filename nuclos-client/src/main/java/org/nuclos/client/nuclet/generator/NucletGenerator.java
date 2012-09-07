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
package org.nuclos.client.nuclet.generator;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.nuclos.client.nuclet.generator.content.AbstractNucletContentGenerator;
import org.nuclos.client.nuclet.generator.content.EntityFieldGroupNucletContentGenerator;
import org.nuclos.client.nuclet.generator.content.EntityFieldNucletContentGenerator;
import org.nuclos.client.nuclet.generator.content.EntityNucletContentGenerator;
import org.nuclos.client.ui.Errors;
import org.nuclos.common.ApplicationProperties;
import org.nuclos.common.NuclosBusinessException;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.SpringApplicationContextHolder;
import org.nuclos.common.dal.vo.EntityObjectVO;
import org.nuclos.common.dbtransfer.NucletContentUID;
import org.nuclos.common.dbtransfer.TransferConstants;
import org.nuclos.common.dbtransfer.TransferOption;
import org.nuclos.common.dbtransfer.ZipOutput;
import org.nuclos.common2.InternalTimestamp;
import org.nuclos.common2.LocaleInfo;
import org.nuclos.common2.SpringLocaleDelegate;
import org.nuclos.common2.StringUtils;
import org.nuclos.server.common.ejb3.LocaleFacadeRemote;
import org.nuclos.server.dbtransfer.TransferFacadeRemote;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

public class NucletGenerator implements TransferConstants {
	
	private static final Logger LOG = Logger.getLogger(NucletGenerator.class);
	
	public static final String XLSX_FILE_VERSION = "1.0";
	
	public static final String XLSX_FILE = "/org/nuclos/client/nuclet/generator/NucletGeneration.xlsx";
	
	public static final String XLSX_FILE_SHEET_VERSION = "_(Version)_";
	
	// former Spring injection
	
	private TransferFacadeRemote transferFacade;
	
	private LocaleFacadeRemote localeFacade;
	
	private SpringLocaleDelegate localeDelegate;
	
	// end of former Spring injection
	
	private Collection<LocaleInfo> locales;
	
	private long id;
	
	private XSSFWorkbook workbook;
	
	private Map<LocaleInfo, Map<String, String>> localeResources;
	
	private EntityNucletContentGenerator genEntity;
	
	private EntityFieldNucletContentGenerator genEntityField;
	
	private EntityFieldGroupNucletContentGenerator genEntityFieldGroup;
	
	private NucletGeneratorLayoutMLFactory layoutMLFactory;
	
	private List<EntityObjectVO> layouts;
	
	private List<EntityObjectVO> layoutUsages;
	
	private final long nulcetId = 1l;
	
	private String path;
	
	private String nucletFileName;
	
	private String nucletName;
	
	private EntityObjectVO nucletVO;
	
	private List<EntityObjectVO> nucletContentUids;
	
	public NucletGenerator() {
		setTransferFacadeRemote(SpringApplicationContextHolder.getBean(TransferFacadeRemote.class));
		setLocaleFacadeRemote(SpringApplicationContextHolder.getBean(LocaleFacadeRemote.class));
		setSpringLocaleDelegate(SpringApplicationContextHolder.getBean(SpringLocaleDelegate.class));
		init();
	}
	
	final void setTransferFacadeRemote(TransferFacadeRemote transferFacade) {
		this.transferFacade = transferFacade;
	}
	
	final TransferFacadeRemote getTransferFacadeRemote() {
		return transferFacade;
	}
	
	final void setLocaleFacadeRemote(LocaleFacadeRemote localeFacade) {
		this.localeFacade = localeFacade;
	}
	
	final LocaleFacadeRemote getLocaleFacadeRemote() {
		return localeFacade;
	}
	
	final void setSpringLocaleDelegate(SpringLocaleDelegate localeDelegate) {
		this.localeDelegate = localeDelegate;
	}
	
	final SpringLocaleDelegate getSpringLocaleDelegate() {
		return localeDelegate;
	}
	
	void init() {
		locales = getLocaleFacadeRemote().getAllLocales(false);
		localeResources = new HashMap<LocaleInfo, Map<String, String>>();
	}
	
	/**
	 * use File Chooser
	 */
	public void createEmptyXLSXFile() {
		final JFileChooser filechooser = new JFileChooser();
		final FileFilter filefilter = new FileFilter() {
			@Override
			public boolean accept(File file) {
				return file.isDirectory() || file.getName().toLowerCase().endsWith("xlsx");
			}
			@Override
			public String getDescription() {
				return "Microsoft Excel (xlsx)";
			}
		};
		filechooser.addChoosableFileFilter(filefilter);
		filechooser.setFileFilter(filefilter);
		
		final int iBtn = filechooser.showSaveDialog(null);

		if (iBtn == JFileChooser.APPROVE_OPTION) {
			final File file = filechooser.getSelectedFile();
			if (file != null) {
				String xlsxFile = file.getAbsolutePath();
				if (!xlsxFile.toLowerCase().endsWith("xlsx")) {
					xlsxFile = xlsxFile + ".xlsx";
				}
				createEmptyXLSXFile(xlsxFile);
			}
		}
	}
	
	/**
	 * 
	 * @param xlsxFile
	 */
	public void createEmptyXLSXFile(String xlsxFile) {
		try {
			InputStream is = NucletGenerator.class.getResourceAsStream(XLSX_FILE);
	        ByteArrayOutputStream bos = new ByteArrayOutputStream();

	        int c = 0;
	        while ((c = is.read()) != -1) {
	            bos.write((char) c);
	        }
	        
	        FileOutputStream fos = new FileOutputStream(xlsxFile);
		    fos.write(bos.toByteArray());
		    fos.close();
	        
		    info("Empty XLSX file created: " + xlsxFile);
		    
		} catch (FileNotFoundException e) {
			LOG.error(e.getMessage(), e);
			Errors.getInstance().showExceptionDialog(null, e);
		} catch (IOException e) {
			LOG.error(e.getMessage(), e);
			Errors.getInstance().showExceptionDialog(null, e);
		}
	}
	
	/**
	 * use File Chooser
	 */
	public void generateNucletFromXLSX() {
		info("Nuclet generation started...");
		
		final JFileChooser filechooser = new JFileChooser();
		final FileFilter filefilter = new FileFilter() {
			@Override
			public boolean accept(File file) {
				return file.isDirectory() || file.getName().toLowerCase().endsWith("xlsx");
			}
			@Override
			public String getDescription() {
				return "Microsoft Excel (xlsx)";
			}
		};
		filechooser.addChoosableFileFilter(filefilter);
		filechooser.setFileFilter(filefilter);
		
		final int iBtn = filechooser.showOpenDialog(null);

		if (iBtn == JFileChooser.APPROVE_OPTION) {
			final File file = filechooser.getSelectedFile();
			if (file != null) {
				try {
					path = file.getAbsolutePath().substring(0, file.getAbsolutePath().lastIndexOf(File.separator));
					nucletName = file.getName();
					nucletName = nucletName.substring(0, nucletName.lastIndexOf("."));
					nucletFileName = nucletName + NUCLET_FILE_EXTENSION;
					generateNucletFromXLSX(new FileInputStream(file));
				} catch (FileNotFoundException e) {
					LOG.error(e.getMessage(), e);
					Errors.getInstance().showExceptionDialog(null, e);
				}
			}
		}
	}
	
	
	/**
	 * 
	 * @param xlsxFile
	 */
	public void generateNucletFromXLSX(String xlsxFile) {
		info("Nuclet generation started...");
		
		try {
			path = xlsxFile.substring(0, xlsxFile.lastIndexOf(File.separator));
			nucletName = xlsxFile.substring(xlsxFile.lastIndexOf(File.separator)+1, xlsxFile.length());
			nucletName = nucletName.substring(0, nucletName.lastIndexOf("."));
			nucletFileName = nucletName + NUCLET_FILE_EXTENSION;
			generateNucletFromXLSX(new FileInputStream(xlsxFile));
		} catch (FileNotFoundException e) {
			LOG.error(e.getMessage(), e);
			Errors.getInstance().showExceptionDialog(null, e);
		}
	}
	
	
	/**
	 * 
	 * @param xlsxFile
	 */
	private void generateNucletFromXLSX(InputStream xlsxFile) {
		try { 
			workbook = new XSSFWorkbook(xlsxFile);
			
			checkFileVersion();
			
			// step 1. Entities
			genEntity = new EntityNucletContentGenerator(this);
			genEntity.generateEntityObjects();
			// step 2. Entity field groups
			genEntityFieldGroup = new EntityFieldGroupNucletContentGenerator(this);
			genEntityFieldGroup.generateEntityObjects();
			// step 3. Entity fields
			genEntityField = new EntityFieldNucletContentGenerator(this, genEntity, genEntityFieldGroup);
			genEntityField.generateEntityObjects();
			
			// step 4. Layouts
			layoutMLFactory = new NucletGeneratorLayoutMLFactory(this);
			layouts = generateLayouts();
			layoutUsages = generateLayoutUsages();
			
			// step 5. Nuclet
			nucletVO = createNucletEntityObjectVO();
			
			// step 6. UIDs
			nucletContentUids = generateUIDs();
			
			// step 7. generate File
			byte[] bytes = generateNucletFile();
			
			// step 8. write File
			String nucletFile = path + File.separator + nucletFileName;
			FileOutputStream fos = new FileOutputStream(nucletFile);
		    fos.write(bytes);
		    fos.close();
		    
		    info("Nuclet generated: " + nucletFile);
						
		} catch (IOException e) {
			LOG.error(e.getMessage(), e);
			Errors.getInstance().showExceptionDialog(null, e);
		} catch (NuclosBusinessException e) {
			LOG.error(e.getMessage(), e);
			Errors.getInstance().showExceptionDialog(null, e);
		} 
	}
	
	private byte[] generateNucletFile() {
		ByteArrayOutputStream bout = new ByteArrayOutputStream(16348);
		ZipOutput zout = new ZipOutput(bout);
		
		addToZip(zout, genEntity);
		addToZip(zout, genEntityFieldGroup);
		addToZip(zout, genEntityField);
		addToZip(zout, NuclosEntity.LAYOUT, layouts);
		addToZip(zout, NuclosEntity.LAYOUTUSAGE, layoutUsages);
		addToZip(zout, NuclosEntity.NUCLET, nucletVO);
		addToZip(zout, UID, nucletContentUids);
		
		TransferOption.Map exportOptions = new TransferOption.HashMap();
		exportOptions.put(TransferOption.IS_NUCLON_IMPORT_ALLOWED, null);
		String root = getTransferFacadeRemote().createMetaDataRoot(
				TRANSFER_VERSION,
				(new NucletContentUID(nucletVO)).uid,
				nucletName,
				ApplicationProperties.getInstance().getNuclosVersion(),
				"GENERIC", // Database Type
				new Date(),
				exportOptions);
		
		zout.addEntry(ROOT_ENTRY_NAME, root);

		zout.close();
		return bout.toByteArray();
	}
	
	private void addToZip(ZipOutput zout, AbstractNucletContentGenerator genContent) {
		addToZip(zout, genContent.getEntity(), genContent.getResult());
	}
	
	private void addToZip(ZipOutput zout, NuclosEntity entity, EntityObjectVO object) {
		List<EntityObjectVO> objects = new ArrayList<EntityObjectVO>();
		objects.add(object);
		addToZip(zout, entity, objects);
	}
	
	private void addToZip(ZipOutput zout, NuclosEntity entity, List<EntityObjectVO> objects) {
		addToZip(zout, entity.getEntityName()+TABLE_ENTRY_SUFFIX, objects);
	}
	
	private void addToZip(ZipOutput zout, String name, List<EntityObjectVO> objects) {
		zout.addEntry(name, toXML(objects));
	}
	
	private List<EntityObjectVO> generateLayouts() {
		List<EntityObjectVO> result = new ArrayList<EntityObjectVO>();
		for (EntityObjectVO eoEntity : genEntity.getResult()) {
			final String entity = eoEntity.getField("entity", String.class);
			try {
				final String layoutML = layoutMLFactory.generateLayout(entity, false, false, true);
				final EntityObjectVO eo = new EntityObjectVO();
				eo.initFields(4, 0);
				eo.setEntity(NuclosEntity.LAYOUT.getEntityName());
				eo.setId(getNextId());
				eo.getFields().put("entityName", entity); // for usages
				eo.getFields().put("name", entity);
				eo.getFields().put("description", entity);
				eo.getFields().put("layoutML", layoutML);
				setMetaFields(eo);
				
				result.add(eo);
				
			} catch (Exception e) {
				LOG.error(e.getMessage(), e);
				error(String.format("During layout generation for entity \"%s\" an error has occurred: %s", entity, e.getMessage()));
			}
		}
		return result;
	}
	
	private List<EntityObjectVO> generateLayoutUsages() {
		List<EntityObjectVO> result = new ArrayList<EntityObjectVO>();
		for (EntityObjectVO eoLayout : layouts) {
			for (int i = 0; i < 2; i++) {
				final EntityObjectVO eo = new EntityObjectVO();
				eo.initFields(4, 0);
				eo.setEntity(NuclosEntity.LAYOUTUSAGE.getEntityName());
				eo.setId(getNextId());
				eo.getFields().put("entity", eoLayout.getField("entityName", String.class));
				eo.getFields().put("searchScreen", i==0);
				eo.getFieldIds().put("layout", eoLayout.getId());
				eo.getFields().put("layout", eoLayout.getField("name", String.class));				
				setMetaFields(eo);
				
				result.add(eo);
			}
		}
		return result;
	}
	
	private List<EntityObjectVO> generateUIDs() {
		final List<EntityObjectVO> result = new ArrayList<EntityObjectVO>();
		result.addAll(generateUIDs(NuclosEntity.NUCLET, Collections.singletonList(nucletVO)));
		result.addAll(generateUIDs(genEntity));
		result.addAll(generateUIDs(genEntityFieldGroup));
		result.addAll(generateUIDs(genEntityField));
		result.addAll(generateUIDs(NuclosEntity.LAYOUT, layouts));
		result.addAll(generateUIDs(NuclosEntity.LAYOUTUSAGE, layoutUsages));
		return result;
	}
	
	private List<EntityObjectVO> generateUIDs(AbstractNucletContentGenerator genContent) {
		return generateUIDs(genContent.getEntity(), genContent.getResult());
	}
	
	private List<EntityObjectVO> generateUIDs(NuclosEntity entity, List<EntityObjectVO> contents) {
		final List<EntityObjectVO> result = new ArrayList<EntityObjectVO>(contents.size());
		for (EntityObjectVO content : contents) {
			final EntityObjectVO uidObject = new EntityObjectVO();
			final NucletContentUID uid = new NucletContentUID(content);
			uidObject.initFields(4, 0);
			uidObject.setEntity(NuclosEntity.NUCLETCONTENTUID.getEntityName());
			uidObject.setId(getNextId());
			uidObject.getFields().put("uid", uid.uid);
			uidObject.getFields().put("nuclosentity", entity.getEntityName());
			uidObject.getFields().put("objectid", content.getId());
			uidObject.getFields().put("objectversion", uid.version);
			setMetaFields(uidObject);
			
			result.add(uidObject);
		}
		return result;
	}
	
	private EntityObjectVO createNucletEntityObjectVO() {
		final EntityObjectVO result = new EntityObjectVO();
		result.initFields(2, 0);
		result.setEntity(NuclosEntity.NUCLET.getEntityName());
		result.setId(nulcetId);
		result.getFields().put("name", nucletName);
		setMetaFields(result);
		
		return result;
	}
	
	protected List<EntityObjectVO> getEntities() {
		return genEntity.getResult();
	}
	
	protected List<EntityObjectVO> getEntityFields() {
		return genEntityField.getResult();
	}
	
	protected List<EntityObjectVO> getEntityFieldGroups() {
		return genEntityFieldGroup.getResult();
	}
	
	private void checkFileVersion() throws NuclosBusinessException {
		final XSSFSheet sheet = workbook.getSheet(XLSX_FILE_SHEET_VERSION);
		for (Row row : sheet) {
			switch (row.getRowNum()) {
				case 0:
					continue; // header row
				case 1:
					String value = row.getCell(0).getStringCellValue();
					if (!StringUtils.equalsIgnoreCase(value, XLSX_FILE_VERSION)) {
						throw new NuclosBusinessException(
								String.format("Wrong File Version \"%s\")! Current Nuclos Generation Version is \"%s\".",
										value, XLSX_FILE_VERSION));
					}
				default:
					return;
			}
		}
	}
	
	private static String toXML(Object o) {
		XStream xstream = new XStream(new DomDriver("UTF-8"));
		return xstream.toXML(o);
	}
	
	protected String getResourceText(String resourceId) {
		return localeResources.get(getSpringLocaleDelegate().getUserLocaleInfo()).get(resourceId);
	}
	
	public void setMetaFields(EntityObjectVO eo) {
		eo.setCreatedBy("Nuclet Generator");
		eo.setChangedBy("Nuclet Generator");
		eo.setCreatedAt(new InternalTimestamp(System.currentTimeMillis()));
		eo.setChangedAt(new InternalTimestamp(System.currentTimeMillis()));
		eo.setVersion(1);
	}
	
	public void error(String error) {
		System.err.println(error);
	}
	
	public void warning(String warning) {
		System.out.println(warning);
	}
	
	public void info(String info) {
		System.out.println(info);
	}
	
	public XSSFWorkbook getWorkbook() {
		return workbook;
	}
	
	public long getNextId() {
		return id++;
	}
	
	public long getNucletId() {
		return nulcetId;
	}
	
	public Collection<LocaleInfo> getLocales() {
		return locales;
	}
	
	public void addLocaleResource(LocaleInfo locale, String resourceId, String text) {
		if (!localeResources.containsKey(locale)) {
			localeResources.put(locale, new HashMap<String, String>());
		}
		localeResources.get(locale).put(resourceId, text);			
	}
	
	
}
