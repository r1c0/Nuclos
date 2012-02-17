package org.nuclos.build;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.tmatesoft.svn.cli.SVNCommandUtil;
import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNLock;
import org.tmatesoft.svn.core.SVNNodeKind;
import org.tmatesoft.svn.core.internal.util.SVNDate;
import org.tmatesoft.svn.core.internal.util.SVNPathUtil;
import org.tmatesoft.svn.core.internal.util.SVNXMLUtil;
import org.tmatesoft.svn.core.internal.wc.SVNConflictVersion;
import org.tmatesoft.svn.core.internal.wc.SVNTreeConflictUtil;
import org.tmatesoft.svn.core.internal.wc.admin.SVNEntry;
import org.tmatesoft.svn.core.wc.ISVNStatusHandler;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNInfo;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNStatus;
import org.tmatesoft.svn.core.wc.SVNStatusType;
import org.tmatesoft.svn.core.wc.SVNTreeConflictDescription;
import org.tmatesoft.svn.core.wc.SVNWCUtil;

public class SvnkitBuildInfo {

	private final SVNClientManager cm = SVNClientManager.newInstance();

	private final File wc;

	public SvnkitBuildInfo(File wc) {
		if (!wc.isDirectory()) {
			throw new IllegalArgumentException("Not a directory: " + wc);
		}
		this.wc = new File(wc, "/.");
		cm.setAuthenticationManager(SVNWCUtil.createDefaultAuthenticationManager());
	}

	public void info(File infoFile) throws SVNException, IOException {
		final SVNInfo info = cm.getWCClient().doInfo(wc, SVNRevision.UNDEFINED);
		final Writer writer = new OutputStreamWriter(new FileOutputStream(infoFile), "UTF-8");
		try {
			printInfo(writer, info);
		}
		finally {
			writer.close();
		}
	}

