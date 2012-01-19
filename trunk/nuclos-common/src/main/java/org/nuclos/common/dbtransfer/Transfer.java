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
package org.nuclos.common.dbtransfer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.nuclos.common.collection.Pair;
import org.nuclos.common.dal.vo.EntityObjectVO;

public class Transfer implements Serializable {

	public static final String TOPIC_CORRELATIONID_CREATE = Transfer.class.getName() + ".CREATE";
	public static final String TOPIC_CORRELATIONID_PREPARE = Transfer.class.getName() + ".PREPARE";
	public static final String TOPIC_CORRELATIONID_RUN = Transfer.class.getName() + ".RUN";

	private final boolean nuclon;
	private byte[] transferFile;
	private int newUserCount;
	private Collection<EntityObjectVO> parameter;
	private TransferOption.Map transferOptions;
	private List<PreviewPart> previewParts;

	private Map<String, List<EntityObjectVO>> importData = new HashMap<String, List<EntityObjectVO>>();
	private org.nuclos.common.dbtransfer.NucletContentUID.Map uidExistingMap;
	private org.nuclos.common.dbtransfer.NucletContentUID.Map uidImportMap;
	private org.nuclos.common.dbtransfer.NucletContentUID.Map uidLocalizedMap;
	private Set<Long> existingNucletIds;

	public Result result = new Result();

	public Transfer(
		boolean nuclon,
		byte[] transferFile,
		int newUserCount,
		Collection<EntityObjectVO> parameter,
		TransferOption.Map transferOptions,
		List<PreviewPart> previewParts)	{
		this.nuclon = nuclon;
		this.transferFile = transferFile;
		this.newUserCount = newUserCount;
		this.parameter = parameter;
		this.transferOptions = transferOptions;
		this.previewParts = previewParts;
	}

	public Transfer(Transfer transfer) {
		this(transfer.nuclon, transfer.transferFile, transfer.newUserCount,transfer.parameter,transfer.transferOptions,transfer.previewParts);
		this.setImportData(transfer.getImportData());
		this.setExistingNucletIds(transfer.getExistingNucletIds());
		this.setUidExistingMap(transfer.getUidExistingMap());
		this.setUidImportMap(transfer.getUidImportMap());
		this.setUidLocalizedMap(transfer.getUidLocalizedMap());
	}

	public void appendWarning(String warning) {
		result.sbWarning.append(warning);
		result.sbWarning.append("\n");
	}

	public byte[] getTransferFile() {
		return transferFile;
	}

	public Collection<EntityObjectVO> getParameter() {
		return parameter;
	}

	public int getNewUserCount() {
		return newUserCount;
	}

	public TransferOption.Map getTransferOptions() {
		return transferOptions;
	}

	public void setParameter(Collection<EntityObjectVO> parameter) {
		this.parameter = parameter;
	}

	public List<PreviewPart> getPreviewParts() {
    	return previewParts;
    }

	public Map<String, List<EntityObjectVO>> getImportData() {
		return importData;
	}

	public void setImportData(Map<String, List<EntityObjectVO>> importData) {
		this.importData = importData;
	}

	public org.nuclos.common.dbtransfer.NucletContentUID.Map getUidExistingMap() {
		return uidExistingMap;
	}

	public void setUidExistingMap(NucletContentUID.Map uidExistingMap) {
		this.uidExistingMap = uidExistingMap;
	}

	public org.nuclos.common.dbtransfer.NucletContentUID.Map getUidImportMap() {
		return uidImportMap;
	}

	public void setUidImportMap(NucletContentUID.Map uidImportMap) {
		this.uidImportMap = uidImportMap;
	}

	public org.nuclos.common.dbtransfer.NucletContentUID.Map getUidLocalizedMap() {
		return uidLocalizedMap;
	}

	public void setUidLocalizedMap(NucletContentUID.Map uidLocalizedMap) {
		this.uidLocalizedMap = uidLocalizedMap;
	}

	public Set<Long> getExistingNucletIds() {
		return existingNucletIds;
	}

	public void setExistingNucletIds(Set<Long> existingNucletIds) {
		this.existingNucletIds = existingNucletIds;
	}

	public static class Result implements Serializable {

		public final StringBuffer sbCritical = new StringBuffer();
		public final StringBuffer sbWarning = new StringBuffer();
		public final List<String> script = new ArrayList<String>();
		public final Set<Pair<String, String>> foundReferences = new HashSet<Pair<String,String>>();

		public String getCriticals() {
			return sbCritical.toString();
		}

		public String getWarnings() {
			return sbWarning.toString();
		}

		public void addWarning(StringBuffer s) {
			if (sbWarning.length() > 0) sbWarning.append("<br />");
			sbWarning.append(s);
		}

		public void addCritical(StringBuffer s) {
			if (sbCritical.length() > 0) sbCritical.append("<br />");
			sbCritical.append(s);
		}

		public boolean hasCriticals() {
			return sbCritical.length() > 0;
		}

		public boolean hasWarnings() {
			return sbWarning.length() > 0;
		}

		public void newWarningLine(String warning) {
			if (sbWarning.length() > 0) sbWarning.append("<br />");
			sbWarning.append(warning);
		}

		public void newCriticalLine(String critical) {
			if (sbCritical.length() > 0) sbCritical.append("<br />");
			sbCritical.append(critical);
		}

	}

	public boolean isNuclon() {
		return nuclon;
	}

}
