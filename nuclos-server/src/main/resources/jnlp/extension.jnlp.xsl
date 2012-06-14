<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output method="xml" encoding="UTF-8" indent="yes" />

	<xsl:param name="codebase" />
	<xsl:param name="jnlp.packEnabled" />

	<xsl:template match="/">
		<jnlp spec="1.0+" codebase="{$codebase}">
			<information>
				<title>Webstart extension</title>
				<vendor>Novabit Informationssysteme GmbH</vendor>
			</information>
			<security>
				<all-permissions />
			</security>
			<resources>
				<j2se version="1.6+" />
				<xsl:for-each select="jnlp/jars/jar">
					<jar href="{text()}" download="{@download}" />
				</xsl:for-each>
				<xsl:for-each select="jnlp/native/jar">
					<nativelib href="native/{text()}" download="{@download}" />
				</xsl:for-each>
				<property name="jnlp.packEnabled" value="{$jnlp.packEnabled}" />
			</resources>
			<component-desc/>
		</jnlp>
	</xsl:template>
</xsl:stylesheet>
