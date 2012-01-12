<xsl:transform
 xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
 version="2.0"
>

<xsl:output method="xml" encoding="UTF-8" indent="yes"/>

<!-- Contact: darren minifie - minofifa@gmail.com
	 This stylesheet transforms the CALabs Algol package structure
	 to the DND MyDooM format -->
	
<!-- NOTES AND BUGS:
	1. The index attribute on the function element should increment. It only increments until the next enclosing block ends.  is this correct, or should the incrementer be global?
	2. Not sure what you want in the externalFile attribute?
	3. Is there any preliminary / post stuff that should be transformed?
	
-->
	<xsl:variable name="now" select="current-dateTime()"/>

	<xsl:template match="/">
		<xsl:apply-templates />
	</xsl:template>

	<xsl:template match="sourceModule" >
		<xsl:element name="section">
			<xsl:attribute name="name"><xsl:value-of select="@name"/></xsl:attribute>
			
			<!-- For each addressPoint, process the subset of callToAddress's -->
			<xsl:for-each select="controlSection">	
				<xsl:element name="functionEntryPoint">
			
					<xsl:attribute name="address"><xsl:value-of select="@startAddress" /></xsl:attribute>
					<xsl:attribute name="section"><xsl:value-of select="@name"/></xsl:attribute>
					<xsl:attribute name="name"><xsl:value-of select="@name"/></xsl:attribute>
					<xsl:attribute name="index"><xsl:value-of select="@length" /></xsl:attribute>
										
					<xsl:variable name="section" select="@name"/>
					
					<xsl:for-each select="addressPoints/addressPoint">	
						<xsl:element name="function">
							
							<xsl:attribute name="address">
								<xsl:value-of select="@hexOffset"/>
							</xsl:attribute>
							<xsl:attribute name="name">
								<xsl:value-of select="@label"/>
							</xsl:attribute>
							<xsl:attribute name="index">
								<xsl:value-of select="position()"/>
							</xsl:attribute>
							<xsl:attribute name="section">
								<xsl:value-of select="$section"/>
							</xsl:attribute>
							
							
							<!-- For each addressPoint, process the subset of callToAddress's -->
							<xsl:for-each select="callToAddress">
								<xsl:variable name="currsection"
									select="substring-after(substring-before(@id,'.'),'@')"/>
								<xsl:element name="call">
									
									<!--<xsl:attribute name="calladdress"><xsl:value-of select="substring-after(../@id,'.')" /></xsl:attribute>-->
									<xsl:attribute name="calladdress">
										<xsl:value-of
											select="substring-after(@id,'.')"/>
									</xsl:attribute>
									<xsl:attribute name="name">
										<xsl:value-of
											select="substring-before(@id,'@')"/>
									</xsl:attribute>
									<xsl:attribute name="functionaddress">
										<xsl:value-of
											select="substring-after(@id,'.')"/>
									</xsl:attribute>
									
									<xsl:choose>
										<xsl:when test="$section != $currsection">
											<xsl:attribute name="index">
												<xsl:text>external</xsl:text>
											</xsl:attribute>
											<xsl:attribute name="externalfile">
												<xsl:value-of select="$currsection"/>
											</xsl:attribute>
										</xsl:when>
										<xsl:otherwise>
											<xsl:attribute name="index">
												<xsl:text>local</xsl:text>
											</xsl:attribute>
											<xsl:attribute name="externalfile">
												<xsl:value-of select="$currsection"/>
											</xsl:attribute>											
										</xsl:otherwise>
									 </xsl:choose>
								</xsl:element>
							</xsl:for-each>
						</xsl:element>
					</xsl:for-each>
				</xsl:element>
			</xsl:for-each>
		</xsl:element>
	</xsl:template>
	

	<!-- The identity transform: let all others through -->
	<xsl:template match="@*|node()">
	   <xsl:copy>
	      <xsl:apply-templates select="@*|node()"/>
	   </xsl:copy>
	</xsl:template>
	
</xsl:transform>