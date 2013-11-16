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

<%@ taglib prefix="s" uri="/struts-tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<s:i18n name="localization.Continuum">
<head>
  <title><s:text name="appearance.page.title"/></title>
</head>

<body>
<h1><s:text name="appearance.section.title"/></h1>

<div style="float: right">
  <a href="<s:url action='editAppearance' />"><s:text name="edit"/></a>
</div>
<h2><s:text name="appearance.companyDetails"/></h2>

<p>
  <s:text name="appearance.description"/>
</p>

<s:set name="companyPom" value="companyPom"/>

<c:if test="${empty (companyPom.groupId) || empty (companyPom.artifactId)}">
  <p>
    <s:text name="appearance.noCompanyPom"/> <a href="<s:url action='editAppearance' />"><s:text name="appearance.selectCompanyPom"/></a>
  </p>
</c:if>

<c:if test="${!empty (companyPom.groupId) && !empty (companyPom.artifactId)}">
  <p>
    <s:text name="appearance.detailsIntroduction"/> <s:text name="appearance.maybeChange"/>
    <a href="<s:url action='editCompanyPom'/>"><s:text name="appearance.editThePomLink"/></a>.
  </p>

  <s:set name="companyModel" value="companyModel"/>
  <table>
    <s:label name="companyPom.groupId" label="%{getText('appearance.companyPom.groupId')}"/>
    <s:label name="companyPom.artifactId" label="%{getText('appearance.companyPom.artifactId')}"/>
    <c:if test="${companyModel != null}">
      <s:label name="companyModel.version" label="%{getText('appearance.companyPom.version')}"/>
    </c:if>
  </table>

  <div style="float: right">
    <a href="<s:url action='editCompanyPom' />"><s:text name="appearance.editCompanyPom"/></a>
  </div>
  <h3><s:text name="appearance.companyPom.section.title"/></h3>

  <c:choose>
    <c:when test="${companyModel != null}">
      <table>
        <tr>
          <th><s:text name="appearance.companyPom.organizationName.label"/></th>
          <td><c:out value="${companyModel.organization.name}"/></td>
        </tr>
        <tr>
          <th><s:text name="appearance.companyPom.organizationUrl.label"/></th>
          <c:set var="companyOrgUrl"><c:out value="${companyModel.organization.url}"/></c:set>
          <td><a href="${companyOrgUrl}" target="_blank">
            <code><c:out value="${companyModel.organization.url}"/></code>
          </a></td>
        </tr>
        <tr>
          <th><s:text name="appearance.companyPom.organizationLogoUrl.label"/></th>
          <td>
            <code><c:out value="${companyModel.properties['organization.logo']}"/></code>
          </td>
        </tr>
      </table>
    </c:when>
    <c:otherwise>
      <s:text name="appearance.companyPomDoesNotExist">
        <s:param>
          <c:out value="${companyPom.groupId}"/>:<c:out value="${companyPom.artifactId}"/>
        </s:param>
      </s:text>
      <a href="<s:url action='editCompanyPom' />"><s:text name="appearance.createCompanyPom"/></a>
    </c:otherwise>
  </c:choose>
</c:if>

<s:actionmessage/>
<s:form action="saveFooter!saveFooter.action" method="post" namespace="/admin">
  <s:token/>
  <div id="axial" class="h3">
    <h3><s:text name="appearance.footerContent"/></h3>
    <div class="axial">
      <table>
        <tbody>  
          <s:textarea cols="120" rows="3" label="%{getText('appearance.htmlContent.label')}" name="footer" />
        </tbody>
      </table>
      <div class="functnbar3">
        <s:submit value="%{getText('save')}" theme="simple"/>
        <input type="button" name="Cancel" value="<s:text name='cancel'/>" onclick="history.back();"/>
      </div>      
    </div>
  </div>
</s:form>
</body>
</s:i18n>
</html>
