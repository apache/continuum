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
<%@ taglib uri="http://www.extremecomponents.org" prefix="ec" %>
<%@ taglib uri="http://plexus.codehaus.org/redback/taglib-1.0" prefix="redback" %>

<s:i18n name="localization.Continuum">
  <ec:table items="allBuildDefinitionSummaries"
            var="buildDefinitionSummary"
            autoIncludeParameters="false"
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
          <s:url id="scheduleUrl" action="schedule" namespace="/" includeParams="none">
            <s:param name="id" value="#attr['buildDefinitionSummary'].scheduleId"/>
          </s:url>
          <s:a href="%{scheduleUrl}"><s:property value="#attr['buildDefinitionSummary'].scheduleName"/></s:a>
        </redback:ifAuthorized>
        <redback:elseAuthorized>
          <s:property value="#attr['buildDefinitionSummary'].scheduleName"/>
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
          <s:url id="buildProjectUrl" action="buildProject" namespace="/">
            <s:param name="projectId" value="#attr['projectId']"/>
            <s:param name="buildDefinitionId" value="#attr['buildDefinitionSummary'].id"/>
            <s:param name="fromProjectPage" value="true"/>
          </s:url>
          <s:a href="%{buildProjectUrl}"><img src="<s:url value='/images/buildnow.gif' includeParams="none"/>" alt="<s:text name='build'/>" title="<s:text name='build'/>" border="0"></s:a>
        </redback:ifAuthorized>
        <redback:elseAuthorized>
          <img src="<s:url value='/images/buildnow_disabled.gif' includeParams="none"/>" alt="<s:text name='build'/>" title="<s:text name='build'/>" border="0" />
        </redback:elseAuthorized>
      </ec:column>
      <ec:column property="editAction" title="&nbsp;" width="1%">
        <%-- if the from is PROJECT then render the links differently --%>
          <s:if test="#attr['buildDefinitionSummary'].from=='PROJECT'">
            <redback:ifAuthorized permission="continuum-modify-project-build-definition" resource="${projectGroupName}">
              <s:url id="editUrl" action="buildDefinition" namespace="/">
                <s:param name="projectId" value="#attr['projectId']"/>
                <s:param name="buildDefinitionId" value="#attr['buildDefinitionSummary'].id"/>
              </s:url>
              <s:a href="%{editUrl}"><img src="<s:url value='/images/edit.gif' includeParams="none"/>" alt="<s:text name='edit'/>" title="<s:text name='edit'/>" border="0"></s:a>
            </redback:ifAuthorized>
            <redback:elseAuthorized>
              <img src="<s:url value='/images/edit_disabled.gif' includeParams="none"/>" alt="<s:text name='edit'/>" title="<s:text name='edit'/>" border="0" />
            </redback:elseAuthorized>
          </s:if>
          <s:else>
            <redback:ifAuthorized permission="continuum-modify-group-build-definition" resource="${projectGroupName}">
              <s:url id="editUrl" action="buildDefinition" namespace="/">
                <s:param name="projectGroupId" value="#attr['buildDefinitionSummary'].projectGroupId"/>
                <s:param name="buildDefinitionId" value="#attr['buildDefinitionSummary'].id"/>
                <s:param name="groupBuildDefinition">true</s:param>
              </s:url>
              <s:a href="%{editUrl}"><img src="<s:url value='/images/edit.gif' includeParams="none"/>" alt="<s:text name='edit'/>" title="<s:text name='edit'/>" border="0"></s:a>
            </redback:ifAuthorized>
            <redback:elseAuthorized>
              <img src="<s:url value='/images/edit_disabled.gif' includeParams="none"/>" alt="<s:text name='edit'/>" title="<s:text name='edit'/>" border="0" />
            </redback:elseAuthorized>
          </s:else>
      </ec:column>
      <ec:column property="deleteAction" title="&nbsp;" width="1%">
        <%-- if the from is PROJECT then render the links differently --%>
          <s:if test="#attr['buildDefinitionSummary'].from=='PROJECT'">
            <redback:ifAuthorized permission="continuum-remove-project-build-definition" resource="${projectGroupName}">
              <s:set var="tname" value="'delToken' + #attr['buildDefinitionSummary'].id" scope="page" />
              <s:token name="%{#attr['tname']}"/>
              <s:url id="removeUrl" action="removeProjectBuildDefinition" namespace="/">
                <s:param name="projectId" value="#attr['projectId']"/>
                <s:param name="buildDefinitionId" value="#attr['buildDefinitionSummary'].id"/>
                <s:param name="confirmed" value="false"/>
                <s:param name="struts.token.name" value="#attr['tname']"/>
                <s:param name="%{#attr['tname']}" value="#session['struts.tokens.' + #attr['tname']]"/>
              </s:url>
              <s:set id="removeId">remove-build-definition-${pageScope.buildDefinitionSummary.id}</s:set>
              <s:a href="%{removeUrl}" id="%{removeId}"><img src="<s:url value='/images/delete.gif' includeParams="none"/>" alt="<s:text name='delete'/>" title="<s:text name='delete'/>" border="0"></s:a>
            </redback:ifAuthorized>
            <redback:elseAuthorized>
              <img src="<s:url value='/images/delete_disabled.gif' includeParams="none"/>" alt="<s:text name='delete'/>" title="<s:text name='delete'/>" border="0" />
            </redback:elseAuthorized>
          </s:if>
          <s:else>
            <redback:ifAuthorized permission="continuum-remove-group-build-definition" resource="${projectGroupName}">
                <s:if test="#attr['buildDefinitionSummary'].id == defaultGroupDefinitionId || #attr['buildDefinitionSummary'].isDefault">
                  <img src="<s:url value='/images/delete_disabled.gif' includeParams="none"/>" alt="<s:text name='delete'/>" title="<s:text name='delete'/>" border="0" />
                </s:if>
                <s:else>
                  <s:set var="tname" value="'delGroupToken' + #attr['buildDefinitionSummary'].id" scope="page"/>
                  <s:token name="%{#attr['tname']}"/>
                  <s:url id="removeUrl" action="removeGroupBuildDefinition" namespace="/">
                    <s:param name="projectGroupId" value="#attr['buildDefinitionSummary'].projectGroupId"/>
                    <s:param name="buildDefinitionId" value="#attr['buildDefinitionSummary'].id"/>
                    <s:param name="groupBuildDefinition">true</s:param>
                    <s:param name="confirmed" value="false"/>
                    <s:param name="struts.token.name" value="#attr['tname']"/>
                    <s:param name="%{#attr['tname']}" value="#session['struts.tokens.' + #attr['tname']]"/>
                  </s:url>
                  <s:a href="%{removeUrl}"><img src="<s:url value='/images/delete.gif' includeParams="none"/>" alt="<s:text name='delete'/>" title="<s:text name='delete'/>" border="0"></s:a>
                </s:else>
            </redback:ifAuthorized>
            <redback:elseAuthorized>
              <img src="<s:url value='/images/delete_disabled.gif' includeParams="none"/>" alt="<s:text name='delete'/>" title="<s:text name='delete'/>" border="0" />
            </redback:elseAuthorized>
          </s:else>
      </ec:column>
    </ec:row>
  </ec:table>
</s:i18n>
