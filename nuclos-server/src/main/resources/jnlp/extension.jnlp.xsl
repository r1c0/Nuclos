<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output method="xml" encoding="UTF-8" indent="yes" />

	<xsl:param name="codebase" />

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
					<jar href="{text()}" />
				</xsl:for-each>
			</resources>
			<component-desc/>
		</jnlp>
	</xsl:template>
</xsl:stylesheet>
