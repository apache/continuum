<%--
  ~ Licensed to the Apache Software Foundation (ASF) under one
  ~ or more contributor license agreements.  See the NOTICE file
  ~ distributed with this work for additional information
  ~ regarding copyright ownership.  The ASF licenses this file
  ~ to you under the Apache License, Version 2.0 (the
  ~ "License"); you may not use this file except in compliance
  ~ with the License.  You may obtain a copy of the License at
  ~
  ~   http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing,
  ~ software distributed under the License is distributed on an
  ~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  ~ KIND, either express or implied.  See the License for the
  ~ specific language governing permissions and limitations
  ~ under the License.
  --%>

<%@ taglib prefix="ww" uri="/webwork" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib uri="continuum" prefix="c1" %>
<html>
<ww:i18n name="localization.Continuum">
<head>
  <title><ww:text name="appearance.page.title"/></title>
  <ww:head/>
</head>

<body>
<h1><ww:text name="appearance.section.title"/></h1>

<div style="float: right">
  <a href="<ww:url action='editAppearance' />"><ww:text name="edit"/></a>
</div>
<h2><ww:text name="appearance.companyDetails"/></h2>

<p>
  <ww:text name="appearance.description"/>
</p>

<ww:set name="companyPom" value="companyPom"/>

<c:if test="${empty (companyPom.groupId) || empty (companyPom.artifactId)}">
  <p>
    <ww:text name="appearance.noCompanyPom"/> <a href="<ww:url action='editAppearance' />"><ww:text name="appearance.selectCompanyPom"/></a>
  </p>
</c:if>

<c:if test="${!empty (companyPom.groupId) && !empty (companyPom.artifactId)}">
  <p>
    <ww:text name="appearance.detailsIntroduction"/> <ww:text name="appearance.maybeChange"/>
    <a href="<ww:url action='editCompanyPom'/>"><ww:text name="appearance.editThePomLink"/></a>.
  </p>

  <ww:set name="companyModel" value="companyModel"/>
  <table>
    <ww:label name="companyPom.groupId" label="%{getText('appearance.companyPom.groupId')}"/>
    <ww:label name="companyPom.artifactId" label="%{getText('appearance.companyPom.artifactId')}"/>
    <c:if test="${companyModel != null}">
      <ww:label name="companyModel.version" label="%{getText('appearance.companyPom.version')}"/>
    </c:if>
  </table>

  <div style="float: right">
    <a href="<ww:url action='editCompanyPom' />"><ww:text name="appearance.editCompanyPom"/></a>
  </div>
  <h3><ww:text name="appearance.companyPom.section.title"/></h3>

  <c:choose>
    <c:when test="${companyModel != null}">
      <table>
        <tr>
          <th><ww:text name="appearance.companyPom.organizationName.label"/></th>
          <td>${companyModel.organization.name}</td>
        </tr>
        <tr>
          <th><ww:text name="appearance.companyPom.organizationUrl.label"/></th>
          <td><a href="${companyModel.organization.url}" target="_blank">
            <code>${companyModel.organization.url}</code>
          </a></td>
        </tr>
        <tr>
          <th><ww:text name="appearance.companyPom.organizationLogoUrl.label"/></th>
          <td>
            <code>${companyModel.properties['organization.logo']}</code>
          </td>
        </tr>
      </table>
    </c:when>
    <c:otherwise>
      <ww:text name="appearance.companyPomDoesNotExist"><ww:param>${companyPom.groupId}:${companyPom.artifactId}</ww:param></ww:text>
      <a href="<ww:url action='editCompanyPom' />"><ww:text name="appearance.createCompanyPom"/></a>
    </c:otherwise>
  </c:choose>
</c:if>
<ww:form action="saveFooter!saveFooter.action" method="get" namespace="/admin">
  <div id="axial" class="h3">
    <h3><ww:text name="appearance.footerContent"/></h3>
    <div class="axial">
      <table>
        <tbody>  
          <ww:textarea cols="120" rows="3" label="%{getText('appearance.htmlContent.label')}" name="footer" />
        </tbody>
      </table>
      <div class="functnbar3">
        <c1:submitcancel value="%{getText('save')}" cancel="%{getText('cancel')}"/>
      </div>      
    </div>
  </div>
</ww:form>
</body>
</ww:i18n>
</html>