	/**
	 * Stolen from SVNInfoCommand.printInfo
	 * @throws IOException 
	 */
	private void printInfo(Writer out, SVNInfo info) throws IOException {
		String path = null;
		if (info.getFile() != null) {
			// path = getSVNEnvironment().getRelativePath(info.getFile());
			path = wc.toString();
			path = SVNCommandUtil.getLocalPath(path);
		}
		else {
			path = info.getPath();
		}
		out.write("Path: " + path + "\n");
		if (info.getKind() != SVNNodeKind.DIR) {
			out.write("Name: " + SVNPathUtil.tail(path.replace(File.separatorChar, '/')) + "\n");
		}
		out.write("URL: " + info.getURL() + "\n");
		if (info.getRepositoryRootURL() != null) {
			out.write("Repository Root: " + info.getRepositoryRootURL() + "\n");
		}
		if (info.getRepositoryUUID() != null) {
			out.write("Repository UUID: " + info.getRepositoryUUID() + "\n");
		}
		if (info.getRevision() != null && info.getRevision().isValid()) {
			out.write("Revision: " + info.getRevision() + "\n");
		}
		String kind = info.getKind() == SVNNodeKind.DIR ? "directory" : (info.getKind() != null ? info.getKind()
				.toString() : "none");
		out.write("Node Kind: " + kind + "\n");
		if (!info.isRemote()) {
			if (info.getSchedule() == null) {
				out.write("Schedule: normal\n");
			}
			else {
				out.write("Schedule: " + info.getSchedule() + "\n");
			}
			if (info.getDepth() != null) {
				if (info.getDepth() != SVNDepth.UNKNOWN && info.getDepth() != SVNDepth.INFINITY) {
					out.write("Depth: " + info.getDepth() + "\n");
				}
			}
			if (info.getCopyFromURL() != null) {
				out.write("Copied From URL: " + info.getCopyFromURL() + "\n");
			}
			if (info.getCopyFromRevision() != null && info.getCopyFromRevision().getNumber() >= 0) {
				out.write("Copied From Rev: " + info.getCopyFromRevision() + "\n");
			}
		}
		if (info.getAuthor() != null) {
			out.write("Last Changed Author: " + info.getAuthor() + "\n");
		}
		if (info.getCommittedRevision() != null && info.getCommittedRevision().getNumber() >= 0) {
			out.write("Last Changed Rev: " + info.getCommittedRevision() + "\n");
		}
		if (info.getCommittedDate() != null) {
			out.write("Last Changed Date: " + SVNDate.formatHumanDate(info.getCommittedDate(), cm.getOptions()) + "\n");
		}
		if (!info.isRemote()) {
			if (info.getTextTime() != null) {
				out.write("Text Last Updated: " + SVNDate.formatHumanDate(info.getTextTime(), cm.getOptions()) + "\n");
			}
			if (info.getPropTime() != null) {
				out.write("Properties Last Updated: " + SVNDate.formatHumanDate(info.getPropTime(), cm.getOptions())
						+ "\n");
			}
			if (info.getChecksum() != null) {
				out.write("Checksum: " + info.getChecksum() + "\n");
			}
			if (info.getConflictOldFile() != null) {
				out.write("Conflict Previous Base File: " + info.getConflictOldFile().getName() + "\n");
			}
			if (info.getConflictWrkFile() != null) {
				out.write("Conflict Previous Working File: " + info.getConflictWrkFile().getName() + "\n");
			}
			if (info.getConflictNewFile() != null) {
				out.write("Conflict Current Base File: " + info.getConflictNewFile().getName() + "\n");
			}
			if (info.getPropConflictFile() != null) {
				out.write("Conflict Properties File: " + info.getPropConflictFile().getName() + "\n");
			}
		}
		if (info.getLock() != null) {
			SVNLock lock = info.getLock();
			if (lock.getID() != null) {
				out.write("Lock Token: " + lock.getID() + "\n");
			}
			if (lock.getOwner() != null) {
				out.write("Lock Owner: " + lock.getOwner() + "\n");
			}
			if (lock.getCreationDate() != null && lock.getCreationDate().getTime() != 0) {
				out.write("Lock Created: " + SVNDate.formatHumanDate(lock.getCreationDate(), cm.getOptions()) + "\n");
			}
			if (lock.getExpirationDate() != null && lock.getExpirationDate().getTime() != 0) {
				out.write("Lock Expires: " + SVNDate.formatHumanDate(lock.getExpirationDate(), cm.getOptions()) + "\n");
			}
			if (lock.getComment() != null) {
				out.write("Lock Comment ");
				int lineCount = SVNCommandUtil.getLinesCount(lock.getComment());
				out.write(lineCount > 1 ? "(" + lineCount + " lines)" : "(1 line)");
				out.write(":\n");
				out.write(lock.getComment());
				out.write("\n");
			}
		}
		if (info.getChangelistName() != null) {
			out.write("Changelist: " + info.getChangelistName() + "\n");
		}
		if (info.getTreeConflict() != null) {
			SVNTreeConflictDescription tc = info.getTreeConflict();
			String description = SVNTreeConflictUtil.getHumanReadableConflictDescription(tc);
			out.write("Tree conflict: " + description + "\n");
			SVNConflictVersion left = tc.getSourceLeftVersion();
			if (left != null) {
				out.write("  Source  left: " + SVNTreeConflictUtil.getHumanReadableConflictVersion(left) + "\n");
			}
			SVNConflictVersion right = tc.getSourceRightVersion();
			if (right != null) {
				out.write("  Source right: " + SVNTreeConflictUtil.getHumanReadableConflictVersion(right) + "\n");
			}
		}
		out.write("\n");
		// getSVNEnvironment().getOut().print(buffer.toString());
	}

	public void status(File statusFile) throws SVNException, IOException {
		final File absPath = wc.getAbsoluteFile();
		final class MyHandler implements ISVNStatusHandler {

			private SVNStatus status;

			private List<String> lines = new ArrayList<String>();

			public void handleStatus(SVNStatus s) {
				final SVNStatusType cs = s.getContentsStatus();
				final SVNStatusType ns = s.getNodeStatus();
				final SVNTreeConflictDescription tc = s.getTreeConflict();
				final SVNEntry entry = s.getEntry();
				if (absPath.equals(s.getFile())) {
					if (status != null && cs == SVNStatusType.STATUS_EXTERNAL && absPath.isDirectory()) {
						status = s;
						status.markExternal();
					}
					else if (status == null) {
						status = s;
					}
				}
				if (tc != null || cs != null || ns != null) {
					final StringBuilder line = new StringBuilder();
					line.append(s.getFile().getPath()).append(" entry=").append(entry);
					// line.append(entry.getName()).append(" ");
					if (tc != null) {
						line.append("C");
					}
					if (cs != null) {
						line.append(" content=").append(cs.toString());
					}
					if (ns != null) {
						line.append(" node=").append(ns.toString());
					}
					lines.add(line.toString());
				}
			}
		}
		;
		final MyHandler handler = new MyHandler();
		final long revision = cm.getStatusClient().doStatus(wc, SVNRevision.HEAD, SVNDepth.INFINITY,
				true, false, true, false, handler, null);
		final Writer writer = new OutputStreamWriter(new FileOutputStream(statusFile), "UTF-8");
		try {
			if (handler.status != null) {
				printStatus(writer, handler.status);
			}
			for (String s : handler.lines) {
				writer.write(s);
				writer.write("\n");
			}
		}
		finally {
			writer.close();
		}
	}

