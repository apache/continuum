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
<%@ taglib uri='http://java.sun.com/jsp/jstl/core' prefix='c'%>
<%@ taglib uri="continuum" prefix="c1" %>
<%@ taglib uri="http://plexus.codehaus.org/redback/taglib-1.0" prefix="redback" %>

<ww:i18n name="localization.Continuum">

  <h3><ww:text name="buildDefinitionSummary.projectGroup.section.title"><ww:param>${projectGroup.name}</ww:param></ww:text></h3>
  <ww:if test="${not empty groupBuildDefinitionSummaries}">
  <ec:table items="groupBuildDefinitionSummaries"
            var="buildDefinitionSummary"
            showExports="false"
            showPagination="false"
            showStatusBar="false"
            filterable="false"
            sortable="false">
    <ec:row>
      <ec:column property="goals" title="projectView.buildDefinition.goals"/>
      <ec:column property="arguments" title="projectView.buildDefinition.arguments"/>
      <ec:column property="buildFile" title="projectView.buildDefinition.buildFile"/>
      <ec:column property="scheduleName" title="projectView.buildDefinition.schedule">
        <redback:ifAuthorized permission="continuum-manage-schedules">
          <ww:url id="scheduleUrl" action="schedule" namespace="/" includeParams="none">
            <ww:param name="id">${pageScope.buildDefinitionSummary.scheduleId}</ww:param>
          </ww:url>
          <ww:a href="%{scheduleUrl}">${pageScope.buildDefinitionSummary.scheduleName}</ww:a> 
        </redback:ifAuthorized>
        <redback:elseAuthorized>
          ${pageScope.buildDefinitionSummary.scheduleName}
        </redback:elseAuthorized>
      </ec:column>
      <ec:column property="profileName" title="projectView.buildDefinition.profile">
        <ww:url id="profileUrl" action="editProfile!edit.action" namespace="/" includeParams="none">
          <ww:param name="profile.id">${pageScope.buildDefinitionSummary.profileId}</ww:param>
        </ww:url>    
        <ww:a href="%{profileUrl}">${pageScope.buildDefinitionSummary.profileName}</ww:a>     
      </ec:column>      
      <ec:column property="from" title="projectView.buildDefinition.from"/>
      <ec:column property="isBuildFresh" title="projectView.buildDefinition.buildFresh"/>
      <ec:column property="isDefault" title="projectView.buildDefinition.default"/>
      <ec:column property="description" title="projectView.buildDefinition.description"/>
      <ec:column property="buildAction" title="&nbsp;" width="1%">
        <redback:ifAuthorized permission="continuum-build-group" resource="${projectGroupName}">
          <ww:url id="buildUrl" action="buildProject" namespace="/">
            <ww:param name="projectGroupId">${pageScope.buildDefinitionSummary.projectGroupId}</ww:param>
            <ww:param name="buildDefinitionId">${pageScope.buildDefinitionSummary.id}</ww:param>
          </ww:url>
          <ww:a href="%{buildUrl}"><img src="<ww:url value='/images/buildnow.gif'/>" alt="<ww:text name='build'/>" title="<ww:text name='build'/>" border="0"></ww:a>
        </redback:ifAuthorized>
        <redback:elseAuthorized>
          <img src="<ww:url value='/images/buildnow_disabled.gif'/>" alt="<ww:text name='build'/>" title="<ww:text name='build'/>" border="0" />
        </redback:elseAuthorized>
      </ec:column>
      <ec:column property="editActions" title="&nbsp;" width="1%">
        <center>
        <redback:ifAuthorized permission="continuum-modify-group" resource="${projectGroupName}">
          <ww:url id="editUrl" action="buildDefinition" method="input" namespace="/" includeParams="none">
            <ww:param name="projectGroupId">${pageScope.buildDefinitionSummary.projectGroupId}</ww:param>
            <ww:param name="buildDefinitionId">${pageScope.buildDefinitionSummary.id}</ww:param>
          </ww:url>
          <ww:a href="%{editUrl}">
              <img src="<ww:url value='/images/edit.gif'/>" alt="<ww:text name='edit'/>" title="<ww:text name='edit'/>" border="0">
          </ww:a>
        </redback:ifAuthorized>
        <redback:elseAuthorized>
          <img src="<ww:url value='/images/edit_disabled.gif'/>" alt="<ww:text name='edit'/>" title="<ww:text name='edit'/>" border="0">
        </redback:elseAuthorized>
        </center>
      </ec:column>    
      <ec:column property="deleteActions" title="&nbsp;" width="1%">
        <center>
        <redback:ifAuthorized permission="continuum-modify-group" resource="${projectGroupName}">
          <ww:url id="removeUrl" action="removeGroupBuildDefinition" namespace="/">
            <ww:param name="projectGroupId">${pageScope.buildDefinitionSummary.projectGroupId}</ww:param>
            <ww:param name="buildDefinitionId">${pageScope.buildDefinitionSummary.id}</ww:param>
            <ww:param name="confirmed" value="false"/>
          </ww:url>
          <ww:a href="%{removeUrl}">
            <img src="<ww:url value='/images/delete.gif'/>" alt="<ww:text name='delete'/>" title="<ww:text name='delete'/>" border="0">
          </ww:a>
        </redback:ifAuthorized>
        <redback:elseAuthorized>
          <img src="<ww:url value='/images/delete_disabled.gif'/>" alt="<ww:text name='delete'/>" title="<ww:text name='delete'/>" border="0">
        </redback:elseAuthorized>
        </center>
      </ec:column>
    </ec:row>
  </ec:table>
  </ww:if>
  <redback:ifAuthorized permission="continuum-modify-group" resource="${projectGroupName}">
    <div class="functnbar3">
      <ww:form action="buildDefinition" method="post">
        <input type="hidden" name="projectGroupId" value="<ww:property value="projectGroupId"/>"/>
        <ww:submit value="%{getText('add')}"/>
      </ww:form>
    </div>
  </redback:ifAuthorized>

  <ww:if test="${not empty projectBuildDefinitionSummaries}">
  <h3>Project Build Definitions</h3>

  <ec:table items="projectBuildDefinitionSummaries"
            var="buildDefinitionSummary"
            showExports="false"
            showPagination="false"
            showStatusBar="false"
            filterable="false"
            sortable="false">
    <ec:row>
      <ec:column property="projectName" title="Project">
        <ww:url id="projectUrl" action="projectView" namespace="/" includeParams="none">
          <ww:param name="projectId" value="${pageScope.buildDefinitionSummary.projectId}"/>
        </ww:url>
        <ww:a href="%{projectUrl}">${pageScope.buildDefinitionSummary.projectName}</ww:a>
      </ec:column>
      <ec:column property="goals" title="projectView.buildDefinition.goals"/>
      <ec:column property="arguments" title="projectView.buildDefinition.arguments"/>
      <ec:column property="buildFile" title="projectView.buildDefinition.buildFile"/>
      <ec:column property="scheduleName" title="projectView.buildDefinition.schedule">
        <redback:ifAuthorized permission="continuum-manage-schedules">
          <ww:url id="scheduleUrl" action="schedule" namespace="/" includeParams="none">
            <ww:param name="id">${pageScope.buildDefinitionSummary.scheduleId}</ww:param>
          </ww:url>
          <ww:a href="%{scheduleUrl}">${pageScope.buildDefinitionSummary.scheduleName}</ww:a> 
        </redback:ifAuthorized>
        <redback:elseAuthorized>
          ${pageScope.buildDefinitionSummary.scheduleName}
        </redback:elseAuthorized>
      </ec:column>
      <ec:column property="profileName" title="projectView.buildDefinition.profile">
        <ww:url id="profileUrl" action="editProfile!edit.action" namespace="/" includeParams="none">
          <ww:param name="profile.id">${pageScope.buildDefinitionSummary.profileId}</ww:param>
        </ww:url>
        <ww:a href="%{profileUrl}">${pageScope.buildDefinitionSummary.profileName}</ww:a>        
      </ec:column>      
      <ec:column property="from" title="projectView.buildDefinition.from"/>
      <ec:column property="isBuildFresh" title="projectView.buildDefinition.buildFresh"/>
      <ec:column property="isDefault" title="projectView.buildDefinition.default"/>
      <ec:column property="description" title="projectView.buildDefinition.description"/>
      <ec:column property="buildNowAction" title="&nbsp;" width="1%">
        <redback:ifAuthorized permission="continuum-build-group" resource="${projectGroupName}">
          <ww:url id="buildProjectUrl" action="buildProject" namespace="/" includeParams="none">
            <ww:param name="projectId">${pageScope.buildDefinitionSummary.projectId}</ww:param>
            <ww:param name="buildDefinitionId">${pageScope.buildDefinitionSummary.id}</ww:param>
          </ww:url>
          <ww:a href="%{buildProjectUrl}">
            <img src="<c:url value='/images/buildnow.gif'/>" alt="<ww:text name='build'/>" title="<ww:text name='build'/>" border="0">
          </ww:a>
        </redback:ifAuthorized>
        <redback:elseAuthorized>
          <img src="<ww:url value='/images/buildnow_disabled.gif'/>" alt="<ww:text name='build'/>" title="<ww:text name='build'/>" border="0" />
        </redback:elseAuthorized>
      </ec:column>
      <ec:column property="editAction" title="&nbsp;" width="1%">
        <redback:ifAuthorized permission="continuum-modify-group" resource="${projectGroupName}">
          <ww:url id="editUrl" action="buildDefinition" method="input" namespace="/">
            <ww:param name="projectId">${pageScope.buildDefinitionSummary.projectId}</ww:param>
            <ww:param name="buildDefinitionId">${pageScope.buildDefinitionSummary.id}</ww:param>
          </ww:url>
          <ww:a href="%{editUrl}">
              <img src="<ww:url value='/images/edit.gif'/>" alt="<ww:text name='edit'/>" title="<ww:text name='edit'/>" border="0">          
          </ww:a>
        </redback:ifAuthorized>
        <redback:elseAuthorized>
          <img src="<ww:url value='/images/edit_disabled.gif'/>" alt="<ww:text name='edit'/>" title="<ww:text name='edit'/>" border="0">
        </redback:elseAuthorized>
      </ec:column>
      <ec:column property="removeAction" title="&nbsp;" width="1%">
        <redback:ifAuthorized permission="continuum-modify-group" resource="${projectGroupName}">
          <ww:url id="removeUrl" action="removeProjectBuildDefinition" namespace="/">
            <ww:param name="projectId">${pageScope.buildDefinitionSummary.projectId}</ww:param>
            <ww:param name="buildDefinitionId">${pageScope.buildDefinitionSummary.id}</ww:param>
            <ww:param name="confirmed" value="false"/>
          </ww:url>
          <ww:a href="%{removeUrl}">
              <img src="<ww:url value='/images/delete.gif'/>" alt="<ww:text name='delete'/>" title="<ww:text name='delete'/>" border="0">
          </ww:a>
        </redback:ifAuthorized>
        <redback:elseAuthorized>
           <img src="<ww:url value='/images/delete_disabled.gif'/>" alt="<ww:text name='delete'/>" title="<ww:text name='delete'/>" border="0"> 
        </redback:elseAuthorized>
      </ec:column>
    </ec:row>
  </ec:table>

  </ww:if>

</ww:i18n>
