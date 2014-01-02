<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
                 xmlns="http://www.w3.org/1999/xhtml">
 
<xsl:template match="softwarePackage">
	<xsl:variable name="package" select="@name"/>
	<xsl:for-each select="sourceModule">
Group:<xsl:value-of select="$package"/>
Member:<xsl:value-of select="@name"/> Size:<xsl:value-of select="@lastAddress"/> Tip:<xsl:value-of select="$package"/>.<xsl:value-of select="@name"/></xsl:for-each>
</xsl:template>
   
</xsl:stylesheet>