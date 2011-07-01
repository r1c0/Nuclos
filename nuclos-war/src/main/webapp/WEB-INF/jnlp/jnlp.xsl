<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:output method="xml" encoding="UTF-8" indent="yes"/>

  <xsl:param name="codebase"/>
  <xsl:param name="url.remoting"/>
  <xsl:param name="url.jms"/>
  <xsl:param name="singleinstance"/>

  <xsl:template match="/">
	<!-- JNLP File for webstart client -->
	<jnlp spec="1.0+" codebase="{$codebase}">
		<information>
			<title>Nuclos</title>
			<vendor>Novabit Informationssysteme GmbH</vendor>
			<homepage href="http://www.nuclos.de"/>
			<description>Nuclos Webstart Client</description>
			<icon href="customer-icon.gif"/>
		</information>
		<security>
			<all-permissions/>
		</security>
		<resources>
			<j2se version="1.6+" initial-heap-size="256m" max-heap-size="512m"/>
			<jar href="nuclos-client.jar" main="true" />
			<xsl:for-each select="jnlp/jars/jar">
				<jar href="{text()}"/>
			</xsl:for-each>
			<nativelib href="nuclos-native.jar"/>
			<property name="url.remoting" value="{$url.remoting}"/>
			<property name="url.jms" value="{$url.jms}"/>
			<property name="log4j.url" value="{$codebase}/log4j.xml"/>
			<property name="nuclos.server.name" value="Webstart"/>
			<property name="nuclos.client.singleinstance" value="{$singleinstance}" />
		</resources>
		<application-desc main-class="org.nuclos.client.main.Main">
			<xsl:for-each select="jnlp/arguments/argument">
				<argument><xsl:value-of select="." /></argument>
			</xsl:for-each>
		</application-desc>
	</jnlp>
  </xsl:template>
</xsl:stylesheet>
