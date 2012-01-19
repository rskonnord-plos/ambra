<?xml version="1.0" encoding="UTF-8"?> 
<!--
  $HeadURL::                                                                                      $
  $Id$

  Copyright (c) 2006-2008 by Topaz, Inc.
  http://topazproject.org

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at
 
      http://www.apache.org/licenses/LICENSE-2.0
 
  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
-->

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
