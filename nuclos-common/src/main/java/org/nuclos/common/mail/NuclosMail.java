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
package org.nuclos.common.mail;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.nuclos.common.NuclosFile;

public class NuclosMail implements Serializable {

	private String from;
	private String to;
	private String subject;
	private String message;
	private Map<String, String> headers = new HashMap<String, String>();
	
	private final Collection<NuclosFile> lstAttachment = new ArrayList<NuclosFile>(1);
	
	public NuclosMail() {}
	
	public NuclosMail(String to, String subject, String message) {
		this(null, to, subject, message);
	}

	public NuclosMail(String from, String to, String subject, String message) {
		this.from = from;
		this.to = to;
		this.subject = subject;
		this.message = message;
	}
	
	public NuclosMail(String to, String subject, String message, Collection<NuclosFile> attachments) {
		this(null, to, subject, message, attachments);
	}
	
	public NuclosMail(String from, String to, String subject, String message, Collection<NuclosFile> attachments) {
		this.from = from;
		this.to = to;
		this.subject = subject;
		this.message = message;
		this.addAttachments(attachments);
	}
	
	public NuclosMail(String to, String subject, String message, NuclosFile attachment) {
		this(null, to, subject, message, attachment);
	}
	
	public NuclosMail(String from, String to, String subject, String message, NuclosFile attachment) {
		this.from = from;
		this.to = to;
		this.subject = subject;
		this.message = message;
		this.addAttachment(attachment);
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}
	
	public String getHeader(final String header) {
		return headers.get(header);
	}

	public void setHeader(final String header, final String value) {
		headers.put(header, value);
	}

	public String getTo() {
		return to;
	}

	public void setTo(String to) {
		this.to = to;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Collection<NuclosFile> getAttachments() {
		return lstAttachment;
	}

	public void addAttachment(NuclosFile file) {
		if (file != null)
			this.lstAttachment.add(file);
	}
	
	public void addAttachments(Collection<NuclosFile> files) {
		if (files != null)
			this.lstAttachment.addAll(files);
	}
	
	
}
