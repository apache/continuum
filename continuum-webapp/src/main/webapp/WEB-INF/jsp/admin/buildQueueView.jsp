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
      <title><s:text name="buildQueue.page.title"/></title>
      <meta http-equiv="refresh" content="60"/>
    </head>
    <body>
      
        <div id="h3">
          <h3>
            <s:text name="buildQueue.currentTask.section.title"/>
          </h3>
          <s:if test="currentBuildProjectTasks.size() > 0">
            <s:set name="currentBuildProjectTasks" value="currentBuildProjectTasks" scope="request"/>
            <ec:table items="currentBuildProjectTasks"
                      autoIncludeParameters="false"
                      var="queue"
                      showExports="false"
                      showPagination="false"
                      showStatusBar="false"
                      sortable="false"
                      filterable="false">
              <ec:row>
                <ec:column property="name" title="buildQueue.currentTask.buildQueue" width="29%"/>
                <ec:column property="projectUrl" title="buildQueue.currentTask.projectName" width="50%">
                  <s:url var="viewUrl" action="buildResults">
                    <s:param name="projectId" value="#attr.queue.task.projectId"/>
                  </s:url>
                  <s:a href="%{viewUrl}"><s:property value="#attr.queue.task.projectName"/></s:a>
                </ec:column>
                <ec:column property="task.buildTrigger.triggeredBy" title="buildQueue.triggeredBy" />
                <ec:column property="task.buildDefinitionLabel" title="buildQueue.currentTask.buildDefinition" width="19%"/>
                <ec:column property="cancelAction" title="&nbsp;" width="1%">
                  <redback:ifAuthorized permission="continuum-manage-queues">
                    <s:url id="cancelUrl" action="cancelCurrentBuildTask" namespace="/">
                      <s:param name="projectId" value="#attr.queue.task.projectId"/>
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
          <s:else>
            <s:text name="buildQueue.no.currentTaks" />
          </s:else>
        </div>
      
      
      <s:form id="removeBuildForm" action="removeBuildQueueEntries" method="post" theme="simple">
        <div id="h3">
          <h3>
            <s:text name="buildQueue.section.title"/>
          </h3>
          <s:if test="buildsInQueue.size() > 0">
            <s:set name="buildsInQueue" value="buildsInQueue" scope="request"/>
            <ec:table items="buildsInQueue"
                      var="queue"
                      autoIncludeParameters="false"
                      showExports="false"
                      showPagination="false"
                      showStatusBar="false"
                      sortable="false"
                      filterable="false">
              <ec:row>
                <redback:ifAuthorized permission="continuum-manage-queues">
                  <ec:column alias="selectedBuildTaskHashCodes" title=" " style="width:5px" filterable="false" sortable="false" headerCell="selectAll">
                    <input type="checkbox" name="selectedBuildTaskHashCodes" value="${queue.task.hashCode}" />
                  </ec:column>
                </redback:ifAuthorized>
                <ec:column property="name" title="buildQueue.currentTask.buildQueue" width="29%"/>
                <ec:column property="projectUrl" title="buildQueue.currentTask.projectName" width="50%">
                  <s:url var="viewUrl" action="buildResults">
                    <s:param name="projectId" value="#attr.queue.task.projectId"/>
                  </s:url>
                  <s:a href="%{viewUrl}"><s:property value="#attr.queue.task.projectName"/></s:a>
                </ec:column>
                <ec:column property="task.buildTrigger.triggeredBy" title="buildQueue.triggeredBy" />
                <ec:column property="task.buildDefinitionLabel" title="buildQueue.currentTask.buildDefinition" width="19%"/>
                <ec:column property="cancelAction" title="&nbsp;" width="1%">
                  <redback:ifAuthorized permission="continuum-manage-queues">
                    <s:url id="cancelUrl" action="removeBuildQueueEntry" namespace="/">
                      <s:param name="projectId" value="#attr.queue.task.projectId"/>
                      <s:param name="buildDefinitionId" value="#attr.queue.task.buildDefinitionId"/>
                      <s:param name="trigger" value="#attr.queue.task.buildTrigger.trigger"/>
                      <s:param name="projectName" value="#attr.queue.task.projectName"/>
                      <s:param name="projectGroupId" value="#attr.queue.task.projectGroupId"/>
                    </s:url>
                    <s:a href="%{cancelUrl}">
                      <img src="<s:url value='/images/cancelbuild.gif' includeParams="none"/>" alt="<s:text name='cancel'/>" title="<s:text name='cancel'/>" border="0">
                    </s:a>
                  </redback:ifAuthorized>
                  <redback:elseAuthorized>
                    <img src="<s:url value='/images/cancelbuild_disabled.gif' includeParams="none"/>" alt="<s:text name='cancel'/>" title="<s:text name='cancel'/>" border="0">
                  </redback:elseAuthorized>
                </ec:column>
              </ec:row>
            </ec:table>
          </s:if>
          <s:else>
            <s:text name="buildQueue.empty"/>
          </s:else>
        </div>
        <s:if test="buildsInQueue.size() > 0">
          <div class="functnbar3">
            <table>
              <tbody>
                <tr>
                  <td>
                    <input type="button" value="<s:text name="buildQueue.removeEntries"/>" onclick="document.forms.removeBuildForm.submit();" />
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </s:if>
      </s:form>

      
        <%-- checkout queue --%>
        <div id="h3">
          <h3>
            <s:text name="checkoutQueue.currentTask.section.title"/>
          </h3>
          <s:if test="currentCheckoutTasks.size() > 0">
            <s:set name="currentCheckoutTasks" value="currentCheckoutTasks" scope="request"/>
            <ec:table items="currentCheckoutTasks"
                      var="queue"
                      autoIncludeParameters="false"
                      showExports="false"
                      showPagination="false"
                      showStatusBar="false"
                      sortable="false"
                      filterable="false">
              <ec:row>
                <ec:column property="name" title="checkoutQueue.currentTask.buildQueue" width="29%"/>
                <ec:column property="projectUrl" title="checkoutQueue.currentTask.projectName" width="69%">
                  <s:url id="viewUrl" action="projectView">
                    <s:param name="projectId" value="#attr.queue.task.projectId"/>
                  </s:url>
                  <s:a href="%{viewUrl}"><s:property value="#attr.queue.task.projectName"/></s:a>
                </ec:column>
                <ec:column property="cancelAction" title="&nbsp;" width="1%">
                  <redback:ifAuthorized permission="continuum-manage-queues">
                    <s:url id="cancelUrl" action="cancelCurrentQueueTask" namespace="/">
                      <s:param name="projectId" value="#attr.queue.task.projectId"/>
                    </s:url>
                    <s:a href="%{cancelUrl}">
                      <img src="<s:url value='/images/cancelbuild.gif' includeParams="none"/>" alt="<s:text name='cancel'/>" title="<s:text name='cancel'/>" border="0">
                    </s:a>
                  </redback:ifAuthorized>
                  <redback:elseAuthorized>
                    <img src="<s:url value='/images/cancelbuild_disabled.gif' includeParams="none"/>" alt="<s:text name='cancel'/>" title="<s:text name='cancel'/>" border="0">
                  </redback:elseAuthorized>
                </ec:column>
              </ec:row>
            </ec:table>
          </s:if>
          <s:else>
            <s:text name="checkoutQueue.no.currentTaks" />
          </s:else>
        </div>
      
        
      <s:form id="removeCheckoutForm" action="removeCheckoutQueueEntries" method="post" theme="simple">
        <div id="h3">
          <h3>
            <s:text name="checkoutQueue.section.title"/>
          </h3>
          <s:if test="checkoutsInQueue.size() > 0">
            <s:set name="checkoutsInQueue" value="checkoutsInQueue" scope="request"/>
            <ec:table items="checkoutsInQueue"
                      var="queue"
                      autoIncludeParameters="false"
                      showExports="false"
                      showPagination="false"
                      showStatusBar="false"
                      sortable="false"
                      filterable="false">
              <ec:row>
                <redback:ifAuthorized permission="continuum-manage-queues">
                  <ec:column alias="selectedCheckOutTaskHashCodes" title=" " style="width:5px" filterable="false" sortable="false" headerCell="selectAll">
                    <input type="checkbox" name="selectedCheckOutTaskHashCodes" value="${queue.task.hashCode}" />
                  </ec:column>
                </redback:ifAuthorized>
                <ec:column property="name" title="checkoutQueue.currentTask.buildQueue" width="29%"/>
                <ec:column property="projectUrl" title="checkoutQueue.currentTask.projectName" width="69%">
                  <s:url id="viewUrl" action="projectView">
                    <s:param name="projectId" value="#attr.queue.task.projectId"/>
                  </s:url>
                  <s:a href="%{viewUrl}"><s:property value="#attr.queue.task.projectName"/></s:a>
                </ec:column>
                <ec:column property="cancelAction" title="&nbsp;" width="1%">
                  <redback:ifAuthorized permission="continuum-manage-queues">
                    <s:url id="cancelUrl" action="removeCheckoutQueueEntry" namespace="/">
                      <s:param name="projectId" value="#attr.queue.task.projectId"/>
                    </s:url>
                    <s:a href="%{cancelUrl}">
                      <img src="<s:url value='/images/cancelbuild.gif' includeParams="none"/>" alt="<s:text name='cancel'/>" title="<s:text name='cancel'/>" border="0">
                    </s:a>
                  </redback:ifAuthorized>
                  <redback:elseAuthorized>
                    <img src="<s:url value='/images/cancelbuild_disabled.gif' includeParams="none"/>" alt="<s:text name='cancel'/>" title="<s:text name='cancel'/>" border="0">
                  </redback:elseAuthorized>
                </ec:column>
              </ec:row>
            </ec:table>
          </s:if>
          <s:else>
            <s:text name="checkoutQueue.empty" />
          </s:else>
        </div>
        <s:if test="checkoutsInQueue.size() > 0">
          <div class="functnbar3">
            <table>
              <tbody>
                <tr>
                  <td>
                    <redback:ifAuthorized permission="continuum-manage-queues">
                    <input type="submit" value="<s:text name="checkoutQueue.removeEntries"/>" onclick="document.forms.removeCheckoutForm.submit();" />
                    </redback:ifAuthorized>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </s:if>
      </s:form>

      
      	<div id="h3">
          <h3><s:text name="prepareBuildQueue.currentTask.section.title"/></h3>
          <s:if test="currentPrepareBuilds.size() > 0">
            <s:set name="currentPrepareBuilds" value="currentPrepareBuilds" scope="request"/>
            <ec:table items="currentPrepareBuilds"
                      var="currentPrepareBuild"
                      autoIncludeParameters="false"
                      showExports="false"
                      showPagination="false"
                      showStatusBar="false"
                      sortable="false"
                      filterable="false">
              <ec:row>
                <ec:column property="queueName" title="prepareBuildQueue.currentTask.buildQueue" width="29%"/>
                <ec:column property="projectGroupUrl" title="prepareBuildQueue.table.projectGroupName">
                  <s:url id="viewUrl" action="projectGroupSummary">
                    <s:param name="projectGroupId" value="#attr.currentPrepareBuild.projectGroupId"/>
                  </s:url>
                  <s:a href="%{viewUrl}"><s:property value="#attr.currentPrepareBuild.projectGroupName"/></s:a>
                </ec:column>
                <ec:column property="scmRootAddress" title="prepareBuildQueue.table.scmRootAddress"/>
              </ec:row>
            </ec:table>
          </s:if>
          <s:else>
            <s:text name="prepareBuildQueue.no.currentTasks"/>
          </s:else>
        </div>
      
       
      <s:form id="removePrepareBuildForm" action="removePrepareBuildEntries" method="post" theme="simple">
        <div id="h3">
          <h3>
            <s:text name="prepareBuildQueue.section.title"/>
          </h3>
          <s:if test="prepareBuildQueues.size() > 0">
            <s:set name="prepareBuildQueues" value="prepareBuildQueues" scope="request"/>
            <ec:table items="prepareBuildQueues"
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
                <ec:column property="queueName" title="prepareBuildQueue.currentTask.buildQueue" width="29%"/>
                <ec:column property="projectGroupUrl" title="prepareBuildQueue.table.projectGroupName">
                  <s:url id="viewUrl" action="projectGroupSummary">
                    <s:param name="projectGroupId" value="#attr.prepareBuildQueue.projectGroupId"/>
                  </s:url>
                  <s:a href="%{viewUrl}"><s:property value="#attr.prepareBuildQueue.projectGroupName"/></s:a>
                </ec:column>
                <ec:column property="scmRootAddress" title="prepareBuildQueue.table.scmRootAddress"/>
                <ec:column property="cancelEntry" title="&nbsp;" width="1%">
                  <redback:ifAuthorized permission="continuum-manage-queues">
                    <s:url id="cancelUrl" action="removePrepareBuildEntry" namespace="/">
                      <s:param name="projectGroupId" value="#attr.prepareBuildQueue.projectGroupId"/>
                      <s:param name="scmRootId" value="#attr.prepareBuildQueue.scmRootId"/>
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
        <s:if test="prepareBuildQueues.size() > 0">
          <div class="functnbar3">
            <table>
              <tbody>
                <tr>
                  <td>
                    <input type="button" name="remove-prepare-build-queues" value="<s:text name="prepareBuildQueue.removeEntries"/>" onclick="document.forms.removePrepareBuildForm.submit();" /> 
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </s:if>
        <s:else>
          <s:text name="prepareBuildQueue.empty"/>
        </s:else>
      </s:form>
    </body>
  </s:i18n>
</html>
