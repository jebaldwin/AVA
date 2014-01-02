<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
                 xmlns="http://www.w3.org/1999/xhtml">

<xsl:template match="softwarePackage">
   <xsl:variable name="package" select="@name"/>
   <xsl:for-each select="sourceModule">
   <xsl:variable name="module" select="@name"/>
    <xsl:for-each select="controlSection">
    	<xsl:variable name="name" select="@name"/>
Stripe:<xsl:value-of select="$package"/>.<xsl:value-of select="$module"/> Kind:<xsl:value-of select="$name"/> Offset:<xsl:value-of select="@startAddress"/> Depth:<xsl:value-of select="@length"/>   	
    </xsl:for-each>
    <xsl:for-each select="dummySection">
    	<xsl:variable name="name" select="@name"/>
Stripe:<xsl:value-of select="$package"/>.<xsl:value-of select="$module"/> Kind:<xsl:value-of select="$name"/> Offset:<xsl:value-of select="@startAddress"/> Depth:<xsl:value-of select="@length"/>
    </xsl:for-each>   	
   </xsl:for-each>
</xsl:template>
       
</xsl:stylesheet>