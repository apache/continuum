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
<s:if test="projects.size() > 0">

  <h3><s:text name="projectGroup.buildsStatut.title"/></h3>
  <table>
    <tr>
      <td>
          <s:text name="projectGroup.buildsStatut.success"/> : <s:property value="groupSummary.numSuccesses"/>
          &nbsp;<img src="<s:url value='/images/icon_success_sml.gif' includeParams="none"/>" alt="<s:text name="projectGroup.buildsStatut.success"/>">
          &nbsp; <s:text name="projectGroup.buildsStatut.errors"/> : <s:property value="groupSummary.numErrors"/>
          &nbsp;<img src="<s:url value='/images/icon_error_sml.gif' includeParams="none"/>" alt="<s:text name="projectGroup.buildsStatut.errors"/>">
          &nbsp; <s:text name="projectGroup.buildsStatut.failures"/> : <s:property value="groupSummary.numFailures"/>
          &nbsp;<img src="<s:url value='/images/icon_warning_sml.gif' includeParams="none"/>" alt="<s:text name="projectGroup.buildsStatut.failures"/>">
      <td>      
    </tr>
  </table>

  <h3><s:text name="projectGroup.projects.title"/></h3>

  <s:form id="projectsForm" action="projectsList" theme="simple">
    <s:hidden name="methodToCall" value="" />
    <s:hidden name="buildDefinitionId" value="-1" />
    <s:hidden name="projectGroupId" />
  <ec:table items="projects"
            var="project"
            autoIncludeParameters="false"
            showExports="false"
            showPagination="false"
            showStatusBar="false"
            sortable="false"
            filterable="false">
    <ec:row highlightRow="true">

      <%-- needed to access project in included pages --%>
      <s:set var="project" value="#attr['project']" scope="request"/>

      <%-- placed here for reusability --%>
      <s:set var="projectIdle" value="!#attr['project'].inBuildingQueue && (#attr['project'].state in {1, 2, 3, 4, 7} || #attr['project'].state > 8)" scope="request"/>

      <redback:ifAuthorized permission="continuum-modify-group" resource="${projectGroupName}">
        <ec:column alias="selectedProjects" title=" " style="width:5px" filterable="false" sortable="false" width="1%" headerCell="selectAll">
          <input type="checkbox" name="selectedProjects" value="${project.id}" />
        </ec:column>
      </redback:ifAuthorized>
      <ec:column property="state" title="&nbsp;" width="1%" cell="org.apache.maven.continuum.web.view.StateCell"/>
      <ec:column property="name" title="summary.projectTable.name" width="50%">
        <s:url id="projectUrl" action="projectView" namespace="/" includeParams="none">
          <s:param name="projectId" value="#attr['project'].id"/>
        </s:url>
        <s:a href="%{projectUrl}"><s:property value="#attr['project'].name"/></s:a>
      </ec:column>
      <ec:column property="version" title="summary.projectTable.version" width="12%"/>
      <ec:column property="buildNumber" title="summary.projectTable.build" width="2%" style="text-align: center">
          <s:if test="#attr['project'].buildNumber > 0 && #attr['project'].buildInSuccessId > 0">
            <redback:ifAuthorized permission="continuum-view-group" resource="${projectGroupName}">
              <s:url id="buildResult" action="buildResult">
                <s:param name="projecGroupId" value="#attr['project'].projectGroupId"/>
                <s:param name="projectId" value="#attr['project'].id"/>
                <s:param name="projectName" value="#attr['project'].name"/>
                <s:param name="buildId" value="#attr['project'].buildInSuccessId"/>
              </s:url>
              <s:a href="%{buildResult}"><s:property value="#attr['project'].buildNumber"/></s:a>
            </redback:ifAuthorized>
            <redback:elseAuthorized>
              <s:property value="#attr['project'].buildNumber"/>
            </redback:elseAuthorized>
          </s:if>
          <s:elseif test="#attr['project'].buildNumber > 0 && #attr['project'].buildInSuccessId < 0}">
              <s:property value="#attr['project'].buildNumber"/>
          </s:elseif>
          <s:else>
            &nbsp;
          </s:else>
      </ec:column>
      <ec:column property="lastBuildDateTime" title="summary.projectTable.lastBuildDateTime" width="30%" cell="date"/>
      <ec:column property="buildNowAction" title="&nbsp;" width="1%">
          <s:if test="#attr['project'].inBuildingQueue">
            <img src="<s:url value='/images/inqueue.gif' includeParams="none"/>" alt="<s:text name="legend.queuedBuild"/>" title="<s:text name="legend.queuedBuild"/>" border="0">
          </s:if>
          <s:else>
            <redback:ifAuthorized permission="continuum-build-group" resource="${projectGroupName}">
                <s:if test="#attr['projectIdle']">
                  <s:url id="buildProjectUrl" action="buildProject" namespace="/" includeParams="none">
                    <s:param name="projectId" value="#attr['project'].id"/>
                    <s:param name="projectGroupId" value="#attr['project'].projectGroupId"/>
                    <s:param name="fromGroupPage" value="true"/>
                  </s:url>
                  <s:a href="%{buildProjectUrl}">
                    <img src="<s:url value='/images/buildnow.gif' includeParams="none"/>" alt="<s:text name="legend.buildNow"/>" title="<s:text name="legend.buildNow"/>" border="0">
                  </s:a>
                </s:if>
                <s:else>
                  <s:url id="cancelBuildProjectUrl" action="cancelBuild" namespace="/" includeParams="none">
                    <s:param name="projectId" value="#attr['project'].id"/>
                    <s:param name="projectGroupId" value="#attr['project'].projectGroupId"/>
                  </s:url>
                    <s:if test="#attr['project'].state != 8">
                      <s:a href="%{cancelBuildProjectUrl}">
                        <img src="<s:url value='/images/cancelbuild.gif' includeParams="none"/>" alt="<s:text name="legend.cancelBuild"/>" title="<s:text name="legend.cancelBuild"/>" border="0">
                      </s:a>
                    </s:if>
                    <s:else>
                      <img src="<s:url value='/images/cancelbuild_disabled.gif' includeParams="none"/>" alt="<s:text name="legend.cancelBuild"/>" title="<s:text name="legend.cancelBuild"/>" border="0">
                    </s:else>
                </s:else>
            </redback:ifAuthorized>
            <redback:elseAuthorized>
                <s:if test="#attr['projectIdle']">
                  <img src="<s:url value='/images/buildnow_disabled.gif' includeParams="none"/>" alt="<s:text name="legend.buildNow"/>" title="<s:text name="legend.buildNow"/>" border="0">
                </s:if>
                <s:else>
                  <img src="<s:url value='/images/cancelbuild_disabled.gif' includeParams="none"/>" alt="<s:text name="legend.cancelBuild"/>" title="<s:text name="legend.cancelBuild"/>" border="0">
                </s:else>
            </redback:elseAuthorized>
          </s:else>
      </ec:column>
      <ec:column property="buildHistoryAction" title="&nbsp;" width="1%">
        <redback:ifAuthorized permission="continuum-view-group" resource="${projectGroupName}">
          <s:if test="#attr['project'].latestBuildId > 0">
            <s:url id="buildResultsUrl" action="buildResults" namespace="/">
              <s:param name="projectId" value="#attr['project'].id"/>
              <s:param name="projectName" value="#attr['project'].name"/>
            </s:url>
            <s:a href="%{buildResultsUrl}"><img src="<s:url value='/images/buildhistory.gif' includeParams="none"/>" alt="<s:text name="legend.buildHistory"/>" title="<s:text name="legend.buildHistory"/>" border="0"></s:a>
          </s:if>
          <s:else>
            <img src="<s:url value='/images/buildhistory_disabled.gif' includeParams="none"/>" alt="<s:text name="legend.buildHistory"/>" title="<s:text name="legend.buildHistory"/>" border="0">
          </s:else>
        </redback:ifAuthorized>
        <redback:elseAuthorized>
          <img src="<s:url value='/images/buildhistory_disabled.gif' includeParams="none"/>" alt="<s:text name="legend.buildHistory"/>" title="<s:text name="legend.buildHistory"/>" border="0">
        </redback:elseAuthorized>
      </ec:column>
      <ec:column property="workingCopyAction" title="&nbsp;" width="1%">
        <redback:ifAuthorized permission="continuum-view-group" resource="${projectGroupName}">
          <s:if test="#attr['project'].state in {10, 2, 3, 4, 6}">
            <s:url id="workingCopyUrl" action="workingCopy" namespace="/">
              <s:param name="projectId" value="#attr['project'].id"/>
            </s:url>
            <s:a href="%{workingCopyUrl}"><img src="<s:url value='/images/workingcopy.gif' includeParams="none"/>" alt="<s:text name="legend.workingCopy"/>" title="<s:text name="legend.workingCopy"/>" border="0"></s:a>
          </s:if>
          <s:else>
            <img src="<s:url value='/images/workingcopy_disabled.gif' includeParams="none"/>" alt="<s:text name="legend.workingCopy"/>" title="<s:text name="legend.workingCopy"/>" border="0">
          </s:else>
        </redback:ifAuthorized>
        <redback:elseAuthorized>
          <img src="<s:url value='/images/workingcopy_disabled.gif' includeParams="none"/>" alt="<s:text name="legend.workingCopy"/>" title="<s:text name="legend.workingCopy"/>" border="0">
        </redback:elseAuthorized>
      </ec:column>
      <ec:column property="releaseAction" title="&nbsp;" width="1%" sortable="false">
        <redback:ifAuthorized permission="continuum-build-group" resource="${projectGroupName}">
          <s:if test="#attr['project'].state == 2 && #attr['project'].projectType == 'maven2'">
            <s:url id="releaseProjectUrl" action="releasePromptGoal" namespace="/">
              <s:param name="projectId" value="#attr['project'].id"/>
            </s:url>
            <s:a href="%{releaseProjectUrl}">
              <img src="<s:url value='/images/releaseproject.gif' includeParams="none"/>" alt="<s:text name="legend.release"/>" title="<s:text name="legend.release"/>" border="0"/>
            </s:a>
          </s:if>
          <s:else>
            <img src="<s:url value='/images/releaseproject_disabled.gif' includeParams="none"/>" alt="<s:text name="legend.release"/>" title="<s:text name="legend.release"/>" border="0"/>
          </s:else>
        </redback:ifAuthorized>
        <redback:elseAuthorized>
          <img src="<s:url value='/images/releaseproject_disabled.gif' includeParams="none"/>" alt="<s:text name="legend.release"/>" title="<s:text name="legend.release"/>" border="0">
        </redback:elseAuthorized>
      </ec:column>
      <ec:column property="deleteAction" title="&nbsp;" width="1%" sortable="false">
        <redback:ifAuthorized permission="continuum-modify-group" resource="${projectGroupName}">
          <s:if test="#attr['projectIdle']">
            <s:set var="tname" value="'delProjectToken' + #attr['project'].id" scope="page"/>
            <s:token name="%{#attr['tname']}"/>
            <s:url id="deleteProjectUrl" action="deleteProject_default" namespace="/">
              <s:param name="projectId" value="#attr['project'].id"/>
              <s:param name="struts.token.name" value="#attr['tname']"/>
              <s:param name="%{#attr['tname']}" value="#session['struts.tokens.' + #attr['tname']]"/>
            </s:url>
            <s:a href="%{deleteProjectUrl}">
              <img src="<s:url value='/images/delete.gif' includeParams="none"/>" alt="<s:text name="delete"/>" title="<s:text name="delete"/>" border="0">
            </s:a>
          </s:if>
          <s:else>
            <img src="<s:url value='/images/delete_disabled.gif' includeParams="none"/>" alt="<s:text name="delete"/>" title="<s:text name="delete"/>" border="0">
          </s:else>
        </redback:ifAuthorized>
        <redback:elseAuthorized>
          <img src="<s:url value='/images/delete_disabled.gif' includeParams="none"/>" alt="<s:text name="delete"/>" title="<s:text name="delete"/>" border="0">
        </redback:elseAuthorized>
      </ec:column>
    </ec:row>
  </ec:table>
  <s:if test="projects.size() > 0">
    <div class="functnbar3">
      <table>
        <tbody>
          <tr>
            <td>
              <redback:ifAuthorized permission="continuum-modify-group" resource="${projectGroupName}">
                <s:select theme="simple" name="buildDef" list="buildDefinitions"
                           listKey="value" listValue="key" headerKey="-1" headerValue="%{getText('projectGroup.buildDefinition.label')}"
                           onchange="$('projectsForm').buildDefinitionId.value=$('buildDef').value" />
                <input type="button" name="build-projects" value="<s:text name="projectGroup.buildProjects"/>" onclick="$('projectsForm').methodToCall.value='build';document.forms.projectsForm.submit();" />
                <input type="button" name="cancel-builds" value="<s:text name="projectGroup.cancelBuilds"/>" onclick="document.forms.projectsForm.action='cancelBuilds.action';document.forms.projectsForm.submit();" />
                <input type="button" name="delete-projects" value="<s:text name="projectGroup.deleteProjects"/>" onclick="document.forms.projectsForm.methodToCall.value='confirmRemove';document.forms.projectsForm.submit();" />
              </redback:ifAuthorized>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </s:if>
  </s:form>
</s:if>
</s:i18n>
