<?xml version="1.0" encoding="UTF-8"?>

<deployment
    xmlns="http://xml.apache.org/axis/wsdd/"
    xmlns:wsdd="http://xml.apache.org/axis/wsdd/"
    xmlns:java="http://xml.apache.org/axis/wsdd/providers/java"
    xsl:version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xml:space="preserve">

  <handler type="java:org.apache.axis.handlers.http.URLMapper" name="URLMapper"/>
  <handler type="java:org.apache.axis.transport.local.LocalResponder" name="LocalResponder"/>

  <xsl:copy-of select="/wsdd:deployment/wsdd:service"/>

  <transport name="http">
    <parameter name="qs:list"   value="org.apache.axis.transport.http.QSListHandler"/>
    <parameter name="qs:method" value="org.apache.axis.transport.http.QSMethodHandler"/>
    <parameter name="qs:wsdl"   value="org.apache.axis.transport.http.QSWSDLHandler"/>
    <requestFlow>
      <handler type="URLMapper"/>
    </requestFlow>
  </transport>

  <transport name="local">
    <responseFlow>
      <handler type="LocalResponder"/>
    </responseFlow>
  </transport>
</deployment>
