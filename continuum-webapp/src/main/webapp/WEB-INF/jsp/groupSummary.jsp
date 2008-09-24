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

<%@ taglib uri="/webwork" prefix="ww" %>
<%@ taglib uri="http://www.extremecomponents.org" prefix="ec" %>
<%@ taglib uri='http://java.sun.com/jsp/jstl/core' prefix='c' %>
<%@ taglib uri="http://plexus.codehaus.org/redback/taglib-1.0" prefix="redback" %>

<html>
<ww:i18n name="localization.Continuum">
  <head>
    <title><ww:text name="groups.page.title"/></title>
    <meta http-equiv="refresh" content="300"/>
  </head>

  <body>
  <div id="h3">

    <ww:if test="${infoMessage != null}">
       <p>${infoMessage}</p>
    </ww:if>
    <ww:else>
       <h3><ww:text name="groups.page.section.title"/></h3>
    </ww:else>
  
    <ww:if test="${empty groups}">
      <ww:text name="groups.page.list.empty"/>
    </ww:if>

    <ww:if test="${not empty groups}">

    <ec:table items="groups"
              var="group"
              showExports="false"
              showPagination="false"
              showStatusBar="false"
              sortable="false"
              filterable="false">
      <ec:row highlightRow="true">
        <ec:column property="name" title="groups.table.name" width="20%" style="white-space: nowrap">
          <a href="<ww:url  action="projectGroupSummary" namespace="/"><ww:param name="projectGroupId" value="%{'${group.id}'}"/></ww:url>">${group.name}</a>
        </ec:column>
        <ec:column property="groupId" title="groups.table.groupId" width="20%"/>
        <ec:column property="repositoryName" title="groups.table.repositoryName" width="20%">
          <redback:ifAuthorized permission="continuum-manage-repositories">
            <ww:url id="editRepositoryUrl" action="editRepository" namespace="/admin" includeParams="none">
              <ww:param name="repository.id">${pageScope.group.repositoryId}</ww:param>
            </ww:url>
            <ww:a href="%{editRepositoryUrl}">${pageScope.group.repositoryName}</ww:a> 
          </redback:ifAuthorized>
          <redback:elseAuthorized>
            ${pageScope.group.repositoryName}
          </redback:elseAuthorized>
        </ec:column>
        <ec:column property="buildGroupNowAction" title="&nbsp;" width="1%">
          <redback:ifAuthorized permission="continuum-build-group" resource="${group.name}">
            <ww:url id="buildProjectGroupUrl" action="buildProjectGroup" namespace="/" includeParams="none">
              <ww:param name="projectGroupId" value="${group.id}"/>
              <ww:param name="buildDefinitionId" value="-1"/>
              <ww:param name="fromSummaryPage" value="true"/>
            </ww:url>
            <ww:a href="%{buildProjectGroupUrl}">
              <img src="<ww:url value='/images/buildnow.gif'/>" alt="<ww:text name="projectGroup.buildGroup"/>" title="<ww:text name="projectGroup.buildGroup"/>" border="0">
            </ww:a>
          </redback:ifAuthorized>
          <redback:elseAuthorized>
            <img src="<ww:url value='/images/buildnow_disabled.gif'/>" alt="<ww:text name="projectGroup.buildGroup"/>" title="<ww:text name="projectGroup.buildGroup"/>" border="0">
          </redback:elseAuthorized>
        </ec:column>
        <ec:column property="releaseProjectGroupAction" title="&nbsp;" width="1%">
          <redback:ifAuthorized permission="continuum-build-group" resource="${group.name}">
            <ww:url id="releaseProjectGroupUrl" action="releaseProjectGroup" namespace="/" includeParams="none">
              <ww:param name="projectGroupId" value="${group.id}"/>
            </ww:url>
            <ww:a href="%{releaseProjectGroupUrl}">
              <img src="<ww:url value='/images/releaseproject.gif'/>" alt="<ww:text name="projectGroup.releaseNow"/>" title="<ww:text name="projectGroup.releaseNow"/>" border="0">
            </ww:a>
          </redback:ifAuthorized>
          <redback:elseAuthorized>
            <img src="<ww:url value='/images/releaseproject_disabled.gif'/>" alt="<ww:text name="projectGroup.releaseNow"/>" title="<ww:text name="projectGroup.releaseNow"/>" border="0">
          </redback:elseAuthorized>
        </ec:column>
        <ec:column property="removeProjectGroupAction" title="&nbsp;" width="1%">
          <redback:ifAuthorized permission="continuum-remove-group" resource="${group.name}">
            <ww:url id="removeProjectGroupUrl" action="removeProjectGroup" namespace="/" includeParams="none">
              <ww:param name="projectGroupId" value="${group.id}"/>
            </ww:url>
            <ww:a href="%{removeProjectGroupUrl}">
              <img src="<ww:url value='/images/delete.gif'/>" alt="<ww:text name="projectGroup.deleteGroup"/>" title="<ww:text name="projectGroup.deleteGroup"/>" border="0">
            </ww:a>
          </redback:ifAuthorized>
          <redback:elseAuthorized>
            <img src="<ww:url value='/images/delete_disabled.gif'/>" alt="<ww:text name="projectGroup.deleteGroup"/>" title="<ww:text name="projectGroup.deleteGroup"/>" border="0">
          </redback:elseAuthorized>
        </ec:column>
        <ec:column property="numSuccesses" title="&nbsp;" format="0" width="2%" style="text-align: right" headerClass="calcHeaderSucces" calc="total" calcTitle="groups.table.summary"/>
        <ec:column property="numFailures" title="&nbsp;" format="0" width="2%" style="text-align: right" headerClass="calcHeaderFailure" calc="total" />
        <ec:column property="numErrors" title="&nbsp;" format="0" width="2%" style="text-align: right" headerClass="calcHeaderError" calc="total"/>
        <ec:column property="numProjects" title="groups.table.totalProjects" format="0" width="1%" style="text-align: right" headerStyle="text-align: center" calc="total"/>
      </ec:row>
    </ec:table>
    </ww:if>
    <redback:ifAuthorized permission="continuum-add-group">
      <div class="functnbar3">
        <table>
          <tr>
            <td>
              <form action="<ww:url  action='addProjectGroup' method='input' namespace='/' />" method="post">
                <input type="submit" name="addProjectGroup" value="<ww:text name="projectGroup.add.section.title"/>"/>
              </form>
            </td>
          </tr>
        </table>
      </div>
    </redback:ifAuthorized>
  </div>
  </body>
</ww:i18n>
</html>
