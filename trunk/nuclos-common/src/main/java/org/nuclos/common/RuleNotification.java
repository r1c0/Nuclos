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
	public String toString() {
		return sRuleName + ": " + super.getMessage();
	}
}	// class RuleNotification
