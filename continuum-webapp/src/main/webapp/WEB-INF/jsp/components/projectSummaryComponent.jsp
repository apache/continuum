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
<%@ taglib uri="continuum" prefix="c1" %>
<%@ taglib uri="http://plexus.codehaus.org/redback/taglib-1.0" prefix="redback" %>

<ww:i18n name="localization.Continuum">
<ww:if test="${not empty projects}">

  <h3><ww:text name="projectGroup.buildsStatut.title"/></h3>
  <table>
    <tr>
      <td>
          <ww:text name="projectGroup.buildsStatut.success"/> : ${groupSummary.numSuccesses}
          &nbsp;<img src="<ww:url value='/images/icon_success_sml.gif'/>" alt="<ww:text name="projectGroup.buildsStatut.success"/>">    
          &nbsp; <ww:text name="projectGroup.buildsStatut.errors"/> : ${groupSummary.numErrors}
          &nbsp;<img src="<ww:url value='/images/icon_error_sml.gif'/>" alt="<ww:text name="projectGroup.buildsStatut.errors"/>">
          &nbsp; <ww:text name="projectGroup.buildsStatut.failures"/> : ${groupSummary.numFailures}
          &nbsp;<img src="<ww:url value='/images/icon_warning_sml.gif'/>" alt="<ww:text name="projectGroup.buildsStatut.failures"/>">
      <td>      
    </tr>
  </table>

  <h3><ww:text name="projectGroup.projects.title"/></h3>

  <form id="projectsForm" action="ProjectsList.action" method="post">
    <input type="hidden" name="methodToCall" value="" />
    <input type="hidden" id="buildDefinitionId" name="buildDefinitionId" />
  <ec:table items="projects"
            var="project"
            showExports="false"
            showPagination="false"
            showStatusBar="false"
            sortable="false"
            filterable="false">
    <ec:row highlightRow="true">

      <%-- needed to access project in included pages --%>
      <c:set var="project" value="${pageScope.project}" scope="request"/>

      <%-- placed here for reusability --%>
      <c:set var="projectIdle" value="${!project.inBuildingQueue and !project.inCheckoutQueue and ( ( ( project.state gt 0 ) and ( project.state lt 5 ) ) or project.state == 10 ) }" scope="request"/>

      <redback:ifAuthorized permission="continuum-modify-group" resource="${projectGroupName}">
        <ec:column alias="checkbox" title=" " style="width:5px" filterable="false" sortable="false" width="1%">
          <input type="checkbox" name="selectedProjects" value="${project.id}" />
        </ec:column>
      </redback:ifAuthorized>
      <ec:column property="state" title="&nbsp;" width="1%" cell="org.apache.maven.continuum.web.view.StateCell"/>
      <ec:column property="name" title="summary.projectTable.name" width="50%">
        <ww:url id="projectUrl" action="projectView" namespace="/" includeParams="none">
          <ww:param name="projectId" value="${project.id}"/>
        </ww:url>
        <ww:a href="%{projectUrl}">${pageScope.project.name}</ww:a>
      </ec:column>
      <ec:column property="version" title="summary.projectTable.version" width="12%"/>
      <ec:column property="buildNumber" title="summary.projectTable.build" width="2%" style="text-align: center">
        <c:choose>
          <c:when test="${project.buildNumber gt 0}">
            <redback:ifAuthorized permission="continuum-view-group" resource="${projectGroupName}">
              <ww:url id="buildResult" action="buildResult">
                <ww:param name="projecGroupId" value="${project.projectGroupId}"/>
                <ww:param name="projectId" value="${project.id}"/>
                <ww:param name="projectName" value="${project.name}"/>
                <ww:param name="buildId" value="${project.buildInSuccessId}"/>
              </ww:url>
              <ww:a href="%{buildResult}">${project.buildNumber}</ww:a>
            </redback:ifAuthorized>
            <redback:elseAuthorized>
              ${project.buildNumber}
            </redback:elseAuthorized>
          </c:when>
          <c:otherwise>
            &nbsp;
          </c:otherwise>
        </c:choose>
      </ec:column>
      <ec:column property="projectGroupName" title="summary.projectTable.group" width="30%"/> 
      <ec:column property="buildNowAction" title="&nbsp;" width="1%">
        <c:choose>
          <c:when test="${project.inBuildingQueue or project.inCheckoutQueue}">
            <img src="<ww:url value='/images/inqueue.gif'/>" alt="In Queue" title="In Queue" border="0">
          </c:when>
          <c:otherwise>
            <redback:ifAuthorized permission="continuum-build-group" resource="${projectGroupName}">
              <c:choose>
                <c:when test="${projectIdle}">
                  <ww:url id="buildProjectUrl" action="buildProject" namespace="/" includeParams="none">
                    <ww:param name="projectId" value="${project.id}"/>
                    <ww:param name="projectGroupId" value="${project.projectGroupId}"/>
                    <ww:param name="fromGroupPage" value="true"/>
                  </ww:url>
                  <ww:a href="%{buildProjectUrl}">
                    <img src="<ww:url value='/images/buildnow.gif'/>" alt="Build Now" title="Build Now" border="0">
                  </ww:a>
                </c:when>
                <c:otherwise>
                  <ww:url id="cancelBuildProjectUrl" action="cancelBuild" namespace="/" includeParams="none">
                    <ww:param name="projectId" value="${project.id}"/>
                  </ww:url>
                  <ww:a href="%{cancelBuildProjectUrl}">
                    <img src="<ww:url value='/images/cancelbuild.gif'/>" alt="Cancel Build" title="Cancel Build" border="0">
                  </ww:a>
                </c:otherwise>
              </c:choose>
            </redback:ifAuthorized>
            <redback:elseAuthorized>
              <c:choose>
                <c:when test="${projectIdle}">
                  <img src="<ww:url value='/images/buildnow_disabled.gif'/>" alt="Build Now" title="Build Now" border="0">
                </c:when>
                <c:otherwise>
                  <img src="<ww:url value='/images/cancelbuild_disabled.gif'/>" alt="Cancel Build" title="Cancel Build" border="0">
                </c:otherwise>
              </c:choose>
            </redback:elseAuthorized>
          </c:otherwise>
        </c:choose>
      </ec:column>
      <ec:column property="buildHistoryAction" title="&nbsp;" width="1%">
        <redback:ifAuthorized permission="continuum-view-group" resource="${projectGroupName}">
        <c:choose>
          <c:when test="${pageScope.project.latestBuildId > 0}">
            <ww:url id="buildResultsUrl" action="buildResults" namespace="/">
              <ww:param name="projectId" value="${project.id}"/>
              <ww:param name="projectName">${project.name}</ww:param>
            </ww:url>
            <ww:a href="%{buildResultsUrl}"><img src="<ww:url value='/images/buildhistory.gif'/>" alt="Build History"
                                                 title="Build History" border="0"></ww:a>
          </c:when>
          <c:otherwise>
            <img src="<ww:url value='/images/buildhistory_disabled.gif'/>" alt="Build History" title="Build History"
                 border="0">
          </c:otherwise>
        </c:choose>
        </redback:ifAuthorized>
        <redback:elseAuthorized>
          <img src="<ww:url value='/images/buildhistory_disabled.gif'/>" alt="Build History" title="Build History"
                 border="0">
        </redback:elseAuthorized>
      </ec:column>
      <ec:column property="workingCopyAction" title="&nbsp;" width="1%">
        <redback:ifAuthorized permission="continuum-view-group" resource="${projectGroupName}">
        <c:choose>
          <c:when test="${pageScope.project.state == 10 || pageScope.project.state == 2 || pageScope.project.state == 3 || pageScope.project.state == 4 || pageScope.project.state == 6}">
            <ww:url id="workingCopyUrl" action="workingCopy" namespace="/">
              <ww:param name="projectId" value="${project.id}"/>
            </ww:url>
            <ww:a href="%{workingCopyUrl}"><img src="<ww:url value='/images/workingcopy.gif'/>" alt="Working Copy"
                                                title="Working Copy" border="0"></ww:a>
          </c:when>
          <c:otherwise>
            <img src="<ww:url value='/images/workingcopy_disabled.gif'/>" alt="Working Copy" title="Working Copy"
                 border="0">
          </c:otherwise>
        </c:choose>
        </redback:ifAuthorized>
        <redback:elseAuthorized>
          <img src="<ww:url value='/images/workingcopy_disabled.gif'/>" alt="Working Copy" title="Working Copy"
                 border="0">
        </redback:elseAuthorized>
      </ec:column>
      <ec:column property="releaseAction" title="&nbsp;" width="1%" sortable="false">
        <redback:ifAuthorized permission="continuum-build-group" resource="${projectGroupName}">
        <c:choose>
          <c:when test="${pageScope.project.state == 2}">
            <ww:url id="releaseProjectUrl" action="releasePromptGoal" namespace="/">
              <ww:param name="projectId" value="${project.id}"/>
            </ww:url>
            <ww:a href="%{releaseProjectUrl}">
              <img src="<ww:url value='/images/releaseproject.gif'/>" alt="Release Project" title="Release Project"
                border="0"/>
            </ww:a>
          </c:when>
          <c:otherwise>
            <img src="<ww:url value='/images/releaseproject_disabled.gif'/>" alt="Release Project"
              title="Release Project" border="0"/>
          </c:otherwise>
        </c:choose>
        </redback:ifAuthorized>
        <redback:elseAuthorized>
          <img src="<ww:url value='/images/releaseproject_disabled.gif'/>" alt="Delete" title="Delete" border="0">
        </redback:elseAuthorized>
      </ec:column>
      <ec:column property="deleteAction" title="&nbsp;" width="1%" sortable="false">
        <redback:ifAuthorized permission="continuum-modify-group" resource="${projectGroupName}">
        <c:choose>
          <c:when
              test="${projectIdle}">
            <ww:url id="deleteProjectUrl" value="deleteProject!default.action" namespace="/">
              <ww:param name="projectId" value="${project.id}"/>
            </ww:url>
            <ww:a href="%{deleteProjectUrl}">
              <img src="<ww:url value='/images/delete.gif'/>" alt="Delete" title="Delete" border="0">
            </ww:a>
          </c:when>
          <c:otherwise>
            <img src="<ww:url value='/images/delete_disabled.gif'/>" alt="Delete" title="Delete" border="0">
          </c:otherwise>
        </c:choose>
        </redback:ifAuthorized>
        <redback:elseAuthorized>
          <img src="<ww:url value='/images/delete_disabled.gif'/>" alt="Delete" title="Delete" border="0">
        </redback:elseAuthorized>
      </ec:column>
    </ec:row>
  </ec:table>
  <ww:if test="${not empty projects}">
    <div class="functnbar3">
      <table>
        <tbody>
          <tr>
            <td>
              <redback:ifAuthorized permission="continuum-modify-group" resource="${projectGroupName}">
                <input type="hidden" name="projectGroupId" value="${project.projectGroupId}" />
                <input type="button" name="delete-projects" value="<ww:text name="projectGroup.deleteProjects"/>" onclick="document.forms.projectsForm.methodToCall.value='remove';document.forms.projectsForm.submit();" />
                <ww:select theme="simple" name="buildDefinitionId" id="buildDefinitions" list="buildDefinitions" 
                           listKey="value" listValue="key" headerKey="-1" headerValue="%{getText('projectGroup.buildDefinition.label')}"
                           onchange="$('projectsForm').buildDefinitionId.value=$('buildDefinitions').value" />                
                <input type="button" name="build-projects" value="<ww:text name="projectGroup.buildProjects"/>" onclick="$('projectsForm').methodToCall.value='build';$('projectsForm').submit();" />
                <input type="button" name="cancel-builds" value="<ww:text name="projectGroup.cancelBuilds"/>" onclick="document.forms.projectsForm.action='cancelBuilds.action';document.forms.projectsForm.submit();" />
                <a href="#" onclick="selectAll();return false;"><ww:text name="selectAll"/></a>
                <a href="#" onclick="unselectAll();return false;"><ww:text name="unselectAll"/></a>
              </redback:ifAuthorized>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </ww:if>
  </form>
</ww:if>

<script language="javascript">
    <!--
    function selectAll()
    {
        var inputs = document.getElementsByTagName("input");
        for( var i = 0; inputs && i < inputs.length; i++ )
        {
            if( inputs[i].type == "checkbox" )
            {
                inputs[i].checked = true;
            }
        }
    }
    function unselectAll()
    {
        var inputs = document.getElementsByTagName("input");
        for( var i = 0; inputs && i < inputs.length; i++ )
        {
            if( inputs[i].type == "checkbox" )
            {
                inputs[i].checked = false;
            }
        }
    }
    -->
</script>
</ww:i18n>
