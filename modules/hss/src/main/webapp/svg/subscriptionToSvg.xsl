<?xml version="1.0" encoding="utf-8"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
								xmlns="http://www.w3.org/2000/svg"
								xmlns:xlink="http://www.w3.org/1999/xlink" 
								version="1.0">

	<xsl:output
		method="xml"
		encoding="utf-8"
		doctype-public="-//W3C//DTD SVG 1.1//EN"
		doctype-system="http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd"/>

	
	<xsl:variable name="X_SUBSCRIPTION">100</xsl:variable>
	<xsl:variable name="X_PRIVATE">260</xsl:variable>
	<xsl:variable name="X_PUBLIC">440</xsl:variable>
	<xsl:variable name="X_IMPLICIT">430</xsl:variable>
	<xsl:variable name="X_SERVICE_PROFILE">610</xsl:variable>
	<xsl:variable name="RECT_WIDTH">140</xsl:variable>
	<xsl:variable name="RECT_HEIGHT">40</xsl:variable>
	<xsl:variable name="DELTA_Y">100</xsl:variable>
	<xsl:variable name="BASE_Y">-49</xsl:variable>
	<xsl:variable name="Y_SUBSCRIPTION" select="(count(/subscription/PublicIdentities/PublicIdentity) * $DELTA_Y + $DELTA_Y) div 2 + $BASE_Y"/>
	<xsl:variable name="DELTA_Y_PRIVATE" select="(count(/subscription/PublicIdentities/PublicIdentity) * $DELTA_Y  + $DELTA_Y) div (1 + count(/subscription/PrivateIDs/PrivateID))"/>
	<xsl:variable name="DELTA_Y_SERVICE_PROFILE" select="(count(/subscription/PublicIdentities/PublicIdentity) * $DELTA_Y  + $DELTA_Y) div (1 + count(/subscription/ServiceProfiles/ServiceProfile))"/>
	<xsl:variable name="IMPLICIT_WIDTH">160</xsl:variable>
	<xsl:variable name="IMPLICIT_HEIGHT">80</xsl:variable>
	<xsl:variable name="DELTA_Y_TEXT_TYPE">-6</xsl:variable>
	<xsl:variable name="DELTA_Y_TEXT_NAME">8</xsl:variable>
	
	<xsl:template match="/">
		<xsl:processing-instruction name="xml-stylesheet">href="svg.css" type="text/css"</xsl:processing-instruction>
		<svg	width="100%" height="100%" version="1.1" xmlns="http://www.w3.org/2000/svg">
				<xsl:apply-templates/>
		</svg>
	</xsl:template>

	
	<xsl:template match="name">
		<text class='elem'>
			<xsl:attribute name="x"><xsl:value-of select="$X_SUBSCRIPTION"/></xsl:attribute>
			<xsl:attribute name="y"><xsl:value-of select="$Y_SUBSCRIPTION + $DELTA_Y_TEXT_TYPE"/></xsl:attribute>
			Subscription
		</text>
		<text class='host-text'>
			<xsl:attribute name="x"><xsl:value-of select="$X_SUBSCRIPTION"/></xsl:attribute>
			<xsl:attribute name="y"><xsl:value-of select="$Y_SUBSCRIPTION + $DELTA_Y_TEXT_NAME"/></xsl:attribute>
			<xsl:value-of select="text()"/>
		</text>
		<rect class="rect">
			<xsl:attribute name="x"><xsl:value-of select="$X_SUBSCRIPTION - $RECT_WIDTH div 2"/></xsl:attribute>
			<xsl:attribute name="y"><xsl:value-of select="$Y_SUBSCRIPTION - $RECT_HEIGHT div 2"/></xsl:attribute>
			<xsl:attribute name="width"><xsl:value-of select="$RECT_WIDTH"/></xsl:attribute>
			<xsl:attribute name="height"><xsl:value-of select="$RECT_HEIGHT"/></xsl:attribute>
		</rect>
	</xsl:template>
	
	<xsl:template match="PrivateIDs">
		<xsl:for-each select="PrivateID">
			<xsl:variable name="y" select='position() * $DELTA_Y_PRIVATE + $BASE_Y' />
			<text class='elem'>
				<xsl:attribute name="x"><xsl:value-of select="$X_PRIVATE"/></xsl:attribute>
				<xsl:attribute name="y"><xsl:value-of select="$y + $DELTA_Y_TEXT_TYPE"/></xsl:attribute>
				Private Identity
			</text>
			<text class='host-text'>
				<xsl:attribute name="x"><xsl:value-of select="$X_PRIVATE"/></xsl:attribute>
				<xsl:attribute name="y"><xsl:value-of select="$y + $DELTA_Y_TEXT_NAME"/></xsl:attribute>
				<xsl:value-of select="text()"/>
			</text>
			<rect class="rect">
				<xsl:attribute name="x"><xsl:value-of select="$X_PRIVATE - $RECT_WIDTH div 2"/></xsl:attribute>
				<xsl:attribute name="y"><xsl:value-of select="$y - $RECT_HEIGHT div 2"/></xsl:attribute>
				<xsl:attribute name="width"><xsl:value-of select="$RECT_WIDTH"/></xsl:attribute>
				<xsl:attribute name="height"><xsl:value-of select="$RECT_HEIGHT"/></xsl:attribute>
			</rect>
			
			<line class='message-line'>
				<xsl:attribute name="x1"><xsl:value-of select="$X_PRIVATE - $RECT_WIDTH div 2"/></xsl:attribute>
				<xsl:attribute name="x2"><xsl:value-of select="$X_SUBSCRIPTION + $RECT_WIDTH div 2"/></xsl:attribute>
				<xsl:attribute name="y1"><xsl:value-of select="$y"/></xsl:attribute>
				<xsl:attribute name="y2"><xsl:value-of select="$Y_SUBSCRIPTION"/></xsl:attribute>
			</line>
		</xsl:for-each>
	</xsl:template>
	
	<xsl:template match="PublicIdentities">
		<xsl:for-each select="PublicIdentity">
			<xsl:variable name="y" select='position() * $DELTA_Y + $BASE_Y' />
			<text class='elem'>
				<xsl:attribute name="x"><xsl:value-of select="$X_PUBLIC"/></xsl:attribute>
				<xsl:attribute name="y"><xsl:value-of select="$y + $DELTA_Y_TEXT_TYPE"/></xsl:attribute>
				Public Identity
			</text>
			<text class='host-text'>
				<xsl:attribute name="x"><xsl:value-of select="$X_PUBLIC"/></xsl:attribute>
				<xsl:attribute name="y"><xsl:value-of select="$y + $DELTA_Y_TEXT_NAME"/></xsl:attribute>
				<xsl:value-of select="Identity/text()"/>
			</text>
			<rect class="rect">
				<xsl:attribute name="x"><xsl:value-of select="$X_PUBLIC - $RECT_WIDTH div 2"/></xsl:attribute>
				<xsl:attribute name="y"><xsl:value-of select="$y - $RECT_HEIGHT div 2"/></xsl:attribute>
				<xsl:attribute name="width"><xsl:value-of select="$RECT_WIDTH"/></xsl:attribute>
				<xsl:attribute name="height"><xsl:value-of select="$RECT_HEIGHT"/></xsl:attribute>
			</rect>
			<xsl:for-each select="Privates/Id">
				<xsl:variable name="yPrivate" select='text() * $DELTA_Y_PRIVATE + $BASE_Y' />
				<line class='message-line'>
					<xsl:attribute name="x1"><xsl:value-of select="$X_PRIVATE + $RECT_WIDTH div 2"/></xsl:attribute>
					<xsl:attribute name="x2"><xsl:value-of select="$X_PUBLIC - $RECT_WIDTH div 2"/></xsl:attribute>
					<xsl:attribute name="y1"><xsl:value-of select="$yPrivate"/></xsl:attribute>
					<xsl:attribute name="y2"><xsl:value-of select="$y"/></xsl:attribute>
				</line>
			</xsl:for-each>
			<line class='message-line'>
				<xsl:attribute name="x1"><xsl:value-of select="$X_PUBLIC + $RECT_WIDTH div 2"/></xsl:attribute>
				<xsl:attribute name="x2"><xsl:value-of select="$X_SERVICE_PROFILE - $RECT_WIDTH div 2"/></xsl:attribute>
				<xsl:attribute name="y1"><xsl:value-of select="$y"/></xsl:attribute>
				<xsl:attribute name="y2"><xsl:value-of select="ServiceProfile/text() * $DELTA_Y_SERVICE_PROFILE + $BASE_Y"/></xsl:attribute>
				
				</line>
		</xsl:for-each>
	</xsl:template>
	
	<xsl:template match="ImplicitSets">
		<xsl:for-each select="ImplicitSet">
			<xsl:variable name="y" select='From/text() * $DELTA_Y + $BASE_Y' />
			<xsl:variable name="height" select='(To/text() - From/text()) * $DELTA_Y + $IMPLICIT_HEIGHT' />

			<text class='implicit-text'>
				<xsl:attribute name="x"><xsl:value-of select="$X_IMPLICIT"/></xsl:attribute>
				<xsl:attribute name="y"><xsl:value-of select="$y + $height - $IMPLICIT_HEIGHT div 2 - 5"/></xsl:attribute>
				Implicit registration set <xsl:value-of select="Id/text()"/>
			</text>
			
			<rect class="implicit">
				<xsl:attribute name="x"><xsl:value-of select="$X_IMPLICIT - $RECT_WIDTH div 2"/></xsl:attribute>
				<xsl:attribute name="y"><xsl:value-of select="$y - $IMPLICIT_HEIGHT div 2"/></xsl:attribute>
				<xsl:attribute name="width"><xsl:value-of select="$IMPLICIT_WIDTH"/></xsl:attribute>
				<xsl:attribute name="height"><xsl:value-of select="$height"/></xsl:attribute>
			</rect>
		</xsl:for-each>
	</xsl:template>
	
	<xsl:template match="ServiceProfiles">
		<xsl:for-each select="ServiceProfile">
			<xsl:variable name="y" select='position() * $DELTA_Y_SERVICE_PROFILE + $BASE_Y' />
			<text class='elem'>
				<xsl:attribute name="x"><xsl:value-of select="$X_SERVICE_PROFILE"/></xsl:attribute>
				<xsl:attribute name="y"><xsl:value-of select="$y + $DELTA_Y_TEXT_TYPE"/></xsl:attribute>
				Service Profile
			</text>
			<text class='host-text'>
				<xsl:attribute name="x"><xsl:value-of select="$X_SERVICE_PROFILE"/></xsl:attribute>
				<xsl:attribute name="y"><xsl:value-of select="$y + $DELTA_Y_TEXT_NAME"/></xsl:attribute>
				<xsl:value-of select="text()"/>
			</text>
			<rect class="rect">
				<xsl:attribute name="x"><xsl:value-of select="$X_SERVICE_PROFILE - $RECT_WIDTH div 2"/></xsl:attribute>
				<xsl:attribute name="y"><xsl:value-of select="$y - $RECT_HEIGHT div 2"/></xsl:attribute>
				<xsl:attribute name="width"><xsl:value-of select="$RECT_WIDTH"/></xsl:attribute>
				<xsl:attribute name="height"><xsl:value-of select="$RECT_HEIGHT"/></xsl:attribute>
			</rect>
			
		</xsl:for-each>
	</xsl:template>
	
</xsl:stylesheet>
