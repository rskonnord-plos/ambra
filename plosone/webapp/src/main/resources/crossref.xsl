<?xml version='1.0' ?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:template match="/">
		<doi_batch xmlns="http://www.crossref.org/schema/3.0.3" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" version="3.0.3" xsi:schemaLocation="http://www.crossref.org/schema/3.0.3 crossref3.0.3.xsd">
			<head>
				<depositor>
					<name>Plos</name>
				</depositor>
			</head>
			<body>
				<book>
					<book_metadata>
						<titles>
							<title>
								<xsl:value-of select="article/front/journal-meta/journal-title"/>
							</title>
						</titles>
						<volume>
							<xsl:value-of select="article/front/article-meta/volume"/>
						</volume>
						<publication_date>
							<month>
								<xsl:value-of select="article/front/article-meta/pub-date/month"/>
							</month>
							<year>
								<xsl:value-of select="article/front/article-meta/pub-date/year"/>
							</year>
						</publication_date>
						<isbn>
							<xsl:value-of select="article/front/journal-meta/issn"/>
						</isbn>
						<publisher>
							<publisher_name>
								<xsl:value-of select="article/front/journal-meta/publisher/publisher-name"/>
							</publisher_name>
							<publisher_place>
								<xsl:value-of select="article/front/journal-meta/publisher/publisher-loc"/>
							</publisher_place>
						</publisher>
					</book_metadata>
					<content_item>
						<titles>
							<title>
								<xsl:value-of select="article/front/article-meta/title-group/article-title"/>
							</title>
						</titles>
						<doi_data>
							<xsl:value-of select="article/front/article-meta/article-id/@pub-id-type"/>
						</doi_data>
						<contributors>
							<person_name>
								<given_name>
									<xsl:value-of select="article/front/article-meta/contrib-group/contrib/name/given-names"/>
								</given_name>
								<surname>
									<xsl:value-of select="article/front/article-meta/contrib-group/contrib/name/surname"/>
								</surname>
							</person_name>
						</contributors>
						<pages>
							<first_page>
								<xsl:value-of select="article/front/article-meta/fpage"/>
							</first_page>
							<last_page>
								<xsl:value-of select="article/front/article-meta/lpage"/>
							</last_page>
						</pages>
					</content_item>
				</book>
			</body>
		</doi_batch>
	</xsl:template>
</xsl:stylesheet><!-- Stylus Studio meta-information - (c) 2004-2006. Progress Software Corporation. All rights reserved.
<metaInformation>
<scenarios ><scenario default="yes" name="pone.0000011.xml" userelativepaths="yes" externalpreview="no" url="..\..\..\..\..\..\install\pone.0000011.xml" htmlbaseurl="" outputurl="" processortype="internal" useresolver="yes" profilemode="0" profiledepth="" profilelength="" urlprofilexml="" commandline="" additionalpath="" additionalclasspath="" postprocessortype="none" postprocesscommandline="" postprocessadditionalpath="" postprocessgeneratedext="" validateoutput="no" validator="internal" customvalidator=""/></scenarios><MapperMetaTag><MapperInfo srcSchemaPathIsRelative="yes" srcSchemaInterpretAsXML="no" destSchemaPath="..\..\..\..\..\..\docs\book303.xml" destSchemaRoot="doi_batch" destSchemaPathIsRelative="yes" destSchemaInterpretAsXML="no" ><SourceSchema srcSchemaPath="..\..\..\..\..\..\install\pone.0000011.xml" srcSchemaRoot="article" AssociatedInstance="" loaderFunction="document" loaderFunctionUsesURI="no"/></MapperInfo><MapperBlockPosition><template match="/"></template></MapperBlockPosition><TemplateContext></TemplateContext><MapperFilter side="source"></MapperFilter></MapperMetaTag>
</metaInformation>
-->