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
  <ec:table items="allBuildDefinitionSummaries"
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
      <ec:column property="profileName" title="projectView.buildDefinition.profile"/>
      <ec:column property="from" title="projectView.buildDefinition.from"/>
      <ec:column property="isBuildFresh" title="projectView.buildDefinition.buildFresh"/>
      <ec:column property="isDefault" title="projectView.buildDefinition.default"/>
      <ec:column property="description" title="projectView.buildDefinition.description"/>
      <ec:column property="type" title="projectView.buildDefinition.type"/>      
      <ec:column property="buildAction" title="&nbsp;" width="1%">
        <redback:ifAuthorized permission="continuum-build-group" resource="${projectGroupName}">
          <ww:url id="buildProjectUrl" action="buildProject" namespace="/">
            <ww:param name="projectId">${projectId}</ww:param>
            <ww:param name="buildDefinitionId">${pageScope.buildDefinitionSummary.id}</ww:param>
            <ww:param name="fromProjectPage" value="true"/>
          </ww:url>
          <ww:a href="%{buildProjectUrl}"><img src="<ww:url value='/images/buildnow.gif'/>" alt="<ww:text name='build'/>" title="<ww:text name='build'/>" border="0"></ww:a>
        </redback:ifAuthorized>
        <redback:elseAuthorized>
          <img src="<ww:url value='/images/buildnow_disabled.gif'/>" alt="<ww:text name='build'/>" title="<ww:text name='build'/>" border="0" />
        </redback:elseAuthorized>
      </ec:column>
      <ec:column property="editAction" title="&nbsp;" width="1%">
        <%-- if the from is PROJECT then render the links differently --%>
        <ww:if test="${pageScope.buildDefinitionSummary.from == 'PROJECT'}">
          <redback:ifAuthorized permission="continuum-modify-project-build-definition" resource="${projectGroupName}">
            <ww:url id="editUrl" action="buildDefinition" method="input" namespace="/">
              <ww:param name="projectId">${projectId}</ww:param>
              <ww:param name="buildDefinitionId">${pageScope.buildDefinitionSummary.id}</ww:param>
            </ww:url>
            <ww:a href="%{editUrl}"><img src="<ww:url value='/images/edit.gif'/>" alt="<ww:text name='edit'/>" title="<ww:text name='edit'/>" border="0"></ww:a>
          </redback:ifAuthorized>
          <redback:elseAuthorized>
            <img src="<ww:url value='/images/edit_disabled.gif'/>" alt="<ww:text name='edit'/>" title="<ww:text name='edit'/>" border="0" />
          </redback:elseAuthorized>
        </ww:if>
        <ww:else>
          <redback:ifAuthorized permission="continuum-modify-group-build-definition" resource="${projectGroupName}">
            <ww:url id="editUrl" action="buildDefinition" method="input" namespace="/">
              <ww:param name="projectGroupId">${pageScope.buildDefinitionSummary.projectGroupId}</ww:param>
              <ww:param name="buildDefinitionId">${pageScope.buildDefinitionSummary.id}</ww:param>
              <ww:param name="groupBuildDefinition">true</ww:param>
            </ww:url>
            <ww:a href="%{editUrl}"><img src="<ww:url value='/images/edit.gif'/>" alt="<ww:text name='edit'/>" title="<ww:text name='edit'/>" border="0"></ww:a>
          </redback:ifAuthorized>
          <redback:elseAuthorized>
            <img src="<ww:url value='/images/edit_disabled.gif'/>" alt="<ww:text name='edit'/>" title="<ww:text name='edit'/>" border="0" />
          </redback:elseAuthorized>
        </ww:else>
      </ec:column>
      <ec:column property="deleteAction" title="&nbsp;" width="1%">
        <%-- if the from is PROJECT then render the links differently --%>
        <ww:if test="${pageScope.buildDefinitionSummary.from == 'PROJECT'}">
          <redback:ifAuthorized permission="continuum-remove-project-build-definition" resource="${projectGroupName}">
            <ww:url id="removeUrl" action="removeProjectBuildDefinition" namespace="/">
              <ww:param name="projectId">${projectId}</ww:param>
              <ww:param name="buildDefinitionId">${pageScope.buildDefinitionSummary.id}</ww:param>
              <ww:param name="confirmed" value="false"/>
            </ww:url>
            <ww:a href="%{removeUrl}"><img src="<ww:url value='/images/delete.gif'/>" alt="<ww:text name='delete'/>" title="<ww:text name='delete'/>" border="0"></ww:a>
          </redback:ifAuthorized>
          <redback:elseAuthorized>
            <img src="<ww:url value='/images/delete_disabled.gif'/>" alt="<ww:text name='delete'/>" title="<ww:text name='delete'/>" border="0" />
          </redback:elseAuthorized>
        </ww:if>
        <ww:else>
          <redback:ifAuthorized permission="continuum-remove-group-build-definition" resource="${projectGroupName}">
            <ww:url id="removeUrl" action="removeGroupBuildDefinition" namespace="/">
              <ww:param name="projectGroupId">${pageScope.buildDefinitionSummary.projectGroupId}</ww:param>
              <ww:param name="buildDefinitionId">${pageScope.buildDefinitionSummary.id}</ww:param>
              <ww:param name="groupBuildDefinition">true</ww:param>
              <ww:param name="confirmed" value="false"/>
            </ww:url>
            <ww:a href="%{removeUrl}"><img src="<ww:url value='/images/delete.gif'/>" alt="<ww:text name='delete'/>" title="<ww:text name='delete'/>" border="0"></ww:a>
          </redback:ifAuthorized>
          <redback:elseAuthorized>
            <img src="<ww:url value='/images/delete_disabled.gif'/>" alt="<ww:text name='delete'/>" title="<ww:text name='delete'/>" border="0" />
          </redback:elseAuthorized>
        </ww:else>
      </ec:column>
    </ec:row>
  </ec:table>
</ww:i18n>
