<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output method="xml" encoding="UTF-8" indent="yes" />

	<xsl:param name="codebase" />
	<xsl:param name="jnlp.packEnabled" />
	<xsl:param name="url.remoting" />
	<xsl:param name="url.jms" />
	<xsl:param name="singleinstance" />
	<xsl:param name="nuclos.version" />
	<xsl:param name="extensions" />
	<xsl:param name="extension-lastmodified" />

	<xsl:template match="/">
		<!-- JNLP File for webstart client -->
		<jnlp spec="1.0+" codebase="{$codebase}">
			<information>
				<title>Nuclos</title>
				<vendor>Novabit Informationssysteme GmbH</vendor>
				<homepage href="http://www.nuclos.de" />
				<description>Nuclos Webstart Client</description>
				<icon href="customer-icon.gif" />
				<icon href="splash-screen.gif" kind="splash" />
			</information>
			<security>
				<all-permissions />
			</security>
			<resources>
				<!--
					-XX:+HeapDumpOnOutOfMemoryError
					will only be used by jse7 because it is not mention at
					http://docs.oracle.com/javase/6/docs/technotes/guides/javaws/developersguide/syntax.html
					
					See also:
					http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6664424
				-->
				<j2se version="1.6+" initial-heap-size="256m" max-heap-size="512m" java-vm-args="-XX:+HeapDumpOnOutOfMemoryError -verbose:gc -ea"/>
				<jar href="nuclos-client-{$nuclos.version}.jar" main="true" />
				<xsl:for-each select="jnlp/jars/jar">
					<jar href="{text()}" download="{@download}" />
				</xsl:for-each>
				<nativelib href="nuclos-native-1.0.jar" download="lazy" />
				<property name="jnlp.packEnabled" value="{$jnlp.packEnabled}" />
				<property name="url.remoting" value="{$url.remoting}" />
				<property name="url.jms" value="{$url.jms}" />
				<property name="log4j.url" value="{$codebase}/log4j.xml" />
				<property name="nuclos.server.name" value="Webstart" />
				<property name="nuclos.client.singleinstance" value="{$singleinstance}" />
				<property name="nuclos.client.webstart" value="true" />
				<property name="java.util.prefs.PreferencesFactory" value="org.nuclos.client.common.prefs.NuclosPreferencesFactory" />
				<xsl:if test="$extensions='true'">
					<extension name="extension" href="extensions/extension-{$extension-lastmodified}.jnlp" />
				</xsl:if>
				<xsl:for-each select="jnlp/themes/theme">
					<extension name="{@name}" href="extensions/themes/theme-{@name}-{@lastmodified}.jnlp" />
				</xsl:for-each>
			</resources>
			<application-desc main-class="org.nuclos.client.main.Main">
				<xsl:for-each select="jnlp/arguments/argument">
					<argument>
						<xsl:value-of select="." />
					</argument>
				</xsl:for-each>
			</application-desc>
		</jnlp>
	</xsl:template>
</xsl:stylesheet>
