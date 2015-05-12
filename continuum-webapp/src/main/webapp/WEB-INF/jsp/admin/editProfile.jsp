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

<%@ taglib uri="/struts-tags" prefix="s" %>
<%@ taglib uri="continuum" prefix="c1" %>
<%@ taglib uri="http://www.extremecomponents.org" prefix="ec" %>
<html>
<s:i18n name="localization.Continuum">
  <head>
    <title>
      <s:text name="profile.page.title"/>
    </title>
  </head>

  <body>
    <div id="axial" class="h3">
      <h3>
        <s:text name="profile.section.title"/>
      </h3>

      <div class="axial">
        <s:if test="hasActionErrors()">
          <h3><s:text name="profile.actionError"/></h3>
          <p>
            <s:actionerror/>
          </p>
        </s:if>
      </div>
      <div class="axial">
        <s:form action="saveBuildEnv" method="post">
        <!--  if other fields are added ProfileAction#save must be changed  -->
        <s:hidden name="profile.id" />
        <s:textfield label="%{getText('profile.name.label')}" name="profile.name"
                     requiredLabel="true" size="100" />
          <c1:ifBuildTypeEnabled buildType="distributed">
            <s:if test ="profile != null">
              <s:if test="profile.buildAgentGroup == null">
                <s:select label="%{getText('profile.build.agent.group')}" name="profile.buildAgentGroup" list="buildAgentGroups" listValue="name"
                          value="-1" listKey="name" headerKey="" headerValue=""/>
              </s:if>
              <s:else>
                <s:select label="%{getText('profile.build.agent.group')}" name="profile.buildAgentGroup" list="buildAgentGroups" listValue="name"
                          listKey="name" headerKey="" headerValue=""/>
              </s:else>
            </s:if>
          </c1:ifBuildTypeEnabled>
          <tr>
            <td colspan="2">
              <div class="functnbar3">
                <s:submit value="%{getText('save')}" theme="simple"/>
                <input type="button" name="Cancel" value="<s:text name='cancel'/>" onclick="history.back();"/>
              </div>
            </td>
          </tr>
        </s:form>
      </div>
      <s:if test="profile.id != 0">
        <div class="axial">
          <ec:table items="profileInstallations"
                    var="profileInstallation"
                    showExports="false"
                    showPagination="false"
                    showStatusBar="false"
                    sortable="false"
                    filterable="false"
                    width="100%"
                    autoIncludeParameters="false">
            <ec:row highlightRow="true">
              <ec:column property="nameEdit" title="profile.installation.name.label" style="white-space: nowrap" width="50%">
                <s:url var="editUrl" action="editInstallation">
                  <s:param name="installation.installationId" value="#attr.profileInstallation.installationId" />
                </s:url>
                <s:a href="%{editUrl}">
                  <s:property value="#attr.profileInstallation.name"/>
                </s:a>
                (<s:property value="#attr.profileInstallation.varValue"/>)
              </ec:column>
              <ec:column property="type" title="installation.type.label" style="white-space: nowrap" width="49%"/>
              <ec:column property="id" title="&nbsp;" width="1%">
                <s:url var="removeUrl" action="removeBuildEnvInstallation">
                  <s:param name="profile.id" value="profile.id"/>
                  <s:param name="installationId" value="#attr.profileInstallation.installationId" />
                </s:url>
                <s:a href="%{removeUrl}">
                  <img src="<s:url value='/images/delete.gif' includeParams="none"/>" alt="<s:text name='delete'/>" title="<s:text name='delete'/>" border="0" />
                </s:a>
              </ec:column>
            </ec:row>
          </ec:table>
          <s:if test="allInstallations.size() > 0">
            <s:form action="addInstallationBuildEnv" method="get">
              <s:hidden name="profile.id" />
              <div class="functnbar3">
                <!-- can't use default profile to display this select -->
                <s:select theme="simple" name="installationId" list="allInstallations" listKey="installationId" listValue="name" />
                <s:submit value="%{getText('add')}" theme="simple"/>
              </div>
            </s:form>
          </s:if>
          <s:else>
            <div class="warningmessage" style="color: red"><s:text name="profile.no.installations" /></div>
          </s:else>
        </div>
      </s:if>
      <s:else>
        <s:if test="allInstallations.size() <= 0">
          <div class="warningmessage" style="color: red"><s:text name="profile.no.installations" /></div>
        </s:if>
      </s:else>
    </div>
  </body>
</s:i18n>
</html>
