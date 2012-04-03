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
package org.nuclos.common;

import org.nuclos.common2.LangUtils;

/**
 * Notification sent by rules for messages on the client.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 * @author	<a href="mailto:uwe.allner@novabit.de">uwe.allner</a>
 * @version 01.00.00
 */
public class RuleNotification extends SimpleClientNotification {

	/** @todo for backward compatibility with rules only - eliminate these */
	@Deprecated
	public static final Priority PRIORITY_HIGH = Priority.HIGH;
	@Deprecated
	public static final Priority PRIORITY_MEDIUM = Priority.NORMAL;
	@Deprecated
	public static final Priority PRIORITY_LOW = Priority.LOW;

	private final String sRuleName;
	private String sourceIdentifier;
	private String targetIdentifier;
	private Integer sourceId;
	private Integer targetId;

	public RuleNotification(Priority priority, String sMessage, String sRuleName) {
		super(priority, sMessage);

		this.sRuleName = sRuleName;
	}

	public String getRuleName() {
		return sRuleName;
	}

	public Integer getSourceId() {
		return sourceId;
	}

	public void setSourceId(Integer sourceId) {
		this.sourceId = sourceId;
	}

	public String getSourceIdentifier() {
		return sourceIdentifier;
	}

	public void setSourceIdentifier(String sourceIdentifier) {
		this.sourceIdentifier = sourceIdentifier;
	}

	public Integer getTargetId() {
		return targetId;
	}

	public void setTargetId(Integer targetId) {
		this.targetId = targetId;
	}

	public String getTargetIdentifier() {
		return targetIdentifier;
	}

	public void setTargetIdentifier(String targetIdentifier) {
		this.targetIdentifier = targetIdentifier;
	}
	
	@Override
	public boolean equals(Object other) {
		if (this == other) return true;
		if (!(other instanceof RuleNotification)) return false;
		final RuleNotification o = (RuleNotification) other;
		return LangUtils.equals(getMessage(), o.getMessage())
				&& LangUtils.equals(getSourceIdentifier(), o.getSourceIdentifier())
				&& LangUtils.equals(getTargetIdentifier(), o.getTargetIdentifier())
				&& LangUtils.equals(getSourceId(), o.getSourceId())
				&& LangUtils.equals(getTargetId(), o.getTargetId());
	}
	
	@Override 
	public int hashCode() {
		int result = 8728721;
		String s = getMessage();
		if (s != null) {
			result += 3 * s.hashCode();
		}
		s = getSourceIdentifier();
		if (s != null) {
			result += 5 * s.hashCode();
		}
		s = getTargetIdentifier();
		if (s != null) {
			result += 7 * s.hashCode();
		}
		Integer i = getSourceId();
		if (i != null) {
			result += 11 * i.hashCode();
		}
		i = getTargetId();
		if (i != null) {
			result += 13 * i.hashCode();
		}
		return result;
	}

	@Override
	public String toString() {
		return sRuleName + ": " + getMessage();
	}
	
	public String toDescription() {
		final StringBuilder result = new StringBuilder();
		result.append("RuleNotification[");
		result.append("msg=").append(getMessage());
		result.append(",srcId").append(sourceId);
		result.append(",srcIdent=").append(sourceIdentifier);
		result.append(",targedId=").append(targetId);
		result.append(",targetIdent=").append(targetIdentifier);
		result.append(",rule=").append(sRuleName);
		result.append("]");
		return result.toString();
	}
}	// class RuleNotification
