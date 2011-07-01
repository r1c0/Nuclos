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
package org.nuclos.common2.communication;

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.Message.RecipientType;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.nuclos.common2.DateUtils;
import org.nuclos.common2.communication.exception.CommonCommunicationException;

/**
 * Communicator class for eMail communication.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:ramin.goettlich@novabit.de">Ramin Goettlich</a>
 * @version	00.01.000
 */
public class MailCommunicator implements Communicator {

	private final String sSmtpServer;
	private final String sUserName;
	private final String sPassword;

	public MailCommunicator(String sSmtpServer, String sUserName, String sPassword) {
		this.sSmtpServer = sSmtpServer;
		this.sUserName = sUserName;
		this.sPassword = sPassword;
	}

	@Override
	public boolean isValid(String sAddress) {
		return sAddress != null && sAddress.contains("@");
	}

	@Override
	public void sendMessage(String bAuth, String sSender, String[] sRecipients, String sSubject, String sMessage) throws CommonCommunicationException {
		Properties props = System.getProperties();
		
		Session session;
		
		if (bAuth != null && bAuth.equals("Y")) {
			props.put("mail.smtp.auth", "true");	 
			Authenticator auth = new SMTPAuthenticator();
			session = Session.getInstance(props, auth);
		}
		else {
			session = Session.getDefaultInstance(props, null);
		}
		
		final Message msg = new MimeMessage(session);
		try {
			// construct message:
			msg.setFrom(new InternetAddress(sSender));
			msg.setSentDate(DateUtils.now());
			msg.setSubject(sSubject);
			for (String sRecipient : sRecipients) {
				if (sRecipient != null) {
					msg.addRecipient(RecipientType.TO, new InternetAddress(sRecipient));
				}
			}
			final MimeBodyPart mimebodypart = new MimeBodyPart();
			mimebodypart.setText(sMessage);
			final Multipart multipart = new MimeMultipart();
			multipart.addBodyPart(mimebodypart);
			msg.setContent(multipart);
			msg.saveChanges();

			// send message:
			final Transport transport = session.getTransport("smtp");
			transport.connect(sSmtpServer, sUserName, sPassword);
			if (msg.getAllRecipients() != null) {
				transport.sendMessage(msg, msg.getAllRecipients());
			}
			transport.close();
		}
		catch (MessagingException ex) {
			throw new CommonCommunicationException(ex);
		}
	}
	
	/**
	 * SimpleAuthenticator is used to do simple authentication when the SMTP
	 * server requires it.
	 */
	private class SMTPAuthenticator extends javax.mail.Authenticator {
 
		@Override
		public PasswordAuthentication getPasswordAuthentication() {
			String username = sUserName;
			String password = sPassword;
			return new PasswordAuthentication(username, password);
		}
	}

}  // class MailCommunicator
