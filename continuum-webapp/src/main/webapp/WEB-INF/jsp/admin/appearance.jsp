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
<html>
<s:i18n name="localization.Continuum">
<head>
  <title><s:text name="appearance.page.title"/></title>
</head>

<body>

<div class="h3">
<h3><s:text name="appearance.companyDetails"/></h3>

  <s:if test="hasActionErrors()">
    <div class="errormessage">
      <s:actionerror/>
    </div>
  </s:if>
  <s:if test="hasActionMessages()">
    <div class="warningmessage">
      <s:actionmessage/>
    </div>
  </s:if>

<div style="float: right">
  <a href="<s:url action='editAppearance' />"><s:text name="edit"/></a>
</div>

<p>
  <s:text name="appearance.description"/>
</p>

<s:set name="companyPom" value="companyPom"/>

<s:if test="companyPom.groupId.length() > 0 && companyPom.artifactId.length() > 0">

  <p>
    <s:text name="appearance.detailsIntroduction"/> <s:text name="appearance.maybeChange"/>
    <a href="<s:url action='editCompanyPom'/>"><s:text name="appearance.editThePomLink"/></a>.
  </p>

  <s:set name="companyModel" value="companyModel"/>
  <table>
    <s:label name="companyPom.groupId" label="%{getText('appearance.companyPom.groupId')}"/>
    <s:label name="companyPom.artifactId" label="%{getText('appearance.companyPom.artifactId')}"/>
    <s:if test="companyModel != null">
      <s:label name="companyModel.version" label="%{getText('appearance.companyPom.version')}"/>
    </s:if>
  </table>

  <div style="float: right">
    <a href="<s:url action='editCompanyPom' />"><s:text name="appearance.editCompanyPom"/></a>
  </div>
  <h3><s:text name="appearance.companyPom.section.title"/></h3>

    <s:if test="companyModel != null">
      <table>
        <tr>
          <th><s:text name="appearance.companyPom.organizationName.label"/></th>
          <td><s:property value="companyModel.organization.name"/></td>
        </tr>
        <tr>
          <th><s:text name="appearance.companyPom.organizationUrl.label"/></th>
          <s:set var="companyOrgUrl" value="companyModel.organization.url" />
          <td><s:a href="%{#companyOrgUrl}" target="_blank">
            <code><s:property value="#companyOrgUrl"/></code>
          </s:a></td>
        </tr>
        <tr>
          <th><s:text name="appearance.companyPom.organizationLogoUrl.label"/></th>
          <td>
            <code><s:property value="companyModel.properties['organization.logo']"/></code>
          </td>
        </tr>
      </table>
    </s:if>
    <s:else>
      <s:text name="appearance.companyPomDoesNotExist">
        <s:param>
          <s:property value="companyPom.groupId + ':' + companyPom.artifactId"/>
        </s:param>
      </s:text>
      <a href="<s:url action='editCompanyPom' />"><s:text name="appearance.createCompanyPom"/></a>
    </s:else>
</s:if>
<s:else>
    <p>
      <s:text name="appearance.noCompanyPom"/> <a href="<s:url action='editAppearance' />"><s:text name="appearance.selectCompanyPom"/></a>
    </p>
</s:else>
</div>

<s:form action="saveFooter" method="post" namespace="/admin">
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
