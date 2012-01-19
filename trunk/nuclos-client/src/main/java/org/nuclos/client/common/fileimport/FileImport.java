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
package org.nuclos.client.common.fileimport;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.nuclos.common.collect.collectable.searchcondition.CollectableSearchCondition;
import org.nuclos.common.collect.collectable.searchcondition.ComparisonOperator;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.fileimport.CommonParseException;
import org.nuclos.client.masterdata.MasterDataDelegate;
import org.nuclos.client.ui.CommonInterruptibleProcess;
import org.nuclos.common.NuclosBusinessException;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.SearchConditionUtils;
import org.nuclos.server.masterdata.valueobject.MasterDataMetaVO;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;

/**
 * Class for handling file imports.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:ramin.goettlich@novabit.de">ramin.goettlich</a>
 * @author	<a href="mailto:rostislav.maksymovskyi@novabit.de">rostislav.maksymovskyi</a>
 * @version 02.00.00
 */
public class FileImport implements CommonInterruptibleProcess {
	private File file;
	private String sEntity;
	private List<MasterDataVO> lstmdvoRecords;
	private FileImportStructure importStructure;
	private MasterDataMetaVO mdmetavo;
	private int iCountCreated;
	private int iCountUpdated;
	private int iCountError;
	private List<String> lstErrorMessages;

	public FileImport(File file, String sEntity, FileImportStructure importStructure) throws CommonParseException, InterruptedException {
		this.file = file;
		this.sEntity = sEntity;
		this.lstmdvoRecords = new java.util.ArrayList<MasterDataVO>();
		this.importStructure = importStructure;
		this.mdmetavo = MasterDataDelegate.getInstance().getMetaData(this.sEntity);
//		this.iCountCreated = 0;
//		this.iCountUpdated = 0;
//		this.iCountError = 0;
//		this.lstErrorMessages = new ArrayList<String>();

		this.read();
		this.write();
	}

	/* (non-Javadoc)
	 * @see org.nuclos.client.common.fileimport.NovabitInterruptibleProcess#setBackgroundProcessInterruptionIntervalForCurrentThread()
	 */
	@Override
	public void setBackgroundProcessInterruptionIntervalForCurrentThread() throws InterruptedException {
		Thread.sleep(GENERAL_INTERRUPTION_INTERVAL);
	}

	private void read() throws CommonParseException, InterruptedException {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			for (int i = 0; i < importStructure.getHeaderlines(); i++) {
				reader.readLine();
			}
			while (reader.ready()) {
				int iColumn = 0;
				String sLine = reader.readLine();
				final Map<String, Object> mpRecord = CollectionUtils.newHashMap();
				while (sLine.length() > 0) {
					iColumn++;
					String sItem;
					if (importStructure.getQualifier() != null && sLine.startsWith(importStructure.getQualifier())) {
						int nextDelimiter = sLine.indexOf(importStructure.getQualifier() + importStructure.getDelimiter());
						if (nextDelimiter < 0) {
							sItem = sLine.substring(importStructure.getQualifier().length(), sLine.length() - importStructure.getDelimiter().length());
							sLine = "";
						}
						else {
							sItem = sLine.substring(importStructure.getQualifier().length(), nextDelimiter);
							sLine = sLine.substring(nextDelimiter + importStructure.getQualifier().length() + importStructure.getDelimiter().length());
						}
					}
					else {
						int nextDelimiter = sLine.indexOf(importStructure.getDelimiter());
						if (nextDelimiter < 0) {
							sItem = sLine;
							sLine = "";
						}
						else {
							sItem = sLine.substring(0, nextDelimiter);
							sLine = sLine.substring(nextDelimiter + importStructure.getDelimiter().length());
						}
					}
					final FileImportStructureItem importStructureItem = importStructure.getItems().get(iColumn);
					if (importStructureItem != null) {
						mpRecord.put(importStructureItem.getFieldName(), importStructureItem.parse(sItem));
					}
				}
				mpRecord.get(importStructure.getKeyfield());
				if (true) {
					final MasterDataVO mdvo = new MasterDataVO(mdmetavo, true);
					mdvo.setFields(mpRecord);
					lstmdvoRecords.add(mdvo);
				}
			}
		}
		catch (IOException ex) {
			throw new NuclosFatalException(ex.getMessage(), ex);
		}
		setBackgroundProcessInterruptionIntervalForCurrentThread();
	}

	private void write() throws InterruptedException {
		iCountCreated = 0;
		iCountUpdated = 0;
		iCountError = 0;
		this.lstErrorMessages = new ArrayList<String>();
		for (MasterDataVO mdvoRecord : this.lstmdvoRecords) {
			try {
				// check if a record with the same key already exists
				final String sFieldName = importStructure.getKeyfield();
				Collection<MasterDataVO> collmdvoRecordExisting = null;
				if (!(sFieldName == null || sFieldName.equals(""))) {
					final CollectableSearchCondition cond = SearchConditionUtils.newComparison(this.sEntity, sFieldName, ComparisonOperator.EQUAL, mdvoRecord.getField(sFieldName));
					collmdvoRecordExisting = MasterDataDelegate.getInstance().getMasterData(this.sEntity, cond);
				}
				if (collmdvoRecordExisting == null || collmdvoRecordExisting.isEmpty()) {
					//insert the new md record
					try {
						MasterDataDelegate.getInstance().create(this.sEntity, mdvoRecord, null);
						iCountCreated++;
					}
					catch (NuclosBusinessException ex) {
						// Ok (tp)
						System.out.println("INSERT-Fehler bei: " + mdvoRecord.getField("mnemonic"));
						this.lstErrorMessages.add(ex.getMessage());
						iCountError++;
					}
				}
				else {
					for (MasterDataVO mdvoRecordExisting : collmdvoRecordExisting) {
						mdvoRecordExisting.setFields(mdvoRecord.getFields());
						try {
							MasterDataDelegate.getInstance().update(this.sEntity, mdvoRecordExisting, null);
							iCountUpdated++;
						}
						catch (NuclosBusinessException ex) {
							/** @todo !!! */
							System.out.println("UPDATE-Fehler bei: " + mdvoRecord.getField("mnemonic"));
							this.lstErrorMessages.add(ex.getMessage());
							iCountError++;
						}
					}
				}
			}
			catch (CommonBusinessException ex) {
				throw new NuclosFatalException(ex);
			}
			setBackgroundProcessInterruptionIntervalForCurrentThread();
		}
	}

	public int getCountCreated() {
		return this.iCountCreated;
	}

	public int getCountUpdated() {
		return this.iCountUpdated;
	}

	public int getCountError() {
		return this.iCountError;
	}

	public List<String>getErrorMessages() {
		return this.lstErrorMessages;
	}

}	// class FileImport
