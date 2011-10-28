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
package org.nuclos.server.common.mail;

import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.log4j.Logger;
import org.nuclos.common.NuclosFile;
import org.nuclos.common.ParameterProvider;
import org.nuclos.common.mail.NuclosMail;
import org.nuclos.common2.StringUtils;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.server.common.ServerParameterProvider;

public class NuclosMailSender {

	private final static Logger LOG = Logger.getLogger(NuclosMailSender.class);

	/**
	 *
	 * @param mail
	 * @throws CommonBusinessException
	 */
	public static void sendMail(final NuclosMail mail) throws CommonBusinessException {
		boolean auth = "Y".equalsIgnoreCase(ServerParameterProvider.getInstance().getValue(ParameterProvider.KEY_SMTP_AUTHENTICATION));
		String smtpHost = ServerParameterProvider.getInstance().getValue(ParameterProvider.KEY_SMTP_SERVER);
		String smtpPort = ServerParameterProvider.getInstance().getValue(ParameterProvider.KEY_SMTP_PORT);
		String login = ServerParameterProvider.getInstance().getValue(ParameterProvider.KEY_SMTP_USERNAME);
		String sender = ServerParameterProvider.getInstance().getValue(ParameterProvider.KEY_SMTP_SENDER);
		String password = ServerParameterProvider.getInstance().getValue(ParameterProvider.KEY_SMTP_PASSWORD);

		Integer iSmtpPort = null;
		try {
			if (smtpPort != null)
				iSmtpPort = new Integer(smtpPort.trim());
		} catch (Exception e) {
			LOG.error("Parsing smtp port failed.", e);
			throw new CommonBusinessException(StringUtils.getParameterizedExceptionMessage("mailsender.error.2", smtpPort.trim()));
		}

		if (StringUtils.isNullOrEmpty(mail.getFrom())) {
			mail.setFrom(sender);
		}
		sendMail(smtpHost, iSmtpPort, auth?login:null, password,  mail);
	}

	/**
	 *
	 * @param smtpHost
	 * @param mail
	 * @throws CommonBusinessException
	 */
	public static void sendMail(final String smtpHost, final NuclosMail mail) throws CommonBusinessException {
		sendMail(smtpHost, null, mail);
	}

	/**
	 *
	 * @param smtpHost
	 * @param smtpPort
	 * @param mail
	 * @throws CommonBusinessException
	 */
	public static void sendMail(final String smtpHost, final Integer smtpPort, final NuclosMail mail) throws CommonBusinessException {
		sendMail(smtpHost, smtpPort, null, null, mail);
	}

	/**
	 *
	 * @param smtpHost
	 * @param smtpPort
	 * @param login
	 * @param password
	 * @param mail
	 * @throws CommonBusinessException
	 */
	public static void sendMail(final String smtpHost, final Integer smtpPort, final String login, final String password, final NuclosMail mail) throws CommonBusinessException{
		try{
			Properties properties = new Properties();
			properties.put("mail.smtp.host", smtpHost);
			if (smtpPort != null) {
				properties.put("mail.smtp.port", smtpPort.intValue());
			}

			Authenticator auth = null;
			if (login != null) {
				properties.put("mail.smtp.user", login);
				properties.put("mail.smtp.starttls.enable","true");
				properties.put("mail.smtp.auth", "true");
				auth = new Authenticator() {

					@Override
					protected PasswordAuthentication getPasswordAuthentication() {
						return new PasswordAuthentication(login, password);
					}
				};
			}
			Session session = Session.getInstance(properties, auth);

			MimeMultipart content = new MimeMultipart();
			MimeBodyPart text = new MimeBodyPart();
//			MimeBodyPart html = new MimeBodyPart();
			text.setText(mail.getMessage());
			text.setHeader("MIME-Version","1.0");
			text.setHeader("Content-Type",text.getContentType());
//			html.setContent("<html>Text als <b>HTML</b></html>", "text/html");
//			html.setHeader("MIME-Version", "1.0");
//			html.setHeader("Content-Type", html.getContentType());
			content.addBodyPart(text);
//			content.addBodyPart(html);

			Message msg = new MimeMessage(session);
			msg.setFrom(new InternetAddress(mail.getFrom()));

			String[] to = mail.getTo().split(";");
			InternetAddress[] recipients = new InternetAddress[to.length];
			for (int i = 0; i < to.length; i++) {
				recipients[i] = new InternetAddress(to[i]);
			}
			msg.setRecipients(Message.RecipientType.TO, recipients);

			msg.setSubject(mail.getSubject());
			msg.setContent(content);
			msg.setHeader("MIME-Version", "1.0");
			msg.setHeader("Content-Type", content.getContentType());

			for (NuclosFile attachment : mail.getAttachments()) {
				DataSource fileDataSource = new FileDataSource(attachment.getFileName());
				fileDataSource.getOutputStream().write(attachment.getFileContents());

				MimeBodyPart messageBodyPart = new MimeBodyPart();
				messageBodyPart.setDataHandler(new DataHandler(fileDataSource));
				messageBodyPart.setFileName(attachment.getFileName());
				content.addBodyPart(messageBodyPart);
			}


			Transport.send(msg);
		} catch (Exception e) {
			LOG.error("Sending email failed.", e);
			throw new CommonBusinessException(StringUtils.getParameterizedExceptionMessage("mailsender.error.1", e.getMessage()));
		}
	}
}
