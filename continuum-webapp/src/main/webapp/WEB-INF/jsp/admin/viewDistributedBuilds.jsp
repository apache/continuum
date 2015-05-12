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

<html>
  <s:i18n name="localization.Continuum">
    <head>
      <title><s:text name="distributedBuilds.page.title"/></title>
      <meta http-equiv="refresh" content="60"/>
    </head>
    <body>

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
      
        <div id="h3">
          <h3><s:text name="distributedBuilds.currentBuild.section.title"/></h3>
          <s:if test="currentDistributedBuilds.size() > 0">
            <s:set name="currentDistributedBuilds" value="currentDistributedBuilds" scope="request"/>
            <ec:table items="currentDistributedBuilds"
                      var="currentBuild"
                      autoIncludeParameters="false"
                      showExports="false"
                      showPagination="false"
                      showStatusBar="false"
                      sortable="false"
                      filterable="false">
              <ec:row>
                <ec:column property="projectUrl" title="distributedBuild.table.projectName">
                  <s:url id="viewUrl" action="buildResults">
                    <s:param name="projectId" value="#attr.currentBuild.projectId"/>
                  </s:url>
                  <s:a href="%{viewUrl}"><s:property value="#attr.currentBuild.projectName"/></s:a>
                </ec:column>
                <ec:column property="buildDefinitionLabel" title="distributedBuild.table.buildDefinitionLabel"/>
                <ec:column property="projectGroupName" title="distributedBuild.table.projectGroupName"/>
                <ec:column property="triggeredBy" title="buildQueue.triggeredBy" />
                <ec:column property="buildAgentUrl" title="distributedBuild.table.buildAgentUrl"/>
                <ec:column property="cancelEntry" title="&nbsp;" width="1%">
                  <s:url id="cancelUrl" action="cancelDistributedBuild" namespace="/">
                    <s:param name="buildAgentUrl" value="#attr.currentBuild.buildAgentUrl"/>
                  </s:url>
                  <redback:ifAuthorized permission="continuum-manage-queues">
                    <s:a href="%{cancelUrl}"><img src="<s:url value='/images/cancelbuild.gif' includeParams="none"/>" alt="<s:text name='cancel'/>" title="<s:text name='cancel'/>" border="0"></s:a>
                  </redback:ifAuthorized>
                  <redback:elseAuthorized>
                    <img src="<s:url value='/images/cancelbuild_disabled.gif' includeParams="none"/>" alt="<s:text name='cancel'/>" title="<s:text name='cancel'/>" border="0">
                  </redback:elseAuthorized>
                </ec:column>
              </ec:row>
            </ec:table>
          </s:if>
          <s:else>
            <s:text name="distributedBuilds.no.currentTasks"/>
          </s:else>
        </div>
      
      <s:form id="removeBuildForm" action="removeDistributedBuildEntries" method="post" theme="simple">
        <div id="h3">
          <h3>
            <s:text name="distributedBuilds.buildQueue.section.title"/>
          </h3>
          <s:if test="distributedBuildQueues.size() > 0">
            <s:set name="distributedBuildQueues" value="distributedBuildQueues" scope="request"/>
            <ec:table items="distributedBuildQueues"
                      autoIncludeParameters="false"
                      var="buildQueue"
                      showExports="false"
                      showPagination="false"
                      showStatusBar="false"
                      sortable="false"
                      filterable="false">
              <ec:row>
                <redback:ifAuthorized permission="continuum-manage-queues">
                  <ec:column alias="selectedBuildTaskHashCodes" title=" " style="width:5px" filterable="false" sortable="false" headerCell="selectAll">
                    <input type="checkbox" name="selectedBuildTaskHashCodes" value="${pageScope.buildQueue.hashCode}" />
                  </ec:column>              
                </redback:ifAuthorized>
                <ec:column property="projectUrl" title="distributedBuild.table.projectName">
                  <s:url id="viewUrl" action="buildResults">
                    <s:param name="projectId" value="#attr.buildQueue.projectId"/>
                  </s:url>
                  <s:a href="%{viewUrl}"><s:property value="#attr.buildQueue.projectName"/></s:a>
                </ec:column>
                <ec:column property="buildDefinitionLabel" title="distributedBuild.table.buildDefinitionLabel"/>
                <ec:column property="projectGroupName" title="distributedBuild.table.projectGroupName"/>
                <ec:column property="triggeredBy" title="buildQueue.triggeredBy" />
                <ec:column property="buildAgentUrl" title="distributedBuild.table.buildAgentUrl"/>
                <ec:column property="cancelEntry" title="&nbsp;" width="1%">
                  <redback:ifAuthorized permission="continuum-manage-queues">
                    <s:url id="cancelUrl" action="removeDistributedBuildEntry" namespace="/">
                      <s:param name="projectId" value="#attr.buildQueue.projectId"/>
                      <s:param name="buildDefinitionId" value="#attr.buildQueue.buildDefinitionId"/>
                      <s:param name="buildAgentUrl" value="#attr.buildQueue.buildAgentUrl"/>
                    </s:url>
                    <s:a href="%{cancelUrl}"><img src="<s:url value='/images/cancelbuild.gif' includeParams="none"/>" alt="<s:text name='cancel'/>" title="<s:text name='cancel'/>" border="0"></s:a>
                  </redback:ifAuthorized>
                  <redback:elseAuthorized>
                    <img src="<s:url value='/images/cancelbuild_disabled.gif' includeParams="none"/>" alt="<s:text name='cancel'/>" title="<s:text name='cancel'/>" border="0">
                  </redback:elseAuthorized>
                </ec:column>
              </ec:row>
            </ec:table>
          </s:if>
        </div>
        <s:if test="distributedBuildQueues.size() > 0">
          <div class="functnbar3">
            <table>
              <tbody>
                <tr>
                  <td>
                    <input type="button" name="remove-build-queues" value="<s:text name="distributedBuilds.removeEntries"/>" onclick="document.forms.removeBuildForm.submit();" /> 
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </s:if>
        <s:else>
          <s:text name="distributedBuilds.empty"/>
        </s:else>
      </s:form>
      
        <div id="h3">
          <h3><s:text name="distributedBuilds.currentPrepareBuild.section.title"/></h3>
          <s:if test="currentDistributedPrepareBuilds.size() > 0">
            <s:set name="currentDistributedPrepareBuilds" value="currentDistributedPrepareBuilds" scope="request"/>
            <ec:table items="currentDistributedPrepareBuilds"
                      var="currentPrepareBuild"
                      autoIncludeParameters="false"
                      showExports="false"
                      showPagination="false"
                      showStatusBar="false"
                      sortable="false"
                      filterable="false">
              <ec:row>
                <ec:column property="projectGroupUrl" title="distributedPrepareBuild.table.projectGroupName">
                  <s:url id="viewUrl" action="projectGroupSummary">
                    <s:param name="projectGroupId" value="#attr.currentPrepareBuild.projectGroupId"/>
                  </s:url>
                  <s:a href="%{viewUrl}"><s:property value="#attr.currentPrepareBuild.projectGroupName"/></s:a>
                </ec:column>
                <ec:column property="scmRootAddress" title="distributedPrepareBuild.table.scmRootAddress"/>
                <ec:column property="buildAgentUrl" title="distributedPrepareBuild.table.buildAgentUrl"/>
              </ec:row>
            </ec:table>
          </s:if>
          <s:else>
            <s:text name="distributedPrepareBuilds.no.currentTasks"/>
          </s:else>
        </div>
      
      
      <s:form id="removePrepareBuildForm" action="removeDistributedPrepareBuildEntries" method="post" theme="simple">
        <div id="h3">
          <h3>
            <s:text name="distributedBuilds.prepareBuildQueue.section.title"/>
          </h3>
          <s:if test="distributedPrepareBuildQueues.size() > 0">
            <s:set name="distributedPrepareBuildQueues" value="distributedPrepareBuildQueues" scope="request"/>
            <ec:table items="distributedPrepareBuildQueues"
                      var="prepareBuildQueue"
                      autoIncludeParameters="false"
                      showExports="false"
                      showPagination="false"
                      showStatusBar="false"
                      sortable="false"
                      filterable="false">
              <ec:row>
                <redback:ifAuthorized permission="continuum-manage-queues">
                  <ec:column alias="selectedPrepareBuildTaskHashCodes" title="&nbsp;" style="width:5px" filterable="false" sortable="false" width="1%" headerCell="selectAll">
                    <input type="checkbox" name="selectedPrepareBuildTaskHashCodes" value="${pageScope.prepareBuildQueue.hashCode}" />
                  </ec:column>              
                </redback:ifAuthorized>
                <ec:column property="projectGroupUrl" title="distributedPrepareBuild.table.projectGroupName">
                  <s:url id="viewUrl" action="projectGroupSummary">
                    <s:param name="projectGroupId" value="#attr.prepareBuildQueue.projectGroupId"/>
                  </s:url>
                  <s:a href="%{viewUrl}"><s:property value="#attr.prepareBuildQueue.projectGroupName"/></s:a>
                </ec:column>
                <ec:column property="scmRootAddress" title="distributedPrepareBuild.table.scmRootAddress"/>
                <ec:column property="buildAgentUrl" title="distributedPrepareBuild.table.buildAgentUrl"/>
                <ec:column property="cancelEntry" title="&nbsp;" width="1%">
                  <redback:ifAuthorized permission="continuum-manage-queues">
                    <s:url id="cancelUrl" action="removeDistributedPrepareBuildEntry" namespace="/">
                      <s:param name="projectGroupId" value="#attr.prepareBuildQueue.projectGroupId"/>
                      <s:param name="scmRootId" value="#attr.prepareBuildQueue.scmRootId"/>
                      <s:param name="buildAgentUrl" value="#attr.prepareBuildQueue.buildAgentUrl"/>
                    </s:url>
                    <s:a href="%{cancelUrl}"><img src="<s:url value='/images/cancelbuild.gif' includeParams="none"/>" alt="<s:text name='cancel'/>" title="<s:text name='cancel'/>" border="0"></s:a>
                  </redback:ifAuthorized>
                  <redback:elseAuthorized>
                    <img src="<s:url value='/images/cancelbuild_disabled.gif' includeParams="none"/>" alt="<s:text name='cancel'/>" title="<s:text name='cancel'/>" border="0">
                  </redback:elseAuthorized>
                </ec:column>
              </ec:row>
            </ec:table>
          </s:if>
        </div>
        <s:if test="distributedPrepareBuildQueues.size() > 0">
          <div class="functnbar3">
            <table>
              <tbody>
                <tr>
                  <td>
                    <input type="button" name="remove-prepare-build-queues" value="<s:text name="distributedPrepareBuilds.removeEntries"/>" onclick="document.forms.removePrepareBuildForm.submit();" /> 
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </s:if>
        <s:else>
          <s:text name="distributedPrepareBuilds.empty"/>
        </s:else>
      </s:form>
      
    </body>
  </s:i18n>
</html>