	public void dispose() {
		cm.dispose();
	}

	private void printStatus(Writer out, SVNStatus status) throws IOException {
		// StringBuffer xmlBuffer = openXMLTag("entry", SVNXMLUtil.XML_STYLE_NORMAL, "path", path, null);
		Map<String, String> xmlMap = new LinkedHashMap<String, String>();
		xmlMap.put("props", status.getPropertiesStatus().toString());
		xmlMap.put("item", status.getContentsStatus().toString());
		if (status.isLocked()) {
			xmlMap.put("wc-locked", "true");
		}
		if (status.isCopied()) {
			xmlMap.put("copied", "true");
		}
		if (status.isSwitched()) {
			xmlMap.put("switched", "true");
		}
		if (status.isFileExternal()) {
			xmlMap.put("file-external", "true");
		}
		if (status.isVersioned() && !status.isCopied()) {
			xmlMap.put("revision", status.getRevision().toString());
		}
		if (status.getTreeConflict() != null) {
			xmlMap.put("tree-conflicted", "true");
		}
		openXMLTag("wc-status", SVNXMLUtil.XML_STYLE_NORMAL, xmlMap, out);
		if (status.isVersioned() && status.getCommittedRevision().isValid()) {
			xmlMap.put("revision", status.getCommittedRevision().toString());
			openXMLTag("commit", SVNXMLUtil.XML_STYLE_NORMAL, xmlMap, out);
			openCDataTag("author", status.getAuthor(), out);
			if (status.getCommittedDate() != null) {
				openCDataTag("date", SVNDate.formatDate(status.getCommittedDate()), out);
			}
			closeXMLTag("commit", out);
		}
		if (status.isVersioned() && status.getLocalLock() != null) {
			openXMLTag("lock", SVNXMLUtil.XML_STYLE_NORMAL, null, out);
			openCDataTag("token", status.getLocalLock().getID(), out);
			openCDataTag("owner", status.getLocalLock().getOwner(), out);
			openCDataTag("comment", status.getLocalLock().getComment(), out);
			openCDataTag("created", SVNDate.formatDate(status.getLocalLock().getCreationDate()), out);
			closeXMLTag("lock", out);
		}
		closeXMLTag("wc-status", out);
		if (status.getRemoteContentsStatus() != SVNStatusType.STATUS_NONE
				|| status.getRemotePropertiesStatus() != SVNStatusType.STATUS_NONE || status.getRemoteLock() != null) {
			xmlMap.put("props", status.getRemotePropertiesStatus().toString());
			xmlMap.put("item", status.getRemoteContentsStatus().toString());
			openXMLTag("repos-status", SVNXMLUtil.XML_STYLE_NORMAL, xmlMap, out);
			if (status.getRemoteLock() != null) {
				openXMLTag("lock", SVNXMLUtil.XML_STYLE_NORMAL, null, out);
				openCDataTag("token", status.getRemoteLock().getID(), out);
				openCDataTag("owner", status.getRemoteLock().getOwner(), out);
				openCDataTag("comment", status.getRemoteLock().getComment(), out);
				openCDataTag("created", SVNDate.formatDate(status.getRemoteLock().getCreationDate()), out);
				if (status.getRemoteLock().getExpirationDate() != null) {
					openCDataTag("expires", SVNDate.formatDate(status.getRemoteLock().getExpirationDate()), out);
				}
				closeXMLTag("lock", out);
			}
			closeXMLTag("repos-status", out);
		}
		closeXMLTag("entry", out);
		// return xmlBuffer;
	}

	private void openXMLTag(String tag, int style, Map<String, String> attributes, Writer out) throws IOException {
		out.write("<");
		out.write(tag);
		for (String key : attributes.keySet()) {
			final String value = attributes.get(key);
			out.write(" ");
			out.write(key);
			out.write("='");
			out.write(value);
			out.write("'");
		}
		out.write(">\n");
		attributes.clear();
	}

	private void openCDataTag(String tag, String cdata, Writer out) throws IOException {
		out.write("<");
		out.write(tag);
		out.write(">");
		out.write(cdata);
		out.write("</");
		out.write(tag);
		out.write(">\n");
	}

	private void closeXMLTag(String tag, Writer out) throws IOException {
		out.write("</");
		out.write(tag);
		out.write(">\n");
	}
}
